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
 
 import java.net.Socket;
 import java.net.ConnectException;
 import java.io.*;
 import java.util.*;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.TimeUnit;
 
 public class Robot {
 	static String proof;
 	static int validationMode;
 	static int policyPush;
 	static int maxTransactions;
 	static int maxOperations;
 	static int minOperations;
 	static int maxServers;
 	static int maxDegree;
 	static int latencyMin;
 	static int latencyMax;
 	static boolean threadSleep;
 	static float integrityCheckSuccessRate;
 	static float localAuthSuccessRate;
 	static float globalAuthSuccessRate;
 	static int policyUpdateMin;
 	static int policyUpdateMax;
 	static long randomSeed;
 	static Random generator;
 	static ExecutorService execSvc;
 
 	/**
 	 * Main method.
 	 *
 	 * @param args - First argument specifies the server address, second
 	 * argument specifies the port number
 	 */
     public static void main(String[] args) {
 		/* To allow outputErrorLog() to properly shut down other servers,
 		 * which is useful if running a series of simulations via shell script,
 		 * we need to be able to send the method the addresses of the servers
 		 * as well as the number of servers being used (i.e., maxServers), so
 		 * we need to load serverConfig.txt and parameters.txt before checking
 		 * for any other errors, e.g., improper command line arguments.
 		 */
 		// Load server information from server configuration file
 		ArrayList<ServerID> serverList = loadConfig("serverConfig.txt");
 		if (serverList == null) {
 			System.err.println("Error loading server configuration file. Exiting.");
 			outputErrorLog(serverList, "Error loading server configuration file.");
 			System.exit(-1);
 		}
 		else {
 			System.out.println("Server configuration file read successfully.");
 		}
 		
 		// Load the parameters for this simulation
 		if (loadParameters("parameters.txt")) {
 			System.out.println("Parameters file read successfully.");
 		}
 		else {
 			System.err.println("Error loading parameters file. Exiting.");
 			outputErrorLog(serverList, "Error loading parameters file.");
 			System.exit(-1);
 		}
 
 		// Error checking for arguments (0, 1, or 6 args)
 		if (args.length != 0 && args.length != 1 && args.length != 6) {
 			System.err.println("Improper argument count.");
 			argsError();
 			outputErrorLog(serverList, "Improper argument count.");
 			System.exit(-1);
 	    }
 		
 		switch (args.length) {
 			case 0:
 				break;
 			case 1:
 				setSeed(serverList, args[0]);
 				break;
 			case 6:
 				setSeed(serverList, args[0]);
 				proof = args[1]; // Note that this does not check against typo
 				setVM(serverList, args[2]);
 				setPush(serverList, args[3]);
 				setOpMin(serverList, args[4]);
 				setOpMax(serverList, minOperations, args[5]);
 				break;
 			default: // We should never reach here, but just in case
 				System.err.println("Default case reached in switch. Exiting.");
 				outputErrorLog(serverList, "Default case reached in switch.");
 				System.exit(-1);
 				break;
 		}
 		
 		// Push parameters to active servers
 		if (!parameterPush(serverList)) {
 			System.err.println("Error pushing parameters. Exiting.");
 			outputErrorLog(serverList, "Error pushing parameters.");
 			System.exit(-1);
 		}
 		
 		// Build a series of transactions using parameters
 		generator = new Random(randomSeed);
 		TransactionData tData = new TransactionData(0, "ZERO");
 		tData.setStartTime();
 		tData.setEndTime(0L);
 		TransactionLog.entry.add(tData);
 		String newTrans = new String();
 		char prevQuery;
 		int queryServer;
 		int operations;
 		// Set up variables for random server policy updates
 		ArrayList<Integer> usedServers = new ArrayList<Integer>();
 		boolean pickRandomServer = false;
 		if (proof.equals("PUNCTUAL") &&
 			(validationMode == 2 || validationMode == 4)) {
 			pickRandomServer = true;
 		}
 		else if (proof.equals("INCREMENTAL") && policyPush == 4) {
 			pickRandomServer = true;
 		}
 		else if (proof.equals("CONTINUOUS") && policyPush == 1) {
 			pickRandomServer = true;
 		}
 		for (int i = 1; i <= maxTransactions; i++) {
 			newTrans = "";
 			prevQuery = 'B';
 			queryServer = 0;
 			// Get random number of queries for this transaction
 			operations = minOperations + generator.nextInt(maxOperations - minOperations);
 			for (int j = 0; j < operations; j++) {
 				String newQuery = new String();
 				// Make a READ or WRITE
 				if (generator.nextBoolean()) {
 					if (prevQuery == 'R') {
 						newQuery += ";R " + i; // ,
 					}
 					else if (prevQuery == 'W') {
 						newQuery += ";R " + i;
 					}
 					else { // First operation
 						newQuery += "R " + i;
 					}
 					prevQuery = 'R';
 				}
 				else {
 					if (prevQuery == 'W') {
 						newQuery += ";W " + i; // ,
 					}
 					else if (prevQuery == 'R') {
 						newQuery += ";W " + i;
 					}
 					else { // First operation
 						newQuery += "W " + i;
 					}
 					prevQuery = 'W';
 				}
 				// Make a server number
 				queryServer = generator.nextInt(maxServers) + 1;
 				newQuery += " " + queryServer;
 				// Add server number to list if not already present
 				if (pickRandomServer) {
 					if (!usedServers.contains(queryServer)) {
 						usedServers.add(queryServer);
 					}
 				}
 				// Add the sequence number
 				newQuery += " " + (j + 1);
 				newTrans += newQuery;
 			}
 			newTrans += ";C " + i + ";exit";
 			// Add a random server to beginning of transaction for random picking
 			if (pickRandomServer) {
 				// Get one of the servers in the list (but not the first, which
 				// is the coordinator) - choose from index 1 to index (size - 1)
 				if (usedServers.size() > 1) {
 					newTrans = "RSERV " + usedServers.get(generator.nextInt(usedServers.size() - 1) + 1) + ";" + newTrans;
 				}
 				// Clear the ArrayList for the next txn
 				usedServers.clear();
 			}
 			tData = new TransactionData(i, newTrans);
 			tData.setStartTime();
 			TransactionLog.entry.add(tData);
 		}
 		
 		// Communicate with CloudServer through pool of RobotThreads
 		int coordinator = 0;
 		String txn;
 		String txnSplit[];
 		execSvc = Executors.newFixedThreadPool(maxDegree);
 			
 		for (int i = 1; i <= maxTransactions; i++) {
 			txn = TransactionLog.entry.get(i).getTxn();
 			txnSplit = txn.split(" ");
 			if (pickRandomServer) {
 				coordinator = Integer.parseInt(txnSplit[3]);
 			}
 			else {
 				coordinator = Integer.parseInt(txnSplit[2]);
 			}
 			execSvc.execute( new RobotThread(i,
 											 coordinator,
 											 txn,
 											 serverList.get(coordinator).getAddress(),
 											 serverList.get(coordinator).getPort(),
 											 latencyMin,
 											 latencyMax,
 											 threadSleep) );
 		}
 		
 		execSvc.shutdown();
 		try {
 			// The tasks are now running concurrently. We wait until all work is
 			// done, with a timeout of 60 minutes (probably longer than
 			// necessary, but we'll be on the safe side)
 			boolean poolean = execSvc.awaitTermination(3600, TimeUnit.SECONDS);
 			// If the execution timed out, false is returned
 			System.out.println("Execution complete: " + poolean);
 		}
 		catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 		
 		// Shut down Policy Server, Cloud Servers
 		for (int i = 0; i <= maxServers; i++) {
 			try {
 				// Connect to the specified server
 				Socket sock = new Socket(serverList.get(i).getAddress(),
 										 serverList.get(i).getPort());
 				// Set up I/O streams with the server
 				ObjectOutputStream output = new ObjectOutputStream(sock.getOutputStream());
 				ObjectInputStream input = new ObjectInputStream(sock.getInputStream());
 				// Send KILL
 				output.writeObject(new Message("KILL"));
 				// Disconnect from server
 				sock.close();
 			}
 			catch (ConnectException ce) {
 				System.err.println(ce.getMessage() +
 								   ": Check server address and port number.");
 				ce.printStackTrace(System.err);
 			}
 			catch (Exception e) {
 				System.err.println("Error during KILL: " + e.getMessage());
 				e.printStackTrace(System.err);
 			}
 		}
 		
 		// Record output log
 		if (outputLog()) {
 			System.out.println("Log created.");
 		}
 		else {
 			System.out.println("Error during log file creation.");
 		}
     }
 	
     /**
      * Load a file containing the parameters and applicable data for the Robot
      */
 	private static boolean loadParameters(String filename) {
 		BufferedReader inputBuf = null;
 		String line = null;
 		// use a try/catch block to open the input file with a FileReader
 		try {
 			inputBuf = new BufferedReader(new FileReader(filename));
 		}
 		catch (FileNotFoundException fnfe) {
 			// if the file is not found, exit the program
 			System.out.println("File \"" + filename + "\" not found. Exiting program.");
 			fnfe.printStackTrace();
 			return false;
 		}
 		
 		// Read and parse the contents of the file
 		try {
 			line = inputBuf.readLine();
 		}
 		catch (IOException ioe) {
 			System.out.println("IOException during readLine(). Exiting program.");
 			ioe.printStackTrace();
 			return false;
 		}
 		while (line != null) {
 			if (line.charAt(0) != '#') { // not a comment line
 				try {
 					String tuple[] = line.split(" ");
 					if (tuple[0].equals("PROOF")) {
 						proof = tuple[1];
 					}
 					else if (tuple[0].equals("VM")) {
 						validationMode = Integer.parseInt(tuple[1]);
 					}
 					else if (tuple[0].equals("PUSH")) {
 						policyPush = Integer.parseInt(tuple[1]);
 					}
 					else if (tuple[0].equals("MT")) {
 						maxTransactions = Integer.parseInt(tuple[1]);
 					}
 					else if (tuple[0].equals("OPMIN")) {
 						minOperations = Integer.parseInt(tuple[1]);
 					}
 					else if (tuple[0].equals("OPMAX")) {
 						maxOperations = Integer.parseInt(tuple[1]);
 					}
 					else if (tuple[0].equals("MS")) {
 						maxServers = Integer.parseInt(tuple[1]);
 					}
 					else if (tuple[0].equals("MD")) {
 						maxDegree = Integer.parseInt(tuple[1]);
 					}
 					else if (tuple[0].equals("LMIN")) {
 						latencyMin = Integer.parseInt(tuple[1]);
 					}
 					else if (tuple[0].equals("LMAX")) {
 						latencyMax = Integer.parseInt(tuple[1]);
 					}
 					else if (tuple[0].equals("SLEEP")) {
 						threadSleep = Boolean.parseBoolean(tuple[1]);
 					}
 					else if (tuple[0].equals("ICSR")) {
 						integrityCheckSuccessRate = Float.parseFloat(tuple[1]);
 					}
 					else if (tuple[0].equals("LASR")) {
 						localAuthSuccessRate = Float.parseFloat(tuple[1]);
 					}
 					else if (tuple[0].equals("GASR")) {
 						globalAuthSuccessRate = Float.parseFloat(tuple[1]);
 					}
 					else if (tuple[0].equals("PMIN")) {
 						policyUpdateMin = Integer.parseInt(tuple[1]);
 					}
 					else if (tuple[0].equals("PMAX")) {
 						policyUpdateMax = Integer.parseInt(tuple[1]);
 					}
 					else if (tuple[0].equals("RS")) {
 						randomSeed = Long.parseLong(tuple[1]);
 					}
 				}
 				catch (Exception e) {
 					System.out.println("Error while parsing \"" + filename +
 									   "\".");
 					e.printStackTrace();
 					return false;
 				}
 			}
 			// get next line
 			try {
 				line = inputBuf.readLine();
 			}
 			catch (IOException ioe) {
 				System.out.println("IOException during readLine().");
 				ioe.printStackTrace();
 				return false;
 			}
 		}
 
 		// close BufferedReader using a try/catch block
 		try {
 			inputBuf.close();
 		}
 		catch (IOException ioe) {
 			// if exception caught, exit the program
 			System.out.println("Error closing reader. Exiting program");
 			ioe.printStackTrace();
 			return false;
 		}
 		
 		return true; // success
 	}
 	
 	public static void argsError() {
 		System.err.println("Usage: java Robot or");
 		System.err.println("Usage: java Robot <Seed> or");
 		System.err.println("Usage: java Robot <Seed> <PROOF> <VM> <PUSH> <OPMIN> <OPMAX>\n");
 	}
 	
 	public static int getTM(ArrayList<ServerID> _serverList, String str) {
 		int number = 0;
 		// Check arg for proper value, range
 		try {
 			number = Integer.parseInt(str);
 			if (number < 1 || number >= _serverList.size()) {
 				System.err.println("Error in server number. Please check server configuration.");
 				outputErrorLog(_serverList, "getTM(): Error in server number.");
 				System.exit(-1);
 			}
 		}
 		catch (Exception e) {
 			System.err.println("Error parsing argument for TM. Please use a valid integer.");
 			argsError();
 			outputErrorLog(_serverList, "getTM(): Error parsing argument for TM.");
 			System.exit(-1);
 		}
 		return number;
 	}
 		
 	public static void setSeed(ArrayList<ServerID> _serverList, String str) {
 		try {
 			randomSeed = Long.parseLong(str);
 		}
 		catch (Exception e) {
 			System.err.println("Error parsing argument for seed. Please use a valid integer.");
 			argsError();
 			outputErrorLog(_serverList, "setSeed(): Error parsing argument for seed.");
 			System.exit(-1);
 		}
 	}
 	
 	public static void setOpMin(ArrayList<ServerID> _serverList, String str) {
 		int min = 0;
 		// Check arg for proper value, range
 		try {
 			min = Integer.parseInt(str);
 			if (min < 1) {
 				System.err.println("Error in OPMIN. Please set a minimum of at least 1.");
 				outputErrorLog(_serverList, "setOpMin(): Error in OPMIN.");
 				System.exit(-1);
 			}
 			minOperations = min;
 		}
 		catch (Exception e) {
 			System.err.println("Error parsing argument for OPMIN. Please use a valid integer.");
 			argsError();
 			outputErrorLog(_serverList, "setOpMin(): Error parsing argument for OPMIN.");
 			System.exit(-1);
 		}
 	}
 	
 	public static void setOpMax(ArrayList<ServerID> _serverList, int min, String str) {
 		int max = 0;
 		// Check arg for proper value, range
 		try {
 			max = Integer.parseInt(str);
 			if (max < min) {
 				System.err.println("Error in OPMAX. Please set a value equal to or greater than OPMIN.");
 				outputErrorLog(_serverList, "setOpMax(): Error in OPMAX.");
 				System.exit(-1);
 			}
 			maxOperations = max;
 		}
 		catch (Exception e) {
 			System.err.println("Error parsing argument for OPMAX. Please use a valid integer.");
 			argsError();
 			outputErrorLog(_serverList, "setOpMax(): Error parsing argument for OPMAX.");
 			System.exit(-1);
 		}
 	}
 	
 	public static void setVM(ArrayList<ServerID> _serverList, String str) {
 		int number = -1;
 		// Check arg for proper value, range
 		try {
 			number = Integer.parseInt(str);
 			if (number < 0 || number > 4) {
 				System.err.println("Error in VM. Please set a value in the range of 0 - 4.");
 				outputErrorLog(_serverList, "setVM(): Error in VM.");
 				System.exit(-1);
 			}
 			validationMode = number;
 		}
 		catch (Exception e) {
 			System.err.println("Error parsing argument for VM. Please use a valid integer.");
 			argsError();
 			outputErrorLog(_serverList, "setVM(): Error parsing argument for VM.");
 			System.exit(-1);
 		}
 	}
 
 	public static void setPush(ArrayList<ServerID> _serverList, String str) {
 		int number = -1;
 		// Check arg for proper value, range
 		try {
 			number = Integer.parseInt(str);
 			if (number < 0 || number > 5) {
 				System.err.println("Error in PUSH. Please set a value in the range of 0 - 5.");
 				outputErrorLog(_serverList, "setPush(): Error in PUSH.");
 				System.exit(-1);
 			}
 			policyPush = number;
 		}
 		catch (Exception e) {
 			System.err.println("Error parsing argument for PUSH. Please use a valid integer.");
 			argsError();
 			outputErrorLog(_serverList, "setPush(): Error parsing argument for PUSH.");
 			System.exit(-1);
 		}
 
 	}
 	
 	public static boolean parameterPush(ArrayList<ServerID> list) {
 		Socket socket;
 		ObjectOutputStream output;
 		ObjectInputStream input;
 		for (int i = 1; i <= maxServers; i++) {
 			try {
 				socket = new Socket(list.get(i).getAddress(), list.get(i).getPort());
 				// Set up I/O streams with the server
 				output = new ObjectOutputStream(socket.getOutputStream());
 				input = new ObjectInputStream(socket.getInputStream());
 				
 				Message msg = new Message("PARAMETERS " +
 										  proof + " " +
 										  validationMode + " " +
 										  policyPush);
 				output.writeObject(msg);
 				msg = (Message)input.readObject();
 				if (!msg.theMessage.equals("ACK")) {
 					System.err.println("Error: Incorrect ACK from " + socket.getInetAddress() +
 									   ":" + socket.getPort());
 					System.err.println("Could not push parameters to server " + i + ". Exiting.");
 					socket.close();
 					return false;
 				}
 				else { // Success
 					System.out.println("Parameters successfully pushed to server " + i + ".");
 				}
 				socket.close();
 			}
 			catch(ConnectException ce) {
 				System.out.println("** Connect Exception for " + list.get(i).getAddress() +
 								   ":" + list.get(i).getPort() +
 								   " - could not push parameters **");
 				return false;
 			}
 			catch(Exception e) {
 				System.err.println("Error during parameters push: " + e.getMessage());
 				e.printStackTrace(System.err);
 				return false;
 			}
 		}
 		return true;
 	}
 	
 	/**
 	 * Loads the configuration file for servers, giving Robot knowledge of
 	 * server addresses as well as its own
 	 *
 	 * @return boolean - true if file loaded successfully, else false
 	 */
 	public static ArrayList<ServerID> loadConfig(String filename) {
 		BufferedReader inputBuf = null;
 		String line = null;
 		ArrayList<ServerID> configList = new ArrayList<ServerID>();
 		
 		// use a try/catch block to open the input file with a FileReader
 		try {
 			inputBuf = new BufferedReader(new FileReader(filename));
 		}
 		catch (FileNotFoundException fnfe) {
 			// if the file is not found, exit the program
 			System.out.println("File \"" + filename + "\" not found.");
 			fnfe.printStackTrace();
 			return null;
 		}
 		// read a line from the file using a try/catch block
 		try {
 			line = inputBuf.readLine();
 		}
 		catch (IOException ioe) {
 			System.out.println("IOException during readLine().");
 			ioe.printStackTrace();
 			return null;
 		}
 		
 		while (line != null) {
 			if (line.charAt(0) != '#') { // not a comment line
 				try {
 					String triplet[] = line.split(" ");
 					configList.add(new ServerID(Integer.parseInt(triplet[0]),
 												triplet[1],
 												Integer.parseInt(triplet[2])));					
 				}
 				catch (Exception e) {
 					System.out.println("Error while parsing \"" + filename +
 									   "\".");
 					e.printStackTrace();
 					return null;
 				}
 			}
 			// get next line
 			try {
 				line = inputBuf.readLine();
 			}
 			catch (IOException ioe) {
 				System.out.println("IOException during readLine().");
 				ioe.printStackTrace();
 				return null;
 			}
 		}
 		
 		// close BufferedReader using a try/catch block
 		try {
 			inputBuf.close();
 		}
 		catch (IOException ioe) {
 			// if exception caught, exit the program
 			System.out.println("Error closing reader.");
 			ioe.printStackTrace();
 			return null;
 		}
 		
 		return configList;
 	}
 	
     /**
      * Output a file with the results of the simulation
      */
 	private static boolean outputLog() {
 		FileWriter outputFile = null;
 		BufferedWriter outputBuf = null;
 		String logID = Long.toString(new Date().getTime());
 		logID = "" + proof.charAt(0) + validationMode + policyPush + logID.substring(3);
		String filename = "Log_" + logID + ".txt";
 		boolean success = true;
 		long avgFullTxn = 0l; // Start to finish
 		long avgTxnTime = 0l; // Reads and writes only
 		long avgCommitTime = 0l; // From commit call to finish
 		
 		// Create an output stream
 		try {			
 			outputFile = new FileWriter(filename, true);
 			// create a BufferedWriter object for the output methods
 			outputBuf = new BufferedWriter(outputFile);
 		}
 		catch(IOException ioe) {
 			System.out.println("IOException during output file creation.");
 			ioe.printStackTrace();
 			success = false;
 		}
 		
 		// Write to file
 		try {
 			outputBuf.write("PARAMETERS:");
 			outputBuf.newLine();
 			outputBuf.write("PROOF=" + proof);
 			outputBuf.newLine();
 			outputBuf.write("VM=" + validationMode);
 			outputBuf.newLine();
 			outputBuf.write("PUSH=" + policyPush);
 			outputBuf.newLine();
 			outputBuf.write("MT=" + maxTransactions);
 			outputBuf.newLine();
 			outputBuf.write("OPMIN=" + minOperations);
 			outputBuf.newLine();
 			outputBuf.write("OPMAX=" + maxOperations);
 			outputBuf.newLine();
 			outputBuf.write("MS=" + maxServers);
 			outputBuf.newLine();
 			outputBuf.write("MD=" + maxDegree);
 			outputBuf.newLine();
 			outputBuf.write("LMIN=" + latencyMin);
 			outputBuf.newLine();
 			outputBuf.write("LMAX=" + latencyMax);
 			outputBuf.newLine();
 			outputBuf.write("SLEEP=" + threadSleep);
 			outputBuf.newLine();
 			outputBuf.write("ICSR=" + integrityCheckSuccessRate);
 			outputBuf.newLine();
 			outputBuf.write("LASR=" + localAuthSuccessRate);
 			outputBuf.newLine();
 			outputBuf.write("PMIN=" + policyUpdateMin);
 			outputBuf.newLine();
 			outputBuf.write("PMAX=" + policyUpdateMax);
 			outputBuf.newLine();
 			outputBuf.write("RS=" + randomSeed);
 			outputBuf.newLine();
 
 			/*** Output metrics ***/
 			// Total time of run
 			outputBuf.write("Total Time=" +
 							(TransactionLog.entry.get(maxTransactions).getEndTime() - TransactionLog.entry.get(1).getStartTime()));
 			outputBuf.newLine();
 			// Calculate averages
 			for (int i = 1; i <= maxTransactions; i++) {
 				avgFullTxn += TransactionLog.entry.get(i).getDuration();
 				avgTxnTime += TransactionLog.entry.get(i).getTxnTime();
 				avgCommitTime += TransactionLog.entry.get(i).getCommitTime();
 			}
 			outputBuf.write("Avg Full Txn Time=" + (avgFullTxn / maxTransactions));
 			outputBuf.newLine();
 			outputBuf.write("Avg Txn Time=" + (avgTxnTime / maxTransactions));
 			outputBuf.newLine();
 			outputBuf.write("Avg Commit Time=" + (avgCommitTime / maxTransactions));
 			outputBuf.newLine();
 			// Full data output
 			if (threadSleep) {
 				for (int i = 1; i <= maxTransactions; i++) {
 					outputBuf.write(TransactionLog.entry.get(i).getTxnNumber() + "\t" +
 									TransactionLog.entry.get(i).getTxn() + "\t" +
 									TransactionLog.entry.get(i).getStartTime() + "\t" +
 									TransactionLog.entry.get(i).getCommitStartTime() + "\t" +
 									TransactionLog.entry.get(i).getEndTime() + "\t" +
 									TransactionLog.entry.get(i).getDuration() + "\t" +
 									TransactionLog.entry.get(i).getTxnTime() + "\t" +
 									TransactionLog.entry.get(i).getCommitTime() + "\t" +
 									TransactionLog.entry.get(i).getStatus());
 					outputBuf.newLine();
 				}
 			}
 			else { // output sleep time in data
 				for (int i = 1; i <= maxTransactions; i++) {
 					outputBuf.write(TransactionLog.entry.get(i).getTxnNumber() + "\t" +
 									TransactionLog.entry.get(i).getTxn() + "\t" +
 									TransactionLog.entry.get(i).getStartTime() + "\t" +
 									TransactionLog.entry.get(i).getCommitStartTime() + "\t" +
 									TransactionLog.entry.get(i).getEndTime() + "\t" +
 									TransactionLog.entry.get(i).getDuration() + "\t" +
 									TransactionLog.entry.get(i).getTxnTime() + "\t" +
 									TransactionLog.entry.get(i).getCommitTime() + "\t" +
 									TransactionLog.entry.get(i).getSleepTime() + "\t" +
 									TransactionLog.entry.get(i).getStatus());
 					outputBuf.newLine();
 				}
 			}
 		}
 		catch(IOException ioe) {
 			System.out.println("IOException while writing to output file.");
 			ioe.printStackTrace();
 			success = false;
 		}
 		
 		// Close the output stream
 		try {
 			outputBuf.close();
 		}
 		catch(IOException ioe) {
 			System.out.println("IOException while closing the output file.");
 			ioe.printStackTrace();
 			success = false;
 		}
 		
 		return success;
 	}
 	
 	/**
      * Output a file if an error occurs.
      */
 	private static boolean outputErrorLog(ArrayList<ServerID> servers, String reason) {
 		FileWriter outputFile = null;
 		BufferedWriter outputBuf = null;
 		String logID = Long.toString(new Date().getTime());
 		logID = "" + proof.charAt(0) + validationMode + policyPush + logID.substring(3);
 		String filename = "Log_ERROR_" + logID + ".txt";
 		boolean success = true;
 		long avgFullTxn = 0l; // Start to finish
 		long avgTxnTime = 0l; // Reads and writes only
 		long avgCommitTime = 0l; // From commit call to finish
 		
 		// Create an output stream
 		try {			
 			outputFile = new FileWriter(filename, true);
 			// create a BufferedWriter object for the output methods
 			outputBuf = new BufferedWriter(outputFile);
 		}
 		catch(IOException ioe) {
 			System.out.println("IOException during output file creation.");
 			ioe.printStackTrace();
 			success = false;
 		}
 		
 		// Write to file
 		try {
 			outputBuf.write("ERROR:");
 			outputBuf.newLine();
 			outputBuf.write(reason);
 			outputBuf.newLine();
 			outputBuf.write("PARAMETERS:");
 			outputBuf.newLine();
 			outputBuf.write("PROOF=" + proof);
 			outputBuf.newLine();
 			outputBuf.write("VM=" + validationMode);
 			outputBuf.newLine();
 			outputBuf.write("PUSH=" + policyPush);
 			outputBuf.newLine();
 			outputBuf.write("MT=" + maxTransactions);
 			outputBuf.newLine();
 			outputBuf.write("OPMIN=" + minOperations);
 			outputBuf.newLine();
 			outputBuf.write("OPMAX=" + maxOperations);
 			outputBuf.newLine();
 			outputBuf.write("MS=" + maxServers);
 			outputBuf.newLine();
 			outputBuf.write("MD=" + maxDegree);
 			outputBuf.newLine();
 			outputBuf.write("LMIN=" + latencyMin);
 			outputBuf.newLine();
 			outputBuf.write("LMAX=" + latencyMax);
 			outputBuf.newLine();
 			outputBuf.write("SLEEP=" + threadSleep);
 			outputBuf.newLine();
 			outputBuf.write("ICSR=" + integrityCheckSuccessRate);
 			outputBuf.newLine();
 			outputBuf.write("LASR=" + localAuthSuccessRate);
 			outputBuf.newLine();
 			outputBuf.write("PMIN=" + policyUpdateMin);
 			outputBuf.newLine();
 			outputBuf.write("PMAX=" + policyUpdateMax);
 			outputBuf.newLine();
 			outputBuf.write("RS=" + randomSeed);
 			outputBuf.newLine();		}
 		catch(IOException ioe) {
 			System.out.println("IOException while writing to output file.");
 			ioe.printStackTrace();
 			success = false;
 		}
 		
 		// Close the output stream
 		try {
 			outputBuf.close();
 		}
 		catch(IOException ioe) {
 			System.out.println("IOException while closing the output file.");
 			ioe.printStackTrace();
 			success = false;
 		}
 		
 		// Shut down Policy Server, Cloud Servers
 		for (int i = 0; i <= maxServers; i++) {
 			try {
 				// Connect to the specified server
 				Socket sock = new Socket(servers.get(i).getAddress(),
 										 servers.get(i).getPort());
 				// Set up I/O streams with the server
 				ObjectOutputStream output = new ObjectOutputStream(sock.getOutputStream());
 				ObjectInputStream input = new ObjectInputStream(sock.getInputStream());
 				// Send KILL
 				output.writeObject(new Message("KILL"));
 				// Disconnect from server
 				sock.close();
 			}
 			catch (ConnectException ce) {
 				System.err.println(ce.getMessage() +
 								   ": Check server address and port number.");
 				ce.printStackTrace(System.err);
 			}
 			catch (Exception e) {
 				System.err.println("Error during KILL: " + e.getMessage());
 				e.printStackTrace(System.err);
 			}
 		}
 		
 		
 		return success;
 	}
 
 }
