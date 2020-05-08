 /**
  * 
  */
 package controllers.util;
 
 import java.security.NoSuchAlgorithmException;
 import java.security.SecureRandom;
 import java.security.spec.InvalidKeySpecException;
 import java.security.spec.KeySpec;
 
 import javax.crypto.SecretKeyFactory;
 import javax.crypto.spec.PBEKeySpec;
 
 import models.EMessages;
 
 import org.apache.commons.codec.binary.Hex;
 
 /**
  * @author Sander Demeester, Jens Rammant
  *
  */
 public class PasswordHasher {
 	
 	/**
	 * @param password the "plaintext" password
 	 * @return a container containing the fully hashed & hexed password and the hexed hash. Ready to be put in the
 	 *       database
 	 * @throws Exception 
 	 */
 	
	public static SaltAndPassword generateSP(char[] password) throws Exception{
 		
 		// Create object for returning salt and password.
 		SaltAndPassword saltAndPassword = new SaltAndPassword();
 		
 		 // Setup a secure PRNG
         SecureRandom random = null;
 
         // Init keyFactory to generate a random string using PBKDF2 with SHA1.
         SecretKeyFactory secretFactory = null;
 
         // Resulting password will be in a byte[] array.
         byte[] passwordByteString = null;
 
         // We will save the password in HEX-format in the database;
         String passwordHEX = "";
 
         // Same for salt
         String saltHEX = "";
         
 
         // Get instance of secureRandom.
         try {
             random = SecureRandom.getInstance("SHA1PRNG");
         } catch (NoSuchAlgorithmException e) {}
 
         byte[] salt = new byte[16]; //RSA PKCS5
 
         // Get salt
         random.nextBytes(salt);
 
         // Get the key for PBKDF2.
        KeySpec PBKDF2 = new PBEKeySpec(password, salt, 1000, 160);
 
         // init keyFactory.
         try{
             secretFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
         }catch(Exception e){
             throw new Exception(EMessages.get("error.text"));
         }
 
         // Generate password from PBKDF2.
         try {
             passwordByteString = secretFactory.generateSecret(PBKDF2).getEncoded();
         } catch (InvalidKeySpecException e) {}
         try{ // Encocde our byte arrays to HEX dumps (to save in the database).
             saltHEX = new String(Hex.encodeHex(salt));
             passwordHEX = new String(Hex.encodeHex(passwordByteString));
         }catch(Exception e){
             throw new Exception(EMessages.get("error.text"));
         }
         
         saltAndPassword.salt = saltHEX;
         saltAndPassword.password = passwordHEX;
         
         return saltAndPassword;
 	}
 
 	public static class SaltAndPassword{
 		
 		public String salt;
 		public String password;
 		
 	}
 }
