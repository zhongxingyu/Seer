 package uk.org.lidalia.crypto.rsa;
 
 import javax.crypto.Cipher;
 import java.security.Key;
 import java.security.interfaces.RSAKey;
 
 public abstract class RsaKey implements Key, RSAKey {
 
     public byte[] encrypt(byte[] input) {
        return doCrypto(input, Cipher.ENCRYPT_MODE, "Encrypting");
     }
 
     public byte[] decrypt(byte[] input) {
        return doCrypto(input, Cipher.DECRYPT_MODE, "Decrypting");
     }
 
    private byte[] doCrypto(byte[] input, int encryptMode, final String action) {
         try {
             Cipher rsa = RsaKeyUtils.cipher();
             rsa.init(encryptMode, this);
             return rsa.doFinal(input);
         } catch (Exception e) {
            throw new IllegalStateException(action + " with an RSA key should always work. Using key=" + this, e);
         }
     }
 }
