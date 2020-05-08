 package darep;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.nio.channels.FileChannel;
 
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
 	
 	public static boolean deleteRecursive(File file) {
 		if(file == null)
 			return true;
 		if (!file.exists()) {
 			return true;
 		}
 		if (file.isDirectory()) {
 			for (File subfile: file.listFiles()) {
 				boolean success = deleteRecursive(subfile);
 				if (!success) {
 					return false;
 				}
 			}
 		}
 		return file.delete();
 	}
 
 	public static final boolean ALIGN_LEFT = true;
 	public static final boolean ALIGN_RIGHT = false;
 	public static String stringToLength(String string, int length, boolean alignment) {
 		// TODO Helper.stringToLength left and right aligned
 		if(string == null)
 			return null;
 		if(length < 0)
 			return null;
 		if (string.length() > length) {
 			return string.substring(0, length);
 		} else if (string.length() < length) {
 			
 			String space = stringTimes(" ", length - string.length());
 			if (alignment == ALIGN_LEFT) {
 				return string + space;
 			} else {
 				return space + string;
 			}
 			
 		} else {
 			return string;
 		}
 	}
 	
 	public static String stringTimes(String s, int i) {
 		
 		if (i <= 0) {
 			return "";
 		}
 		
 		StringBuilder sb = new StringBuilder();
 		for (int j = 0; j < i; j++) {
 			sb.append(s);
 		}
 		return sb.toString();
 	}
 	
 	public static String streamToString(InputStream stream) throws FileNotFoundException, IOException{
 		BufferedReader reader = null;
 		StringBuffer sb = new StringBuffer();
 
 		try {
 			reader = new BufferedReader(new InputStreamReader(stream));
 			String s;
 			while ((s = reader.readLine()) != null) {
 				// \n or \r\n depending on OS
 				sb.append(s).append(System.getProperty("line.separator"));
 			}
 		} finally {
 			reader.close();
 		}
 		return sb.toString();
 	}
 	
 	public static void copyRecursive(File from, File to) throws IOException {
 	
 		File canonicalFrom = from.getCanonicalFile();
 		File canonicalTo = to.getCanonicalFile();
 		
 		if (canonicalFrom.isDirectory()) {
 			canonicalTo.mkdir();
 			File[] content = canonicalFrom.listFiles();
 			for (int i = 0; i < content.length; i++) {
 				copyRecursive(content[i],
 						new File(canonicalTo.getPath(), content[i].getName()));
 			}
 		} else {
 			FileChannel source = null;
 			FileChannel destination = null;
 			source = new FileInputStream(canonicalFrom).getChannel();
 			destination = new FileOutputStream(canonicalTo).getChannel();
 			try {
 				destination.transferFrom(source, 0, source.size());
 			} finally {
 				source.close();
 				destination.close();
 			}
 		}
 	}
 	
 	
 }	
