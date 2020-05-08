 package client;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.net.ServerSocket;
 import java.net.Socket;
 
 import common.Status;
 import java.util.HashMap;
 
 import server.Server;
 
 public class Client_Driver {
 	private static User currentUser;
 	private static GUI g;
 	//public static Comm serverComm;
 	
 	public static HashMap<String, Comm> comms;
 	
 	public static void createUser(String userName ){
		comms = new HashMap<String, Comm>();
 		currentUser = new User(userName );
 		// Update Server with user name so there are no duplicates
 	}
 	
 	public static User getCurrentUser(){
 		return currentUser;
 	}
 	
 /*	public static Comm getComm(){
 		return serverComm;
 	}*/
 	
 	public static void updateBuddyStatus( String buddyName, Status buddyStatus ){
 		g.refreshBuddy(buddyName, buddyStatus == Status.offline ? false : true );
 	}
 	
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		FileInputStream f_in;
 		ObjectInputStream obj_in;
 		Object obj = null;
 		
 		// Determines if there is already a user to use
 		boolean exists = false;
 		try {
 			if( new File( "user.ser" ).exists() ){
 				f_in = new FileInputStream("user.ser");
 				obj_in = new ObjectInputStream(f_in);
 				obj = obj_in.readObject();
 				currentUser = (User) obj;
 				exists = true;
 			}
 			System.out.println ("about to create some comm" );
 			System.out.println ("created some comm" );
 		}catch (Exception e) {
 			System.err.println( e.getMessage() );
 		}
 		
 		g = new GUI( exists );
 		
 		try{
 			Comm.initComm();
 		} catch (NoInternetException e) {
 			System.err.println( e.getMessage() );
 		}
 		
 		
 		ServerSocket commListened = null;
 		try {
 			commListened = new ServerSocket( Comm.COMM_PORT );
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		while( true ){
 			try {
 				Socket connection = commListened.accept();
 	    		Comm c = new Comm( connection );
 	    		IMPacket received = c.receiveIMPacket();
 	    		comms.put( received.getSrcUsername(), c );
 	    		c.start();
 			}catch( NoInternetException nie ){
 				System.err.println( nie.getMessage() );
 			}catch( IOException e ){
 				e.printStackTrace();
 			}
 		}
 		//TODO close the socket somehow
 		//commListened.close();
 		
 
 
 
 	}
 }
