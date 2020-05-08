 package poker.server;
 
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Toolkit;
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.NetworkInterface;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.util.Enumeration;
 
 import javax.net.ssl.SSLServerSocketFactory;
 import javax.swing.*;
 
 public class Server extends JFrame{
 
 	/* Private instance constants */
 	private static final int PORT = 9876;
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
     private static JTextField serverIPtext = new JTextField();
     private static JTextField serverPortText = new JTextField();
 
 	/**
 	 * Starts a 'ServerSocket' with specified 'PORT', 'CONNECTION_LIMIT', 'serverAddress'.
 	 * Handles client connections, starts a client connection on a new thread and creates 
 	 * game rooms, that get a separate thread aswell. Once client gets connected - he gets
 	 * added to the gaming room. Once player count requirement for a game room is met - the game
 	 * gets started. Once game is started a new 'Room' is created and newly connected players
 	 * get added to it.
 	 */
 	
 	public static void main(String[] args) {
 		if(args.length > 2) {
 			int playercount = Integer.parseInt(args[0]);
 			if(!(playercount > 1 && playercount < 10)) {
 				System.out.println("Usage: [playercount] [IP address] [port number]");
 				System.out.println("player count must be between 2 and 9");
 				System.exit(0);
 			}
 			int port = Integer.parseInt(args[2]);
			if(!(port > 1 && port < 10)) {
 				System.out.println("Usage: [playercount] [IP address] [port number]");
 				System.out.println("port number must be between 0000 and 9999");
 				System.exit(0);
 			}
 			start(playercount, args[1], port);
 		} else {
 			start(4, null, -1);
 		}
 	}
 	
 	public static void start(int playerCount, String addr, int port) {
 		server = null;
 		client = null;
 		int portnum = PORT;
 		try {
 			// Assigns localhost address to 'serverAddress'.
 			
 			if(addr == null) {
 				Enumeration<InetAddress> cookie = NetworkInterface.getNetworkInterfaces().nextElement().getInetAddresses();
 				InetAddress temp = null;
 				while(cookie.hasMoreElements()) {
 					temp = cookie.nextElement();
 					
 					if(temp.getAddress()[0] == (byte)0xC0){
 						serverAddress = temp;
 						break;
 					}
 				}
 				
 				if(serverAddress == null) {
 					serverAddress = InetAddress.getLocalHost();
 				}
 				
 			} else {
 				serverAddress = InetAddress.getByName(addr);
 				portnum = port;
 			}
 //			serverAddress = InetAddress.getByName("192.168.1.108");
 			// Initializes 'ServerSocket'.
 	//		server = SSLServerSocketFactory.getDefault().createServerSocket(PORT,
 	//										CONNECTION_LIMIT, serverAddress);
 			
 			server = new ServerSocket(portnum, CONNECTION_LIMIT, serverAddress);
             
 			// GUI IMPLEMENTATION'S START
 			showServerInfo.setLayout(new FlowLayout());
 			showServerInfo.setSize(250, 110);
 			showServerInfo.setLocation(screenSize.width / 2 - showServerInfo.getSize().width / 2, screenSize.height / 2 - showServerInfo.getSize().height / 2);
 			showServerInfo.setResizable(false);
 			showServerInfo.setVisible(true);
 			
 			showServerInfo.setTitle("Server");
 			showServerInfo.getContentPane().setLayout(null);
             showServerInfo.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
 			showServerInfo.add(serverIP(), null);
 			showServerInfo.add(serverPort(), null);
             showServerInfo.add(serverIPtext(), null);
             showServerInfo.add(serverPortText(), null);
 
             showServerInfo.validate();
             showServerInfo.repaint();
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
 		 serverIP.setBounds(15, 10, 100, 25);
 		 serverIP.setText("Server IP: ");
 		 serverIP.setVisible(true);
 		 return serverIP;
 	    }
 	 private static JLabel serverPort(){
 		 serverPort.setBounds(15, 45, 100, 25);
 		 serverPort.setText("Server port:");
 		 serverPort.setVisible(true);
 		 return serverPort;
 	    }
     private static JTextField serverIPtext(){
         serverIPtext.setBounds(125, 10, 110, 25);
         serverIPtext.setText("" + serverAddress.getHostAddress());
         serverIPtext.setEditable(false);
         serverIPtext.setVisible(true);
         return serverIPtext;
     }
     private static JTextField serverPortText(){
         serverPortText.setBounds(125, 45, 110, 25);
         serverPortText.setText("" + PORT);
         serverPortText.setEditable(false);
         serverPortText.setVisible(true);
         return serverPortText;
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
