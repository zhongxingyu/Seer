 package edu.purdue.cs252.lab06;
 
 import java.util.*;
 import java.net.*;
 import java.io.*;
 
 import android.os.Handler;
 import android.os.Message;
 
 public class DirectoryClientListener implements Runnable
 {    
     private Socket _clientSocket = null;
     private Handler _UIthread;
     
     DirectoryClientListener(Socket clientSocket, Handler UIthread)
     {
         _clientSocket = clientSocket;
         _UIthread = UIthread;
     }
     
     public void run()
     {
         try
         {            
             // load in the stream data
             InputStream is = _clientSocket.getInputStream();
             BufferedReader br = new BufferedReader(new InputStreamReader(is));
 
             String line;
 
             while((line = br.readLine()) != null)
             {
             	if (line.startsWith("<IncomingCall>"))
                 {
                     String username = br.readLine();
                     username = username.substring(10, username.length() - 11);
                     
                     String ipAddress = br.readLine();
                    ipAddress = ipAddress.substring(11, ipAddress.length() - 11);
                     
                     Message msg = new Message();
                     msg.what = 1;
                     msg.obj = new String[] { username, ipAddress };
                     
                     _UIthread.sendMessage(msg);
                 }
                 else if (line.startsWith("<Hangup>"))
                 {
                 	Message msg = new Message();
                     msg.what = 2;
                     
                     _UIthread.sendMessage(msg);
                 }
                 else if (line.startsWith("<Busy>"))
                 {
                 	Message msg = new Message();
                     msg.what = 3;
                     
                     _UIthread.sendMessage(msg);
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
                     
                     Message msg = new Message();
                     msg.what = 0;
                     msg.obj = users;
                     
                     _UIthread.sendMessage(msg);
                 }
             }
             
             br.close();
             is.close();            
         }
         catch (Exception ex)
         {
             // TODO: handle exception
         	System.out.println(ex.getMessage());
         }
     }
 }
 
