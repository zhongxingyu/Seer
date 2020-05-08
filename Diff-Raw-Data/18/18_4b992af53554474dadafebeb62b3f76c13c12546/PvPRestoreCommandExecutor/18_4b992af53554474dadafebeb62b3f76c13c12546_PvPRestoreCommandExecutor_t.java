 package com.github.triarry.PvPRestore;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 
 public class PvPRestoreCommandExecutor implements CommandExecutor {
 
 	@SuppressWarnings("unused")
 	private PvPRestore plugin;
 	  
 	public PvPRestoreCommandExecutor(PvPRestore plugin){ 
 		this.plugin = plugin; 
 	}
 
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
 		if (label.equalsIgnoreCase("pvprestore")) {
 			sender.sendMessage(ChatColor.LIGHT_PURPLE + "#####");
			sender.sendMessage(ChatColor.GREEN + "Currently running PvP Restore version 1.4.6");
 			sender.sendMessage(ChatColor.GREEN + "Plugin made by triarry");
 			sender.sendMessage(ChatColor.LIGHT_PURPLE + "#####");
 			return true;
 		}
 		return true;
 	}
 }
