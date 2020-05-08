 package fr.iutvalence.java.mp.RollingBall;
 
 /**
  * the Vector extends the Point class and it is used to set up the physic
  * rules in the game. 
  * The Vector is the thing we will use to move an object on our map.
  * 
  * @author andrejul
  * 
  */
 public class Vector extends Point
 {
 
     /**
      * Vector created with the component of the vector
      * @param x
      *         abscissa of the component of the vector
      * @param y
      *         ordered of the component of the vector
      */
     public Vector(double x, double y)
     {
         super(x, y);
     }
     
     /**
     * method to add two points
      * 
     * @param point
     *              the Point we want to add to the current Point
     * @return Point
     *              the resulting Point
      */
    // TODO (refactor) this method should be renamed sum and take a vector as parameter
    public Vector addPoint(Point point)
     {
        return new Vector( this.getX() + point.getX(), this.getY() + point.getY() );
     }
     
     /**
      * Returns an ASCII representation of the vector as the ASCII representation of the point
      * @return String
      *              the ASCII representation of the vector
      */
     public String toString()
     {
         return "V" + super.toString();
     }
 }
