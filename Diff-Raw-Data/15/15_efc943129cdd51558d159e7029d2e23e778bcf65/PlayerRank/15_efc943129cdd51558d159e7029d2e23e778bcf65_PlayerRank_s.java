 package com.thepastimers.Rank;
 
 import com.thepastimers.Database.Database;
 import com.thepastimers.Database.Table;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Logger;
 
 /**
  * Created with IntelliJ IDEA.
  * User: derp
  * Date: 10/1/12
  * Time: 8:54 PM
  * To change this template use File | Settings | File Templates.
  */
 public class PlayerRank extends Table {
     public static String table = "rank";
 
     int id;
     private static Map<Integer,PlayerRank> dataMap;
 
     public PlayerRank() {
         id = -1;
     }
 
     String player;
     String rank;
 
     public int getId() {
         return id;
     }
 
     public void setId(int id) {
         this.id = id;
     }
 
     public String getRank() {
         return rank;
     }
 
     public void setRank(String rank) {
         this.rank = rank;
     }
 
     public String getPlayer() {
         return player;
     }
 
     public void setPlayer(String player) {
         this.player = player;
     }
 
     public static List<PlayerRank> parseResult(ResultSet result) throws SQLException {
         List<PlayerRank> ret = new ArrayList<PlayerRank>();
 
         if (result == null) {
             return ret;
         }
 
         while (result.next()) {
             PlayerRank r = new PlayerRank();
             r.setId(result.getInt("id"));
             r.setPlayer(result.getString("player"));
             r.setRank(result.getString("rank"));
 
             ret.add(r);
         }
 
         return ret;
     }
 
     public boolean delete(Database d) {
         if (id == -1) {
             return true;
         }
         if (d == null) {
             return false;
         }
         boolean result = d.query("DELETE FROM " + table + " WHERE id = " + id);
         if (result) {
             dataMap.remove(id);
         }
         return result;
     }
 
     public boolean save(Database d) {
         if (d == null) {
             return false;
         }
         if (id == -1) {
             String columns = "(player,rank)";
             String values = "('" + d.makeSafe(player) + "','" + d.makeSafe(rank)  + "')";
             boolean result = d.query("INSERT INTO " + table + columns + " VALUES" + values);
 
             ResultSet keys = d.getGeneratedKeys();
 
             if (keys != null && result) {
                 try {
                     if (keys.next()) {
                         setId(keys.getInt(1));
                         dataMap.put(getId(),this);
                     }
                 } catch (SQLException e) {
                     // fallback method
                     refreshCache(d,null);
                 }
             }
             return result;
         } else {
             StringBuilder query = new StringBuilder();
             query.append("UPDATE " + table + " SET ");
 
             query.append("player = '" + d.makeSafe(player) + "'" + ", ");
             query.append("rank = '" + d.makeSafe(rank) + "'");
 
             query.append("WHERE id = " + id);
             return d.query(query.toString());
         }
     }
 
     public static String getTableInfo() {
         StringBuilder builder = new StringBuilder(table);
         builder.append(" int id, string player, string rank");
 
         return builder.toString();
     }
 
     public static void refreshCache(Database d, Logger l) {
         if (d == null) {
             return;
         }
         List<PlayerRank> PlayerRankList = (List<PlayerRank>)d.select(PlayerRank.class,"");
 
         dataMap = new HashMap<Integer, PlayerRank>();
 
         if (PlayerRankList == null) {
             return;
         }
 
         for (PlayerRank pd : PlayerRankList) {
             dataMap.put(pd.getId(),pd);
         }
 
         if (l != null) {
             l.info("Cache refresh complete, have " + dataMap.keySet().size() + " entries.");
         }
     }
 
     public static PlayerRank getRankForPlayer(String player) {
         for (Integer i : dataMap.keySet()) {
             PlayerRank r = dataMap.get(i);
             if (r.getPlayer().equalsIgnoreCase(player)) {
                 return r;
             }
         }
 
         return null;
     }
 }
