 package org.aldeon.gui;
 
 import javafx.application.Application;
 import javafx.event.Event;
 import javafx.event.EventHandler;
 import javafx.geometry.Insets;
 import javafx.geometry.Pos;
 import javafx.scene.Scene;
 import javafx.scene.control.Button;
 import javafx.scene.control.TextField;
 import javafx.scene.image.ImageView;
 import javafx.scene.layout.BorderPane;
 import javafx.scene.layout.GridPane;
 import javafx.scene.layout.StackPane;
 import javafx.scene.layout.VBox;
 import javafx.scene.paint.*;
 import javafx.scene.shape.Line;
 import javafx.scene.shape.Rectangle;
 import javafx.scene.text.Font;
 import javafx.scene.text.Text;
 import javafx.stage.Stage;
 import org.aldeon.gui.listeners.IdCreator;
 import org.aldeon.gui.listeners.OnOptionClicked;
 import org.aldeon.gui.listeners.ThreadCreator;
 import org.aldeon.gui.listeners.ThreadInspector;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Prophet
  * Date: 12.10.13
  * Time: 20:38
  * To change this template use File | Settings | File Templates.
  */
 public class GUIManager extends Application {
     private BorderPane layout;
     private VBox sidebar;
     private Stage window;
 
     //TODO: Wszystkie losowania kolorów
 
     public VBox addSidebar(int windowWidth, int windowHeight){
         VBox vbox = new VBox();
         vbox.setStyle("-fx-background-color:linear-gradient(from 0% 0% to 100% 0%, #222222, #222222 90%, #1b1b1b 100%)");
 
         StackPane stacks[] = new StackPane[5];
         Text subs[] = new Text[]{
                 new Text(""),
                 new Text("Identities"),
                 new Text("Threads"),
                 new Text("Friends"),
                 new Text("Settings")
         };
         for (Text sub : subs) {
             sub.setFont(Font.font("Verdana", 12));
             sub.setStroke(Color.web("#666666"));
             sub.setFill(Color.web("#666666"));
         }
         Rectangle options[] = new Rectangle[]{
                 new Rectangle(0.128*windowWidth, 0.1*windowHeight),
                 new Rectangle(0.128*windowWidth, 0.14*windowHeight),
                 new Rectangle(0.128*windowWidth, 0.14*windowHeight),
                 new Rectangle(0.128*windowWidth, 0.14*windowHeight),
                 new Rectangle(0.128*windowWidth, 0.14*windowHeight)
         };
         options[0].setFill(new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                 new Stop(0, Color.web("#1d1d1d")),
                 new Stop(0.9, Color.web("#1d1d1d")),
                 new Stop(1, Color.web("#1b1b1b"))));
         for (int i=1;i<options.length;i++)
             options[i].setFill(new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                     new Stop(0, Color.web("#222222")),
                     new Stop(0.9, Color.web("#222222")),
                     new Stop(1, Color.web("#1b1b1b"))));
 
         Line breaks[]=new Line[]{
                 new Line(0,0.1*windowHeight,0.128*windowWidth,0.1*windowHeight),
                 new Line(0,0.24*windowHeight,0.128*windowWidth,0.24*windowHeight),
                 new Line(0,0.38*windowHeight,0.128*windowWidth,0.38*windowHeight),
                 new Line(0,0.52*windowHeight,0.128*windowWidth,0.52*windowHeight),
                 new Line(0,0.66*windowHeight,0.128*windowWidth,0.66*windowHeight),
         };
         for(Line ln: breaks){
             ln.setStroke(Color.web("#333333"));
         }
         ImageView[] icons = new ImageView[]{
                new ImageView(getClass().getResource("\\resource\\Logo.png").toExternalForm()),
                new ImageView(getClass().getResource("\\resource\\Identities.png").toExternalForm()),
                new ImageView(getClass().getResource("\\resource\\Threads.png").toExternalForm()),
                new ImageView(getClass().getResource("\\resource\\Friends.png").toExternalForm()),
                new ImageView(getClass().getResource("\\resource\\Settings.png").toExternalForm()),
         };
         for(int i=0;i<icons.length;i++){
             icons[i].setPreserveRatio(true);
             icons[i].setFitHeight(options[i].getHeight()*0.5);
         }
         for(int i=0;i<options.length;i++){
             stacks[i]=new StackPane();
             stacks[i].getChildren().add(options[i]);
             stacks[i].setAlignment(icons[i], Pos.CENTER);
             stacks[i].getChildren().add(icons[i]);
             stacks[i].setAlignment(subs[i],Pos.BOTTOM_CENTER);
             stacks[i].getChildren().add(subs[i]);
         }
         for (StackPane stack : stacks) {
             stack.setOnMousePressed(new OnOptionClicked(stack, this));
         }
         for (int i=0; i<options.length; i++) {
             vbox.getChildren().add(stacks[i]);
             vbox.getChildren().add(breaks[i]);
         }
 
         stacks[2].getChildren().get(stacks[2].getChildren().indexOf(subs[2]));
         return vbox;
     }
 
     public void updateSidebar(){            //TODO: Update sidebara na zapalony po kliknięciu
         System.out.println("UPDATE");
         VBox newSidebar = new VBox();
         newSidebar.setStyle(sidebar.getStyle());
         newSidebar.getChildren().addAll(sidebar.getChildren());
         sidebar = newSidebar;
     }
 
     public StackPane threadMode(){
         StackPane newId= new StackPane();
         newId.setStyle("-fx-background-radius:10px; -fx-background-color:#222222;");
         newId.setPrefSize(400,50);
         TextField searchText = new TextField();
         searchText.setStyle("-fx-background-color:#1f1f1f; -fx-background-radius:5px");
         searchText.setPrefWidth(newId.getWidth() * 0.5);
         newId.setAlignment(searchText, Pos.CENTER_LEFT);
         StackPane.setMargin(searchText, new Insets(0,80,0,15));
         newId.getChildren().add(searchText);
         Button searchButton = new Button();
         searchButton.setStyle("-fx-background-color:#151515; -fx-background-radius:5px; -fx-text-fill:#006464;");
         searchButton.setFont(new Font("Verdana", 7));
         searchButton.setText("ADD THREAD");
         searchButton.setOnAction(new ThreadCreator(newId, this));
         newId.setAlignment(searchButton, Pos.CENTER_RIGHT);
         StackPane.setMargin(searchButton, new Insets(0, 10, 0, 0));
         newId.getChildren().add(searchButton);
         return newId;
     }
 
     public StackPane identityMode(String id, String hash, Paint clr){
         StackPane newId= new StackPane();
         Rectangle field = new Rectangle(130,160);
         field.setArcHeight(10);
         field.setArcWidth(10);
         field.setFill(Color.web("#222222"));
         Text nextId = new Text("NEW");
         nextId.setFont(new Font("Verdana",12));
         nextId.setFill(Color.web("#ffffff"));
         Text plus = new Text("+");
         plus.setFont(new Font("Verdana", 64));
         plus.setFill(Color.web("#006464"));
         newId.setAlignment(Pos.CENTER);
         newId.getChildren().add(field);
         newId.getChildren().add(plus);
         newId.setAlignment(nextId,Pos.TOP_CENTER);
         StackPane.setMargin(nextId,new Insets(20,0,0,0));
         newId.getChildren().add(nextId);
         newId.setOnMousePressed(new IdCreator(newId));
         return newId;
     }
 
     public GridPane setMainPane(String option){
         GridPane pane = new GridPane();
         pane.setVgap(20);
         pane.setHgap(20);
         pane.setStyle("-fx-background-color:#444444;");
         pane.setPrefSize(700,700);
         if(option=="main"){
            pane.getChildren().add(new ImageView(getClass().getResource("\\resource\\main.png").toExternalForm()));
         }else if(option=="Identities"){
             pane.setPadding(new Insets(30, 30, 30, 30));
             pane.getChildren().add(identityMode("ID","HASH",Color.web("#ffffff")));
             //i powrzucaj całą resztę dzieci
         }else if(option=="Threads"){
             pane.setPadding(new Insets(30, 0, 0, 30));
             pane.getChildren().add(threadMode());
         }else if(option=="Friends"){
 
         }else if(option=="Settings"){
 
         }else System.out.println("ERROR HAS OCURRED"+option);
         return pane;
     }
 
     public void changeMain(String option){
 
         layout=new BorderPane();
         layout.setLeft(sidebar);
         layout.setCenter(setMainPane(option));
         Scene scene = new Scene(layout);
         window.setScene(scene);
         window.show();
     }
 
     public void changeMain(GridPane pane){
         layout=new BorderPane();
         layout.setLeft(sidebar);
         layout.setCenter(pane);
         Scene scene = new Scene(layout);
         window.setScene(scene);
         window.show();
     }
 
     @Override
     public void start(Stage stage) throws Exception {
         layout = new BorderPane();
         sidebar = addSidebar(800,700);
         layout.setLeft(sidebar);
         layout.setCenter(setMainPane("main"));
         Scene scene = new Scene(layout);
         window=stage;
         window.setScene(scene);
         window.show();
     }
 }
