 package client;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.net.InetSocketAddress;
 import java.net.ServerSocket;
 
 public class ClientStateSender extends Thread {
     private ClientStateMonitor clientStateMonitor;
     private int port;
     public volatile boolean alive;
 
     public ClientStateSender(int port, ClientStateMonitor clientStateMonitor) {
         this.clientStateMonitor = clientStateMonitor;
         this.port = port;
     }
     public void run() {
         ServerSocket serverSocket = null;
         OutputStream outputStream;
 
         try {
            serverSocket = new ServerSocket(port);
         } catch (IOException e) {
             System.err.println("[ClientStateSender] Could not start ServerSocket on port: " + port + ".");
             System.exit(1);
         }
 
         while (true) {
             try {
                 outputStream = serverSocket.accept().getOutputStream();
                 while (true) {
                     outputStream.write(clientStateMonitor.getModeBlocking());
                 }
             } catch (IOException e) {
                 System.out.println("[ClientStateSender] OutputStream closed. Reconnecting.");
             }
         }
     }
 }
