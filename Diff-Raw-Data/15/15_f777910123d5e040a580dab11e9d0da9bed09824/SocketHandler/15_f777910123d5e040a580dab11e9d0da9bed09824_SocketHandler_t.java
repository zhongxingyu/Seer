 
 package com.vu.se.hm.net;
 
 import java.io.*;
 import java.net.*;
 import java.util.concurrent.ConcurrentLinkedQueue;
 
 /**
  * SocketHandler. also known as player. The individual socket handlers for each player
  * used by GuesserServer.
  */
 public class SocketHandler implements Runnable{
     private static volatile ServerSocket server; //Only one server socket is need for all players
     private static int maxplayers = 4;
     private static int numplayers = 0;
     private Socket socket;
     private ObjectOutputStream out;
     private ObjectInputStream in;
     private boolean connected;
     private volatile ConcurrentLinkedQueue<HangmanPacket> queue;
     
     /**
      * Constructor
      * @param queue shared queue between all SocketHandlers and Guesser server to store
      * the various incoming packets.
      * @param port 
      */
     public SocketHandler(ConcurrentLinkedQueue<HangmanPacket> queue, int port){
         try{
             this.queue = queue;
             connected = false;
             if(server == null){
                 server = new ServerSocket(port, maxplayers); //If this is the first player create server sucket.
             }
         } catch (IOException e){
             System.out.println(e);
         }       
     }
     
     /**
      * waitForConnection(). this is called when we want the handler to listen for
      * an incoming player connection. Will hang till connection is established.
      */
     public void waitForConnection(){
         System.out.println("Waiting for connections");
         try{
             socket = server.accept(); //Will hang till connection is established
             out = new ObjectOutputStream(socket.getOutputStream());
             in = new ObjectInputStream(socket.getInputStream());
             connected = true;
            numplayers++;
         } catch (IOException e){
             System.out.println(e);
         }
     }
     
     /**
      * stopWaitingForConnections(). Stops the server from listening for connections.
      */
     public void stopWaitingForConnections(){
         try{
             server.close();
             System.out.println("No longer waiting for connections.");
         } catch (IOException e){
             System.out.println(e);
         }
     }
     
     /**
      * close(). Close out all the connections for this instance.
      * if the last player is removed, then tell the server socket to stop listening.
      */
     public void close(){
         if(connected){
             try{
                 out.close();
                 in.close();
                 socket.close();
                 connected  = false;
                 numplayers--;
                 System.out.println("Close");
             if(numplayers < 1){
                 stopWaitingForConnections();
             }
             } catch (IOException e){
                 System.out.println(e);
             }
         }
         
     }
     
     /**
      * sendData(). Sends the data out the output stream and to client.
      * @param packet HangmanPacket
      */
     public void sendData(HangmanPacket packet){
         try{
             out.writeObject(packet);
         } catch (IOException e){
             System.out.println(e);
         }
     }
     
     /**
      * run(). Called when new thread is started
      * Waits for incoming connections, then while connected reads input objects
      * and adds them to the queue for the GuesserServer to process. Finally closes
      * all streams and connections.
      */
     @Override
     public void run() {
         waitForConnection();
         HangmanPacket packet;
         System.out.println("Connected");
         while(connected){
             try{
                 packet = (HangmanPacket) in.readObject();
                 addToQueue(packet);
             } catch (IOException e){
                 System.out.println();
             } catch (ClassNotFoundException e){
                 System.out.println();
             }
         }
         close();
     }
     
     public boolean isConnected(){
         return connected;
     }
     
     public synchronized void addToQueue(HangmanPacket packet){
         queue.add(packet);
     }
     
     /**
      * Finalize(). Should not be manually called, here in case of logic breakdown
      * @throws Throwable 
      */
     @Override
     public void finalize() throws Throwable{
         close();
         super.finalize();
     }
 }
