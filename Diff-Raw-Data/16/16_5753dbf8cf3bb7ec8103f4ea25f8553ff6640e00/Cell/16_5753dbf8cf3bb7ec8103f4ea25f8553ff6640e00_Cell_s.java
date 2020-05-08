 import javax.swing.*;
 import java.awt.*;
 
 
 /*
  * Cell object written for amL
  * 
  * Programming Languages and Translators, Fall 2012
  * 
  * Sriramkumar Balasubramanian (sb3457)
  * Evan Drewry (ewd2106)
  * Timothy Giel (tkg2104)
  * Nikhil Helferty (nh2407)
  * 
  * Includes where the cell is, whether it has been visited,
  * the "value" of the cell (is at walkable, is it a target, etc.),
  * as well as information about displaying it in the Swing GUI.
  * 
  */
 public class Cell extends JLabel
 {
     private int row; // the row of the cell (top left is row 0, column 0)
     private int column; // the column of the cell
     private int value; // value of the cell: 0 if spot is a "hole", 1 if walkable, 2 if start point, 3 if target
     private boolean visited; // whether or not the bot has visited this point

     public Cell(int value, int r, int c)
     {
         setHorizontalAlignment(JLabel.CENTER);
         setFont(new Font("Times New Roman", Font.PLAIN, 24));
         setBorder(BorderFactory.createLineBorder(Color.BLACK));
         if (value == 2) {
             visited = true;
             setText("BOT");
         }
         else visited = false;
         if (value == 3) setText("TARGET");
         if (value == 0) setBackground(Color.BLACK);
         setOpaque(true);
         setPreferredSize(new Dimension(120, 120));
         this.value = value;
         row = r;
         column = c;
     }
     
     // is this cell the target for the bot?
     public boolean isTarget() {
         if (value == 3) return true;
         else return false;
     }
     
     // is this the source (start point of the bot)
     public boolean isSource() { 
         if (value == 2) return true;
         else return false; 
     }
     
     // returns the unique integer ID of the cell
     // unique ID calculated as follows: (number columns) * (row of cell) + column of cell
     // note that it will not work if AMLJava is not running successfully (this should not be a problem)
     public int get_Loc() {
         return AMLJava.width * (row) + column;        
     }
     
     // getter functions
     public int getRow() { return row; }
     public int getCol() { return column; }
     public int getValue() { return value; }
     public boolean getVisited() { return visited; }
     
     public void visited() { visited = true; } // set visited to true
 }
