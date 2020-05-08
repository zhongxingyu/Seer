 package com.turt2live.antishare;
 
 import java.util.Vector;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
import com.turt2live.antishare.conversations.ConfigurationConversation;
 import com.turt2live.antishare.regions.ASRegion;
 import com.turt2live.antishare.regions.RegionKey;
 
 public class CommandHandler implements CommandExecutor {
 
 	private AntiShare plugin;
 
 	public CommandHandler(AntiShare plugin){
 		this.plugin = plugin;
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command command, String cmd, String[] args){
 		if(cmd.equalsIgnoreCase("antishare") ||
 				cmd.equalsIgnoreCase("as") ||
 				cmd.equalsIgnoreCase("antis") ||
 				cmd.equalsIgnoreCase("ashare")){
 			if(args.length > 0){
 				if(args[0].equalsIgnoreCase("reload") || args[0].equalsIgnoreCase("rl")){
 					if(plugin.getPermissions().has(sender, "AntiShare.reload")){
 						plugin.reloadConfig();
 						MultiWorld.detectWorlds((AntiShare) Bukkit.getServer().getPluginManager().getPlugin("AntiShare"));
 						plugin.storage.reload(sender);
 						AntiShare.log.info("AntiShare Reloaded.");
 						if(sender instanceof Player){
 							ASUtils.sendToPlayer(sender, ChatColor.GREEN + "AntiShare Reloaded.");
 						}
 					}else{
 						ASUtils.sendToPlayer(sender, ChatColor.DARK_RED + "You do not have permission!");
 					}
 					return true;
 				}else if(args[0].equalsIgnoreCase("region")){
					if(plugin.getPermissions().has(sender, "AntiShare.regions")){
 						if(args.length < 3){
 							ASUtils.sendToPlayer(sender, ChatColor.RED + "Syntax error, try: /as region <gamemode> <name>");
 						}else{
 							plugin.getRegionHandler().newRegion(sender, args[1], args[2]);
 						}
 					}else{
 						ASUtils.sendToPlayer(sender, ChatColor.DARK_RED + "You do not have permission!");
 					}
 					return true;
 				}else if(args[0].equalsIgnoreCase("rmregion")){
					if(plugin.getPermissions().has(sender, "AntiShare.regions")){
 						if(args.length > 1){
 							plugin.getRegionHandler().removeRegion(args[1], sender);
 						}else{
 							if(!(sender instanceof Player)){
 								ASUtils.sendToPlayer(sender, ChatColor.DARK_RED + "You have not supplied a name, try /as rmregion <name>");
 							}else{
 								plugin.getRegionHandler().removeRegion(((Player) sender).getLocation(), (Player) sender);
 							}
 						}
 					}else{
 						ASUtils.sendToPlayer(sender, ChatColor.DARK_RED + "You do not have permission!");
 					}
 					return true;
 				}else if(args[0].equalsIgnoreCase("editregion")){
					if(plugin.getPermissions().has(sender, "AntiShare.regions")){
 						boolean valid = false;
 						if(args.length >= 3){
 							if(RegionKey.isKey(args[2])){
 								if(!RegionKey.requiresValue(RegionKey.getKey(args[2]))){
 									valid = true; // we have at least 3 values in args[] and the key does not need a value
 								}
 							}
 						}
 						if(args.length >= 4){
 							valid = true;
 						}
 						if(!valid){
 							if(args.length >= 2){
 								if(args[1].equalsIgnoreCase("help")){
 									ASUtils.sendToPlayer(sender, ChatColor.GOLD + "/as editregion <name> <key> <value>");
 									ASUtils.sendToPlayer(sender, ChatColor.AQUA + "Key: " + ChatColor.WHITE + "name " + ChatColor.AQUA + "Value: " + ChatColor.WHITE + "<any name>");
 									ASUtils.sendToPlayer(sender, ChatColor.AQUA + "Key: " + ChatColor.WHITE + "ShowEnterMessage " + ChatColor.AQUA + "Value: " + ChatColor.WHITE + "true/false");
 									ASUtils.sendToPlayer(sender, ChatColor.AQUA + "Key: " + ChatColor.WHITE + "ShowExitMessage " + ChatColor.AQUA + "Value: " + ChatColor.WHITE + "true/false");
 									ASUtils.sendToPlayer(sender, ChatColor.AQUA + "Key: " + ChatColor.WHITE + "EnterMessage " + ChatColor.AQUA + "Value: " + ChatColor.WHITE + "<enter message>");
 									ASUtils.sendToPlayer(sender, ChatColor.AQUA + "Key: " + ChatColor.WHITE + "ExitMessage " + ChatColor.AQUA + "Value: " + ChatColor.WHITE + "<exit message>");
 									ASUtils.sendToPlayer(sender, ChatColor.AQUA + "Key: " + ChatColor.WHITE + "inventory " + ChatColor.AQUA + "Value: " + ChatColor.WHITE + "'none'/'set'");
 									ASUtils.sendToPlayer(sender, ChatColor.AQUA + "Key: " + ChatColor.WHITE + "gamemode " + ChatColor.AQUA + "Value: " + ChatColor.WHITE + "survival/creative");
 									ASUtils.sendToPlayer(sender, ChatColor.AQUA + "Key: " + ChatColor.WHITE + "area " + ChatColor.AQUA + "Value: " + ChatColor.WHITE + "No Value");
 									ASUtils.sendToPlayer(sender, ChatColor.YELLOW + "'Show____Message'" + ChatColor.WHITE + " - True to show the message");
 									ASUtils.sendToPlayer(sender, ChatColor.YELLOW + "'____Message'" + ChatColor.WHITE + " - Use {name} to input the region name.");
 									ASUtils.sendToPlayer(sender, ChatColor.YELLOW + "'inventory'" + ChatColor.WHITE + " - Sets the region's inventory. 'none' to not have a default inventory, 'set' to mirror yours");
 									ASUtils.sendToPlayer(sender, ChatColor.YELLOW + "'area'" + ChatColor.WHITE + " - Sets the area based on your WorldEdit selection");
 								}else{
 									ASUtils.sendToPlayer(sender, ChatColor.DARK_RED + "Incorrect syntax, try: /as editregion <name> <key> <value>");
 									ASUtils.sendToPlayer(sender, ChatColor.RED + "For keys and values type /as editregion help");
 								}
 							}else{
 								ASUtils.sendToPlayer(sender, ChatColor.DARK_RED + "Incorrect syntax, try: /as editregion <name> <key> <value>");
 								ASUtils.sendToPlayer(sender, ChatColor.RED + "For keys and values type /as editregion help");
 							}
 						}else{
 							String name = args[1];
 							String key = args[2];
 							String value = args[3];
 							if(args.length > 4){
 								for(int i = 4; i < args.length; i++){ // Starts at args[4]
 									value = value + args[i] + " ";
 								}
 								value = value.substring(0, value.length() - 1);
 							}
 							if(plugin.getRegionHandler().getRegionByName(name) == null){
 								ASUtils.sendToPlayer(sender, ChatColor.RED + "That region does not exist!");
 							}else{
 								if(RegionKey.isKey(key)){
 									plugin.getRegionHandler().editRegion(plugin.getRegionHandler().getRegionByName(name), RegionKey.getKey(key), value, sender);
 								}else{
 									ASUtils.sendToPlayer(sender, ChatColor.DARK_RED + "That is not a valid region key");
 									ASUtils.sendToPlayer(sender, ChatColor.RED + "For keys and values type /as editregion help");
 								}
 							}
 						}
 					}else{
 						ASUtils.sendToPlayer(sender, ChatColor.DARK_RED + "You do not have permission!");
 					}
 					return true;
				}else if(args[0].equalsIgnoreCase("listplugins")){
					if(plugin.getPermissions().has(sender, "AntiShare.regions")){
 						int page = 1;
 						if(args.length >= 2){
 							try{
 								page = Integer.parseInt(args[1]);
 							}catch(Exception e){
 								ASUtils.sendToPlayer(sender, ChatColor.RED + "'" + args[1] + "' is not a number!");
 								return true;
 							}
 						}
 						page = Math.abs(page);
 						int resultsPerPage = 6; // For ease of changing
 						Vector<ASRegion> regions = plugin.storage.getAllRegions();
 						int maxPages = (int) Math.ceil(regions.size() / resultsPerPage);
 						if(maxPages < 1){
 							maxPages = 1;
 						}
 						if(maxPages < page){
 							ASUtils.sendToPlayer(sender, ChatColor.RED + "Page " + page + " does not exist! The last page is " + maxPages);
 							return true;
 						}
 						String pagenation = ChatColor.DARK_GREEN + "=======[ " + ChatColor.GREEN + "AntiShare Regions " + ChatColor.DARK_GREEN + "|" + ChatColor.GREEN + " Page " + page + "/" + maxPages + ChatColor.DARK_GREEN + " ]=======";
 						ASUtils.sendToPlayer(sender, pagenation);
 						for(int i = ((page - 1) * resultsPerPage); i < (resultsPerPage < regions.size() ? (resultsPerPage * page) : regions.size()); i++){
 							ASUtils.sendToPlayer(sender, ChatColor.DARK_AQUA + "#" + (i + 1) + " " + ChatColor.GOLD + regions.get(i).getName()
 									+ ChatColor.YELLOW + " Created By: " + ChatColor.AQUA + regions.get(i).getWhoSet()
 									+ ChatColor.YELLOW + " World: " + ChatColor.AQUA + regions.get(i).getWorld().getName());
 						}
 						ASUtils.sendToPlayer(sender, pagenation);
 					}else{
 						ASUtils.sendToPlayer(sender, ChatColor.DARK_RED + "You do not have permission!");
 					}
 					return true;
				}else if(args[0].equalsIgnoreCase("config")){
					if(plugin.getPermissions().has(sender, "AntiShare.config")){
						if(sender instanceof Player){
							new ConfigurationConversation(plugin, (Player) sender);
						}else{
							ASUtils.sendToPlayer(sender, ChatColor.RED + "There is no console support for the configuration helper.");
						}
					}
 				}else{
 					return false; //Shows usage in plugin.yml
 				}
 			}
 			// Unhandled command (such as /as help, or /as asjkdhgasjdg)
 			return false; //Shows usage in plugin.yml
 		}
 		return false; //Shows usage in plugin.yml
 	}
 
 }
