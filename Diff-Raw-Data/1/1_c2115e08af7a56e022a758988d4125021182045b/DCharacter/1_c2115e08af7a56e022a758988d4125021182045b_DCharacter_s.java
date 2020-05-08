 package com.censoredsoftware.demigods.engine.player;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.entity.ExperienceOrb;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 
 import redis.clients.johm.*;
 
 import com.censoredsoftware.demigods.engine.Demigods;
 import com.censoredsoftware.demigods.engine.battle.Battle;
 import com.censoredsoftware.demigods.engine.data.DataManager;
 import com.censoredsoftware.demigods.engine.element.Ability;
 import com.censoredsoftware.demigods.engine.element.Deity;
 import com.censoredsoftware.demigods.engine.element.Structure.Structure;
 import com.censoredsoftware.demigods.engine.language.Translation;
 import com.censoredsoftware.demigods.engine.location.DLocation;
 import com.censoredsoftware.demigods.engine.util.Structures;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Sets;
 
 @Model
 public class DCharacter implements Battle.Participant
 {
 	@Id
 	private Long id;
 	@Attribute
 	@Indexed
 	private String name;
 	@Attribute
 	@Indexed
 	private long player;
 	@Attribute
 	private double health;
 	@Attribute
 	private double maxhealth;
 	@Attribute
 	private Integer hunger;
 	@Attribute
 	private Float experience;
 	@Attribute
 	private Integer level;
 	@Attribute
 	private Integer kills;
 	@Attribute
 	private Integer deaths;
 	@Reference
 	private DLocation location;
 	@Attribute
 	@Indexed
 	private String deity;
 	@Attribute
 	@Indexed
 	private Boolean active;
 	@Attribute
 	@Indexed
 	private Boolean immortal;
 	@Attribute
 	@Indexed
 	private Boolean usable;
 	@Reference
 	private Meta meta;
 	@Reference
 	private Inventory inventory;
 
 	void setName(String name)
 	{
 		this.name = name;
 	}
 
 	void setDeity(Deity deity)
 	{
 		this.deity = deity.getInfo().getName();
 	}
 
 	void setPlayer(DPlayer player)
 	{
 		this.player = player.getId();
 	}
 
 	public void setImmortal(boolean option)
 	{
 		this.immortal = option;
 		JOhm.save(this);
 	}
 
 	public void setActive(boolean option)
 	{
 		this.active = option;
 		JOhm.save(this);
 	}
 
 	public void saveInventory()
 	{
 		this.inventory = Util.createInventory(this);
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
 		this.location = DLocation.Util.create(location);
 	}
 
 	public void setMeta(Meta meta)
 	{
 		this.meta = meta;
 	}
 
 	public void setUsable(boolean usable)
 	{
 		this.usable = usable;
 	}
 
 	public Inventory getInventory()
 	{
 		if(this.inventory == null) this.inventory = Util.createEmptyInventory();
 		return this.inventory;
 	}
 
 	public Meta getMeta()
 	{
 		if(this.meta == null) this.meta = Util.createMeta();
 		return this.meta;
 	}
 
 	public OfflinePlayer getOfflinePlayer()
 	{
 		return DPlayer.Util.load(this.player).getOfflinePlayer();
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
 		return this.location.toLocation();
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
 		return getDeity().getInfo().getName().equalsIgnoreCase(deityName);
 	}
 
 	public Deity getDeity()
 	{
 		return Deity.Util.getDeity(this.deity);
 	}
 
 	public String getAlliance()
 	{
 		return getDeity().getInfo().getAlliance();
 	}
 
 	public Boolean isImmortal()
 	{
 		return this.immortal;
 	}
 
 	/**
 	 * Returns the number of total kills.
 	 * 
 	 * @return int
 	 */
 	public int getKills()
 	{
 		return this.kills;
 	}
 
 	/**
 	 * Sets the amount of kills to <code>amount</code>.
 	 * 
 	 * @param amount the amount of kills to set to.
 	 */
 	public void setKills(int amount)
 	{
 		this.kills = amount;
 		JOhm.save(this);
 	}
 
 	/**
 	 * Adds 1 kill.
 	 */
 	public void addKill()
 	{
 		this.kills += 1;
 		JOhm.save(this);
 	}
 
 	/**
 	 * Returns the number of deaths.
 	 * 
 	 * @return int
 	 */
 	public int getDeaths()
 	{
 		return this.deaths;
 	}
 
 	/**
 	 * Sets the number of deaths to <code>amount</code>.
 	 * 
 	 * @param amount the amount of deaths to set.
 	 */
 	public void setDeaths(int amount)
 	{
 		this.deaths = amount;
 		JOhm.save(this);
 	}
 
 	/**
 	 * Adds a death.
 	 */
 	public void addDeath()
 	{
 		this.deaths += 1;
 		JOhm.save(this);
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
 		for(Ability.Bind bind : getMeta().getBinds())
 		{
 			Player player = getOfflinePlayer().getPlayer();
 			player.getInventory().setItem(bind.getSlot(), bind.getItem());
 		}
 	}
 
 	public Long getId()
 	{
 		return id;
 	}
 
 	public void remove()
 	{
 		for(Structure.Save structureSave : Structures.getStructuresSavesWithFlag(Structure.Flag.DELETE_WITH_OWNER))
 		{
 			if(structureSave.hasOwner() && structureSave.getOwner().getId().equals(getId())) structureSave.remove();
 		}
 		JOhm.delete(Inventory.class, getInventory().getId());
 		JOhm.delete(Meta.class, getMeta().getId());
 		JOhm.delete(DCharacter.class, getId());
 	}
 
 	@Model
 	public static class Inventory
 	{
 		@Id
 		private Long id;
 		@Reference
 		private DItemStack helmet;
 		@Reference
 		private DItemStack chestplate;
 		@Reference
 		private DItemStack leggings;
 		@Reference
 		private DItemStack boots;
 		@Array(of = DItemStack.class, length = 36)
 		private DItemStack[] items;
 
 		void setHelmet(ItemStack helmet)
 		{
 			this.helmet = DItemStack.Util.create(helmet);
 		}
 
 		void setChestplate(ItemStack chestplate)
 		{
 			this.chestplate = DItemStack.Util.create(chestplate);
 		}
 
 		void setLeggings(ItemStack leggings)
 		{
 			this.leggings = DItemStack.Util.create(leggings);
 		}
 
 		void setBoots(ItemStack boots)
 		{
 			this.boots = DItemStack.Util.create(boots);
 		}
 
 		void setItems(org.bukkit.inventory.Inventory inventory)
 		{
 			if(this.items == null) this.items = new DItemStack[36];
 			for(int i = 0; i < 35; i++)
 			{
 				if(inventory.getItem(i) == null)
 				{
 					this.items[i] = DItemStack.Util.create(new ItemStack(Material.AIR));
 				}
 				else
 				{
 					this.items[i] = DItemStack.Util.create(inventory.getItem(i));
 				}
 			}
 		}
 
 		public Long getId()
 		{
 			return this.id;
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
 			if(this.helmet != null) inventory.setHelmet(this.helmet.toItemStack());
 			if(this.chestplate != null) inventory.setChestplate(this.chestplate.toItemStack());
 			if(this.leggings != null) inventory.setLeggings(this.leggings.toItemStack());
 			if(this.boots != null) inventory.setBoots(this.boots.toItemStack());
 
 			if(this.items != null)
 			{
 				// Set items
 				for(int i = 0; i < 35; i++)
 				{
 					if(this.items[i] != null) inventory.setItem(i, this.items[i].toItemStack());
 				}
 			}
 
 			// Delete
 			JOhm.delete(Inventory.class, id);
 		}
 	}
 
 	@Model
 	public static class Meta
 	{
 		@Id
 		private Long id;
 		@Attribute
 		private Integer ascensions;
 		@Attribute
 		private Integer favor;
 		@Attribute
 		private Integer maxFavor;
 		@CollectionSet(of = Ability.Bind.class)
 		private Set<Ability.Bind> binds;
 		@CollectionMap(key = String.class, value = Boolean.class)
 		private Map<String, Boolean> taskData;
 		@CollectionMap(key = String.class, value = Boolean.class)
 		private Map<String, Ability.Devotion> devotionData;
 		@CollectionMap(key = String.class, value = DLocation.class)
 		private Map<String, DLocation> warps;
 		@CollectionMap(key = String.class, value = DLocation.class)
 		private Map<String, DLocation> invites;
 		@CollectionSet(of = Notification.class)
 		private Set<Notification> notifications;
 
 		void initialize()
 		{
 			// this.binds = Sets.newHashSet();
 			this.taskData = new HashMap<String, Boolean>();
 			this.devotionData = new HashMap<String, Ability.Devotion>();
 		}
 
 		public long getId()
 		{
 			return this.id;
 		}
 
 		public void addNotification(Notification notification)
 		{
 			getNotifications().add(notification);
 			JOhm.save(this);
 		}
 
 		public void removeNotification(Notification notification)
 		{
 			getNotifications().remove(notification);
 			JOhm.save(this);
 		}
 
 		public Set<Notification> getNotifications()
 		{
 			if(this.notifications == null) this.notifications = Sets.newHashSet();
 			return this.notifications;
 		}
 
 		public void clearNotifications()
 		{
 			getNotifications().clear();
 		}
 
 		public boolean hasNotifications()
 		{
 			return !this.notifications.isEmpty();
 		}
 
 		public void addWarp(String name, Location location)
 		{
 			getWarps().put(name.toLowerCase(), DLocation.Util.create(location));
 			JOhm.save(this);
 		}
 
 		public void removeWarp(String name)
 		{
 			getWarps().remove(name.toLowerCase());
 			JOhm.save(this);
 		}
 
 		public Map<String, DLocation> getWarps()
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
 			getInvites().put(name.toLowerCase(), DLocation.Util.create(location));
 			JOhm.save(this);
 		}
 
 		public void removeInvite(String name)
 		{
 			getInvites().remove(name.toLowerCase());
 			JOhm.save(this);
 		}
 
 		public Map<String, DLocation> getInvites()
 		{
 			if(this.invites == null) this.invites = Maps.newHashMap();
 			return this.invites;
 		}
 
 		public void clearInvites()
 		{
 			getInvites().clear();
 		}
 
 		public boolean hasInvites()
 		{
 			return !this.invites.isEmpty();
 		}
 
 		public void addDevotion(Ability.Devotion devotion)
 		{
 			if(!this.devotionData.containsKey(devotion.getType().toString())) this.devotionData.put(devotion.getType().toString(), devotion);
 			JOhm.save(this);
 		}
 
 		public Ability.Devotion getDevotion(Ability.Devotion.Type type)
 		{
 			if(this.devotionData.containsKey(type.toString()))
 			{
 				return this.devotionData.get(type.toString());
 			}
 			else
 			{
 				addDevotion(Ability.Util.createDevotion(type));
 				return this.devotionData.get(type.toString());
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
 			this.binds.add(bind);
 		}
 
 		public Ability.Bind setBound(String ability, int slot, ItemStack item)
 		{
 			Ability.Bind bind = Ability.Util.createBind(ability, slot, item);
 			this.binds.add(bind);
 			return bind;
 		}
 
 		public Ability.Bind getBind(int slot)
 		{
 			for(Ability.Bind bind : this.binds)
 			{
 				if(bind.getSlot() == slot) return bind;
 			}
 			return null;
 		}
 
 		public Ability.Bind getBind(String ability)
 		{
 			for(Ability.Bind bind : this.binds)
 			{
 				if(bind.getAbility().equalsIgnoreCase(ability)) return bind;
 			}
 			return null;
 		}
 
 		public Ability.Bind getBind(ItemStack item)
 		{
 			for(Ability.Bind bind : this.binds)
 			{
 				if(item.hasItemMeta() && item.getItemMeta().hasLore() && item.getItemMeta().getLore().toString().contains(bind.getIdentifier()))
 				{
 					return bind;
 				}
 			}
 			return null;
 		}
 
 		public Set<Ability.Bind> getBinds()
 		{
 			return this.binds;
 		}
 
 		public void removeBind(String ability)
 		{
 			if(isBound(ability))
 			{
 				Ability.Bind bind = getBind(ability);
 				this.binds.remove(bind);
 				JOhm.delete(Ability.Bind.class, bind.getId());
 			}
 		}
 
 		public void removeBind(ItemStack item)
 		{
 			if(isBound(item))
 			{
 				Ability.Bind bind = getBind(item);
 				this.binds.remove(bind);
 				JOhm.delete(Ability.Bind.class, bind.getId());
 			}
 		}
 
 		public void removeBind(Ability.Bind bind)
 		{
 			this.binds.remove(bind);
 			JOhm.delete(Ability.Bind.class, bind.getId());
 		}
 
 		public boolean isFinishedTask(String taskName)
 		{
 			return taskData.containsKey(taskName) && taskData.get(taskName);
 		}
 
 		public void finishTask(String taskName, boolean option)
 		{
 			taskData.put(taskName, option);
 		}
 
 		public Integer getAscensions()
 		{
 			return this.ascensions;
 		}
 
 		public void addAscension()
 		{
 			this.ascensions += 1;
 			JOhm.save(this);
 		}
 
 		public void addAscensions(int amount)
 		{
 			this.ascensions += amount;
 			JOhm.save(this);
 		}
 
 		public void subtractAscensions(int amount)
 		{
 			this.ascensions -= amount;
 			JOhm.save(this);
 		}
 
 		public void setAscensions(int amount)
 		{
 			this.ascensions = amount;
 			JOhm.save(this);
 		}
 
 		public Integer getFavor()
 		{
 			return this.favor;
 		}
 
 		public void setFavor(int amount)
 		{
 			this.favor = amount;
 			JOhm.save(this);
 		}
 
 		public void addFavor(int amount)
 		{
 			if((this.favor + amount) > this.maxFavor)
 			{
 				this.favor = this.maxFavor;
 			}
 			else
 			{
 				this.favor += amount;
 			}
 			JOhm.save(this);
 		}
 
 		public void subtractFavor(int amount)
 		{
 			if((this.favor - amount) < 0)
 			{
 				this.favor = 0;
 			}
 			else
 			{
 				this.favor -= amount;
 			}
 			JOhm.save(this);
 		}
 
 		public Integer getMaxFavor()
 		{
 			return this.maxFavor;
 		}
 
 		public void addMaxFavor(int amount)
 		{
 			if((this.maxFavor + amount) > Demigods.config.getSettingInt("caps.favor"))
 			{
 				this.maxFavor = Demigods.config.getSettingInt("caps.favor");
 			}
 			else
 			{
 				this.maxFavor += amount;
 			}
 			JOhm.save(this);
 		}
 
 		public void setMaxFavor(int amount)
 		{
 			if(amount < 0) this.maxFavor = 0;
 			if(amount > Demigods.config.getSettingInt("caps.favor")) this.maxFavor = Demigods.config.getSettingInt("caps.favor");
 			else this.maxFavor = amount;
 			JOhm.save(this);
 		}
 
 		@Override
 		public Object clone() throws CloneNotSupportedException
 		{
 			throw new CloneNotSupportedException();
 		}
 	}
 
 	public static class Util
 	{
 		public static void create(DPlayer player, String chosenDeity, String chosenName, boolean switchCharacter)
 		{
 			DCharacter character = create(player, chosenName, chosenDeity);
 
 			if(player.getOfflinePlayer().isOnline())
 			{
 				Player online = player.getOfflinePlayer().getPlayer();
 				online.setDisplayName(Deity.Util.getDeity(chosenDeity).getInfo().getColor() + chosenName + ChatColor.WHITE);
 				online.setPlayerListName(Deity.Util.getDeity(chosenDeity).getInfo().getColor() + chosenName + ChatColor.WHITE);
 
 				online.sendMessage(ChatColor.GREEN + Demigods.language.getText(Translation.Text.CHARACTER_CREATE_COMPLETE).replace("{deity}", chosenDeity));
 				online.getWorld().strikeLightningEffect(online.getLocation());
 
 				for(int i = 0; i < 20; i++)
 					online.getWorld().spawn(online.getLocation(), ExperienceOrb.class);
 			}
 
 			// Switch to new character
 			if(switchCharacter) player.switchCharacter(character);
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
 			character.setPlayer(player);
 			character.setName(charName);
 			character.setDeity(deity);
 			character.setImmortal(immortal);
 			character.setUsable(true);
 			character.setMaxHealth(40.0);
 			character.setHealth(40.0);
 			character.setHunger(20);
 			character.setExperience(0);
 			character.setLevel(0);
 			character.setKills(0);
 			character.setDeaths(0);
 			character.setLocation(player.getOfflinePlayer().getPlayer().getLocation());
 			character.setMeta(Util.createMeta());
 			JOhm.save(character);
 			return character;
 		}
 
 		public static Inventory createInventory(DCharacter character)
 		{
 			PlayerInventory inventory = character.getOfflinePlayer().getPlayer().getInventory();
 			Inventory charInventory = new Inventory();
 			if(inventory.getHelmet() != null) charInventory.setHelmet(inventory.getHelmet());
 			if(inventory.getChestplate() != null) charInventory.setChestplate(inventory.getChestplate());
 			if(inventory.getLeggings() != null) charInventory.setLeggings(inventory.getLeggings());
 			if(inventory.getBoots() != null) charInventory.setBoots(inventory.getBoots());
 			charInventory.setItems(inventory);
 			JOhm.save(charInventory);
 			JOhm.save(character);
 			return charInventory;
 		}
 
 		public static Inventory createEmptyInventory()
 		{
 			Inventory charInventory = new Inventory();
 			charInventory.setHelmet(new ItemStack(Material.AIR));
 			charInventory.setChestplate(new ItemStack(Material.AIR));
 			charInventory.setLeggings(new ItemStack(Material.AIR));
 			charInventory.setBoots(new ItemStack(Material.AIR));
 			JOhm.save(charInventory);
 			return charInventory;
 		}
 
 		public static Meta createMeta()
 		{
 			Meta charMeta = new Meta();
 			charMeta.initialize();
 			charMeta.setAscensions(Demigods.config.getSettingInt("character.defaults.ascensions"));
 			charMeta.setFavor(Demigods.config.getSettingInt("character.defaults.favor"));
 			charMeta.setMaxFavor(Demigods.config.getSettingInt("character.defaults.max_favor"));
 			charMeta.addDevotion(Ability.Util.createDevotion(Ability.Devotion.Type.OFFENSE));
 			charMeta.addDevotion(Ability.Util.createDevotion(Ability.Devotion.Type.DEFENSE));
 			charMeta.addDevotion(Ability.Util.createDevotion(Ability.Devotion.Type.PASSIVE));
 			charMeta.addDevotion(Ability.Util.createDevotion(Ability.Devotion.Type.STEALTH));
 			charMeta.addDevotion(Ability.Util.createDevotion(Ability.Devotion.Type.SUPPORT));
 			charMeta.addDevotion(Ability.Util.createDevotion(Ability.Devotion.Type.ULTIMATE));
 			JOhm.save(charMeta, true);
 			return charMeta;
 		}
 
 		public static DCharacter load(Long id)
 		{
 			return JOhm.get(DCharacter.class, id);
 		}
 
 		public static Set<DCharacter> loadAll()
 		{
 			return JOhm.getAll(DCharacter.class);
 		}
 
 		public static void updateUsableCharacters()
 		{
 			for(DCharacter character : loadAll())
 			{
 				character.updateUseable();
 			}
 		}
 
 		public static DCharacter getCharacterByName(String name)
 		{
 			for(DCharacter loaded : loadAll())
 				if(loaded.getName().equalsIgnoreCase(name)) return loaded;
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
 			Set<DCharacter> active = Sets.newHashSet();
 			for(DCharacter character : loadAll())
 			{
 				if(character.isActive()) active.add(character);
 			}
 			return active;
 		}
 
 		// TODO Remake this.
 		public static void onCharacterKillCharacter(DCharacter attacker, DCharacter killed)
 		{
 			String attackerAlliance = "Mortal";
 			if(attacker != null) attackerAlliance = attacker.getAlliance();
 			String killedAlliance = "Mortal";
 			if(killed != null) killedAlliance = killed.getAlliance();
 
 			if(attacker != null)
 			{
 				attacker.addKill();
 			}
 
 			if(killed == null) Demigods.message.broadcast(Demigods.language.getText(Translation.Text.MORTAL_SLAIN_2).replace("{attacker}", ChatColor.YELLOW + attacker.getName() + ChatColor.GRAY).replace("{attackerAlliance}", attackerAlliance));
 			else Demigods.message.broadcast(ChatColor.GRAY + Demigods.language.getText(Translation.Text.DEMI_SLAIN_2).replace("{killed}", ChatColor.YELLOW + killed.getName() + ChatColor.GRAY).replace("{killedAlliance}", killedAlliance).replace("{attacker}", ChatColor.YELLOW + attacker.getName() + ChatColor.GRAY).replace("{attackerAlliance}", attackerAlliance));
 		}
 
 		// TODO Remake this.
 		public static void onCharacterBetrayCharacter(DCharacter attacker, DCharacter killed)
 		{
 			String alliance = attacker.getAlliance();
 
 			// TODO: Punishments.
 
 			if(!alliance.equals("Mortal")) Demigods.message.broadcast(ChatColor.GRAY + Demigods.language.getText(Translation.Text.DEMI_BETRAY).replace("{killed}", ChatColor.YELLOW + killed.getName() + ChatColor.GRAY).replace("{attacker}", ChatColor.YELLOW + attacker.getName() + ChatColor.GRAY).replace("{alliance}", alliance));
 			else Demigods.message.broadcast(ChatColor.GRAY + Demigods.language.getText(Translation.Text.MORTAL_BETRAY));
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
 	}
 }
