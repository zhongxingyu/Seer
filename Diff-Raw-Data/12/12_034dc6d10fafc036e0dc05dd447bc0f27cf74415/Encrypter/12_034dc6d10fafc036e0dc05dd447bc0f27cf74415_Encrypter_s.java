 /**
  * 
  */
 package cpsc551.HadoopEncrypt.Encrypter;
 
 import javax.crypto.Cipher;
 import javax.crypto.IllegalBlockSizeException;
 import javax.crypto.SecretKey;
 
 /**
  * Performs encryption and decryption operations
  * @author Chad Wyszynski
  *
  */
 public class Encrypter 
 {
 	private Cipher encrypter;
 	private Cipher decrypter;
 	
 	/**
 	 * Creates a new Encrypter to perform encryption and decryption operations
 	 * @param key key to be used with symmetric cipher
 	 */
	public Encrypter(SecretKey key)
 	{
		try
		{
 			encrypter = Cipher.getInstance("AES");
 			decrypter = Cipher.getInstance("AES");
 			encrypter.init(Cipher.ENCRYPT_MODE, key);
 			decrypter.init(Cipher.DECRYPT_MODE, key);
		}
		catch(Exception e) {}; //TODO Actually handle exceptions
 	}
 	
 	/**
 	 * Encrypts a string
 	 * @param s string to encrypt
 	 * @return encrypted version of string
 	 */
 	public byte[] encrypt(byte[] bytes)
 	{
 		try
 		{			
 			return encrypter.doFinal(bytes);
 		}
 		catch(Exception e) {}; //TODO exception catching?
 		return null;
 	}
 	
 	/**
 	 * Decrypts an encrypted string string
 	 * @param encrypted byte sequence to decrypt
 	 * @return decrypted version of string
 	 */
 	public byte[] decrypt(byte[] encrypted)
 	{
 		try
 		{
 			return decrypter.doFinal(encrypted);
 		}
 	    catch (javax.crypto.BadPaddingException e) 
 	    {
 	    	byte[] b = {1};//TODO exception catching?
 	    	return b;
 	    } 
 		catch (IllegalBlockSizeException e) 
 		{
 			byte[] b = {2};
 	    	return b;
 	    } 
 	}
 
 	/**
 	 * Access block size of underlying cipher
 	 * @return block size of underlying cipher
 	 */
 	public int getBlockSize()
 	{
 		return encrypter.getBlockSize();
 	}
 }
