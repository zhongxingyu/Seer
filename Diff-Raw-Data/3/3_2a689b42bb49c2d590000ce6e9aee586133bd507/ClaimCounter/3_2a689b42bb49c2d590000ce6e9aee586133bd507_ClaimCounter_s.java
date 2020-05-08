 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package claimcounter;
 
 import java.net.URL;
 import java.util.Random;
 import javafx.animation.AnimationTimer;
 import javafx.application.Application;
 import javafx.fxml.FXMLLoader;
 import javafx.scene.Parent;
 import javafx.scene.Scene;
 import javafx.scene.text.Font;
 import javafx.stage.Stage;
 //import claimcounter.ClaimCounterController;
 
 /**
  *
  * @author chrismoylan
  */
 public class ClaimCounter extends Application {
     private static final Random     RND          = new Random();
     //private static final long     TIME_PERIOD  =   5000000000l; // about 5 seconds
     private static final long       TIME_PERIOD  =   60000000000l; // about a minute
     private long                    lastTimeCall = 0;
     private ClaimCounterController  controller;
     private ClaimData               data;
 
     private final AnimationTimer TIMER        = new AnimationTimer() {
         @Override
         public void handle(long l) {
             long currentNanoTime = System.nanoTime();
                 if (currentNanoTime > lastTimeCall + TIME_PERIOD) {
                     data.updateData();
 
                     controller.setCounter(data.fetch("claim_count"));
                     controller.setRadial1(data.fetch("estimates_per_hour"));
                     controller.setRadial2(data.fetch("estimates_today"));
                     controller.setRadial3(data.fetch("claims_today"));
 
                     lastTimeCall = System.nanoTime();
                 }
             }
     };
 
     private void init(Stage primaryStage) throws Exception {
 
         // Load the custom font so that the css can access it
        Font.loadFont("file:resources/fonts/Marmellata.ttf", 12);
 
         // Load the fxml and controller
         URL location = getClass().getResource("ClaimCounter.fxml");
         FXMLLoader fxmlLoader = new FXMLLoader();
         fxmlLoader.setLocation(location);
         Parent root = (Parent) fxmlLoader.load(location.openStream());
 
         // Store controller on the instance so that it can be accessed
         controller = fxmlLoader.getController();
 
         // Load data to drive the counter
         data = new ClaimData();
 
         // Set some window properties
         primaryStage.setResizable(true);
         primaryStage.setTitle("All your claim are belong to us");
 
         //Parent root = (Parent) fxmlLoader.load(location.openStream());
         primaryStage.setScene(new Scene(root));
     }
 
     //@Override
     public void play() {
         TIMER.start();
     }
 
     @Override
     public void stop() {
         TIMER.stop();
     }
 
     @Override
     public void start(Stage primaryStage) throws Exception {
         init(primaryStage);
         primaryStage.show();
         play();
     }
 
     public static void main(String[] args) { launch(args); }
 }
 
 
