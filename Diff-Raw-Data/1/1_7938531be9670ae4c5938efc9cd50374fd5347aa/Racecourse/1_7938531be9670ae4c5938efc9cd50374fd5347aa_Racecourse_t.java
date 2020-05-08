 package uk.thecodingbadgers.minekart.racecourse;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Random;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.Sound;
 import org.bukkit.World;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Item;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.ItemMeta;
 import org.bukkit.util.Vector;
 
 import com.sk89q.worldedit.IncompleteRegionException;
 import com.sk89q.worldedit.bukkit.WorldEditPlugin;
 import com.sk89q.worldedit.bukkit.selections.Selection;
 import com.sk89q.worldedit.regions.CuboidRegion;
 import com.sk89q.worldedit.regions.Region;
 
 import uk.thecodingbadgers.minekart.MineKart;
 import uk.thecodingbadgers.minekart.jockey.Jockey;
 import uk.thecodingbadgers.minekart.race.Race;
 import uk.thecodingbadgers.minekart.race.RaceSinglePlayer;
 import uk.thecodingbadgers.minekart.race.RaceState;
 
 /**
  * @author TheCodingBadgers
  * 
  *         The base racecourse class, used to define a racecourse
  * 
  */
 public abstract class Racecourse {
 
 	/** The world that the racecouse resides in */
 	protected World world = null;
 
 	/** The bounds of the racecourse */
 	protected Region bounds = null;
 
 	/** The name of the racecourse */
 	protected String name = null;
 
 	/** The string representation of this course type */
 	protected String type = null;
 
 	/** A map of all registered multi point location sets */
 	protected Map<String, List<Location>> multiPoints = null;
 
 	/** A map of all registered single point locations */
 	protected Map<String, Location> singlePoints = null;
 
 	/** The file configuration used by this racecourse */
 	protected File fileConfiguration = null;
 
 	/** The race which uses this course */
 	protected Race race = null;
 
 	/** The entity type to use as a mount */
 	protected EntityType mountType = EntityType.HORSE;
 
 	/** Is the course enabled */
 	protected boolean enabled = true;
 
 	/** All spawned powerupItems */
 	protected List<Item> powerupItems = null;
 
 	/** The block the jockeys have to hit to ready up */
 	protected Material readyblock;
 
 	/** The minimum number of jockeys required for a race to take place **/
 	protected int minimumNoofPlayers = 2;
 
 	/** The cooldown between pickuping up powerups **/
 	protected int powerupCooldown = 2;
 
 	/**
 	 * Class constructor
 	 */
 	public Racecourse() {
 
 		this.multiPoints = new HashMap<String, List<Location>>();
 		this.singlePoints = new HashMap<String, Location>();
 		this.powerupItems = new ArrayList<Item>();
 
 		registerWarp(Bukkit.getConsoleSender(), "spawn", "add");
 		registerWarp(Bukkit.getConsoleSender(), "powerup", "add");
 		registerWarp(Bukkit.getConsoleSender(), "lobby", "set");
 		registerWarp(Bukkit.getConsoleSender(), "spectate", "set");
 	}
 
 	/**
 	 * Setup the racecourse. Setting up the bounds of the arena based on
 	 * player world edit seleciton.
 	 * 
 	 * @param player The player who is setting up the course
 	 * @return True if the location is within the course bounds, false
 	 *         otherwise.
 	 */
 	public boolean setup(Player player, String name) {
 
 		WorldEditPlugin worldEdit = MineKart.getInstance().getWorldEditPlugin();
 		Selection seleciton = worldEdit.getSelection(player);
 		if (seleciton == null) {
 			MineKart.output(player, "Please make a world edit selection covering the bounds of the racecourse...");
 			return false;
 		}
 
 		// Set the arena bounds from the selection
 		world = seleciton.getWorld();
 		try {
 			bounds = seleciton.getRegionSelector().getRegion().clone();
 		} catch (IncompleteRegionException e) {
 			MineKart.output(player, "An invalid selection was made using world edit. Please make a complete cuboid selection and try again.");
 			return false;
 		}
 
 		this.name = name;
		this.readyblock = Material.IRON_BLOCK;
 
 		this.fileConfiguration = new File(MineKart.getRacecourseFolder() + File.separator + this.name + "." + this.type + ".yml");
 		if (!this.fileConfiguration.exists()) {
 			try {
 				if (!this.fileConfiguration.createNewFile()) {
 					MineKart.output(player, "Failed to create config file for racecourse '" + this.name + "' at the following location");
 					MineKart.output(player, this.fileConfiguration.getAbsolutePath());
 					return false;
 				}
 			} catch (Exception ex) {
 				MineKart.output(player, "Failed to create config file for racecourse '" + this.name + "' at the following location");
 				MineKart.output(player, this.fileConfiguration.getAbsolutePath());
 				return false;
 			}
 		}
 
 		this.race = new RaceSinglePlayer();
 		this.race.setCourse(this);
 
 		return true;
 	}
 
 	/**
 	 * Check to see if a given location is within the course bounds.
 	 * 
 	 * @param location The location to check
 	 * @return True if the location is within the course bounds, false
 	 *         otherwise.
 	 */
 	public boolean isInCourseBounds(Location location) {
 
 		// Is the location in the same world
 		if (!location.getWorld().equals(this.world))
 			return false;
 
 		// Create a world edit vector and test against the course bounds
 		com.sk89q.worldedit.Vector vec = new com.sk89q.worldedit.Vector(location.getX(), location.getY(), location.getZ());
 
 		return this.bounds.contains(vec);
 	}
 
 	/**
 	 * Load the racecourse from file.
 	 */
 	@SuppressWarnings("deprecation")
 	public void load(File configfile) {
 
 		FileConfiguration file = YamlConfiguration.loadConfiguration(configfile);
 		this.fileConfiguration = configfile;
 
 		// Course name
 		this.name = file.getString("racecourse.name");
 
 		// Course bounds
 		this.world = Bukkit.getWorld(file.getString("racecourse.world"));
 		this.bounds = loadRegion(file, "racecourse.bounds");
 
 		// Mount settings
 		this.mountType = EntityType.fromName(file.getString("mount.type", "EntityHorse"));
 
 		// Lobby settings
 		this.readyblock = Material.getMaterial(file.getString("lobby.readyblock", "IRON_BLOCK"));
 		this.minimumNoofPlayers = file.getInt("racecourse.minimumJockeys", 2);
 		this.powerupCooldown = file.getInt("racecourse.powerupCooldown", 500);
 
 		// Single point locations
 		int noofSinglePoints = file.getInt("racecourse.singlepoint.count");
 		for (int pointIndex = 0; pointIndex < noofSinglePoints; ++pointIndex) {
 			final String path = "racecourse.singlepoint." + pointIndex;
 			final String name = file.getString(path + ".name");
 			final Location location = loadLocation(file, path + ".location");
 			this.singlePoints.put(name, location);
 		}
 
 		// Multi-point locations
 		int noofMultiPoints = file.getInt("racecourse.multipoint.count");
 		for (int pointIndex = 0; pointIndex < noofMultiPoints; ++pointIndex) {
 			final String path = "racecourse.multipoint." + pointIndex;
 			List<Location> locations = new ArrayList<Location>();
 
 			final String name = file.getString(path + ".name");
 			final int noofLocations = file.getInt(path + ".count");
 			for (int locationIndex = 0; locationIndex < noofLocations; ++locationIndex) {
 				locations.add(loadLocation(file, path + ".location." + locationIndex));
 			}
 
 			this.multiPoints.put(name, locations);
 		}
 
 		this.race = new RaceSinglePlayer();
 		this.race.setCourse(this);
 	}
 
 	/**
 	 * Save the racecourse to file.
 	 */
 	@SuppressWarnings("deprecation")
 	public void save() {
 
 		FileConfiguration file = YamlConfiguration.loadConfiguration(this.fileConfiguration);
 
 		// Course name
 		file.set("racecourse.name", this.name);
 
 		// Course bounds
 		file.set("racecourse.world", this.world.getName());
 		saveRegion(file, "racecourse.bounds", this.bounds);
 
 		// Mount settings
 		file.set("mount.type", this.mountType.getName());
 
 		// Lobby settings
 		file.set("lobby.readyblock", this.readyblock.name());
 		file.set("racecourse.minimumJockeys", this.minimumNoofPlayers);
 		file.set("racecourse.powerupCooldown", this.powerupCooldown);
 
 		// Single point locations
 		file.set("racecourse.singlepoint.count", this.singlePoints.size());
 		int pointIndex = 0;
 		for (Entry<String, Location> point : this.singlePoints.entrySet()) {
 
 			if (point.getValue() == null) {
 				continue;
 			}
 
 			final String path = "racecourse.singlepoint." + pointIndex;
 			file.set(path + ".name", point.getKey());
 			saveLocation(file, path + ".location", point.getValue());
 			pointIndex++;
 		}
 
 		// Multi-point locations
 		file.set("racecourse.multipoint.count", this.multiPoints.size());
 		pointIndex = 0;
 		for (Entry<String, List<Location>> point : this.multiPoints.entrySet()) {
 			final String path = "racecourse.multipoint." + pointIndex;
 			List<Location> locations = point.getValue();
 
 			if (locations == null || locations.isEmpty()) {
 				continue;
 			}
 
 			file.set(path + ".name", point.getKey());
 			file.set(path + ".count", locations.size());
 			int locationIndex = 0;
 			for (Location location : locations) {
 				saveLocation(file, path + ".location." + locationIndex, location);
 				locationIndex++;
 			}
 			pointIndex++;
 		}
 
 		try {
 			file.save(this.fileConfiguration);
 		} catch (Exception ex) {
 		}
 	}
 
 	/**
 	 * Save a given location to a file configuration
 	 * 
 	 * @param file The file to save too
 	 * @param path The path in the file config
 	 * @param location The location to save.
 	 */
 	protected void saveLocation(FileConfiguration file, String path, Location location) {
 		file.set(path + ".x", location.getX());
 		file.set(path + ".y", location.getY());
 		file.set(path + ".z", location.getZ());
 		file.set(path + ".pitch", location.getPitch());
 		file.set(path + ".yaw", location.getYaw());
 	}
 
 	/**
 	 * Load a given location from a file configuration
 	 * 
 	 * @param file The file to save too
 	 * @param path The path in the file config
 	 * @return The loaded location.
 	 */
 	protected Location loadLocation(FileConfiguration file, String path) {
 		Double x = file.getDouble(path + ".x");
 		Double y = file.getDouble(path + ".y");
 		Double z = file.getDouble(path + ".z");
 		float pitch = (float) file.getDouble(path + ".pitch");
 		float yaw = (float) file.getDouble(path + ".yaw");
 
 		return new Location(this.world, x, y, z, yaw, pitch);
 	}
 
 	/**
 	 * Save a given region to a file configuration
 	 * 
 	 * @param file The file to save too
 	 * @param path The path in the file config
 	 * @param region The region to save.
 	 */
 	protected void saveRegion(FileConfiguration file, String path, Region region) {
 		file.set(path + ".min.x", region.getMinimumPoint().getX());
 		file.set(path + ".min.y", region.getMinimumPoint().getY());
 		file.set(path + ".min.z", region.getMinimumPoint().getZ());
 		file.set(path + ".max.x", region.getMaximumPoint().getX());
 		file.set(path + ".max.y", region.getMaximumPoint().getY());
 		file.set(path + ".max.z", region.getMaximumPoint().getZ());
 	}
 
 	/**
 	 * Load a given region from a file configuration
 	 * 
 	 * @param file The file to save too
 	 * @param path The path in the file config
 	 * @return The loaded region.
 	 */
 	protected Region loadRegion(FileConfiguration file, String path) {
 		Double minX = file.getDouble(path + ".min.x");
 		Double minY = file.getDouble(path + ".min.y");
 		Double minZ = file.getDouble(path + ".min.z");
 		Double maxX = file.getDouble(path + ".max.x");
 		Double maxY = file.getDouble(path + ".max.y");
 		Double maxZ = file.getDouble(path + ".max.z");
 
 		return new CuboidRegion(new com.sk89q.worldedit.Vector(minX, minY, minZ), new com.sk89q.worldedit.Vector(maxX, maxY, maxZ));
 	}
 
 	/**
 	 * Output the remaining requirements to complete this arena
 	 * 
 	 * @param sender The sender to receive the output information
 	 * @return True if all requirements have been met
 	 */
 	public boolean outputRequirements(CommandSender sender) {
 
 		boolean fullySetup = true;
 
 		// single points
 		for (Entry<String, Location> point : this.singlePoints.entrySet()) {
 			if (point.getValue() == null) {
 				MineKart.output(sender, " - Add a " + point.getKey() + " spawn point [/mk set" + point.getKey() + " <coursename>]");
 				fullySetup = false;
 			}
 		}
 
 		// multi-points
 		for (Entry<String, List<Location>> point : this.multiPoints.entrySet()) {
 			if (point.getValue() == null || point.getValue().size() < 2) {
 				MineKart.output(sender, " - Add " + point.getKey() + "s (minimum of 2 required) [/mk add" + point.getKey() + " <coursename>]");
 				fullySetup = false;
 			}
 		}
 
 		return fullySetup;
 	}
 
 	/**
 	 * Output all information about this racecourse
 	 * 
 	 * @param sender The thing to tell the information
 	 */
 	public void outputInformation(CommandSender sender) {
 
 		MineKart.output(sender, "Course Name: " + this.name);
 		MineKart.output(sender, "World: " + this.world.getName());
 		MineKart.output(sender, "Bounds: " + this.bounds.toString());
 		MineKart.output(sender, "-------------");
 
 		for (Entry<String, Location> point : this.singlePoints.entrySet()) {
 			if (point.getValue() != null) {
 				MineKart.output(sender, point.getKey() + ": " + point.getValue().toString());
 			}
 		}
 		MineKart.output(sender, "-------------");
 
 		for (Entry<String, List<Location>> point : this.multiPoints.entrySet()) {
 			if (point.getValue() != null && !point.getValue().isEmpty()) {
 				MineKart.output(sender, point.getKey());
 				for (Location location : point.getValue()) {
 					MineKart.output(sender, " - " + location.toString());
 				}
 			}
 		}
 		MineKart.output(sender, "-------------");
 
 	}
 
 	/**
 	 * Register a warp type
 	 * 
 	 * @param player The player registering the warp
 	 * @param name The name of the warp to register
 	 * @param type The type of warp to register, set or add.
 	 */
 	public void registerWarp(CommandSender player, String name, String type) {
 
 		if (type.equalsIgnoreCase("set")) {
 			if (this.singlePoints.containsKey(name))
 				return;
 
 			this.singlePoints.put(name, null);
 		} else if (type.equalsIgnoreCase("add")) {
 			if (this.multiPoints.containsKey(name))
 				return;
 
 			this.multiPoints.put(name, new ArrayList<Location>());
 		} else {
 			MineKart.output(player, "Unknown warp type '" + type + "', please use 'set' or 'add'");
 		}
 
 	}
 
 	/**
 	 * Set a single point warp
 	 * 
 	 * @param player The player setting the warp
 	 * @param warpname The name of the warp to set
 	 */
 	public void setWarp(Player player, String warpname) {
 
 		if (!this.singlePoints.containsKey(warpname)) {
 			MineKart.output(player, "There is no single point warp with the name '" + warpname + "'.");
 			return;
 		}
 
 		this.singlePoints.remove(warpname);
 		this.singlePoints.put(warpname, player.getLocation());
 		MineKart.output(player, "The point " + warpname + " has been set!");
 	}
 
 	/**
 	 * Add a multi-point warp
 	 * 
 	 * @param player The player adding the warp
 	 * @param warpname The name of the warp to add to
 	 */
 	public void addWarp(Player player, String warpname) {
 
 		if (!this.multiPoints.containsKey(warpname)) {
 			MineKart.output(player, "There is no multi-point warp with the name '" + warpname + "'.");
 			return;
 		}
 
 		List<Location> locations = this.multiPoints.get(warpname);
 		this.multiPoints.remove(warpname);
 		locations.add(player.getLocation());
 		this.multiPoints.put(warpname, locations);
 		MineKart.output(player, "A new point has been added to " + warpname + "!");
 		MineKart.output(player, warpname + " now has " + locations.size() + " points.");
 	}
 
 	/**
 	 * Get the name of the racecourse
 	 * 
 	 * @return The name of the racecourse
 	 */
 	public String getName() {
 		return this.name;
 	}
 
 	/**
 	 * Get the race which uses the racecourse
 	 * 
 	 * @return The race instance
 	 */
 	public Race getRace() {
 		return this.race;
 	}
 
 	/**
 	 * Get a warp by its name
 	 * 
 	 * @param warpname The name of the warp to find
 	 * @return The location of the given warp, or null if a warp wasn't found
 	 */
 	public Location getWarp(String warpname) {
 
 		if (this.singlePoints.containsKey(warpname)) {
 			return this.singlePoints.get(warpname);
 		}
 
 		return null;
 	}
 
 	/**
 	 * Get a multi warp by its name
 	 * 
 	 * @param warpname The name of the warp to find
 	 * @return The list of locations of the given warp, or null if a warp
 	 *         wasn't found
 	 */
 	public List<Location> getMultiWarp(String warpname) {
 
 		if (this.multiPoints.containsKey(warpname)) {
 			return this.multiPoints.get(warpname);
 		}
 
 		return null;
 	}
 
 	/**
 	 * Get the bounds of the racecourse
 	 * 
 	 * @return The bounds of the racecourse
 	 */
 	public Object getBounds() {
 		return this.bounds;
 	}
 
 	/**
 	 * Called when a jockey moves
 	 * 
 	 * @param jockey The jockey who moved
 	 * @param race The race the jockeys are in
 	 */
 	public boolean onJockeyMove(Jockey jockey, Race race) {
 
 		if (race.getState() != RaceState.InRace)
 			return false;
 
 		Location location = jockey.getPlayer().getLocation();
 		com.sk89q.worldedit.Vector position = new com.sk89q.worldedit.Vector(location.getBlockX(), location.getBlockY(), location.getBlockZ());
 
 		if (!this.bounds.contains(position)) {
 			race.outputToRace("The jockey " + ChatColor.YELLOW + jockey.getPlayer().getName() + ChatColor.WHITE + " has left the race course.");
 			race.removeJockey(jockey);
 			return false;
 		}
 
 		return true;
 	}
 
 	/**
 	 * Called when a race starts
 	 * 
 	 * @param race The race which is starting
 	 */
 	public void onRaceStart(Race race) {
 
 		List<Location> powerups = this.multiPoints.get("powerup");
 		for (Location location : powerups) {
 			Location spawnLocation = new Location(location.getWorld(), location.getX(), location.getY() + 1.0, location.getZ());
 			spawnPowerup(spawnLocation);
 		}
 	}
 
 	/**
 	 * 
 	 * @param location
 	 */
 	private void spawnPowerup(Location location) {
 
 		ItemStack powerup = new ItemStack(Material.CHEST); // TODO: Make configrable
 		ItemMeta meta = powerup.getItemMeta();
 		meta.setDisplayName("Powerup " + (new Random()).nextInt());
 		powerup.setItemMeta(meta);
 		powerup.addUnsafeEnchantment(Enchantment.PROTECTION_FIRE, 0);
 
 		Item item = location.getWorld().dropItem(location, powerup);
 		item.setVelocity(new Vector(0, 0, 0));
 		this.powerupItems.add(item);
 
 		location.getWorld().playSound(location, Sound.FIREWORK_TWINKLE, 1.0f, 1.0f);
 
 	}
 
 	/**
 	 * Called when a race ends
 	 * 
 	 * @param race The race which is ending
 	 */
 	public void onRaceEnd(Race race) {
 
 		for (Item powerup : this.powerupItems) {
 			powerup.remove();
 		}
 
 	}
 
 	/**
 	 * Get the mount type this race course uses
 	 * 
 	 * @return The EntityType that this course uses as a mount
 	 */
 	public EntityType getMountType() {
 		return this.mountType;
 	}
 
 	/**
 	 * Set the mount type this race course uses
 	 * 
 	 * @param mountType The EntityType that this course should use as a mount
 	 */
 	public void setMountType(EntityType mountType) {
 		this.mountType = mountType;
 		this.save();
 	}
 
 	/**
 	 * Set the enabled state of the course
 	 * 
 	 * @param enabled True to enable the course, False to disable
 	 */
 	public void setEnabled(boolean enabled) {
 		this.enabled = enabled;
 	}
 
 	/**
 	 * Get the enabled state of the race course
 	 * 
 	 * @return True if enabled, fale if disabled.
 	 */
 	public boolean isEnabled() {
 		return this.enabled;
 	}
 
 	/**
 	 * 
 	 * @param powerup
 	 */
 	public void removePowerup(Location location) {
 		this.powerupItems.remove(location);
 		location.getWorld().playSound(location, Sound.VILLAGER_YES, 1.0f, 1.0f);
 
 		final Location spawnLocation = new Location(location.getWorld(), location.getX(), location.getY(), location.getZ());
 
 		Bukkit.getScheduler().runTaskLater(MineKart.getInstance(), new Runnable() {
 
 			@Override
 			public void run() {
 				spawnPowerup(spawnLocation);
 			}
 
 		}, 5 * 20L);
 	}
 
 	/**
 	 * Gets the block the jockeys have to hit to "ready up"
 	 * 
 	 * @return The block the jockeys have to hit to "ready up"
 	 */
 	public Material getReadyBlock() {
 		return this.readyblock;
 	}
 
 	/**
 	 * Get the minimum number of players need to for a race to take place
 	 * 
 	 * @return The minimum number of players
 	 */
 	public int getMinimumPlayers() {
 		return this.minimumNoofPlayers;
 	}
 
 	/**
 	 * Gets the time between players being able to pickup powerups, in
 	 * milliseconds
 	 * 
 	 * @return the time between players being able to pickup powerups, in
 	 *         milliseconds
 	 */
 	public long getPowerupCooldown() {
 		return powerupCooldown;
 	}
 }
