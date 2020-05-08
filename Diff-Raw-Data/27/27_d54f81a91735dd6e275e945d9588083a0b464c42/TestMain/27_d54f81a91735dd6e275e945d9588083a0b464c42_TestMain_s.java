 package ece454p1;
 
 import java.util.*;
 import java.util.Map.Entry;
 import java.net.*;
 import java.io.*;
 
 public class TestMain {
 
 	public static InetAddress localAddress;
 	public static int localPort;
 	
 	static String peersFilename = null;
 	static int peerNumber = -1;
 
 	private static void printUsageAndQuit(int code) {
 		System.out.println();
 		System.out.println("Usage:");
 		System.out.println("\tjava ece454p1.TestMain peers-file peer-number");
 		System.out.println();
 		System.exit(code);
 	}
 	
 	private static void getMoreInput(){
 		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
 		System.out.print("Input PeerFile: ");
 		try {
 			peersFilename = br.readLine();
 		} catch (IOException e) {
 			System.out.println("Error parsing peerName");
 			System.exit(1);
 		}
 		System.out.print("Input PeerNumber: ");
 		try {
 			peerNumber = Integer.parseInt(br.readLine());
 		} catch (IOException e) {
 			System.out.println("Error parsing peerNumber");
 			System.exit(1);
 		}
 	}
 
 	/**
 	 * This is our simple command line test harness
 	 * It has usage of java ece454p1.TestMain peers-file peer-number
 	 * where peers-file is the path to the peers.txt
 	 * and peer-number is the line number of ip and port of local machine
 	 * 
 	 * peer number is used to make the local instances to play nicely
 	 * originally, the ip address was used to determine the local machine
 	 * 
 	 * Available commands to the harness are:
 	 * {insert, query, join, leave} for testing the peer API
 	 * insert (filename) will tell peer to insert the filename
 	 * query will create new Status object to be altered by peer
 	 * join will tell peer to join the pool and signal other peers
 	 * leave will tell peer to  leave the pool and notify other peers
 	 * join and leave will fail if you are already connected/disconnected
 	 * {show, set, exit} to make development easier easier
 	 * show will show the online and off-line status of proxy peers
 	 * set will change the local port of the peer
 	 * exit will end the test harness (doesn't system.exit)
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		// parse args
 		for (int i = 0; i < args.length; i++) {
 			if (i == args.length - 2) { // Peers file
 				peersFilename = args[i];
 			} else if (i == args.length - 1) { // Peer number
 				try {
 					peerNumber = Integer.parseInt(args[i]);
 				} catch (NumberFormatException e) {
 					System.out.println("Error parsing peer number");
 				}
 			} else {
 				System.out.println("Unknown arg: " + args[i]);
 			}
 		}
 
 		if (peersFilename == null || peerNumber == -1) {
 			System.out.println("Error: Missing peers file or peer number.");
 			getMoreInput();
 		}
 
 		// Parse peers file
 		ArrayList<ProxyPeer> proxyPeerList = new ArrayList<ProxyPeer>();
 		RandomAccessFile peersIn = null;
 		try {
 			peersIn = new RandomAccessFile(peersFilename, "r");
 
 			int lineNumber = 0;
 			String line;
 			while ((line = peersIn.readLine()) != null) {
 				String[] items = line.split(" ");
 				if (items.length != 2) {
 					System.out.println("Error parsing peers file");
 					System.exit(1);
 				}
 
 				InetAddress address = null;
 				try {
 					address = InetAddress.getByName(items[0]);
 				} catch (UnknownHostException e) {
 					System.out.println("Error parsing peer address");
 					System.exit(1);
 				}
 
 				int port = -1;
 				try {
 					port = Integer.parseInt(items[1]);
 				} catch (NumberFormatException e) {
 					System.out.println("Error parsing port number");
 					System.exit(1);
 				}
 				if (lineNumber == peerNumber) {
 					localAddress = address;
 					localPort = port;
 				} else {
					proxyPeerList.add(new ProxyPeer(address, port, false));
 				}
 				lineNumber++;
 			}
 		} catch (IOException e) {
 			System.out.println("Error reading peers file");
 			System.exit(1);
 		} finally {
 			try {
 				if (peersIn != null)
 					peersIn.close();
 			} catch (IOException e) {
 
 			}
 		}
 
 		if (peerNumber < 0 || peerNumber > proxyPeerList.size()) {
 			System.out.println("Error: Peer number out of range");
 			System.exit(1);
 		}
 
 		// Startup info
 		System.out.println("We are "+localAddress.getHostAddress() + ":" + localPort);
 		System.out.println("Other peers:");
 		for (ProxyPeer p : proxyPeerList){
 			System.out.println("\t"+p.host.getHostAddress() + ":" + p.port);
 		}
 
 		// Create local peer
 		Peer peer = new Peer(proxyPeerList);
 		peer.localAddress = localAddress;
 		peer.localPort = localPort;
 		
 		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
 		String input = "";
 		while(true){
 			System.out.print("COMMANDS (insert, query, join, leave) PROMPT> ");
 			String argv[] = {};
 			try {
 				input = br.readLine();
 				if (input.split(" ").length > 1){
 					argv = input.split(" ");
 					input = argv[0];
 				}
 			} catch (IOException ioe) {
 			}
 			if (input.toLowerCase().equals("show")){
 				for(ProxyPeer pi : proxyPeerList){
 					System.out.println(pi.host+":"+pi.port+" is "+(pi.connected ? "online" : "offline"));
 				}
 			}else if (input.toLowerCase().equals("set")){
 				System.out.println("Change myPort to...");
 				String input2 = "";
 				try {
 					input2 = br.readLine();
 					peer.localPort = Integer.parseInt(input2);
 				} catch (IOException ioe) {
 				}		
 			}else if (input.toLowerCase().equals("insert")){
 				String input2 = "";
 				if (argv.length > 1){
 					input2 = argv[1];
 				}else{
 					System.out.println("what is the path of the file you want to insert?");
 					try {
 						input2 = br.readLine();
 					} catch (IOException ioe) {
 					}
 				}
 				System.out.println("Telling peer to insert "+input2);
 				peer.insert(input2);
 			}else if (input.toLowerCase().equals("insert")){
 				System.out.println("what is the path of the file you want to insert?");
 				String input2 = "";
 				try {
 					input2 = br.readLine();
 					System.out.println("Telling peer to insert "+input2);
 					peer.insert(input2);
 				} catch (IOException ioe) {
 				}
 			}else if(input.toLowerCase().equals("query")){
 				System.out.println("Telling peer to query...");
 				Status status = new Status();
 				peer.query(status);
 				System.out.println("Status contains info of "+status.numberOfFiles()+" files");
 			}else if(input.toLowerCase().equals("join")){
 				if(peer.join()== ReturnCodes.ERR_UNKNOWN_WARNING){
 					System.out.println("Peer is already connected. Leave first.");
 				}else{
 					System.out.println("Telling peer to join...");
 				}
 			}else if(input.toLowerCase().equals("leave")){
 				if(peer.leave()== ReturnCodes.ERR_UNKNOWN_WARNING){
 					System.out.println("Peer is already not connected. Join first.");
 				}else{
 					System.out.println("Telling peer to leave...");
 				}
 			}else if(input.toLowerCase().equals("exit")){
 				break;
 			}else{
 				System.out.println("Invalid command!");
 			}
 		}
 	}
 }
