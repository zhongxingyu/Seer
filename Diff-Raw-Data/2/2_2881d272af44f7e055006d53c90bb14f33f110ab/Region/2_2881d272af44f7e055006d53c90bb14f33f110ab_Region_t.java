 /*******************************************************************************
  * Copyright (c) 2013 Travis Ralston.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Lesser Public License v2.1
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
  * 
  * Contributors:
  * turt2live (Travis Ralston) - initial API and implementation
  ******************************************************************************/
 package com.turt2live.antishare.regions;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.entity.Player;
 import org.bukkit.metadata.FixedMetadataValue;
 
 import com.feildmaster.lib.configuration.EnhancedConfiguration;
 import com.turt2live.antishare.AntiShare;
 import com.turt2live.antishare.config.RegionConfiguration;
 import com.turt2live.antishare.cuboid.Cuboid;
 import com.turt2live.antishare.cuboid.RegionCuboid;
 import com.turt2live.antishare.inventory.ASInventory;
 import com.turt2live.antishare.regions.RegionWall.Wall;
 import com.turt2live.antishare.util.ASUtils;
 import com.turt2live.antishare.util.Action;
 import com.turt2live.antishare.util.PermissionNodes;
 
 /**
  * An AntiShare Region
  */
 public class Region {
 
 	private static AntiShare plugin = AntiShare.p;
 
 	/**
 	 * Location where region configurations are stored
 	 */
 	public static final File REGION_CONFIGURATIONS = new File(plugin.getDataFolder(), "region_configurations");
 	/**
 	 * Location where region information is stored
 	 */
 	public static final File REGION_INFORMATION = new File(plugin.getDataFolder(), "data" + File.separator + "regions");
 	/**
 	 * Region structure version
 	 */
 	public static final int REGION_VERSION = 2;
 
 	private String worldName = "antishare", owner = "antishare", id = "-1", enterMessage = "You entered {name}!", exitMessage = "You left {name}!", name = "AntiShareRegion";
 	private RegionCuboid size = new RegionCuboid(this);
 	private boolean showEnterMessage = true, showExitMessage = true;
	private ASInventory inventory = null;
 	private final Map<String, GameMode> gamemodes = new HashMap<String, GameMode>();
 	private RegionConfiguration config = RegionConfiguration.getConfig(this);
 	private GameMode gamemode = GameMode.CREATIVE;
 
 	/**
 	 * Gets the name of this region
 	 * 
 	 * @return the region name
 	 */
 	public String getName(){
 		return name;
 	}
 
 	/**
 	 * Gets the world name this region belongs to
 	 * 
 	 * @return the world this region resides in
 	 */
 	public String getWorldName(){
 		return worldName;
 	}
 
 	/**
 	 * Gets the person who made the region
 	 * 
 	 * @return the region owner
 	 */
 	public String getOwner(){
 		return owner;
 	}
 
 	/**
 	 * Gets the unique region ID
 	 * 
 	 * @return the region ID
 	 */
 	public String getID(){
 		return id;
 	}
 
 	/**
 	 * Gets the region's "enter message"
 	 * 
 	 * @return the enter message
 	 */
 	public String getEnterMessage(){
 		return enterMessage;
 	}
 
 	/**
 	 * Gets the region's "exit message"
 	 * 
 	 * @return the exit message
 	 */
 	public String getExitMessage(){
 		return exitMessage;
 	}
 
 	/**
 	 * Determines if the enter message is shown to players
 	 * 
 	 * @return true if shown
 	 */
 	public boolean isEnterMessageShown(){
 		return showEnterMessage;
 	}
 
 	/**
 	 * Determines if the exit message is shown to players
 	 * 
 	 * @return true if shown
 	 */
 	public boolean isExitMessageShown(){
 		return showExitMessage;
 	}
 
 	/**
 	 * Gets a <b>cloned</b> copy of the cuboid this region represents
 	 * 
 	 * @return the <b>cloned</b> cuboid of this region
 	 */
 	public RegionCuboid getCuboid(){
 		return size.clone();
 	}
 
 	/**
 	 * Gets the <b>cloned</b> copy of the inventory for this region. This can be null.
 	 * 
 	 * @return null for no inventory, otherwise a <b>cloned</b> inventory
 	 */
 	public ASInventory getInventory(){
 		return inventory == null ? null : inventory.clone();
 	}
 
 	/**
 	 * Gets the Game Mode for this region
 	 * 
 	 * @return the region's Game Mode
 	 */
 	public GameMode getGameMode(){
 		return gamemode;
 	}
 
 	/**
 	 * Gets the region's configuration class
 	 * 
 	 * @return the configuration
 	 */
 	public RegionConfiguration getConfig(){
 		return config;
 	}
 
 	/**
 	 * Sets the region name
 	 * 
 	 * @param name the new name
 	 */
 	public void setName(String name){
 		this.name = name;
 	}
 
 	/**
 	 * Sets the world by which this region resides in
 	 * 
 	 * @param world the region's world
 	 */
 	public void setWorld(World world){
 		this.worldName = world.getName();
 		size.setWorld(world);
 	}
 
 	/**
 	 * Sets the region's owner (creator)
 	 * 
 	 * @param owner the new creator
 	 */
 	public void setOwner(String owner){
 		this.owner = owner;
 	}
 
 	/**
 	 * Sets the regions unique ID. This is not verified to be unique internally and is trusted as such.
 	 * 
 	 * @param id the new ID
 	 */
 	public void setID(String id){
 		this.id = id;
 	}
 
 	/**
 	 * Sets the enter message for this region
 	 * 
 	 * @param enterMessage the new enter message
 	 */
 	public void setEnterMessage(String enterMessage){
 		this.enterMessage = enterMessage;
 	}
 
 	/**
 	 * Sets the exit message for this region
 	 * 
 	 * @param exitMessage the new exit message
 	 */
 	public void setExitMessage(String exitMessage){
 		this.exitMessage = exitMessage;
 	}
 
 	/**
 	 * Sets the area by which this region occupies, this is not verified internally for overlapping regions. <b>The RegionCuboid passed is cloned before being set</b>
 	 * 
 	 * @param cuboid the new region area
 	 */
 	public void setCuboid(Cuboid cuboid){
 		this.size = RegionCuboid.fromCuboid(cuboid, this);
 	}
 
 	/**
 	 * Sets the boolean status to show or hide the enter message
 	 * 
 	 * @param showEnterMessage true to show the enter message
 	 */
 	public void setShowEnterMessage(boolean showEnterMessage){
 		this.showEnterMessage = showEnterMessage;
 	}
 
 	/**
 	 * Sets the boolean status to show or hide the exit message
 	 * 
 	 * @param showExitMessage true to show the exit message
 	 */
 	public void setShowExitMessage(boolean showExitMessage){
 		this.showExitMessage = showExitMessage;
 	}
 
 	/**
 	 * Sets the inventory for this region. This can be null for no inventory. <b>The inventory is cloned before being set internally</b>
 	 * 
 	 * @param asInventory
 	 */
 	public void setInventory(ASInventory asInventory){
 		this.inventory = asInventory != null ? asInventory.clone() : null;
 	}
 
 	/**
 	 * Sets the Game Mode for this region
 	 * 
 	 * @param gamemode the region's new Game Mode
 	 */
 	public void setGameMode(GameMode gamemode){
 		this.gamemode = gamemode;
 	}
 
 	/**
 	 * Sets the configuration of this region.
 	 * 
 	 * @param config the new configuration
 	 */
 	public void setConfig(RegionConfiguration config){
 		this.config = config;
 	}
 
 	/**
 	 * Gets the nearest wall to a location within the region.<br>
 	 * Use {@link #getFaceLocation(Location) getFaceLocation(Location)} to get a ceiling/floor
 	 * 
 	 * @param location the location
 	 * @return the RegionWall (or null if the location is not in the region)
 	 */
 	public RegionWall getWallLocation(Location location){
 		if(!size.isContained(location)){
 			return null;
 		}
 
 		// Variables
 		Location min = size.getMinimumPoint();
 		Location max = size.getMaximumPoint();
 		Location northWall = new Location(size.getWorld(), min.getX() > max.getX() ? min.getX() : max.getX(), location.getY(), location.getZ());
 		Location southWall = new Location(size.getWorld(), min.getX() > max.getX() ? max.getX() : min.getX(), location.getY(), location.getZ());
 		Location eastWall = new Location(size.getWorld(), location.getX(), location.getY(), min.getZ() > max.getZ() ? min.getZ() : max.getZ());
 		Location westWall = new Location(size.getWorld(), location.getX(), location.getY(), min.getZ() > max.getZ() ? max.getZ() : min.getZ());
 
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
 		if(!size.isContained(location)){
 			return null;
 		}
 
 		// Variables
 		Location min = size.getMinimumPoint();
 		Location max = size.getMaximumPoint();
 		Location northWall = new Location(size.getWorld(), min.getX() > max.getX() ? min.getX() : max.getX(), location.getY(), location.getZ());
 		Location southWall = new Location(size.getWorld(), min.getX() > max.getX() ? max.getX() : min.getX(), location.getY(), location.getZ());
 		Location eastWall = new Location(size.getWorld(), location.getX(), location.getY(), min.getZ() > max.getZ() ? min.getZ() : max.getZ());
 		Location westWall = new Location(size.getWorld(), location.getX(), location.getY(), min.getZ() > max.getZ() ? max.getZ() : min.getZ());
 		Location ceil = new Location(size.getWorld(), location.getX(), min.getY() > max.getY() ? min.getY() : max.getY(), location.getZ());
 		Location floor = new Location(size.getWorld(), location.getX(), min.getY() > max.getY() ? max.getY() : min.getY(), location.getZ());
 
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
 		if(!size.isContained(location)){
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
 		plugin.getMessages().sendTo(player, playerMessage, true);
 		plugin.getMessages().notifyParties(player, Action.ENTER_REGION, false, getName()); // Player name is applied because player message is ignored
 
 		// Set the player
 		if(!AntiShare.hasPermission(player, PermissionNodes.REGION_ROAM)){
 			gamemodes.put(player.getName(), player.getGameMode());
 			if(player.getGameMode() != gamemode){
 				player.setGameMode(gamemode);
 			}
 			if(inventory != null){
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
 		if(!AntiShare.hasPermission(player, PermissionNodes.REGION_ROAM)){
 			gamemodes.put(player.getName(), player.getGameMode());
 			if(player.getGameMode() != gamemode){
 				player.setGameMode(gamemode);
 			}
 			if(inventory != null){
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
 		plugin.getMessages().sendTo(player, playerMessage, true);
 		plugin.getMessages().notifyParties(player, Action.EXIT_REGION, false, getName()); // Player name is applied because player message is ignored
 
 		// Tag the player so the Game Mode listener knows to ignore them
 		player.setMetadata("antishare-regionleave", new FixedMetadataValue(plugin, true));
 
 		// Reset the player
 		if(!AntiShare.hasPermission(player, PermissionNodes.REGION_ROAM)){
 			if(inventory != null){
 				plugin.getInventoryManager().removeFromTemporary(player);
 			}
 			player.setGameMode(gamemodes.get(player.getName()) == null ? player.getGameMode() : gamemodes.get(player.getName()));
 		}
 	}
 
 	/**
 	 * Saves the region and all of it's information to disk
 	 */
 	public void save(){
 		if(!REGION_INFORMATION.exists()){
 			REGION_INFORMATION.mkdirs();
 		}
 		File saveFile = new File(REGION_INFORMATION, ASUtils.fileSafeName(name) + ".yml");
 		if(!saveFile.exists()){
 			try{
 				saveFile.createNewFile();
 			}catch(IOException e){
 				e.printStackTrace();
 			}
 		}
 		EnhancedConfiguration yaml = new EnhancedConfiguration(saveFile, plugin);
 		yaml.load();
 		yaml.set("name", getName());
 		yaml.set("id", getID());
 		yaml.set("cuboid", getCuboid());
 		yaml.set("owner", getOwner());
 		yaml.set("gamemode", getGameMode().name());
 		yaml.set("showEnter", isEnterMessageShown());
 		yaml.set("showExit", isExitMessageShown());
 		yaml.set("enterMessage", getEnterMessage());
 		yaml.set("exitMessage", getExitMessage());
 		yaml.set("worldName", getWorldName());
 		yaml.set("players", playersAsList());
 		yaml.set("version", REGION_VERSION);
 		yaml.save();
 	}
 
 	/**
 	 * Loads a region from a YAML file. The passed file is assumed to be a valid region file
 	 * 
 	 * @param saveFile the region file
 	 * @return the region, or null if there was an error
 	 */
 	public static Region fromFile(File saveFile){
 		Region region = new Region();
 		AntiShare plugin = AntiShare.p;
 		EnhancedConfiguration yaml = new EnhancedConfiguration(saveFile, plugin);
 		yaml.load();
 		region.setName(yaml.getString("name"));
 		World world = plugin.getServer().getWorld(yaml.getString("worldName"));
 		if(world == null){
 			plugin.getLogger().warning(plugin.getMessages().getMessage("unknown-world", yaml.getString("worldName")));
 			return null;
 		}
 		region.setEnterMessage(yaml.getString("enterMessage"));
 		region.setExitMessage(yaml.getString("exitMessage"));
 		region.setShowEnterMessage(yaml.getBoolean("showEnter"));
 		region.setShowExitMessage(yaml.getBoolean("showExit"));
 		region.setID(yaml.getString("id"));
 		region.setGameMode(GameMode.valueOf(yaml.getString("gamemode")));
 		region.setWorld(world);
 		region.setConfig(RegionConfiguration.getConfig(region));
 		region.setInventory(plugin.getInventoryManager().loadRegionInventory(region));
 		if(yaml.getInt("version", 0) == REGION_VERSION){
 			List<String> players = yaml.getStringList("players");
 			region.populatePlayers(players);
 
 			Cuboid area = (Cuboid) yaml.get("cuboid");
 			region.setCuboid(area);
 
 			region.setOwner(yaml.getString("owner"));
 		}else{
 			double mix = yaml.getDouble("mi-x"), miy = yaml.getDouble("mi-y"), miz = yaml.getDouble("mi-z");
 			double max = yaml.getDouble("ma-x"), may = yaml.getDouble("ma-y"), maz = yaml.getDouble("ma-z");
 			Location l1 = new Location(world, mix, miy, miz);
 			Location l2 = new Location(world, max, may, maz);
 			RegionCuboid cuboid = new RegionCuboid(region, l1, l2);
 			region.setCuboid(cuboid);
 			region.setID(saveFile.getName().replace(".yml", ""));
 			region.setOwner(yaml.getString("set-by"));
 			loadLegacyPlayerInformation(region);
 		}
 		if(region.getID().equalsIgnoreCase("-1")){
 			region.setID(String.valueOf(System.nanoTime()));
 		}
 		return region;
 	}
 
 	private List<String> playersAsList(){
 		List<String> list = new ArrayList<String>();
 		for(String playername : gamemodes.keySet()){
 			list.add(playername + " " + gamemodes.get(playername).name());
 		}
 		return list;
 	}
 
 	private void populatePlayers(List<String> list){
 		for(String record : list){
 			String[] parts = record.split(" ");
 			if(parts.length > 1){
 				String playerName = parts[0];
 				GameMode gamemode = GameMode.valueOf(parts[1]);
 				gamemodes.put(playerName, gamemode);
 			}
 		}
 	}
 
 	private static void loadLegacyPlayerInformation(Region region){
 		// Check file/folder
 		File saveFolder = new File(plugin.getDataFolder(), "data" + File.separator + "region_players");
 		File saveFile = new File(saveFolder, region.getID() + ".yml");
 		if(!saveFile.exists()){
 			return;
 		}
 
 		// Load
 		EnhancedConfiguration playerInfo = new EnhancedConfiguration(saveFile, plugin);
 		playerInfo.load();
 		for(String key : playerInfo.getKeys(false)){
 			region.gamemodes.put(key, GameMode.valueOf(playerInfo.getString(key)));
 		}
 	}
 
 	/**
 	 * Called on region creation
 	 */
 	public void onCreate(){
 		World world = plugin.getServer().getWorld(getWorldName());
 		List<Player> players = world.getPlayers();
 		if(players != null){
 			for(Player player : players){
 				if(size.isContained(player.getLocation())){
 					alertEntry(player);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Called on region update
 	 * 
 	 * @param last the old region cuboid
 	 */
 	public void onUpdate(Cuboid last){
 		World world = plugin.getServer().getWorld(getWorldName());
 		List<Player> players = world.getPlayers();
 		if(players != null){
 			for(Player player : players){
 				if(size.isContained(player.getLocation()) && !last.isContained(player.getLocation())){
 					alertEntry(player);
 				}else if(last.isContained(player.getLocation()) && !size.isContained(player.getLocation())){
 					alertExit(player);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Called on region deletion
 	 */
 	public void onDelete(){
 		World world = plugin.getServer().getWorld(getWorldName());
 		List<Player> players = world.getPlayers();
 		if(players != null){
 			for(Player player : players){
 				if(size.isContained(player.getLocation())){
 					alertExit(player);
 				}
 			}
 		}
 	}
 
 }
