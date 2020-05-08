 package com.censoredsoftware.Demigods.Engine.Object;
 
 import java.util.*;
 
 import org.bukkit.*;
 import org.bukkit.conversations.Conversation;
 import org.bukkit.conversations.ConversationContext;
 import org.bukkit.entity.ExperienceOrb;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 import org.bukkit.scheduler.BukkitRunnable;
 
 import redis.clients.johm.*;
 
 import com.censoredsoftware.Demigods.Engine.Conversation.Prayer;
 import com.censoredsoftware.Demigods.Engine.Demigods;
 import com.censoredsoftware.Demigods.Engine.Utility.DataUtility;
 import com.censoredsoftware.Demigods.Engine.Utility.TextUtility;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Sets;
 
 @Model
 public class DPlayer
 {
 	@Id
 	private Long id;
 	@Attribute
 	@Indexed
 	private String player;
 	@Attribute
 	private long lastLoginTime;
 	@Attribute
 	@Indexed
 	private Boolean PvP;
 	@Reference
 	private Character current;
 	@Reference
 	private Character previous;
 
 	void setPlayer(String player)
 	{
 		this.player = player;
 		Util.save(this);
 	}
 
 	public OfflinePlayer getOfflinePlayer()
 	{
 		return Bukkit.getOfflinePlayer(this.player);
 	}
 
 	public void setLastLoginTime(Long time)
 	{
 		this.lastLoginTime = time;
 		Util.save(this);
 	}
 
 	public void setPvP(Boolean PvP)
 	{
 		this.PvP = PvP;
 		Util.save(this);
 	}
 
 	public Long getLastLoginTime()
 	{
 		return this.lastLoginTime;
 	}
 
 	public Boolean getPvP()
 	{
 		return this.PvP;
 	}
 
 	public void switchCharacter(Character newChar)
 	{
 		Player player = getOfflinePlayer().getPlayer();
 
 		if(!newChar.getOfflinePlayer().equals(getOfflinePlayer()))
 		{
 			player.sendMessage(ChatColor.RED + "You can't do that.");
 			return;
 		}
 
 		// Update the current character
 		Character currChar = getCurrent();
 		if(currChar != null)
 		{
 			// Set the values
 			// TODO: Confirm that this covers all of the bases.
 			currChar.setMaxHealth(player.getMaxHealth());
 			currChar.setHealth(player.getHealth());
 			currChar.setHunger(player.getFoodLevel());
 			currChar.setLevel(player.getLevel());
 			currChar.setExperience(player.getExp());
 			currChar.setLocation(player.getLocation());
 			currChar.saveInventory();
 
 			// Disown pets
 			Pet.Util.disownPets(currChar.getName());
 
 			// Set to inactive and update previous
 			currChar.setActive(false);
 			this.previous = currChar;
 
 			// Save it
 			Character.Util.save(currChar);
 		}
 
 		// Update their inventory
 		if(Util.getCharacters(player).size() == 1) newChar.saveInventory();
 		newChar.getInventory().setToPlayer(player);
 
 		// Update health, experience, and name
 		// TODO: Confirm that this covers all of the bases too.
 		player.setDisplayName(newChar.getDeity().getInfo().getColor() + newChar.getName());
 		try
 		{
 			player.setPlayerListName(newChar.getDeity().getInfo().getColor() + newChar.getName());
 		}
 		catch(Exception e)
 		{
 			Demigods.message.warning("Character name too long.");
 			e.printStackTrace();
 		}
 		player.setMaxHealth(newChar.getMaxHealth());
 		player.setHealth(newChar.getHealth());
 		player.setFoodLevel(newChar.getHunger());
 		player.setExp(newChar.getExperience());
 		player.setLevel(newChar.getLevel());
 
 		// Set new character to active
 		newChar.setActive(true);
 		this.current = newChar;
 
 		// Re-own pets
 		Pet.Util.reownPets(player, newChar);
 
 		// Teleport them
 		try
 		{
 			player.teleport(newChar.getLocation());
 		}
 		catch(Exception e)
 		{
 			Demigods.message.severe("There was a problem while teleporting a player to their character.");
 		}
 
 		// Save instances
 		Util.save(this);
 		Character.Util.save(newChar);
 	}
 
 	public boolean hasCurrent()
 	{
 		return getCurrent() != null;
 	}
 
 	public Character getCurrent()
 	{
 		if(this.current != null && this.current.canUse()) return this.current;
 		return null;
 	}
 
 	public Region getRegion()
 	{
 		if(getOfflinePlayer().isOnline()) return Region.Util.getRegion(getOfflinePlayer().getPlayer().getLocation());
 		return Region.Util.getRegion(getCurrent().getLocation());
 	}
 
 	public Character getPrevious()
 	{
 		return this.previous;
 	}
 
 	public Set<Character> getCharacters()
 	{
 		return new HashSet<Character>()
 		{
 			{
 				for(Character character : getRawCharacters())
 				{
 					if(character != null && character.canUse()) add(character);
 				}
 			}
 		};
 	}
 
 	private List<Character> getRawCharacters()
 	{
 		return JOhm.find(Character.class, "player", player);
 	}
 
 	public boolean canUseCurrent()
 	{
 		if(getCurrent() != null && getCurrent().canUse()) return true;
 		if(!getOfflinePlayer().isOnline() || getCurrent() == null) return false;
 		getOfflinePlayer().getPlayer().sendMessage(ChatColor.RED + "Your current character was unable to load!");
 		getOfflinePlayer().getPlayer().sendMessage(ChatColor.RED + "Please contact the server administrator immediately.");
 		return false;
 	}
 
 	@Model
 	public static class Character implements Battle.Participant
 	{
 		@Id
 		private Long id;
 		@Attribute
 		@Indexed
 		private String name;
 		@Attribute
 		@Indexed
 		private String player;
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
 		@Reference
 		private Meta meta;
 		@Reference
 		private Inventory inventory;
 		@CollectionMap(key = String.class, value = DLocation.class)
 		private Map<String, DLocation> warps;
 		@CollectionMap(key = String.class, value = DLocation.class)
 		private Map<String, DLocation> invites;
 
 		void setName(String name)
 		{
 			this.name = name;
 		}
 
 		void setDeity(Deity deity)
 		{
 			this.deity = deity.getInfo().getName();
 		}
 
 		void setPlayer(OfflinePlayer player)
 		{
 			this.player = player.getName();
 		}
 
 		public void setImmortal(boolean option)
 		{
 			this.immortal = option;
 			Util.save(this);
 		}
 
 		public void setActive(boolean option)
 		{
 			this.active = option;
 			Util.save(this);
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
 
 		public void remove()
 		{
			for(Structure.Save structureSave : Structure.Util.getStructuresSavesWithFlag(Structure.Flag.DELETE_WITH_OWNER))
 			{
				if(structureSave.hasOwner() && structureSave.getOwner().getId().equals(getId())) structureSave.remove();
 			}
 			JOhm.delete(Inventory.class, getInventory().getId());
 			JOhm.delete(Meta.class, getMeta().getId());
 			JOhm.delete(Character.class, getId());
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
 			return this.location.toLocation();
 		}
 
 		public Location getCurrentLocation()
 		{
 			if(getOfflinePlayer().isOnline()) return getOfflinePlayer().getPlayer().getLocation();
 			return getLocation();
 		}
 
 		@Override
 		public Character getRelatedCharacter()
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
 
 		@Override
 		public void setPvP(boolean pvp)
 		{
 			DPlayer.Util.getPlayer(getOfflinePlayer()).setPvP(pvp);
 		}
 
 		@Override
 		public Boolean getPvP()
 		{
 			return DPlayer.Util.getPlayer(getOfflinePlayer()).getPvP();
 		}
 
 		public String getAlliance()
 		{
 			return getDeity().getInfo().getAlliance();
 		}
 
 		public Boolean isImmortal()
 		{
 			return this.immortal;
 		}
 
 		public void addWarp(String name, Location location)
 		{
 			if(this.warps == null) this.warps = Maps.newHashMap();
 			this.warps.put(name.toLowerCase(), DLocation.Util.create(location));
 			Util.save(this);
 		}
 
 		public void removeWarp(String name)
 		{
 			this.warps.remove(name.toLowerCase());
 			Util.save(this);
 		}
 
 		public Map<String, DLocation> getWarps()
 		{
 			return this.warps;
 		}
 
 		public boolean hasWarps()
 		{
 			return !this.warps.isEmpty();
 		}
 
 		public void addInvite(String name, Location location)
 		{
 			if(this.invites == null) this.invites = Maps.newHashMap();
 			this.invites.put(name.toLowerCase(), DLocation.Util.create(location));
 			Util.save(this);
 		}
 
 		public void removeInvite(String name)
 		{
 			this.invites.remove(name.toLowerCase());
 			Util.save(this);
 		}
 
 		public Map<String, DLocation> getInvites()
 		{
 			return this.invites;
 		}
 
 		public boolean hasInvites()
 		{
 			return !this.invites.isEmpty();
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
 			Util.save(this);
 		}
 
 		/**
 		 * Adds 1 kill.
 		 */
 		public void addKill()
 		{
 			this.kills += 1;
 			Util.save(this);
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
 			Util.save(this);
 		}
 
 		/**
 		 * Adds a death.
 		 */
 		public void addDeath()
 		{
 			this.deaths += 1;
 			Util.save(this);
 		}
 
 		public Long getId()
 		{
 			return id;
 		}
 
 		@Override
 		public Object clone() throws CloneNotSupportedException
 		{
 			throw new CloneNotSupportedException();
 		}
 
 		public boolean canUse()
 		{
 			for(Deity deity : Demigods.getLoadedDeities())
 			{
 				if(deity.getInfo().getName().equals(this.deity)) return true;
 			}
 			return false;
 		}
 
 		@Model
 		public static class Inventory
 		{
 			@Id
 			private Long id;
 			@Attribute
 			@Indexed
 			private long owner;
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
 
 			void setOwner(Long id)
 			{
 				this.owner = id;
 			}
 
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
 
 			public Character getOwner()
 			{
 				return Character.Util.load(this.owner);
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
 			private Map<String, Boolean> abilityData;
 			@CollectionMap(key = String.class, value = Boolean.class)
 			private Map<String, Boolean> taskData;
 			@CollectionMap(key = String.class, value = Boolean.class)
 			private Map<String, Ability.Devotion> devotionData;
 
 			void initialize()
 			{
 				this.binds = Sets.newHashSet();
 				this.abilityData = new HashMap<String, Boolean>();
 				this.taskData = new HashMap<String, Boolean>();
 				this.devotionData = new HashMap<String, Ability.Devotion>();
 			}
 
 			public long getId()
 			{
 				return this.id;
 			}
 
 			public void addDevotion(Ability.Devotion devotion)
 			{
 				if(!this.devotionData.containsKey(devotion.getType().toString())) this.devotionData.put(devotion.getType().toString(), devotion);
 				Util.save(this);
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
 				Util.save(this);
 			}
 
 			public void addAscensions(int amount)
 			{
 				this.ascensions += amount;
 				Util.save(this);
 			}
 
 			public void subtractAscensions(int amount)
 			{
 				this.ascensions -= amount;
 				Util.save(this);
 			}
 
 			public void setAscensions(int amount)
 			{
 				this.ascensions = amount;
 				Util.save(this);
 			}
 
 			public Integer getFavor()
 			{
 				return this.favor;
 			}
 
 			public void setFavor(int amount)
 			{
 				this.favor = amount;
 				Util.save(this);
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
 				Util.save(this);
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
 				Util.save(this);
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
 				Util.save(this);
 			}
 
 			public void setMaxFavor(int amount)
 			{
 				if(amount < 0) this.maxFavor = 0;
 				if(amount > Demigods.config.getSettingInt("caps.favor")) this.maxFavor = Demigods.config.getSettingInt("caps.favor");
 				else this.maxFavor = amount;
 				Util.save(this);
 			}
 
 			@Override
 			public Object clone() throws CloneNotSupportedException
 			{
 				throw new CloneNotSupportedException();
 			}
 		}
 
 		public static class Util
 		{
 			public static void create(Player player, String chosenDeity, String chosenName, boolean switchCharacter)
 			{
 				Character character = create(player, chosenName, chosenDeity);
 
 				if(player.isOnline())
 				{
 					Player online = player.getPlayer();
 					online.setDisplayName(Deity.Util.getDeity(chosenDeity).getInfo().getColor() + chosenName + ChatColor.WHITE);
 					online.setPlayerListName(Deity.Util.getDeity(chosenDeity).getInfo().getColor() + chosenName + ChatColor.WHITE);
 
 					online.sendMessage(ChatColor.GREEN + Demigods.text.getText(TextUtility.Text.CHARACTER_CREATE_COMPLETE).replace("{deity}", chosenDeity));
 					online.getWorld().strikeLightningEffect(online.getLocation());
 
 					for(int i = 0; i < 20; i++)
 						online.getWorld().spawn(online.getLocation(), ExperienceOrb.class);
 				}
 
 				// Switch to new character
 				if(switchCharacter) DPlayer.Util.getPlayer(player).switchCharacter(character);
 			}
 
 			public static Character create(OfflinePlayer player, String charName, String charDeity)
 			{
 				if(getCharacterByName(charName) == null)
 				{
 					// Create the Character
 					return create(player, charName, Deity.Util.getDeity(charDeity), true);
 				}
 				return null;
 			}
 
 			private static Character create(final OfflinePlayer player, final String charName, final Deity deity, final boolean immortal)
 			{
 				Character character = new Character();
 				character.setPlayer(player);
 				character.setName(charName);
 				character.setDeity(deity);
 				character.setImmortal(immortal);
 				character.setMaxHealth(40.0);
 				character.setHealth(40.0);
 				character.setHunger(20);
 				character.setExperience(0);
 				character.setLevel(0);
 				character.setKills(0);
 				character.setDeaths(0);
 				character.setLocation(player.getPlayer().getLocation());
 				character.setMeta(Util.createMeta());
 				save(character);
 				return character;
 			}
 
 			public static Inventory createInventory(Character character)
 			{
 				PlayerInventory inventory = character.getOfflinePlayer().getPlayer().getInventory();
 				Inventory charInventory = new Inventory();
 				charInventory.setOwner(character.getId());
 				if(inventory.getHelmet() != null) charInventory.setHelmet(inventory.getHelmet());
 				if(inventory.getChestplate() != null) charInventory.setChestplate(inventory.getChestplate());
 				if(inventory.getLeggings() != null) charInventory.setLeggings(inventory.getLeggings());
 				if(inventory.getBoots() != null) charInventory.setBoots(inventory.getBoots());
 				charInventory.setItems(inventory);
 				Util.save(charInventory);
 				return charInventory;
 			}
 
 			public static Inventory createEmptyInventory()
 			{
 				Inventory charInventory = new Inventory();
 				charInventory.setHelmet(new ItemStack(Material.AIR));
 				charInventory.setChestplate(new ItemStack(Material.AIR));
 				charInventory.setLeggings(new ItemStack(Material.AIR));
 				charInventory.setBoots(new ItemStack(Material.AIR));
 				Util.save(charInventory);
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
 				Util.save(charMeta);
 				return charMeta;
 			}
 
 			public static void save(Character character)
 			{
 				JOhm.save(character);
 			}
 
 			public static Character load(Long id)
 			{
 				return JOhm.get(Character.class, id);
 			}
 
 			public static Set<Character> loadAll()
 			{
 				return JOhm.getAll(Character.class);
 			}
 
 			public static Character getCharacterByName(String name)
 			{
 				for(Character loaded : loadAll())
 					if(loaded.getName().equalsIgnoreCase(name)) return loaded;
 				return null;
 			}
 
 			public static boolean charExists(String name)
 			{
 				return getCharacterByName(name) != null;
 			}
 
 			public static boolean isCooledDown(Character player, String ability, boolean sendMsg)
 			{
 				if(DataUtility.hasKeyTemp(player.getName(), ability + "_cooldown") && Long.parseLong(DataUtility.getValueTemp(player.getName(), ability + "_cooldown").toString()) > System.currentTimeMillis())
 				{
 					if(sendMsg) player.getOfflinePlayer().getPlayer().sendMessage(ChatColor.RED + ability + " has not cooled down!");
 					return false;
 				}
 				else return true;
 			}
 
 			public static void setCoolDown(Character player, String ability, long cooldown)
 			{
 				DataUtility.saveTemp(player.getName(), ability + "_cooldown", cooldown);
 			}
 
 			public static long getCoolDown(Character player, String ability)
 			{
 				return Long.parseLong(DataUtility.getValueTemp(player.getName(), ability + "_cooldown").toString());
 			}
 
 			public static Set<Character> getAllActive()
 			{
 				Set<Character> active = Sets.newHashSet();
 				for(Character character : loadAll())
 				{
 					if(character.isActive()) active.add(character);
 				}
 				return active;
 			}
 
 			public static OfflinePlayer getOwner(long charID)
 			{
 				return load(charID).getOfflinePlayer();
 			}
 
 			public static Set<Character> getDeityList(String deity)
 			{
 				// Define variables
 				Set<Character> deityList = Sets.newHashSet();
 				for(Character character : loadAll())
 				{
 					if(character.getDeity().getInfo().getName().equalsIgnoreCase(deity)) deityList.add(character);
 				}
 				return deityList;
 			}
 
 			public static Set<Character> getActiveDeityList(String deity)
 			{
 				// Define variables
 				Set<Character> deityList = Sets.newHashSet();
 				for(Character character : getAllActive())
 				{
 					if(character.getDeity().getInfo().getName().equalsIgnoreCase(deity)) deityList.add(character);
 				}
 				return deityList;
 			}
 
 			public static Set<Character> getAllianceList(String alliance)
 			{
 				// Define variables
 				Set<Character> allianceList = Sets.newHashSet();
 				for(Character character : loadAll())
 				{
 					if(character.getAlliance().equalsIgnoreCase(alliance)) allianceList.add(character);
 				}
 				return allianceList;
 			}
 
 			public static Set<Character> getActiveAllianceList(String alliance)
 			{
 				// Define variables
 				Set<Character> allianceList = Sets.newHashSet();
 				for(Character character : getAllActive())
 				{
 					if(character.getAlliance().equalsIgnoreCase(alliance)) allianceList.add(character);
 				}
 				return allianceList;
 			}
 
 			public static Set<Character> getImmortalList()
 			{
 				// Define variables
 				Set<Character> immortalList = Sets.newHashSet();
 				for(Character character : loadAll())
 				{
 					if(character.isImmortal()) immortalList.add(character);
 				}
 				return immortalList;
 			}
 
 			public static void onCharacterKillCharacter(Character attacker, Character killed)
 			{
 				String attackerAlliance = "Mortal";
 				if(attacker != null) attackerAlliance = attacker.getAlliance();
 				String killedAlliance = "Mortal";
 				if(killed != null) killedAlliance = killed.getAlliance();
 
 				attacker.addKill();
 
 				if(killed == null && attacker == null) Demigods.message.broadcast(Demigods.text.getText(TextUtility.Text.MORTAL_SLAIN_1));
 				else if(killed == null && attacker != null) Demigods.message.broadcast(Demigods.text.getText(TextUtility.Text.MORTAL_SLAIN_2).replace("{attacker}", ChatColor.YELLOW + attacker.getName() + ChatColor.GRAY).replace("{attackerAlliance}", attackerAlliance));
 				else if(killed != null && attacker == null) Demigods.message.broadcast(ChatColor.GRAY + Demigods.text.getText(TextUtility.Text.DEMI_SLAIN_1).replace("{killed}", ChatColor.YELLOW + killed.getName() + ChatColor.GRAY).replace("{killedAlliance}", killedAlliance));
 				else if(killed != null && attacker != null) Demigods.message.broadcast(ChatColor.GRAY + Demigods.text.getText(TextUtility.Text.DEMI_SLAIN_2).replace("{killed}", ChatColor.YELLOW + killed.getName() + ChatColor.GRAY).replace("{killedAlliance}", killedAlliance).replace("{attacker}", ChatColor.YELLOW + attacker.getName() + ChatColor.GRAY).replace("{attackerAlliance}", attackerAlliance));
 			}
 
 			public static void onCharacterBetrayCharacter(Character attacker, Character killed)
 			{
 				String alliance = attacker.getAlliance();
 
 				// TODO: Punishments.
 
 				if(!alliance.equals("Mortal")) Demigods.message.broadcast(ChatColor.GRAY + Demigods.text.getText(TextUtility.Text.DEMI_BETRAY).replace("{killed}", ChatColor.YELLOW + killed.getName() + ChatColor.GRAY).replace("{attacker}", ChatColor.YELLOW + attacker.getName() + ChatColor.GRAY).replace("{alliance}", alliance));
 				else Demigods.message.broadcast(ChatColor.GRAY + Demigods.text.getText(TextUtility.Text.MORTAL_BETRAY));
 			}
 
 			/**
 			 * Returns true if <code>char1</code> is allied with <code>char2</code> based
 			 * on their current alliances.
 			 * 
 			 * @param char1 the first character to check.
 			 * @param char2 the second character to check.
 			 * @return boolean
 			 */
 			public static boolean areAllied(Character char1, Character char2)
 			{
 				return char1.getAlliance().equalsIgnoreCase(char2.getAlliance());
 			}
 
 			public static void save(Inventory inventory)
 			{
 				try
 				{
 					JOhm.save(inventory);
 				}
 				catch(Exception e)
 				{
 					Demigods.message.severe("Could not save inventory: " + inventory.getId());
 				}
 			}
 
 			public static Inventory loadInventory(long id)
 			{
 				return JOhm.get(Inventory.class, id);
 			}
 
 			public static Set<Inventory> loadAllInentories()
 			{
 				return JOhm.getAll(Inventory.class);
 			}
 
 			public static Meta loadMeta(long id)
 			{
 				return JOhm.get(Meta.class, id);
 			}
 
 			public static Set<Meta> loadAllMeta()
 			{
 				return JOhm.getAll(Meta.class);
 			}
 
 			public static void save(Meta meta)
 			{
 				JOhm.save(meta);
 			}
 		}
 	}
 
 	public static class Util
 	{
 		public static DPlayer create(OfflinePlayer player)
 		{
 			DPlayer trackedPlayer = new DPlayer();
 			trackedPlayer.setPlayer(player.getName());
 			trackedPlayer.setLastLoginTime(player.getLastPlayed());
 			trackedPlayer.setPvP(false);
 			save(trackedPlayer);
 			return trackedPlayer;
 		}
 
 		public static void save(DPlayer trackedPlayer)
 		{
 			JOhm.save(trackedPlayer);
 		}
 
 		public static DPlayer load(Long id)
 		{
 			return JOhm.get(DPlayer.class, id);
 		}
 
 		public static Set<DPlayer> loadAll()
 		{
 			try
 			{
 				return JOhm.getAll(DPlayer.class);
 			}
 			catch(Exception e)
 			{
 				return Sets.newHashSet();
 			}
 		}
 
 		public static DPlayer getPlayer(OfflinePlayer player)
 		{
 			try
 			{
 				List<DPlayer> tracking = JOhm.find(DPlayer.class, "player", player.getName());
 				return tracking.get(0);
 			}
 			catch(Exception ignored)
 			{}
 			return create(player);
 		}
 
 		/**
 		 * Returns the current alliance for <code>player</code>.
 		 * 
 		 * @param player the player to check.
 		 * @return String
 		 */
 		public static String getCurrentAlliance(OfflinePlayer player)
 		{
 			Character character = getPlayer(player).getCurrent();
 			if(character == null || !character.isImmortal()) return "Mortal";
 			return character.getAlliance();
 		}
 
 		/**
 		 * Returns a List of all of <code>player</code>'s characters.
 		 * 
 		 * @param player the player to check.
 		 * @return List the list of all character IDs.
 		 */
 		public static Set<Character> getCharacters(OfflinePlayer player)
 		{
 			return getPlayer(player).getCharacters();
 		}
 
 		/**
 		 * Returns true if the <code>player</code> is currently immortal.
 		 * 
 		 * @param player the player to check.
 		 * @return boolean
 		 */
 		public static boolean isImmortal(OfflinePlayer player)
 		{
 			Character character = getPlayer(player).getCurrent();
 			return character != null && character.isImmortal();
 		}
 
 		/**
 		 * Returns true if <code>player</code> has a character with the name <code>charName</code>.
 		 * 
 		 * @param player the player to check.
 		 * @param charName the charName to check with.
 		 * @return boolean
 		 */
 		public static boolean hasCharName(OfflinePlayer player, String charName)
 		{
 			final Set<Character> characters = getCharacters(player);
 
 			for(Character character : characters)
 			{
 				if(character == null) continue;
 				if(character.getName().equalsIgnoreCase(charName)) return true;
 			}
 			return false;
 		}
 
 		/**
 		 * Returns true if the <code>player</code> is currently praying.
 		 * 
 		 * @param player the player to check.
 		 * @return boolean
 		 */
 		public static boolean isPraying(Player player)
 		{
 			try
 			{
 				return DataUtility.hasKeyTemp(player.getName(), "prayer_conversation");
 			}
 			catch(Exception ignored)
 			{}
 			return false;
 		}
 
 		/**
 		 * Removes all temp data related to prayer for the <code>player</code>.
 		 * 
 		 * @param player the player to clean.
 		 */
 		public static void clearPrayerSession(Player player)
 		{
 			DataUtility.removeTemp(player.getName(), "prayer_conversation");
 			DataUtility.removeTemp(player.getName(), "prayer_context");
 			DataUtility.removeTemp(player.getName(), "prayer_location");
 		}
 
 		/**
 		 * Returns the context for the <code>player</code>'s prayer converstion.
 		 * 
 		 * @param player the player whose context to return.
 		 * @return ConversationContext
 		 */
 		public static ConversationContext getPrayerContext(Player player)
 		{
 			if(!isPraying(player)) return null;
 			return (ConversationContext) DataUtility.getValueTemp(player.getName(), "prayer_context");
 		}
 
 		/**
 		 * Changes prayer status for <code>player</code> to <code>option</code> and tells them.
 		 * 
 		 * @param player the player the manipulate.
 		 * @param option the boolean to set to.
 		 */
 		public static void togglePraying(Player player, boolean option)
 		{
 			if(option)
 			{
 				// Toggle on
 				togglePrayingSilent(player, true);
 			}
 			else
 			{
 				// Toggle off
 				togglePrayingSilent(player, false);
 
 				// Message them
 				clearChat(player);
 				player.sendMessage(ChatColor.AQUA + "You are no longer praying.");
 				player.sendMessage(ChatColor.GRAY + "Your chat has been re-enabled.");
 			}
 		}
 
 		/**
 		 * Changes prayer status for <code>player</code> to <code>option</code> silently.
 		 * 
 		 * @param player the player the manipulate.
 		 * @param option the boolean to set to.
 		 */
 		public static void togglePrayingSilent(Player player, boolean option)
 		{
 			if(option)
 			{
 				// Create the conversation and save it
 				Conversation prayer = Prayer.startPrayer(player);
 				DataUtility.saveTemp(player.getName(), "prayer_conversation", prayer);
 				DataUtility.saveTemp(player.getName(), "prayer_location", player.getLocation());
 			}
 			else
 			{
 				// Save context and abandon the conversation
 				if(DataUtility.hasKeyTemp(player.getName(), "prayer_conversation"))
 				{
 					Conversation prayer = (Conversation) DataUtility.getValueTemp(player.getName(), "prayer_conversation");
 					DataUtility.saveTemp(player.getName(), "prayer_context", prayer.getContext());
 					prayer.abandon();
 				}
 
 				// Remove the data
 				DataUtility.removeTemp(player.getName(), "prayer_conversation");
 				DataUtility.removeTemp(player.getName(), "prayer_location");
 			}
 		}
 
 		/**
 		 * Clears the chat for <code>player</code> using .sendMessage().
 		 * 
 		 * @param player the player whose chat to clear.
 		 */
 		public static void clearChat(Player player)
 		{
 			for(int x = 0; x < 120; x++)
 				player.sendMessage(" ");
 		}
 
 		/**
 		 * Clears the chat for <code>player</code> using .sendRawMessage().
 		 * 
 		 * @param player the player whose chat to clear.
 		 */
 		public static void clearRawChat(Player player)
 		{
 			for(int x = 0; x < 120; x++)
 				player.sendRawMessage(" ");
 		}
 	}
 
 	public static class FavorRunnable extends BukkitRunnable
 	{
 		private double multiplier;
 
 		public FavorRunnable(double multiplier)
 		{
 			this.multiplier = multiplier;
 		}
 
 		@Override
 		public void run()
 		{
 			for(Player player : Bukkit.getOnlinePlayers())
 			{
 				Character character = DPlayer.Util.getPlayer(player).getCurrent();
 				if(character == null || !character.isImmortal()) continue;
 				int regenRate = (int) Math.ceil(multiplier * character.getMeta().getAscensions());
 				if(regenRate < 5) regenRate = 5;
 				character.getMeta().addFavor(regenRate);
 			}
 		}
 	}
 }
