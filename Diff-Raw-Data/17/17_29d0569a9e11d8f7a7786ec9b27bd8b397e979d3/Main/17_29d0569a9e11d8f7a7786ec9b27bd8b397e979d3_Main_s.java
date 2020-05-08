 package client;
 
 import java.io.IOException;
 
import server.MockServer;

 public class Main {
 	
 	private final static int DEFAULT_PORT = 9882;
 
 	public static void main(String[] args) {
 		try{
 			Client c = new Client(DEFAULT_PORT);
 		} catch(IOException e){
 			e.printStackTrace();
 		}
 	}
 }
