 package com.censoredsoftware.demigods.player;
 
 import com.censoredsoftware.demigods.Demigods;
 import com.censoredsoftware.demigods.ability.Ability;
 import com.censoredsoftware.demigods.battle.Participant;
 import com.censoredsoftware.demigods.data.DataManager;
 import com.censoredsoftware.demigods.deity.Deity;
 import com.censoredsoftware.demigods.helper.ConfigFile;
 import com.censoredsoftware.demigods.item.DItemStack;
 import com.censoredsoftware.demigods.location.DLocation;
 import com.censoredsoftware.demigods.structure.Structure;
 import com.censoredsoftware.demigods.util.Structures;
 import com.google.common.base.Function;
 import com.google.common.base.Predicate;
 import com.google.common.collect.*;
 import com.google.common.primitives.Ints;
 import org.bukkit.*;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.serialization.ConfigurationSerializable;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 
 import java.util.*;
 import java.util.concurrent.ConcurrentHashMap;
 
 public class DCharacter implements Participant, ConfigurationSerializable
 {
 	private UUID id;
 	private String name;
 	private String player;
 	private double health;
 	private double maxhealth;
 	private Integer hunger;
 	private Float experience;
 	private Integer level;
 	private Integer killCount;
 	private Integer deathCount;
 	private UUID location;
 	private String deity;
 	private Boolean active;
 	private Boolean usable;
 	private UUID meta;
 	private UUID inventory;
 	private Set<String> deaths;
 
 	public DCharacter()
 	{
 		deaths = Sets.newHashSet();
 	}
 
 	public DCharacter(UUID id, ConfigurationSection conf)
 	{
 		this.id = id;
 		name = conf.getString("name");
 		player = conf.getString("player");
 		health = conf.getDouble("health");
 		maxhealth = conf.getDouble("maxhealth");
 		hunger = conf.getInt("hunger");
 		experience = Float.valueOf(conf.getString("experience"));
 		level = conf.getInt("level");
 		killCount = conf.getInt("killCount");
 		deathCount = conf.getInt("deathCount");
 		location = UUID.fromString(conf.getString("location"));
 		deity = conf.getString("deity");
 		active = conf.getBoolean("active");
 		usable = conf.getBoolean("usable");
 		meta = UUID.fromString(conf.getString("meta"));
 		if(conf.isString("inventory")) inventory = UUID.fromString(conf.getString("inventory"));
 		if(conf.isList("deaths")) deaths = Sets.newHashSet(conf.getStringList("deaths"));
 	}
 
 	@Override
 	public Map<String, Object> serialize()
 	{
 		Map<String, Object> map = Maps.newHashMap();
 		map.put("name", name);
 		map.put("player", player);
 		map.put("health", health);
 		map.put("maxhealth", maxhealth);
 		map.put("hunger", hunger);
 		map.put("experience", experience);
 		map.put("level", level);
 		map.put("killCount", killCount);
 		map.put("deathCount", deathCount);
 		map.put("location", location.toString());
 		map.put("deity", deity);
 		map.put("active", active);
 		map.put("usable", usable);
 		map.put("meta", meta.toString());
 		if(inventory != null) map.put("inventory", inventory.toString());
 		if(deaths != null) map.put("deaths", Lists.newArrayList(deaths));
 		return map;
 	}
 
 	void generateId()
 	{
 		id = UUID.randomUUID();
 	}
 
 	void setName(String name)
 	{
 		this.name = name;
 	}
 
 	void setDeity(Deity deity)
 	{
 		this.deity = deity.getName();
 	}
 
 	void setPlayer(DPlayer player)
 	{
 		this.player = player.getPlayerName();
 	}
 
 	public void setActive(boolean option)
 	{
 		this.active = option;
 		Util.save(this);
 	}
 
 	public void saveInventory()
 	{
 		this.inventory = Util.createInventory(this).getId();
 		Util.save(this);
 	}
 
 	public void setHealth(double health)
 	{
 		this.health = health;
 	}
 
 	public void setMaxHealth(double maxhealth)
 	{
 		this.maxhealth = maxhealth;
 	}
 
 	public void setHunger(int hunger)
 	{
 		this.hunger = hunger;
 	}
 
 	public void setLevel(int level)
 	{
 		this.level = level;
 	}
 
 	public void setExperience(float exp)
 	{
 		this.experience = exp;
 	}
 
 	public void setLocation(Location location)
 	{
 		this.location = DLocation.Util.create(location).getId();
 	}
 
 	public void setMeta(Meta meta)
 	{
 		this.meta = meta.getId();
 	}
 
 	public void setUsable(boolean usable)
 	{
 		this.usable = usable;
 	}
 
 	public Inventory getInventory()
 	{
 		if(Util.getInventory(this.inventory) == null) this.inventory = Util.createEmptyInventory().getId();
 		return Util.getInventory(this.inventory);
 	}
 
 	public Meta getMeta()
 	{
 		return Util.loadMeta(this.meta);
 	}
 
 	public OfflinePlayer getOfflinePlayer()
 	{
 		return Bukkit.getOfflinePlayer(this.player);
 	}
 
 	public String getName()
 	{
 		return this.name;
 	}
 
 	public Boolean isActive()
 	{
 		return this.active;
 	}
 
 	public Location getLocation()
 	{
 		if(this.location == null) return null;
 		return DLocation.Util.load(this.location).toLocation();
 	}
 
 	public Location getCurrentLocation()
 	{
 		if(getOfflinePlayer().isOnline()) return getOfflinePlayer().getPlayer().getLocation();
 		return getLocation();
 	}
 
 	@Override
 	public DCharacter getRelatedCharacter()
 	{
 		return this;
 	}
 
 	@Override
 	public LivingEntity getEntity()
 	{
 		return getOfflinePlayer().getPlayer();
 	}
 
 	public String getPlayer()
 	{
 		return player;
 	}
 
 	public Integer getLevel()
 	{
 		return this.level;
 	}
 
 	public Double getHealth()
 	{
 		return this.health;
 	}
 
 	public Double getMaxHealth()
 	{
 		return this.maxhealth;
 	}
 
 	public Integer getHunger()
 	{
 		return this.hunger;
 	}
 
 	public Float getExperience()
 	{
 		return this.experience;
 	}
 
 	public Boolean isDeity(String deityName)
 	{
 		return getDeity().getName().equalsIgnoreCase(deityName);
 	}
 
 	public Deity getDeity()
 	{
 		return Deity.Util.getDeity(this.deity);
 	}
 
 	public String getAlliance()
 	{
 		return getDeity().getAlliance();
 	}
 
 	/**
 	 * Returns the number of total killCount.
 	 * 
 	 * @return int
 	 */
 	public int getKillCount()
 	{
 		return this.killCount;
 	}
 
 	/**
 	 * Sets the amount of killCount to <code>amount</code>.
 	 * 
 	 * @param amount the amount of killCount to set to.
 	 */
 	public void setKillCount(int amount)
 	{
 		this.killCount = amount;
 		Util.save(this);
 	}
 
 	/**
 	 * Adds 1 kill.
 	 */
 	public void addKill()
 	{
 		this.killCount += 1;
 		Util.save(this);
 	}
 
 	/**
 	 * Returns the number of deathCount.
 	 * 
 	 * @return int
 	 */
 	public int getDeathCount()
 	{
 		return this.deathCount;
 	}
 
 	/**
 	 * Sets the number of deathCount to <code>amount</code>.
 	 * 
 	 * @param amount the amount of deathCount to set.
 	 */
 	public void setDeathCount(int amount)
 	{
 		this.deathCount = amount;
 		Util.save(this);
 	}
 
 	/**
 	 * Adds a death.
 	 */
 	public void addDeath()
 	{
 		this.deathCount += 1;
 		if(deaths == null) deaths = Sets.newHashSet();
 		deaths.add(new Death(this).getId().toString());
 		Util.save(this);
 	}
 
 	/**
 	 * Adds a death.
 	 */
 	public void addDeath(DCharacter attacker)
 	{
 		this.deathCount += 1;
 		deaths.add(new Death(this, attacker).getId().toString());
 		Util.save(this);
 	}
 
 	public Collection<Death> getDeaths()
 	{
 		if(deaths == null) deaths = Sets.newHashSet();
 		return Collections2.transform(deaths, new Function<String, Death>()
 		{
 			@Override
 			public Death apply(String s)
 			{
 				try
 				{
 					return Death.Util.load(UUID.fromString(s));
 				}
 				catch(Exception ignored)
 				{}
 				return null;
 			}
 		});
 	}
 
 	@Override
 	public void setCanPvp(boolean pvp)
 	{
 		DPlayer.Util.getPlayer(getOfflinePlayer()).setCanPvp(pvp);
 	}
 
 	@Override
 	public Boolean canPvp()
 	{
 		return DPlayer.Util.getPlayer(getOfflinePlayer()).canPvp();
 	}
 
 	@Override
 	public Object clone() throws CloneNotSupportedException
 	{
 		throw new CloneNotSupportedException();
 	}
 
 	public boolean isUsable()
 	{
 		return this.usable;
 	}
 
 	public void updateUseable()
 	{
 		this.usable = Deity.Util.getDeity(this.deity) != null;
 	}
 
 	public void refreshBinds()
 	{
 		if(!getOfflinePlayer().isOnline()) return;
 		for(String stringBind : getMeta().getBinds())
 		{
 			Player player = getOfflinePlayer().getPlayer();
 			Ability.Bind bind = Ability.Util.loadBind(UUID.fromString(stringBind));
 			player.getInventory().setItem(bind.getSlot(), bind.getItem());
 		}
 	}
 
 	public UUID getId()
 	{
 		return id;
 	}
 
 	public void remove()
 	{
 		for(Structure.Save structureSave : Structures.getStructuresSavesWithFlag(Structure.Flag.DELETE_WITH_OWNER))
 			if(structureSave.hasOwner() && structureSave.getOwner().equals(getId())) structureSave.remove();
 		Util.deleteInventory(getInventory().getId());
 		Util.deleteMeta(getMeta().getId());
 		Util.delete(getId());
 
 		DPlayer.Util.getPlayer(getOfflinePlayer()).resetCurrent();
 	}
 
 	public static class Inventory implements ConfigurationSerializable
 	{
 		private UUID id;
 		private UUID helmet;
 		private UUID chestplate;
 		private UUID leggings;
 		private UUID boots;
 		private String[] items;
 
 		public Inventory()
 		{}
 
 		public Inventory(UUID id, ConfigurationSection conf)
 		{
 			this.id = id;
 			if(conf.getString("helmet") != null) helmet = UUID.fromString(conf.getString("helmet"));
 			if(conf.getString("chestplate") != null) chestplate = UUID.fromString(conf.getString("chestplate"));
 			if(conf.getString("leggins") != null) leggings = UUID.fromString(conf.getString("leggings"));
 			if(conf.getString("boots") != null) boots = UUID.fromString(conf.getString("boots"));
 			if(conf.getStringList("items") != null)
 			{
 				List<String> stringItems = conf.getStringList("items");
 				items = new String[stringItems.size()];
 				for(int i = 0; i < stringItems.size(); i++)
 					items[i] = stringItems.get(i);
 			}
 		}
 
 		@Override
 		public Map<String, Object> serialize()
 		{
 			Map<String, Object> map = Maps.newHashMap();
 			if(helmet != null) map.put("helmet", helmet.toString());
 			if(chestplate != null) map.put("chestplate", chestplate.toString());
 			if(leggings != null) map.put("leggings", leggings.toString());
 			if(boots != null) map.put("boots", boots.toString());
 			if(items != null) map.put("items", Lists.newArrayList(items));
 			return map;
 		}
 
 		public void generateId()
 		{
 			id = UUID.randomUUID();
 		}
 
 		void setHelmet(ItemStack helmet)
 		{
 			this.helmet = DItemStack.Util.create(helmet).getId();
 		}
 
 		void setChestplate(ItemStack chestplate)
 		{
 			this.chestplate = DItemStack.Util.create(chestplate).getId();
 		}
 
 		void setLeggings(ItemStack leggings)
 		{
 			this.leggings = DItemStack.Util.create(leggings).getId();
 		}
 
 		void setBoots(ItemStack boots)
 		{
 			this.boots = DItemStack.Util.create(boots).getId();
 		}
 
 		void setItems(org.bukkit.inventory.Inventory inventory)
 		{
 			if(this.items == null) this.items = new String[36];
 			for(int i = 0; i < 35; i++)
 			{
 				if(inventory.getItem(i) == null) this.items[i] = DItemStack.Util.create(new ItemStack(Material.AIR)).getId().toString();
 				else this.items[i] = DItemStack.Util.create(inventory.getItem(i)).getId().toString();
 			}
 		}
 
 		public UUID getId()
 		{
 			return this.id;
 		}
 
 		public ItemStack getHelmet()
 		{
 			if(this.helmet == null) return null;
 			DItemStack item = DItemStack.Util.load(this.helmet);
 			if(item != null) return item.toItemStack();
 			return null;
 		}
 
 		public ItemStack getChestplate()
 		{
 			if(this.chestplate == null) return null;
 			DItemStack item = DItemStack.Util.load(this.chestplate);
 			if(item != null) return item.toItemStack();
 			return null;
 		}
 
 		public ItemStack getLeggings()
 		{
 			if(this.leggings == null) return null;
 			DItemStack item = DItemStack.Util.load(this.leggings);
 			if(item != null) return item.toItemStack();
 			return null;
 		}
 
 		public ItemStack getBoots()
 		{
 			if(this.boots == null) return null;
 			DItemStack item = DItemStack.Util.load(this.boots);
 			if(item != null) return item.toItemStack();
 			return null;
 		}
 
 		/**
 		 * Applies this inventory to the given <code>player</code>.
 		 * 
 		 * @param player the player for whom apply the inventory.
 		 */
 		public void setToPlayer(Player player)
 		{
 			// Define the inventory
 			PlayerInventory inventory = player.getInventory();
 
 			// Clear it all first
 			inventory.clear();
 			inventory.setHelmet(new ItemStack(Material.AIR));
 			inventory.setChestplate(new ItemStack(Material.AIR));
 			inventory.setLeggings(new ItemStack(Material.AIR));
 			inventory.setBoots(new ItemStack(Material.AIR));
 
 			// Set the armor contents
 			if(getHelmet() != null) inventory.setHelmet(getHelmet());
 			if(getChestplate() != null) inventory.setChestplate(getChestplate());
 			if(getLeggings() != null) inventory.setLeggings(getLeggings());
 			if(getBoots() != null) inventory.setBoots(getBoots());
 
 			if(this.items != null)
 			{
 				// Set items
 				for(int i = 0; i < 35; i++)
 					if(this.items[i] != null) inventory.setItem(i, DItemStack.Util.load(UUID.fromString(this.items[i])).toItemStack());
 			}
 
 			// Delete
 			Util.deleteInventory(id);
 		}
 
 		public static class File extends ConfigFile
 		{
 			private static String SAVE_PATH;
 			private static final String SAVE_FILE = "characterInventories.yml";
 
 			public File()
 			{
 				super(Demigods.plugin);
 				SAVE_PATH = Demigods.plugin.getDataFolder() + "/data/";
 			}
 
 			@Override
 			public ConcurrentHashMap<UUID, Inventory> loadFromFile()
 			{
 				final FileConfiguration data = getData(SAVE_PATH, SAVE_FILE);
 				ConcurrentHashMap<UUID, Inventory> map = new ConcurrentHashMap<UUID, Inventory>();
 				for(String stringId : data.getKeys(false))
 					map.put(UUID.fromString(stringId), new Inventory(UUID.fromString(stringId), data.getConfigurationSection(stringId)));
 				return map;
 			}
 
 			@Override
 			public boolean saveToFile()
 			{
 				FileConfiguration saveFile = getData(SAVE_PATH, SAVE_FILE);
 				Map<UUID, Inventory> currentFile = loadFromFile();
 
 				for(UUID id : DataManager.inventories.keySet())
 					if(!currentFile.keySet().contains(id) || !currentFile.get(id).equals(DataManager.inventories.get(id))) saveFile.createSection(id.toString(), Util.getInventory(id).serialize());
 
 				for(UUID id : currentFile.keySet())
 					if(!DataManager.inventories.keySet().contains(id)) saveFile.set(id.toString(), null);
 
 				return saveFile(SAVE_PATH, SAVE_FILE, saveFile);
 			}
 		}
 	}
 
 	public static class Meta implements ConfigurationSerializable
 	{
 		private UUID id;
 		private Integer ascensions;
 		private Integer favor;
 		private Integer maxFavor;
 		private Set<String> binds;
 		private Set<String> notifications;
 		private Map<String, Object> devotionData;
 		private Map<String, Object> warps;
 		private Map<String, Object> invites;
 
 		public Meta()
 		{}
 
 		public Meta(UUID id, ConfigurationSection conf)
 		{
 			this.id = id;
 			ascensions = conf.getInt("ascensions");
 			favor = conf.getInt("favor");
 			maxFavor = conf.getInt("maxFavor");
 			binds = Sets.newHashSet(conf.getStringList("binds"));
 			notifications = Sets.newHashSet(conf.getStringList("notifications"));
 			devotionData = conf.getConfigurationSection("devotionData").getValues(false);
 			warps = conf.getConfigurationSection("warps").getValues(false);
 			invites = conf.getConfigurationSection("invites").getValues(false);
 		}
 
 		@Override
 		public Map<String, Object> serialize()
 		{
 			Map<String, Object> map = Maps.newHashMap();
 			map.put("ascensions", ascensions);
 			map.put("favor", favor);
 			map.put("maxFavor", maxFavor);
 			map.put("binds", Lists.newArrayList(binds));
 			map.put("notifications", Lists.newArrayList(notifications));
 			map.put("devotionData", devotionData);
 			map.put("warps", warps);
 			map.put("invites", invites);
 			return map;
 		}
 
 		public void generateId()
 		{
 			id = UUID.randomUUID();
 		}
 
 		void initialize()
 		{
 			this.binds = Sets.newHashSet();
 			this.notifications = Sets.newHashSet();
 			this.warps = Maps.newHashMap();
 			this.invites = Maps.newHashMap();
 			this.devotionData = Maps.newHashMap();
 		}
 
 		public UUID getId()
 		{
 			return this.id;
 		}
 
 		public void addNotification(Notification notification)
 		{
 			getNotifications().add(notification.getId().toString());
 			Util.saveMeta(this);
 		}
 
 		public void removeNotification(Notification notification)
 		{
 			getNotifications().remove(notification.getId().toString());
 			Util.saveMeta(this);
 		}
 
 		public Set<String> getNotifications()
 		{
 			if(this.notifications == null) this.notifications = Sets.newHashSet();
 			return this.notifications;
 		}
 
 		public void clearNotifications()
 		{
 			notifications.clear();
 		}
 
 		public boolean hasNotifications()
 		{
 			return !this.notifications.isEmpty();
 		}
 
 		public void addWarp(String name, Location location)
 		{
 			warps.put(name.toLowerCase(), DLocation.Util.create(location).getId().toString());
 			Util.saveMeta(this);
 		}
 
 		public void removeWarp(String name)
 		{
 			getWarps().remove(name.toLowerCase());
 			Util.saveMeta(this);
 		}
 
 		public Map<String, Object> getWarps()
 		{
 			if(this.warps == null) this.warps = Maps.newHashMap();
 			return this.warps;
 		}
 
 		public void clearWarps()
 		{
 			getWarps().clear();
 		}
 
 		public boolean hasWarps()
 		{
 			return !this.warps.isEmpty();
 		}
 
 		public void addInvite(String name, Location location)
 		{
 			getInvites().put(name.toLowerCase(), DLocation.Util.create(location).getId().toString());
 			Util.saveMeta(this);
 		}
 
 		public void removeInvite(String name)
 		{
 			getInvites().remove(name.toLowerCase());
 			Util.saveMeta(this);
 		}
 
 		public Map<String, Object> getInvites()
 		{
 			if(this.invites == null) this.invites = Maps.newHashMap();
 			return this.invites;
 		}
 
 		public void clearInvites()
 		{
 			invites.clear();
 		}
 
 		public boolean hasInvites()
 		{
 			return !this.invites.isEmpty();
 		}
 
 		public void addDevotion(Ability.Devotion devotion)
 		{
 			if(!this.devotionData.containsKey(devotion.getType().toString())) this.devotionData.put(devotion.getType().toString(), devotion.getId().toString());
 			Util.saveMeta(this);
 		}
 
 		public Ability.Devotion getDevotion(Ability.Devotion.Type type)
 		{
 			if(this.devotionData.containsKey(type.toString())) return Ability.Util.loadDevotion(UUID.fromString(this.devotionData.get(type.toString()).toString()));
 			else
 			{
 				addDevotion(Ability.Util.createDevotion(type));
 				return Ability.Util.loadDevotion(UUID.fromString(this.devotionData.get(type.toString()).toString()));
 			}
 		}
 
 		public boolean checkBind(String ability, ItemStack item)
 		{
 			return(isBound(item) && getBind(item).getAbility().equalsIgnoreCase(ability));
 		}
 
 		public boolean checkBind(String ability, int slot)
 		{
 			return(isBound(slot) && getBind(slot).getAbility().equalsIgnoreCase(ability));
 		}
 
 		public boolean isBound(int slot)
 		{
 			return getBind(slot) != null;
 		}
 
 		public boolean isBound(String ability)
 		{
 			return getBind(ability) != null;
 		}
 
 		public boolean isBound(ItemStack item)
 		{
 			return getBind(item) != null;
 		}
 
 		public void addBind(Ability.Bind bind)
 		{
 			this.binds.add(bind.getId().toString());
 		}
 
 		public Ability.Bind setBound(String ability, int slot, ItemStack item)
 		{
 			Ability.Bind bind = Ability.Util.createBind(ability, slot, item);
 			this.binds.add(bind.getId().toString());
 			return bind;
 		}
 
 		public Ability.Bind getBind(int slot)
 		{
 			for(String bind : this.binds)
 				if(Ability.Util.loadBind(UUID.fromString(bind)).getSlot() == slot) return Ability.Util.loadBind(UUID.fromString(bind));
 			return null;
 		}
 
 		public Ability.Bind getBind(String ability)
 		{
 			for(String bind : this.binds)
 				if(Ability.Util.loadBind(UUID.fromString(bind)).getAbility().equalsIgnoreCase(ability)) return Ability.Util.loadBind(UUID.fromString(bind));
 			return null;
 		}
 
 		public Ability.Bind getBind(ItemStack item)
 		{
 			for(String bind : this.binds)
 				if(item.hasItemMeta() && item.getItemMeta().hasLore() && item.getItemMeta().getLore().toString().contains(Ability.Util.loadBind(UUID.fromString(bind)).getIdentifier())) return Ability.Util.loadBind(UUID.fromString(bind));
 			return null;
 		}
 
 		public Set<String> getBinds()
 		{
 			return this.binds;
 		}
 
 		public void removeBind(String ability)
 		{
 			if(isBound(ability))
 			{
 				Ability.Bind bind = getBind(ability);
 				this.binds.remove(bind.getId().toString());
 				Ability.Util.deleteBind(bind.getId());
 			}
 		}
 
 		public void removeBind(ItemStack item)
 		{
 			if(isBound(item))
 			{
 				Ability.Bind bind = getBind(item);
 				this.binds.remove(bind.getId().toString());
 				Ability.Util.deleteBind(bind.getId());
 			}
 		}
 
 		public void removeBind(Ability.Bind bind)
 		{
 			this.binds.remove(bind.getId().toString());
 			Ability.Util.deleteBind(bind.getId());
 		}
 
 		public Integer getAscensions()
 		{
 			return this.ascensions;
 		}
 
 		public void addAscension()
 		{
 			this.ascensions += 1;
 			Util.saveMeta(this);
 		}
 
 		public void addAscensions(int amount)
 		{
 			this.ascensions += amount;
 			Util.saveMeta(this);
 		}
 
 		public void subtractAscensions(int amount)
 		{
 			this.ascensions -= amount;
 			Util.saveMeta(this);
 		}
 
 		public void setAscensions(int amount)
 		{
 			this.ascensions = amount;
 			Util.saveMeta(this);
 		}
 
 		public Integer getFavor()
 		{
 			return this.favor;
 		}
 
 		public void setFavor(int amount)
 		{
 			this.favor = amount;
 			Util.saveMeta(this);
 		}
 
 		public void addFavor(int amount)
 		{
 			if((this.favor + amount) > this.maxFavor) this.favor = this.maxFavor;
 			else this.favor += amount;
 			Util.saveMeta(this);
 		}
 
 		public void subtractFavor(int amount)
 		{
 			if((this.favor - amount) < 0) this.favor = 0;
 			else this.favor -= amount;
 			Util.saveMeta(this);
 		}
 
 		public Integer getMaxFavor()
 		{
 			return this.maxFavor;
 		}
 
 		public void addMaxFavor(int amount)
 		{
 			if((this.maxFavor + amount) > Demigods.config.getSettingInt("caps.favor")) this.maxFavor = Demigods.config.getSettingInt("caps.favor");
 			else this.maxFavor += amount;
 			Util.saveMeta(this);
 		}
 
 		public void setMaxFavor(int amount)
 		{
 			if(amount < 0) this.maxFavor = 0;
 			if(amount > Demigods.config.getSettingInt("caps.favor")) this.maxFavor = Demigods.config.getSettingInt("caps.favor");
 			else this.maxFavor = amount;
 			Util.saveMeta(this);
 		}
 
 		@Override
 		public Object clone() throws CloneNotSupportedException
 		{
 			throw new CloneNotSupportedException();
 		}
 
 		public static class File extends ConfigFile
 		{
 			private static String SAVE_PATH;
 			private static final String SAVE_FILE = "characterMetas.yml";
 
 			public File()
 			{
 				super(Demigods.plugin);
 				SAVE_PATH = Demigods.plugin.getDataFolder() + "/data/";
 			}
 
 			@Override
 			public ConcurrentHashMap<UUID, Meta> loadFromFile()
 			{
 				final FileConfiguration data = getData(SAVE_PATH, SAVE_FILE);
 				ConcurrentHashMap<UUID, Meta> map = new ConcurrentHashMap<UUID, Meta>();
 				for(String stringId : data.getKeys(false))
 					map.put(UUID.fromString(stringId), new Meta(UUID.fromString(stringId), data.getConfigurationSection(stringId)));
 				return map;
 			}
 
 			@Override
 			public boolean saveToFile()
 			{
 				FileConfiguration saveFile = getData(SAVE_PATH, SAVE_FILE);
 				Map<UUID, Meta> currentFile = loadFromFile();
 
 				for(UUID id : DataManager.characterMetas.keySet())
 					if(!currentFile.keySet().contains(id) || !currentFile.get(id).equals(DataManager.characterMetas.get(id))) saveFile.createSection(id.toString(), Util.loadMeta(id).serialize());
 
 				for(UUID id : currentFile.keySet())
 					if(!DataManager.characterMetas.keySet().contains(id)) saveFile.set(id.toString(), null);
 
 				return saveFile(SAVE_PATH, SAVE_FILE, saveFile);
 			}
 		}
 	}
 
 	public static class File extends ConfigFile
 	{
 		private static String SAVE_PATH;
 		private static final String SAVE_FILE = "characters.yml";
 
 		public File()
 		{
 			super(Demigods.plugin);
 			SAVE_PATH = Demigods.plugin.getDataFolder() + "/data/";
 		}
 
 		@Override
 		public ConcurrentHashMap<UUID, DCharacter> loadFromFile()
 		{
 			final FileConfiguration data = getData(SAVE_PATH, SAVE_FILE);
 			ConcurrentHashMap<UUID, DCharacter> map = new ConcurrentHashMap<UUID, DCharacter>();
 			for(String stringId : data.getKeys(false))
 				map.put(UUID.fromString(stringId), new DCharacter(UUID.fromString(stringId), data.getConfigurationSection(stringId)));
 			return map;
 		}
 
 		@Override
 		public boolean saveToFile()
 		{
 			FileConfiguration saveFile = getData(SAVE_PATH, SAVE_FILE);
 			Map<UUID, DCharacter> currentFile = loadFromFile();
 
 			for(UUID id : DataManager.characters.keySet())
 				if(!currentFile.keySet().contains(id) || !currentFile.get(id).equals(DataManager.characters.get(id))) saveFile.createSection(id.toString(), Util.load(id).serialize());
 
 			for(UUID id : currentFile.keySet())
 				if(!DataManager.characters.keySet().contains(id)) saveFile.set(id.toString(), null);
 
 			return saveFile(SAVE_PATH, SAVE_FILE, saveFile);
 		}
 	}
 
 	public static class Util
 	{
 		public static void save(DCharacter character)
 		{
 			DataManager.characters.put(character.getId(), character);
 		}
 
 		public static void saveMeta(Meta meta)
 		{
 			DataManager.characterMetas.put(meta.getId(), meta);
 		}
 
 		public static void saveInventory(Inventory inventory)
 		{
 			DataManager.inventories.put(inventory.getId(), inventory);
 		}
 
 		public static void delete(UUID id)
 		{
 			DataManager.characters.remove(id);
 		}
 
 		public static void deleteMeta(UUID id)
 		{
 			DataManager.characterMetas.remove(id);
 		}
 
 		public static void deleteInventory(UUID id)
 		{
 			DataManager.inventories.remove(id);
 		}
 
 		public static void create(DPlayer player, String chosenDeity, String chosenName, boolean switchCharacter)
 		{
 			// Switch to new character
 			if(switchCharacter) player.switchCharacter(create(player, chosenName, chosenDeity));
 		}
 
 		public static DCharacter create(DPlayer player, String charName, String charDeity)
 		{
 			if(getCharacterByName(charName) == null)
 			{
 				// Create the Character
 				return create(player, charName, Deity.Util.getDeity(charDeity), true);
 			}
 			return null;
 		}
 
 		private static DCharacter create(final DPlayer player, final String charName, final Deity deity, final boolean immortal)
 		{
 			DCharacter character = new DCharacter();
 			character.generateId();
 			character.setPlayer(player);
 			character.setName(charName);
 			character.setDeity(deity);
 			character.setUsable(true);
 			character.setMaxHealth(40.0);
 			character.setHealth(40.0);
 			character.setHunger(20);
 			character.setExperience(0);
 			character.setLevel(0);
 			character.setKillCount(0);
 			character.setDeathCount(0);
 			character.setLocation(player.getOfflinePlayer().getPlayer().getLocation());
 			character.setMeta(Util.createMeta());
 			save(character);
 			return character;
 		}
 
 		public static Inventory createInventory(DCharacter character)
 		{
 			PlayerInventory inventory = character.getOfflinePlayer().getPlayer().getInventory();
 			Inventory charInventory = new Inventory();
 			charInventory.generateId();
 			if(inventory.getHelmet() != null) charInventory.setHelmet(inventory.getHelmet());
 			if(inventory.getChestplate() != null) charInventory.setChestplate(inventory.getChestplate());
 			if(inventory.getLeggings() != null) charInventory.setLeggings(inventory.getLeggings());
 			if(inventory.getBoots() != null) charInventory.setBoots(inventory.getBoots());
 			charInventory.setItems(inventory);
 			saveInventory(charInventory);
 			return charInventory;
 		}
 
 		public static Inventory createEmptyInventory()
 		{
 			Inventory charInventory = new Inventory();
 			charInventory.generateId();
 			charInventory.setHelmet(new ItemStack(Material.AIR));
 			charInventory.setChestplate(new ItemStack(Material.AIR));
 			charInventory.setLeggings(new ItemStack(Material.AIR));
 			charInventory.setBoots(new ItemStack(Material.AIR));
 			saveInventory(charInventory);
 			return charInventory;
 		}
 
 		public static Meta createMeta()
 		{
 			Meta charMeta = new Meta();
 			charMeta.initialize();
 			charMeta.generateId();
 			charMeta.setAscensions(Demigods.config.getSettingInt("character.defaults.ascensions"));
 			charMeta.setFavor(Demigods.config.getSettingInt("character.defaults.favor"));
 			charMeta.setMaxFavor(Demigods.config.getSettingInt("character.defaults.max_favor"));
 			charMeta.addDevotion(Ability.Util.createDevotion(Ability.Devotion.Type.OFFENSE));
 			charMeta.addDevotion(Ability.Util.createDevotion(Ability.Devotion.Type.DEFENSE));
 			charMeta.addDevotion(Ability.Util.createDevotion(Ability.Devotion.Type.PASSIVE));
 			charMeta.addDevotion(Ability.Util.createDevotion(Ability.Devotion.Type.STEALTH));
 			charMeta.addDevotion(Ability.Util.createDevotion(Ability.Devotion.Type.SUPPORT));
 			charMeta.addDevotion(Ability.Util.createDevotion(Ability.Devotion.Type.ULTIMATE));
 			saveMeta(charMeta);
 			return charMeta;
 		}
 
 		public static Set<DCharacter> loadAll()
 		{
 			return Sets.newHashSet(DataManager.characters.values());
 		}
 
 		public static DCharacter load(UUID id)
 		{
 			return DataManager.characters.get(id);
 		}
 
 		public static Meta loadMeta(UUID id)
 		{
 			return DataManager.characterMetas.get(id);
 		}
 
 		public static Inventory getInventory(UUID id)
 		{
 			try
 			{
 				return DataManager.inventories.get(id);
 			}
 			catch(Exception ignored)
 			{}
 			return null;
 		}
 
 		public static void updateUsableCharacters()
 		{
 			for(DCharacter character : loadAll())
 				character.updateUseable();
 		}
 
 		public static DCharacter getCharacterByName(final String name)
 		{
 			try
 			{
 				Iterators.find(loadAll().iterator(), new Predicate<DCharacter>()
 				{
 					@Override
 					public boolean apply(DCharacter loaded)
 					{
 						return loaded.getName().equalsIgnoreCase(name);
 					}
 				});
 			}
 			catch(NoSuchElementException ignored)
 			{}
 			return null;
 		}
 
 		public static boolean charExists(String name)
 		{
 			return getCharacterByName(name) != null;
 		}
 
 		public static boolean isCooledDown(DCharacter player, String ability, boolean sendMsg)
 		{
 			if(DataManager.hasKeyTemp(player.getName(), ability + "_cooldown") && Long.parseLong(DataManager.getValueTemp(player.getName(), ability + "_cooldown").toString()) > System.currentTimeMillis())
 			{
 				if(sendMsg) player.getOfflinePlayer().getPlayer().sendMessage(ChatColor.RED + ability + " has not cooled down!");
 				return false;
 			}
 			else return true;
 		}
 
 		public static void setCoolDown(DCharacter player, String ability, long cooldown)
 		{
 			DataManager.saveTemp(player.getName(), ability + "_cooldown", cooldown);
 		}
 
 		public static long getCoolDown(DCharacter player, String ability)
 		{
 			return Long.parseLong(DataManager.getValueTemp(player.getName(), ability + "_cooldown").toString());
 		}
 
 		public static Set<DCharacter> getAllActive()
 		{
 			return Sets.filter(loadAll(), new Predicate<DCharacter>()
 			{
 				@Override
 				public boolean apply(DCharacter character)
 				{
 					return character.isActive();
 				}
 			});
 		}
 
 		/**
 		 * Returns true if <code>char1</code> is allied with <code>char2</code> based
 		 * on their current alliances.
 		 * 
 		 * @param char1 the first character to check.
 		 * @param char2 the second character to check.
 		 * @return boolean
 		 */
 		public static boolean areAllied(DCharacter char1, DCharacter char2)
 		{
 			return char1.getAlliance().equalsIgnoreCase(char2.getAlliance());
 		}
 
 		public static Collection<DCharacter> getOnlineCharactersWithDeity(final String deity)
 		{
 			return getCharactersWithPredicate(new Predicate<DCharacter>()
 			{
 				@Override
 				public boolean apply(DCharacter character)
 				{
 					return character.isActive() && character.getOfflinePlayer().isOnline() && character.getDeity().getName().equalsIgnoreCase(deity);
 				}
 			});
 		}
 
 		public static Collection<DCharacter> getOnlineCharactersWithAlliance(final String alliance)
 		{
 			return getCharactersWithPredicate(new Predicate<DCharacter>()
 			{
 				@Override
 				public boolean apply(DCharacter character)
 				{
 					return character.isActive() && character.getOfflinePlayer().isOnline() && character.getAlliance().equalsIgnoreCase(alliance);
 				}
 			});
 		}
 
 		public static Collection<DCharacter> getOnlineCharactersBelowAscension(final int ascention)
 		{
 			return getCharactersWithPredicate(new Predicate<DCharacter>()
 			{
 				@Override
 				public boolean apply(DCharacter character)
 				{
 					return character.isActive() && character.getOfflinePlayer().isOnline() && character.getMeta().getAscensions() < ascention;
 				}
 			});
 		}
 
 		public static Collection<DCharacter> getOnlineCharacters()
 		{
 			return getCharactersWithPredicate(new Predicate<DCharacter>()
 			{
 				@Override
 				public boolean apply(DCharacter character)
 				{
 					return character.isActive() && character.getOfflinePlayer().isOnline();
 				}
 			});
 		}
 
 		public static Collection<DCharacter> getCharactersWithPredicate(Predicate<DCharacter> predicate)
 		{
 			return Collections2.filter(loadAll(), predicate);
 		}
 
 		public static int getMedianOverallOnlineAscension()
 		{
 			return median(Ints.toArray(Collections2.transform(getOnlineCharacters(), new Function<DCharacter, Integer>()
 			{
 				@Override
 				public Integer apply(DCharacter character)
 				{
 					return character.getMeta().getAscensions();
 				}
 			})));
 		}
 
 		public static int getMedianOverallAscension()
 		{
 			return median(Ints.toArray(Collections2.transform(loadAll(), new Function<DCharacter, Integer>()
 			{
 				@Override
 				public Integer apply(DCharacter character)
 				{
 					return character.getMeta().getAscensions();
 				}
 			})));
 		}
 
 		private static int median(int[] i)
 		{
			if(i == null || i.length < 3) return 1;
 			Arrays.sort(i);
 			int middle = i.length / 2;
 			if(i.length % 2 == 0) return (i[middle - 1] + i[middle]) / 2;
 			else return i[middle];
 		}
 	}
 }
