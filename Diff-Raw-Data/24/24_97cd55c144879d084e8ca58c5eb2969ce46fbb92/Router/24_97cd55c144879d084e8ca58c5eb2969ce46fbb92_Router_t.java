 package com.steamedpears.comp3004.routing;
 
 import com.google.gson.Gson;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 import com.steamedpears.comp3004.SevenWonders;
 import com.steamedpears.comp3004.models.Changeable;
 import com.steamedpears.comp3004.models.Player;
 import com.steamedpears.comp3004.models.PlayerCommand;
 import com.steamedpears.comp3004.models.SevenWondersGame;
import org.apache.log4j.Logger;
import org.apache.log4j.helpers.LogLog;
 
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 public abstract class Router extends Changeable implements ChangeListener {
 
     public static Router getHostRouter(int port, int totalPlayers){
        log.debug("Creating Host Router");
         totalPlayers = Math.max(Math.min(totalPlayers, SevenWonders.MAX_PLAYERS), SevenWonders.MIN_PLAYERS);
         return new HostRouter(port, totalPlayers);
     }
 
     public static Router getClientRouter(String ipAddress, int port){
        log.debug("Creating Client Router");
         return new ClientRouter(ipAddress, port);
     }
 
     public static final int HOST_PORT = 1567;
 
     public static String PROP_ROUTE_CARDS = "cards";
     public static String PROP_ROUTE_WONDERS = "wonders";
     public static String PROP_ROUTE_DECK = "deck";
     public static String PROP_ROUTE_PLAYERS = "players";
     public static String PROP_ROUTE_YOU_ARE = "ur";
 
     public static String COMMAND_ROUTE_TAKE_TURN = "takeUrTurn";
     public static String COMMAND_ROUTE_OK = "k";
     public static String COMMAND_ROUTE_CONNECT = "ohai";
 
    private static Logger log = Logger.getLogger(Router.class);

 
     //localGame#applyCommands should be called when every player has decided on a command
     private SevenWondersGame localGame;
     private boolean playing;
     protected int localPlayerId;
     protected List<ChangeListener> changeListeners;
 
     protected Router() {
         this.localGame = new SevenWondersGame(this);
         localGame.addChangeListener(this);
         this.playing = false;
     }
 
     public abstract void registerMove(Player player, PlayerCommand command);
 
     public abstract void beginGame();
 
     public abstract int getTotalHumanPlayers();
 
     protected void setPlaying(boolean playing){
         this.playing = playing;
     }
 
     public SevenWondersGame getLocalGame(){
         return localGame;
     }
 
     public boolean isPlaying(){
         return playing;
     }
 
     public int getLocalPlayerId(){
         return localPlayerId;
     }
 
     protected JsonObject playerCommandsToJson(Map<Player, PlayerCommand> commands){
         Gson gson = new Gson();
         JsonObject result = new JsonObject();
         for(Player p: commands.keySet()){
             result.add(""+p.getPlayerId(), gson.toJsonTree(commands.get(p)));
         }
 
         return result;
     }
 
     protected Map<Player, PlayerCommand> jsonToPlayerCommands(JsonObject obj){
         Gson gson = new Gson();
 
         Map<Player, PlayerCommand> result = new HashMap<Player, PlayerCommand>();
         Set<Map.Entry<String, JsonElement>> entries = obj.entrySet();
         SevenWondersGame local = getLocalGame();
         for(Map.Entry<String, JsonElement> entry: entries){
             result.put(local.getPlayerById(Integer.parseInt(entry.getKey())),
                     gson.fromJson(entry.getValue(), PlayerCommand.class));
         }
 
         return result;
     }
 
     public void stateChanged(ChangeEvent event){
         announceChange(event.getSource());
     }
 }
