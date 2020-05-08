 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.honeybadgers.flltutorial.ui.main.content.utilities;
 
 import com.honeybadgers.flltutorial.model.Option;
 import java.awt.Color;
import java.awt.Paint;
 import javax.swing.BorderFactory;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 /**
  *
  * @author chingaman
  */
 public final class OptionPanel extends JPanel implements Cloneable
 {
     public static enum OptionState
     {
         NORMAL, HIDDEN_OCCUPY, DRAGGED, DROPPED, UNOCCUPIED, CORRECT, INCORRECT
     }
     OptionState state;
     private Option option;
     JLabel description;
     public OptionPanel()
     {
         super();
         this.state = OptionState.UNOCCUPIED;
         this.description = new JLabel("");
         this.description.setVisible(true);
         this.setOpaque(true);
     }
     public OptionPanel(Option option)
     {
         super();
         this.option = option;
         this.state = OptionState.NORMAL;
         //add text area
         this.description = new JLabel(option.getDescription());
         this.description.setVisible(true);
         //add label to store picture if option contains picture
         //add all components
         this.setState(OptionState.NORMAL);
     }
 
     //TODO: create methods that will draw a button under different STATES
     public OptionPanel copy()
     {
             OptionPanel optionPanel = new OptionPanel(this.option);
             return optionPanel;
     }
     /**
      * This object method transfers in a give option, and stores it. It may not show
      * the option immediately, unless the state of the option panel is NORMAL.
      * 
      * @param option the Option object that will be stored in this OptionPanel
      */
     public void transferOption(Option option)
     {
         this.option = option;
         if(this.description != null)
         {
             this.description.setText(this.option.getDescription());
             if(this.description.getParent() != null)
             {
                 this.description.revalidate();
             }
         }
     }
     /**
      * This object method sets the state of this OptionPanel. It is mainly used for display
      * purposes.
      * 
      * @param state the enumerate type OptionPanel.OptionState which denotes the state of the panel.
      */
     public void setState(OptionState state)
     {
         this.state = state;
         switch(this.state)
         {
             case NORMAL: /*beginning state*/
                 if(this.description != null && this.description.getParent() != this)
                 {
                     this.description.setVisible(true);
                     this.add(this.description);
                 }
                 this.setOpaque(true);
                 this.setBackground(Color.YELLOW);
                 this.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                 break;
             case HIDDEN_OCCUPY:
                 this.setBorder(null);
                 this.remove(this.description);
                 this.setBackground(Color.GRAY);
                 this.setBorder(null);
                 break;
             case UNOCCUPIED:
                 if(this.description != null && this.description.getParent() == this)
                 {
                     this.remove(this.description);
                 }
                 this.setBackground(Color.LIGHT_GRAY);
                 this.setBorder(BorderFactory.createLineBorder(Color.BLACK));
             case CORRECT:
                 if(this.description != null && this.description.getParent() != this)
                 {
                     this.description.setVisible(true);
                     this.add(this.description);
                 }
                 this.setOpaque(true);
                 this.setBackground(Color.GREEN);
                 this.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                 break;
             case INCORRECT:
                 if(this.description != null && this.description.getParent() != this)
                 {
                     this.description.setVisible(true);
                     this.add(this.description);
                 }
                 this.setOpaque(true);
                 this.setBackground(Color.RED);
                 this.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                 break;
             default:
                 break;
         }
     }
     public Option getOption() {
         return option;
     }
 }
