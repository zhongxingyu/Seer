 package com.github.snnappie.armorhider.commands;
 
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.github.snnappie.armorhider.ArmorHider;
 import com.github.snnappie.armorhider.ArmorHider.ArmorPiece;
 
 
 public class HideCommand implements CommandExecutor {
 
 	private ArmorHider plugin;
 
 	public HideCommand(ArmorHider plugin) {
 		this.plugin = plugin;
 	}
 
 	// TODO review better ways to handle this
 	private static String[][] armorStrings = { 
 		{ "all" }, // 0
 		{ "hat", "head", "helmet" }, // 1
 		{ "chest", "body", "chestpiece", "chestplate" }, // 2
 		{ "legs", "leggings" }, // 3
 		{ "boots", "shoes", "feet" } // 4
 	};
 	
 	
 	public static enum CommandType {
 		HIDEARMOR, SHOWARMOR, HIDEENCHANT, SHOWENCHANT;
 	}
 
 	
 	public static ArmorPiece getArmorPieceFromString(String piece) {
 		
 		for (int i = 0; i < armorStrings.length; i++) {
 			for (String pieceString : armorStrings[i]) {
 				if (pieceString.equalsIgnoreCase(piece)) {
 					switch (i) {
 					case 0:
 						return ArmorPiece.ALL;
 					case 1:
 						return ArmorPiece.HAT;
 					case 2:
 						return ArmorPiece.CHEST;
 					case 3:
 						return ArmorPiece.LEGS;
 					case 4:
 						return ArmorPiece.BOOTS;
 					}
 				}
 			}
 		}
 		
 		return null;
 	}
 
 	
 	private void run(CommandType command, Player player, ArmorPiece piece) {
 		
 		switch (command) {
 		case HIDEARMOR:
 			plugin.hideArmor(player, piece);
 			break;
 		case SHOWARMOR:
 			plugin.showArmor(player, piece);
 			break;
 		case HIDEENCHANT:
 			plugin.hideEnchantments(player, piece);
 			break;
 		case SHOWENCHANT:
 			plugin.showEnchantments(player, piece);
 			break;
 		}
 		
 		informPlayer(player, command);
 	}
 	
 	public static void informPlayer(Player player, CommandType command) {
 		switch (command) {
 		case HIDEARMOR:
 			player.sendMessage(ChatColor.RED + "Armor hidden!");
 			break;
 		case SHOWARMOR:
 			player.sendMessage(ChatColor.RED + "Armor revealed!");
 			break;
 		case HIDEENCHANT:
 			player.sendMessage(ChatColor.RED + "Enchantments hidden!");
 			break;
 		case SHOWENCHANT:
 			player.sendMessage(ChatColor.RED + "Enchantments revealed!");
 			break;
 		}
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
 
 		Player player = null;
 		if (sender instanceof Player) {
 			player = (Player) sender;
 		} else {
 			sender.sendMessage("Must be a player!");
 			return false;
 		}
 		
 		// TODO change this if necessary
		if (args.length >= 3 || args.length <= 0) {
 			player.sendMessage(ChatColor.RED + "Incorrect arguments!");
 			return false;
 		}
 		
 		
 		CommandType command = null;
 		ArmorPiece piece = null;
 		if (cmd.getName().equalsIgnoreCase("hidearmor")) {
 			command = CommandType.HIDEARMOR;
 		} else if (cmd.getName().equalsIgnoreCase("showarmor")) {
 			command = CommandType.SHOWARMOR;
 		} else if (cmd.getName().equalsIgnoreCase("hideenchant")) {
 			command = CommandType.HIDEENCHANT;
 		} else if (cmd.getName().equalsIgnoreCase("showenchant")) {
 			command = CommandType.SHOWENCHANT;
 		}
 		
 		// trying to hide one piece of armor
		if (args.length == 1) {
 			piece = getArmorPieceFromString(args[0]);
 			if (piece == null) {
 				player.sendMessage(ChatColor.RED + "Invalid argument: " + args[0]);
 				return false;
 			}
 			
 			if (!checkHidePerms(player, piece, command)) {
 				return true;
 			}
 			
 		} else if (args.length == 2) {
 			// player is hiding all armor except for one piece
 			if (args[0].equalsIgnoreCase("all") && args[1].startsWith("-")) {
 				piece = getArmorPieceFromString(args[1].substring(1));
 				if (piece == null) {
 					player.sendMessage(ChatColor.RED + "Invalid argument: " + args[1]);
 					return false;
 				}
 				
 				if (!checkHidePerms(player, ArmorPiece.ALL, command)){
 					return true;
 				}
 				
 				// special case: hiding all but one piece
 				switch (command) {
 				case HIDEARMOR:
 					plugin.hideArmor(player, ArmorPiece.ALL);
 					plugin.showArmor(player, piece);
 					break;
 				case SHOWARMOR:
 					plugin.showArmor(player, ArmorPiece.ALL);
 					plugin.hideArmor(player, piece);
 					break;
 				case HIDEENCHANT:
 					plugin.hideEnchantments(player, ArmorPiece.ALL);
 					plugin.showEnchantments(player, piece);
 					break;
 				case SHOWENCHANT:
 					plugin.showEnchantments(player, ArmorPiece.ALL);
 					plugin.hideEnchantments(player, piece);
 					break;
 				}
 				
 				informPlayer(player, command);
 				return true;
 			}
 			
 			// player subtracted from something that wasn't 'all'
 			if (args[1].startsWith("-")) {
 				return false;
 			}
 			
 			// player is trying to hide another players armor
 			if (!player.hasPermission(ArmorHider.PERM_OTHER)) {
 				player.sendMessage(ChatColor.RED + "You do not have permission to hide other players armor!");
 				return true;
 			}
 			
 			player = Bukkit.getPlayer(args[0]);
 			if (player == null) {
 				sender.sendMessage(ChatColor.RED + args[0] + " is not online");
 				return true;
 			}
 			
 			piece = getArmorPieceFromString(args[1]);
 			if (piece == null) {
 				sender.sendMessage(ChatColor.RED + "Invalid argument: " + args[1]);
 				return false;
 			}
 			
 			if (!checkHidePerms(player, piece, command)) {
 				return true;
 			}
 			
 		}
 		
 		// if we got here, run the command
 		run(command, player, piece);
 		return true;
 	}
 	
 	private boolean checkHidePerms(Player player, ArmorPiece piece, CommandType command) {
 		// handle permissions for hide enchantments
 		if (command == CommandType.SHOWENCHANT || command == CommandType.HIDEENCHANT) {
 			if (!player.hasPermission(ArmorHider.PERM_ENCHANT)) {
 				player.sendMessage(ChatColor.RED + "You do not have permissions to hide enchantments!");
 				return false;
 			}
 		}
 		// handle permissions for individual pieces
 		switch (piece) {
 		case HAT:
 			if (!player.hasPermission(ArmorHider.PERM_HAT) && !player.hasPermission(ArmorHider.PERM_ALL)) {
 				player.sendMessage(ChatColor.RED + "You do not have permissions to hide your hat!");
 				return false;
 			}
 			break;
 		case CHEST:
 			if (!player.hasPermission(ArmorHider.PERM_CHEST) && !player.hasPermission(ArmorHider.PERM_ALL)) {
 				player.sendMessage(ChatColor.RED + "You do not have permissions to hide your chestplate!");
 				return false;
 			}
 			break;
 		case LEGS:
 			if (!player.hasPermission(ArmorHider.PERM_LEGS) && !player.hasPermission(ArmorHider.PERM_ALL)) {
 				player.sendMessage(ChatColor.RED + "You do not have permissions to hide your leggings!");
 				return false;
 			}
 			break;
 		case BOOTS:
 			if (!player.hasPermission(ArmorHider.PERM_BOOTS) && !player.hasPermission(ArmorHider.PERM_ALL)) {
 				player.sendMessage(ChatColor.RED + "You do not have permissions to hide your boots!");
 				return false;
 			}
 			break;
 		case ALL:
 			if (!player.hasPermission(ArmorHider.PERM_ALL)) {
 				player.sendMessage(ChatColor.RED + "You do not have permissions to hide all your armor!");
 				return false;
 			}
 			break;
 		}
 		
 		return true;
 	}
 }
