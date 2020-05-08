 package vooga.rts.networking.server;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.ConsoleHandler;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import util.logger.HandlerMail;
 import util.logger.HandlerMemory;
 import util.logger.IVoogaHandler;
 import util.logger.LoggerManager;
 import vooga.rts.networking.NetworkBundle;
 import vooga.rts.networking.communications.ExpandedLobbyInfo;
 import vooga.rts.networking.communications.LobbyInfo;
 import vooga.rts.networking.communications.Message;
 import vooga.rts.networking.communications.clientmessages.ClientInfoMessage;
 
 
 /**
  * Class that provides default behavior for IThreadContainer and provides other behaviors
  * for thread containers. This is the superclass for MatchmakerServer, GameContainer, Room, Lobby,
  * and GameServer. This provides default (empty) behavior for IThreadContainer and default
  * (non-empty) behavior for IMessageReceiver.
  * 
  * @author David Winegar
  * 
  */
 public abstract class AbstractThreadContainer implements IThreadContainer, IMessageReceiver {
 
     private Map<Integer, ConnectionThread> myConnectionThreads =
             new HashMap<Integer, ConnectionThread>();
     private Logger myLogger;
     public static final IVoogaHandler EMAIL_HANDLER =
             new HandlerMemory(new HandlerMail("vooga-networking-logger@duke.edu",
                                                  new String[] { "david.s.winegar@gmail.com" },
                                                  "mail.smtp.host", "Log update",
                                                  "New log item received: \n"), 1, Level.SEVERE);
 
     /**
      * Default empty constructor, initializes state and logger.
      */
     public AbstractThreadContainer () {
         LoggerManager log = new LoggerManager();
         log.addHandler(EMAIL_HANDLER);
         myLogger = log.getLogger();
         myLogger.addHandler(new ConsoleHandler());
     }
 
     /**
      * Constructor that copies all current threads from the AbstractThreadContainer passed in.
      * 
      * @param container AbstractThreadContainer
      */
     public AbstractThreadContainer (AbstractThreadContainer container) {
         this();
         myConnectionThreads = new HashMap<Integer, ConnectionThread>(container.myConnectionThreads);
         for (ConnectionThread thread : myConnectionThreads.values()) {
             thread.switchMessageServer(this);
         }
     }
     
     /**
      * This method returns the logger for the class.
      * @return logger
      */
     protected Logger getLogger () {
         return myLogger;
     }
 
     @Override
     public void joinGameContainer (ConnectionThread thread, String gameName) {
     }
 
     @Override
     public void joinLobby (ConnectionThread thread, int lobbyNumber) {
     }
 
     @Override
     public void leaveLobby (ConnectionThread thread, ExpandedLobbyInfo lobbyInfo) {
     }
 
     @Override
     public void requestGameStart (ConnectionThread thread) {
     }
 
     @Override
     public void requestLobbies (ConnectionThread thread) {
     }
 
     @Override
     public void startLobby (ConnectionThread thread, LobbyInfo lobbyInfo) {
     }
 
     @Override
     public void updateLobbyInfo (ConnectionThread thread, ExpandedLobbyInfo myLobbyInfo) {
     }
 
     @Override
     public void clientIsReadyToStart (ConnectionThread thread) {
 
     }
 
     @Override
     public void removeConnection (ConnectionThread thread) {
         myConnectionThreads.remove(thread.getID());
     }
 
     /**
      * Receives a message and then executes default behavior, stamping it and if it is a
      * systemMessage, executing it.
      * 
      * @param message message received
      * @param thread thread received from
      */
     @Override
     public void receiveMessageFromClient (Message message, ConnectionThread thread) {
         LoggerManager.DEFAULT_LOGGER.log(Level.FINEST, NetworkBundle.getString("MessageReceived") +
                                                        thread.getID());
         stampMessage(message);
         if (message instanceof ClientInfoMessage) {
             ClientInfoMessage systemMessage = (ClientInfoMessage) message;
             systemMessage.affectServer(thread, this);
         }
     }
 
     /**
      * Overridable method for stamping this message called by receiveMessageFromClient.
      * 
      */
     protected void stampMessage (Message message) {
         message.stampTime();
     }
 
     /**
      * Adds a connection.
      */
     protected void addConnection (ConnectionThread thread) {
         myConnectionThreads.put(thread.getID(), thread);
         thread.switchMessageServer(this);
     }
 
     /**
      * Send a message to all connection threads.
      */
     protected void sendMessageToAllConnections (Message message) {
         for (ConnectionThread thread : myConnectionThreads.values()) {
             thread.sendMessage(message);
         }
     }
 
     /**
      * Send a message to a specific connection thread.
      */
     protected void sendMessageToClient (ConnectionThread thread, Message message) {
         thread.sendMessage(message);
     }
 
     /**
      * Returns whether the AbstractThreadContainer has any connection threads or not.
      */
     protected boolean haveNoConnections () {
         return myConnectionThreads.isEmpty();
     }
 
     /**
      * Removes all connection threads.
      */
     protected void removeAllConnections () {
         myConnectionThreads.clear();
     }
 
     protected int getNumberOfConnections () {
         return myConnectionThreads.size();
     }
 }
