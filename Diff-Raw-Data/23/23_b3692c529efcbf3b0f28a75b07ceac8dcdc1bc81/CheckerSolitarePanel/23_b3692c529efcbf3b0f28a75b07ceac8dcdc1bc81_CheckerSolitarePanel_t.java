 package checkerssolitare;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import javax.swing.JPanel;
 
 
 import java.util.*;
 
 class CheckersSolitarePanel extends JPanel {
   
 	LinkedList checkerPlacement;
 	
 	CheckersSolitarePanel(LinkedList l) {
 		checkerPlacement = l;
 	}
 	
 /*
  * Method drawVehicle(pos_x, pos_y, vehicle, direction...) 
  * displays a vehicle "vehicle" (one of 'A' through 'K', 'O' through 'R' and 'X') 
  * starting at row "pos_x", column "pos_y" in direction "direction". 
  * 
  * TODO: Needs refactoring: vehicles, colors and lengths should be 
  * coded using meaningful symbolic constants.
 */
 	
   private void drawChecker(int pos_x, int pos_y, char type, Graphics g) {
 	  if (type == 'O') g.setColor(Color.white);
 	  else if (type == 'C') g.setColor(Color.blue);
 	  
 	  int thickness = 4;
 	  
 	  g.fillOval(pos_x*thickness, pos_y*thickness, thickness, thickness);
 	  
   }
 	
   public void paintComponent(Graphics g) {
     super.paintComponent(g);
     g.setColor(Color.gray);
     
     int thickness = 4;
 
    for (int k = 0; k<7; k++) {
    	for (int l = 0; l<7; l++) {
     		g.fill3DRect(100 + k*40, 50 + l*40, 40, 40, true);
     	    for (int i = 1; i <= thickness; i++)
     	      g.draw3DRect(100 + k*40 - i, 50 + l*40 - i, 40 + 2 * i - 1, 40 + 2 * i - 1, true);
     	}
     }
     
     for (Object o : checkerPlacement) {
     	checkerssolitare.CheckerNode cNode = (checkerssolitare.CheckerNode) o;
     	drawChecker(cNode.getX(), cNode.getY(), cNode.getType(), g);
     }
     
   }
 
 }
