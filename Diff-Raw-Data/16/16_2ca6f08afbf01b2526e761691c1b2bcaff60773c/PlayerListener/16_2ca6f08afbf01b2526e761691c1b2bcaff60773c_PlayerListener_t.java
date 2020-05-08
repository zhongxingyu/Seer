 package net.dejiplus.dejipluscmd.listener;
 
import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
 
 public class PlayerListener implements Listener {
 
     //debug
 
 
     @EventHandler
     public void onPlayerJoin(final PlayerJoinEvent event) {
 
         final Player player = event.getPlayer();
 
        event.setJoinMessage(ChatColor.AQUA + player.getName() + "がログインしました。");

    }

    @EventHandler
    public void onPlayerQuit(final PlayerQuitEvent event) {

        final Player player = event.getPlayer();

        event.setQuitMessage(ChatColor.AQUA + player.getName() + "がログアウトしました。");
 
     }
 
 }
