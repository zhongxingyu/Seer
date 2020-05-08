 package game;
 
 import java.util.*;
 import java.util.concurrent.locks.Condition;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 import java.io.*;
 import java.net.*;
 
 import com.google.gson.*;
 
 public class Server {
 
 	private ServerSocket serverSocket;
 	private ArrayList<Participant> participantList;
 	private int numberOfPlayers = 1;
 	// private String stateString = null;
 	private ServerGameState game;
 	private Gson json;
 	private StopWatch timer;
 	private int activePlayerCount = 0;
 	private Message message;
 	private Boolean resultsSent;
 	private final Lock lock;
 	private final Lock actPlLock;
 	private final Condition done;
 	private int count;
 	private boolean readReady;
 	private boolean writeReady;
 	private ServerRunnable[] runner;
 	private Thread[] threads;
 	private Boolean namesSent;
 	private String[] nameList;
 
 	Server(int port) throws IOException {
 		serverSocket = new ServerSocket(port);
 		participantList = new ArrayList<Participant>();
 		game = new ServerGameState();
 		json = new Gson();
 		timer = new StopWatch(20);
 		lock = new ReentrantLock();
 		actPlLock = new ReentrantLock();
 		done = lock.newCondition();
 		setCount(0);
 		runner = new ServerRunnable[4];
 		threads = new Thread[4];
 		setResultsSent(false);
 		setMessage(new Message(0,null));
 		setNamesSent(true);
 		System.out.println("Starting and listening on: "+getCurrentInetAddress()+":"+port);
 	}
 
 	public void setNamesSent(Boolean namesSent) {
 		this.namesSent = namesSent;
 	}
 
 	public Boolean getNamesSent() {
 		return this.namesSent;
 	}
 
 	public void setNumberOfPlayers(int numberOfPlayers) { // Proper functioning only guaranteed for >=1 value.
 		this.numberOfPlayers = (numberOfPlayers >= 1)? numberOfPlayers : 1;
 	}
 
 	public int getNumberOfPlayers() {
 		return numberOfPlayers;
 	}
 
 	public ServerSocket getServerSocket() {
 		return serverSocket;
 	}
 
 	public String getCurrentInetAddress(){
 		Enumeration<NetworkInterface> nets = null;
 		try {
 			nets = NetworkInterface.getNetworkInterfaces();//Get list of interfaces
 		} catch (SocketException e) {
 			nets = null;
 		}
 		if(nets != null){
 			for (NetworkInterface netint : Collections.list(nets)){
 				try {//Find the interface that is active and host is communicating on
 					if(netint.isUp() && !netint.isPointToPoint() && !netint.isVirtual() && !netint.isLoopback()){
 						Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
 						for (InetAddress inetAddress : Collections.list(inetAddresses)) {
 							String inet = inetAddress.toString();
 							inet = inet.substring(1);//Find the address on the interface we want that is "real" and IPv4.
 							if(inet.substring(0,7).compareTo("169.254") != 0 && !inet.contains(":")){
 								return inet;
 							}
 						}
 					}
 				} catch (SocketException e) {
 					System.err.println("Unable to get local address server is utilizing.");
 				}
 			}
 		}
 		else{
 			System.err.println("Unable to get local interface server is utilizing.");
 		}
 		return "Unknown.Address";//If we couldn't determine the address
 	}
 
 	public ArrayList<Participant> getParticipantList() {
 		return participantList;
 	}
 
 	public void setParticipantList(ArrayList<Participant> newParticipantList) {
 		participantList = newParticipantList;
 	}
 
 	public ServerGameState getGameState() {
 		return game;
 	}
 
 	public Gson getGson() {
 		return json;
 	}
 
 	public void setGson(Gson json) {
 		this.json = json;
 	}
 
 	public StopWatch getTimer() {
 		return timer;
 	}
 
 	public void setTimer(StopWatch timer) {
 		this.timer = timer;
 	}
 
 	// Reads new Controller objects from all participants in given list
 	public void readMessagesFromAll(ArrayList<Participant> aParticipantList) {
 		getLock().lock();
 		try{
 			while(getCount() > 0){
 				done.await();
 			}
 			setWReady(false);
 			setCount(getNumberOfPlayers());
 			setRReady(true);
 			done.signalAll();
 
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		} finally {
 			getLock().unlock();
 		}
 	}
 
 	public Message getMessage() {
 		return message;
 	}
 
 	public void setMessage(Message message) {
 		this.message = message;
 	}
 
 	// Writes the Server's message to all clients
 	public void writeMessageToAll(ArrayList<Participant> aParticipantList) {
 		getLock().lock();
 		try {
 			//System.out.println("readReady: " + readReady + "\nwriteReady: " + writeReady);
 			setCount(getNumberOfPlayers());
 			setWReady(true);
 			done.signalAll();
 		} finally {
 			getLock().unlock();
 		}
 	}
 
 	/*
 	// Updates messageFromClient for all participants.  The Server can interpret messages from the client.
 	public void readMessageFromAll(ArrayList<Participant> aParticipantList) {
 		for (Participant p: aParticipantList) {
 			if (p.isActive())
 				try {
 					p.readMessage();
 				}
 			catch (IOException e) {
 				System.err.println("Participant disconnected while reading message.  Set to inactive." + e.getMessage());
 				p.setActive(false);
 				setActivePlayerCount(getActivePlayerCount()-1);
 			}
 		}
 	}*/
 
 	// Writes the current game state to all clients as a JSON string
 	public void writeGameStateToAll(ArrayList<Participant> aParticipantList) { 
 		//System.out.println(json.toJson(getGameState().convert()));  //print content of each gamestate
 		String currentGameState = json.toJson(getGameState().convert());
 		for (Participant p: aParticipantList) {
 			if (p.isActive()) // Only try to write to active players; thread will be responsible for changing back to active on reconnect
 				try {
 					p.writeToClient(currentGameState);
 				}
 			catch (IOException e) {
 				System.err.println("Participant disconnected while writing game state.  Set to inactive. " + e.getMessage());
 				p.setActive(false);
 				setActivePlayerCount(getActivePlayerCount()-1);
 			}
 		}
 	}
 
 	// Generate a list of participants from network connection
 	public ArrayList<Participant> connectParticipants(int num) throws IOException {
 		Socket s = null;
 		ArrayList<Participant> newParticipantList = new ArrayList<Participant>();
 
 		// Try to accept a connection; if successful, add a Participant with that connected socket
 		for (int i=0;i<num;i++) { 
 			try {
 				s = getServerSocket().accept();
 			}
 			catch (IOException e) {
 				s = null;
 			}
 			if (s != null) {
 				RemoteParticipant p = new RemoteParticipant(s);
 				p.setPlayer(getGameState().addPlayer());
 				newParticipantList.add(p);
 				System.out.println("Player: "+(i+1)+" connected.");
 				setActivePlayerCount(getActivePlayerCount() + 1);
 			}
 		}
 		setNumberOfPlayers(newParticipantList.size());
 		getServerSocket().close(); // Stop accepting connections
 		return newParticipantList;
 	}
 
 	public int getActivePlayerCount() {
 		return activePlayerCount;
 	}
 
 	public void setActivePlayerCount(int activePlayerCount) {
 		this.activePlayerCount = activePlayerCount;
 	}
 
 	public void applyAllControls(ArrayList<Participant> aParticipantList) {
 		for (Participant p: aParticipantList) {
 			if (p.isActive())
 				getGameState().readControls(p);
 			else
 				getGameState().suspendPlayer(p);
 		}
 	}
 
 	public Boolean resultsSent() {
 		return resultsSent;
 	}
 
 	public void setResultsSent(Boolean truthValue) {
 		this.resultsSent = truthValue;
 	}
 
 	public Lock getLock() {
 		return lock;
 	}
 
 	public Lock getActPlLock() {
 		return actPlLock;
 	}
 
 	public Condition getCondition() {
 		return done;
 	}
 
 	public int getCount() {
 		return this.count;
 	}
 
 	public void setCount(int c) {
 		this.count = c;
 	}
 
 	public boolean getRReady() {
 		return readReady;
 	}
 
 	public void setRReady(boolean read) {
 		this.readReady = read;
 	}

 	public boolean getWReady() {
 		return writeReady;
 	}
 
 	public void setWReady(boolean write) {
 		this.writeReady = write;
 	}
 
 	public Thread getThread(int i){
 		return this.threads[i];
 	}
 
 	public void startThreads() {
 		for(int i = 0; i < getNumberOfPlayers(); i++){
 			runner[i] = new ServerRunnable(this, i);
 			threads[i] = new Thread(runner[i]);
 			this.getThread(i).start();
 		}
 	}
 
 	public void handleAllMessages(ArrayList<Participant> aParticipantList) {
 		getLock().lock();
 		try {
 			while(getCount() > 0){
 				done.await();
 			}
 			setRReady(false);
 
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		} finally {
 			getLock().unlock();
 		}
 		for (Participant p: aParticipantList) {
			if(p.isActive()){
				if (p.getMessageFromClient().getNumber() == 2) { // 2 indicates a name message
					p.setName(json.fromJson(p.getMessageFromClient().getMessage(),String.class));
					if (p.getName().compareTo("") == 0)
						p.setName("Player " + (aParticipantList.indexOf(p) + 1));
					setNamesSent(false);
				}
				else if (p.getMessageFromClient().getNumber() == 0) { // 0 indicates a Controller
					p.setController(json.fromJson(p.getMessageFromClient().getMessage(), Controller.class));
				}
 			}
 		}
 	}
 
 	public String[] getNameList(ArrayList<Participant> aParticipantList) {
 		nameList = new String[getNumberOfPlayers()];
 		for (int i=0;i<nameList.length;i++) {
 			nameList[i] = aParticipantList.get(i).getName();
 		}
 		return nameList;
 	}
 
 
 	// Main method
 	public static void main(String []args) {
 		int port = 5379;
 		int numberOfPlayers = 2;
 		if (args.length > 0){
 			numberOfPlayers = Integer.parseInt(args[0]); // args[0] is not just program name in Java
 			if (args.length > 1){
 				port = Integer.parseInt(args[1]); //port may be passed in as second arg
 			}
 		}
 		else {
 			System.out.println("Please enter the number of players:");
 			Scanner inputScanner = new Scanner(System.in);
 			numberOfPlayers = inputScanner.nextInt();
 		} 
 
 		Server theServer = null;
 		try {
 			theServer = new Server(port);
 		}
 		catch (Exception e) {
 			System.err.println("Could not start the server.");
 			System.exit(1);
 		}
 
 		theServer.setNumberOfPlayers(numberOfPlayers);
 
 		// Connect clients and adds them to the participantList
 		try {
 			theServer.setParticipantList(theServer.connectParticipants(theServer.getNumberOfPlayers()));
 		}
 		catch (Exception e) {
 			System.err.println("Error connecting client to server.\n");
 		}
 
 		theServer.startThreads();
 
 		// All participants should connected; begin communication cycle
 		while (theServer.getActivePlayerCount() > 0) {
 
 			theServer.getTimer().loopStart(); //log start time
 
 
 			theServer.readMessagesFromAll(theServer.getParticipantList()); // Reads updated controllers into all participants
 			theServer.handleAllMessages(theServer.getParticipantList()); // Handles all messages received

 			//theServer.getGameState().update(); // updates game state using game logic

 			if (theServer.getGameState().getEnd() == 1 && !theServer.resultsSent()) {
 				theServer.getMessage().setNumber(1);
 				theServer.getMessage().setMessage(theServer.json.toJson(theServer.getGameState().getResults()));
 				theServer.setResultsSent(true);
 			}
 			else if (theServer.getNamesSent() == false) { // The names need to be updated
 				theServer.getMessage().setNumber(2);
 				theServer.getMessage().setMessage(theServer.json.toJson(theServer.getNameList(theServer.getParticipantList())));
 				theServer.setNamesSent(true);
 			}	
 			else {
 				// GameState is updated by this point; send it to all
 				theServer.applyAllControls(theServer.getParticipantList()); // Applies controls for all participants
 				theServer.getGameState().update();
 				theServer.getMessage().setNumber(0);
 				theServer.getMessage().setMessage(theServer.json.toJson(theServer.getGameState().convert()));
 			}
 			theServer.writeMessageToAll(theServer.getParticipantList());
 			//theServer.writeGameStateToAll(theServer.getParticipantList());
 
 			theServer.getTimer().loopRest(); //rest until loop end	
 
 		}
 		System.out.println("All clients disconnected.  Server going offline.");
 		System.exit(0);
 
 	}
 }
