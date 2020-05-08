 package me.furt.CraftEssence.commands;
 
 import me.furt.CraftEssence.CraftEssence;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 public class GiveCommand implements CommandExecutor {
 	CraftEssence plugin;
 
 	public GiveCommand(CraftEssence instance) {
 		this.plugin = instance;
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command command,
 			String label, String[] args) {
 		if (plugin.isPlayer(sender)) {
 			if (!CraftEssence.Permissions.has((Player) sender,
 					"craftessence.give")) {
 				sender.sendMessage(ChatColor.YELLOW
 						+ "You to dont have proper permissions for that command.");
 				return true;
 			}
 		}
 
 		if (args.length < 2)
 			return false;
 
 		if (plugin.playerMatch(args[0]) != null) {
 			int itemAmount = args.length < 3 ? 1 : Integer.parseInt(args[2]);
 			Player giveTo = plugin.getServer().getPlayer(args[0]);
 			String[] itemList = plugin.itemList();
 			String itemDur;
 			ItemStack stack = null;
 
 			if (!args[1].contains(";")) {
 				for (String list : itemList) {
 					String[] listSplit = list.split("-");
 					String[] itemID = listSplit[0].split(";");
 					String[] nameList = listSplit[1].split(":");
					if(itemID[0].equalsIgnoreCase(args[0])) {
						stack = new ItemStack(Integer.parseInt(itemID[0]));
						stack.setAmount(itemAmount);
						giveTo.getInventory().addItem(stack);
						sender.sendMessage(CraftEssence.premessage
								+ "Giving " + itemAmount + " "
								+ stack.getType().toString().toLowerCase()
								+ " to " + giveTo.getDisplayName() + ".");
						giveTo.sendMessage(ChatColor.GRAY
								+ "You got a gift!");
						return true;
					}
 					for (String name : nameList) {
 						if (name.equalsIgnoreCase(args[1])) {
 							if (itemID.length == 2) {
 								itemDur = itemID[1];
 							} else {
 								itemDur = "0";
 							}
 							stack = new ItemStack(Integer.parseInt(itemID[0]));
 							stack.setAmount(itemAmount);
 							stack.setDurability(Short.parseShort(itemDur));
 							giveTo.getInventory().addItem(stack);
 							sender.sendMessage(CraftEssence.premessage
 									+ "Giving " + itemAmount + " "
 									+ stack.getType().toString().toLowerCase()
 									+ " to " + giveTo.getDisplayName() + ".");
 							giveTo.sendMessage(ChatColor.GRAY
 									+ "You got a gift!");
 							return true;
 						}
 					}
 				}
 				sender.sendMessage(CraftEssence.premessage + "Item: " + args[1]
 						+ " not found.");
 				return false;
 			} else {
 				for (String list : itemList) {
 					String[] listSplit = list.split("-");
 					String[] item = listSplit[0].split(";");
 					String[] itemID = args[1].split(";");
 					if (item[0].equalsIgnoreCase(itemID[0])) {
 						if (itemID.length == 2) {
 							itemDur = itemID[1];
 						} else {
 							itemDur = "0";
 						}
 						stack = new ItemStack(Integer.parseInt(itemID[0]));
 						stack.setAmount(itemAmount);
 						stack.setDurability(Short.parseShort(itemDur));
 						giveTo.getInventory().addItem(stack);
 						sender.sendMessage(CraftEssence.premessage + "Giving "
 								+ itemAmount + " "
 								+ stack.getType().toString().toLowerCase()
 								+ " to " + giveTo.getDisplayName() + ".");
 						giveTo.sendMessage(ChatColor.GRAY + "You got a gift!");
 						return true;
 					}
 				}
 				sender.sendMessage(CraftEssence.premessage + "Item: " + args[1]
 						+ " not found.");
 				return false;
 			}
 		} else {
 			sender.sendMessage(CraftEssence.premessage
 					+ "Player is offline or not found.");
 			return true;
 		}
 	}
 
 }
