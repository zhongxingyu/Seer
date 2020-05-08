 import java.io.IOException;
 import java.io.InputStream;
 import java.net.Socket;
 
 public class ServerStateReceiver extends Thread {
     private final ServerStateMonitor serverStateMonitor;
     private final String url;
     private final int port;
 
     public ServerStateReceiver(String url, int port, ServerStateMonitor serverStateMonitor) {
         this.serverStateMonitor = serverStateMonitor;
         this.port = port;
         this.url = url;
     }
 
     public void run() {
         Socket socket;
         InputStream inputStream;
 
         while (true) {
             try {
                 socket = new Socket(url, port);
                 inputStream = socket.getInputStream();
 
                 while (true) {
                     switch (inputStream.read()) {
                         case ServerStateMonitor.IDLE:
                        case ServerStateMonitor.MOVIE:
                             serverStateMonitor.unsetForcedMode();
                             break;
                         case ServerStateMonitor.IDLE_FORCED:
                             serverStateMonitor.setMode(ServerStateMonitor.IDLE_FORCED);
                             break;
                         case ServerStateMonitor.MOVIE_FORCED:
                             serverStateMonitor.setMode(ServerStateMonitor.MOVIE_FORCED);
                             break;
                         case -1:
                             throw new IOException("Stream closed.");
                     }
                 }
             } catch (IOException e) {
                 System.out.println("[ServerStateReceiver] No connection to client: " + url + " on port: " + port + ". Reconnecting in 1 second.");
                 try { sleep(1000); } catch (InterruptedException e1) { e1.printStackTrace(); }
             }
         }
     }
 }
