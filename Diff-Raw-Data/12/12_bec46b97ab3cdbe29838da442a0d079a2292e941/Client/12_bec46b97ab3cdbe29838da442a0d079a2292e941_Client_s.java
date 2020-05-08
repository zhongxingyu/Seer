 package dungeon.ui;
 
 import dungeon.game.messages.PlayerJoinCommand;
 import dungeon.messages.LifecycleEvent;
 import dungeon.messages.Mailman;
 import dungeon.messages.Message;
 import dungeon.messages.MessageHandler;
 import dungeon.models.Player;
 import dungeon.models.Room;
 import dungeon.models.World;
 import dungeon.models.messages.Transform;
 import dungeon.server.Server;
 import dungeon.ui.messages.MenuCommand;
 import dungeon.ui.messages.PlayerMessage;
 
 import java.io.IOException;
 import java.net.SocketException;
 import java.util.List;
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.concurrent.atomic.AtomicReference;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * A client to the game server.
  *
  * This handles the communication with the server, holds a local version of the world and the ID of the player.
  */
 public class Client implements MessageHandler {
   private static final Logger LOGGER = Logger.getLogger(Client.class.getName());
 
   private final Mailman mailman;
 
   private final AtomicReference<Integer> playerId = new AtomicReference<>();
 
   private final AtomicReference<World> world = new AtomicReference<>();
 
   private ServerConnection serverConnection;
 
   private final MessageForwarder messageForwarder = new MessageForwarder(this);
 
   private Server server;
 
   public Client (Mailman mailman) {
     this.mailman = mailman;
   }
 
   @Override
   public void handleMessage (Message message) {
     if (message instanceof Transform) {
       this.world.set(this.world.get().apply((Transform)message));
     } else if (message instanceof PlayerMessage
       && ((PlayerMessage) message).getPlayerId() == this.playerId.get()
       && this.serverConnection != null) {
       try {
         this.serverConnection.write(message);
       } catch (IOException e) {
         LOGGER.log(Level.WARNING, "Could not send message to server", e);
       }
     } else if (message == LifecycleEvent.SHUTDOWN) {
       this.stop();
     }
   }
 
   public void send (Message message) {
     this.mailman.send(message);
   }
 
   public int getPlayerId () {
     return this.playerId.get();
   }
 
   /**
    * @return The player object in the current {@link #world} belonging to this client
    */
   public Player getPlayer () {
     return this.world.get().getPlayer(this.getPlayerId());
   }
 
   /**
    * @return All connected players
    */
   public List<Player> getPlayers () {
     return this.world.get().getPlayers();
   }
 
   /**
    * @return The room that this client is currently in
    */
   public Room getCurrentRoom () {
     Player player = this.getPlayer();
 
     if (player == null) {
       return null;
     } else {
       return this.world.get().getCurrentRoom(player);
     }
   }
 
   public void connect (String host, int port) {
     try {
       this.serverConnection = new ServerConnection(host, port);
     } catch (IOException e) {
       LOGGER.log(Level.WARNING, "Could not connect", e);
     }
 
     LOGGER.info("Acquire ID");
 
     try {
       this.playerId.set((Integer)this.serverConnection.read());
     } catch (IOException e) {
       e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
     } catch (ClassNotFoundException e) {
       e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
     }
 
     LOGGER.info("Synchronize world");
 
     try {
       this.world.set((World)this.serverConnection.read());
     } catch (IOException e) {
       LOGGER.log(Level.WARNING, "IOError", e);
     } catch (ClassNotFoundException e) {
       LOGGER.log(Level.WARNING, "Class not found", e);
     }
 
     LOGGER.info("Join player");
     Player player = new Player(this.playerId.get(), "Link");
 
     try {
       this.serverConnection.write(new PlayerJoinCommand(player));
     } catch (IOException e) {
       LOGGER.log(Level.WARNING, "Could not join", e);
     }
 
     this.messageForwarder.start();
 
     this.send(MenuCommand.START_GAME);
   }
 
   public void startServer (int port) {
     try {
       this.server = new Server(port);
     } catch (Exception ex) {
       LOGGER.warning("Could not start server");
       return;
     }
 
     server.start();
 
     try {
       Thread.sleep(500);
     } catch (InterruptedException e1) {
       // Ignore
     }
 
     this.connect("localhost", port);
   }
 
   private void stop () {
     LOGGER.info("Shutdown client");
 
     this.messageForwarder.stop();
    this.serverConnection.close();
 
     if (this.server != null) {
       this.server.stop();
     }
   }
 
   /**
    * Injects messages from the server into the local event system.
    */
   private static class MessageForwarder implements Runnable {
     private static final Logger LOGGER = Logger.getLogger(MessageForwarder.class.getName());
 
     private final Client client;
 
     private final Thread thread = new Thread(this);
 
     private final AtomicBoolean running = new AtomicBoolean(false);
 
     private MessageForwarder (Client client) {
       this.client = client;
     }
 
     @Override
     public void run () {
       this.running.set(true);
 
       while (this.running.get()) {
         try {
           Object received = this.client.serverConnection.read();
 
           if (received instanceof Message) {
             this.client.mailman.send((Message)received);
           }
         } catch (SocketException e) {
           LOGGER.log(Level.INFO, "The socket has been closed", e);
           this.stop();
         } catch (IOException e) {
           LOGGER.log(Level.WARNING, "Something failed while receiving from the server", e);
         } catch (ClassNotFoundException e) {
           LOGGER.log(Level.WARNING, "Received message of unknown class", e);
         }
       }
     }
 
     public void start () {
       this.thread.start();
     }
 
     public void stop () {
       this.running.set(false);
     }
   }
 }
