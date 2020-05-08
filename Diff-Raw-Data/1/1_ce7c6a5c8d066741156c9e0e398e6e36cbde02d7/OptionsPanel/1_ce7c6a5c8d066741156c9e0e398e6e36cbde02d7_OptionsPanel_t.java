 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.honeybadgers.flltutorial.ui.main.content;
 
 import com.honeybadgers.flltutorial.model.Option;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.util.List;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 /**
  *
  * @author chingaman
  */
 abstract public class OptionsPanel extends JScrollPane {
     private Dimension preferedDimension = new Dimension(200, 500);
     private Dimension minDimension = new Dimension(150, 400);
     private Dimension maxDimension = new Dimension(32767, 32767);
     JPanel clickedPanel;
     List<Option> options;
     OptionsPanel(List<Option> options)
     {
         super();
         this.setPreferredSize(preferedDimension);
         this.setMinimumSize(minDimension);
         this.setMaximumSize(maxDimension);
         this.setBackground(Color.BLACK);
         this.clickedPanel = new JPanel();
         //this.clickedPanel.setPreferredSize(new Dimension(200,200));
         this.add(clickedPanel);
         this.options = options;
     }
     
 }
