 package me.arno.blocklog.search;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.entity.EntityType;
 
 import me.arno.blocklog.BlockLog;
 import me.arno.blocklog.logs.BlockEntry;
 import me.arno.blocklog.logs.LogType;
 import me.arno.blocklog.util.Query;
 
 public class BlockSearch {
 	public String player;
 	public String entity;
 	
 	public String world;
 	public Location location;
 	public int area = 0;
 	
 	public int rollback = 0;
 	
 	public int since = 0;
 	public int until = 0;
 	
 	public boolean groupByLocation = true;
 	
 	public void setPlayer(String player) {
 		this.player = player;
 	}
 	
 	public void setEntity(String entity) {
 		if(entity.equalsIgnoreCase("tnt"))
 			entity = "primed_tnt";
 		
 		this.entity = entity;
 	}
 	
 	public void setWorld(String world) {
 		this.world = world;
 	}
 	
 	public void setLocation(Location location) {
 		this.location = location;
 		setWorld(location.getWorld().getName());
 	}
 	
 	public void setArea(int area) {
 		this.area = area;
 	}
 	
 	public void setRollback(int rollback) {
 		this.rollback = rollback;
 	}
 	
 	public void setDate(int since) {
 		setDate(since, 0);
 	}
 	
 	public void setDate(int since, int until) {
 		this.since = since;
 		this.until = until;
 	}
 	
 	public ArrayList<BlockEntry> getResults() {
 		ArrayList<BlockEntry> blockEntries = new ArrayList<BlockEntry>();
 		
 		World world = location.getWorld();
 		
 		int xMin = location.getBlockX() - area;
 		int xMax = location.getBlockX() + area;
 		int yMin = location.getBlockY() - area;
 		int yMax = location.getBlockY() + area;
 		int zMin = location.getBlockZ() - area;
 		int zMax = location.getBlockZ() + area;
 		
 		Query query = new Query("blocklog_blocks");
 		query.select("*");
 		if(player != null)
 			query.where("player", player);
 		if(entity != null)
 			query.where("entity", entity);
 		if(since != 0)
 			query.where("date", since, ">");
 		if(until != 0)
 			query.where("date", until, "<");
 		if(area != 0)
 			query.where("x", xMin, ">=").where("x", xMax, "<=").where("y", yMin, ">=").where("y", yMax, "<=").where("z", zMin, ">=").where("z", zMax, "<=");
 		if(world != null)
 			query.where("world", world);
 		if(rollback != 0)
 			query.where("rollback", rollback);
 		
 		if(groupByLocation)
 			query.groupBy("x", "y", "z");
 		
 		query.orderBy("date", "DESC");
 		
 		try {
 			ResultSet rs = query.getResult();
 			
 			for(BlockEntry edit : BlockLog.plugin.getQueueManager().getBlockEntries()) {
 				if(checkEdit(edit))
 					blockEntries.add(edit);
 			}
 			
 			while(rs.next()) {
 				int id = rs.getInt("id");
 				String player = rs.getString("player");
 				String entity = rs.getString("entity");
 				int block = rs.getInt("block");
 				byte data = rs.getByte("data");
 				int type = rs.getInt("type");
				int rollback = rs.getInt("rollback");
 				long date = rs.getLong("date");
 				
 				Location loc = new Location(Bukkit.getWorld(rs.getString("world")), rs.getInt("x"), rs.getInt("y"), rs.getInt("z"));
 
 				EntityType entityType = EntityType.valueOf(entity);
 				LogType logType = LogType.values()[type];
 				
 				BlockEntry blockEntry = new BlockEntry(player, entityType, logType, loc, block, data);
 				blockEntry.setId(id);
 				blockEntry.setRollback(rollback);
 				blockEntry.setDate(date);
 				
 				blockEntries.add(blockEntry);
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		
 		return blockEntries;
 	}
 	
 	public boolean checkEdit(BlockEntry edit) {
 		if(!edit.getWorld().equalsIgnoreCase(world))
 			return false;
 		
 		if(edit.getRollback() == rollback)
 			return false;
 		
 		if(player != null) {
 			if(!player.equalsIgnoreCase(edit.getPlayer()))
 				return false;
 		}
 		
 		if(entity != null) {
 			if(!entity.equalsIgnoreCase(edit.getEntity()))
 				return false;
 		}
 		
 		if(since != 0) {
 			if(edit.getDate() < since)
 				return false;
 		}
 		
 		if(until != 0) {
 			if(edit.getDate() > until)
 				return false;
 		}
 		
 		if(area > 0) {
 			if(!(edit.getX() >= location.getX() && edit.getX() <= location.getX() && edit.getY() >= location.getY() && edit.getY() <= location.getY() && edit.getZ() <= location.getZ() && edit.getZ() >= location.getZ()))
 				return false;
 		}
 		
 		return true;
 	}
 }
