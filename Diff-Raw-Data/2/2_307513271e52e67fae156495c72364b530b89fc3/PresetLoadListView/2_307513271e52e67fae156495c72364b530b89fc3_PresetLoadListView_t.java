 package com.intelix.digihdmi.app.views;
 
 import com.intelix.digihdmi.util.IconImageButton;
 import javax.swing.JButton;
 import org.jdesktop.application.Application;
 
 public class PresetLoadListView extends ButtonListView {
 
     @Override
     protected void initializeHomePanel() {
         super.initializeHomePanel();
 
         IconImageButton matrixView = new IconImageButton("MatrixIconBtn");
 
         matrixView.setAction(
            Application.getInstance().getContext().getActionMap().get("showAndLoadMatrixView")
         );
         this.homePanel.add(matrixView);
     }
 }
