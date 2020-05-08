 package ch.hsr.waktu.services;
 
 import java.io.UnsupportedEncodingException;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 
 public class Md5 {
 	public static String hash(String input) {
 		MessageDigest digest = null;
		byte[] hash = null;
		
 		try {
 			digest = java.security.MessageDigest.getInstance("MD5");
 		} catch (NoSuchAlgorithmException e) {
 			e.printStackTrace();
 		}
 		try {
 			digest.update(input.getBytes("UTF-8"));
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 		}
		
		if(digest != null){
			hash = digest.digest();
		}
		
 		return byteArrayToString(hash);
 
 	}
 
 	public static String byteArrayToString(byte[] binaryData) {
 
 		final char[] hexadecimal = { '0', '1', '2', '3', '4', '5', '6', '7',
 				'8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
 
 		if (binaryData.length != 16)
 			return null;
 
 		char[] buffer = new char[32];
 
 		for (int i = 0; i < 16; i++) {
 			int low = (int) (binaryData[i] & 0x0f);
 			int high = (int) ((binaryData[i] & 0xf0) >> 4);
 			buffer[i * 2] = hexadecimal[high];
 			buffer[i * 2 + 1] = hexadecimal[low];
 		}
 
 		return new String(buffer);
 	}
 }
