 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package restaurante.UI.CustomComponents;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author aluno
  */
 public class SearchButton extends restaurante.UI.CustomComponents.JButton {
 
     public SearchButton() {
         super();
         this.setText("");
         try {
            this.setIcon(new javax.swing.ImageIcon(getClass().getResource("../Images/mesa.png"))); // NOI18N
         } catch (Exception e) {
             Logger.getLogger(LayoutCreator.class.getName()).log(Level.SEVERE, null, e);
         }
        
     }
 
     @Override
     public void setText(String text) {
         super.setText("");
     }
 
 }
