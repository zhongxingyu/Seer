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
      * display's frequency of the ball
      */
     public final static double DISPLAY_FREQUENCY = 60;
     
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
     public MovingBall(double radiusOfTheBallWanted, Point centerOfTheBallWanted, Vector speedOfTheBall)
     {
         super(radiusOfTheBallWanted, centerOfTheBallWanted);
         this.speedVector = speedOfTheBall;
     }
 
     /**
      * method to get the next position of the ball at a frequency given
      * @return Point
      *              the next center of the ball
      */
     public Point nextPositionOfTheBall()
     {
         Point temp = new Point( ( this.speedVector.getX()/DISPLAY_FREQUENCY ) + this.getCenter().getX(), 
                 ( this.speedVector.getY()/DISPLAY_FREQUENCY ) + this.getCenter().getY() );
         return temp;
     }
     
     /**
      * method to get the speedVector of the ball
      * 
      * @return Point 
      *              the speedVector of the ball
      */
     public Vector getSpeedVector()
     {
         return this.speedVector;
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
        this.speedVector = this.speedVector.addPoint(pointToAddToTheVector);
    }
    
    /**
      * Returns an ASCII representation of the movingball as [ the ASCII representation of a ball, the speed of the ball ]
      * @return String
      *              the ASCII representation of the movingball
      */
     public String toString()
     {
         return "MB[ " + super.toString() + ", " + this.speedVector.toString() + " ]";
     }
 }
