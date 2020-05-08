 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package app.navigps.gui.detailspanel;
 
 import javax.swing.JLabel;
 
 /**
  *
  * @author wara
  */
public abstract class AbstractDecoratePanel extends AlphaJPanel{
 
     /**
      *
      * @param text
      */
     abstract public void setTitle(String text);
     /**
      *
      * @return
      */
     abstract public String getTitle();
     /**
      *
      * @return
      */
     abstract public JLabel getContent();
     /**
      *
      * @param lab
      */
     abstract public void setContent(JLabel lab);
 }
