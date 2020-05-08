 package demo.server;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentSkipListMap;
 
 import javax.annotation.PostConstruct;
 import javax.enterprise.context.ApplicationScoped;
 import javax.enterprise.event.Event;
 import javax.enterprise.event.Observes;
 import javax.inject.Inject;
 
 import org.jboss.errai.bus.client.api.base.MessageBuilder;
 import org.jboss.errai.bus.client.api.messaging.Message;
 import org.jboss.errai.bus.client.api.messaging.MessageBus;
 import org.jboss.errai.bus.client.api.messaging.MessageCallback;
 import org.jboss.errai.bus.client.api.messaging.RequestDispatcher;
 import org.jboss.errai.bus.server.annotations.Service;
 
 import demo.client.shared.lobby.Invitation;
 import demo.client.shared.lobby.LobbyUpdate;
 import demo.client.shared.lobby.LobbyUpdateRequest;
 import demo.client.shared.lobby.RegisterRequest;
 import demo.client.shared.message.Command;
 import demo.client.shared.message.ExitMessage;
 import demo.client.shared.message.MoveEvent;
 import demo.client.shared.message.ScoreEvent;
 import demo.client.shared.meta.GameRoom;
 import demo.client.shared.meta.Player;
 import demo.client.shared.meta.ScoreTracker;
 
 /**
  * A class for facilitating games between clients over a network.
  * 
  * This class responds to, fires events to, and catches events from clients; maintains the list of
  * games and players in the lobby; and also uses a message bus to act as a relay between clients.
  */
 @ApplicationScoped
 @Service("Relay")
 public class Server implements MessageCallback {
 
   private static final long LOBBY_TIMEOUT = 20000;
   private static final long GAME_TIMEOUT = 10000;
 
   /** A map of game ids to games that are currently in progress. */
   private Map<Integer, GameRoom> games = new ConcurrentHashMap<Integer, GameRoom>();
   /** A map of player ids to lobbyPlayers that are currently in the lobby. */
   private Map<Integer, Player> lobbyPlayers = new ConcurrentHashMap<Integer, Player>();
   /** Heart beat timestamps of players in lobby. */
   private Map<Integer, Long> lobbyHeartBeats = new ConcurrentHashMap<Integer, Long>();
   /** Heart beat timestamps of players in games. */
   private Map<Player, Long> gameHeartBeats = new ConcurrentSkipListMap<Player, Long>();
   /** This value is incremented to assign unique player ids. */
   private int curPlayerId = 1;
   /** This value is incremented to assign unique game ids. */
   private int curGameId = 1;
 
   /** Used for receiving messages from clients. */
   @Inject
   private MessageBus messageBus;
   /** Used for sending message to clients. */
   @Inject
   private RequestDispatcher dispatcher;
   /** Used for sending clients their registered player information. */
   @Inject
   private Event<Player> playerRegistration;
   /** Used for sending lobby updates to clients. */
   @Inject
   private Event<LobbyUpdate> lobbyUpdate;
 
   private Timer lobbyTimer;
   private Timer gameTimer;
 
   /** For debugging only. */
   private int debugId;
   /** For debugging only. */
   private static int curDebugId = 1;
 
   /** For debugging only. */
   private static synchronized int nextDebugId() {
     return curDebugId++;
   }
 
   /**
    * Create a Server for hosting the lobby and Block Drop games. This object is meant to be used as
    * a singleton.
    */
   public Server() {
     debugId = nextDebugId();
     System.out.println("Server" + debugId + ": Server object is constructed.");
   }
 
   @PostConstruct
   private void init() {
     lobbyTimer = new Timer();
     lobbyTimer.schedule(new TimerTask() {
       @Override
       public void run() {
         Server.this.cleanLobby();
       }
     }, 0, LOBBY_TIMEOUT);
 
     gameTimer = new Timer();
     gameTimer.schedule(new TimerTask() {
       @Override
       public void run() {
         Server.this.cleanGameRooms();
       }
     }, 0, GAME_TIMEOUT);
   }
 
   /**
    * Find and remove idle players from game rooms.
    */
   protected void cleanGameRooms() {
     long curTime = System.currentTimeMillis();
 
     for (Entry<Player, Long> heartBeat : gameHeartBeats.entrySet()) {
       if (curTime - heartBeat.getValue() > GAME_TIMEOUT) {
         removePlayerFromGame(heartBeat.getKey(), heartBeat.getKey().getGameId());
       }
     }
     System.out.println("Server: GameRooms cleaned");
   }
 
   /**
    * Find and remove idle players from the lobby.
    */
   protected void cleanLobby() {
     long curTime = System.currentTimeMillis();
     List<Integer> removed = new ArrayList<Integer>();
 
     for (Entry<Integer, Long> heartBeat : lobbyHeartBeats.entrySet()) {
       if (curTime - heartBeat.getValue() > LOBBY_TIMEOUT) {
         lobbyPlayers.remove(heartBeat.getKey());
         removed.add(heartBeat.getKey());
       }
     }
 
     lobbyHeartBeats.entrySet().removeAll(removed);
     System.out.println("Server: Lobby cleaned");
 
     if (!removed.isEmpty()) {
       sendLobbyList();
     }
   }
 
   /**
    * Get a unique id for a player.
    * 
    * @return A positive integer that is unique to a player.
    */
   public synchronized int nextPlayerId() {
     return curPlayerId++;
   }
 
   /**
    * Get a unique id for a game.
    * 
    * @return A positive integer that is unique to a game.
    */
   public synchronized int nextGameId() {
     return curGameId++;
   }
 
   /**
    * Register a player by assigning them an id and then fire this information back to the client.
    * 
    * @param request
    *          The request containing the Player object to be registered.
    */
   public void registerPlayer(@Observes RegisterRequest request) {
     Player player = request.getPlayer();
     // If the player has not been registered, give them an id.
     if (!request.hasRegistered()) {
       player.setId(nextPlayerId());
     }
     // Otherwise they are likely going from a game to the lobby, so just reset their game id.
     else {
       player.setGameId(0);
     }
 
     // Put the player in the lobby if not already.
     if (!lobbyPlayers.containsKey(player.getId())) {
       lobbyPlayers.put(player.getId(), player);
       lobbyHeartBeats.put(player.getId(), System.currentTimeMillis());
     }
 
     // For debugging.
     System.out.println("Server" + debugId + ": Player registered.");
 
     // Send the registered player back to the client.
     playerRegistration.fire(player);
   }
 
   /**
    * Respond to a lobbyUpdateRequest by sending a copy of the lobby to the client. This method
    * should only be invoked by the Errai Framework in response to a
    * {@code Event<LobbyUpdateRequest>} fired from a client.
    */
   public void handleLobbyUpdateRequest(@Observes LobbyUpdateRequest lobbyUpdateRequest) {
     sendLobbyList();
   }
 
   /**
    * Respond to an invitation by relaying it to the appropriate client. This method should only be
    * invoked by the Errai Framework in response to an {@code Event<Invitation>} fired from a client.
    */
   public void handleInvitation(@Observes Invitation invitation) {
     // For debugging.
     System.out.println("Server" + debugId + ": Invitation received from " + invitation.getHost().getName());
 
     // Make game room
     GameRoom room = new GameRoom();
     room.setId(nextGameId());
     games.put(room.getId(), room);
     invitation.setGameId(room.getId());
     addPlayerToGame(invitation.getHost(), room.getId());
 
     for (Player guest : invitation.getGuests()) {
       System.out.println("Server" + debugId + ": Attempting to relay invitation to " + "Client" + guest.getId());
       // Relay invitation to appropriate client.
       Invitation targetedInvite = new Invitation(invitation, guest);
       MessageBuilder.createMessage().toSubject("Client" + guest.getId()).command(Command.INVITATION)
               .withValue(targetedInvite).noErrorHandling().sendNowWith(dispatcher);
     }
 
     sendLobbyList();
   }
 
   private void addPlayerToGame(Player player, int gameId) {
     lobbyPlayers.remove(player.getId());
     lobbyHeartBeats.remove(player.getId());
     games.get(gameId).addPlayer(player);
     gameHeartBeats.put(player, System.currentTimeMillis());
     player.setGameId(gameId);
     MessageBuilder.createMessage().toSubject("Client" + player.getId()).command(Command.JOIN_GAME)
             .withValue(games.get(gameId)).noErrorHandling().sendNowWith(dispatcher);
     ScoreTracker scoreTracker = games.get(gameId).getScoreTracker(player);
     updateScoreLocal(scoreTracker);
     updateScoreRemote(new ScoreEvent(scoreTracker));
     System.out.println("Server: " + player.getName() + " added to Game" + gameId);
   }
 
   /**
    * Fire an {@code Event<LobbyUpdate>} to connected clients. This should result in clients
    * refreshing their lobby lists.
    */
   public void sendLobbyList() {
     lobbyUpdate.fire(new LobbyUpdate(lobbyPlayers, games));
     // For debugging.
     System.out.println("Server" + debugId + ": Lobby list sent.");
   }
 
   @Override
   public void callback(Message message) {
     switch (Command.valueOf(message.getCommandType())) {
     case JOIN_GAME:
       Invitation invitation = message.getValue(Invitation.class);
       addPlayerToGame(invitation.getTarget(), invitation.getGameId());
       sendLobbyList();
       break;
     case LEAVE_GAME:
       ExitMessage exitMessage = message.getValue(ExitMessage.class);
       removePlayerFromGame(exitMessage.getPlayer(), exitMessage.getGame().getId());
       sendLobbyList();
       break;
     case UPDATE_SCORE:
       ScoreEvent scoreEvent = message.getValue(ScoreEvent.class);
       updateScoreLocal(scoreEvent.getScoreTracker());
       updateScoreRemote(scoreEvent);
       break;
     case MOVE_UPDATE:
       MoveEvent moveEvent = message.getValue(MoveEvent.class);
       updateGameRoomHeartBeat(moveEvent.getPlayer());
       broadcastMove(moveEvent);
       break;
     case GAME_KEEP_ALIVE:
       updateGameRoomHeartBeat(message.getValue(Player.class));
       broadcastPause(message.getValue(Player.class));
       break;
     case LOBBY_KEEP_ALIVE:
       updateLobbyHeartBeat(message.getValue(Player.class));
       break;
     case INVITATION:
       break;
     default:
       break;
     }
   }
 
   private void broadcastPause(Player player) {
     MessageBuilder.createMessage("Game" + player.getGameId()).command(Command.GAME_KEEP_ALIVE).withValue(player)
             .noErrorHandling().sendNowWith(messageBus);
     System.out.println("pause update broadcasted for player " + player.getName());
   }
 
   private void updateGameRoomHeartBeat(Player player) {
     gameHeartBeats.put(player, System.currentTimeMillis());
     System.out.println("Server: " + player.getName() + " sent heartbeat");
   }
 
   private void updateLobbyHeartBeat(Player player) {
     lobbyHeartBeats.put(player.getId(), System.currentTimeMillis());
   }
 
   private void broadcastMove(MoveEvent moveEvent) {
     MessageBuilder.createMessage("Game" + moveEvent.getGameId()).command(Command.MOVE_UPDATE).withValue(moveEvent)
             .noErrorHandling().sendNowWith(messageBus);
   }
 
   private void updateScoreRemote(ScoreEvent event) {
     MessageBuilder.createMessage("Game" + event.getScoreTracker().getGameId()).command(Command.UPDATE_SCORE)
             .withValue(event).noErrorHandling().sendNowWith(dispatcher);
   }
 
   private void updateScoreLocal(ScoreTracker value) {
     GameRoom room = games.get(value.getGameId());
     room.updateScoreTracker(value);
   }
 
   private void removePlayerFromGame(Player player, int gameId) {
    if (games.containsKey(gameId)) {
      games.get(gameId).removePlayer(player.getId());
      if (games.get(gameId).isEmpty()) {
        games.remove(gameId);
        sendLobbyList();
      }
     }
     gameHeartBeats.remove(player);
     player.setGameId(0);
    if (games.containsKey(gameId)) {
      MessageBuilder.createMessage("Game" + gameId).command(Command.LEAVE_GAME).withValue(player).noErrorHandling()
              .sendNowWith(messageBus);
      System.out.println("Server: " + player.getName() + " kicked for being idle");
    }
   }
 }
