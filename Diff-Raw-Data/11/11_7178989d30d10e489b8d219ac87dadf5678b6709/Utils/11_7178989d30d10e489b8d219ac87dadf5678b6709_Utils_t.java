 package net.hexid;
 
 public class Utils {
 	/**
 	 * Combine strings with a common string
 	 * @param glue
 	 * @param str
	 * @return A single string
 	 */
 	public static String join(String glue, String... str) {
 		StringBuilder sb = new StringBuilder();
 		int i;
 		for(i = 0; i < str.length-1; i++)
 			sb.append(str[i] + glue);
 		return sb.toString() + str[i];
 	}

 	/**
 	 * Combine a list of strings with a common string
 	 * @param glue
 	 * @param str
 	 * @return {@link #join(String, String...)}
 	 */
	public static String join(String glue, java.util.List<String> str) {
 		return join(glue, str.toArray(new String[0]));
 	}
 }
