 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package restaurante.UI.CustomComponents;
 
 import java.awt.Image;
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 
 /**
  *
  * @author Alan
  */
 public class JButton extends javax.swing.JButton {
     private Image image = null;
     private static final int MARGIN = 2;
     
     @Override
     public void setIcon(Icon defaultIcon) {
         image = ((ImageIcon)defaultIcon).getImage();
         this.setText("");
         super.setIcon(null);
     }
     
     @Override
     public void paint(java.awt.Graphics g) {
         super.paint(g);
         if(image == null) { return; }
         g.drawImage(image, MARGIN, MARGIN, getWidth() - MARGIN * 2, getHeight() - MARGIN * 2, null);
     }
 }
