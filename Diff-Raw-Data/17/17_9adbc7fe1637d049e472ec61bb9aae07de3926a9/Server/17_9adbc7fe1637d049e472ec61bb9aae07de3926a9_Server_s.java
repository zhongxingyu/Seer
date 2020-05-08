 package com.tshap88.chat;
 
 import java.net.*;
 import java.io.*;
 
 public class Server {
 
     public static void main(String[] args) {
 
         ServerSocket ssocket;
 
         try {
             ServerConnections sc = new ServerConnections();
             ssocket = new ServerSocket(ConfFile.getPORT());
             System.out.println("Server is up: " + ssocket.getInetAddress().getHostName());
 
             while (true) {
 
                 Socket socket = ssocket.accept();
                 sc.addServerConnection(socket);
                 Thread threads = new Thread(new ServerImpRun(sc));
                 threads.start();
             }
 
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 }
