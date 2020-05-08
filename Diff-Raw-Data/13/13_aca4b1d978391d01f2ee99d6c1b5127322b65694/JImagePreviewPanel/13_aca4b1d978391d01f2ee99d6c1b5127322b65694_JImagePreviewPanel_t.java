 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.demo.fileViewer;
 
 import java.awt.Graphics;
 import java.awt.image.BufferedImage;
 import javax.swing.JPanel;
import javax.swing.SwingUtilities;
 
 /**
  *
  * @author ECO
  */
 public class JImagePreviewPanel extends JPanel {
 
     public BufferedImage getImage() {
         return image;
     }
 
     public void setImage(BufferedImage image) {
         this.image = image;
        SwingUtilities.updateComponentTreeUI(this);
     }
     private BufferedImage image;
 
     @Override
     protected void paintComponent(Graphics g) {
         super.paintComponent(g);
 
         if (image != null) {
             final int W0=getWidth();
             final int H0=getHeight();
 
             final int imgWidth = image.getWidth(this);
             final int imgHeight = image.getHeight(this);
 
             int calcWidth;
             int calcHeight;
 
             if (imgWidth < W0 && imgHeight < H0) {
                 calcWidth = imgWidth;
                 calcHeight = imgHeight;
             } else {
                 final float scale = Math.min((float)W0 / (float)imgWidth, (float)H0 / (float)imgHeight);
                 calcWidth = (int)Math.min(imgWidth * scale, W0);
                 calcHeight = (int)Math.min(imgHeight * scale, H0);
             }
             final int X1 = (W0 - calcWidth) / 2;
             final int Y1 = (H0 - calcHeight) / 2;
 
             g.drawImage(image, X1, Y1, calcWidth, calcHeight, this);
         }
     }
 }
