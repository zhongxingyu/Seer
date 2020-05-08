 package com.me.tft_02.tftaddon.listener;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 
 import com.gmail.nossr50.events.experience.McMMOPlayerLevelUpEvent;
 import com.me.tft_02.tftaddon.TfTAddon;
 import com.me.tft_02.tftaddon.util.UserProfiles;
 
 public class McMMOListener implements Listener {
     private TfTAddon plugin;
 
     public McMMOListener(final TfTAddon instance) {
         plugin = instance;
     }
 
     final UserProfiles users = new UserProfiles();
 
     /**
      * Monitor McMMOPlayerLevelUpEvent events.
      * 
      * @param event The event to monitor
      */
     @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
     public void onLevelupEvent(McMMOPlayerLevelUpEvent event) {
         int levelRequired = plugin.getConfig().getInt("Announce_Level_Up.Power_Level");
         double messageDistance = TfTAddon.getInstance().getConfig().getDouble("Announce_Level_Up.Range");
 
         if (levelRequired <= 0) {
             return;
         }
 
         Player player = event.getPlayer();
         int power_level = users.getSkillLevel(player, null);
 
        if (power_level / levelRequired > 1) {
             if (messageDistance > 0) {
                 for (Player players : player.getWorld().getPlayers()) {
                     if (players != player && players.getLocation().distance(player.getLocation()) < messageDistance) {
                         players.sendMessage(ChatColor.GOLD + player.getName() + ChatColor.GRAY + " has just reached power level " + ChatColor.GREEN + power_level);
                     }
                 }
             } else {
                 Bukkit.broadcastMessage(ChatColor.GOLD + player.getName() + ChatColor.GRAY + " has just reached power level " + ChatColor.GREEN + power_level);
             }
         }
     }
 
 }
