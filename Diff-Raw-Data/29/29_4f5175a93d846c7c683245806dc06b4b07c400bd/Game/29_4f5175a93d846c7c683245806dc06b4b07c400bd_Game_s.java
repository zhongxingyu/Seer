 package ChiniMess;
 
 import java.util.ArrayList;
 
 import Players.*;
 
 public class Game {
 
     /**
      * @param args
      */
     public static void main(String[] args) {
         Board b = new Board(); // normal standard Board
         GameStatus status = GameStatus.GAME_RUNNING;
         System.out.println(b);
         
        Player White = new GreedyPlayer();
        Player Black = new RandomPlayer();
         
         while(status == GameStatus.GAME_RUNNING){
             Move m;
             if (b.getPlayerOnTurn() == Board.WHITE)
                 m = White.chooseMove(b);
             else //if (b.getPlayerOnTurn() == Board.BLACK)
                 m = Black.chooseMove(b);
             
             
             b.executeMove(m);
             status = b.gameOver();
             System.out.println(b);
             System.out.println("picked Move:" + m);
             System.out.println("Score: " + b.calculateScore());
         }
         
         // Determine the winning player
         if (status == GameStatus.GAME_WHITEWINS)
             System.out.println("White wins!");
         
         else if (status == GameStatus.GAME_BLACKWINS)
             System.out.println("Black wins!");
         
         else
             System.out.println("Draw!");
 
     }
 }
