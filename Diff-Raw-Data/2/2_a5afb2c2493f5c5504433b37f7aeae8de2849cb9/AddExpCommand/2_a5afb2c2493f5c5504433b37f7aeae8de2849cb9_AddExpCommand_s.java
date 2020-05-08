 package com.gildorym.basicchar;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 
 public class AddExpCommand implements CommandExecutor {
 	
 	private BasicChar plugin;
 	
 	public AddExpCommand(BasicChar plugin) {
 		this.plugin = plugin;
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
 		if (sender.hasPermission("basicchar.command.addexp")) {
 			if (plugin.getServer().getPlayer(args[0]) != null) {
 				if (plugin.experience.get(plugin.getServer().getPlayer(args[0]).getName()) == null) {
 					plugin.experience.put(plugin.getServer().getPlayer(args[0]).getName(), 0);
 				}
				plugin.experience.put(plugin.getServer().getPlayer(args[0]).getName(), plugin.experience.get(args[0]) + Integer.parseInt(args[1]));
 				int expToNextLevel = (int) Math.round((1000 * plugin.levels.get(plugin.getServer().getPlayer(args[0]).getName())) * (0.5 * (plugin.levels.get(plugin.getServer().getPlayer(args[0]).getName()) + 1)));
 				if (plugin.experience.get(plugin.getServer().getPlayer(args[0]).getName()) >= expToNextLevel) {
 					plugin.experience.put(plugin.getServer().getPlayer(args[0]).getName(), expToNextLevel);
 					plugin.levels.put(plugin.getServer().getPlayer(args[0]).getName(), plugin.levels.get(plugin.getServer().getPlayer(args[0]).getName()) + 1);
 				}
 				plugin.getServer().getPlayer(args[0]).setExp(plugin.experience.get(plugin.getServer().getPlayer(args[0]).getName()) / expToNextLevel);
 			}
 		}
 		return true;
 	}
 
 }
