 package com.bukkit.zand.blockhead;
 
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.player.PlayerChatEvent;
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 
 
 /**
  * Handle events for all Player related events
  * @author zand
  */
 public class BlockHeadPlayerListener extends PlayerListener {
 	public final BlockHead plugin;
 
     public BlockHeadPlayerListener(BlockHead instance) {
     	plugin = instance;
     }
     
     public void onPlayerCommand(PlayerChatEvent event) {
     	
     	if (event.isCancelled()) return;
     	
     	// remove the / and split to args
     	String[] args = event.getMessage().toLowerCase().substring(1).split(" ");
     	
     	if (plugin.isCommand(args[0])) {
     		Player player = event.getPlayer();
     		
     		if (args.length == 2) {	
     			// print the current version
     			if (args[1].equals("help")) {
     				ChatColor ch = ChatColor.LIGHT_PURPLE;
     				ChatColor cc = ChatColor.WHITE;
     				ChatColor cd = ChatColor.GOLD;
     				ChatColor ct = ChatColor.YELLOW;
     				player.sendMessage(ch + plugin.versionInfo);
     				player.sendMessage(cc + "/" + args[0] + " help " + cd + "-" + ct + " Displays this");
     				player.sendMessage(cc + "/" + args[0] + " version " + cd + "-" + ct + " Displays the current version");
     				player.sendMessage(cc + "/" + args[0] + " " + cd + "-" + ct + " Puts the currently held item on your head");
     				if (player.isOp()) {
 	    				player.sendMessage(cc + "/" + args[0] + " [item id]" + cd + "-" + ct + " Puts a block with item id on your head");
 	    				player.sendMessage(cc + "/" + args[0] + " [player] [item id]" + cd + "-" + ct + " Puts a block on another players head");
     				}
     			}
     			else if (args[1].startsWith("ver")) player.sendMessage(plugin.versionInfo);
     			else { // /hat [item id]
     				if (player.isOp()) placeOnHead(player, new ItemStack(Integer.valueOf(args[1]), 1));
     				else player.sendMessage(ChatColor.DARK_RED + "Your not allowed to use that command");
     			}
     			
     		} else if (args.length > 2) {
     			if (player.isOp()) {
     				// Get the stack
     				ItemStack stack = new ItemStack(Integer.valueOf(args[2]), 1);
     				
     				// Check the stack
     				if (stack.getTypeId() > 255 || stack.getTypeId() < 1) {
     					player.sendMessage(ChatColor.RED + "Not a valid block id");
     					return;
     				}
     				
     				// Look for Player
     				List<Player> players = plugin.getServer().matchPlayer(args[1]);
     				
     				// Player not Found
     				if (players.size() < 1) player.sendMessage(ChatColor.RED + "Could not find player");
     				
     				// More than 1 Player Found
     				else if (players.size() > 1) {
     					player.sendMessage(ChatColor.RED + "More than one player found");
     					String msg = "";
     					for (Player other : players) msg += " " + other.getName();
     					player.sendMessage(msg.trim());
     				}
     				
     				// Player Found
     				else {
     					Player other = players.get(0);
     					placeOnHead(other, stack);
     					player.sendMessage("Putting a block on " + other.getName() + "'s head.");
     				}
     			}
 				else player.sendMessage(ChatColor.DARK_RED + "Your not allowed to use that command");
     		
     		}else { // hat [player] [item id]
 	    		placeOnHead(player, player.getItemInHand());
     		}
     		
     		event.setCancelled(true);
     	}
     	
     }
     
     private boolean placeOnHead(Player player, ItemStack item) {
     	PlayerInventory inv = player.getInventory();
 		if (item.getAmount() < 1) {
 			player.sendMessage(ChatColor.RED + "You have no item in your hand");
 			return false;
 		}
 		
 		int id = item.getTypeId();
 		if (id < 1 || id > 255) {
			player.sendMessage(ChatColor.RED + "You can put that item on your head");
 			return false;
 		}
 		
 		ItemStack helmet = inv.getHelmet();
 		
 		
 		inv.setHelmet(new ItemStack(id, 1));
 		if (item.getAmount() > 1) item.setAmount(item.getAmount()-1);
 		else inv.remove(item);
 		
 		// put what was in the helmet spot back
 		if (helmet.getAmount() > 0) {
 			HashMap<Integer, ItemStack> leftover = inv.addItem(helmet);
 			if (!leftover.isEmpty()) {
 				player.sendMessage("Was unble to put the old headhear away, droping it at your feet");
 				
 				// Drop the stacks
 				for (Map.Entry<Integer, ItemStack> e : leftover.entrySet()) 
 					player.getWorld().dropItem(player.getLocation(), e.getValue());
 			}
 		}
 		 
 		
 		
 		player.sendMessage("Enjoy your new Headgear");
 		return true;
     }
     
 }
 
