 package me.sacnoth.bottledexp;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 
 public class BottledExpCommandExecutor implements CommandExecutor {
 
 	public BottledExpCommandExecutor(BottledExp plugin) {
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd,
 			String commandLabel, String[] args) {
 		if ((sender instanceof Player)) {
 			Player player = (Player) sender;
 			if (cmd.getName().equalsIgnoreCase("bottle") && BottledExp.checkPermission("bottle.use", player)) {
 				int currentxp = player.getTotalExperience();
 
 				if (args.length == 0) {
 					sender.sendMessage(BottledExp.langCurrentXP + ": "
 							+ currentxp + " XP!");
 				} else if (args.length == 1) {
 					int amount;
 					if (args[0].equals("max")) {
 						if (BottledExp.checkPermission("bottle.max", player)) {
 							amount = (int) Math.floor(currentxp / 10);
 						}
 						else
 						{
 							return false;
 						}
 					} else {
 						try {
 							amount = Integer.valueOf(args[0]).intValue();
 						} catch (NumberFormatException nfe) {
 							sender.sendMessage(ChatColor.RED
 									+ BottledExp.errAmount);
 							return false;
 						}
 					}
 					if (currentxp < amount * BottledExp.xpCost) {
 						sender.sendMessage(ChatColor.RED + BottledExp.errXP);
 					}
					else if (amount <= 0) {
						amount = 0;
 						sender.sendMessage(BottledExp.langOrder1 + " " + amount
 								+ " " + BottledExp.langOrder2);
 					}
 					else {
 						PlayerInventory inventory = player.getInventory();
 						ItemStack items = new ItemStack(384, amount);
 						inventory.addItem(items);
 						player.setTotalExperience(0);
 						player.setLevel(0);
 						player.setExp(0);
 						player.giveExp(currentxp - (amount * BottledExp.xpCost));
 						sender.sendMessage(BottledExp.langOrder1 + " " + amount
 								+ " " + BottledExp.langOrder2);
 					}
 				}
 				return true;
 			}
 		} else {
 			sender.sendMessage(ChatColor.RED + "You must be a player!");
 			return false;
 		}
 		return false;
 	}
 }
