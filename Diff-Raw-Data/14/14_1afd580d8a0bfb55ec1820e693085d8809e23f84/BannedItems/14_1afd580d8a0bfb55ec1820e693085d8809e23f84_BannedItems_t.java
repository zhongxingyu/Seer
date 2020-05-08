 /**
 * BannedItems - v1.4.1
  * 
  * Simple plugin that overrides the default /give server command.
  * Only items not in the ignored-items.txt list are allowed to spawn
  * with the command.
  * 
  * This plugin also spawn items in the inventory instead of spawning
  * it in front of the user.
  * The requested amount of stack size is also respected and given as
  * requested.
  * 
  * No license this time ;) Have fun!
  * 
  * @author Kozie <flamefingers at gmail dot com>
  */
 
 package org.kozie87;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.util.ArrayList;
 import org.bukkit.Server;
 import org.bukkit.Material;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class BannedItems extends JavaPlugin {
 
 	// Set local variables
 	private ArrayList<String> ignoreList;
 	private File folder;
 	private String fileName = "ignored-items.txt";
 	
 	/**
 	 * Function to run when the plugin gets enabled
 	 * This function will initiate the list from the file.
 	 */
 	public void onEnable() {
 		
 		// Set default values
 		folder = getDataFolder();
 		ignoreList = new ArrayList<String>();
 		
 		// Read out the ignored items list, if possible
 		loadIgnoreList();
 		System.out.println("BannedItems has been loaded!");
 	}
 	
 	/**
 	 * Function to execute when the current plugin gets disabled
 	 */
 	public void onDisable() {
 		
 		// Run some interesting things here
 	}
 	
 	/**
 	 * Function to catch commands given by the player.
 	 * This function will catch the /give function.
 	 * @param sender The player that inits the command
 	 * @param cmd The command object that the player requests
 	 * @param commandLabel Just the label of the command, without the slash
 	 * @param args Remaining arguments that were passed after the command
 	 * @return boolean
 	 */
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		
 		// Get the server
 		Server server = sender.getServer();
 		
 		// Catch the 'give' command
 		if ((cmd.getName().equalsIgnoreCase("give") && args.length > 1) || (cmd.getName().equalsIgnoreCase("item") && args.length > 0)) {
 			
 			// Convert the sender to a Player object and set a default target player
 			Player senderPlayer = (Player) sender;
 			Player player = (Player) sender;
 			
 			// Set default amount of one
 			int itemId, amount = 1;
 			
 			// Give to a player or to self?
 			if (cmd.getName().equalsIgnoreCase("give")) {
 				
 				// Get the player and the value id the user is requesting
 				player = server.getPlayer(args[0]);
 				
 				// Get the item id
 				try {
 					itemId = Integer.parseInt(args[1]);
 				} catch (Exception e) {
 					
 					senderPlayer.sendMessage("That number is not valid.");
 					return true;
 				}
 				
 				// Check if there's an amount requested
 				if (args.length > 2 && Integer.parseInt(args[2]) > 0) {
 					amount = Integer.parseInt(args[2]);
 				}
 			} else {
 				
 				// Get the item id
				try {
					itemId = Integer.parseInt(args[0]);
				} catch (Exception e) {
					
					senderPlayer.sendMessage("That number is not valid.");
					return true;
				}
 				
 				// Check if there's an amount requested
 				if (args.length > 1 && Integer.parseInt(args[1]) > 0) {
 					amount = Integer.parseInt(args[1]);
 				}
 			}
 			
 			// Check if the sender has the rights to use the command
 			//if (!sender.isOp()) {
 			if (!senderPlayer.hasPermission("BannedItems.give")) {
 				
 				//sender.sendMessage("You have to be an admin/op to use this command");
 				sender.sendMessage("You do not have permission to use this command");
 				return true;
 			}
 			
 			// Check if the player was found
 			if (player == null) {
 				sender.sendMessage("The requested player is not connected.");
 				return true;
 			}
 			
 			return this.giveItem(senderPlayer, player, itemId, amount);
 		}
 		
 		return false;
 	}
 	
 	/**
 	 * Function that hands out the candy :)
 	 * @param sender The player that hands out the candy
 	 * @param player Target player that gets the item(s)
 	 * @param itemId The id of the item that we want to give
 	 * @param amount The amount of the stack to hand over
 	 * @return boolean
 	 */
 	private boolean giveItem(Player sender, Player player, Integer itemId, Integer amount) {
 		
 		// Check if the item is allowed
		if (!this.isIgnored(itemId)) {
 			
 			try {
 				// Check excistence of the item
 				Material material = Material.getMaterial(itemId);
 				if (material.equals(false)) {
 					
 					sender.sendMessage("That item does not exists");
 					return true;
 				}
 				
 				// Build up the item stack
 				ItemStack itemStack = new ItemStack(itemId);
 				itemStack.setAmount(amount);
 				
 				// Eventually hand out the candy ^_^
 				player.getInventory().addItem(itemStack);
 				player.updateInventory();
 				
 				// Send message to the the users who are involved
 				if (sender.getName() == player.getName()) {
 				
 					sender.sendMessage("You gave yourself item "+itemId);
 				} else {
 					
 					sender.sendMessage("Item "+itemId+" given to "+player.getName());
 					player.sendMessage(sender.getName()+" gave you item "+itemId);
 				}
 			} catch (Exception e) {
 				
 				sender.sendMessage("Something went wrong, maybe a non existing itemId");
 				return true;
 			}
 			
 			return true;
 		} else {
 			
 			// Send message to the player
 			sender.sendMessage("Sorry but that item is restricted");
 			return true;
 		}
 	}
 	
 	/**
 	 * Function that does the whole loading process of the list file.
 	 * The file is stored as an array
 	 * @return boolean
 	 */
 	private boolean loadIgnoreList() {
 		
 		try {
 			// Create the folder if it does not exist
 			if (!folder.exists()) {
 				folder.mkdir();
 			}
 			
 			// Open the list file
 			File list = new File(folder.getAbsolutePath() + File.separator + fileName);
 			if (list.exists()) {
 				
 				ignoreList.clear();
 				BufferedReader reader = new BufferedReader(new FileReader(list));
 				String line = reader.readLine();
 				
 				// Keep on reading the file until end is reached
 				while (line != null) {
 					ignoreList.add(line);
 					line = reader.readLine();
 				}
 				
 				// Close the reader
 				reader.close();
 				
 				System.out.println("BannedItems: Item ignore list succesfully loaded");
 			} else {
 				
 				System.out.println("BannedItems: Notice! no item ignore list exists.");
 			}
 		} catch (Exception e) {
 			
 			System.out.println("error: "+e);
 			return false;
 		}
 		
 		return true;
 	}
 	
 	/**
 	 * Function to check the existence of an item id
 	 * in the ignore list
 	 * @param itemId
 	 * @return boolean
 	 */
 	private boolean isIgnored(int itemId) {
 		
 		for (String listedItemId : ignoreList) {
 			if (itemId == Integer.parseInt(listedItemId)) {
 				return true;
 			}
 		}
 		
 		return false;
 	}
 }
