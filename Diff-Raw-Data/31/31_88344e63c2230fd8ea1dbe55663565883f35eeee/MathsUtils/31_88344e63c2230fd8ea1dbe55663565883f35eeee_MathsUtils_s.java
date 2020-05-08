 /**
  * See http://www.presage2.info/ for more details on Presage2
  */
 package uk.ac.imperial.dws04.utils.MathsUtils;
 
 /**
  * @author dws04
  *
  */
 public class MathsUtils {
 
 	/**
 	 * Convenience method for intuitively handling negative modulo
 	 * @param a
 	 * @param b
 	 * @return a % b (by ((a%b)+b)%b)
 	 */
 	public static final int mod(int a, int b) {
		return (((a%b)+b)%b );
 	}
 	
 }
