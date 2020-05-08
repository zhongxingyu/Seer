 package fastboard.checkmove.fastcheck.dcolumn;
 
 import fastboard.FastBoardLines;
 import fastboard.checkmove.FastCheck;
 
 /**
  * Created by IntelliJ IDEA.
  * User: knhjp
  * Date: Nov 29, 2009
  * Time: 03:19:22 PM
 * This class tells you, given the current configuration, whether or not c6 is a valid move
  */
 public class FastCheckD6 implements FastCheck {
     private boolean[][] fastCheckCalcArray;
 
     public FastCheckD6(boolean[][] fastCheckCalcArray) {
         this.fastCheckCalcArray = fastCheckCalcArray;
     }
 
     @Override public boolean isValidMove(FastBoardLines lines) {
         return
                 fastCheckCalcArray[2][lines.d1_d8] ||
                 fastCheckCalcArray[4][lines.a6_h6] ||
                 fastCheckCalcArray[2][lines.a3_f8] ||
                 fastCheckCalcArray[4][lines.b8_h2];
     }
 }
