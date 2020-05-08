 package fr.iutvalence.java.mp.clickToShoot;
 
// TODO (fix) detail comment : what player and what display are used?
 /**
 * Model of the Main class which we will execute
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
