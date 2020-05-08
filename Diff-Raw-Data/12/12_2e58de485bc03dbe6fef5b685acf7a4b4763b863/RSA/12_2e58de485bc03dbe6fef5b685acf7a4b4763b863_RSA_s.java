 package crypt;
 
 import java.security.KeyPair;
 import java.security.KeyPairGenerator;
 import java.security.NoSuchAlgorithmException;
 
 public class RSA {
 	
	  public static KeyPair generateKeyPair() {
 		  	KeyPair keyPair = null;
 		    try {
 		      KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
		      gen.initialize(1024);
 		      keyPair = gen.generateKeyPair();
 		    } catch (NoSuchAlgorithmException e) {
 				// TODO Auto-generated catch block
 		    	e.printStackTrace();
 		    }
 			return keyPair;
 		  }
 }
