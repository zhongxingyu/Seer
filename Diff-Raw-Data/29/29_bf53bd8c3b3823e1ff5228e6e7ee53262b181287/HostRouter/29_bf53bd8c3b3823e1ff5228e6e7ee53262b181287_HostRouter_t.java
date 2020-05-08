 package com.steamedpears.comp3004.routing;
 
 import com.google.gson.Gson;
 import com.google.gson.JsonArray;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 import com.google.gson.JsonParser;
 import com.google.gson.JsonPrimitive;
 import com.steamedpears.comp3004.SevenWonders;
 import com.steamedpears.comp3004.models.Card;
 import com.steamedpears.comp3004.models.Player;
 import com.steamedpears.comp3004.models.PlayerCommand;
 import com.steamedpears.comp3004.models.SevenWondersGame;
 import com.steamedpears.comp3004.models.Wonder;
 import com.steamedpears.comp3004.models.players.HumanPlayer;
 import com.steamedpears.comp3004.views.NewGameDialog;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 class HostRouter extends Router {
     private ServerSocket serverSocket;
     private List<Client> clients;
     private JsonArray cardJSON;
     private JsonArray wonderJSON;
     private Map<String, Wonder> wonders;
     private Map<Player, PlayerCommand> registeredMoves;
     private int maxPlayers;
 
     public HostRouter(int port, int maxPlayers) {
         try {
             this.serverSocket = new ServerSocket(port);
         } catch (IOException e) {
             e.printStackTrace();
             System.exit(-1);
         }
         this.clients = new ArrayList<Client>();
         this.maxPlayers = maxPlayers;
         localPlayerId = 0;
     }
 
     @Override
     public void registerMove(Player player, PlayerCommand command) {
         //TODO: make this threadsafe
         registeredMoves.put(player, command);
     }
 
     @Override
     public void beginGame() {
         loadModelConfigs();
 
         SevenWondersGame game = getLocalGame();
         game.setDeck(game.generateRandomDeck(maxPlayers));
 
         constructPlayers();
 
         broadcastInitialConfig();
 
         game.start();
     }
 
     private void loadModelConfigs(){
         try {
             JsonParser parser = new JsonParser();
             this.cardJSON = parser
                     .parse(new FileReader(SevenWonders.PATH_CARDS))
                     .getAsJsonObject()
                     .get(Router.PROP_ROUTE_CARDS)
                     .getAsJsonArray();
             this.wonderJSON = parser
                     .parse(new FileReader(SevenWonders.PATH_WONDERS))
                     .getAsJsonObject()
                     .get(Router.PROP_ROUTE_WONDERS)
                     .getAsJsonArray();
         } catch (FileNotFoundException e) {
             e.printStackTrace();
             System.exit(-1);
         }
 
         getLocalGame().setCards(this.cardJSON);
         this.wonders = Wonder.parseWonders(this.wonderJSON);
     }
 
     private void constructPlayers(){
         List<Wonder> wonderList = new ArrayList<Wonder>();
         wonderList.addAll(wonders.values());
 
         Collections.shuffle(wonderList);
 
         SevenWondersGame game = getLocalGame();
         for(int i=0; i<maxPlayers; ++i){
             Player player;
             Wonder wonder = wonderList.get(i);
             wonder.randomizeSide();
             if(i<clients.size()){
                 player = new HumanPlayer(wonder, game);
             }else{
                 player = Player.getAIPlayer(wonder, game);
             }
             if(i==0 || i>=clients.size()){
                 game.addLocalPlayer(player);
             }
         }

        List<Player> players = game.getPlayers();
        Player oldPlayer = players.get(players.size()-1);
        Player newPlayer;
        for(int i=0; i<players.size(); ++i){
            newPlayer = players.get(i);
            newPlayer.setPlayerLeft(oldPlayer);
            oldPlayer.setPlayerRight(newPlayer);
        }
     }
 
     @Override
     public void run(){
         if(isPlaying()){
             waitForClients();
         }else{
             listenForCommands();
         }
     }
 
     private void waitForClients(){
         while(true){
             try{
                 clients.add(new Client(serverSocket.accept()));
             }catch(IOException e){
                 e.printStackTrace();
                 System.exit(-1);
             }
         }
     }
 
     private void listenForCommands(){
         for(Client client: clients){
             //TODO: ..?
         }
     }
 
     private void broadcastInitialConfig(){
         Gson gson = new Gson();
         JsonObject result = new JsonObject();
         result.add(PROP_ROUTE_CARDS, this.cardJSON);
         result.add(PROP_ROUTE_WONDERS, this.wonderJSON);
 
         JsonObject players = new JsonObject();
         for(Player player: getLocalGame().getPlayers()){
             players.addProperty("" + player.getPlayerId(), player.getWonder().getId());
         }
         result.add(PROP_ROUTE_PLAYERS, players);
 
         JsonArray deck = new JsonArray();
         for(List<Card> ageDeck: getLocalGame().getDeck()){
             JsonArray ageDeckJSON = new JsonArray();
             for(Card card: ageDeck){
                 ageDeckJSON.add(new JsonPrimitive(card.getId()));
             }
         }
         result.add(PROP_ROUTE_DECK, deck);
 
         broadcast(result.toString());
     }
 
     private void broadcastTakeTurn(){
         broadcast(COMMAND_ROUTE_TAKE_TURN);
     }
 
     private void broadcastPlayerCommands(){
         broadcast(playerCommandsToJson(registeredMoves).toString());
     }
 
     private void broadcast(String message){
         for(Client client: clients){
             client.sendMessage(message);
         }
     }
 
     private class Client extends Thread{
         private BufferedReader in;
         private PrintWriter out;
         private Socket client;
 
         public Client(Socket client){
             try {
                 in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                 out = new PrintWriter(client.getOutputStream(), true);
             } catch (IOException e) {
                 e.printStackTrace();
                 System.exit(-1);
             }
         }
 
         @Override
         public void run(){
             //TODO: ...?
         }
 
         public void sendMessage(String message){
             out.print(message);
             out.flush();
         }
     }
 
 
 }
