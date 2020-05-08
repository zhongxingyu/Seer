 package org.geometerplus.expansion.crypto;
 
 import java.io.InputStream;
 
 /**
  * User: mda
  * Date: 1/30/13
  * Time: 5:11 PM
  */
 public class AESEncryptorFBreader {
     private static Encryptor sEncryptor;
     private static String sPassword;
 
     public static void init(Encryptor.EncryptorSetting encryptorSetting){
         sEncryptor = new Encryptor(encryptorSetting);
     }
 
     public static boolean isEncrypted(InputStream inputStream){
         return EncryptorUtils.isEncrypted(inputStream, sEncryptor.getEncryptedFilePrefix());
     }
 
     public static InputStream decrypt(InputStream inputStream){
         return sEncryptor.decrypt(inputStream, sPassword);
     }
 
     public static void update(String password) {
         sPassword = password;
     }
 }
