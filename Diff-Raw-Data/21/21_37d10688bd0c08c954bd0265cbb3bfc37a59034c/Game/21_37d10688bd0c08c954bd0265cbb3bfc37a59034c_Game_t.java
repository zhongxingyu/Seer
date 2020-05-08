 package reversi;
 
 public class Game {
   public static final int BLACK = 1;
   public static final int WHITE = 2;
   public static final int EMPTY = 0;
   public static final int OUT_OF_BOUNDS = -1;
   public Board board;
   public Player player1;
   public Player player2;
 
   public Game (int numHumans) {
     board = new Board();
     // black traditionally goes first
     switch (numHumans) {
     case 0:
       player1 = new ComputerPlayer(BLACK);
       player2 = new ComputerPlayer(WHITE);
     case 1:
       player1 = new HumanPlayer(BLACK);
       player2 = new ComputerPlayer(WHITE);
     case 2:
       player1 = new HumanPlayer(BLACK);
       player2 = new HumanPlayer(WHITE);
     }
   }
 
   /**
    * Starts the game loop
    */
   public void beginGame() {
     Player currentPlayer = player1;
     while (board.existValidMove(currentPlayer.color)) {
       // display the board and scores
       System.out.println(board);
       System.out.println("BLACK: " + board.getScore(BLACK));
       System.out.println("WHITE: " + board.getScore(WHITE));
       
       // get the current player to decide a move
      Move m = currentPlayer.getMove(board);      
       
       // put their piece on the board
      boolean goodMove = board.makeMove(m);
      
      Player nextPlayer = (currentPlayer.color == WHITE) ? player1 : player2;
      
      if (!goodMove) {
    	  System.out.println("Invalid Move.  Please Try again.");    	  
      } else if (board.existValidMove(nextPlayer.color)) {
    	  // switch to the other player
          currentPlayer = nextPlayer;
      } //Else repeat this persons turn.  
 
      
     }
     
     // Decide who wins
     int player1Score = board.getScore(BLACK);
     int player2Score = board.getScore(BLACK);
     if (player1Score > player2Score) {
       System.out.println("Black wins!");
     } else if (player1Score < player2Score){
       System.out.println("White wins!");
     } else if (player1Score == player2Score) {
       System.out.println("Tie!");
     }
   }
 
   public static void main(String[] args) {
    Game g = new Game(1);
     g.beginGame();
   }
 }
 
