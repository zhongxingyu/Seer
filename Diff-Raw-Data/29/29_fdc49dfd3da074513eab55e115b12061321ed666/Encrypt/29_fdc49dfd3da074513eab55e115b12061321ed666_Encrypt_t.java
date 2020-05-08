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
 
	public static String encryptWithAES(String data, byte[] key) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		if(key.length < 16) {
			for(int i = 0; i < (16 - key.length); i++) {
				key[key.length + i] = key[i];
 			}
		} else if(key.length > 16) {
			for(int i = 0; key.length != 16; i--) {
				key[key.length + i] = (Byte) null;
 			}
 		}
 		Cipher aes = Cipher.getInstance("AES");
		SecretKeySpec keymaker = new SecretKeySpec(key, "AES");
 		aes.init(Cipher.ENCRYPT_MODE, keymaker);
 		return new String(aes.doFinal(data.getBytes()));
 	}
 }
