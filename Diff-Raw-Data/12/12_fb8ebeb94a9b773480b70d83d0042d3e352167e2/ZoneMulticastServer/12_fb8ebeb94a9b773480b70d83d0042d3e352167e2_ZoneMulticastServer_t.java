 /*
  * class for a multicast server that accepts and processes
  * UDP datagrams for control logic
  *
  * for more on IPv4 Multicast addresses see
  * http://www.iana.org/assignments/multicast-addresses/multicast-addresses.xml
  *
  * this class is largely based off of
  * http://www.roseindia.net/java/example/java/net/udp/UDPMulticastServer.shtml
  */
 package zonecontrol;
 
 import java.io.IOException;
 import java.net.DatagramPacket;
 import java.net.InetAddress;
 import java.net.MulticastSocket;
 import java.net.UnknownHostException;
 import java.util.Date;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author Jason Zerbe
  */
 public class ZoneMulticastServer {
 
     protected Thread serverThread = null;
    protected ZoneThread myZoneThread = null;
     protected MulticastSocket serverSocket = null;
     protected boolean printNetworkCommandToTerminal = false;
 
     public ZoneMulticastServer(boolean printNetworkCommandToTerminal) {
         this.printNetworkCommandToTerminal = printNetworkCommandToTerminal;
     }
 
     public void startServer() {
    	myZoneThread = new ZoneThread();
     }
 
     public void stopServer() {
         System.out.print("ZMS thread and socket stopping ...");
 
         serverThread.interrupt();
         try {
             serverThread.join();
         } catch (InterruptedException ex) {
             Logger.getLogger(
                     ZoneMulticastServer.class.getName()).log(
                     Level.SEVERE, null, ex);
         }
         serverSocket.close();
 
         System.out.println("ZMS thread joined and socket closed");
 
         serverSocket = null;
         serverThread = null;
     }
 
     protected class ZoneThread implements Runnable {
 
         public ZoneThread() {
             System.out.println("ZMS thread starting ...");
             serverThread = new Thread(this);
             serverThread.start();
             System.out.println("ZMS thread started");
         }
 
         @Override
         public void run() {
             try {
                 serverSocket = new MulticastSocket(
                         ZoneConstants.getInstance().getGroupPortInt());
             } catch (IOException ex) {
                 Logger.getLogger(
                         ZoneMulticastServer.class.getName()).log(
                         Level.SEVERE, null, ex);
             }
 
             try {
                 serverSocket.setTimeToLive(
                         ZoneConstants.getInstance().getGroupTTLInt());
             } catch (IOException ex) {
                 Logger.getLogger(
                         ZoneMulticastServer.class.getName()).log(
                         Level.SEVERE, null, ex);
             }
 
             InetAddress groupAddress = null;
             try {
                 groupAddress = InetAddress.getByName(
                         ZoneConstants.getInstance().getGroupAddressStr());
             } catch (UnknownHostException ex) {
                 Logger.getLogger(
                         ZoneMulticastServer.class.getName()).log(
                         Level.SEVERE, null, ex);
             }
 
             boolean serverHasJoinedGroup = false;
             while (!serverHasJoinedGroup) {
                 try {
                     serverSocket.joinGroup(groupAddress);
                 } catch (IOException ex) { //unable to join multicast group
                     Logger.getLogger(
                             ZoneMulticastServer.class.getName()).log(
                             Level.SEVERE, null, ex);
 
                     try { //pause for 2 seconds to give NIC a chance to connect
                         Thread.sleep(2000);
                     } catch (InterruptedException ex1) {
                         Logger.getLogger(
                                 ZoneMulticastServer.class.getName()).log(
                                 Level.WARNING, null, ex1);
                     }
 
                     continue; //try again
                 }
                 serverHasJoinedGroup = true; //finally connected to the group
             }
 
             System.out.println("ZMS started and listening to group");
 
             while (true) {
                 // receive request from client
                 final byte[] buffer = new byte[ZoneConstants.getInstance().getGroupMaxByte()];
                 final DatagramPacket packet = new DatagramPacket(
                         buffer, buffer.length, groupAddress,
                         ZoneConstants.getInstance().getGroupPortInt());
                 try {
                     serverSocket.receive(packet);
                 } catch (IOException ex) {
                     Logger.getLogger(
                             ZoneMulticastServer.class.getName()).log(
                             Level.SEVERE, null, ex);
                 }
                 final String theNetworkCommand = new String(buffer).trim().toLowerCase();
 
                 //notify via the console of datagram
                 if (printNetworkCommandToTerminal) {
                     System.out.println(new Date().toString()
                             + " - recieved:\n" + theNetworkCommand);
                 }
 
                 // process said request
                 ZoneServerLogic.getInstance().processNetworkCommand(theNetworkCommand);
             }
         }
     }
 }
