 package com.gtdclan.itemstowarp;
 
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockState;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.SignChangeEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.block.Sign;
 
 /**
  * The Class Listeners.
  */
 public class Listeners implements Listener {
 	
 	private Main plugin;
 	
 	public Listeners(Main instance) {
 		plugin = instance;
 	}
 	
 	@EventHandler
 	public void onInteract(PlayerInteractEvent event) {
 		Player player = event.getPlayer();
 		Action action = event.getAction();
 		Block block = event.getClickedBlock();
 		if (action == Action.RIGHT_CLICK_BLOCK && (block.getType() == Material.WALL_SIGN || block.getType() == Material.SIGN_POST)) {
 			if (player.hasPermission("itemstowarp.warp.sign")) {
 				BlockState blockState = block.getState();
 				Sign sign = (Sign) blockState;
 				String line0 = sign.getLine(0);
 				String line1 = sign.getLine(1);
 				if (line0.equalsIgnoreCase("[warp]")) {
 					player.performCommand("itw warp " + line1);
 					event.setCancelled(true);
 				}
 			}
 			else {
 				player.sendMessage(plugin.Util.parseColors("^redYou do not have permission to use sign warps."));
 			}
 		}
 	}
 	
 	@EventHandler
 	public void onSignChange(SignChangeEvent event) {
 		Player player = event.getPlayer();
 		String line0 = event.getLine(0);
 		String line1 = event.getLine(1);
 		String line3 = "";
 		if (line0.equalsIgnoreCase("[warp]")) {
 			if (player.hasPermission("itemstowarp.warp.sign.create")) {
 				DB data = plugin.database.getDatabase().find(DB.class).where().ieq("Warpname", line1).findUnique();
 				if (data == null) {
 					player.sendMessage("^redError: Can't find warp.");
 					event.setCancelled(true);
 				}
 				else {
 					if (data.getIsprivate()) {
 						line3 = "Private";
 					}
 					event.setLine(0, "[Warp]");
 					event.setLine(1, data.getWarpname());
 					event.setLine(2, data.getPlayername());
 					event.setLine(3, line3);
 				}
 			}
 			else {
 				player.sendMessage(plugin.Util.parseColors("^redYou do not have permission to create sign warps."));
 				event.setCancelled(true);
 			}
 		}
 	}
 	
 	@EventHandler
 	public void onBlockBreak(BlockBreakEvent event) {
 		Player player = event.getPlayer();
 		String playerName = player.getName();
 		Block block = event.getBlock();
 		if (block.getType() == Material.WALL_SIGN || block.getType() == Material.SIGN_POST) {
 			BlockState blockState = block.getState();
 			Sign sign = (Sign) blockState;
 			String line0 = sign.getLine(0);
 			
 			if (line0.equalsIgnoreCase("[warp]") && plugin.signProtected) {
 				String line2 = sign.getLine(2);
 				Boolean isOwner = playerName.equals(line2);
 				Boolean hasPerm = player.hasPermission("itemstowarp.warp.sign.removeany");
 				
				if (!isOwner || !hasPerm) {
 					event.setCancelled(true);
 					player.sendMessage(plugin.Util.parseColors("^redYou do not have permission to remove sign warps."));
 					sign.update();
 				}
 			}
 		}
 	}
 }
