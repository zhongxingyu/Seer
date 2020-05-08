 package com.niccholaspage.nICs.ics;
 
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.CreatureType;
 import org.bukkit.event.block.BlockRedstoneEvent;
 
 import com.niccholaspage.nICs.nICs;
 
 public class N1000 implements IC {
 
 	@Override
 	public Boolean run(nICs plugin, BlockRedstoneEvent event) {
 		boolean power = (event.getNewCurrent() > 0);
 		Sign sign = (Sign)event.getBlock().getState();
 		World world = event.getBlock().getWorld();
 		if (power == false) return null;
 		Location block = plugin.getBlockBehindOfSign(sign).getLocation();
 		block.setY(block.getY() + 1);
 		int times = 1;
 		if (plugin.isInt(sign.getLine(3))) times = Integer.parseInt(sign.getLine(3));
 		for (int i = 0; i < times; i++){
 		world.spawnCreature(block, CreatureType.fromName(sign.getLine(2)));
 		}
 		return null;
 	}
 
 	@Override
 	public String getName() {
 		return "SPAWNMOB";
 	}
 
 	@Override
 	public String canPlace(String[] lines) {
 		Boolean pass = false;
		if (lines.length > 1){
 		lines[2] = lines[2].substring(0, 1).toUpperCase() + lines[2].substring(1);
 		for (int i = 0; i < CreatureType.values().length; i++){
 			if (!(CreatureType.fromName(lines[2]) == null)){
 				pass = true;
 			}
 		}
		}
 		if (pass) return null; else return "Line 3 must be a mob name!";
 	}
 
 }
