 package fr.iutvalence.java.mp.RollingBall;
 
 /**
  * extend of the class Ball which can be in move
  * 
  * @author andrejul
  * 
  */
 public class MovingBall extends Ball
 {
 
     /**
      * speed vector of the MovingBall
      */
     private Vector speedVector;
 
     /**
      * MovingBall created with a radius, a center and a vector (the speed of the
      * ball)
      * 
      * @param radiusOfTheBallWanted
      *            radius of the ball
      * @param centerOfTheBallWanted
      *            center of the ball
      * @param speedOfTheBall
      *            speed of the ball
      */
     public MovingBall(int radiusOfTheBallWanted, Point centerOfTheBallWanted, Vector speedOfTheBall)
     {
         super(radiusOfTheBallWanted, centerOfTheBallWanted);
         this.speedVector = speedOfTheBall;
     }
 
     /**
      * method to set up to date the vector
      * 
     * @param pointToAddToTheVector
      *            Point which define the speed of the vector
      */
    // TODO (FIXED) rename this method and use relevant parameter
    public void setNewSpeed(Point pointToAddToTheVector)
     {
        this.speedVector.getEndingPoint().addPoint(pointToAddToTheVector);
     }
 
 }
