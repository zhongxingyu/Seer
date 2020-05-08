 import java.io.UnsupportedEncodingException;
 import java.nio.charset.Charset;
 import java.security.InvalidAlgorithmParameterException;
 import java.security.InvalidKeyException;
 import java.security.Key;
 import java.security.KeyPair;
 import java.security.KeyPairGenerator;
 import java.security.NoSuchAlgorithmException;
 import java.security.NoSuchProviderException;
 import java.security.SecureRandom;
 import java.security.Security;
 import java.security.Signature;
 import java.security.SignatureException;
 import java.security.PublicKey;
 import java.security.PrivateKey;
 import java.security.MessageDigest;
 
 import javax.crypto.BadPaddingException;
 import javax.crypto.Cipher;
 import javax.crypto.IllegalBlockSizeException;
 import javax.crypto.NoSuchPaddingException;
 import javax.crypto.KeyGenerator;
 import javax.crypto.spec.IvParameterSpec;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 
 //import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
 import org.bouncycastle.jce.provider.BouncyCastleProvider;
 
 
 class CryptoEngine
 {
 	private KeyPairGenerator RSAKeyGen;
 	private KeyGenerator AESKeyGen;
 	public final int keySize;
 
 
     //NOTE: should we be using this cipher instead of a generic one? -John
 	//private PaddedBufferedBlockCipher AESCipher;
 
 //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
 //-- CONSTRUCTOR
 //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
 	
 	public CryptoEngine() 
 	{
 		Security.addProvider(new BouncyCastleProvider());
 		keySize = 128;
 
 		//set up RSA objects
 		try {
 			RSAKeyGen = KeyPairGenerator.getInstance("RSA");
 			RSAKeyGen.initialize(1024);
 		} 
 		catch (NoSuchAlgorithmException e) {
 			System.out.println("WARNING:  CRYPTOENGINE;  RSA key genrator initialization failure");
 		}
 
   		//set up AES objects
         try {
 			AESKeyGen = KeyGenerator.getInstance("AES", "BC");
 			AESKeyGen.init(128);
 		} 
 		catch (Exception e) {
 			System.out.println("WARNING:  CRYPTOENGINE;  AES key genrator initialization failure");
 		}
 	}
 
 //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
 //-- HASH FUNCTIONS
 //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
 	
 	public byte[] hashString(String str)
 	{
 		MessageDigest md = null;
 		try
 		{
 			md = MessageDigest.getInstance("SHA");
 		}
 		catch(Exception e)
 		{
 			System.out.println("WARNING: Could not hash password");
 			e.printStackTrace();
 		}
 		return md.digest(str.getBytes());
 	}
 	
 	
 //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
 //-- KEY GENERATORS
 //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
 
 	public AESKeySet genAESKeySet()
 	{
 		//gent a key, IV pair and return it
 	    return new AESKeySet(AESKeyGen.generateKey(), new IvParameterSpec(new byte[16]));
 	}
 	public KeyPair genRSAKeyPair()
 	{
 		KeyPair keys = null;
 		try
 		{
 			keys = RSAKeyGen.generateKeyPair();
 		}
 		catch(Exception e)
 		{
 			System.out.println("WARNING:  CRYPTOENGINE;  RSAS key gen failure");
 			return null;
 		}
   		return keys;
 	}
 
 //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
 //-- SIGNING AND VERIFICATION 
 //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
 
 	//return the signature as a byte array
 	public byte[] RSASign(byte[] plainText, PrivateKey key) 
 	{
   	    Signature signature;
   	    byte[] sigBytes = null;
 		try 
 		{
 			signature = Signature.getInstance("SHA1withRSA", "BC");
 			signature.initSign(key, new SecureRandom()); 
 			signature.update(plainText);
 			sigBytes = signature.sign();
 		} 
 		catch (Exception e) 
 		{
 			System.out.println("WARNING:  CRYPTOENGINE;  RSA sign failure");
 		}
 		return sigBytes;
 	}
 
 	//return boolean of verified signature in byte form
 	public boolean RSAVerify(byte[] plainText, byte[] sigBytes, PublicKey key) 
 	{
 		boolean verified = false;
 	  	Signature signature;
 		try 
 		{
 			signature = Signature.getInstance("SHA1withRSA", "BC");
 		    signature.initVerify(key);
 			signature.update(plainText);
 		    verified = signature.verify(sigBytes);
 		} 
 		catch (Exception e) 
 		{
 			System.out.println("WARNING:  CRYPTOENGINE;  RSA signature verification failure");
 		}
 		return verified;
 	}
 	
 
 //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
 //-- ENCRYPTION WRAPPERS AND DRIVER FUNCTIONS
 //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
 	
 	//wrappers for encrypt and decrypt
 	public byte[] AESEncrypt(byte[] plainText, AESKeySet keySet) 
 	{
 		return DriverCoreFunction(plainText,  Cipher.ENCRYPT_MODE, keySet);
 	}
 	public byte[] AESDecrypt(byte[] cipherText, AESKeySet keySet) 
 	{
 		return DriverCoreFunction(cipherText, Cipher.DECRYPT_MODE, keySet);
 	}
 	//performs encryption and decryption for aAES
 	public byte[] DriverCoreFunction(byte[] bytes, int mode, AESKeySet keySet) 
 	{
 		Cipher cipher = null;
 		byte[]result = null;
 		Key key = keySet.getKey();
 		IvParameterSpec IV = keySet.getIV();
 
 		try 
 		{
 			cipher = Cipher.getInstance("AES/CBC/PKCS5Padding", "BC");
 			cipher.init(mode, key, IV);
 			result = cipher.doFinal(bytes);	
 		} 
 		catch (Exception e) {
 			System.out.println("WARNING:  CRYPTOENGINE;  AES cipher failure; encrypt(1)/decrypt(2)="+mode);
			e.printStackTrace();
 		}
 		 
 		 return result;
 	}
 
 	public byte[] RSAEncrypt(byte[] plainText, Key key) 
 	{		
 		return DriverCoreFunction(plainText, Cipher.ENCRYPT_MODE, (Key)key);
 	}
 	public byte[] RSADecrypt(byte[] cipherText, Key key) 
 	{
 		return DriverCoreFunction(cipherText, Cipher.DECRYPT_MODE, (Key)key);
 	}
 
 	//performs encryption and decryption for RSA
 	public byte[] DriverCoreFunction(byte[] bytes,  int mode, Key key) 
 	{
 		Cipher cipher = null;
 		byte[]result = null;
 
 		try 
 		{
 			cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
 			cipher.init(mode, key);	
 			result = cipher.doFinal(bytes);	
 		} 
 		catch (Exception e) 
 		{
 			System.out.println("WARNING:  CRYPTOENGINE;  RSA cipher failure;  encrypt(1)/decrypt(2)="+mode);
 			e.printStackTrace();
 		}
 		 
 		 return result;
 	}
 
 //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
 //-- EXTERNAL UTILITY FUNCTIONS
 //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
 
 	//string specific serializers/deserializers
 	public String byteToStr(byte[] input)
 	{
 	    byte[] output = new byte[input.length];
 	    int i = 0;
 
 	    for (Byte current : input) 
 	    {
 	        output[i] = current;
 	        i++;
 	    }
 
 		return new String(output,  Charset.forName("ISO-8859-1"));	
 	}
 
 	public byte[] strToByte( String input) 
 	{
 		try 
 		{
 			return input.getBytes("ISO-8859-1");
 		} 
 		catch (UnsupportedEncodingException e) 
 		{
 			System.out.println("WARNING:  CRYPTOENGINE;  string to byte conversion failure");
 			return null;
 		}
 	}
 
 	//general purpose serializer
     public byte[] serialize(Object obj)
     {
     	byte[] byteArray = null;
     	try
     	{
         	ByteArrayOutputStream bStream = new ByteArrayOutputStream();
         	ObjectOutputStream oStream = new ObjectOutputStream(bStream);
         	oStream.writeObject(obj);
         	byteArray = bStream.toByteArray();
 		}
         catch(Exception e)
         {
 			System.out.println("WARNING:  CRYPTOENGINE;  serializing error, NULL returned");
         	return null;
         }
         return byteArray;
     }
 
 	//general purpose deserializer
     public Object deserialize(byte[] bytes)
     {
     	Object obj = null;
     	try
     	{
 	        ByteArrayInputStream bStream = new ByteArrayInputStream(bytes);
 	        ObjectInputStream oStream = new ObjectInputStream(bStream);
 	        obj = oStream.readObject();
 		}
         catch(Exception e)
         {
 			System.out.println("WARNING:  CRYPTOENGINE;  deserializing error, NULL returned");
         	return null;
         }
         return obj;
     }
 }
 
