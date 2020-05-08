 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package pl.chormon.LoginPoint;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerLoginEvent;
 import org.bukkit.scheduler.BukkitRunnable;
 
 /**
  *
  * @author Chormon
  */
 public class LoginListener implements Listener {
     
     private static LoginPoint plugin;
     
     public LoginListener(LoginPoint plugin) {
         plugin.getServer().getPluginManager().registerEvents(this, plugin);
         LoginListener.plugin = plugin;
     }
     
     @EventHandler
     public void onPlayerLogin(PlayerLoginEvent event) {
         Player player = event.getPlayer();
         Location location = Config.getLoginPoint();    
         new BukkitRunnableImpl(player, location).runTaskLater(plugin, 1L);
     }
 
     private static class BukkitRunnableImpl extends BukkitRunnable {
 
         private final Player player;
         private final Location location;
 
         public BukkitRunnableImpl(Player player, Location location) {
             this.player = player;
             this.location = location;
         }
 
         @Override
         public void run() {
           boolean teleported = player.teleport(location);
          if(!teleported) plugin.getLogger().info(ChatColor.RED + "Failed to teleport player " + player.getName());
         }
     }
     
 }
