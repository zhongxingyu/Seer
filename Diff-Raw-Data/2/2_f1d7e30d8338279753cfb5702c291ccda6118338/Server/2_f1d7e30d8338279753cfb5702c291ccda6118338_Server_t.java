 package poker.server;
 
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Toolkit;
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.UnknownHostException;
 
 import javax.net.ssl.SSLServerSocketFactory;
 import javax.swing.*;
 
 public class Server extends JFrame{
 
 	/* Private instance constants */
 	private static final int PORT = 9999;
 	private static final int CONNECTION_LIMIT = 50;
	private static final int MAX_PLAYERS_IN_ROOM = 3;
 	private static final int WAITING_TIMEOUT = 0;
 
 	/* Private instance variables. 
 	 * 'roomID' is identification for room, used once room is created.
 	 * 'waitingTimeExceeded' is evaluation if game should be started once minimum player count requirement is met.
 	 */
 	private static int roomID = 1;
 	private static boolean waitingTimeExceeded = false;
 	private static Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
 	
 	/*
 	 * GUI Variables
 	 */
 	private static JFrame showServerInfo = new JFrame();
 	private static JLabel serverIP = new JLabel();
 	private static JLabel serverPort = new JLabel();
 	
 	/**
 	 * Starts a 'ServerSocket' with specified 'PORT', 'CONNECTION_LIMIT', 'serverAddress'.
 	 * Handles client connections, starts a client connection on a new thread and creates 
 	 * game rooms, that get a separate thread aswell. Once client gets connected - he gets
 	 * added to the gaming room. Once player count requirement for a game room is met - the game
 	 * gets started. Once game is started a new 'Room' is created and newly connected players
 	 * get added to it.
 	 */
 	
 	public static void main(String[] args) {
 		server = null;
 		client = null;
 		try {
 			// Assigns localhost address to 'serverAddress'.
 			serverAddress = InetAddress.getLocalHost();
 			// Initializes 'ServerSocket'.
 	//		server = SSLServerSocketFactory.getDefault().createServerSocket(PORT,
 	//										CONNECTION_LIMIT, serverAddress);
 			
 			server = new ServerSocket(PORT, CONNECTION_LIMIT, serverAddress);
             
 			// GUI IMPLEMENTATION'S START
 			showServerInfo.setLayout(new FlowLayout());
 			showServerInfo.setSize(180, 110);
 			showServerInfo.setLocation(screenSize.width / 2 - showServerInfo.getSize().width / 2, screenSize.height / 2 - showServerInfo.getSize().height / 2);
 			showServerInfo.setResizable(false);
 			showServerInfo.setVisible(true);
 			
 			showServerInfo.setTitle("Server");
 			showServerInfo.getContentPane().setLayout(null);
 			
 			showServerInfo.add(serverIP(), null);
 			showServerInfo.add(serverPort(), null);
 			// GUI IMPLEMENTATION'S END
 			
 			while (true) {
 				gameRoom = new Room(roomID++);
 				// Keeps track of player count in currently created room.
 				int playersInRoom = 0;
 				// Handles new client connections, adds these clients to rooms.
 				while (playersInRoom < MAX_PLAYERS_IN_ROOM
 						&& !waitingTimeExceeded) {
 					client = server.accept();
 					// Adds connected player to the game room.
 					gameRoom.addUser(client);
 					playersInRoom++;
 					// Check if minimum player requirement is met.
 					if (playersInRoom == 2) {
 						//startGameCountdown();
 					}
 				}
 				// Once the loop has ended, the game in the room has started and players connect to new room.
 				playersInRoom = 0;
 				Thread t = new Thread(gameRoom);
 				t.start();
 				waitingTimeExceeded = false;
 			}
 		} catch (UnknownHostException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	/**
 	 * GUI Labels initialization 
 	 */
 	
 	 private static JLabel serverIP(){
 		 serverIP.setBounds(15, 10, 150, 25);
 		 serverIP.setText("Server IP:  " + serverAddress.getHostAddress());
 		 serverIP.setVisible(true);
 		 return serverIP;
 	    }
 	 private static JLabel serverPort(){
 		 serverPort.setBounds(15, 45, 150, 25);
 		 serverPort.setText("Server port:  " + PORT);
 		 serverPort.setVisible(true);
 		 return serverPort;
 	    }
 	
 	
 	/** 
 	 * Sets waitingTimeExceeded value, which determines whether more player connections should be accepted.
 	 */
 	public static void setWaitingTimeExceeded(boolean state) {
 		waitingTimeExceeded = state;
 	}
 
 	/** 
 	 * Once minimum player count requirement is met, separate thread is started
 	 * which interrupts server.accept() method by creating another client connection once
 	 * 'WAITING_TIMOUT' delay has come to an end.
 	 */
 	@SuppressWarnings("unused")
 	private static void startGameCountdown() {
 		// Creates instance of the 'Timer' class. Parameters are passed so that client connection can be simulated.
 		Timer timer = new Timer(WAITING_TIMEOUT, serverAddress, PORT, gameRoom);
 		countdownThread = new Thread(timer);
 		countdownThread.start();
 	}
 
 	/* Private instance variables that aren't initialized yet. */
 	private static ServerSocket server;
 	private static Socket client;
 	private static Room gameRoom;
 	private static InetAddress serverAddress;
 	private static Thread countdownThread;
 }
