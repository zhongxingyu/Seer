 package net.timstans.hidemyplugins;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 
 public class PlayerListener implements Listener {
 	
 	@EventHandler
 	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
 		Player p = event.getPlayer();
 		if(event.getMessage().toLowerCase().startsWith("/pl")) {
			if(p.hasPermission("hmp.plugins")){
				
			}else{
				p.sendMessage(ChatColor.DARK_RED + "Access Denied");
				event.setCancelled(true);
			}
			
		}
		if(event.getMessage().toLowerCase().startsWith("/?")) {
			if(p.hasPermission("hmp.plugins")){
				
			}else{
				p.sendMessage(ChatColor.DARK_RED + "Access Denied");
				event.setCancelled(true);
			}
			
 		}
 	}
 
 }
