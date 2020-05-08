 package com.niccholaspage.nICs.ics;
 
 import org.bukkit.block.Sign;
 import org.bukkit.event.block.BlockRedstoneEvent;
 
 import com.niccholaspage.nICs.IC;
 import com.niccholaspage.nICs.nICs;
 
 public class N1001 implements IC {
 
 	@Override
 	public Boolean run(nICs plugin, BlockRedstoneEvent event) {
 		Boolean power = (event.getNewCurrent() > 0);
 		if (power){
 			Sign sign = (Sign)event.getBlock().getState();
			event.getBlock().getWorld().setStorm(sign.getLine(2).equalsIgnoreCase("on") ? true : false);
 		}
 		return null;
 	}
 
 	@Override
 	public String getName() {
 		return "WEATHER CHANGER";
 	}
 
 	@Override
 	public String canPlace(String[] lines) {
		if (lines[2].equalsIgnoreCase("on") || lines[2].equalsIgnoreCase("off")){
 			return null;
 		}else {
 			return "Line 3 must be 'on' or 'off.'";
 		}
 	}
 
 }
