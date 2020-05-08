 package com.wwsean08.clear;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Server;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 
 public class PreviewCommand implements CommandExecutor{
 	Clear plugin;
 	Server server;
 	private HashMap<Player, ItemStack[]> originalInventory;
 	public PreviewCommand(Clear instance){
 		plugin = instance;
 		server = Bukkit.getServer();
 		originalInventory = new HashMap<Player, ItemStack[]>();
 	}
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		if(commandLabel.equalsIgnoreCase("preview")){
 			if(sender instanceof Player){
 				if(args.length == 0){
 					Player player = (Player) sender;
 					unpreview(player);
 				}
 				if(args.length == 1){
 					Player player = (Player) sender;
					Player affected = server.matchPlayer(args[0]).get(0);
					if(affected!=null){
						if(plugin.usesSP){
							if(player.hasPermission("clear.other")){
 								preview(player, affected);
 								ClearRunnable run = new ClearRunnable(this, player);
 								server.getScheduler().scheduleSyncDelayedTask(plugin, run, 6000);
 							}
						}else if(player.isOp()){
							preview(player, affected);
							ClearRunnable run = new ClearRunnable(this, player);
							server.getScheduler().scheduleSyncDelayedTask(plugin, run, 6000);
						}else{
							sender.sendMessage("You do not have permission to use this command");
							plugin.log.warning(plugin.PREFIX + player.getDisplayName() + " Attempted to preview another players inventory");
 						}
 					}
 				}
 			}
 			else{
 				if(args.length == 0){
 					sender.sendMessage(ChatColor.GRAY + "Error: I need a name of who to preview");
 				}else{
 					Player player = server.getPlayer(args[0]);
 					ArrayList<String> contains = preview(player);
 					sender.sendMessage(ChatColor.GRAY + "Here is the inventory of " + player.getDisplayName());
 					for(String s : contains){
 						sender.sendMessage(s);
 					}
 				}
 			}
 		}
 		else if(commandLabel.equalsIgnoreCase("unpreview") || commandLabel.equalsIgnoreCase("revert")){
 			if(sender instanceof Player){
 				Player player = (Player) sender;
 				unpreview(player);
 			}
 		}
 		return true;
 	}
 
 	/**
 	 * is used to display a players inventory from the server console
 	 * @param player the player whose inventory is being previewed
 	 * @return a list of items to display to the server console
 	 */
 	public ArrayList<String> preview(Player player) {
 		ArrayList<String> inventoryList = new ArrayList<String>();
 		ArrayList<Integer> itemNumbers = new ArrayList<Integer>();
 		HashMap<Integer, Integer> ammount = new HashMap<Integer, Integer>();
 		PlayerInventory inv = player.getInventory();
 		ItemStack[] contents = inv.getContents();
 		//loop thru the contents of the inventory in order to make our array list of items
 		for(ItemStack a : contents){
 			if(a != null){
 				Integer value = a.getTypeId();
 				if(itemNumbers.contains(Integer.valueOf(value))){
 					int i = ammount.get(value);
 					i += a.getAmount();
 					ammount.put(value, i);
 				}else{
 					itemNumbers.add(value);
 					ammount.put(value, a.getAmount());
 				}
 			}
 		}
 		//build an array list of strings with the ammount they have and the item
 		for(Integer i : itemNumbers){
 			StringBuilder out = new StringBuilder();
 			out.append(ammount.get(i) + "x ");
 			for(int j = 0; j < plugin.items.size(); j++){
 				if(plugin.items.get(j).getItem() == i.intValue()){
 					out.append(plugin.items.get(j).getOutput());
 					break;
 				}
 			}
 			inventoryList.add(out.toString());
 		}
 		return inventoryList;
 	}
 
 	/**
 	 * Allows an admin to preview a players inventory
 	 * @param previewer The admin that will be previewing the inventory
 	 * @param previewee The player whose inventory we want to preview
 	 */
 	public void preview(Player previewer, Player previewee){
 		ItemStack[] preview = previewee.getInventory().getContents();
 		if(!originalInventory.containsKey(previewer))
 			originalInventory.put(previewer, previewer.getInventory().getContents());
 		previewer.getInventory().setContents(preview);
 		ClearRunnable runner = new ClearRunnable(this, previewer);
 		server.getScheduler().scheduleSyncDelayedTask(plugin, runner, 6000);
 		previewer.sendMessage("You are now previewing " + previewee.getDisplayName());
 	}
 
 	/**
 	 * Restores the content of an admins inventory if they are previewing one
 	 * @param previewer the admin who is getting their inventory back
 	 */
 	public void unpreview(Player previewer){
 		if(originalInventory.containsKey(previewer)){
 			previewer.getInventory().clear();
 			previewer.getInventory().setContents(originalInventory.get(previewer));
 			originalInventory.remove(previewer);
 			previewer.sendMessage("Your inventory has been restored");
 		}
 	}
 }
