 /**
  * File: IncrementalThread.java
  * @author: Tucker Trainor <tmt33@pitt.edu>
  *
  * A server thread to handle incremental punctual proofs of authorization.
  * Extends the PunctualThread class.
  */
 
 import java.lang.Thread;
 import java.net.Socket;
 import java.net.ConnectException;
 import java.io.*;
 import java.util.*;
 
 public class IncrementalThread extends PunctualThread {
 	
 	/**
 	 * Constructor that sets up the socket we'll chat over
 	 *
 	 * @param _socket - The socket passed in from the server
 	 * @param _my_tm - The Transaction Manager that called the thread
 	 */
 	public IncrementalThread(Socket _socket, CloudServer _my_tm) {
 		super(_socket, _my_tm);
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
 				else if (msg.theMessage.equals("KILL")) {
 					my_tm.shutdownServer();
 					break;
 				}
 				else if (msg.theMessage.indexOf("POLICYUPDATE") != -1) { // Policy update from Policy Server
 					String msgSplit[] = msg.theMessage.split(" ");
 					int update = Integer.parseInt(msgSplit[1]);
 					// Check that we aren't going backwards in a race condition
 					if (my_tm.getPolicy() < update) {
 						my_tm.setPolicy(update);
 					}
 					latencySleep(); // Simulate latency
 					output.writeObject(new Message(msgText)); // send ACK
 					break;
 				}
 				else if (msg.theMessage.indexOf("PARAMETERS") != -1) { // Configuration change
 					// PARAMETERS <PROOF> <VM> <PUSH>
 					String msgSplit[] = msg.theMessage.split(" ");
 					my_tm.proof = msgSplit[1];
 					my_tm.validationMode = Integer.parseInt(msgSplit[2]);
 					my_tm.policyPush = Integer.parseInt(msgSplit[3]);
 					System.out.println("Server parameters updated: " + msg.theMessage);
 					System.out.println("Proof: " + my_tm.proof);
 					System.out.println("Validation mode: " + my_tm.validationMode);
 					System.out.println("Policy push mode: " + my_tm.policyPush);
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
 								if (my_tm.validationMode >= 0 && my_tm.validationMode <= 2) {
 									transactionPolicyVersion = my_tm.getPolicy();
 								}
 								else { // Get and set freshest global policy
 									my_tm.setPolicy(my_tm.callPolicyServer());
 									transactionPolicyVersion = my_tm.getPolicy();									
 								}
 								System.out.println("Transaction " + query[1] +
 												   " Policy version set: " +
 												   transactionPolicyVersion);
 							}
 							
 							// Check transaction policy against server policy
 							if (checkLocalAuth() == false) {
 								msgText = "ABORT LOCAL_POLICY_FAIL";
 								System.out.println("ABORT LOCAL_POLICY_FAIL: " +
 												   "READ for txn " + query[1] +
 												   " sequence " + query[3]);
 							}
 							else { // OK to read
 								System.out.println("READ for txn " + query[1] +
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
 							int passServer = Integer.parseInt(query[2]);
 							int txnNum = Integer.parseInt(query[1]);
 							// Check if server has participated yet
 							if (!sockList.hasSocket(passServer)) {
 								if (!join(passServer, txnNum)) {
 									msgText = "FAIL during join(" + passServer +
 									") for txn " + txnNum; 
 								}
 								else {
 									// The other server has joined, so now run
 									// a transaction consistency check
 									if (checkTxnConsistency() == false) {
 										msgText = "ABORT TXN_CONSISTENCY_FAIL";
 										System.out.println("ABORT TXN_CONSISTENCY_FAIL: " +
 														   "READ for txn " + query[1] +
 														   " sequence " + query[3]);
 									}
 									else {
 										// Consistency confirmed, pass operation
 										System.out.println("*** Txn consistency validated for sequence " + query[3]);
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
 							}
 							else {
 								// Server has previously joined, pass operation
 								msgText = passQuery(Integer.parseInt(query[2]), queryGroup[i]);
 								System.out.println("Response to READ of transaction " + query[1] +
 												   " sequence " + query[3] +
 												   " to server " + query[2] +
 												   ": " + msgText);
 							}
 						}
 					}
 					else if (query[0].equals("W")) { // WRITE
 						// Check server number, perform query or pass on
 						if (Integer.parseInt(query[2]) == my_tm.serverNumber) { // Perform query on this server
 							// Check that if a fresh Policy version is needed
 							// (e.g. if this query has been passed in) it is set
 							if (transactionPolicyVersion == 0) {
 								if (my_tm.validationMode >= 0 && my_tm.validationMode <= 2) {
 									transactionPolicyVersion = my_tm.getPolicy();
 								}
 								else { // Get and set freshest global policy
 									my_tm.setPolicy(my_tm.callPolicyServer());
 									transactionPolicyVersion = my_tm.getPolicy();									
 								}
 								System.out.println("Transaction " + query[1] +
 												   " Policy version set: " +
 												   transactionPolicyVersion);
 							}
 							
 							// Check transaction policy against server policy
 							if (checkLocalAuth() == false) {
 								msgText = "ABORT LOCAL_POLICY_FAIL";
 								System.out.println("ABORT LOCAL_POLICY_FAIL: " +
 												   "WRITE for txn " + query[1] +
 												   " sequence " + query[3]);
 							}
 							else { // OK to write
 								System.out.println("WRITE for txn " + query[1] +
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
 							}
 						}
 						else { // Pass to server
 							int passServer = Integer.parseInt(query[2]);
 							int txnNum = Integer.parseInt(query[1]);
 							// Check if server has participated yet
 							if (!sockList.hasSocket(passServer)) {
 								if (!join(passServer, txnNum)) {
 									msgText = "FAIL during join(" + passServer +
 											  ") for txn " + txnNum; 
 								}
 								else {
 									// The other server has joined, so now run
 									// a transaction consistency check
 									if (checkTxnConsistency() == false) {
 										msgText = "ABORT TXN_CONSISTENCY_FAIL";
 										System.out.println("ABORT TXN_CONSISTENCY_FAIL: " +
 														   "WRITE for txn " + query[1] +
 														   " sequence " + query[3]);
 									}
 									else {
 										// Consistency confirmed, pass operation
 										System.out.println("*** Txn consistency validated for sequence " + query[3]);
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
 							}
 							// Server has previously joined, pass operation
 							else {
 								msgText = passQuery(Integer.parseInt(query[2]), queryGroup[i]);
 								System.out.println("Response to WRITE of transaction " + query[1] +
 												   " sequence " + query[3] +
 												   " to server " + query[2] +
 												   ": " + msgText);
 							}
 						}
 					}
 					else if (query[0].equals("PASSR")) { // Passed read operation
 						// Check transaction policy against server policy
 						if (checkLocalAuth() == false) {
 							msgText = "ABORT LOCAL_POLICY_FAIL";
 							System.out.println("ABORT LOCAL_POLICY_FAIL: " +
 											   "READ for txn " + query[1] +
 											   " sequence " + query[3]);
 						}
 						else { // OK to read
 							System.out.println("READ for txn " + query[1] +
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
 					else if (query[0].equals("PASSW")) { // Passed write operation
 						// Check transaction policy against server policy
 						if (checkLocalAuth() == false) {
 							msgText = "ABORT LOCAL_POLICY_FAIL";
 							System.out.println("ABORT LOCAL_POLICY_FAIL: " +
 											   "WRITE for txn " + query[1] +
 											   " sequence " + query[3]);
 						}
 						else { // OK to write
 							System.out.println("WRITE for txn " + query[1] +
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
 						}
 					}
 					else if (query[0].equals("JOIN")) {
 						// Coordinator is requesting server to join txn,
 						// set the transaction policy version for this server
 						if (transactionPolicyVersion == 0) {
 							if (my_tm.validationMode >= 0 && my_tm.validationMode <= 2) {
 								// Get policy from the server
 								transactionPolicyVersion = my_tm.getPolicy();
 							}
 							else { // Get and set freshest global policy
 								my_tm.setPolicy(my_tm.callPolicyServer());
 								transactionPolicyVersion = my_tm.getPolicy();									
 							}
 							System.out.println("Transaction " + query[1] +
 											   " Policy version set: " +
 											   transactionPolicyVersion);
 						}
 						if (transactionPolicyVersion < 1) {
 							msgText = "JOIN_FAIL"; // Error message if policy is not properly set
 						}
 					}
 					else if (query[0].equals("VERSION")) { // Coordinator is requesting policy version
 						msgText = "VERSION " + transactionPolicyVersion;
 					}
 					else if (query[0].equals("PTC")) { // Prepare-to-Commit
 						msgText = prepareToCommit(Integer.parseInt(query[1]));
 					}
 					else if (query[0].equals("C")) { // COMMIT
 						System.out.println("COMMIT phase - transaction " + query[1]);
 						// Force global update if necessary
 						if (my_tm.policyPush == 5) {
 							forcePolicyUpdate(3);
 						}
 						// Begin 2PC/2PV methods
 						msgText = coordinatorCommit();
 						System.out.println("Status of 2PC/2PV of transaction " + query[1] +
 										   ": " + msgText);
 					}
 					else if (query[0].equals("RSERV")) { // Random server for policy pushing
 						randomServer = Integer.parseInt(query[1]);
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
 
 	public boolean join(int otherServer, int txnNumber) {
 		// Do only if server has not participated yet
 		if (!sockList.hasSocket(otherServer)) {
 			String server = my_tm.serverList.get(otherServer).getAddress();
 			int port = my_tm.serverList.get(otherServer).getPort();
 			Message msg = null;
 			try {
 				// Create new socket, add it to SocketGroup
 				System.out.println("Connecting to " + server +
 								   " on port " + port);
 				Socket sock = new Socket(server, port);
 				sockList.addSocketObj(otherServer, new SocketObject(sock,
 																	new ObjectOutputStream(sock.getOutputStream()),	
 																	new ObjectInputStream(sock.getInputStream())));
 				// Push policy update if necessary
 				if (my_tm.policyPush == 4) { // Do during operations
 					if (otherServer == randomServer) { // Do if random server is picked
 						System.out.println("*** Pushing policy update ***");
 						// Send policy server msg, wait for ACK
 						try {
 							Message pushMsg = new Message("POLICYPUSH");
 							// Connect to the policy server
 							final Socket pSock = new Socket(my_tm.serverList.get(0).getAddress(),
 															my_tm.serverList.get(0).getPort());
 							// Set up I/O streams with the policy server
 							final ObjectOutputStream output = new ObjectOutputStream(pSock.getOutputStream());
 							final ObjectInputStream input = new ObjectInputStream(pSock.getInputStream());
 							System.out.println("Connected to Policy Server at " +
 											   my_tm.serverList.get(0).getAddress() + ":" +
 											   my_tm.serverList.get(0).getPort());
 							// Send
 							output.writeObject(pushMsg);
 							// Rec'v ACK
 							pushMsg = (Message)input.readObject();
 							if (!pushMsg.theMessage.equals("ACK")) {
 								System.err.println("*** Error with Policy Server during POLICYPUSH.");
 							}
 							// Close the socket - won't be calling again on this thread
 							pSock.close();
 						}
 						catch (Exception e) {
 							System.err.println("Error: " + e.getMessage());
 							e.printStackTrace(System.err);
 						}
 					}
 				}
 				// Tell new participant that they are about to join
 				msg = new Message("JOIN " + txnNumber);
 				// Send message
 				latencySleep(); // Simulate latency to other server
 				sockList.get(otherServer).output.writeObject(msg);
 				// Get response
 				msg = (Message)sockList.get(otherServer).input.readObject();
 				if (!msg.theMessage.equals("ACK")) {
 					return false; // Failure to join properly
 				}
 				return true; // Successful join
 			}
 			catch (ConnectException ce) {
 				System.err.println(ce.getMessage() +
 								   ": Check server address and port number.");
 				ce.printStackTrace(System.err);
 			}
 			catch (Exception e) {
 				System.err.println("Error during join(): " + e.getMessage());
 				e.printStackTrace(System.err);
 			}
 			return false;
 		}
 		else {
 			return true;
 		}
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
 		Message msg = null;
 		
 		try {
 
 			// Add "PASS" to beginning of query (so we have PASSR or PASSW)
 			msg = new Message("PASS" + query);
 			// Send query
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
 
 	public boolean checkTxnConsistency() {
 		int mode = my_tm.validationMode;
 		Message msg = null;
 		System.out.println("checkTxnConsistency() called, mode " + mode);
 		if (mode == 0 || mode == 1 || mode == 2) {
 			// View consistency - call all participants, see if versions match
 			// Call all participants, ask for version
 			if (sockList.size() > 0) {
 				int serverNum[] = new int[sockList.size()];
 				int counter = 0;
 				boolean consistencyOkay = true;
 				// Gather server sockets
 				for (Enumeration<Integer> socketList = sockList.keys(); socketList.hasMoreElements();) {
 					serverNum[counter] = socketList.nextElement();
 					counter++;
 				}
 				// Send messages to all participants
 				for (int i = 0; i < sockList.size(); i++) {
 					if (serverNum[i] != 0) { // Don't call the Policy server
 						try {
							System.out.println("Asking server " + serverNum +
 											   " for txn policy version.");
 							msg = new Message("VERSION");
 							latencySleep(); // Simulate latency
 							// Send
 							sockList.get(serverNum[i]).output.writeObject(msg);
 						}
 						catch (Exception e) {
 							System.err.println("VERSION Send Error: " + e.getMessage());
 							e.printStackTrace(System.err);
 						}
 					}
 				}
 				// Receive responses
 				for (int i = 0; i < sockList.size(); i++) {
 					if (serverNum[i] != 0) { // Don't listen for the Policy server
 						try {
 							msg = (Message)sockList.get(serverNum[i]).input.readObject();
 							// Check response
 							String msgSplit[] = msg.theMessage.split(" ");
 							if (my_tm.policyPush == 4) { // Check version
								System.out.println("Server " + serverNum +
 												   " is using txn policy version " +
 												   Integer.parseInt(msgSplit[1]));
 								if (Integer.parseInt(msgSplit[1]) != transactionPolicyVersion) {
 									return false;
 								}
 							}
 							else if (my_tm.policyPush == 5) { // Not checking until PTC time
								System.out.println("Server " + serverNum +
 												   " is using txn policy version " +
 												   Integer.parseInt(msgSplit[1]) +
 												   " (not checking until PTC)");
 							}
 							else if (my_tm.policyPush == 0) {
 								// No policy pushing - no need to check
 							}
 							else {
 								System.out.println("Invalid policy push variable is set: " + my_tm.policyPush);
 							}
 						}
 						catch (Exception e) {
 							System.err.println("VERSION Recv Error: " + e.getMessage());
 							e.printStackTrace(System.err);
 						}
 					}
 				}
 			}
 			
 			return true;
 		}
 		else if (mode == 3 || mode == 4) {
 			// Get global version, compare against all participants
 			int globalVersion = my_tm.callPolicyServer();
 			// Compare to coordinator's version first
 			if (globalVersion != transactionPolicyVersion) {
 				if (my_tm.policyPush == 4) { // Check version
 					return false;
 				}
 				else { // Not checking until PTC time (5), or no pushes (0)
 					// We're cool
 				}
 			}
 			else { // Check any participants
 				// Call all participants, ask for version
 				if (sockList.size() > 0) {
 					int serverNum[] = new int[sockList.size()];
 					int counter = 0;
 					boolean consistencyOkay = true;
 					// Gather server sockets
 					for (Enumeration<Integer> socketList = sockList.keys(); socketList.hasMoreElements();) {
 						serverNum[counter] = socketList.nextElement();
 						counter++;
 					}
 					// Send messages to all participants
 					for (int i = 0; i < sockList.size(); i++) {
 						if (serverNum[i] != 0) { // Don't call the Policy server
 							try {
								System.out.println("Asking server " + serverNum +
 												   " for txn policy version.");
 								msg = new Message("VERSION");
 								latencySleep(); // Simulate latency
 								// Send
 								sockList.get(serverNum[i]).output.writeObject(msg);
 							}
 							catch (Exception e) {
 								System.err.println("VERSION Send Error: " + e.getMessage());
 								e.printStackTrace(System.err);
 							}
 						}
 					}
 					// Receive responses
 					for (int i = 0; i < sockList.size(); i++) {
 						if (serverNum[i] != 0) { // Don't listen for the Policy server
 							try {
 								msg = (Message)sockList.get(serverNum[i]).input.readObject();
 								// Check response
 								String msgSplit[] = msg.theMessage.split(" ");
 								if (my_tm.policyPush == 4) { // Check version
									System.out.println("Server " + serverNum +
 													   " is using txn policy version " +
 													   Integer.parseInt(msgSplit[1]));
 									if (Integer.parseInt(msgSplit[1]) != globalVersion) {
 										return false;
 									}
 								}
 								else if (my_tm.policyPush == 5) { // Not checking until PTC time
									System.out.println("Server " + serverNum +
 													   " is using txn policy version " +
 													   Integer.parseInt(msgSplit[1]) +
 													   " (not checking until PTC)");
 								}
 								else if (my_tm.policyPush == 0) {
 									// No policy pushing - no need to check
 								}
 								else {
 									System.out.println("Invalid policy push variable is set: " + my_tm.policyPush);
 								}
 							}
 							catch (Exception e) {
 								System.err.println("VERSION Recv Error: " + e.getMessage());
 								e.printStackTrace(System.err);
 							}
 						}
 					}
 				}
 			}
 			return true;
 		}
 		else {
 			System.out.println("*** Unknown validation mode: " + mode +
 							   " ***");
 			return false;
 		}
 	}
 	
 	/**
 	 * When the coordinator receives a request to COMMIT, it directs the flow
 	 * of the transaction to a global consistency check.
 	 *
 	 * @return String - the result of the 2PC/2PV check, either COMMIT or ABORT
 	 */
 	public String coordinatorCommit() {
 		// Call each participating server with a PTC message
 		if (my_tm.validationMode >= 0 && my_tm.validationMode <= 4) {
 			if (my_tm.validationMode == 0) { // 2PC Only
 				// Get YES/NO from all participants
 				if (prepareCall(0).equals("NO")) {
 					return "ABORT PTC_RESPONSE_NO";
 				}
 			}
 			else if (my_tm.validationMode == 1 || my_tm.validationMode == 3) {
 				// Check freshest global policy version == txn version
 				if (my_tm.callPolicyServer() == transactionPolicyVersion) {
 					if (prepareCall(0).equals("NO")) {
 						return "ABORT PTC_RESPONSE_NO";
 					}
 				}
 				else {
 					return "ABORT GLOBAL_CONSISTENCY_FAIL";
 				}
 			}
 			else { // VM == 2 || VM == 4
 				// Get current global policy from policy server
 				int globalVersion = my_tm.callPolicyServer();
 				if (globalVersion == transactionPolicyVersion) {
 					// Get YES/NO from all participants
 					if (prepareCall(0).equals("NO")) {
 						return "ABORT PTC_RESPONSE_NO";
 					}
 				}
 				else {
 					// Get responses from all participants
 					String response = prepareCall(globalVersion);
 					if (response.equals("NO")) {
 						return "ABORT PTC_RESPONSE_NO";
 					}
 					else { // "YES" was received
 						if (response.indexOf("FALSE") != -1) {
 							// Someone responded FALSE
 							return "ABORT PTC_RESPONSE_FALSE";
 						}
 					}
 				}
 			}
 		}
 		else {
 			return "ABORT UNKNOWN_MODE";
 		}
 		
 		return "COMMIT";
 	}
 	
 	/**
 	 * The prepare-to-commit method that is invoked when participating servers
 	 * received the PTC call from the coordinator
 	 *
 	 * @param globalVersion - used for global consistency check
 	 * @return String
 	 */
 	public String prepareToCommit(int globalVersion) {
 		// Receive PTC message, handle options
 		if (my_tm.validationMode == 0) { // 2PC only
 			// Return integrity status
 			if (integrityCheck()) {
 				return "YES";
 			}
 		}
 		else if (my_tm.validationMode == 1 || my_tm.validationMode == 3) {
 			// Global single chance
 			if (integrityCheck()) {
 				return "YES";
 			}
 		}
 		else if (my_tm.validationMode == 2 || my_tm.validationMode == 4) {
 			// Check integrity, then call Policy Server for freshest policy,
 			// re-run authorizations
 			if (integrityCheck()) {
 				// Get fresh policy - this is a bit of a hack, just call server
 				// and set transaction policy from globalVersion
 				my_tm.callPolicyServer(); // For posterity
 				transactionPolicyVersion = globalVersion;
 				// Re-run authorizations
 				System.out.println("Running auth. on transaction " +
 								   queryLog.get(0).getTransaction() + 
 								   " queries using policy version " +
 								   transactionPolicyVersion);
 				for (int j = 0; j < queryLog.size(); j++) {
 					if (!checkLocalAuth()) {
 						System.out.println("Authorization of " + queryLog.get(j).getQueryType() +
 										   " for txn " + queryLog.get(j).getTransaction() +
 										   ", seq " + queryLog.get(j).getSequence() +
 										   " with policy v. " + transactionPolicyVersion +
 										   " (was v. " + queryLog.get(j).getPolicy() +
 										   "): FAIL");
 						return "YES FALSE"; // (authorization failed)
 					}
 					else {
 						System.out.println("Authorization of " + queryLog.get(j).getQueryType() +
 										   " for txn " + queryLog.get(j).getTransaction() +
 										   ", seq " + queryLog.get(j).getSequence() +
 										   " with policy v. " + transactionPolicyVersion +
 										   " (was v. " + queryLog.get(j).getPolicy() +
 										   "): PASS");
 					}
 				}
 				return "YES TRUE"; // Successful re-authorizations
 			}
 		}
 		return "NO";
 	}
 		
 	/**
 	 * The prepareCall() method is invoked by the coordinator to handle
 	 * integrity checks and/or authorizations for itself and any participants
 	 *
 	 * @param version - the policy version being compared
 	 * @return string
 	 */
 	public String prepareCall(int version) {
 		boolean integrityOkay = true;
 		boolean authorizationsOkay = true;
 		// Call all participants, send PTC message and gather responses
 		if (sockList.size() > 0) {
 			Message msg = null;
 			int serverNum[] = new int[sockList.size()];
 			int counter = 0;
 			
 			// Gather server sockets
 			for (Enumeration<Integer> socketList = sockList.keys(); socketList.hasMoreElements();) {
 				serverNum[counter] = socketList.nextElement();
 				counter++;
 			}
 			// Send messages to all participants
 			for (int i = 0; i < sockList.size(); i++) {
 				if (serverNum[i] != 0) { // Don't call the Policy server
 					try {
 						msg = new Message("PTC " + version);
 						latencySleep(); // Simulate latency
 						// Send
 						sockList.get(serverNum[i]).output.writeObject(msg);
 					}
 					catch (Exception e) {
 						System.err.println("prepareCall() send Error: " + e.getMessage());
 						e.printStackTrace(System.err);
 					}
 				}
 			}
 			
 			// Handle coordinator's operations
 			if (!integrityCheck()) {
 				integrityOkay = false;
 			}
 			if (integrityOkay) {
 				// Check authorizations if necessary
 				if (my_tm.validationMode == 2 || my_tm.validationMode == 4) {
 					if (version != transactionPolicyVersion) {
 						// Perform re-authorizations on self
 						transactionPolicyVersion = version;
 						System.out.println("Running auth. on transaction " +
 										   queryLog.get(0).getTransaction() + 
 										   " queries using policy version " +
 										   transactionPolicyVersion);
 						for (int j = 0; j < queryLog.size(); j++) {
 							if (queryLog.get(j).getPolicy() != transactionPolicyVersion) {
 								if (!checkLocalAuth()) {
 									System.out.println("Authorization of " + queryLog.get(j).getQueryType() +
 													   " for txn " + queryLog.get(j).getTransaction() +
 													   ", seq " + queryLog.get(j).getSequence() +
 													   " with policy v. " + transactionPolicyVersion +
 													   " (was v. " + queryLog.get(j).getPolicy() +
 													   "): FAIL");
 									authorizationsOkay = false;
 								}
 								else {
 									System.out.println("Authorization of " + queryLog.get(j).getQueryType() +
 													   " for txn " + queryLog.get(j).getTransaction() +
 													   ", seq " + queryLog.get(j).getSequence() +
 													   " with policy v. " + transactionPolicyVersion +
 													   " (was v. " + queryLog.get(j).getPolicy() +
 													   "): PASS");
 								}
 							}
 							else {
 								System.out.println("Authorization of " + queryLog.get(j).getQueryType() +
 												   " for txn " + queryLog.get(j).getTransaction() +
 												   ", seq " + queryLog.get(j).getSequence() +
 												   " with policy v. " + transactionPolicyVersion +
 												   ": ALREADY DONE");
 							}
 						}
 					}
 				}
 			}
 			
 			// Receive responses
 			for (int i = 0; i < sockList.size(); i++) {
 				if (serverNum[i] != 0) { // Don't listen for the Policy server
 					try {
 						msg = (Message)sockList.get(serverNum[i]).input.readObject();
 						// Check response
 						if (msg.theMessage.indexOf("NO") != -1) { // Someone responded NO
 							integrityOkay = false;
 						}
 						else if (my_tm.validationMode == 2 || my_tm.validationMode == 4) {
 							if (msg.theMessage.indexOf("FALSE") != -1) { // Someone responded FALSE
 								authorizationsOkay = false;
 							}
 						}
 					}
 					catch (Exception e) {
 						System.err.println("prepareCall() recv Error: " + e.getMessage());
 						e.printStackTrace(System.err);
 					}
 				}
 			}
 		}
 		else { // Only the coordinator is participating in txn
 			if (!integrityCheck()) {
 				integrityOkay = false;
 			}
 			if (integrityOkay) {
 				// Check authorizations if necessary
 				if (my_tm.validationMode == 2 || my_tm.validationMode == 4) {
 					if (version != transactionPolicyVersion) {
 						// Perform re-authorizations on self
 						transactionPolicyVersion = version;
 						System.out.println("Running auth. on transaction " +
 										   queryLog.get(0).getTransaction() + 
 										   " queries using policy version " +
 										   transactionPolicyVersion);
 						for (int j = 0; j < queryLog.size(); j++) {
 							if (queryLog.get(j).getPolicy() != transactionPolicyVersion) {
 								if (!checkLocalAuth()) {
 									System.out.println("Authorization of " + queryLog.get(j).getQueryType() +
 													   " for txn " + queryLog.get(j).getTransaction() +
 													   ", seq " + queryLog.get(j).getSequence() +
 													   " with policy v. " + transactionPolicyVersion +
 													   " (was v. " + queryLog.get(j).getPolicy() +
 													   "): FAIL");
 									authorizationsOkay = false;
 								}
 								else {
 									System.out.println("Authorization of " + queryLog.get(j).getQueryType() +
 													   " for txn " + queryLog.get(j).getTransaction() +
 													   ", seq " + queryLog.get(j).getSequence() +
 													   " with policy v. " + transactionPolicyVersion +
 													   " (was v. " + queryLog.get(j).getPolicy() +
 													   "): PASS");
 								}
 							}
 							else {
 								System.out.println("Authorization of " + queryLog.get(j).getQueryType() +
 												   " for txn " + queryLog.get(j).getTransaction() +
 												   ", seq " + queryLog.get(j).getSequence() +
 												   " with policy v. " + transactionPolicyVersion +
 												   ": ALREADY DONE");
 							}
 						}
 					}
 				}
 			}
 		}
 		
 		if (!integrityOkay) {
 			return "NO";
 		}
 		else if (!authorizationsOkay) {
 			return "YES FALSE";
 		}
 		
 		return "YES TRUE";
 	}
 }
