 package com.wolvencraft.prison.events;
 
 //import org.bukkit.ChatColor;
 import java.util.List;
 
import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerJoinEvent;
 
 import com.wolvencraft.prison.PrisonSuite;
 import com.wolvencraft.prison.updater.Updater;
 import com.wolvencraft.prison.util.Message;
 
 
 public class LoginListener implements Listener
 {
 	
 	public LoginListener(PrisonSuite plugin) {
         plugin.getServer().getPluginManager().registerEvents(this, plugin);
     }
 	
 	@EventHandler(priority = EventPriority.HIGH)
 	public void onPlayerInteract(PlayerJoinEvent event) {
 		if(!PrisonSuite.getSettings().UPDATE) return;
 		
 		Player player = event.getPlayer();
 		if(player.hasPermission("prison.admin")) {
 			if(!Updater.checkVersion()) {
				String updateMessage = "PrisonSuite is not up to date (" + ChatColor.GREEN;
 				List<String> needUpdating = Updater.getOldPlugins();
				updateMessage += needUpdating.get(0);
 				if(needUpdating.size() > 1) {
 					for(int i = 1; i < needUpdating.size(); i++) {
						updateMessage = updateMessage + ChatColor.WHITE + ", " + ChatColor.GREEN + needUpdating.get(i);
 					}
 				}
				updateMessage = updateMessage + ChatColor.WHITE + ")";
				Message.sendError(player, updateMessage);
 			}
 		}
 		return;
 	}
 }
