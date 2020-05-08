 /**
  * Main game server class. Allows users to logon to the server and provides
  * matchmaking services. Basic TCP server code based on code from CS283
  * homepage.
  */
 package cs283.catan;
 
 import java.io.*;
 import java.util.*;
 import java.util.regex.*;
 import java.net.*;
 import java.util.concurrent.atomic.AtomicInteger;
 
 /**
  * @author John
  *
  */
 public class ServerMain {
       
     /**
      * Maximum number of simultaneous connections
      */
     private static final int MAX_CONNECTIONS = 16;
     
     /**
      * Maximum username length
      */
     private static final int MAX_USERNAME_LENGTH = 16;
     
     /**
      * Message: too many connections
      */
     private static final String CONN_LIMIT_MSG = "Server handling too " +
                                                  "many users. Please " +
                                                  "try again later.";
     
     /**
      * Message: login success
      */
     private static final String LOGIN_SUCCESS_MSG = "Successfully logged on!";
     
     /**
      * Message: username in use
      */
     private static final String LOGIN_FAILURE_MSG = "Username in use!";
     
     
     /**
      * Current number of connections. An atomic integer since multiple threads
      * will be using the value
      */
     private static AtomicInteger numberConnections = new AtomicInteger(0);
     
     /**
      * Map that will store the usernames currently in use mapped to 
      * ServerConnectionHandler objects
      */
     private static Map<String, ServerConnectionHandler> userList = 
                                  new HashMap<String, ServerConnectionHandler>();
     
     /**
      * Map that will store the games currently in the lobby mapped to the list 
      * of players in each game. The key is the String name of the game, and the 
      * value is the String array of players in the game (should be size 4).
      */
     private static Map<String, String[]> lobbyGames = 
                                                 new HashMap<String, String[]>();
     
     /**
      * Map that will store the games currently in progress. The key is the
      * String name of the game, and the value is the game object.
      */
     private static Map<String, ServerCatanGame> inProgressGames = 
                                          new HashMap<String, ServerCatanGame>();
     
     
     /**
      * Object that is used to notify user threads that the lobby has changed
      */
     private static Object lobbyChangeNotifier = new Object();
     
     
     /**
      * Timestamp when the server started (used for debugging purposes) 
      */
     private static long serverStartTime;
 
     
     /**
      * @param args
      * @throws Exception
      */
     @SuppressWarnings("resource")
     public static void main(String[] args) {
         // Record the starting timestamp and set the thread name for debugging 
         // purposes
         serverStartTime = (new Date()).getTime();
         Thread.currentThread().setName("Main Thread");
         
         // Read the port from the command line arguments
         if (args.length != 1) {
             System.out.println("Usage: <Port>");
             System.exit(0);
         }
         
         // Open the server socket
         ServerSocket parentSocket = null;
         
         try {
             int listenBacklog = 50;
             // Create the socket. Address 0.0.0.0 indicates that the socket
             // will accept connections on any of the interfaces of the server
             parentSocket = 
                       new ServerSocket(Integer.parseInt(args[0]), listenBacklog,
                                        InetAddress.getByName("0.0.0.0"));
         } catch (IOException e) {
             System.out.println("Unable to open server socket: " +
                                e.getMessage());
             System.exit(0);
         } catch (NumberFormatException e) {
             System.out.println("Invalid port: " + args[0]);
             System.exit(0);
         } catch (IllegalArgumentException e) {
             System.out.println("Port not in range: " + args[0]);
             System.exit(0);
         }
         
         System.out.println("Server starting...\n");
         
         // Code for testing purposes. These players are not actually playing.
         // UNCOMMENT FOR DEBUGGING: 
         // String sampleNames[] = {"Austin", "Daniel", "Kevin", null};
         // lobbyGames.put("Ultimate Showdown", sampleNames);
         
         int totalThreadsCreated = 0; // Counter used for debugging purposes
         
         // Accept connections and start new threads for each connection
         while (true) {
             try {
                 Socket childSocket = parentSocket.accept();
                 
                 // Check whether the maximum number of connections is being used
                 if (numberConnections.get() < MAX_CONNECTIONS) {
                     numberConnections.getAndIncrement();
                     
                     totalThreadsCreated++;
                     
                     Thread connectionThread = 
                            new Thread(new ServerConnectionHandler(childSocket));
                     
                     // Set name for debugging purposes
                     connectionThread.setName("Handler Thread " +
                                              totalThreadsCreated);
                     
                     connectionThread.start();
                     
                 } else {
                     // Notify the client that it cannot be served at this moment
                     ObjectOutputStream outputStream = new ObjectOutputStream(
                                                  childSocket.getOutputStream());
                     outputStream.writeObject(CONN_LIMIT_MSG);
                     outputStream.flush();
                     
                     childSocket.close();
                 }
             } catch (IOException e) {
                 printServerMsg("Error accepting new connection: " +
                                    e.getMessage());
             }
         }
     }
     
     /**
      * Prints a message, prepended with a timestamp and thread id.
      * @param message
      */
     private static void printServerMsg(String message) {
         double timestamp = 0.001 * ((new Date().getTime()) - serverStartTime);
         String threadName = Thread.currentThread().getName();
         
         System.out.format("[time=%.4f, thread='%s']\t%s\n", timestamp,
                           threadName, message);
     }
 
     
     /**
      * Adds a new game to lobby with the name gameName and one player so far
      * with the name username. Note: responsibility of caller to synchronize
      * access to lobbyGames for thread safety.
      * @param gameName
      * @param username
      * @return true if the game was added, false if the game could not be added.
      */
     private static boolean addGame(String gameName, String username) {
         boolean isGameAdded = false;
         
         // Make sure the game does not already exist
         if (!lobbyGames.containsKey(gameName) && 
             !inProgressGames.containsKey(gameName)) {
             
             // Create a String array to hold the 4 players
             String playerArray[] = new String[4];
             
             // Initialize the first player to be username. The remaining 
             // players are set to null by default
             playerArray[0] = username;
             
             // Add the game to the map
             lobbyGames.put(gameName, playerArray);
             
             isGameAdded = true;
         } 
         
         if (isGameAdded) {
             notifyLobbyChanged();
         }
         
         return isGameAdded;
     }
     
     /**
      * Adds a player named username to a game in the lobby with the name
      * gameName. Note: responsibility of caller to synchronize access to
      * lobbyGames for thread safety.
      * @param gameName
      * @param username
      * @return true if the player was added to the game, false if the player
      *         could not be added to the game.
      */
     private static boolean addPlayerToGame(String gameName, String username) {
         boolean isPlayerAdded = false;
         
         // Make sure the game exists
         if (lobbyGames.containsKey(gameName)) {
             String playerArray[] = lobbyGames.get(gameName);
             
             // Make sure player array is not null (it should not be)
             if (playerArray != null) {
             
                 // Add the player to the next available position, if any
                 for (int i = 0; i < playerArray.length; i++) {
                     if (playerArray[i] == null) {
                         // Add the player to this available position
                         playerArray[i] = username;
                         isPlayerAdded = true;
                         
                         break;
                     }
                 }
             }
         }
         
         if (isPlayerAdded) {
             notifyLobbyChanged();
         }
         
         return isPlayerAdded;
     }
     
     /**
      * Removes a player named username from a game in the lobby with the name
      * gameName. Note: responsibility of sender to synchronize access to
      * lobbyGames for thread safety.
      * @param gameName
      * @param username
      * @return true if the player was removed from the game, false if the player
      *         was not removed or was not part of the game.
      */
     private static boolean removePlayerFromGame(String gameName, 
                                                 String username) {
         boolean isPlayerRemoved = false;
         
         String playerArray[] = lobbyGames.get(gameName);
         
         if (playerArray != null) {
             // Remove username from the player array, shifting
             // everyone in the array down one place
             
             for (int i = 0; i < playerArray.length && 
                                 playerArray[i] != null; i++) {
                 
                 // If this element is not the last element, the next element
                 // is at position i + 1. Otherwise, the next element is null
                 String nextPlayer = i < playerArray.length - 1 
                               ? playerArray[i + 1] : null;
                               
                 if (!isPlayerRemoved) {
                     if (playerArray[i].equals(username)) {
                         
                         playerArray[i] = nextPlayer;
                         isPlayerRemoved = true;
                         
                     }
                 } else {
                     // Shift elements to the left now that username
                     // has been deleted
                     playerArray[i] = nextPlayer;
                 }
             }
             
             // If the game is empty, delete the game
             if (playerArray[0] == null) {
                 lobbyGames.remove(gameName);
             }
         }
         
         if (isPlayerRemoved) {
             notifyLobbyChanged();
         }
         
         return isPlayerRemoved;
 
     }
     
     /**
      * Determines whether or not a game with name gameName is completely full 
      * of players. Note: responsibility of sender to synchronize access to
      * lobbyGames for thread safety.
      * @param gameName
      * @return true if the game is full, false if the game does not exist or
      *         is not full.
      */
     private static boolean isGameFull(String gameName) {
         boolean isGameFull = true;
         
         // Make sure the game exists
         if (lobbyGames.containsKey(gameName)) {
             String playerArray[] = lobbyGames.get(gameName);
             
             // Make sure player array is not null (it should not be)
             if (playerArray != null) {
                 
                 // Iterate through the player array. If at any point a null
                 // value is reached, there is room for another player and
                 // the game is not full.
                 for (int i = 0; i < playerArray.length; i++) {
                     if (playerArray[i] == null) {
                         isGameFull = false;
                         
                         break;
                     }
                 }
             }
         } else {
             // The game does not exist, so just return false
             isGameFull = false;
         }
         
         return isGameFull;
     }
     
     /**
      * Removes a game named gameName from the lobby and starts the game. Note:
      * responsibility of sender to synchronize access to lobbyGames for thread
      * safety.
      * @param gameName
      */
     private static void startNewGame(String gameName) {
         // Remove the game from the lobby and add to in progress games
         String playerNameArray[] = lobbyGames.remove(gameName);
         
         System.out.print("Starting the game '" + gameName + "' with the " +
                            "players ");
         System.out.printf("'%s', '%s', '%s', and '%s'.\n", playerNameArray[0], 
                           playerNameArray[1], playerNameArray[2], 
                           playerNameArray[3]);
         
 
         // Start a new game
         
         // Create the player array
         Player playerArray[] = new Player[4];
         
         for (int i = 0; i < playerArray.length; i++) {
             playerArray[i] = new Player(playerNameArray[i], i);
         }
 
         // Create the Catan game
         ServerCatanGame game = new ServerCatanGame(playerArray);
         game.gameSetup();
         
         inProgressGames.put(gameName, game);
         
         synchronized (userList) {
             for (int i = 0; i < playerArray.length; i++) {
                 userList.get(playerArray[i].getUsername()).setCatanGame(game);
             }
         }
         // Notify everyone that the lobby changed
         notifyLobbyChanged();
     }
     
     /**
      * Notifies all of the users that the lobby has changed
      */
     private static void notifyLobbyChanged() {
         synchronized (lobbyChangeNotifier) {
             lobbyChangeNotifier.notifyAll();
         }
     }
     
     /**
      * Logs the user with name username off the system. If the user was in a
      * game currently in the lobby, remove the user from that game. If the user 
      * was in an in-progress game, end that game.
      * @param username
      * @param lobbyGame
      */
     private static void logoffUser(String username, String lobbyGame,
                                    String inProgressGameName,
                                    ServerCatanGame inProgressGame) {
         // Check if the user is in a lobby game. If so, remove the user from
         // that game.
         if (lobbyGame != null) {
             synchronized (lobbyGames) {
                 removePlayerFromGame(lobbyGame, username);
             }
         }
         
         // Check if the user is in an in progress game. If so, end the game.
         if (inProgressGame != null) {
             // Remove the game name from the list of in progress games
             synchronized (lobbyGames) {
                 inProgressGames.remove(inProgressGameName);
             }
             
             // Notify each player to end the game
             Player playerArray[] = inProgressGame.getPlayerArray();
             
             if (playerArray != null) {
                 for (int i = 0; i < playerArray.length; i++) {
                     if (!playerArray[i].getUsername().equals(username)) {
                         
                         ServerConnectionHandler handler;
                         synchronized (userList) {
                             handler = 
                                      userList.get(playerArray[i].getUsername());
                         }
                         // Notify each player besides the one logging off that
                         // the game is over.
                         if (handler != null) {
                             try {
                                 handler.sendGameOverMsg();
                             } catch (Exception e) {
                                 e.printStackTrace();
                             }
                         }
                     }
                 }
             }
         }
         
         // Remove the user from the list of users
         synchronized (userList) {
             userList.remove(username);
         }
         
         printServerMsg("Successfully logged off user '" + username + "'!");
     }
     
      
     /**
      * Connection handler class that manages one user.
      *
      */
     public static class ServerConnectionHandler implements Runnable {
         
         /**
          * Enum that represents the possible modes the user could be in
          *
          */
         private enum UserMode {
             Initialization,
             LobbyMode,
             GameMode,
         }
         
         /**
          * Client socket object
          */
         private Socket clientSocket;
         
         /**
          * Object input stream for the socket
          */
         private ObjectInputStream objInputStream;
         
         /**
          * Object output stream for the socket
          */
         private ObjectOutputStream objOutputStream;
         
         /**
          * Value of the username
          */
         private String username;
         
         /**
          * Name of the lobby game the user is currently in. Set to null
          * if the user is not in a lobby game.
          */
         private String lobbyGameName;
         
         /**
          * Name of the in-progress game the user is currently in. Set to null
          * if the user is not in an in-progress game.
          */
         private String inProgressGameName;
         
         /**
          * Catan game object
          */
         private ServerCatanGame catanGame;
         
         /**
          * Indicates the mode the user is currently in
          */
         private UserMode currentMode;
         
         /**
          * 
          * @param socket
          */
         public ServerConnectionHandler(Socket socket) {
             this.clientSocket = socket;
             this.username = this.lobbyGameName = this.inProgressGameName = null;
             this.catanGame = null;
             this.currentMode = UserMode.Initialization;
         }
         
         /**
          * Set the Catan game object
          * @param game
          */
         public void setCatanGame(ServerCatanGame game) {
             this.catanGame = game;
         }
         
         /**
          * Main entry point of the connection handler
          */
         @Override
         public void run() {
             
             boolean isLogonSuccessful = false;
             
             Thread lobbyPushThread = null;
             Thread gamePushThread = null;
             
             try {
                 
                 // Create the input and output streams
                 objInputStream = 
                         new ObjectInputStream(clientSocket.getInputStream());
                 
                 objOutputStream = 
                         new ObjectOutputStream(clientSocket.getOutputStream());
                 
                 
                 // Read the username sent from the client
                 
                 username = (String) objInputStream.readObject();
                 
                 // Limit the username to 16 characters
                 if (username.length() > MAX_USERNAME_LENGTH) {
                     username = username.substring(0,  MAX_USERNAME_LENGTH);
                 }
                 
 
                 // Attempt a logon of username. First check to ensure that the
                 // username is available
                 synchronized (userList) {
                     if (!userList.containsKey(username)) {
                         userList.put(username, this);
                         
                         isLogonSuccessful = true;
                     }
                 }
                 
                 // Make sure logon was successful
                 if (isLogonSuccessful) {
                     
                     printServerMsg("'" + username + "' logged on!"); 
                     
                     objOutputStream.writeObject(LOGIN_SUCCESS_MSG);
                     objOutputStream.flush();
                     
                     // Handle lobby and game stuff
                     
 
                     // Start thread that will push lobby changes to the user
                     lobbyPushThread = new Thread() {
                         public void run() {
                             handleLobbyPush();
                         }
                     };
                     
                     lobbyPushThread.start();
                     
                     handleLobby();
 
                     // End the lobby push thread
                     lobbyPushThread.interrupt();
                     lobbyPushThread.join(1000);
                     
                     // Handle the game
                     
                     // Start thread that will push game changes to the user
                     gamePushThread = new Thread() {
                         public void run() {
                             handleGamePush();
                         }
                     };
                     
                     gamePushThread.start();
 
                     handleGame();
                     
                     // End the game push thread
                     gamePushThread.interrupt();
                     gamePushThread.join(1000);
                     
                 } else {
                     printServerMsg("Logon attempt by '" + username +
                                        "' failed!");
                     
                     objOutputStream.writeObject(LOGIN_FAILURE_MSG);
                     objOutputStream.flush();
                 }  
                 
             } catch (Exception e) {
                 printServerMsg("Connection problem with '" + username + 
                                    "': " + e.getMessage());
                 
                 e.printStackTrace();
                 
                 if (lobbyPushThread != null && lobbyPushThread.isAlive()) {
                     lobbyPushThread.interrupt();
                 }
                 
                 if (gamePushThread != null && gamePushThread.isAlive()) {
                     gamePushThread.interrupt();
                 }
             }
             
             
             // Attempt to close the socket
             try {
                 clientSocket.close();
             } catch (Exception e) {
                 // Just ignore error
             }
             
             
             // Log the user off the system if the user is logged on
             // This code needs to be outside of the try/catch block 
             // so that the user will always be logged off the system, 
             // even when an exception is thrown
             if (isLogonSuccessful) {
                 logoffUser(username, lobbyGameName, inProgressGameName,
                            catanGame);
             }
             
             // Decrease the number of connections
             numberConnections.getAndDecrement();
         }
         
         /**
          * Manages all of the user interaction with the lobby.
          * @throws Exception
          */
         private void handleLobby() throws Exception {
             currentMode = UserMode.LobbyMode;
             
             // Initially, server sends the client the current state of the
             // lobby
             synchronized (lobbyGames) {
                 synchronized (objOutputStream) {
                     objOutputStream.writeObject("Lobby");
                     objOutputStream.writeObject(lobbyGames);
                     objOutputStream.flush();
                 }
             }
   
             // Listen for client commands while the client is in the lobby.
             while (currentMode == UserMode.LobbyMode) {
                 // Receive command from the client
                 String msg = (String) objInputStream.readObject();
                 printServerMsg("Message received.");
                 System.out.println("\n=========RECEIVED MESSAGE=========");
                 System.out.println(msg);
                 System.out.println("==================================\n");
 
                 String split[] = msg.split("\n");
                 
                 boolean isSuccessful = false;
                 String failureMsg = null;
                 
                 // Perform the appropriate actions in response to the message
                 synchronized (lobbyGames) {
                     if (split[0].equals("Create Game")) {
                         
                         if (lobbyGameName == null) {
                             if (addGame(split[1], split[2])) {
                                 lobbyGameName = split[1];
                                 isSuccessful = true;
                             }
                         }
                         
                         if (!isSuccessful) {
                             failureMsg = "Failed to create game '" +
                                     split[1] + "'";
                         }
                         
                     } else if (split[0].equals("Join Game")) {
                         
                         if (lobbyGameName == null) {
                             if (addPlayerToGame(split[1], split[2])) {
                                 lobbyGameName = split[1];
                                 isSuccessful = true;
                                 
                                 // Check to see if the game is now full. If so,
                                 // start the game
                                 if (isGameFull(lobbyGameName)) {
                                     startNewGame(lobbyGameName);
                                 }
                             }
                         }
                         
                         if (!isSuccessful) {
                             failureMsg = "Failed to add user '" +
                                     split[2] + "' to game '" +
                                     split[1] + "'";
                         }
                         
                     } else if (split[0].equals("Remove User from Game")) {
                         
                         if (removePlayerFromGame(split[1], split[2])) {
                             lobbyGameName = null;
                             isSuccessful = true;
                         } 
                         
                         if (!isSuccessful) {
                             failureMsg = "Failed to remove user '" +
                                          split[2] + "' from game '" +
                                          split[1] + "'";
                         }
                     } else if (split[0].equals("Begin Game")) {
                         // Make sure game can start
                         if (lobbyGameName != null &&
                             inProgressGames.containsKey(lobbyGameName)) {
                             
                             currentMode = UserMode.GameMode;
                             inProgressGameName = lobbyGameName;
                             lobbyGameName = null;
                             
                             isSuccessful = true;
                         }
                         
                         if (!isSuccessful) {
                             failureMsg = "Failed to begin playing game '" +
                                          split[1] + "'";
                         }
                     }
                 }
                 
                 String response = isSuccessful ? "Success" : failureMsg;
                 
                 synchronized (objOutputStream) {
                     objOutputStream.writeObject(response);
                     objOutputStream.flush();
                 }
             }
             
             printServerMsg("'" + username + "' leaving lobby");
         }
      
         /**
          * Waits for notification of a change to the lobby. When the lobby
          * changes, send the update to the user.
          */
         private void handleLobbyPush() {
             while (!Thread.interrupted()) {
                 try {
                     // Wait for a notification that the lobby has been 
                     // updated
                     synchronized (lobbyChangeNotifier) {
                         lobbyChangeNotifier.wait();
                     }
                     
                     // Send the lobby data to the user
                     synchronized (lobbyGames) {
                         synchronized (objOutputStream) {
                             objOutputStream.reset();
                             objOutputStream.writeObject(new String("Lobby"));
                             objOutputStream.writeObject(lobbyGames);
                             objOutputStream.flush();
                             
                             
                             // Check if lobby game has been moved to in progress
                             // games
                             if (lobbyGameName != null &&
                                 inProgressGames.containsKey(lobbyGameName)) {
                                 
                                 // Send message to the client indicating that
                                 // the game is starting
                                 objOutputStream.writeObject("Game starting");
                                 objOutputStream.flush();
                             }
                         }
                     }
                     
                 } catch (InterruptedException e) {
                     // End the thread since it has been interrupted
                     break;
                 } catch (Exception e) {
                     // Just continue loop
                 }
             }
 
             printServerMsg("Ending lobby push thread.");
             
         }
         
         /**
          * Manages all of the user interaction with the game, including chat.
          * @throws Exception
          */
         private void handleGame() throws Exception {
             // Initially the game sends a chat message to the client welcoming
             // it to the game
             sendChatMessage("chat*SERVER: Welcome!");
             
             // Send the initial game data to the user
             synchronized (catanGame) {
                 synchronized (objOutputStream) {
                     objOutputStream.reset();
                     objOutputStream.writeObject("game*");
                     objOutputStream.writeObject(catanGame);
                     objOutputStream.flush();
                 }
             }
             
             // Send the first roll
             sendRollMessage(catanGame.getDiceRoll(), catanGame.getTurn());
            
             
             while (currentMode == UserMode.GameMode) {
                 // Receive a message
                 String msg = (String) objInputStream.readObject();
                 
                 printServerMsg("Message received.");
                 System.out.println("\n=========RECEIVED MESSAGE=========");
                 System.out.println(msg);
                 System.out.println("==================================\n");
                 
                 if (msg.startsWith("cmd**")) { // Game command received
                     boolean isTurn = false;
                     synchronized (catanGame) {
                         isTurn = catanGame.isTurn(username);
                     }
                     
                     
                     // Make sure it is the player's turn
                     if (isTurn) {
                         parseGameCommand(msg.substring(5));
                     } else {
                     	parseTradeAccept(msg.substring(5), username);
                         
                     }
                 } else if (msg.startsWith("chat*")) { // Chat message received
                     
                     Player playerArray[] = catanGame.getPlayerArray();
                     
                     int firstIndex = msg.indexOf("/*/");
                     //if this is a targeted message
                     if (firstIndex != -1) {
                         int lastIndex = msg.indexOf("*/*");
                         String address = msg.substring(firstIndex+3, 
                                                            lastIndex);
                         
                         //match address with user's socket
                         ServerConnectionHandler target = null;  
                         
                         // Find the target ServerConnectionHandler to which the
                         // message will be routed
                         if (playerArray != null) {
                             for (int i = 0; i < playerArray.length; i++) {
                                 if (playerArray[i].getUsername()
                                     .equals(address)) {
                                     
                                     synchronized (userList) {
                                         target = 
                                                 userList.get(playerArray[i]
                                                              .getUsername());
                                     }
                                     
                                     break;
                                 }
                             }
                         }
                         
                         //attach chat* command to send out
                         //String chatCommand = "chat*";
                         String messageToSend = new String(msg.substring(0, 
                                                                    firstIndex));
                         messageToSend = messageToSend.concat(msg
                                                        .substring(lastIndex+3));
                         //messageToSend = chatCommand.concat(messageToSend);
                         
                         if (target != null) {
                             // Send the message to the target and the sender
                             int colonIndex = messageToSend.indexOf(":");
                             String senderConfirmation = 
                                             String.format("%s: \"%s\" %s",
                                        messageToSend.substring(0, colonIndex),
                                        address,
                                        messageToSend.substring(colonIndex + 2));
                             
                             sendChatMessage(senderConfirmation);
                             target.sendChatMessage(messageToSend);
                         } else {
                             // Send an error message to the client
                             sendChatMessage("chat*SERVER: User '" + address +
                                             "' was not found!");
                         }
                         
                     } else
                     {
                         //not a targeted chat signal
                         String messageToSend = msg;
                         
                         //Iterate through all sockets and send
                         if (playerArray != null) {
                             for (int i = 0; i < playerArray.length; i++) {
                                 ServerConnectionHandler handler;
                                 synchronized (userList) {
                                     handler = userList.get(playerArray[i]
                                                           .getUsername());
                                 }
                                 
                                 if (handler != null) {
                                     handler.sendChatMessage(messageToSend);
                                 }
                             }
                         }
                     }
                     
                 } else if (msg.equals("End Turn")) {
                     // Notify the game that a player's turn has ended
                     boolean advanced = false;
                     
                     synchronized (catanGame) {
                         advanced = catanGame.advanceTurn(username);
                     }
                     
                     if (advanced) {
                         // Notify each player of the new turn
                         Player playerArray[] = catanGame.getPlayerArray();
                         
                         if (playerArray != null) {
                             for (int i = 0; i < playerArray.length; i++) {
                                 
                                 ServerConnectionHandler handler;
                                 synchronized (userList) {
                                     handler = 
                                          userList.get(playerArray[i].getUsername());
                                 }
                                 
                                 // Notify each user of the roll
                                 if (handler != null) {
                                     try {
                                         handler.sendRollMessage(
                                                 catanGame.getDiceRoll(),
                                                 catanGame.getTurn());
                                     } catch (Exception e) {
                                         e.printStackTrace();
                                     }
                                 }
                             }
                         }
                     }
                 }
             }
         }
         
         private void parseTradeAccept(String message, String username) throws Exception
         {
         	try
         	{
         		Player myPlayer = catanGame.getPlayer(username);
             	if(myPlayer == null)
             	{
             		sendChatMessage("The player " + username + " does not exist.");
             		return;
             	}
             	
             	String acceptString = "^(trade accept)";
             	Pattern acceptPattern = Pattern.compile(acceptString);
             	Matcher acceptMatcher = acceptPattern.matcher(message);
              	if(acceptMatcher.find())
             	{
             		
             		//we have a valid accept command
             		//we determine whether there is an active offer
             		//if there isn't, we inform the user
              		if(!catanGame.hasActiveTrade())
              		{
              			sendChatMessage("chat*SERVER: No active trades");
              		}
             		
             		//if there is, then ask whether player
             		//has resources to complete trade
              		else if(myPlayer.getNumCards(ResourceCard.BRICK.toString()) >= catanGame.acceptArray[0]
                     	&& myPlayer.getNumCards(ResourceCard.LUMBER.toString()) >= catanGame.acceptArray[1]
                         && myPlayer.getNumCards(ResourceCard.ORE.toString()) >= catanGame.acceptArray[2]
                         && myPlayer.getNumCards(ResourceCard.WHEAT.toString()) >= catanGame.acceptArray[3]
                         && myPlayer.getNumCards(ResourceCard.WOOL.toString()) >= catanGame.acceptArray[4])
              		{
              			//if so, we complete the trade as follows:
                 		//remove the cards from offering player's hand
                 		//(use two resCard arrays)
              			String typeArray[] = {"brick", "lumber", "ore", "wheat", "wool"};
              			Player owner = catanGame.getPlayer(
              					catanGame.getPlayerArray()[catanGame.getTurn()].toString());
                 		for(int i = 0; i<catanGame.offerArray.length; ++i)
                 		{
                 			owner.removeCards(typeArray[i], catanGame.offerArray[i]);
                 		}
              			
                 		//put cards in accepting player's hand
                 		for(int i = 0; i<catanGame.offerArray.length; ++i)
                 		{
                 			myPlayer.addCards(typeArray[i], catanGame.offerArray[i]);
                 		}
                 		
                 		//remove cards from accepting player's hand
                 		for(int i = 0; i<catanGame.acceptArray.length; ++i)
                 		{
                 			myPlayer.removeCards(typeArray[i], catanGame.acceptArray[i]);
                 		}
                 		
                 		//put cards in offering player's hand
                 		for(int i = 0; i<catanGame.acceptArray.length; ++i)
                 		{
                 			owner.addCards(typeArray[i], catanGame.acceptArray[i]);
                 		}
                 		
                 		//announce successful trade
                 		sendChatMessage("chat*SERVER: Trade successful");
                 		//reset trade
              			catanGame.resetTrade();
              			synchronized (catanGame.gameChangedNotifier) {
                             catanGame.gameChangedNotifier.notifyAll();
                         }
              			
              		}
             		//if not, then we inform the user
              		else
              		{
              			sendChatMessage("chat*SERVER: " + username + " does not have the resources" +
              					"to complete this trade");
              			
              		}
             		
             		
             		
             		
             		
             		
             	}
              	else
              	{
              		sendChatMessage("chat*SERVER: Wait your turn!");
              	}
         	}
         	catch(Exception e)
         	{
         		System.out.println("Exception (parseTradeAccept): " + e.toString());
         	}
         	
         	
         }
         
         /**
          * Parses a game command.
          * @param command
          */
         private void parseGameCommand(String message) throws Exception {
             Board board = catanGame.getBoard();
             
             Set<Player> playerSet = null;
             // Indicates whether the state of the game has changed because of
             // a command
             boolean isGameChanged = false;
             
             // Find the player representing the current player
             Player playerArray[] = catanGame.getPlayerArray();
             Player owner = null;
             
             for (int i = 0; i < playerArray.length; i++) {
                 if (playerArray[i].getUsername().equals(username)) {
                     owner = playerArray[i];
                     break;
                 }
             }
             
             if (owner.settlementPlacementMode == 2)
             {
             	if (message.indexOf("buy") != -1)	
                 {
                     /*
                     buy
                         settlement (coord)
                         city (coord)
                         road (coord) to (coord)
                         devcard
                     */
                     if (message.indexOf("settlement") != -1)
                     {
                         try{
                             message = message.substring(message.indexOf("settlement") + 10);
                             Scanner scanMessage = new Scanner(message);
                             int coordinate1 = scanMessage.nextInt();
                             int coordinate2 = scanMessage.nextInt();
                             int coordinate3 = scanMessage.nextInt();
                             
                             	if (!board.freeAddSettlement(new Coordinate(coordinate1,
                                                                coordinate2,
                                                                coordinate3),
                                                                owner, false)) {
                             		sendChatMessage("chat*SERVER: Unable to add " +
                             				"settlement.");
                             	} else {
                             		isGameChanged = true;
                             		owner.settlementPlacementMode--;
                             	}
                             
                             	scanMessage.close();
                             
                             }catch (Exception InputMismatchException)
                             {
                                 sendChatMessage("chat*SERVER: Invalid command, " +
                                                      "you dummy!");
                             }
                         
                     }else
                     {
                     	sendChatMessage("chat*Server: wrong command.  You must buy a settlement");
                     }
                 }else
                 {
                 	sendChatMessage("chat*Server: wrong command.  You must buy a settlement");
                 }
             }else if (owner.roadBuilderMode == 2)
             {
             	if (message.indexOf("buy") != -1)	
                 {
                     /*
                     buy
                         settlement (coord)
                         city (coord)
                         road (coord) to (coord)
                         devcard
                     */
                     if (message.indexOf("road") != -1)
                     {
                     try{
                         message = message.substring(message.indexOf("road") + 4);
                         Scanner scanMessage = new Scanner(message);
                         int coordinate1 = scanMessage.nextInt();
                         int coordinate2 = scanMessage.nextInt();
                         int coordinate3 = scanMessage.nextInt();
                         message = message.substring(message.indexOf(',') +1);
                         scanMessage.close();
                         
                         scanMessage = new Scanner(message);
                         int coordinate4 = scanMessage.nextInt();
                         int coordinate5 = scanMessage.nextInt();
                         int coordinate6 = scanMessage.nextInt();
                         
                         if (!board.freeAddRoad(new Coordinate(coordinate1,
                                                           coordinate2,
                                                           coordinate3),
                                            new Coordinate(coordinate4,
                                                           coordinate5,
                                                           coordinate6),
                                            owner)) {
                             sendChatMessage("chat*SERVER: Unable to add " +
                                             "road.");
                         } else {
                             isGameChanged = true;
                             owner.roadBuilderMode--;
                         }
                         
                         scanMessage.close();
                         //command
                         }catch (Exception InputMismatchException)
                         {
                             sendChatMessage("chat*SERVER: Invalid command, " +
                             		"you dummy!");
                         }
                     
                     }else
                 	{
                 	sendChatMessage("chat*SERVER: You must build roads right now");
                 	}
                 }else
             	{
             	sendChatMessage("chat*SERVER: You must build roads right now");
             	}
             }else if (owner.settlementPlacementMode == 1)
             {
             	if (message.indexOf("buy") != -1 
             	    && catanGame.isSecondSettlementPlacing())	
                 {
                     /*
                     buy
                         settlement (coord)
                         city (coord)
                         road (coord) to (coord)
                         devcard
                     */
                     if (message.indexOf("settlement") != -1)
                     {
                         try{
                             message = message.substring(message.indexOf("settlement") + 10);
                             Scanner scanMessage = new Scanner(message);
                             int coordinate1 = scanMessage.nextInt();
                             int coordinate2 = scanMessage.nextInt();
                             int coordinate3 = scanMessage.nextInt();
                             
                             if (!board.freeAddSettlement(new Coordinate(coordinate1,
                                                                coordinate2,
                                                                coordinate3),
                                                     owner, true)) {
                                 sendChatMessage("chat*SERVER: Unable to add " +
                                                 "settlement.");
                             } else {
                                 isGameChanged = true;
                                 owner.settlementPlacementMode--;
                             }
                             
                             scanMessage.close();
                             
                             }catch (Exception InputMismatchException)
                             {
                                 sendChatMessage("chat*SERVER: Invalid command, " +
                                                      "you dummy!");
                             }
                         
                     }else
                     {
                     	sendChatMessage("chat*Server: wrong command.  You must buy a settlement");
                     }
                 }else
                 {
                 	sendChatMessage("chat*Server: wrong command.  You must buy a settlement");
                 }
             }else if (owner.roadBuilderMode == 1)
             {
             	if (message.indexOf("buy") != -1)	
                 {
                     /*
                     buy
                         settlement (coord)
                         city (coord)
                         road (coord) to (coord)
                         devcard
                     */
                     if (message.indexOf("road") != -1)
                     {
                     try{
                         message = message.substring(message.indexOf("road") + 4);
                         Scanner scanMessage = new Scanner(message);
                         int coordinate1 = scanMessage.nextInt();
                         int coordinate2 = scanMessage.nextInt();
                         int coordinate3 = scanMessage.nextInt();
                         message = message.substring(message.indexOf(',') +1);
                         scanMessage.close();
                         
                         scanMessage = new Scanner(message);
                         int coordinate4 = scanMessage.nextInt();
                         int coordinate5 = scanMessage.nextInt();
                         int coordinate6 = scanMessage.nextInt();
                         
                         if (!board.freeAddRoad(new Coordinate(coordinate1,
                                                           coordinate2,
                                                           coordinate3),
                                            new Coordinate(coordinate4,
                                                           coordinate5,
                                                           coordinate6),
                                            owner)) {
                             sendChatMessage("chat*SERVER: Unable to add " +
                                             "road.");
                         } else {
                             isGameChanged = true;
                             owner.roadBuilderMode--;
                         }
                         
                         scanMessage.close();
                         //command
                         }catch (Exception InputMismatchException)
                         {
                             sendChatMessage("chat*SERVER: Invalid command, " +
                             		"you dummy!");
                         }
                     
                     }else
                 	{
                 	sendChatMessage("chat*SERVER: You must build roads right now");
                 	}
                 }else
             	{
             	sendChatMessage("chat*SERVER: You must build roads right now");
             	}
             }else if (owner.yearOfPlentyMode != 0)
             {
             	if (message.equals("WOOL"))
             	{
             		owner.addCards(message, 1);
             		owner.yearOfPlentyMode--;
             	}else if(message.equals("BRICK"))
             	{
             		owner.addCards(message, 1);
             		owner.yearOfPlentyMode--;
             	}else if(message.equals("LUMBER"))
             	{
             		owner.addCards(message, 1);
             		owner.yearOfPlentyMode--;
             	}else if(message.equals("WHEAT"))
             	{
             		owner.addCards(message, 1);
             		owner.yearOfPlentyMode--;
             	}else if(message.equals("ORE"))
             	{
             		owner.addCards(message, 1);
             		owner.yearOfPlentyMode--;
             	}else 
             	{
             		sendChatMessage("chat*Server: Wrong resource");
             	}
             }else if (owner.robberMode)
             {
             	if(message.indexOf("robber")!= -1)
                 {
                     //robber i i 
             		 int robberIndex = message.indexOf("robber");
                      
                      try{
                          message = message.substring(robberIndex + 7);
                         
                          
                          Scanner scanMessage = new Scanner(message);
                          int coordinate1 = scanMessage.nextInt();
                          int coordinate2 = scanMessage.nextInt();
                          
                          scanMessage.close();
                          playerSet = board.moveRobber(coordinate1, coordinate2);
                          if (playerSet != null)
                          {
                         	 if (message.indexOf("steal") != -1)
                          	{
                          		message = message.substring(message.indexOf("steal")+6);
                          		for(Player a: playerSet)
                          		{
                          			if (a.toString().equals(message))
                          			{
                          				if(a.getNumCards() != 0)
                          				{
                                        	 owner.robberMode = false;
                          				}else
                          				{
                          					boolean notStolen = true;
                          					while (notStolen)
                          					{
                          					Random randomCard = new Random();
                          					int card = randomCard.nextInt(4);
                          					String cardType = null;
                          					if (card == 0)
                          					{
                          						cardType = new String("BRICK");
 
                          					}else if (card == 1)
                          					{
                          						cardType = new String("WOOL");
                          					}else if (card == 1)
                          					{
                          						cardType = new String("LUMBER");
                          					}
                          					else if (card == 1)
                          					{
                          						cardType = new String("ORE");
                          					}
                          					else 
                          					{
                          						cardType = new String("WHEAT");
 
                          					}
                          					notStolen = !a.removeCards(cardType, 1);
                          					if (!notStolen)
                          					{
                          						owner.addCards(cardType, 1);
                          					}
                          					
                          					}
                          				}
                          			}
                          		}
                          	}
                         }
                          
                          }catch (Exception InputMismatchException)
                          {
                              sendChatMessage("chat*SERVER: Invalid command, " +
                                               "you dummy!");
                          }
                    isGameChanged = true;
                 }
             	
             	owner.robberMode = false;
             }else if (owner.stealMode)
             {
             	
             }else if (message.indexOf("buy") != -1)	
             {
                 /*
                 buy
                     settlement (coord)
                     city (coord)
                     road (coord) to (coord)
                     devcard
                 */
                 if (message.indexOf("settlement") != -1)
                 {
                     try{
                         message = message.substring(message.indexOf("settlement") + 10);
                         Scanner scanMessage = new Scanner(message);
                         int coordinate1 = scanMessage.nextInt();
                         int coordinate2 = scanMessage.nextInt();
                         int coordinate3 = scanMessage.nextInt();
                         
                         if (!board.addSettlement(new Coordinate(coordinate1,
                                                            coordinate2,
                                                            coordinate3),
                                                 owner, true, true)) {
                             sendChatMessage("chat*SERVER: Unable to add " +
                                             "settlement.");
                         } else {
                             isGameChanged = true;
                         }
                         
                         scanMessage.close();
                         
                         }catch (Exception InputMismatchException)
                         {
                             sendChatMessage("chat*SERVER: Invalid command, " +
                                                  "you dummy!");
                         }
                     
                 }else if (message.indexOf("city") != -1)
                 {
                     try{
                         message = message.substring(message.indexOf("city") + 4);
                         Scanner scanMessage = new Scanner(message);
                         int coordinate1 = scanMessage.nextInt();
                         int coordinate2 = scanMessage.nextInt();
                         int coordinate3 = scanMessage.nextInt();
                         
                         if (!board.upgradeSettlement(new Coordinate(coordinate1,
                                                      coordinate2,
                                                      coordinate3),
                                                      owner)) {
                              sendChatMessage("chat*SERVER: Unable to add " +
                                              "city.");
                         } else {
                             isGameChanged = true;
                         }
                         
                         scanMessage.close();
                         //command
                         }catch (Exception InputMismatchException)
                         {
                             sendChatMessage("chat*SERVER: Invalid command, " +
                                     "you dummy!");
                         }
                     
                 }else if (message.indexOf("road") != -1)
                 {
                     try{
                         message = message.substring(message.indexOf("road") + 4);
                         Scanner scanMessage = new Scanner(message);
                         int coordinate1 = scanMessage.nextInt();
                         int coordinate2 = scanMessage.nextInt();
                         int coordinate3 = scanMessage.nextInt();
                         message = message.substring(message.indexOf(',') +1);
                         scanMessage.close();
                         
                         scanMessage = new Scanner(message);
                         int coordinate4 = scanMessage.nextInt();
                         int coordinate5 = scanMessage.nextInt();
                         int coordinate6 = scanMessage.nextInt();
                         
                         if (!board.addRoad(new Coordinate(coordinate1,
                                                           coordinate2,
                                                           coordinate3),
                                            new Coordinate(coordinate4,
                                                           coordinate5,
                                                           coordinate6),
                                            owner, true)) {
                             sendChatMessage("chat*SERVER: Unable to add " +
                                             "road.");
                         } else {
                             isGameChanged = true;
                         }
                         
                         scanMessage.close();
                         //command
                         }catch (Exception InputMismatchException)
                         {
                             sendChatMessage("chat*SERVER: Invalid command, " +
                                     "you dummy!");
                         }
                     
                 }else if (message.indexOf("devcard") != -1)
                 {
                 
                     if (!catanGame.drawDevelopmentCard(owner)) {
                         sendChatMessage("chat*SERVER: Unable to buy " +
                                         "development card.");
                     } else {
                         isGameChanged = true;
                     }
                 }else 
                 {
                     sendChatMessage("chat*SERVER: Invalid command, " +
                             "you dummy!");
                 }
                 
                 
             }else if(message.indexOf("tradepoint") != -1)
             {
                 try
                 {
                     message = message.substring(message.indexOf("tradepoint")+ 10);
                     Scanner messageScanner = new Scanner(message);
                     int tradeNumber = messageScanner.nextInt();
                     String resourceOne = message.substring(3, message.indexOf("to"));
                     String resourceTwo = message.substring(message.indexOf("to")+3);
                     String errorMessage = board.tradeport(tradeNumber, resourceOne, resourceTwo, owner);
                     
                     if (errorMessage != null)
                     {
                     	sendChatMessage(errorMessage);
                     }
                     
                     messageScanner.close();
                 }catch (Exception InputMismatchException)
                 {
                     sendChatMessage("chat*SERVER: Invalid command, " +
                             "you dummy!");
                 }
                 
             }else if(message.indexOf("trade") != -1)
             {
             	try
             	{
             		//Written by Kevin Zeillmann
                 	//Uses regular expressions because they're wonderful
                 	message = message.toLowerCase();
                 	String offerString = 
             			"^trade offer (wool |ore |wheat |brick |lumber )+for" +
             			"( wool| ore| wheat| brick| lumber)+";
                 	
                 	
                 	Pattern offerPattern = Pattern.compile(offerString);
                 	
                 	Matcher offerMatcher = 	offerPattern.matcher(message);
                 	
                 	if(offerMatcher.find()) {
                 		
                 		//then we know we have a valid offer syntax
                 		//first need to determine the offer and 
                 		//place that in an intArray
                 		
                 		Scanner myScan = new Scanner(message);
                 		//first we consume the first two words
                 		myScan.next();
                 		myScan.next();
                 		String nextWord = myScan.next();
                 		//now we want to see what happens until we hit the "for" string
                 		//this is guaranteed to occur since we already checked with
                 		//regular expressions
                 		
                 		//create an array to store results
                 		//initialize everything to zero
                 		int offerArray[] = new int[5];
                 		for(int i = 0; i<5; ++i)
                 		{
                 			offerArray[i] = 0;
                 		}
                 		
                 		while(!nextWord.equals("for"))
                 		{
                 			if(nextWord.equals("brick"))
                 			{
                 				offerArray[0]++;
                 				
                 			}
                 			else if(nextWord.equals("lumber"))
                 			{
                 				offerArray[1]++;
                 			}
                 			else if(nextWord.equals("ore"))
                 			{
                 				offerArray[2]++;            				
                 			}
                 			else if(nextWord.equals("wheat"))
                 			{
                 				offerArray[3]++;
                 			}
                 			else if(nextWord.equals("wool"))
                 			{
                 				offerArray[4]++;
                 			}
                 			else
                 			{
                 				System.out.println("Error: This should never happen.");
                 			}
                 			nextWord = myScan.next();
                 		}
                 		//ok we just scanned for - so now we scan for the other array
                 		int acceptArray[] = new int[5];
                 		for(int i = 0; i<5; ++i)
                 		{
                 			acceptArray[i] = 0;
                 		}
                 		while(myScan.hasNext())
                 		{
                 			nextWord = myScan.next();
                 			if(nextWord.equals("brick"))
                 			{
                 				acceptArray[0]++;
                 				
                 			}
                 			else if(nextWord.equals("lumber"))
                 			{
                 				acceptArray[1]++;
                 			}
                 			else if(nextWord.equals("ore"))
                 			{
                 				acceptArray[2]++;            				
                 			}
                 			else if(nextWord.equals("wheat"))
                 			{
                 				acceptArray[3]++;
                 			}
                 			else if(nextWord.equals("wool"))
                 			{
                 				acceptArray[4]++;
                 			}
                 			else //never happens
                 			{
                 				System.out.println("Error: This should never happen.");
                 			}
                 			
                 			
                 		}
                 		//so we have two arrays with the info          		
                 		
                 		
                 		//then we determine whether the player has the cards
                 		//necessary to complete the trade
                 		if(owner.getNumCards(ResourceCard.BRICK.toString()) >= offerArray[0]
                 		&& owner.getNumCards(ResourceCard.LUMBER.toString()) >= offerArray[1]
                 		&& owner.getNumCards(ResourceCard.ORE.toString()) >= offerArray[2]
                 		&& owner.getNumCards(ResourceCard.WHEAT.toString()) >= offerArray[3]
                 		&& owner.getNumCards(ResourceCard.WOOL.toString()) >= offerArray[4])
                 		{
                 			//create the trade
                 			catanGame.setActiveTrade(offerArray, acceptArray);
                 		}
                 		else
                 		{
                 			sendChatMessage("chat*SERVER: You don't have the resources to " +
                 					"offer that trade");
                 		}
                 		myScan.close();
                 		                 
                     }
                
                 	else{
                 		sendChatMessage("chat*SERVER: Invalid trade command");
                     }
                 	
                 	
                     /*int andIndex1 = message.indexOf("and");
                     int andIndex2 = message.indexOf("and", andIndex1 +1);
                     int forIndex = message.indexOf("for");
                     
                     if (forIndex != -1)
                     {
                         try
                         {
                         message = message.substring(6);
                     //  System.out.println(message);
                         if (andIndex1 == -1)
                         {
                             //single trade
                             Scanner numberScan = new Scanner(message);
                             int thisItemNumber = numberScan.nextInt();
                             String thisItem = message.substring(2, message.indexOf("for")-1);
                             message = message.substring(message.indexOf("for")+3);
                             numberScan.close();
                             numberScan = new Scanner(message);
                             int thatItemNumber = numberScan.nextInt();
                             String thatItem = message.substring(3);
                             //command
                             System.out.println(thisItemNumber);
                             System.out.println(thisItem);
                             System.out.println(thatItemNumber);
                             System.out.println(thatItem);
                             
                             numberScan.close();
                         }else if (andIndex2 == -1 && andIndex1 < forIndex)
                         {
                             //2 for 1 trade
                         //  System.out.println(message);
                             Scanner numberScan = new Scanner(message);
                             int thisItemNumber = numberScan.nextInt();
                             String thisItem = message.substring(2, message.indexOf("and")-1);
                             message = message.substring(message.indexOf("and")+4);
                             //System.out.println(message);
                             numberScan.close();
                             numberScan = new Scanner(message);
                             int andThisItemNumber = numberScan.nextInt();
                             String andThisItem = message.substring(2, message.indexOf("for")-1);
                             
                             message = message.substring(message.indexOf("for")+4);
                         //  System.out.println(message);
                             numberScan.close();
                             numberScan = new Scanner(message);
                             int thatItemNumber = numberScan.nextInt();
                             String thatItem = message.substring(2);
                             //command
 
 
                             System.out.println(thisItemNumber);
                             System.out.println(thisItem);
                             System.out.println(andThisItemNumber);
                             System.out.println(andThisItem);
                             System.out.println(thatItemNumber);
                             System.out.println(thatItem);
                             
                             numberScan.close();
 
                         }else if (andIndex2 == -1 && andIndex1 > forIndex)
                         {
                             //1 for 2 trade
                             //System.out.println(message);
                             Scanner numberScan = new Scanner(message);
                             int thisItemNumber = numberScan.nextInt();
                             String thisItem = message.substring(2 ,message.indexOf("for")-1);
                             message = message.substring(message.indexOf("for")+4);
                         //  System.out.println(message);
                             numberScan.close();
                             numberScan = new Scanner(message);
                             int thatItemNumber = numberScan.nextInt();
                             String thatItem = message.substring(2, message.indexOf("and")-1);
                             message = message.substring(message.indexOf("and")+4);
                         //  System.out.println(message);
                             numberScan.close();
                             numberScan = new Scanner(message);
                             int andThatItemNumber = numberScan.nextInt();
                             String andThatItem = message.substring(2);
                             //command
                             
                             System.out.println(thisItemNumber);
                             System.out.println(thisItem);
                             System.out.println(thatItemNumber);
                             System.out.println(thatItem);
                             System.out.println(andThatItemNumber);
                             System.out.println(andThatItem);
                             numberScan.close();
 
                         }else
                         {
                             //2 for 2 trade 
                             Scanner numberScan = new Scanner(message);
                             int thisItemNumber = numberScan.nextInt();
                             String thisItem = message.substring(2, message.indexOf("and")-1);
                             message = message.substring(message.indexOf("and")+4);
                             //System.out.println(message);
                             numberScan.close();
                             numberScan = new Scanner(message);
                             int andThisItemNumber = numberScan.nextInt();
                             String andThisItem = message.substring(2, message.indexOf("for")-1);
                             message = message.substring(message.indexOf("for")+4);
                             //System.out.println(message);
                             numberScan.close();
                             numberScan = new Scanner(message);
                             int thatItemNumber = numberScan.nextInt();
                             String thatItem = message.substring(2, message.indexOf("and")-1);
                             message = message.substring(message.indexOf("and")+4);
                             //System.out.println(message);
                             numberScan.close();
                             numberScan = new Scanner(message);
                             int andThatItemNumber = numberScan.nextInt();
                             String andThatItem = message.substring(2);
                             //command
                             //command
                             System.out.println(thisItemNumber);
                             System.out.println(thisItem);
                             System.out.println(andThisItemNumber);
                             System.out.println(andThisItem);
                             System.out.println(thatItemNumber);
                             System.out.println(thatItem);
                             System.out.println(andThatItemNumber);
                             System.out.println(andThatItem);
                             numberScan.close();
                         }
                         }catch (Exception InputMismatchException)
                         {
                             sendChatMessage("chat*SERVER: Invalid command, " +
                                     "you dummy!");
                        }
                     */
             	}
             	catch(InputMismatchException e)
             	{
             		sendChatMessage("chat*SERVER: Invalid chat message");
             	}
             	
             }else if(message.indexOf("play") != -1)
             {
                 if(message.indexOf("year of plenty") != -1)
                 {
                 	for (DevelopmentCard devCard : owner.devCards) {
             	        if (devCard.getDevCardType() == 
             	            DevelopmentCard.DevCardType.YEAR_OF_PLENTY) {
             	            
             	            owner.devCards.remove(devCard);
             	            
                         	owner.yearOfPlentyMode = 2;
             	            break;
             	        }
             	    }
                 	if (owner.yearOfPlentyMode == 0)
                 	{
                 		sendChatMessage("chat*SERVER: You don't have a year of plenty");
                 	}
                     
                 }else if (message.indexOf("knight") != -1)
                 {
                 	if (owner.playKnight())
                 	{
                 		owner.robberMode = true;
                 	}
                 }else if (message.indexOf("road builder") != -1)
                 {
                 	for (DevelopmentCard devCard : owner.devCards) {
             	        if (devCard.getDevCardType() == 
             	            DevelopmentCard.DevCardType.ROAD_BUILDING) {
             	            
             	            owner.devCards.remove(devCard);
             	            
             	            owner.roadBuilderMode = 2;
             	            break;
             	        }
             	    }
                 	if (owner.roadBuilderMode == 0)
                 	{
                 		sendChatMessage("chat*SERVER: You don't have a road builder");
                 	}
                 }else if (message.indexOf("monopoly") != -1)
                 {
                 	boolean monopolized = false;
                     String resourceToMonopolize = message.substring(message.indexOf("monopoly")+9);
                     for (DevelopmentCard devCard : owner.devCards) {
             	        if (devCard.getDevCardType() == 
             	            DevelopmentCard.DevCardType.MONOPOLY) {
             	            
             	            if (resourceToMonopolize.equals("WOOL"))
                         	{
             	            	int bonusResources = 0;
             	        		for(int i=0; i<4; i++){
             	        			if(i != catanGame.getTurn()){
             	        				catanGame.getPlayerArray()[i].removeCards(resourceToMonopolize, catanGame.getPlayerArray()[i].getNumCards(resourceToMonopolize));
             	        				bonusResources+=catanGame.getPlayerArray()[i].getNumCards(resourceToMonopolize);
             	        			}
             	        		}
             	            owner.devCards.remove(devCard);
             	            monopolized = true;
                         	}else if(resourceToMonopolize.equals("BRICK"))
                         	{         
                         		int bonusResources = 0;
             	        		for(int i=0; i<4; i++){
             	        			if(i != catanGame.getTurn()){
             	        				catanGame.getPlayerArray()[i].removeCards(resourceToMonopolize, catanGame.getPlayerArray()[i].getNumCards(resourceToMonopolize));
             	        				bonusResources+=catanGame.getPlayerArray()[i].getNumCards(resourceToMonopolize);
             	        			}
             	        		}
                         	owner.devCards.remove(devCard);
             	            monopolized = true;
                         	}else if(resourceToMonopolize.equals("LUMBER"))
                         	{         
                         		int bonusResources = 0;
             	        		for(int i=0; i<4; i++){
             	        			if(i != catanGame.getTurn()){
             	        				catanGame.getPlayerArray()[i].removeCards(resourceToMonopolize, catanGame.getPlayerArray()[i].getNumCards(resourceToMonopolize));
             	        				bonusResources+=catanGame.getPlayerArray()[i].getNumCards(resourceToMonopolize);
             	        			}
             	        		}
                         	owner.devCards.remove(devCard);
             	            monopolized = true;
                         	}else if(resourceToMonopolize.equals("WHEAT"))
                         	{          
                         		int bonusResources = 0;
             	        		for(int i=0; i<4; i++){
             	        			if(i != catanGame.getTurn()){
             	        				catanGame.getPlayerArray()[i].removeCards(resourceToMonopolize, catanGame.getPlayerArray()[i].getNumCards(resourceToMonopolize));
             	        				bonusResources+=catanGame.getPlayerArray()[i].getNumCards(resourceToMonopolize);
             	        			}
             	        		}
                         	owner.devCards.remove(devCard);
             	            monopolized = true;
                         	}else if(resourceToMonopolize.equals("ORE"))
                         	{
                         		int bonusResources = 0;
             	        		for(int i=0; i<4; i++){
             	        			if(i != catanGame.getTurn()){
             	        				catanGame.getPlayerArray()[i].removeCards(resourceToMonopolize, catanGame.getPlayerArray()[i].getNumCards(resourceToMonopolize));
             	        				bonusResources+=catanGame.getPlayerArray()[i].getNumCards(resourceToMonopolize);
             	        			}
             	        		}
                 	            owner.devCards.remove(devCard);
                 	            monopolized = true;
                 	        }else 
                         	{
                         		sendChatMessage("chat*Server: Wrong resource");
                         	}
 
             	            break;
             	        }
             	    }
                 	if (!monopolized)
                 	{
                 		sendChatMessage("chat*SERVER: You don't have a monopoly");
                 	}
                     
                 }else
                 {
                     sendChatMessage("chat*SERVER: Invalid command, " + "you dummy!");
                 }
                 
             }else if(message.indexOf("Get Ye Flask") != -1)
             {
                 sendChatMessage("chat*Server: You Can't Get Ye Flask");
                 owner.addCards("BRICK", 10);
                 owner.addCards("WHEAT", 10);
                 owner.addCards("LUMBER", 10);
                 owner.addCards("ORE", 10);
                 owner.addCards("WOOL", 10);
                 isGameChanged = true;
               //debug mode  
             }else
             {
                 sendChatMessage("chat*SERVER: Do not pass go, " +
                                 "you will not collect $200.");
             }
             
             // If the game was changed from any of the commands, notify all of
             // the players that the game has been changed so that they can
             // send the updated information to their clients.
             if (isGameChanged) {
                 synchronized (catanGame.gameChangedNotifier) {
                     catanGame.gameChangedNotifier.notifyAll();
                 }
             }
             
         }
         
         /**
          * Waits for notification of a change to the game. When the game
          * changes, send the update to the user.
          */
         private void handleGamePush() {
             while (!Thread.interrupted()) {
                 try {
                     // Wait for a notification that the lobby has been 
                     // updated
                     synchronized (catanGame.gameChangedNotifier) {
                         catanGame.gameChangedNotifier.wait();
                     }
                     
                     printServerMsg("Sending game data");
                     
                     // Send the game data to the user
                     synchronized (catanGame) {
                         synchronized (objOutputStream) {
                             objOutputStream.reset();
                             objOutputStream.writeObject("game*");
                             objOutputStream.writeObject(catanGame);
                             objOutputStream.flush();
                         }
                     }
                     
                 } catch (InterruptedException e) {
                     // End the thread since it has been interrupted
                     break;
                 } catch (NotSerializableException e) {
                     e.printStackTrace();
                     System.out.println(e.getMessage());
                 } catch (Exception e) {
                     e.printStackTrace();
                     // Just continue loop
                 }
             }
 
             printServerMsg("Ending game push thread.");
             
         }
         
         /**
          * Send a chat message to the client.
          * @param message
          */
         public void sendChatMessage(String message) throws Exception {
             printServerMsg("Sending chat message to " + username + ": " + 
                            message);
             synchronized (objOutputStream) {
                 objOutputStream.writeObject(message);
                 objOutputStream.flush();
             }
         }
         
         /**
          * Send a message to the client notifying them the game is over.
          */
         public void sendGameOverMsg() throws Exception {
             printServerMsg("Sending game over message to client '" + username +
                            "'");
             synchronized (objOutputStream) {
                 objOutputStream.writeObject("Game Over");
                 objOutputStream.flush();
             }
         }
         
         /**
          * Sends a message to the client indicating a roll has been made.
          * @param roll
          * @param currentPlayer
          */
         public void sendRollMessage(int roll, int currentPlayer) 
                                                               throws Exception {
             Player playerArray[] = catanGame.getPlayerArray();
             
             // Write the data to the client
             synchronized (objOutputStream) {
                 objOutputStream.reset();
                 
                 objOutputStream.writeObject("roll*");
                 objOutputStream.writeInt(roll);
                 objOutputStream.writeInt(currentPlayer);
                 objOutputStream.writeObject(playerArray);
                 
                 objOutputStream.flush();
             }
         }
         
     }
 }
