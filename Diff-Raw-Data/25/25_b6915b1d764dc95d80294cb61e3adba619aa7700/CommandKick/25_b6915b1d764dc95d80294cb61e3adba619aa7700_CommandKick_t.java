 package me.reddy360.theholyflint.command;
 
 import me.reddy360.theholyflint.PluginMain;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class CommandKick implements CommandExecutor {
 	PluginMain pluginMain;
 	public CommandKick(PluginMain pluginMain) {
 		this.pluginMain = pluginMain;
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command command,
 			String label, String[] args) {
 		if(!sender.hasPermission(pluginMain.pluginManager.getPermission("thf.kick"))){
 			sender.sendMessage(ChatColor.DARK_RED + "You cannot KICK ME!");
 			return true;
 		}
 		if(args.length == 0){
 			sender.sendMessage(ChatColor.DARK_AQUA + "You need arguments! Argue!");
 			return true;
 		}
 		if(args.length == 1){
 			Player player = Bukkit.getPlayer(args[0]);
 			if(player == null){
 				sender.sendMessage(ChatColor.DARK_RED + "FAIL! /ban [Player] [Reason]");
 				return true;
 			}
 			if(player.hasPermission(pluginMain.pluginManager.getPermission("thf.kick.exempt"))){
 				sender.sendMessage(ChatColor.DARK_RED + "He can't be kicked! Noooooooo!");
 				return true;
 			}
 			player.kickPlayer("[THF] you have been kicked by " + sender.getName());
 		}else{
 			Player player = Bukkit.getPlayer(args[0]);
 			if(player == null){
 				sender.sendMessage(ChatColor.DARK_RED + "They're not here!");
 				return true;
 			}
 			if(player.hasPermission(pluginMain.pluginManager.getPermission("thf.kick.exempt"))){
 				sender.sendMessage(ChatColor.DARK_RED + "You can't kick me! I'm exempt!");
 				return true;
 			}
			player.kickPlayer(ChatColor.stripColor("[THF] Kicked: " + convertArgs(args)));
 		}
 		return true;
 	}
 
 	private String convertArgs(String[] args) {
 		String returnValue = "";
 		for(int x = 1; x < args.length; x++){
 			returnValue = returnValue + args[x];
 		}
 		return returnValue;
 	}
 
 }
