 package com.aes;
 
 import javax.crypto.*;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.security.*;
 import java.security.KeyStore.SecretKeyEntry;
 import java.security.cert.CertificateException;
 
 /**
  * keytool command.
  * keytool -genseckey -keyalg AES -alias myseckey -keysize 256 -keypass
  * mykeypass -storetype jceks -keystore mystore.jck -storepass mystorepass
  */
 
 public class AESCrypto {
 
     private static final String TYPE_OF_KEYSTORE = "JCEKS";
     private static final String PATH_TO_KEYSTORE = "src/main/resources/mystore.jck";
     private static final String KEY_ALIAS = "myseckey";
     private static final String ALIAS_PASSWORD = "mykeypass";
     private static final String STORE_PASSWORD = "mystorepass";
     private static final String SECRET_TEXT = "My Secret Text";
 
    private AESCrypto() {
    }

     /**
      * @param typeKeyStore   @throws java.io.IOException
      * @param pathToKeyStore
      * @param storePasswod
      * @param aliasPassword
      * @param keyAlias
      * @throws java.io.FileNotFoundException
      * @throws java.security.cert.CertificateException
      *
      * @throws java.security.UnrecoverableEntryException
      *
      */
 
     public SecretKey getSecretKey(String typeKeyStore,
                                   String pathToKeyStore,
                                   String storePasswod,
                                   String keyAlias,
                                   String aliasPassword ) throws KeyStoreException,
             IOException, CertificateException, NoSuchAlgorithmException, UnrecoverableEntryException {
 
         KeyStore ks = KeyStore.getInstance(typeKeyStore);
         ks.load(new FileInputStream(pathToKeyStore),
                 storePasswod.toCharArray());
 
         SecretKeyEntry entry = (SecretKeyEntry) ks.getEntry(
                 keyAlias,
                 new KeyStore.PasswordProtection(aliasPassword
                         .toCharArray()));
         return entry.getSecretKey();
     }
 
     public static void main(String[] argv) {
 
         try {
 
             SecretKey myDesKey = new AESCrypto().getSecretKey(
                     TYPE_OF_KEYSTORE,
                     PATH_TO_KEYSTORE,
                     STORE_PASSWORD,
                     KEY_ALIAS,
                     ALIAS_PASSWORD);
 
             Cipher desCipher;
 
             // Create the cipher with AES algorithm
             desCipher = Cipher.getInstance("AES");
 
             // Initialize the cipher for encryption
             desCipher.init(Cipher.ENCRYPT_MODE, myDesKey);
 
             // secret text
             byte[] text = SECRET_TEXT.getBytes();
 
             System.out.println("Text [Byte Format] : " + text);
             System.out.println("Text : " + new String(text));
 
             // Encrypt the text
             byte[] textEncrypted = desCipher.doFinal(text);
 
             System.out.println("Text Encrypted : " + textEncrypted);
 
             // Switch the cipher to decryption
             desCipher.init(Cipher.DECRYPT_MODE, myDesKey);
 
             // Decrypt the text
             byte[] textDecrypted = desCipher.doFinal(textEncrypted);
 
             System.out.println("Text Decrypted : " + new String(textDecrypted));
 
         } catch (NoSuchAlgorithmException e) {
             e.printStackTrace();
         } catch (NoSuchPaddingException e) {
             e.printStackTrace();
         } catch (InvalidKeyException e) {
             e.printStackTrace();
         } catch (IllegalBlockSizeException e) {
             e.printStackTrace();
         } catch (BadPaddingException e) {
             e.printStackTrace();
         } catch (KeyStoreException e) {
             e.printStackTrace();
         } catch (CertificateException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         } catch (UnrecoverableEntryException e) {
             e.printStackTrace();
         }
 
     }
 
 }
