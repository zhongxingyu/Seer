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
 import com.github.Holyvirus.Blacksmith.core.Tools.Sign.SignType;
 import com.github.Holyvirus.Blacksmith.core.Tools.Sign.SignValidator;
 import com.github.Holyvirus.Blacksmith.core.perms.Permission;
 
 public class BlockListener implements Listener{
 
 	private BlackSmith plugin;
 	private Permission pH;
 	
 	public BlockListener(BlackSmith plugin) {
 		this.plugin = plugin;
 		this.pH = plugin.getPermHandler().getEngine();
 	}
 	
 	@EventHandler
 	public void signChanged(SignChangeEvent event){
 		Player player = event.getPlayer();
 		SignType st = SignValidator.getType((Sign) event);
 		switch(st) {
 			case VALUE:
				if(pH.has(player, "blacksmith.place.value")){
 					event.setCancelled(true);
 			          player.sendMessage(ChatColor.DARK_RED + "You are not allowed to place a Blacksmith Value sign!");
 			          event.getBlock().breakNaturally();
 			          return;
 				}
 				player.sendMessage(ChatColor.GREEN + "Successfully placed a BlackSmith Value sign!");
 		          break;
 			case REPAIR:
				if(pH.has(player, "blacksmith.place.repair")){
 					event.setCancelled(true);
 			          player.sendMessage(ChatColor.DARK_RED + "You are not allowed to place a Blacksmith Repair sign!");
 			          event.getBlock().breakNaturally();
 			          return;
 				}
 				player.sendMessage(ChatColor.GREEN + "Successfully placed a BlackSmith Repair sign!");
 		          break;
 			case KILL:
				if(pH.has(player, "blacksmith.place.kill") || (!BlackSmith.getPlugin().getConfig().getBoolean("BlackSmith.global.debug"))){
 					event.setCancelled(true);
 			          player.sendMessage(ChatColor.DARK_RED + "You are not allowed to place a Blacksmith Kill sign!");
 			          event.getBlock().breakNaturally();
 			          return;
 				}
 				player.sendMessage(ChatColor.GREEN + "Successfully placed a BlackSmith Kill sign!");
 		          break;
 			case INVALID:
 		          player.sendMessage(ChatColor.RED + "You have placed an invalid sign. Type either ''Value'' or ''Repair'' into the second line!");
 		          event.getBlock().breakNaturally();
 		          return;
 		}
 	}
 	@EventHandler
 	public void blockBreak(BlockBreakEvent event){
 		Player player = event.getPlayer();
 		Block b = event.getBlock();
 		if(b.getState() instanceof Sign) {
 			Sign localSign = (Sign)b.getState();
 			SignType st = SignValidator.getType((Sign) b.getState());
 			player = event.getPlayer();
 			switch(st) {
 				case VALUE:
					if(pH.has(player, "blacksmith.remove.value")) {
 						event.setCancelled(true);
 				          player.sendMessage(ChatColor.DARK_RED + "You are not allowed to place a Blacksmith Value sign!");
 				          localSign.update();
 				          return;
 					}
 					player.sendMessage(ChatColor.GREEN + "Successfully removed BlackSmith Value sign!");
 			          break;
 				case REPAIR:
					if(pH.has(player, "blacksmith.remove.repair")) {
 						event.setCancelled(true);
 				          player.sendMessage(ChatColor.DARK_RED + "You are not allowed to place a Blacksmith Repair sign!");
 				          localSign.update();
 				          return;
 					}
 					player.sendMessage(ChatColor.GREEN + "Successfully removed BlackSmith Repair sign!");
 			          break;
 				case KILL:
					if(pH.has(player, "blacksmith.remove.kill") || (!BlackSmith.getPlugin().getConfig().getBoolean("BlackSmith.global.debug"))){
 						event.setCancelled(true);
 				          player.sendMessage(ChatColor.DARK_RED + "You are not allowed to place a Blacksmith Kill sign!");
 				          localSign.update();
 				          return;
 					}
 					player.sendMessage(ChatColor.GREEN + "Successfully removed BlackSmith Kill sign!");
 			          break;
 				case INVALID:
 					player.sendMessage(ChatColor.RED + "You have placed an invalid sign. Type either ''Value'' or ''Repair'' into the second line!");
 					localSign.update();
 			          return;
 			}
 		}
 	}
 }
