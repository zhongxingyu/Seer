 package ch.hsr.bieridee.android.utils;
 
import org.apache.commons.codec.binary.Hex;
 import org.apache.commons.codec.digest.DigestUtils;
 import org.mindrot.jbcrypt.BCrypt;
 
 /**
  * Utility class with cryptography related static functions.
  */
 public final class Crypto {
 
 	private Crypto() {
 		// Do not instantiate
 	}
 
 	/**
 	 * Hashes a user password using the bcrypt algorithm.
	 * Uses the first 22 characters of a SHA256 hash of the username as salt.
 	 *
 	 * @param password The password that should be hashed
 	 * @param username The username
 	 * @return Hashed password
 	 */
 	public static String hashUserPw(final String password, final String username) {
		final byte[] sha256 = DigestUtils.sha256(username);
		final String salt = new String(Hex.encodeHex(sha256)).substring(0,22);
		return BCrypt.hashpw(password, "$2$10$" + salt);
 	}
 
 }
