 package fr.iutvalence.java.mp.clickToShoot;
 
 /**
 * @author founasa
  * 
  */
 public interface Display
 {
     /**
      * display the score
      * 
      * @param score
      *            amount of the score
      */
     public void displayScore(int score);
 
     /**
      * display the coordinates of the target
      * @param P position of target        
      */
     // TODO (FIXED) replace the two coordinates by a single position parameter
     public void displayTargetCoordinates(Position P);
 
     /**
      * display the coordinates of the player
      * @param P position of the shoot
      */
     // TODO (FIXED) replace the two coordinates by a single position parameter
     public void displayShotCoordinates(Position P);
 
     /**
      * display the number of shoots remaining
      * 
      * @param number
      *            number of shoots done by the player
      */
     public void displayRemainingShots(int number);
 
     /**
      * display if the target was touched or not
      * 
      * @param destroyed
      *            TRUE for the target has been destroyed FALSE otherwise
      */
     public void displayIsTargetDestroyed(boolean destroyed);
 
     /**
      * display a message if the time is done
      */
     public void displayTimeOut();
 }
