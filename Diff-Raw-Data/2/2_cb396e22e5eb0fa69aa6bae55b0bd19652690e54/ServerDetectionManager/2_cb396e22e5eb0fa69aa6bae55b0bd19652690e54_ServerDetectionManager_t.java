 package com.jostrobin.battleships.service.network.rmi.chat.server;
 
 import java.io.IOException;
 import java.net.*;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Enumeration;
 import java.util.List;
 
 import com.jostrobin.battleships.exception.BattleshipServiceException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * The UDP server dealing with all the incoming network packets in the process of finding the servers in the network.
  * It answers if another application instance is looking for servers and forwards answers to our requests to the
  * callback listeners.
  *
  * @author joscht
  */
 public class ServerDetectionManager implements Runnable
 {
     private static final Logger LOG = LoggerFactory.getLogger(ServerDetectionManager.class);
 
     /**
      * Port on which we try to find other servers.
      */
     private static final int DETECTION_PORT = 4242;
 
     /**
      * Port on which we run
      */
     private static final int LISTENING_PORT = 4242;
 
     private static final int BUFFER_SIZE = 64;
 
     // TODO: Replace at compile time odr so
     private static final String VERSION = "26102011";
 
     private static final String ARE_YOU_THERE = "areyouthere";
 
     private static final String YES_I_AM = "yesiam";
 
     private DatagramSocket socket;
 
     private boolean running = true;
 
     /**
      * A list of listeners to notify when we receive messages.
      */
     private List<ServerDetectionListener> listeners = null;
 
     public ServerDetectionManager(List<ServerDetectionListener> listeners)
     {
         try
         {
             socket = new DatagramSocket(LISTENING_PORT);
         }
         catch (SocketException e)
         {
             throw new BattleshipServiceException("Could not open server socket.", e);
         }
         if (listeners == null)
         {
             listeners = new ArrayList<ServerDetectionListener>();
         }
         this.listeners = Collections.unmodifiableList(listeners);
     }
 
     @Override
     public void run()
     {
         LOG.debug("Detection server up an running...");
         while (running)
         {
             byte[] buffer = new byte[BUFFER_SIZE];
             DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
             try
             {
                 socket.receive(packet);
                 // a server has answered, decide on the answer whether it must be forwarded to the callback
 
                 InetAddress address = packet.getAddress();
 
                 String message = new String(buffer, "UTF-8").trim();
                 LOG.trace("Received a UDP message");
                 if (message.startsWith(VERSION))
                 {
                     // someone is looking for servers, answer him
                     if (message.equals(VERSION + ARE_YOU_THERE))
                     {
                         // we need to check if it was our own broadcast and ignore if that's the case
                         boolean isForeign = true;
                         Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
                         for (NetworkInterface netint : Collections.list(nets))
                         {
                             List<InterfaceAddress> interfaceAddresses = netint.getInterfaceAddresses();
                             for (InterfaceAddress interfaceAddress : interfaceAddresses)
                             {
                                 if (interfaceAddress.getAddress().equals(address))
                                 {
                                    isForeign = false;
                                 }
                             }
                         }
 
                         // it's not our own broadcast
                         if (isForeign)
                         {
                             LOG.debug("received 'are you there' call from a client.", address);
                             answer(address);
                         }
                     }
                     // we were looking for servers and found one
                     else if (message.equals(VERSION + YES_I_AM))
                     {
                         LOG.info("Server detected at" + address);
                         for (ServerDetectionListener callback : listeners)
                         {
                             callback.addServer(address);
                         }
                     }
                 }
                 else
                 {
                     // discard, wrong version or not meant for us
                 }
             }
             catch (IOException e)
             {
                 throw new BattleshipServiceException("Could not read from server socket.", e);
             }
         }
     }
 
     /**
      * Sends an answer about our existence back to the specified address.
      *
      * @param address
      */
     private void answer(InetAddress address)
     {
         byte[] buffer = new byte[BUFFER_SIZE];
 
         // send UDP packets to all the possible server nodes
         DatagramSocket answerSocket = null;
         try
         {
             String message = VERSION + YES_I_AM;
             buffer = message.getBytes("UTF-8");
 
             answerSocket = new DatagramSocket();
             DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, LISTENING_PORT);
             answerSocket.send(packet);
         }
         catch (Exception e)
         {
             LOG.warn("Could not respond to client.", e);
         }
         finally
         {
             if (answerSocket != null)
             {
             	answerSocket.close();
             }
         }
     }
 
     /**
      * Sends a broadcast to all the nodes in the network to find other games.
      */
     public void sendBroadcast()
     {
         byte[] buffer = new byte[BUFFER_SIZE];
 
         // send UDP packets to all the possible server nodes
         DatagramSocket socket = null;
         try
         {
             String message = VERSION + ARE_YOU_THERE;
             buffer = message.getBytes("UTF-8");
 
             socket = new DatagramSocket();
             socket.setBroadcast(true);
 
             // send a broadcast to the broadcast addresses of any network interface available
             Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
             for (NetworkInterface netint : Collections.list(nets))
             {
                 List<InterfaceAddress> interfaceAddresses = netint.getInterfaceAddresses();
                 for (InterfaceAddress interfaceAddress : interfaceAddresses)
                 {
                     if (interfaceAddress.getBroadcast() != null)
                     {
                         InetAddress address = interfaceAddress.getBroadcast();
                         DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, DETECTION_PORT);
                         socket.send(packet);
                     }
                 }
             }
         }
         catch (SocketException e)
         {
             throw new BattleshipServiceException("Could not create socket to detect other servers.", e);
         }
         catch (IOException e)
         {
             throw new BattleshipServiceException("Could not send UDP packet to detect other servers.", e);
         }
         finally
         {
             if (socket != null)
             {
                 socket.close();
             }
         }
     }
 }
