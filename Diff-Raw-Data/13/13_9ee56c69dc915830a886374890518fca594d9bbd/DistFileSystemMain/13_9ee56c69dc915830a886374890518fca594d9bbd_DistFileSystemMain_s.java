 /**
  * @author paul
  */
 
 
 package distmain;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.UnknownHostException;
 import java.util.Vector;
 
 import distclient.*;
 import distconfig.DistConfig;
 import distfilelisting.LocalPathList;
 import distfilelisting.UserManagement;
 import distnodelisting.NodeSearchTable;
 import distserver.Server;
 
 
 public class DistFileSystemMain {
 	
 	private String prompt = "%s : ";
 	private BufferedReader inStream;
 	private DistConfig distConfig = null;
 	private UserManagement userManage = null;
 	private NodeSearchTable nst = null;
 	
 	private String ipAddress = null;
 	private Thread thServ = null;
 	private Vector<Thread> backgrounded = new Vector<Thread>();
 	
 	public DistFileSystemMain () {
 		try {
 			inStream = new BufferedReader(new InputStreamReader(System.in));
 			distConfig = DistConfig.get_Instance();
 			userManage = UserManagement.get_Instance();
 			nst = NodeSearchTable.get_Instance();
 			
 			System.out.print("Username: ");
 			String userName = inStream.readLine();
 			userManage.set_ownUserName(userName);
 			
 			System.out.print("IP Of Net: ");
 			ipAddress = inStream.readLine();
 		} 
 		catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		this.start_server();
 
 		// If IP address was given, connect to existing network
 		// Otherwise this will 
 		if (!ipAddress.equals(""))
 			this.connect_to_network();
 		
 		this.run_interface();
 	}
 	
 	public void start_server() {
 		Server serv = new Server();
 		thServ = new Thread (serv);
 		thServ.start();
 	}
 	
 	public void connect_to_network() {
 		try {
 			int nextID;
 			String nextIP;
 			
 			// Connect to the network and get initial search location
 			System.out.printf("Connecting to %s\n", ipAddress);
 			Client cli = new Client();
 			ClntEnterNetwork cen = new ClntEnterNetwork(ipAddress, cli);
 			cen.run();
 			cen = null;
 		
 			nextID = cli.getServId();
 			nextIP = cli.getServIp();
 			
 			// Locate the servers location
 			cli = null;
 			cli = new Client();
 			ClntCheckPosition ccp = new ClntCheckPosition(nextIP, nextID, cli);
 			ccp.run();
 			ccp = null;
 			
 			String[] pred = cli.getPredecessor();
 			String[] succ = cli.getSuccessor();
 			
 			// Connect to the predecessor
 			cli = null;
 			cli = new Client();
 			System.out.printf("Connecting to predecessor %s\n", pred[1]);
 			ClntNewPredecessor cnp = new ClntNewPredecessor(pred[1], cli);
 			cnp.run();
 			cnp = null;
 			
 			// Connect to the successor
 			cli = null;
 			cli = new Client();
 			System.out.printf("Connecting to successor %s\n", succ[1]);
 			ClntNewSuccessor cns = new ClntNewSuccessor(pred[1], cli);
 			cns.run();
 			cns = null;
 			
 			// Send new node notification
 			cli = null;
 			cli = new Client();
 			System.out.printf("Sending the new node notification\n");
 			ClntNewNode cnn = new ClntNewNode(succ[1], cli);
 			Thread newnode = new Thread (cnn);
 			newnode.start();
 			backgrounded.add(newnode);
 			cli = null;
 			
 			System.out.println ("Connected to the network\n");
 			
 		} 
 		catch (Exception e) {
 			e.printStackTrace();
 			System.exit(-1);
 		}
 	}
 	
 	public void run_interface() {
 		String input;
 		boolean exit = false;
 		while (!exit) {
 			try {
 				System.out.printf(this.prompt, this.userManage.get_ownUserName());
 				input = inStream.readLine();
 				
				switch (input) {
				case "view predecessor":
 					System.out.printf("Predecessor ID = %s\n", this.nst.get_predecessorID());
 					System.out.printf("Predecessor IP = %s\n", this.nst.get_predecessorIPAddress());
					break;
					
				case "view node search table":
 					for(int index = 0; index < this.nst.size(); index++) {
 						System.out.printf("Entry: %d\tID: %s\tIP: %s\n",
 								index, this.nst.get_IDAt(index), this.nst.get_IPAt(index));
 					}
					break;
 				}
 			}
 			
 			catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	public static void main (String[] args) {
 		DistFileSystemMain dfsm = new DistFileSystemMain();
 	}
 }
 
