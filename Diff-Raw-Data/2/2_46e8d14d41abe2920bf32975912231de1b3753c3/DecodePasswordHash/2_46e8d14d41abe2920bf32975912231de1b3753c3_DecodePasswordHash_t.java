 /**
  * Credit -> git://gist.github.com/1846191.git
 *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 package com.charandeepmatta.oracle.sqldeveloper;
 
 import static org.apache.commons.lang.StringUtils.isEmpty;
 import static org.apache.commons.lang.StringUtils.trimToEmpty;
 
 import java.security.GeneralSecurityException;
 
 import javax.crypto.Cipher;
 import javax.crypto.spec.IvParameterSpec;
 import javax.crypto.spec.SecretKeySpec;
 
 public class DecodePasswordHash {
     private final String passwordHash;
 
     public DecodePasswordHash(final String passwordHash) {
         this.passwordHash = passwordHash;
     }
 
     private String decode() {
         int hashLength = passwordHash.length();
         byte[] secret = new byte[hashLength / 2];
         for (int i = 0; i < hashLength; i += 2) {
             String pair = passwordHash.substring(i, i + 2);
             secret[i / 2] = (byte) Integer.parseInt(pair, 16);
         }
         try {
             return new String(decryptPassword(secret));
         } catch (GeneralSecurityException e) {
             throw new RuntimeException(e);
         }
     }
 
     private byte[] decryptPassword(final byte[] result) throws GeneralSecurityException {
         byte constant = result[0];
         if (constant != (byte) 5) {
             throw new IllegalArgumentException();
         }
         byte[] secretKey = new byte[8];
         System.arraycopy(result, 1, secretKey, 0, 8);
         byte[] encryptedPassword = new byte[result.length - 9];
         System.arraycopy(result, 9, encryptedPassword, 0, encryptedPassword.length);
         byte[] iv = new byte[8];
         for (int i = 0; i < iv.length; i++) {
             iv[i] = 0;
         }
         Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
         cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(secretKey, "DES"), new IvParameterSpec(iv));
         return cipher.doFinal(encryptedPassword);
     }
 
     public static void main(final String[] args) {
         String passwordHash = getValidatedPasswordHash(args);
         String decodedPassword = new DecodePasswordHash(passwordHash).decode();
         System.out.println(passwordHash + " = " + decodedPassword);
     }
 
     private static String getValidatedPasswordHash(final String[] args) {
         String passwordHash = trimToEmpty(getPasswordHash(args));
         if (isEmpty(passwordHash) || passwordHash.length() % 2 != 0) {
             System.err.println("Usage: java DecodePasswordHash <password>");
             System.err.println("Password must consist of hex pairs. Length must be even.");
             System.exit(1);
         }
         return passwordHash;
     }
 
     private static String getPasswordHash(final String[] args) {
         if (args.length != 1) {
             return System.getProperty("passwordHash");
         }
         return args[0];
     }
 }
