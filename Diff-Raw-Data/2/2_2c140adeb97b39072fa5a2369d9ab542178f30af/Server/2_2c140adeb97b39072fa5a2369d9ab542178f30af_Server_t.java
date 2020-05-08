 package cs4520.server;
 
 import java.net.*;
 import java.util.ArrayList;
 import java.io.*;
 
 import javax.net.ssl.*;
 
 
 /**
  * @author Oliver Maskery
  *
  * Object representing the CS4520 server, handling login requests by users before servicing valid logins with secret data.
  */
 public class Server {
 	private ArrayList<Connection> mConnections = new ArrayList<Connection>();	// array of current connections
 	private UserManager mUsers = new UserManager();								// user manager object for storing login credentials
 	private SSLServerSocket mServer;												// server socket for accepting connections
 	
 	/**
 	 * Server constructor
 	 * @param _port The port on which the server should listen
 	 * @throws IOException
 	 */
 	public Server(int _port) throws IOException
 	{
 		// Start the server
 		System.out.print("Starting server for CS4520 coursework...");
		mServer = (SSLServerSocket) SSLServerSocketFactory.getDefault().createServerSocket(_port);
 		System.out.println("done");
 		
 		// for simplicity
 		mUsers.addUser("guest", "meow", User.Level.Guest);
 		mUsers.addUser("rolfharris", "canyoutellwhatitisyet", User.Level.User);
 		mUsers.addUser("admin", "secretsecret", User.Level.Administrator);
 		
 		// Start accepting connections
 		System.out.println("Entering main accept loop...");
 		while(true)
 		{
 			// Accept a client
 			Socket client = mServer.accept();
 			// Wrap it in a Connection object
 			final Connection newConnection = new Connection(client, mUsers);
 			// Register for notification when the Connection is complete, so we can remove it from our list
 			newConnection.addListener(new CompletionListener() {
 				public void onCompletion(Object _sender) {
 					mConnections.remove(newConnection);
 				}
 			});
 			
 			// add the new connection to our list
 			mConnections.add(newConnection);
 			
 			// start asynchronously handling that new connection
 			newConnection.start();
 		}
 	}
 	
 	public static void main(String[] args) {
 		try {
 			// run the server
 			new Server(28000);
 		} catch (IOException e) {
 			System.err.println("Exception starting server:");
 			e.printStackTrace(System.err);
 		}
 	}
 }
