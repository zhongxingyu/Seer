 
 import java.util.ArrayList;
 
 
 public class GameLogic implements IGameLogic {
     public enum Tile {EMPTY, PLAYER1, PLAYER2}
     
     private int width = 0;
     private int height = 0;
     private int playerID;
     
     private Board board;
     
     public GameLogic() {
         //TODO Write your implementation for this method
     }
 	
     public void initializeGame(int width, int height, int playerID) {
         this.width = width;
         this.height = height;
         this.playerID = playerID;
         
         board = new Board();
         board.initBoard(width, height);
     }
 	
     public Winner gameFinished() {
        if(board.isTerminal()) {
            if(board.getWinner() == Board.Winner.PLAYER1) {
                return IGameLogic.Winner.PLAYER1;
            } else if(board.getWinner() == Board.Winner.PLAYER2) {
                return IGameLogic.Winner.PLAYER2;
            } else {
                return IGameLogic.Winner.TIE;
            }
         } else {
             return IGameLogic.Winner.NOT_FINISHED;
         }
     }
 
 
     public void insertCoin(int column, int playerID) {
         System.out.println("Insert: " + column + " Player: " + playerID);
         board.insert(column, playerID);
     }
 
     public int decideNextMove() {
         
         
         Board boardCopy = new Board(board);
         int decision;
      // Measure Time
         long startTime = System.nanoTime();
 //        decision = Minimax.decision(boardCopy, playerID);
         decision = ABSearch.ABSearch(board, 5, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY, playerID)[1];
         
         long endTime = System.nanoTime();
         long elapsed = endTime - startTime;
         System.out.println("Decision: " + decision + " Took: " + elapsed + "ns");
        
         return decision;
     }  
 }
