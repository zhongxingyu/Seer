 package pro.kornev.kcar.proxy;
 
 import com.google.gson.Gson;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import pro.kornev.kcar.protocol.Data;
 import pro.kornev.kcar.protocol.Protocol;
 
 import java.io.*;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.Queue;
 
 /**
  * User: kvv
  * Date: 17.10.13
  * Time: 20:32
  */
 public final class NetworkService implements Runnable {
     private static final int MAX_ERRORS = 10;
 
     private final Logger log;
     private int port;
     private Queue<Data> inputQueue;
     private Queue<Data> outputQueue;
     private Reader reader = null;
     private Writer writer = null;
    private boolean clientAccepted = false;
 
     public NetworkService(int port, Queue<Data> inputQueue, Queue<Data> outputQueue) {
         this.port = port;
         this.log = LoggerFactory.getLogger("Network service: " + port);
         this.inputQueue = inputQueue;
         this.outputQueue = outputQueue;
     }
 
     @Override
     public void run() {
         try {
             log.info("Run listener on port {}", port);
             ServerSocket listener = new ServerSocket(port);
             log.debug("Start cleaner", port);
             new Thread(new Cleaner(listener)).start();
             while (!listener.isClosed()) {
                 Socket client = listener.accept();
                 log.info("Client accepted");
 
                 log.debug("Shutdown reader and writer");
                 if (reader != null) {
                     reader.shutdown();
                 }
                 if (writer != null) {
                     writer.shutdown();
                 }
 
                 log.debug("Start new reader and writer");
                 reader = new Reader(client);
                 writer = new Writer(client);
                 new Thread(reader).start();
                 new Thread(writer).start();
                 log.debug("Paused cleaner");
                setClientAccepted(true);
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     class Reader implements Runnable {
         private final Logger log;
         private Socket client;
 
         Reader(Socket client) {
             this.log = LoggerFactory.getLogger("Reader: " + port);
             this.client = client;
         }
 
         @Override
         public void run() {
             try {
                 DataInputStream input = new DataInputStream(client.getInputStream());
 
                 while (!client.isClosed()) {
                     if (input.available() == 0) {
                         sleep();
                         continue;
                     }
 
                     Data data = Protocol.fromInputStream(input);
                     log.debug("Read command: " + data.cmd);
                     inputQueue.add(data);
 
                     if (data.cmd == 1 && data.bData == 0) {
                         Data response = new Data();
                         response.id = data.id;
                         response.cmd = data.cmd;
                         response.type = data.type;
                         response.bData = 1;
                         outputQueue.add(response);
                     }
                 }
                 log.info("Read socket was closed", port);
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
 
         public void shutdown() {
             try {
                 client.close();
                setClientAccepted(false);
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
     }
 
     class Writer implements Runnable {
         private final Logger log;
         private Socket client;
 
         Writer(Socket client) {
             this.log = LoggerFactory.getLogger("Writer: " + port);
             this.client = client;
         }
 
         @Override
         public void run() {
             try {
                 DataOutputStream output = new DataOutputStream(client.getOutputStream());
 
                 while (!client.isClosed()) {
                     Data data = outputQueue.poll();
                     if (data == null) {
                         sleep();
                         continue;
                     }
 
                     log.debug("Write command: " + data.cmd);
                     Protocol.toOutputStream(data, output);
                 }
                 log.info("Write socket was closed", port);
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
 
         public void shutdown() {
             try {
                 client.close();
                setClientAccepted(false);
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
     }
 
     class Cleaner implements Runnable {
         private final Logger log;
         private ServerSocket listener;
 
         Cleaner(ServerSocket listener) {
             this.log = LoggerFactory.getLogger("Cleaner: " + port);
             this.listener = listener;
         }
 
         @Override
         public void run() {
             while (!listener.isClosed()) {
                if (!isClientAccepted()) {
                     Data data = outputQueue.poll();
                     if (data == null) {
                         sleep();
                     }
                     else {
                        log.debug("Drop data with id {}", data.id);
                     }
                 }
                 else {
                     sleep();
                 }
             }
             log.info("Cleaner was closed", port);
         }
     }
 
    synchronized boolean isClientAccepted() {
        return clientAccepted;
    }

    synchronized void setClientAccepted(boolean clientAccepted) {
        this.clientAccepted = clientAccepted;
    }

     private void sleep() {
         try {
             Thread.sleep(10);
         } catch (InterruptedException e) {
             e.printStackTrace();
         }
     }
 }
