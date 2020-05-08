 package com.runetooncraft.plugins.EasyMobArmory.core;
 
 import java.util.ArrayList;
 import java.util.List;
 
 
 import org.bukkit.Bukkit;
 import org.bukkit.Chunk;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 
 import com.bergerkiller.bukkit.common.entity.CommonEntity;
 
 public class CoreMethods {
 
 	public static Location CheckIfAirBlock(Location loc) {
 		if(loc.getBlock().getTypeId() == 0) {
 			return loc;
 		}else{
 			loc.setY(loc.getBlockY() + 1);
 			return CheckIfAirBlock(loc);
 		}
 	}
 	
 	public static List<Entity> GetEntitiesInChunk(Location Chunkloc) {
 
 		int x = (int) Chunkloc.getX();
 		int y = (int) Chunkloc.getY();
 		int z = (int) Chunkloc.getZ();
 
 		List<Entity> Entities = new ArrayList<Entity>();
 			Chunk chunk = Chunkloc.getWorld().getChunkAt(Chunkloc);
 			Entity[] ChunkEntities = chunk.getEntities();
 			for(Entity e : ChunkEntities) {
 				Entities.add(e);
 			}			
 		return Entities;
 	}
 	
 	public static List<Entity> GetEntitiesInRadius(Location CenterPoint, int Radius) {
 		List<Entity> EntityList = null;
 		if(Bukkit.getServer().getPluginManager().getPlugin("BKCommonLib") != null) {
 			int x = (int) CenterPoint.getX();
 			int y = (int) CenterPoint.getY();
 			int z = (int) CenterPoint.getZ();
 				World BukkitWorld = CenterPoint.getWorld();
 				CommonEntity entity = CommonEntity.create(EntityType.EXPERIENCE_ORB);
 				entity.setLocation(x, y, z,0,0);
 				entity.spawn(CenterPoint);
 				EntityList = entity.getNearbyEntities(Radius);
 		}
 		return EntityList;
 	}
 	
 	public static List<Player> GetPlayersInRadius(Location CenterPoint, int Radius) {
 		List<Entity> EntityList = GetEntitiesInRadius(CenterPoint,Radius);
 		List<Player> PlayerList = null;
 		for(Entity e : EntityList) {
 			if(e.getType().equals(EntityType.PLAYER)) {
 				PlayerList.add((Player) e);
 			}
 		}
 		return PlayerList;
 	}
 	
 	public static boolean PlayerIsInRadius(Location CenterPoint, int Radius) {
		if(GetPlayersInRadius(CenterPoint,Radius).isEmpty()) {
 			return false;
 		}else{
 			return true;
 		}
 	}
 }
