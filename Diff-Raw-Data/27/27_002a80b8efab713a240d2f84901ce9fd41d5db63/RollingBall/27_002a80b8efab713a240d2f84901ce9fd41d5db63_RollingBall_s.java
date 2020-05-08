 package fr.iutvalence.java.mp.RollingBall;
 
 /**
  * 
  * see readme.md
  * Means and rules of the game.
  * @author andrejul
  *
  */
 public class RollingBall
 {
     /**
      * the name of the player
      */
     private String nameOfThePlayer;
 
     /**
      * the map used by the player
      */
     private Map mapUsedByThePlayer;
     
     /**
      * the ball used by the player
      */
     private Ball ballUsedByThePlayer;
     
     /**
      * the score of the player
      */
     private Score scoreOfThePlayer;
     
    // TODO (fix) the following two fields look like local variables
    /**
     * integer used to ease the calculation in the intersect method
     */
    private int x1;
    
    /**
     * integer used
     */
    private int y1;
     
     /**
      * rollingball created with three parameters : the name of the player, the
      * map where the player wants to play and the ball the player want to use
      * @param nameOfThePlayerPlaying the name of the player
      * @param mapChoosedByThePlayer the map used by the player
      * @param ballChoosedByThePlayer the ball choose by the player
      */
     public RollingBall(String nameOfThePlayerPlaying, 
                         Map mapChoosedByThePlayer, Ball ballChoosedByThePlayer)
     {
         this.nameOfThePlayer = nameOfThePlayerPlaying;
         this.mapUsedByThePlayer = mapChoosedByThePlayer;
         this.ballUsedByThePlayer = ballChoosedByThePlayer;
     }
 
    // TODO (fix) this method should not be declared here but elsewhere
    /**
     * method which tests if a ball is on(/in) a segment
     * @return return a boolean, if yes : the ball is on a segment, 
     * otherwise it's out of a segment of the map
     */
    private boolean intersect()
    {
        x1 = this.ballUsedByThePlayer.getMiddlePoint().getX();
        y1 = this.ballUsedByThePlayer.getMiddlePoint().getY();
    }
    
     /**
      * set the physical rules of our environment and launch the game
      */
     public void play()
     {
         
     }
     
 }
