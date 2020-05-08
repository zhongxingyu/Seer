 package epsilon.net;
 
 import java.io.IOException;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.util.concurrent.BlockingQueue;
 
 /**
  * ListenerThread creates a thread that listens for incoming packets.
  * When a packet is received it is added to a packet queue.
  *
  * @author mm
  */
 public class ListenerThread implements Runnable {
 
     private BlockingQueue<DatagramPacket> packetQueue;
     private DatagramSocket socket;
     private boolean isRunning = true;    
     
     /**
      * Constructor
     * 
      * @param socket
      * @param packetQueue
      */
     public ListenerThread(DatagramSocket socket, BlockingQueue<DatagramPacket> packetQueue) {
         this.packetQueue = packetQueue;
         this.socket = socket;    
     }
 
     /**
      * Thread that listens after incoming packets. If a packet is
      * received it is added to the packet queue.
      */
     public void run() {
         System.out.println("Listening thread started");
 
         byte[] buf = new byte[NetworkHandler.BUFFER_SIZE];
         DatagramPacket incomingPacket;
 
         while (isRunning) {
             try {
                 incomingPacket = new DatagramPacket(buf, buf.length);
                 socket.receive(incomingPacket);
                 System.out.println("Packet received from " + incomingPacket.getAddress());
                 packetQueue.put(incomingPacket);
             }
             catch (IOException e) {
                 System.out.println(e.getMessage());
             }
             catch (InterruptedException ie) {
                 System.out.println("Could not add packet to queue");
             }
         }
         socket.close();
     }
 
     /**
      * Stop listening for incoming packets
      */
     public void stopListener() {
         isRunning = false;
     }
 
 }
