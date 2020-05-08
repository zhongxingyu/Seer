 package me.furt.CraftEssence.commands;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 import me.furt.CraftEssence.CraftEssence;
 
 public class ItemCommand implements CommandExecutor {
 	CraftEssence plugin;
 
 	public ItemCommand(CraftEssence instance) {
 		this.plugin = instance;
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command command,
 			String label, String[] args) {
 		if (plugin.isPlayer(sender)) {
 			Player player = (Player) sender;
 			if (!CraftEssence.Permissions.has(player, "craftessence.item")) {
 				sender.sendMessage(ChatColor.YELLOW
 						+ "You to dont have proper permissions for that command.");
 				return true;
 			}
 		}
 		if (!plugin.isPlayer(sender))
 			return false;
 
 		if (args.length == 0)
 			return false;
 
 		Player player = (Player) sender;
 		String[] itemList = plugin.itemList();
 		int itemAmount = args.length < 2 ? 1 : Integer.parseInt(args[1]);
 		ItemStack stack = null;
 		String itemDur;
 		if (!args[0].contains(";")) {
 			for (String list : itemList) {
 				String[] listSplit = list.split("-");
 				String[] itemID = listSplit[0].split(";");
 				String[] nameList = listSplit[1].split(":");
				if(itemID[0].equalsIgnoreCase(args[0])) {
					stack = new ItemStack(Integer.parseInt(itemID[0]));
					stack.setAmount(itemAmount);
					player.getInventory().addItem(stack);
					sender.sendMessage(CraftEssence.premessage
							+ "You recieve " + itemAmount + " "
							+ stack.getType().toString().toLowerCase()
							+ "!");
					return true;
				}
 				for (String name : nameList) {
 					if (name.equalsIgnoreCase(args[0])) {
 						if (itemID.length == 2) {
 							itemDur = itemID[1];
 						} else {
 							itemDur = "0";
 						}
 						stack = new ItemStack(Integer.parseInt(itemID[0]));
 						stack.setAmount(itemAmount);
 						stack.setDurability(Short.parseShort(itemDur));
 						player.getInventory().addItem(stack);
 						sender.sendMessage(CraftEssence.premessage
 								+ "You recieve " + itemAmount + " "
 								+ stack.getType().toString().toLowerCase()
 								+ "!");
 						return true;
 					}
 				}
 			}
 			sender.sendMessage(CraftEssence.premessage + "Item: " + args[0]
 					+ " not found.");
 			return false;
 		} else {
 			for (String list : itemList) {
 				String[] listSplit = list.split("-");
 				String[] item = listSplit[0].split(";");
 				String[] itemID = args[0].split(";");
 				if (item[0].equalsIgnoreCase(itemID[0])) {
 					if (itemID.length == 2) {
 						itemDur = itemID[1];
 					} else {
 						itemDur = "0";
 					}
 					stack = new ItemStack(Integer.parseInt(itemID[0]));
 					stack.setAmount(itemAmount);
 					stack.setDurability(Short.parseShort(itemDur));
 					player.getInventory().addItem(stack);
 					sender.sendMessage(CraftEssence.premessage + "You recieve "
 							+ itemAmount + " "
 							+ stack.getType().toString().toLowerCase() + "!");
 					return true;
 				}
 			}
 			sender.sendMessage(CraftEssence.premessage + "Item: " + args[0]
 					+ " not found.");
 			return false;
 		}
 
 	}
 }
