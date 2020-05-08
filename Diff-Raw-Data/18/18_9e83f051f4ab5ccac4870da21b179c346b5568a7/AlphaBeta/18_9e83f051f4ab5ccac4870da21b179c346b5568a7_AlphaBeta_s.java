 package ai;
 
 import datamodels.Board;
 import datamodels.Move;
 
 import java.util.ArrayDeque;
 
 /**
  * Created by
  * User: jakub
  * Date: 26.05.13
  */
 public class AlphaBeta {
 
     private Board clonedBoard;
     private Move bestMove = null;
     private boolean color;
     private int depth = -1;
    private static final float TIME_TO_MOVE = 14000; // [ms]
     private float startTime = -1;
     private int evaluateOption;
 
     public AlphaBeta(Board board, boolean color){
         clonedBoard = new Board(board);
         this.color = color;
     }
 
     public boolean getColor() {
         return color;
     }
 
     public Move getBestMove(int depth, Board board, int evaluateOption){
         this.evaluateOption = evaluateOption;
         bestMove = null;
         clonedBoard = new Board(board);
 //        clonedBoard = board;
 
         this.depth = depth;
 
 
         startTime = System.nanoTime();
          alphaBetaMax(Integer.MIN_VALUE, Integer.MAX_VALUE, depth);
 
         float timeItTook = (System.nanoTime() - startTime) / 1000000;
 
         System.out.println("Move took: " + timeItTook/1000 + " s");
 
         return bestMove;
     }
 
     private int alphaBetaMax(int alpha, int beta, int depthLeft) {
 
         if(depthLeft == 0){
             return clonedBoard.evaluate(evaluateOption);
         }
 
         ArrayDeque<Move> allMoves = clonedBoard.getAllMovesForCurrentPlayer();
 
         for(Move move : allMoves){
 
 
             clonedBoard.makeMove(move, true);
             int score = alphaBetaMin(alpha, beta, depthLeft-1);
             clonedBoard.unmakeMove(move);
             move.setScore(score);
 
             if(depthLeft == depth){
                 if(bestMove != null){
                     if(move.getScore() > bestMove.getScore())
                         bestMove = move;
                 }
                 else
                         bestMove = move;
 
             }
 
 
             if(score >= beta){
                 return beta;
             }
             if(score > alpha){
                 alpha = score;
             }
         }
 
         return alpha;
     }
 
     private int alphaBetaMin(int alpha, int beta, int depthLeft) {
 
         if(depthLeft == 0){
             return clonedBoard.evaluate(evaluateOption);
         }
 
         ArrayDeque<Move> allMoves = clonedBoard.getAllMovesForCurrentPlayer();
 
         for(Move move : allMoves){
 
 
             clonedBoard.makeMove(move, true);
             int score = alphaBetaMax(alpha, beta, depthLeft-1);
             clonedBoard.unmakeMove(move);
 
             if(score <= alpha){
                 return alpha;
             }
             if(score < beta){
                 beta = score;
             }
         }
 
         return beta;
     }
 
 }
