 package edu.purdue.cs252.lab06;
 
 import java.util.*;
 import java.net.*;
 import java.io.*;
 
 import android.os.Handler;
 
 public class DirectoryClient
 {    
     private String _ipAddress;
     private int _port = 6900;    
     
     private Socket _socket = null;
     
     private Handler _UIthread;
     
     public DirectoryClient(String ipAddress, Handler UIthread)
     {
         _ipAddress = ipAddress;
         _UIthread = UIthread;
     }
     
     public boolean connect()
     {            
         try
         {
         	_socket = new Socket(InetAddress.getByName(_ipAddress), _port);
          	
         	// initialize keep-alive client 
             HeartbeatClient hc = new HeartbeatClient(_socket);
             
             Thread t1 = new Thread(hc, "HeartbeatClient");
             t1.start();
             
             // initialize listener
             DirectoryClientListener dcl = new DirectoryClientListener(_socket, _UIthread);
             
             Thread t2 = new Thread(dcl, "DirectoryClientListener");
             t2.start();
             
             System.out.println("DirectoryClientListener running on thread " + t2.getId());
         }
         catch (Exception ex)
         {
             System.out.println(ex.getMessage());
         	
         	return false;
         }
         
         return true;
     }
     
     public boolean addUser(String username) throws IOException
     {
         if (_socket == null)
         {
             throw new IOException("Not connected to server.");
         }
         
         try
         {
             PrintWriter out = new PrintWriter(_socket.getOutputStream(), true);
             
             out.println("<AddUser>");
             out.println("<Username>" + username + "</Username>");
             out.println("</AddUser>");
         } 
         catch (Exception ex) 
         {
             return false;
         }
         
         return true;
     }
     
     public boolean getDirectory() throws IOException
     {
         if (_socket == null)
         {
             throw new IOException("Not connected to server.");
         }
         
         try
         {
             PrintWriter out = new PrintWriter(_socket.getOutputStream(), true);            
             
             out.println("<GetDirectory></GetDirectory>");
         }
         catch (Exception ex)
         {
             return false;
         }
         
         return true;
     }
     
     public boolean sendCall(String destinationIPAddress) throws IOException
     {
         if (_socket == null)
         {
             throw new IOException("Not connected to server.");
         }
         
         try
         {
             PrintWriter out = new PrintWriter(_socket.getOutputStream(), true);
             
             out.println("<SendCall>");
             out.println("<IPAddress>" + destinationIPAddress + "</IPAddress>");
             out.println("</SendCall>");
         }
         catch (Exception ex)
         {
             return false;
         } 
         
         return true;
     }
     
     public boolean acceptCall(String destinationIPAddress) throws IOException
     {
         if (_socket == null)
         {
             throw new IOException("Not connected to server.");
         }
         
         try
         {
             PrintWriter out = new PrintWriter(_socket.getOutputStream(), true);
             
             out.println("<AcceptCall>");
             out.println("<IPAddress>" + destinationIPAddress + "</IPAddress>");
             out.println("</AcceptCall>");
         }
         catch (Exception ex)
         {
             return false;
         } 
         
         return true;
     }
     
     public boolean hangUp(String destinationIPAddress) throws IOException
     {
         if (_socket == null)
         {
             throw new IOException("Not connected to server.");
         }
         
         try
         {
             PrintWriter out = new PrintWriter(_socket.getOutputStream(), true);
             
            out.println("<Hangup>");
             out.println("<IPAddress>" + destinationIPAddress + "</IPAddress>");
            out.println("</Hangup>");
         }
         catch (Exception ex)
         {
             return false;
         } 
         
         return true;
     }
 }
 
