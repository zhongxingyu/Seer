 package com.niccholaspage.nICs.ics;
 
 import org.bukkit.event.block.BlockRedstoneEvent;
 
 import com.niccholaspage.nICs.nICs;
 
 public class N1001 implements IC {
 
 	@Override
 	public Boolean run(nICs plugin, BlockRedstoneEvent event) {
 		Boolean power = (event.getNewCurrent() > 0);
 		if (power){
			event.getBlock().getWorld().setThundering(true);
 		}
 		return null;
 	}
 
 	@Override
 	public String getName() {
 		return "WEATHER CHANGER";
 	}
 
 	@Override
 	public String canPlace(String[] lines) {
		return null;
 	}
 
 }
