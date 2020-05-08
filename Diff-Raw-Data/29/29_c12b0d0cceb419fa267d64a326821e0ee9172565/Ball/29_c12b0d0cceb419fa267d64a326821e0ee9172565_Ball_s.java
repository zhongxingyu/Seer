 package fr.iutvalence.java.mp.RollingBall;
 
 /**
  * 
  * a simple ball which will roll over the map
  * @author andrejul
  *
  */
 public class Ball
 {
    // TODO (fix) rename field (more explicit)
     /**
      * radius of the ball
      */
    private int r;
     
    // TODO (fix) rename field (more explicit)
     /**
      * center of the ball
      */
    private Point o;
     
    // TODO (fix) write a "real" comment
     /**
     * a simple ball
     * @param r radius of the ball
     * @param o center of the ball
      */
    public Ball(int r, Point o)
     {
        this.r = r;
        this.o = o;
     }
 }
