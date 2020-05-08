 package ru.spbau.cheque.server.reciever;
 
 import java.io.IOException;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 
 public class ChequeRecognitionServer {
     private final ServerSocket myServerSocket;
     private final ExecutorService pool;
 
     public ChequeRecognitionServer(int port) throws NoConnectionException{
         try{
            myServerSocket = new ServerSocket(port);
         } catch (IOException e){
             System.err.println("Can't listen port 3843");
             throw new NoConnectionException();
         }
         pool = Executors.newCachedThreadPool();
     }
 
     public void start() throws NoConnectionException {
         try{
             for(;;){
                 pool.execute(new Worker(myServerSocket.accept()));
             }
         } catch (IOException e){
                 System.err.println("Can't accept on ServerSocket.");
                 throw new NoConnectionException();
         }
     }
 }
