package edu.purdue.cs252.lab06;
 
 import java.util.*;
 import java.net.*;
 import java.io.*;
 
 public class DirectoryClient
 {    
     private String _ipAddress;
     private int _port = 6900;    
     
     private Socket _socket = null;
     
     private DirectoryScreen _UIthread;
     
     public DirectoryClient(String ipAddress, DirectoryScreen UIthread)
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
             
             Thread t1 = new Thread(hc);
             t1.start();
             
             // initialize listener
             DirectoryClientListener dcl = new DirectoryClientListener(_socket, _UIthread);
             
             Thread t2 = new Thread(dcl);
             t2.start();
         }
         catch (Exception ex)
         {
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
     
     public boolean sendCall(int destinationIPAddress) throws IOException
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
     
     public boolean hangUp(int destinationIPAddress) throws IOException
     {
         if (_socket == null)
         {
             throw new IOException("Not connected to server.");
         }
         
         try
         {
             PrintWriter out = new PrintWriter(_socket.getOutputStream(), true);
             
             out.println("<HangUp>");
             out.println("<IPAddress>" + destinationIPAddress + "</IPAddress>");
             out.println("</HangUp>");
         }
         catch (Exception ex)
         {
             return false;
         } 
         
         return true;
     }
 }
 
