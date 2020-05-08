 package com.turt2live.antishare.manager;
 
 import java.io.File;
import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.turt2live.antishare.Systems.Manager;
 import com.turt2live.antishare.cuboid.Cuboid;
 import com.turt2live.antishare.inventory.ASInventory;
 import com.turt2live.antishare.inventory.ASInventory.InventoryType;
 import com.turt2live.antishare.listener.RegionListener;
 import com.turt2live.antishare.regions.Region;
 import com.turt2live.antishare.regions.RegionConfiguration;
 import com.turt2live.antishare.regions.RegionKey.RegionKeyType;
 import com.turt2live.antishare.util.ASUtils;
 
 public class RegionManager extends AntiShareManager {
 
 	private Map<String, Set<Region>> regions = new HashMap<String, Set<Region>>();
 
 	/**
 	 * Creates a new Region Manager
 	 */
 	public RegionManager(){
 		plugin.getServer().getPluginManager().registerEvents(new RegionListener(this), plugin);
 	}
 
 	/**
 	 * Loads regions into memory for a specific world name
 	 * 
 	 * @param worldname the world
 	 */
 	public void loadWorld(String worldname){
 		File path = Region.REGION_INFORMATION;
 		if(!path.exists()){
 			path.mkdirs();
 		}
 		File[] list = path.listFiles();
 		if(list != null){
 			for(File file : list){
 				if(file.getName().endsWith(".yml")){
 					Region region = Region.fromFile(file);
 					if(region != null && region.getWorldName().equals(worldname)){
 						Set<Region> set = new HashSet<Region>();
 						if(regions.containsKey(region.getWorldName())){
 							set.addAll(regions.get(region.getWorldName()));
 						}
 						set.add(region);
 						regions.put(region.getWorldName(), set);
 					}
 				}
 			}
 		}
 		if(regions.keySet().size() > 0){
 			plugin.getLogger().info("Regions Loaded: " + regions.keySet().size());
 		}
 	}
 
 	/**
 	 * Loads all reachable regions into memory
 	 */
 	@Override
 	public boolean load(){
 		File path = Region.REGION_INFORMATION;
 		if(!path.exists()){
 			path.mkdirs();
 		}
 		File[] list = path.listFiles();
 		if(list != null){
 			for(File file : list){
 				if(file.getName().endsWith(".yml")){
 					Region region = Region.fromFile(file);
 					if(region != null){
 						Set<Region> set = new HashSet<Region>();
 						if(regions.containsKey(region.getWorldName())){
 							set.addAll(regions.get(region.getWorldName()));
 						}
 						set.add(region);
 						regions.put(region.getWorldName(), set);
 					}
 				}
 			}
 		}
 		if(regions.keySet().size() > 0){
 			plugin.getLogger().info("Regions Loaded: " + regions.keySet().size());
 		}
 		return true;
 	}
 
 	/**
 	 * Saves all loaded regions to disk, this overwrites (and deletes unloaded regions) any regions in the save folders
 	 */
 	@Override
 	public boolean save(){
 		ASUtils.wipeFolder(Region.REGION_CONFIGURATIONS, null);
 		ASUtils.wipeFolder(Region.REGION_INFORMATION, null);
 		for(String world : regions.keySet()){
 			Set<Region> regions = this.regions.get(world);
 			for(Region region : regions){
 				region.save();
 			}
 		}
 		regions.clear();
 		return true;
 	}
 
 	/**
 	 * Determines if a location is in a region
 	 * 
 	 * @param location the location
 	 * @return true if contained in a region
 	 */
 	public boolean isRegion(Location location){
 		String worldname = location.getWorld().getName();
 		if(!regions.containsKey(worldname)){
 			return false;
 		}
 		Set<Region> regions = this.regions.get(worldname);
 		for(Region region : regions){
 			if(region.getCuboid().isContained(location)){
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Gets the region at a location
 	 * 
 	 * @param location the location
 	 * @return the region, or null if not found
 	 */
 	public Region getRegion(Location location){
 		String worldname = location.getWorld().getName();
 		if(!regions.containsKey(worldname)){
 			return null;
 		}
 		Set<Region> regions = this.regions.get(worldname);
 		for(Region region : regions){
 			if(region.getCuboid().isContained(location)){
 				return region;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Gets a region by name. Case in-sensitive
 	 * 
 	 * @param name the region name
 	 * @return the region, or null if not found
 	 */
 	public Region getRegion(String name){
 		for(World world : plugin.getServer().getWorlds()){
 			if(regions.containsKey(world.getName())){
 				Set<Region> regions = this.regions.get(world.getName());
 				for(Region region : regions){
 					if(region.getName().equalsIgnoreCase(name)){
 						return region;
 					}
 				}
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Adds a region to the manager
 	 * 
 	 * @param cuboid the cuboid, this should not overlap other regions (but is not checked here for that)
 	 * @param owner the region owner
 	 * @param name the region name
 	 * @param gamemode the region's Game Mode
 	 */
 	public void addRegion(Cuboid cuboid, String owner, String name, GameMode gamemode){
 		Region region = new Region();
 		region.setCuboid(cuboid);
 		region.setWorld(cuboid.getWorld());
 		region.setOwner(owner);
 		region.setName(name);
 		region.setGameMode(gamemode);
 		region.setConfig(new RegionConfiguration(region));
 		region.setID(String.valueOf(System.nanoTime()));
 		Set<Region> regions = new HashSet<Region>();
 		if(this.regions.containsKey(cuboid.getWorld().getName())){
 			regions.addAll(this.regions.get(cuboid.getWorld().getName()));
 		}
 		regions.add(region);
 		region.onCreate();
 		this.regions.put(cuboid.getWorld().getName(), regions);
 	}
 
 	void inject(Region region){
 		Set<Region> regions = new HashSet<Region>();
 		if(this.regions.containsKey(region.getWorldName())){
 			regions.addAll(this.regions.get(region.getWorldName()));
 		}
 		regions.add(region);
 		region.onCreate();
 		this.regions.put(region.getWorldName(), regions);
 	}
 
 	/**
 	 * Removes a region by name. Case in-senstive
 	 * 
 	 * @param name the region name.
 	 */
 	public void removeRegion(String name){
 		for(World world : plugin.getServer().getWorlds()){
 			if(regions.containsKey(world.getName())){
 				Set<Region> regions = this.regions.get(world.getName());
 				Iterator<Region> iterator = regions.iterator();
				List<Region> remove = new ArrayList<Region>();
 				while (iterator.hasNext()){
 					Region region = iterator.next();
 					if(region.getName().equalsIgnoreCase(name)){
						remove.add(region);
 					}
 				}
				for(Region region : remove){
					region.onDelete();
					regions.remove(region);
				}
 				this.regions.put(world.getName(), regions);
 			}
 		}
 	}
 
 	/**
 	 * Determines if a region name is in use
 	 * 
 	 * @param name the region name
 	 * @return true if in use
 	 */
 	public boolean isRegionNameTaken(String name){
 		return getRegion(name) != null;
 	}
 
 	/**
 	 * Gets all the regions for a world
 	 * 
 	 * @param world the world
 	 * @return a set of regions, this will NEVER be null
 	 */
 	public Set<Region> getAllRegions(World world){
 		Set<Region> returnableRegions = new HashSet<Region>();
 		if(regions.containsKey(world.getName())){
 			returnableRegions.addAll(regions.get(world.getName()));
 		}
 		return returnableRegions;
 	}
 
 	/**
 	 * Gets all the loaded regions
 	 * 
 	 * @return a set of regions, this will NEVER be null
 	 */
 	public Set<Region> getAllRegions(){
 		Set<Region> returnableRegions = new HashSet<Region>();
 		for(World world : plugin.getServer().getWorlds()){
 			returnableRegions.addAll(getAllRegions(world));
 		}
 		return returnableRegions;
 	}
 
 	/**
 	 * Gets all regions of a specific game mode
 	 * 
 	 * @param gamemode the game mode to search
 	 * @return a set of regions, this will NEVER be null
 	 */
 	public Set<Region> getAllRegions(GameMode gamemode){
 		Set<Region> returnableRegions = new HashSet<Region>();
 		for(Region region : getAllRegions()){
 			if(region.getGameMode() == gamemode){
 				returnableRegions.add(region);
 			}
 		}
 		return returnableRegions;
 	}
 
 	/**
 	 * Updates (edits) a region
 	 * 
 	 * @param region the region to edit
 	 * @param key the variable key to change
 	 * @param value the value of the change
 	 * @param sender the command sender applying the change
 	 */
 	public void updateRegion(Region region, RegionKeyType key, String value, CommandSender sender){
 		boolean changed = false;
 		Cuboid last = region.getCuboid(); // Pre-cloned
 		switch (key){
 		case NAME:
 			if(isRegionNameTaken(value)){
 				ASUtils.sendToPlayer(sender, ChatColor.RED + "Region name '" + value + "' already exists!", true);
 			}else{
 				region.setName(value);
 				changed = true;
 			}
 			break;
 		case ENTER_MESSAGE_SHOW:
 			if(ASUtils.getBoolean(value) != null){
 				region.setShowEnterMessage(ASUtils.getBoolean(value));
 				changed = true;
 			}else{
 				ASUtils.sendToPlayer(sender, ChatColor.RED + "Value '" + value + "' is unknown, did you mean 'true' or 'false'?", true);
 			}
 			break;
 		case EXIT_MESSAGE_SHOW:
 			if(ASUtils.getBoolean(value) != null){
 				region.setShowExitMessage(ASUtils.getBoolean(value));
 				changed = true;
 			}else{
 				ASUtils.sendToPlayer(sender, ChatColor.RED + "Value '" + value + "' is unknown, did you mean 'true' or 'false'?", true);
 			}
 			break;
 		case INVENTORY:
 			if(value.equalsIgnoreCase("none")){
 				region.setInventory(null);
 				changed = true;
 			}else if(value.equalsIgnoreCase("set")){
 				if(sender instanceof Player){
 					region.setInventory(ASInventory.generate((Player) sender, InventoryType.REGION));
 					changed = true;
 				}else{
 					ASUtils.sendToPlayer(sender, ChatColor.RED + "You can't set an inventory from the console, only clear.", true);
 				}
 			}else{
 				ASUtils.sendToPlayer(sender, ChatColor.RED + "Value '" + value + "' is unknown to me, did you mean 'none' or 'set'?", true);
 			}
 			break;
 		case SELECTION_AREA:
 			if(!(sender instanceof Player)){
 				ASUtils.sendToPlayer(sender, ChatColor.DARK_RED + "You are not a player, sorry!", true);
 				break;
 			}
 			Player player = (Player) sender;
 			if(((CuboidManager) plugin.getSystemsManager().getManager(Manager.CUBOID)).isCuboidComplete(player.getName())){
 				Cuboid cuboid = ((CuboidManager) plugin.getSystemsManager().getManager(Manager.CUBOID)).getCuboid(player.getName());
 				region.setCuboid(cuboid);
 				changed = true;
 			}else{
 				ASUtils.sendToPlayer(sender, ChatColor.RED + "You need to use the Cuboid tool to create a cuboid.", true);
 			}
 			break;
 		case GAMEMODE:
 			if(value.equalsIgnoreCase("creative") || value.equalsIgnoreCase("c") || value.equalsIgnoreCase("1")){
 				region.setGameMode(GameMode.CREATIVE);
 				changed = true;
 			}else if(value.equalsIgnoreCase("survival") || value.equalsIgnoreCase("s") || value.equalsIgnoreCase("0")){
 				region.setGameMode(GameMode.SURVIVAL);
 				changed = true;
 			}else{
 				ASUtils.sendToPlayer(sender, ChatColor.RED + "I don't know what Game Mode '" + value + "' is!", true);
 			}
 			break;
 		case ENTER_MESSAGE:
 			region.setEnterMessage(value);
 			changed = true;
 			break;
 		case EXIT_MESSAGE:
 			region.setExitMessage(value);
 			changed = true;
 			break;
 		default:
 			break;
 		}
 		if(changed){
 			region.onUpdate(last);
 			ASUtils.sendToPlayer(sender, ChatColor.GREEN + "Region saved.", true);
 		}
 	}
 
 }
