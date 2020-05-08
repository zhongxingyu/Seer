 package fr.iutvalence.java.mp.tictactoe;
 
 /**
  * This class represents the actions of the game's players
 *
  */
 public class RandomPlayer implements Player
 {
     /**
      * Gets the position in which the player has decided to put his mark
      * @return the position in which the player has decided to put his mark
      */
     public Position getChoice()
     {
         int column = (int) (Grid.DEFAULT_GRID_SIZE * Math.random());
         int line = (int) (Grid.DEFAULT_GRID_SIZE * Math.random());
         return new Position(column, line);
     }
     
     
 }
