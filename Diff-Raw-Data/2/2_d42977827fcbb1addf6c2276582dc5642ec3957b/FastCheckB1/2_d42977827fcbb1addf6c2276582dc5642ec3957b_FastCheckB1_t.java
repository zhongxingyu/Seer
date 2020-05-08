 package fastboard.checkmove.fastcheck.bcolumn;
 
 import fastboard.FastBoardLines;
 import fastboard.checkmove.FastCheck;
 
 /**
  * Created by IntelliJ IDEA.
  * User: knhjp
  * Date: Nov 22, 2009
  * Time: 12:47:58 AM
 * This class tells you, given the current configuration, whether or not b1 is a valid move
  */
 public class FastCheckB1 implements FastCheck {
     private boolean[][] fastCheckCalcArray;
 
     public FastCheckB1(boolean[][] fastCheckCalcArray) {
         this.fastCheckCalcArray = fastCheckCalcArray;
     }
 
     @Override public boolean isValidMove(FastBoardLines lines) {
         return
                 fastCheckCalcArray[7][lines.b1_b8] ||
                 fastCheckCalcArray[6][lines.a1_h1] ||
                 fastCheckCalcArray[6][lines.b1_h7];
     }
 }
