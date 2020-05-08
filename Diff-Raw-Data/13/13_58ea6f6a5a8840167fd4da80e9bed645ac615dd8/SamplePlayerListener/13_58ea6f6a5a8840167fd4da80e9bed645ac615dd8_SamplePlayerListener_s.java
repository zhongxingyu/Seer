 package com.taamc.PlayerDinger;
 
 import org.bukkit.Sound;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerJoinEvent;
 
 /**
  * Handle events for all Player related events
  * 
  * @author Dinnerbone
  */
 public class SamplePlayerListener implements Listener {
 	private final PlayerDinger plugin;
 
 	public SamplePlayerListener(PlayerDinger instance) {
 		plugin = instance;
 	}
 
 	@EventHandler
 	public void onPlayerJoin(PlayerJoinEvent event) {
 		if (event.getPlayer().hasPermission("playerdinger.ding.cause")) {
 			Player[] plist = plugin.getServer().getOnlinePlayers();
 			for (Player p : plist) {
 				if (p.hasPermission("playerdinger.ding.receive")) {
 					p.playSound(
 							p.getLocation(),
 							SoundUtil.matchSoundType(plugin.config.sound),
							plugin.config.volume,
							plugin.config.pitch);
 				}
 			}
 		}
 	}
 }
