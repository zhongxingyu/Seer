 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package capstone.server.util;
 
 import capstone.player.GameRecord;
 import java.util.List;
 import java.util.Map;
 import java.util.ArrayList;
 import java.util.concurrent.ConcurrentHashMap;
 
 public class GameRecorder {
 
     private static Map<String, List<String>> gameIDs = new ConcurrentHashMap<String, List<String>>();  //PlayerName -> GameID
     private static Map<String, GameRecord> players = new ConcurrentHashMap<String, GameRecord>();      //GameID -> GameRecord
 
     public static void record(String gameID, String player, String coords) {
 
         if (!gameIDs.containsKey(player)) {
             gameIDs.put(player, new ArrayList<String>());
             //gameIDs.get(player).add(gameID);
         }
         if (gameIDs.get(player).indexOf(gameID) == -1) {
             GameRecord game;
             if(players.get(gameID) == null){
                 game = new GameRecord(); 
                 gameIDs.get(player).add(gameID);
             }
             else
             {
                 game = players.get(gameID);
             }
             game.putCoords(coords);
             if (players.get(gameID) != game)
             {
                 players.put(gameID, game);
             }
            gameIDs.get(player).add(gameID);
         } 
         else {
             players.get(gameID).putCoords(coords);
         }
         GameRecord game = players.get(gameID);
         if (game.getPlayer1().equals(""))
         {
             game.setGameID(gameID);
             game.setPlayer1(player);
         }
         else if (!player.equals(game.getPlayer1()) && game.getPlayer2().equals(""))
         {
             game.setPlayer2(player);
         }
     }
     public static List<String> getGames(String player){
         List<String> games = gameIDs.get(player);
         List<String> gamePlayers = new ArrayList();
         for (String e: games)
         {
             GameRecord temp = players.get(e);
             String playerTemp = "{" + "\"gid\": \""+temp.getGameID()+ "\" , \"p1\" :\"" + temp.getPlayer1() + "\" , \"p2\" :\"" + temp.getPlayer2()+"\"}";
             gamePlayers.add(playerTemp);
         }
         return gamePlayers;
     }
     public static List<String> getGameCoords (String gameID){
         GameRecord record = players.get(gameID);
         List temp = record.getCoords();
         return temp;
 }
 }
