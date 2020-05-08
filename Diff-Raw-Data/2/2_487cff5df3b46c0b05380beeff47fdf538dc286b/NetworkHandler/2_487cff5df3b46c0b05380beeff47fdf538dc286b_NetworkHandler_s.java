 package epsilon.net;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.Inet4Address;
 import java.net.Inet6Address;
 import java.net.InetAddress;
 import java.net.NetworkInterface;
 import java.net.Socket;
 import java.net.SocketException;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingQueue;
 
 /**
  * Singleton for managing and interfacing with the network subsystem.
  *
  * @author Magnus Mikalsen
  */
 public class NetworkHandler {
 
     // Constants
     public static final int SERVER_PORT = 6001;
     public static final int CLIENT_PORT = 6002;
     public static final int BUFFER_SIZE = 1000;
 
     private PacketParser parser;
     private ListenerThread listener;
     private SenderThread sender;
 
     // Incoming packet queue
     private BlockingQueue<DatagramPacket> incomingPacketQueue;
 
     // Outgoing packet queue
     private BlockingQueue<DatagramPacket> outgoingPacketQueue;
 
     // List of player game states
     private HashMap<String, double[]> playerStateList;
 
     // List of players we havent received information about before
     private ArrayList<String> newPlayers;
 
     private DatagramSocket socket;
 
     private boolean connectionEstablished = false;
 
     /**
      * Private constructor.
      * Initializes incoming and outgoing packet queues.
      */
     private NetworkHandler() {
         incomingPacketQueue = new LinkedBlockingQueue<DatagramPacket>();
         outgoingPacketQueue = new LinkedBlockingQueue<DatagramPacket>();
         playerStateList = new HashMap<String, double[]>();
         newPlayers = new ArrayList<String>();
     }
 
     /**
      * Inner class to create a instance of NetworkHandler which is
      * loaded when NetworkHandler.getInstance() method is called or
      * when INSTANCE is accessed.
      */
     private static class NetworkHandlerHolder {
         public static final NetworkHandler INSTANCE = new NetworkHandler();
     }
 
     /**
      * Get instance of NetworkHandler.
      *
      * @return INSTANCE
      */
     public static NetworkHandler getInstance() {
         return NetworkHandlerHolder.INSTANCE;
     }
 
     /**
      * Connect to the server by first establishing a TCP connection to give
      * server the player name. If name is available then the server sends a
      * OK message and a UDP connection can start. If the player name is taken
      * then we cast an exception so that a error message can be displayed.
      *
      * @param serverAddress IP address to server
      * @param name Local players name
      * @throws IOException Problem accessing socket
      * @throws Exception Error from server
      */
     public void connect(InetAddress serverAddress, String name) throws IOException, Exception {
 
         // Socket to establish connection
         Socket connectionSocket = new Socket(serverAddress, SERVER_PORT);
 
         // Check if connection is established
         if (connectionSocket != null && connectionSocket.isConnected()) {
 
             // Input from socket
             BufferedReader input = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
 
             // Output to socket
             PrintWriter output = new PrintWriter(connectionSocket.getOutputStream(), true);
 
             System.out.println("Connection established");
 
             // Send client name to server
             output.println(name);
 
             // Get respons from server
             String inputLine = input.readLine();
 
             if (inputLine.equals("OK")) {
                 // Respons indicates that the player was added to the server and
                 // the network subsystem can start normally
                 System.out.println("Got OK from server");
 
                 //String color = input.readLine();
 
                 //System.out.println(color);
 
                 // Get local IP to bind socket to
                 InetAddress bindIP = getFirstNonLoopbackAddress(true, false);
 
                 // Create socket on local interface
                 socket = new DatagramSocket(CLIENT_PORT, bindIP);
 
                 listener = new ListenerThread(socket, incomingPacketQueue);
                 parser = new PacketParser(incomingPacketQueue, playerStateList, this, name);
                 sender = new SenderThread(socket, serverAddress, name, outgoingPacketQueue);
 
                 // Start network threads
                 new Thread(listener).start();
                 new Thread(parser).start();
                 new Thread(sender).start();
 
                 connectionEstablished = true;
 
                 // close connection input, output and connection socket
                 input.close();
                 output.close();
                 connectionSocket.close();
             }
             if (inputLine.equals("ERROR")) {
                 // Respons indicates that the player was already registered on the server
                throw new Exception("Player name is already in use");
             }
         }
         else {
             System.out.println("Could not connect to server");
         }
 
     }
 
     /**
      * Stop the server by stopping all network threads and closing socket.
      */
     public void disconnect() {
         if (connectionEstablished) {
             listener.stopListener();
             parser.stopParser();
             sender.stopSender();
         }
     }
 
     /**
      * Send player information to server.
      */
     public void sendPlayerAction() {
         if (connectionEstablished) {
             sender.addToSendQueue();
         }
     }
 
     /**
      * Get a network players game state by name.
      *
      * @param playerName Name of player
      * @return playerPos Player game state
      */
     public double[] getPlayerStateByName(String playerName) {
         double[] playerState = playerStateList.get(playerName);
         return playerState;
     }
 
     /**
      * Check if there are new players.
      *
      * @return hasNewPlayers True if there are new players, false otherwise
      */
     public boolean hasNewPlayers() {
         boolean newPlayerState = false;
 
         if (!newPlayers.isEmpty()) {
             newPlayerState = true;
         }
         
         return newPlayerState;
     }
 
     /**
      * Get name of the last player added to the new player list.
      * 
      * @return newPlayer Last player in the new player list
      */
     public synchronized String getNewPlayer() {
         int arraySize = newPlayers.size();
         String newPlayer = newPlayers.get(arraySize-1);
         newPlayers.remove(arraySize-1);
         return newPlayer;
     }
 
     /**
      * Add a players name to the new players list
      *
      * @param name Player name
      */
     public synchronized void addNewPlayer(String playerName) {
        newPlayers.add(playerName);
     }
 
 
     /**
      * iterate through all addresses on host and return first non-loopback address.
      * This is mainly for linux compatibility
      *
      * @param preferIpv4 Search for IPv4 address
      * @param preferIPv6 Search for IPv6 address
      * @return addr First non-loopback IP address found
      * @throws SocketException No IP address found
      */
     private static InetAddress getFirstNonLoopbackAddress(boolean preferIpv4, boolean preferIPv6) throws SocketException {
         Enumeration en = NetworkInterface.getNetworkInterfaces();
         while (en.hasMoreElements()) {
             NetworkInterface i = (NetworkInterface) en.nextElement();
             for (Enumeration en2 = i.getInetAddresses(); en2.hasMoreElements();) {
                 InetAddress addr = (InetAddress) en2.nextElement();
                 if (!addr.isLoopbackAddress()) {
                     if (addr instanceof Inet4Address) {
                         if (preferIPv6) {
                             continue;
                         }
                         return addr;
                     }
                     if (addr instanceof Inet6Address) {
                         if (preferIpv4) {
                             continue;
                         }
                         return addr;
                     }
                 }
             }
         }
         return null;
     }
     
 }
