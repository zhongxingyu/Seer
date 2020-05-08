 /**
  * File: Robot.java
  * @author: Tucker Trainor <tmt33@pitt.edu>
  *
  * Based on original code "EchoClient.java" by Adam J. Lee (adamlee@cs.pitt.edu) 
  *
  * Simple client class. This class connects to a CloudServer to send
  * text back and forth. Java message serialization is used to pass
  * Message objects around.
  */
 
 import java.net.Socket;             // Used to connect to the server
 import java.io.ObjectInputStream;   // Used to read objects sent from the server
 import java.io.ObjectOutputStream;  // Used to write objects to the server
 import java.io.BufferedReader;      // Needed to read from the console
 import java.io.InputStreamReader;   // Needed to read from the console
 
 public class Robot {
 	/**
 	 * Main method.
 	 *
	 * @param args  First argument specifies the server to connect to
 	 */
     public static void main(String[] args) {
 		// Error checking for arguments
 		if (args.length != 2) {
 			System.err.println("Not enough arguments.\n");
 			System.err.println("Usage: java Robot <Server name or IP> <Port Number>\n");
 			System.exit(-1);
 	    }
 		try {
 			// Connect to the specified server
 			final Socket sock = new Socket(args[0], Integer.parseInt(args[1]));
 			System.out.println("Connected to " + args[0] +
 							   " on port " + Integer.parseInt(args[1]));
 			
 			// Set up I/O streams with the server
 			final ObjectOutputStream output = new ObjectOutputStream(sock.getOutputStream());
 			final ObjectInputStream input = new ObjectInputStream(sock.getInputStream());
 
 			// loop to send messages
 			Message msg = null, resp = null;
 			do {
 				// Read and send message. Since the Message class
 				// implements the Serializable interface, the
 				// ObjectOutputStream "output" object automatically
 				// encodes the Message object into a format that can
 				// be transmitted over the socket to the server.
 				msg = new Message(readSomeText());
 				output.writeObject(msg);
 
 				// Get ACK and print. Since Message implements
 				// Serializable, the ObjectInputStream can
 				// automatically read this object off of the wire and
 				// encode it as a Message. Note that we need to
 				// explicitly cast the return from readObject() to the
 				// type Message.
 				resp = (Message)input.readObject();
 				System.out.println("\nServer says: " + resp.theMessage + "\n");
 			} while (!msg.theMessage.toUpperCase().equals("EXIT"));
 			
 			// shut things down
 			sock.close();
 		}
 		catch (Exception e) {
 			System.err.println("Error: " + e.getMessage());
 			e.printStackTrace(System.err);
 		}
     }
 
     /**
      * Simple method to print a prompt and read a line of text.
      *
      * @return A line of text read from the console
      */
 	private static String readSomeText() {
 		try {
 			System.out.println("Enter a line of text, or type \"EXIT\" to quit.");
 			System.out.print(" > ");	
 			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
 			return in.readLine();
 		}
 		catch (Exception e) {
 			// Uh oh...
 			return "";
 		}
 	}
 }
