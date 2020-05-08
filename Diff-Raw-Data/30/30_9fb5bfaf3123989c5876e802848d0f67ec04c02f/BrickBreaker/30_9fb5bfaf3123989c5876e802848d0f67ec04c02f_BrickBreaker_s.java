 package fr.iutvalence.java.mp.BrickBreaker;
 
 // TODO (fixed) write a "real" comment
 /**
 * Main class, the program start here and use the other classes
  * @author ochiers
  * 
  */
 
 public class BrickBreaker
 {
     /**
      * The program start here
      * @param args
      */
     // TODO (fixed) write a comment
     public static void main(String[]args)
     {
         // tests 
         
         Game newGame = new Game();
         for(int i = 0; i< newGame.NUMBER_OF_BRICKS; i++)
         {
             System.out.println(newGame.tabBrick[i].toString());
         }
     }
 }
