 package pw.svn.util;
 
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.Random;
 
 public class MessageManipulator {
 
 	private static MessageDigest digest;
 	static String[] emoticons = { "o_o", ":D", ":I", "xD", ":|", ":O", ":P",
 		"._.", ">.<", "(o_o)", ":L", ":c)", ">:/", "." };
 	private static Random rand = new Random();
 	
 	public static void main(String[] args) {
 		System.out.println(manipulate("alo"));
 	}
 	
 	public static String manipulate(String message) {
 		boolean done = false;
 		String tmp = message;
 		StringBuilder sb = new StringBuilder();
 		
 		int i = 0;
 		while (!done) {
 			
 			tmp = tmp.replaceFirst(" ?:-?[a-zA-Z\\(\\)\\[\\]]{1,1} ", (char) 0x4 + " "
 					+ emoticons[rand.nextInt(emoticons.length)] + " " + (char) 0x3);
 			if (tmp.contains((char) 0x3 + "")) {
				sb.append(hash(tmp.substring(0, tmp.indexOf((char) 0x4) - 1).trim()) + tmp.substring(tmp.indexOf((char) 0x4) + 1, tmp.indexOf((char) 0x3)));
 				tmp = tmp.substring(tmp.indexOf((char) 0x3) + 1);
 				i++;
 			} else {
 				done = true;
 				if (i == 0) {
 					sb.append(hash(tmp).trim());
 				}
 			}
 		}
 		message = sb.toString();
 
 		return message;
 
 	}
 
 	
 	/**
 	 * Converts the bytes in the MessageDigest into hex
 	 * @return sb.toString() - the bytes in the MessageDigest in hex
 	 */
 	public static String hash(String message){
 		StringBuilder messageBuilder = new StringBuilder();
 		messageBuilder.append(message);
 		messageBuilder.append(getSalt());
 		try {
 			digest = MessageDigest.getInstance("MD5");
 			digest.update(messageBuilder.toString().getBytes());
 		} catch (NoSuchAlgorithmException e) {
 			e.printStackTrace();
 		}
 		StringBuffer sb = new StringBuffer();
 		byte[] byteData = digest.digest();
         for (int i = 0; i < byteData.length; i++) {
          sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
         }
         return sb.toString();
 	}
 	
 	public static String getSalt(){
 		StringBuilder sb = new StringBuilder();
 		for(int i = 0; i < 30; i++){
 			sb.append(Integer.toHexString(rand.nextInt(16)));
 		}
 		return sb.toString();
 	}
 	
 }
