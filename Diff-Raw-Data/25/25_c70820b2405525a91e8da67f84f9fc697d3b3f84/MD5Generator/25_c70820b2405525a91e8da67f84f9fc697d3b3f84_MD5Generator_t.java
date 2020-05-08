 package be.artesis.timelog.secure;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 
 
 public class MD5Generator {
 	
 	private int index;
 	
 	public MD5Generator() {
 		index = 0;
 	}
 	
 	public String gen(String paswoord) throws NoSuchAlgorithmException {
 		
 		final String secret = "|@#[{^644.4654f 4r-CHRONOMATIC-e4f8r7$^,;:='--@#^!846^{ !{!";
 		paswoord += secret;
 		MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
 		digest.update(paswoord.getBytes());
 		byte[] hash = digest.digest();
 		
 		StringBuilder sb = new StringBuilder();
         for (int i = 0; i < hash.length; ++i) {
           sb.append(Integer.toHexString((hash[i] & 0xFF) | 0x100).substring(1,3));
         }
         
         String pass = sb.toString();
         // Maak MD5 1000 maal, recursief
        /*while (index < 1000) {
         	index++;
         	return gen(pass);
         }*/
         return pass;
 	}
 
 }
