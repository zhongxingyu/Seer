 package fastboard.checkmove.helper;
 
 /**
  * Created by IntelliJ IDEA.
  * User: knhjp
  * Date: Nov 8, 2009
  * Time: 11:37:10 AM
  * This class is used to determine the color of square 0
  */
 public class FastFlipCalcHelper0 implements FastFlipCalcHelper {
     @Override public boolean isWhite(int line) {
        return false;
     }
 
     @Override public boolean isBlack(int line) {
         return false;
     }
 
     @Override public boolean isEmpty(int line) {
         return false;
     }
 }
