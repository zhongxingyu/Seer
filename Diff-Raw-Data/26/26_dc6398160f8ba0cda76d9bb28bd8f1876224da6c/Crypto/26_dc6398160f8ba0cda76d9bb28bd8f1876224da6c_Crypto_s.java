 package ch.hsr.bieridee.android.utils;
 
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
	 * Uses an MD5 hash of the username as salt.
 	 *
 	 * @param password The password that should be hashed
 	 * @param username The username
 	 * @return Hashed password
 	 */
 	public static String hashUserPw(final String password, final String username) {
		final String salt = DigestUtils.md5Hex(username);
		return BCrypt.hashpw(password, salt);
 	}
 
 }
