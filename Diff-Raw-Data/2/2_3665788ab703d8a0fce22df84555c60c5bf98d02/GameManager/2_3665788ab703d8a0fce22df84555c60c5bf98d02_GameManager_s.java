 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package capstone.server;
 
 import capstone.game.*;
 import capstone.player.GameBot;
 import capstone.player.Player;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.concurrent.ArrayBlockingQueue;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.servlet.http.HttpSession;
 
 /**
  *
  * @author Max
  */
 public class GameManager {
     
     static Map<HttpSession, RemotePlayer> players = new ConcurrentHashMap<HttpSession, RemotePlayer>();
     static Map<HttpSession, GameSession> gameSessions = new ConcurrentHashMap<HttpSession, GameSession>();
     static Map<GameSession, List<HttpSession>> watchers = new ConcurrentHashMap<GameSession, List<HttpSession>>();
     static Map<String, GameSession> gameIDs = new ConcurrentHashMap<String, GameSession>();
     static Map<HttpSession, BlockingQueue<String>> states = new ConcurrentHashMap<HttpSession, BlockingQueue<String>>();
     
     static Map<String, String> openGames = new ConcurrentHashMap<String,String>();
     
     //For now, only one bot - DefaultBot
     private static final Player DEFAULT_BOT = new GameBot();
     
     //Add a player to an existing game
     public static void joinGame(HttpSession session, String gameID){
         try {
             session.getServletContext().log("Player joining game session (ID "+gameID+")");
             RemotePlayer player = players.get(session);
             GameSession game = gameIDs.get(gameID);
             game.Join(player);
             if(!game.isOpen()){
                 openGames.remove(game.SessionID);
             }
         } catch (IllegalGameException ex) {
             newGame(session);
         }
     }
     
     public static void BotJoin(HttpSession session){
         try {
             gameSessions.get(session).Join(DEFAULT_BOT);
         } catch (IllegalGameException ex) {
             Logger.getLogger(GameManager.class.getName()).log(Level.WARNING, "Error adding bot to game", ex);
         }
     }
     
     public static String getOpenGames(){
         StringBuilder builder = new StringBuilder();
         builder=builder.append("{\"games:\"");
         boolean first=true;
         for(Entry<String, String> entry: openGames.entrySet()){
             if(!first){
                 builder=builder.append(",");
             }
             else{
                 first=false;
             }
             builder=builder.append("[\"").append(entry.getKey()).append("\",\"").append(entry.getValue()).append("\"]");
         }
         return builder.append("}").toString();
     }
     
     //Create a new game session
     public static void newGame(HttpSession session){
         GameSession game = new GameSession();
         gameIDs.put(game.SessionID, game);
         try {
             game.Join(players.get(session));
             gameSessions.put(session, game);
            openGames.put(game.SessionID, session.getAttribute("name").toString());
             List<HttpSession> sessions = watchers.get(game);
             if(sessions==null){
                 sessions=new ArrayList<HttpSession>();
                 watchers.put(game, sessions);
             }
             sessions.add(session);
             
             String initialMessage = JSONBuilder.buildJSON(game, players.get(session));
             states.get(session).offer(initialMessage);
         } catch (IllegalGameException ex) {
             Logger.getLogger(GameManager.class.getName()).log(Level.SEVERE, "Error creating new game", ex);
         }
         
     }
     
     public static void newPlayer(HttpSession session, String name){
         session.setAttribute("_user", name);
         if(!players.containsKey(session)){
             players.put(session, new RemotePlayer(name));
         }
         BlockingQueue<String> messageQueue = new ArrayBlockingQueue<String>(10);
         states.put(session, messageQueue);
     }
     
     public static void disconnect (HttpSession session) {
         GameManager.leave(session);
         players.remove(session);
         states.remove(session);
     }
     
     public static void leave(HttpSession session){
         GameSession game = gameSessions.get(session);
         game.Leave(players.get(session));
         states.get(session).clear();
         gameIDs.remove(game.SessionID);
         openGames.remove(game.SessionID);
     }
     
     //Return the oldest state. If a newer state is available, remove that state.
     public static String getGame(HttpSession session){
         BlockingQueue<String> messages = states.get(session);
         try {
             return messages.take();
         } catch (InterruptedException ex) {
             Logger.getLogger(GameManager.class.getName()).log(Level.SEVERE, null, ex);
             return null;
         }
     }
     
     public static RemotePlayer getPlayer(HttpSession session){
         return players.get(session);
     }
     
     public static void makeMove(HttpSession session, int a, int b, int x, int y){
         Coordinates coords = new Coordinates(a, b, x, y);
         GameSession game = gameSessions.get(session);
         RemotePlayer player = players.get(session);
         GameState board = game.getCurrentGame();
         //only move if we're supposed to
         if(player.isActive()){
             if (GameRules.validMove(board, coords)){
                 player.setActive(false);
                 game.move(player, coords);
                 
                 String nextState=JSONBuilder.buildJSON(game, player);
                 for(HttpSession s: watchers.get(game)){
                 states.get(s).offer(nextState);
                 }
             }
 
         }
     }
     
 }
