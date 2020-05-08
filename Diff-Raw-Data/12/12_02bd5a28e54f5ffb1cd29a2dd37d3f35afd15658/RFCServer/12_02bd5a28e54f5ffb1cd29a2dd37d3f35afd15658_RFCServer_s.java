 package edu.ncsu.ip.gogo.peer.server;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.ObjectInputStream;
 import java.net.ServerSocket;
 import java.net.Socket;
 
 import edu.ncsu.ip.gogo.dao.MessageRequest;
 import edu.ncsu.ip.gogo.peer.handler.PeerRequestHandler;
 
 
 public class RFCServer implements Runnable {
     
     private int rfcServerPort;
 
     public RFCServer(int port) {
         this.rfcServerPort = port;
     }
 
     public void run() {
         
         ServerSocket serverSocket = null;
         try {
             serverSocket = new ServerSocket(rfcServerPort);
         } catch (IOException e) {
             System.out.println("RFCServer.run() - Could not start RFC server on : "+ rfcServerPort + ". Exiting!");
             System.exit(1);
         }
         
         System.out.println("RFCServer.run() - Started RfCServer on port: " + rfcServerPort + ". Waiting for peer requests ...");
         
         while (true) {
            Socket clientSocket = null;
            InputStream is = null;
            ObjectInputStream ois = null;
             try {
                 clientSocket = serverSocket.accept();
                 is = clientSocket.getInputStream();   
                 ois = new ObjectInputStream(is);
                 MessageRequest msg = (MessageRequest)ois.readObject();
                 PeerRequestHandler handler = new PeerRequestHandler(clientSocket, msg);
                 Thread handlerThread = new Thread(handler);
                 handlerThread.start();
             } catch (IOException e) {
                 System.out.println("RFCServer.run() - Accept failed with IOException: " + e.getMessage());
                 e.printStackTrace();
             } catch (Exception e){
                 System.out.println("RFCServer.run() - Accept failed with Exception: " + e.getMessage());
                 e.printStackTrace();
             } finally { 
                 try {
                    ois.close();
                    is.close();
                 } catch (Exception e) {
                     System.out.println("RFCServer.run() - Exception in finally block: " + e.getMessage());
                     e.printStackTrace();
                 }
             }
                 
         }
     }
 }
