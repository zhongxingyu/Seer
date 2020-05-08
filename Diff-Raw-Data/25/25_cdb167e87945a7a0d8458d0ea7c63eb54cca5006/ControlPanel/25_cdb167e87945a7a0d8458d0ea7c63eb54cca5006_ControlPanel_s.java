 package com.greymax.vkplayer.ui.components.control.panel;
 
 import javafx.fxml.FXMLLoader;
 import javafx.scene.layout.HBox;
 import javafx.scene.layout.Priority;
 
 import java.io.IOException;
 
 public class ControlPanel extends HBox {
 
   public ControlPanel() {
     try {
      HBox root = FXMLLoader.load(getClass().getResource("../../../main/ControlPanel.fxml"));
       root.setMaxWidth(Double.MAX_VALUE);
       HBox.setHgrow(root, Priority.ALWAYS);
       this.getChildren().addAll(root);
     } catch (IOException e) {
       e.printStackTrace();
     }
   }
 }
