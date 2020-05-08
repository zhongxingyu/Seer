package com.cs252.lab06;
 
 import java.util.*;
 import java.net.*;
 import java.io.*;
 
 public class DirectoryClientListener implements Runnable
 {    
     private Socket _clientSocket = null;
     private DirectoryScreen _UIthread;
     
     DirectoryClientListener(Socket clientSocket, DirectoryScreen UIthread)
     {
         _clientSocket = clientSocket;
         _UIthread = UIthread;
     }
     
     public void run()
     {
         System.out.println("Client listener starts running.");
         
         try
         {            
             // load in the stream data
             InputStream is = _clientSocket.getInputStream();
             BufferedReader br = new BufferedReader(new InputStreamReader(is));
 
             String line;
 
             while((line = br.readLine()) != null)
             {                
                 // IncomingCall request
                 if (line.startsWith("<IncomingCall>"))
                 {
                     String username = br.readLine();
                     username = username.substring(10,username.length() - 11);
                     String ipAddress = br.readLine();
                     ipAddress = ipAddress.substring(11,ipAddress.length() - 11);
                     
                     //TODO: call UI method, class of that method required 
                     _UIthread.displayIncomingCall(username, ipAddress);
                 }
                 
                 // Hang up request
                 else if (line.startsWith("<Hangup>"))
                 {
                     //TODO: call UI method, class of that method required 
                 	_UIthread.displayHangup();
                 }
                 
                 // Busy request
                 else if (line.startsWith("<Busy>"))
                 {
                     //TODO: call UI method, class of that method required 
                 	_UIthread.displayBusy();
                 }
                 
                 else if (line.startsWith("<Directory>"))
                 {
                     ArrayList<User> users = new ArrayList<User>();
                     
                     while(!(line = br.readLine()).startsWith("</Directory"))
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
 
                             users.add(u);                            
                         }
                     }                    
                     
                     _UIthread.updateDirectory(users);
                 }
             }
             
             br.close();
             is.close();            
         }
         catch (Exception ex)
         {
             // TODO: handle exception
         }
     }
 }
 
