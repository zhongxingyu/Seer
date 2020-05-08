 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package capstone.server;
 
 import capstone.game.Coordinates;
 import capstone.game.GameSession;
 import capstone.game.IllegalGameException;
 import capstone.player.GameBot;
 import capstone.player.Player;
 import java.util.Map;
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
     static Map<String, GameSession> gameIDs = new ConcurrentHashMap<String, GameSession>();
     
     //For now, only one bot - DefaultBot
     private static final Player DEFAULT_BOT = new GameBot();
     
     //Add a player to an existing game
     public static void addPlayer(HttpSession session, String gameID){
         try {
             session.getServletContext().log("Player joining game session (ID "+gameID+")");
             Player player = players.get(session);
             gameIDs.get(gameID).Join(player);
         } catch (IllegalGameException ex) {
             newGame(session);
         }
     }
     
     public static void newGame(HttpSession session){
         //For now, only add the default bot
         GameSession game = new GameSession();
         gameIDs.put(game.SessionID, game);
         try {
             game.Join(players.get(session));
             game.Join(DEFAULT_BOT);
         } catch (IllegalGameException ex) {
             Logger.getLogger(GameManager.class.getName()).log(Level.SEVERE, "Error creating new game", ex);
         }
         
     }
     
     public static void newPlayer(HttpSession session, String name){
         session.setAttribute("_user", name);
         if(!players.containsKey(session)){
             players.put(session, new RemotePlayer(name));
         }
     }
     
     public static void disconnect (HttpSession session) {
         Player player = players.remove(session);
         gameSessions.get(session).Leave(player);
     }
     
     public static GameSession getGame(HttpSession session){
         return gameSessions.get(session);
     }
     
    public static RemotePlayer getPlayer(HttpSession session){
         return players.get(session);
     }
     
     public static void makeMove(HttpSession session, int a, int b, int x, int y){
         Coordinates coords = new Coordinates(a, b, x, y);
         GameSession game = gameSessions.get(session);
         RemotePlayer player = players.get(session);
         //only move if we're supposed to
         if(player.isActive()){
             player.setActive(false);
             game.move(player, coords);
         }
     }
     
 }
