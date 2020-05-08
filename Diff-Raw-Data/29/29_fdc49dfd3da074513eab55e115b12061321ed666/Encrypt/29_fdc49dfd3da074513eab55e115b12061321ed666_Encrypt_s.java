 package com.tommytony.io;
 
 import java.security.InvalidKeyException;
 import java.security.Key;
 import java.security.NoSuchAlgorithmException;
 
 import javax.crypto.BadPaddingException;
 import javax.crypto.Cipher;
 import javax.crypto.IllegalBlockSizeException;
 import javax.crypto.KeyGenerator;
 import javax.crypto.NoSuchPaddingException;
 import javax.crypto.SecretKey;
 import javax.crypto.spec.SecretKeySpec;
 
 public class Encrypt {
 
	public static String encryptWithAES(String data, String key) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		byte[] keyBytes = key.getBytes();
		if(keyBytes.length < 16) {
			for(int i = 0; i < (16 - keyBytes.length); i++) {
				keyBytes[keyBytes.length + i] = keyBytes[i];
 			}
		} else if(keyBytes.length > 16) {
			for(int i = 0; keyBytes.length != 16; i--) {
				keyBytes[keyBytes.length + i] = (Byte) null;
 			}
 		}
 		Cipher aes = Cipher.getInstance("AES");
		SecretKeySpec keymaker = new SecretKeySpec(keyBytes, "AES");
 		aes.init(Cipher.ENCRYPT_MODE, keymaker);
 		return new String(aes.doFinal(data.getBytes()));
 	}
 }
