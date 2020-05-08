 /**
  * CSE 403 AA
  * Project Nonogram: Backend
  * @author  HyeIn Kim
  * @version v1.0, University of Washington
  * @since   Spring 2013
  */
 

 package uw.cse403.nonogramfun.utility;
 

 /**
  * ParameterPolice is a police officer that helps keeping state of other object
  * valid by checking validity of parameters passed to their methods
  * (ParameterPolice class is singleton and all its methods are static).
  * It arrests(scary!) the execution of program by throwing various exceptions when
  * parameters are turned out to be invalid!
  */
 public class ParameterPolice {
 

 	// An empty private constructor.
		private ParameterPolice() { }
 
 
 	/**
 	 * Accepts an object as a parameter and throws an
 	 * IllegalArgumentException if it is null.
 	 * @param o An object to be checked if it is null.
 	 * @throws IllegalArgumentException if the passed object is null.
 	 */
 	public static void checkIfNull(Object o, String objectName) {
 		if (o == null) {
 			throw new IllegalArgumentException("Error: " + objectName + " is null!");
 		}
 	}
 
 	/**
 	 * Accepts an 2D array and throws an IllegalArgumentException
 	 * if the passed array is null or contains null element.
 	 * @param array An array to be checked for validity.
 	 * @throws IllegalArgumentException if the passed array is null or contains null element.
 	 */
 	public static void checkIfValid2DArray(Object[][] array) {
 		checkIfNull(array, "Array");
 		for (int i=0; i<array.length; i++) {
			for (int j=0; i<array[0].length; j++) {
 				checkIfNull(array[i][j], "Array Element at [" + i + "][" + j + "]");
      }
 		}
 	}
 
 	/**
 	 * Throws IllegalArgumentException if given index is out of range, [lowB, upB)
 	 * @param i - index to be checked
 	 * @param lowB - inclusive lower bound of i
 	 * @param upB - exclusive upper bound of i
 	 */
 	public static void checkIfWithinRange(int i, int lowB, int upB) {
 		if (i < lowB || upB <= i) {
 	        throw new IllegalArgumentException("Error: Given number (" + 
             i + ") is out of bound [" + lowB + ", " + upB + ")!");
 		}
 	}
 
 	/**
 	 * Throws IllegalArgumentException if given number is not positive 
 	 * @param n - number to be checked
 	 */
 	public static void checkIfNotPositive(int n) {
 		if (n <= 0) {
 			throw new IllegalArgumentException("Error: Given number \"" + 
         n + "\" is not positive!");
 		}
 	}
 
 	/**
 	 * Throws IllegalArgumentException if given number is negative
 	 * @param n - number to be checked
 	 */
 	public static void checkIfNegative(int n) {
 		if (n < 0) {
 			throw new IllegalArgumentException("Error: Given number \"" +
         n + "\" is negative!");
 		}
 	}
 }
 
