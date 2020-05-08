 package com.github.Sabersamus.Basic.Commands;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.github.Sabersamus.Basic.Basic;
 import com.github.Sabersamus.Basic.Settings;
 import com.github.Sabersamus.Basic.Economy.API.Economy;
 
 public class WhoCommand implements CommandExecutor{
 	public static Basic plugin;
 	public WhoCommand(Basic instance){
 		plugin = instance;
 	}
 	@Override
 	public boolean onCommand(CommandSender cs, Command cmd, String aliases, String[] args) {
 		if(cmd.getName().equalsIgnoreCase("who")){
 			Economy api = plugin.getEconomyAPI();
 			Settings config = plugin.getSettings();
 			if(cs instanceof  Player){
 				Player player = (Player)cs;
 					if(args.length == 0){
 						StringBuilder players = new StringBuilder();
 							for(Player online: Bukkit.getOnlinePlayers()){
 								if(!player.canSee(online)){
 									continue;
 								}
 								if(players.length() > 0){
 									players.append(", ");
 								}
 								players.append(online.getDisplayName());
 							}
 						player.sendMessage(ChatColor.BLUE + "Online players: " + players.toString());
 						player.sendMessage(ChatColor.DARK_AQUA + "-*-*-* " + ChatColor.GOLD + (Bukkit.getOnlinePlayers().length) + " player(s) online " + ChatColor.DARK_AQUA + " -*-*-*");
 						return true;
 					}else if(args.length == 1){
 						if(player.hasPermission("basic.whoinfo")){
 							Player target = Bukkit.getPlayer(args[0]);
 								if(target != null){
 									int x = api.getBalance(target);
 									String mName = config.getSettings().getString("Economy.name");
 									player.sendMessage(ChatColor.DARK_PURPLE + "Information on: " + target.getDisplayName());
 									if(config.getSettings().getBoolean("Economy.enabled") == true){
 										player.sendMessage(ChatColor.DARK_AQUA + mName + ": " + ChatColor.WHITE + x);
 										player.sendMessage(ChatColor.DARK_AQUA + "IP: " + ChatColor.WHITE + target.getAddress().getHostName());
 										player.sendMessage(ChatColor.DARK_AQUA + "World: " + ChatColor.WHITE + target.getWorld().getName());
 										return true;
 									}
 								}
 						}
 					}
 			}
 		}
 		return false;
 }
 }
