 package com.madavan.bridge.server;
 
 import java.io.IOException;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.ArrayList;
 
 public class BridgeServer extends Thread {
 
   private ServerSocket _serverSocket;
   
   public static void main(String[] args) {
     if(args.length != 1)
       System.out.println("Usage: java BridgeServer port");
     else {
     	try {
     	      new BridgeServer(Integer.parseInt(args[0])).start();
     	} catch (IOException e) {
     		e.printStackTrace();
     	}
     }
   }
   
   public BridgeServer(int port) throws IOException {
     _serverSocket = new ServerSocket(port);
     System.out.println("BridgeServer@constructor: ServerSocket initialized.");
   }
   
   @Override
   public void run() {
     try {
       System.out.println("BridgeServer@run: ServerSocket accepting clients.");
       
       while(true) {
         // ArrayList<BridgePlayer> players = new ArrayList<BridgePlayer>();
         // for(int i = 0; i < 4; i++)
         //  players.put(new BridgePlayer(_serverSocket.accept());
         // new BridgeThread(players);
       }
     } catch(IOException e) {
       System.out.println("BridgeThread@run: " + e.toString());
       e.printStackTrace();
     }
   }
 }
