 package org.sjunion.trafficanalyzer;
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Scanner;
 
 import org.sjunion.trafficanalyzer.TrafficTree.TreeNode;
 
 /**
  * The main class for the traffic analyzer.
  * It merely serves to process user input
  * and do the initial analysis of the data
  * from WireShark.
  * @author cstech
  *
  */
 public class TrafficMain {
 
 	private static TrafficTree tree;
 	private static Scanner in;
 	/**
 	 * @param args
 	 * @throws IOException 
 	 */
 	public static void main(String[] args) throws IOException {
 		tree = new TrafficTree();
 		
 		if (args.length != 1) {
 			System.out.println("No file specified. Please supply a CSV" 
 					+ " file from a run of WireShark: " + args.length);
 			return;
 		}
 		
 		BufferedReader br = new BufferedReader(new FileReader(args[0]));
 		
 		String line = br.readLine();
 		
 		int i = 0;
 		System.out.print("\nAnalyzing");
 		while ((line = br.readLine()) != null) {
 			if (i % 5000 == 0) {
 				System.out.print(".");
 			}
 			tree.addNode(line);
 			i++;
 		}
 		br.close();
 		
 		System.out.println("\nIPs Logged: " + tree.size());
 		in = new Scanner(System.in);
 		String userInput = "";
 		while (!userInput.equals("5")) {
 			System.out.print("What would you like to do? \n"
 					+ "1. View Logged IP Addresses\n"
 					+ "2. View Traffic From A Specific IP Address\n"
 					+ "3. Find IPs Using A Specific Protocol\n"
 					+ "4. Find IPs Using A Dead Resource\n"
 					+  "5. Quit\n"
 					+ "> ");
 			userInput = in.next();
 			processInput(userInput);
 		}
 		System.out.print("Exiting...");
 		in.close();
 	}
 
 	/**
 	 * Processes user Input from main method and requests more as
 	 * needed.
 	 * @param userInput
 	 */
 	private static void processInput(String userInput) {
 		switch (userInput) {
 			case "1" : tree.printIPs();
 						break;
 			case "2" : 	System.out.print("IP > ");
 						String ip = in.next();
 						if (!TrafficTree.isValidIP(ip)) {
 							System.out.println("Invalid IP Format (Use IPv4)");
 						}
 						else {
 							TreeNode n = tree.searchTree(ip);
 							if (n == null) {
 								System.out.println("IP Not Found");
 							}
 							else {
 								System.out.println("IP Found");
 								System.out.println(n.toString());
 							}
 						}
 						break;
		    //TODO HEY
 			case "3" : System.out.print("Protocol > ");
 						String protocol = in.next();
 						ArrayList<TreeNode> list = tree.findProtocol(protocol);
 						if (list.size() == 0) {
							System.out.println("No IPs With That Protocol Found");
 						}
 						else {
 							System.out.println("Found These IP Addresses:");
 							for (int i = 1; i <= list.size(); i++) {
 								System.out.println("[" + i + "] " + list.get(i - 1).getAddress());
 							}
 						}
 						break;
 			case "4" :  System.out.print("Resource IP > ");
 						String address = in.next();
						ArrayList<TreeNode> oList = tree.findProtocol(address);
 						if (oList.size() == 0) {
							System.out.println("No IPs Access That Resource");
 						}
 						else {
 							System.out.println("Found These IP Addresses:");
 							for (int i = 1; i <= oList.size(); i++) {
								System.out.println("[i"  + "] " + oList.get(i - 1).getAddress());
 							}
 						}
 						break;	
 			default : break;
 		}
 	}
 }
