 package com.thepastimers.CastleWars;
 
 import com.thepastimers.Database.Database;
 import com.thepastimers.Database.Table;
 import com.thepastimers.Plot.PlotData;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Logger;
 
 /**
  * Created with IntelliJ IDEA.
  * User: solum
  * Date: 8/17/13
  * Time: 12:23 PM
  * To change this template use File | Settings | File Templates.
  */
 public class CastleSpawner extends Table {
     public static String table = "castle_spawner";
 
     private static Map<Integer,CastleSpawner> dataMap;
     public static Map<Integer,List<CastleSpawner>> castleDataMap;
 
     int castle;
     int x;
     int y;
     int z;
     String prevBlock;
 
     public int getCastle() {
         return castle;
     }
 
     public void setCastle(int castle) {
         this.castle = castle;
     }
 
     public int getX() {
         return x;
     }
 
     public void setX(int x) {
         this.x = x;
     }
 
     public int getY() {
         return y;
     }
 
     public void setY(int y) {
         this.y = y;
     }
 
     public int getZ() {
         return z;
     }
 
     public void setZ(int z) {
         this.z = z;
     }
 
     public String getPrevBlock() {
         return prevBlock;
     }
 
     public void setPrevBlock(String prevBlock) {
         this.prevBlock = prevBlock;
     }
 
     public boolean delete(Database d) {
         if (id == -1) {
             return true;
         }
         if (d == null) {
             return false;
         }
         boolean result = d.query("DELETE FROM " + table + " WHERE ID = " + id);
 
         if (result) {
             dataMap.remove(id);
             castleDataMap.remove(castle);
         }
 
         return result;
     }
 
     public static List<CastleSpawner> parseResult(ResultSet result) throws SQLException {
         List<CastleSpawner> ret = new ArrayList<CastleSpawner>();
 
         if (result == null) {
             return ret;
         }
 
         while (result.next()) {
             CastleSpawner p = new CastleSpawner();
 
             p.setId(result.getInt("id"));
             p.setCastle(result.getInt("castle"));
             p.setX(result.getInt("x"));
             p.setY(result.getInt("y"));
             p.setZ(result.getInt("z"));
             p.setPrevBlock(result.getString("prevBlock"));
 
             ret.add(p);
         }
 
         return ret;
     }
 
     public boolean save(Database d) {
         if (d == null) {
             return false;
         }
         if (id == -1) {
             String columns = "(castle,x,y,z,prev_block)";
             String values = "(" + castle + "," + x + "," + y + "," + z + ",'" + d.makeSafe(prevBlock) + "')";
             boolean result = d.query("INSERT INTO " + table + columns + " VALUES" + values);
 
             ResultSet keys = d.getGeneratedKeys();
 
             if (keys != null && result) {
                 try {
                     if (keys.next()) {
                         setId(keys.getInt(1));
                         dataMap.put(getId(),this);
                         List<CastleSpawner> csList = castleDataMap.get(castle);
                         if (csList == null) {
                             csList = new ArrayList<CastleSpawner>();
                         }
                         csList.add(this);
                         castleDataMap.put(castle, csList);
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
 
             query.append("castle = " + castle + ", ");
             query.append("x = " + x + ", ");
             query.append("y = " + y + ", ");
             query.append("z = " + z + ", ");
             query.append("prev_block = '" + d.makeSafe(prevBlock) + "' ");
 
             query.append("WHERE id = " + id);
             return d.query(query.toString());
         }
     }
 
     public static String getTableInfo() {
         StringBuilder builder = new StringBuilder(table);
 
         builder.append(" int id, int castle, int x, int y, int z, string prev_block");
 
         return builder.toString();
     }
 
     public static void refreshCache(Database d, Logger l) {
         if (d == null) {
             return;
         }
         List<CastleSpawner> CastleSpawnerList = (List<CastleSpawner>)d.select(CastleSpawner.class,"");
 
         dataMap = new HashMap<Integer, CastleSpawner>();
         castleDataMap = new HashMap<Integer, List<CastleSpawner>>();
 
         if (CastleSpawnerList == null) {
             return;
         }
 
         for (CastleSpawner pd : CastleSpawnerList) {
             dataMap.put(pd.getId(),pd);
             List<CastleSpawner> csList = castleDataMap.get(pd.getCastle());
             if (csList == null) {
                 csList = new ArrayList<CastleSpawner>();
             }
             csList.add(pd);
             castleDataMap.put(pd.getCastle(), csList);
         }
 
         if (l != null) {
             l.info("Cache refresh complete, have " + dataMap.keySet().size() + " entries.");
         }
     }
 
     public static List<CastleSpawner> getSpawnersForCastle(CastleData pd) {
         if (castleDataMap.containsKey(pd.getId())) {
             return castleDataMap.get(pd.getId());
         } else {
             return new ArrayList<CastleSpawner>();
         }
     }
 
     public static void clearSpawners(World w, CastleData cd) {
         List<CastleSpawner> csList = getSpawnersForCastle(cd);
 
         for (CastleSpawner s : csList) {
             Block b = w.getBlockAt(s.getX(),s.getY(),s.getZ());
            b.setType(Material.getMaterial(s.getPrevBlock()));
         }
     }
 
     public static void rebuildSpawners(World w, CastleData cd) {
         List<CastleSpawner> csList = getSpawnersForCastle(cd);
 
         for (CastleSpawner s : csList) {
             Block b = w.getBlockAt(s.getX(),s.getY(),s.getZ());
             b.setType(Material.GOLD_BLOCK);
         }
     }
 }
