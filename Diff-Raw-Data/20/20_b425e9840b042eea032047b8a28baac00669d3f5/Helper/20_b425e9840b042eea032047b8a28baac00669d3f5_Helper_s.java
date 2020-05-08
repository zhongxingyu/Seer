 package darep;
 
 import java.io.File;
 
 /**
  * Contains some useful static methods that don't specifically belong
  * to a package.
  */
 public class Helper {
 
 	/**
 	 * Returns true, if the given array contains the object "search".
 	 * Uses the .equals() method for comparison.
 	 * @param search
 	 * @param array
 	 * @return
 	 */
 	public static <T> boolean arrayContains(T search, T[] array) {
 		for (T currItem: array) {
 			if (currItem.equals(search)) {
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	/**
 	 * Tests if two arrays are permutations of each other
 	 * (same length and same values, but maybe in a different order)
 	 * @param a1
 	 * @param a2
 	 * @return
 	 */
 	public static <T> boolean arrayIsPermutation(T[] a1, T[] a2) {
 		boolean sameLength = (a1.length == a2.length);
 		
 		// create array with booleans which items are in the other array
 		boolean[] cmp = new boolean[a1.length];
 		for (int i = 0; i < a1.length; i++) {
 			cmp[i] = arrayContains(a1[i], a2);
 		}
 		
 		// if one value of cmp-array is false return false
 		for (boolean curr: cmp) {
 			if (!curr) return false;
 		}
 		
 		// if all values of cmp-array are true and arrays have the same length,
 		// return true
 		return (true && sameLength);
 	}
 	
 	public static boolean deleteDir(File dir) {
 		if (!dir.exists()) {
 			return true;
 		}
 		if (dir.isDirectory()) {
 			for (File subfile: dir.listFiles()) {
 				boolean success = deleteDir(subfile);
 				if (!success) {
 					return false;
 				}
 			}
 		}
 		return dir.delete();
 	}
 
 	public static String stringToLength(String string, int length) {
 		if (string.length() > length) {
 			return string.substring(0, length);
 		} else if (string.length() < length) {
 			StringBuilder sb = new StringBuilder();
 			while (sb.length() + string.length() < length) {
 				sb.append(" ");
 			}
 			sb.append(string);
 			return sb.toString();
 		} else {
 			return string;
 		}
 	}
 	
 }
