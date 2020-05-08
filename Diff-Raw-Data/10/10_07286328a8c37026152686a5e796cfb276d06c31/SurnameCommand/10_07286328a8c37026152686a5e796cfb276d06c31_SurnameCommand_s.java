 package com.gildorym;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 
 public class SurnameCommand implements CommandExecutor {
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
 		if (args.length >= 1) {
 			String surname = "";
 			for (String arg : args) {
 				surname += arg + " ";
 			}
			Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "pex user " + sender.getName() + " suffix \" " + surname + "\"");
 			sender.sendMessage(ChatColor.AQUA + "Your surname has been changed to " + surname + ".");
		} else {
			Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "pex user " + sender.getName() + " suffix \"\"");
			sender.sendMessage(ChatColor.AQUA + "Your surname has been removed.");
 		}
 		return true;
 	}
 
 }
