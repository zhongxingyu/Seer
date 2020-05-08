 package com.cs252.lab06;
 
 import java.util.*;
 import java.net.*;
 import java.io.*;
 
 public class DirectoryClient
 {    
     private String _ipAddress;
    private int _port = 6900;
     
     
     private Socket _socket = null;
     
     public DirectoryClient(String ipAddress)
     {
         _ipAddress = ipAddress;
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
     
     public ArrayList<User> getDirectory() throws IOException
     {
         if (_socket == null)
         {
             throw new IOException("Not connected to server.");
         }
         
         ArrayList<User> directory = new ArrayList<User>();
         
         try
         {
             PrintWriter out = new PrintWriter(_socket.getOutputStream(), true);            
             out.println("<GetDirectory></GetDirectory>");
             
             InputStream is = _socket.getInputStream();
             BufferedReader br = new BufferedReader(new InputStreamReader(is));
             
             String line;
             
             while ((line = br.readLine()) != null)
             {
                 if (line.startsWith("<User>"))
                 {
                     String username = br.readLine();
                     username = username.substring(10, username.length() - 11);
                     
                     String ipAddress = br.readLine();
                     ipAddress = ipAddress.substring(11, ipAddress.length() - 12);
                     
                     String status = br.readLine();
                     status = status.substring(8, status.length() - 9);
                     
                     User u = new User(username, ipAddress);
                     u.setStatus(status);
                     
                     directory.add(u);
                 }
                 else
                 {
                     // TODO: this needs to be refactored 
                     if (line.startsWith("<Directory>") == false && line.startsWith("</User>") == false)
                         break;
                 }
             }
         }
         catch (Exception ex)
         {
             // TODO: handle this exception
         }
         
         return directory;
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
