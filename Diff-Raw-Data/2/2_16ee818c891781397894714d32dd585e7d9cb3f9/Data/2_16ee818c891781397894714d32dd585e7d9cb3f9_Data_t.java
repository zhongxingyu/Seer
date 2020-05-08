 package com.censoredsoftware.demigods.engine.data;
 
 import com.censoredsoftware.censoredlib.data.ServerData;
 import com.censoredsoftware.censoredlib.data.TimedData;
 import com.censoredsoftware.censoredlib.data.inventory.CItemStack;
 import com.censoredsoftware.censoredlib.data.location.CLocation;
 import com.censoredsoftware.censoredlib.data.player.Notification;
 import com.censoredsoftware.censoredlib.helper.ConfigFile;
 import com.censoredsoftware.demigods.engine.DemigodsPlugin;
 import com.censoredsoftware.demigods.engine.language.English;
 import com.google.common.base.Supplier;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Table;
 import com.google.common.collect.Tables;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.serialization.ConfigurationSerializable;
 import org.bukkit.entity.Player;
 
 import java.util.Collection;
 import java.util.Map;
 import java.util.Set;
 import java.util.UUID;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 
 public class Data
 {
 	public static final DemigodsFile<String, DPlayer> PLAYER = new DemigodsFile<String, DPlayer>("players.yml")
 	{
 		@Override
 		public DPlayer create(String mojangAccount, ConfigurationSection conf)
 		{
 			return new DPlayer(mojangAccount, conf);
 		}
 
 		@Override
 		public String convertFromString(String stringId)
 		{
 			return stringId;
 		}
 	};
 	public static final DemigodsFile<UUID, CLocation> LOCATION = new DemigodsFile<UUID, CLocation>("locations.yml")
 	{
 		@Override
 		public CLocation create(UUID uuid, ConfigurationSection conf)
 		{
 			return new CLocation(uuid, conf);
 		}
 
 		@Override
 		public UUID convertFromString(String stringId)
 		{
 			return UUID.fromString(stringId);
 		}
 	};
 	public static final DemigodsFile<UUID, StructureData> STRUCTURE = new DemigodsFile<UUID, StructureData>("structures.yml")
 	{
 		@Override
 		public StructureData create(UUID uuid, ConfigurationSection conf)
 		{
 			return new StructureData(uuid, conf);
 		}
 
 		@Override
 		public UUID convertFromString(String stringId)
 		{
 			return UUID.fromString(stringId);
 		}
 	};
 	public static final DemigodsFile<UUID, DCharacter> CHARACTER = new DemigodsFile<UUID, DCharacter>("characters.yml")
 	{
 		@Override
 		public DCharacter create(UUID uuid, ConfigurationSection conf)
 		{
 			return new DCharacter(uuid, conf);
 		}
 
 		@Override
 		public UUID convertFromString(String stringId)
 		{
 			return UUID.fromString(stringId);
 		}
 
 	};
 	public static final DemigodsFile<UUID, DCharacter.Meta> CHARACTER_META = new DemigodsFile<UUID, DCharacter.Meta>("metas.yml")
 	{
 		@Override
 		public DCharacter.Meta create(UUID uuid, ConfigurationSection conf)
 		{
 			return new DCharacter.Meta(uuid, conf);
 		}
 
 		@Override
 		public UUID convertFromString(String stringId)
 		{
 			return UUID.fromString(stringId);
 		}
 	};
 	public static final DemigodsFile<UUID, DDeath> DEATH = new DemigodsFile<UUID, DDeath>("deaths.yml")
 	{
 		@Override
 		public DDeath create(UUID uuid, ConfigurationSection conf)
 		{
 			return new DDeath(uuid, conf);
 		}
 
 		@Override
 		public UUID convertFromString(String stringId)
 		{
 			return UUID.fromString(stringId);
 		}
 	};
 	public static final DemigodsFile<UUID, Skill> SKILL = new DemigodsFile<UUID, Skill>("skills.yml")
 	{
 		@Override
 		public Skill create(UUID uuid, ConfigurationSection conf)
 		{
 			return new Skill(uuid, conf);
 		}
 
 		@Override
 		public UUID convertFromString(String stringId)
 		{
 			return UUID.fromString(stringId);
 		}
 	};
 	public static final DemigodsFile<UUID, DCharacter.Inventory> CHARACTER_INVENTORY = new DemigodsFile<UUID, DCharacter.Inventory>("inventories.yml")
 	{
 		@Override
 		public DCharacter.Inventory create(UUID uuid, ConfigurationSection conf)
 		{
 			return new DCharacter.Inventory(uuid, conf);
 		}
 
 		@Override
 		public UUID convertFromString(String stringId)
 		{
 			return UUID.fromString(stringId);
 		}
 	};
 	public static final DemigodsFile<UUID, DCharacter.EnderInventory> CHARACTER_ENDER_INVENTORY = new DemigodsFile<UUID, DCharacter.EnderInventory>("enderInventories.yml")
 	{
 		@Override
 		public DCharacter.EnderInventory create(UUID uuid, ConfigurationSection conf)
 		{
 			return new DCharacter.EnderInventory(uuid, conf);
 		}
 
 		@Override
 		public UUID convertFromString(String stringId)
 		{
 			return UUID.fromString(stringId);
 		}
 	};
 	public static final DemigodsFile<UUID, CItemStack> ITEM_STACK = new DemigodsFile<UUID, CItemStack>("itemstacks.yml")
 	{
 		@Override
 		public CItemStack create(UUID uuid, ConfigurationSection conf)
 		{
 			return new CItemStack(uuid, conf);
 		}
 
 		@Override
 		public UUID convertFromString(String stringId)
 		{
 			return UUID.fromString(stringId);
 		}
 	};
 	public static final DemigodsFile<UUID, DSavedPotion> SAVED_POTION = new DemigodsFile<UUID, DSavedPotion>("savedpotions.yml")
 	{
 		@Override
 		public DSavedPotion create(UUID uuid, ConfigurationSection conf)
 		{
 			return new DSavedPotion(uuid, conf);
 		}
 
 		@Override
 		public UUID convertFromString(String stringId)
 		{
 			return UUID.fromString(stringId);
 		}
 	};
 	public static final DemigodsFile<UUID, DPet> PET = new DemigodsFile<UUID, DPet>("pets.yml")
 	{
 		@Override
 		public DPet create(UUID uuid, ConfigurationSection conf)
 		{
 			return new DPet(uuid, conf);
 		}
 
 		@Override
 		public UUID convertFromString(String stringId)
 		{
 			return UUID.fromString(stringId);
 		}
 	};
 	public static final DemigodsFile<UUID, Notification> NOTIFICATION = new DemigodsFile<UUID, Notification>("notifications.yml")
 	{
 		@Override
 		public Notification create(UUID uuid, ConfigurationSection conf)
 		{
 			return new Notification(uuid, conf);
 		}
 
 		@Override
 		public UUID convertFromString(String stringId)
 		{
 			return UUID.fromString(stringId);
 		}
 	};
 	public static final DemigodsFile<UUID, Battle> BATTLE = new DemigodsFile<UUID, Battle>("battles.yml")
 	{
 		@Override
 		public Battle create(UUID uuid, ConfigurationSection conf)
 		{
 			return new Battle(uuid, conf);
 		}
 
 		@Override
 		public UUID convertFromString(String stringId)
 		{
 			return UUID.fromString(stringId);
 		}
 	};
 	public static final DemigodsFile<UUID, TimedData> TIMED_DATA = new DemigodsFile<UUID, TimedData>("timeddata.yml")
 	{
 		@Override
 		public TimedData create(UUID uuid, ConfigurationSection conf)
 		{
 			return new TimedData(uuid, conf);
 		}
 
 		@Override
 		public UUID convertFromString(String stringId)
 		{
 			return UUID.fromString(stringId);
 		}
 	};
 	public static final DemigodsFile<UUID, ServerData> SERVER_DATA = new DemigodsFile<UUID, ServerData>("serverdata.yml")
 	{
 		@Override
 		public ServerData create(UUID uuid, ConfigurationSection conf)
 		{
 			return new ServerData(uuid, conf);
 		}
 
 		@Override
 		public UUID convertFromString(String stringId)
 		{
 			return UUID.fromString(stringId);
 		}
 	};
 	public static final DemigodsFile<UUID, TributeData> TRIBUTE_DATA = new DemigodsFile<UUID, TributeData>("tributedata.yml")
 	{
 		@Override
 		public TributeData create(UUID uuid, ConfigurationSection conf)
 		{
 			return new TributeData(uuid, conf);
 		}
 
 		@Override
 		public UUID convertFromString(String stringId)
 		{
 			return UUID.fromString(stringId);
 		}
 	};
 
 	public static DemigodsFile[] values()
 	{
 		return new DemigodsFile[] { PLAYER, LOCATION, STRUCTURE, CHARACTER, CHARACTER_META, DEATH, SKILL, CHARACTER_INVENTORY, CHARACTER_ENDER_INVENTORY, ITEM_STACK, SAVED_POTION, PET, NOTIFICATION, BATTLE, TIMED_DATA, SERVER_DATA, TRIBUTE_DATA };
 	}
 
 	// Temp Data
 	private static Table<String, String, Object> tempData;
 
 	public static final String SAVE_PATH;
 
 	static
 	{
 		// Data folder
 		SAVE_PATH = DemigodsPlugin.plugin().getDataFolder() + "/data/"; // Don't change this.
 
 		for(DemigodsFile data : values())
 			data.loadToData();
 		tempData = Tables.newCustomTable(new ConcurrentHashMap<String, Map<String, Object>>(), new Supplier<ConcurrentHashMap<String, Object>>()
 		{
 			@Override
 			public ConcurrentHashMap<String, Object> get()
 			{
 				return new ConcurrentHashMap<>();
 			}
 		});
 	}
 
 	private Data()
 	{}
 
 	public static void save()
 	{
 		for(DemigodsFile data : values())
 			data.saveToFile();
 	}
 
 	public static void flushData()
 	{
 		// Kick everyone
 		for(Player player : Bukkit.getOnlinePlayers())
 			player.kickPlayer(ChatColor.GREEN + English.DATA_RESET_KICK.getLine());
 
 		// Clear the data
 		for(DemigodsFile data : values())
 			data.clear();
 		tempData.clear();
 
 		save();
 
 		// Reload the PLUGIN
 		Bukkit.getServer().getPluginManager().disablePlugin(DemigodsPlugin.plugin());
 		Bukkit.getServer().getPluginManager().enablePlugin(DemigodsPlugin.plugin());
 	}
 
 	/*
 	 * Temporary data
 	 */
 	public static boolean hasKeyTemp(String row, String column)
 	{
 		return tempData.contains(row, column);
 	}
 
 	public static Object getValueTemp(String row, String column)
 	{
 		if(hasKeyTemp(row, column)) return tempData.get(row, column);
 		else return null;
 	}
 
 	public static void saveTemp(String row, String column, Object value)
 	{
 		tempData.put(row, column, value);
 	}
 
 	public static void removeTemp(String row, String column)
 	{
 		if(hasKeyTemp(row, column)) tempData.remove(row, column);
 	}
 
 	/*
 	 * Timed data
 	 */
 	public static void saveTimed(String key, String subKey, Object data, Integer seconds)
 	{
 		// Remove the data if it exists already
 		TimedDataManager.remove(key, subKey);
 
 		// Create and save the timed data
 		TimedData timedData = new TimedData();
 		timedData.generateId();
 		timedData.setKey(key);
 		timedData.setSubKey(subKey);
 		timedData.setData(data.toString());
 		timedData.setSeconds(seconds);
 		TIMED_DATA.put(timedData.getId(), timedData);
 	}
 
 	/*
 	 * Timed data
 	 */
 	public static void saveTimedWeek(String key, String subKey, Object data)
 	{
 		// Remove the data if it exists already
 		TimedDataManager.remove(key, subKey);
 
 		// Create and save the timed data
 		TimedData timedData = new TimedData();
 		timedData.generateId();
 		timedData.setKey(key);
 		timedData.setSubKey(subKey);
 		timedData.setData(data.toString());
 		timedData.setHours(168);
 		TIMED_DATA.put(timedData.getId(), timedData);
 	}
 
 	public static void removeTimed(String key, String subKey)
 	{
 		TimedDataManager.remove(key, subKey);
 	}
 
 	public static boolean hasTimed(String key, String subKey)
 	{
 		return TimedDataManager.find(key, subKey) != null;
 	}
 
 	public static Object getTimedValue(String key, String subKey)
 	{
 		return TimedDataManager.find(key, subKey).getData();
 	}
 
 	public static long getTimedExpiration(String key, String subKey)
 	{
 		return TimedDataManager.find(key, subKey).getExpiration();
 	}
 
 	/*
 	 * Server data
 	 */
 	public static void saveServerData(String key, String subKey, Object data)
 	{
 		// Remove the data if it exists already
 		ServerDataManager.remove(key, subKey);
 
 		// Create and save the timed data
 		ServerData serverData = new ServerData();
 		serverData.generateId();
 		serverData.setKey(key);
 		serverData.setSubKey(subKey);
 		serverData.setData(data.toString());
 		SERVER_DATA.put(serverData.getId(), serverData);
 	}
 
 	public static void removeServerData(String key, String subKey)
 	{
 		ServerDataManager.remove(key, subKey);
 	}
 
 	public static boolean hasServerData(String key, String subKey)
 	{
 		return ServerDataManager.find(key, subKey) != null;
 	}
 
 	public static Object getServerDataValue(String key, String subKey)
 	{
 		return ServerDataManager.find(key, subKey).getData();
 	}
 
	public abstract static class DemigodsFile<ID, DATA extends ConfigurationSerializable> extends ConfigFile<ID, DATA>
 	{
 		private final String saveFile;
 		private ConcurrentMap<ID, DATA> dataStore = Maps.newConcurrentMap();
 
 		protected DemigodsFile(String saveFile)
 		{
 			this.saveFile = saveFile;
 		}
 
 		@Override
 		public final ConcurrentMap<ID, DATA> getLoadedData()
 		{
 			return dataStore;
 		}
 
 		@Override
 		public final Map<String, Object> serialize(ID id)
 		{
 			return getLoadedData().get(id).serialize();
 		}
 
 		@Override
 		public final String getSavePath()
 		{
 			return SAVE_PATH;
 		}
 
 		@Override
 		public final String getSaveFile()
 		{
 			return saveFile;
 		}
 
 		@Override
 		public final void loadToData()
 		{
 			dataStore = loadFromFile();
 		}
 
 		public final boolean containsKey(ID key)
 		{
 			return dataStore.containsKey(key);
 		}
 
 		public final DATA get(ID key)
 		{
 			return dataStore.get(key);
 		}
 
 		public final void put(ID key, DATA value)
 		{
 			dataStore.put(key, value);
 		}
 
 		public final void remove(ID key)
 		{
 			dataStore.remove(key);
 		}
 
 		public final Set<ID> keySet()
 		{
 			return dataStore.keySet();
 		}
 
 		public final Set<Map.Entry<ID, DATA>> entrySet()
 		{
 			return dataStore.entrySet();
 		}
 
 		public final Collection<DATA> values()
 		{
 			return dataStore.values();
 		}
 
 		public final void clear()
 		{
 			dataStore.clear();
 		}
 	}
 }
