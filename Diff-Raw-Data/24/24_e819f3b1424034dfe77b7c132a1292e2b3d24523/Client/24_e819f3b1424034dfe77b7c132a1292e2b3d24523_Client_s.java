 package game;
 import java.util.*;
 import java.io.*;
 import java.net.*;
 
 import com.google.gson.*;
 
 
 public class Client {
 	
 	private Socket socket;
 	private String stateString;
 	private BufferedReader reader;
 	private BufferedWriter writer;
 	private ClientGameState game;
 	private Gson json;
 	private Controller controller;
 	private StopWatch timer;
 	private static int port = 5379;
 	private Message messageFromServer;
 	private Message messageToServer;
 	private GameResults gameResults;
 	private String name;
 	private Boolean nameSent;
 	private String[] nameList;
 	private View view;
 	
 	Client (InetAddress addr, int port, View v) throws IOException { 
 		setSocket(new Socket(addr,port)); // Establish connection
 		setStateString("Awaiting state from server.\n");
 		setReader(new BufferedReader(new InputStreamReader(getSocket().getInputStream())));
 		setWriter(new BufferedWriter(new OutputStreamWriter(getSocket().getOutputStream())));
 		setController(new Controller());
 		setView(v);
 		setTimer(new StopWatch(20));
 		setMessageFromServer(new Message(0,null));
 		setMessageToServer(new Message(0,null));
 		setGameResults(null);
 		setName("Player");
 		setNameSent(false);
 		
 		getView().attachController(getController());
 		json = new Gson();
 	}
 	
 	public void setNameList(String[] nameList) {
 		this.nameList = nameList;
 	}
 	
 	public String[] getNameList() {
 		return this.nameList;
 	}
 	
 	public void setNameSent(Boolean nameSent) {
 		this.nameSent = nameSent;
 	}
 	
 	public Boolean getNameSent() {
 		return this.nameSent;
 	}
 	
 	public void setName(String name) {
 		this.name = name;
 	}
 	
 	public String getName() {
 		return this.name;
 	}
 	
 	public void readStateString() throws IOException {
 		setStateString(getReader().readLine()); // Should block until line received from server
 	}
 	
 	public void setState(GameState g) throws Exception {
 		if (g == null)
 			throw new Exception("Null game state.");
 		
 		this.game = (ClientGameState) g; // Will be casting (ServerGameState) objects to ClientGameState
 	}
 	
 	public ClientGameState getState(){
 		return this.game;
 	}
 	
 	public void readGameState() throws IOException, Exception {
 		setState(json.fromJson(getReader().readLine(), ClientGameState.class));
 	}
 	
 	public void readMessageFromServer() throws IOException, Exception {
 		setMessageFromServer(json.fromJson(getReader().readLine(), Message.class));
 	}
 	
 	public Message getMessageFromServer() {
 		return this.messageFromServer;
 	}
 	
 	public Message getMessageToServer() {
 		return this.messageToServer;
 	}
 	
 	public void handleMessageFromServer() throws Exception {
 		//System.out.println(getMessageFromServer().getNumber());
 		if (getMessageFromServer().getNumber() == 0) {
 			setState(json.fromJson(getMessageFromServer().getMessage(), ClientGameState.class));
 		}
 		if (getMessageFromServer().getNumber() == 1) {
 			setGameResults(json.fromJson(getMessageFromServer().getMessage(),GameResults.class));
 			System.out.println("Game results received: " + getGameResults());
 		}
 		if (getMessageFromServer().getNumber() == 2) { // Name list received
 			setNameList(json.fromJson(getMessageFromServer().getMessage(), String[].class));
 			System.out.println("Names received:");
 			for (String s: getNameList()) {
 				System.out.println(s);
 			}
 		}
 	}
 	
 	public void setGameResults(GameResults gameResults) {
 		this.gameResults = gameResults;
 	}
 	
 	public GameResults getGameResults() {
 		return this.gameResults;
 	}
 	
 	public void writeToServer(String outbound) throws IOException {
 		getWriter().write(outbound);
 		getWriter().newLine();
 		getWriter().flush();
 	}
 	
 	public void setWriter(BufferedWriter writer) {
 		this.writer = writer;
 	}
 	
 	public BufferedWriter getWriter() {
 		return writer;
 	}
 	public void setReader(BufferedReader bufferedReader) {
 		this.reader = bufferedReader;
 	}
 	
 	public BufferedReader getReader() {
 		return this.reader;
 	}
 	
 	public String getStateString() {
 		return this.stateString;
 	}
 	
 	public void setStateString(String string) {
 		this.stateString = string;
 	}
 	
 	public void setSocket(Socket socket) {
 		this.socket = socket;
 	}
 	
 	public Socket getSocket() {
 		return this.socket;
 	}
 	
 	public Controller getController() {
 		return controller;
 	}
 	
 	public void setController(Controller c) {
 		this.controller = c;
 	}
  	
 	public void writeController() throws IOException {
 		writeToServer(json.toJson(getController())); // There is also a Gson method to directly write to a Writer
 	}
 
 	public View getView() {
 		return view;
 	}
 
 	public void setMessageFromServer(Message messageFromServer) {
 		this.messageFromServer = messageFromServer;
 	}
 	
 	public void setMessageToServer(Message messageToServer) {
 		this.messageToServer = messageToServer;
 	}
 	
 	public void setView(View v) {
 		this.view = v;
 	}
 	
 	public void updateController() {
 		getController().update();
 	}
 	public StopWatch getTimer() {
 		return timer;
 	}
 	public void setTimer(StopWatch timer) {
 		this.timer = timer;
 	}
 	
 	public void writeMessageToServer() throws IOException {
 		writeToServer(json.toJson(getMessageToServer()));
 	}
 	
 	public void play() throws Exception {
 		while (getSocket().isConnected() && getView().isVisible()) {
 			
 			getTimer().loopStart(); // Start the loop
 			
 			updateController(); // Update controller
 			
 			if (getNameSent() == false) {
 				//System.out.println("here for sending the name");
 				getMessageToServer().setNumber(2);
 				getMessageToServer().setMessage(json.toJson(getName()));
 				setNameSent(true);
 			}
 			else {
 				//System.out.println("here to send controller");
 				getMessageToServer().setNumber(0);
 				getMessageToServer().setMessage(json.toJson(getController()));
 			}
 			writeMessageToServer();
 			//writeController(); // Write controller to the server
 			readMessageFromServer();
 			handleMessageFromServer();
 			
 			//readGameState(); // Read the game state from the server and update the current game state
 			if(getMessageFromServer().getNumber() == 0){
 				getView().reDraw(getState());// Client draws game state here!
 			}
 			getTimer().loopRest();// Rest for the rest of the loop
 			
			if(getController().getStart() > 250){//Disconnect on escape for 5 seconds
 				getSocket().close();
 			}
 			
 		}
 	}
 	
 	/*
 	public static void main(String[] args) {
 		BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));
 		InetAddress serverAddr = InetAddress.getLoopbackAddress();
 		int port = 5379;
 		
 		if (args.length != 2) {
 			System.out.println("Please enter the IP address to connect to:");
 			try {
 				serverAddr = InetAddress.getByName(inputReader.readLine());
 				System.out.println("Please enter the port to connect to:");
 				port = Integer.parseInt(inputReader.readLine());
 				if (port <= 1000)
 					throw new Exception("Port must not be negative or well-known.");
 			}
 			catch (Exception e) {
 				System.err.println("Error in input: " + e.getMessage());
 				System.err.println("Using default values (localhost, 5379).");
 				serverAddr = InetAddress.getLoopbackAddress();
 				port = 5379;
 			}
 		}
 		else { // Attempt to parse command-line input
 			try {
 				serverAddr = InetAddress.getByName(args[0]);
 				port = Integer.parseInt(args[1]);
 				if (port <= 1000)
 					throw new Exception("Port must not be negative or well-known.");
 			}
 			catch (Exception e) {
 				System.err.println("Error in command-line input: " + e.getMessage());
 				System.err.println("Using default values (localhost, 5379).");
 				serverAddr = InetAddress.getLoopbackAddress();
 				port = 5379;
 			}
 		}
 		
 		Client theClient = null;
 		try {
 			theClient = new Client(serverAddr,port,new View(new Arena()));
 		}
 		catch (IOException e) {
 			System.err.println("Failed to create client.");
 			System.exit(1);
 		}
 		
 		try {
 			theClient.play();
 		}
 		catch (Exception e) {
 			
 		}
 		
 		theClient.getView().setVisible(false);
 			
 		System.out.println("Game over. Thanks for playing!");
 		System.exit(0); //super quit
 	}*/
 }
