 package com.thepastimers.Pvp;
 
 import com.thepastimers.Database.Database;
 import com.thepastimers.Database.Table;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: solum
  * Date: 8/8/13
  * Time: 9:00 PM
  * To change this template use File | Settings | File Templates.
  */
 public class HeadCount extends Table {
     public static String table = "head_count";
 
     int id;
 
     public HeadCount() {
         id = -1;
     }
 
     String player;
     int headCount;
 
     public int getId() {
         return id;
     }
 
     public void setId(int id) {
         this.id = id;
     }
 
     public String getPlayer() {
         return player;
     }
 
     public void setPlayer(String player) {
         this.player = player;
     }
 
     public int getHeadCount() {
         return headCount;
     }
 
     public void setHeadCount(int headCount) {
         this.headCount = headCount;
     }
 
     public static List<HeadCount> parseResult(ResultSet result) throws SQLException {
         List<HeadCount> ret = new ArrayList<HeadCount>();
 
         if (result == null) {
             return ret;
         }
 
         while (result.next()) {
             HeadCount p = new HeadCount();
 
             p.setId(result.getInt("id"));
             p.setPlayer(result.getString("player"));
             p.setHeadCount(result.getInt("head_count"));
             ret.add(p);
         }
 
         return ret;
     }
 
     public boolean save(Database d) {
         if (d == null) {
             return false;
         }
         if (id == -1) {
             String columns = "(player,head_count)";
             String values = "('" + d.makeSafe(player) + "'," + headCount + ")";
             return d.query("INSERT INTO " + table + columns + " VALUES" + values);
         } else {
             StringBuilder query = new StringBuilder();
             query.append("UPDATE " + table + " SET ");
 
             query.append("player = '" + d.makeSafe(player) + "'" + ", ");
            query.append("head_count = " + headCount + ", ");
 
             query.append("WHERE id = " + id);
             return d.query(query.toString());
         }
     }
 
     public static String getTableInfo() {
         StringBuilder ret = new StringBuilder();
         ret.append(table);
         ret.append(": int id, string player, int head_count");
 
         return ret.toString();
     }
 }
