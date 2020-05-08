 package com.github.jkschoen.jsma.misc;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.nio.file.Files;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 
 public class JSMAUtils {
 	
 	public static String uncapitalize(String input){
 		if (input == null){
 			return null;
 		}
 		if (input.length()==1){
 			return input.toLowerCase();
 		}
		return input.substring(0, 1).toLowerCase() + input.substring(1);
 	}
 
 	public static String md5(File file) throws NoSuchAlgorithmException,
 			IOException {
 		if (!file.exists() || !file.isFile()) {
 			return null;
 		}
 		return md5(Files.readAllBytes(file.toPath()));
 	}
 
 	public static String md5(String md5Me) throws NoSuchAlgorithmException,
 			UnsupportedEncodingException {
 		return md5(md5Me.getBytes("UTF-8"));
 	}
 
 	public static String md5(byte[] md5Me) throws NoSuchAlgorithmException {
 		MessageDigest md = MessageDigest.getInstance("MD5");
 		md.update(md5Me);
 
 		byte byteData[] = md.digest();
 
 		StringBuffer hexString = new StringBuffer();
 		for (int i = 0; i < byteData.length; i++) {
 			String hex = Integer.toHexString(0xff & byteData[i]);
 			if (hex.length() == 1)
 				hexString.append('0');
 			hexString.append(hex);
 		}
 		return hexString.toString();
 	}
 }
