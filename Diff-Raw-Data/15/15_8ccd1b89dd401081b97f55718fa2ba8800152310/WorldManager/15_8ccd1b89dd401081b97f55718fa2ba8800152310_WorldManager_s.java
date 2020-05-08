 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package multiworld.data;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import multiworld.ConfigException;
 import multiworld.InvalidWorldNameException;
 import multiworld.Utils;
 import multiworld.WorldGenException;
 import multiworld.api.MultiWorldWorldData;
 import multiworld.api.events.FlagChanceEvent;
 import multiworld.api.events.WorldCreateEvent;
 import multiworld.api.flag.FlagName;
 import multiworld.flags.FlagMap;
 import multiworld.flags.FlagValue;
 import multiworld.worldgen.NullGen;
 import multiworld.worldgen.WorldGenerator;
 import org.bukkit.Bukkit;
 import org.bukkit.Difficulty;
 import org.bukkit.World;
 import org.bukkit.WorldCreator;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.generator.ChunkGenerator;
 
 /**
  *
  * @author Fernando
  */
 public class WorldManager implements WorldUntils
 {
 	private final Map<String, WorldContainer> worlds;
 
 	WorldManager()
 	{
 		this.worlds = new HashMap<String, WorldContainer>();
 	}
 
 	@Override
 	public World getWorld(String name)
 	{
 		return this.getWorldMeta(name, true).getBukkitWorld();
 	}
 
 	@Override
 	public InternalWorld getInternalWorld(String name, boolean mustBeLoaded)
 	{
 		if (name == null)
 		{
			throw new NullPointerException("Name may not be null");
 		}
 		WorldContainer w = this.getWorldMeta(name, mustBeLoaded);
 		if (w == null)
 		{
 			return null;
 		}
 		if (!mustBeLoaded)
 		{
 			return w.getWorld();
 		}
 		else
 		{
 			return w.isLoaded() ? w.getWorld() : null;
 		}
 	}
 
 	@Override
 	public boolean isWorldLoaded(String name)
 	{
 		WorldContainer world = this.getWorldMeta(name, false);
 		if (world == null)
 		{
 			return false;
 		}
 		return world.isLoaded();
 	}
 
 	boolean isWorldExisting(String world)
 	{
 		WorldContainer w = this.getWorldMeta(world, false);
 		return w != null;
 	}
 
 	@Override
 	public InternalWorld[] getLoadedWorlds()
 	{
 		List<World> loadedWorlds = Bukkit.getWorlds();
 		int size = loadedWorlds.size();
 		InternalWorld[] array = new InternalWorld[size];
 		for (int i = 0; i < size; i++)
 		{
 			array[i] = getInternalWorld(loadedWorlds.get(i).getName(), true);
 		}
 		return array;
 	}
 
 	@Override
 	public WorldContainer[] getWorlds()
 	{
 		List<World> loadedWorlds = Bukkit.getWorlds();
 		Collection<WorldContainer> registeredWorlds = this.worlds.values();
 		Set<WorldContainer> output = new HashSet<WorldContainer>();
 
 		for (World i : loadedWorlds)
 		{
 			output.add(this.getWorldMeta(i.getName(), false));
 		}
 		for (WorldContainer i : registeredWorlds)
 		{
 			output.add(i);
 		}
 
 		int size = output.size();
 		WorldContainer[] array = new WorldContainer[size];
 		return output.toArray(array);
 	}
 
 	@Override
	public InternalWorld[] getWorlds(boolean b)
 	{
		if (b)
 		{
 			return this.getLoadedWorlds();
 		}
 		List<World> loadedWorlds = Bukkit.getWorlds();
 		Collection<WorldContainer> registeredWorlds = this.worlds.values();
 		Set<InternalWorld> output = new HashSet<InternalWorld>();
 
 		for (World i : loadedWorlds)
 		{
 			output.add(this.getInternalWorld(i.getName(), false));
 		}
 		for (WorldContainer i : registeredWorlds)
 		{
 			output.add(i.getWorld());
 		}
 
 		int size = output.size();
 		InternalWorld[] array = new InternalWorld[size];
 		return output.toArray(array);
 	}
 
 	/**
 	 * Sets an flag on a world
 	 *
 	 * @param world the world to set on
 	 * @param flag The flag to affect
 	 * @param value The new value
 	 */
 	@Override
 	public void setFlag(String world, FlagName flag, FlagValue value)
 	{
 		this.setFlag(world, flag, value, false);
 	}
 
 	private void setFlag(String worldName, FlagName flag, FlagValue value, boolean isStartingUp)
 	{
 		WorldContainer w = this.getWorldMeta(worldName, false);
 		InternalWorld world = w.getWorld();
 		world.getFlags().put(flag, value);
 		if (w.isLoaded())
 		{
 			if (flag == FlagName.SPAWNMONSTER)
 			{
 				world.getWorld().setSpawnFlags(value.getAsBoolean(), world.getWorld().getAllowAnimals());
 			}
 			else if (flag == FlagName.SPAWNANIMAL)
 			{
 				world.getWorld().setSpawnFlags(world.getWorld().getAllowMonsters(), value.getAsBoolean());
 			}
 			else if (flag == FlagName.REMEMBERSPAWN)
 			{
 				world.getWorld().setKeepSpawnInMemory(value.getAsBoolean());
 			}
 			else if (flag == FlagName.PVP)
 			{
 				world.getWorld().setPVP(value.getAsBoolean());
 			}
 			else if (flag == FlagName.SAVEON)
 			{
 				world.getWorld().setAutoSave(value.getAsBoolean());
 			}
 		}
 		if (!isStartingUp)
 		{
 			if (value != FlagValue.UNKNOWN)
 			{
 				new FlagChanceEvent(w, flag, value.getAsBoolean(flag)).call();
 			}
 		}
 	}
 
 	/**
 	 * Gets an flag from the specified world
 	 *
 	 * @param worldName
 	 * @param flag The flag to return
 	 * @return The value of the specified flag
 	 */
 	@Override
 	public FlagValue getFlag(String worldName, FlagName flag)
 	{
 		WorldContainer w = this.getWorldMeta(worldName, false);
 		if (w.isLoaded())
 		{
 			InternalWorld world = w.getWorld();
 			if (flag == FlagName.SPAWNMONSTER)
 			{
 				return FlagValue.fromBoolean(world.getWorld().getAllowMonsters());
 			}
 			if (flag == FlagName.SPAWNANIMAL)
 			{
 				return FlagValue.fromBoolean(world.getWorld().getAllowAnimals());
 			}
 			if (flag == FlagName.REMEMBERSPAWN)
 			{
 				return FlagValue.fromBoolean(world.getWorld().getKeepSpawnInMemory());
 			}
 			if (flag == FlagName.CREATIVEWORLD)
 			{
 				FlagValue flagValue = world.getFlags().get(flag);
 				if (flagValue == null)
 				{
 					return FlagValue.fromBoolean(Bukkit.getDefaultGameMode() == org.bukkit.GameMode.CREATIVE);
 				}
 				return flagValue;
 			}
 			if (flag == FlagName.SAVEON)
 			{
 				return FlagValue.fromBoolean(world.getWorld().isAutoSave());
 			}
 			if (flag == FlagName.RECIEVECHAT || flag == FlagName.SENDCHAT)
 			{
 				return world.getFlags().containsKey(flag) ? world.getFlags().get(flag) : FlagValue.TRUE;
 			}
 			return FlagValue.fromBoolean(world.getWorld().getPVP());
 		}
 		else
 		{
 			if (w.getWorld().getFlags().containsKey(flag))
 			{
 				return w.getWorld().getFlags().get(flag);
 			}
 			else
 			{
 				return FlagValue.UNKNOWN;
 			}
 		}
 
 	}
 
 	@Override
 	public boolean makeWorld(String name, WorldGenerator env, long seed, String options) throws WorldGenException
 	{
 		if (this.getWorldMeta(name, false) != null)
 		{
 			return false;
 		}
 		InternalWorld worldData = new InternalWorld(name, seed, World.Environment.NORMAL, null, options, new FlagMap(), env.name(), 2);
 		env.makeWorld(worldData);
 		if (worldData.getEnv() == null)
 		{
 			return false;
 		}
 		this.createWorld(worldData, true);
 		return true;
 	}
 
 	@Override
 	public boolean deleteWorld(String world, boolean mustSave)
 	{
 		WorldContainer w = this.getWorldMeta(world, false);
 		if (w == null)
 		{
 			return false;
 		}
 		if (w.isLoaded())
 		{
 			if (!this.unloadWorld(null, false))
 			{
 				return false;
 			}
 		}
 		this.worlds.remove(world);
 		return true;
 	}
 
 	@Override
 	public boolean unloadWorld(String world, boolean mustSave)
 	{
 
 		if (Bukkit.unloadWorld(world, true))
 		{
 			this.worlds.get(world.toLowerCase()).setLoaded(false);
 			return true;
 		}
 		else
 		{
 			boolean isLoaded = Bukkit.getWorld(world) != null;
 			this.worlds.get(world.toLowerCase()).setLoaded(isLoaded);
 			return !isLoaded;
 		}
 	}
 
 	@Override
 	public World loadWorld(String name, boolean mustSave)
 	{
 		WorldContainer option = this.worlds.get(name.toLowerCase());
 		if (option.isLoaded())
 		{
 			return Bukkit.getWorld(name);
 		}
 		InternalWorld world = option.getWorld();
 		WorldCreator creator = WorldCreator.name(world.getName()).seed(world.getSeed()).environment(world.getEnv());
 		if (world.getGen() != null)
 		{
 			creator = creator.generator(world.getGen());
 		}
 		World bukkitWorld = null;
 		try
 		{
 			bukkitWorld = Bukkit.createWorld(creator);
 		}
 		finally
 		{
 			option.setLoaded(Bukkit.getWorld(name) != null);
 		}
 		bukkitWorld.setDifficulty(Difficulty.getByValue(option.getWorld().getDifficulty()));
 		Iterator<Map.Entry<FlagName, FlagValue>> i = world.getFlags().entrySet().iterator();
 		while (i.hasNext())
 		{
 			Map.Entry<FlagName, FlagValue> next = i.next();
 			if (next.getValue() == FlagValue.UNKNOWN)
 			{
 				continue;
 			}
 			this.setFlag(name, next.getKey(), next.getValue(), true);
 		}
 		return bukkitWorld;
 	}
 
 	private void createWorld(InternalWorld w, boolean mustSave)
 	{
 		this.addWorld(w, false);
 		if (mustSave)
 		{
 			new WorldCreateEvent(this.getWorldMeta(w.getName(), false)).call();
 		}
 	}
 
 	@Override
 	public WorldContainer getWorldMeta(String world, boolean mustLoad)
 	{
 		WorldContainer w = worlds.get(world.toLowerCase());
 		if (w == null)
 		{
 			World tmp = Bukkit.getWorld(world);
 			if (tmp == null)
 			{
 				return null;
 			}
 			ChunkGenerator tmp1 = tmp.getGenerator();
 			if (tmp1 != null)
 			{
 				w = this.addWorld(
 					new InternalWorld(tmp.getName(), tmp.getSeed(), tmp.getEnvironment(), NullGen.get(), tmp1.getClass().getName(),
 							  new FlagMap(),
 							  "NULLGEN", tmp.getDifficulty().getValue()), true);
 			}
 			else
 			{
 				w = this.addWorld(new InternalWorld(tmp.getName(), tmp.getSeed(), tmp.getEnvironment(), null, "", new FlagMap(),
 								    tmp.getEnvironment().name(), tmp.getDifficulty().getValue()), true);
 			}
 		}
 		return w;
 	}
 
 	/**
 	 * Copy the flags from world 1 to another world.
 	 *
 	 * @param fromWorld
 	 * @param destinationWorld
 	 * @throws ConfigException
 	 */
 	private void copyFlags(InternalWorld fromWorld, InternalWorld destinationWorld)
 	{
 		Iterator<Map.Entry<FlagName, FlagValue>> i = fromWorld.getFlags().entrySet().iterator();
 		while (i.hasNext())
 		{
 			Map.Entry<FlagName, FlagValue> next = i.next();
 			this.setFlag(destinationWorld.getName(), next.getKey(), next.getValue(), true);
 		}
 	}
 
 	/**
 	 * Sets the nether portal from world "fromworld" to be redirected to "toworld"
 	 *
 	 * @param fromWorld
 	 * @param toWorld
 	 * @return
 	 */
 	@Override
 	public boolean setPortal(String fromWorld, String toWorld)
 	{
 		if (fromWorld == null)
 		{
 			throw new NullPointerException("Param fromWorld may not be null");
 		}
 		WorldContainer w1 = this.getWorldMeta(fromWorld, false);
 		WorldContainer w2;
 		if (toWorld == null)
 		{
 			w2 = null;
 		}
 		else
 		{
 			w2 = this.getWorldMeta(toWorld, false);
 		}
 		if (w1 == null)
 		{
 			return false;
 		}
 		if (w2 == null)
 		{
 			w1.getWorld().setPortalLink(null);
 		}
 		else
 		{
 			w1.getWorld().setPortalLink(w2.getWorld().getName());
 		}
 		return true;
 	}
 
 	/**
 	 * Sets the end portal from world "fromworld" to be redirected to "toworld"
 	 *
 	 * @param fromWorld
 	 * @param toWorld
 	 * @return
 	 */
 	@Override
 	public boolean setEndPortal(String fromWorld, String toWorld)
 	{
 		if (fromWorld == null)
 		{
 			throw new NullPointerException("Param fromWorld may not be null");
 		}
 		WorldContainer w1 = this.getWorldMeta(fromWorld, false);
 		WorldContainer w2;
 		if (toWorld == null)
 		{
 			w2 = null;
 		}
 		else
 		{
 			w2 = this.getWorldMeta(toWorld, false);
 		}
 		if (w1 == null)
 		{
 			return false;
 		}
 		if (w2 == null)
 		{
 			w1.getWorld().setEndLink("");
 		}
 		else
 		{
 			w1.getWorld().setEndLink(w2.getWorld().getName());
 		}
 		return true;
 	}
 
 	private WorldContainer addWorld(InternalWorld w, boolean isLoaded)
 	{
 		WorldContainer world = new WorldContainer(w, isLoaded);
 		this.worlds.put(w.getName().toLowerCase(), world);
 		return world;
 	}
 
 	@Override
 	public MultiWorldWorldData[] getAllWorlds()
 	{
 
 		return getWorlds();
 	}
 
 	@Override
 	public void saveWorlds(ConfigurationSection worldSection, MyLogger log, SpawnWorldControl spawn)
 	{
 		ConfigurationSection l2;
 		ConfigurationSection l3;
 		for (WorldContainer i : this.getWorlds())
 		{
 			InternalWorld w = i.getWorld();
 			try
 			{
 				Utils.checkWorldName(w.getName());
 			}
 			catch (InvalidWorldNameException ex)
 			{
 				log.warning("Was not able to save world named: " + w.getName());
 				continue;
 			}
 			if (w.getMainGen().equalsIgnoreCase("NullGen"))
 			{
 				continue;
 			}
 			l2 = worldSection.createSection(w.getName());
 			l2.set("seed", w.getSeed());
 			l2.set("worldgen", w.getMainGen());
 			l2.set("options", w.getOptions());
 			l2.set("difficulty", w.getDifficulty());
 			l2.set("autoload", i.isLoaded());
 			if (!w.getFlags().isEmpty())
 			{
 				l3 = l2.createSection("flags");
 				for (Map.Entry<FlagName, FlagValue> i1 : w.getFlags().entrySet())
 				{
 
 					FlagValue get = this.getFlag(w.getName(), i1.getKey());
 					if (get != FlagValue.UNKNOWN)
 					{
 						l3.set(i1.getKey().name(), get == FlagValue.TRUE ? true : false);
 					}
 				}
 			}
 			if (!w.getPortalWorld().isEmpty())
 			{
 				l2.set("netherportal", w.getPortalWorld());
 			}
 			else
 			{
 				l2.set("netherportal", null);
 			}
 			if (!w.getEndPortalWorld().isEmpty())
 			{
 				l2.set("endportal", w.getEndPortalWorld());
 			}
 			else
 			{
 				l2.set("endportal", null);
 			}
 			if (spawn != null)
 			{
 				l2.set("spawnGroup", spawn.getGroupByWorld(w.getName()));
 			}
 
 		}
 	}
 
 	@Override
 	public void loadWorlds(ConfigurationSection worldList, MyLogger logger, Difficulty baseDifficulty, SpawnWorldControl spawn)
 	{
 		Iterator<String> list = worldList.getValues(false).keySet().iterator();
 		while (list.hasNext())
 		{
 			String worldName = list.next();
 			if (!worldList.isConfigurationSection(worldName))
 			{
 				logger.warning(worldName + " = not a valid world, sorry");
 				continue;
 			}
 			try
 			{
 				ConfigurationSection world = worldList.getConfigurationSection(worldName);
 
 				/* Get the seed */
 				long seed = world.getLong("seed", 0L);
 
 				/* Get the world gen */
 				WorldGenerator gen = WorldGenerator.valueOf(world.getString("worldgen", "NORMAL").toUpperCase());
 
 				/* Get the options to pass to world gen */
 				String options = world.getString("options", "");
 
 				/* Get the flags set on the world */
 				FlagMap flags = new FlagMap();
 				ConfigurationSection flagList = world.getConfigurationSection("flags");
 				if (flagList != null)
 				{
 					FlagName[] flagNames = FlagName.class.getEnumConstants();
 					for (int i = 0; i < flagNames.length; i++)
 					{
 						if (!flagList.isBoolean(flagNames[i].name()))
 						{
 							continue;
 						}
 						flags.put(flagNames[i], FlagValue.fromBoolean(flagList.getBoolean(flagNames[i].name())));
 					}
 				}
 				String portal = world.getString("netherportal", "");
 				String endPortal = world.getString("endportal", "");
 
 				/* Makes the world */
 				InternalWorld worldData = new InternalWorld();
 				worldData.setWorldName(worldName);
 				worldData.setWorldSeed(seed);
 				worldData.setMadeBy(gen.getName());
 				worldData.setPortalLink(portal);
 				worldData.setEndLink(endPortal);
 				worldData.setFlags(flags);
 				worldData.setOptions(options);
 				worldData.setDifficulty(world.getInt("difficulty", baseDifficulty.getValue()));
 
 				/* Passes the object to the world gens for further chances */
 				gen.makeWorld(worldData);
 				if (worldData.getEnv() == null)
 				{
 					continue;
 				}
 
 				/* Loads the world at the mem of server */
 				this.createWorld(worldData, false);
 				if (world.getBoolean("autoload", true))
 				{
 					this.loadWorld(worldData.getName(), false);
 				}
 				if (spawn != null)
 				{
 					String spawnGroup = world.getString("spawnGroup", "defaultGroup");
 					if (!spawn.registerWorldSpawn(worldName, spawnGroup))
 					{
 						world.set("spawnGroup", "defaultGroup");
 						spawn.registerWorldSpawn(worldName, "defaultGroup");
 					}
 
 				}
 
 
 			}
 			catch (IllegalArgumentException err)
 			{
 				worldList.set(worldName, null);
 				logger.warning("Invalid world: " + worldName);
 			}
 			catch (WorldGenException err)
 			{
 				worldList.set(worldName, null);
 				logger.warning("Invalid world gen used for world '" + worldName + "': " + err.getLocalizedMessage());
 			}
 			catch (Exception err)
 			{
 				logger.throwing(this.getClass().getName(), "load", err, "Some error with '" + worldName + "': " + err.getLocalizedMessage());
 			}
 		}
 	}
 }
