 package cs.dsn.dwc.server;
 
import cs.dsn.dwc.sdp.DiscoveryListener;
 import org.junit.Test;
 
 import java.io.IOException;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.InetAddress;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.fail;
 
 /**
  * Tests for CountServer
  */
 public class CountServerTest {
   private static final int SERVER_PORT = 45234;
   private static final int CLIENT_PORT = 45235;
   private volatile AssertionError assertionError = null;
 
   @Test
   public void serverUdpRespondTest() {
     final CountServer server = new CountServer(SERVER_PORT);
     Thread thread = new Thread(server);
     thread.start();
 
     // set up listening thread
     Thread clientListener = new Thread(new Runnable() {
       @Override
       public void run() {
         try {
           DatagramSocket clientSocket = new DatagramSocket(CLIENT_PORT);
           byte[] receiveBuf = new byte[1024];
           DatagramPacket receivePacket = new DatagramPacket(receiveBuf, receiveBuf.length);
           clientSocket.receive(receivePacket);
           String response = new String(receiveBuf).trim();
           assertEquals(response, DiscoveryListener.ALIVE);
 
         } catch (IOException e) {
           assertionError = new AssertionError(e.getMessage());
         }
       }
     });
     clientListener.start();
 
     try {
       DatagramSocket clientSocket = new DatagramSocket();
       clientSocket.setBroadcast(true);
       String message = DiscoveryListener.DISCOVER + "\n" + CLIENT_PORT;
       InetAddress address = InetAddress.getLocalHost();
       DatagramPacket packet = new DatagramPacket(message.getBytes(), message.length(), address, SERVER_PORT);
       clientSocket.send(packet);
     } catch (IOException e) {
       fail("Exception thrown: " + e);
     }
 
     try {
       clientListener.join();
       if (assertionError != null)
         fail(assertionError.getMessage());
     } catch (InterruptedException e) {
       e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
     }
 
     server.stop();
   }
 }
