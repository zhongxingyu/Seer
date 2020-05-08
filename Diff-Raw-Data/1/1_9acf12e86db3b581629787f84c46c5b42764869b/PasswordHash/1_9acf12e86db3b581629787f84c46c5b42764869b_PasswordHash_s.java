 package hash;
 
 import java.math.BigInteger;
 import java.security.NoSuchAlgorithmException;
 import java.security.SecureRandom;
 import java.security.spec.InvalidKeySpecException;
 import java.sql.SQLException;
 import java.util.ArrayList;
 
 import javax.crypto.SecretKeyFactory;
 import javax.crypto.spec.PBEKeySpec;
 
 import sql.User;
 import db.DBAccess;
 
 /*
  * PBKDF2 salted password hashing.
  * Based on class developed by: havoc AT defuse.ca
  */
 public class PasswordHash {
 	public static final String PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA1";
 
 	// The following constants may be changed without breaking existing hashes.
 	public static final int SALT_BYTES = 24;
 	public static final int HASH_BYTES = 24;
 	public static final int PBKDF2_ITERATIONS = 1000;
 
 	public static final int ITERATION_INDEX = 0;
 	public static final int SALT_INDEX = 1;
 	public static final int PBKDF2_INDEX = 2;
 	
 	private DBAccess dbaccess = new DBAccess();
 
 	/**
 	 * Returns a salted PBKDF2 hash of the password.
 	 * 
 	 * @param password: the password to hash
 	 * @return a salted PBKDF2 hash of the password
 	 */
 	public static String createHash(String password)
 			throws NoSuchAlgorithmException, InvalidKeySpecException {
 		return createHashRandom(password.toCharArray());
 	}
 
 	/** //REMOVE//
 	 * Returns a salted PBKDF2 hash of the password.
 	 * 
 	 * @param password: the password to hash
 	 * @return a salted PBKDF2 hash of the password
 	 */
 	public static String createHashRandom(char[] password)
 			throws NoSuchAlgorithmException, InvalidKeySpecException {
 		// Generates a random salt
 		SecureRandom random = new SecureRandom();
 		byte[] salt = new byte[SALT_BYTES];
 		random.nextBytes(salt);
 
 		// Hashes the password
 		byte[] hash = pbkdf2(password, salt, PBKDF2_ITERATIONS, HASH_BYTES);
 		
 		// Format iterations:salt:hash
 		return PBKDF2_ITERATIONS + ":" + toHex(salt) + ":" + toHex(hash);
 	}
 	
 	/**
 	 * Generates a random salt.
 	 * 
 	 * @return a random salt
 	 */
 	public String createRandomSalt()
 			throws NoSuchAlgorithmException, InvalidKeySpecException {
 		SecureRandom random = new SecureRandom();
 		byte[] salt = new byte[SALT_BYTES];
 		random.nextBytes(salt);
 
 		return toHex(salt);
 	}
 	
 	/**
 	 * Validates a password using a hash.
 	 * 
 	 * @param password: the password to check
 	 * @param userID: the id used to get a respective salt and hash in the database
 	 * @return true if the password is correct, false if not
 	 */
 	public boolean validatePassword(char[] password, String userID)
 			throws NoSuchAlgorithmException, InvalidKeySpecException,
 			SQLException, InstantiationException, IllegalAccessException,
 			ClassNotFoundException {
 		String tempHash = "a";
 		byte[] salt = null;
 		String hash = "";
 		User user = new User(dbaccess);
 		ArrayList<ArrayList<String>> result = new ArrayList<ArrayList<String>>();
 		try {
 			user.getSaltAndHash(result, userID);
 			if (!result.isEmpty()) {
 				ArrayList<String> userInfo = result.get(0);
 				// Gets salt and hash from database
 				salt = userInfo.get(0).toString().getBytes();
 				hash = userInfo.get(1).toString();
 				// Creates a hash using the typed password and the real salt
 				// returned from database for the entered user.
 				tempHash = createHash(password, salt);
 			}
 		} catch (Exception error) {
 			error.printStackTrace();
 		}
 		/* Number of Connections
 		ResultSet connections = DBConnection.getInstance().getNumberOfConnections();
 		while (connections.next()) {
 			System.out.println(connections.getRow());
 		}
 		*/
 		dbaccess.closeConnection();
 		return hash.equals(tempHash);
 	}
 	
 	/**
 	 * Creates a hash using a password and a salt.
 	 * 
 	 * @param password: the password to check
 	 * @param userID: the id used to get a respective salt and hash in the database
 	 * @return true if the password is correct, false if not
 	 */
 	public static String createHash(char[] password, byte[] salt)
 			throws NoSuchAlgorithmException, InvalidKeySpecException {
 		// Hashes the password
 		byte[] hash = pbkdf2(password, salt, PBKDF2_ITERATIONS, HASH_BYTES);
 		// Delivery format: "iterations:salt:hash"
 		return toHex(hash);
 	}
 
 	/**
 	 * Validates a password using a hash.
 	 * 
 	 * @param password: the password to check
 	 * @param goodHash: the hash of the valid password
 	 * @return true if the password is correct, false if not
 	 */
 	public static boolean validatePassword(String password, String goodHash)
 			throws NoSuchAlgorithmException, InvalidKeySpecException {
 		// Decode the hash into its parameters
 		String[] params = goodHash.split(":");
 		int iterations = Integer.parseInt(params[ITERATION_INDEX]);
 		byte[] salt = fromHex(params[SALT_INDEX]);
 		byte[] hash = fromHex(params[PBKDF2_INDEX]);
 		// Compute the hash of the provided password, using the same salt,
 		// iteration count, and hash length
 		byte[] testHash = pbkdf2(password.toCharArray(), salt, iterations,
 				hash.length);
 		// Compare the hashes in constant time. The password is correct if
 		// both hashes match.
 		return slowEquals(hash, testHash);
 	}
 
 	/**
 	 * Compares two byte arrays in length-constant time. This comparison method
 	 * is used so that password hashes cannot be extracted from an on-line
 	 * system using a timing attack and then attacked off-line.
 	 * 
 	 * @param a: the first byte array
 	 * @param b: the second byte array
 	 * @return true if both byte arrays are the same, false if not
 	 */
 	private static boolean slowEquals(byte[] a, byte[] b) {
 		int diff = a.length ^ b.length;
 		for (int i = 0; i < a.length && i < b.length; i++)
 			diff |= a[i] ^ b[i];
 		return diff == 0;
 	}
 
 	/**
 	 * Computes the PBKDF2 hash of a password.
 	 * 
 	 * @param password: the password to hash.
 	 * @param salt: the salt
 	 * @param iterations: the iteration count (slowness factor)
 	 * @param bytes: the length of the hash to compute in bytes
 	 * @return the PBDKF2 hash of the password
 	 */
 	private static byte[] pbkdf2(char[] password, byte[] salt, int iterations,
 			int bytes) throws NoSuchAlgorithmException, InvalidKeySpecException {
 		PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, bytes * 8);
 		SecretKeyFactory skf = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM);
 		return skf.generateSecret(spec).getEncoded();
 	}
 
 	/**
 	 * Converts a string of hexadecimal characters into a byte array.
 	 * 
 	 * @param hex: the hex string
 	 * @return the hex string decoded into a byte array
 	 */
 	private static byte[] fromHex(String hex) {
 		byte[] binary = new byte[hex.length() / 2];
 		for (int i = 0; i < binary.length; i++) {
 			binary[i] = (byte) Integer.parseInt(
 					hex.substring(2 * i, 2 * i + 2), 16);
 		}
 		return binary;
 	}
 
 	/**
 	 * Converts a byte array into a hexadecimal string.
 	 * 
 	 * @param array: the byte array to convert
 	 * @return a length*2 character string encoding the byte array
 	 */
 	private static String toHex(byte[] array) {
 		BigInteger bi = new BigInteger(1, array);
 		String hex = bi.toString(16);
 		int paddingLength = (array.length * 2) - hex.length();
 		if (paddingLength > 0)
 			return String.format("%0" + paddingLength + "d", 0) + hex;
 		else
 			return hex;
 	}
 }
