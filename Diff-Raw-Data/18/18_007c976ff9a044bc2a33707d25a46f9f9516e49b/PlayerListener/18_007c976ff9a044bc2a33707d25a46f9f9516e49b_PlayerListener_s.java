 package net.timstans.hidemyplugins;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 
 public class PlayerListener implements Listener {
 	
 	@EventHandler
 	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
 		Player p = event.getPlayer();
		if ((event.getMessage().toLowerCase().startsWith("/plugins")) || 
			      (event.getMessage().toLowerCase().startsWith("/pl ")) || 
			      (event.getMessage().toLowerCase().equals("/pl")))
		{
			if (!event.getPlayer().hasPermission("hmp.plugins")){
				event.setCancelled(true);
				p.sendMessage("4Access Denied!");
			}else{
				
			}
			
 		}
		
 	}
 
 }
