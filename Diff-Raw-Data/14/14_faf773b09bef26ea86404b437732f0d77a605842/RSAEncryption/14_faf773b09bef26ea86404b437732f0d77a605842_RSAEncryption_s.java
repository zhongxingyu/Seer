 import java.io.File;
 import java.util.ArrayList;
 import java.math.BigInteger;
 /**
  * This class does RSA encryption/decryption
  * @author Jonathan Nanney
  * */
 public class RSAEncryption 
 {
    /** The key to encrypt/decrypt with */
    private RSABaseKey key;
    /** The data to encrypt/decrypt */
    private BigInteger original;
    /** Makes BigIntegers positive */
    private int SIGN=1;
    
    /**
     * Constructor that takes the data to encrypt/decrypt and a RSA Key.
     * @param data - the data to encrypt/decrypt
     * @param key - the RSA key to use for encryption/decryption
     * */
    public RSAEncryption(byte[] data, RSABaseKey key)
    {
       original = new BigInteger(SIGN, data);
       this.key = key;
    }
 
    /**
     * Constructor that takes the data to encrypt/decrypt and a RSA key.
     * @param original - the data to encrypt/decrypt
     * @param key - the RSA key to use for encryption/decryption
     * */
   public RSAEncryption(BigInteger original, RSABaseKey key)
    {
       this.original = original;
       this.key = key;
   }
    
    /**
     * Constructor that takes the data to encrypt/decrypt and will generate a
     * new key of the given length.
     * @param data - the data to encrypt/decrypt
     * @param keylength - the length of the key (in bits) to generate 
     * */
    public RSAEncryption(byte[] data, int keylength)
    {
       original = new BigInteger(SIGN, data);
       this.key = new RSAPrivateKey(keylength);
    }
    
    /**
     * Encrypts the data
     * @return the encrypted data
     * */
    public BigInteger encrypt()
    {
 	   BigInteger n = key.getPrimeProduct();
       if(original.compareTo(n) > 0)
       {
          System.err.println("Message too large.  Implement splitting");
       }
 
       BigInteger cyphertext = original.modPow(key.getEncryptionExponent(), n);
 	   return cyphertext;
    }
    
    /**
     * Decrypts the data
     * @return the decrypted data
     * */
    public BigInteger decrypt()
    {
       if(!(key instanceof RSAPrivateKey))
       {
          System.err.println("Messages may only be decrypted with private keys");
          System.exit(1);
       }
 
       RSAPrivateKey privateKey = (RSAPrivateKey) key;
 	   BigInteger n = key.getPrimeProduct();
       BigInteger result = original.modPow(privateKey.getDecryptionExponent(), n);
 	   return result;
    }
    
    /**
     * Allows you to switch the data to encrypt/decrypt
     * @param newText - the new data to encrypt/decrypt
     * */
    public void switchOriginal(BigInteger newText)
    {
       this.original = newText;
    }
 
 }
