 package fr.iutvalence.java.mp.RollingBall;
 
 /**
  * 
  * two integers which define the point
  * 
  * @author andrejul
  * 
  */
 public class Point
 {
 
     /**
      * abscissa of the point
      */
     private int x;
 
     /**
      * ordered of the point
      */
     private int y;
 
     /**
      * point created with two defined integers
      * 
      * @param x
      *            abscissa of the point
      * @param y
      *            ordered of the point
      */
     public Point(int x, int y)
     {
         this.x = x;
         this.y = y;
     }
 
     /**
      * method to get the abscissa of the point
      * 
      * @return integer the abscissa of the point
      */
     public int getX()
     {
         return this.x;
     }
 
     /**
      * method to get the ordered of the point
      * 
      * @return integer the ordered of the point
      */
     public int getY()
     {
         return this.y;
    }
    
     
    // TODO (fix) consider overriding toString to return an ASCII representation of the point
     // e.g. (x,y)
 
 }
