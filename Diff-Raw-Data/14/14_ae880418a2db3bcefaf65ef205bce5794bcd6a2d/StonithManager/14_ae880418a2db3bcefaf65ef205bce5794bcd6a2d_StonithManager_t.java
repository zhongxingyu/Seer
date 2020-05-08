 package server;
 
 import java.io.IOException;
 import java.net.ServerSocket;
 
 public class StonithManager extends Thread{
 	/*Set to true if you want the program to display debugging messages.*/
 	boolean debugging = true;
 	
 	/* The STONITH port. */
 	private int stonithPort;
 	
 	public StonithManager(int stPort){
 		stonithPort = stPort;
		this.start();
 	}
 	
 	public void run(){
 		/* We open the socket connection. */
         ServerSocket listenSocket;
 		try {
 			while (true){
 				/* First, we open the STONITH port.*/
 				listenSocket = new ServerSocket(stonithPort);
 				if (debugging){
 		        	System.out.println("Listening at port  " + stonithPort);
 		        	System.out.println("LISTEN SOCKET=" + listenSocket);
 		        }
 				
 				/* Then, if the other client tries to connect to this port, 
 				 * we will accept the connection to show the other server
 				 * that we aren't dead and the problem of not receiving messages
 				 * is due to troubles in the link and not to troubles in our
 				 * machine.
 				 */
 				listenSocket.accept(); // BLOQUEANTE
 			}
 			
 		} catch (IOException e) {
 			System.out.println("Listen in Stonith Manager:" + e.getMessage());
 		}
         
 	}
 }
