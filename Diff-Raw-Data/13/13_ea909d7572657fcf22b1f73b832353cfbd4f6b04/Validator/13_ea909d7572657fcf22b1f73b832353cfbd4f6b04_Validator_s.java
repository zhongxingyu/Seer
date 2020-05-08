 package jipdbs.core.util;
 
 import java.util.regex.Pattern;
 
 import org.apache.commons.lang.StringUtils;
 
 public final class Validator {
 
 	private final static String IP_RE = "^(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d|[\\*])){3}$";
 	private final static String CLIENT_ID_RE = "^@([0-9]+)$";
 	private final static String MAIL_RE = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
 	
 	public static boolean isValidIp(String value) {
 		return Pattern.matches(IP_RE, value);
 	}
 	
 	public static boolean isValidClientId(String value) {
 		return Pattern.matches(CLIENT_ID_RE, value);
 	}
 
 	public static boolean isValidEmail(String value) {
 		return Pattern.matches(MAIL_RE, value);
 	}
 
 	public static boolean isValidPlayerName(String value) {
 		if (StringUtils.isEmpty(value)) return false;
 
 		for (int i = 0; i < value.length(); i++)
 			if (!validPlayerNameChar(value.charAt(i)))
 				return false;
 		return true;
 	}
 
 	private static boolean validPlayerNameChar(char c) {
 		// Continuously improve this.
 		return c < 256 && c != ' ';
 	}	
 }
