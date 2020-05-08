package com.cs252.lab06;
 
 import java.io.*; 
 import java.net.*;
 
 public class HeartbeatClient implements Runnable
 {
     Socket _clientSocket = null;
      
     HeartbeatClient(Socket clientSocket)
     {
         _clientSocket = clientSocket;
     } 
 
     public void run()
     {
         try
         {
             PrintWriter out = new PrintWriter(_clientSocket.getOutputStream(), true);
 
             while (true)
             {
                 out.println("<KeepAlive></KeepAlive");
                 
                 try
                 {
                     Thread.sleep(5000);
                 }
                 catch (InterruptedException ex)
                 {
                     // TODO: handle this exception
                 }
             }
         }
         catch (IOException ex)
         {
             // TODO: handle this exception
         }
     }
 }
 
