 package org.sslexample.server;
 
 import java.io.FileInputStream;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.security.KeyStore;
 import java.util.logging.Logger;
 
 import javax.net.ServerSocketFactory;
 import javax.net.ssl.KeyManagerFactory;
 import javax.net.ssl.SSLContext;
 import javax.net.ssl.SSLServerSocket;
 import javax.net.ssl.SSLSocket;
 
 public class Server {
 	static String keystore = "selfsigned.jks";
 	public static final int SSL_PORT = 1443;
 	static char keystorepass[] = "123456".toCharArray();
 	static char keypassword[] = "123456".toCharArray();
 	public static ServerSocket getServer() throws Exception {
 		KeyStore ks = KeyStore.getInstance("JKS");
 		ks.load(new FileInputStream(keystore), keystorepass);
 		KeyManagerFactory kmf = 
 			KeyManagerFactory.getInstance("SunX509");
 		kmf.init(ks, keypassword);
 		SSLContext sslcontext = 
 			SSLContext.getInstance("SSLv3");
 		sslcontext.init(kmf.getKeyManagers(), null, null);
 		ServerSocketFactory ssf = 
 			sslcontext.getServerSocketFactory();
 		SSLServerSocket serversocket = (SSLServerSocket) 
 		ssf.createServerSocket(SSL_PORT);
 		return serversocket;
 
 	}
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		ServerSocket listen;
 		try {
 			listen = getServer();
 			Logger.getAnonymousLogger().info("Listening to "+listen.toString());
 			if(true) {
				SSLSocket toClient = (SSLSocket)listen.accept();
//				toClient.startHandshake();
				// If you'll enable above line, empty connection will work
				Logger.getAnonymousLogger().info("Accpeted: "+toClient.toString());
				toClient.close();
 				Logger.getAnonymousLogger().info("Closed");
 			}
 		} catch(Exception e) {
 			System.out.println("Exception: "+e.getMessage());
 		}
 	}
 
 }
