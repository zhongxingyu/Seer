 /**
  * File: WorkerThread.java
  * @author: Tucker Trainor <tmt33@pitt.edu>
  *
  * Based on original code "EchoThread.java" by Adam J. Lee (adamlee@cs.pitt.edu) 
  *
  * A simple server thread. This class just echoes the messages sent
  * over the socket until the socket is closed.
  */
 
 import java.lang.Thread;
 import java.net.Socket;
 import java.net.ConnectException;
 import java.io.*;
 import java.util.*;
 
 /* TODO: Follow flow of transaction, remove unnecessary code, do code for global check */
 
 public class WorkerThread extends Thread {
     private final Socket socket; // The socket that we'll be talking over
 	private CloudServer my_tm; // The Transaction Manager that called the thread
 	private SocketList sockList = new SocketList();
 	private ArrayList<QueryRecord> queryLog = new ArrayList<QueryRecord>();
 	private int transactionPolicyVersion = 0;
 	private int totalSleepTime = 0; // used if my_tm.threadSleep == false
 	private Random generator;
 	private ArrayList<Integer> versions = new ArrayList<Integer>();
 
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
 
 		PrintStream printStreamOriginal = System.out;
 		if (!my_tm.verbose) {
 			System.setOut(new PrintStream(new OutputStream() {
 				public void close() {}
 				public void flush() {}
 				public void write(byte[] b) {}
 				public void write(byte[] b, int off, int len) {}
 				public void write(int b) {}
 			}));
 		}
 		
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
 				else if (msg.theMessage.indexOf("POLICYUPDATE") != -1) { // Policy update from Policy Server
 					String msgSplit[] = msg.theMessage.split(" ");
 					int update = Integer.parseInt(msgSplit[1]);
 					// Check that we aren't going backwards in a race condition
 					if (my_tm.getPolicy() < update) {
 						my_tm.setPolicy(update);
 						System.out.println("Server Policy Version updated to v." + update);
 					}
 					latencySleep(); // Simulate latency
 					output.writeObject(new Message(msgText)); // send ACK
 					break;
 				}
 				else if (msg.theMessage.indexOf("PARAMETERS") != -1) { // Configuration change
 					// PARAMETERS <LMIN> <LMAX> <SLEEP> <VM> <ICSR> <LASR>
 					String msgSplit[] = msg.theMessage.split(" ");
 					my_tm.latencyMin = Integer.parseInt(msgSplit[1]);
 					my_tm.latencyMax = Integer.parseInt(msgSplit[2]);
 					my_tm.threadSleep = Boolean.parseBoolean(msgSplit[3]);
 					my_tm.validationMode = Integer.parseInt(msgSplit[4]);
 					my_tm.integrityCheckSuccessRate = Float.parseFloat(msgSplit[5]);
 					my_tm.localAuthSuccessRate = Float.parseFloat(msgSplit[6]);
 					System.out.println("Server parameters updated:");
 					System.out.println(msg.theMessage);
 					// No artificial latency needed, send ACK
 					output.writeObject(new Message(msgText));
 					break;
 				}
 				
 				// Separate queries
 				String queryGroup[] = msg.theMessage.split(",");
 				for (int i = 0; i < queryGroup.length; i++) {
 					// Handle instructions
 					String query[] = queryGroup[i].split(" ");
 					if (query[0].equals("R")) { // READ
 						// Check server number, perform query or pass on
 						if (Integer.parseInt(query[2]) == my_tm.serverNumber) { // Perform query on this server
 							// Check that if a fresh Policy version is needed
 							// (e.g. if this query has been passed in) it is set
 							if (transactionPolicyVersion == 0) {
 								transactionPolicyVersion = my_tm.getPolicy();
 								System.out.println("Transaction " + query[1] +
 												   " Policy version set: " +
 												   transactionPolicyVersion);
 							}
 							
 							// Check transaction policy against server policy
 							if (checkLocalAuth() == false) {
 								msgText = "ABORT LOCAL_POLICY_FAIL";
 								System.out.println("ABORT LOCAL_POLICY_FAIL: " +
 												   "READ for transaction " + query[1] +
 												   " sequence " + query[3]);
 							}
 							// Check data access for error
 							else if (checkDataAccess() == false) {
 								msgText = "ABORT DATA_ACCESS_FAIL";
 								System.out.println("ABORT DATA_ACCESS_FAIL: " +
 												   "READ for transaction " + query[1] +
 												   " sequence " + query[3]);
 							}
 							else { // OK to read
 								System.out.println("READ for transaction " + query[1] +
 												   " sequence " + query[3]);
 								databaseRead();
 								// Add policy version for passed query logging
 								msgText += " " + transactionPolicyVersion;
 								// Add to query log
 								if (addToQueryLog(query, transactionPolicyVersion)) {
 									System.out.println("Transaction " + query[1] +
 													   " sequence " + query[3] +
 													   " query logged.");
 								}
 								else {
 									System.out.println("Error logging query.");
 								}
 							}
 						}
 						else { // Pass to server
 							System.out.println("Pass READ of transaction " + query[1] +
 											   " sequence " + query[3] +
 											   " to server " + query[2]);
 							msgText = passQuery(Integer.parseInt(query[2]), queryGroup[i]);
 							System.out.println("Response to READ of transaction " + query[1] +
 											   " sequence " + query[3] +
 											   " to server " + query[2] +
 											   ": " + msgText);
 						}
 					}
 					else if (query[0].equals("W")) { // WRITE
 						// Check server number, perform query or pass on
 						if (Integer.parseInt(query[2]) == my_tm.serverNumber) { // Perform query on this server
 							// Check that if a fresh Policy version is needed, it is gotten
 							if (transactionPolicyVersion == 0) {
 								transactionPolicyVersion = my_tm.getPolicy();
 								System.out.println("Transaction " + query[1] +
 												   " Policy version set: " +
 												   transactionPolicyVersion);
 								// Note: If policy has not been set, this server
 								// is the coordinator.
 							}
 
 							// Check transaction policy against server policy
 							if (checkLocalAuth() == false) {
 								msgText = "ABORT LOCAL_POLICY_FAIL";
 								System.out.println("ABORT LOCAL_POLICY_FAIL: " +
 												   "WRITE for transaction " + query[1] +
 												   " sequence " + query[3]);
 							}
 							// Check data access for error
 							else if (checkDataAccess() == false) {
 								msgText = "ABORT DATA_ACCESS_FAIL";
 								System.out.println("ABORT DATA_ACCESS_FAIL: " +
 												   "WRITE for transaction " + query[1] +
 												   " sequence " + query[3]);
 							}
 							else { // OK to write
 								System.out.println("WRITE for transaction " + query[1] +
 												   " sequence " + query[3]);
 								databaseWrite();
 								// Add policy version for passed query logging
 								msgText += " " + transactionPolicyVersion;
 								// Add to query log
 								if (addToQueryLog(query, transactionPolicyVersion)) {
 									System.out.println("Transaction " + query[1] +
 													   " sequence " + query[3] +
 													   " query logged.");
 								}
 								else {
 									System.out.println("Error logging query.");
 								}
 								// Tell RobotThread to add this server to its commitStack
 								// server is query[2], transaction is query[1], sequence is query[3]
 								msgText = "ACS " + query[2] + " " + query[1] + " " + query[3] + " " + transactionPolicyVersion;
 							}
 						}
 						else { // Pass to server
 							System.out.println("Pass WRITE of transaction " + query[1] +
 											   " sequence " + query[3] +
 											   " to server " + query[2]);
 							msgText = passQuery(Integer.parseInt(query[2]), queryGroup[i]);
 							System.out.println("Response to WRITE of transaction " + query[1] +
 											   " sequence " + query[3] +
 											   " to server " + query[2] +
 											   ": " + msgText);
 						}
 					}
 					else if (query[0].equals("COORDPOLICY")) { // Policy update from coordinator
 						transactionPolicyVersion = Integer.parseInt(query[1]);
 						System.out.println("Transaction Policy Version updated to v." + transactionPolicyVersion);
 						latencySleep(); // Simulate latency and return ACK
 					}
 					else if (query[0].equals("A")) { // Re-authorize a query
 						// Query example: "A <global policy version>"
 						// Retrieve policy version: Integer.parseInt(query[1])
 						if (checkLocalAuth()) {
 							msgText = "GLOBALPASS";
 						}
 						else {
 							msgText = "GLOBALFAIL";
 						}
 					}
 					else if (query[0].equals("RUNAUTHS")) { // Run authorizations on all queries
 						System.out.println("Running authorizations on queries using policy version " +
 										   Integer.parseInt(query[1]));
 						msgText = "TRUE";
 						for (int j = 0; j < queryLog.size(); j++) {
							System.out.print("Authorization " + queryLog.get(j).getQueryType() +
											 " for sequence " + queryLog.get(j).getSequence());
 							if (!checkLocalAuth()) {
								System.out.println(": FAIL");
 								msgText = "FALSE";
 								break;
 							}
 							else {
								System.out.println(": PASS");
 							}
 						}
 					}
 					else if (query[0].equals("PTC")) { // Prepare-to-Commit
 						if (my_tm.validationMode >= 0 || my_tm.validationMode <= 2) {
 							msgText = prepareToCommit(0); // No global version
 						}
 						else { // Uses a global version, pass to method
 							msgText = prepareToCommit(Integer.parseInt(query[1]));
 						}
 					}
 					else if (query[0].equals("C")) { // COMMIT
 						System.out.println("COMMIT phase - transaction " + query[1]);
 						msgText = coordinatorCommit();
 						System.out.println("Status of 2PC/2PV of transaction " + query[1] +
 										   ": " + msgText);
 					}
 					else if (query[0].equals("S")) { // Sleep for debugging
 						Thread.sleep(Integer.parseInt(query[1]));
 					}
 					else if (query[0].toUpperCase().equals("EXIT")) { // end of transaction
 						// send exit flag to RobotThread
 						msgText = "FIN";
 						if (!my_tm.threadSleep) { // append total sleep time to message
 							msgText += " " + totalSleepTime;
 						}
 					}
 				}
 				latencySleep(); // Simulate latency to RobotThread
 				// ACK completion of this query group to RobotThread
 				output.writeObject(new Message(msgText));
 			}
 			// Close any SocketGroup connection
 			if (sockList.size() > 0) {
 				int serverNum;
 				for (Enumeration<Integer> socketList = sockList.keys(); socketList.hasMoreElements();) {
 					msg = new Message("DONE");
 					serverNum = socketList.nextElement();
 					latencySleep(); // Simulate latency
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
 		System.out.flush();
 		System.setOut(printStreamOriginal);
 	}
 
 	/**
 	 * Passes a query to other specified server
 	 *
 	 * @param otherServer - The number of the server to pass to
 	 * @param query - The query that must be performed on another server
 	 *
 	 * @return String - the ACK/ABORT from the other server
 	 */
 	public String passQuery(int otherServer, String query) {
 		String server = my_tm.serverList.get(otherServer).getAddress();
 		int port = my_tm.serverList.get(otherServer).getPort();
 		Message msg = null;
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
 				// Send new server the transaction's beginning policy version
 				msg = new Message("COORDPOLICY " + transactionPolicyVersion);
 				latencySleep(); // Simulate latency to other server
 				sockList.get(otherServer).output.writeObject(msg);
 				msg = (Message)sockList.get(otherServer).input.readObject();
 				System.out.println("Server " + otherServer +
 								   " says: " + msg.theMessage +
 								   " for updating policy version.");
 				if (!msg.theMessage.equals("ACK")) {
 					System.err.println("Unsuccessful transfer of policy version to server " + otherServer);
 					return "FAIL";
 				}
 			}
 
 			// Send query
 			msg = new Message(query);
 			latencySleep(); // Simulate latency to other server
 			sockList.get(otherServer).output.writeObject(msg);
 			msg = (Message)sockList.get(otherServer).input.readObject();
 			System.out.println("Server " + otherServer +
 							   " says: " + msg.theMessage +
 							   " for passed query " + query);
 			// else it is an ABORT, no need to log, will be handled by RobotThread
 			return msg.theMessage;
 		}
 		catch (ConnectException ce) {
 			System.err.println(ce.getMessage() +
 							   ": Check server address and port number.");
 			ce.printStackTrace(System.err);
 		}
 		catch (Exception e) {
 			System.err.println("Error during passQuery(): " + e.getMessage());
 			e.printStackTrace(System.err);
 		}
 		return "FAIL";
 	}
 	
 	public boolean addToQueryLog(String query[], int policyVersion) {
 		try {
 			QueryRecord item = new QueryRecord(query[0],
 											   Integer.parseInt(query[1]),
 											   Integer.parseInt(query[2]),
 											   Integer.parseInt(query[3]),
 											   policyVersion);
 			queryLog.add(item);
 			return true;
 		}
 		catch (Exception e) {
 			System.err.println("Error: " + e.getMessage());
 			e.printStackTrace(System.err);
 		}
 		return false;
 	}
 	
 	/**
 	 * When the coordinator receives a request to COMMIT, it directs the flow
 	 * of the transaction to either a view consistency check or a global
 	 * consistency check.
 	 *
 	 * @return String - the result of the 2PV check, either COMMIT or ABORT
 	 */
 	public String coordinatorCommit() {
 		String commitStatus = "COMMIT";
 		
 		// Call each participating server with a PTC message		
 		if (my_tm.validationMode >= 0 && my_tm.validationMode <= 2) {
 			// View consistency checks
 			commitStatus = viewConsistencyCheck();
 		}
 		else if (my_tm.validationMode == 3 || my_tm.validationMode == 4) {
 			// Global consistency checks
 			commitStatus = globalConsistencyCheck();
 		}
 		else {
 			commitStatus = "ABORT UNKNOWN_MODE";
 		}
 		
 		return commitStatus;
 	}
 	
 	/**
 	 * The prepare-to-commit method that is invoked when participating servers
 	 * received the PTC call from the coordinator
 	 *
 	 * @param globalVersion - used for global consistency check
 	 * @return boolean
 	 */
 	public String prepareToCommit(int globalVersion) {
 		// Receive PTC message, handle options
 		if (my_tm.validationMode == 0) { // 2PC only
 			// Return integrity status
 			if (integrityCheck()) {
 				return "YES";
 			}
 			else {
 				return "NO";
 			}
 		}
 		else if (my_tm.validationMode == 1 || my_tm.validationMode == 2) {
 			// 1. Rec'v PTC, request for policy version
 			//    Return integrity status (YES/NO), Policy version
 			if (integrityCheck()) {
 				return "YES " + transactionPolicyVersion;
 			}
 			else {
 				return "NO";
 			}
 		}
 		else if (my_tm.validationMode == 3) {
 			// Check global master policy version against transaction version
 			if (globalVersion == transactionPolicyVersion) {
 				// Perform integrity check
 				if (integrityCheck()) {
 					// Run local authorizations
 					System.out.println("Running authorizations on queries using policy version " +
 									   transactionPolicyVersion);
 					for (int j = 0; j < queryLog.size(); j++) {
 						System.out.print("Authorization " + queryLog.get(j).getQueryType() +
 										 " for sequence " + queryLog.get(j).getSequence());
 						if (!checkLocalAuth()) {
 							System.out.println(": FAIL");
 							return "YES FALSE"; // (authorization failed)
 						}
 						else {
 							System.out.println(": PASS");
 						}
 					}
 					return "YES TRUE"; // (integrity and authorizations pass)
 				}
 				else {
 					return "NO FALSE"; // (integrity fail)
 				}
 			}
 			else {
 				return "YES FALSE"; // (policy inequality)
 			}
 		}
 		else { // (my_tm.validationMode == 4)
 			// Check global master policy version against transaction version
 			if (globalVersion != transactionPolicyVersion) {
 				// Have server get global version from the policy server
 				int calledGlobal = my_tm.callPolicyServer();
 				// Check version for possible race condition
 				if (calledGlobal > globalVersion) {
 					calledGlobal = globalVersion;
 				}
 				// Perform integrity check
 				if (integrityCheck()) {
 					// Run local authorizations
 					System.out.println("Running authorizations on queries using policy version " +
 									   calledGlobal);
 					for (int j = 0; j < queryLog.size(); j++) {
 						System.out.print("Authorization " + queryLog.get(j).getQueryType() +
 										 " for sequence " + queryLog.get(j).getSequence());
 						if (!checkLocalAuth()) {
 							System.out.println(": FAIL");
 							return "YES FALSE"; // (authorization failed)
 						}
 						else {
 							System.out.println(": PASS");
 						}
 					}
 					return "YES TRUE"; // (integrity and authorizations pass)
 				}
 				else {
 					return "NO FALSE"; // (integrity fail)
 				}
 			}			
 			else { // (globalVersion == transactionPolicyVersion) 
 				// Perform integrity check
 				if (integrityCheck()) {
 					// Run local authorizations
 					System.out.println("Running authorizations on queries using policy version " +
 									   transactionPolicyVersion);
 					for (int j = 0; j < queryLog.size(); j++) {
 						System.out.print("Authorization " + queryLog.get(j).getQueryType() +
 										 " for sequence " + queryLog.get(j).getSequence());
 						if (!checkLocalAuth()) {
 							System.out.println(": FAIL");
 							return "YES FALSE"; // (authorization failed)
 						}
 						else {
 							System.out.println(": PASS");
 						}
 					}
 					return "YES TRUE"; // (integrity and authorizations pass)
 				}
 				else {
 					return "NO FALSE"; // (integrity fail)
 				}
 			}
 		}
 	}
 
 	/**
 	 * Handles the 2PV view consistency check. Calls each participant with the
 	 * PTC command, receives back their policy versions, and determines whether
 	 * or not to run proofs of authorization.
 	 *
 	 * @return String - COMMIT or ABORT under view consistency
 	 */
 	public String viewConsistencyCheck() {
 		String status = "COMMIT";
 		Message msg = null;
 		
 		// Call all participants, send PTC and gather policy versions
 		if (sockList.size() > 0) {
 			int serverNum;
 			for (Enumeration<Integer> socketList = sockList.keys(); socketList.hasMoreElements();) {
 				serverNum = socketList.nextElement();
 				if (serverNum != 0) { // Don't call the Policy server
 					try {
 						msg = new Message("PTC");
 						latencySleep(); // Simulate latency
 						// Send
 						sockList.get(serverNum).output.writeObject(msg);
 						// Rec'v
 						msg = (Message)sockList.get(serverNum).input.readObject();
 						// Check response, add policy version to ArrayList
 						if (msg.theMessage.indexOf("YES") != -1) {
 							if (my_tm.validationMode != 0) { // Not 2PC only
 								String msgSplit[] = msg.theMessage.split(" ");
 								versions.add(Integer.parseInt(msgSplit[1]));
 							}
 						}
 						else { // ABORT - someone responded with a NO
 							return "ABORT PTC_RESPONSE_NO";
 						}
 					}
 					catch (Exception e) {
 						System.err.println("Policy Check Error: " + e.getMessage());
 						e.printStackTrace(System.err);
 					}
 				}
 			}
 		}
 		
 		// If 2PC only, no need to check policies or run auths
 		if (my_tm.validationMode == 0) {
 			return status;
 		}
 		
 		// Turn ArrayList into an array of ints, sort and compare versions
 		Integer versionArray[] = new Integer[versions.size()];
 		versionArray = versions.toArray(versionArray);
 		// Sort array, compare first value with last
 		Arrays.sort(versionArray);
 		if (versionArray[0] == versionArray[versionArray.length - 1]) {
 			// Policy versions match across servers - run authorizations
 			
 			status = runAuths((int)versionArray[0]);
 		}
 		else { // Handle inequality
 			if (my_tm.validationMode == 1) { // ABORT
 				status = "ABORT VIEW_CONSISTENCY_FAIL";
 			}
 			else { // Find common policy and run authorizations with it
 				// For simplicity, use minimum of versions as common policy
 				status = runAuths((int)versionArray[0]);
 			}
 		}
 		
 		return status;
 	}
 
 	/**
 	 * Handles the 2PV global consistency check. Sends each participant the PTC
 	 * command and the global master policy version. If all participants are
 	 * using the global version, then authorizations can be performed. Otherwise
 	 * a decision is made whether to allow calls to the policy server to refresh
 	 * or to ABORT.
 	 *
 	 * @return String - COMMIT or ABORT
 	 */
 	public String globalConsistencyCheck() {
 		String status = "COMMIT";
 		Message msg = null;
 		
 		// Have coordinator's server call the policy server and retrieve the
 		// current global master policy version
 		int globalVersion = my_tm.callPolicyServer();		
 		// Call all participants, send PTC and global version
 		if (sockList.size() > 0) {
 			int serverNum;
 			for (Enumeration<Integer> socketList = sockList.keys(); socketList.hasMoreElements();) {
 				serverNum = socketList.nextElement();
 				if (serverNum != 0) { // Don't call the Policy server
 					try {
 						msg = new Message("PTC " + globalVersion);
 						latencySleep(); // Simulate latency
 						// Send
 						sockList.get(serverNum).output.writeObject(msg);
 						// Rec'v
 						msg = (Message)sockList.get(serverNum).input.readObject();
 						
 						// mode 3: if all participants are using global, they
 						// run auths and return YES/NO, TRUE/FALSE
 						// if any are not using global, ABORT
 						
 						// mode 4: if any not using global, they call policy
 						// server and get global, run auths, return Y/N, T/F
 						// if auths fail, ABORT
 						
 						// Check response
 						if (msg.theMessage.indexOf("NO") != -1) { // Someone responded NO
 							return "ABORT PTC_RESPONSE_NO";
 						}
 						else if (msg.theMessage.indexOf("FALSE") != -1) { // Someone responded FALSE
 							return "ABORT PTC_RESPONSE_FALSE";
 						}
 					}
 					catch (Exception e) {
 						System.err.println("Global Consistency Check Error: " + e.getMessage());
 						e.printStackTrace(System.err);
 					}
 				}
 			}
 		}
 
 		return status;
 	}
 	
 	/**
 	 * Method to run authorizations on each query performed on all participating
 	 * servers, including the coordinator.
 	 *
 	 * @return String - COMMIT or ABORT
 	 */
 	public String runAuths(int version) {
 		// Check local auths on coordinator
 		System.out.println("Running authorizations on queries using policy version " +
 						   version);
 		for (int j = 0; j < queryLog.size(); j++) {
 			System.out.print("Authorization " + queryLog.get(j).getQueryType() +
 							 " for sequence " + queryLog.get(j).getSequence());
 			if (!checkLocalAuth()) {
 				System.out.println(": FAIL");
 				return "ABORT LOCAL_AUTHORIZATION_FAIL";
 			}
 			else {
 				System.out.println(": PASS");
 			}
 		}
 		
 		// Contact all other participants, have them run authorizations and return results
 		if (sockList.size() > 0) {
 			Message msg = null;
 			int serverNum;
 			
 			for (Enumeration<Integer> socketList = sockList.keys(); socketList.hasMoreElements();) {
 				serverNum = socketList.nextElement();
 				if (serverNum != 0) { // Don't call the Policy server
 					try {
 						msg = new Message("RUNAUTHS " + version);
 						latencySleep(); // Simulate latency
 						// Send
 						sockList.get(serverNum).output.writeObject(msg);
 						// Rec'v
 						msg = (Message)sockList.get(serverNum).input.readObject();
 						// Check response, add policy version to ArrayList
 						if (msg.theMessage.equals("FALSE")) {
 							return "ABORT LOCAL_AUTHORIZATION_FAIL";
 						}
 					}
 					catch (Exception e) {
 						System.err.println("runAuths() error: " + e.getMessage());
 						e.printStackTrace(System.err);
 					}
 				}
 			}
 		}
 		
 		return "COMMIT";
 	}
 	
 	public void databaseRead() {
 		if (my_tm.threadSleep) {
 			try {
 				// sleep for a random period of time between 75ms and 125ms
 				Thread.sleep(75 + generator.nextInt(50));
 			}
 			catch(Exception e) {
 				System.err.println("databaseRead() Sleep Error: " + e.getMessage());
 				e.printStackTrace(System.err);
 			}
 		}
 		else {
 			totalSleepTime += 75 + generator.nextInt(50);
 		}
 	}
 	
 	public void databaseWrite() {
 		if (my_tm.threadSleep) {
 			try {
 				// sleep for a random period of time between 150ms and 225ms
 				Thread.sleep(150 + generator.nextInt(75));
 			}
 			catch(Exception e) {
 				System.err.println("databaseWrite() Sleep Error: " + e.getMessage());
 				e.printStackTrace(System.err);
 			}
 		}
 		else {
 			totalSleepTime += 150 + generator.nextInt(75);
 		}
 	}
 	
 	/**
 	 * Checks if accessing requested data was successful
 	 *
 	 * @return boolean - true if access was successful, else false
 	 */
 	public boolean checkDataAccess() {
 		/* STUB */
 		// perform random success operation
 		return true;
 	}
 	
 	/**
 	 * Checks the local policy for authorization to data
 	 *
 	 * @return boolean - true if authorization check comes back OK, else false
 	 */
 	public boolean checkLocalAuth() {
 		if (my_tm.threadSleep) {
 			try {
 				// sleep for a random period of time between 50ms and 150ms
 				Thread.sleep(50 + generator.nextInt(100));
 			}
 			catch(Exception e) {
 				System.err.println("checkLocalAuth() Sleep Error: " + e.getMessage());
 				e.printStackTrace(System.err);
 			}
 		}
 		else {
 			totalSleepTime += 50 + generator.nextInt(100);
 		}
 		// Perform random success operation
 		if (my_tm.localAuthSuccessRate < 1.0) {
 			return coinToss(my_tm.localAuthSuccessRate);
 		}
 		else {
 			return true;
 		}
 	}
 	
 	/**
 	 * Checks the integrity of the data for the commit (2PC)
 	 *
 	 * @return boolean - true if integrity check comes back OK, else false
 	 */
 	public boolean integrityCheck() {
 		
 		// Sleep for duration of check, between 150ms and 225ms
 		if (my_tm.threadSleep) {
 			try {
 				Thread.sleep(150 + generator.nextInt(75));
 			}
 			catch(Exception e) {
 				System.err.println("verifyIntegrity() Sleep Error: " + e.getMessage());
 				e.printStackTrace(System.err);
 			}
 		}
 		else {
 			totalSleepTime += 150 + generator.nextInt(75);
 		}
 		// Perform random success operation if necessary
 		if (my_tm.integrityCheckSuccessRate < 1.0) {
 			return coinToss(my_tm.integrityCheckSuccessRate);
 		}
 		else {
 			return true;
 		}
 	}
 	
 	public boolean coinToss(float successRate) {
 		if (generator.nextFloat() > successRate) {
 			return false;
 		}
 		return true;
 	}
 	
 	public void latencySleep() {
 		if (my_tm.threadSleep) {
 			try {
 				// Sleep for a random period of time between min ms and max ms
 				Thread.sleep(my_tm.latencyMin + generator.nextInt(my_tm.latencyMax - my_tm.latencyMin));
 			}
 			catch(Exception e) {
 				System.err.println("latencySleep() Error: " + e.getMessage());
 				e.printStackTrace(System.err);
 			}
 		}
 		else {
 			totalSleepTime += my_tm.latencyMin + generator.nextInt(my_tm.latencyMax - my_tm.latencyMin);
 		}
 	}
 	
 	/**
 	 * A class to store sockets between servers in order to reduce the number
 	 * of connections necessary during a transaction.
 	 */
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
