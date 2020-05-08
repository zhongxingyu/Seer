 package models;
 
 import java.lang.reflect.Type;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 
 import models.Player.Serializer;
 
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 import com.google.gson.JsonPrimitive;
 import com.google.gson.JsonSerializationContext;
 import com.google.gson.JsonSerializer;
 
 import exception.GameException;
 
 import play.libs.F.ArchivedEventStream;
 import play.libs.F.IndexedEvent;
 import play.libs.F.Promise;
 
 import util.IdGenerator;
 import util.RichUtil;
 
 /**
  * Game model.
  * 
  * @author GuoLin
  *
  */
 public class Game {
 
     private static final Map<Integer, Game> STORE = new HashMap<Integer, Game>();
 
     private Random seed = new Random(System.currentTimeMillis());
 
     private LinkedList<Role> availableRoles;
 
     public Integer id;
 
     public String name;
 
     public List<Player> players;
     
     public Player currentPlayer;
     
     public GameMap gameMap;
     
     public int round;
     
     public final ArchivedEventStream<Event> events = new ArchivedEventStream<Event>(20);
     
     public long lastReceived;
 
     public Promise<List<IndexedEvent<Event>>> nextEvents(long lastReceived) {
         return events.nextEvents(lastReceived);
     }
     
     public void refreshLastReceived() {
         List<IndexedEvent> historyEvents = events.availableEvents(lastReceived);
         for (IndexedEvent indexedEvent : historyEvents) {
             if (indexedEvent.id > lastReceived) {
                 lastReceived = indexedEvent.id;
             }
         }
     }
     
     public Game(Room room) {
         this.id = room.id;
         this.name = room.name;
         this.gameMap = MapGenerator.generateMap();
         this.round = 0;
         this.lastReceived = 0;
 
         if (players == null) {
             players = new ArrayList<Player>();
         }
         for (Player player: room.players) {
             players.add(player);
             player.randomStart(gameMap);
             player.game = this;
         }
         this.initAvailableRoles();
         for (Player player: players) {
             randomRole(player);
         }
         this.events.publish(new StartEvent(this));
         if (currentPlayer == null) {
             this.nextPlayer();
         }
     }
     
     private void initAvailableRoles() {
         if (availableRoles == null) {
             availableRoles = new LinkedList<Role>();
         }
         for (Role role : Role.all()) {
             boolean taken = false;
             for (Player player : players) {
                 if (role.equals(player.role)) {
                     taken = true;
                     break;
                 }
             }
             if (taken == false) {
                 availableRoles.add(role);
             }
         }
     }
     
     private void randomRole(Player player) {
         if (player.role.isRandom()) {
             if (availableRoles.size() == 0) {
                 throw new GameException("Not enough role available");
             }
             int random = seed.nextInt(availableRoles.size());
             player.role = availableRoles.get(random);
             availableRoles.remove(random);
         }
     }
 
     public Game save() {
         STORE.put(id, this);
         return this;
     }
 
     public static Game get(Integer id) {
         return STORE.get(id);
     }
     
     public boolean validPlayer(String connected) {
         return currentPlayer != null && connected.equals(currentPlayer.name);
     }
     
     /**
      * Record the last active time of the given player.
      * A {@link GameException} would be thrown if no player match the given id.
      * @param playerId The given player id.
      */
     public void recordLastAvtive(Integer playerId) {
         for (Player player : players) {
             if (playerId.equals(player.id)) {
                 player.recordLastActive();
                 return;
             }
         }
         throw new GameException("No such player " + playerId);
     }
     
     /**
      * Switch to the next player.
      * This method would be applied at the start of the game.
      * Or when the turn of one player is ended.
      * A {@link NextPlayerEvent} would be generated in the process.
      */
     public void nextPlayer() {
         if (currentPlayer == null) {
             currentPlayer = players.get(0);
             this.nextRound();
         }
         else {
             int currentIndex = players.indexOf(currentPlayer);
             int nextIndex = (currentIndex + 1) % players.size();
             currentPlayer = players.get(nextIndex);
             if (nextIndex == 0) {
                 this.nextRound();
             }
         }
         if (currentPlayer.survive == false) {
             nextPlayer();
         }
         this.events.publish(new NextPlayerEvent(currentPlayer.name));
     }
     
     /**
      * Change the the given cell to the given owner.
      * If the <code>ownerName</code> is null, it indicates to clear the owner of the given cell.
      * @param ownerName The owner to change into.
      * @param cell The given cell list.
      */
     public void changeCellOwner(String ownerName, Cell... cells) {
         List<Integer> cellIds = new ArrayList<Integer>();
         for (Cell cell : cells) {
             cellIds.add(cell.id);
         }
         this.events.publish(new OwnerChangeEvent(ownerName, cellIds));
     }
     
     private void nextRound() {
         if (!this.gameEnd()) {
             round++;
             NextRoundEvent nextRoundEvent = new NextRoundEvent(round);
             this.events.publish(nextRoundEvent);
         }
     }
     
     private boolean gameEnd() {
         int aliveCount = 0;
         for (Player player : players) {
             if (player.survive == true) {
                 aliveCount++;
             }
         }
         if (aliveCount == 1) {
             this.events.publish(new EndGameEvent());
             return true;
         }
         return false;
     }
     
     public enum Action {
         
         ROLL {
             @Override
             public void doAction(Game currentGame) {
                 int value = currentGame.currentPlayer.roll();
                 currentGame.currentPlayer.move(value);
             }
         },
         
         PASS {
             @Override
             public void doAction(Game currentGame) {
                 currentGame.currentPlayer.pass();
             }
             
         },
         
         BUY {
             @Override
             public void doAction(Game currentGame) {
                 currentGame.currentPlayer.buyCell();
                 currentGame.nextPlayer();
             }
         },
         
         END {
             @Override
             public void doAction(Game currentGame) {
                 currentGame.nextPlayer();
             }
             
         };
         
         public abstract void doAction(Game currentGame);
     }
 
     public static class StartEvent extends Event {
 
         public final Game game;
         
         public StartEvent(Game game) {
             this.game = game;
         }
     }
     
     public static class OwnerChangeEvent extends Event {
         
         public final String ownerName;
         
         public final List<Integer> cellIds;
         
         public OwnerChangeEvent(String ownerName, List<Integer> cellIds) {
             this.ownerName = ownerName;
             this.cellIds = cellIds;
         }
     }
     
     public static class NextPlayerEvent extends Event {
         
         public final String playerName;
         
         public NextPlayerEvent(String playerName) {
             this.playerName = playerName;
         }
         
     }
     
     public static class NextRoundEvent extends Event {
         
         public final int round;
         
         public NextRoundEvent(int round) {
             this.round = round;
         }
     }
     
     public static class EndGameEvent extends Event {
         
     }
     
     public static class Serializer implements JsonSerializer<Game> {
         @Override
         public JsonElement serialize(Game src, Type type,
                 JsonSerializationContext ctx) {
             JsonObject gameObject = new JsonObject();
             gameObject.add("id", new JsonPrimitive(src.id));
             gameObject.add("name", new JsonPrimitive(src.name));
             gameObject.add("players", ctx.serialize(src.players));
             gameObject.add("currentPlayer", ctx.serialize(src.currentPlayer));
             gameObject.add("gameMap", ctx.serialize(src.gameMap));
             gameObject.add("round", new JsonPrimitive(src.round));
             return gameObject;
         }
     }
 
 }
