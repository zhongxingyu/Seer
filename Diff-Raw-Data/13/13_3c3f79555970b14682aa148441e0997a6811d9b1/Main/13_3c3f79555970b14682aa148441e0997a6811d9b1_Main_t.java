 package mud;
 
 import java.io.IOException;
 import java.net.UnknownHostException;
 import mud.network.client.ClientFrame;
 import mud.network.client.GameClient;
 import mud.network.server.GameServer;
 
 /**
  *
  * @author Japhez
  */
 public class Main {
 
     final static int PORT = 1337;
 
     /**
      * Starts the server on a new thread and listens for client connections.
      *
      * @throws IOException
      */
     public static void startServer() throws IOException {
         GameServer gameServer = new GameServer(PORT);
         new Thread(gameServer).start();
     }
 
     public static void connectClient() throws UnknownHostException, IOException {
         ClientFrame clientFrame = new ClientFrame();
         GameClient gameClient = new GameClient("localhost", PORT, clientFrame.getjTextArea1(), clientFrame.getjTextField1());
         clientFrame.setVisible(true);
     }
 
     public static void main(String[] args) throws IOException {
         startServer();
         connectClient();
     }
 }
