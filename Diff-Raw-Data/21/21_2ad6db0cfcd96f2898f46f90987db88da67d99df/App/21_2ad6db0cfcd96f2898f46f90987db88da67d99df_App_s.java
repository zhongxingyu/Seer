 package com.opencsi.SSLTunnel;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.security.KeyManagementException;
 import java.security.KeyStoreException;
 import java.security.NoSuchAlgorithmException;
 import java.security.UnrecoverableKeyException;
 import java.security.cert.CertificateException;
 
 import com.opencsi.core.Core;
 import com.opencsi.entity.Message;
 
 public class App 
 {
 	
     public static void main( String[] args )
     {    
     	try{
     		(new Core(args)).run();
     	}
     	catch(FileNotFoundException e)
     	{
     		System.out.println(Message.getSSLError("FileNotFound"));
     		System.out.println("[CRITICAL]: Closing server.");
     	}
     	catch(IOException e)
     	{
     		System.out.println(Message.getSSLError("WrongKey"));
     		System.out.println("[CRITICAL]: Closing server.");
     	} catch (UnrecoverableKeyException e) {
    		e.printStackTrace();
 		} catch (KeyManagementException e) {
			e.printStackTrace();
 		} catch (KeyStoreException e) {
			e.printStackTrace();
 		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
 		} catch (CertificateException e) {
 			System.out.println(Message.getSSLError("WrongCertificate"));
     		System.out.println("[CRITICAL]: Closing server.");
 		}
     }
 }
