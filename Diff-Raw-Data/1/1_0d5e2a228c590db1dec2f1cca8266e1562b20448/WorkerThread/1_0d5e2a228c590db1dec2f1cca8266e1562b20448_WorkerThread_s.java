 /**
  * File: WorkerThread.java
  * @author: Tucker Trainor <tmt33@pitt.edu>
  *
  * Based on original code "EchoThread.java" by Adam J. Lee (adamlee@cs.pitt.edu) 
  *
  * A simple server thread. This class just echoes the messages sent
  * over the socket until the socket is closed.
  */
 
 /* TODO: Handle new Policy Version pushes - POLICYUPDATE called from a policy thread
  */
 
 import java.lang.Thread;            // We will extend Java's base Thread class
 import java.net.Socket;
 import java.net.ConnectException;
 import java.io.ObjectInputStream;   // For reading Java objects off of the wire
 import java.io.ObjectOutputStream;  // For writing Java objects to the wire
 import java.util.*;
 
 public class WorkerThread extends Thread {
     private final Socket socket; // The socket that we'll be talking over
 	private CloudServer my_tm; // The Transaction Manager that called the thread
 	private final int minSleep = 5; // minimum number of ms for a READ/WRITE
 	private final int maxSleep = 100; // maximum number of ms for a READ/WRITE
 	private SocketList sockList = new SocketList();
 	private int transactionPolicyVersion = 0;
 	private Random generator;
 
 	/**
 	 * Constructor that sets up the socket we'll chat over
 	 *
 	 * @param _socket - The socket passed in from the server
 	 * @param _my_tm - The Transaction Manager that called the thread
 	 */
 	public WorkerThread(Socket _socket, CloudServer _my_tm) {
 		socket = _socket;
 		my_tm = _my_tm;
 	}
 
 	/**
 	 * run() is basically the main method of a thread. This thread
 	 * simply reads Message objects off of the socket.
 	 */
 	public void run() {
 		generator = new Random(new Date().getTime());
 		try {
 			// Print incoming message
 			System.out.println("** New connection from " + socket.getInetAddress() +
 							   ":" + socket.getPort() + " **");
 
 			// Set up I/O streams with the calling thread
 			final ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
 			final ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
 
 			Message msg = null;
 			Message resp = null;
 			while (true) {
 				// Loop to read messages
 				String msgText = "ACK";
 				// Read and print message
 				msg = (Message)input.readObject();
 				System.out.println("[" + socket.getInetAddress() +
 									   ":" + socket.getPort() + "] " + msg.theMessage);
 				
 				if (msg.theMessage.equals("DONE")) {
 					break;
 				}
 				else if (msg.theMessage.indexOf("POLICYUPDATE") != -1) { // Policy update
 					String msgSplit[] = msg.theMessage.split(" ");
 					int update = Integer.parseInt(msgSplit[1]);
 					// Check that we aren't going backwards in a race condition
 					if (my_tm.serverPolicyVersion < update) {
 						my_tm.serverPolicyVersion = update;
 						System.out.println("Server Policy Version updated to v." + update);
 					}
 					output.writeObject(new Message(msgText));
 					break;
 				}
 				
 				// Separate queries
 				String queryGroup[] = msg.theMessage.split(",");
 				for (int i = 0; i < queryGroup.length; i++) {
 					// Handle instructions
 					String query[] = queryGroup[i].split(" ");
 					if (query[0].equals("B")) { // BEGIN
 						System.out.println("BEGIN transaction " + query[1]);
 						// Set the transaction's Policy version
 						transactionPolicyVersion = my_tm.serverPolicyVersion;
 						System.out.println("Policy version set: " + transactionPolicyVersion);
 					}
 					else if (query[0].equals("R")) { // READ
 						// Check server number, perform query or pass on
 						if (Integer.parseInt(query[2]) == my_tm.serverNumber) { // Perform query on this server
 							// Check that if a fresh Policy version is needed
 							// (e.g. if this query has been passed in) it is set
 							if (transactionPolicyVersion == 0) {
 								transactionPolicyVersion = my_tm.serverPolicyVersion;
 							}
 							
 							if (accessData() == false) {
 								// message an error, abort transaction
 							}
 							else {
 								System.out.println("READ for transaction " + query[1]);
 							}
 						}
 						else { // pass to server
 							System.out.println("Pass READ of transaction " + query[1] +
 											   " to server " + query[2]);
 							if (passQuery(Integer.parseInt(query[2]), queryGroup[i])) {
 								System.out.println("READ of transaction " + query[1] +
 												   " to server " + query[2] +
 												   " successful");
 							}
 							else { // error in passQuery()
 								System.out.println("ERROR in passQuery()");
 							}
 						}
 					}
 					else if (query[0].equals("W")) { // WRITE
 						// Check server number, perform query or pass on
 						if (Integer.parseInt(query[2]) == my_tm.serverNumber) { // Perform query on this server
 							// Check that if a fresh Policy version is needed, it is gotten
 							if (transactionPolicyVersion == 0) {
 								transactionPolicyVersion = my_tm.serverPolicyVersion;
 							}
 
 							if (accessData() == false) {
 								// message an error, abort transaction
 							}
 							else {
 								System.out.println("WRITE for transaction " + query[1]);
 								// add time to counter or sleep
 								
 								// tell RobotThread to add this server to its commitStack
 								msgText = "ACS " + query[2];
 							}
 						}
 						else { // pass to server
 							System.out.println("Pass WRITE of transaction " + query[1] +
 											   " to server " + query[2]);
 							if (passQuery(Integer.parseInt(query[2]), queryGroup[i])) {
 								System.out.println("WRITE of transaction " + query[1] +
 												   " to server " + query[2] +
 												   " successful");
 								
 								// tell RobotThread to add this server to its commitStack
 								msgText = "ACS " + query[2];
 							}
 							else { // error in passQuery()
 								System.out.println("ERROR in passQuery()");
 							}
 						}
 					}
 					else if (query[0].equals("POLICY")) { // POLICY
 						// Return Policy version on this server to caller
 						msgText = "VERSION " + Integer.toString(transactionPolicyVersion);
 					}
 					else if (query[0].equals("C")) { // COMMIT
 						System.out.println("COMMIT - transaction " + query[1]);
 						
 						// First, verify policy across servers
 						if (viewPolicyCheck() != 0) { // a server was not fresh
 							System.out.println("View Consistency Policy FAIL - transaction " + query[1]);
 							msgText = "ABORT POLICY_FAIL";
 						}
 						else {
 							System.out.println("View Consistency Policy OK - transaction " + query[1]);
 							// call verifyIntegrity() here?
 						}
 					}
 					else if (query[0].toUpperCase().equals("EXIT")) { // end of transaction
 						// send exit flag to RobotThread
 						msgText = "FIN";
 					}
 				}
 				// ACK completion of this query group
 				output.writeObject(new Message(msgText));
 			}
 			// Close any SocketGroup connection
 			if (sockList.size() > 0) {
 				int serverNum;
 				for (Enumeration<Integer> socketList = sockList.keys(); socketList.hasMoreElements();) {
 					msg = new Message("DONE");
 					serverNum = socketList.nextElement();
 					sockList.get(serverNum).output.writeObject(msg);
 					sockList.get(serverNum).socket.close();
 				}
 			}
 			
 			// Close and cleanup
 			System.out.println("** Closing connection with " + socket.getInetAddress() +
 							   ":" + socket.getPort() + " **");
 			socket.close();
 		}
 		catch(Exception e) {
 			System.err.println("Error: " + e.getMessage());
 			e.printStackTrace(System.err);
 		}
 	}
 
 	/**
 	 * Passes a query to other specified server
 	 *
 	 * @param otherServer - The number of the server to pass to
 	 * @param query - The query that must be performed on another server
 	 *
 	 * @return boolean - true if query was successful, else false
 	 */
 	public boolean passQuery(int otherServer, String query) {
 		String server = my_tm.serverList.get(otherServer).getAddress();
 		int port = my_tm.serverList.get(otherServer).getPort();
 		
 		try {
 			// Check SocketList for an existing socket, else create and add new
 			if (!sockList.hasSocket(otherServer)) {
 				// Create new socket, add it to SocketGroup
 				System.out.println("Connecting to " + server +
 								   " on port " + port);
 				Socket sock = new Socket(server, port);
 				sockList.addSocketObj(otherServer, new SocketObject(sock,
 																	new ObjectOutputStream(sock.getOutputStream()),	
 																	new ObjectInputStream(sock.getInputStream())));
 			}
 
 			Message msg = null, resp = null;
 			
 			// send query
 			msg = new Message(query);
 			sockList.get(otherServer).output.writeObject(msg);
 			resp = (Message)sockList.get(otherServer).input.readObject();
 			System.out.println("Server " + otherServer +
 							   " says: " + resp.theMessage);			
 			return true;
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
 		return false;
 	}
 	
 	/**
 	 * Checks the integrity of the data for the commit
 	 *
 	 * @return boolean - true if integrity check comes back OK, else false
 	 */
 	public boolean verifyIntegrity() {
 		System.out.println("verifyIntegrity() stub");
 		// perform random success operation
 		return true;
 	}
 
 	/**
 	 * Checks if accessing requested data was successful
 	 *
 	 * @return boolean - true if access was successful, else false
 	 */
 	public boolean accessData() {
 		System.out.println("accessData() stub");
 		
 		try {
 			// sleep for a random period of time
 			Thread.sleep(minSleep + generator.nextInt(maxSleep - minSleep));
 		}
 		catch(Exception e) {
 			System.err.println("Error: " + e.getMessage());
 			e.printStackTrace(System.err);
 		}
 		
 		// perform random success operation
 		return true;
 	}
 	
 	/**
 	 * Checks the local policy for authorization to data
 	 *
 	 * @return boolean - true if authorization check comes back OK, else false
 	 */
 	public boolean getLocalAuth() {
 		System.out.println("getLocalAuth() stub");
 		// perform random success operation
 		return true;
 	}
 	
 	/**
 	 * Checks the global policy for authorization to data
 	 *
 	 * @return boolean - true if authorization check comes back OK, else false
 	 */
 	public boolean getPolicyAuth() {
 		System.out.println("getPolicyAuth() stub");
 		// perform random success operation
 		return true;
 	}
 	
 	/**
 	 * Checks all involved servers for Policy version freshness
 	 *
 	 * @return int - 0 if all servers are fresh, 1+ if not
 	 */
 	public int viewPolicyCheck() {
//		int masterPolicyVersion = transactionPolicyVersion; // use main TM's policy for now
 		int masterPolicyVersion = my_tm.serverPolicyVersion; // store freshest policy
 		int stale = 0;
 		Message msg = null;
 		Message resp = null;
 		
 		if (sockList.size() > 0) {
 			int serverNum;
 			for (Enumeration<Integer> socketList = sockList.keys(); socketList.hasMoreElements();) {
 				serverNum = socketList.nextElement();
 				if (serverNum != 0) { // Don't call the Policy server
 					try {
 						msg = new Message("POLICY");
 						sockList.get(serverNum).output.writeObject(msg);
 						resp = (Message)sockList.get(serverNum).input.readObject();
 						// Compare Policy versions
 						String msgSplit[] = resp.theMessage.split(" ");
 						if (msgSplit[0].equals("VERSION") && Integer.parseInt(msgSplit[1]) < masterPolicyVersion) {
 							stale++;
 						}
 					}
 					catch (Exception e) {
 						System.err.println("Policy Check Error: " + e.getMessage());
 						e.printStackTrace(System.err);
 					}
 				}
 			}
 		}
 		return stale;
 	}
 	
 	public class SocketList {
 		private Hashtable<Integer, SocketObject> list = new Hashtable<Integer, SocketObject>();
 		
 		public void addSocketObj(int serverNum, SocketObject so) {
 			list.put(serverNum, so);
 		}
 		
 		public boolean hasSocket(int serverNum) {
 			if (list.containsKey(serverNum)) {
 				return true;
 			}
 			return false;
 		}
 		
 		public SocketObject get(int serverNum) {
 			return list.get(serverNum);
 		}
 		
 		public int size() {
 			return list.size();
 		}
 		
 		public Enumeration<Integer> keys() {
 			return list.keys();
 		}
 		
 	}
 	
 	class SocketObject {
 		public Socket socket;
 		public ObjectOutputStream output;
 		public ObjectInputStream input;
 		
 		public SocketObject(Socket s, ObjectOutputStream oos, ObjectInputStream ois) {
 			socket = s;
 			output = oos;
 			input = ois;
 		}
 	}
 }
