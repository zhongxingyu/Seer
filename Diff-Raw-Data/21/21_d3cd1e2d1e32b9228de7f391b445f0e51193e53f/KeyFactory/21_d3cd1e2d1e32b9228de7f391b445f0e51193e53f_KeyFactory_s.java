 package com.atlassian.refapp.trustedapps.internal;
 
 import java.security.Key;
 import java.security.KeyPair;
 import java.security.NoSuchAlgorithmException;
 import java.security.NoSuchProviderException;
 import java.security.PrivateKey;
 import java.security.PublicKey;
 import java.security.spec.InvalidKeySpecException;
 
 import com.atlassian.sal.api.pluginsettings.PluginSettings;
 import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
 import com.atlassian.security.auth.trustedapps.EncryptionProvider;
 import com.atlassian.user.util.Base64Encoder;
 
 /**
  * Factory for getting key pairs to use in trusted applications.  Stores keys in the pluginsettings for possible
  * persistence between restarts.
  */
 public class KeyFactory
 {
     private EncryptionProvider encryptionProvider;
 	private PluginSettings pluginSettings;
 
 	public KeyFactory(EncryptionProvider encryptionProvider, PluginSettingsFactory pluginSettingsFactory)
     {
         this.encryptionProvider = encryptionProvider;
         pluginSettings = pluginSettingsFactory.createSettingsForKey(getClass().getName());
     }
 
 	public KeyPair getKeyPair()
 	{
 		KeyPair keyPair;
         if (pluginSettings.get("public-key") != null)
         {
             keyPair = fetchKeyPair();
         }
         else
         {
             keyPair = createKeyPair();
             storeKeyPair(keyPair);
         }
         return keyPair;
 	}
 	
     private KeyPair createKeyPair()
     {
         try
         {
             return encryptionProvider.generateNewKeyPair();
         }
         catch (NoSuchAlgorithmException e)
         {
             throw new IllegalArgumentException("No such algorithm", e);
         }
         catch (NoSuchProviderException e)
         {
             throw new IllegalArgumentException("No such provider", e);
         }
     }
 
     private void storeKeyPair(KeyPair keyPair)
     {
         pluginSettings.put("public-key", encode(keyPair.getPublic()));
         pluginSettings.put("private-key", encode(keyPair.getPrivate()));
     }
     
     private String encode(Key key)
     {
     	return new String(Base64Encoder.encode(key.getEncoded()));
     }
 
     private KeyPair fetchKeyPair()
     {
     	return new KeyPair(fetchPublicKey(), fetchPrivateKey());
     }
     
     private PrivateKey fetchPrivateKey()
     {
    	String keyStr = (String) pluginSettings.get("public-key");
         final byte[] data = Base64Encoder.decode(keyStr.getBytes());
         try
         {
             return encryptionProvider.toPrivateKey(data);
         }
         catch (NoSuchProviderException e)
         {
             return new InvalidPrivateKey(e);
         }
         catch (NoSuchAlgorithmException e)
         {
             return new InvalidPrivateKey(e);
         }
         catch (InvalidKeySpecException e)
         {
             return new InvalidPrivateKey(e);
         }
     }
     
     private PublicKey fetchPublicKey()
     {
     	String keyStr = (String) pluginSettings.get("public-key");
         final byte[] data = Base64Encoder.decode(keyStr.getBytes());
         try
         {
             return encryptionProvider.toPublicKey(data);
         }
         catch (NoSuchProviderException e)
         {
             return new InvalidPublicKey(e);
         }
         catch (NoSuchAlgorithmException e)
         {
             return new InvalidPublicKey(e);
         }
         catch (InvalidKeySpecException e)
         {
             return new InvalidPublicKey(e);
         }
     }
 
 
     /**
      * If there are problems creating a key, one of these will be returned instead.
      * Rather than returning the actual key, the toString() will return the causal exception.
      */
     public static class InvalidPrivateKey extends InvalidKey implements PrivateKey
     {
         public InvalidPrivateKey(Exception cause)
         {
             super(cause);
         }
     }
 
     /**
      * If there are problems creating a key, one of these will be returned instead.
      * Rather than returning the actual key, the toString() will return the causal exception.
      */
     public static class InvalidPublicKey extends InvalidKey implements PublicKey
     {
         public InvalidPublicKey(Exception cause)
         {
             super(cause);
         }
     }
 
     static class InvalidKey implements Key
     {
         private final Exception cause;
 
         public InvalidKey(Exception cause)
         {
             this.cause = cause;
         }
 
         public String getAlgorithm()
         {
             return "";
         }
 
         ///CLOVER:OFF
         public String getFormat()
         {
             return "";
         }
         ///CLOVER:ON
 
         public byte[] getEncoded()
         {
             return new byte[0];
         }
 
         public String toString()
         {
             return "Invalid Key: " + cause.toString();
         }
 
         public Exception getCause()
         {
             return cause;
         }
     }
 }
