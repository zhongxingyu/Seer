 package network;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.Socket;
 
 import logic.User;
 
 import requests.LogInRequest;
 import requests.XMLParsable;
 
 public class ServerConnectionHandler extends Thread {
 
 	private Socket connection;
 	private ServerConnection serverConnection;
 	private BufferedReader reader;
 	private PrintWriter writer;
 	
 	public ServerConnectionHandler(Socket connection, ServerConnection serverConnection) {
 		this.connection = connection;
 		this.serverConnection = serverConnection;
 		serverConnection.addConnectionHandler(this);
 		try {
 		reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
 			writer = new PrintWriter(connection.getOutputStream(), true);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	@Override
 	public void run() {
 		System.out.println("ServerConnectionHandler running!");
 		String xml;
 		while((xml = receive()) != null) {
 			System.out.println("Received XML: " + xml);
 			// Receive LogInRequest
 			try {
 				Object o = XMLParsable.toObject(xml);
 				if(o instanceof LogInRequest) {
 					System.out.println("Received log in request!");
 					LogInRequest req = (LogInRequest)o;
 					User user = serverConnection.getUser(req.getUserName());
 					if(user != null && user.getPassword().equals(req.getPassword()) && !user.isOnline()) {
 						System.out.println("Log in OK!");
 						user.setIsOnline(true);
 						req.setAccepted(true);
 						if(req.isAccepted()) {
 							System.out.println("Log in is set to accepted!");
 						}
 					} else {
 						System.out.println("Username or password failed");
 					}
 					String temp = req.toXml();
 					System.out.println("Sending XML: " + temp);
 					send(temp);
 				} else {
 					System.out.println("Received something unknown!");
 					// TODO
 				}
 			} catch(Exception e) {
 				// TODO
 			}
 		}
 		// TODO
 	}
 	
 	public String receive() {
 		try {
 			return reader.readLine();
 		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	public void send(String msg) {
 		writer.println(msg);
 	}
 	
 	public void close() {
 		try {
 			connection.close();
 		} catch (Exception e) {}
 		try {
 			reader.close();
 		} catch (Exception e) {}
 		try {
 			writer.close();
 		} catch (Exception e) {}
 	}
 }
