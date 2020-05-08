 package fastboard.checkmove.fastcheck.ccolumn;
 
 import fastboard.FastBoardLines;
 import fastboard.checkmove.FastCheck;
 
 /**
  * Created by IntelliJ IDEA.
  * User: knhjp
  * Date: Nov 29, 2009
  * Time: 03:19:22 PM
 * This class tells you, given the current configuration, whether or not c8 is a valid move
  */
 public class FastCheckC8 implements FastCheck {
     private boolean[][] fastCheckCalcArray;
 
     public FastCheckC8(boolean[][] fastCheckCalcArray) {
         this.fastCheckCalcArray = fastCheckCalcArray;
     }
 
     @Override public boolean isValidMove(FastBoardLines lines) {
         return
                 fastCheckCalcArray[0][lines.c1_c8] ||
                 fastCheckCalcArray[5][lines.a8_h8] ||
                 fastCheckCalcArray[5][lines.c8_h3] ||
                 fastCheckCalcArray[0][lines.a6_c8];
     }
 }
