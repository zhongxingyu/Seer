 package fr.iutvalence.java.mp.RollingBall;
 
 /**
  * 
  * a simple ball which will roll over the map
  * 
  * @author andrejul
  * 
  */
 public class Ball
 {
     /**
      * radius of the ball
      */
    // TODO (FIXED) maybe double should be used instead of int
    private double radius;
 
     /**
      * center of the ball
      */
     private Point center;
 
     /**
      * ball created with two defined parameters : a radius and a center
      * 
      * @param radiusOfTheBallWanted
      *            radius of the ball
      * @param centerOfTheBallWanted
      *            center of the ball
      */
    public Ball(double radiusOfTheBallWanted, Point centerOfTheBallWanted)
     {
         this.radius = radiusOfTheBallWanted;
         this.center = centerOfTheBallWanted;
     }
 
     /**
      * method to get the center of the ball
      * 
      * @return Point 
      *              the center of the ball
      */
     public Point getCenter()
     {
         return this.center;
     }
 
     /**
      * method to get the radius of the ball
      * 
      * @return Integer 
      *              radius of the ball
      */
    public double getRadius()
     {
         return this.radius;
     }
 
    // TODO (FIXED) finish writing comment
     /**
     * Returns an ASCII representation of the ball as : [ center of the ball, radius of the ball ]
     * @return String
     *              the ASCII representation of the ball
      */
     public String toString()
     {
 
        return "[ "+this.center+", "+ this.radius+" ]";
     }
 }
