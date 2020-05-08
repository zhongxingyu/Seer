 package com.comphenix.xp;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerLoginEvent;
 
 public class ExperienceInformer implements Listener {
 
 	// Whether or not to display warning messages
	private final String permissionInfo = "experiencemod.info";
 
 	private List<String> warningMessages = new ArrayList<String>();
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onBlockBreakEvent(PlayerLoginEvent event) {
 		
 		Player player = event.getPlayer();
 		
 		// Automatically display warning messages
		if (player != null) {
 			displayWarnings(player);
 		}
 	}
 	
 	public boolean displayWarnings(CommandSender sender) {
 		
 		// Player or console
 		if (sender.hasPermission(permissionInfo)) {
 			// Print warning messages
 			for (String message : warningMessages) {
 				sender.sendMessage(ChatColor.RED + "[ExperienceMod] Warning: " + message);
 			}
 			
 			return true;
 		}
 		
 		// No permission
 		return false;
 	}
 	
 	public void addWarningMessage(String message) {
 		warningMessages.add(message);
 	}
 	
 	public void clearMessages() {
 		warningMessages.clear();
 	}
 }
