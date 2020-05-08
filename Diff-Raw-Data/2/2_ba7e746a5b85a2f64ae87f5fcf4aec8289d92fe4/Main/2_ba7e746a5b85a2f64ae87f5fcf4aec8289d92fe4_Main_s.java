 /*
  * Mohammad Juma
  * 
  * TCSS 360 - Winter 2013
  * TCSS 360 Project
  * November 11, 2013
  */
 
 import view.LoginPane;
 import view.MainPane;
 import view.RegisterPane;
 import javafx.application.Application;
 import javafx.scene.Scene;
 import javafx.stage.Stage;
 
 /**
  * Begins the program by instantiating and starting the JavaFX GUI.
  * 
  * @author Mohammad Juma
  * @version 11-11-2013
  */
 public class Main extends Application {
     
     /**
      * Private constructor, to prevent instantiation of this class.
      *
     private Main() {
         // Does nothing.
     }
     */
     
     /**
      * Used primarily within the IDE to launch the JavaFX GUI.
      * Note: This method is not required for JavaFX applications as the JavaFX Packager
      *       Tool embeds the JavaFX Launcher in the final JAR file.
      * @param args Command line arguments.
      */
     public static void main(String[] args) {
         launch(args);
     }
     
     /**
      * @param primaryStage The main stage that holds this applications GUI.
      */
     @Override
     public void start(Stage primaryStage) {
         primaryStage.setTitle("TCSS360 Project");
 
        Scene scene = new Scene(new MainPane(), 950, 600);
         primaryStage.setScene(scene);
         
         primaryStage.show();
     }
 }
