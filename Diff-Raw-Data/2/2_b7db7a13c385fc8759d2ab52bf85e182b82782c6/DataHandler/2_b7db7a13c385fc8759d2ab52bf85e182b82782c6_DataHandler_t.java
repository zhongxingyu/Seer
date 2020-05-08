 package multiworld.data;
 
 import multiworld.ConfigException;
 import multiworld.MultiWorldPlugin;
 import multiworld.WorldGenException;
 import multiworld.api.MultiWorldWorldData;
 import multiworld.api.flag.FlagName;
 import multiworld.flags.FlagValue;
 import multiworld.worldgen.WorldGenerator;
 import org.bukkit.Bukkit;
 import org.bukkit.Difficulty;
 import org.bukkit.Server;
 import org.bukkit.World;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.file.FileConfiguration;
 
 /**
  *
  * @author Fernando
  */
 public final class DataHandler implements WorldUntils
 {
 	private final WorldManager worlds = new WorldManager();
 	private FileConfiguration config;
 	private final MultiWorldPlugin plugin;
 	private MyLogger logger;
 	private Difficulty difficulty;
 	private LangStrings lang;
 	private boolean autoLoadWorld = false;
 	private boolean unloadWorldsOnDisable = false;
 	private SpawnWorldControl spawn;
 	public final static ConfigNode<ConfigurationSection> OPTIONS_NODE = new ConfigNode<ConfigurationSection>("options", null, ConfigurationSection.class);
 	public final static ConfigNode<Boolean> OPTIONS_BLOCK_ENDER_CHESTS = new ConfigNode<Boolean>(OPTIONS_NODE, "blockEnderChestInCrea", false, Boolean.class);
 	public final static ConfigNode<Boolean> OPTIONS_LINK_NETHER = new ConfigNode<Boolean>(OPTIONS_NODE, "useportalhandler", false, Boolean.class);
 	public final static ConfigNode<Boolean> OPTIONS_LINK_END = new ConfigNode<Boolean>(OPTIONS_NODE, "useEndPortalHandler", false, Boolean.class);
 	public final static ConfigNode<Boolean> OPTIONS_WORLD_CHAT = new ConfigNode<Boolean>(OPTIONS_NODE, "useWorldChatSeperator", false, Boolean.class);
 	public final static ConfigNode<Boolean> OPTIONS_GAMEMODE = new ConfigNode<Boolean>(OPTIONS_NODE, "usecreativemode", false, Boolean.class);
 	public final static ConfigNode<Boolean> OPTIONS_GAMEMODE_INV = new ConfigNode<Boolean>(OPTIONS_NODE, "usecreativemodeinv", true, Boolean.class);
 	public final static ConfigNode<Boolean> OPTIONS_DEBUG = new ConfigNode<Boolean>(OPTIONS_NODE, "debug", false, Boolean.class);
 	//public final static ConfigNode<Boolean> OPTIONS_USE_SPAWN_CONTROL = new ConfigNode<Boolean>(OPTIONS_NODE, "spawnControl", false, Boolean.class);
 	public final static ConfigNode<Integer> OPTIONS_DIFFICULTY = new ConfigNode<Integer>(OPTIONS_NODE, "difficulty", 2, Integer.class);
 	public final static ConfigNode<String> OPTIONS_LOCALE = new ConfigNode<String>(OPTIONS_NODE, "locale", "en_US", String.class);
 	public final static ConfigNode<Boolean> OPTIONS_WORLD_SPAWN = new ConfigNode<Boolean>(OPTIONS_NODE, "useWorldSpawnHandler", false, Boolean.class);
 
 	/**
 	 * Makes the object
 	 *
 	 * @param server The server whits runs the plugin
 	 * @param config
 	 * @param plugin The main plugin running this
 	 * @throws ConfigException When there was an error
 	 */
 	public DataHandler(Server server, FileConfiguration config, MultiWorldPlugin plugin) throws ConfigException
 	{
 		this.config = config;
 		this.plugin = plugin;
 		this.load(true);
 	}
 
 	@Override
 	public void loadWorlds(ConfigurationSection worldList, MyLogger logger, Difficulty baseDifficulty, SpawnWorldControl spawn)
 	{
 		worlds.loadWorlds(worldList, logger, baseDifficulty, spawn);
 	}
 
 	public void save() throws ConfigException
 	{
 		this.config.options().header("# options.debug: must the debug output be printed?\n"
 			+ "# options.difficulty: what is the server diffecalty?\n"
 			+ "# options.locale: what set of lang files must be used, supported: en_US, nl_NL, de_DE, it_IT\n"
 			+ "# spawnGroup: used to set whits worlds have whits spawn, difficult to use");
 		//this.config.set("options.debug", this.logger.getDebug());
 		//this.config.set("options.difficulty", this.difficulty.getValue());
 		//this.config.set("options.usecreativemode", this.isCreativeEnabled);
 		//this.config.set("options.usecreativemodeinv", this.isCreativeInvEnabled);
 		//this.config.set("options.useportalhandler", this.isPortalHandlerEnabled);
 		//this.config.set("options.useEndPortalHandler", this.endPortalHandlerEnabled);
 		//this.config.set("options.useWorldChatSeperator", this.useWorldChat);
 		//this.config.set("options.blockEnderChestInCrea", this.blockEnderChests);
 		//this.config.set("options.locale", this.lang.getLocale().toString());
 
 		ConfigurationSection l1;
 		l1 = this.config.createSection("worlds");
 		saveWorlds(l1, logger, this.spawn);
 		if (this.spawn != null)
 		{
 			this.spawn.save(config.createSection("spawnGroup"));
 		}
 		this.plugin.saveConfig();
 
 
 	}
 
 	@Override
 	public void saveWorlds(ConfigurationSection worldSection, MyLogger log, SpawnWorldControl spawn)
 	{
 		worlds.saveWorlds(worldSection, log, spawn);
 	}
 
 	public void load() throws ConfigException
 	{
 		this.load(false);
 	}
 
 	private void load(boolean isStartingUp) throws ConfigException
 	{
 		if (!isStartingUp)
 		{
 			this.plugin.reloadConfig();
 			this.config = this.plugin.getConfig();
 		}
 
 		this.logger = new MyLogger(getNode(OPTIONS_DEBUG), "MultiWorld");
 		this.logger.fine("config loaded");
 
 
 		this.difficulty = Difficulty.getByValue(getNode(OPTIONS_DIFFICULTY));
 
 		/* locale setting */
 		{
 			String tmp1 = "";
 			String tmp2 = "";
 			String tmp3 = "";
			String[] tmp4 = getNode(OPTIONS_LOCALE).split("_");
 			switch (tmp4.length)
 			{
 				case 3:
 					tmp3 = tmp4[2];
 				case 2:
 					tmp2 = tmp4[1];
 				default:
 					tmp1 = tmp4[0];
 					break;
 			}
 
 			this.lang = new LangStrings(tmp1, tmp2, tmp3, this.plugin);
 		}
 		/* addons settings */
 		{
 			this.getNode(DataHandler.OPTIONS_DEBUG);
 			this.getNode(DataHandler.OPTIONS_GAMEMODE);
 			this.getNode(DataHandler.OPTIONS_GAMEMODE_INV);
 			this.getNode(DataHandler.OPTIONS_BLOCK_ENDER_CHESTS);
 			this.getNode(DataHandler.OPTIONS_LINK_END);
 			this.getNode(DataHandler.OPTIONS_LINK_NETHER);
 			this.getNode(DataHandler.OPTIONS_WORLD_SPAWN);
 		}
 		ConfigurationSection spawnGroup = this.config.getConfigurationSection("spawnGroup");
 		if (spawnGroup == null)
 		{
 			this.config.set("spawnGroup.defaultGroup.world", Bukkit.getWorlds().get(0).getName());
 		}
 		this.spawn = new SpawnWorldControl(spawnGroup, this);
 		ConfigurationSection worldList = this.config.getConfigurationSection("worlds");
 		if (worldList != null)
 		{
 			loadWorlds(worldList, this.logger, this.difficulty, this.spawn);
 		}
 
 
 
 	}
 
 	public MyLogger getLogger()
 	{
 		return this.logger;
 	}
 
 	@Override
 	public String toString()
 	{
 		return "DataHandler{"
 			+ "worlds=" + worlds
 			+ ", config=" + config
 			+ ", plugin=" + plugin
 			+ ", logger=" + logger
 			+ ", difficulty=" + difficulty
 			+ ", lang=" + lang
 			+ ", unloadWorldsOnDisable=" + unloadWorldsOnDisable
 			+ '}';
 	}
 
 	public LangStrings getLang()
 	{
 		return this.lang;
 	}
 
 	@Override
 	public boolean unloadWorld(String world, boolean mustSave) throws ConfigException
 	{
 		boolean temp = worlds.unloadWorld(world, mustSave);
 		if (temp && mustSave)
 		{
 			this.save();
 		}
 		return temp;
 	}
 
 	@Override
 	public boolean setPortal(String fromWorld, String toWorld)
 	{
 		boolean temp = worlds.setPortal(fromWorld, toWorld);
 		return temp;
 	}
 
 	@Override
 	public void setFlag(String world, FlagName flag, FlagValue value) throws ConfigException
 	{
 		worlds.setFlag(world, flag, value);
 		this.save();
 	}
 
 	@Override
 	public boolean setEndPortal(String fromWorld, String toWorld)
 	{
 		return worlds.setEndPortal(fromWorld, toWorld);
 	}
 
 	@Override
 	public boolean makeWorld(String name, WorldGenerator env, long seed, String options) throws ConfigException, WorldGenException
 	{
 		return worlds.makeWorld(name, env, seed, options);
 	}
 
 	@Override
 	public World loadWorld(String name, boolean mustSave) throws ConfigException
 	{
 		World w = worlds.loadWorld(name, mustSave);
 		if (mustSave)
 		{
 			this.save();
 		}
 		return w;
 	}
 
 	@Override
 	public boolean isWorldLoaded(String name)
 	{
 		return worlds.isWorldLoaded(name);
 	}
 
 	boolean isWorldExisting(String world)
 	{
 		return worlds.isWorldExisting(world);
 	}
 
 	@Override
 	public InternalWorld[] getWorlds(boolean b)
 	{
 		return worlds.getWorlds(b);
 	}
 
 	@Override
 	public WorldContainer getWorldMeta(String world, boolean mustLoad)
 	{
 		return worlds.getWorldMeta(world, mustLoad);
 	}
 
 	@Override
 	public World getWorld(String name)
 	{
 		return worlds.getWorld(name);
 	}
 
 	@Override
 	public InternalWorld[] getLoadedWorlds()
 	{
 		return worlds.getLoadedWorlds();
 	}
 
 	@Override
 	public InternalWorld getInternalWorld(String name, boolean mustBeLoaded)
 	{
 		return worlds.getInternalWorld(name, mustBeLoaded);
 	}
 
 	@Override
 	public FlagValue getFlag(String worldName, FlagName flag)
 	{
 		return worlds.getFlag(worldName, flag);
 	}
 
 	@Override
 	public MultiWorldWorldData[] getAllWorlds()
 	{
 		return worlds.getAllWorlds();
 	}
 
 	@Override
 	public boolean deleteWorld(String world, boolean mustSave) throws ConfigException
 	{
 		boolean temp = worlds.deleteWorld(world, mustSave);
 		if (temp && mustSave)
 		{
 			this.save();
 		}
 		return temp;
 	}
 
 	@Override
 	public WorldContainer[] getWorlds()
 	{
 		return worlds.getWorlds();
 	}
 
 	private class ConfigGroup
 	{
 		private final ConfigurationSection insideNodes;
 		private final String groupName;
 
 		ConfigGroup(String groupName, ConfigurationSection insideNodes)
 		{
 			this.groupName = groupName;
 			this.insideNodes = insideNodes;
 
 		}
 
 		/**
 		 * @return the groupName
 		 */
 		public String getGroupName()
 		{
 			return groupName;
 		}
 
 		/**
 		 * @return the insideNodes
 		 */
 		public ConfigurationSection getInsideNodes()
 		{
 			return insideNodes;
 		}
 
 		@Override
 		public boolean equals(Object obj)
 		{
 			if (obj == null)
 			{
 				return false;
 			}
 			if (getClass() != obj.getClass())
 			{
 				return false;
 			}
 			final ConfigGroup other = (ConfigGroup) obj;
 			if (this.insideNodes != other.insideNodes && (this.insideNodes == null || !this.insideNodes.equals(other.insideNodes)))
 			{
 				return false;
 			}
 			if ((this.groupName == null) ? (other.groupName != null) : !this.groupName.equals(other.groupName))
 			{
 				return false;
 			}
 			return true;
 		}
 
 		@Override
 		public int hashCode()
 		{
 			int hash = 7;
 			hash = 37 * hash + (this.insideNodes != null ? this.insideNodes.hashCode() : 0);
 			hash = 37 * hash + (this.groupName != null ? this.groupName.hashCode() : 0);
 			return hash;
 		}
 	}
 
 	public <T> T getNode(ConfigNode<T> input)
 	{
 
 		return input.get(config);
 	}
 
 	public <T> void setNode(ConfigNode<T> input, T value)
 	{
 		input.set(config, value);
 	}
 
 	public SpawnWorldControl getSpawns()
 	{
 		return this.spawn;
 	}
 }
