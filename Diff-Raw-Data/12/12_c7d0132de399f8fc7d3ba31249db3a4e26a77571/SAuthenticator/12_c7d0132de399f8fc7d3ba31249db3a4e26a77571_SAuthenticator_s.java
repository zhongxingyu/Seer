 package server;
 
 import java.util.Hashtable;
 /**
  * Class used to authenticate a client
  */
 public class SAuthenticator {
 	Hashtable<String, String> passwords;
 	
 	public SAuthenticator() {
 		passwords = new Hashtable<String, String>();
 		passwords.put("user", "pass");
 		passwords.put("me","test");
 		passwords.put("bla","mypass");
 		passwords.put("qwe","rty");
 		passwords.put("emma","emma");
 		passwords.put("vali","vali");
 		passwords.put("test","test");
 		passwords.put("unu","unu");
 	}
 	
 	public boolean authenticate(String user, String password) {
 		String ps = passwords.get(user);
 		if (password.equals(ps))
 			return true;
 		return false;
 	}
 }
