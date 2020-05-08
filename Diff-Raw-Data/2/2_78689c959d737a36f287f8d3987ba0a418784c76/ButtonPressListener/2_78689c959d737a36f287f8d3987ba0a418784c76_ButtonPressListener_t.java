 package com.wolvencraft.prison.mines.events;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 
 import com.wolvencraft.prison.hooks.EconomyHook;
 import com.wolvencraft.prison.mines.MineCommand;
 import com.wolvencraft.prison.mines.PrisonMine;
 import com.wolvencraft.prison.mines.mine.DisplaySign;
 import com.wolvencraft.prison.mines.mine.Mine;
 import com.wolvencraft.prison.mines.util.Message;
 import com.wolvencraft.prison.mines.util.Util;
 
 public class ButtonPressListener implements Listener {
 
 	public ButtonPressListener(PrisonMine plugin) {
 		Message.debug("Initiating ButtonPressListener");
 		plugin.getServer().getPluginManager().registerEvents(this, plugin);
 	}
 	
 	@EventHandler
 	public void onButtonPress(PlayerInteractEvent event) {
 		if(event.isCancelled()) return;
 		
 		if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
 			
 			Block block = event.getClickedBlock();
 			
 			if(block.getType() == Material.STONE_BUTTON || block.getType() == Material.WOOD_BUTTON) {
 		        Message.debug("ButtonPressEvent passed");
 		        
 				Location locationAbove = block.getLocation();
 				locationAbove.setY(block.getLocation().getBlockY() + 1);
 				Block blockAbove = block.getWorld().getBlockAt(locationAbove);
 				
 				if(blockAbove.getType() == Material.WALL_SIGN) {
 					DisplaySign sign = DisplaySign.get(locationAbove);
 					if(sign != null && sign.getReset()) {
 						Mine curMine = Mine.get(sign.getParent());
 						Player player = event.getPlayer();
 						
 						if(!player.hasPermission("prison.mine.reset.sign." + curMine.getId()) && !player.hasPermission("prison.mine.reset.sign")) {
 							Message.sendError(player, PrisonMine.getLanguage().ERROR_ACCESS);
 							return;
 						}
 						
 						if(curMine.getCooldown() && curMine.getCooldownEndsIn() > 0 && !player.hasPermission("prison.mine.bypass.cooldown")) {
 							Message.sendError(Util.parseVars(PrisonMine.getLanguage().RESET_COOLDOWN, curMine));
 							return;
 						}
 						
						if(EconomyHook.usingVault() && sign.getPaid() && sign.getPrice() != -1) {
 							Message.debug("Withdrawing " + sign.getPrice() + " from " + player.getName());
 							if(!EconomyHook.withdraw(player, sign.getPrice())) return;
 							Message.debug("Successfully withdrawn the money. New balance: " + EconomyHook.getBalance(player));
 							Message.sendSuccess(player, Util.parseColors(PrisonMine.getLanguage().SIGN_WITHDRAW.replaceAll("<PRICE>", sign.getPrice() + "")));
 						} else Message.debug("Vault not found");
 						
 						MineCommand.RESET.run(curMine.getName());
 					}
 				}
 			}
 		}
 		
 		return;
 	}
 }
