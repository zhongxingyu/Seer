 package fr.iutvalence.java.mp.RollingBall;
 
 /**
  * 
  * two integers which define the point
  * 
  * @author andrejul
  * 
  */
// TODO (FIXED) maybe it should be better to consider coordinates as doubles
 public class Point
 {
 
     /**
      * abscissa of the point
      */
    private double x;
 
     /**
      * ordered of the point
      */
    private double y;
 
     /**
      * point created with two defined integers
      * 
      * @param x
      *            abscissa of the point
      * @param y
      *            ordered of the point
      */
    public Point(double x, double y)
     {
         this.x = x;
         this.y = y;
     }
 
     /**
      * method to get the abscissa of the point
      * 
      * @return integer the abscissa of the point
      */
    public double getX()
     {
         return this.x;
     }
 
     /**
      * method to get the ordered of the point
      * 
      * @return integer the ordered of the point
      */
    public double getY()
     {
         return this.y;
     }    
 
     /**
      * method to add two points
      * 
      * @param point
      *              the Point we want to add to the current Point
      * @return Point
      *              the resulting Point
      */
     public Point addPoint(Point point)
     {
         return new Point(this.x+point.x,this.y+point.y);
     }
     
    // TODO (FIXED) finish writing comment
 
     /**
     * Returns an ASCII representation of the point as ( the abscissa of the point, the ordered of the point )
     * @return String
     *              the ASCII representation of the vector
      */
     public String toString()
     {
        return "( " + this.x + ", " + this.y + " )";
     }
 
 }
