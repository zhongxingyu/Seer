 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package restaurante.UI.CustomComponents;
 
 import java.awt.Image;
 import javax.swing.JComponent;
 
 /**
  *
  * @author Alan
  */
 public class JImage extends JComponent {
     Image image = null;
     
     public void setImage(Image defaultIcon) {
         image = defaultIcon;
     }
     
     @Override
     public void paint(java.awt.Graphics g) {
         super.paint(g);
         if(image == null) { return; }
         g.drawImage(image, 0, 0, getWidth(), getHeight(), null);
     }
 }
