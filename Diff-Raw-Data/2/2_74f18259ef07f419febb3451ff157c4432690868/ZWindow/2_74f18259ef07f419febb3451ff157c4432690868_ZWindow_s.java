 package org.zjfx;
 
 import javafx.event.ActionEvent;
 import javafx.event.EventHandler;
 import javafx.fxml.FXMLLoader;
 import javafx.geometry.Orientation;
 import javafx.scene.Node;
 import javafx.scene.Scene;
 import javafx.scene.SceneBuilder;
 import javafx.scene.control.SeparatorBuilder;
 import javafx.scene.input.MouseButton;
 import javafx.scene.input.MouseEvent;
 import javafx.scene.layout.*;
 import javafx.scene.paint.Color;
 import javafx.scene.text.TextBuilder;
 import javafx.stage.Stage;
 import javafx.stage.StageStyle;
 import javafx.util.Callback;
 
 import java.io.IOException;
 
 public abstract class ZWindow<T> {
 
     private Stage stage;
     private ZModel<T> model;
     private Node view;
     private double x;
     private double y;
 
     public ZWindow(Stage stage, String title, T model) {
         this.stage = stage;
         this.model = ZModel.wrap(model);
         initStage(title);
     }
 
     public ZWindow(Stage stage, String title) {
         this(stage, title, null);
     }
 
     public ZWindow(String title) {
         this(new Stage(), title);
     }
 
     public ZModel<T> getModel() {
         return model;
     }
 
     protected void initStage(String title) {
         Stage stage = getStage();
         stage.initStyle(StageStyle.TRANSPARENT);
         stage.setHeight(300);
         stage.setWidth(400);
         stage.setTitle(title);
         stage.setScene(createScene(stage));
     }
 
     public Stage getStage() {
         return stage;
     }
 
     public Scene createScene(Stage stage) {
         BorderPane borderPane = BorderPaneBuilder
                 .create()
                 .styleClass("zwindow-main-pane")
                 .top(createHeader(stage))
                 .center(createForm())
                 .build();
         final Scene scene = SceneBuilder.create().root(borderPane).fill(Color.TRANSPARENT).build();
        scene.getStylesheets().add(getClass().getResource("zwindow.css").toExternalForm());
         return scene;
     }
 
     protected abstract Node createForm();
 
     protected Node createForm(String resource) {
         ZResourceBundle<T> bundle = new ZResourceBundle<T>(getModel());
         FXMLLoader loader = new FXMLLoader(getClass().getResource(resource), bundle, new DisplayerBuilderFactory(getModel()),
                 new Callback<Class<?>, Object>() {
                     @Override
                     public Object call(Class<?> aClass) {
                         try {
                             return aClass.newInstance();
                         } catch (InstantiationException e) {
                             throw new RuntimeException(e);
                         } catch (IllegalAccessException e) {
                             throw new RuntimeException(e);
                         }
                     }
                 });
         try {
             return view = (Node) loader.load();
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
     }
 
     protected Node createHeader(final Stage stage) {
         StackPane header = StackPaneBuilder.create().build();
         header.getChildren().add(createWindowButtons(stage));
         header.getChildren().add(createTitle(stage.getTitle()));
         return BorderPaneBuilder
                 .create()
                 .center(header)
                 .bottom(createSeparator())
                 .onMousePressed(new EventHandler<MouseEvent>() {
                     @Override
                     public void handle(MouseEvent mouseEvent) {
                         x = mouseEvent.getScreenX() - stage.getX();
                         y = mouseEvent.getScreenY() - stage.getY();
                     }
                 })
                 .onMouseDragged(new EventHandler<MouseEvent>() {
                     @Override
                     public void handle(MouseEvent mouseEvent) {
                         stage.setX(mouseEvent.getScreenX() - x);
                         stage.setY(mouseEvent.getScreenY() - y);
                     }
                 })
                 .onMouseClicked(new EventHandler<MouseEvent>() {
                     @Override
                     public void handle(MouseEvent mouseEvent) {
                         if (mouseEvent.getClickCount() == 2 && mouseEvent.getButton() == MouseButton.PRIMARY)
                             stage.setFullScreen(true);
                     }
                 })
                 .build();
     }
 
     protected Node createSeparator() {
         return SeparatorBuilder.create().styleClass("zwindow-header-separator").orientation(Orientation.HORIZONTAL).build();
     }
 
     protected Node createTitle(String title) {
         return TextBuilder.create().text(title).build();
     }
 
     protected Node createButton(String imageResource, EventHandler<ActionEvent> actionEventEventHandler) {
         return new ImageButton(imageResource, actionEventEventHandler);
     }
 
     protected Node createCloseButton(final Stage stage) {
         return createButton("close", new EventHandler<ActionEvent>() {
             @Override
             public void handle(ActionEvent actionEvent) {
                 stage.close();
             }
         });
     }
 
     protected Node createMinimizeButton(final Stage stage) {
         return createButton("minimize", new EventHandler<ActionEvent>() {
             @Override
             public void handle(ActionEvent actionEvent) {
                 stage.setIconified(true);
             }
         });
     }
 
     protected Node createMaximizeButton(final Stage stage) {
         return createButton("maximize", new EventHandler<ActionEvent>() {
             @Override
             public void handle(ActionEvent actionEvent) {
                 stage.setFullScreen(true);
             }
         });
     }
 
     protected Node createWindowButtons(final Stage stage) {
         return HBoxBuilder
                 .create()
                 .styleClass("zwindow-button-bar")
                 .children(
                         createCloseButton(stage),
                         createMinimizeButton(stage),
                         createMaximizeButton(stage)
                 )
                 .build();
     }
 }
