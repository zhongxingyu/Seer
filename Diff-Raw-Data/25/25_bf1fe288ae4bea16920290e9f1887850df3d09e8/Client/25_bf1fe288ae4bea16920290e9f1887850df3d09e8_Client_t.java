 package client;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.net.InetAddress;
 import java.net.Socket;
 import java.net.UnknownHostException;
 
 import poker.GUI.Login;
 
 
 /**
  * Client Class used as a main entry point for the program and for
  * initializing parameters.
  * 
  * @author Aleksey
  */
 
 public class Client {
 	
 	/**
 	 * Main entry point in the client side program
 	 * 
 	 * @param args (is ignored)
 	 */
     private static Login login;
 
 	public static void main(String args[]) throws UnknownHostException {
        login = new Login();
 	}
 	
 	public static void start(String name) {
 		
 		try {
 			Socket socket = new Socket(InetAddress.getLocalHost(), 9999);
 			PrintWriter out = new PrintWriter(socket.getOutputStream());
 			out.println(name);
 			out.flush();
 						
 			TaskQueue que = new TaskQueue();
 			Conn conn = new Conn(socket, socket.getOutputStream());
 			
 			
			ClientGame game = new ClientGame(conn);	
			game.run();	
 						
 		} catch (UnknownHostException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		
 		
 		
 	}
 }
 			
 
 			
 			
 
 
