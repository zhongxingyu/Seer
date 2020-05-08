 package com.lucropia.security;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.math.BigInteger;
 import java.security.InvalidKeyException;
 import java.security.KeyFactory;
 import java.security.NoSuchAlgorithmException;
 import java.security.interfaces.RSAPublicKey;
 import java.security.spec.InvalidKeySpecException;
 import java.security.spec.RSAPublicKeySpec;
 
 import javax.crypto.BadPaddingException;
 import javax.crypto.Cipher;
 import javax.crypto.IllegalBlockSizeException;
 import javax.crypto.NoSuchPaddingException;
 
 public class RSAUtils {
 	protected RSAPublicKey publicKey = null;
 
 	protected static RSAPublicKey getPublicKey(BigInteger modulus,
 			BigInteger exponent) throws LucropiaException {
 		String errmsg = null;
 		try {
 			RSAPublicKeySpec pubKeySpec = new RSAPublicKeySpec(modulus,
 					exponent);
 			KeyFactory factory = KeyFactory.getInstance("RSA");
 			RSAPublicKey pubKey = (RSAPublicKey) factory
 					.generatePublic(pubKeySpec);
 			return pubKey;
 		} catch (NoSuchAlgorithmException e) {
 			errmsg = String.format("NoSuchAlgorithmException: %s\r\n",
 					e.getMessage());
 		} catch (InvalidKeySpecException e) {
 			errmsg = String.format("InvalidKeySpecException: %s\r\n",
 					e.getMessage());
 		}
 		throw new LucropiaException(errmsg);
 	}
 
 	protected static byte[] encryptWithPublicKey(byte[] input, RSAPublicKey key)
 			throws LucropiaException {
 		String errmsg;
 		try {
 			Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
 			int start = 0;
			int maxPlainLength = key.getModulus().bitCount() / 8;
 			cipher.init(Cipher.ENCRYPT_MODE, key);
 			ByteArrayOutputStream stream = new ByteArrayOutputStream();
 			while (start < input.length) {
 				int len = (start + maxPlainLength) > input.length ? (input.length - start)
 						: maxPlainLength;
 				stream.write(cipher.doFinal(input, start, len));
 				start += len;
 			}
 			byte[] outbuf = stream.toByteArray();
 			System.err.print(String.format("cipher length: %d\r\n",
 					outbuf.length));
 			return outbuf;
 		} catch (NoSuchAlgorithmException e) {
 			errmsg = String.format("NoSuchAlgorithmException: %s\r\n",
 					e.getMessage());
 		} catch (NoSuchPaddingException e) {
 			errmsg = String.format("NoSuchPaddingException: %s\r\n",
 					e.getMessage());
 		} catch (InvalidKeyException e) {
 			errmsg = String.format("InvalidKeyException: %s\r\n",
 					e.getMessage());
 		} catch (IllegalBlockSizeException e) {
 			errmsg = String.format("IllegalBlockSizeException: %s\r\n",
 					e.getMessage());
 		} catch (BadPaddingException e) {
 			errmsg = String.format("BadPaddingException: %s\r\n",
 					e.getMessage());
 		} catch (IOException e) {
 			// hardly happen
 			errmsg = String.format("IOException: %s\r\n", e.getMessage());
 		}
 		if (errmsg != null) {
 			System.err.print(errmsg);
 			throw new LucropiaException(errmsg);
 		}
 		return null;
 	}
 
 	public RSAUtils(BigInteger modulus, BigInteger exponent) {
 		try {
 			this.publicKey = RSAUtils.getPublicKey(modulus, exponent);
 		} catch (LucropiaException e) {
 			System.err
 					.print(String.format("init fail: %s\r\n", e.getMessage()));
 		}
 	}
 
 	public byte[] encryptPublicKey(byte[] input) throws LucropiaException {
 		return RSAUtils.encryptWithPublicKey(input, this.publicKey);
 	}
 
 	public byte[] encryptPublicKey(String input) throws LucropiaException,
 			UnsupportedEncodingException {
 		return this.encryptPublicKey(input, "utf-8");
 	}
 
 	public byte[] encryptPublicKey(String input, String charsetName)
 			throws LucropiaException, UnsupportedEncodingException {
 		byte[] inputbytes = input.getBytes(charsetName);
 		return RSAUtils.encryptWithPublicKey(inputbytes, this.publicKey);
 	}
 
 	public static byte[] encryptRSAPublicKey(byte[] input, BigInteger modulus,
 			BigInteger exponent) throws LucropiaException {
 		RSAPublicKey pubKey = RSAUtils.getPublicKey(modulus, exponent);
 		byte[] outbuf = RSAUtils.encryptWithPublicKey(input, pubKey);
 		return outbuf;
 	}
 
 	public static byte[] encryptRSAPublicKey(String input, BigInteger modulus,
 			BigInteger exponent) throws UnsupportedEncodingException,
 			LucropiaException {
 		return RSAUtils.encryptRSAPublicKey(input, "utf-8", modulus, exponent);
 	}
 
 	public static byte[] encryptRSAPublicKey(String input, String charsetName,
 			BigInteger modulus, BigInteger exponent)
 			throws UnsupportedEncodingException, LucropiaException {
 		byte[] inputbytes = input.getBytes(charsetName);
 		return RSAUtils.encryptRSAPublicKey(inputbytes, modulus, exponent);
 	}
 }
