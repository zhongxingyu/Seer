 package com.wwsean08.clear;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.logging.Logger;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Server;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.ConsoleCommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class Clear extends JavaPlugin {
 	Logger log = Logger.getLogger("Minecraft");
 	ArrayList<ClearItemHolder> items;
 	PreviewCommand preview;
 	final String PREFIX = "[ClearInv]";
 	boolean usesSP = true;
 	private final String VERSION = "1.9.5";
 	private String DBVersion = "1.1.4";
 	private File itemFile = null;
 	private Server server;
 	private FileConfiguration config;
 	private HashMap<String, ClearUndoHolder> undo;
 
 
 	@Override
 	public void onEnable() {
 		initVariables();
 		getDBV();
 		createConfig();
 		if(config.getBoolean("autoupdate", true))
 			checkForUpdates();
 		usesSP = config.getBoolean("superperm", true);
 		loadItems();
 		getCommand("preview").setExecutor(preview);
 		getCommand("unpreview").setExecutor(preview);
 		log.info(PREFIX + " version " + VERSION + " enabled");
 	}
 
 	@Override
 	public void onDisable() {
 		Player[] player = server.getOnlinePlayers();
 		for(Player p : player){
 			preview.unpreview(p);
 		}
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		if (sender instanceof Player) {
 			if(usesSP){
 				Perm(sender, args);
 			}
 			// if no permissions set up
 			else 
 				NoPerm(sender, args);
 
 		} else if (sender instanceof ConsoleCommandSender) 
 			consoleClear(sender, args);
 		return true;
 	}
 
 	/**
 	 * used if the server uses superperm (aka bukkit perms)
 	 * @param sender is the player who sends the command
 	 * @param args is the arguments of the command
 	 * 
 	 */
 
 	private void Perm(CommandSender sender, String[] args) {
 		Player player = (Player) sender;
 		if (player.hasPermission("clear.self") || player.hasPermission("clear.other") || player.hasPermission("clear.admin")) {
 			if (args.length == 0)
 				clearAll(player);
 			else if(args[0].trim().equals("*")){
 				if(player.hasPermission("clear.admin")){
 					Player[] online = server.getOnlinePlayers();
 					if(args.length == 1){
 						for(Player p : online)
 							clearAllRemote(sender, p);
 					}
 					else if(args[1].trim().equalsIgnoreCase("except")){
 						for(Player p : online)
 							clearExceptRemote(sender, p, args);
 					}
 					else if(args[1].equalsIgnoreCase("armor")){
 						for(Player p : online)
 							clearArmorRemote(sender, p);
 					}
 					else{
 						for(Player p : online)
 							clearItemRemote(sender, p, args);
 					}
 				}
 			}
 			else if (args[0].equalsIgnoreCase("reload")){
 				if(player.hasPermission("clear.admin")){
 					loadItems();
					sender.sendMessage("Reloaded items");
 				}
 			}else if(args[0].equalsIgnoreCase("version")){
				sender.sendMessage(PREFIX + "version " + VERSION);
 			}
 			else if (args[0].equalsIgnoreCase("except")){
 				clearExcept(player, args);
 			}
 			else if (args[0].equalsIgnoreCase("undo")){
 				clearUndo(player);
 			}
 			else if (args[0].equalsIgnoreCase("armor"))
 				clearArmor(player);
 			else if(args[0].equalsIgnoreCase("boots") || args[0].equalsIgnoreCase("boot")){
 				player.getInventory().setBoots(null);
 				player.sendMessage(ChatColor.GRAY + "Boots removed");
 			}else if(args[0].equalsIgnoreCase("helmet") || args[0].equalsIgnoreCase("helm")){
 				player.getInventory().setHelmet(null);
 				player.sendMessage(ChatColor.GRAY + "Helmet removed");
 			}else if(args[0].equalsIgnoreCase("pants") || args[0].equalsIgnoreCase("leggings")){
 				player.getInventory().setLeggings(null);
 				player.sendMessage(ChatColor.GRAY + "Leggings removed");
 			}else if(args[0].equalsIgnoreCase("shirt") || args[0].equalsIgnoreCase("chestplate") || args[0].equalsIgnoreCase("chest")){
 				player.getInventory().setChestplate(null);
 				player.sendMessage(ChatColor.GRAY + "Chestplate removed");
 			}else if (args[0].equalsIgnoreCase("help")) {
 				playerHelp(sender);
 			} else if ((server.getPlayer(args[0]) != null) && (player.hasPermission("clear.other") || player.hasPermission("clear.admin") || sender.isOp())) {
 				Player affectedPlayer = server.matchPlayer(args[0]).get(0);
 				if (args.length == 1) {
 					clearAllRemote(player, affectedPlayer);
 				} else if (args[1].equalsIgnoreCase("except")) {
 					clearExceptRemote(player, affectedPlayer, args);
 				} else if(args[1].equalsIgnoreCase("armor")){
 					clearArmorRemote(player, affectedPlayer);
 				} else
 					clearItemRemote(player, affectedPlayer, args);
 			} else if ((server.getPlayer(args[0]) != null) && !((player.hasPermission("clear.other") || !(player.hasPermission("clear.admin")) || !sender.isOp()))) {
 				sender.sendMessage(ChatColor.RED + "You do not have permission to use that command");
 				log.warning(PREFIX + player.getDisplayName() + " tried to clear another players inventory without the necessary permissions");
 			} else 
 				clearItem(player, args);
 		}
 	}
 
 	/**
 	 * takes care of commands if no permissions system is detected
 	 * @param sender is the player who sent the command
 	 * @param args are the arguments for the command
 	 * 
 	 */
 	private void NoPerm(CommandSender sender, String[] args){
 		Player player = (Player) sender;
 		if (args.length == 0) 
 			clearAll(player);
 		else if (args[0].equalsIgnoreCase("except")) 
 			clearExcept(player, args);
 		else if(args[0].trim().equals("*")){
 			if(sender.isOp()){
 				Player[] online = server.getOnlinePlayers();
 				if(args.length == 1){
 					for(Player p : online)
 						clearAllRemote(sender, p);
 				}else if(args[1].trim().equalsIgnoreCase("except")){
 					for(Player p : online){
 						clearExceptRemote(sender, p, args);
 					}
 				}else if(args[1].trim().equalsIgnoreCase("armor")){
 					for(Player p : online){
 						clearArmorRemote(sender, p);
 					}
 				}
 				else{
 					for(Player p : online){
 						clearItemRemote(sender, p, args);
 					}
 				}
 			}else{
 				sender.sendMessage(ChatColor.RED + "You do not have permission to use that command");
 				log.warning(PREFIX + player.getDisplayName() + " tried to clear another players inventory without the necessary permissions");
 			}
 		}
 		else if (args[0].equalsIgnoreCase("help")) 
 			playerHelp(sender);
 		else if (args[0].equalsIgnoreCase("reload")){
 			if(player.isOp()){
 				loadItems();
				sender.sendMessage("Reloaded items");
 			}
 		}else if(args[0].equalsIgnoreCase("version")){
			sender.sendMessage(PREFIX + "version " + VERSION);
 		}
 		else if (args[0].equalsIgnoreCase("undo")){
 			clearUndo(player);
 		}
 		//begin armor removal
 		else if(args[0].equalsIgnoreCase("armor"))
 			clearArmor(player);
 		else if(args[0].equalsIgnoreCase("boots") || args[0].equalsIgnoreCase("boot")){
 			player.getInventory().setBoots(null);
 			player.sendMessage(ChatColor.GRAY + "Boots removed");
 		}else if(args[0].equalsIgnoreCase("helmet") || args[0].equalsIgnoreCase("helm")){
 			player.getInventory().setHelmet(null);
 			player.sendMessage(ChatColor.GRAY + "Helmet removed");
 		}else if(args[0].equalsIgnoreCase("pants") || args[0].equalsIgnoreCase("leggings")){
 			player.getInventory().setLeggings(null);
 			player.sendMessage(ChatColor.GRAY + "Leggings removed");
 		}else if(args[0].equalsIgnoreCase("shirt") || args[0].equalsIgnoreCase("chestplate") || args[0].equalsIgnoreCase("chest")){
 			player.getInventory().setChestplate(null);
 			player.sendMessage(ChatColor.GRAY + "Chestplate removed");
 		}
 		//end armor removal
 		else if (server.getPlayer(args[0]) != null) {
 			if (sender.isOp()) {
 				Player affectedPlayer = server.matchPlayer(args[0]).get(0);
 				if (args.length == 1)
 					clearAllRemote(sender, affectedPlayer);
 				else if (args[1].equalsIgnoreCase("except")) 
 					clearExceptRemote(sender, affectedPlayer, args);
 				else if (args[1].trim().equalsIgnoreCase("armor"))
 					clearArmorRemote(sender, affectedPlayer);
 				else 
 					clearItemRemote(sender, affectedPlayer, args);
 			} else {
 				sender.sendMessage(ChatColor.RED + "You do not have permission to use that command");
 				log.warning(PREFIX + player.getDisplayName() + " tried to clear another players inventory without the necessary permissions");
 			}
 		} else 
 			clearItem(player, args);
 	}
 
 	/**
 	 * takse care of commands sent by the console
 	 * @param sender is the console which sent the command
 	 * @param args are the arguments for the command
 	 */
 	private void consoleClear(CommandSender sender, String[] args){
 		if (args.length >= 1) {
 			if (args[0].equalsIgnoreCase("help")){
 				consoleHelp(sender);
 				return;
 			}else if (args[0].equalsIgnoreCase("reload")){
 				loadItems();
				sender.sendMessage("Reloaded items");
 			}else if(args[0].equalsIgnoreCase("version")){
				sender.sendMessage(PREFIX + "version " + VERSION);
 			}else if (args[0].equalsIgnoreCase("undo")){
 				clearUndo(sender);
 			}else if(args[0].equalsIgnoreCase("*")){
 				Player[] online = server.getOnlinePlayers();
 				if(args.length == 1){
 					for(Player p : online)
 						clearAllRemote(sender, p);
 				}
 				else if(args[1].trim().equalsIgnoreCase("except")){
 					for(Player p : online)
 						clearExceptRemote(sender, p, args);
 				}
 				else if(args[1].equalsIgnoreCase("armor")){
 					for(Player p : online)
 						clearArmorRemote(sender, p);
 				}
 				else{
 					for(Player p : online)
 						clearItemRemote(sender, p, args);
 				}
 			}else if (args[1].equalsIgnoreCase("except")){
 				Player player = server.getPlayer(args[0]);
 				clearExceptRemote(sender, player, args);
 			}
 			else if(args[1].equalsIgnoreCase("armor")){
 				Player player = server.getPlayer(args[0]);
 				clearArmorRemote(sender, player);
 			}
 			else if(args[1].equalsIgnoreCase("boots") || args[1].equalsIgnoreCase("boot")){
 				Player player = server.getPlayer(args[0]);
 				player.getInventory().setBoots(null);
 				sender.sendMessage(ChatColor.GRAY + "Boots removed from " + player.getDisplayName());
 			}else if(args[1].equalsIgnoreCase("helmet") || args[1].equalsIgnoreCase("helm")){
 				Player player = server.getPlayer(args[0]);
 				player.getInventory().setHelmet(null);
 				sender.sendMessage(ChatColor.GRAY + "Helmet removed from " + player.getDisplayName());
 			}else if(args[1].equalsIgnoreCase("pants") || args[1].equalsIgnoreCase("leggings")){
 				Player player = server.getPlayer(args[0]);
 				player.getInventory().setLeggings(null);
 				sender.sendMessage(ChatColor.GRAY + "Leggings removed from " + player.getDisplayName());
 			}else if(args[1].equalsIgnoreCase("shirt") || args[1].equalsIgnoreCase("chestplate") || args[1].equalsIgnoreCase("chest")){
 				Player player = server.getPlayer(args[0]);
 				player.getInventory().setChestplate(null);
 				sender.sendMessage(ChatColor.GRAY + "Chestplate removed from " + player.getDisplayName());
 			}else if (args.length == 1){
 				Player player = server.getPlayer(args[0]);
 				clearAllRemote(sender, player);
 			}else {
 				Player player = server.getPlayer(args[0]);
 				clearItemRemote(sender, player, args);
 			}
 		}
 	}
 	/**
 	 * displays the help text if the console sent the command
 	 * @param sender is the person who sent the command
 	 */
 	private void consoleHelp(CommandSender sender){
 		sender.sendMessage(ChatColor.BLUE + "Because you are the server console, you don't have an inventory, however you can clear other players inventories");
 		sender.sendMessage(ChatColor.BLUE + "To clear a players inventory completely type:");
 		sender.sendMessage(ChatColor.RED + "/clear <player>");
 		sender.sendMessage(ChatColor.BLUE + "To clear certain items from a players inventory type:");
 		sender.sendMessage(ChatColor.RED + "/clear <player> <item1> [item2] [item3]...");
 		sender.sendMessage(ChatColor.BLUE + "And if you want to clear everything bu8t certain items from a players inventory type this:");
 		sender.sendMessage(ChatColor.RED + "/clear <player> except <item1> [item2] [item3]...");
 		sender.sendMessage(ChatColor.GOLD + "Thank you for using wwsean08's inventory clearing plugin");
 	}
 	/**
 	 * displays the help text if the player sent the command
 	 * @param sender Is the person who sent the command
 	 */
 	private void playerHelp(CommandSender sender){
 		Player player = (Player) sender;
 		sender.sendMessage(ChatColor.AQUA + "you can clear your inventory of everything like this:");
 		sender.sendMessage(ChatColor.RED + "/clear");
 		sender.sendMessage(ChatColor.AQUA + "You can exclude items using the except keyword as the first argument like this:");
 		sender.sendMessage(ChatColor.RED + "/clear except sand");
 		sender.sendMessage(ChatColor.AQUA + "You can delete select items by naming them as arguments like this:");
 		sender.sendMessage(ChatColor.RED + "/clear sand gravel");
 		if (sender.isOp() || player.hasPermission("clear.other")) {
 			sender.sendMessage(ChatColor.AQUA + "And you have permission to clear other peoples invetories, and view them");
 			sender.sendMessage(ChatColor.RED + "/clear name item1 item2...");
 			sender.sendMessage(ChatColor.AQUA + "Tp view them the command is preview and to unview them its unpreview");
 			sender.sendMessage(ChatColor.RED + "/preview <user> to put the players inventory in yours");
 			sender.sendMessage(ChatColor.RED + "/unpreview to restore your inventory");
 		}
 	}
 
 	/**
 	 * Clears all the items out of the players inventory
 	 * @param sender is the player who sent the command
 	 */
 	public void clearAll(Player sender) {
 		ClearUndoHolder holder = new ClearUndoHolder(sender.getName(), new ArrayList<ItemStack>(Arrays.asList(sender.getInventory().getContents())));
 		undo.put(sender.getName(), holder);
 		sender.getInventory().clear();
 		sender.sendMessage(ChatColor.GRAY + "Inventory Cleared");
 	}
 
 	/**
 	 * Clears all the items out of another user's inventory
 	 * @param sender is the player who sent the command.
 	 * @param affected is the player who's inventory gets cleared.
 	 */
 	public void clearAllRemote(CommandSender sender, Player affected) {
 		ClearUndoHolder holder = new ClearUndoHolder(affected.getName(), new ArrayList<ItemStack>(Arrays.asList(affected.getInventory().getContents())));
 		undo.put(sender.getName(), holder);
 		affected.getInventory().clear();
 		sender.sendMessage(ChatColor.GRAY + affected.getDisplayName() + "'s inventory has been cleared.");
 	}
 
 	/**
 	 * clears all the items except for the ones specified by the player.
 	 * @param sender is the player who sent the command
 	 * @param args the list of items to exclude (either in number of name form).
 	 */
 	public void clearExcept(Player sender, String[] args) {
 		ArrayList<ItemStack> removed = new ArrayList<ItemStack>();
 		PlayerInventory pi = sender.getInventory();
 		ArrayList<Integer> clear = new ArrayList<Integer>();
 		ArrayList<String> successful = new ArrayList<String>();
 		for(int i = 0; i<pi.getSize(); i++)
 			clear.add(i);
 		for(String input : args){
 			for(int j = 0; j < items.size(); j++){
 				if(items.get(j).getInput().equalsIgnoreCase(input)){
 					if(hasData(items.get(j).getItem())){
 						for(int k = 0; k<pi.getSize(); k++){
 							if(pi.getItem(k).getTypeId() == items.get(j).getItem()){
 								if(checkData(pi.getItem(k).getData().getData(), items.get(j).getDamage())){
 									clear.remove((Integer)k);
 									if(!successful.contains(items.get(j).getOutput())){
 										successful.add(items.get(j).getOutput());
 									}
 								}
 							}
 						}
 					}else{
 						for(int k = 0; k<pi.getSize();k++){
 							if(pi.getItem(k).getTypeId() == items.get(j).getItem()){
 								clear.remove((Integer)k);
 								if(!successful.contains(items.get(j).getOutput())){
 									successful.add(items.get(j).getOutput());
 								}
 							}
 						}
 					}
 					break;
 				}
 			}
 		}
 
 		for(Integer slot : clear){
 			removed.add(pi.getItem(slot).clone());
 			pi.clear(slot);
 		}
 		StringBuilder output = new StringBuilder();
 		if(successful.size() >= 3){
 			for(int i = 0; i<successful.size()-1; i++){
 				output.append(successful.get(i) + ", ");
 			}
 			output.append("and " + successful.get(successful.size()-1));
 		}else if(successful.size() == 2){
 			output.append(successful.get(0));
 			output.append(" and ");
 			output.append(successful.get(1));
 		}else if(successful.size() == 1){
 			output.append(successful.get(0));
 		}else{
 			sender.sendMessage(ChatColor.RED + "Clear except command failed or you didn't have that to start out with");
 		}
 		sender.sendMessage(ChatColor.GRAY + "Successfully removed everything except " + output);
 		ClearUndoHolder holder = new ClearUndoHolder(sender.getName(), removed);
 		undo.put(sender.getName(), holder);
 	}
 
 
 	/**
 	 * clears all the items except for the ones specified by the player.
 	 * @param sender is the player who sent the command
 	 * @param args the list of items to exclude (either in number of name form).
 	 */
 	public void clearExceptRemote(CommandSender sender, Player affected, String[] args) {
 		ArrayList<ItemStack> removed = new ArrayList<ItemStack>();
 		PlayerInventory pi;
 		if(affected != null)
 			pi = affected.getInventory();
 		else{
 			sender.sendMessage(ChatColor.RED + PREFIX + " Error: player variable was null!");
 			return;
 		}
 		ArrayList<Integer> clear = new ArrayList<Integer>();
 		ArrayList<String> successful = new ArrayList<String>();
 		for(int i = 0; i<pi.getSize(); i++){
 			clear.add(i);
 		}
 		for(String a : args){
 			for(int j = 0;j<items.size(); j++){
 				if(items.get(j).getInput().equalsIgnoreCase(a)){
 					for(int k = 0; k<35; k++){
 						if(hasData(items.get(j).getItem())){
 							if(pi.getItem(k).getTypeId() == items.get(j).getItem()){
 								if(checkData(pi.getItem(k).getData().getData(), items.get(j).getDamage())){
 									clear.remove((Integer)k);
 									if(!successful.contains(items.get(j).getOutput())){
 										successful.add(items.get(j).getOutput());
 									}
 								}
 							}
 						}else{
 							if(pi.getItem(k).getTypeId() == items.get(j).getItem()){
 								clear.remove((Integer)k);
 								if(!successful.contains(items.get(j).getOutput())){
 									successful.add(items.get(j).getOutput());
 								}
 							}
 						}
 					}
 				}
 			}
 		}
 		for(Integer slot : clear){
 			if(pi != null){
 				removed.add(pi.getItem(slot).clone());
 				pi.clear(slot);
 			}
 		}
 		StringBuilder output = new StringBuilder();
 		if(successful.size() >= 3){
 			for(int i = 0; i<successful.size()-1; i++){
 				output.append(successful.get(i) + ", ");
 			}
 			output.append("and " + successful.get(successful.size()-1));
 		}else if(successful.size() == 2){
 			output.append(successful.get(0));
 			output.append(" and ");
 			output.append(successful.get(1));
 		}else if(successful.size() == 1){
 			output.append(successful.get(0));
 		}else{
 			sender.sendMessage(ChatColor.RED + "Clear except command failed or they didn't have that to start out with");
 			return;
 		}
 		sender.sendMessage(ChatColor.GRAY + "Successfully removed everything except " + output);
 		ClearUndoHolder holder = new ClearUndoHolder(affected.getName(), removed);
 		undo.put(sender.getName(), holder);
 	}
 
 	/**
 	 * clears all of the selected items by the player
 	 * @param Sender the player who sent the command
 	 * @param args is the list or item(s) that the user wants to delete from their inventory
 	 */
 	public void clearItem(Player sender, String[] args) {
 		ArrayList<ItemStack> removed = new ArrayList<ItemStack>();
 		PlayerInventory pi = sender.getInventory();
 		for(String input : args){
 			for(int i = 0; i<items.size(); i++){
 				if(input.equalsIgnoreCase(items.get(i).getInput())){
 					if(!hasData(items.get(i).getItem())){
 						for(int j = 0; j<pi.getSize(); j++){
 							ItemStack IS = pi.getItem(j);
 							if(IS == null)
 								continue;
 							if(IS.getTypeId() == items.get(i).getItem()){
 								removed.add(pi.getItem(j).clone());
 								pi.clear(j);
 							}
 						}
 					}else{
 						for(int j = 0; j<pi.getSize(); j++){
 							ItemStack IS = pi.getItem(j);
 							if(IS == null)
 								continue;
 							if(hasData(IS.getTypeId())){
 								if(checkData(IS.getData().getData(), items.get(i).getDamage())){
 									removed.add(IS.clone());
 									pi.clear(j);
 								}
 							}
 						}
 					}
 					sender.sendMessage(ChatColor.GRAY + "Cleared all " + items.get(i).getOutput());
 					break;
 				}
 			}
 		}
 		ClearUndoHolder holder = new ClearUndoHolder(sender.getName(), removed);
 		undo.put(sender.getName(), holder);
 	}
 
 	/**
 	 * clears all of the selected items by the player
 	 * @param Sender is the player who sent the command.
 	 * @param affected is the player who's items are being removed
 	 * @param args is the list or item(s) that the user wants to delete from their inventory
 	 */
 	public void clearItemRemote(CommandSender sender, Player affected, String[] args) {
 		ArrayList<ItemStack> removed = new ArrayList<ItemStack>();
 		PlayerInventory pi;
 		if(affected != null)
 			pi = affected.getInventory();
 		else{
 			sender.sendMessage(ChatColor.RED + PREFIX + " An error occured while clearing " + args[0] + "'s inventory");
 			return;
 		}
 		for(String input : args){
 			for(int i = 0; i<items.size(); i++){
 				if(input.equalsIgnoreCase(items.get(i).getInput())){
 					if(!hasData(items.get(i).getItem())){
 						for(int j = 0; j<pi.getSize(); j++){
 							ItemStack IS = pi.getItem(j);
 							if(IS == null)
 								continue;
 							if(IS.getTypeId() == items.get(i).getItem()){
 								removed.add(pi.getItem(j).clone());
 								pi.clear(j);
 							}
 						}
 					}else{
 						for(int j = 0; j<pi.getSize(); j++){
 							ItemStack IS = pi.getItem(j);
 							if(hasData(IS.getTypeId())){
 								if(checkData(IS.getData().getData(), items.get(i).getDamage())){
 									removed.add(IS.clone());
 									pi.clear(j);
 								}
 							}
 						}
 					}
 					sender.sendMessage(ChatColor.GRAY + "Cleared all " + items.get(i).getOutput());
 					break;
 				}
 			}
 		}
 		ClearUndoHolder holder = new ClearUndoHolder(affected.getName(), removed);
 		undo.put(sender.getName(), holder);
 	}
 	/**
 	 * allows a player to undo the last clearing of an inventory they did.  This will not fix a clearing of all players.
 	 * @param player
 	 */
 	public void clearUndo(Player player){
 		ClearUndoHolder holder = undo.get(player.getName());
 		if(holder != null){
 			Player affected = server.getPlayer(holder.getPlayer());
 			if(affected != null){
 				PlayerInventory pi = affected.getInventory();
 				if(pi != null){
 					for(ItemStack IS : holder.getOldInventory()){
 						if(IS == null || IS.getTypeId() == 0)
 							continue;
 						pi.addItem(IS);
 					}
 					undo.remove(player.getName());
 				}
 			}
 			else{
 				player.sendMessage(ChatColor.RED + "It would appear that " + holder.getPlayer() + " is offline so you can't undo clearing his inventory");
 			}
 		}else{
 			player.sendMessage(ChatColor.GRAY + "Nothing to undo");
 		}
 	}
 	/**
 	 * allows a command sender (the console specifically) to undo the last clearing of an inventory they did. 
 	 * This will not fix a clearing of all players.
 	 * @param sender
 	 */
 	public void clearUndo(CommandSender sender){
 		ClearUndoHolder holder = undo.get(sender.getName());
 		if(holder != null){
 			Player affected = server.getPlayer(holder.getPlayer());
 			if(affected != null){
 				PlayerInventory pi = affected.getInventory();
 				if(pi != null){
 					for(ItemStack IS : holder.getOldInventory()){
 						if(IS == null || IS.getTypeId() == 0)
 							continue;
 						pi.addItem(IS);
 					}
 					undo.remove(sender.getName());
 				}
 				else{
 					sender.sendMessage(ChatColor.RED + "It would appear that " + holder.getPlayer() + " is offline so you can't undo clearing his inventory");
 				}
 			}
 		}else{
 			sender.sendMessage(ChatColor.GRAY + "Nothing to undo");
 		}
 	}
 
 	private void clearArmor(Player sender){
 		sender.getInventory().setBoots(null);
 		sender.getInventory().setChestplate(null);
 		sender.getInventory().setHelmet(null);
 		sender.getInventory().setLeggings(null);
 		sender.sendMessage(ChatColor.GRAY + "Armor removed");
 	}
 
 	private void clearArmorRemote(CommandSender sender, Player affected){
 		affected.getInventory().setBoots(null);
 		affected.getInventory().setChestplate(null);
 		affected.getInventory().setHelmet(null);
 		affected.getInventory().setLeggings(null);
 		sender.sendMessage(ChatColor.GRAY + affected.getName() + " has had his armor removed by you");
 	}
 
 	/**
 	 * Checks the data to see if the two pieces of data given are the same
 	 * @param data is the data of the item from the inventory
 	 * @param damage is the data we want it to be
 	 */
 	private boolean checkData(byte data, int damage) {
 		Byte testByte = data;
 		return testByte.intValue() == damage;
 	}
 	/**
 	 * checks against known items which have data
 	 * @param the item ID used to determine if it has data (or can have data).
 	 */
 	private boolean hasData(int ID) {
 		switch(ID){
 		case 17:
 		case 18:
 		case 26:
 		case 35:
 		case 43:
 		case 92:
 		case 93:
 		case 263:
 		case 351:
 		case 373:
 		case 383:
 			return true;
 		}
 		return false;
 	}
 
 	/** 
 	 * A method for getting the version of the items.csv file.
 	 */
 	private void getDBV(){
 		try {
 			FileReader reader = new FileReader(itemFile);
 			BufferedReader in = new BufferedReader(reader);
 			String line = in.readLine();
 			DBVersion = line;
 		}catch(Exception e){
 		}
 	}
 
 	/**
 	 * This method loads the csv into the ClearItemHolder object
 	 */
 	private void loadItems(){
 		items = new ArrayList<ClearItemHolder>();
 		String line = "";
 		try {
 			FileReader reader = new FileReader(itemFile);
 			BufferedReader in = new BufferedReader(reader);
 			in.readLine();	//version line
 			line = in.readLine();
 			while(line != null){
 				String[] args = line.split(",");
 				int item = Integer.parseInt(args[1].trim());
 				int damage = Integer.parseInt(args[2].trim());
 				ClearItemHolder newItem = new ClearItemHolder(args[0], item, damage, args[3]);
 				items.add(newItem);
 				line = in.readLine();
 			}
 		} catch (FileNotFoundException e) {
 			log.warning(ChatColor.RED + "You have not downloaded the items.csv, make sure to download the new one as it is necessary for the 1.8 update");
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch(NumberFormatException e){
 			log.warning("If you did NOT edit the items.csv tell wwsean08 in the bukkit forums that there is an error with this line: " + line);
 		}
 	}
 	/**
 	 * creates the config.yml if one doesn't exist or is outdated
 	 * 
 	 */
 	private void createConfig(){
 		config = this.getConfig();
 		config.options().copyDefaults(true);
 		this.saveConfig();
 		//just getting ready for when the bleeding edge stuff comes out
 		if(!itemFile.exists()){
 			//saveResource("items.csv", true);	//for when the new method comes out
 			try {
 				BufferedReader in = new BufferedReader(new InputStreamReader(this.getResource("items.csv")));
 				BufferedWriter out = new BufferedWriter(new FileWriter(itemFile));
 				String line = in.readLine();
 				while(line != null){
 					out.write(line + "\n");
 					line = in.readLine();
 				}
 				in.close();
 				out.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	/**
 	 * This initializes the instance variables in order to clean up the onEnable method
 	 */
 	private void initVariables() {
 		itemFile = new File(this.getDataFolder() + File.separator + "items.csv");
 		new ClearPlayerListener(this);
 		server = Bukkit.getServer();
 		preview = new PreviewCommand(this);
 		undo = new HashMap<String, ClearUndoHolder>();
 	}
 
 	/**
 	 * Checks for updates for the client and the items.csv
 	 */
 	private void checkForUpdates(){
 		String check = "http://dcp.wwsean08.com/check.php?id=";
 		String clientCheck = check + "ClearInv%20New";
 		String itemsCheck = check + "items%20new";
 		try {
 			//plugin
 			URL client = new URL(clientCheck);
 			BufferedReader buf = new BufferedReader(new InputStreamReader(client.openStream()));
 			String line = buf.readLine();
 			line = line.substring(0, line.indexOf("&"));
 			if(newer(VERSION, line))
 				log.info(PREFIX + " There is a new version of ClearInv available for download, you can get it at http://dcp.wwsean08.com/dl.php?id=ClearInv%20New&ver=latest");
 			//items.csv
 			client = new URL(itemsCheck);
 			buf = new BufferedReader(new InputStreamReader(client.openStream()));
 			line = buf.readLine();
 			line = line.substring(0, line.indexOf("&"));
 			if(newer(DBVersion, line))
 				log.info(PREFIX + " There is a new version of the items.csv available for download, you can get it at http://dcp.wwsean08.com/dl.php?id=items%20new&ver=latest");
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Compares the 2 versions and returns true if there is an update, otherwise returns false
 	 * @param current the version of the file on the server
 	 * @param check the version to compare it to
 	 * @return
 	 */
 	private boolean newer(String current, String check){
 		boolean result = false;
 		String[] currentVersion = current.split("\\.");
 		String[] checkVersion = check.split("\\.");
 		int i = Integer.parseInt(currentVersion[0]);
 		int j = Integer.parseInt(checkVersion[0]);
 		if(i>j)
 			result = false;
 		else if(i==j){
 			i = Integer.parseInt(currentVersion[1]);
 			j = Integer.parseInt(checkVersion[1]);
 			if(i>j)
 				result = false;
 			else if(i == j){
 				i = Integer.parseInt(currentVersion[2]);
 				j = Integer.parseInt(checkVersion[2]);
 				if(i >= j)
 					result = false;
 				else
 					result = true;
 			}else
 				result = true;
 		}else
 			result = true;
 		return result;
 	}
 }
