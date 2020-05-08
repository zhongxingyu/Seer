 package core.util;
 
 import java.io.UnsupportedEncodingException;
 import java.math.BigInteger;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 
 /**
  * Helper methods for strings.
  * 
  * @author jani
  * 
  */
 public class StringUtil {
 	/**
 	 * Gets the SHA-256 hash code of a string.
 	 * 
 	 * @param input
 	 *            The string to hash..
 	 * @return the SHA-256 hash code.
 	 */
 	public static String sha256(String input) {
 		MessageDigest digest;
 		try {
 			digest = MessageDigest.getInstance("SHA-256");
 
 			digest.reset();
 			byte[] bytes = digest.digest(input.getBytes("UTF-8"));
 
 			BigInteger bigInt = new BigInteger(1, bytes);
			return bigInt.toString(16);
 
 		} catch (NoSuchAlgorithmException e) {
 			assert false;
 		} catch (UnsupportedEncodingException e) {
 			assert false;
 		}
 
 		return null;
 	}
 }
