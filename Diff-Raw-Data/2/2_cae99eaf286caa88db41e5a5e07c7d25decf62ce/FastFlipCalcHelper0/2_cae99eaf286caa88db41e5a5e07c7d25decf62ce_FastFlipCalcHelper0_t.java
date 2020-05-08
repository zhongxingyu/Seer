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
         return line%3 == 2;
     }
 
     @Override public boolean isBlack(int line) {
         return line%3 == 1;
     }
 
     @Override public boolean isEmpty(int line) {
        return line%3 == 0;
     }
 }
