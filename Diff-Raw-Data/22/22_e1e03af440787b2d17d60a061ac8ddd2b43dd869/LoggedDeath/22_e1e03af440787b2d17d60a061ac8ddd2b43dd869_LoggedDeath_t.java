 package me.arno.blocklog.logs;
 
 import java.sql.SQLException;
 import java.sql.Statement;
 
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.entity.Player;
 
 import me.arno.blocklog.BlockLog;
 
 public class LoggedDeath {
 	private final BlockLog plugin;
 	private final Player player;
 	private final Integer type;
 	private final Location location;
 	private final Long time;
 	
 	public LoggedDeath(BlockLog plugin, Player player, Integer type) {
 		this.plugin = plugin;
 		this.player = player;
 		this.type = type;
 		this.time = System.currentTimeMillis()/1000;
 		this.location = player.getLocation();
 	}
 	
 	public void save() {
 		try {
 			Statement stmt = plugin.conn.createStatement();
 			stmt.executeUpdate("INSERT INTO blocklog_deaths (player, type, world, x, y, z, date) VALUES ('" + getPlayerName() + "', '" + getType() + "', '" + getWorldName() + "', " + getX() + ", " + getY() + ", " + getZ() + ", " + time + ")");
 		} catch(SQLException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public String getPlayerName() {
 		return player.getName();
 	}
 	
 	public Player getPlayer() {
 		return player;
 	}
 	
 	public Integer getType() {
 		return type;
 	}
 	
 	public String getWorldName() {
 		return location.getWorld().getName();
 	}
 	
 	public World getWorld() {
 		return location.getWorld();
 	}
 	
 	public Location getLocation() {
 		return location;
 	}
 	
 	public Integer getX() {
 		return location.getBlockX();
 	}
 	
 	public Integer getY() {
		return location.getBlockY();
 	}
 	
 	public Integer getZ() {
		return location.getBlockZ();
 	}
 }
