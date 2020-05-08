 package uk.org.lidalia.crypto.rsa;
 
 import javax.crypto.Cipher;
 import java.security.Key;
 import java.security.interfaces.RSAKey;
 
 public abstract class RsaKey implements Key, RSAKey {
 
     public byte[] encrypt(byte[] input) {
        return doCrypto(input, Cipher.ENCRYPT_MODE);
     }
 
     public byte[] decrypt(byte[] input) {
        return doCrypto(input, Cipher.DECRYPT_MODE);
     }
 
    private byte[] doCrypto(byte[] input, int encryptMode) {
         try {
             Cipher rsa = RsaKeyUtils.cipher();
             rsa.init(encryptMode, this);
             return rsa.doFinal(input);
         } catch (Exception e) {
            throw new IllegalStateException("Encrypting with an RSA key should always work. Using key="+ this, e);
         }
     }
 }
