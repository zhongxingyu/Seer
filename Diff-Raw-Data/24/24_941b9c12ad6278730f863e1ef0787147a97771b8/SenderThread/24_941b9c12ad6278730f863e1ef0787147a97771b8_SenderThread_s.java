 package epsilonserver.net;
 
 import epsilonserver.entity.EntityHandler;
 import epsilonserver.game.ServerGUI;
 import java.io.IOException;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.InetAddress;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.concurrent.BlockingQueue;
 
 /**
  * SenderThread creates a thread for sending packets to client.
  * Also includes functionality for creating datagram packets.
  *
  * @author Magnus Mikalsen
  */
 public class SenderThread implements Runnable {
 
     private DatagramSocket socket;
     private EntityHandler eHandler;
     private boolean isRunning = true;
 
     // Outgoing packet queue
     private BlockingQueue<DatagramPacket> outgoingPacketQueue;
 
     /**
      * Constructor
      *
      * @param socket Datagram socket
      * @param outgoingPacketQueue Outgoing packet queue
      */
     public SenderThread(DatagramSocket socket, BlockingQueue<DatagramPacket> outgoingPacketQueue) {
         this.socket = socket;
         this.outgoingPacketQueue = outgoingPacketQueue;
         eHandler = EntityHandler.getInstance();
     }
 
     /**
      * Thread that gets a packet from the outgoing packet queue and sends it.
      */
     public void run() {
 
         ServerGUI.getInstance().setSystemMessage("Sender thread started");
 
         while (isRunning) {
 
             try {
                 // Get packet from outgoing packet queue
                 DatagramPacket packet = outgoingPacketQueue.take();
 
                // Sending packet
                 socket.send(packet);
             }
             catch (InterruptedException e) {
                System.out.println("Could not get packet from outgoing packet queue");
             }
             catch (IOException e) {
                System.out.println("Problem accessing socket in sender thread");
             }
 
         }
         
     }
 
     /**
      * Get a array containing names of all registered players. When the array
      * is iterated a packet is created for every player. The packet contains
      * a game state message that contains names and positions of every registered 
      * player except the player the packet is for, and a hash of the game state
      * message. The purpose of the hash is to make certain that the message 
      * is correct when received.
      * For hashing the SHA algorithm is currently used.
      */
     public synchronized void addToSendQueue() {
         byte[] buf = new byte[NetworkHandler.BUFFER_SIZE];
 
         // get array of player names
         String[] nameArray = eHandler.getNameArray();
 
         for (int i = 0; i < nameArray.length; i++) {
 
             // String added to packet
             String sendString = "";
 
             // Game state message
             String gameStateString = "";
 
             // Get game state message
             gameStateString = eHandler.getGameStateString(nameArray[i]);
 
             // Check if game state message is empty
             if (!gameStateString.equals("")) {
 
                 try {
                     // create a hash of the game state message using SHA algorithm
                     MessageDigest hash = MessageDigest.getInstance("SHA");
                     byte[] hashSum = hash.digest(gameStateString.getBytes());
 
                     // Create a hexadecimal representation of the hash
                     StringBuilder hexString = new StringBuilder();
                     for (int j = 0; j < hashSum.length; j++) {
                         hexString.append(Integer.toHexString(0xFF & hashSum[j]));
                     }
 
                     // Add hash to end of game state string
                     String hashString = hexString.toString();
                     sendString = gameStateString + hashString;
                 }
                 catch (NoSuchAlgorithmException e) {
                    ServerGUI.getInstance().setErrorMessage("Could not find hashing algorithm in sender");
                 }
 
                 // Convert final game state message to bytes
                 buf = sendString.getBytes();
 
                 // Get players IP address
                 InetAddress ip = eHandler.getAddressByName(nameArray[i]);
 
                 // Create datagram packet
                 DatagramPacket outgoingPacket =
                         new DatagramPacket(buf, buf.length, ip, NetworkHandler.CLIENT_PORT);
 
                 try {
                     // Add packet to outgoing packet queue
                     outgoingPacketQueue.put(outgoingPacket);
                 }
                 catch (InterruptedException e) {
                     ServerGUI.getInstance().setErrorMessage("Could not add packet to ougoing packet queue");
                 }
             }
             
         }
     }
 
     /**
      * Stop running sender thread
      */
     public void stopSender() {
         isRunning = false;
     }
 
 
 }
