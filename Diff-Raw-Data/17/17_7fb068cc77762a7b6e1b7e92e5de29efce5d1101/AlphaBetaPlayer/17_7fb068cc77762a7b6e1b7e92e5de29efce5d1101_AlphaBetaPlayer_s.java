 package tictactoe;
 
 import java.awt.event.*;
 import java.util.*;
 
 /**
  * Implementation of the {@code Player} interface which uses a minimax algorithm
  * with alpha-beta pruning.
  *
  * @author Todd Taomae
  */
 public class AlphaBetaPlayer implements Player
 {
     /** Minimum possible heuristic score */
     private static final int MIN_SCORE = -100;
     /** Maximum possible heuristic score */
     private static final int MAX_SCORE = 100;
 
     private int maxDepth;
     private Mark myMark;
     private Random rng;
 
     /**
      * Constructs a new player with the specified maximum search depth.
      *
     * @param   depth   maximum search depth
      */
     public AlphaBetaPlayer(int d)
     {
         // max depth should be at least 2.
         this.maxDepth = Math.max(d, 2);
         this.myMark = Mark.NONE;
         this.rng = new Random();
     }
 
     /**
      * Returns the selected move for the specified {@code Board}.
      *
      * @param   b   board to evaluate
      * @return  the selected move for the specified {@code Board}
      */
     public int getMove(Board b)
     {
         this.myMark = b.getCurrentPlayer();
 
         // special case: first turn - slect corner
         if (b.getTurn() == 0) {
             return 0;
         }
 
         return alphabetaRoot(b, this.maxDepth, MIN_SCORE-1, MAX_SCORE+1);
     }
 
     /**
      * Returns the best move for the specified Board.
      *
      * @param   board   board that is being searched
      * @param   depth   maximum search depth
      * @param   alpha   alpha cutoff (minimum)
      * @param   beta    beat cutoff (maximum)
      * @return  the index of the best move
      */
     private int alphabetaRoot(Board board, int depth, int alpha, int beta)
     {
         int bestHeuristic = MIN_SCORE;
         HashMap<Integer, Board> possibleMoves = new HashMap<Integer, Board>();
         ArrayList<Integer> bestMoves = new ArrayList<Integer>();
 
 
         // populate list of possible moves
         for (int i = 0; i < 9; i++) {
             Board temp = (Board)board.clone();
             try {
                 if (temp.markAt(i) == Mark.NONE) {
                     temp.play(i);
                     possibleMoves.put(i, temp);
                 }
 
             } catch (IllegalMoveException e) {
                 // should never reach here, since valid moves are checked above
             }
         }
 
         // try each possible move
         for (Map.Entry<Integer, Board> entry : possibleMoves.entrySet()) {
             int heuristic = alphabeta(entry.getValue(), depth - 1, alpha, beta);
             // if move is better than previous best
             if (heuristic > bestHeuristic) {
                 // create new list of best moves
                 bestMoves = new ArrayList<Integer>();
                 bestMoves.add(entry.getKey());
                 bestHeuristic = heuristic;
 
             } else if (heuristic == bestHeuristic) {
                 // if equal to previous best, add to list of best moves
                 bestMoves.add(entry.getKey());
             }
         }
 
         // select a random move from list of best moves
         return bestMoves.get(this.rng.nextInt(bestMoves.size()));
     }
 
     /**
      * Returns the heuristic value for the best move of the specified Board.
      *
      * @param   board   board that is being searched
      * @param   depth   maximum search depth
      * @param   alpha   alpha cutoff (minimum)
      * @param   beta    beat cutoff (maximum)
      * @return  heuristic value of the best move
      */
     private int alphabeta(Board board, int depth, int alpha, int beta)
     {
         // if terminal node, return heuristic.
         if (depth == 0 || board.getWinner() != Mark.NONE) {
             if (board.getWinner() == this.myMark) {
                 return MAX_SCORE;
             } else if (board.getWinner() == this.myMark.opposite()) {
                 return MIN_SCORE;
             } else {
                 return 0;
             }
         }
 
         // create list of possible moves
         ArrayList<Board> possibleMoves = new ArrayList<Board>();
 
         for (int i = 0; i < 9; i++) {
             Board temp = (Board)board.clone();
             try {
                 if (temp.markAt(i) == Mark.NONE) {
                     temp.play(i);
                     possibleMoves.add(temp);
                 }
             } catch (IllegalMoveException e) {
                 // should never reach here, since valid moves are checked above
             }
         }
 
         // maximize
         if (board.getCurrentPlayer() == this.myMark) {
             for (Board b : possibleMoves) {
                 alpha = Math.max(alpha, alphabeta(b, depth-1, alpha, beta));
                 if (beta <= alpha) {
                     break;
                 }
             }
 
             return alpha;
 
         // minimize
         } else {
             for (Board b : possibleMoves) {
                 beta = Math.min(beta, alphabeta(b, depth-1, alpha, beta));
                 if (beta <= alpha) {
                     break;
                 }
             }
 
             return beta;
         }
     }
 
     public void actionPerformed(ActionEvent ae) { /* do nothing */ }
}
