 /*
  * To change this license header, choose License Headers in Project Properties.
  * To change this template file, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package solvableminesweepernetbeans;
 
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 
 /**
  *
  * @author Kenric
  */
 public class Button extends JButton{
     ImageIcon F,Q,I,WF,MM; //Flag, Question mark, Identity, Wrong Flag, Missed Mine
     byte value = 0;
     /*
     0:nothing/unclicked
     1:Flag
     2:Question Mark
     */
     int x;
     int y;
     int width;
     int height;
     char beneath;
     boolean dug = false; // to check if the square has been dug up and prevent further interraction if it has.
     
     // ww = window_width, wh = window_height
     public Button(int x, int y, int scaled_width, int scaled_height, char identity){
         beneath = identity;
         this.x = x;
         this.y = y;
         this.width = scaled_width;
         this.height = scaled_height;
         switch(beneath){
             case '*':
                 I = toImageIcon("Mine.png");
                 break;
             default:
                 I = toImageIcon(beneath+".png");
                 break;
         }
         F = toImageIcon("Flag.png");
         Q = toImageIcon("QMark.png");
         WF = toImageIcon("Wrong_Flag.png");
         MM = toImageIcon("Missed_Mine.png");
     }
     
     private ImageIcon toImageIcon(String path){
        return new ImageIcon(new ImageIcon(getClass().getResource("images/"+path)).getImage().getScaledInstance(width, height, java.awt.Image.SCALE_SMOOTH));
     }
 }
