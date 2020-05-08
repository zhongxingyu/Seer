 package fr.iutvalence.java.mp.clickToShoot;
 
 /**
 * Implementation of a player shooting randomly
  * 
  * @author founasa
  */
 public class RandomPlayer implements Player
 {
     /**
      * Return the coordinates of a random click
      * 
      * @return Position
      */
     public Position getShotPosition()
     {
         int x = (int) (Math.random() * (Grid.MAX_X - Grid.MIN_XY)) + Grid.MIN_XY;
         int y = (int) (Math.random() * (Grid.MAX_Y - Grid.MIN_XY)) + Grid.MIN_XY;
         return new Position(x, y);
     }
 }
