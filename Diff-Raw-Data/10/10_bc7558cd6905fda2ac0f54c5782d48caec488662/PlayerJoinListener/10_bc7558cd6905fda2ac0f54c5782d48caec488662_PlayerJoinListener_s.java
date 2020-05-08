 package com.mstiles92.bookrules;
 
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerJoinEvent;
 
 public class PlayerJoinListener implements Listener {
 	private final BookRulesPlugin plugin;
 	
 	public PlayerJoinListener(BookRulesPlugin plugin) {
 		this.plugin = plugin;
 	}
 	
 	@EventHandler
 	public void OnPlayerJoin(PlayerJoinEvent e) {
		if (!plugin.getConfig().getBoolean("Give-Books-On-First-Join") || !e.getPlayer().hasPlayedBefore()) {
			plugin.log("Give-Books-On-First-Join: " + plugin.getConfig().getBoolean("Give-Books-On-First-Join"));
			plugin.log("Player.hasPlayedBefore(): " + e.getPlayer().hasPlayedBefore());
 			return;
 		}
 		
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new GiveBookRunnable(plugin, e.getPlayer()), 15);
 	}
 
 }
