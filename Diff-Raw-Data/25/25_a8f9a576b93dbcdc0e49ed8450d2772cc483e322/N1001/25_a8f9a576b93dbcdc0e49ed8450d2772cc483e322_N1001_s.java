 package com.niccholaspage.nICs.ics;
 
import org.bukkit.World;
 import org.bukkit.block.Sign;
 import org.bukkit.event.block.BlockRedstoneEvent;
 
 import com.niccholaspage.nICs.nICs;
 
 public class N1001 implements IC {
 
 	@Override
 	public Boolean run(nICs plugin, BlockRedstoneEvent event) {
 		Sign sign = (Sign)event.getBlock().getState();
		World world = sign.getWorld();
 		Boolean power = (event.getNewCurrent() > 0);
 		if (power){
 			if (sign.getLine(2).equalsIgnoreCase("rain")){
				sign.getBlock().
 			}else if (sign.getLine(2).equalsIgnoreCase("snow")){
 				
 			}
 		}
 		return null;
 	}
 
 	@Override
 	public String getName() {
 		return "WEATHER CHANGER";
 	}
 
 	@Override
 	public String canPlace(String[] lines) {
 		if ((!(lines[2].equalsIgnoreCase("rain"))) && !((lines[2].equalsIgnoreCase("snow"))))
 			return "Line 3 must equal 'rain' or 'snow'";
 		return null;
 	}
 
 }
