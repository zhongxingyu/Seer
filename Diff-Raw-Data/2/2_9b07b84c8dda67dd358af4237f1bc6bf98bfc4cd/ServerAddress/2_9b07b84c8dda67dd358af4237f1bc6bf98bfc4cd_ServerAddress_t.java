 package com.slugsource.steam.servers;
 
 import java.net.InetAddress;
 
 /**
  *
  * @author Nathan Fearnley
  */
 public class ServerAddress
 {
 
     private InetAddress address;
     private int port;
 
     public ServerAddress()
     {
     }
 
    public ServerAddress(InetAddress address, int port)
     {
         this.address = address;
         this.port = port;
     }
 
     public InetAddress getAddress()
     {
         return address;
     }
 
     public void setAddress(InetAddress address)
     {
         this.address = address;
     }
 
     public int getPort()
     {
         return port;
     }
 
     public void setPort(int port)
     {
         this.port = port;
     }
 }
