 package com.xebia.demo.javafx;
 
 import javafx.animation.Animation.Status;
 import javafx.animation.PathTransition;
 import javafx.animation.PathTransition.OrientationType;
 import javafx.animation.RotateTransition;
 import javafx.animation.Timeline;
 import javafx.beans.property.DoubleProperty;
 import javafx.scene.Parent;
 import javafx.scene.effect.GaussianBlur;
 import javafx.scene.effect.PerspectiveTransform;
 import javafx.scene.effect.Reflection;
 import javafx.scene.image.Image;
 import javafx.scene.image.ImageView;
 import javafx.scene.paint.Color;
 import javafx.scene.shape.CubicCurveTo;
 import javafx.scene.shape.MoveTo;
 import javafx.scene.shape.Path;
 import javafx.util.Duration;
 
 public class XebiaLogo extends Parent {
 
     public static final double REFLECTION_SIZE = 0.25;
     public static final double WIDTH = 200;
     public static final double HEIGHT = WIDTH + (WIDTH * REFLECTION_SIZE);
     private static final double RADIUS_H = WIDTH / 2;
     private static final double BACK = WIDTH / 10;
     private PerspectiveTransform transform = new PerspectiveTransform();
     /** Angle Non-Observable Property */
     public final DoubleProperty angle = new DoubleProperty(45.0);
 
     private RotateTransition rotateTransition;
     private final PathTransition pathTransition;
     
     final ImageView imageView;
     
     public XebiaLogo(Path path) {
         String imageUrl = XebiaLogo.class.getResource("xebialogo.png").toExternalForm();
        Image image = image = new Image(imageUrl, 241, 91, true, true, false);
         // create content
         imageView = new ImageView();
         imageView.setImage(image);
         Reflection reflection = new Reflection();
         reflection.setFraction(REFLECTION_SIZE);
         imageView.setEffect(reflection);
         getChildren().add(imageView);
         angle.set(45.0);
         
         rotateTransition = new RotateTransition(Duration.valueOf(4000), this);
         rotateTransition.setFromAngle(0);
         rotateTransition.setToAngle(720);
         rotateTransition.setCycleCount(Timeline.INDEFINITE);
         rotateTransition.setAutoReverse(true);
         
 
         pathTransition = new PathTransition();
         pathTransition.setDuration(Duration.valueOf(4000));
         pathTransition.setPath(path);
         pathTransition.setNode(this);
         pathTransition.setOrientation(OrientationType.ORTHOGONAL_TO_TANGENT);
         pathTransition.setCycleCount(Timeline.INDEFINITE);
         pathTransition.setAutoReverse(true);
 
 
 
     }
     
 
     public void doSomeTransition() {
         if (pathTransition.getStatus()==Status.RUNNING) {
             pathTransition.pause();
             rotateTransition.play();
         } else {
             pathTransition.play();
             if (rotateTransition.getStatus()==Status.RUNNING)
                 rotateTransition.pause();
         }
     }
     
     public void pathTransation() {
 
     }
 }
