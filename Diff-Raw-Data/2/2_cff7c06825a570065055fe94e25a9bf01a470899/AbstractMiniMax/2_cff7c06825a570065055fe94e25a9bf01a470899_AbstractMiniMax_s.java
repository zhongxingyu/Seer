 package search.minimax;
 
 import search.TreeSearch;
 import base.board.Board;
 
 /**
  * Created by IntelliJ IDEA.
  * User: knhjp
  * Date: 16-Sep-2009
  * Time: 6:12:11 PM
  * This class does the naive minimax search
  */
 public abstract class AbstractMiniMax implements TreeSearch {
     private final static int minVal =-65; //this value has to be lower than the worst possible score (-64)
 
     @Override public int search(int color, Board[] board, int empties) {
         int bestScore = minVal;
         for (int curLocation = 11 ; curLocation<89; curLocation++) {
             if (curLocation%10 == 9) {
                 curLocation +=2;
             }
 
             if (board[empties].isMoveValid(color,curLocation)) {
                 board[empties-1].copyBoard(board[empties]);
                 board[empties-1].makeMove(color,curLocation);
                 bestScore = Math.max(bestScore,-search(-color,board,empties-1));
             }
         }
 
         if (bestScore == minVal) { //this happens in case of a pass
            return handlePass(color,board,empties);
         } else {
             return bestScore;
         }
     }
 
     abstract int handlePass(int color, Board[] board, int empties);
 }
