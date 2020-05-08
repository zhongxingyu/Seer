 package me.chaseoes.afterdeathcommands;
 
 import java.util.HashMap;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class AfterDeathCommands extends JavaPlugin implements Listener {
 
     // Variables
     private HashMap<String, Integer> players = new HashMap<String, Integer>();
     private Integer taskID = null;
     private Integer time = null;
 
     @Override
     public void onEnable() {
         // Configuration
         getConfig().options().header("AfterDeathCommands v" + getDescription().getVersion() + " by chaseoes. #");
         getConfig().addDefault("time", 30);
         getConfig().options().copyHeader(true);
         getConfig().options().copyDefaults(true);
         saveConfig();
         time = getConfig().getInt("time");
 
         // Register Listeners
         getServer().getPluginManager().registerEvents(this, this);
     }
 
     @Override
     public void onDisable() {
         reloadConfig();
         saveConfig();
     }
 
     @EventHandler(priority = EventPriority.HIGHEST)
     public void onDeath(final PlayerDeathEvent event) {
         // Add player to hashmap.
         final String player = event.getEntity().getName();
        if (!(players.get(player) != null)) {
             players.put(player, 1);
         }
 
         // New repeating task.
         taskID = getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {
             @Override
             public void run() {
                 Integer i = players.get(player) + 1;
                 players.put(player, i);
                 if (i >= time) {
                     players.remove(player);
                     stopTask();
                     return;
                 }
             }
         }, 0L, 20L);
     }
 
     @EventHandler(priority = EventPriority.HIGHEST)
     public void onCommandPreProcess(PlayerCommandPreprocessEvent event) {
         Player player = event.getPlayer();
         if (!player.hasPermission("afterdeathcommands.bypass")) {
             if (players.containsKey(player.getName())) {
                 event.setCancelled(true);
                 player.sendMessage("cYou died! Please wait 6" + (time - players.get(player.getName())) + " cseconds before using commands.");
             }
         }
     }
 
     public void stopTask() {
         try {
             getServer().getScheduler().cancelTask(taskID);
         } catch (Exception e) {
 
         }
     }
 }
