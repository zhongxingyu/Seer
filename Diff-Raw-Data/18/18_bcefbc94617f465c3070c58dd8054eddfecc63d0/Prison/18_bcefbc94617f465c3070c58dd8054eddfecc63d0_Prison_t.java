 package net.marcuswhybrow.minecraft.law;
 
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 
 import com.sun.tools.javac.util.List;
 
 public class Prison {
 	private String name;
 	private HashMap<String, Cell> cells;
 	private LawWorld lawWorld;
 	private DefaultCell defaultCell;
 	
 	public Prison(LawWorld lawWorld, String name) {
 		this(lawWorld, name, true);
 	}
 	
 	public Prison(LawWorld lawWorld, String name, Boolean save) {
 		this.name = name;
 		this.cells = new HashMap<String, Cell>();
 		this.lawWorld = lawWorld;
 		
 		if (save) {
 			// Save the new name
 			Plugin plugin = Law.get().getPlugin();
 			FileConfiguration config = plugin.getConfig();
 			config.set(this.getConfigPrefix() + ".name", this.name);
 			plugin.saveConfig();
 		}
 	}
 	
 	public String getName() {
 		return this.name;
 	}
 	
 	@Override
 	public String toString() {
 		return this.name;
 	}
 	
 	public void setDefaultCell(Location cellLocation) {
 		this.setDefaultCell(cellLocation, true);
 	}
 	
 	public void setDefaultCell(Location cellLocation, Boolean save) {
		this.defaultCell = new DefaultCell(this, cellLocation, save);
 	}
 	
 	public void addCell(String cellName, Location cellLocation) {
 		this.addCell(cellName, cellLocation);
 	}
 	
 	public void addCell(String cellName, Location cellLocation, Boolean save) {
 		cells.put(cellName, new Cell(this, cellName, cellLocation));
 	}
 	
 	public void removeCell(String cellName) {
 		this.removeCell(cellName);
 	}
 	
 	public void removeCell(String cellName, Boolean save) {
 		
 		Cell cell = cells.remove(name);
 		if (cell != null) {
 			cell.delete();
 		}
 	}
 	
 	public void delete() {
 		lawWorld.removePrison(this);
 		
 		Plugin plugin = Law.get().getPlugin();
 		FileConfiguration config = plugin.getConfig();
 		config.set(this.getConfigPrefix(), null);
 		plugin.saveConfig();
 	}
 	
 	public void imprisonPlayer(Player player, String cell) {
 		this.imprisonPlayer(player, cell, true);
 	}
 	
 	public boolean imprisonPlayer(Player player, String cellName, Boolean save) {
 		Cell cell = (cellName == null) ? defaultCell : this.cells.get(cellName);
 		
 		if (cell == null) {
 			return false;
 		}
 		
 		cell.imprisonPlayer(player, save);
 		return true;
 	}
 	
 	public boolean hasCell(String cellName) {
 		if (cellName == null) {
 			return defaultCell != null;
 		} else {
 			return this.cells.containsKey(cellName);
 		}
 	}
 	
 	private String getConfigPrefix() {
 		return "worlds." + this.lawWorld.getName() + ".prisons." + this.getName();
 	}
 	
 	
 	private class Cell {
 		protected String name;
 		protected Prison prison;
 		protected HashMap<String, Player> imprisonedPlayers;
 		protected Location location;
 		
 		public Cell(Prison prison, String name, Location location) {
 			this(prison, name, location, true);
 		}
 		
 		public Cell(Prison prison, String name, Location location, boolean save) {
 			this.prison = prison;
 			this.name = name;
 			this.imprisonedPlayers = new HashMap<String, Player>();
 			this.location = location;
 			
 			if (save) {
 				// Save the new cell
 				Plugin plugin = Law.get().getPlugin();
 				FileConfiguration config = plugin.getConfig();
 				config.set(getConfigPrefix() + ".name", this.getName());
 				config.set(getConfigPrefix() + ".location.x", location.getX());
 				config.set(getConfigPrefix() + ".location.y", location.getY());
 				config.set(getConfigPrefix() + ".location.z", location.getZ());
 				config.set(getConfigPrefix() + ".location.pitch", location.getPitch());
 				config.set(getConfigPrefix() + ".location.yaw", location.getYaw());
 				plugin.saveConfig();
 			}
 		}
 		
 		public String getName() {
 			return this.name;
 		}
 		
 		public void delete() {
 			// Remove from memory
 			cells.remove(this.getName());
 			
 			// Remove from config file
 			Plugin plugin = Law.get().getPlugin();
 			FileConfiguration config = plugin.getConfig();
 			config.set(getConfigPrefix(), null);
 			plugin.saveConfig();
 		}
 		
 		public Location getLocation() {
 			return this.location;
 		}
 		
 		public Prison getPrison() {
 			return this.prison;
 		}
 		
 		public Player[] getImprisonedPlayers() {
 			return this.imprisonedPlayers.values().toArray(new Player[this.imprisonedPlayers.size()]);
 		}
 		
 		public void imprisonPlayer(Player player) {
 			this.imprisonPlayer(player, true);
 		}
 		
 		public void imprisonPlayer(Player player, boolean save) {
 			this.imprisonedPlayers.put(player.getDisplayName(), player);
 			player.teleport(this.location);
 			
 			if (save) {
 				// Save the new cell
 				
 				Player[] imprisonedPlayers = this.getImprisonedPlayers();
 				String[] imprisonedPlayerNames = new String[imprisonedPlayers.length];
 				int i = 0;
 				
 				for (Player imprisonedPlayer : imprisonedPlayers) {
 					imprisonedPlayerNames[i++] = imprisonedPlayer.getDisplayName();
 				}
 				
 				Plugin plugin = Law.get().getPlugin();
 				FileConfiguration config = plugin.getConfig();
 				config.set(getConfigPrefix() + ".imprisoned_players", Arrays.asList(imprisonedPlayerNames));
 				
 				plugin.saveConfig();
 			}
 		}
 		
 		public void freePlayer(Player player) {
 			this.freePlayer(player, true);
 		}
 		
 		public void freePlayer(Player player, boolean save) {
 			this.imprisonedPlayers.remove(player.getDisplayName());
 			
 			if (save) {
 				// Save the new cell
 				Plugin plugin = Law.get().getPlugin();
 				FileConfiguration config = plugin.getConfig();
 				config.set(getConfigPrefix() + ".imprisoned_players", this.getImprisonedPlayers());
 				plugin.saveConfig();
 			}
 		}
 		
 		protected String getConfigPrefix() {
 			return this.prison.getConfigPrefix() + ".cells."+ this.getName();
 		}
 	}
 	
 	
 	private class DefaultCell extends Cell {
 		
 		public DefaultCell(Prison prison, Location location) {
 			this(prison, location, true);
 		}
 		
 		public DefaultCell(Prison prison, Location location, boolean save) {
 			super(prison, "default_cell", location, save);
 		}
 		
 		@Override
 		protected String getConfigPrefix() {
 			return this.prison.getConfigPrefix() + ".default_cell";
 		}
 	}
 }
