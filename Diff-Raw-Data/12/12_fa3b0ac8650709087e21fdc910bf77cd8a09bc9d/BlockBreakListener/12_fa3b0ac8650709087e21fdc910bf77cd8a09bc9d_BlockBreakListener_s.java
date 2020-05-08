 package com.github.dreadslicer.tekkitrestrict.listeners;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 
 import com.github.dreadslicer.tekkitrestrict.Log;
 import com.github.dreadslicer.tekkitrestrict.TRLimiter;
 import com.github.dreadslicer.tekkitrestrict.tekkitrestrict;
 
 public class BlockBreakListener implements Listener{
 	/** @return <b>True</b> if id < 8 or id = 12, 13, 17, 24, 35, 44, 98 or 142. <b>False</b> otherwise. */
 	private static boolean Exempt(int id){
 		return (id < 8 || id == 12 || id == 13 || id == 17 || id == 24 || id == 35 || id == 44 || id == 98 || id == 142);
 	}
 	
 	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
 	public void onBlockBreak(BlockBreakEvent event) {
 		if (Exempt(event.getBlock().getTypeId())) return;
 		
 		Player player = event.getPlayer();
 		if (player == null) return;
 		String pname = player.getName();
 		if (pname.equalsIgnoreCase("[BuildCraft]") || pname.equalsIgnoreCase("[RedPower]")) return;
 		try {
 			String blockPlayerName = TRLimiter.getPlayerAt(event.getBlock());
 			if (blockPlayerName != null) {
 				TRLimiter il = TRLimiter.getLimiter(blockPlayerName);
 				il.checkBreakLimit(event);
 			}
 		} catch(Exception ex){
 			tekkitrestrict.log.warning("Error in onBlockBreak, Block limiter!");
 			Log.Exception(ex, false);
 		}
 	}
 }
