 package edu.victone.scrabblah.logic.player;
 
 /**
  * Created with IntelliJ IDEA.
  * User: vwilson
  * Date: 9/11/13
  * Time: 5:23 PM
  */
 
 public class AIPlayer extends Player {
     public static String[] playerNames = {"Charles B.", "Bill G.", "Steve J.", "Steve W.", "Alan T.", "John V.N.", "Bob H.", "Ken S.", "John J."};
     private double skillLevel;
 
     public AIPlayer(String name, int rank) {
         this(name, rank, 1.0); //creates godlike scrabble players
     }
 
     public AIPlayer(String name, int rank, double skillLevel) {
         super(name, rank);
        this.skillLevel = skillLevel;
 
     }
 
     @Override
     public String toString() {
         return "P" + rank + ": " + name + " (AI) - Score: " + score;
     }
 }
