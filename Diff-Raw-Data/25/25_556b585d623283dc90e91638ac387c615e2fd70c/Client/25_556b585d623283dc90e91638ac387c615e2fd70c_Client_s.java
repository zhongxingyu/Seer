 package client;
 
 import java.security.*;
 import java.security.cert.CertificateException;
 import java.util.Scanner;
 import java.io.*;
 import java.net.*;
 
 import javax.net.ssl.KeyManagerFactory;
 import javax.net.ssl.SSLContext;
 import javax.net.ssl.SSLEngine;
 import javax.net.ssl.SSLEngineResult;
 import javax.net.ssl.SSLServerSocketFactory;
 import javax.net.ssl.SSLSession;
 import javax.net.ssl.SSLSocket;
 import javax.net.ssl.SSLSocketFactory;
 import javax.net.ssl.TrustManagerFactory;
 
 import util.NetworkCommunication;
 import util.PasswordManager;
 
 public class Client {
 
 	private static final int PORT = 5678;
 
 	public static void main(String[] args) {
 
		PasswordManager pwMn = new PasswordManager();
 
 		System.setProperty("javax.net.ssl.trustStore",
 				"./certificates/CA/truststore");
 
 		SSLSocketFactory factory = null;
 		SSLContext ctx = null;
 		KeyManagerFactory kmf = null;
 		KeyStore ks = null;
 		TrustManagerFactory tmf = null;
 
 		boolean notFound = true;
 		FileInputStream stream = null;
 		String pass = null;
 		String name = null;
 
 		while (notFound) {
 			try {
 				Scanner scan = new Scanner(System.in);
 				System.out.print("Namn: ");
 				name = scan.next();
				System.out.print("Password: ");
 				pass = scan.next();
 
 				stream = new FileInputStream("./certificates/" + name + "/"
 						+ name + ".jks");
 				notFound = false;
 			} catch (FileNotFoundException e) {
 				notFound = true;
 				System.out.println("Wrong name or password, please try again");
 			}
 
 		}
 
 		try {
 			char[] passphrase = pass.toCharArray();
 
 			ctx = SSLContext.getInstance("TLS");
 			kmf = KeyManagerFactory.getInstance("SunX509");
 			ks = KeyStore.getInstance("JKS");
 			tmf = TrustManagerFactory.getInstance("SunX509");
 
 			ks.load(stream, passphrase);
 
 			kmf.init(ks, passphrase);
 			tmf.init(ks);
 			ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
 
 			factory = ctx.getSocketFactory();
 
 			SSLSocket client = (SSLSocket) factory.createSocket("localhost",
 					PORT);
 
 			client.setUseClientMode(true);
 			client.startHandshake();
 			System.out.println(client);
 
 			PrintWriter toServer = new PrintWriter(new OutputStreamWriter(
 					client.getOutputStream()), true);
 			BufferedReader fromServer = new BufferedReader(
 					new InputStreamReader(client.getInputStream()));
 
 			NetworkCommunication nc = new NetworkCommunication(toServer,
 					fromServer);
 
 			nc.send(name);
 
 			// Parse welcome message
 			System.out.println("Welcome: " + nc.receive());
 
 			Scanner sc = new Scanner(System.in);
 
 			// http://en.wikipedia.org/wiki/REPL
 			String response = null;
 			String userInput = null;
 			do {
 				System.out.print("Enter command: ");
 
 				// Read - input from client -> server
 				userInput = sc.nextLine();
 				System.out.println("Input: " + userInput);
 				nc.send(userInput);
 
 				// Eval - output form server -> client
 				response = nc.receive();
 				System.out.println("Read Server:");
 				System.out.println(response);
 
 				// Print - print result
 				// System.out.println("Handled: " + response);
 
 				// Loop - repeat!
 			} while (response != null);
 
 		} catch (UnknownHostException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (NoSuchAlgorithmException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (KeyStoreException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (CertificateException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (UnrecoverableKeyException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (KeyManagementException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 
 		}
 	}
 }
