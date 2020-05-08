 package com.exadel.borsch.util;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.math.BigInteger;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 
 
 /**
  * @author Andrey Zhilka
  */
 public final class Encoder {
     private Encoder() {
     }
 
    public static final Logger LOGGER = LoggerFactory.getLogger(Encoder.class);

     public static String encodeWithMD5(String toEncode, String salt)
         throws RuntimeException {
         String hash;
         String forHash = salt + toEncode;
 
         MessageDigest m;
 
         try {
             m = MessageDigest.getInstance("MD5");
             m.update(forHash.getBytes(), 0, forHash.length());
            int length = 16;
            hash = new BigInteger(1, m.digest()).toString(length);
         } catch (NoSuchAlgorithmException e) {
             LOGGER.trace("MD5 algorithm was not found", e);
             throw new RuntimeException();
         }
         return hash;
     }
 }
 
