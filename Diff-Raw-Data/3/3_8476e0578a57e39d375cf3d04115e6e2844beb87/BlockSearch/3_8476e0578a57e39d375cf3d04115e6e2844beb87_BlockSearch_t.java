 package me.arno.blocklog.search;
 
 import java.sql.Connection;
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
 	private Connection conn;
 	private String player;
 	private String entity;
 	
 	private String world;
 	private Location location;
 	private int area = 0;
 	
 	private int rollback = 0;
 	
 	private int since = 0;
 	private int until = 0;
 	
 	private boolean groupByLocation = true;
 	
 	private int limit = 5;
 	
 	public BlockSearch() { this.conn = BlockLog.getInstance().conn; }
 	public BlockSearch(Connection conn) { this.conn = conn; }
 
 	public void setPlayer(String player) {
 		this.player = player;
 	}
 	
 	public void setEntity(String entity) {
 		if(entity != null) {
 			if(entity.equalsIgnoreCase("tnt"))
 				entity = "primed_tnt";
 		}
 		
 		this.entity = entity;
 	}
 	
 	public void setWorld(String world) {
 		this.world = world;
 	}
 	
 	public void setLocation(Location location) {
 		this.location = location;
		if(location != null)
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
 	
 	public void setLimit(int limit) {
 		this.limit = limit;
 	}
 	
 	public ArrayList<BlockEntry> getResults() {
 		ArrayList<BlockEntry> blockEntries = new ArrayList<BlockEntry>();
 		
 		World world = null;
 		int xMin = 0; int xMax = 0; int yMin = 0; int yMax = 0; int zMin = 0; int zMax = 0;
 		
 		if(location != null) {
 			world = location.getWorld();
 			
 			xMin = location.getBlockX() - area;
 			xMax = location.getBlockX() + area;
 			yMin = location.getBlockY() - area;
 			yMax = location.getBlockY() + area;
 			zMin = location.getBlockZ() - area;
 			zMax = location.getBlockZ() + area;
 		}
 		
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
 		if(area != 0 && location != null)
 			query.where("x", xMin, ">=").where("x", xMax, "<=").where("y", yMin, ">=").where("y", yMax, "<=").where("z", zMin, ">=").where("z", zMax, "<=");
 		if(world != null)
 			query.where("world", world.getName());
 		
 		query.where("rollback", rollback);
 		
 		if(groupByLocation)
 			query.groupBy("x", "y", "z");
 		
 		query.orderBy("date", "DESC");
 		
 		try {
 			ResultSet rs = query.getResult(conn);
 			
 			for(BlockEntry edit : BlockLog.getInstance().getQueueManager().getBlockEntries()) {
 				if(limit == 0)
 					break;
 				if(checkEdit(edit)) {
 					blockEntries.add(edit);
 					limit--;
 				}
 			}
 			
 			while(rs.next()) {
 				if(limit == 0)
 					break;
 				
 				int id = rs.getInt("id");
 				String player = rs.getString("player");
 				String entity = rs.getString("entity");
 				int block = rs.getInt("block");
 				byte data = rs.getByte("data");
 				int oldBlock = rs.getInt("old_block");
 				byte oldData = rs.getByte("old_data");
 				int type = rs.getInt("type");
 				int rollback = rs.getInt("rollback");
 				long date = rs.getLong("date");
 				
 				Location loc = new Location(Bukkit.getWorld(rs.getString("world")), rs.getInt("x"), rs.getInt("y"), rs.getInt("z"));
 
 				EntityType entityType = EntityType.valueOf(entity.toUpperCase());
 				LogType logType = LogType.values()[type];
 				
 				BlockEntry blockEntry = new BlockEntry(player, entityType, logType, loc, block, data, oldBlock, oldData);
 				blockEntry.setId(id);
 				blockEntry.setRollback(rollback);
 				blockEntry.setDate(date);
 				
 				blockEntries.add(blockEntry);
 				limit--;
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		
 		return blockEntries;
 	}
 	
 	public boolean checkEdit(BlockEntry edit) {
 		if(!world.equalsIgnoreCase(edit.getWorld()))
 			return false;
 		
 		if(rollback != edit.getRollback())
 			return false;
 		
 		if(player != null) {
 			if(!player.equalsIgnoreCase(edit.getPlayer()))
 				return false;
 		}
 		
 		if(entity != null) {
 			if(!entity.equalsIgnoreCase(edit.getEntity()))
 				return false;
 		}
 		
 		if(since > 0) {
 			if(edit.getDate() > since)
 				return false;
 		}
 		
 		if(until > 0) {
 			if(edit.getDate() < until)
 				return false;
 		}
 		
 		if(area > 0) {
 			if(!(edit.getX() >= location.getX() && edit.getX() <= location.getX() && edit.getY() >= location.getY() && edit.getY() <= location.getY() && edit.getZ() <= location.getZ() && edit.getZ() >= location.getZ()))
 				return false;
 		}
 		
 		return true;
 	}
 }
