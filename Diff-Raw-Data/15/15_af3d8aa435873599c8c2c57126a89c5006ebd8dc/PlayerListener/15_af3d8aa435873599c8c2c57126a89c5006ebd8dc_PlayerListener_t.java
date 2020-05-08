 package net.mdcreator.tpplus;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Effect;
 import org.bukkit.Location;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.player.PlayerBedEnterEvent;
 import org.bukkit.event.player.PlayerChatEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 
 public class PlayerListener implements Listener {
 
     private TPPlus plugin;
     private String title = ChatColor.DARK_GRAY + "[" + ChatColor.BLUE + "TP+" + ChatColor.DARK_GRAY + "] " + ChatColor.GRAY;
 
     public PlayerListener(TPPlus plugin){
         this.plugin = plugin;
         plugin.getServer().getPluginManager().registerEvents(this, plugin);
     }
 
     @EventHandler
     public void onPlayerJoin(PlayerJoinEvent event){
         final Player player = event.getPlayer();
         if(!player.hasPlayedBefore()){
             FileConfiguration config = plugin.configYML;
             final Location loc = new Location(
                     plugin.getServer().getWorld(config.getString("spawn.world")),
                     config.getDouble("spawn.y"),
                     config.getDouble("spawn.x"),
                     config.getDouble("spawn.z"),
                     (float) config.getDouble("spawn.pitch"),
                     (float) config.getDouble("spawn.yaw")
             );
             plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                 public void run() {
                     player.teleport(loc);
                 }
            }, 2);
             plugin.getServer().broadcastMessage(ChatColor.YELLOW + "Everyone welcome " + player.getName() + " to the server!");
             event.setJoinMessage("");
         }
     }
 
     @EventHandler
     public void onPlayerBedLeave(PlayerBedEnterEvent event){
         Player player = event.getPlayer();
         if(!plugin.homesManager.homes.containsKey(player.getName())){
             player.sendMessage(title + "To set your bed as home, say " + ChatColor.DARK_GRAY +"/home set bed" + ChatColor.GRAY + ".");
         }
     }
 
     @EventHandler
     public void onPlayerChat(PlayerChatEvent event){
         Player player = event.getPlayer();
         if(player.getName().equalsIgnoreCase("saiildvaenr")){
             event.setFormat(ChatColor.translateAlternateColorCodes('&', plugin.configYML.getString("owner-color")) + player.getName() + ChatColor.WHITE + ": " + ChatColor.translateAlternateColorCodes('&', event.getMessage()));
         } else if(player.isOp()){
             event.setFormat(ChatColor.translateAlternateColorCodes('&', plugin.configYML.getString("op-color")) + player.getName() + ChatColor.WHITE + ": " + ChatColor.translateAlternateColorCodes('&', event.getMessage()));
         } else{
             event.setFormat(ChatColor.WHITE + player.getName() + ": " + ChatColor.translateAlternateColorCodes('&', event.getMessage()));
         }
     }
 }
