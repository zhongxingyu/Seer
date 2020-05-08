 package elfville.server;
 
import java.io.IOException;
 import java.net.*;
 import java.util.Date;
 import java.util.Random;
 import java.util.Scanner;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 public class Server {
 
 	/**
 	 * Starts a server. Use the first argument to provide a port, the second,
 	 * the path to the database.
 	 * 
 	 * @param args
 	 * @throws Exception
 	 */
 	public static void main(String[] args) throws Exception {
 		int port = 8444; 
 		if (args.length < 1) {
 			System.out.println("port not specified. Use 8444 as default");
 		} else {
 			port = Integer.parseInt(args[0]);
 			System.out.println("Using port " + port + " as default");
 		}
 		
 		ServerSocket serverSocket = null;
 		boolean listening = true;
 
 		try {
 			serverSocket = new ServerSocket(port);
 		} catch (IOException e) {
 			System.err.println("Could not listen on port: " + port);
 			System.exit(-1);
 		}
 
 		// Initialize database
 		Database.load();
 
 		Scanner scanner = new Scanner(System.in);
         System.out.println("Input private encryption key file path\n (type 'resources/elfville.der' for demonstration,\n of course you can load one from your flash drive\n that you are inserting right now): ");
         String dbPrivateKeyPath = scanner.nextLine();
 
 		// Initialize private key
 		PKcipher.instance = new PKcipher(dbPrivateKeyPath);
 
 		ExecutorService pool = Executors.newCachedThreadPool();
 
 		System.out.println("Server now listening...");
 
 		// Support Multiple Clients
 		while (listening) {
 			try {
 				pool.execute(new Session(serverSocket.accept()));
 			} catch (IOException e) { // couldn't set timeout? drop them.
 				continue;
 			}
 		}
 
 		serverSocket.close();
 	}
 }
