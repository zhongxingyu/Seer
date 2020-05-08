 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package aether.net;
 
 import aether.conf.ConfigMgr;
 import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.InetAddress;
 import java.net.InterfaceAddress;
 import java.net.NetworkInterface;
 import java.net.SocketException;
 import java.util.Iterator;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author aniket
  */
 public class NetMgr {
     
     
     
     private static String interfaceName = ConfigMgr.getInterfaceName();
     
     
     
     private int port;
     private DatagramSocket socket;
     
     
     public NetMgr (int port) throws SocketException {
         
         socket = new DatagramSocket (port, ConfigMgr.getLocalIp());
     }
     
     
     
     
     
     
     /**
      * Set the timeout for the socket
      * @param timeout   int having timeout in milliseconds
      * @throws SocketException 
      */
     public void setTimeout (int timeout) throws SocketException {
         socket.setSoTimeout(timeout);
     }
     
     
     
     
     
     
     /** 
      * Convert the object into series of bytes
      * @param m Object to be converted in a byte array.
      * @return  byte array containing the bytes of the object
      * @throws IOException 
      */
     private byte[] serialize (Object m) throws IOException {
         ByteArrayOutputStream bOut = new ByteArrayOutputStream();
         ObjectOutputStream oOut = new ObjectOutputStream(bOut);
         oOut.writeObject(m);
         return bOut.toByteArray();
     }
     
     
     
     /**
      * Send a message over the network
      * @param m     Message m to be sent
      * @throws IOException 
      */
     public void send (Message m) throws IOException {
         byte[] datagramPayload = serialize(m);
         DatagramPacket packet = new DatagramPacket(datagramPayload, 
                 datagramPayload.length, m.getDestIp(), port);
         
         socket.send(packet);
     }
     
     
     
     /**
      * Receive message from the network
      * @return  Received message. null on failure
      * @throws IOException 
      */
     public Message receive () throws IOException {
         DatagramPacket dResponse = new DatagramPacket(new byte[1024],1024);
         socket.receive(dResponse);
         Message m = null;
         try {
             m = deserializeMessage(dResponse.getData());
         } catch (ClassNotFoundException ex) {
             System.err.println("[ERROR]: Retrieving message from datagram"
                     + " class not found");
             ex.printStackTrace();
         }
         return m;
     }
     
     
     
     /**
      * Deserialize a message from the byte stream in a datagram
      * @param bytes     Message byte stream
      * @return          Deserialized message
      * @throws IOException
      * @throws ClassNotFoundException 
      */
     private Message deserializeMessage (byte[] bytes) throws IOException, 
             ClassNotFoundException {
         Message m;
         m = (Message) deserialize (bytes);
         return m;
     }
     
     
     /**
      * Deserialize an object from a byte stream
      * @param bytes     Object bytes
      * @return          Deserialized object
      * @throws IOException
      * @throws ClassNotFoundException 
      */
     private Object deserialize (byte[] bytes) throws IOException, 
             ClassNotFoundException {
         ByteInputStream bIn = new ByteInputStream();
         ObjectInputStream oIn = new ObjectInputStream(bIn);
         return oIn.readObject();
     }
     
     
     
     /**
      * Return the broadcast address of the cluster interface.
      * @return  InetAddress having the broadcast address of the cluster 
      *          interface. null on failure.
      */
     public static InetAddress getBroadcastAddr () {
         
         NetworkInterface iFace;
         try {
             iFace = NetworkInterface.getByName(interfaceName);
             if (iFace.isLoopback() || (! iFace.isUp()) ) {
                 return null;
             }
         } catch (SocketException e) {
             System.err.println("[ERROR]: could not find the interface");
             return null;
         }
         
         Iterator i = iFace.getInterfaceAddresses().iterator();
         
         while (i.hasNext()) {
             InterfaceAddress addr = (InterfaceAddress) i.next();
             if (addr == null) {
                 continue;
             }
             InetAddress broadcast = addr.getBroadcast();
             if (broadcast == null) {
                 continue;
             } else {
                 return broadcast;
             }
         }
         return null;
         
     }
 }
