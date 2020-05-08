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
     private int radius;
 
     /**
      * center of the ball
      */
     private Point middlePoint;
 
     /**
      * ball created with two defined parameters : a radius and a center
      * 
      * @param radiusOfTheBallWanted
      *            radius of the ball
      * @param centerOfTheBallWanted
      *            center of the ball
      */
     public Ball(int radiusOfTheBallWanted, Point centerOfTheBallWanted)
     {
         this.radius = radiusOfTheBallWanted;
         this.middlePoint = centerOfTheBallWanted;
     }
 
     /**
      * method to get the center of the ball
      * 
     * @return Point 
     *              the center of the ball
      */
     public Point getMiddlePoint()
     {
         return this.middlePoint;
     }
 
     /**
      * method to get the radius of the ball
      * 
     * @return Integer 
     *              radius of the ball
      */
     public int getRadius()
     {
         return this.radius;
     }

    // TODO (FIXED) consider overriding toString to return an ASCII representation of the ball
     // e.g. [(x,y), r]
     // it can rely on the toString method overridden in Point
    /**
     * method to get the parameters of the Ball to String
     * 
     * @return String
     *              parameters of the Ball
     */
    public String toString()
    {

        return "["+this.middlePoint+","+ this.radius+"]";
    }
 }
