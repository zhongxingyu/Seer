 package fastboard.checkmove.fastcheck.ccolumn;
 
 import fastboard.FastBoardLines;
 import fastboard.checkmove.FastCheck;
 
 /**
  * Created by IntelliJ IDEA.
  * User: knhjp
  * Date: Nov 22, 2009
  * Time: 12:47:58 AM
  * This class tells you, given the current configuration, whether or not c1 is a valid move
  */
 public class FastCheckC1 implements FastCheck {
     private boolean[][] fastCheckCalcArray;
 
     public FastCheckC1(boolean[][] fastCheckCalcArray) {
         this.fastCheckCalcArray = fastCheckCalcArray;
     }
 
     @Override public boolean isValidMove(FastBoardLines lines) {
         return
                 fastCheckCalcArray[7][lines.c1_c8] ||
                 fastCheckCalcArray[5][lines.a1_h1] ||
                fastCheckCalcArray[5][lines.c1_h6];
     }
 }
