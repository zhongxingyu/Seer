 import java.util.*;
 
 /**
  * Translated from the original python written by Christine Alvorado
  */
 
 public class Player {
 
     public static Scanner sc = new Scanner(System.in);
 
     // Constants
     public static int HUMAN = 0;
     public static int RANDOM = 1;
     public static int MINIMAX = 2;
     public static int ABPRUNE = 3;
     public static int CUSTOM = 4;
 
     // Data Members
     private int num;
     private int opp;
     private int type;
     private int ply;
     private int nodes;
 
     // Constructor    
     public Player(int playerNum, int oppNum, int playerType, int ply) {
         this.num = playerNum;
         this.opp = oppNum;
         this.type = playerType;
         this.ply = ply;
         this.nodes = 0;
     }
 
     // Methods
     public String toString() {
         return "" + this.num;
     }    
     
     public int getNum() {
         return num;
     }
     
     public int minimaxMove(BoardGame board, int ply ) {
         // Choose the best minimax move.  Returns (move, val) 
         int move = -1;
         int score = Integer.MIN_VALUE;
         Player turn = this;
         for (Integer m : board.legalMoves( this )) {
             if (ply == 0) {
                 return m;
             }
             if (board.gameOver()) {
                 return -1;  // Can't make a move, the game is over
             }
             BoardGame nb = (BoardGame)board.clone();
             nb.makeMove(this, m);
             Player opp = new Player(this.opp, this.num, this.type, this.ply);
             int s[] = opp.minValue(nb, ply-1, turn);
             System.out.println("Move " + m + " score " + s[0] + " reply " + s[1]);
             if (s[0] > score) {
                 move = m;
                 score = s[0];
             }
         }
         return move;
     }
 
     public int[] maxValue(BoardGame board, int ply, Player turn) {
         // Find the minimax value for the next move for this player
         //    at a given board configuation
         //    Returns (score, oppMove)
         if (board.gameOver()) {
             int[] t = {turn.score(board), -1};
             return t;
         }
         int[] s = {Integer.MIN_VALUE, -1};
         for (Integer m : board.legalMoves( this )) {
             if (ply == 0) {
                 s[0] = turn.score(board);
                 s[1] = m;
                 return s;
             }
 
             // make a new player to play the other side
             Player opponent = new Player(this.opp, this.num, this.type, this.ply);
 
             // Copy the board so that we don't ruin it
             BoardGame nextBoard = (BoardGame)board.clone();
 
             nextBoard.makeMove( this, m );
             int[] s2 = opponent.minValue(nextBoard, ply-1, turn);
             if (s2[0] > s[0]) {
                 s[0] = s2[0];
                 s[1] = m;            
             }
         }
         return s;
     }
     
     public int[] minValue(BoardGame board, int ply, Player turn ) {
         // Find the minimax value for the next move for this player
         //    at a given board configuation
         if (board.gameOver()) {
             int[] t = {turn.score(board), -1};
             return t;
         }
         int[] s = {Integer.MAX_VALUE, -1};
         for (Integer m : board.legalMoves( this )) {
             if (ply == 0) {
                 s[0] = turn.score(board);
                 s[1] = m;
                 return s;
             }
             // make a new player to play the other side
             Player opponent = new Player(this.opp, this.num, this.type, this.ply);
             // Copy the board so that we don't ruin it
             BoardGame nextBoard = (BoardGame)board.clone();
             nextBoard.makeMove( this, m );
             int[] s2 = opponent.maxValue(nextBoard, ply-1, turn);
             if (s2[0] < s[0]) {
                 s[0] = s2[0];
                 s[1] = m;            
             }
         }
         return s;
     }
 
     public int alphaBetaMove(BoardGame board, int ply ) {
         // Choose the best minimax move.  Returns (move, val) 
         int move = -1;
         int score = Integer.MIN_VALUE;
         int alpha = Integer.MIN_VALUE;
         int beta = Integer.MAX_VALUE;
 
         Player turn = this;
         for (Integer m : board.legalMoves( this )) {
             if (ply == 0) {
                 return m;
             }
             if (board.gameOver()) {
                 return -1;  // Can't make a move, the game is over
             }
             BoardGame nb = (BoardGame)board.clone();
             nb.makeMove(this, m);
             Player opp = new Player(this.opp, this.num, this.type, this.ply);
             int s[] = opp.alphaBetaMinValue(nb, ply-1, turn, alpha, beta);
             System.out.println("Move " + m + " score " + s[0] + " reply " + s[1]);
             if (s[0] > score) {
                 move = m;
                 score = s[0];
             }
         }
         return move;
     }
 
     public int[] alphaBetaMaxValue(BoardGame board, int ply, Player turn, int alpha, int beta) {
         // Find the minimax value for the next move for this player
         //    at a given board configuation
         //    Returns (score, oppMove)
         if (board.gameOver()) {
             int[] t = {turn.score(board), -1};
             return t;
         }
         int[] s = {Integer.MIN_VALUE, -1};
         for (Integer m : board.legalMoves( this )) {
             if (ply == 0) {
                 s[0] = turn.score(board);
                 s[1] = m;
                 return s;
             }
 
             // make a new player to play the other side
             Player opponent = new Player(this.opp, this.num, this.type, this.ply);
 
             // Copy the board so that we don't ruin it
             BoardGame nextBoard = (BoardGame)board.clone();
 
             nextBoard.makeMove( this, m );
             int[] s2 = opponent.alphaBetaMinValue(nextBoard, ply-1, turn, alpha, beta);
             if (s2[0] > s[0]) {
                 s[0] = s2[0];
                 s[1] = m;            
             }
             if (s[0] >= beta) {
                 break;
             }
             alpha = (int)Math.max(alpha, s[0]);
         }
         return s;
     }
     
     public int[] alphaBetaMinValue(BoardGame board, int ply, Player turn, int alpha, int beta ) {
         // Find the minimax value for the next move for this player
         //    at a given board configuation
         if (board.gameOver()) {
             int[] t = {turn.score(board), -1};
             return t;
         }
         int[] s = {Integer.MAX_VALUE, -1};
         for (Integer m : board.legalMoves( this )) {
             if (ply == 0) {
                 s[0] = turn.score(board);
                 s[1] = m;
                 return s;
             }
             // make a new player to play the other side
             Player opponent = new Player(this.opp, this.num, this.type, this.ply);
             // Copy the board so that we don't ruin it
             BoardGame nextBoard = (BoardGame)board.clone();
             nextBoard.makeMove( this, m );
             int[] s2 = opponent.alphaBetaMaxValue(nextBoard, ply-1, turn, alpha, beta);
             if (s2[0] < s[0]) {
                 s[0] = s2[0];
                 s[1] = m;            
             }
             if (s[0] <= alpha) {
                 break;
             }    
             beta = (int)Math.min(beta, s[0]);
         }
         return s;
     }
 
     // The default player defines a very simple score function
     // You will write the score function in the MancalaPlayer below
     // to improve on this function.
     public int score(BoardGame board) {
         // Returns the score for this player given the state of the board
         Axiom b = (Axiom)board;
         if (board.hasWon(this.num)) {
             return 100;
         } else if (board.hasWon(this.opp)) {
             return -100;
         } else {
             return b.freeCubes(this.num) - b.freeCubes(this.opp);
         }
     }
 
     public int chooseMove(BoardGame board) {
         // Returns the next move that this player wants to make
         int move = -1;
         System.out.println(board.showMoves(this));
         if (type == HUMAN) {
             System.out.println("Please enter your move:");
             move = sc.nextInt();
             while (!board.legalMove(this, move)) {
                 System.out.println("" + move + "is not valid");
                 System.out.println("Please enter your move:");
                 move = sc.nextInt();
             }
         } else if (type == RANDOM) {
             ArrayList<Integer> t = board.legalMoves(this);
             move = t.get((int)(Math.random() * t.size()));
             System.out.println("chose move " + move);
         } else if (type == MINIMAX) {
             move = minimaxMove( board, ply );
             System.out.println("chose move " + move);
         } else if (this.type == this.ABPRUNE) {
             move = alphaBetaMove( board, ply);
             System.out.println("chose move " + move);
         } else if (type == CUSTOM) {
             System.out.println("Custom player not yet implemented");
         } else {
             System.out.println("Unknown player type");
         }
        System.out.println("" + num + ": Making move " + board.showMove(move));
         return move;
     }
 } 
