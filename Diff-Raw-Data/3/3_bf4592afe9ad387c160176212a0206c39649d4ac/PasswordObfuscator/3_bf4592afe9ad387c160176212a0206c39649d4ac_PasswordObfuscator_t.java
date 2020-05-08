 /*
  * Copyright (C) 2011 by Alexandre Jasmin <alexandre.jasmin@gmail.com>
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 
 package com.github.ajasmin.telususageandroidwidget;
 
 import java.math.BigInteger;
 import java.security.GeneralSecurityException;
 import java.security.KeyFactory;
 import java.security.PrivateKey;
 import java.security.PublicKey;
 import java.security.interfaces.RSAPrivateCrtKey;
 import java.security.spec.KeySpec;
 import java.security.spec.PKCS8EncodedKeySpec;
 import java.security.spec.RSAPublicKeySpec;
 
 import javax.crypto.Cipher;
 
 import android.content.res.Resources;
 
 
 public class PasswordObfuscator {
     private static final PrivateKey privateKey = getPrivateKey();
     private static final PublicKey publicKey = getPublicKey(privateKey);
 
     /**
      * Obfuscate a password making it sightly harder to read.
      * This offers no real security as the key is embedded within the app.
      * @param password
      * @return obfuscated password
      */
     public static String obfuscate(String password) {
        if (password == null) {return null;}
         try {
             Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
             cipher.init(Cipher.ENCRYPT_MODE, publicKey);
             byte[] encrypted = cipher.doFinal(password.getBytes("UTF-8"));
             return toHex(encrypted);
         } catch (Exception e) {
             throw new Error(e);
         }
     }
 
     /**
      * Unobfoscate a password
      * This offers no real security.
      * @param obfuscated
      * @return
      */
     public static String unobfuscate(String obfuscated) {
        if (obfuscated == null) {return null;}
         try {
             Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
             cipher.init(Cipher.DECRYPT_MODE, privateKey);
             byte[] unobfuscated = cipher.doFinal(fromHex(obfuscated));
             return new String(unobfuscated, "UTF-8");
         } catch (Exception e) {
             throw new Error(e);
         }
     }
 
     private static byte[] fromHex(String hex) {
         return new BigInteger(hex, 16).toByteArray();
     }
 
     private static String toHex(byte[] bytes) {
         return String.format("%x", new BigInteger(bytes));
     }
 
     private static PublicKey getPublicKey(PrivateKey privateKey) {
         try {
             RSAPrivateCrtKey rsaKey = (RSAPrivateCrtKey) privateKey;
             BigInteger modulus = rsaKey.getModulus();
             BigInteger exponent = rsaKey.getPublicExponent();
             RSAPublicKeySpec keySpec = new RSAPublicKeySpec(modulus, exponent);
             return KeyFactory.getInstance("RSA").generatePublic(keySpec);
         } catch (GeneralSecurityException e) {
             throw new Error(e);
         }
     }
 
     private static PrivateKey getPrivateKey() {
         try {
             Resources resources = MyApp.getContext().getResources();
             byte[] keyBytes = Util.readStream(resources.openRawResource(R.raw.password_key));
             KeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
             return KeyFactory.getInstance("RSA").generatePrivate(keySpec);
         } catch (Exception e) {
             throw new Error(e);
         }
     }
 }
