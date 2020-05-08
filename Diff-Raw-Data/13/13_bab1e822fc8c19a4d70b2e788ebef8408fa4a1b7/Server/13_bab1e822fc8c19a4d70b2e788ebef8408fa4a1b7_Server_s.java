 package game;
 
 import java.util.*;
 import java.io.*;
 import java.net.*;
 import com.google.gson.*;
 
 public class Server {
 	
 	private Gson jsonGen;
 	private ServerSocket serverSocket;
 	private ArrayList<Participant> participantList;
 	private int numberOfPlayers = 0;
 	// private String stateString = null;
 	private ServerGameState game;
 	private Gson json;
 	
 	Server() throws IOException {
 		serverSocket = new ServerSocket(5379);
 		participantList = new ArrayList<Participant>();
 		game = new ServerGameState();
 		json = new Gson();
 	}
 	
 	Server(int port) throws IOException {
 		serverSocket = new ServerSocket(port);
 		participantList = new ArrayList<Participant>();
 		game = new ServerGameState();
 		json = new Gson();
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
 	
 	/*
 	public void setStateString(String newStateString) {
 		stateString = newStateString;
 	}
 	*/
 	/*
 	public String getStateString() {
 		return stateString;
 	}
 	*/
 	public ArrayList<Participant> getParticipantList() {
 		return participantList;
 	}
 	
 	public void setParticipantList(ArrayList<Participant> newParticipantList) {
 		participantList = newParticipantList;
 	}
 	
 	public ServerGameState getGameState() {
 		return game;
 	}
 	
 	// Reads new Controller objects from all participants in given list
 	public void readControllersFromAll(ArrayList<Participant> aParticipantList) throws IOException {
 		for (Participant p: aParticipantList) {
 			p.readController();
 		}
 	}
 	
 	// Writes the current game state to all clients as a JSON string
 	public void writeGameStateToAll(ArrayList<Participant> aParticipantList) { 
 		for (Participant p: aParticipantList) {
 			p.writeToClient(json.toJson(getGameState()));
 		}
 	}
 	
 	// Generate a list of participants from network connection
 	public ArrayList<Participant> connectParticipants(int num) throws IOException {
 		Socket s = null;
		ArrayList<Participant> newParticipantList = new ArrayList<>();
 		
 		// Try to accept a connection; if successful, add a Participant with that connected socket
 		for (int i=0;i<num;i++) { 
 			try {
 				s = getServerSocket().accept();
 			}
 			catch (Exception e) {
 				s = null;
 			}
 			if (s != null) {
				newParticipantList.add(new RemoteParticipant(s));
 			}
 		}
 		return newParticipantList;
 	}
 	
 	public void applyAllControls(ArrayList<Participant> aParticipantList) {
 		for (Participant p: aParticipantList) {
 			getGameState().readControls(p.getPlayer(), p.getController());
 		}
 	}
 	
 	public void run(int numberOfPlayers) {
 		setNumberOfPlayers(numberOfPlayers);
 		run();
 	}
 	
 	public void run() {
 		
 		/* Test code
 		for (int i=0;i<100;i++) {
 			System.out.println(i + " Server: in its own thread!");
 			try {
 				Thread.sleep(15);
 			}
 			catch (Exception e) {
 				System.out.println("Can't sleep. Clown will eat me.");
 			}
 		}
 		Thread.currentThread().stop(); // Deprecated and bad, but I need results now
 		*/
 		jsonGen = new Gson();
 		Scanner inputScanner = new Scanner(System.in);
 		
 		// Connect clients and adds them to the participantList
 		try {
 			setParticipantList(connectParticipants(getNumberOfPlayers()));
 		}
 		catch (Exception e) {
 			System.err.println("Error connecting client to server. Exiting.\n");
 		}
 		
 		
 		// All participants should connected; begin communication cycle
 		for (int i=0;i<100;i++) { // A round of One Hundred exchanges for testing purposes
 			
 			try {
 				readControllersFromAll(getParticipantList()); // Reads updated controllers into all participants
 			}
 			catch (Exception e) {
 				System.err.println("Could not receive a participant's controller information.");
 			}
 			
 			// Controllers now ready for application to game state
 			// HERE GAME LOGIC SHOULD BE UPDATED USING THE CONTROLLERS
 			// My stab at it:
 			applyAllControls(getParticipantList()); // Applies controls for all participants
 			getGameState().update(); // updates game state using game logic
 			
 			// GameState is updated by this point; send it to all
 			writeGameStateToAll(getParticipantList());
 			
 			// All controller strings should be updated; create updated state string from them
 			/*
 			String newStateString = "";
 			for (int j=1;j<=participantList.size();j++) {
 				Message response = jsonGen.fromJson(participantList.get(j-1).getControllerString(), Message.class);
 				newStateString = newStateString + "Player " + j + " sent: "+ "#:"+response.getNumber()+": "+response.getMessage() + ", ";
 			}
 			System.out.println(newStateString);
 			System.out.println("Enter a line to send to the clients.");
 			String nextLine = inputScanner.nextLine();
 			*/
 			
 			//setStateString(jsonGen.toJson(game/*GameState*/));
 			//Message theMessage = new Message(nextLine+newStateString, i);
 			//setStateString(jsonGen.toJson(theMessage));
 			
 			// State string is ready; send to all participants
 			/*
 			for (Participant p: participantList) {
 				p.writeToClient(getStateString());
 			}
 			*/
 		}
 		
 		// Done with test round; stop thread
 		System.out.println("Done with test round; server going offline.");
 	}
 	
 	public static void main(String []args) {
 
 		// Couldn't get the thread attempt to work, so hacked together this stand-alone approach
 		
 		Server thisServer = null;
 		Scanner inputScanner = new Scanner(System.in);
 		
 		try {
 			thisServer = new Server();
 		}
 		catch (Exception e) {
 			System.out.println("Could not start the server.");
 			System.exit(1);
 		}
 		
 		if (thisServer != null) {
 			System.out.println("Please enter the number of players:");
 			thisServer.setNumberOfPlayers(inputScanner.nextInt());
 			
 			thisServer.run();
 		}
 		else
 			System.exit(1);
 	}
 }
