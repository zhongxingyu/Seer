 package de.minestar.core.manager;
 
 import java.util.concurrent.ConcurrentHashMap;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 
 import de.minestar.core.units.MinestarPlayer;
 
 public class PlayerManager {
 
     private ConcurrentHashMap<String, MinestarPlayer> onlinePlayerList, offlinePlayerList;
 
     public PlayerManager() {
         this.refreshPlayerList();
     }
 
     // /////////////////////////////////////////////
     //
     // UPDATE METHODS
     //
     // /////////////////////////////////////////////
 
     private void refreshPlayerList() {
         offlinePlayerList = new ConcurrentHashMap<String, MinestarPlayer>();
         onlinePlayerList = new ConcurrentHashMap<String, MinestarPlayer>();
         Player[] players = Bukkit.getOnlinePlayers();
         for (Player player : players) {
             this.getPlayer(player);
         }
     }
 
     public void savePlayers() {
         for (MinestarPlayer player : this.offlinePlayerList.values())
             player.saveData();
 
         for (MinestarPlayer player : this.onlinePlayerList.values())
             player.saveData();
     }
 
     // /////////////////////////////////////////////
     //
     // ADD A PLAYER
     //
     // /////////////////////////////////////////////
 
     private MinestarPlayer addPlayer(String playerName) {
         MinestarPlayer thisPlayer = this.offlinePlayerList.get(playerName);
         if (thisPlayer == null) {
             thisPlayer = new MinestarPlayer(playerName);
         } else {
             this.offlinePlayerList.remove(playerName);
         }
         this.onlinePlayerList.put(playerName, thisPlayer);
         return thisPlayer;
     }
 
     // /////////////////////////////////////////////
     //
     // REMOVE A PLAYER
     //
     // /////////////////////////////////////////////
 
     public MinestarPlayer removePlayer(Player player) {
         return this.removePlayer(player.getName());
     }
 
     public MinestarPlayer removePlayer(String playerName) {
         MinestarPlayer removedPlayer = this.onlinePlayerList.get(playerName);
         if (removedPlayer != null) {
             this.offlinePlayerList.put(playerName, removedPlayer);
             removedPlayer.setOffline();
         }
         return this.onlinePlayerList.remove(playerName);
     }
 
     // /////////////////////////////////////////////
     //
     // GET A PLAYER
     //
     // /////////////////////////////////////////////
 
     public MinestarPlayer getPlayer(Player player) {
         return this.getPlayer(player.getName());
     }
 
     public MinestarPlayer getPlayer(String playerName) {
         MinestarPlayer thisPlayer = this.onlinePlayerList.get(playerName);
         return (thisPlayer != null ? thisPlayer : this.addPlayer(playerName));
     }
 }
