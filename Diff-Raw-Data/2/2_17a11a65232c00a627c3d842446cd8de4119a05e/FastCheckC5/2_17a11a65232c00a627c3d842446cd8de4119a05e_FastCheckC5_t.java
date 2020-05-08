 package fastboard.checkmove.fastcheck.ccolumn;
 
 import fastboard.FastBoardLines;
 import fastboard.checkmove.FastCheck;
 
 /**
  * Created by IntelliJ IDEA.
  * User: knhjp
  * Date: Nov 29, 2009
  * Time: 03:19:22 PM
 * This class tells you, given the current configuration, whether or not c5 is a valid move
  */
 public class FastCheckC5 implements FastCheck {
     private boolean[][] fastCheckCalcArray;
 
     public FastCheckC5(boolean[][] fastCheckCalcArray) {
         this.fastCheckCalcArray = fastCheckCalcArray;
     }
 
     @Override public boolean isValidMove(FastBoardLines lines) {
         return
                 fastCheckCalcArray[3][lines.c1_c8] ||
                 fastCheckCalcArray[5][lines.a5_h5] ||
                 fastCheckCalcArray[3][lines.a3_f8] ||
                 fastCheckCalcArray[4][lines.a7_g1];
     }
 }
