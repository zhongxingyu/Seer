 package cz.pavel.uugooglesync.utils;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class HtmlParser {
 	
	/** Default string encoding */
	private static final String DEFAULT_ENCODING = "UTF-8";
	
 	/**
 	 * Cache of compiled regexp patterns.
 	 */
 	private static final Map<String, Pattern> compiledPatterns = new HashMap<String, Pattern>();
 	
 	public static String getContents(InputStream is) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is, DEFAULT_ENCODING));
 		StringBuilder result = new StringBuilder();
 		String line = reader.readLine();
 		while (line != null) {
 			result.append(line);
 			line = reader.readLine();
 		}
 		return result.toString();
 	}
 	
 	public static String extractRegExp(String data, String regexp) {
		return extractRegExp(data, regexp, false);
		
	}
	
	public static String extractRegExp(String data, String regexp, boolean decode) {
 		Pattern pattern = compiledPatterns.get(regexp);
 		if (pattern == null) {
 			pattern = Pattern.compile("(?i)" + regexp);
 			compiledPatterns.put(regexp, pattern);
 		}
 		Matcher matcher = pattern.matcher(data);
		String result = matcher.find() ? matcher.group(1).trim() : "";
		if (decode) {
			result = result.replace("&amp;", "&");
 		}
		return result;
 	}
 
 }
