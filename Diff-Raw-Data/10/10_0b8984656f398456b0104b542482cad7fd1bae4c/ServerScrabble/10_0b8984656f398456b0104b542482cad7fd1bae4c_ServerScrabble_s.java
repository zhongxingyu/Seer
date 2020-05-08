 package server.server.ctrl;
 
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.ServerSocket;
 import java.net.Socket;
 import server.common.GameException;
 import server.common.Message;
 import server.server.connection.ServerProtocol;
 import server.server.model.GameFactory;
 import server.server.model.IGame;
 
 /**
  *
  * @author Bernard <bernard.debecker@gmail.com>, Romain <ro.foncier@gmail.com>
  */
 public class ServerScrabble {
 
     private IGame game;
     private int port = 8189;
     private ObjectMapper om = new ObjectMapper();
 
     public static void main(String[] args) {
         ServerScrabble serverScrabble = new ServerScrabble();
         serverScrabble.start();
     }
 
     public ServerScrabble() {
         game = GameFactory.getGame();
     }
 
     public ServerScrabble(int port) {
         this.port = port;
     }
 
     public void start() {
         int connectionNumber = 0;
         try {
             InetAddress address = InetAddress.getLocalHost();
             System.out.println("Address : " + address);
             ServerSocket server = new ServerSocket(port);
             while (true) {
                 System.out.println("Connection(s) established : " + connectionNumber);
                 System.out.println("Current actived thread(s) :" + Thread.activeCount());
                 Socket s = server.accept();
                 connectionNumber++;
                 ServerProtocol sProto = new ServerProtocol(s);
                 Thread clientThread = new ThreadCtrl(sProto, this);
                 clientThread.start();
                 clientThread.interrupt();
             }
         } catch (Exception e) {
             System.out.println("Error : " + e);
         }
     }
 
     /**
      * Allows to create a new account.
      *
      * @param pl_name
      * @param pl_pwd
      * @return the player UUID
      */
     public synchronized Message newAccount(String pl_name, String pl_pwd) {
         Message response = null;
         try {
             // Try to create a new player acount
             response = game.newAccount(pl_name, pl_pwd);
             if (response == null) {
                 throw new GameException(GameException.typeErr.SYSKO);
             }
         } catch (GameException e) {
             response = processError(e);
         }
         return response;
     }
 
     /**
      * Allows to log the current player.
      *
      * @param pl_name
      * @param pl_pwd
      * @return the player UUID
      */
     public synchronized Message login(String pl_name, String pl_pwd) {
         Message response = null;
         try {
             // Try to log the current player
             response = game.login(pl_name, pl_pwd);
 
             if (response == null) {
                 throw new GameException(GameException.typeErr.SYSKO);
             }
         } catch (GameException e) {
             response = processError(e);
         }
         return response;
     }
 
     /**
      * Allows to logout the current user.
      *
      * @param pl_id
      * @return
      */
     public synchronized Message logout(String pl_id) {
         Message response = null;
         try {
             // Try to log the current player
             response = game.logout(pl_id);
 
             if (response == null) {
                 throw new GameException(GameException.typeErr.SYSKO);
             }
         } catch (GameException e) {
             response = processError(e);
         }
         return response;
     }
 
     /**
      * Allows to create a new Play instance.
      *
      * @param playerID
      * @return the new playID and the formatedRack.
      */
     public synchronized Message createNewPlay(String playerID) {
         Message response = null;
         try {
             // Try to create a new game for the current player
             response = game.createNewPlay(playerID);
 
             if (response == null) {
                 throw new GameException(GameException.typeErr.SYSKO);
             }
         } catch (GameException e) {
             response = processError(e);
         }
         return response;
     }
 
     /**
      * Allows to log an anonymous player/
      *
      * @param pl_id
      * @return status
      */
     public synchronized Message newAnonymGame(String pl_id) {
         Message response = null;
         try {
             // Try to create a new game for the current anonymous player
             response = game.createNewAnonymPlay(pl_id);
             if (response == null) {
                 throw new GameException(GameException.typeErr.SYSKO);
             }
         } catch (GameException e) {
             response = processError(e);
         }
         return response;
     }
 
     /**
      * Allows to load the list of plays for the current player.
      * @param playerID
      * @return a formated list of plays for this player.
      */
     public synchronized Message loadPlayList(String playerID) {
         Message response = null;
         try {
             // Try to load the plays list for the current player
             response = game.loadPlayList(playerID);
 
             if (response == null) {
                 throw new GameException(GameException.typeErr.SYSKO);
             }
         } catch (GameException e) {
             response = processError(e);
         }
         return response;
     }
 
     /**
      * Allows to load a specific play for the current player.
      * @param playerID
      * @param playID
      * @return a formated list of word in JSON and a formatedRack.
      */
     public synchronized Message loadGame(String playerID, String playID) {
         Message response = null;
         try {
             // Try to load an existed play for the current player
             response = game.loadSavedPlay(playerID, playID);
 
             if (response == null) {
                 throw new GameException(GameException.typeErr.SYSKO);
             }
         } catch (GameException e) {
             response = processError(e);
         }
         return response;
     }
 
     public synchronized Message saveGame(int type, String playerID, String playID, String ga_infos) {
         Message response = null;
         try {
             // Try to save current play for the current player
             response = game.SavePlay(type, playerID, playID, ga_infos);
 
             if (response == null) {
                 throw new GameException(GameException.typeErr.SYSKO);
             }
         } catch (GameException e) {
             response = processError(e);
         }
         return response;
     }
 
     /**
      * Try to delete all informations which belong to the current anonymous
      * user.
      * @param playerID
      * @return status
      */
     public synchronized Message deleteAnonym(String playerID) {
         Message response = null;
         try {
             // Try to delete an existed play for the current anonymous player
             response = game.deleteAnonym(playerID);
 
             if (response == null) {
                 throw new GameException(GameException.typeErr.SYSKO);
             }
         } catch (GameException e) {
             response = processError(e);
         }
         return response;
     }
 
     public synchronized Message gameTreatment(String data) {
         Message response = null;
         System.out.println(data);
         try {
             // Check if the player's game is correct.
             JsonNode root = om.readTree(data);
             String test = root.get("tiles").toString();
             response = game.checkGame(root.get("player_id").asText(), root.get("play_id").asText(), root.get("orientation").asInt(), root.get("tiles").toString());
             if (response == null) {
                 throw new GameException(GameException.typeErr.SYSKO);
             }
         } catch (IOException ex) {
         } catch (GameException e) {
             response = processError(e);
         }
         return response;
     }
 
     public synchronized Message exchangeTile(String playerID, String tiles) {
         Message response = null;
         try {
             response = game.exchangeTile(playerID, tiles);
 
             if (response == null) {
                 throw new GameException(GameException.typeErr.SYSKO);
             }
         } catch (GameException e) {
             response = processError(e);
         }
         return response;
     }
 
     public synchronized Message switchTile(String playerID, String position) {
         Message response = null;
         try {
             response = game.switchTile(playerID, position);
 
             if (response == null) {
                 throw new GameException(GameException.typeErr.SYSKO);
             }
         } catch (GameException e) {
             response = processError(e);
         }
         return response;
     }
 
     /**
      * Handle errors throws during the Game process.
      *
      * @param e
      * @return a message instance with specific header.
      */
     private synchronized Message processError(GameException e) {
         Message error = null;
         switch (e.getError()) {
             case SYSKO:
                 error = new Message(Message.SYSKO, "");
                 outputPrint("Server error : System KO.");
                 break;
             case PLAYER_EXISTS:
                 error = new Message(Message.PLAYER_EXISTS, "");
                 outputPrint("Server error : Player already exists.");
                 break;
             case PLAYER_NOT_EXISTS:
                 error = new Message(Message.PLAYER_NOT_EXISTS, "");
                 outputPrint("Server error : Player does not yet exist.");
                 break;
             case LOGIN_ERROR:
                 error = new Message(Message.LOGIN_ERROR, "");
                 outputPrint("Server error : Login error.");
                 break;
             case LOGOUT_ERROR:
                 error = new Message(Message.LOGOUT_ERROR, "");
                 outputPrint("Server error : Logout error.");
                 break;
             case PLAYER_NOT_LOGGED:
                 error = new Message(Message.PLAYER_NOT_LOGGED, "");
                 outputPrint("Server error : The current player does not yet logged.");
                 break;
             case LOAD_GAME_LIST_ERROR:
                 error = new Message(Message.LOAD_GAME_LIST_ERROR, "");
                 outputPrint("Server error : The current player does not yet any plays saved on the server.");
                 break;
             case LOAD_GAME_ERROR:
                 error = new Message(Message.LOAD_GAME_ERROR, "");
                 outputPrint("Server error : The current player can not load play saved on the server.");
                 break;
             case XML_FILE_NOT_EXISTS:
                 error = new Message(Message.LOAD_GAME_ERROR, ""); // Send this error because the client side must ignore how data are saved on the server. This error concerned a data loading error. (from file or DB, ...)
                 outputPrint("Server error : The current player can not load play saved on the server.");
                 break;
             case NEW_GAME_ANONYM_ERROR:
                 error = new Message(Message.NEW_GAME_ANONYM_ERROR, "");
                 outputPrint("Server error : The current anonymous player is already logged on the server. No play may be created.");
                 break;
             case DELETE_ANONYM_ERROR:
                 error = new Message(Message.DELETE_ANONYM_ERROR, "");
                 outputPrint("Server error : The current anonymous player does not yet logged on the server. No play to remove.");
                 break;
             case GAME_IDENT_ERROR:
                 error = new Message(Message.GAME_IDENT_ERROR, "");
                 outputPrint("Server error : The current player does not yet logged on the server or can't play at specific game.");
                 break;
             case TILE_EXCHANGE_ERROR:
                 error = new Message(Message.TILE_EXCHANGE_ERROR, "");
                 outputPrint("Server error : Something went wrong with the tile exchange. Don't ask me, I don't know what.");
                 break;
         }
         return error;
     }
 
     /**
      * Displays server messages.
      *
      * @param msg
      */
     private void outputPrint(String msg) {
         System.out.println("SERVER : " + msg);
     }
 }
