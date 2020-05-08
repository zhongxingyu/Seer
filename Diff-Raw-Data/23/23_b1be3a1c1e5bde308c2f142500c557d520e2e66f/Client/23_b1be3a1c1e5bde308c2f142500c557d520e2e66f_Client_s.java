 package org.sslexample.client;
 
 import java.io.IOException;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.util.logging.Logger;
 
 import javax.net.SocketFactory;
 import javax.net.ssl.SSLSocketFactory;
 
 import org.sslexample.trustall.TrustAllSSLSocketFactory;
 
 public class Client {
 	public static void main(String[] args) throws UnknownHostException, IOException {
 		TrustAllSSLSocketFactory.trustAllCerts();
 		SocketFactory sockMaker = SSLSocketFactory.getDefault();
		Socket server = sockMaker.createSocket("localhost", 1443);
		Logger.getAnonymousLogger().info("Connected to "+server.toString());
 		byte[] b = new byte[10];
		int retval = server.getInputStream().read();
 		Logger.getAnonymousLogger().info("Read with return v: "+retval);
 		Logger.getAnonymousLogger().info("Read a single byte: "+(int)b[0]);
 		
		server.close();
 	}
 
 }
