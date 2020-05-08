 /**
  * File: PolicyServer.java
  * @author: Tucker Trainor <tmt33@pitt.edu>
  *
  * A server class to simulate a Policy Server. Periodically sends policy
  * updates to active servers, listens for requests for current Policy version.
  */
 
 import java.net.Socket;
 import java.net.ServerSocket;
 import java.io.*;
 import java.util.*;
 
 public class PolicyServer {
 	final static int serverNumber = 0; // Default number for this server
 	static int maxServers;
 	static int latencyMin;
 	static int latencyMax;
 	static int policyUpdateMin;
 	static int policyUpdateMax;
 	static ArrayList<ServerID> serverList;
 	static boolean verbose = false;
 
 	public PolicyServer() {
 	}
 
     public static void main(String[] args) {	
 		if (args.length == 1) {
			if (args[1].equalsIgnoreCase("V")) {
 				verbose = true;
 			}
 			else {
 				System.err.println("Error parsing argument. Please use valid argument.");
 				System.err.println("Usage: java PolicyServer <V|v>\n");
 				System.exit(-1);
 			}
 	    }
 
 		// Load the parameters file to get maximum number of servers
 		if (loadParameters("parameters.txt")) {
 			System.out.println("Parameters file loaded.");
 		}
 		else {
 			System.err.println("Error loading parameters file. Exiting.");
 			System.exit(-1);
 		}
 		// Load server information from configuration file
 		serverList = loadConfig("serverConfig.txt");
 		if (serverList == null) {
 			System.err.println("Error loading configuration file. Exiting.");
 			System.exit(-1);
 		}
 		else {
 			System.out.println("Configuration file loaded. Policy Engine is ready.");
 		}
 		
 		PolicyServer server = new PolicyServer();
 		
 		// Allow disabling of policy updater by setting update frequencies to 0
 		if ((policyUpdateMin + policyUpdateMax) > 0) {
 			// Launch the Policy Updater
 			try {
 				PolicyUpdater puthread = new PolicyUpdater(server);
 				puthread.start();
 			}
 			catch(Exception e) {
 				System.err.println("Error: " + e.getMessage());
 				e.printStackTrace(System.err);
 			}
 		}
 
 		server.start(server);
     }
 	
 	public void start(PolicyServer server) {
 		try {
 			// Create and seed random number generator
 			Random generator = new Random(new Date().getTime());
 			
 			// This is basically just listens for new client connections
 			final ServerSocket serverSock = new ServerSocket(serverList.get(serverNumber).getPort());
 			
 			// A simple infinite loop to accept connections requesting current
 			// Policy version
 			Socket sock = null;
 			PolicyRequestThread prthread = null;
 			while(true) {
 				// Accept an incoming connection
 				sock = serverSock.accept();
 				// Create a thread to handle this connection
 				if (latencyMax > latencyMin) { // Range of latency values
 					prthread = new PolicyRequestThread(server, sock, (latencyMin + generator.nextInt(latencyMax - latencyMin)));
 				}
 				else if (latencyMax == 0) { // No artificial latency
 					prthread = new PolicyRequestThread(server, sock, 0);
 				}
 				else { // Fixed latency value
 					prthread = new PolicyRequestThread(server, sock, latencyMax);
 				}
 				prthread.start();
 			}
 		}
 		catch(Exception e) {
 			System.err.println("Error: " + e.getMessage());
 			e.printStackTrace(System.err);
 		}
 	}
 	
 	/**
 	 * Loads the configuration file for this engine
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
      * Load a file containing the parameters and applicable data for the engine
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
 					if (tuple[0].equals("MS")) {
 						maxServers = Integer.parseInt(tuple[1]);
 					}
 					else if (tuple[0].equals("LMIN")) {
 						latencyMin = Integer.parseInt(tuple[1]);
 					}
 					else if (tuple[0].equals("LMAX")) {
 						latencyMax = Integer.parseInt(tuple[1]);
 					}
 					else if (tuple[0].equals("PMIN")) {
 						policyUpdateMin = Integer.parseInt(tuple[1]);
 					}
 					else if (tuple[0].equals("PMAX")) {
 						policyUpdateMax = Integer.parseInt(tuple[1]);
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
