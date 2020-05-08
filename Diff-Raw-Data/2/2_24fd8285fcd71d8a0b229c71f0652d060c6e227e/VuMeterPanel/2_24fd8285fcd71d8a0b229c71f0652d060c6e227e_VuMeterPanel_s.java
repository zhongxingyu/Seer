 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.gyver.matrixmover.gui.component;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import javax.swing.JPanel;
 
 /**
  *
  * @author Jonas
  */
 public class VuMeterPanel extends JPanel {
     
     private float vuValue = 0;
 
     public void setVuValue(float vuValue) {
         this.vuValue = vuValue;
         this.repaint();
     }
     
     @Override
     protected void paintComponent(Graphics grphcs) {
         super.paintComponent(grphcs);
 
         Graphics2D vuGraphics = (Graphics2D) grphcs;
         
         int vuWidth = Math.round(this.getSize().width / 35F * vuValue);
         
         vuGraphics.setColor(new Color(0x292929));
         vuGraphics.fillRect(0, 0, this.getSize().width, this.getSize().height);
         
         vuGraphics.setColor(new Color(0x5D5DB3));
        vuGraphics.fillRect(0, 0, this.getSize().width - vuWidth, this.getSize().height);
         
 
     }
     
 }
