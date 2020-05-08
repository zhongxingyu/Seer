 package util;
 
 import java.security.MessageDigest;
 
 public class Security {
 
 	private final static String secLog = "security.log";
 	
 	public static String md5(String cpwd){
 		
		String md5hash = "";
 		try {
			byte[] bytes = cpwd.getBytes("UTF-8");
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] md5 = md.digest(bytes);
			md5hash = new String (md5, "UTF-8");
		} catch (Exception e) {
 			GenericLogger gl = new GenericLogger(secLog);
			gl.logError(cpwd + " is not hashable");
			gl.closeLogger();
 		}
		return md5hash;
 	}
 }
