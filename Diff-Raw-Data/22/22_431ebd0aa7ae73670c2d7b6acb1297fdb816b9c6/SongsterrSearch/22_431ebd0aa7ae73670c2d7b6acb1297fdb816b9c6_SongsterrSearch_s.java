 package com.basil.songsterr.client;
 
 
 import javafx.application.Application;
 import javafx.fxml.FXMLLoader;
 import javafx.geometry.Insets;
 import javafx.geometry.Pos;
 import javafx.scene.Parent;
 import javafx.scene.Scene;
 import javafx.scene.layout.VBoxBuilder;
 import javafx.scene.text.Text;
 import javafx.stage.Modality;
 import javafx.stage.Stage;
 
 /**
  *
  * @author Basil
  */
 public class SongsterrSearch extends Application {
     
     @Override
     public void start(Stage primaryStage) {
         Parent root = null;
         try {
            root = FXMLLoader.load(getClass()
                    .getResource("ui/SearchForm.fxml"));
         } catch (Exception ex) {
             showMessage(ex.getLocalizedMessage(), "Exception");
         }
         if (root != null) {
             Scene scene = new Scene(root, 600, 426);
            scene.getStylesheets()
                    .add(getClass()
                    .getResource("ui/searchform.css").toExternalForm());
             primaryStage.setTitle("Songsterr search");
             primaryStage.setScene(scene);
             primaryStage.show();
         }
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
     
     public static void showMessage(String message, String title) {
         Stage dialogStage = new Stage();
         dialogStage.initModality(Modality.WINDOW_MODAL);
         dialogStage.setScene(new Scene(VBoxBuilder.create()
             .children(new Text(message))
             .alignment(Pos.CENTER).padding(new Insets(5)).build()));
         dialogStage.setTitle(title);
         dialogStage.setWidth(400);
         dialogStage.setHeight(150);
         dialogStage.show();
     }
 }
