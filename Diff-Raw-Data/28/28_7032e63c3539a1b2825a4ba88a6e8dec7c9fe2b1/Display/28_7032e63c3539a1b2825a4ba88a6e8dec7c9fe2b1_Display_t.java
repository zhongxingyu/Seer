 package fr.iutvalence.java.mp.RollingBall;
 
 /**
  * display of the game
  * 
  * @author andrejul
  *
  */
 public class Display
 {
    
    // TODO (FIXED) all the following methods should not be static
     /**
      * method to print a message when the map is created
      */
    public void mapCreated()
     {
         System.out.println("map created");
     }
 
     /**
      * method to print a message when the ball is created
      */
    public void ballCreated()
     {
         System.out.println("balle créée");        
     }
 
     /**
      * method to print a message when the ball flies
      * 
      * @param movingBall
      *                  the moving ball which is flying
      */
    public void flyingBall(MovingBall movingBall)
     {
         System.out.println("vole petite baballe !");
         System.out.println(movingBall);
     }
 
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
    public void theBallHits(Segment segmentHit, Vector reactPower, MovingBall ballInMove)
     {
         System.out.println("stop ! tu touches !");
         System.out.println(segmentHit);
         System.out.println("R " + reactPower);
         System.out.println(ballInMove);
     }
 
     /**
      * method to print the message when the game starts
      * 
      * @param playerName
      *                  the name of the player
      */
    public void gameStart(String playerName)
     {
         System.out.println("welcome " + playerName + " !!"); 
     }
     
     /**
      * method to print a message when a round of the game ends
      * 
      * @param playerName
      *              the name of the player
      * @param scoreOfThePlayer
      *              the score of the player
      */
    public void roundEnd(String playerName, double scoreOfThePlayer)
     {
         System.out.println("Your current score " + playerName + " : " + (int)scoreOfThePlayer + " !!");       
     }
     
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
    public void gameEnd(double numberOfBounces, String playerName, double scoreOfThePlayer)
     {
         System.out.println("La balle ne bouge plus :(\n--> end of the play");
         System.out.println("La balle a rebondit " + (int)numberOfBounces + " fois ! :p");
         System.out.println("Your final score " + playerName + " : " + (int)scoreOfThePlayer + " !!");       
     }
     
     /**
      * method to print a message when the ball bounces
      */
    public void bounce()
     {
         System.out.println("Boing !");        
     }
 }
