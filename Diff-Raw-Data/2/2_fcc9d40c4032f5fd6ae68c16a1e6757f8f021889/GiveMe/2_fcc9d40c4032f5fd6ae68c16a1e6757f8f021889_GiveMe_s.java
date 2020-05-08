 package com.cianmcgovern.giveit;
 
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.ChatColor;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 
 /**
  * This class deals with the /giveme command
  *
  * @author cianmcgovern91@gmail.com
  * @version 1.3
  * 
  * 
  */
 
 public class GiveMe {
 	// Use the values defined in GiveIt
 	public String name = GiveIt.name;
 	public int amount = GiveIt.amount;
 	private IdChange idchange = new IdChange();
 	private final LogToFile log = new LogToFile();
 	// Carry out checks and give player requested items
 	public boolean giveme(CommandSender sender, String[] trimmedArgs){
 
 		if ((trimmedArgs[0] == null) || (trimmedArgs[1]== null)) {
 			return false;
 		}
 		Player player = (Player)sender;
 		PlayerInventory inventory = player.getInventory();
 		String item = idchange.idChange(trimmedArgs[0]);
 
 		// Check to see if the player requested an item that isn't allowed
 		if(GiveIt.prop.getProperty(item)==null){
 			player.sendMessage(ChatColor.DARK_RED+ "GiveIt: Sorry but it is not possible to spawn that item");
 			return true;
 		}
 
 		else if(GiveIt.prop.getProperty(item).contains(".")==true){
 			// Parse the player's name from the allowed.txt file
 			String in = GiveIt.prop.getProperty(item);
 			int position = in.indexOf(".");
 			amount = Integer.parseInt(in.substring(0, position));
 			name = in.substring(position+1,in.length());
 
 			if(Integer.parseInt(trimmedArgs[1])<=amount && name.equalsIgnoreCase(player.getName())){
 				amount = Integer.parseInt(GiveIt.prop.getProperty(item));
 				ItemStack itemstack = new ItemStack(Integer.valueOf(item));
 				itemstack.setAmount(Integer.parseInt(trimmedArgs[1]));
 				inventory.addItem(itemstack);
 				// Log the player's requested items to log file
 				log.writeOut(player, item, trimmedArgs[1]);
 				player.sendMessage(ChatColor.BLUE+ "GiveIt: Item added to your inventory");
 			}
 			// Send a message to the player telling them to choose a lower amount
 			else if(Integer.parseInt(trimmedArgs[1])>amount && name.equalsIgnoreCase(player.getName()))
 				player.sendMessage(ChatColor.DARK_RED+ "GiveIt: Sorry, please choose a lower amount");
 			else if(!name.equalsIgnoreCase(player.getName()))
 				player.sendMessage(ChatColor.DARK_RED+ "GiveIt: Sorry, but you are not allowed to spawn that item");
 			return true;
 		}
 		else if(GiveIt.prop.getProperty(item).contains(".")==false){
 			amount = Integer.parseInt(GiveIt.prop.getProperty(item));
 			ItemStack itemstack = new ItemStack(Integer.valueOf(item));
 			itemstack.setAmount(Integer.parseInt(trimmedArgs[1]));
 			inventory.addItem(itemstack);
 
 			player.sendMessage(ChatColor.BLUE+ "GiveIt: Item added to your inventory");
 			// Log the player's requested items to log file
 			log.writeOut(player, item, trimmedArgs[1]);
 			return true;
 		}
 		// Send a message to the player telling them to choose a lower amount
 		else if(Integer.parseInt(trimmedArgs[1])>amount){
 			player.sendMessage(ChatColor.DARK_RED+ "GiveIt: Sorry, please choose a lower amount");
 			return true;
 		}
		return false;
 	}
 }
