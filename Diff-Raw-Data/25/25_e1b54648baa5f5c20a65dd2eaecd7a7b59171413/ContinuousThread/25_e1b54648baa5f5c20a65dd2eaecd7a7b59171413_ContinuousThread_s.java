 /**
  * File: ContinuousThread.java
  * @author: Tucker Trainor <tmt33@pitt.edu>
  *
  * A server thread to handle continuous proofs of authorization.
  * Extends the IncrementalThread class.
  */
 
 import java.lang.Thread;
 import java.net.Socket;
 import java.net.ConnectException;
 import java.io.*;
 import java.util.*;
 
 public class ContinuousThread extends IncrementalThread {
 	
 	/**
 	 * Constructor that sets up the socket we'll chat over
 	 *
 	 * @param _socket - The socket passed in from the server
 	 * @param _my_tm - The Transaction Manager that called the thread
 	 */
 	public ContinuousThread(Socket _socket, CloudServer _my_tm) {
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
 							// Check that a txn policy version has been set
 							if (transactionPolicyVersion == 0) {
 								if (my_tm.validationMode == 2 && query.length == 4) {
 									// Only the coordinator should call the
 									// policy server, and only the coordinator
 									// receives query.length == 4
 									my_tm.setPolicy(my_tm.callPolicyServer());
 									transactionPolicyVersion = my_tm.getPolicy();
 								}
 								else {
 									// Get freshest policy on local server
 									transactionPolicyVersion = my_tm.getPolicy();
 								}
 								System.out.println("Transaction " + query[1] +
 												   " Policy version set: " +
 												   transactionPolicyVersion);
 							}
 							
 							// query[] has a length of 5 or 6 - handle each
 							if (query.length >= 5) {
 								// Get passed in policy version
 								int coordPolicy = Integer.parseInt(query[4]);
 								// Handle possible policy version inequality
 								if (transactionPolicyVersion < coordPolicy) {
 									// Update txn policy, rerun auths if necessary
 									transactionPolicyVersion = coordPolicy;
 									if (!rerunAuths(transactionPolicyVersion)) {
 										msgText = "ABORT LOCAL_POLICY_FALSE";
 									}
 								}
 								if (query.length == 6) { // Push a policy update
 									transactionPolicyVersion++;
 								}
 								// Perform usual auth and operation
 								if (checkLocalAuth() == false) {
 									msgText = "ABORT LOCAL_POLICY_FALSE";
 									System.out.println("ABORT LOCAL_POLICY_FALSE: " +
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
 							else { // query.length == 4 (legacy code)
 								// Check authorization
 								if (checkLocalAuth() == false) {
 									msgText = "ABORT LOCAL_POLICY_FALSE";
 									System.out.println("ABORT LOCAL_POLICY_FALSE: " +
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
 					else if (query[0].equals("W")) { // WRITE
 						// Check server number, perform query or pass on
 						if (Integer.parseInt(query[2]) == my_tm.serverNumber) { // Perform query on this server
 							// Check that a txn policy version has been set
 							if (transactionPolicyVersion == 0) {
 								if (my_tm.validationMode == 2 && query.length == 4) {
 									// Only the coordinator should call the
 									// policy server, and only the coordinator
 									// receives query.length == 4
 									my_tm.setPolicy(my_tm.callPolicyServer());
 									transactionPolicyVersion = my_tm.getPolicy();
 								}
 								else {
 									// Get freshest policy on local server
 									transactionPolicyVersion = my_tm.getPolicy();
 								}
 								System.out.println("Transaction " + query[1] +
 												   " Policy version set: " +
 												   transactionPolicyVersion);
 							}
 							
 							
 							// query[] has a length of 5 or 6 - handle each
 							if (query.length >= 5) {
 								// Get passed in policy version
 								int coordPolicy = Integer.parseInt(query[4]);
 								// Handle possible policy version inequality
 								if (transactionPolicyVersion < coordPolicy) {
 									// Update txn policy, rerun auths if necessary
 									transactionPolicyVersion = coordPolicy;
 									if (!rerunAuths(transactionPolicyVersion)) {
 										msgText = "ABORT LOCAL_POLICY_FALSE";
 									}
 								}
 								if (query.length == 6) { // Push a policy update
 									transactionPolicyVersion++;
 								}
 								// Perform usual auth and operation
 								if (checkLocalAuth() == false) {
 									msgText = "ABORT LOCAL_POLICY_FALSE";
 									System.out.println("ABORT LOCAL_POLICY_FALSE: " +
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
 							else { // query.length == 4 (legacy code)
 								// Check authorization
 								if (checkLocalAuth() == false) {
 									msgText = "ABORT LOCAL_POLICY_FALSE";
 									System.out.println("ABORT LOCAL_POLICY_FALSE: " +
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
 					else if (query[0].equals("VERSION")) { // Coordinator is requesting policy version
 						if (transactionPolicyVersion == 0) {
 							// This thread may not have had txn policy set yet
 							transactionPolicyVersion = my_tm.getPolicy();
 						}
 						if (query.length == 2 && query[1].equals("P")) { // Policy push
 							transactionPolicyVersion++;
 						}
 						msgText = "VERSION " + transactionPolicyVersion;
 					}
 					else if (query[0].equals("C")) { // COMMIT
 						System.out.println("COMMIT phase - transaction " + query[1]);
 						// Begin 2PC/2PV methods
 						msgText = commitPhase();
 						System.out.println("Status of 2PC/2PV of transaction " + query[1] +
 										   ": " + msgText);
 					}
 					else if (query[0].equals("PTC")) { // Prepare-to-Commit
 						if (integrityCheck()) {
 							msgText = "YES";
 						}
 						else {
 							msgText = "NO";
 						}
 					}
 					else if (query[0].equals("2PVC")) {
 						if (integrityCheck()) { // If integrity check passes
 							String result2PV = answer2PV(Integer.parseInt(query[1]));
 							if (result2PV.indexOf("TRUE") != -1) { // 2PV successful
 								msgText = "YES TRUE " + Integer.parseInt(query[1]);
 							}
 							else {
 								msgText = "YES FALSE " + Integer.parseInt(query[1]);
 							}
 						}
 						else {
 							msgText = "NO";
 						}
 					}
 					else if (query[0].equals("2PV")) {
 						// receives: 2PV [policy from coord]
 						// Reruns auths (if necessary) with greater of local/coord policy
 						// returns:
 						// TRUE [policy]
 						// FALSE [policy]
 						msgText = answer2PV(Integer.parseInt(query[1]));
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
 	
 	/**
 	 * Passes a query to other specified server
 	 *
 	 * @param otherServer - The number of the server to pass to
 	 * @param query - The query that must be performed on another server
 	 *
 	 * @return String - the response from the other server
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
 				
 				// Add policy version and/or push sentinel to query
 				query += " " + transactionPolicyVersion;
 				if (my_tm.policyPush == 2) {
 					// Push policy update at each joining server
 					query += " P";
 				}
 				else if (!hasUpdated && my_tm.policyPush == 1) {
 					// Push policy update to a random server
 					if (otherServer == randomServer) {
 						// Add a sentinel to end of query
 						query += " P";
 						hasUpdated = true; // This only needs to be done once
 					}
 				}
 				
 				// Send message
 				msg = new Message(query);
 				latencySleep(); // Simulate latency to other server
 				sockList.get(otherServer).output.writeObject(msg);
 				msg = (Message)sockList.get(otherServer).input.readObject();
 				System.out.println("Server " + otherServer +
 								   " says: " + msg.theMessage +
 								   " for passed query " + query);
 				String msgSplit[] = msg.theMessage.split(" ");
 				// Expecting TRUE <policy version> or FALSE <policy version>
 				if (msg.theMessage.indexOf("FALSE") != -1) {
 					return "ABORT LOCAL_AUTHORIZATION_FAIL";
 				}
 				int recdPolicy = Integer.parseInt(msgSplit[1]);
 				if (recdPolicy > transactionPolicyVersion) {
 					// Run 2PV - any server who has a txn policy < rec'd must
 					// run authorizations again
 					if (!run2PV(recdPolicy)) {
 						return "ABORT LOCAL_AUTHORIZATION_FAIL";
 					}
 				}
				return "ACK"; // Everything is OK
 			}
 			
 			// Send the normal query
 			msg = new Message(query);
 			latencySleep(); // Simulate latency to other server
 			sockList.get(otherServer).output.writeObject(msg);
 			msg = (Message)sockList.get(otherServer).input.readObject();
 			System.out.println("Server " + otherServer +
 							   " says: " + msg.theMessage +
 							   " for passed query " + query);
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
 	
 	/**
 	 * Performs the actions necessary by the coordinator for committing
 	 *
 	 * @return String - COMMIT or ABORT plus reason
 	 */
 	public String commitPhase() {
 		if (my_tm.validationMode == 0) { // 2PC only? Valid option?
 			return run2PC();
 		}
 		else if (my_tm.validationMode == 1) { // View consistency - 2PC only
 			return run2PC();
 		}
 		else if (my_tm.validationMode == 2) { // Global consistency - 2PVC
 			return run2PVC(transactionPolicyVersion);
 		}
 		
 		return "ABORT UNKNOWN_VALIDATION_MODE";
 	}
 
 	/**
 	 * Runs proofs of authorization on operations previously done with an
 	 * earlier version.
 	 *
 	 * @param int - the current policy version that we need to use for auths
 	 *
 	 * @return boolean - false if FALSE, true if TRUE
 	 */
 	public boolean rerunAuths(int currentPolicyVersion) {
 		for (int j = 0; j < queryLog.size(); j++) {
 			if (queryLog.get(j).getPolicy() < currentPolicyVersion) {
 				if (!checkLocalAuth()) {
 					System.out.println("Authorization of " + queryLog.get(j).getQueryType() +
 									   " for txn " + queryLog.get(j).getTransaction() +
 									   ", seq " + queryLog.get(j).getSequence() +
 									   " with policy v. " + transactionPolicyVersion +
 									   " (was v. " + queryLog.get(j).getPolicy() +
 									   "): FALSE");
 					return false;
 				}
 				else {
 					System.out.println("Authorization of " + queryLog.get(j).getQueryType() +
 									   " for txn " + queryLog.get(j).getTransaction() +
 									   ", seq " + queryLog.get(j).getSequence() +
 									   " with policy v. " + transactionPolicyVersion +
 									   " (was v. " + queryLog.get(j).getPolicy() +
 									   "): TRUE");
 					// Update policy version used for proof
 					queryLog.get(j).setPolicy(transactionPolicyVersion);
 				}
 			}
 		}
 		return true;
 	}
 
 	/**
 	 * Performs the 2PC algorithm with the servers participating in the
 	 * transaction.
 	 *
 	 * @return String - the result of the 2PV process
 	 */
 	public String run2PC() {
 		if (sockList.size() > 0) {
 			Message msg = null;
 			int serverNum[] = new int[sockList.size()];
 			int counter = 0;
 			boolean integrityOkay = true;
 			// Gather server sockets
 			for (Enumeration<Integer> socketList = sockList.keys(); socketList.hasMoreElements();) {
 				serverNum[counter] = socketList.nextElement();
 				counter++;
 			}
 			// Send messages to all participants
 			for (int i = 0; i < sockList.size(); i++) {
 				if (serverNum[i] != 0) { // Don't call the Policy server
 					try {
 						msg = new Message("PTC");
 						latencySleep(); // Simulate latency
 						// Send
 						sockList.get(serverNum[i]).output.writeObject(msg);
 					}
 					catch (Exception e) {
 						System.err.println("PTC Send Error: " + e.getMessage());
 						e.printStackTrace(System.err);
 					}
 				}
 			}
 			
 			// Check coordinator's integrity
 			if (!integrityCheck()) {
 				integrityOkay = false;
 			}
 			
 			// Receive responses
 			for (int i = 0; i < sockList.size(); i++) {
 				if (serverNum[i] != 0) { // Don't listen for the Policy server
 					try {
 						msg = (Message)sockList.get(serverNum[i]).input.readObject();
 						// Check response, add policy version to ArrayList
 						System.out.println("Response of server " + serverNum[i] +
 										   " for message PTC: " + msg.theMessage);
 						// Parse response
 						if (msg.theMessage.indexOf("NO") != -1) { // Someone responded NO
 							integrityOkay = false;
 						}
 					}
 					catch (Exception e) {
 						System.err.println("PTC Recv Error: " + e.getMessage());
 						e.printStackTrace(System.err);
 					}
 				}
 			}
 			// Check for any reported integrity failures
 			if (!integrityOkay) {
 				return "ABORT PTC_RESPONSE_NO";
 			}
 		}
 		else { // Coordinator is only participant
 			if (!integrityCheck()) {
 				return "ABORT PTC_RESPONSE_NO";
 			}
 		}
 		return "COMMIT";
 	}
 	
 	/**
 	 * Performs the 2PV algorithm with the servers participating in the
 	 * transaction.
 	 *
 	 * @param int - the policy version to perform 2PV with
 	 *
 	 * @return boolean - the result of the 2PV process
 	 */
 	public boolean run2PV(int freshestPolicy) {
 		boolean authorizationsOkay = true;
 		// Contact all servers, send 2PV [policy] and gather responses
 		if (sockList.size() > 0) {
 			Message msg = null;
 			int serverNum[] = new int[sockList.size()];
 			int counter = 0;
 			int recdPolicy;
 			int highestPolicyForFalse = 0;
 			boolean needToRun = true;
 
 			// Gather server sockets
 			for (Enumeration<Integer> socketList = sockList.keys(); socketList.hasMoreElements();) {
 				serverNum[counter] = socketList.nextElement();
 				counter++;
 			}
 			// Run 2PV as long as necessary
 			while (needToRun && authorizationsOkay) {
 				needToRun = false;
 				// Send messages to all participants
 				for (int i = 0; i < sockList.size(); i++) {
 					if (serverNum[i] != 0) { // Don't call the Policy server
 						try {
 							msg = new Message("2PV " + freshestPolicy);
 							latencySleep(); // Simulate latency
 							// Send
 							sockList.get(serverNum[i]).output.writeObject(msg);
 						}
 						catch (Exception e) {
 							System.err.println("run2PV() Send Error: " + e.getMessage());
 							e.printStackTrace(System.err);
 						}
 					}
 				}
 				
 				// Check that coordinator's policy version is up to date
 				if (freshestPolicy > transactionPolicyVersion) {
 					// Re-run proofs on coordinator
 					transactionPolicyVersion = freshestPolicy;
 					System.out.println("Running auth. on transaction " +
 									   queryLog.get(0).getTransaction() + 
 									   " queries using policy version " +
 									   transactionPolicyVersion);
 					if (!rerunAuths(transactionPolicyVersion)) {
 						authorizationsOkay = false;
 					}
 				}
 				
 				// Receive responses
 				for (int i = 0; i < sockList.size(); i++) {
 					if (serverNum[i] != 0) { // Don't listen for the Policy server
 						try {
 							msg = (Message)sockList.get(serverNum[i]).input.readObject();
 							System.out.println("Response of server " + serverNum[i] +
 											   " for message 2PV " + freshestPolicy +
 											   ": " + msg.theMessage);
 							// Parse response: TRUE [policy] or FALSE [policy]
 							String msgSplit[] = msg.theMessage.split(" ");
 							recdPolicy = Integer.parseInt(msgSplit[1]);
 							
 							if (msgSplit[0].equals("FALSE")) {
 								if (recdPolicy > highestPolicyForFalse) {
 									highestPolicyForFalse = recdPolicy;
 								}
 							}
 							else { // (msgSplit[0].equals("TRUE"))
 								if (recdPolicy > freshestPolicy) {
 									freshestPolicy = recdPolicy;
 								}
 							}
 						}
 						catch (Exception e) {
 							System.err.println("run2PV() Recv Error: " + e.getMessage());
 							e.printStackTrace(System.err);
 						}
 					}
 				}
 				// If we received a FALSE for a policy version equal to or
 				// greater than the most recent version that returned TRUE
 				if (highestPolicyForFalse >= freshestPolicy) {
 					authorizationsOkay = false;
 					needToRun = false;
 				}
 				// If there was a server with a fresher policy than the
 				// coordinator, run 2PV again with the freshest policy
 				else if (freshestPolicy > transactionPolicyVersion) {
 					transactionPolicyVersion = freshestPolicy;
 					// Allow another chance if coordinator set authOkay to false
 					authorizationsOkay = true;
 					needToRun = true;
 				}
 			}
 		}
 
 		return authorizationsOkay;
 	}
 	
 	/**
 	 * Performs the local checks when 2PV is called from the coordinator
 	 * transaction.
 	 *
 	 * @param int - the policy version to perform proofs with
 	 *
 	 * @return String - the result of the process (TRUE/FALSE [policy version]
 	 */
 	public String answer2PV(int coordPolicy) {
 		if (coordPolicy > transactionPolicyVersion) { // Re-run proofs with fresher policy
 			transactionPolicyVersion = coordPolicy;
 			System.out.println("Running auth. on transaction " +
 							   queryLog.get(0).getTransaction() + 
 							   " queries using policy version " +
 							   transactionPolicyVersion);
 			if (!rerunAuths(transactionPolicyVersion)) {
 				return "FALSE " + transactionPolicyVersion; // (authorization failed)
 			}
 		}
 		return "TRUE " + transactionPolicyVersion;
 	}
 	
 	/**
 	 * Performs the 2PVC algorithm with the servers participating in the
 	 * transaction.
 	 *
 	 * @param int - the policy version to perform 2PVC with
 	 *
 	 * @return String - the result of the 2PVC process
 	 */
 	public String run2PVC(int policyVersion) {
 		// Get and set freshest global policy - make call even though we are
 		// discarding response
 		int freshestPolicy = my_tm.callPolicyServer();
 		if (my_tm.policyPush == 3) {
 			// Push update to policy version
 			freshestPolicy = transactionPolicyVersion + 1;
 		}
 		else {
 			freshestPolicy = transactionPolicyVersion;
 		}
 		
 		boolean start2PV = false;
 		
 		// Contact all servers, send 2PVC [policy] and gather responses
 		if (sockList.size() > 0) {
 			Message msg = null;
 			int serverNum[] = new int[sockList.size()];
 			int counter = 0;
 			int recdPolicy;
 			int highestPolicyForFalse = 0;
 			boolean integrityOkay = true;
 			
 			// Gather server sockets
 			for (Enumeration<Integer> socketList = sockList.keys(); socketList.hasMoreElements();) {
 				serverNum[counter] = socketList.nextElement();
 				counter++;
 			}
 			// Send messages to all participants
 			for (int i = 0; i < sockList.size(); i++) {
 				if (serverNum[i] != 0) { // Don't call the Policy server
 					try {
 						msg = new Message("2PVC " + transactionPolicyVersion);
 						latencySleep(); // Simulate latency
 						sockList.get(serverNum[i]).output.writeObject(msg);
 					}
 					catch (Exception e) {
 						System.err.println("run2PVC() Send Error: " + e.getMessage());
 						e.printStackTrace(System.err);
 					}
 				}
 			}
 
 			// Perform integrity check on coordinator
 			if (!integrityCheck()) {
 				integrityOkay = false;
 			}
 			// Check policy version
 			if (freshestPolicy > transactionPolicyVersion) {
 				transactionPolicyVersion = freshestPolicy;
 				// Coordinator needs to rerun proofs with newer policy
 				if (!rerunAuths(transactionPolicyVersion)) {
 					highestPolicyForFalse = transactionPolicyVersion;
 				}
 			}
 
 			// Receive responses
 			for (int i = 0; i < sockList.size(); i++) {
 				if (serverNum[i] != 0) { // Don't listen for the Policy server
 					try {
 						msg = (Message)sockList.get(serverNum[i]).input.readObject();
 						System.out.println("Response of server " + serverNum[i] +
 										   " for message 2PVC " + transactionPolicyVersion +
 										   ": " + msg.theMessage);
 						// Parse response: YES TRUE [policy] or YES FALSE [policy] or NO
 						String msgSplit[] = msg.theMessage.split(" ");
 						
 						if (msgSplit[0].equals("NO")) {
 							integrityOkay = false;
 						}
 						else if (msgSplit[1].equals("FALSE")) {
 							recdPolicy = Integer.parseInt(msgSplit[2]);
 							if (recdPolicy > highestPolicyForFalse) {
 								highestPolicyForFalse = recdPolicy;
 							}
 						}
 						else { // (msgSplit[1].equals("TRUE"))
 							recdPolicy = Integer.parseInt(msgSplit[2]);
 							if (recdPolicy > freshestPolicy) {
 								freshestPolicy = recdPolicy;
 							}
 						}
 					}
 					catch (Exception e) {
 						System.err.println("run2PVC() Recv Error: " + e.getMessage());
 						e.printStackTrace(System.err);
 					}
 				}
 			}
 
 			// If an integrity check failed, abort
 			if (!integrityOkay) {
 				return "ABORT PTC_RESPONSE_NO";
 			}
 			// If we received a FALSE for a policy version equal to or
 			// greater than the most recent version that returned TRUE
 			if (highestPolicyForFalse >= freshestPolicy) {
 				return "ABORT LOCAL_POLICY_FALSE_2PVC";
 			}
 			// If there was a server with a fresher policy than the
 			// coordinator, run 2PV again with the freshest policy
 			else if (freshestPolicy > transactionPolicyVersion) {
 				transactionPolicyVersion = freshestPolicy;
 				start2PV = true;
 			}
 		}
 		else { // Coordinator is the only participant
 			// Perform integrity check on coordinator
 			if (!integrityCheck()) {
 				System.out.println("*** Integrity check failed on coordinator ***");
 				return "ABORT PTC_RESPONSE_NO";
 			}
 			// Check policy version
 			if (freshestPolicy > transactionPolicyVersion) {
 				transactionPolicyVersion = freshestPolicy;
 				// Coordinator needs to rerun proofs with newer policy
 				if (!rerunAuths(transactionPolicyVersion)) {
 					return "ABORT LOCAL_POLICY_FALSE_2PVC";
 				}
 			}
 		}
 		
 		if (start2PV) {
 			if (!run2PV(transactionPolicyVersion)) {
 				return "ABORT LOCAL_POLICY_FALSE_2PV";
 			}
 		}
 		
 		return "COMMIT";
 	}
 	
 }
