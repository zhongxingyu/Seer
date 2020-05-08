 package de.minestar.core.units;
 
 import java.io.File;
 
 import org.anjocaido.groupmanager.data.User;
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 
 import com.bukkit.gemo.utils.UtilPermissions;
 
 import de.minestar.core.MinestarCore;
 import de.minestar.core.data.Data;
 import de.minestar.core.data.DataType;
 import de.minestar.core.exceptions.BukkitPlayerOfflineException;
 import de.minestar.minestarlibrary.utils.PlayerUtils;
 
 public class MinestarPlayer {
    private boolean online = true;
     private final String playerName;
     private String group;
     private Data data;
 
     public MinestarPlayer(String playerName) {
         this.playerName = playerName;
         if (Bukkit.getPlayer(playerName) != null)
             this.setOnline();
         else
             this.setOffline();
         this.load();
     }
 
     public MinestarPlayer(Player player) {
         this(player.getName());
         this.updateBukkitPlayer();
     }
 
     public void setOnline() {
         this.online = true;
     }
 
     public void setOffline() {
         this.online = false;
     }
 
     public boolean isOnline() {
         return this.online;
     }
 
     public boolean isOffline() {
         return !this.isOnline();
     }
 
     public void load() {
         // UPDATE THE PLAYER
         this.updateGroup();
 
         // INIT DATA
         this.data = new Data(new File(MinestarCore.dataFolder, "playerdata"), playerName, DataType.NBT);
 
         // INITIALIZE NICKNAME & LISTNAME
         this.data.setString("general.nickName", this.playerName);
         this.data.setString("general.listName", this.playerName);
 
         // LOAD DATA FROM FILE
         this.data.load();
     }
 
     public String getPlayerName() {
         return playerName;
     }
 
     public String getNickName() {
         return this.data.getString("general.nickName");
     }
 
     public void setNickName(String nickName) {
         this.data.setString("general.nickName", nickName);
         this.updateBukkitPlayer();
     }
 
     public String getListName() {
         return this.data.getString("general.listName");
     }
 
     public void setListName(String listName) {
         this.data.setString("general.listName", listName);
         this.updateBukkitPlayer();
     }
 
     public String updateGroup() {
         // GET THE PLAYER
         Player player = PlayerUtils.getOnlinePlayer(this.playerName);
         if (player != null) {
             // GET GROUP WITH UTILPERMISSIONS
             this.group = UtilPermissions.getGroupName(player);
         } else {
             this.group = "default";
             // GET GROUP FROM GROUPMANAGER
             if (MinestarCore.groupManager != null) {
                 User user = MinestarCore.groupManager.getWorldsHolder().getWorldData("world").getUser(playerName);
                 this.group = user.getGroupName();
             }
         }
         return getGroup();
     }
 
     public String getGroup() {
         return group;
     }
 
     public boolean isInGroup(String groupName) {
         return this.group.equalsIgnoreCase(groupName);
     }
 
     /**
      * Update the bukkitplayer
      */
     public void updateBukkitPlayer() {
         // ONLY UPDATE, IF ONLINE
         if (this.isOffline())
             return;
 
         // GET THE PLAYER
        Player player = PlayerUtils.getOnlinePlayer(this.playerName);
         if (player == null)
             throw new BukkitPlayerOfflineException(this.playerName);
 
         player.setDisplayName(this.getNickName());
         player.setPlayerListName(this.getListName());
     }
 
     public void saveData() {
         this.data.save();
     }
 }
