 package Mastermind;
 
 import java.awt.*;
 import javax.swing.*;
 import java.awt.event.*;
 
 public class OutputButton extends JComponent {
     private int  currColor;
 
     public OutputButton() {
         currColor = 0;
     }
 
     public void paint(Graphics g) {
         if (currColor == 1) {
             g.setColor(Color.WHITE);
             g.fillOval(0,0,10,10);
         } else if (currColor == 2) {
             g.setColor(Color.BLACK);
             g.fillOval(0,0,10,10);
         } else {
             g.setColor(Color.BLACK);
            g.drawOval(0,0,30,30);
         }
     }
 
     public int getCurrColor() {
         return currColor;
     }
 
     public void setColor(int newColor) {
         currColor = newColor;
         repaint();
     }
 
     public Dimension getMinimumSize() {
         return new Dimension(10,10);
     }
 
     public Dimension getPreferredSize() {
         return new Dimension(10,10);
     }
 }
