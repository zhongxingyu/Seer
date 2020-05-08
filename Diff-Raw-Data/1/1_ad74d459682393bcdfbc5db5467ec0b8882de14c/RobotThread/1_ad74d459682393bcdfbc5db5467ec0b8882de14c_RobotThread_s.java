 /**
  * File: RobotThread.java
  * @author: Tucker Trainor <tmt33@pitt.edu>
  *
  * Based on original code "EchoThread.java" by Adam J. Lee (adamlee@cs.pitt.edu) 
  *
  * A thread that accepts a series of transactions from the Robot and sends them
  * to a CloudServer
  */
 
 import java.lang.Thread;
 import java.net.Socket;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.ConnectException;
 
 public class RobotThread extends Thread {
     private final String transactions;
 	private final String server;
 	private final int port;
 
 	/**
 	 * Constructor that sets up transaction communication
 	 *
 	 * @param _transactions - A string representing the queries to be run
 	 * @param _server - The server name where the primary Transaction Manager
 	 * is located
 	 * @param _port - The port number of the server
 	 */
 	public RobotThread(String _transactions, String _server, int _port) {
 		transactions = _transactions;
 		server = _server;
 		port = _port;
 	}
 
 	/**
 	 * run() is basically the main method of a thread. This thread
 	 * simply reads Message objects off of the socket.
 	 */
 	public void run() {
 		try {
 			// Connect to the specified server
 			final Socket sock = new Socket(server, port);
 			System.out.println("Connected to " + server +
 							   " on port " + port);
 			
 			// Set up I/O streams with the server
 			final ObjectOutputStream output = new ObjectOutputStream(sock.getOutputStream());
 			final ObjectInputStream input = new ObjectInputStream(sock.getInputStream());
 			
 			// Divide transaction into groups to process in chunks (i.e., all
 			// contiguous READs or WRITEs)
 			String queryGroups[] = transactions.split(";");
 			int groupIndex = 0;
 			
 			// Loop to send query qroups
 			Message msg = null, resp = null;
 			do {
 				// Read and send message
 				msg = new Message(queryGroups[groupIndex]);
 				groupIndex++;
 				output.writeObject(msg);
 				
 				// Get ACK and print
 				resp = (Message)input.readObject();
 				System.out.println("Server says: " + resp.theMessage);
 				resp = (Message)input.readObject();
 				if (resp.theMessage.equals("ACK")) {
 					System.out.println("RobotThread: query group processed");
 				}
 				else if (queryGroups[groupIndex].toUpperCase().equals("EXIT")) {
 					System.out.println("RobotThread: transaction complete");
 				}
 				else { // Something went wrong
 					System.out.println("RobotThread: query handling error");
 				}
 			} while (!msg.theMessage.toUpperCase().equals("EXIT"));
 			
 			// shut things down
 			sock.close();
 		}
 		catch (ConnectException ce) {
 			System.err.println(ce.getMessage() +
 							   ": Check server address and port number.");
 			ce.printStackTrace(System.err);
 		}
 		catch (Exception e) {
 			System.err.println("Error: " + e.getMessage());
 			e.printStackTrace(System.err);
 		}
 
 	}
 }
