 package com.hoccer.data;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.security.InvalidAlgorithmParameterException;
 import java.security.InvalidKeyException;
 import java.security.KeyFactory;
 import java.security.KeyPair;
 import java.security.KeyPairGenerator;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.security.PrivateKey;
 import java.security.PublicKey;
 import java.security.SecureRandom;
 import java.security.spec.InvalidKeySpecException;
 import java.security.spec.PKCS8EncodedKeySpec;
 import java.security.spec.RSAPrivateKeySpec;
 import java.security.spec.RSAPublicKeySpec;
 import java.security.spec.X509EncodedKeySpec;
 import java.util.Arrays;
 
 import javax.crypto.BadPaddingException;
 import javax.crypto.Cipher;
 import javax.crypto.IllegalBlockSizeException;
 import javax.crypto.Mac;
 import javax.crypto.NoSuchPaddingException;
 import javax.crypto.spec.IvParameterSpec;
 import javax.crypto.spec.SecretKeySpec;
 
 import android.util.Log;
 
 /**
  * Usage:
  * 
  * <pre>
  * String crypto = CryptoHelper.encrypt(masterpassword, cleartext)
  * ...
  * String cleartext = CryptoHelper.decrypt(masterpassword, crypto)
  * </pre>
  * 
  * @author Pavel Mayer, based on example by ferenc.hechler
  */
 
 public class CryptoHelper {
     static String MOD = "CryptoHelper";
 
     public static KeyPair generateRSAKeyPair(int len) throws NoSuchAlgorithmException {
         KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
         kpg.initialize(len);
         KeyPair kp = kpg.genKeyPair();
         return kp;
     }
 
     public static RSAPublicKeySpec getPublicKeySpec(KeyPair kp) throws NoSuchAlgorithmException,
             InvalidKeySpecException {
         KeyFactory fact = KeyFactory.getInstance("RSA");
         RSAPublicKeySpec pub = fact.getKeySpec(kp.getPublic(), RSAPublicKeySpec.class);
         return pub;
     }
 
     public static RSAPublicKeySpec getPublicKeySpec(PublicKey pubkey)
             throws NoSuchAlgorithmException, InvalidKeySpecException {
         KeyFactory fact = KeyFactory.getInstance("RSA");
         RSAPublicKeySpec pub = fact.getKeySpec(pubkey, RSAPublicKeySpec.class);
         return pub;
     }
 
     public static RSAPrivateKeySpec getPrivateKeySpec(KeyPair kp) throws NoSuchAlgorithmException,
             InvalidKeySpecException {
         KeyFactory fact = KeyFactory.getInstance("RSA");
         RSAPrivateKeySpec priv = fact.getKeySpec(kp.getPrivate(), RSAPrivateKeySpec.class);
         return priv;
     }
 
     public static RSAPrivateKeySpec getPrivateKeySpec(PrivateKey privkey)
             throws NoSuchAlgorithmException, InvalidKeySpecException {
         KeyFactory fact = KeyFactory.getInstance("RSA");
         RSAPrivateKeySpec priv = fact.getKeySpec(privkey, RSAPrivateKeySpec.class);
         return priv;
     }
 
     public static String toString(RSAPrivateKeySpec priv) {
         String result = "RSA-Private:" + "modulos:" + priv.getModulus() + ",exponent:"
                 + priv.getPrivateExponent();
         return result;
     }
 
     public static String toString(RSAPublicKeySpec pub) {
         String result = "RSA-Public:" + "modulos:" + pub.getModulus() + ",exponent:"
                 + pub.getPublicExponent();
         return result;
     }
 
     public static int getDefaultKeySize() {
         // return 128;
         return 256;
     }
 
     public static String getDefaultCrypto() {
         return "AES";
     }
 
     public static String getDefaultMode() {
         return "CBC";
     }
 
     public static String getDefaultPadding() {
         return "PKCS7Padding";
     }
 
     public static String getDefaultHash() {
         return "SHA256";
         // return "SHA-1";
     }
 
     public static byte[] wrapRSA1024_X509(byte[] pure_DER_rsa_pub_key) throws InvalidKeyException {
         if (pure_DER_rsa_pub_key.length > 150) {
             throw new InvalidKeyException("Key too long, not RSA1024");
         }
         byte[] header = toByte("30819f300d06092a864886f70d010101050003818d00");
         byte seq1LenIndex = 2;
         byte seq2LenIndex = 20;
 
         byte seq2Len = (byte) (pure_DER_rsa_pub_key.length + 1);
         byte seq1Len = (byte) (seq2Len + 18);
 
         header[seq1LenIndex] = seq1Len;
         header[seq2LenIndex] = seq2Len;
 
         return concat(header, pure_DER_rsa_pub_key);
     }
 
     public static byte[] unwrapRSA1024_X509(byte[] X509_rsa_pub_key) throws InvalidKeyException {
         if (X509_rsa_pub_key.length > 150 + 21) {
             throw new InvalidKeyException("Key too long");
         }
         return skip(X509_rsa_pub_key, 22);
     }
 
     public static byte[] wrapRSA1024_PKCS8(byte[] pure_DER_rsa_priv_key) throws InvalidKeyException {
         if (pure_DER_rsa_priv_key.length > 650) {
             throw new InvalidKeyException("Key too long, not RSA1024");
         }
         byte[] header = toByte("30820278020100300d06092a864886f70d010101050004820262");
         byte seq1LenIndex = 2;
         byte seq2LenIndex = 24;
 
         int seq2Len = pure_DER_rsa_priv_key.length - 2;
         int seq1Len = seq2Len + 22;
 
         header[seq1LenIndex] = (byte) (seq1Len / 255);
         header[seq1LenIndex + 1] = (byte) (seq1Len % 255);
         header[seq2LenIndex] = (byte) (seq2Len / 255);
         header[seq2LenIndex + 1] = (byte) (seq2Len % 255);
 
         return concat(header, pure_DER_rsa_priv_key);
     }
 
     public static byte[] unwrapRSA1024_PKCS8(byte[] PKCS8_rsa_priv_key) throws InvalidKeyException {
         if (PKCS8_rsa_priv_key.length > 650 + 25) {
             throw new InvalidKeyException("Key too long");
         }
         return skip(PKCS8_rsa_priv_key, 26);
     }
 
     public static PublicKey makePublicRSA1024Key(byte[] pure_DER_rsa_pub_key)
             throws NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException {
         KeyFactory kf = KeyFactory.getInstance("RSA");
 
         X509EncodedKeySpec pubSpec = new X509EncodedKeySpec(wrapRSA1024_X509(pure_DER_rsa_pub_key));
         PublicKey myPublicKey = kf.generatePublic(pubSpec);
         return myPublicKey;
     }
 
     public static PrivateKey makePrivateRSA1024Key(byte[] pure_DER_rsa_priv_key)
             throws NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException {
         KeyFactory kf = KeyFactory.getInstance("RSA");
 
         PKCS8EncodedKeySpec privSpec = new PKCS8EncodedKeySpec(
                 CryptoHelper.wrapRSA1024_PKCS8(pure_DER_rsa_priv_key));
         PrivateKey myPrivateKey = kf.generatePrivate(privSpec);
         return myPrivateKey;
     }
 
     public static void testRSA() {
         try {
             byte[] testsalt = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19,
                     20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32 };
             // int keybytes = getDefaultKeySize() / 8;
             // byte[] testsalt = new byte[keybytes];
             // for (byte i = 0; i < keybytes; ++i) {
             // testsalt[i] = (byte) (i + 1);
             // }
 
             Log.v(MOD, "testRSA-AES Key Generator Testing:");
             Log.v(MOD, "salt=" + Base64.encodeBytes(testsalt));
             Log.v(MOD, "salt=" + toHex(testsalt));
             // Log.v(MOD, "random_salt=" + toHex(makeRandomBytes(getDefaultKeySize()/8)));
             // Log.v("SHA1(password)=", toHex(md("password", "SHA-1")));
             Cipher c = makeCipher(testsalt, new String("password").getBytes("UTF-8"),
                     Cipher.ENCRYPT_MODE, getDefaultCrypto(), getDefaultKeySize(), getDefaultHash());
             byte[] encrypted = crypt(c, new String("test").getBytes("UTF-8"));
             Log.v(MOD, "AES-encrypted=" + Base64.encodeBytes(encrypted));
             Log.v(MOD, "AES-encrypted=" + toHex(encrypted));
             Cipher d = makeCipher(testsalt, new String("password").getBytes("UTF-8"),
                     Cipher.DECRYPT_MODE, getDefaultCrypto(), getDefaultKeySize(), getDefaultHash());
             byte[] decrypted = crypt(d, encrypted);
             Log.v(MOD, "AES-decrypted=" + new String(decrypted));
             Log.v(MOD, "done test");
 
             KeyPair kp = generateRSAKeyPair(1024);
             // Log.v(MOD, "RSA" + toString(getPrivateKeySpec(kp)));
             // Log.v(MOD, "RSA" + toString(getPublicKeySpec(kp)));
             String encr = encryptRSA(kp.getPublic(), "blafasel12345678");
             byte[] encrIOS = encryptRSA(kp.getPublic(), "blafasel12345678".getBytes("UTF-8"));
             Log.v(MOD, "RSA-encrypted-iOS:" + Base64.encodeBytes(encrIOS));
 
             // Log.v(MOD, "RSA-encrypted:" + encr);
             String decr = decryptRSA(kp.getPrivate(), encr);
             Log.v(MOD, "RSA-decrypted:" + decr);
 
             byte[] pubenc = kp.getPublic().getEncoded();
             byte[] privenc = kp.getPrivate().getEncoded();
             Log.v(MOD, "RSA-pub-ts[" + pubenc.length + "]:" + Base64.encodeBytes(pubenc));
             // Log.v(MOD, "RSA-pub-ts[" + pubenc.length + "]:" + toHex(pubenc));
             Log.v(MOD, "RSA-priv-ts[" + privenc.length + "]:" + Base64.encodeBytes(privenc));
             // Log.v(MOD, "RSA-priv-ts[" + privenc.length + "]:" + toHex(privenc));
 
             byte[] pubencIOS = unwrapRSA1024_X509(pubenc);
             Log.v(MOD, "RSA-pub-IOS[" + pubencIOS.length + ":" + Base64.encodeBytes(pubencIOS));
             byte[] privencIOS = unwrapRSA1024_PKCS8(privenc);
             Log.v(MOD, "RSA-priv-IOS[" + privencIOS.length + "]:" + Base64.encodeBytes(privencIOS));
 
             byte[] pubWrapped = wrapRSA1024_X509(pubencIOS);
             byte[] privWrapped = wrapRSA1024_PKCS8(privencIOS);
             Log.v(MOD,
                     "RSA-pubWrapped-ts[" + pubWrapped.length + "]:"
                             + Base64.encodeBytes(pubWrapped));
             // Log.v(MOD, "RSA-pub-ts[" + pubWrapped + "]:" + toHex(pubWrapped));
             Log.v(MOD,
                     "RSA-privWrapped-ts[" + privWrapped.length + "]:"
                             + Base64.encodeBytes(privWrapped));
             // Log.v(MOD, "RSA-priv-ts[" + privWrapped + "]:" + toHex(privWrapped));
             boolean pubOK = Arrays.equals(pubWrapped, pubenc);
             boolean privOK = Arrays.equals(privWrapped, privenc);
             Log.v(MOD, "RSA-wrapper-OK priv:[" + privOK + "]:" + " pub:[" + pubOK + "]:");
 
             String myPubKey2 = "MIGJAoGBAMSvNQyQdJtrsg54RIgb5P6eo8w/VZZoq2QsQbjo/ayqGUp03EDJk31C8aFuq2PLz2FLueC5e+1/IquJKlEXJE7iMN2vORLVna8Mck3C8PN0vEaYKqxM5bq9wyYkzQAg6MJ8jn8Xw7natgE9dtEvpZMcHtzbqiDnLKFAVRAUDgxbAgMBAAE=";
             String myPrivKey2 = "MIICWwIBAAKBgGxE9LQ//NX+i7IDQOD1OZd73R8fk1bIEEvIuGtYuoGMw4u+0eVJYZt4z+njOpJ/H1JMRUQO6PgBIlAsgxhD6SIkjo8vowr9u/R59D+F0t7UzHISZsuxiF6Vh4791tXtqER+9AgsAwaU496OfhmZdnNMinJz5SN7bz0s6XqAV4EhAgMBAAECgYAVzHBkVjnGsCBaL/OBF36H9GVZ3dahc1hsmbYfztaGPNwmJ75E5thjIBjkY16onjWlMTwE7ueS/090SvH+EbY/XG8/7cmZu6GvB4d54Sl07s+IV69wqGKPKl/1SSu1utPD0KlQzdWkaejByz+JoCFJw7m+zYWjSZxgm9G3y1DWQQJBATQOXHBVP3pHk8gM9uukiWNVUhDHqavaM+C2IYVPiyoT4pjXFE4OR/w+9060QDeX+8kSfQF37EKFs7l+HUO8T4kCQFn5Rk2VKYcTFjqJc3d8JoaVlQLjDNbhVjSNwzPeDnlIUSC9wsdiOJ6ixmIfUHRXOPap3jfsVi6T7yYKbGhr5tkCQQDBXTZW6Ju0tJMlokWnuhrm+BpQIBP3pDqmFYzK8hgHbH3ytCaxrDMxOZDgnTIl80d/ehRvRIhPZT9f8rKJ3v0JAkAVQkXvPOhUBxmAeUu0FryPnjZYOUemWhXhUwGldrlaxNCOeOfV7opMSU+wjY+X/afy+E4OTqRKWx/tkBbvUVd5AkEAuNaeJaJ0zq6mwngEhVIfjyntFF31rL4no5FKa7ePQ4TdcqWzlYHq6vdxmAZRhlQuN+48L3gjd6XHxkx+g78qkA==";
 
             String myPubKey = "MIGJAoGBALr4jEVZIw+hWUWXcCYw1aXqeSuJnda7YupliMaIdRvVVyrvTE7bHCHpgG6Q961hvFa5a38Jn5lnb/lerLj6n6NRtGhdqNXIsgZ+tQKkBIW4PaHxn5Gni7jw8ZfKbN/D437K2wjNPxS0E6Av1wWgeOmUkqUBjeBHb/rexRnqO3eVAgMBAAE=";
             String myPrivKey = "MIICWwIBAAKBgQC6+IxFWSMPoVlFl3AmMNWl6nkriZ3Wu2LqZYjGiHUb1Vcq70xO2xwh6YBukPetYbxWuWt/CZ+ZZ2/5Xqy4+p+jUbRoXajVyLIGfrUCpASFuD2h8Z+Rp4u48PGXymzfw+N+ytsIzT8UtBOgL9cFoHjplJKlAY3gR2/63sUZ6jt3lQIDAQABAoGAYHTtWLF9pwikV4Si9PDop6npTQ64ARm3FBnBkDrBv9Q2Hg5KHbxoLQ6blW7wd+AeG9eYn3dFgQyd9dZj4SJazAKJ5G0eWjga6jwDDI6+0SIQnlHmsCYPoI2ZTBHWQEyBbGiGenGcqKbVyvL9StbrJ9HFENj+PE3GHJ08qxeXExkCQQGg0CKWkiYz223Vtd3GjmHQFqWIrWmw0Zn1RDuXJAmRkNlrVikmsv/T/kFgItelmXbXoOH/CJdALTOmdPeW0+c/AkBy1aberRxac6GeOE5j1YO+ONJxii5JvsAR4O0VkhWkUydpCZ0f7cE6DB6NQKbK9YGxWlpWf32bOW6HnAuzFGArAkAO8ierWoZAKcggd6sCKazcN1OsOPunOXzZzJ6OZt5o99a0AJztJFIEGgPiHJ269GvMg5pW+MnjpTtK5rrSD7slAkBUgzjUGMMNLpx7PSU0BCd5D4iRVwjJ7UCd59OUVHbpAOm4PAMPRIM4nUK+4h3esOBKDhz+G8XtP09BLm7N1OkRAkEA/FHUOFYfhEczDCI4tuh3lbA34HzGlmFPpXkQqTKutQeVcZ7ER/M1f8uSDrYym5LrjM1GEOU6j02gyMBOCO0pvg==";
             String mySecretNoPadding = "m4n8SOqu8Z6amloq8tg9hS/fhguhyNOrikBhyJIwQxHJLF00yfw7mSYapYkT71edyrZWZBWzVUUCjYhC40To7u8YFuqSQkdSDF1ALWtXtGYlBOtZPFRxSqVDgo/jb7mZXgyxjtbqIi5W7TQoqBFLts5o5wXXk2BY=";
             String mySecretPCKS1 = "I5dIaAiB9OScIs2zqvGCVh2J26gX6fE/ggT5qEizhS4gfrmG3y/M1lLMR0Y8H1nyGFkerjRLgiHPAbS0OawEJbWaA2qtSzTa2Jlo6yuOx3ZAjwr4ojlPZDmkwn6sy1As2il+9twNtPyQmN0fk7c7j3Ni1plY1y5mf8lMJooHfSk=";
 
             byte[] pubBytes = Base64.decode(myPubKey);
             pubBytes = wrapRSA1024_X509(pubBytes);
 
             byte[] privBytes = Base64.decode(myPrivKey);
             privBytes = wrapRSA1024_PKCS8(privBytes);
 
             Log.v(MOD, "RSA-pub-ts-mod[" + pubenc.length + "]:" + Base64.encodeBytes(pubBytes));
             Log.v(MOD, "RSA-priv-ts-mod[" + privenc.length + "]:" + Base64.encodeBytes(privBytes));
             Log.v(MOD, "RSA-pub-ts-mod[" + pubenc.length + "]:" + toHex(pubBytes));
             Log.v(MOD, "RSA-priv-ts-mod[" + privenc.length + "]:" + toHex(privBytes));
 
             KeyFactory kf = KeyFactory.getInstance("RSA");
 
             X509EncodedKeySpec pubSpec = new X509EncodedKeySpec(pubBytes);
             PublicKey publicKey = kf.generatePublic(pubSpec);
 
             PKCS8EncodedKeySpec privSpec = new PKCS8EncodedKeySpec(privBytes);
             PrivateKey privateKey = kf.generatePrivate(privSpec);
 
             Log.v(MOD, "RSA" + toString(getPrivateKeySpec(privateKey)));
             Log.v(MOD, "RSA" + toString(getPublicKeySpec(publicKey)));
             String encr2 = encryptRSA(publicKey, "blafasel12345678");
             Log.v(MOD, "RSA-encrypted:" + encr2);
             String decr2 = decryptRSA(privateKey, encr2);
             Log.v(MOD, "RSA-decrypted:" + decr2);
             Log.v(MOD, "RSA-pub-ts:" + Base64.encodeBytes(publicKey.getEncoded()));
             Log.v(MOD, "RSA-priv-ts:" + Base64.encodeBytes(privateKey.getEncoded()));
 
             byte[] iosBytes = Base64.decode(mySecretPCKS1);
             byte[] decr3 = decryptRSA(privateKey, iosBytes);
             Log.v(MOD, "RSA-decrypted-sec:" + new String(decr3));
 
         } catch (NoSuchAlgorithmException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         } catch (InvalidKeySpecException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         } catch (InvalidKeyException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         } catch (NoSuchPaddingException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         } catch (BadPaddingException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         } catch (IllegalBlockSizeException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         } catch (UnsupportedEncodingException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
             // } catch (InvalidAlgorithmParameterException e) {
             // // TODO Auto-generated catch block
             // e.printStackTrace();
         } catch (IOException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         } catch (InvalidAlgorithmParameterException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
     }
 
     public static byte[] sha1_mac(byte[] message, byte[] keyString)
             throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeyException {
         SecretKeySpec key = new SecretKeySpec(keyString, "HmacSHA1");
         Mac mac = Mac.getInstance("HmacSHA1");
         mac.init(key);
         byte[] bytes = mac.doFinal(message);
         return bytes;
     }
 
     public static String sha1_mac(String message, String keyString)
             throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeyException {
         return new String(Base64.encodeBytes(sha1_mac(message.getBytes("UTF-8"),
                 keyString.getBytes("UTF-8"))));
     }
 
     public static byte[] md(byte[] bytes, String algorithm) throws UnsupportedEncodingException,
             NoSuchAlgorithmException {
         MessageDigest md = MessageDigest.getInstance(algorithm); // SHA-1 and SHA256 tested
         md.update(bytes);
         byte[] digest = md.digest();
         return digest;
     }
 
     public static byte[] md_sha1(byte[] bytes) throws UnsupportedEncodingException,
             NoSuchAlgorithmException {
         return md(bytes, "SHA-1");
     }
 
     public static byte[] md_sha256(byte[] bytes) throws UnsupportedEncodingException,
             NoSuchAlgorithmException {
         return md(bytes, "SHA256");
     }
 
     public static byte[] md(String raw, String algorithm) throws UnsupportedEncodingException,
             NoSuchAlgorithmException {
         byte[] bytes = raw.getBytes("UTF-8"); // "8859_1"/* encoding */ );
         return md(bytes, algorithm);
     }
 
     public static byte[] decryptRSA(PrivateKey priv, byte[] encrypted)
             throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException,
             BadPaddingException, IllegalBlockSizeException {
         Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
         // Cipher cipher = Cipher.getInstance("RSA/ECB/NoPadding");
         cipher.init(Cipher.DECRYPT_MODE, priv);
         byte[] decrypted = cipher.doFinal(encrypted);
         return decrypted;
     }
 
     public static byte[] encryptRSA(PublicKey pub, byte[] clear) throws NoSuchPaddingException,
             NoSuchAlgorithmException, InvalidKeyException, BadPaddingException,
             IllegalBlockSizeException {
         Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
         // Cipher cipher = Cipher.getInstance("RSA/ECB/NoPadding");
         cipher.init(Cipher.ENCRYPT_MODE, pub);
         byte[] encrypted = cipher.doFinal(clear);
         return encrypted;
     }
 
     public static String decryptRSA(PrivateKey priv, String encrypted)
             throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException,
             BadPaddingException, IllegalBlockSizeException {
         return new String(decryptRSA(priv, toByte(encrypted)));
     }
 
     public static String encryptRSA(PublicKey pub, String clear) throws NoSuchPaddingException,
             NoSuchAlgorithmException, InvalidKeyException, BadPaddingException,
             IllegalBlockSizeException {
         return toHex(encryptRSA(pub, clear.getBytes()));
     }
 
     /*
      * saveToFile("public.key", pub.getModulus(), pub.getPublicExponent());
      * saveToFile("private.key", priv.getModulus(), priv.getPrivateExponent());
      */
     /*
      * public static void saveToFile(String fileName, BigInteger mod, BigInteger exp) throws
      * Exception { ObjectOutputStream oout = new ObjectOutputStream( new BufferedOutputStream(new
      * FileOutputStream(fileName))); try { oout.writeObject(mod); oout.writeObject(exp); } catch
      * (Exception e) { throw new Exception("error", e); } finally { oout.close(); } }
      */
 
     public static byte[] concat(byte[] first, byte[] second) {
         byte[] result = new byte[first.length + second.length];
         System.arraycopy(first, 0, result, 0, first.length);
         System.arraycopy(second, 0, result, first.length, second.length);
         return result;
     }
 
     public static byte[] shorten(byte[] array, int length) {
         if (length == array.length) {
             return array;
         }
         byte[] result = new byte[length];
         System.arraycopy(array, 0, result, 0, length);
         return result;
     }
 
     public static byte[] skip(byte[] array, int skiplength) {
         byte[] result = new byte[array.length - skiplength];
         System.arraycopy(array, skiplength, result, 0, result.length);
         return result;
     }
 
     public static byte[] overwrite(byte[] array, byte[] into, int offset) {
         byte[] result = new byte[into.length];
         System.arraycopy(into, 0, result, 0, into.length);
         System.arraycopy(array, 0, result, offset, array.length);
         return result;
     }
 
     // public static byte[] getRawKey2(byte[] password, String transformation, int keysize,
     // String random_algorithm) throws NoSuchAlgorithmException, UnsupportedEncodingException {
     // byte[] raw = md(password, "SHA-1");
     // Log.v("SHA1-digest-key:", toHex(raw));
     // return raw;
     // }
 
     public static byte[] getRawKey(byte[] salt, byte[] password, String transformation,
             int keysize, String hash_algorithm) throws NoSuchAlgorithmException,
             UnsupportedEncodingException {
         byte[] key = concat(password, salt);
         byte[] hash = md(key, hash_algorithm);
         byte[] raw = shorten(hash, keysize / 8);
         Log.v(MOD, "getRawKey2: salt=" + toHex(salt));
         Log.v(MOD, "getRawKey2: pass=" + toHex(password));
         Log.v(MOD, "getRawKey2: pre-key" + toHex(key));
         Log.v(MOD, "getRawKey2: hash:(" + hash_algorithm + "):" + toHex(hash));
         Log.v(MOD, "getRawKey2: hashed-key:(" + hash_algorithm + "):" + toHex(raw));
         return raw;
     }
 
     // public static byte[] getRawKey1(byte[] password, String transformation, int keysize,
     // String random_algorithm) throws NoSuchAlgorithmException {
     // KeyGenerator kgen = KeyGenerator.getInstance(transformation);
     // SecureRandom sr = SecureRandom.getInstance(random_algorithm);
     // sr.setSeed(password);
     // kgen.init(keysize, sr);
     // SecretKey skey = kgen.generateKey();
     // byte[] raw = skey.getEncoded();
     // Log.v("SHA1-kgen-key:", toHex(raw));
     // return raw;
     // }
 
     public static byte[] makeRandomBytes(int bytes) {
         SecureRandom sr;
         try {
             sr = SecureRandom.getInstance("SHA1PRNG");
             // sr.setSeed(System.nanoTime());
             byte[] salt = new byte[bytes];
             sr.nextBytes(salt);
             // byte[] salt = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16 };
             return salt;
         } catch (NoSuchAlgorithmException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         return null;
     }
 
     public static Cipher makeCipher(byte[] salt, byte[] passphrase, int mode,
             String transformation, int keysize, String hash_algorithm)
             throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
             UnsupportedEncodingException, InvalidAlgorithmParameterException {
         Log.v(MOD, "makeCipher: salt=" + toHex(salt));
         Log.v(MOD, "makeCipher: password:" + Base64.encodeBytes(passphrase));
         Log.v(MOD, "makeCipher: mode=" + mode);
         Log.v(MOD, "makeCipher: transformation: " + transformation);
         Log.v(MOD, "makeCipher: keysize: " + keysize);
         Log.v(MOD, "makeCipher: random_algorithm: " + hash_algorithm);
         byte[] rawKey = getRawKey(salt, passphrase, transformation, keysize, hash_algorithm);
         SecretKeySpec skeySpec = new SecretKeySpec(rawKey, transformation);
         Cipher cipher = Cipher.getInstance(transformation + "/CBC/" + getDefaultPadding());
         byte[] nulliv = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
         cipher.init(mode, skeySpec, new IvParameterSpec(nulliv));
         return cipher;
     }
 
     public static Cipher makeCipherECB(byte[] salt, String password, int mode,
             String transformation, int keysize, String hash_algorithm)
             throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
             UnsupportedEncodingException, InvalidAlgorithmParameterException {
         byte[] rawKey = getRawKey(salt, password.getBytes("UTF-8"), transformation, keysize,
                 hash_algorithm);
         SecretKeySpec skeySpec = new SecretKeySpec(rawKey, transformation);
         Cipher cipher = Cipher.getInstance(transformation + "/ECB/" + getDefaultPadding());
         cipher.init(mode, skeySpec);
         return cipher;
     }
 
     public static String encrypt(byte[] salt, byte[] passphrase, String cleartext)
             throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException,
             BadPaddingException, IllegalBlockSizeException, UnsupportedEncodingException,
             InvalidAlgorithmParameterException {
 
         Cipher c = makeCipher(salt, passphrase, Cipher.ENCRYPT_MODE, getDefaultCrypto(),
                 getDefaultKeySize(), getDefaultHash());
         byte[] result = crypt(c, cleartext.getBytes());
         return Base64.encodeBytes(result);
     }
 
     public static String decrypt(byte[] salt, byte[] passphrase, String encrypted_b64)
             throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException,
             BadPaddingException, IllegalBlockSizeException, IOException,
             InvalidAlgorithmParameterException {
         Cipher c = makeCipher(salt, passphrase, Cipher.DECRYPT_MODE, getDefaultCrypto(),
                 getDefaultKeySize(), getDefaultHash());
         byte[] enrypted = Base64.decode(encrypted_b64);
         byte[] result = crypt(c, enrypted);
         return Base64.encodeBytes(result);
 
     }
 
     public static byte[] crypt(Cipher cipher, byte[] clear) throws NoSuchPaddingException,
             NoSuchAlgorithmException, InvalidKeyException, BadPaddingException,
             IllegalBlockSizeException {
         byte[] encrypted = cipher.doFinal(clear);
         return encrypted;
     }
 
     // String conversion
     public static String toHex(String txt) {
         return toHex(txt.getBytes());
     }
 
     public static String fromHex(String hex) {
         return new String(toByte(hex));
     }
 
     public static byte[] toByte(String hexString) {
         int len = hexString.length() / 2;
         byte[] result = new byte[len];
         for (int i = 0; i < len; i++)
             result[i] = Integer.valueOf(hexString.substring(2 * i, 2 * i + 2), 16).byteValue();
         return result;
     }
 
     public static String toHex(byte[] buf) {
         if (buf == null)
             return "";
         StringBuffer result = new StringBuffer(2 * buf.length);
         for (int i = 0; i < buf.length; i++) {
             appendHex(result, buf[i]);
         }
         return result.toString();
     }
 
     private final static String HEX = "0123456789abcdef";
 
     private static void appendHex(StringBuffer sb, byte b) {
         sb.append(HEX.charAt((b >> 4) & 0x0f)).append(HEX.charAt(b & 0x0f));
     }
 
 }
