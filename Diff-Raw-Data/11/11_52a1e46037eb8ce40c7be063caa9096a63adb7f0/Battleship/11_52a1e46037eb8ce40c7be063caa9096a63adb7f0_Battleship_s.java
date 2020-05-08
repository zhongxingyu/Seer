 package de.htwg.se.battleship;
 
 import org.apache.log4j.PropertyConfigurator;
 
 import de.htwg.se.battleship.aview.tui.TextUI;
 import de.htwg.se.battleship.controller.Controller;
 import de.htwg.se.battleship.controller.IController;
 
 /**
  * Initial class to start java program
  * 
  * @author Philipp Daniels<philipp.daniels@gmail.com>
  */
 public final class Battleship {
 
     private static Battleship    instance = null;
 
     /**
      * Return always the same instance of Battleship
      * 
      * @return Battleship instance
      */
     public static Battleship getInstance() {
         if (instance == null) {
             instance = new Battleship();
         }
         return instance;
     }
 
     /**
      * close main
      */
     private Battleship() {
         PropertyConfigurator.configure("log4j.properties");
         IController controller = new Controller();
         new TextUI(controller, System.in);
     }
 
     /**
      * Start java program
      * 
      * @param args
      */
    public static void main() {
         Battleship.getInstance();
     }
 
 }
