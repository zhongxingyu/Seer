 package com.niccholaspage.nICs.ics;
 
 import org.bukkit.World;
 import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
 import org.bukkit.event.block.BlockRedstoneEvent;
 
 import com.niccholaspage.nICs.nICs;
 
 public class MC1237 implements IC {
 	public Boolean run(nICs plugin, BlockRedstoneEvent event){
 		Sign sign = (Sign)event.getBlock().getState();
 		boolean power = (event.getNewCurrent() > 0);
 		World world = event.getBlock().getWorld();
 		if (power == true){
			for (int i = 0; i < world.getLivingEntities().size(); i++){
				if (world.getLivingEntities().get(i) instanceof Player)
					((Player)world.getLivingEntities().get(i)).sendMessage((sign.getLine(2) + sign.getLine(3)).replace("&", ""));
 			}
 		}
 		return null;
 	}
 	
 	public String getName(){
 		return "WORLD BROADCAST";
 	}
 	
 	public String canPlace(String[] lines){
 		if (lines[2].equals("")) return "Line 3 must contain a string!";
 		return null;
 	}
 }
