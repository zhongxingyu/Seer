 package minesweeper;
 
 
 import static minesweeper.BoardStatus.*;
 
 /**
  *
  * @author shiera
  */
 public class Main {
 
     public static void main(String[] args) {
        GameLogic game = new GameLogic(5, 5);
         game.play();
 
     }
 }
