 package risk.game.server;
 
 import java.io.IOException;
 import java.net.*;
 import java.util.LinkedList;
 import risk.common.Logger;
 import risk.common.RiskIO;
 import risk.common.Settings;
 import risk.game.Game;
 import risk.game.Player;
 import risk.network.QueuedSender;
 import risk.protocol.ServerCommandVisitor;
 import risk.protocol.command.Command;
 import risk.protocol.command.CommandFromClient;
 import risk.protocol.command.GameEndedCmd;
 
 public class GameServer extends Thread implements ConnectionAcceptor, CommandExecutor, CommandSender {
     /**
      * A listener that accepts connections from clients.
      */
     private ConnectionListener listener;
 
     /**
      * A list of ClientHandlers that handles clients.
      */
     private LinkedList<ClientHandler> clientHandlers = new LinkedList<ClientHandler>();
 
     /**
      * A ThreadGroup for the ClientHandler threads.
      */
     private ThreadGroup threadGroup = new ThreadGroup("ClientHandlerGroup");
 
     /**
      * This object protects the ClientHandler list from being accessed/modified
      * from more than one thread at the same time.
      */
     private Object clientHandlerLock = new Object();
 
     private LinkedList<CommandFromClient> commandQueue = new LinkedList<CommandFromClient>();
 
     /**
      * This object protects the CommandQueue from being accessed/modified
      * from more than one thread at the same time.
      */
     private Object commandLock = new Object();
 
     private static final int COMMAND_INTERRUPT_TIMEOUT = 1000;
 
     private Game game = new Game();
 
     private boolean closingDown = false;
 
     public GameServer() {
         super("GameServerThread");
     }
 
     /**
      * Call this when the GameServer thread exits. This will do some cleanup
      * and interrupt other threads.
      */
     private void onExit() {
         Logger.loginfo("Exiting, interrupting server threads");
         if (listener != null) {
             try {
                 listener.interrupt();
             } catch (SecurityException e) {
                 Logger.logexception(e, "Insufficient permissions to interrupt thread");
             }
         }
 
         synchronized (clientHandlerLock) {
             for (ClientHandler ch : clientHandlers) {
                 try {
                     ch.interrupt();
                 } catch (SecurityException e) {
                     Logger.logexception(e, "Insufficient permissions to interrupt thread");
                 }
             }
         }
     }
 
     @Override
     public void run() {
         try {
             try {
                 Logger.loginfo("Server started");
 
                 // Start listener thread
                 startListening();
 
                 // Wait for incoming commands
                 handleCommands();
             } finally {
                 onExit();
             }
         } catch (Exception e) {
             Logger.logexception(e, "Unhandled exception");
         }
     }
 
     /**
      * This function starts the listener thread that will wait for incoming connections.
      */
     private void startListening() {
         Logger.logdebug("Starting listener thread");
         listener = new ConnectionListener(Settings.getInstance().getServerListenPort(), this);
         listener.start();
     }
 
     /**
      * This function waits for Commands being put to CommandQueue, and processes them.
      */
     private void handleCommands() {
         while (!interrupted()) {
             CommandFromClient ccmd;
             synchronized (commandLock) {
                 if (commandQueue.isEmpty()) {
                     try {
                         commandLock.wait(COMMAND_INTERRUPT_TIMEOUT);
                     } catch (InterruptedException e) {
                         break;
                     }
                 }
                 if (commandQueue.isEmpty())
                     continue;
                 ccmd = commandQueue.removeFirst();
                 Logger.logdebug("Got command from CommandQueue");
             }
             ServerCommandVisitor scv = new ServerCommandVisitor(this, game, game, ccmd.clientHandler);
             ccmd.cmd.accept(scv);
         }
     }
 
     /**
      * This function is called by ConnectionListener upon a new connection.
      * It creates a new ClientHander for the connection.
      */
     @Override
     public void newConnection(Socket s) {
         Logger.loginfo("New connection arrived from " + s.getInetAddress() + ":" + s.getPort());
         try {
             ClientHandler tmp = new ClientHandler(threadGroup, s, this, this);
             synchronized (clientHandlerLock) {
                 clientHandlers.add(tmp);
                 tmp.start();
             }
         } catch (IOException e) {
             Logger.logexception(e, "Client connection LOST!");
         }
     }
 
     /**
      * This function is called by ConnectionListener when ConnectionListener exits.
      */
     @Override
     public void stoppedListening() {
         Logger.logdebug("Stopped listening.");
     }
 
     /**
      * This function allows incoming commands to be put in a queue, that will be
      * processed by the GameServer.
      */
     @Override
     public void QueueCommand(CommandFromClient ccmd) {
         synchronized (commandLock) {
             commandQueue.add(ccmd);
             commandLock.notify();
         }
     }
 
     @Override
     public void sendCmd(Command cmd, Player p) {
         synchronized (clientHandlerLock) {
             for (ClientHandler ch : clientHandlers) {
                 if (p == null || ch.getPlayer() == p)
                     ch.queueForSend(cmd);
             }
         }
     }
     
     @Override
     public void sendPlayerJoinedCmd(Player p) {
         synchronized (clientHandlerLock) {
             for (ClientHandler ch : clientHandlers) {
                 ch.queuePlayerJoinedForSend(p);
             }
         }
     }
 
     @Override
     public void connectionLost(ClientHandler ch) {
         boolean needSend = false;
         synchronized (clientHandlerLock) {
             clientHandlers.remove(ch);
             if (!closingDown) {
                 closingDown = true;
                 needSend  = true;
             }
         }
         if (needSend) {
             Logger.logdebug("Connection lost, notifying users");
             Player p = ch.getPlayer();
             sendCmd(new GameEndedCmd(p, GameEndedCmd.QUIT), null);
             closeConnections();
         }
     }
 
     @Override
     public void closeConnections() {
         synchronized (clientHandlerLock) {
             for (ClientHandler ch : clientHandlers) {
                 ch.closeConnection();
             }
            clientHandlers.clear();
         }
     }
 }
