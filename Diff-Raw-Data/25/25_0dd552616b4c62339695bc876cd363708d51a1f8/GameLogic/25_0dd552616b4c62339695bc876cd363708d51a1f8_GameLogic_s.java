 
 
 
 public class GameLogic implements IGameLogic {
     public enum Tile {EMPTY, PLAYER1, PLAYER2}
     
     private int width = 0;
     private int height = 0;
     private int playerID;
     
     private Board board;
     
     public GameLogic() {
         //TODO Write your implementation for this method
     }
 	
     @Override
     public void initializeGame(int width, int height, int playerID) {
         this.width = width;
         this.height = height;
         this.playerID = playerID;
         
         board = new Board();
         board.initBoard(width, height);
     }
 	
     @Override
     public Winner gameFinished() {
         if(board.getWinner() == Board.Winner.PLAYER1) {
             return IGameLogic.Winner.PLAYER1;
         } else if(board.getWinner() == Board.Winner.PLAYER2) {
             return IGameLogic.Winner.PLAYER2;
         } else if(board.getWinner() == Board.Winner.TIE) {
             return IGameLogic.Winner.TIE;   
         } else {
             return IGameLogic.Winner.NOT_FINISHED;
         }
     }
 
 
     @Override
     public void insertCoin(int column, int playerID) {
         System.out.println("Insert: " + column + " Player: " + playerID);
         board.insert(column, playerID);
     }
 
     @Override
     public int decideNextMove() {
         Board boardCopy = new Board(board);
         int decision;
      // Measure Time
         long startTime = System.nanoTime();
 //        decision = Minimax.decision(boardCopy, playerID);
 //        decision = ABSearchDepthCutoff.Search(board, 10, playerID);
        decision = ABSearchDepthCutoffWithOrdering.Search(board, 3, playerID, playerID);
         
         long endTime = System.nanoTime();
         double elapsed = (endTime - startTime) / 1e9;
 
         System.out.format("Decision: %d Took: %.6fs%n", decision, elapsed);
        
         return decision;
     }  
 }
