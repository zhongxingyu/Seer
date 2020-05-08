 package fr.iutvalence.java.mp.RollingBall;
 
 /**
  * 
  * a simple ball which will roll over the map
  * @author andrejul
  *
  */
 public class Ball
 {
    // TODO (FIXED) rename field (more explicit)
     /**
      * radius of the ball
      */
    private int radiusOfTheBall;
     
    // TODO (FIXED) rename field (more explicit)
     /**
      * center of the ball
      */
    private Point centerOfTheBall;
     
    // TODO (FIXED) write a "real" comment
     /**
     * ball created with two defined parameters : a radius and a center
     * @param radiusOfTheBallWanted radius of the ball
     * @param centerOfTheBallWanted center of the ball
      */
    public Ball(int radiusOfTheBallWanted, Point centerOfTheBallWanted)
     {
        this.radiusOfTheBall = radiusOfTheBallWanted;
        this.centerOfTheBall = centerOfTheBallWanted;
     }
 }
