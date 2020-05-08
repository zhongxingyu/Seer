 package indaprojekt;
 
 /**
  * A class for things of general usefulness
  *
  */
 public class General 
 {
 	/**
 	 * Moves <code>a</code> towards zero with <code>b</code> units
 	 */
 	public static float towardsZero(float a, float b)
 	{
 		if (a < 0) {
			return a + b;
 		} else if (a > 0) {
			return a - b;
 		} else {
 			return a;
 		}
 	}
 }
