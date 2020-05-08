 /* 
  * Copyright (c) 2012, noxan
  * See LICENSE for details.
  */
 
 package com.github.noxan.aves.demo;
 
 import java.io.IOException;
 
 import com.github.noxan.aves.net.Connection;
 import com.github.noxan.aves.server.Server;
 import com.github.noxan.aves.server.ServerHandler;
 import com.github.noxan.aves.server.SocketServer;
 
 public class DemoServer implements ServerHandler {
     public static void main(String[] args) {
         Server server = new SocketServer(new DemoServer());
         try {
             server.start();
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     @Override
     public void readData(Connection connection, Object data) {
	System.out.println(connection + ": " + data);
     }
 
     @Override
     public void clientConnect(Connection connection) {
	System.out.println(connection + " connect");
     }
 
     @Override
     public void clientDisconnect(Connection connection) {
	System.out.println(connection + " disconnect");
     }
 
     @Override
     public void clientLost(Connection connection) {
	System.out.println(connection + " lost");
     }
 }
