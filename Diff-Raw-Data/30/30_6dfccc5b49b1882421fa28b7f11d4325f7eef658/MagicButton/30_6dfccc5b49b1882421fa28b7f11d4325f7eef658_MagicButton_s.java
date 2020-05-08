 
 import java.awt.Component;
 import java.awt.Graphics;
 import javax.swing.Icon;
 
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 /**
  *
  * @author davidecker
  */
 public class MagicButton extends javax.swing.JButton{
     Icon icon;
     
     
     MagicButton(){
         icon = new Icon() {
 
             public void paintIcon(Component c, Graphics g, int x, int y) {
                 throw new UnsupportedOperationException("Not supported yet.");
             }
 
             public int getIconWidth() {
                 throw new UnsupportedOperationException("Not supported yet.");
             }
 
             public int getIconHeight() {
                 throw new UnsupportedOperationException("Not supported yet.");
             }
         };
         
     }
     
     
     
 }
