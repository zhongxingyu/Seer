 package gov.cida.sedmap.io.util;
 
 import java.security.SecureRandom;
 
 import org.apache.log4j.Logger;
 
 public class StrUtils {
 
 	private static final Logger logger = Logger.getLogger(StrUtils.class);
	public static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
 
 
 	public static int occurrences(String substr, String str) {
 		int index = 0;
 		int count = 0;
 
 		while (index != -1) {
 
 			index = str.indexOf(substr, index);
 
 			if (index != -1) {
 				count++;
 				index += substr.length();
 			}
 		}
 
 		return count;
 	}
 
 
 	public static String uniqueName(int length) {
 		logger.debug("uniqueName");
 		StringBuilder name = new StringBuilder();
 
 		SecureRandom rand = new SecureRandom(); // TODO maybe include seed bytes
 
 		for (int c=0; c<length; c++) {
 			int ch = rand.nextInt( CHARS.length() );
 			name.append( CHARS.charAt(ch) );
 		}
 
 		return name.toString();
 	}
 
 
 
 }
