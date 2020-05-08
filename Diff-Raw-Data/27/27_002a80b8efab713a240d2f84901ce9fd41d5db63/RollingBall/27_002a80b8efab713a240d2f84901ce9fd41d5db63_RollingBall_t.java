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
     
    // TODO (FIXED) the following two fields look like local variables

     
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
 
    // TODO (FIXED) this method should not be declared here but elsewhere

     /**
      * set the physical rules of our environment and launch the game
      */
     public void play()
     {
         
     }
     
 }
