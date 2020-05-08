 package com.steamedpears.comp3004.routing;
 
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 import com.google.gson.JsonParser;
 import com.google.gson.stream.JsonReader;
 import com.steamedpears.comp3004.models.Player;
 import com.steamedpears.comp3004.models.PlayerCommand;
 import com.steamedpears.comp3004.models.SevenWondersGame;
 import org.apache.log4j.Logger;
 
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.Socket;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.concurrent.Executor;
 import java.util.concurrent.Executors;
 
 class ClientRouter extends Router implements Runnable{
     private static Logger log = Logger.getLogger(ClientRouter.class);
 
     private Socket host;
     private JsonReader in;
     private PrintWriter out;
     private JsonParser parser;
     private Executor pool = Executors.newFixedThreadPool(1);
     private int totalHumanPlayers;
 
     public ClientRouter(String ipAddress, int port) {
         try {
             this.host = new Socket(ipAddress, port);
             this.in = new JsonReader(new InputStreamReader(host.getInputStream()));
             this.out = new PrintWriter(host.getOutputStream());
             this.parser = new JsonParser();
             this.totalHumanPlayers = 0;
             start();
         } catch (IOException e) {
             e.printStackTrace();
             System.exit(-1);
         }
     }
 
     @Override
     public void registerMove(Player player, PlayerCommand command) {
         log.debug("registering move");
         Map<Player, PlayerCommand> commands = new HashMap<Player, PlayerCommand>();
         commands.put(player, command);
         sendCommands(commands);
     }
 
     private void sendOkay(){
         log.debug("Telling host I am ready for more");
         out.print(COMMAND_ROUTE_OK);
         out.flush();
     }
 
     private void sendCommands(Map<Player, PlayerCommand> commands){
         out.print(playerCommandsToJson(commands).toString());
         out.flush();
     }
 
     private void waitForTakeTurn(){
         log.debug("Waiting for host to give 'okay' to take turn");
         JsonElement elem = parser.parse(in);
         log.debug("Got 'okay' to take turn; taking turn");
         getLocalGame().takeTurns();
     }
 
     private boolean waitForCommands(){
         log.debug("Waiting for host to send back all the player commands");
         JsonObject obj = parser.parse(in).getAsJsonObject();
         log.debug("Got player commands; applying commands: "+obj.toString());
         Map<Player, PlayerCommand> commands = jsonToPlayerCommands(obj);
         return getLocalGame().applyCommands(commands);
     }
 
     private void waitForInitialConfig(){
         log.debug("Waiting for initial config from host");
         JsonObject obj = parser.parse(in).getAsJsonObject();
 
         log.debug("Got initial config from host, building model: "+obj.toString());
         SevenWondersGame game = getLocalGame();
         game.setCards(obj.getAsJsonArray(PROP_ROUTE_CARDS));
         game.setDeck(obj.getAsJsonArray(PROP_ROUTE_DECK));
         game.setWonders(obj.getAsJsonArray(PROP_ROUTE_WONDERS));
         game.setPlayers(obj.getAsJsonObject(PROP_ROUTE_PLAYERS));
         log.debug("Model built");
     }
 
     private void waitForLocalPlayerId() {
         log.debug("Getting localPlayerId from host");
         JsonObject obj = parser.parse(in).getAsJsonObject();
         localPlayerId = obj.get(PROP_ROUTE_YOU_ARE).getAsInt();
         log.debug("Got localPlayerId: "+localPlayerId);
     }
 
     @Override
     public void beginGame() {
         //This method doesn't seem to have much meaning for the ClientRouter
     }
 
     @Override
     public int getTotalHumanPlayers() {
         //TODO: have the host send this information to the clients
         return totalHumanPlayers;
     }
 
     private void start(){
         pool.execute(this);
     }
 
     @Override
     public void run() {
         log.debug("Starting Client Router");
         waitForLocalPlayerId();
         waitForInitialConfig();
         boolean gameOver = false;
         while(!gameOver){
             waitForTakeTurn();
             gameOver = waitForCommands();
             sendOkay();
         }
         log.debug("Game over; client router stopping");
     }
 }
