 package FinalFantasy.worldObjects;
 
 import FinalFantasy.*;
 
 import RPGGrid.actor.*;
 import RPGGrid.input.*;
 
 /**
  * An <code>EmptySpaceDungeon<code> moves the player to its location,
  * and thus enableing movement of the player, and has a random
  * chance to initiate a battle.
  */
 public class EmptySpaceDungeon extends WorldObject
 {
     
     /**
      * Moves the player to its location, and has a random chance
      * of initiating a battle with an monster when called.
      * @param p The player to move and potentialy add to a battle
      */
     public void interact(ThePlayer p) {
         p.moveTo(getLocation());
         
        if(Math.random() > 0.92)
         {
             System.out.println("A battle Started!");
             Battle b = new Battle(p.getFFCharacter());
         }
     }
 }
