package simvis;

import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;


/**
 *Original Author:jewelsea,http://stackoverflow.com/users/1155209/jewelsea
 *Modified by:Tess Daughton
 **/


public class EarthViewer implements EventHandler
{

  private final double MINI_EARTH_RADIUS;
  private final double LARGE_EARTH_RADIUS;

  private static final double VIEWPORT_SIZE = 800;
  private static final double ROTATE_SECS = 30;

  private static final double MAP_WIDTH = 8192 / 2d;
  private static final double MAP_HEIGHT = 4092 / 2d;
  private static final double SCALE_DELTA = 1.1;
  private double mousePosX = 0, mousePosY = 0, mouseOldX = 0, mouseOldY = 0;
  private double zoomPosition = 0;


  private static final String DIFFUSE_MAP = "DIFFUSE_MAP.jpg";
  // "http://www.daidegasforum.com/images/22/world-map-satellite-day-nasa-earth.jpg";
  private static final String NORMAL_MAP = "NORMAL_MAP.jpg";
  //"http://planetmaker.wthr.us/img/earth_normalmap_flat_8192x4096.jpg";
  private static final String SPECULAR_MAP = "SPEC_MAP.jpg";
  // "http://planetmaker.wthr.us/img/earth_specularmap_flat_8192x4096.jpg";

  private final PerspectiveCamera CAMERA = new PerspectiveCamera();
  private static Scene scene;
  private static Group largeEarth;
  private static Group miniEarth;


  /**
   * Creates a 3D globe representation with rotation and zooming capabilities
   * @param smallEarthRadius  user specified size for radius of "mini" Earth
   * @param largeEarthRadius  user specified size for radius of "Large" Earth
   */
  public EarthViewer(double smallEarthRadius, double largeEarthRadius)
  {
    MINI_EARTH_RADIUS = smallEarthRadius;
    LARGE_EARTH_RADIUS = largeEarthRadius;
    largeEarth = buildScene(LARGE_EARTH_RADIUS);
    miniEarth = buildScene(MINI_EARTH_RADIUS);
  }

  /**
   * Called within the EarthViewer constructor
   * Makes a Group for both the large and small "versions" of Earth
   * @param earthRadius   either the "small" or "large" user specified radius
   * @return
   */
  public Group buildScene(double earthRadius)
  {
    Sphere earth = new Sphere(earthRadius);
    earth.setTranslateX(VIEWPORT_SIZE / 2d);
    earth.setTranslateY(VIEWPORT_SIZE / 2d);

    PhongMaterial earthMaterial = new PhongMaterial();
    earthMaterial.setDiffuseMap
        (new Image(getClass().getClassLoader().getResourceAsStream(DIFFUSE_MAP), MAP_WIDTH, MAP_HEIGHT, true, true));
    earthMaterial.setBumpMap
        (new Image(getClass().getClassLoader().getResourceAsStream(NORMAL_MAP), MAP_WIDTH, MAP_HEIGHT, true, true));
    earthMaterial.setSpecularMap
        (new Image(getClass().getClassLoader().getResourceAsStream(SPECULAR_MAP), MAP_WIDTH, MAP_HEIGHT, true, true));

    earth.setMaterial(earthMaterial);

    return new Group(earth);
  }

  /**
   * Used by Client GUI to toggle between Earth Viewing Modes
   * @return  Large Earth Group to be attached in client's layout
   */
  public Group getLargeEarth()
  {
    return largeEarth;
  }


  /**
   * Used by Client GUI to toggle between Earth Viewing Modes
   * @return  Mini Earth Group to be attached in client's layout
   */
  public Group getMiniEarth()
  {
    return miniEarth;
  }

  /**
   * Runs a continuous animation of the Earth rotating around its y-axis
   * Used for Mini Earth Mode in client GUI
   */
  public void startRotate()
  {
    rotateAroundYAxis(miniEarth).play();
  }

  /**
   * Initialize and define listeners for the "large" Earth which has interactive capabilities unlike "mini" Earth
   * Defines "scrolling" around the globe
   * WILL define "zooming" into the globe (was previously defined but needs to be edited)
   */
  public void startEarth()
  {

    largeEarth.setOnMouseDragged(new EventHandler<MouseEvent>()
    {
      @Override
      public void handle(MouseEvent me)
      {
        if (me.getSceneX() > mouseOldX)
        {
          mousePosX += 1;
          largeEarth.setRotationAxis(Rotate.Y_AXIS);
          largeEarth.setRotate(mousePosX);

        }
        else if (me.getSceneX() < mouseOldX)
        {
          mousePosX -= 1;
          largeEarth.setRotationAxis(Rotate.Y_AXIS);
          largeEarth.setRotate(mousePosX);
        }
        else if (me.getSceneY() > mouseOldY)
        {
          mousePosY += 1;
          largeEarth.setRotationAxis(Rotate.X_AXIS);
          largeEarth.setRotate(mousePosY);
        }
        else if (me.getSceneY() < mouseOldY)
        {
          mousePosY -= 1;
          largeEarth.setRotationAxis(Rotate.X_AXIS);
          largeEarth.setRotate(mousePosY);
        }

        mouseOldX = me.getSceneX();
        mouseOldY = me.getSceneY();
      }
    });
    /**setTranslate can be used to zoom in and out on the world*/
    largeEarth.setOnScroll(new EventHandler<ScrollEvent>()
    {
      @Override
      public void handle(ScrollEvent me)
      {

        if (me.getDeltaY() < 0 && zoomPosition > -840)
        {
          largeEarth.setTranslateZ(zoomPosition -= 10);
        }
        else if (me.getDeltaY() > 0 && zoomPosition < 500)
        {
          largeEarth.setTranslateZ(zoomPosition += 10);
        }
        //System.out.println(String.format("deltaX: %.3f deltaY: %.3f", me.getDeltaX(), me.getDeltaY()));
        //System.out.println(zoomPosition);
      }
    });


    largeEarth.setOnMousePressed(new EventHandler<MouseEvent>()
    {
      @Override
      public void handle(MouseEvent me)
      {

      }
    });


    largeEarth.setOnKeyPressed(new EventHandler<KeyEvent>()
    {
      @Override
      public void handle(KeyEvent event)
      {
        switch (event.getCode())
        {
          case P:
            rotateAroundYAxis(largeEarth).play();
        }
      }
    });
  }

  /**
   * Creates continous cycle of our sphere object "Earth" rotating around its y_axis
   * @param node
   * @return
   */
  private RotateTransition rotateAroundYAxis(Node node)
  {
    RotateTransition rotate = new RotateTransition(Duration.seconds(ROTATE_SECS), node);
    rotate.setAxis(Rotate.Y_AXIS);
    rotate.setFromAngle(360);
    rotate.setToAngle(0);
    rotate.setInterpolator(Interpolator.LINEAR);
    rotate.setCycleCount(RotateTransition.INDEFINITE);

    return rotate;
  }


  @Override
  /**
   * NEEDS ADDITIONAL INFORMATION
   * Tess 11/28 -> I believe this is meant to handle zooming but is not being used right now.
   * Please comment if you have more info.
   */
  public void handle(Event event)
  {
    ScrollEvent e = (ScrollEvent) event;
    CAMERA.setTranslateZ(CAMERA.getTranslateZ() * 0.75f);
    event.consume();
    //event.fireEvent(scene, event);
  }
}
