 package com.minecarts.bouncer.helper;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import com.minecarts.bouncer.Bouncer;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 
 public class DBHelper {
     private Bouncer plugin;
     public DBHelper(Bouncer plugin){
         this.plugin = plugin;
     }
     public String isIdentiferBanned(String identifier){
        String result = null;
         try{
             Connection conn = this.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM `player_bans` WHERE `identifier` = ? LIMIT 1");            
             if(ps == null){
                 plugin.log.warning("GetBanList query failed");
                 conn.close();
             }
             ps.setString(1, identifier);
             ResultSet set = ps.executeQuery();
             if(set.next()) {
                result = set.getString("reason");
             }
             set.close();
             ps.close();
             conn.close();
         } catch (SQLException e) {
             e.printStackTrace();
         }
        return result;
     }
     
     public String getJoinMessage(String playerName){
         return this.getKey("Bouncer_JoinMessage", playerName);
     }
     public String getQuitMessage(String playerName){
         return this.getKey("Bouncer_QuitMessage", playerName);
     }
     
     public String getKey(String key, String playerName){
        String result = null;
         try{
             Connection conn = this.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM `player_meta` WHERE `player` = ? AND `key`=? LIMIT 1");
             if(ps == null){
                 plugin.log.warning("getJoinString query failed");
                 conn.close();
             }
             ps.setString(1, playerName);
             ps.setString(2, key);
             ResultSet set = ps.executeQuery();
             if(set.next()) {
                 result = set.getString("value");
             }
             set.close();
             ps.close();
             conn.close();
         } catch (SQLException e) {
             e.printStackTrace();
         }
        return result;
     }
 
     
     private Connection getConnection(){
         return plugin.dbc.getConnection("minecarts");
     }
 }
