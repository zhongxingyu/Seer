 package com.censoredsoftware.Demigods.Engine.Object.Player;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.entity.ExperienceOrb;
 import org.bukkit.entity.Player;
 
 import redis.clients.johm.*;
 
 import com.censoredsoftware.Demigods.Engine.Demigods;
 import com.censoredsoftware.Demigods.Engine.Object.Deity.Deity;
 import com.censoredsoftware.Demigods.Engine.Object.General.DemigodsLocation;
 import com.censoredsoftware.Demigods.Engine.Object.Structure.StructureInfo;
 import com.censoredsoftware.Demigods.Engine.Object.Structure.StructureSave;
 import com.censoredsoftware.Demigods.Engine.Utility.DataUtility;
 import com.censoredsoftware.Demigods.Engine.Utility.TextUtility;
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
 	private DemigodsLocation location;
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
 	@CollectionMap(key = DemigodsLocation.class, value = String.class)
 	private Map<DemigodsLocation, String> warps;
 	@CollectionMap(key = DemigodsLocation.class, value = String.class)
 	private Map<DemigodsLocation, String> invites;
 
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
 		this.inventory = PlayerCharacterInventory.create(this);
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
 		this.location = DemigodsLocation.create(location);
 	}
 
 	public void setMeta(PlayerCharacterMeta meta)
 	{
 		this.meta = meta;
 	}
 
 	public static void create(Player player, String chosenDeity, String chosenName, boolean switchCharacter)
 	{
 		PlayerCharacter character = create(player, chosenName, chosenDeity);
 
 		if(player.isOnline())
 		{
 			Player online = player.getPlayer();
 			online.setDisplayName(Deity.getDeity(chosenDeity).getInfo().getColor() + chosenName + ChatColor.WHITE);
 			online.setPlayerListName(Deity.getDeity(chosenDeity).getInfo().getColor() + chosenName + ChatColor.WHITE);
 
 			online.sendMessage(ChatColor.GREEN + Demigods.text.getText(TextUtility.Text.CHARACTER_CREATE_COMPLETE).replace("{deity}", chosenDeity));
 			online.getWorld().strikeLightningEffect(online.getLocation());
 
 			for(int i = 0; i < 20; i++)
 				online.getWorld().spawn(online.getLocation(), ExperienceOrb.class);
 		}
 
 		// Switch to new character
 		if(switchCharacter) PlayerWrapper.getPlayer(player).switchCharacter(character);
 	}
 
 	public static PlayerCharacter create(OfflinePlayer player, String charName, String charDeity)
 	{
 		if(PlayerCharacter.getCharacterByName(charName) == null)
 		{
 			// Create the Character
 			return create(player, charName, Deity.getDeity(charDeity), true);
 		}
 		return null;
 	}
 
 	private static PlayerCharacter create(final OfflinePlayer player, final String charName, final Deity deity, final boolean immortal)
 	{
 		PlayerCharacter character = new PlayerCharacter();
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
 		character.setMeta(PlayerCharacterMeta.create());
 		PlayerCharacter.save(character);
 		return character;
 	}
 
 	public static void save(PlayerCharacter character)
 	{
 		JOhm.save(character);
 	}
 
 	public void remove()
 	{
 		for(StructureSave structureSave : StructureSave.loadAll())
 		{
 			if(structureSave.getStructureInfo().getFlags().contains(StructureInfo.Flag.DELETE_ON_OWNER_DELETE) && structureSave.getOwner() != null && structureSave.getOwner().getId().equals(getId())) structureSave.remove();
 		}
 		JOhm.delete(PlayerCharacter.class, getId());
 	}
 
 	public static PlayerCharacter load(Long id)
 	{
 		return JOhm.get(PlayerCharacter.class, id);
 	}
 
 	public static Set<PlayerCharacter> loadAll()
 	{
 		return JOhm.getAll(PlayerCharacter.class);
 	}
 
 	public static PlayerCharacter getCharacterByName(String name)
 	{
 		for(PlayerCharacter loaded : loadAll())
 		{
 			if(loaded.getName().equalsIgnoreCase(name)) return loaded;
 		}
 		return null;
 	}
 
 	public static boolean charExists(String name)
 	{
 		return getCharacterByName(name) != null;
 	}
 
 	public PlayerCharacterInventory getInventory()
 	{
 		if(this.inventory == null) this.inventory = PlayerCharacterInventory.createEmpty();
 		return this.inventory;
 	}
 
 	public PlayerCharacterMeta getMeta()
 	{
 		if(this.meta == null)
 		{
 			this.meta = PlayerCharacterMeta.create();
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
 
 	public void addWarp(DemigodsLocation location, String name)
 	{
 		this.warps.put(location, name.toUpperCase());
 		save(this);
 	}
 
 	public void removeWarp(DemigodsLocation location)
 	{
 		this.warps.remove(location);
 		save(this);
 	}
 
 	public Map<DemigodsLocation, String> getWarps()
 	{
 		return this.warps;
 	}
 
 	public void addInvite(DemigodsLocation location, String name)
 	{
 		this.invites.put(location, name);
 		save(this);
 	}
 
 	public void removeInvite(DemigodsLocation location)
 	{
 		this.invites.remove(location);
 		save(this);
 	}
 
 	public void clearInvites()
 	{
 		this.invites = new HashMap<DemigodsLocation, String>();
 		save(this);
 	}
 
 	public Map<DemigodsLocation, String> getInvites()
 	{
 		return this.invites;
 	}
 
	public boolean hasWarps()
	{
		return this.warps != null && !this.warps.isEmpty();
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
 		if(DataUtility.hasKeyTemp(player.getName(), ability + "_cooldown") && Long.parseLong(DataUtility.getValueTemp(player.getName(), ability + "_cooldown").toString()) > System.currentTimeMillis())
 		{
 			if(sendMsg) player.getOfflinePlayer().getPlayer().sendMessage(ChatColor.RED + ability + " has not cooled down!");
 			return false;
 		}
 		else return true;
 	}
 
 	public static void setCoolDown(PlayerCharacter player, String ability, long cooldown)
 	{
 		DataUtility.saveTemp(player.getName(), ability + "_cooldown", cooldown);
 	}
 
 	public static long getCoolDown(PlayerCharacter player, String ability)
 	{
 		return Long.parseLong(DataUtility.getValueTemp(player.getName(), ability + "_cooldown").toString());
 	}
 
 	public static Set<PlayerCharacter> getAllActive()
 	{
 		Set<PlayerCharacter> active = Sets.newHashSet();
 		for(PlayerCharacter character : loadAll())
 		{
 			if(character.isActive()) active.add(character);
 		}
 		return active;
 	}
 
 	public static OfflinePlayer getOwner(long charID)
 	{
 		return load(charID).getOfflinePlayer();
 	}
 
 	public static Set<PlayerCharacter> getDeityList(String deity)
 	{
 		// Define variables
 		Set<PlayerCharacter> deityList = Sets.newHashSet();
 		for(PlayerCharacter character : loadAll())
 		{
 			if(character.getDeity().getInfo().getName().equalsIgnoreCase(deity)) deityList.add(character);
 		}
 		return deityList;
 	}
 
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
 
 	public static Set<PlayerCharacter> getAllianceList(String alliance)
 	{
 		// Define variables
 		Set<PlayerCharacter> allianceList = Sets.newHashSet();
 		for(PlayerCharacter character : loadAll())
 		{
 			if(character.getAlliance().equalsIgnoreCase(alliance)) allianceList.add(character);
 		}
 		return allianceList;
 	}
 
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
 
 	public static Set<PlayerCharacter> getImmortalList()
 	{
 		// Define variables
 		Set<PlayerCharacter> immortalList = Sets.newHashSet();
 		for(PlayerCharacter character : loadAll())
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
 
 	public boolean canUse()
 	{
 		for(Deity deity : Demigods.getLoadedDeities())
 		{
 			if(deity.getInfo().getName().equals(this.deity)) return true;
 		}
 		return false;
 	}
 }
