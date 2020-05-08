 package fr.iutvalence.java.mp.RollingBall;
 
 /**
  * 
  * see readme.md Means and rules of the game.
  * 
  * @author andrejul
  * 
  */
//TODO (FIXED) this class has a duplicate (RollingBall)
 public class RollingBallGame
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
     private MovingBall ballUsedByThePlayer;
 
     
    // TODO (FIXED) looks like a local variable
 
     /**
      * rollingball game created with three parameters : the name of the player, the
      * map where the player wants to play and the ball the player want to use
      * 
      * @param nameOfThePlayerPlaying
      *            the name of the player
      * @param mapChoosedByThePlayer
      *            the map used by the player
      * @param ballChoosedByThePlayer
      *            the ball choose by the player
      */
     public RollingBallGame(String nameOfThePlayerPlaying, Map mapChoosedByThePlayer, MovingBall ballChoosedByThePlayer)
     {
         this.nameOfThePlayer = nameOfThePlayerPlaying;
         this.mapUsedByThePlayer = mapChoosedByThePlayer;
         this.ballUsedByThePlayer = ballChoosedByThePlayer;
     }
 
     /**
      * play the game
      */
     public void play()
     {
         boolean youCanPlay = true;
         Point gravityPower = new Point(0, -10);
         int time;
        int scoreOfThePlayer;
        
         // TODO (FIXED) rename local variable (more explicit)
         int indiceOfTheSegmentsField;
 
         time = 0;
         while (youCanPlay)
         {
             indiceOfTheSegmentsField = 0;
             while (!(this.mapUsedByThePlayer.getSegmentsOfTheField()[indiceOfTheSegmentsField].intersect(this.ballUsedByThePlayer))
                     && indiceOfTheSegmentsField < this.mapUsedByThePlayer.getSegmentsOfTheField().length)
             {
                 indiceOfTheSegmentsField++;
             }
             if (indiceOfTheSegmentsField == this.mapUsedByThePlayer.getSegmentsOfTheField().length)
             {
                this.ballUsedByThePlayer.setNewSpeed(gravityPower);
             }
             else
             {
                 // faire l'attribution après avoir fini la fonction dans segment
                 Point forceReaction = new Point(0,0);
                 
                this.ballUsedByThePlayer.setNewSpeed(gravityPower);
                this.ballUsedByThePlayer.setNewSpeed(forceReaction);
             }
         }
     }
 
 }
