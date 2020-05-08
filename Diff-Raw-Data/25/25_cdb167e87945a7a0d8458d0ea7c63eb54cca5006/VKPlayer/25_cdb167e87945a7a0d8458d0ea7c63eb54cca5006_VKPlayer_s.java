 package com.greymax.vkplayer;
 
 import javafx.application.Application;
 import javafx.fxml.FXMLLoader;
 import javafx.scene.Parent;
 import javafx.scene.Scene;
 import javafx.scene.image.Image;
 import javafx.stage.Stage;
 
 public class VKPlayer extends Application{
 
   @Override
   public void start(Stage stage) throws Exception {
     stage.hide();
     Parent root = FXMLLoader.load(getClass().getResource("ui/main/index.fxml"));
//    root.getStylesheets().add("css/main/controlPanel.css");
     stage.setTitle("VK Player");
     stage.setResizable(true);
     stage.getIcons().add(new Image("image/ico.png"));
    stage.setScene(new Scene(root, 650, 410));
     stage.show();
   }
 }
