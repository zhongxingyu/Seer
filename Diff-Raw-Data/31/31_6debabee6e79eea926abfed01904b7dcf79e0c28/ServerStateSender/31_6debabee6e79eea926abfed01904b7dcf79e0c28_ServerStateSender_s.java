 import java.io.IOException;
 import java.io.OutputStream;
 import java.net.InetSocketAddress;
 import java.net.ServerSocket;
 
 public class ServerStateSender extends Thread {
     private ServerStateMonitor serverStateMonitor;
     private int port;
 
     public ServerStateSender(int port, ServerStateMonitor serverStateMonitor) {
         System.out.println("ServerStateSender: started.");
         this.serverStateMonitor = serverStateMonitor;
         this.port = port;
     }
 
     public void run() {
         ServerSocket serverSocket = null;
         OutputStream outputStream;
         try {
            serverSocket = new ServerSocket();
            serverSocket.setReuseAddress(true);
            serverSocket.bind(new InetSocketAddress(port));
         } catch (IOException e) {
             System.err.println("[ServerStateSender] Could not start ServerSocket on port: " + port + ".");
             System.exit(1);
         }
 
         while (true) {
             try {
                 outputStream = serverSocket.accept().getOutputStream();
                 while (true) {
                     outputStream.write(serverStateMonitor.getModeBlocking());
                 }
             } catch (IOException e) {
                 System.out.println("[ServerStateSender] OutputStream closed. Reconnecting.");
             }
         }
     }
 }
