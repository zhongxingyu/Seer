 package client;
 /* CS3283 Project
  * 
  * Game UDP Client: communicates to with server through UDP connections to get informations
  * 
  */
 
 import java.util.*;
 import java.net.*;
 import java.io.*;
 
 public class GameClient {
 	private final int ROW = 3;		// Number of rows of Nodes
 	final int COLUMN = 3;	// Number of column of Nodes
 
 	final int N_NUM = 9;	// Total number of Nodes
 	final int T_NUM = 3;	// Number of treasures on the map
 	final int P_NUM = 2;	// Number of players
 	final int K_NUM = 20;	// Number of key codes
 
 	// Event code
 	final int invalidEvent = 0;
 	final int loginEvent = 1; 
 	final int mapUpdateEvent = 2;
 	final int openTreasureEvent = 3;
 	final int scoreUpdateEvent = 4;
 	final int addKeyEvent = 5;
 	final int endOfGameEvent = 6;
 
 	final int timeOutDuration = 500;
 	Random randomGenerator;
 
 	// Stores when player informations, add player when they connects to the server for the first time
 	// there is no playerID stored, the index of the array is equals to playerID 
 	// Currently stored values are: logged on?, playerScore and number of keys held
 
 	private int[][] playerList;
 
 	private int[] treasureList;	// stores treasure location
 	private int globalEvent;		// store when and where the global event happens
 
 	private static int[] playerLocation;
 
 	// Client socket variables
 
 	// How to get IP on VM linux 
 	/* lackern@lackern-VirtualBox:~$ ifconfig
 	 * eth0 Link encap:Ethernet  HWaddr 08:00:27:98:0c:c7
 	 * inet addr:192.168.1.29  Bcast:192.168.1.255  Mask:255.255.255.0 */
 
 	final int port = 9001;
 	final String gameServerAddress = "localhost";
 	private DatagramSocket socket;
 	InetAddress inetAddress;
 
 	public GameClient() throws Exception {
 
 		initializeSocket();
 		initializeTreasureList();
 		initializePlayerList();
 		initializePlayerLocation();
 		globalEvent = 0;
 	}
 	
 	private void initializeSocket() throws Exception
 	{
 		socket = new DatagramSocket();
 		socket.setSoTimeout(timeOutDuration);
 		inetAddress = InetAddress.getByName(gameServerAddress);
 	}
 
 	private void initializeTreasureList()
 	{
 		treasureList = new int[N_NUM];
 		for(int i = 0; i < N_NUM; i++)
 			treasureList[i] = 0;
 	}
 
 	private void initializePlayerList()
 	{
 		playerList = new int[P_NUM][3];
 		for(int i = 0; i<P_NUM; i++)
 		{
 			playerList[i][0] = 0;
 			playerList[i][1] = 0;
 			playerList[i][2] = 0;
 		}
 	}
 
 	private void initializePlayerLocation()
 	{
 		playerLocation = new  int[P_NUM];
 		for(int i = 0; i < P_NUM; i++)
 			playerLocation[i] = 0;
 	}
 
 	public String loginEvent(int playerID) throws Exception {
 
 		/* Use DataGramSocket for UDP connection convert string "request" to array of bytes,
 		 * suitable for creation of DatagramPacket */
 		String request = loginEvent +";" + playerID;
 
 		/* Start of UDP protocol */
 		byte outgoingBuffer[] = request.getBytes();
 
 		// Now create a packet (with destination inetAddress and port)
 		DatagramPacket outgoingPacket = new DatagramPacket(outgoingBuffer, outgoingBuffer.length, inetAddress, port);
 
 		// sends request to game server
 		socket.send(outgoingPacket);
 
 		// create a packet buffer to store data from packets received.
 		byte[] incomingBuffer = new byte[1000];
 		DatagramPacket incomingPacket = new DatagramPacket(incomingBuffer, incomingBuffer.length);
 
 		// receive the reply from server.
 		socket.receive(incomingPacket);
 
 		// convert reply to string and print to System.out
 		String reply = new String(incomingPacket.getData(), 0, incomingPacket.getLength());
 		System.out.println(reply + " [loginEvent: GameClient.java]");
 		/* End of UDP protocol */
 
 		// Game Server's loginEvent reply format: Failure or Successful or Already Logon
 		return reply;
 	}
 
 	public void mapUpdateEvent(int playerID) throws Exception {
 
 		/* Use DataGramSocket for UDP connection convert string "request" to array of bytes,
 		 * suitable for creation of DatagramPacket */
 		String request = mapUpdateEvent + ";" + playerID;
 
 		/* Start of protocol */
 		byte outgoingBuffer[] = request.getBytes();
 
 		// Now create a packet (with destination inetAddress and port)
 		DatagramPacket outgoingPacket = new DatagramPacket(outgoingBuffer, outgoingBuffer.length, inetAddress, port);
 
 		// Sends request to game server
 		socket.send(outgoingPacket);
 
 		// Create a packet buffer to store data from packets received.
 		byte[] incomingBuffer = new byte[1000];
 		DatagramPacket incomingPacket = new DatagramPacket(incomingBuffer, incomingBuffer.length);
 
 		// Receive the reply from server.
 		socket.receive(incomingPacket);
 
 		// Convert reply to string and print to System.out
 		String reply = new String(incomingPacket.getData(), 0, incomingPacket.getLength());
 		System.out.println(reply + " [mapUpdateEvent: GameClient.java]");
 
 		// Tokenize the reply string
 		StringTokenizer requestToken;
 		requestToken = new StringTokenizer(reply, ";");
 		/* End of protocol */
 
 		// Game Server's mapUpdateEvent reply format: 
 		// p1 logon status, p1 score, p1 location, p2 ... pP_NUM, treasure0,1,2,3 ... N_NUM, global event status
 
 		// Update playerList and playerLocaiton array
 		for(int i = 0; i < P_NUM; i++)
 		{
 			playerList[i][0] = Integer.parseInt(requestToken.nextToken());
 			playerList[i][1] = Integer.parseInt(requestToken.nextToken());
 			playerList[i][2] = Integer.parseInt(requestToken.nextToken());
 			playerLocation[i] =  Integer.parseInt(requestToken.nextToken());
 		}
 
 		// Update treasureList array
 		for(int i = 0; i < N_NUM; i++)
 			treasureList[i] = Integer.parseInt(requestToken.nextToken());
 
 		// Update global event
 		globalEvent = Integer.parseInt(requestToken.nextToken());
 	}
 
 	public String openTreasureEvent(int playerID) throws Exception {
 
 		/* Use DataGramSocket for UDP connection convert string "request" to array of bytes,
 		 * suitable for creation of DatagramPacket */
 		String request = openTreasureEvent +";" + playerID +";"+ playerLocation[playerID];
 
 		/* Start of UDP protocol */
 		byte outgoingBuffer[] = request.getBytes();
 
 		// Now create a packet (with destination inetAddress and port)
 		DatagramPacket outgoingPacket = new DatagramPacket(outgoingBuffer, outgoingBuffer.length, inetAddress, port);
 
 		// sends request to game server
 		socket.send(outgoingPacket);
 
 		// create a packet buffer to store data from packets received.
 		byte[] incomingBuffer = new byte[1000];
 		DatagramPacket incomingPacket = new DatagramPacket(incomingBuffer, incomingBuffer.length);
 
 		// receive the reply from server.
 		socket.receive(incomingPacket);
 
 		// convert reply to string and print to System.out
 		String reply = new String(incomingPacket.getData(), 0, incomingPacket.getLength());
 		System.out.println(reply + " [openTreasureEvent: GameClient.java]");
 		/* End of UDP protocol */
 
		// Game Server's openTreasureEvent reply format: a mini game ID: 0 1 2 ... < MG_NUM
 		return reply;
 	}
 
 	public String scoreUpdateEvent(int playerID, int newScore) throws Exception {
 
 		/* Use DataGramSocket for UDP connection convert string "request" to array of bytes,
 		 * suitable for creation of DatagramPacket */
 		String request = scoreUpdateEvent +";" + playerID + ";" + newScore;
 
 		/* Start of UDP protocol */
 		byte outgoingBuffer[] = request.getBytes();
 
 		// Now create a packet (with destination inetAddress and port)
 		DatagramPacket outgoingPacket = new DatagramPacket(outgoingBuffer, outgoingBuffer.length, inetAddress, port);
 
 		// sends request to game server
 		socket.send(outgoingPacket);
 
 		// create a packet buffer to store data from packets received.
 		byte[] incomingBuffer = new byte[1000];
 		DatagramPacket incomingPacket = new DatagramPacket(incomingBuffer, incomingBuffer.length);
 
 		// receive the reply from server.
 		socket.receive(incomingPacket);
 
 		// convert reply to string and print to System.out
 		String reply = new String(incomingPacket.getData(), 0, incomingPacket.getLength());
 		System.out.println(reply + " [scoreUpdateEvent: GameClient.java]");
 		/* End of UDP protocol */
 
 		// Game Server's scoreUpdateEvent reply format: Failure or Successful
 		return reply;
 	}
 	
 	public String addKeyEvent(int playerID, int keyCode) throws Exception {
 
 		/* Use DataGramSocket for UDP connection convert string "request" to array of bytes,
 		 * suitable for creation of DatagramPacket */
 		String request = addKeyEvent +";" + playerID + ";" + keyCode;
 
 		/* Start of UDP protocol */
 		byte outgoingBuffer[] = request.getBytes();
 
 		// Now create a packet (with destination inetAddress and port)
 		DatagramPacket outgoingPacket = new DatagramPacket(outgoingBuffer, outgoingBuffer.length, inetAddress, port);
 
 		// sends request to game server
 		socket.send(outgoingPacket);
 
 		// create a packet buffer to store data from packets received.
 		byte[] incomingBuffer = new byte[1000];
 		DatagramPacket incomingPacket = new DatagramPacket(incomingBuffer, incomingBuffer.length);
 
 		// receive the reply from server.
 		socket.receive(incomingPacket);
 
 		// convert reply to string and print to System.out
 		String reply = new String(incomingPacket.getData(), 0, incomingPacket.getLength());
 		System.out.println(reply + " [addKeyEvent: GameClient.java]");
 		/* End of UDP protocol */
 
 		// Game Server's addKeyEvent reply format: Failure or Successful
 		return reply;
 	}
 
 	// Returns the X position of a playerID
 	public int getPlayerX(int playerID) {
 
 		int x = playerLocation[playerID] / 3;
 		return x;
 	}
 	// Returns the Y position of a playerID
 	public int getPlayerY(int playerID) {
 
 		int y = playerLocation[playerID] % 3;
 		return y;
 	}
 
 	// Returns a 1D array of treasure info
 	public int[] getTreasureList(){
 		return treasureList;
 	}
 
 	// Returns a 2D array of treasure info
 	public int[][] getTreasureList2D(){
 
 		int[][] treasureList2D = new int[ROW][COLUMN];
 
 		for(int i = 0; i< N_NUM; i++)
 			treasureList2D[i/COLUMN][i%COLUMN] = treasureList[i];
 
 		return treasureList2D;
 	}
 
 	// Returns the logon status of playerID
 	public boolean getLogonStatus(int playerID){
 		return playerList[playerID][0] == 1;
 	}
 
 	// Returns current score of a playerID
 	public int getPlayerScore(int playerID){
 		return playerList[playerID][1];
 	}
 
 	// Returns player's ranking //incomplete
 	public int getPlayerRanking(){
 		return 1;
 	}
 
 	// Closes the UDP socket
 	public void closeSocket(){
 		socket.close();
 	}
 }
