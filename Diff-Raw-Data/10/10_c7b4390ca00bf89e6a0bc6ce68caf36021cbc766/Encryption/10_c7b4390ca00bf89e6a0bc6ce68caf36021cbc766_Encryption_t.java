 package edu.cs408.vormund;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.security.InvalidKeyException;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.Arrays;
 
 import javax.crypto.*;
 import javax.crypto.spec.SecretKeySpec;
 
 import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
 
 public class Encryption {
 
 	public static byte[] encryptHash(String userPassword) throws NoSuchAlgorithmException {
 		MessageDigest sha1 = MessageDigest.getInstance("SHA-256");
 		byte[] bytes=null;
 		try {
 			bytes = userPassword.getBytes("UTF-8");
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 		}
 		byte[] hashed = sha1.digest(bytes);
 
 		return hashed;
 	}
 
 	public static String encryptHashString(String userPassword) throws NoSuchAlgorithmException {
 
 		return byteArrayToHexString(encryptHash(userPassword));
 	}
 
 	public static byte[] encryptBlob(String key, String dataBlob){
 		byte[] keyBytes = key.getBytes();
 		byte[] blobBytes = dataBlob.getBytes();
 
 		Cipher cipher=null;
 		try {
 			cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
 		} catch (NoSuchAlgorithmException e) {
 			e.printStackTrace();
 		} catch (NoSuchPaddingException e) {
 			e.printStackTrace();
 		}
         SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES");
         try {
 			cipher.init(Cipher.ENCRYPT_MODE, secretKey);
 		} catch (InvalidKeyException e) {
 			e.printStackTrace();
 		}
         String encryptedString=null;
 		try {
 			encryptedString = Base64.encode(cipher.doFinal(blobBytes));
 		} catch (IllegalBlockSizeException e) {
 			e.printStackTrace();
 		} catch (BadPaddingException e) {
 			e.printStackTrace();
 		}
 
         return encryptedString.getBytes();
 	}
 
 	public static String decryptBlob(String key, byte[] dataBlob){
 		Cipher cipher=null;
 		try {
 			cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
 		} catch (NoSuchAlgorithmException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (NoSuchPaddingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	    SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "AES");
 	    try {
 			cipher.init(Cipher.DECRYPT_MODE, secretKey);
 		} catch (InvalidKeyException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 	    String decrypted = null;
 
 	    try {
 			decrypted = new String(cipher.doFinal(Base64.decode(new String(dataBlob))));
 		} catch (IllegalBlockSizeException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (BadPaddingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	    return decrypted;
 	}
	
 	public static String byteArrayToHexString(byte[] b) {
 		String result = "";
 		for (int i=0; i < b.length; i++) {
 			result +=
 			    Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring( 1 );
 		}
 		return result;
 	}
}
