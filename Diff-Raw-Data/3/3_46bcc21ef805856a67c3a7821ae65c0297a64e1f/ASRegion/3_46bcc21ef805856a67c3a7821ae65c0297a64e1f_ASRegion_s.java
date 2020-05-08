 package com.turt2live.antishare.regions;
 
 import java.io.File;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.logging.Level;
 
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.entity.Player;
 import org.bukkit.metadata.FixedMetadataValue;
 
 import com.feildmaster.lib.configuration.EnhancedConfiguration;
 import com.sk89q.worldedit.bukkit.selections.CuboidSelection;
 import com.sk89q.worldedit.bukkit.selections.Selection;
 import com.turt2live.antishare.AntiShare;
 import com.turt2live.antishare.AntiShare.LogType;
 import com.turt2live.antishare.ErrorLog;
 import com.turt2live.antishare.inventory.ASInventory;
 import com.turt2live.antishare.notification.Alert.AlertTrigger;
 import com.turt2live.antishare.notification.Alert.AlertType;
 import com.turt2live.antishare.permissions.PermissionNodes;
 import com.turt2live.antishare.regions.RegionWall.Wall;
 import com.turt2live.antishare.storage.PerRegionConfig;
 
 /**
  * AntiShare Region
  * 
  * @author turt2live
  */
 public class ASRegion {
 
 	private AntiShare plugin;
 	private World world;
 	private String setBy;
 	private GameMode gamemode;
 	private Selection region;
 	private String id;
 	private String name;
 	private boolean showEnterMessage = true;
 	private boolean showExitMessage = true;
 	private ASInventory inventory;
 	private String enterMessage = "You entered '{name}'!";
 	private String exitMessage = "You left '{name}'!";
 	private ConcurrentHashMap<String, GameMode> previousGameModes = new ConcurrentHashMap<String, GameMode>();
 	private PerRegionConfig config;
 
 	/**
 	 * Creates a new region
 	 * 
 	 * @param region the selection (area)
 	 * @param setBy the player who made this
 	 * @param gamemode the gamemode of the region
 	 */
 	public ASRegion(Selection region, String setBy, GameMode gamemode){
 		this.region = new CuboidSelection(region.getWorld(), region.getMaximumPoint(), region.getMinimumPoint());
 		this.setBy = setBy;
 		this.gamemode = gamemode;
 		this.world = region.getWorld();
 		id = String.valueOf(System.currentTimeMillis());
 		plugin = AntiShare.getInstance();
 		name = id;
 	}
 
 	/**
 	 * Creates a new region
 	 * 
 	 * @param world the world
 	 * @param minimum the minimum point
 	 * @param maximum the maximum point
 	 * @param setBy the player who made this
 	 * @param gamemode the gamemode of the region
 	 */
 	public ASRegion(World world, Location minimum, Location maximum, String setBy, GameMode gamemode){
 		this.region = new CuboidSelection(world, minimum, maximum);
 		this.setBy = setBy;
 		this.gamemode = gamemode;
 		this.world = region.getWorld();
 		id = String.valueOf(System.currentTimeMillis());
 		plugin = AntiShare.getInstance();
 		name = id;
 	}
 
 	/**
 	 * Delete any hidden data (such as configurations) for this region
 	 */
 	public void delete(){
 		File path = new File(plugin.getDataFolder(), "region_configurations");
 		new File(path, getName() + ".yml").delete();
 	}
 
 	/**
 	 * Build the configuration
 	 */
 	public void buildConfiguration(){
 		config = new PerRegionConfig(this);
 	}
 
 	/**
 	 * Sets the unique ID of this region
 	 * 
 	 * @param ID the new Unique ID
 	 */
 	public void setUniqueID(String ID){
 		id = ID;
 	}
 
 	/**
 	 * Sets the gamemode of this region
 	 * 
 	 * @param gamemode the new gamemode
 	 */
 	public void setGameMode(GameMode gamemode){
 		this.gamemode = gamemode;
 	}
 
 	/**
 	 * Sets the name of this region
 	 * 
 	 * @param name the new name
 	 */
 	public void setName(String name){
 		this.name = name;
 	}
 
 	/**
 	 * Sets the show message options
 	 * 
 	 * @param showEnter true = show enter message
 	 * @param showExit true = show exit message
 	 */
 	public void setMessageOptions(boolean showEnter, boolean showExit){
 		showEnterMessage = showEnter;
 		showExitMessage = showExit;
 	}
 
 	/**
 	 * Sets the area of this region
 	 * 
 	 * @param selection the new area
 	 */
 	public void setRegion(Selection selection){
 		if(selection == null){
 			return;
 		}
 		region = selection;
 	}
 
 	/**
 	 * Sets the inventory of this region
 	 * 
 	 * @param inventory the inventory
 	 */
 	public void setInventory(ASInventory inventory){
 		this.inventory = inventory;
 	}
 
 	/**
 	 * Sets the enter message for this region, {name} is the region name
 	 * 
 	 * @param message the new message
 	 */
 	public void setEnterMessage(String message){
 		if(message == null){
 			message = "You entered '{name}'!";
 		}
 		enterMessage = message;
 	}
 
 	/**
 	 * Sets the exit message for this region, {name} is the region name
 	 * 
 	 * @param message the new message
 	 */
 	public void setExitMessage(String message){
 		if(message == null){
 			message = "You left '{name}'!";
 		}
 		exitMessage = message;
 	}
 
 	/**
 	 * Gets the name of this region
 	 * 
 	 * @return the name
 	 */
 	public String getName(){
 		return name;
 	}
 
 	/**
 	 * Determines if the enter message is to be shown
 	 * 
 	 * @return true if the enter message is shown
 	 */
 	public boolean isEnterMessageActive(){
 		return showEnterMessage;
 	}
 
 	/**
 	 * Determines if the exit message is to be shown
 	 * 
 	 * @return true if the exit message is shown
 	 */
 	public boolean isExitMessageActive(){
 		return showExitMessage;
 	}
 
 	/**
 	 * Gets the world this region is within
 	 * 
 	 * @return the world
 	 */
 	public World getWorld(){
 		return world;
 	}
 
 	/**
 	 * Gets the name of the person who set this region
 	 * 
 	 * @return the name of the creator
 	 */
 	public String getWhoSet(){
 		return setBy;
 	}
 
 	/**
 	 * Gets the gamemode of this region
 	 * 
 	 * @return the gamemode
 	 */
 	public GameMode getGameMode(){
 		return gamemode;
 	}
 
 	/**
 	 * Gets the raw WorldEdit selection for this region
 	 * 
 	 * @return the world edit selection
 	 */
 	public Selection getSelection(){
 		return region;
 	}
 
 	/**
 	 * Gets the unique ID for this region
 	 * 
 	 * @return the unique ID
 	 */
 	public String getUniqueID(){
 		return id;
 	}
 
 	/**
 	 * Gets the enter message for this region
 	 * 
 	 * @return the enter message
 	 */
 	public String getEnterMessage(){
 		return enterMessage;
 	}
 
 	/**
 	 * Gets the exit message for this region
 	 * 
 	 * @return the exit message
 	 */
 	public String getExitMessage(){
 		return exitMessage;
 	}
 
 	/**
 	 * Gets the inventory for this region
 	 * 
 	 * @return the inventory
 	 */
 	public ASInventory getInventory(){
 		return inventory;
 	}
 
 	/**
 	 * Determines if a location is within this region
 	 * 
 	 * @param location the location
 	 * @return true if inside
 	 */
 	public boolean has(Location location){
 		if(location == null){
 			return false;
 		}
 		return region.contains(location);
 	}
 
 	/**
 	 * Gets the nearest wall to a location within the region.<br>
 	 * Use {@link #getFaceLocation(Location) getFaceLocation(Location)} to get a ceiling/floor
 	 * 
 	 * @param location the location
 	 * @return the RegionWall (or null if the location is not in the region)
 	 */
 	public RegionWall getWallLocation(Location location){
 		if(!has(location)){
 			return null;
 		}
 
 		// Variables
 		Location min = region.getMinimumPoint();
 		Location max = region.getMaximumPoint();
 		Location northWall = new Location(world, (min.getX() > max.getX() ? min.getX() : max.getX()), location.getY(), location.getZ());
 		Location southWall = new Location(world, (min.getX() > max.getX() ? max.getX() : min.getX()), location.getY(), location.getZ());
 		Location eastWall = new Location(world, location.getX(), location.getY(), (min.getZ() > max.getZ() ? min.getZ() : max.getZ()));
 		Location westWall = new Location(world, location.getX(), location.getY(), (min.getZ() > max.getZ() ? max.getZ() : min.getZ()));
 
 		// Check distances to walls
 		double toNorth = Math.abs(northWall.distanceSquared(location));
 		double toSouth = Math.abs(southWall.distanceSquared(location));
 		double toEast = Math.abs(eastWall.distanceSquared(location));
 		double toWest = Math.abs(westWall.distanceSquared(location));
 
 		// Find walls and return the wall (or lack of)
 		if(toNorth <= toSouth && toNorth <= toEast && toNorth <= toWest){
 			return new RegionWall(Wall.NORTH, northWall);
 		}else if(toSouth <= toNorth && toSouth <= toEast && toSouth <= toWest){
 			return new RegionWall(Wall.SOUTH, southWall);
 		}else if(toEast <= toNorth && toEast <= toSouth && toEast <= toWest){
 			return new RegionWall(Wall.EAST, eastWall);
 		}else if(toWest <= toNorth && toWest <= toEast && toWest <= toSouth){
 			return new RegionWall(Wall.WEST, westWall);
 		}
 		return null;
 	}
 
 	/**
 	 * Gets the nearest face to a location within the region
 	 * 
 	 * @param location the location
 	 * @return the RegionWall (or null if the location is not in the region)
 	 */
 	public RegionWall getFaceLocation(Location location){
 		if(!has(location)){
 			return null;
 		}
 
 		// Variables
 		Location min = region.getMinimumPoint();
 		Location max = region.getMaximumPoint();
 		Location northWall = new Location(world, (min.getX() > max.getX() ? min.getX() : max.getX()), location.getY(), location.getZ());
 		Location southWall = new Location(world, (min.getX() > max.getX() ? max.getX() : min.getX()), location.getY(), location.getZ());
 		Location eastWall = new Location(world, location.getX(), location.getY(), (min.getZ() > max.getZ() ? min.getZ() : max.getZ()));
 		Location westWall = new Location(world, location.getX(), location.getY(), (min.getZ() > max.getZ() ? max.getZ() : min.getZ()));
 		Location ceil = new Location(world, location.getX(), (min.getY() > max.getY() ? min.getY() : max.getY()), location.getZ());
 		Location floor = new Location(world, location.getX(), (min.getY() > max.getY() ? max.getY() : min.getY()), location.getZ());
 
 		// Get distances to faces
 		double toNorth = Math.abs(northWall.distanceSquared(location));
 		double toSouth = Math.abs(southWall.distanceSquared(location));
 		double toEast = Math.abs(eastWall.distanceSquared(location));
 		double toWest = Math.abs(westWall.distanceSquared(location));
 		double toFloor = Math.abs(floor.distanceSquared(location));
 		double toCeil = Math.abs(ceil.distanceSquared(location));
 
 		// Find face and return the face (or lack of)
 		if(toNorth <= toSouth && toNorth <= toEast && toNorth <= toWest){
 			return new RegionWall(Wall.NORTH, northWall);
 		}else if(toSouth <= toNorth && toSouth <= toEast && toSouth <= toWest){
 			return new RegionWall(Wall.SOUTH, southWall);
 		}else if(toEast <= toNorth && toEast <= toSouth && toEast <= toWest){
 			return new RegionWall(Wall.EAST, eastWall);
 		}else if(toWest <= toNorth && toWest <= toEast && toWest <= toSouth){
 			return new RegionWall(Wall.WEST, westWall);
 		}else if(toCeil <= toNorth && toCeil <= toEast && toCeil <= toWest && toCeil <= toFloor && toCeil <= toSouth){
 			return new RegionWall(Wall.CEILING, ceil);
 		}else if(toFloor <= toNorth && toFloor <= toEast && toFloor <= toWest && toFloor <= toCeil && toFloor <= toSouth){
 			return new RegionWall(Wall.FLOOR, floor);
 		}
 		return null;
 	}
 
 	/**
 	 * Gets a point outside the region.<br>
 	 * Use {@link #getPointOutsideFace(Location, int) getPointOutsideFace(Location, int)} to get a ceiling/floor
 	 * 
 	 * @param location the location within the region
 	 * @param fromBorder the distance to get from the region (absolute values)
 	 * @return the new location (or null if the location is not inside the region)
 	 */
 	public Location getPointOutside(Location location, int fromBorder){
 		fromBorder = Math.abs(fromBorder); // Sanity
 		if(!has(location)){
 			return null;
 		}
 		return getWallLocation(location).add(fromBorder).getPoint();
 	}
 
 	/**
 	 * Gets a point outside the region
 	 * 
 	 * @param location the location within the region
 	 * @param fromBorder the distance to get from the region (absolute values)
 	 * @return the new location (or null if the location is not inside the region)
 	 */
 	public Location getPointOutsideFace(Location location, int fromBorder){
 		return getFaceLocation(location).add(fromBorder).getPoint();
 	}
 
 	/**
 	 * Alerts the player (and server) of the player's entry to a region.
 	 * This also performs the required checks to ensure the player is correctly
 	 * inside the region (meaning gamemode and inventory, etc)
 	 * 
 	 * @param player the player entering the region
 	 */
 	public void alertEntry(Player player){
 		// Message
 		String playerMessage = "no message";
 		if(showEnterMessage){
 			playerMessage = ChatColor.GOLD + enterMessage.replaceAll("\\{name\\}", name);
 		}
 		plugin.getAlerts().alert(ChatColor.YELLOW + player.getName() + ChatColor.WHITE + " entered the region " + ChatColor.YELLOW + name, player, playerMessage, AlertType.REGION, AlertTrigger.GENERAL);
 
 		// Set the player
 		if(!plugin.getPermissions().has(player, PermissionNodes.REGION_ROAM)){
 			previousGameModes.put(player.getName(), player.getGameMode());
 			if(player.getGameMode() != gamemode){
 				player.setGameMode(gamemode);
 			}
 			if(inventory != null && !inventory.isEmpty()){
 				plugin.getInventoryManager().setToTemporary(player, inventory);
 			}
 		}
 	}
 
 	/**
 	 * Performs inventory and Game Mode checks on a player for this region
 	 * without ever alerting the player or server of the entry.
 	 * 
 	 * @param player the player entering the region
 	 */
 	public void alertSilentEntry(Player player){
 		// Set the player
 		if(!plugin.getPermissions().has(player, PermissionNodes.REGION_ROAM)){
 			previousGameModes.put(player.getName(), player.getGameMode());
 			if(player.getGameMode() != gamemode){
 				player.setGameMode(gamemode);
 			}
 			if(inventory != null && !inventory.isEmpty()){
 				plugin.getInventoryManager().setToTemporary(player, inventory);
 			}
 		}
 	}
 
 	/**
 	 * Alerts the player (and server) of the player's exit from a region.
 	 * This also performs the required checks to ensure the player is correctly
 	 * outside the region (meaning gamemode and inventory, etc)
 	 * 
 	 * @param player the player exiting the region
 	 */
 	public void alertExit(Player player){
 		// Message
 		String playerMessage = "no message";
 		if(showExitMessage){
 			playerMessage = ChatColor.GOLD + exitMessage.replaceAll("\\{name\\}", name);
 		}
 		plugin.getAlerts().alert(ChatColor.YELLOW + player.getName() + ChatColor.WHITE + " left the region " + ChatColor.YELLOW + name, player, playerMessage, AlertType.REGION, AlertTrigger.GENERAL);
 
 		// Tag the player so the Game Mode listener knows to ignore them
 		player.setMetadata("antishare-regionleave", new FixedMetadataValue(plugin, true));
 
 		// Reset the player
 		if(!plugin.getPermissions().has(player, PermissionNodes.REGION_ROAM)){
 			if(inventory != null && !inventory.isEmpty()){
 				plugin.getInventoryManager().removeFromTemporary(player);
 			}
 			player.setGameMode(previousGameModes.get(player.getName()) == null ? player.getGameMode() : previousGameModes.get(player.getName()));
 		}
 	}
 
 	/**
 	 * Saves the player information to disk
 	 */
 	public void savePlayerInformation(){
 		// Check file/folder
 		File saveFolder = new File(plugin.getDataFolder(), "region_players");
 		saveFolder.mkdirs();
 		File saveFile = new File(saveFolder, id + ".yml");
 		if(!saveFile.exists()){
 			try{
 				saveFile.createNewFile();
 			}catch(Exception e){
 				AntiShare.getInstance().getMessenger().log("AntiShare encountered and error. Please report this to turt2live.", Level.SEVERE, LogType.ERROR);
 				AntiShare.getInstance().getMessenger().log("Please see " + ErrorLog.print(e) + " for the full error.", Level.SEVERE, LogType.ERROR);
 			}
 		}else{
 			saveFile.delete();
 			try{
 				saveFile.createNewFile();
 			}catch(Exception e){
 				AntiShare.getInstance().getMessenger().log("AntiShare encountered and error. Please report this to turt2live.", Level.SEVERE, LogType.ERROR);
 				AntiShare.getInstance().getMessenger().log("Please see " + ErrorLog.print(e) + " for the full error.", Level.SEVERE, LogType.ERROR);
 			}
 		}
 
 		// Save
 		EnhancedConfiguration playerInfo = new EnhancedConfiguration(saveFile, plugin);
 		playerInfo.load();
 		for(String player : previousGameModes.keySet()){
 			playerInfo.set(player, previousGameModes.get(player).name());
 		}
 		playerInfo.save();
 	}
 
 	/**
 	 * Loads player information for this region
 	 */
 	public void loadPlayerInformation(){
 		// Check file/folder
 		File saveFolder = new File(plugin.getDataFolder(), "region_players");
 		saveFolder.mkdirs();
 		File saveFile = new File(saveFolder, id + ".yml");
 		if(!saveFile.exists()){
 			return;
 		}
 
 		// Load
 		EnhancedConfiguration playerInfo = new EnhancedConfiguration(saveFile, plugin);
 		playerInfo.load();
 		for(String key : playerInfo.getKeys(false)){
 			previousGameModes.put(key, GameMode.valueOf(playerInfo.getString(key)));
 		}
 	}
 
 	/**
 	 * Gets this region's configuration
 	 * 
 	 * @return the configuration
 	 */
 	public PerRegionConfig getConfig(){
 		return config;
 	}
 }
