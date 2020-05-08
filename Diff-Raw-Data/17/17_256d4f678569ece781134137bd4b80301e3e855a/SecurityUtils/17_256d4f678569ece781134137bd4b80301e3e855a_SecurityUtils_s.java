 package com.madalla.util.security;
 
 import java.io.UnsupportedEncodingException;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 
 import org.apache.commons.lang.RandomStringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import sun.misc.BASE64Encoder;
 
 public class SecurityUtils {
 	
 	private static final Log log = LogFactory.getLog(SecurityUtils.class);
 
 	public static String encrypt(String string) {
 		try {
 			MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
 			messageDigest.update(string.getBytes("UTF-8"));
 			byte[] raw = messageDigest.digest();
 			String hash = new BASE64Encoder().encode(raw);
 			return hash;
 		} catch (NoSuchAlgorithmException e) {
 			log.error("encrypt - No Such Algorithm.",e);
 			throw new RuntimeException(e);
 		} catch (UnsupportedEncodingException e){
 			log.error("encrypt - UnsupportedEncodingException.",e);
 			throw new RuntimeException(e);
 		}
 	}
 	
 	public static String getGeneratedPassword(){
 		return RandomStringUtils.randomAlphanumeric(6);
 	}
 
 }
