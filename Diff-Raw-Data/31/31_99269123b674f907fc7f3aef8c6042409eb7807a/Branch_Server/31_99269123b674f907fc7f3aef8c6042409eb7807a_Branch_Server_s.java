 import java.io.*;
 import java.net.*;
 import java.util.*;
 
 
 public class Branch_Server {
 
 	public String name = "some branch";
 	private int local_time = 0;
 	public int port = 4444;
 	public ServerThread serverThread = new ServerThread(this);
 	public Branch branch = new Branch();
 	public int lastSnapshot = 0;
 
 	public HashMap<String, Account> accounts = new HashMap<String, Account>();
 	public HashMap<String, Branch> inNeighbors = new HashMap<String, Branch>();
 	public HashMap<String, Branch> outNeighbors = new HashMap<String, Branch>();
 	private HashMap<String, Snapshot> snapshots = new HashMap<String, Snapshot>();
 	private NetworkWrapper messages;
 
 	public Branch_Server(String name, int port) {
 		this.name = name;
 		this.port = port;
 
 		branch.name = name;
 		branch.ServPort = port;
		branch.GUIPort = 12345;
 
 		System.out.println("Branch name: " + name);
 		serverThread.port = port;
 		serverThread.name = name;
 		
 
 		System.out.println("outNeighbors is " + outNeighbors.toString());
 		messages = new NetworkWrapper(outNeighbors);
 	}
 
 	/**
 	 * Adds a branch that can be communicated with
 	 */
 	public void addOutEdge(Branch branch) {
 		if (branch == null || outNeighbors.containsKey(branch.name) || this.name == branch.name) {
 			System.out.println("Not adding link");
 			return;
 		}
 
 		System.out.println("Adding link from " + this.name + " to " + branch.name);
 		outNeighbors.put(branch.name, branch);
 		return;
 	}
 
 	/**
 	 * Adds a branch that can communicate with this branch
 	 */
 	public void addInEdge(Branch branch) {
 		if (branch == null || inNeighbors.containsKey(branch.name) || this.name == branch.name) {
 			System.out.println("Not adding link");
 			return;
 		}
 
 		System.out.println("Adding link from " + branch.name + " to " + this.name);
 		inNeighbors.put(branch.name, branch);
 		return;
 	}
 
 	public void start() {
 		if (serverThread != null) {
 			Thread thread = new Thread(serverThread);
 			thread.start();
 		}
 		System.out.println("Done starting branch " + branch.name);
 	}
 
 	public String process_input(String input) {
 		String answer = "";
 
 		String[] tokens = input.split(" ");
 		String messageID = "";
 
 		char messageType = 0;
 		String command = "";
 		String accountID = "";
 		String arg4 = "";
 		String arg5 = "";
 
 		if (tokens.length > 0) {
 			messageID = tokens[0];
 
 			for (int i = 0; i < tokens.length; i++) {
 				tokens[i] = tokens[i].trim();
 			}
 
 			messageType = messageID.charAt(0);
 			messageID = messageID.substring(1); // strip message type character
 		}
 
 		if (tokens.length > 1)
 			command = tokens[1];
 
 		if (tokens.length > 2)
 			accountID = tokens[2];
 
 		if (tokens.length > 3)
 			arg4 = tokens[3];
 
 		if (tokens.length > 4)
 			arg5 = tokens[4];
 
 		if (messageType == 'c') {
 			// client request
 
 			System.out.println("Command is [" + command + "]");
 
 			if (command.equals("w")) {
 				++local_time;
 				answer = withdrawal(accountID, Float.parseFloat(arg4));
 			} else if (command.equals("d")) {
 				++local_time;
 				answer = deposit(accountID, Float.parseFloat(arg4));
 			} else if (command.equals("t")) {
 				++local_time;
 				answer = transfer(messageID, accountID, arg4, Float.parseFloat(arg5));
 			} else if (command.equals("s")) {
 				answer = startSnapshot();
 			} else if (command.equals("q")) {
 				++local_time;
 				answer = query(accountID);
 			}
 		} else if (messageType == 's') {
 			if (command.equals("m")) {
 				answer = markerMessage( tokens[2], tokens[3], tokens[4] );
 			}
 		}
 
 
 		answer = "s" + messageID + " " + answer;
 		return answer;
 	}
 
 	/** 
 	 * Start a snapshot
 	 */
 	public String startSnapshot() {
 		int snapshotID = lastSnapshot + 1;
 		lastSnapshot = snapshotID;
 		handleMarker(null, this.branch, snapshotID);
 		transmitMarker(this.branch.name, snapshotID);
 
 		return "ok";
 	}
 
 
 	/** 
 	 * Handles the reception of a snapshot marker
 	 *
 	 * @param sourceBranch The branch that sent the marker.
 	 * @param originBranch The branch that started the snapshot.
 	 * @param snapshotNumber The origin branch's snapshot number.
 	 */
 	public void handleMarker(Branch sourceBranch, Branch originBranch, int snapshotNumber) {
 		String snap_name = Snapshot.getName(originBranch,snapshotNumber);
 		Snapshot snap;
 		if ( !snapshots.containsKey(Snapshot.getName(originBranch,snapshotNumber)) ) {
 			//Store the state of the branch
 			Iterator it = accounts.values().iterator();
 			ArrayList<Account> values = new ArrayList<Account>();
 			while (it.hasNext()) {
 				values.add( new Account( (Account)it.next()) );
 			}
 			snap = new Snapshot(local_time, values, originBranch, snapshotNumber);
 			snapshots.put(snap.getName(), snap);
 		} else {
 			snap = snapshots.get(snap_name);
 		}
 		snap.addMarker(originBranch);
 		if ( snap.isFinished( new HashSet<Branch>(inNeighbors.values()) ) ) {
 			sendGUISnapshot(snap);
 			snapshots.remove(snap.getName());
 		}
 	}
 
 	/**
 	 * Notifies ongoing snapshots of transfers
 	 */
 	void notifySnapshots(Account source, Account destination, float amount) {
 		for (Snapshot snap : snapshots.values()) {
 			snap.addTransfer(source, destination, amount);
 		}
 	}
 
 	/**
 	 * Sends the snapshot information to the GUI
 	 */
 	void sendGUISnapshot(Snapshot snap) {
 		String message = snap.getMessage();
 		try {
 			serverThread.sendToGUI(message);
 		} catch (Exception e) {
 			System.err.println(e);
 		}
 	}
 
 	public String markerMessage(String arg1, String arg2, String arg3) {
 		String result = "";
 
 		Branch source = inNeighbors.get(arg1);
 		if (source == null)
 			return "invalidSource";
 
 		Branch origin = inNeighbors.get(arg2);
 		if (origin == null)
 			return "invalidOrigin";
 
 		int snapshotID = Integer.parseInt(arg3);
 
 		handleMarker(source, origin, snapshotID);
 
 		return "ok";
 	}
 
 	public String withdrawal(String accountID, float amount) {
 		String answer = "error";
 
 		if (!getBranchFromAccountID(accountID).equals(this.name))
 			return "wrongbranch";
 
 		checkOrCreateAccount(accountID);
 
 		if (amount < 0) {
 			answer = "error-invalid";
 			return answer;
 		}
 
 		Account account = accounts.get(accountID);
 
 		if (account == null)
 			return answer;
 
 		account.addBalance(-amount);
 	
 		answer = "ok";
 
 		return answer;
 	}
 
 	public String getBranchFromAccountID(String accountID) {
 		return Account.getBranchID(accountID);
 	}
 
 	public String deposit(String accountID, float amount) {
 		String answer = "error";
 
 		System.out.println("deposit");
 
 		if (!getBranchFromAccountID(accountID).equals(this.name))
 			return "wrongbranch " + getBranchFromAccountID(accountID) + ", " + this.name;
 
 		checkOrCreateAccount(accountID);
 		
 
 		if (amount < 0) {
 			answer = "error-invalid";
 			return answer;
 		}
 
 		Account account = accounts.get(accountID);
 
 		if (account == null)
 			return answer;
 
 		account.addBalance(amount);
 	
 		answer = "ok";
 
 		return answer;
 	}
 
 	public boolean validAccount(String accountID) {
 		return validAccount(accountID, false);
 	}
 
 	public boolean validAccount(String accountID, boolean mustBeLocal) {
 		String branchID = getBranchFromAccountID(accountID);
 		if (!mustBeLocal && !branchID.equals(this.name)) {
 			System.out.println("account " + accountID + " is in different branch (" + branchID + ", this is " + this.name + " ... do we have access?");
 			if (outNeighbors.containsKey(branchID)) {
 				System.out.println("Yes, because neighbors are " + messages.whoNeighbors());
 				return true; // remote branch that this one can talk to
 			}
 				System.out.println("No, neighbors are " + messages.whoNeighbors());
 		}
 
 		Account account = accounts.get(accountID);
 
 		return account != null;
 	}
 
 	public String transfer(String messageID, String srcAccountID, String dstAccountID, float amount) {
 		String answer = "error";
 
 		if (!getBranchFromAccountID(srcAccountID).equals(this.name))
 			return "wrongbranch " + getBranchFromAccountID(srcAccountID) + ", " + this.name;
 
 		// make sure source account exists locally
 		checkOrCreateAccount(srcAccountID);
 
 		// make sure destination account exists or belongs to a branch that can be contacted
 		if (!validAccount(dstAccountID)) {
 			answer = "error-to";
 			return answer;
 		}
 
 		Account accountFrom = accounts.get(srcAccountID);
 		Account accountTo = accounts.get(dstAccountID);
 
 		if (amount < 0) {
 			answer = "error-invalid";
 			return answer;
 		}
 
 		/*
 		if (accountFrom.getBalance() < amount) {
 			answer = "error-insufficient";
 			return answer;
 		}
 		*/
 
 		notifySnapshots(accountFrom, accountTo, amount);
 		accountFrom.addBalance(-amount);
 		if (validAccount(dstAccountID, true)) {
 			System.out.println("Local transfer of " + amount + " from " + srcAccountID + " to " + dstAccountID);
 			accountTo.addBalance(amount);
 		} else {
 			System.out.println("Transferring " + amount + " to " + dstAccountID);
 			sendTransfer(dstAccountID, messageID, amount);
 		}
 
 		answer = "ok";
 
 		return answer;
 	}
 
 	public void sendTransfer(String accountID, String messageID, float amount) {
 		String branchID = getBranchFromAccountID(accountID);
 		String message = "";
 		message = "c" + messageID + " d " + accountID + " " + amount;
 		messages.send( branchID, message );
 	}
 
 
 	public String query(String accountID) {
 		String answer = "error";
 
 		Account account = null;
 
 		if (!getBranchFromAccountID(accountID).equals(this.name))
 			return "wrongbranch";
 
 		checkOrCreateAccount(accountID);
 
 		account = accounts.get(accountID);
 
 		if (account == null)
 			return answer;
 
 		answer = "q" + " " + accountID + " " + Float.toString(account.getBalance());
 
 		return answer;
  	}
 
 	public void checkOrCreateAccount(String accountID) {
 		if (!validAccount(accountID)) {
 			Account account = new Account(accountID);
 			accounts.put(accountID, account);
 		}
 	}
 
 
 	/**
 	 * Send a marker to all out edges
 	 */
 	void transmitMarker(String originBranch, int snapshotID) {
 		String message = "s m " + name + " " + originBranch + " " + snapshotID;
 
 		for (Map.Entry<String, Branch> branch : outNeighbors.entrySet()) {
                 	String key = branch.getKey();
 			Branch outBranch = branch.getValue();
 
 	                messages.send( outBranch.name, message );
 		}
 	}
 
 }
 
 class Snapshot {
 	class Transfer {
 		Account source;
 		Account destination;
 		float amount;
 
 		public Transfer(Account src, Account dest, float moneys) {
 			source = src;
 			destination = dest;
 			amount = moneys;
 		}
 
 		public Transfer(Transfer t) {
 			source = t.source;
 			destination = t.destination;
 			amount = t.amount;
 		}
 	}
 
 	int local_time;
 	ArrayList<Account> accounts;
 	ArrayList<Transfer> transfers = new ArrayList<Transfer>();
 	HashSet<Branch> markers = new HashSet<Branch>();
 	public Branch origin;
 	public int number;
 	
 	Snapshot (int time, ArrayList<Account> accnts, Branch originBranch, int snapshotNumber) {
 		local_time = time;
 		accounts = accnts;
 		origin = originBranch;
 		number = snapshotNumber;
 	}
 
 	/**
 	 * Called when a marker message is received
 	 */
 	void addMarker(Branch sourceBranch) {
 		markers.add(sourceBranch);
 	}
 
 
 	/**
 	 * Called to notify the snapshot of a transfer
 	 */
 	void addTransfer(Account source, Account destination, float amount) {
 		transfers.add(new Transfer(source, destination, amount));
 	}
 
 	public boolean equals(Object o) {
 		if (o instanceof Snapshot) {
 			Snapshot other = (Snapshot)o;
 			if (other.origin.equals(origin) && other.number==number) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	String getMessage() {
 		String result = origin.name + "." + number + " b";
 		for (Account account : accounts) {
 			result += " " + account.id + " " + account.getBalance();
 		}
 		result += " p";
 		for (Transfer t : transfers) {
 			result += " " + t.source.id + " " + t.destination.id + " " + t.amount;
 		}
 		System.out.println(result);
 		return result;
 	}
 
 	public String getName() {
 		return getName(origin, number);
 	}
 	
 	public static String getName(Branch branch, int number) {
 		return branch.name + "." + number;
 	}
 
 	public int hashCode() {
 		return getName().hashCode();
 	}
 
 	/**
 	 * Returns true if all of the marker messages have been received
 	 */
 	public boolean isFinished(Set<Branch> inEdges) {
 		inEdges.removeAll(markers);
 		if (inEdges.isEmpty()) {
 			return true;
 		}
 		return false;
 	}
 }
 
 class ServerThread implements Runnable {
     protected DatagramSocket socket = null;
     protected BufferedReader in = null;
     protected boolean serverRunning = true;
 
     public String name;
     public int port;
     public Branch_Server thisBranch;
    InetAddress GUIAddress;
    int GUIPort;
 
     public ServerThread(Branch_Server branch) {
 	this(branch, "ServerThread", 4444);
     }
 
     public ServerThread(Branch_Server branch, int port) {
 	this(branch, "ServerThread", port);
     }
 
     public ServerThread(Branch_Server branch, String name, int port) { 
 	this.thisBranch = branch;
 	this.name = name;
 	this.port = port;
     }
 
     public void sendToGUI(String message) throws IOException {
        sendToGUI(message, GUIAddress, GUIPort);
     }
 
    public void sendToGUI(String message, InetAddress address, int port) throws IOException {
 	byte[] buf = new byte[message.length()+1];
 	buf = message.getBytes();
 	DatagramPacket packet = new DatagramPacket(buf, buf.length, address, port);
         socket.send(packet);
     }
 
     public void run() {
 	this.port = port;
 
 	try {
 		socket = new DatagramSocket(port);
 	} catch(Exception e) {
 
 	}
 
         while (serverRunning) {
             try {
                 byte[] inbuf = new byte[256];
 
                     // receive request
                 DatagramPacket packet = new DatagramPacket(inbuf, inbuf.length);
                 socket.receive(packet);
 
 		
 		// figure out response
 		String input = new String(inbuf);
                 String dString = thisBranch.process_input( input );
 
 		
 		System.out.println("Sending: " + dString);
 		
 
 		    // send the response to the client at "address" and "port"
                GUIAddress = packet.getAddress();
                GUIPort = packet.getPort();
		sendToGUI(dString);
             } catch (IOException e) {
                 e.printStackTrace();
 		serverRunning = false;
             }
 
 		try {
 			Thread.sleep(100);
 		} catch (Exception e) {
 		}
         }
 	System.out.println("Closing socket.");
         socket.close();
     }
 
 }
 
 
 class NetworkWrapper {
 	public HashMap<String, Branch> topology = new HashMap<String, Branch>();
 
 	public NetworkWrapper(HashMap<String, Branch> topology) {
 		this.topology = topology;
 	}
 
 	public boolean send(String branchID, String message) {
 		if (!topology.containsKey(branchID)) {
 			return false;
 		}
 
 		Branch branch = topology.get(branchID);
 		if (branch == null)
 			return false;
 		try {
 			DatagramSocket clientSocket = new DatagramSocket();
 			InetAddress IPAddress = InetAddress.getByName("localhost");
 			byte[] sendData = message.getBytes();
 
 			System.out.println("Sending (to branch): " + message);
 
 			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, branch.ServPort);
 			clientSocket.send(sendPacket);
 			clientSocket.close();
 		} catch( Exception e ) {
 			return false;
 		}
 		return true;
 	}
 
 	public String whoNeighbors() {
 		System.out.println("topology is " + topology.toString());
 
 		String result = "neighbors: ";
 		
 		Object[] keys = topology.keySet().toArray();
 		String key;
 		for (int i = 0; i < keys.length; i++) {
 			key = (String)keys[i];
 
 			result = result + key + " ";	
 		}
 
 		return result;
 	}
 }
