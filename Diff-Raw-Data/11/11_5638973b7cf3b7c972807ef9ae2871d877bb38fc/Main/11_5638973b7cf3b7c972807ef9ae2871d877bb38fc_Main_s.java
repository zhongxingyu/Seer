 package no.rokaas;
 
 import javafx.application.Application;
 import javafx.event.ActionEvent;
 import javafx.event.EventHandler;
 import javafx.geometry.Insets;
 import javafx.geometry.Pos;
 import javafx.scene.Scene;
 import javafx.scene.control.Label;
 import javafx.scene.control.TextField;
 import javafx.scene.layout.GridPane;
 import javafx.scene.paint.Color;
 import javafx.stage.Stage;
 
 public class Main extends Application {
     private Scene scene;
     private final static String homePage = "http://www.google.com";
 
     @Override
     public void start(Stage stage) throws Exception {
         GridPane grid = getGridPane();
         stage.setTitle("JfxBrowser");
        scene = new Scene(grid, 1200, 900, Color.GHOSTWHITE);
         stage.setScene(scene);
         scene.getStylesheets().add("/no/rokaas/Browser.css");
         stage.show();
     }
 
     private GridPane getGridPane() {
         GridPane grid = new GridPane();
         grid.setAlignment(Pos.CENTER);
         grid.setHgap(10);
         grid.setVgap(10);
         grid.setPadding(new Insets(25, 25, 25, 25));
 
         Label addressLabel = new Label("Address:");
         grid.add(addressLabel, 0, 0, 1, 1);
 
         final TextField address = new TextField();
         grid.add(address, 1, 0, 1, 1);
 
         final Browser browser = new Browser(homePage);
         address.setText(browser.getLocation());
         address.setOnAction(new EventHandler<ActionEvent>() {
             @Override
             public void handle(ActionEvent actionEvent) {
                 fixAddress(address);
                 browser.load(address.getText());
                 address.setText(browser.getLocation());
             }
         });
         grid.add(browser, 0, 1, 2, 1);
         return grid;
     }
 
     private void fixAddress(TextField address) {
         String content = address.getText();
         int numberOfDots = content.length() - content.replace(".", "").length();
         if (numberOfDots == 0) {
             // do a google search
             address.setText("http://www.google.com/search?q=" + content.replace(' ', '+'));
             return;
         } else if (numberOfDots == 1) {
             // only one . found - prepend www.
             content = "www." + content;
         }
         if (!content.startsWith("http://")) {
             address.setText("http://" + content);
         }
     }
 
     public static void main(String[] args) {
         launch(args);
     }
 }
