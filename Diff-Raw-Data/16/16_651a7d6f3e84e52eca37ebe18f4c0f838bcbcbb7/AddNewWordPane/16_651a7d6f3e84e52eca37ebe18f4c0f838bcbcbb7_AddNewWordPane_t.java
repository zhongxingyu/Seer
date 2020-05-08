 package com.flashcard.fx.scene.logged.pane;
 
 import com.flashcard.fx.App;
 import com.flashcard.fx.scene.logged.UserScene;
 import com.flashcard.system.Service;
 import javafx.event.ActionEvent;
 import javafx.event.EventHandler;
 import javafx.geometry.Insets;
 import javafx.geometry.Pos;
 import javafx.scene.control.Button;
 import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
 import javafx.scene.layout.VBox;
 import javafx.scene.layout.GridPane;
 import javafx.scene.text.Font;
 import javafx.scene.text.FontWeight;
 import javafx.scene.text.Text;
 
 import javafx.scene.text.Text;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 import org.apache.http.client.fluent.Request;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: ksiazelobozow
  * Date: 08.06.13
  * Time: 17:03
  */
 
 @Component
 public class AddNewWordPane extends GridPane{
     private static AddNewWordPane instance;
     //private final VBox resultsBox;
     private final TextField englishWordField = new TextField();
     private final TextField polishWordField = new TextField();
     private final Button addButton = new Button("Add");
     @Autowired
     private Service service;
     @Autowired
     private UserScene userScene;
     private String imageURL = "";
 
     public AddNewWordPane(){
         init();
     }
 
     public AddNewWordPane(String wordPolish, String wordEnglish){
         init();
         englishWordField.setText(wordEnglish);
         polishWordField.setText(wordPolish);
     }
 
     public void init(){
         setAlignment(Pos.CENTER);
         setHgap(10);
         setVgap(10);
         setPadding(new Insets(25, 25, 25, 25));
 
         Text sceneTitle = new Text("Add new words");
         sceneTitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
         add(sceneTitle, 0, 0, 2, 1);
 
         Text polishTitle = new Text("Word in Polish");
         polishTitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 14));
         add(polishTitle, 0, 1, 2, 1);
 
         polishWordField.setMaxWidth(Double.MAX_VALUE);
         add(polishWordField, 0, 2, 2, 2);
 
         Text englishTitle = new Text("Word in English");
         englishTitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 14));
         add(englishTitle, 0, 4, 2, 1);
 
         englishWordField.setMaxWidth(Double.MAX_VALUE);
         add(englishWordField, 0, 5, 2, 2);
 
         addButton.setOnAction(new EventHandler<ActionEvent>() {
             @Override
             public void handle(ActionEvent keyEvent) {
                 String englishWord = englishWordField.getText();
                 String polishWord = polishWordField.getText();
 
                 try {
                     service.addNewWord(englishWord, polishWord);
                     userScene.setPane(new MessagePane("The word has been added."));
                 } catch (Exception e1) {
                     e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                 }
 
                 polishWordField.setText("");
                 englishWordField.setText("");
             }
         });
         add(addButton, 0, 7, 2, 1);
         VBox vBox = new VBox();
         final HBox imagesBox = new HBox(10);
         vBox.getChildren().add(imagesBox);
         Button button = new Button("Search for image");
         button.setOnAction(new EventHandler<ActionEvent>() {
             @Override
             public void handle(ActionEvent actionEvent) {
                 try {
                     List<String> words = Arrays.asList(englishWordField.getText());
                     for (String word : words) {
                         if (word != null && !word.equals("")) {
                            List<String> images = service.getImages(word, 5);
                             for (final String image : images) {
                                 final ImageView imageView = new ImageView(new Image(Request.Get(image).execute().returnContent().asStream()));
                                 imageView.setPreserveRatio(true);
                                 imageView.setFitHeight(80);
                                 imageView.setFitWidth(80);
                                 final HBox box = new HBox(10);
                                 imageView.setOnMouseClicked(new EventHandler<MouseEvent>() {
                                     @Override
                                     public void handle(MouseEvent mouseEvent) {
                                         String cssBordering = "-fx-opacity: 1;";
                                         for (int i = 0; i < imagesBox.getChildren().size(); i++) {
                                             imagesBox.getChildren().get(i).setStyle("-fx-opacity: 0.7");
                                         }
                                         box.setStyle(cssBordering);
                                     }
                                 });
                                 box.getChildren().add(imageView);
                                 imagesBox.getChildren().add(box);
                             }
                         }
                     }
                     App.getInstance().getPrimaryStage().sizeToScene();
                 } catch (Exception e) {
                     e.printStackTrace();  //TODO: implement body of catch statement.
                 }
             }
         });
         vBox.getChildren().add(button);
         add(vBox, 0, 8, 2, 1);
 
     }
 }
