 package com.epita.mti.plic.opensource.controlibserver.connection;
 
 import java.io.IOException;
 import java.net.*;
import java.util.Enumeration;
import java.util.HashMap;
 
 /**
  * This class contains methods to open a connection from the server on a network
  * interface and to obtain the list of available interfaces
  *
  * @author Mickael "Mogmi" Bidon
  */
 public class ConnectionManager
 {
 
   /**
    * The ServerSocket which will handle the connection
    */
   private ServerSocket inputSocket;
   private ServerSocket outputSocket;
 
   public ConnectionManager()
   {
   }
 
   /**
    * Open a connection on the network
    *
    * @param port The port on which the connection is opened
    * @return
    */
  public boolean openConnection(int input, int output) throws IOException
   {
     if (this.outputSocket != null)
     {
       return (false);
     }
     this.outputSocket = new ServerSocket(output);
 
     if (this.inputSocket != null)
     {
       return (false);
     }
     this.inputSocket = new ServerSocket(input);
 
     return (true);
   }
 
   /**
    * Close the connection
    *
    * @throws IOException
    */
   public void closeConnection() throws IOException
   {
     this.outputSocket.close();
     this.inputSocket.close();
   }
 
   /**
    * Get the list of available network interfaces
    *
    * @return A map containing the name of the interface and the IP address
    * linked to it
    */
   public HashMap<String, HashMap<String, String>> getInterfaces() throws SocketException
   {
     Enumeration e = NetworkInterface.getNetworkInterfaces();
     HashMap<String, HashMap<String, String>> res = new HashMap<String, HashMap<String, String>>();
 
     while (e.hasMoreElements())
     {
       NetworkInterface ni = (NetworkInterface) e.nextElement();
       HashMap<String, String> hash = new HashMap<String, String>();
 
       Enumeration e2 = ni.getInetAddresses();
       while (e2.hasMoreElements())
       {
         InetAddress ip = (InetAddress) e2.nextElement();
         if (ip instanceof Inet4Address)
         {
           hash.put("IPV4", ip.getHostAddress());
         } else if (ip instanceof Inet6Address)
         {
           hash.put("IPV6", ip.getHostAddress());
         }
 
       }
       res.put(ni.getName(), hash);
     }
     return res;
   }
 
   public ServerSocket getInputSocket()
   {
     return inputSocket;
   }
 
   public ServerSocket getOutputSocket()
   {
     return outputSocket;
   }
 
 
 }
