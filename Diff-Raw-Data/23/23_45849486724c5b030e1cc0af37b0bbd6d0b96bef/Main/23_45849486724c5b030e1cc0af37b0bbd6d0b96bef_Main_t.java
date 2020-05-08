 package fr.iutvalence.java.mp.clickToShoot;
 
// TODO (FIXED) detail comment : what player and what display are used?
 /**
 * Model of the Main class which we will execute, we use here an abstract display and player, to choose
 * differents sources, here RandomPlayer (the computer do a random shot) and ConsoleDisplay (all the
 * displays are in the console).
  * 
  * @author founasa
  */
 public class Main
 {
     /**
      * Application's main
      * 
      * @param args
      *            command-line arguments (none here)
      */
     public static void main(String[] args)
     {
         ClickToShoot game = new ClickToShoot();
         game.setPlayer(new RandomPlayer());
         game.setDisplay(new ConsoleDisplay());
         game.play();
     }
 }
