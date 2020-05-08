 package de.tum.in.dbs.project.wis;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Main class to start the benchmark client
  */
 public class Main {
 
 	/**
 	 * @param 1st param: CSV file with urls and amount 2nd param: delimiter of
 	 *        the csv file 3rd param: Sleep time (in seconds) 4th param: Amount
 	 *        of simulated clients 5th param: Amount of website calls per client
 	 * 
 	 */
 	public static void main(String[] args) {
 		// Check the params
 		if (args.length != 5) {
 			printUsage();
 			return;
 		}
 		String fileName = "";
 		char delimiter;
 		int sleepTimeSec = 0;
 		int amountClients = 0;
 		int amountCalls = 0;
 		try {
 			fileName = args[0];
 			delimiter = args[1].charAt(0);
 			sleepTimeSec = Integer.valueOf(args[2]);
 			amountClients = Integer.valueOf(args[3]);
 			amountCalls = Integer.valueOf(args[4]);
 		} catch (NumberFormatException nfe) {
 			printUsage();
 			return;
 		}
 
 		// Log welcome
 		System.out
 				.println("---------------------------------------------------------------");
 		System.out.println("Welcome to the WIS Benchmark Client.");
 		System.out
 				.println("---------------------------------------------------------------");
 
 		// Read the configuration file
 		File f = new File(fileName);
 		if (!f.canRead()) {
 			System.out.println("Can not read the configuration file.");
 			System.out.println("Aborted.");
 			return;
 		}
 
 		// Parse the configuration file
 		List<String> websites = ConfigurationParser.parseConfiguration(f,
 				delimiter);
 
 		// Initialize the stats
 		Stats.initialize(websites, amountClients, amountCalls);
 
 		// Start the clients
 		startClients(websites, amountClients, sleepTimeSec, amountCalls);
 	}
 
 	/**
 	 * Print usage to the console
 	 */
 	private static void printUsage() {
 		System.out
 				.println("---------------------------------------------------------------");
 		System.out
 				.println("Please start the WIS Benchmark Client with the following parameters:");
 		System.out.println("1st parameter: CSV file with urls and amount");
 		System.out.println("2nd parameter: Delimiter of the CSV file");
 		System.out.println("3rd parameter: Sleep time (in seconds)");
 		System.out.println("4th parameter: Amount of simulated clients");
 		System.out.println("5th parameter: Amount of website calls per client");
 		System.out
 				.println("---------------------------------------------------------------");
 	}
 
 	/**
 	 * Start the clients. For each client one thread will be started.
 	 * 
 	 * @param websites
 	 *            list with websites
 	 * @param amountClients
 	 *            amount of clients
 	 * @param sleepTimeSec
 	 *            sleeptime in seconds
 	 * @param amountCalls
 	 *            amount of website calls per client
 	 */
 	private static void startClients(List<String> websites, int amountClients,
 			int sleepTimeSec, int amountCalls) {
 		System.out.println("Start the clients ...");
 		for (int i = 1; i <= amountClients; i++) {
 			// Create a copy of the website list for the client
 			List<String> websitesForClient = new ArrayList<String>();
 			for (String website : websites) {
 				websitesForClient.add(website);
 			}
 
 			// Start the client
 			Client client = new Client(i, websitesForClient, sleepTimeSec,
 					amountCalls);
 			client.start();
 		}
 	}
 
 }
