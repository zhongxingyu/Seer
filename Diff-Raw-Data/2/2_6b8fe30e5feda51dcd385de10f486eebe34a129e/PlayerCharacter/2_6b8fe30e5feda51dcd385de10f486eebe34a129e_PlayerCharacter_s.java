 package com.censoredsoftware.Demigods.Engine.PlayerCharacter;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.OfflinePlayer;
 
 import redis.clients.johm.*;
 
 import com.censoredsoftware.Demigods.Engine.Deity.Deity;
 import com.censoredsoftware.Demigods.Engine.DemigodsData;
 import com.censoredsoftware.Demigods.Engine.Tracked.TrackedLocation;
 import com.censoredsoftware.Demigods.Engine.Tracked.TrackedModelFactory;
 import com.google.common.collect.Sets;
 
 @Model
 public class PlayerCharacter
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
 	private Integer health;
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
 	private TrackedLocation location;
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
 	private PlayerCharacterMeta meta;
 	@Reference
 	private PlayerCharacterInventory inventory;
 	@CollectionMap(key = TrackedLocation.class, value = String.class)
 	private Map<TrackedLocation, String> warps;
 	@CollectionMap(key = TrackedLocation.class, value = String.class)
 	private Map<TrackedLocation, String> invites;
 
 	public static void save(PlayerCharacter character)
 	{
 		DemigodsData.jOhm.save(character);
 	}
 
 	public void delete()
 	{
 		DemigodsData.jOhm.delete(PlayerCharacter.class, getId());
 	}
 
 	public static PlayerCharacter load(Long id) // TODO This belongs somewhere else.
 	{
 		return DemigodsData.jOhm.get(PlayerCharacter.class, id);
 	}
 
 	public static Set<PlayerCharacter> loadAll()
 	{
 		return DemigodsData.jOhm.getAll(PlayerCharacter.class);
 	}
 
 	public static PlayerCharacter getCharacterByName(String name)
 	{
 		for(PlayerCharacter loaded : loadAll())
 		{
 			if(loaded.getName().equalsIgnoreCase(name)) return loaded;
 		}
 		return null;
 	}
 
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
 		save(this);
 	}
 
 	public void setActive(boolean option)
 	{
 		this.active = option;
 		save(this);
 	}
 
 	public void saveInventory()
 	{
 		this.inventory = PlayerCharacterFactory.createPlayerCharacterInventory(this);
 	}
 
 	public void setHealth(int health)
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
 		this.location = TrackedModelFactory.createTrackedLocation(location);
 	}
 
 	public void setMeta(PlayerCharacterMeta meta)
 	{
 		this.meta = meta;
 	}
 
 	public PlayerCharacterInventory getInventory()
 	{
 		if(this.inventory == null) this.inventory = PlayerCharacterFactory.createEmptyCharacterInventory();
 		return this.inventory;
 	}
 
 	public PlayerCharacterMeta getMeta()
 	{
 		if(this.meta == null)
 		{
 			this.meta = PlayerCharacterFactory.createCharacterMeta();
 		}
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
 
 	public Integer getHealth()
 	{
 		return this.health;
 	}
 
 	public Integer getLevel()
 	{
 		return this.level;
 	}
 
 	public ChatColor getHealthColor()
 	{
 		int hp = getHealth();
 		int maxHP = Bukkit.getPlayer(getOfflinePlayer().getName()).getMaxHealth();
 		ChatColor color = ChatColor.RESET;
 
 		// Set favor color dynamically
 		if(hp < Math.ceil(0.33 * maxHP)) color = ChatColor.RED;
 		else if(hp < Math.ceil(0.66 * maxHP) && hp > Math.ceil(0.33 * maxHP)) color = ChatColor.YELLOW;
 		if(hp > Math.ceil(0.66 * maxHP)) color = ChatColor.GREEN;
 
 		return color;
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
 		return Deity.getDeity(this.deity);
 	}
 
 	public String getAlliance()
 	{
 		return getDeity().getInfo().getAlliance();
 	}
 
 	public Boolean isImmortal()
 	{
 		return this.immortal;
 	}
 
 	public void addWarp(TrackedLocation location, String name)
 	{
		this.warps.put(location, name);
 		save(this);
 	}
 
 	public void removeWarp(TrackedLocation location)
 	{
 		this.warps.remove(location);
 		save(this);
 	}
 
 	public Map<TrackedLocation, String> getWarps()
 	{
 		return this.warps;
 	}
 
 	public void addInvite(TrackedLocation location, String name)
 	{
 		this.invites.put(location, name);
 		save(this);
 	}
 
 	public void removeInvite(TrackedLocation location)
 	{
 		this.invites.remove(location);
 		save(this);
 	}
 
 	public void clearInvites()
 	{
 		this.invites = new HashMap<TrackedLocation, String>();
 		save(this);
 	}
 
 	public Map<TrackedLocation, String> getInvites()
 	{
 		return this.invites;
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
 		save(this);
 	}
 
 	/**
 	 * Adds 1 kill.
 	 */
 	public void addKill()
 	{
 		this.kills += 1;
 		save(this);
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
 		save(this);
 	}
 
 	/**
 	 * Adds a death.
 	 */
 	public void addDeath()
 	{
 		this.deaths += 1;
 		save(this);
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
 
 	public static boolean isCooledDown(PlayerCharacter player, String ability, boolean sendMsg)
 	{
 		if(DemigodsData.hasKeyTemp(player.getName(), ability + "_cooldown") && Long.parseLong(DemigodsData.getValueTemp(player.getName(), ability + "_cooldown").toString()) > System.currentTimeMillis())
 		{
 			if(sendMsg) player.getOfflinePlayer().getPlayer().sendMessage(ChatColor.RED + ability + " has not cooled down!");
 			return false;
 		}
 		else return true;
 	}
 
 	public static void setCoolDown(PlayerCharacter player, String ability, long cooldown)
 	{
 		DemigodsData.saveTemp(player.getName(), ability + "_cooldown", cooldown);
 	}
 
 	public static long getCoolDown(PlayerCharacter player, String ability)
 	{
 		return Long.parseLong(DemigodsData.getValueTemp(player.getName(), ability + "_cooldown").toString());
 	}
 
 	public static Set<PlayerCharacter> getAllChars()
 	{
 		return PlayerCharacter.loadAll();
 	}
 
 	public static PlayerCharacter getChar(Long id)
 	{
 		return PlayerCharacter.load(id);
 	}
 
 	public static PlayerCharacter getCharByName(String charName)
 	{
 		for(PlayerCharacter character : getAllChars())
 		{
 			if(character.getName().equalsIgnoreCase(charName)) return character;
 		}
 		return null;
 	}
 
 	public static Set<PlayerCharacter> getAllActive()
 	{
 		Set<PlayerCharacter> active = Sets.newHashSet();
 		for(PlayerCharacter character : getAllChars())
 		{
 			if(character.isActive()) active.add(character);
 		}
 		return active;
 	}
 
 	public static OfflinePlayer getOwner(long charID)
 	{
 		return getChar(charID).getOfflinePlayer();
 	}
 
 	/*
 	 * getDeityList() : Gets list of characters in aligned to a Deity.
 	 */
 	public static Set<PlayerCharacter> getDeityList(String deity)
 	{
 		// Define variables
 		Set<PlayerCharacter> deityList = Sets.newHashSet();
 		for(PlayerCharacter character : getAllChars())
 		{
 			if(character.getDeity().getInfo().getName().equalsIgnoreCase(deity)) deityList.add(character);
 		}
 		return deityList;
 	}
 
 	/*
 	 * getActiveDeityList() : Gets list of active characters in aligned to a Deity.
 	 */
 	public static Set<PlayerCharacter> getActiveDeityList(String deity)
 	{
 		// Define variables
 		Set<PlayerCharacter> deityList = Sets.newHashSet();
 		for(PlayerCharacter character : getAllActive())
 		{
 			if(character.getDeity().getInfo().getName().equalsIgnoreCase(deity)) deityList.add(character);
 		}
 		return deityList;
 	}
 
 	/*
 	 * getAllianceList() : Gets list of characters in an alliance.
 	 */
 	public static Set<PlayerCharacter> getAllianceList(String alliance)
 	{
 		// Define variables
 		Set<PlayerCharacter> allianceList = Sets.newHashSet();
 		for(PlayerCharacter character : getAllChars())
 		{
 			if(character.getAlliance().equalsIgnoreCase(alliance)) allianceList.add(character);
 		}
 		return allianceList;
 	}
 
 	/*
 	 * getActiveAllianceList() : Gets list of active characters in an alliance.
 	 */
 	public static Set<PlayerCharacter> getActiveAllianceList(String alliance)
 	{
 		// Define variables
 		Set<PlayerCharacter> allianceList = Sets.newHashSet();
 		for(PlayerCharacter character : getAllActive())
 		{
 			if(character.getAlliance().equalsIgnoreCase(alliance)) allianceList.add(character);
 		}
 		return allianceList;
 	}
 
 	/*
 	 * getImmortalList() : Gets list of currently immortal players.
 	 */
 	public static Set<PlayerCharacter> getImmortalList()
 	{
 		// Define variables
 		Set<PlayerCharacter> immortalList = Sets.newHashSet();
 		for(PlayerCharacter character : getAllChars())
 		{
 			if(character.isImmortal()) immortalList.add(character);
 		}
 		return immortalList;
 	}
 
 	/**
 	 * Returns true if <code>char1</code> is allied with <code>char2</code> based
 	 * on their current alliances.
 	 * 
 	 * @param char1 the first character to check.
 	 * @param char2 the second character to check.
 	 * @return boolean
 	 */
 	public static boolean areAllied(PlayerCharacter char1, PlayerCharacter char2)
 	{
 		return char1.getAlliance().equalsIgnoreCase(char2.getAlliance());
 	}
 }
