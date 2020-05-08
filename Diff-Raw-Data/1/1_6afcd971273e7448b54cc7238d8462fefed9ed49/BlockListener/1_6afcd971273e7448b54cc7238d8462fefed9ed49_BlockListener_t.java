 package com.github.Holyvirus.Blacksmith.Listeners;
 
 import org.bukkit.ChatColor;
 import org.bukkit.block.Block;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.SignChangeEvent;
 
 import com.github.Holyvirus.Blacksmith.BlackSmith;
 import com.github.Holyvirus.Blacksmith.core.config;
 import com.github.Holyvirus.Blacksmith.core.Tools.Sign.SignType;
 import com.github.Holyvirus.Blacksmith.core.Tools.Sign.SignValidator;
 import com.github.Holyvirus.Blacksmith.core.perms.Permission;
 
 public class BlockListener implements Listener{
 
 	private BlackSmith plugin;
 	private Permission pH;
 	config conf = config.Obtain();
 	
 	public BlockListener(BlackSmith plugin) {
 		this.plugin = plugin;
 		this.pH = plugin.getPermHandler().getEngine();
 	}
 	
 	@EventHandler
 	public void signChanged(SignChangeEvent event){
 		Player player = event.getPlayer();
 		if(SignValidator.isBlackSmithSign(event)){
 			SignType st = SignValidator.getType(event);
 			switch(st) {
 				case VALUE:
 					if(!pH.has(player, "blacksmith.place.value")){
 						event.setCancelled(true);
 						player.sendMessage(ChatColor.DARK_RED + "You are not allowed to place a Blacksmith Value sign!");
 						event.getBlock().breakNaturally();
 						return;
 					}
 					player.sendMessage(ChatColor.GREEN + "Successfully placed a BlackSmith Value sign!");
 					break;
 				case REPAIR:
 					if(!pH.has(player, "blacksmith.place.repair")){
 						event.setCancelled(true);
 						player.sendMessage(ChatColor.DARK_RED + "You are not allowed to place a Blacksmith Repair sign!");
 						event.getBlock().breakNaturally();
 						return;
 					}
 					player.sendMessage(ChatColor.GREEN + "Successfully placed a BlackSmith Repair sign!");
 					break;
 				case KILL:
 					if(!pH.has(player, "blacksmith.place.kill")){
 						player.sendMessage(ChatColor.DARK_RED + "You are not allowed to place a Blacksmith Kill sign!");
 						event.setCancelled(true);
 						event.getBlock().breakNaturally();
 						return;
 					}else if(!conf.getBoolean("BlackSmith.global.debug")){
 							player.sendMessage(ChatColor.DARK_RED + "Debug mode is not enabled!");
 							event.setCancelled(true);
 							event.getBlock().breakNaturally();
 							return;
 					}
 					player.sendMessage(ChatColor.GREEN + "Successfully placed a BlackSmith Kill sign!");
 					break;
 				case FREE:
 					if(!pH.has(player, "blacksmith.place.free")) {
 						event.setCancelled(true);
 						player.sendMessage(ChatColor.DARK_RED + "You are not allowed to place a Blacksmith Free sign!");
 						event.getBlock().breakNaturally();
 						return;
 					}
 					player.sendMessage(ChatColor.GREEN + "Successfully placed BlackSmith Free sign!");
 					break;
 				case DISMANTLE:
 					if(!pH.has(player, "blacksmith.place.dismantle")) {
 						event.setCancelled(true);
 						player.sendMessage(ChatColor.DARK_RED + "You are not allowed to place a Blacksmith Dismantle sign!");
 						event.getBlock().breakNaturally();
 						return;
 					}
 					player.sendMessage(ChatColor.GREEN + "Successfully placed BlackSmith Dismantle sign!");
 					break;
 				case ENCHANT:
 					if (!this.pH.has(player, "blacksmith.place.enchant")) {
 						event.setCancelled(true);
 						player.sendMessage(ChatColor.DARK_RED + "You are not allowed to place a Blacksmith Enchant sign!");
 						event.getBlock().breakNaturally();
 						return;
 					}
 					player.sendMessage(ChatColor.GREEN + "Successfully placed BlackSmith Enchant sign!");
					break;
 				case INVALID:
 					player.sendMessage(ChatColor.RED + "You have placed an invalid sign. Type either \"Value\", \"Repair\", \"Kill\", \"Free\" or \"Dismantle\" into the second line!");
 					event.getBlock().breakNaturally();
 					return;
 			}
 		}
 	}
 	@EventHandler
 	public void blockBreak(BlockBreakEvent event){
 		Player player = event.getPlayer();
 		Block b = event.getBlock();
 		if(b.getState() instanceof Sign) {
 			Sign localSign = (Sign)b.getState();
 			SignType st = SignValidator.getType(localSign);
 			player = event.getPlayer();
 			switch(st) {
 				case VALUE:
 					if(!pH.has(player, "blacksmith.remove.value")) {
 						event.setCancelled(true);
 						player.sendMessage(ChatColor.DARK_RED + "You are not allowed to remove a Blacksmith Value sign!");
 						localSign.update();
 						return;
 					}
 					player.sendMessage(ChatColor.GREEN + "Successfully removed the BlackSmith Value sign!");
 					break;
 				case REPAIR:
 					if(!pH.has(player, "blacksmith.remove.repair")) {
 						event.setCancelled(true);
 						player.sendMessage(ChatColor.DARK_RED + "You are not allowed to remove a Blacksmith Repair sign!");
 						localSign.update();
 						return;
 					}
 					player.sendMessage(ChatColor.GREEN + "Successfully removed the BlackSmith Repair sign!");
 					break;
 				case KILL:
 					if(!pH.has(player, "blacksmith.remove.kill")){
 						event.setCancelled(true);
 						player.sendMessage(ChatColor.DARK_RED + "You are not allowed to remove a Blacksmith Kill sign!");
 						localSign.update();
 						return;
 					}
 					player.sendMessage(ChatColor.GREEN + "Successfully removed the BlackSmith Kill sign!");
 					break;
 				case FREE:
 					if(!pH.has(player, "blacksmith.remove.free")) {
 						event.setCancelled(true);
 						player.sendMessage(ChatColor.DARK_RED + "You are not allowed to remove a Blacksmith Free sign!");
 						localSign.update();
 						return;
 					}
 					player.sendMessage(ChatColor.GREEN + "Successfully removed the BlackSmith Free sign!");
 					break;
 				case DISMANTLE:
 					if(!pH.has(player, "blacksmith.remove.dismantle")) {
 						event.setCancelled(true);
 						player.sendMessage(ChatColor.DARK_RED + "You are not allowed to remove a Blacksmith Dismantle sign!");
 						event.getBlock().breakNaturally();
 						return;
 					}
 					player.sendMessage(ChatColor.GREEN + "Successfully removed the BlackSmith Dismantle sign!");
 					break;
 				case ENCHANT:
 					if (!this.pH.has(player, "blacksmith.remove.enchant")) {
 						event.setCancelled(true);
 						player.sendMessage(ChatColor.DARK_RED + "You are not allowed to remove a Blacksmith Enchant sign!");
 						event.getBlock().breakNaturally();
 						return;
 					}
 					player.sendMessage(ChatColor.GREEN + "Successfully removed the BlackSmith Enchant sign!");
 					break;
 			}
 		}
 	}
 }
