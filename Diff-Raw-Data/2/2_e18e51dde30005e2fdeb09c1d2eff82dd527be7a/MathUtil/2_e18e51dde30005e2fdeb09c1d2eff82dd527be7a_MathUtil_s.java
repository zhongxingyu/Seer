 /**
  * 
  */
 package gov.nih.nci.caintegrator.util;
 
 import java.util.Collections;
 import java.util.List;
 
 /**
  * @author sahnih
  * 
  */
 
 public class MathUtil {
 	/**
 	 * The method returns you the log2 value
 	 * 
 	 */
 	public static Double getLog2(Double value) {
 		if (value != null) {
 			return Math.log(value) / Math.log(2);
 		}
 		return null;
 	}
 
 	/**
 	 * The method returns you the antilogarithm of log base 2
 	 * 
 	 */
 	public static Double getAntiLog2(Double value) {
 		// Calculate the antilogarithm of log base 2
 		if (value != null) {
 			return Math.pow(2.0, value);
 		}
 		return null;
 	}
 	
 	/**
 	 * This method returns you the median value for a List of Doubles
 	 * @param list
 	 * @return
 	 */
 	public static Double median(List<Double> list) {
 		   //	  List must be first sorted
 	   Collections.sort(list);
 	   int middle = list.size()/2;  // subscript of middle element
 	   if (list.size()%2 == 1) {
 	       // Odd number of elements -- return the middle one.
 	       return list.get(middle);
 	   } else {
 	      // Even number -- return average of middle two
 	      // Must cast the numbers to double before dividing.
	      return (list.get(middle-1) + list.get(middle) / 2.0);
 	   }
 	}
 	
 	/**
 	 * This method return a random number between the range of m & n
 	 * @param m
 	 * @param n
 	 * @return
 	 */
 	public static double getRandom(double m, double n) {
 		double a=Math.max(m, n);
 		double b=Math.min(m, n);
 		  return Math.floor(Math.random()*(a-b))+b;
 		}
 }
