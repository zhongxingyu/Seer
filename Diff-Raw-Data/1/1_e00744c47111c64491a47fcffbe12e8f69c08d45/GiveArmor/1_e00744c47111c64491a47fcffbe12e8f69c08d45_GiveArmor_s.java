 package me.Joeyy.Armor; 
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 public class GiveArmor implements CommandExecutor{
 	Armor plugin;
 
 	public GiveArmor(Armor instance) {
 		this.plugin = instance;
 	}
 
	@Override
 	public boolean onCommand(CommandSender sender, Command command,
 			String label, String[] args) {
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
 							sender.sendMessage(Armor.premessage
 									+ "Giving " + itemAmount + " "
 									+ stack.getType().toString().toLowerCase()
 									+ " to " + giveTo.getDisplayName() + ".");
 							giveTo.sendMessage(ChatColor.GRAY
 									+ "You got a gift!");
 							return true;
 						}
 					}
 				}
 				sender.sendMessage(Armor.premessage + "Item: " + args[1]
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
 						sender.sendMessage(Armor.premessage + "Giving "
 								+ itemAmount + " "
 								+ stack.getType().toString().toLowerCase()
 								+ " to " + giveTo.getDisplayName() + ".");
 						giveTo.sendMessage(ChatColor.GRAY + "You got a gift!");
 						return true;
 					}
 				}
 				sender.sendMessage(Armor.premessage + "Item: " + args[1]
 						+ " not found.");
 				return false;
 			}
 		} else {
 			sender.sendMessage(Armor.premessage
 					+ "Player is offline or not found.");
 			return true;
 		}
 	}
 
 }
 
 
