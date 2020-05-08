 /*
  * Copyright (c) 2011. KloudTek Ltd
  */
 
 package com.kloudtek.util;
 
import sun.misc.IOUtils;

 import java.io.IOException;
 import java.io.InputStream;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.security.SecureRandom;
 import java.util.Arrays;
 
 public class CryptoUtils {
     private static final SecureRandom random = new SecureRandom();
 
     public static MessageDigest createDigest( Algorithm alg ) {
         try {
             return MessageDigest.getInstance(alg.id);
         } catch (NoSuchAlgorithmException e) {
             throw new UnexpectedException(e);
         }
     }
 
     public static byte[] createDigest(byte[] data, Algorithm alg) {
         try {
             MessageDigest sha = MessageDigest.getInstance(alg.id);
             sha.update(data);
             return sha.digest();
         } catch (NoSuchAlgorithmException e) {
             throw new UnexpectedException(e);
         }
     }
 
     public static byte[] createDigest(InputStream inputStream, Algorithm alg) throws IOException {
         try {
             byte[] buffer = new byte[8192];
             MessageDigest digest = MessageDigest.getInstance(alg.id);
            for ( int i = inputStream.read(buffer) ; i != -1 ; i = inputStream.read()) {
                 digest.update(buffer,0,i);
             }
             return digest.digest();
         } catch (NoSuchAlgorithmException e) {
             throw new UnexpectedException("Algorithm not supported: "+alg,e);
         }
     }
 
     public static byte[] createSaltedDigest(byte[] data, Algorithm alg) {
         return createSaltedDigest(generateSalt(), data, alg);
     }
 
     public static byte[] createSaltedDigest(byte[] salt, byte[] data, Algorithm alg) {
         try {
             MessageDigest sha = MessageDigest.getInstance(alg.id);
             sha.update(data);
             sha.update(salt);
             byte[] digest = sha.digest();
             byte[] digestWithSalt = new byte[alg.hashLen + salt.length];
             System.arraycopy(digest, 0, digestWithSalt, 0, digest.length);
             System.arraycopy(salt, 0, digestWithSalt, digest.length, salt.length);
             return digestWithSalt;
         } catch (NoSuchAlgorithmException e) {
             throw new UnexpectedException(e);
         }
     }
 
     public static boolean compareSaltedDigest(byte[] digest, byte[] data, Algorithm alg) {
         try {
             MessageDigest sha = MessageDigest.getInstance(alg.id);
             byte[] digestData = Arrays.copyOfRange(digest, 0, alg.hashLen);
             byte[] salt = Arrays.copyOfRange(digest, alg.hashLen, digest.length);
             sha.update(data);
             sha.update(salt);
             byte[] encoded = sha.digest();
             return MessageDigest.isEqual(digestData, encoded);
         } catch (NoSuchAlgorithmException e) {
             throw new UnexpectedException(e);
         }
     }
 
     public static boolean compareSaltedDigest(String b64Digest, String data, Algorithm alg) {
         return compareSaltedDigest(Base64.decode(b64Digest),data.getBytes(),alg);
     }
 
     private static byte[] generateSalt() {
         try {
             final byte[] salt = new byte[8];
             final SecureRandom saltGen = SecureRandom.getInstance("SHA1PRNG");
             saltGen.nextBytes(salt);
             return salt;
         } catch (NoSuchAlgorithmException e) {
             throw new RuntimeException(e);
         }
     }
 
     public static byte[] createSaltedDigest(String text, Algorithm alg) {
         return createSaltedDigest(text.getBytes(), alg);
     }
 
     public static String createB64SaltedDigest(String text, Algorithm alg) {
         return Base64.encode(createSaltedDigest(text, alg));
     }
 
     public static String createB64SaltedDigest(byte[] data, Algorithm alg) {
         return Base64.encode(createSaltedDigest(data, alg));
     }
 
     public enum Algorithm {
         MD5(16,"MD5"), SHA1(20, "SHA-1"), SHA256(32, "SHA-256"), SHA512(64, "SHA-512");
         private int hashLen;
         private String id;
 
         private Algorithm(int hashLen, String id) {
             this.hashLen = hashLen;
             this.id = id;
         }
     }
 }
