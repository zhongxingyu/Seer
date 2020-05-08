 package guess_my_number_server;
 
 /**
  */
 
 import java.io.*;
 import java.net.SocketTimeoutException;
 
 public class GuessMyNumberServer {
 
 	public static final int SERVER_PORT = 4950;
 	public static final int MAXBUF = 1024;
 
 	static boolean running = true;
 	static boolean handshake = false;
 	static boolean playing = false;
	static int LOWEST  = 0;
	static int HIGHEST = 100;
 	
 	/**
	 * Handles connections and state of the server
 	 */
 	public static void main(String[] args) {
 		while (true) {
 			Session client = new Session();
 
 			while (client.isConnected() == false) {
 				try {
 					client.handshake();
				} catch (SocketTimeoutException ste) {
 					client.close();
 					System.out.printf(".");
 				} catch (IOException e) {
 					e.printStackTrace();
 					System.exit(-1);
 				}
 			}
 			if (client.isConnected()) {
 				try {
 					client.playGame();
 				} catch (SocketTimeoutException ste) {
 					client.close();
 					System.out.printf(".");
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 			client = null;
 		}
 	}
 }
