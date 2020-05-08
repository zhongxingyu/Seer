 package util;
 
 import java.util.HashMap;
 import java.util.Random;
 import java.io.UnsupportedEncodingException;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 
 public class PasswordManager {
 
 	public class HashedPassword {
 		private byte[] hash;
 		private byte[] salt;
 
 		public HashedPassword(byte[] hash, byte[] salt) {
 			this.hash = hash;
 			this.salt = salt;
 		}
 
 		public byte[] getHash() {
 			return hash;
 		}
 
 		public byte[] getSalt() {
 			return salt;
 		}
 	}
 	
 	private HashMap<String, HashedPassword> passwords = new HashMap<String, HashedPassword>();
 	private Random rand = new Random();
 	
 	public PasswordManager() {
 		
 	}
 	
 	public byte[] getHash(int iterationNb, String password, byte[] salt) throws NoSuchAlgorithmException, UnsupportedEncodingException {
 	       MessageDigest digest = MessageDigest.getInstance("SHA-1");
 	       digest.reset();
 	       digest.update(salt);
 	       byte[] input = digest.digest(password.getBytes("UTF-8"));
 	       for (int i = 0; i < iterationNb; i++) {
 	           digest.reset();
 	           input = digest.digest(input);
 	       }
 	       return input;
 	   }
 	
 	public void createPassword(String username, String password) {
 		
 		// Create random salt
 		byte[] salt = new byte[64];
 	    rand.nextBytes(salt); // Random salt value
 	    
 	    byte[] hash = null;
 		try {
 			hash = getHash(1, password, salt);
 			
 			if (hash != null) {
 				// Store password for user
 				passwords.put(username, new HashedPassword(hash, salt));			
 			}
 			
 			return;
 			
 			// FIXME: log these errors.. 
 		} catch (NoSuchAlgorithmException e) {
 			e.printStackTrace();
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 		}
 		
 		// FIXME: FATAL ERROR
 	}
 	
	public boolean checkPassword(String username, String hash) {
 		return passwords.get(username).getHash().equals(hash);
 	}
 	
 	public byte[] getSalt(String username) {
 		return passwords.get(username).getSalt();
 	}
 }
