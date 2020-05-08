 /*
  *  Copyright Mattias Liljeson Sep 14, 2011
  */
 package gameserver;
 
 import common.*;
 import java.awt.Color;
 import java.net.*;
 import java.util.ArrayList;
 
 /**
  *
  * @author Mattias Liljeson <mattiasliljeson.gmail.com>
  */
 public class ClientHandler implements Runnable{
     public final static String SERVER_HOSTNAME = "localhost";
     public final static int COMM_PORT = 5679;
     //ServerSocket servSock;
     private ArrayList<ClientConnection> clientConnections; // TODO: Change impl to Map?
     private Object lockClientConnections = new Object();
     private int nextClientID = 0; //Client ID counter. Increases with every connected client. Cannot use clients.size() since it decreases when clients are removed. We want a unique number.
     private GameServer server;
     private Channel channel;
     
     public ClientHandler(GameServer server){
 //        try{
 //            servSock = new ServerSocket(COMM_PORT);
 //        }catch(IOException ignore){}
         channel = new Channel(COMM_PORT);
         
         this.server = server;
         clientConnections = new ArrayList<ClientConnection>();
         System.out.println("ClientHandler started");
     }
     
     @Override
     public void run() {
         // Wait for clients to connect.
         // When a client has connected, create a new ClientConnection
         Socket clientSock = null;
         channel.startServer();
         
         while(true){
             clientSock = channel.accept();
             System.out.println("A new client has connected");
 
             if(clientSock != null){
                 ClientConnection clientConn = new ClientConnection(clientSock, this, nextClientID);
                 synchronized(this) {
                     clientConnections.add(clientConn);
                 }
                 // TODO: Add a car for the client, fetch car color etc
                 Car clientCar = new Car(400,200,0, Color.red);
                 server.addCar(nextClientID, clientCar);
 		
                 //increase the id counter to prepare for the next client connection
                 nextClientID++;
 				
                 Thread thread = new Thread(clientConn);
                 thread.start();
                 clientSock = null;
             }
 
             System.out.println("Client has been served by ClientHandler. "
                     + "Now looking for new connections");
         }
     }
     
     public void pollClients(){
         synchronized(this) {
             for(ClientConnection client : clientConnections){
                 client.poll();
             }
         }
     }
     
    public void removeClient(int id) {
         boolean result = false;
         synchronized(this) {
             if(clientConnections.remove(id) != null)
                 result = true;
         }
         return result;
     }
     
     public void sendRaceUpdate(RaceUpdate update){
         synchronized(this) {
             for(ClientConnection client : clientConnections){
                 client.sendRaceUpdate(update);
             }
         }
     }
     
     public void updateKeyStates(int id, KeyStates keyStates){
         server.updateKeyStates(id, keyStates);
     }
 }
