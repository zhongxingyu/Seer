 package fr.iutvalence.java.mp.themorpion;
 
 import java.util.Random;
 
 /**
  * 
  * This class describes a player with his attributes
  * 
  * @author andrvale
  * 
  */
 
 public class Player
 {
 
     // TODO (fix) this method should not test bounds nor throw an exception
     // (this should be done in TicTacToe)
     /**
      * Check if position wanted by the player is between 1 and 9
      * 
      * @return position wanted by the player
      * 
      */
     public Position askPosition()
     {
         int randColumn;
         int randLine;
         Random rand = new Random();
         randColumn = rand.nextInt(TicTacToe.NUMBER_OF_COLUMNS);
         randLine = rand.nextInt(TicTacToe.NUMBER_OF_LINES);
         Position pos = new Position(randLine, randColumn);
 
         return pos;
 
     }
 }
