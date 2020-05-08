 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintStream;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.SocketException;
 
 import org.freedesktop.dbus.exceptions.DBusException;
 
 public class Server {
 
 	// port number should be more than 1024
 
 	public static final int PORT = 4242;
 	private static boolean run = true;
 	private static ServerSocket sersock = null;
 	private static Socket sock = null;
 
 	public static void main(String args[]) {
 
 		System.out.println(" Waiting for command !! ");
 		
 		  try { new NewClass(); } catch (DBusException e) { // TODO
 		 e.printStackTrace(); }
 		 
 		try {
 			// Initialising the ServerSocket
 			sersock = new ServerSocket(PORT);
 
 			// Gives the Server Details Machine name, Port number
 			System.out.println("Server Started  :" + sersock);
 
 			try {
 
 				// makes a socket connection to particular client after
 				// which two way communication take place
 				sock = sersock.accept();

 				System.out.println("Client Connected  :" + sock);			 
 
 				// Send message to the client i.e Response
 				PrintStream ios = new PrintStream(sock.getOutputStream());
 				ios.println("Hello from server");
 				ios.println("Comment va ?");
				ios.close();
 				
 				// Receive message from client i.e Request from client
 				BufferedReader is = new BufferedReader(new InputStreamReader(sock.getInputStream()));
 			
 				CommandParser parser = new CommandParser();
 				System.out.println("Parsing commands");
 				while (run) { 
 					String str = is.readLine();
					System.out.println(str);
 
 					if (!str.isEmpty()) {
 						Command c = parser.parse(str);
 						switch (c.getCommand()) {
 						case PLAY:
 							System.out.println("Play");
 							try {
 								new NewClass();
 							} catch (Exception e) {
 								System.out.println("Erreur DBUS");
 								e.printStackTrace();
 							}
 							break;
 						case QUIT:
 							System.out.println("Quit");
 							run = false;
 							break;
 						}
 					}
 				}
 			} catch (SocketException se) {
 				System.out.println("Server Socket problem  " + se.getMessage());
 				se.printStackTrace();
 			} catch (Exception e) {
 				System.out.println("Couldn't start " + e.getMessage());
 			}
 
 			// Usage of some methods in Socket class
 			System.out.println(" Connection from :  " + sock.getInetAddress());
 
 		} catch (Exception e) {
 			System.out.println("Erreur de connection.");
 		}
 	}
 
 	private void quit() {
 		// Close the Socket connection
 		try {
 			sock.close();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 } // Server class
