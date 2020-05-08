 package epsilonserver.net;
 
 import epsilonserver.game.ServerGUI;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.Inet4Address;
 import java.net.Inet6Address;
 import java.net.InetAddress;
 import java.net.NetworkInterface;
 import java.net.SocketException;
 import java.net.UnknownHostException;
 import java.util.Enumeration;
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
     public static final int BUFFER_SIZE = 256;
 
     private PacketParser parser;
     private ListenerThread listener;
     private SenderThread sender;
 
     // Incoming packet queue
     private BlockingQueue<DatagramPacket> incomingPacketQueue;
 
     // Outgoing packet queue
     private BlockingQueue<DatagramPacket> outgoingPacketQueue;
     
     private DatagramSocket socket;
 
     /**
      * Private constructor.
      * Initializes incoming and outgoing pakcet queues.
      */
     private NetworkHandler() {
         incomingPacketQueue = new LinkedBlockingQueue<DatagramPacket>();
         outgoingPacketQueue = new LinkedBlockingQueue<DatagramPacket>();
     }
 
     /**
      * inner class to create a instance of NetworkHandler. this is
      * loaded when NetworkHandlre.getInstance() method is called or
      * when INSTANCE is accessed.
      */
     private static class NetworkHandlerHolder {
         public static final NetworkHandler INSTANCE = new NetworkHandler();
     }
 
     /**
      * Get instance of NetworkHandler.
      *
      * @return INSTANCE Instance of NetworkHandler
      */
     public static NetworkHandler getInstance() {
         return NetworkHandlerHolder.INSTANCE;
     }
 
     /**
      * Start servers network subsystem. this includes creating a socket,
      * and starting network threads.
      */
     public void startServer() {
 
         try {
             // get local ip to bind socket to
             InetAddress bindIP = getFirstNonLoopbackAddress(true, false);
 
             socket = new DatagramSocket(SERVER_PORT, bindIP);
            ServerGUI.getInstance().setSystemMessage("Socket created on interface " + InetAddress.getLocalHost());
         }
         catch (SocketException se) {
             ServerGUI.getInstance().setErrorMessage("Could not create socket");
         }
        catch (UnknownHostException ue) {
            ServerGUI.getInstance().setErrorMessage("Problem with local IP address");
        }
 
         listener = new ListenerThread(socket, incomingPacketQueue);
         parser = new PacketParser(incomingPacketQueue);
         sender = new SenderThread(socket, outgoingPacketQueue);
 
         // start network threads
         new Thread(listener).start();
         new Thread(parser).start();
         new Thread(sender).start();
     }
 
     /**
      * Stop the server by stopping all network threads and closing socket.
      */
     public void stopServer() {
         listener.stopListener();
         parser.stopParser();
         sender.stopSender();
         ServerGUI.getInstance().setSystemMessage("Server stopped");
     }
 
     /**
      * Send game data to all registered players.
      */
     public void updateClients() {
         sender.addToSendQueue();
     }
 
     /**
      * iterate through all addresses on host and return first non-loopback address.
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
