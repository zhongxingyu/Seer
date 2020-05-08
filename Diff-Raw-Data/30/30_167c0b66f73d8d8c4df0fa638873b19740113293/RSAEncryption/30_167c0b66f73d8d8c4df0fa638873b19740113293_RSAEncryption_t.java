 package com.battlegame;
 
 import java.io.BufferedInputStream;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.ObjectInputStream;
 import java.math.BigInteger;
 import java.security.InvalidKeyException;
 import java.security.KeyFactory;
 import java.security.NoSuchAlgorithmException;
 import java.security.PrivateKey;
 import java.security.PublicKey;
 import java.security.spec.RSAPrivateKeySpec;
 import java.security.spec.RSAPublicKeySpec;
 
 import javax.crypto.BadPaddingException;
 import javax.crypto.Cipher;
 import javax.crypto.IllegalBlockSizeException;
 import javax.crypto.NoSuchPaddingException;
 
 import android.content.res.AssetManager;
 
 // Code from
 // http://www.javamex.com/tutorials/cryptography/rsa_encryption_2.shtml
 
 // Note that this assumes key distribution already
 
 public class RSAEncryption {
 
 	public static String publicKeyType;
 	public static String privateKeyType;
 	
 	public static AssetManager assetMgr;
 	
 	public static PublicKey readPublicKeyFromFile() throws IOException {
 		InputStream in = assetMgr.open(publicKeyType + ".public.key");
 		ObjectInputStream oin = new ObjectInputStream(new BufferedInputStream(in));
 		try {
 			BigInteger m = (BigInteger) oin.readObject();
 			BigInteger e = (BigInteger) oin.readObject();
 			RSAPublicKeySpec keySpec = new RSAPublicKeySpec(m, e);
 			KeyFactory fact = KeyFactory.getInstance("RSA");
 			PublicKey pubKey = fact.generatePublic(keySpec);
 			return pubKey;
 		} catch (Exception e) {
 			throw new RuntimeException("Spurious serialisation error", e);
 		} finally {
 			oin.close(); 
 		}
 	}
 	
 	public static PrivateKey readPrivateKeyFromFile() throws IOException {
 		InputStream in = assetMgr.open(privateKeyType + ".private.key");
 		ObjectInputStream oin = new ObjectInputStream(new BufferedInputStream(in));
 		try {
 			BigInteger m = (BigInteger) oin.readObject();
 			BigInteger e = (BigInteger) oin.readObject();
 			RSAPrivateKeySpec keySpec = new RSAPrivateKeySpec(m, e);
 			KeyFactory fact = KeyFactory.getInstance("RSA");
 			PrivateKey privKey = fact.generatePrivate(keySpec);
 			return privKey;
 		} catch (Exception e) {
 			throw new RuntimeException("Spurious serialisation error", e);
 		} finally {
 			oin.close();
 		}
 	}
 
 	public static byte[] rsaEncrypt(byte[] data) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
 		PublicKey pubKey = readPublicKeyFromFile();
 		Cipher cipher = Cipher.getInstance("RSA");
 		cipher.init(Cipher.ENCRYPT_MODE, pubKey);
 		byte[] cipherData = cipher.doFinal(data);
 		return cipherData;
 	}
 	
 	public static byte[] rsaDecrypt(byte[] data) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
 		PrivateKey privKey = readPrivateKeyFromFile();
 		Cipher cipher = Cipher.getInstance("RSA");
 		cipher.init(Cipher.DECRYPT_MODE, privKey);
 		byte[] cipherData = cipher.doFinal(data);
 		return cipherData;
 	}
	
	public static byte[] rsaPrivateEncrypt(byte[] data) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		PrivateKey privKey = readPrivateKeyFromFile();
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.ENCRYPT_MODE, privKey);
		byte[] cipherData = cipher.doFinal(data);
		return cipherData;
	}
	
	public static byte[] rsaPublicDecrypt(byte[] data) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		PublicKey pubKey = readPublicKeyFromFile();
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.DECRYPT_MODE, pubKey);
		byte[] cipherData = cipher.doFinal(data);
		return cipherData;
	}
 }
