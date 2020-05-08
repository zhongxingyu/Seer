 package com.sosmedia.communitychest;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import org.bukkit.ChatColor;
 import org.bukkit.block.Block;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 
 public class CommandListener implements CommandExecutor {
 
 	private CommunityChestMain plugin;
 	private ArrayList<Player> pending = new ArrayList<Player>();
 	private String name;
 	
 	public CommandListener(CommunityChestMain plugin) {
 		this.plugin = plugin;
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
 		Player player = null;
 
 		if(sender instanceof Player) {
 			player = (Player) sender;
 		}
 		if(player != null) {
			if((label.equalsIgnoreCase("cc") || label.equalsIgnoreCase("communitychest")) && args.length > 0) {
 
 				HashMap<String, CardboardBox[]> chests = plugin.loadFile1();
 				String[] keys = (String[])( chests.keySet().toArray( new String[chests.size()] ) );
 				if(args[0].equalsIgnoreCase("list")) {
 					player.sendMessage(ChatColor.GREEN + "Community Chest list:");
					if(keys.length == 0) {
						player.sendMessage("No community chests found");
					} else {
 					player.sendMessage(keys);
					}
 					return true;
 				} 
 				if(args[0].equalsIgnoreCase("delete") && args.length > 1) {
 					name = args[1];
 					for(int i = 2; i < args.length; i++) {
 							name += " " + args[i];
 					}
 			
 					if(chests.containsKey(name)) {
 						pending.add(player);
 						player.sendMessage(ChatColor.YELLOW + "Deleting chest " + ChatColor.GREEN + name + ChatColor.YELLOW + " will permanently remove ALL instances of it's inventory. Are you sure you want to continue?");
 						player.sendMessage("To continue, type: " + ChatColor.DARK_RED + "/cc yes");
 						player.sendMessage("To abort, type: " + ChatColor.GREEN + "/cc no");
 						return true;
 						
 					} else {
 						player.sendMessage(ChatColor.RED + "Community chest name " + ChatColor.GREEN + name + ChatColor.RED + " was not found");
 						return true;
 					}
 				} 
 				if(args[0].equalsIgnoreCase("yes") && pending.contains(player)) {
 					HashMap<String, ArrayList<DeconBlock>> signs = plugin.loadFile2();
 					ArrayList<DeconBlock> list = signs.get(name);
 					for(int i = 0; i < list.size(); i++) {
 						DeconBlock deconBlock = list.get(i);
 						Block block = deconBlock.getBlock();
 						plugin.cl.emptyChest(block);
 						block.breakNaturally();
 					}						
 					chests.remove(name);
 					plugin.saveFile(chests, plugin.fileInventory);		
 					player.sendMessage(ChatColor.GREEN + name + ChatColor.YELLOW + " has been removed");
 					pending.remove(player);
 					name = null;
 					return true;
 				}
 				if(args[0].equalsIgnoreCase("no") && pending.contains(player)) {
 					player.sendMessage(ChatColor.YELLOW + "Community chest " + ChatColor.GREEN + name + ChatColor.YELLOW + " has not been removed" );
 					pending.remove(player);
 					name = null;
 					return true;
 				}
 			} 
 		}else {
 			sender.sendMessage("This command is for players only");
 			return true;
 		}
 		return false;
 	}
 }
