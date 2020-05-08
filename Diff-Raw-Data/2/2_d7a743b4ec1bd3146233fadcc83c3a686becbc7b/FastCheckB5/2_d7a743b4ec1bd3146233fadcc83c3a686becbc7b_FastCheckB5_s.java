 package fastboard.checkmove.fastcheck.bcolumn;
 
 import fastboard.FastBoardLines;
 import fastboard.checkmove.FastCheck;
 
 /**
  * Created by IntelliJ IDEA.
  * User: knhjp
  * Date: Nov 29, 2009
  * Time: 03:19:22 PM
 * This class tells you, given the current configuration, whether or not a5 is a valid move
  */
 public class FastCheckB5 implements FastCheck {
     private boolean[][] fastCheckCalcArray;
 
     public FastCheckB5(boolean[][] fastCheckCalcArray) {
         this.fastCheckCalcArray = fastCheckCalcArray;
     }
 
     @Override public boolean isValidMove(FastBoardLines lines) {
         return
                 fastCheckCalcArray[3][lines.b1_b8] ||
                 fastCheckCalcArray[6][lines.a5_h5] ||
                 fastCheckCalcArray[3][lines.a4_e8] ||
                 fastCheckCalcArray[4][lines.a6_f1];
     }
 }
