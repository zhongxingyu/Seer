 package net.hexid;
 
 import java.util.List;
 import java.io.File;
 
 public class Utils {
 	/**
 	 * Combine strings with a common string
 	 * @param glue
 	 * @param str
 	 * @return A single string
 	 */
 	public static String join(String glue, String... str) {
 		if(str.length > 0) {
 			StringBuilder sb = new StringBuilder(str[0]);
 			for(int i = 1; i < str.length; i++)
 				sb.append(glue).append(str[i]);
 			return sb.toString();
 		} else return "";
 	}
 
 	/**
 	 * Combine a list of strings with a common string
 	 * @param glue
 	 * @param str
 	 * @return {@link #join(String,String...)}
 	 */
 	public static String join(String glue, List<String> str) {
 		return join(glue, str.toArray(new String[str.size()]));
 	}
 
 	public static String joinFile(String... str) {
 		return join(File.separator, str);
 	}
 
 	/**
 	 * Get the directory that the application is being run from
 	 * @return directory
 	 */
 	public static File getPWD() {
 		return new File(Utils.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getParentFile();
 	}
 
 	public static String[] appendStrToArray(String[] arr, String str) {
 		String[] array = new String[arr.length+1];
		for(int i = 0; i < arr.length-1; i++) {
 			array[i] = arr[i];
 		}
 		array[arr.length] = str;
 		return array;
 	}
 }
