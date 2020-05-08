 package com.thepastimers.UserMap;
 
 import com.thepastimers.Database.Database;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  * Created by rwijtman on 3/7/14.
  */
 public class UserMap extends JavaPlugin implements Listener {
     Database database;
 
     public static int NO_USER = -1;
 
     @Override
     public void onEnable() {
         getLogger().info("UserMap init");
 
         getServer().getPluginManager().registerEvents(this,this);
 
         database = (Database)getServer().getPluginManager().getPlugin("Database");
         if (database == null) {
             getLogger().warning("Unable to load Database plugin. Some functionality will not be available.");
         }
 
         getLogger().info("Table info: ");
         getLogger().info(UserMapping.getTableInfo());
         UserMapping.refreshCache(database,getLogger());
 
        // now map every user that's currently on the server

        for (Player p : getServer().getOnlinePlayers()) {
            updateUUID(p);
        }

         getLogger().info("UserMap init complete");
     }
 
     @Override
     public void onDisable() {
         getLogger().info("UserMap disabled");
     }
 
    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent event) {
         updateUUID(event.getPlayer());
     }
 
     public UserMapping getUUIDObject(Player player) {
         if (player == null) {
             return null;
         }
         UserMapping um = UserMapping.getMappingForPlayer(player);
         if (um == null) {
             return null;
         }
         return um;
     }
 
     public int getUUID(Player player) {
         if (player == null) {
             return NO_USER;
         }
         UserMapping um = UserMapping.getMappingForPlayer(player);
         if (um == null) {
             return NO_USER;
         }
         return um.getUuid();
     }
 
     public int getUUID(String player) {
         if (player == null) {
             return NO_USER;
         }
 
         UserMapping um = UserMapping.getMappingForPlayer(player);
         if (um == null) {
             return NO_USER;
         }
         return um.getUuid();
     }
 
     public void updateUUID(Player p) {
         if (p == null) return;
         UserMapping um = getUUIDObject(p);
         if (um == null) {
             um = new UserMapping();
         }
         um.setUserName(p.getName());
         um.setUuid(p.getUniqueId().hashCode());
         if (!um.save(database)) {
             getLogger().warning("Warning: cannot update player's user mapping for " + p.getName());
         }
     }
 }
