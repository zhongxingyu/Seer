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
    	catch(java.net.ConnectException e)
    	{
    		System.out.println(Message.getSSLError("NoConnection"));
    		System.out.println("[CRITICAL]: Closing server.");
    	}
     	catch(IOException e)
     	{
     		System.out.println(Message.getSSLError("WrongKey"));
     		System.out.println("[CRITICAL]: Closing server.");
     	} catch (UnrecoverableKeyException e) {
    		System.out.println(Message.getSSLError("UnrecoverableKeyException"));
    		System.out.println("[CRITICAL]: Closing server.");
 		} catch (KeyManagementException e) {
			System.out.println(Message.getSSLError("KeyManagementException"));
    		System.out.println("[CRITICAL]: Closing server.");
 		} catch (KeyStoreException e) {
			System.out.println(Message.getSSLError("KeyStoreException"));
    		System.out.println("[CRITICAL]: Closing server.");
 		} catch (NoSuchAlgorithmException e) {
			System.out.println(Message.getSSLError("NoSuchAlgorithmException"));
    		System.out.println("[CRITICAL]: Closing server.");
 		} catch (CertificateException e) {
 			System.out.println(Message.getSSLError("WrongCertificate"));
     		System.out.println("[CRITICAL]: Closing server.");
 		}
     }
 }
