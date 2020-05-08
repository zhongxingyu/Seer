 package mud;
 
 import java.io.IOException;
 import java.net.UnknownHostException;
 import mud.network.client.ClientFrame;
 import mud.network.client.GameClient;
 import mud.network.client.GameClient.ConnectionChoice;
 import mud.network.server.GameServer;
 
 /**
  * The main plain, brain.
  *
  * @author Japhez
  */
 public class Main {
 
     /**
      * Starts the server on a new thread and listens for client connections.
      *
      * @throws IOException
      */
     public static void startServer(boolean localOnly) throws IOException {
         GameServer gameServer = new GameServer(GameServer.DEFAULT_PORT, localOnly);
         new Thread(gameServer).start();
     }
 
     /**
      * Attempts to create and connect the client application, then starts a
      * server and connects it if necessary.
      *
      * @throws UnknownHostException
      * @throws IOException
      */
     public static void connectClient() throws UnknownHostException, IOException {
         ClientFrame clientFrame = new ClientFrame();
         GameClient gameClient = new GameClient(clientFrame.getjTextArea1(), clientFrame.getjTextField1());
         clientFrame.setVisible(true);
         //Block until the connection choice is determined
         ConnectionChoice connectionChoice = gameClient.getConnectionChoice();
         //Check for local only play
         if (connectionChoice.equals(ConnectionChoice.LOCAL_SOLO)) {
             startServer(true);
         }
         //Check for local hosting
         if (connectionChoice.equals(ConnectionChoice.LOCAL_CO_OP)) {
            startServer(true);
         }
         //Otherwise the connection is remote, no need for server
     }
 
     public static void main(String[] args) throws IOException {
         connectClient();
     }
 }
