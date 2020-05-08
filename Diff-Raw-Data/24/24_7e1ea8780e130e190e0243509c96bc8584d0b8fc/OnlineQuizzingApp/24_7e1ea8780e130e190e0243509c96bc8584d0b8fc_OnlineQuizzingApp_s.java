 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.sohail.online_quizzing_app;
 
 import javafx.application.Application;
 import javafx.scene.control.Dialogs;
 import javafx.scene.control.Dialogs.DialogOptions;
 import javafx.scene.control.Dialogs.DialogResponse;
 import javafx.stage.Stage;
 
 /**
  *
  * @author sohail.alam
  */
 public class OnlineQuizzingApp extends Application {
 
     private static boolean isLoggedIn = false;
     private static OnlineQuizzingApp instance;
     private Stage stage;
 
     public OnlineQuizzingApp() {
         instance = this;
     }
 
     public static OnlineQuizzingApp getInstance() {
         return instance;
     }
 
     /**
      * The main() method is ignored in correctly deployed JavaFX application.
      * main() serves only as fallback in case the application can not be
      * launched through deployment artifacts, e.g., in IDEs with limited FX
      * support. NetBeans ignores main().
      *
      * @param args the command line arguments
      */
     public static void main(String[] args) {
         launch(args);
     }
 
     @Override
     public void start(Stage primaryStage) throws Exception {
         try {
             stage = primaryStage;
             SceneLoaders.getInstance().gotoLogin(getStage());
            primaryStage.show();
 
//            //Show dialog requesting full screen mode
//            DialogResponse response = Dialogs.showConfirmDialog(stage,
//                    "Do you want to continue with Full Screen Enabled. You may also switch between Full Screen and Normal Screen later.",
//                    "For a better visual experience, it is recommended to run this application in Full Screen Mode.",
//                    "Full Screen Access", DialogOptions.YES_NO);
//            if (response.equals(response.YES)) {
//                SceneLoaders.getInstance().doFullScreen(true, stage);
//            } else {
//                SceneLoaders.getInstance().doFullScreen(false, stage);
//            }
             
         } catch (Exception ex) {
             //Handle Exception
         }
     }
 
     /**
      * @return the isLoggedIn
      */
     public static boolean isIsLoggedIn() {
         return isLoggedIn;
     }
 
     /**
      * @param isLoggedIn the isLoggedIn to set
      */
     public static void setIsLoggedIn(boolean isLoggedIn) {
         OnlineQuizzingApp.isLoggedIn = isLoggedIn;
     }
 
     /**
      * @return the stage
      */
     public Stage getStage() {
         return stage;
     }
 }
