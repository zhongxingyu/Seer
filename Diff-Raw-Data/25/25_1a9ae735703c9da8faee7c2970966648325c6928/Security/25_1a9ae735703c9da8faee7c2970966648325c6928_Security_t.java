 package controllers;
 
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 
 import org.apache.commons.codec.binary.Base64;
 
 import com.sun.corba.se.spi.orbutil.fsm.Input;
 
 import play.libs.Crypto;
 import play.i18n.Messages;
 import models.User;
 
 /**
  * Temporary extension to make referencing easier
  * May later be extended with new features etc
  * 
  * @author dschlyter
  */
 public class Security extends Secure.Security
 {
 	
 	// To make fetching easier
 	public static User connectedUser()
 	{
		return User.find("byUsernameIlike", Security.connected()).first();
 	}
 	
 	/**
 	 * Check if any of the arguments is the connected user
 	 * @param users list of users that are authorized
 	 * @return true if connected user is one of the arguments, otherwise false.
 	 */
 	public static boolean isAuthorized(User ... users)
 	{
 		User connected = connectedUser();
 		if (connected == null)
 			return false;
 		
 		for (User user : users)
 			if (connected.equals(user))
 				return true;
 				
 		return false;
 	}
 	
 	public static String sha512Hash(String username, String password) throws NoSuchAlgorithmException
 	{
 		// FIXME(Peksa): Play only has support for shitty md5, patch at:
 		// https://github.com/hhandoko/play/commit/51622677ca9cd8312cd994ae5a3b03524eb3fdc0
 		// but it's not yet in an official version of play.
 		// String hash = Crypto.passwordHash(password);
 		return new String(Base64.encodeBase64(MessageDigest.getInstance("SHA-512").digest((username + password).getBytes())));
 	}
 	
 	static boolean authenticate(String username, String password)
 	{
 		if (validation.hasErrors())
 			error(Messages.get("paramsFail"));
		User user = User.find("byUsernameIlike", username).first();
 		if (user == null || user.password == null)
 			return false;
 
 		try
 		{
			String hash = sha512Hash(user.username, password);
 			if (hash.equals(user.password))
 				return true;
 		}
 		catch (NoSuchAlgorithmException e)
 		{
 			error("Couldn't find SHA-512");
 			return false;
 		}
 		return false;
 		
 	}
 }
