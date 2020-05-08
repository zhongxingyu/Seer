 package com.lenis0012.bukkit.btm;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.PluginManager;
 
 import com.lenis0012.bukkit.btm.api.Api;
 import com.lenis0012.bukkit.btm.api.Disguise;
 import com.lenis0012.bukkit.btm.events.PlayerDisguiseEvent;
 import com.lenis0012.bukkit.btm.events.PlayerUndisguiseEvent;
 
 public class BTMCommand implements CommandExecutor {
 	
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
 		if(!(sender instanceof Player)) {
 			sender.sendMessage("This command can only be executed as player");
 			return true;
 		}
 		
 		Player player = (Player)sender;
 		BeTheMob plugin = BeTheMob.instance;
 		String name = player.getName();
 		PluginManager pm = Bukkit.getServer().getPluginManager();
 		Api api = BeTheMob.getApi();
 		
 		if(args.length > 0) {
 			if(args[0].equalsIgnoreCase("mob")) {
 				if(args.length > 1) {
 					api.removeDisguise(player);
 					
 					EntityType type = this.parseType(args[1]);
					
 					if(type != null) {
 						if(this.hasPerms(player, "btm.disguise.mob."+args[1]) || this.hasPerms(player, "btm.disguise.mob.*")) {
 							PlayerDisguiseEvent ev = new PlayerDisguiseEvent(player, type);
 							pm.callEvent(ev);
 							if(!ev.isCancelled()) {
 								Location loc = player.getLocation();
 								List<String> extras = this.parseExtras(args, player);
 								Disguise dis = api.createDisguise(player, loc, type, extras);
 								api.addDisguise(player, dis);
 								inf(player, "Succesfully diguised as a "+args[1]);
 							}
 						} else
 							err(player, "Not enough permissions");
 					} else
 						err(player, "Invalid entity type");
 				} else
 					err(player, "Invalid arguments");
 			} else if(args[0].equalsIgnoreCase("player")) {
 				api.removeDisguise(player);
 				
 				if(args.length > 1) {
 					String target = args[1];
 					if(this.hasPerms(player, "btm.disguise.player."+target) || this.hasPerms(player, "btm.disguise.player.*")) {
 						if(target.length() > 16) {
 							err(player, "Names must be shorter than 17 chars");
 							return true;
 						}
 						
 						PlayerDisguiseEvent ev = new PlayerDisguiseEvent(player, target);
 						pm.callEvent(ev);
 						if(!ev.isCancelled()) {
 							int itemInHand = player.getItemInHand().getTypeId();
 							Location loc = player.getLocation();
 							Disguise dis = new Disguise(player, plugin.nextID--, loc, ev.getName(), itemInHand);
 							dis.spawn(loc.getWorld());
 							plugin.disguises.put(name, dis);
 							plugin.setHidden(player, true);
 							inf(player, "Succesfully diguised as player: "+args[1]);
 						}
 					} else
 						err(player, "Not enough permissions");
 				} else
 					err(player, "Invalid arguments");
 			} else if(args[0].equalsIgnoreCase("off")) {
 				if(plugin.disguises.containsKey(name)) {
 					Disguise dis = plugin.disguises.get(name);
 					
 					PlayerUndisguiseEvent ev = new PlayerUndisguiseEvent(player, dis);
 					pm.callEvent(ev);
 					if(!ev.isCancelled()) {
 						api.removeDisguise(player);
 						inf(player, "You are no longer disguised");
 					}
 				} else
 					err(player, "You are not disguised");
			} else if(args[0].equalsIgnoreCase("reload")){
				BeTheMob.instance.reloadConfig();
				inf(player, "Configuration reloaded!");
 			} else
 				err(player, "Invalid argument, try 'mob','player' or 'off'");
 		} else
 			err(player, "Usage: "+cmd.getUsage());
 		
 		return true;
 	}
 	
 	private boolean hasPerms(Player player, String node) {
 		return player.hasPermission(node) || player.hasPermission("btm.*");
 	}
 	
 	private List<String> parseExtras(String[] args, Player player) {
 		List<String> extras = new ArrayList<String>();
 		
 		if(args.length > 2) {
 			for(int i = 2; i < args.length; i++) {
 				if(player.hasPermission("btm.flag."+args[i].replace("-", "")) || player.hasPermission("btm.flag.*"))
 					extras.add(args[i]);
 			}
 		}
 		
 		return extras;
 	}
 	
 	private void inf(Player player, String inf) {
 		player.sendMessage(ChatColor.GREEN+inf);
 	}
 	
 	private void err(Player player, String err) {
 		player.sendMessage(ChatColor.RED+err);
 	}
 	
 	private EntityType parseType(String toParse) {
		for(EntityType type : EntityType.values()){
			if(type.toString().equalsIgnoreCase(toParse))
 				return type;
 		}
		return null;
 	}
 }
