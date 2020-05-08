 package other;
 
 /**
  * Created by Sobercheg on 11/23/13.
  * <p/>
 * A cup pyramid:
  * <pre>
  *        |_|        level=0
  *       |_|_|       level=1
  *      |_|_|_|      level=2
  *     |_|_|_|_|     ...
  * cup: 0 1 2 3 ...
  * </pre>
  * Each cup has 1 liter volume. Now someone pours some specific volume of water on the cup(0, 0).
  * Determine how much water there is in some particular cup (given level, cup).
  */
 public class CupPyramid {
 
     public double getVolume(int level, int cup, double initialVolume) {
         // base case
         if (level == 0) return initialVolume;
 
         double overflowVolumeFromLeftUpper = 0d;
         if (cup > 0) overflowVolumeFromLeftUpper = (getVolume(level - 1, cup - 1, initialVolume) - 1) / 2;
         if (overflowVolumeFromLeftUpper < 0) overflowVolumeFromLeftUpper = 0;
         double overflowVolumeFromRightUpper = 0d;
         if (cup < level) overflowVolumeFromRightUpper = (getVolume(level - 1, cup, initialVolume) - 1) / 2;
         if (overflowVolumeFromRightUpper < 0) overflowVolumeFromRightUpper = 0;
         return overflowVolumeFromLeftUpper + overflowVolumeFromRightUpper;
     }
 
     public static void main(String[] args) {
         CupPyramid pyramid = new CupPyramid();
         System.out.println(pyramid.getVolume(2, 1, 4));
     }
 }
