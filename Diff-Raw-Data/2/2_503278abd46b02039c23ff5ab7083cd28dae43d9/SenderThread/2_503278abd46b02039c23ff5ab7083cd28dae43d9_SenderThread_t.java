 package epsilon.net;
 
 import epsilon.game.Game;
 import java.io.IOException;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.InetAddress;
 
 /**
  * SenderThread creates a thread that gets the players position and sends it to the server
  * @author mm
  */
 public class SenderThread implements Runnable {
 
     private DatagramSocket socket;
     private InetAddress serverAddress;
     private String clientName;
     private Game game;
 
     /**
      * Constructor
      * @param socket
      * @param serverAddress
      * @param name
      */
     public SenderThread(DatagramSocket socket, InetAddress serverAddress, String name) {
         this.socket = socket;
         this.serverAddress = serverAddress;
         this.clientName = name;
         game = Game.get();
 
     }
 
     /**
      * Thread for creating a packet with player position information and
      * sending the information to the server
      */
     public void run() {
         byte[] buf = new byte[NetworkHandler.BUFFER_SIZE];
         DatagramPacket outgoingPacket;
 
         double[] posArray = game.getPlayerPosition();
         String playerPosString = clientName +" " + posArray[0] + "" + posArray[1];
 
         if (!playerPosString.isEmpty()) {
             System.out.println("\n" + "Sending string: " + playerPosString + "\n");
         }
 
         buf = playerPosString.getBytes();
        outgoingPacket = new DatagramPacket(buf, buf.length, serverAddress, NetworkHandler.SERVER_PORT);
         
         try {
             socket.send(outgoingPacket);
             System.out.println("Packet sent");
         }
         catch (IOException e) {
             System.out.println("Count not send packet to server " + serverAddress);
         }
 
     }
 
 }
