 import java.io.IOException;
 import java.io.PrintWriter;
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.SocketException;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 
 /**
  * A Bayou replica.
  * 
  * @author jam4879
  *
  */
 
 public class Server {
 
 	private int sID;									//the server ID
 	private int port;									//this server's port number
 	private Socket sendsock;							//the socket used for connecting and sending to this server
 	private ServerSocket recvsock;						//socket to accept replica and client connections
 	private HashMap<Integer, Socket> socks;				//map of sID/ports -> replica sockets
 	private HashMap<Socket, PrintWriter> ostreams;		
 	private Playlist playlist;
 	private VersionVector versionVector;
 	private Log uncommittedWrites;						//a log of writes that haven't been committed
 	private Log committedWrites;						//a log of all writes that have not been garbage collected
 	private long largestCSN;
 	private boolean isPrimary;							//says whether this server is the primary
 	private ArrayList<Integer> lastContacts;
 	private boolean freeForEntropy;
 	private HashMap<Integer, Write> entropy_writes;
 	private int max_otherID;
 
 	public Server(int p, int sID)
 	{
 		this.sID = sID;
 		port = p;
 		isPrimary = false;
 		socks = new HashMap<Integer, Socket>();
 		ostreams = new HashMap<Socket, PrintWriter>();
 		playlist = new Playlist();
 		versionVector = new VersionVector();
 		uncommittedWrites = new Log();					//the tentative writes to this server
 		committedWrites = new Log();						//the stable writes that this server is aware of
 
 		max_otherID = -1;
 		largestCSN = 0;
 
 		lastContacts = new ArrayList<Integer>();
 		freeForEntropy = true;
 		try
 		{
 			recvsock = new ServerSocket(port);
 			addShutdownHooks(this);
 		}
 		catch(IOException e)
 		{
 			e.printStackTrace();
 		}
 		Runnable listener = new Runnable()
 		{
 			public void run()
 			{
 				listen();
 			}
 		};
 		Thread listenThread = new Thread(listener);	//this is to give control back to Runner
 		listenThread.start();
 	}
 
 	public void connectToServer(int otherPort, int otherID)
 	{
 		if(!(socks.containsKey(otherID) || socks.containsKey(otherPort))){
 			try {
 				sendsock = new Socket();
 				sendsock.connect(new InetSocketAddress(InetAddress.getLocalHost(), otherPort));
 				//System.out.println(this + ": connect to server " + otherPort);
 				PrintWriter dout = new PrintWriter(sendsock.getOutputStream(), true);
 				//dout.println("=====> " + this.recvsock.getLocalPort() + ": requesting ACK from " + otherPort);
 				//sendsock.getOutputStream().write((this + ": requesting ACK from " + otherPort).getBytes());
 				dout.println("serverConnect " + sID);
 				socks.put(otherID, sendsock);
 				ostreams.put(sendsock, dout);
 				new ReplicaThread(this, sendsock);
 			} catch (UnknownHostException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	public void closeConnectionTo(int otherPort)
 	{
 		try {
 			Socket temp = socks.remove(otherPort);
 			if(temp != null)
 			{
 				ostreams.remove(temp).close();
 				temp.close();
 			}	
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	private void listen()
 	{
 		while(true)
 		{
 			Socket sock;
 			try
 			{
 				System.out.println(this + " is waiting for connections");
 				sock = recvsock.accept();
 				System.out.println(this + " receiving connection from " + sock.getPort());
 				PrintWriter dout = new PrintWriter(sock.getOutputStream(), true);
 				//dout.println("=======> I am " + recvsock.getLocalPort());
 				ostreams.put(sock, dout);
 				socks.put(sock.getPort(), sock);
 				new ReplicaThread(this, sock);
 				dout.println("serverConnect " + sID);
 			}
 			catch(SocketException e)
 			{
 				System.exit(0);
 			}
 			catch(IOException e)
 			{
 				e.printStackTrace();
 			}
 		}
 	}
 
 	private void addShutdownHooks(final Server server)
 	{
 		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable()
 		{
 			public void run()
 			{
 				server.shutdown();
 			}
 		}));
 	}
 
 	private void shutdown()
 	{
 		try 
 		{
 			recvsock.close();
 		}
 		catch(SocketException e)
 		{
 			e.printStackTrace();
 		}
 		catch(IOException e)
 		{
 			e.printStackTrace();
 		}
 	}
 
 	public void printLog() {
 		System.out.println("Committed writes for server " + sID + committedWrites);
 		System.out.println("Uncommitted writes for server " + sID + uncommittedWrites);
 	}
 
 
 	public void printPlaylist() {
 		System.out.println(playlist);
 		System.out.println("=======================");
 		System.out.println("Version Vector: \n" + versionVector.toStringForTesting());
 	}
 
 	public String toString()
 	{
 		return "Server on port " + port;
 	}
 
 	public synchronized void addToPlaylist(String song, String url) 
 	{
 		long acceptStamp = System.currentTimeMillis();
 		Write w;
 		if(isPrimary)
 		{
 			largestCSN = Math.max(acceptStamp, largestCSN+1);
 			w = new Write(acceptStamp, largestCSN, sID, isPrimary, "add", song, url);
 			committedWrites.log(w);
 		}
 		else
 		{
 			w = new Write(acceptStamp, Constants.INFINITY, sID, isPrimary, "add", song, url);
 			uncommittedWrites.log(w);
 		}
 		versionVector.changeLatestAccept(sID, acceptStamp);
 
 		if(!playlist.containsSong(song)) {
 			playlist.add(song, url);
 		}
 		else {
 			System.out.println("---------------> Song already exists!");
 		}
 	}
 
 
 	public synchronized void editPlaylist(String song, String url) {
 		long acceptStamp = System.currentTimeMillis();
 		Write w; 
 		if(isPrimary)
 		{
 			largestCSN = Math.max(acceptStamp, largestCSN+1);
 			w = new Write(acceptStamp, largestCSN, sID, isPrimary, "edit",song, url);
 			committedWrites.log(w);
 		}
 		else
 		{
 			w = new Write(acceptStamp, Constants.INFINITY, sID, isPrimary, "edit", song, url);
 			uncommittedWrites.log(w);
 		}
 		versionVector.changeLatestAccept(sID, acceptStamp);
 		playlist.edit(song, url);
 	}
 
 	public synchronized void deleteFromPlaylist(String song) {
 		long acceptStamp = System.currentTimeMillis();
 		Write w;
 		if(isPrimary)
 		{
 			largestCSN = Math.max(acceptStamp, largestCSN+1);
 			w = new Write(acceptStamp, largestCSN, sID, isPrimary, "delete", song, null);
 			committedWrites.log(w);
 		}
 		else
 		{
 			w = new Write(acceptStamp, Constants.INFINITY, sID, isPrimary, "delete", song, null);
 			uncommittedWrites.log(w);
 		}
 		versionVector.changeLatestAccept(sID, acceptStamp);
 
 		if(playlist.containsSong(song)) {
 			playlist.delete(song);		
 		}
 		else {
 			System.out.println("--------------------> Song does not exist!");
 		}
 	}
 
 	public String getPlaylistString()
 	{
 		return playlist.toString();
 	}
 
 	/**
 	 * The primary stabilizes all writes up to the earliest acceptStamp in its version vector.
 	 */
 	public synchronized void stabilizeWrites()
 	{
 		if(!isPrimary)
 			return;
 		//TODO
 	}
 
 	public void makePrimary() {
 		isPrimary = true;
 		//TODO
 	}
 
 	public void print(String msg)
 	{
 		System.out.println(msg);
 	}
 
 	public void sendMessageToServer(int otherID, String message)
 	{
 		System.out.println(message);
 		ostreams.get(socks.get(otherID)).println(message);
 	}
 
 	public void addConnectionToMap(Integer otherID, Socket sock) {
 		socks.remove(sock.getPort());
 		socks.put(otherID, sock);
 
 		if(this.max_otherID < otherID) {
 			this.max_otherID = otherID;
 		}
 	}
 
 	public void printConnections() {
 		System.out.println("Server" + this.sID  + " connections");
 		for(Integer sID : socks.keySet())
 			System.out.println(sID + " -> " + " remote port: " + socks.get(sID).getPort() + " local port " + socks.get(sID).getLocalPort());
 	}
 
 	public void isolate()
 	{
 		for(Socket sock : socks.values())
 		{
 			ostreams.get(sock).close();
 			try {
 				sock.close();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		socks.clear();
 		ostreams.clear();
 
 	}
 
 	public void breakConnectionWith(Integer sB) {
 		Socket sockB = socks.remove(sB);
 		if(sockB != null)
 			ostreams.remove(sockB).close();
 		else
 			System.out.println("failure in breakconnection");
 	}
 
 	public synchronized void setFreeForEntropy(boolean freeness) {
 		freeForEntropy = freeness;
 	}
 
 	//Sender calls this
 	public synchronized void startEntropySession() {
 		if(freeForEntropy) {
 
 			if(lastContacts.size() == socks.keySet().size()) {
 				lastContacts.clear();
 			}
 
 			for(Integer i: socks.keySet()) {
 				if(i <= this.max_otherID && !lastContacts.contains(i)){
 					lastContacts.add(i);
 					break;
 				}	
 			}
 
 			if(lastContacts.size() > 0) {
 				Socket socket = socks.get(lastContacts.get(lastContacts.size() - 1));
 
 				if(socket != null) {
 					PrintWriter dout = (PrintWriter)ostreams.get(socket);
 
 					String msg = Constants.begin_entropySession_msg + " " + this.sID;
 					dout.println(msg);
 				}
 				else {
 					System.out.println("No contact found for new Entropy session --> startEntropySession");
 				}
 			}
 			else {
 				System.out.println("---------------> No other replica is connected!");
 			}
 		}
 	}
 
 	//Receiver does this
 	public synchronized void sendResponseToEntropyRequest(Socket otherSock) {
 		PrintWriter dout = (PrintWriter)ostreams.get(otherSock);
 		String msg;
 
 
 		if(freeForEntropy) {
 
 			msg = Constants.entropyAck_msg + " " + this.sID + " " + this.largestCSN + " " + this.versionVector.toString();
 			//			this.entropy_msg_num = 0;	
 			entropy_writes = new HashMap<Integer, Write>();
 			setFreeForEntropy(false);
 		}
 		else {
 			msg = Constants.entropyReject_msg + " " + sID;
 		}
 
 		dout.println(msg);
 	}
 
 	public synchronized void basic_anti_entropy_protocol(Socket receiver_sock, VersionVector r_vector, long r_csn) {
 		int msg_num = 1;
 		Iterator<Write> it; 
 		Write nextWrite;
 		
 		if(r_csn < this.largestCSN) {
 			it = committedWrites.iterator();
 
 			if(it.hasNext()) {
 				while(it.hasNext()) {
 					nextWrite = it.next();
 
 					if(nextWrite.getAcceptStamp() <= r_vector.getStamp(nextWrite.getServerId())) {
 						sendCommitNotification(receiver_sock, nextWrite, msg_num);
 					}
 					else {
 						sendWrite(receiver_sock, nextWrite, msg_num);
 					}
 
 					msg_num++;
 				}
 
 				//				msg_num--;
 
 				//				PrintWriter dout = (PrintWriter)ostreams.get(receiver_sock);
 				//				String msg = "Commit_Write_Finished " + msg_num + " messages.";
 				//				dout.println(msg);				
 			}
 			else {
 				System.out.println("No Committed writes exists for server: " + sID);
 			}
 		}
 
 		it = uncommittedWrites.iterator();
 
 		if(it.hasNext()) {
 			while(it.hasNext()) {
 				nextWrite = it.next();
 
 				if(r_vector.getStamp(nextWrite.getServerId()) < nextWrite.getAcceptStamp()) {
 					sendWrite(receiver_sock, nextWrite, msg_num);
					msg_num++;
 				}
 
 			}
 		}
 		else {
 			System.out.println("No uncommitted writes exist for server: " + sID);
 		}
 		
 		msg_num--;
 		
 		PrintWriter dout = (PrintWriter)ostreams.get(receiver_sock);
 		String msg = Constants.entropy_finish_msg + " " + msg_num + " messages.";
 		dout.println(msg);	
 
 		setFreeForEntropy(true);
 	}
 
 	private synchronized void sendCommitNotification(Socket receiver_sock, Write update, int msg_num) {
 		PrintWriter dout = (PrintWriter)ostreams.get(receiver_sock);
 		String msg = Constants.commit_notification + " " + msg_num + " " + update.getAcceptStamp() + " " + update.getServerId() + " " + update.getCSN();
 		dout.println(msg);
 	}
 
 	public synchronized void sendWrite(Socket receiver_sock, Write update, int msg_num) {
 		PrintWriter dout = (PrintWriter)ostreams.get(receiver_sock);
 		String msg = Constants.server_write_msg + " " + msg_num + " " + update.toString();
 		dout.println(msg);
 	}
 
 	public synchronized void commitExistingWrite(int msg_num, long w_accept_stamp, int w_server_Id, long w_csn) {
 		Write existing_write = new Write(w_accept_stamp, w_csn, w_server_Id, true, null, null, null);
 
 		entropy_writes.put(msg_num, existing_write);
 	}
 
 	public synchronized void addServerWrite(int msg_num, Write update) {
 		entropy_writes.put(msg_num, update);
 	}
 
 	public synchronized void finishEntropyProcessing(int num_msgs, int otherID) {
 //		Write nextWrite;
 //		Write committedWrite;
 				
 		for(int i = 1; i<=num_msgs; i++) {
 			Write nextWrite = entropy_writes.get(i);
 			
 
 			if(nextWrite.isCommitted()) {
 				if(nextWrite.getUpdateCmd() != null) {
 					committedWrites.log(nextWrite);
 				}
 				else {
 					//committedWrite = committedWrites.removeWrite(nextWrite.getAcceptStamp(), nextWrite.getServerId());
 					Write committedWrite = uncommittedWrites.removeWrite(nextWrite.getAcceptStamp(), nextWrite.getServerId());
 					committedWrite.setAcceptStamp(nextWrite.getAcceptStamp());
 					committedWrite.setCSN(nextWrite.getCSN());
 					committedWrite.setSID(nextWrite.getServerId());
 					committedWrite.setCommitted(true);
 
 					committedWrites.log(committedWrite);
 				}
 				
 				largestCSN = nextWrite.getCSN();
 			}
 			else {
 				if(isPrimary) {
 					largestCSN = Math.max(System.currentTimeMillis(), largestCSN+1);
 					nextWrite.setCommitted(true);
 					nextWrite.setCSN(largestCSN);
 
 					committedWrites.log(nextWrite);
 				}
 				else {
 					uncommittedWrites.log(nextWrite);
 				}
 			}
 
 			versionVector.changeifMax(nextWrite.getServerId(), nextWrite.getAcceptStamp());
 		}
 
 		entropy_writes.clear();
 		setFreeForEntropy(true);
 		rePopulatePlaylist();
 	}
 
 	private void rePopulatePlaylist() {
 		playlist.clear();
 		Iterator<Write> it = committedWrites.iterator();
 
 		while(it.hasNext()) {
 			applyWrite(it.next());
 		}
 
 		it = uncommittedWrites.iterator();
 		while(it.hasNext()) {
 			applyWrite(it.next());
 		}
 	}
 
 	private void applyWrite(Write w) {
 		
 		if("add".equals(w.getUpdateCmd())) {
 			playlist.add(w.getSong(), w.getURL());
 		}
 		else if("edit".equals(w.getUpdateCmd())) {
 			playlist.edit(w.getSong(), w.getURL());
 		}
 		else if("delete".equals(w.getUpdateCmd())) {
 			playlist.delete(w.getSong());
 		}
 	}
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 }
