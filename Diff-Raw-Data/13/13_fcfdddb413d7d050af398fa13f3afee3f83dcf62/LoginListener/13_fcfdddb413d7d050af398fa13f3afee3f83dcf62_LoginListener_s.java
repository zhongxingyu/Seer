 package com.github.thebiologist13;
 
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerJoinEvent;
 
 public class LoginListener implements Listener {
 	
 	private FileConfiguration config;
 	Logger log = Logger.getLogger("Minecraft");
 	
 	public LoginListener(NeverBreak neverBreak) {
 		this.config = neverBreak.getCustomConfig();
 	}
 	
 	/*
 	 * All this class does is set the player's NeverBreak mode 
 	 * to the automatic one in the config.yml on join.
 	 */
 	
 	@EventHandler
 	public void onJoin(PlayerJoinEvent ev) {
 		if(config.getBoolean("messageOnLogin", true)) {
			Player p = ev.getPlayer();
 			p.sendMessage(ChatColor.GREEN + "Your NeverBreak mode has been set to " + ChatColor.GOLD + config.getBoolean("autoNeverBreak") + ChatColor.GREEN + " !");
			ToggleCommand.mode.put(p, config.getBoolean("autoNeverBreak"));
 		}
 	}
 }
