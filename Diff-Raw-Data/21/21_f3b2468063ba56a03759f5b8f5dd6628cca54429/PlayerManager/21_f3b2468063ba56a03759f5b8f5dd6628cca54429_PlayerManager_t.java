 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package me.ibhh.xpShop;
 
 import org.bukkit.entity.Player;
 
 /**
  *
  * @author Simon
  */
 public class PlayerManager {
 
     private xpShop plugin;
 
     public PlayerManager(xpShop pl) {
         plugin = pl;
     }
 
     public int BroadcastMsg(String Permission, String msg) {
         int BroadcastedPlayers = 0;
        if(plugin.toggle){
            return 0;
        }
         try {
             for (Player player : plugin.getServer().getOnlinePlayers()) {
                 if (plugin.PermissionsHandler != null) {
                     if (plugin.PermissionsHandler.checkpermissionssilent(player, Permission)) {
                             player.sendMessage(plugin.config.Prefix + plugin.Prefix + plugin.config.Text + msg);
                             BroadcastedPlayers++;
                     }
                 }
             }
         } catch (Exception e) {
             plugin.Logger("Error on broadcasting message.", "Error");
         }
         return BroadcastedPlayers;
     }
 
     public int BroadcastconsoleMsg(String Permission, String msg) {
         int BroadcastedPlayers = 0;
        if(plugin.toggle){
            return 0;
        }
         try {
             for (Player player : plugin.getServer().getOnlinePlayers()) {
                 if (plugin.PermissionsHandler != null) {
                     if (plugin.PermissionsHandler.checkpermissionssilent(player, Permission)) {
                         if (plugin.DebugMsg.containsKey(player.getName())) {
                             if (plugin.DebugMsg.get(player.getName())) {
                                 player.sendMessage(plugin.config.Prefix + plugin.Prefix + plugin.config.Text + msg);
                                 BroadcastedPlayers++;
                             }
                         }
                     }
                 }
             }
         } catch (Exception e) {
             plugin.Logger("Error on broadcasting console message.", "Error");
         }
         return BroadcastedPlayers;
     }
 }
