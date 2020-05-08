 package wordgame;
 
import java.lang.reflect.Array;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import javafx.application.Application;
 import javafx.scene.Group;
 import javafx.scene.Scene;
import javafx.scene.control.Label;
 import javafx.scene.layout.TilePane;
 import javafx.stage.Stage;
 
 /**
  *
  * @author Adge
  */
 public class WordGame extends Application {
 
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) {
         launch(args);
     }
     private static final String[] words = new String[]{"WE", "AT", "FOR", "COME", "HERE", "HOME"};
     private static final double HEIGHT = 40;
     private static final double WIDTH = 100;
     private GameController controller;
     
     @Override
     public void start(Stage primaryStage) {
         primaryStage.setTitle("Ryan's word game");
         Group root = new Group();
         TilePane tilePane = new TilePane();
         tilePane.setPrefColumns(4);
         tilePane.setHgap(5);
         tilePane.setVgap(5);
         List<String> wordList = new ArrayList<>();
         final ArrayList<String> wordCheckList = new ArrayList<>();
         for (int index = 0; index < words.length; index++) {
             wordList.add(words[index]);
             wordList.add(words[index]);
             wordCheckList.add(words[index]);
         }
         controller = new GameController(wordCheckList);
         
         Collections.shuffle(wordList);
         
         for (String word : wordList) {
             Card card = new Card(word, WIDTH, HEIGHT);
             card.setOnMouseClicked(new CardMouseEventHandler(card, controller));
             tilePane.getChildren().add(card);
         }
         
         primaryStage.setScene(new Scene(root));
        Label titleLabel = new Label("Ryan's Word Game");
        root.getChildren().add(titleLabel);
        root.getChildren().add(tilePane);
         primaryStage.show();
     }
 }
