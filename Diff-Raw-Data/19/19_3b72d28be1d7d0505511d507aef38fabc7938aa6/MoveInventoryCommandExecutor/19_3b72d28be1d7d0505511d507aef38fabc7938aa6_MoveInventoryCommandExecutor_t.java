 package me.babarix.MoveInventory;
 
 import java.util.HashMap;
 
 import org.bukkit.block.Block;
 import org.bukkit.block.Chest;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 
 public class MoveInventoryCommandExecutor implements CommandExecutor {
 
 	private final MoveInventory plugin;
 
 	public MoveInventoryCommandExecutor(MoveInventory plugin) {
 		this.plugin = plugin;
	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command command,
 			String label, String[] args) {
 		Player player;
 		Block tblock;
 		Chest chest;
 		Inventory ichest, iplayer;
 		HashMap<Integer, ItemStack> leftovers;
 
 		if (label.equalsIgnoreCase("mi")) {
 
 			if (sender instanceof Player) {
 				player = (Player) sender;
 			} else {
				return true;
			}
			if (args.length < 1) {
				help(player);
 			}
 			tblock = player.getTargetBlock(null, 30);
 			if (tblock.getTypeId() != 54 || tblock == null) {
 				player.sendMessage("Your Target is no chest.");
 				return true;
 			}
 
 			chest = (Chest) tblock.getState();
 			ichest = chest.getInventory();
 			iplayer = player.getInventory();
			if (args[0].equalsIgnoreCase("tc")) {
 				if (ichest.firstEmpty() == -1) {
 					player.sendMessage("The Target is alredy full.");
 					return true;
 				}
 				for (ItemStack item : iplayer.getContents()) {
 					if (item != null && ichest.firstEmpty() != -1) {
 						leftovers = ichest.addItem(item);
 						if (leftovers.size() == 0) {
 							report(item.getAmount(), item.getType().name(),
 									player);
 							iplayer.removeItem(item);
 						} else {
 							iplayer.removeItem(item);
 							iplayer.addItem(leftovers.get(0));
 							report(item.getAmount()
 									- leftovers.get(0).getAmount(), item
 									.getType().name(), player);
 						}
 					}
 
 				}
 				return true;
 			} else if (args[0].equalsIgnoreCase("tp")) {
 				if (iplayer.firstEmpty() == -1) {
 					player.sendMessage("The Target is alredy full.");
 					return true;
 				}
 				for (ItemStack item : ichest.getContents()) {
 					if (item != null && iplayer.firstEmpty() != -1) {
 
 						leftovers = iplayer.addItem(item);
 						if (leftovers.size() == 0) {
 							ichest.removeItem(item);
 							report(item.getAmount(), item.getType().name(),
 									player);
 						} else {
 							ichest.removeItem(item);
 							ichest.addItem(leftovers.get(0));
 							report(item.getAmount()
 									- leftovers.get(0).getAmount(), item
 									.getType().name(), player);
 						}
 					}
 
 				}
 				return true;
 			} else if (args[0].equalsIgnoreCase("v")) {
 				toggleVerbose(player);
 			} else {
 				help(player);
 				return true;
 			}
 		}
 		return true;
 	}
 
 	public void toggleVerbose(Player player) {
 
 		if (plugin.verbose.containsKey(player.getName())) {
 			if (plugin.verbose.get(player.getName())) {
 				plugin.verbose.put(player.getName(), false);
 				player.sendMessage("Reporting disabled");
 			} else {
 				plugin.verbose.put(player.getName(), true);
 				player.sendMessage("Reporting enabled");
 			}
 		} else {
 			plugin.verbose.put(player.getName(), true);
 			player.sendMessage("Reporting enabled");
 		}
 	}
 
 	public void report(int amout, String name, Player player) {
 		if (plugin.verbose.containsKey(player.getName())) {
 			if (plugin.verbose.get(player.getName())) {
 				player.sendMessage("Moved " + amout + " of " + name);
 			}
 		} else {
 			plugin.verbose.put(player.getName(), false);
 		}
 	}
 
 	public void help(Player player) {
 		player.sendMessage("usage: /mi [tc|tp|v]");
 		player.sendMessage("tc: Moves your inventory into target chest.");
 		player.sendMessage("tp: Moves target chests inventory into your inventory.");
 		player.sendMessage("v:  Turns move message on/off");
 	}
 }
