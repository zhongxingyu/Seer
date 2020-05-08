 package com.tngtech.internal.leapcontrol.ui;
 
 import javafx.event.EventHandler;
 import javafx.fxml.FXMLLoader;
 import javafx.fxml.JavaFXBuilderFactory;
 import javafx.scene.Parent;
 import javafx.scene.Scene;
 import javafx.stage.Stage;
 import javafx.stage.WindowEvent;
 
 import java.io.IOException;
 import java.net.URL;
 
 public class FxWindow
 {
   public FxController start(Stage primaryStage)
   {
     FXMLLoader loader = getLoader();
     Scene scene = getScene(loader);
     addStyleSheetToScene(scene);
 
     showStage(primaryStage, scene);
 
     FxController controller = loader.getController();
     attachCloseEventHandler(primaryStage, controller);
 
     return controller;
   }
 
   private FXMLLoader getLoader()
   {
     FXMLLoader fxmlLoader = new FXMLLoader();
 
     fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());
     return fxmlLoader;
   }
 
   private Scene getScene(FXMLLoader loader)
   {
     URL fxmlLocationUrl = getResourceUrl("ui/droneControl.fxml");
     loader.setLocation(fxmlLocationUrl);
 
     try
     {
       return new Scene((Parent) loader.load(fxmlLocationUrl.openStream()));
     } catch (IOException e)
     {
       throw new IllegalStateException("Error loading the FXML file", e);
     }
   }
 
   private void addStyleSheetToScene(Scene scene)
   {
     String styleSheet = getResourceUrl("ui/droneControl.css").toExternalForm();
     scene.getStylesheets().add(styleSheet);
   }
 
   private URL getResourceUrl(String location)
   {
     try
     {
       return Thread.currentThread().getContextClassLoader().getResource(location);
     } catch (NullPointerException e)
     {
       throw new IllegalStateException(String.format("Error loading resource %s", location));
     }
   }
 
   private void showStage(Stage primaryStage, Scene scene)
   {
     primaryStage.setScene(scene);
 
     primaryStage.setTitle("Drone control");
    primaryStage.setWidth(900);
    primaryStage.setHeight(700);
 
     primaryStage.show();
   }
 
   private void attachCloseEventHandler(Stage primaryStage, final FxController controller)
   {
     primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>()
     {
       @Override
       public void handle(WindowEvent windowEvent)
       {
         controller.onApplicationClose();
       }
     });
   }
 }
