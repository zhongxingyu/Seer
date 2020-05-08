 /*
  * TopStack (c) Copyright 2012-2013 Transcend Computing, Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the License);
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an AS IS BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.msi.tough.security;
 
 import java.io.UnsupportedEncodingException;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 
 import javax.annotation.Resource;
 import javax.crypto.SecretKey;
 import javax.crypto.spec.SecretKeySpec;
 
 import org.apache.commons.codec.binary.Base64;
 import org.bouncycastle.crypto.DataLengthException;
 import org.bouncycastle.crypto.InvalidCipherTextException;
 import org.bouncycastle.crypto.engines.AESEngine;
 import org.bouncycastle.crypto.paddings.PKCS7Padding;
 import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
 import org.bouncycastle.crypto.params.KeyParameter;
 
 import com.google.common.base.Charsets;
 import com.msi.tough.core.Appctx;
 
 public class AESSecurity {
 
     private static AESSecurity instance = null;
 
     @Resource(name="DB_PASSWORD")
     private String systemPassword = null;
 
     public static AESSecurity getInstance() {
         if (instance == null) {
             instance = new AESSecurity();
         }
         return instance;
     }
 
     public static String encrypt(String message) {
         byte[] encrypted;
         try {
             encrypted = Base64.encodeBase64(getInstance().process(true,
                     message.getBytes("UTF-8")));
         } catch (UnsupportedEncodingException e) {
             throw new IllegalStateException(
                     "Cannot convert string to byte array using encoding specified.");
         }
 
         try {
             return new String(encrypted, "UTF-8");
         } catch (UnsupportedEncodingException e) {
             throw new IllegalStateException(
                     "Encrypted bytes cannot be returned as a string with encoding specified.");
         }
     }
 
     public static String decrypt(String encrypted) {
         byte[] decrypt = getInstance().process(false,
                 Base64.decodeBase64(encrypted.getBytes()));
         try {
             return new String(decrypt, "UTF-8").trim();
         } catch (UnsupportedEncodingException e) {
             throw new IllegalStateException(
                     "Decrypted bytes cannot be returned as a string with encoding specified.");
         }
 
     }
 
     private byte[] process(boolean encrypt, byte[] message) {
         try {
             // generate a SHA-256 for the password (install/db password) to use
             // a secret key for encryption.
             MessageDigest md = MessageDigest.getInstance("SHA-256");
 
             md.update(getDefaultPassword());
             byte[] passwordSHA = md.digest();
             final SecretKey sk = new SecretKeySpec(passwordSHA, "AES");
 
             KeyParameter key = new KeyParameter(sk.getEncoded());
             PaddedBufferedBlockCipher cipher = new PaddedBufferedBlockCipher(
                     new AESEngine(), new PKCS7Padding());
             cipher.init(encrypt, key);
             byte[] output = new byte[cipher.getOutputSize(message.length)];
             int bytesWrittenOut = cipher.processBytes(message, 0,
                     message.length, output, 0);
             cipher.doFinal(output, bytesWrittenOut);
             cipher.reset();
             return output;
         } catch (NoSuchAlgorithmException e) {
             throw new IllegalStateException("No such algorithm.");
         } catch (DataLengthException e) {
             throw new IllegalStateException(
                     "Cannot finalize due to bad data length in cipher.");
         } catch (IllegalStateException e) {
             throw new IllegalStateException(
                     "Illegal state: this might be caused by a " +
                     "bad key or password being used");
         } catch (InvalidCipherTextException e) {
             throw new IllegalStateException(
                     "Cipher text being used is not valid.");
         }
     }
 
     private byte[] getDefaultPassword() {
         if (this.systemPassword == null) {
             String systemPassword = Appctx.getBean("DB_PASSWORD");
             if (systemPassword == null) {
                 throw new NullPointerException(
                         "No install password can be found.");
             }
             return systemPassword.getBytes(Charsets.UTF_8);
         }
        return this.systemPassword.getBytes(Charsets.UTF_8);
     }
 }
