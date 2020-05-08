 package com.ardaxi.authenticator;
 
 import java.io.UnsupportedEncodingException;
 import java.security.AlgorithmParameters;
 import java.security.InvalidAlgorithmParameterException;
 import java.security.InvalidKeyException;
 import java.security.NoSuchAlgorithmException;
 import java.security.SecureRandom;
 import java.security.spec.InvalidKeySpecException;
 import java.security.spec.KeySpec;
 import java.util.Arrays;
 
 import javax.crypto.BadPaddingException;
 import javax.crypto.Cipher;
 import javax.crypto.IllegalBlockSizeException;
 import javax.crypto.KeyGenerator;
 import javax.crypto.NoSuchPaddingException;
 import javax.crypto.SecretKey;
 import javax.crypto.SecretKeyFactory;
 import javax.crypto.spec.IvParameterSpec;
 import javax.crypto.spec.PBEKeySpec;
 import javax.crypto.spec.SecretKeySpec;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.preference.PreferenceManager;
 import android.util.Base64;
 
 public class CryptoHelper {
 	private SecureRandom random;
 	private SharedPreferences preferences;
 	private Cipher encrypt, decrypt;
 	
 	/**
 	 * Constructs a new CryptoHelper deriving the password using values stored in preferences
 	 * If the values are absent, they are randomly generated
 	 * It is suggested to call close() as soon as possible
 	 * @param context The Context in which to save the preferences
 	 * @param password The password for encryption/decryption
 	 */
 	public CryptoHelper(Context context, char[] password)
 	{
 		random = new SecureRandom();
 		preferences = PreferenceManager.getDefaultSharedPreferences(context);
 		SecretKeyFactory factory;
 		try {
 			factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
			KeySpec spec = new PBEKeySpec(password, getBytes("salt"), 65536, 256);
 			Arrays.fill(password, '\u0000');
 			password = null;
 			SecretKey key = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
 			encrypt = getCipher(Cipher.ENCRYPT_MODE, key);
 			decrypt = getCipher(Cipher.DECRYPT_MODE, key);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public String encrypt(String plaintext)
 	{
 		try {
 			return Base64.encodeToString(encrypt.doFinal(plaintext.getBytes("UTF-8")),Base64.DEFAULT);
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 		} catch (IllegalBlockSizeException e) {
 			e.printStackTrace();
 		} catch (BadPaddingException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 	public String decrypt(String ciphertext)
 	{
 		try {
 			return new String(decrypt.doFinal(Base64.decode(ciphertext, Base64.DEFAULT)), "UTF-8");
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 		} catch (IllegalBlockSizeException e) {
 			e.printStackTrace();
 		} catch (BadPaddingException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 	
 	public void close()
 	{
 		encrypt = null;
 		decrypt = null;
 		System.gc();
 	}
 	
 	private byte[] getBytes(String param)
 	{
 		byte[] bytes;
 		String str = preferences.getString(param, "");
 		if(!str.equals(""))
 			return Base64.decode(str, Base64.DEFAULT);
 		bytes = param.equals("IV") ? new byte[16] : new byte[8];
 		random.nextBytes(bytes);
 		preferences.edit().putString(param, Base64.encodeToString(bytes, Base64.DEFAULT)).commit();
 		return bytes;
 	}
 	
 	private Cipher getCipher(int opmode, SecretKey key) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException
 	{
 		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
 		cipher.init(opmode, key, new IvParameterSpec(getBytes("IV")));
 		return cipher;
 	}
 }
