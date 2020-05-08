 package client;
 
 import java.io.IOException;
 import java.io.ObjectOutputStream;
 import java.net.InetAddress;
 
 import networking.packages.*;
 
 public class Client extends Thread{
 	
 	
 	
 	
 	public Client(InetAddress serverAddress) throws IOException{
 		
 		
 	}
 	
 	
 	public void sendRequest(NetworkRequest request) throws IOException{
 		System.out.println("sending request: "+request);
 		ObjectOutputStream out = new ObjectOutputStream(Program.server.getOutputStream());
 		out.writeObject(request);
 //		out.close();
 //		server.close();
 	}
 	
 	
 }
 
