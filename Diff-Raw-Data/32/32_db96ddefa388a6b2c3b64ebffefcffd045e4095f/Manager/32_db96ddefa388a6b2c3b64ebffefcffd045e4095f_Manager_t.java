 package pl.tzaras.fitness.manager;
 
 import javafx.application.Application;
 import javafx.fxml.FXMLLoader;
 import javafx.scene.Parent;
 import javafx.scene.Scene;
 import javafx.stage.Stage;
 import pl.tzaras.fitness.manager.db.DatabaseEngine;
 
 /**
  *
  * @author tommi
  */
 public class Manager extends Application {
     
     @Override
     public void start(Stage stage) throws Exception {
     	DatabaseEngine dbEngine = new DatabaseEngine();
 		dbEngine.start();
 		
         Parent root = FXMLLoader.load(getClass().getResource("Manager.fxml"));
         Scene scene = new Scene(root);
        stage.setResizable(false);
         stage.setScene(scene);
         stage.setTitle("Fitness Manager");
         //scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
         stage.show();
         
         dbEngine.stop();
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
 }
