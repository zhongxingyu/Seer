 package epsilon.net;
 
 import java.net.DatagramPacket;
 import java.util.HashMap;
 import java.util.StringTokenizer;
 import java.util.concurrent.BlockingQueue;
 
 /**
  * PacketParser class creates a parsing thread that takes
  * incoming packets from the packet queue and parses its data.
  * @author mm
  */
 public class PacketParser implements Runnable {
 
     private BlockingQueue<DatagramPacket> packetQueue;
     private HashMap<String, double[]> playerPosList;
     private NetworkHandler netHandler;
 
     private boolean isRunning = true;    
 
     /**
      * Constructor
      * @param packetQueue
      */
     public PacketParser(BlockingQueue<DatagramPacket> packetQueue,
             HashMap<String, double[]> playerPosList, NetworkHandler netHandler) {
         this.packetQueue = packetQueue;
         this.playerPosList = playerPosList;
         this.netHandler = netHandler;
     }
 
     /**
      * Parse incoming packets from the server
      */
     public void run() {
         while (isRunning) {
             try {
                 DatagramPacket packet = packetQueue.take();
                 String packetString = new String(packet.getData(), 0, packet.getLength());
 
                 StringTokenizer part = new StringTokenizer(packetString);
 
                 while (part.hasMoreTokens()) {
                     String name = part.nextToken();
                     String posX = part.nextToken();
                     String posY = part.nextToken();
 
                    double[] posArray = new double[1];
 
                     try {
                         posArray[0] = Double.parseDouble(posX);
                         posArray[1] = Double.parseDouble(posY);
                     }
                     catch (NumberFormatException e) {
                         System.out.println("Cant convert x or y coordinates to double");
                     }
 
                     if (!playerPosList.containsKey(name)) {
                         netHandler.addNewPlayer(name);
                     }
                     
                     playerPosList.put(name, posArray);
                 }
             }
             catch (InterruptedException ie) {
                 System.out.println("Interrupt from queue");
             }
         }
     }
 
     /**
      * Stop parser thread
      */
     public void stopParser() {
         isRunning = false;
     }
 
 }
