 package fr.iutvalence.java.mp.RollingBall;
 
 /**
  * Interface which is an abstract display
  * 
  * @author andrejul
  *
  */
 public interface Display
 {
    // TODO (FIXED) you have defined the interface with regards to the only available
     // implementation : look ahead and imagine what kind of callback method any display needs
     /**
      * method to print a message when the map is created
      */
     public void mapCreated();
 
     /**
      * method to print a message when the ball is created
      */
     public void ballCreated();

     /**
      * method to print a message when the ball flies
      * 
      * @param movingBall
      *                  the moving ball which is flying
      */
     public void flyingBall(MovingBall movingBall);
 
     /**
      * method to print the message when the ball hits a segment
      * 
      * @param segmentHit 
      *                  the segment hit
      * @param reactPower 
      *                  the vector which characterized 
      * @param ballInMove 
      *                  the moving ball
      */
     public void theBallHits(Segment segmentHit, Vector reactPower, MovingBall ballInMove);
 
     /**
      * method to print the message when the game starts
      * 
      * @param playerName
      *                  the name of the player
      */
     public void gameStart(String playerName);

    /**
     * method which displays the map somewhere
     * 
     * @param map
     *                  the map we need to display
     */
    public void roundStart(Map map);

     /**
      * method to print a message when a round of the game ends
      * 
      * @param playerName
      *              the name of the player
      * @param scoreOfThePlayer
      *              the score of the player
      */
     public void roundEnd(String playerName, double scoreOfThePlayer);

     /**
      * method to print the message when the game ends
      * 
      * @param numberOfBounces
      *                  number of bounces of the ball
      * @param playerName
      *                  the name of the player
      * @param scoreOfThePlayer
      *                  the score of the player                  
      */
     public void gameEnd(double numberOfBounces, String playerName, double scoreOfThePlayer);

     /**
      * method to print a message when the ball bounces
      */
     public void bounce();
 }
