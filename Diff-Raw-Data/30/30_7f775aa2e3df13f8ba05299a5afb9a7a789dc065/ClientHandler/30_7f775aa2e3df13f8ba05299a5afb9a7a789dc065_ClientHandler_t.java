 /*
  *  Copyright Mattias Liljeson Sep 14, 2011
  */
 package gameserver;
 
 import common.*;
 import java.io.*;
 import java.net.*;
 import java.util.ArrayList;
 
 /**
  *
  * @author Mattias Liljeson <mattiasliljeson.gmail.com>
  */
 public class ClientHandler implements Runnable{
     public final static String SERVER_HOSTNAME = "localhost";
     public final static int COMM_PORT = 5679;
     ServerSocket servSock;
     ArrayList<ClientConnection> clients; // TODO: Change impl to Map?
     int numClients = 0;
     GameServer server;
     
     public ClientHandler(GameServer server){
         try{
             servSock = new ServerSocket(COMM_PORT);
         }catch(IOException ignore){}
         
         this.server = server;
         clients = new ArrayList<ClientConnection>();
        System.out.println("ClientHandler started");
     }
     
     @Override
     public void run() {
         //throw new UnsupportedOperationException("Not supported yet.");
         //Wait for clients to connect
         //When someone has connected create a new ClientConnection
         
         Socket clientSock = null;
         while(true){
             try{
                 clientSock = servSock.accept();
             }catch(IOException ex){
                 System.out.println("Error when waiting for connection");
                 ex.printStackTrace();
             }
 
             System.out.println("A new client has connected");
 
             if(clientSock != null){
                 ClientConnection clientConn = new ClientConnection(clientSock, this, numClients);
                 clients.add(clientConn);
                 numClients++;
                 
                 Thread thread = new Thread(clientConn);
                 thread.start();
                 clientSock = null;
             }
 
            System.out.println("Client has been served by ClientHandler. "
                    + "Now looking for new connections");
             int i = 0; 
         }
     }
     
     public void pollClients(){
         for(ClientConnection client : clients){
             client.poll();
         }
     }
     
     public void sendRaceUpdate(RaceUpdate update){
         for(ClientConnection client : clients){
             client.sendRaceUpdate(update);
         }
     }
     
     public void updateKeyStates(int id, KeyStates keyStates){
         server.updateKeyStates(id, keyStates);
     }
 }
