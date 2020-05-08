 /**
  * Copyright (C) 2011 Adriano Monteiro Marques
  *
  * Author:  Zubair Nabi <zn.zubairnabi@gmail.com>
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
  * USA
  */
 
 package org.umit.icm.mobile.utils;
 
 import java.io.IOException;
 import java.security.KeyFactory;
 import java.security.NoSuchAlgorithmException;
 import java.security.PrivateKey;
 import java.security.PublicKey;
 import java.security.spec.InvalidKeySpecException;
 import java.security.spec.RSAPrivateKeySpec;
 import java.security.spec.RSAPublicKeySpec;
 
 public class CryptoKeyWriter {
 	
 	public static void writeMyPublicKey(PublicKey publicKey) 
 		throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
 		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
 		RSAPublicKeySpec publicKeySpec 
 		= keyFactory.getKeySpec(publicKey, RSAPublicKeySpec.class);
     	RSACrypto.saveKey(Constants.MY_PUBLIC_KEY_FILE
     			, publicKeySpec.getModulus()
     			, publicKeySpec.getPublicExponent());
 	}
 	
 	public static void writeMyPrivateKey(PrivateKey privateKey) 
 	throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
 	KeyFactory keyFactory = KeyFactory.getInstance("RSA");
 	RSAPrivateKeySpec privateKeySpec 
 	= keyFactory.getKeySpec(privateKey, RSAPrivateKeySpec.class);
 	RSACrypto.saveKey(Constants.MY_PRIVATE_KEY_FILE
 			, privateKeySpec.getModulus()
 			, privateKeySpec.getPrivateExponent());
 }
 	
 	public static void writeMySecretKey(byte[] secretKey) throws IOException {
 		AESCrypto.saveKey(Constants.MY_SECRET_KEY_FILE, secretKey);
 	}
 	
 	public static void writeMyCipheredKey(byte[] myCipheredKey) throws IOException, RuntimeException {
		SDCardReadWrite.writeString(Constants.MY_CIPHERED_KEY_FILE
				, Constants.KEYS_DIR, new String(myCipheredKey));
 	}
 	
 	public static void writePeerPublicKey(PublicKey publicKey, String peerIP) 
 	throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
 	KeyFactory keyFactory = KeyFactory.getInstance("RSA");
 	RSAPublicKeySpec publicKeySpec 
 	= keyFactory.getKeySpec(publicKey, RSAPublicKeySpec.class);
 	RSACrypto.saveKey(peerIP+Constants.PEER_PUBLIC_KEY_FILE
 			, publicKeySpec.getModulus()
 			, publicKeySpec.getPublicExponent());
 	}
 }
