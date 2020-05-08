 package util;
 
 import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
 
 public class Security {
 
 	private final static String secLog = "security.log";
 	
 	public static String md5(String cpwd){
 		
		MessageDigest md5 = null;
 		try {
			md5 = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
 			GenericLogger gl = new GenericLogger(secLog);
			gl.logError("Error: " + cpwd + " can't be hashed");
 		}
		
		return (new HexBinaryAdapter()).marshal(md5.digest(cpwd.getBytes())).toLowerCase();
 	}
	
	public static void main(String[] args){
		System.out.println(Security.md5("test"));
	}
	
 }
