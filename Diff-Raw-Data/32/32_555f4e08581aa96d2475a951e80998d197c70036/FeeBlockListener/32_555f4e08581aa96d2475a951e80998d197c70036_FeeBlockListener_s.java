 package org.melonbrew.fee.listeners;
 
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.SignChangeEvent;
 import org.melonbrew.fee.Fee;
 import org.melonbrew.fee.Phrase;
 
 public class FeeBlockListener implements Listener {
 	private final Fee plugin;
 	
 	public FeeBlockListener(Fee plugin){
 		this.plugin = plugin;
 		
 		plugin.getServer().getPluginManager().registerEvents(this, plugin);
 	}
 	
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onSignChange(SignChangeEvent event){
 		Player player = event.getPlayer();
 		
 		if (!plugin.isSignFee(event.getLine(0))){
 			return;
 		}
 		
 		event.setLine(0, Phrase.SIGN_START.parse());
 		
 		if (!player.hasPermission("fee.sign")){
 			cancelSignChange(event);
 			
 			return;
 		}
 		
 		try {
 			Double.parseDouble(event.getLine(1));
 		} catch (NumberFormatException e){
 			cancelSignChange(event);
 			
 			Phrase.INVALID_AMOUNT.sendWithPrefix(player);
 			
 			return;
 		}
 		
		Block bottomBlock = event.getBlock().getRelative(BlockFace.DOWN);
 		
 		if (!plugin.containsSupportedBlock(bottomBlock.getType())){
			cancelSignChange(event);
 			
			Phrase.NOT_ABOVE_SUPPORTED_ITEM.sendWithPrefix(player);
 			
			return;
 		}
 		
 		if (!player.hasPermission("fee.sign.other") || event.getLine(2).isEmpty()){
 			event.setLine(2, player.getName());
 		}
 		
 		Phrase.CREATED_A_SIGN.sendWithPrefix(player);
 	}
 	
 	private void cancelSignChange(SignChangeEvent event){
 		event.setCancelled(true);
 		
 		Block block = event.getBlock();
 		
 		block.breakNaturally();
 		
 		
 	}
 }
