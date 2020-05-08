 /**
  * Driver class.
  */
 public class Battleship {
     public static void main(String[] args) {
         GameController gc = new GameController();
         gc.gameSetup();
         gc.play();
     }
 }
