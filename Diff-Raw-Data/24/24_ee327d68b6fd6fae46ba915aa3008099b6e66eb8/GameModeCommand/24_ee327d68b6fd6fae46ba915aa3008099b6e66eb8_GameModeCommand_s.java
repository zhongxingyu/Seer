 package com.turt2live.antishare;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class GameModeCommand implements CommandExecutor {
 
 	private AntiShare plugin;
 
 	public GameModeCommand(AntiShare plugin){
 		this.plugin = plugin;
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
 		GameMode to = null;
 		Player target = null;
		if(command.getName().equalsIgnoreCase("gm") || command.getName().equalsIgnoreCase("gamemode")){
 			if(!(sender instanceof Player)){
 				if(args.length < 2){
 					target = Bukkit.getPlayer(args[0]);
 					if(target != null){
 						to = target.getGameMode().equals(GameMode.CREATIVE) ? GameMode.SURVIVAL : GameMode.CREATIVE;
 					}else{
 						ASUtils.sendToPlayer(sender, ChatColor.RED + "Player not found! Sorry :(");
 						return true;
 					}
 				}
 			}
 			if(args.length < 1){
 				target = (Player) sender;
 				to = target.getGameMode() == GameMode.CREATIVE ? GameMode.SURVIVAL : GameMode.CREATIVE;
 			}else if(args.length == 1 && sender instanceof Player){
 				target = (Player) sender;
 				to = ASUtils.getGameMode(args[0]);
 				if(to == null){ // for /gm turt2live
 					target = Bukkit.getPlayer(args[0]);
 					if(target == null){
 						// Reset
 						target = (Player) sender;
 					}else{
 						to = target.getGameMode() == GameMode.CREATIVE ? GameMode.SURVIVAL : GameMode.CREATIVE;
 					}
 				}
 			}else if(args.length == 1 && !(sender instanceof Player)){
 				target = Bukkit.getPlayer(args[0]);
 				if(target != null){
 					to = target.getGameMode().equals(GameMode.CREATIVE) ? GameMode.SURVIVAL : GameMode.CREATIVE;
 				}
 			}else if(args.length == 2){
 				target = Bukkit.getPlayer(args[0]);
 				to = ASUtils.getGameMode(args[1]);
 			}
 			if(target == null || to == null){
 				if(target == null){
 					ASUtils.sendToPlayer(sender, ChatColor.RED + "Player not found! Sorry :(");
 				}else{
 					ASUtils.sendToPlayer(sender, ChatColor.RED + "Gamemode not known! Sorry :(");
 				}
 				return false;
 			}
 			// Double check permissions
 			if(!plugin.getPermissions().has(sender, "AntiShare.gamemode")){
 				ASUtils.sendToPlayer(sender, ChatColor.RED + "You cannot do that!");
 				return true;
 			}
 			if(sender instanceof Player && !target.getName().equals(sender.getName())){
 				// For safer casting
 				if(!plugin.getPermissions().has(sender, "AntiShare.gamemode.others")){
 					ASUtils.sendToPlayer(sender, ChatColor.RED + "You cannot do that!");
 					return true;
 				}
 			}
 			target.setGameMode(to);
 			// Alert the right people
 			ASUtils.sendToPlayer(target, "You were changed to " + to.toString().toLowerCase());
 			if(!target.getName().equalsIgnoreCase(sender.getName())){
 				ASUtils.sendToPlayer(sender, "You changed " + target.getName() + "'s Game Mode to " + to.toString().toLowerCase());
 			}
 			return true; // All done
 		}
 		return false;
 	}
 
 }
