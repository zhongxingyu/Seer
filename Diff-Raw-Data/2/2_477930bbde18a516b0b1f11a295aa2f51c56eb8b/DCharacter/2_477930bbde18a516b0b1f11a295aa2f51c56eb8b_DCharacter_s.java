 package com.censoredsoftware.demigods.player;
 
 import com.censoredsoftware.demigods.ability.Ability;
 import com.censoredsoftware.demigods.battle.Participant;
 import com.censoredsoftware.demigods.data.DataManager;
 import com.censoredsoftware.demigods.deity.Deity;
 import com.censoredsoftware.demigods.item.DItemStack;
 import com.censoredsoftware.demigods.language.Symbol;
 import com.censoredsoftware.demigods.location.DLocation;
 import com.censoredsoftware.demigods.structure.Structure;
 import com.censoredsoftware.demigods.util.Configs;
 import com.censoredsoftware.demigods.util.Messages;
 import com.google.common.base.Function;
 import com.google.common.base.Predicate;
 import com.google.common.collect.*;
 import org.bukkit.*;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.serialization.ConfigurationSerializable;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 import org.bukkit.potion.PotionEffect;
 
 import java.util.*;
 
 public class DCharacter implements Participant, ConfigurationSerializable
 {
 	private UUID id;
 	private String name;
 	private String player;
 	private boolean alive;
 	private double health;
 	private Integer hunger;
 	private Float experience;
 	private Integer level;
 	private Integer killCount;
 	private UUID location;
 	private UUID bedSpawn;
 	private String deity;
 	private Set<String> minorDeities;
 	private boolean active;
 	private boolean usable;
 	private UUID meta;
 	private UUID inventory;
 	private Set<String> potionEffects;
 	private Set<String> deaths;
 
 	public DCharacter()
 	{
 		deaths = Sets.newHashSet();
 		potionEffects = Sets.newHashSet();
 		minorDeities = Sets.newHashSet();
 	}
 
 	public DCharacter(UUID id, ConfigurationSection conf)
 	{
 		this.id = id;
 		name = conf.getString("name");
 		player = conf.getString("player");
 		if(conf.isBoolean("alive")) alive = conf.getBoolean("alive");
 		health = conf.getDouble("health");
 		hunger = conf.getInt("hunger");
 		experience = Float.valueOf(conf.getString("experience"));
 		level = conf.getInt("level");
 		killCount = conf.getInt("killCount");
 		location = UUID.fromString(conf.getString("location"));
 		if(conf.getString("bedSpawn") != null) bedSpawn = UUID.fromString(conf.getString("bedSpawn"));
 		deity = conf.getString("deity");
 		active = conf.getBoolean("active");
 		usable = conf.getBoolean("usable");
 		meta = UUID.fromString(conf.getString("meta"));
 		if(conf.isList("minorDeities")) minorDeities = Sets.newHashSet(conf.getStringList("minorDeities"));
 		if(conf.isString("inventory")) inventory = UUID.fromString(conf.getString("inventory"));
 		if(conf.isList("deaths")) deaths = Sets.newHashSet(conf.getStringList("deaths"));
 		if(conf.isList("potionEffects")) potionEffects = Sets.newHashSet(conf.getStringList("potionEffects"));
 	}
 
 	@Override
 	public Map<String, Object> serialize()
 	{
 		Map<String, Object> map = Maps.newHashMap();
 		map.put("name", name);
 		map.put("player", player);
 		map.put("alive", alive);
 		map.put("health", health);
 		map.put("hunger", hunger);
 		map.put("experience", experience);
 		map.put("level", level);
 		map.put("killCount", killCount);
 		map.put("location", location.toString());
 		if(bedSpawn != null) map.put("bedSpawn", bedSpawn.toString());
 		map.put("deity", deity);
 		if(minorDeities != null) map.put("minorDeities", Lists.newArrayList(minorDeities));
 		map.put("active", active);
 		map.put("usable", usable);
 		map.put("meta", meta.toString());
 		if(inventory != null) map.put("inventory", inventory.toString());
 		if(deaths != null) map.put("deaths", Lists.newArrayList(deaths));
 		if(potionEffects != null) map.put("potionEffects", Lists.newArrayList(potionEffects));
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
 
 	void setMinorDeities(Set<String> set)
 	{
 		this.minorDeities = set;
 	}
 
 	public void addMinorDeity(Deity deity)
 	{
 		this.minorDeities.add(deity.getName());
 	}
 
 	public void removeMinorDeity(Deity deity)
 	{
 		this.minorDeities.remove(deity.getName());
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
 
 	public void setAlive(boolean alive)
 	{
 		this.alive = alive;
 		Util.save(this);
 	}
 
 	public void setHealth(double health)
 	{
 		this.health = health;
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
 
 	public void setBedSpawn(Location location)
 	{
 		this.bedSpawn = DLocation.Util.create(location).getId();
 	}
 
 	public void setMeta(Meta meta)
 	{
 		this.meta = meta.getId();
 	}
 
 	public void setUsable(boolean usable)
 	{
 		this.usable = usable;
 	}
 
 	public void setPotionEffects(Collection<PotionEffect> potions)
 	{
 		if(potions != null)
 		{
 			if(potionEffects == null) potionEffects = Sets.newHashSet();
 
 			for(PotionEffect potion : potions)
 				potionEffects.add((new SavedPotion(potion)).getId().toString());
 		}
 	}
 
 	public Set<PotionEffect> getPotionEffects()
 	{
 		if(potionEffects == null) potionEffects = Sets.newHashSet();
 
 		Set<PotionEffect> set = new HashSet<PotionEffect>();
 		for(String stringId : potionEffects)
 		{
 			try
 			{
 				PotionEffect potion = Util.getSavedPotion(UUID.fromString(stringId)).toPotionEffect();
 				if(potion != null)
 				{
 					DataManager.savedPotions.remove(UUID.fromString(stringId));
 					set.add(potion);
 				}
 			}
 			catch(Exception ignored)
 			{}
 		}
 
 		potionEffects.clear();
 		return set;
 	}
 
 	public Inventory getInventory()
 	{
 		if(Util.getInventory(inventory) == null) inventory = Util.createEmptyInventory().getId();
 		return Util.getInventory(inventory);
 	}
 
 	public Meta getMeta()
 	{
 		return Util.loadMeta(meta);
 	}
 
 	public OfflinePlayer getOfflinePlayer()
 	{
 		return Bukkit.getOfflinePlayer(player);
 	}
 
 	public String getName()
 	{
 		return name;
 	}
 
 	public boolean isActive()
 	{
 		return active;
 	}
 
 	public Location getLocation()
 	{
 		if(location == null) return null;
 		return DLocation.Util.load(location).toLocation();
 	}
 
 	public Location getBedSpawn()
 	{
 		if(bedSpawn == null) return null;
 		return DLocation.Util.load(bedSpawn).toLocation();
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
 		return level;
 	}
 
 	public boolean isAlive()
 	{
 		return alive;
 	}
 
 	public Double getHealth()
 	{
 		return health;
 	}
 
 	public Double getMaxHealth()
 	{
 		return getDeity().getMaxHealth();
 	}
 
 	public Integer getHunger()
 	{
 		return hunger;
 	}
 
 	public Float getExperience()
 	{
 		return experience;
 	}
 
 	public boolean isDeity(String deityName)
 	{
 		return getDeity().getName().equalsIgnoreCase(deityName);
 	}
 
 	public Deity getDeity()
 	{
 		return Deity.Util.getDeity(this.deity);
 	}
 
 	public Collection<Deity> getMinorDeities()
 	{
 		return Collections2.transform(minorDeities, new Function<String, Deity>()
 		{
 			@Override
 			public Deity apply(String deity)
 			{
 				return Deity.Util.getDeity(deity);
 			}
 		});
 	}
 
 	public String getAlliance()
 	{
 		return getDeity().getAlliance();
 	}
 
 	public int getKillCount()
 	{
 		return killCount;
 	}
 
 	public void setKillCount(int amount)
 	{
 		killCount = amount;
 		Util.save(this);
 	}
 
 	public void addKill()
 	{
 		killCount += 1;
 		Util.save(this);
 	}
 
 	public int getDeathCount()
 	{
 		return deaths.size();
 	}
 
 	public void addDeath()
 	{
 		if(deaths == null) deaths = Sets.newHashSet();
 		deaths.add(new Death(this).getId().toString());
 		Util.save(this);
 	}
 
 	public void addDeath(DCharacter attacker)
 	{
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
 
 	public int getFavorRegen()
 	{
 		int favorRegenSkill = getMeta().getSkill(Skill.Type.FAVOR) != null ? 2 * getMeta().getSkill(Skill.Type.FAVOR).getLevel() : 0;
 		int regenRate = (int) Math.ceil(Configs.getSettingDouble("multipliers.favor") * getDeity().getFavorRegen() + favorRegenSkill);
 		if(regenRate < 30) regenRate = 30;
 		return regenRate;
 	}
 
 	@Override
 	public void setCanPvp(boolean pvp)
 	{
 		DPlayer.Util.getPlayer(getOfflinePlayer()).setCanPvp(pvp);
 	}
 
 	@Override
 	public boolean canPvp()
 	{
 		return DPlayer.Util.getPlayer(getOfflinePlayer()).canPvp();
 	}
 
 	public boolean isUsable()
 	{
 		return usable;
 	}
 
 	public void updateUseable()
 	{
 		usable = Deity.Util.getDeity(this.deity) != null;
 	}
 
 	public UUID getId()
 	{
 		return id;
 	}
 
 	public Collection<Pet> getPets()
 	{
 		return Pet.Util.findByOwner(id);
 	}
 
 	public void remove()
 	{
 		for(Structure structureSave : Structure.Util.getStructureWithFlag(Structure.Flag.DELETE_WITH_OWNER))
 			if(structureSave.hasOwner() && structureSave.getOwner().equals(getId())) structureSave.remove();
 		Util.deleteInventory(getInventory().getId());
 		Util.deleteMeta(getMeta().getId());
 		Util.delete(getId());
 		if(DPlayer.Util.getPlayer(getOfflinePlayer()).getCurrent().getName().equalsIgnoreCase(name)) DPlayer.Util.getPlayer(getOfflinePlayer()).resetCurrent();
 	}
 
 	public void sendAllianceMessage(String message)
 	{
 		DemigodsChatEvent chatEvent = new DemigodsChatEvent(message, DCharacter.Util.getOnlineCharactersWithAlliance(getAlliance()));
 		Bukkit.getPluginManager().callEvent(chatEvent);
 		if(!chatEvent.isCancelled()) for(Player player : chatEvent.getRecipients())
 			player.sendMessage(message);
 	}
 
 	public void chatWithAlliance(String message)
 	{
 		sendAllianceMessage(" " + ChatColor.GRAY + getAlliance() + "s " + ChatColor.DARK_GRAY + "" + Symbol.BLACK_FLAG + " " + getDeity().getColor() + name + ChatColor.GRAY + ": " + ChatColor.RESET + message);
 		Messages.info("[" + getAlliance() + "]" + name + ": " + message);
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
 			if(conf.getString("leggings") != null) leggings = UUID.fromString(conf.getString("leggings"));
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
 				{
 					if(this.items[i] != null)
 					{
 						ItemStack itemStack = DItemStack.Util.load(UUID.fromString(this.items[i])).toItemStack();
 						if(itemStack != null) inventory.setItem(i, DItemStack.Util.load(UUID.fromString(this.items[i])).toItemStack());
 					}
 				}
 			}
 
 			// Delete
 			Util.deleteInventory(id);
 		}
 	}
 
 	public static class Meta implements ConfigurationSerializable
 	{
 		private UUID id;
 		private UUID character;
 		private int favor;
 		private int maxFavor;
 		private int skillPoints;
 		private Set<String> notifications;
 		private Map<String, Object> binds;
 		private Map<String, Object> skillData;
 		private Map<String, Object> warps;
 		private Map<String, Object> invites;
 
 		public Meta()
 		{}
 
 		public Meta(UUID id, ConfigurationSection conf)
 		{
 			this.id = id;
 			favor = conf.getInt("favor");
 			maxFavor = conf.getInt("maxFavor");
 			skillPoints = conf.getInt("skillPoints");
 			notifications = Sets.newHashSet(conf.getStringList("notifications"));
 			character = UUID.fromString(conf.getString("character"));
 			if(conf.getConfigurationSection("skillData") != null) skillData = conf.getConfigurationSection("skillData").getValues(false);
 			if(conf.getConfigurationSection("binds") != null) binds = conf.getConfigurationSection("binds").getValues(false);
 			if(conf.getConfigurationSection("warps") != null) warps = conf.getConfigurationSection("warps").getValues(false);
 			if(conf.getConfigurationSection("invites") != null) invites = conf.getConfigurationSection("invites").getValues(false);
 		}
 
 		@Override
 		public Map<String, Object> serialize()
 		{
 			Map<String, Object> map = Maps.newHashMap();
 			map.put("character", character.toString());
 			map.put("favor", favor);
 			map.put("maxFavor", maxFavor);
 			map.put("skillPoints", skillPoints);
 			map.put("notifications", Lists.newArrayList(notifications));
 			map.put("binds", binds);
 			map.put("skillData", skillData);
 			map.put("warps", warps);
 			map.put("invites", invites);
 			return map;
 		}
 
 		public void generateId()
 		{
 			id = UUID.randomUUID();
 		}
 
 		void setCharacter(DCharacter character)
 		{
 			this.character = character.getId();
 		}
 
 		void initialize()
 		{
 			notifications = Sets.newHashSet();
 			warps = Maps.newHashMap();
 			invites = Maps.newHashMap();
 			skillData = Maps.newHashMap();
 			binds = Maps.newHashMap();
 		}
 
 		public UUID getId()
 		{
 			return id;
 		}
 
 		public DCharacter getCharacter()
 		{
 			return DCharacter.Util.load(character);
 		}
 
 		public void setSkillPoints(int skillPoints)
 		{
 			this.skillPoints = skillPoints;
 		}
 
 		public int getSkillPoints()
 		{
 			return skillPoints;
 		}
 
 		public void addSkillPoints(int skillPoints)
 		{
 			setSkillPoints(getSkillPoints() + skillPoints);
 		}
 
 		public void subtractSkillPoints(int skillPoints)
 		{
 			setSkillPoints(getSkillPoints() - skillPoints);
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
 			return !notifications.isEmpty();
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
 
 		public void resetSkills()
 		{
 			getRawSkills().clear();
 			for(Skill.Type type : Skill.Type.values())
 				if(type.isDefault()) addSkill(Skill.Util.createSkill(getCharacter(), type));
 		}
 
 		public void cleanSkills()
 		{
 			List<String> toRemove = Lists.newArrayList();
 
 			// Locate obselete skills
 			for(String skillName : getRawSkills().keySet())
 			{
 				try
 				{
 					// Attempt to find the value of the skillname
 					Skill.Type.valueOf(skillName.toUpperCase());
 				}
 				catch(Exception ignored)
 				{
 					// There was an error. Catch it and remove the skill.
 					toRemove.add(skillName);
 				}
 			}
 
 			// Remove the obsolete skills
 			for(String skillName : toRemove)
 				getRawSkills().remove(skillName);
 		}
 
 		public void addSkill(Skill skill)
 		{
 			if(!getRawSkills().containsKey(skill.getType().toString())) getRawSkills().put(skill.getType().toString(), skill.getId().toString());
 			Util.saveMeta(this);
 		}
 
 		public Skill getSkill(Skill.Type type)
 		{
 			if(getRawSkills().containsKey(type.toString())) return Skill.Util.loadSkill(UUID.fromString(getRawSkills().get(type.toString()).toString()));
 			return null;
 		}
 
 		public Map<String, Object> getRawSkills()
 		{
 			if(skillData == null) skillData = Maps.newHashMap();
 			return skillData;
 		}
 
 		public Collection<Skill> getSkills()
 		{
 			return Collections2.transform(getRawSkills().values(), new Function<Object, Skill>()
 			{
 				@Override
 				public Skill apply(Object obj)
 				{
 					return Skill.Util.loadSkill(UUID.fromString(obj.toString()));
 				}
 			});
 		}
 
 		public boolean checkBound(String abilityName, Material material)
 		{
 			return getBinds().containsKey(abilityName) && binds.get(abilityName).equals(material.name());
 		}
 
 		public boolean isBound(Ability ability)
 		{
 			return getBinds().containsKey(ability.getName());
 		}
 
 		public boolean isBound(Material material)
 		{
 			return getBinds().containsValue(material.name());
 		}
 
 		public void setBind(Ability ability, Material material)
 		{
 			getBinds().put(ability.getName(), material.name());
 		}
 
 		public Map<String, Object> getBinds()
 		{
 			if(binds == null) binds = Maps.newHashMap();
 			return this.binds;
 		}
 
 		public void removeBind(Ability ability)
 		{
 			getBinds().remove(ability.getName());
 		}
 
 		public void removeBind(Material material)
 		{
 			if(getBinds().containsValue(material.name()))
 			{
 				String toRemove = null;
 				for(Map.Entry<String, Object> entry : getBinds().entrySet())
 				{
 					toRemove = entry.getValue().equals(material.name()) ? entry.getKey() : null;
 				}
 				getBinds().remove(toRemove);
 			}
 		}
 
 		public int getIndividualSkillCap()
 		{
 			int total = 0;
 			for(Skill skill : getSkills())
 				total += skill.getLevel();
 			return getOverallSkillCap() - total;
 		}
 
 		public int getOverallSkillCap()
 		{
 			// TODO: I'm setting this up like this so it can be manipulated later on on an individual basis.
 			return Configs.getSettingInt("caps.skills");
 		}
 
 		public int getAscensions()
 		{
 			double total = 0.0;
 
 			for(Skill skill : getSkills())
 				total += skill.getLevel();
 
 			return (int) Math.ceil(total / getSkills().size());
 		}
 
 		public Integer getFavor()
 		{
 			return favor;
 		}
 
 		public void setFavor(int amount)
 		{
 			favor = amount;
 			Util.saveMeta(this);
 		}
 
 		public void addFavor(int amount)
 		{
 			if((favor + amount) > maxFavor) favor = maxFavor;
 			else favor += amount;
 			Util.saveMeta(this);
 		}
 
 		public void subtractFavor(int amount)
 		{
 			if((favor - amount) < 0) favor = 0;
 			else favor -= amount;
 			Util.saveMeta(this);
 		}
 
 		public Integer getMaxFavor()
 		{
 			return maxFavor;
 		}
 
 		public void addMaxFavor(int amount)
 		{
 			if((maxFavor + amount) > Configs.getSettingInt("caps.favor")) maxFavor = Configs.getSettingInt("caps.favor");
 			else maxFavor += amount;
 			Util.saveMeta(this);
 		}
 
 		public void setMaxFavor(int amount)
 		{
 			if(amount < 0) maxFavor = 0;
 			if(amount > Configs.getSettingInt("caps.favor")) maxFavor = Configs.getSettingInt("caps.favor");
 			else maxFavor = amount;
 			Util.saveMeta(this);
 		}
 
 		public void subtractMaxFavor(int amount)
 		{
 			setMaxFavor(getMaxFavor() - amount);
 		}
 
 		@Override
 		public Object clone() throws CloneNotSupportedException
 		{
 			throw new CloneNotSupportedException();
 		}
 	}
 
 	@Override
 	public Object clone() throws CloneNotSupportedException
 	{
 		throw new CloneNotSupportedException();
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
 				return create(player, charName, Deity.Util.getDeity(charDeity));
 			}
 			return null;
 		}
 
 		private static DCharacter create(final DPlayer player, final String charName, final Deity deity)
 		{
 			DCharacter character = new DCharacter();
 			character.generateId();
 			character.setAlive(true);
 			character.setPlayer(player);
 			character.setName(charName);
 			character.setDeity(deity);
 			character.setMinorDeities(new HashSet<String>(0));
 			character.setUsable(true);
 			character.setHealth(deity.getMaxHealth());
 			character.setHunger(20);
 			character.setExperience(0);
 			character.setLevel(0);
 			character.setKillCount(0);
 			character.setLocation(player.getOfflinePlayer().getPlayer().getLocation());
 			character.setMeta(Util.createMeta(character));
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
 
 		public static Meta createMeta(DCharacter character)
 		{
 			Meta charMeta = new Meta();
 			charMeta.initialize();
 			charMeta.setCharacter(character);
 			charMeta.generateId();
 			charMeta.setFavor(Configs.getSettingInt("character.defaults.favor"));
 			charMeta.setMaxFavor(Configs.getSettingInt("character.defaults.max_favor"));
 			charMeta.resetSkills();
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
 
 		public static SavedPotion getSavedPotion(UUID id)
 		{
 			try
 			{
 				return DataManager.savedPotions.get(id);
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
 				return Iterators.find(loadAll().iterator(), new Predicate<DCharacter>()
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
 					return character.isUsable() && character.isActive();
 				}
 			});
 		}
 
 		public static Set<DCharacter> getAllUsable()
 		{
 			return Sets.filter(loadAll(), new Predicate<DCharacter>()
 			{
 				@Override
 				public boolean apply(DCharacter character)
 				{
 					return character.isUsable();
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
 
 		public static Collection<DCharacter> getOnlineCharactersWithAbility(final String abilityName)
 		{
 			return getCharactersWithPredicate(new Predicate<DCharacter>()
 			{
 				@Override
 				public boolean apply(DCharacter character)
 				{
 					if(character.isActive() && character.getOfflinePlayer().isOnline())
 					{
 						for(Ability abilityToCheck : character.getDeity().getAbilities())
 							if(abilityToCheck.getName().equalsIgnoreCase(abilityName)) return true;
 					}
 					return false;
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
 
 		public static Collection<DCharacter> getOnlineCharactersWithoutAlliance(final String alliance)
 		{
 			return getCharactersWithPredicate(new Predicate<DCharacter>()
 			{
 				@Override
 				public boolean apply(DCharacter character)
 				{
 					return character.isActive() && character.getOfflinePlayer().isOnline() && !character.getAlliance().equalsIgnoreCase(alliance);
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
 			return Collections2.filter(getAllUsable(), predicate);
 		}
 
 		/**
 		 * Updates favor for all online characters.
 		 */
 		public static void updateFavor()
 		{
 			for(Player player : Bukkit.getOnlinePlayers())
 			{
 				DCharacter character = DPlayer.Util.getPlayer(player).getCurrent();
 				if(character != null) character.getMeta().addFavor(character.getFavorRegen());
 			}
 		}
 	}
 }
