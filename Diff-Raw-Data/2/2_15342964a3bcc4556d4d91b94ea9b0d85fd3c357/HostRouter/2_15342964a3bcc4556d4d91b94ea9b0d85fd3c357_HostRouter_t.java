 package com.steamedpears.comp3004.routing;
 
 import com.google.gson.JsonArray;
 import com.google.gson.JsonObject;
 import com.google.gson.JsonParser;
 import com.steamedpears.comp3004.SevenWonders;
 import com.steamedpears.comp3004.models.Player;
 import com.steamedpears.comp3004.models.PlayerCommand;
 import com.steamedpears.comp3004.models.SevenWondersGame;
 import com.steamedpears.comp3004.models.Wonder;
 import com.steamedpears.comp3004.models.players.HumanPlayer;
 import org.apache.log4j.Logger;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 
 class HostRouter extends Router {
     private ServerSocket serverSocket;
     private Map<Integer, Client> clients;
     private JsonArray cardJSON;
     private JsonArray wonderJSON;
     private Map<Player, PlayerCommand> registeredMoves;
     private ExecutorService pool;
     private Future lobbyThread;
     private Future listenerThread;
     private int maxPlayers;
 
     private static Logger log = Logger.getLogger(HostRouter.class);
 
     /**
      * Creates a HostRouter with the given port which creates a game with the given number of players
      * @param port the port to listen on
      * @param maxPlayers the number of players the game has
      */
     public HostRouter(int port, int maxPlayers) {
         try {
             this.serverSocket = new ServerSocket(port);
         } catch (IOException e) {
             log.error("Error establishing host socket", e);
             System.exit(-1);
         }
         this.clients = new HashMap<Integer, Client>();
         this.maxPlayers = maxPlayers;
         setLocalPlayerId(0);
 
         registeredMoves = new HashMap<Player, PlayerCommand>();
         pool = Executors.newFixedThreadPool(SevenWonders.MAX_PLAYERS+2);
 
         start();
     }
 
     @Override
     public synchronized void registerMove(Player player, PlayerCommand command) {
         log.debug("Registering command from player: "+player.getPlayerId()+" - "+command);
         registeredMoves.put(player, command);
     }
 
     @Override
     public void beginGame() {
         if(isPlaying()) return;
         log.debug("Beginning game");
         lobbyThread.cancel(true);
 
         loadModelConfigs();
 
         SevenWondersGame game = getLocalGame();
         game.setDeck(game.generateRandomDeck(maxPlayers));
 
         constructPlayers();
 
         this.setPlaying(true);
 
         listenerThread = pool.submit(new Runnable() {
             public void run() {
                 listenForCommands();
             }
         });
 
         broadcastInitialConfig();
 
         startNextTurn();
     }
 
     @Override
     public int getTotalHumanPlayers() {
         return clients.size();
     }
 
     private void loadModelConfigs(){
         log.debug("Loading model config");
 
         JsonParser parser = new JsonParser();
         this.cardJSON = parser
                 .parse(new InputStreamReader(HostRouter.class.getResourceAsStream(SevenWonders.PATH_CARDS)))
                 .getAsJsonObject()
                 .get(Router.PROP_ROUTE_CARDS)
                 .getAsJsonArray();
         this.wonderJSON = parser
                 .parse(new InputStreamReader(HostRouter.class.getResourceAsStream(SevenWonders.PATH_WONDERS)))
                 .getAsJsonObject()
                 .get(Router.PROP_ROUTE_WONDERS)
                 .getAsJsonArray();
 
 
         getLocalGame().setCards(this.cardJSON);
         getLocalGame().setWonders(this.wonderJSON);
     }
 
     private void constructPlayers(){
         log.debug("Constructing players");
         List<Wonder> wonderList = new ArrayList<Wonder>();
         wonderList.addAll(getLocalGame().getWonders().values());
 
         Collections.shuffle(wonderList);
 
         SevenWondersGame game = getLocalGame();
         for(int i=0; i<maxPlayers; ++i){
             Player player;
             Wonder wonder = wonderList.get(i);
             wonder.randomizeSide();
             if(i<=clients.size()){
                 player = new HumanPlayer(wonder, game);
             }else{
                 player = Player.newAIPlayer(wonder, game);
             }
             if(i==0 || i>clients.size()){
                 game.addLocalPlayer(player);
             }else{
                 game.addPlayer(player);
             }
         }
         game.finalizePlayers();
     }
 
     /**
      * Starts the thread associated with this HostRouter.
      */
     public void start(){
         log.debug("Starting Host Router");
         lobbyThread = pool.submit(new Runnable() {
             public void run() {
                 waitForClients();
             }
         });
     }
 
     private void waitForClients(){
         log.debug("Waiting for clients to connect");
         while(!Thread.interrupted()){
             try{
                 while(clients.size()+1==maxPlayers){
                     log.debug("Game full, waiting for game start or loss of clients");
                     garbageCollectClients();
                     try {
                         Thread.sleep(200);
                     } catch (InterruptedException e) {
                         return;
                     }
                 }
 
                 log.debug("Waiting for new client");
                 Socket socket = serverSocket.accept();
                 log.debug("Got new client");
                 garbageCollectClients();
 
                 if(Thread.interrupted()){
                     log.debug("Oh wait, the game has started, killing the client and returning");
                     socket.close();
                     return;
                 }
 
                 Client client = new Client(socket, getNextClientId());
 
                 clients.put(client.clientNumber, client);
                 announceChange(this);
                 log.debug("Client added");
             } catch(IOException e){
                 log.error("Error establishing connection to client");
                 cleanup();
             }
         }
     }
 
     private void garbageCollectClients() {
         log.debug("Garbage collecting clients");
         List<Integer> deadClients = new ArrayList<Integer>();
         for(Client client: clients.values()){
             if(client.isClosed()){
                 deadClients.add(client.clientNumber);
             }
         }
 
         for(Integer clientNumber: deadClients){
             clients.remove(clientNumber);
         }
         log.debug("Clients garbage collected");
     }
 
     private int getNextClientId(){
         log.debug("Getting client id");
         int id = 1;
         while(clients.containsKey(id)){
             id++;
         }
         return id;
     }
 
     private void waitForOkays(){
         log.debug("Waiting for clients to respond 'ok'");
         for(Client client: clients.values()){
             client.getOkay();
         }
         log.debug("All clients responded 'ok'");
     }
 
     private void listenForCommands(){
         log.debug("Listening for commands");
         while(true){
             SevenWondersGame game = getLocalGame();
             if(registeredMoves.size()==game.getPlayers().size()){
                 log.debug("All player commands received");
 
                 boolean gameOver = game.applyCommands(registeredMoves);
 
                 broadcastPlayerCommands();
 
                 waitForOkays();
                 try {
                     log.debug("Waiting for game to finish up");
                     while(!game.isTurnDone()){
                         Thread.sleep(100);
                     }
                 } catch (InterruptedException e) {
                     log.error("Interrupted while waiting for game to finish", e);
                     System.exit(-1);
                 }
 
                 if(!gameOver){
                     startNextTurn();
                 }else{
                     log.info("game is over");
                 }
             }
             try {
                 Thread.sleep(100);
             } catch (InterruptedException e) {
                 log.error("Error for moves to be registered", e);
                 System.exit(-1);
             }
         }
     }
 
     private void startNextTurn(){
         log.debug("Starting next turn");
         getLocalGame().takeTurns();
         broadcastTakeTurn();
         for(Client client: clients.values()){
             pool.execute(client);
         }
     }
 
     private void broadcastInitialConfig(){
         log.debug("broadcasting initial config");
         JsonObject result = new JsonObject();
         result.add(PROP_ROUTE_CARDS, this.cardJSON);
         result.add(PROP_ROUTE_WONDERS, this.wonderJSON);
 
         result.add(PROP_ROUTE_PLAYERS, getLocalGame().getPlayersAsJSON());
 
         result.add(PROP_ROUTE_DECK, getLocalGame().getDeckAsJSON());
 
         broadcast(result.toString());
     }
 
     private void broadcastTakeTurn(){
         log.debug("broadcasting 'take turn'");
         JsonObject obj = new JsonObject();
         obj.addProperty(COMMAND_ROUTE_TAKE_TURN,true);
         broadcast(obj.toString());
     }
 
     private void broadcastPlayerCommands(){
         log.debug("broadcasting players commands");
         broadcast(playerCommandsToJson(registeredMoves).toString());
         registeredMoves = new HashMap<Player, PlayerCommand>();
     }
 
     private void broadcast(String message){
         for(Client client: clients.values()){
             log.info(String.format("Broadcasting to client %d",client.clientNumber));
             client.sendMessage(message);
         }
     }
 
     private class Client implements Runnable{
         private int clientNumber;
         private SocketWrapper socket;
         private JsonParser parser;
 
         private Client(Socket client, int clientNumber){
             log.debug("Establishing connection with client:" +clientNumber);
             this.socket = new SocketWrapper(client);
             this.parser = new JsonParser();
             this.clientNumber = clientNumber;
 
             JsonObject obj = new JsonObject();
             obj.addProperty(PROP_ROUTE_YOU_ARE, clientNumber);
             log.debug(obj.toString());
             sendMessage(obj.toString());
         }
 
         private void getOkay(){
             socket.readLine();
             if(!socket.isValid()) {
                 log.error("Error waiting for okay from client");
                 System.exit(-1);
             }
             log.debug("Got okay: "+clientNumber);
         }
 
         private void sendMessage(String message){
             log.debug("Sending ("+clientNumber+"): "+message);
             socket.println(message);
         }
 
         @Override
         public void run(){
             log.debug("Listening for client commands: "+clientNumber);
             String result = socket.readLine();
             if(!socket.isValid()) {
                 // deal with client disconnect here
                 log.error("Client disconnect, BAIL OUT");
                 System.exit(-1);
             }
             try {
                 JsonObject commandsJSON = parser.parse(result).getAsJsonObject();
                 Map<Player, PlayerCommand> commands = jsonToPlayerCommands(commandsJSON);
                 for(Player player: commands.keySet()){
                     registerMove(player, commands.get(player));
                 }
                 log.debug("Got client commands: "+clientNumber);
             } catch (Exception e) {
                 log.error("error waiting for commands from client", e);
                 System.exit(-1);
             }
         }
 
         private boolean isClosed() {
            return !socket.isValid();
         }
 
         private void cleanup() throws IOException {
             socket.close();
         }
     }
 
     @Override
     public void cleanup() {
         super.cleanup();
         for(Client c : clients.values()) {
             try {
                 c.cleanup();
             } catch(IOException e) {
                 log.warn("IOException while closing client socket");
             }
         }
         try{
             if(serverSocket != null) {
                 serverSocket.close();
             }
             serverSocket = null;
         } catch(IOException e) {
             log.warn("IOException while closing server socket");
         }
     }
 }
