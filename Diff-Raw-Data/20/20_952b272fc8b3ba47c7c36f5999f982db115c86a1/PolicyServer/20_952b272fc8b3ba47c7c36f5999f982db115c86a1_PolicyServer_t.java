 /**
  * File: PolicyServer.java
  * @author: Tucker Trainor <tmt33@pitt.edu>
  *
  * A simple server class to simulate a Policy Server. Periodically sends policy
  * updates to active servers.
  */
 
 /* TODO: change listener to client, send off threads to all active cloud servers
  */
 
 import java.net.Socket;
 import java.io.FileReader;
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.*;
 
 public class PolicyServer {
 	final static int serverNumber = 0; // Default number for this server
 	static int maxServers;
 	static int minPolicyUpdateSleep;
 	static int maxPolicyUpdateSleep;
 	static int minPolicyPushSleep;
 	static int maxPolicyPushSleep;
 	static int policyVersion = 1;
 	
     public static void main(String[] args) {		
 		// Load the parameters file to get maximum number of servers
 		if (loadParameters("parameters.txt")) {
 			System.out.println("Parameters file loaded.");
 		}
 		else {
 			System.err.println("Error loading parameters file. Exiting.");
 			System.exit(-1);
 		}
 		// Load server information from configuration file
 		ArrayList<ServerID> serverList = loadConfig("serverConfig.txt");
 		if (serverList == null) {
 			System.err.println("Error loading configuration file. Exiting.");
 			System.exit(-1);
 		}
 		else {
 			System.out.println("Configuration file loaded. Policy Server is ready at " +
 							   serverList.get(serverNumber).getAddress() + ":" +
 							   serverList.get(serverNumber).getPort() + ".");
 		}
 		
		System.out.println("Policy version set to v. " + policyVersion);
 		PolicyThread thread = null;
 		
 		// Send initial round of policy versions to servers immediately
 		try {
 			for (int i = 1; i <= maxServers; i++) {
 				thread = new PolicyThread(policyVersion,
 										  serverList.get(i).getAddress(),
 										  serverList.get(i).getPort(),
 										  0,
 										  0);
 				thread.start();
 			}
 		}
 		catch(Exception e) {
 			System.err.println("Error: " + e.getMessage());
 			e.printStackTrace(System.err);
 		}
 
 		// Create and seed random number generator
 		Random generator = new Random(new Date().getTime());
 		
 		// Start updates
 		try {
 			// Loop periodic update pushes
 			while (policyVersion < Integer.MAX_VALUE) {
 				// Sleep
 				Thread.sleep(minPolicyUpdateSleep + generator.nextInt(maxPolicyUpdateSleep - minPolicyUpdateSleep));
 				// Update policy version
 				policyVersion++;
 				System.out.println("Policy version updated to v. " + policyVersion);
 				// Spread the word
 				for (int i = 1; i <= maxServers; i++) {
 					thread = new PolicyThread(policyVersion,
 											  serverList.get(i).getAddress(),
 											  serverList.get(i).getPort(),
 											  minPolicyPushSleep,
 											  maxPolicyPushSleep);
 					thread.start();
 				}
 			}
 		}
 		catch(Exception e) {
 			System.err.println("Error: " + e.getMessage());
 			e.printStackTrace(System.err);
 		}
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
 					if (Integer.parseInt(triplet[0]) == serverNumber) {
 						// Get minSleep, maxSleep
 						minPolicyUpdateSleep = Integer.parseInt(triplet[3]);
 						maxPolicyUpdateSleep = Integer.parseInt(triplet[4]);
 						minPolicyPushSleep = Integer.parseInt(triplet[5]);
 						maxPolicyPushSleep = Integer.parseInt(triplet[6]);
 					}
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
 					if (tuple[0].equals("MS")) {
 						maxServers = Integer.parseInt(tuple[1]);
 					}
 //					else if (tuple[0].equals("RS")) {
 //						randomSeed = Long.parseLong(tuple[1]);
 //					}
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
 }
