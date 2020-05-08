 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.xebia.demo.javafx;
 
 import javafx.animation.Interpolator;
 import javafx.animation.KeyFrame;
 import javafx.animation.KeyValue;
 import javafx.animation.Timeline;
 import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberBinding;
import javafx.beans.binding.NumberExpression;
 import javafx.event.ActionEvent;
 import javafx.event.EventHandler;
 import javafx.scene.Group;
 import javafx.scene.Scene;
 import javafx.scene.control.Button;
 import javafx.scene.effect.DropShadow;
 import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
 import javafx.scene.shape.CubicCurveTo;
 import javafx.scene.shape.MoveTo;
 import javafx.scene.shape.Path;
import javafx.scene.shape.StrokeType;
 import javafx.stage.Stage;
 import javafx.util.Duration;
 
 /**
  *
  * @author Gerbrand van Dieijen <gerbrand@vandieijen.nl>
  */
 public class Main extends Application {
 
     private static final Duration DURATION = Duration.valueOf(1500);
     private static final Interpolator INTERPOLATOR = Interpolator.EASE_BOTH;
 
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) {
         Application.launch(Main.class, args);
     }
 
     @Override
     public void start(Stage primaryStage) {
         primaryStage.setTitle("JavaFX Demo");
         final Group root = new Group();
         final Scene scene = new Scene(root, 300, 250, Color.LIGHTGREY);
 
         Path path = new Path();
         final MoveTo startOfPath = new MoveTo(20, 20);
         path.getElements().add(startOfPath);
         path.getElements().add(new CubicCurveTo(380, 0, 380, 120, 200, 120));
         path.getElements().add(new CubicCurveTo(0, 120, 0, 240, 380, 240));
         path.setStroke(Color.DODGERBLUE);
         path.getStrokeDashArray().setAll(5d, 5d);
 
         final XebiaLogo logo = new XebiaLogo(path);
 
         Button btn = new Button("Press me");
 
         btn.setLayoutX(100);
         btn.setLayoutY(180);
         btn.setOnAction(new EventHandler<ActionEvent>() {
 
             public void handle(ActionEvent ke) {
                 logo.doSomeTransition();
             }
         });
        
         final Button blurBtn = new Button("Some shadow");
 
        
         final DropShadow dropShadow = new DropShadow();
         dropShadow.setSpread(0.34);
         dropShadow.setOffsetX(8);
         dropShadow.setOffsetY(10);
         blurBtn.setLayoutX(190);
         blurBtn.setLayoutY(180);
         blurBtn.setOnAction(new EventHandler<ActionEvent>() {
 
             boolean started = false;
             boolean effect = false;
             Timeline timeline = new Timeline();
             KeyFrame keyFrame = null;
 
             public void handle(ActionEvent ke) {
                 timeline.stop();
                 //remove previous
                 timeline.getKeyFrames().remove(keyFrame);
 
                 KeyValue keyValue;
                 if (!effect) {
                     //create a keyValue for horizontal translation of circle to the position 155px with given interpolator
                     keyValue = new KeyValue(dropShadow.radiusProperty(), 0d, Interpolator.LINEAR);
                     effect = true;
                 } else {
                     keyValue = new KeyValue(dropShadow.radiusProperty(), 20d, Interpolator.LINEAR);
                     effect = false;
                 }
 
                 //create a keyFrame with duration 4s
                 keyFrame = new KeyFrame(Duration.valueOf(4000), keyValue);
 
                 //add the keyframe to the timeline
                 timeline.getKeyFrames().add(keyFrame);
 
 
                 logo.imageView.setEffect(dropShadow);
                 timeline.play();
             }
         });
 
            Circle circle = new Circle(20,Color.web("white",0.05f));
            circle.setStrokeType(StrokeType.OUTSIDE);
            circle.setStroke(Color.web("white",0.2f));
            circle.setStrokeWidth(4f);
            circle.centerXProperty().bind(logo.translateXProperty().add(10d));
            circle.setCenterY(60d);
            //circle.setCenterX(50d);
            root.getChildren().add(circle);
            
            
            
         root.getChildren().add(path);
         root.getChildren().add(logo);
         root.getChildren().add(btn);
         root.getChildren().add(blurBtn);
 
 
         primaryStage.setScene(scene);
         primaryStage.setVisible(true);
 
     }
 }
