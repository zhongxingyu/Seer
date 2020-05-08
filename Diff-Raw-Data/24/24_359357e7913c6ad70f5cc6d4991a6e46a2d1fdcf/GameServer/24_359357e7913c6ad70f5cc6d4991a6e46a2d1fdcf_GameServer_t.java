 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Game.Network;
 
 import java.io.IOException;
 import java.net.ServerSocket;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.locks.Condition;
 import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
 
 /**
  *
  * @author michal
  */
 public class GameServer {
 
     Map clients;
     int maxConnections;
     int numConnections;
     int port;
     ServerSocket servSock; // server socket
     ConnectionHandler connHandler;
     GameState currentState;
     Thread handlerThread;
     ArrayList<GameState> clientStates;
     Condition broadcastCond;
     Lock lock;
 
     public static void main(String[] args) {
         GameServer g = new GameServer();
     }
 
     /**
      * Initialises with a specific port and number of connections
      * @param port
      * @param maxConnections
      */
     public GameServer(int port, int maxConnections) {
         this.port = port;
         this.maxConnections = maxConnections;
        lock = new ReentrantLock();
         currentState = new GameState();
         clientStates = new ArrayList<GameState>();
         numConnections = 0;
         clients = new HashMap<String, GServerSocket>();
         manageConnections();
     }
 
     /*
      * Connects to a default port range and initalises with a default player number.
      */
     public GameServer() {
         this(findUsablePort(2000, 4000), 4);
     }
 
     /**
      * Disconnects the server completely.
      */
     public void killServer() {
         Set clientKeys = clients.keySet();
         for (Object client : clientKeys) {
             GServerSocket s = (GServerSocket) clients.get(client);
             s.disconnect(true);
         }
         handlerThread.interrupt();
     }
 
     /**
      * Processes the game states in the arraylist to create an amalgamation of them.
      * returns the resulting game state. This method should only be called by the
      * game panel in order to do logic processing.
      * @returnGameServer.java:81
      */
     public GameState processGameStates() {
         GameState updateState = currentState;
         System.out.println("^^^^^^^^^^^processing states received from clients^^^^^^^^^^^^^^");
         System.out.println("current server state:\n" + updateState);
         lock.lock();
         try {
             broadcastCond = lock.newCondition();
             for (GameState gameState : clientStates) {
                 System.out.println("processing client state:\n" + gameState);
                 updateState.joinState(gameState);
                 System.out.println("client state joined to the server state:");
                 System.out.println(updateState);
                 System.out.println("state processed");
             }
             broadcastCond.signalAll();
         } finally {
             lock.unlock();
         }
         System.out.println("finished processing");
         System.out.println("^^^^^^^^^^^^^^^^^^^^^^");
         return updateState;
     }
 
     /**
      * Send the updated game state to each client.
      * @param state
      */
     public void broadcastGameState(GameState state) {
         currentState = state;
         System.out.println("$$$$$$$$$$$state to send$$$$$$$$$$$$");
         System.out.println(currentState);
         System.out.println("$$$$$$$$$$$$$$$$$$$");
         Set clientKeys = clients.keySet();
         for (Object client : clientKeys) {
             try {
                 GServerSocket s = (GServerSocket) clients.get(client);
                 s.sendObject("gamestate");
                 s.sendObject(state);
             } catch (IOException ex) {
                 System.out.println("Error while broadcasting the game state");
                 ex.printStackTrace();
             }
         }
         // remove all the old game states so that we can receive new ones.
         clientStates.clear();
     }
 
     public void readState(String sockName) {
         try {
             GServerSocket interactor = (GServerSocket) clients.get(sockName);
             GameState newState = (GameState) interactor.readObject();
             addGameState(newState);
         } catch (IOException ex) {
             System.out.println("Exception while reading a game state from " + sockName);
             ex.printStackTrace();
         } catch (ClassNotFoundException ex) {
             System.out.println(" class not found exception when attempting to read object from " + sockName);
             ex.printStackTrace();
         }
     }
 
     public void addGameState(GameState state) {
         clientStates.add(state);
     }
 
     /**
      * Gets connections to the server from the connection handler. Runs in a separate thread.
      * the handler should also deal with when clients disconnect.
      */
     private void manageConnections() {
         try {
             servSock = new ServerSocket(port);
             System.out.printf("Server listening on port %d\n", port);
             connHandler = new ConnectionHandler(this, maxConnections);
             handlerThread = new Thread(connHandler);
             handlerThread.start();
         } catch (IOException ex) {
             System.out.println("Error while attempting to listen for connections.");
             ex.printStackTrace();
         }
     }
 
     public void addClient(String name, GServerSocket clientSocket) {
         try {
             clients.put(name, clientSocket);
             System.out.println("added client");
             clientSocket.sendObject("sockname");
             System.out.println("sent name req");
             clientSocket.sendObject(name);
             System.out.println("sent name object");
             numConnections++;
         } catch (IOException ex) {
             System.out.println("Error while sending the socket name.");
             ex.printStackTrace();
         }
     }
 
     public int getNumConnections() {
         return numConnections;
     }
 
     // for use when a client is disconnected by the connection handler.
     protected void decrementConnections() {
         numConnections--;
     }
 
     // <editor-fold defaultstate="collapsed" desc="port utils">
     /**
      * Try and find a port that is not in use within the specified range of ports.
      * @param rangeStart
      * @param rangeEnd
      * @return
      */
     public static int findUsablePort(int rangeStart, int rangeEnd) {
         if (!validRange(rangeStart, rangeEnd)) {
             rangeStart = 2000;
             rangeEnd = 3000;
             System.err.printf("Invalid range entered. using the range %d to %d instead.\n", rangeStart, rangeEnd);
         }
         int usablePort = Integer.MIN_VALUE;
         ServerSocket testSock = null;
         for (int i = rangeStart; i <= rangeEnd; i++) {
             try {
                 testSock = new ServerSocket(i);
                 usablePort = i;
                 System.out.printf("Port %d is available.\n", usablePort);
                 break;
             } catch (IOException ex) {
                 System.out.printf("Port %d in use.\n", i);
             } finally {
                 try {
                     testSock.close();
                 } catch (IOException ex) {
                     System.err.printf("Unable to close the test socket at port %d\n", i);
                 } catch (Exception ex) {
                     System.err.println("Some other error ocurred while trying to close the test socket. "
                             + "The connection was probably null. This shouldn't be a problem.");
                 }
             }
         }
         return usablePort;
     }
 
     /**
      * Checks whether a port range specified is valid.
      * @param rangeStart
      * @param rangeEnd
      * @return
      */
     private static boolean validRange(int rangeStart, int rangeEnd) {
         int diff = rangeEnd - rangeStart;
         if (diff > 0 && rangeEnd > rangeStart && rangeStart > 0 && rangeEnd <= 65535) {
             return true;
         }
         return false;
     }
     // </editor-fold>
 }
