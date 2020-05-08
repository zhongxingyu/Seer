 package com.censoredsoftware.demigods.location;
 
 import com.censoredsoftware.demigods.Demigods;
 import com.censoredsoftware.demigods.data.DataManager;
 import com.censoredsoftware.demigods.helper.ConfigFile;
 import com.censoredsoftware.demigods.util.Randoms;
 import com.google.common.base.Predicate;
 import com.google.common.collect.Iterators;
 import com.google.common.collect.Sets;
 import org.bukkit.Bukkit;
 import org.bukkit.Chunk;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.serialization.ConfigurationSerializable;
 
 import java.util.*;
 import java.util.concurrent.ConcurrentHashMap;
 
 public class DLocation implements ConfigurationSerializable
 {
 	private UUID id;
 	String world;
 	Double X;
 	Double Y;
 	Double Z;
 	Float pitch;
 	Float yaw;
 	String region;
 
 	public DLocation()
 	{}
 
 	public DLocation(UUID id, ConfigurationSection conf)
 	{
 		this.id = id;
 		world = conf.getString("world");
 		X = conf.getDouble("X");
 		Y = conf.getDouble("Y");
 		Z = conf.getDouble("Z");
 		pitch = Float.parseFloat(conf.getString("pitch"));
 		yaw = Float.parseFloat(conf.getString("yaw"));
 		region = conf.getString("region");
 	}
 
 	@Override
 	public Map<String, Object> serialize()
 	{
 		Map<String, Object> map = new HashMap<String, Object>();
 		map.put("world", world);
 		map.put("X", X);
 		map.put("Y", Y);
 		map.put("Z", Z);
 		map.put("pitch", String.valueOf(pitch));
 		map.put("yaw", String.valueOf(yaw));
 		map.put("region", region);
 		return map;
 	}
 
 	public void generateId()
 	{
 		id = UUID.randomUUID();
 	}
 
 	void setWorld(String world)
 	{
 		this.world = world;
 	}
 
 	void setX(Double X)
 	{
 		this.X = X;
 	}
 
 	void setY(Double Y)
 	{
 		this.Y = Y;
 	}
 
 	void setZ(Double Z)
 	{
 		this.Z = Z;
 	}
 
 	void setYaw(Float yaw)
 	{
 		this.yaw = yaw;
 	}
 
 	void setPitch(Float pitch)
 	{
 		this.pitch = pitch;
 	}
 
 	void setRegion(Region region)
 	{
 		this.region = region.toString();
 	}
 
 	public Location toLocation() throws NullPointerException
 	{
 		return new Location(Bukkit.getServer().getWorld(this.world), this.X, this.Y, this.Z, this.yaw, this.pitch);
 	}
 
 	public UUID getId()
 	{
 		return this.id;
 	}
 
 	public Double getX()
 	{
 		return this.X;
 	}
 
 	public Double getY()
 	{
 		return this.Y;
 	}
 
 	public Double getZ()
 	{
 		return this.Z;
 	}
 
 	public String getWorld()
 	{
 		return this.world;
 	}
 
 	public Region getRegion()
 	{
 		return Region.Util.getRegion(toLocation());
 	}
 
 	@Override
 	public Object clone() throws CloneNotSupportedException
 	{
 		throw new CloneNotSupportedException();
 	}
 
 	public static class File extends ConfigFile
 	{
 		private static String SAVE_PATH;
 		private static final String SAVE_FILE = "locations.yml";
 
 		public File()
 		{
 			super(Demigods.plugin);
 			SAVE_PATH = Demigods.plugin.getDataFolder() + "/data/";
 		}
 
 		@Override
 		public ConcurrentHashMap<UUID, DLocation> loadFromFile()
 		{
 			final FileConfiguration data = getData(SAVE_PATH, SAVE_FILE);
 			ConcurrentHashMap<UUID, DLocation> map = new ConcurrentHashMap<UUID, DLocation>();
 			for(String stringId : data.getKeys(false))
 				map.put(UUID.fromString(stringId), new DLocation(UUID.fromString(stringId), data.getConfigurationSection(stringId)));
 			return map;
 		}
 
 		@Override
 		public boolean saveToFile()
 		{
 			FileConfiguration saveFile = getData(SAVE_PATH, SAVE_FILE);
 			Map<UUID, DLocation> currentFile = loadFromFile();
 
 			for(UUID id : DataManager.locations.keySet())
 				if(!currentFile.keySet().contains(id) || !currentFile.get(id).equals(DataManager.locations.get(id))) saveFile.createSection(id.toString(), Util.load(id).serialize());
 
 			for(UUID id : currentFile.keySet())
 				if(!DataManager.locations.keySet().contains(id)) saveFile.set(id.toString(), null);
 
 			return saveFile(SAVE_PATH, SAVE_FILE, saveFile);
 		}
 	}
 
 	public static class Util
 	{
 		public static void save(DLocation location)
 		{
 			DataManager.locations.put(location.getId(), location);
 		}
 
 		public static void delete(UUID id)
 		{
 			DataManager.locations.remove(id);
 		}
 
 		public static DLocation load(UUID id)
 		{
 			return DataManager.locations.get(id);
 		}
 
 		public static DLocation create(String world, double X, double Y, double Z, float yaw, float pitch)
 		{
 			DLocation trackedLocation = new DLocation();
 			trackedLocation.generateId();
 			trackedLocation.setWorld(world);
 			trackedLocation.setX(X);
 			trackedLocation.setY(Y);
 			trackedLocation.setZ(Z);
 			trackedLocation.setYaw(yaw);
 			trackedLocation.setPitch(pitch);
 			trackedLocation.setRegion(Region.Util.getRegion((int) X, (int) Z, world));
 			save(trackedLocation);
 			return trackedLocation;
 		}
 
 		public static DLocation create(Location location)
 		{
 			return create(location.getWorld().getName(), location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
 		}
 
 		public static Set<DLocation> loadAll()
 		{
 			return Sets.newHashSet(DataManager.locations.values());
 		}
 
 		public static DLocation get(final Location location)
 		{
 			try
 			{
 				return Iterators.find(loadAll().iterator(), new Predicate<DLocation>()
 				{
 					@Override
 					public boolean apply(DLocation tracked)
 					{
 						return location.getX() == tracked.getX() && location.getY() == tracked.getY() && location.getBlockZ() == tracked.getZ() && location.getWorld().getName().equals(tracked.getWorld());
 					}
 				});
 			}
 			catch(NoSuchElementException ignored)
 			{}
 			return create(location);
 		}
 
 		/**
 		 * Randoms a random location with the center being <code>reference</code>.
 		 * Must be at least <code>min</code> blocks from the center and no more than <code>max</code> blocks away.
 		 * 
 		 * @param reference the location used as the center for reference.
 		 * @param min the minimum number of blocks away.
 		 * @param max the maximum number of blocks away.
 		 * @return the random location generated.
 		 */
 		public static Location randomLocation(Location reference, int min, int max)
 		{
 			Location location = reference.clone();
 			double randX = Randoms.generateIntRange(min, max);
 			double randZ = Randoms.generateIntRange(min, max);
 			location.add(randX, 0, randZ);
 			double highestY = location.clone().getWorld().getHighestBlockYAt(location);
 			location.setY(highestY);
 			return location;
 		}
 
 		/**
 		 * Returns a random location within the <code>chunk</code> passed in.
 		 * 
 		 * @param chunk the chunk that we will obtain the location from.
 		 * @return the random location generated.
 		 */
 		public static Location randomChunkLocation(Chunk chunk)
 		{
 			Location reference = chunk.getBlock(Randoms.generateIntRange(1, 16), 64, Randoms.generateIntRange(1, 16)).getLocation();
 			double locX = reference.getX();
 			double locY = chunk.getWorld().getHighestBlockYAt(reference);
 			double locZ = reference.getZ();
 			return new Location(chunk.getWorld(), locX, locY, locZ);
 		}
 
 		/**
 		 * Returns a set of blocks in a radius of <code>radius</code> at the provided <code>location</code>.
 		 * 
 		 * @param location the center location to getDesign the blocks from.
 		 * @param radius the radius around the center block from which to getDesign the blocks.
 		 * @return Set<Block>
 		 */
 		public static Set<Block> getBlocks(Location location, int radius)
 		{
 			// Define variables
 			Set<Block> blocks = Sets.newHashSet();
 			blocks.add(location.getBlock());
 
 			for(int x = 0; x <= radius; x++)
 				blocks.add(location.add(x, 0, x).getBlock());
 
 			return blocks;
 		}
 
 		public static Location getFloorBelowLocation(Location location)
 		{
 			if(location.getBlock().getType().isSolid()) return location;
 			return getFloorBelowLocation(location.getBlock().getRelative(BlockFace.DOWN).getLocation());
 		}
 
 		public static List<Location> getCirclePoints(Location center, final double radius, final int points)
 		{
 			final World world = center.getWorld();
 			final double X = center.getX();
 			final double Y = center.getY();
 			final double Z = center.getZ();
 			List<Location> list = new ArrayList<Location>();
 			for(int i = 0; i < points; i++)
 			{
 				double x = X + radius * Math.cos((2 * Math.PI * i) / points);
 				double z = Z + radius * Math.sin((2 * Math.PI * i) / points);
 				list.add(new Location(world, x, Y, z));
 			}
 			return list;
 		}
 
 		public static float toDegree(double angle)
 		{
 			return (float) Math.toDegrees(angle);
 		}
 
 		public static double distanceFlat(Location location1, Location location2)
 		{
 			double Y = location1.getY();
 			Location location3 = location2.clone();
 			location3.setY(Y);
 			return location1.distance(location3);
 		}
 	}
 }
