 package models;
 
 import org.codehaus.jackson.JsonNode;
 import play.libs.F;
 import play.mvc.WebSocket;
 
 import java.util.ArrayList;
 
 import static models.Message.sendChat;
 import static models.Message.sendMessage;
 import static models.Message.usernameAlreadyChoosen;
 
 public class GameManager {
     private static ArrayList<Game> games = new ArrayList<Game>();
 
 
     public static void join(String username, WebSocket.In<JsonNode> in, WebSocket.Out<JsonNode> out) {
         Game game = getGame();
         if (!game.isPlayerOneDefined()) {
             Player playerOne = new Player(username, out, game.getGameId());
             sendMessage(playerOne, "wait", "Waiting for other player to join.....");
             game.setPlayerOne(playerOne);
             openWebSocket(in,playerOne);
 
         } else if (!game.isPlayerTwoDefined()) {
             //TODO verify username playerTwo is not = playerOne
             if(game.getPlayerOne().getUsername().equals(username)){
                 usernameAlreadyChoosen(out);
                 //TODO we have to redirect to the index;
             }
             Player playerTwo = new Player(username, out, game.getGameId());
             game.setPlayerTwo(playerTwo);
             openWebSocket(in,playerTwo);
             game.startGame();
 
         } else {
            createGame();
             join(username, in, out);
         }
     }
 
        private static void openWebSocket(WebSocket.In<JsonNode> in, final Player player) {
 
         in.onMessage(new F.Callback<JsonNode>() {
             public void invoke(JsonNode jsonNode) throws Throwable {
                 Game game = getGameById(player.getGameId());
                 String messageType = jsonNode.get("kind").asText();
                 String messageText = jsonNode.get("messageText").asText();
                 if (!game.isStart()){
                     if (messageType.equals("chat")) {
                         //TODO Habilitamos el chat, pero no pueden tocar el tablero! --> Falta
                         sendMessage(player, "chat", "Still waitting for opponent.\n" + messageText);
                     }
                 }else{
                     if (messageType.equals("chat")) {
                         sendChat(player, game.getOpponent(player), messageText);
                     }
                     if (messageType.equals("play")){
                        game.play(player);
                     }
                 }
             }
         });
 
         in.onClose(new F.Callback0() {
             public void invoke() throws Throwable {
                 Game game = getGameById(player.getGameId());
                 Player opponent = game.getOpponent(player);
                 sendMessage(opponent,"leave", opponent.getUsername() + " has left the game");
                 game.leave();
                 if (game.isFinish()) {
                     games.remove(games.indexOf(game));
                 }
             }
         });
     }
 
     private static Game getGameById(String gameId) {
         for (Game game : games) {
             if (game.getGameId().equals(gameId)) {
                 return game;
             }
         }
         return null;
     }
 
     private static Game createGame(){
         Game game = new Game();
         games.add(game);
         return game;
     }
 
     private static Game getGame(){
         Game game;
         if(games.isEmpty()){
             game = createGame();
         }
         else{
             game = games.get(games.size()-1);
 
         }
         return game;
     }
 
 
 }
