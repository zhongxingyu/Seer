 /*
  * created : Jul 10, 2011
  * by : Latief
  */
 package com.secondstack.swing.panel;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import javax.swing.JPanel;
 
 /**
  *
  * @author Latief
  */
 public class JPPanelTransparency extends JPanel {
 
     private float alphaTransparency;
 
     public JPPanelTransparency() {
         super();
         setOpaque(false);
         alphaTransparency = 0.5f;
     }
 
     @Override
     protected void paintComponent(Graphics g) {
         super.paintComponent(g);
 
         Graphics2D g2d = (Graphics2D) g.create();
 
         Color colorTransparency = new Color(getBackground().getRed(), getBackground().getGreen(), getBackground().getBlue(), (int) (255 * alphaTransparency));
         g2d.setColor(colorTransparency);
         g2d.fillRect(0, 0, getWidth(), getHeight());
 
         g2d.dispose();
     }
 
     public float getAlphaTransparency() {
         return alphaTransparency;
     }
 
     public void setAlphaTransparency(float alphaTransparency) {
         this.alphaTransparency = alphaTransparency;
        repaint();
     }
 }
