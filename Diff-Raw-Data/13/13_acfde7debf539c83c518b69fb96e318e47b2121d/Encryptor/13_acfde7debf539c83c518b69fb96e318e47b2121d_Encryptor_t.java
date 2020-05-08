 package org.geometerplus.expansion.crypto;
 
 import javax.crypto.Cipher;
 import javax.crypto.SecretKey;
 import javax.crypto.SecretKeyFactory;
 import javax.crypto.spec.IvParameterSpec;
 import javax.crypto.spec.PBEKeySpec;
 import javax.crypto.spec.SecretKeySpec;
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 
 public class Encryptor {
     private static final String TAG = Encryptor.class.getSimpleName();
     private static final String PBE_ALGORITHM = "PBEWithSHA256And256BitAES-CBC-BC";
     private EncryptorSetting mEncryptorSetting;
 
     public Encryptor(EncryptorSetting setting){
         mEncryptorSetting = setting;
     }
 
    public EncryptorSetting getEncryptorSetting() {
        return mEncryptorSetting;
    }

     public String encrypt(String plainText, String deviceID) {
         return processText(plainText, deviceID, Cipher.ENCRYPT_MODE);
     }
 
     public String decrypt(String encryptedText, String deviceID) {
         return processText(encryptedText, deviceID, Cipher.DECRYPT_MODE);
     }
 
     public InputStream encrypt(InputStream plainText, String deviceID) {
         return processText(plainText, deviceID, Cipher.ENCRYPT_MODE);
     }
 
     public InputStream decrypt(InputStream encryptedText, String password) {
         return processText(encryptedText, password, Cipher.DECRYPT_MODE);
     }
 
     private String processText(String inData, String password, int opmode) {
         InputStream inStreamData = null;
         try {
             inStreamData = new ByteArrayInputStream(inData.getBytes("UTF8"));
         } catch (UnsupportedEncodingException e) {
             e.printStackTrace();
         }
         InputStream outStreamData = processText(inStreamData, password, opmode);
 
         if (outStreamData == null) {
             return null;
         }
         try {
             return new String(EncryptorUtils.toByteArray(outStreamData), "UTF8");
         } catch (IOException e) {
             System.out.println(TAG + e.getMessage());
         }
         return null;
     }
 
     private InputStream processText(InputStream inData, String password, int opmode) {
         byte[] outData;
         try {
             Cipher cipher = createChipher(opmode, password);
 
             byte[] inDataArray = EncryptorUtils.toByteArray(inData);
             outData = cipher.doFinal(inDataArray);
         } catch (Exception e) {
             System.out.println(TAG + e.getMessage());
             return inData;
         }
 
         return new ByteArrayInputStream(outData);
     }
 
     public Cipher createChipher(int opmode, String seed) throws Exception {
         byte[] rawKey = EncryptorUtils.hmacDigest(seed, mEncryptorSetting.getSalt(), mEncryptorSetting.getHMacAlgorithm()).getBytes("UTF8");
 
         SecretKeySpec skeySpec = new SecretKeySpec(rawKey, mEncryptorSetting.getAlgorithm());
         Cipher cipher = Cipher.getInstance(mEncryptorSetting.getCipherAlgorithm());
         IvParameterSpec ivSpec = new IvParameterSpec(mEncryptorSetting.getIV());
         cipher.init(opmode, skeySpec, ivSpec);
 
         return cipher;
     }
 
     public byte[] getRawKeyPBE(char[] seed) throws Exception {
         PBEKeySpec pbeKeySpec = new PBEKeySpec(seed);
         SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(PBE_ALGORITHM);
         SecretKey tempKey = keyFactory.generateSecret(pbeKeySpec);
         SecretKey secretKey = new SecretKeySpec(tempKey.getEncoded(), mEncryptorSetting.getAlgorithm());
         byte[] raw = secretKey.getEncoded();
 
         return raw;
     }
 
     public String getEncryptedFilePrefix(){
         return mEncryptorSetting.getEncryptedFilePrefix();
     }
 
     public static class EncryptorSetting {
         private byte[] mIV = new byte[16];
         private String mSalt = "";
         private String mAlgorithm = "AES";
         private String mHMacAlgorithm = "HmacMD5";
         private String mCipherAlgorithm = "AES/CBC/PKCS5Padding";
         private String mEncryptedFilePrefix = "okadabook_encrypted_file";
 
         public byte[] getIV() {
             return mIV;
         }
 
         public void setIV(byte[] IV) {
             mIV = IV;
         }
 
         public String getSalt() {
             return mSalt;
         }
 
         public void setSalt(String salt) {
             mSalt = salt;
         }
 
         public String getAlgorithm() {
             return mAlgorithm;
         }
 
         public void setAlgorithm(String algorithm) {
             mAlgorithm = algorithm;
         }
 
         public String getHMacAlgorithm() {
             return mHMacAlgorithm;
         }
 
         public void setHMacAlgorithm(String HMacAlgorithm) {
             mHMacAlgorithm = HMacAlgorithm;
         }
 
         public String getCipherAlgorithm() {
             return mCipherAlgorithm;
         }
 
         public void setCipherAlgorithm(String cipherAlgorithm) {
             mCipherAlgorithm = cipherAlgorithm;
         }
 
         public String getEncryptedFilePrefix() {
             return mEncryptedFilePrefix;
         }
 
         public void setEncryptedFilePrefix(String encryptedFilePrefix) {
             mEncryptedFilePrefix = encryptedFilePrefix;
         }
     }
 }
