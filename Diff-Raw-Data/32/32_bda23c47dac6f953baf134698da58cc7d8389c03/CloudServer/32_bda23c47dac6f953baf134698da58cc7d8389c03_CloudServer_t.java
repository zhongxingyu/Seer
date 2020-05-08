 /**
  * File: CloudServer.java
  * @author: Tucker Trainor <tmt33@pitt.edu>
  *
  * Based on original code "EchoServer.java" by Adam J. Lee (adamlee@cs.pitt.edu) 
  *
  * A simple server class. Accepts client connections and forks
  * WorkerThreads to handle the bulk of the work.
  */
 
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.io.*;
 import java.util.ArrayList;
 
 public class CloudServer {
 	public static ArrayList<ServerID> serverList;
 	public int serverNumber;
 	private int serverPolicyVersion;
 	public static boolean verbose = false;
 	public static String proof;
 	public static int validationMode;
 	public static int policyPush;
 	public static int latencyMin;
 	public static int latencyMax;
 	public static boolean threadSleep;
 	public static float integrityCheckSuccessRate;
 	public static float localAuthSuccessRate;
 	public static float globalAuthSuccessRate;
 	
 	public CloudServer(int _serverNumber) {
 		serverNumber = _serverNumber;
 	}
 
     /**
      * Main routine. Loads the parameters and starts the server.
      */
     public static void main(String[] args) {
 		int serverNumber = 0;
 		// Get new server number from command line
 		if (args.length == 1) {
 			try {
 				serverNumber = Integer.parseInt(args[0]);
 			}
 			catch (Exception e) {
 				System.err.println("Error parsing argument. Please use valid integers.");
 				System.err.println("Usage: java CloudServer <Server Number>\n");
 				System.exit(-1);
 			}
 	    }
 		else if (args.length == 2) {
 			try {
 				serverNumber = Integer.parseInt(args[0]);
 				if (args[1].equalsIgnoreCase("V")) {
 					verbose = true;
 				}
 			}
 			catch (Exception e) {
 				System.err.println("Error parsing argument. Please use valid arguments.");
 				System.err.println("Usage: java CloudServer <Server Number> <V|v>\n");
 				System.exit(-1);
 			}
 	    }
 		
 		// Load server information from configuration file
 		serverList = loadConfig("serverConfig.txt");
 		if (serverList == null) {
 			System.err.println("Error loading configuration file. Exiting.");
 			System.exit(-1);
 		}
 		else {
 			System.out.println("Configuration file loaded.");
 		}
 		
 		// Load the parameters for this simulation
 		if (loadParameters("parameters.txt")) {
 			System.out.println("Parameters file read successfully.");
 		}
 		else {
 			System.err.println("Error loading parameters file. Exiting.");
 			System.exit(-1);
 		}
 
 		CloudServer server = new CloudServer(serverNumber);
 		// Set the currect policy on this server from the Policy Server
 		server.setPolicy(server.callPolicyServer());
 		if (server.serverPolicyVersion == 0) {
 			System.out.println("Error retrieving Policy Version from Policy Server");
 			System.exit(-1);
 		}
 
 		System.out.println("Server " + serverNumber + " is ready.");
 		// Start listening for client connections
 		server.start();
 	}
 	
     /**
      * Server start routine. Just a dumb loop that keeps accepting new
      * client connections.
      */
 	public void start() {
 		try {
 			// This is basically just listens for new client connections
 			final ServerSocket serverSock = new ServerSocket(serverList.get(serverNumber).getPort());
 			
 			// A simple infinite loop to accept connections
 			Socket sock = null;
			DeferredThread threadD = null;
			PunctualThread threadP = null;
			while(true) {
				// Accept an incoming connection
				sock = serverSock.accept();
				// Create a thread to handle this connection
				if (proof.equalsIgnoreCase("DEFERRED")) {
					threadD = new DeferredThread(sock, this);
					threadD.start();
 				}
				else if (proof.equalsIgnoreCase("PUNCTUAL")) {
					threadP = new PunctualThread(sock, this);
					threadP.start();
 				}
				// Additional proofs handling here
 			}
 		}
 		catch (Exception e) {
 			System.err.println("Error: " + e.getMessage());
 			e.printStackTrace(System.err);
 		}
     }
 	
 	public int getPolicy() {
 		return serverPolicyVersion;
 	}
 	
 	public void setPolicy(int update) {
 		serverPolicyVersion = update;
 		if (verbose) {
 			System.out.println("Server Policy Version updated to v. " + update);
 		}
 	}
 	
 	public int callPolicyServer() {
 		try {
 			// Connect to the Policy Server
 			final Socket policySocket = new Socket(serverList.get(0).getAddress(),
 												   serverList.get(0).getPort());
 			if (verbose) {
 				System.out.println("CloudServer " + serverNumber + " calling Policy Server");
 			}
 			// Set up I/O streams with the server
 			final ObjectOutputStream output = new ObjectOutputStream(policySocket.getOutputStream());
 			final ObjectInputStream input = new ObjectInputStream(policySocket.getInputStream());
 			// Send message
 			Message msg = new Message("POLICYREQUEST");
 			output.writeObject(msg);
 			// Receive response
 			msg = (Message)input.readObject();
 			if (msg.theMessage.equals("FAIL")) {
 				System.out.println("*** CloudServer Policy Request FAIL ***");
 			}
 			else {
 				policySocket.close();
 				return Integer.parseInt(msg.theMessage);
 			}
 		}
 		catch (Exception e) {
 			System.err.println("Error: " + e.getMessage());
 			e.printStackTrace(System.err);
 		}
 		return 0; // FAIL
 	}
 	
 	/**
 	 * Loads the configuration file for this server
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
 	
 	/**
      * Allows the Robot to shut down the server.
      */
 	public static void shutdownServer() {
 		System.exit(0);
 	}
 }
