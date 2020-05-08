 package com.dechiridas.bitauth.listeners;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 import org.bukkit.ChatColor;
 
 import com.dechiridas.bitauth.BitAuth;
 
 public class BACommandListener implements Listener {
 	private BitAuth plugin;
 	
 	public BACommandListener(BitAuth instance) {
 		this.plugin = instance;
 	}
 
 	@EventHandler
	public void onCommand(PlayerCommandPreprocessEvent event) {
 		// Splits the raw message into an array with space delimiter, e.g. command[0] is /login, command[1] is <password>
 		String[] command = event.getMessage().split("\\s+");
 		
		if (command[0].matches("/(login|chpasswd|pwreset|register")) {
 			String [] args = new String[command.length - 1];
 			System.arraycopy(command, 1, args, 0, command.length - 1);
 			
 			Player player = event.getPlayer();
 			plugin.log.println(ChatColor.stripColor(player.getDisplayName()) + " issued server command: " + command[0]);
 			
			if (command[0].equalsIgnoreCase("/login"))
				plugin.database.tryLoginManual(player, args);
 			else if (command[0].equalsIgnoreCase("/chpasswd"))
 				plugin.database.tryChangePassword(player, args);
 			else if (command[0].equalsIgnoreCase("/pwreset"))
 				plugin.database.tryResetPassword(player, args);
			else if (command[0].equalsIgnoreCase("/register"))
				plugin.database.tryRegister(player, args[0]);
 			
 			event.setCancelled(true);
 		}
 	}
 }
