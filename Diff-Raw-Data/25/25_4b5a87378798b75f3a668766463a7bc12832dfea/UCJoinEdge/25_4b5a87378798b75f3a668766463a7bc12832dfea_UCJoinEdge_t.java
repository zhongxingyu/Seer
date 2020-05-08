 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package BT.managers.UC;
 
 import BT.managers.CoordinateManager;
 import java.awt.BasicStroke;
 import java.awt.Graphics2D;
 
 /**
  *
  * @author Karel Hala
  */
public class UCJoinEdge extends CoordinateManager{
     private CoordinateManager firstObject;
     private CoordinateManager secondObject;
     private int startX;
     private int startY;
     private int endX;
     private int endY;
     
     public UCJoinEdge ()
     {
     
     }
     
     public void setFirstObject(CoordinateManager object)
     {
         this.startX = object.getX();
         this.startY = object.getY();
         this.endX = object.getX();
         this.endY = object.getY();
         this.firstObject = object;
     }
     
     public CoordinateManager getfirstObject()
     {
         return this.firstObject;
     }
     
     public void setSecondObject(CoordinateManager object)
     {
         this.endX = object.getX();
         this.endY = object.getY();
         this.secondObject = object;
     }
     
     public CoordinateManager getSecondObject()
     {
         return this.secondObject;
     }
     
     public void setMouseCoordinates(int x, int y)
     {
         this.endX = x;
         this.endY = y;
     }
     
     public void drawJoinEdge(Graphics2D g) {
         if (this.getSecondObject() != null)
         {
             endX = secondObject.getX();
             endY = secondObject.getY();
         }
         startX = firstObject.getX();
         startY = firstObject.getY();
         
         g.setStroke(new BasicStroke(2));
         g.drawLine(startX, startY, endX, endY);
     }
     
 }
