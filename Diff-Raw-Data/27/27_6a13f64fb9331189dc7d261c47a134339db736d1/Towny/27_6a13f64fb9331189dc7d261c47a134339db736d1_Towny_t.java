 package com.turt2live.antishare.compatibility;
 
 import org.bukkit.Location;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 
 import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
 import com.palmergames.bukkit.towny.object.Town;
 import com.palmergames.bukkit.towny.object.TownBlock;
 import com.palmergames.bukkit.towny.object.TownyUniverse;
 import com.turt2live.antishare.compatibility.type.RegionProtection;
 
 public class Towny extends RegionProtection {
 
 	@Override
 	public boolean isRegion(Location location){
		return !TownyUniverse.isWilderness(location.getBlock());
 	}
 
 	@Override
 	public boolean isAllowed(Player player, Block block){
 		if(!isRegion(block.getLocation())){
 			return true;
 		}
 		TownBlock tblock = TownyUniverse.getTownBlock(block.getLocation());
 		if(tblock == null){
 			return true;
 		}
 		try{
 			Town town = tblock.getTown();
 			if(town == null){
 				return true;
 			}
 			if(town.hasResident(player.getName()) || town.getMayor().getName().equalsIgnoreCase(player.getName())){
 				return true;
 			}
 		}catch(NotRegisteredException e){
 			return true;
 		}
 		return false;
 	}
 }
