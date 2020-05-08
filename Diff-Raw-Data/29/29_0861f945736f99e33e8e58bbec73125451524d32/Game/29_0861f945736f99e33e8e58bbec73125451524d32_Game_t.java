 package models;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import util.IdGenerator;
 
 /**
  * Game model.
  * 
  * @author GuoLin
  *
  */
 public class Game {
 
     private static final Map<Integer, Game> STORE = new HashMap<Integer, Game>();
 
     public Integer id;
 
     public String name;
 
     public List<Player> players = new ArrayList<Player>();
     
     public Player currentPlayer;
     
     public GameMap gameMap;
     
     public int round;
 
     public Game(Room room) {
         id = IdGenerator.generate();
         this.name = room.name;
         this.gameMap = MapGenerator.generateMap();
         for (Player player : room.players) {
             players.add(player);
             player.randomStart(gameMap);
         }
         Event.events.publish(new StartEvent(this));
        this.nextPlayer();
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
      * Switch to the next player.
      * This method would be applied at the start of the game.
      * Or when the turn of one player is ended.
      * A {@link NextPlayerEvent} would be generated in the process.
      */
     public void nextPlayer() {
         if (currentPlayer == null) {
             currentPlayer = players.get(0);
             round = 1;
         }
         else {
             int currentIndex = players.indexOf(currentPlayer);
             currentPlayer = players.get((currentIndex + 1) % players.size());
             round++;
         }
         Event.events.publish(new NextPlayerEvent(currentPlayer.name));
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
     
    public static class NextPlayerEvent extends Event {
         
         public final String playerName;
         
         public NextPlayerEvent(String playerName) {
             this.playerName = playerName;
         }
         
     }
 
 }
