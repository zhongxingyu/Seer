 
 import java.io.BufferedOutputStream;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.security.MessageDigest;
 import java.security.cert.X509Certificate;
 import java.util.Hashtable;
 
 import javax.crypto.Cipher;
 import javax.crypto.CipherInputStream;
 import javax.crypto.SecretKey;
 import javax.crypto.SecretKeyFactory;
 import javax.crypto.spec.PBEKeySpec;
 import javax.crypto.spec.PBEParameterSpec;
 
 public class Communicate {
 
 	static X509Certificate cert = null;
	static String fserverIp = null;
 	static boolean isConnected = false;
 	public static boolean checkAuthentication()
 	{
 		if(cert != null)
 			return true;
 		else
 			return false;
 				
 	}
 	public static boolean Login(String hash)
 	{
 		if(hash != null && !hash.trim().equals(""))
 		{
 		    cert = Encrypt.Login(hash);
 		    return checkAuthentication();
 		}
 		else
 			return false;
 	}
 	
 	public static byte[] Encrypt(byte[] data)
 	{
 		try{
         return Encrypt.EncryptData(data);
 		}
 		catch(Exception ex)
 		{
 			return null;
 		}
 	}
 
 	public static byte[] Decrypt(byte [] data)
 	{
 		try{
         return Encrypt.Decrypt(data);
 		}
 		catch(Exception ex)
 		{
 			return null;
 		}
 	}
 	
 	public static boolean Login(String hash, String caAddress)
 	{
 		if(caAddress != null && !caAddress.trim().equals(""))
 		{
 		   cert = Encrypt.Login(hash, caAddress);
 		   return checkAuthentication();
 		}
 		else
 			return false;
 	}
 	
 	
 	public static boolean Logout()
 	{
 		return Encrypt.logout();
 	}
 	public static Hashtable sendMsg(Hashtable table, String ipAddress, int port)
 	{
		if(!isConnected || !fserverIp.equals(ipAddress)){
 			isConnected = Encrypt.initiate(ipAddress, port);
			fserverIp = ipAddress;
		}
 		return Encrypt.sendMsg(table);
 	}
 }
