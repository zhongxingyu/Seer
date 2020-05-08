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
 
 import javax.crypto.BadPaddingException;
 import javax.crypto.Cipher;
 import javax.crypto.IllegalBlockSizeException;
 import javax.crypto.NoSuchPaddingException;
 import javax.crypto.KeyGenerator;
 import javax.crypto.spec.IvParameterSpec;
 
 import java.util.List;
 
 public class UserToken implements UserTokenInterface, java.io.Serializable
 {
 	private String issuer; 
 	private String subject; 
     private List<String> groups;
     private byte[] signature;
    private CryptoEngine cEngine;
 
 	public UserToken(String Issuer, String Subject)
 	{
 		issuer = Issuer; 
 		subject = Subject; 
         signature = null;
 	}
 
     public UserToken(String Issuer, String Subject, List<String> Groups)
     {
         issuer = Issuer; 
         subject = Subject; 
         groups = Groups;
         signature = null;
        CryptoEngine cEngine = new CryptoEngine();
     }
 
     public String getIssuer()
     {
     	return issuer; 
     }
 
     public String getSubject()
     {
     	return subject;
     }
 
     public List<String> getGroups()
     {
         return groups; 
     }
 
     public Boolean addGroup(String group)
     {
         return groups.add(group);
     }
 
     public Boolean inGroup(String group)
     {
         return groups.contains(group);
     }
 
     public Boolean removeGroup(String group)
     {
         return groups.remove(group);
     }
 
     public int groupCount()
     {
         return groups.size();
     }
 
     public void clearGroups()
     {
         groups.clear();
     }
 
 //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
 //-- SIGNING AND VERIFICATION 
 //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
 
     public void sign(PrivateKey key)
     {
     }
 
 
     public boolean verifySignature(PrivateKey key)
     {
         return false;
     }
 
 //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
 //-- Utility Functions
 //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

     private byte[] getContentsInBytes()
     {
        return null;
     }
 }
