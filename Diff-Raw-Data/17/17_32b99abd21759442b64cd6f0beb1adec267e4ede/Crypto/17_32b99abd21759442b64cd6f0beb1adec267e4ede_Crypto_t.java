 package com.amee.domain.auth.crypto;
 
 import com.sun.crypto.provider.SunJCE;
 import org.apache.commons.codec.binary.Base64;
 
 import javax.crypto.*;
 import javax.crypto.spec.DESedeKeySpec;
 import javax.crypto.spec.IvParameterSpec;
 import java.io.*;
 import java.security.*;
 import java.security.spec.InvalidKeySpecException;
 
 public class Crypto {
 
     private final static String KEY_FILE = "amee.keyFile";
     private final static String SALT_FILE = "amee.saltFile";
     private static byte[] salt = null;
     private static Key key = null;
     private static IvParameterSpec iv = null;
 
     public Crypto() {
         super();
     }
 
     private synchronized static void initialise() throws CryptoException {
         if (Crypto.key == null) {
             Security.addProvider(new SunJCE());
             String keyFileName = System.getProperty(KEY_FILE);
             String saltFileName = System.getProperty(SALT_FILE);
             if ((keyFileName != null) && (saltFileName != null)) {
                 File keyFile = new File(keyFileName);
                 File saltFile = new File(saltFileName);
                 if (keyFile.isFile() && saltFile.isFile()) {
                     Crypto.key = Crypto.readKeyFromFile(keyFile);
                     Crypto.salt = Crypto.readSaltFromFile(saltFile);
                     Crypto.iv = new javax.crypto.spec.IvParameterSpec(Crypto.salt);
                 }
             }
             if ((Crypto.key == null) || (Crypto.iv == null)) {
                 throw new RuntimeException("Could not create Key or IvParameterSpec instances. Check key and salt files.");
             }
         }
     }
 
     public static String getAsMD5AndBase64(String s) throws CryptoException {
         Crypto.initialise();
         try {
             MessageDigest md = MessageDigest.getInstance("MD5");
             md.update(Crypto.salt);
             md.update(s.getBytes());
             return new String(Base64.encodeBase64(md.digest()));
         } catch (NoSuchAlgorithmException e) {
             System.out.println("initialise() caught NoSuchAlgorithmException: " + e.getMessage());
             throw new CryptoException("NoSuchAlgorithmException", e);
         }
     }
 
     public static String encrypt(String toBeEncrypted) throws CryptoException {
         Crypto.initialise();
         byte[] data = toBeEncrypted.getBytes();
         try {
             Cipher cipher = Cipher.getInstance("DESede/CBC/PKCS5Padding");
             cipher.init(Cipher.ENCRYPT_MODE, Crypto.key, Crypto.iv);
             byte[] result = cipher.doFinal(data);
             result = Base64.encodeBase64(result);
             return new String(result);
         } catch (InvalidAlgorithmParameterException e) {
             System.out.println("encrypt() caught InvalidAlgorithmParameterException: " + e.getMessage());
             throw new CryptoException("InvalidAlgorithmParameterException ", e);
         } catch (InvalidKeyException e) {
             System.out.println("encrypt() caught InvalidKeyException: " + e.getMessage());
             throw new CryptoException("InvalidKeyException", e);
         } catch (IllegalBlockSizeException e) {
             System.out.println("encrypt() caught IllegalBlockSizeException: " + e.getMessage());
             throw new CryptoException("IllegalBlockSizeException", e);
         } catch (BadPaddingException e) {
             System.out.println("encrypt() caught BadPaddingException: " + e.getMessage());
             throw new CryptoException("BadPaddingException", e);
         } catch (NoSuchPaddingException e) {
            System.out.println("encrypt() caught NoSuchPaddingException: " + e.getMessage());
             throw new CryptoException("NoSuchPaddingException", e);
         } catch (NoSuchAlgorithmException e) {
            System.out.println("encrypt() caught NoSuchAlgorithmException: " + e.getMessage());
             throw new CryptoException("NoSuchAlgorithmException", e);
         }
     }
 
     public static String decrypt(String toBeDecrypted) throws CryptoException {
         Crypto.initialise();
         try {
             Cipher cipher = Cipher.getInstance("DESede/CBC/PKCS5Padding");
             byte[] data = toBeDecrypted.getBytes("UTF-8");
             data = Base64.decodeBase64(data);
             cipher.init(Cipher.DECRYPT_MODE, Crypto.key, Crypto.iv);
             byte[] result = cipher.doFinal(data);
             return new String(result);
         } catch (InvalidAlgorithmParameterException e) {
             System.out.println("decrypt() caught InvalidAlgorithmParameterException: " + e.getMessage());
             e.printStackTrace();
             throw new CryptoException("InvalidAlgorithmParameterException: ", e);
         } catch (UnsupportedEncodingException e) {
             System.out.println("decrypt() caught UnsupportedEncodingException: " + e.getMessage());
             throw new CryptoException("UnsupportedEncodingException", e);
         } catch (InvalidKeyException e) {
             System.out.println("decrypt() caught InvalidKeyException: " + e.getMessage());
             throw new CryptoException("InvalidKeyException", e);
         } catch (IllegalBlockSizeException e) {
             System.out.println("decrypt() caught IllegalBlockSizeException: " + e.getMessage());
             throw new CryptoException("IllegalBlockSizeException", e);
         } catch (BadPaddingException e) {
             System.out.println("decrypt() caught BadPaddingException: " + e.getMessage());
             throw new CryptoException("BadPaddingException", e);
         } catch (NoSuchPaddingException e) {
            System.out.println("decrypt() caught NoSuchPaddingException: " + e.getMessage());
             throw new CryptoException("NoSuchPaddingException ", e);
         } catch (NoSuchAlgorithmException e) {
            System.out.println("decrypt() caught NoSuchAlgorithmException: " + e.getMessage());
             throw new CryptoException("NoSuchAlgorithmException ", e);
         }
     }
 
     public static void main(String[] args) throws CryptoException {
         System.out.println("Creating new key...");
         System.out.flush();
         SecretKey key = getNewKey();
         saveKeyToFile(key, new File(args[0]));
         System.out.println("...done.");
     }
 
     public static void saveKeyToFile(SecretKey key, File file) throws CryptoException {
 
         SecretKeyFactory keyFactory;
         DESedeKeySpec keySpec;
         byte[] keyAsBytes;
         FileOutputStream output;
 
         try {
             // convert key to byte array
             keyFactory = SecretKeyFactory.getInstance("DESede");
             keySpec = (DESedeKeySpec) keyFactory.getKeySpec(key, DESedeKeySpec.class);
             keyAsBytes = keySpec.getKey();
 
             // save key to file
             output = new FileOutputStream(file);
             output.write(keyAsBytes);
             output.close();
 
         } catch (NoSuchAlgorithmException e) {
             System.out.println("saveKeyToFile() caught NoSuchAlgorithmException: " + e.getMessage());
             throw new CryptoException("NoSuchAlgorithmException", e);
         } catch (InvalidKeySpecException e) {
             System.out.println("saveKeyToFile() caught InvalidKeySpecException: " + e.getMessage());
             throw new CryptoException("InvalidKeySpecException", e);
         } catch (IOException e) {
             System.out.println("saveKeyToFile() caught IOException: " + e.getMessage());
             throw new CryptoException("IOException", e);
         }
     }
 
     public static SecretKey readKeyFromFile(File file) throws CryptoException {
 
         DataInputStream input;
         byte[] keyAsBytes;
         DESedeKeySpec keySpec;
         SecretKeyFactory keyFactory;
         SecretKey key;
 
         try {
             // read key byte array from file
             input = new DataInputStream(new FileInputStream(file));
             keyAsBytes = new byte[(int) file.length()];
             input.readFully(keyAsBytes);
             input.close();
 
             // get key from key byte array
             keySpec = new DESedeKeySpec(keyAsBytes);
             keyFactory = SecretKeyFactory.getInstance("DESede");
             key = keyFactory.generateSecret(keySpec);
 
         } catch (IOException e) {
             System.out.println("readKeyFromFile() caught IOException: " + e.getMessage());
             throw new CryptoException("IOException", e);
         } catch (InvalidKeyException e) {
             System.out.println("readKeyFromFile() caught InvalidKeyException: " + e.getMessage());
             throw new CryptoException("InvalidKeyException", e);
         } catch (NoSuchAlgorithmException e) {
             System.out.println("readKeyFromFile() caught NoSuchAlgorithmException: " + e.getMessage());
             throw new CryptoException("NoSuchAlgorithmException", e);
         } catch (InvalidKeySpecException e) {
             System.out.println("readKeyFromFile() caught InvalidKeySpecException: " + e.getMessage());
             throw new CryptoException("InvalidKeySpecException", e);
         }
 
         return key;
     }
 
 
     public static byte[] readSaltFromFile(File file) throws CryptoException {
 
         DataInputStream input;
         byte[] salt;
 
         try {
             // read salt byte array from file
             input = new DataInputStream(new FileInputStream(file));
             salt = new byte[(int) file.length()];
             input.readFully(salt);
             input.close();
             // must be 8 bytes
             if (salt.length != 8) {
                 throw new RuntimeException("Salt from '" + file.getAbsolutePath() + "' is not 8 bytes.");
             }
         } catch (IOException e) {
            System.out.println("readSaltFromFile() caught IOException: " + e.getMessage());
             throw new CryptoException("IOException", e);
         }
 
         return salt;
     }
 
     private static SecretKey getNewKey() throws CryptoException {
         try {
             KeyGenerator keyGenerator = KeyGenerator.getInstance("DESede");
             return keyGenerator.generateKey();
         } catch (NoSuchAlgorithmException e) {
            System.out.println("getNewKey() caught NoSuchAlgorithmException: " + e.getMessage());
             throw new CryptoException("NoSuchAlgorithmException", e);
         }
     }
 }
