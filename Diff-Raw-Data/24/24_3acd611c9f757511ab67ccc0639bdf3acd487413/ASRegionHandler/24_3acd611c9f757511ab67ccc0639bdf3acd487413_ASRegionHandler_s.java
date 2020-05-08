 package com.turt2live.antishare.worldedit;
 
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.turt2live.antishare.ASUtils;
 import com.turt2live.antishare.AntiShare;
 
 public class ASRegionHandler {
 
 	private AntiShare plugin;
 	private boolean hasWorldEdit = false;
 	private ASWorldEdit worldedit;
 
 	public ASRegionHandler(AntiShare plugin){
 		this.plugin = plugin;
 		if(plugin.getServer().getPluginManager().getPlugin("WorldEdit") != null){
 			hasWorldEdit = true;
 			worldedit = new ASWorldEdit(plugin);
 		}else{
 			AntiShare.log.warning("[" + plugin.getDescription().getFullName() + "] WorldEdit is not installed!");
 		}
 	}
 
 	public void newRegion(CommandSender sender, String gamemodeName){
 		if(!hasWorldEdit){
 			ASUtils.sendToPlayer(sender, ChatColor.DARK_RED + "WorldEdit is not installed. No region set.");
 			return;
 		}
 		GameMode gamemode;
 		if(gamemodeName.equalsIgnoreCase("creative") || gamemodeName.equalsIgnoreCase("c") || gamemodeName.equalsIgnoreCase("1")){
 			gamemode = GameMode.CREATIVE;
		}else if(gamemodeName.equalsIgnoreCase("survival") || gamemodeName.equalsIgnoreCase("c=s") || gamemodeName.equalsIgnoreCase("0")){
 			gamemode = GameMode.SURVIVAL;
 		}else{
 			ASUtils.sendToPlayer(sender, ChatColor.DARK_RED + "I don't know what Game Mode '" + gamemodeName + "' is!");
 			return;
 		}
 		if(!(sender instanceof Player)){
 			ASUtils.sendToPlayer(sender, ChatColor.DARK_RED + "You are not a player, sorry!");
 			return;
 		}
 		worldedit.newRegion((Player) sender, gamemode);
 	}
 
 	public AntiShare getPlugin(){
 		return plugin;
 	}
 
 	public ASWorldEdit getWorldEditHandler(){
 		return worldedit;
 	}
 
 }
