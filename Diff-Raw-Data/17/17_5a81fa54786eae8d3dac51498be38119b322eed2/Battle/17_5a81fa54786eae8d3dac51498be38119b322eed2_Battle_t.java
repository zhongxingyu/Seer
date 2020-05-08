 package com.censoredsoftware.demigods.battle;
 
 import com.censoredsoftware.demigods.Demigods;
 import com.censoredsoftware.demigods.data.DataManager;
 import com.censoredsoftware.demigods.exception.SpigotNotFoundException;
 import com.censoredsoftware.demigods.helper.ConfigFile;
 import com.censoredsoftware.demigods.location.DLocation;
 import com.censoredsoftware.demigods.player.DCharacter;
 import com.censoredsoftware.demigods.player.DPlayer;
 import com.censoredsoftware.demigods.player.Pet;
 import com.censoredsoftware.demigods.structure.Structure;
 import com.censoredsoftware.demigods.util.*;
 import com.google.common.base.Function;
 import com.google.common.base.Functions;
 import com.google.common.base.Predicate;
 import com.google.common.collect.*;
 import org.bukkit.*;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.serialization.ConfigurationSerializable;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Tameable;
 import org.bukkit.util.Vector;
 
 import java.util.*;
 import java.util.concurrent.ConcurrentHashMap;
 
 public class Battle implements ConfigurationSerializable
 {
 	private UUID id;
 	private UUID startLoc;
 	private boolean active;
 	private double range;
 	private long duration;
 	private int minKills;
 	private int maxKills;
 	private long startTime;
 	private long deleteTime;
 	private Set<String> involvedPlayers;
 	private Set<String> involvedTameable;
 	private int killCounter;
 	private Map<String, Object> kills;
 	private Map<String, Object> deaths;
 	private UUID startedBy;
 
 	public Battle()
 	{}
 
 	public Battle(UUID id, ConfigurationSection conf)
 	{
 		this.id = id;
 		startLoc = UUID.fromString(conf.getString("startLoc"));
 		active = conf.getBoolean("active");
 		range = conf.getDouble("range");
 		duration = conf.getLong("duration");
 		minKills = conf.getInt("minKills");
 		maxKills = conf.getInt("maxKills");
 		startTime = conf.getLong("startTime");
 		deleteTime = conf.getLong("deleteTime");
 		involvedPlayers = Sets.newHashSet(conf.getStringList("involvedPlayers"));
 		involvedTameable = Sets.newHashSet(conf.getStringList("involvedTameable"));
 		killCounter = conf.getInt("killCounter");
 		kills = conf.getConfigurationSection("kills").getValues(false);
 		deaths = conf.getConfigurationSection("deaths").getValues(false);
 		startedBy = UUID.fromString(conf.getString("startedBy"));
 	}
 
 	@Override
 	public Map<String, Object> serialize()
 	{
 		Map<String, Object> map = new HashMap<String, Object>();
 		map.put("startLoc", startLoc.toString());
 		map.put("active", active);
 		map.put("range", range);
 		map.put("duration", duration);
 		map.put("minKills", minKills);
 		map.put("maxKills", maxKills);
 		map.put("startTime", startTime);
 		map.put("deleteTime", deleteTime);
 		map.put("involvedPlayers", Lists.newArrayList(involvedPlayers));
 		map.put("involvedTameable", Lists.newArrayList(involvedTameable));
 		map.put("killCounter", killCounter);
 		map.put("kills", kills);
 		map.put("deaths", deaths);
 		map.put("startedBy", startedBy.toString());
 		return map;
 	}
 
 	public void generateId()
 	{
 		id = UUID.randomUUID();
 	}
 
 	public void setRange(double range)
 	{
 		this.range = range;
 		Util.save(this);
 	}
 
 	public void setActive()
 	{
 		this.active = true;
 		Util.save(this);
 	}
 
 	public void setInactive()
 	{
 		this.active = false;
 		Util.save(this);
 	}
 
 	public void setDuration(long duration)
 	{
 		this.duration = duration;
 		Util.save(this);
 	}
 
 	public void setMinKills(int kills)
 	{
 		this.minKills = kills;
 		Util.save(this);
 	}
 
 	public void setMaxKills(int kills)
 	{
 		this.maxKills = kills;
 		Util.save(this);
 	}
 
 	void setStartLocation(Location location)
 	{
 		this.startLoc = DLocation.Util.create(location).getId();
 	}
 
 	void setStartTime(long time)
 	{
 		this.startTime = time;
 	}
 
 	void setDeleteTime(long time)
 	{
 		this.deleteTime = time;
 		Util.save(this);
 	}
 
 	public UUID getId()
 	{
 		return this.id;
 	}
 
 	public double getRange()
 	{
 		return this.range;
 	}
 
 	public boolean isActive()
 	{
 		return this.active;
 	}
 
 	public long getDuration()
 	{
 		return this.duration;
 	}
 
 	public int getMinKills()
 	{
 		return this.minKills;
 	}
 
 	public int getMaxKills()
 	{
 		return this.maxKills;
 	}
 
 	public Location getStartLocation()
 	{
 		return DLocation.Util.load(this.startLoc).toLocation();
 	}
 
 	public long getStartTime()
 	{
 		return this.startTime;
 	}
 
 	public long getDeleteTime()
 	{
 		return this.deleteTime;
 	}
 
 	void setStarter(DCharacter character)
 	{
 		this.startedBy = character.getId();
 		addParticipant(character);
 	}
 
 	void initialize()
 	{
 		this.kills = Maps.newHashMap();
 		this.deaths = Maps.newHashMap();
 		this.involvedPlayers = Sets.newHashSet();
 		this.involvedTameable = Sets.newHashSet();
 		this.killCounter = 0;
 	}
 
 	public void addParticipant(Participant participant)
 	{
 		if(participant instanceof DCharacter) this.involvedPlayers.add((participant.getId().toString()));
 		else this.involvedTameable.add(participant.getId().toString());
 		Util.save(this);
 	}
 
 	public void removeParticipant(Participant participant)
 	{
 		if(participant instanceof DCharacter) this.involvedPlayers.remove((participant.getId().toString()));
 		else this.involvedTameable.remove(participant.getId().toString());
 		Util.save(this);
 	}
 
 	public void addKill(Participant participant)
 	{
 		this.killCounter += 1;
 		DCharacter character = participant.getRelatedCharacter();
 		if(this.kills.containsKey(character.getId().toString())) this.kills.put(character.getId().toString(), Integer.parseInt(this.kills.get(character.getId().toString()).toString()) + 1);
 		else this.kills.put(character.getId().toString(), 1);
 		Util.save(this);
 	}
 
 	public void addDeath(Participant participant)
 	{
 		DCharacter character = participant.getRelatedCharacter();
 		if(this.deaths.containsKey(character)) this.deaths.put(character.getId().toString(), Integer.parseInt(this.deaths.get(character.getId().toString()).toString()) + 1);
 		else this.deaths.put(character.getId().toString(), 1);
 		Util.save(this);
 		Util.sendBattleStats(this);
 	}
 
 	public DCharacter getStarter()
 	{
 		return DCharacter.Util.load(this.startedBy);
 	}
 
 	public Set<Participant> getParticipants()
 	{
 		return Sets.union(Sets.newHashSet(Collections2.transform(involvedPlayers, new Function<String, Participant>()
 		{
 			@Override
 			public Participant apply(String character)
 			{
 				return DCharacter.Util.load(UUID.fromString(character));
 			}
 		})), Sets.newHashSet(Collections2.transform(involvedTameable, new Function<String, Participant>()
 		{
 			@Override
 			public Participant apply(String tamable)
 			{
 				return Pet.Util.load(UUID.fromString(tamable));
 			}
 		})));
 	}
 
 	public int getKillCounter()
 	{
 		return this.killCounter;
 	}
 
 	public void end()
 	{
 		sendMessage(ChatColor.RED + "The battle now is over!"); // TODO Add more info.
 		String winner = Util.findWinner(this);
 		sendMessage(ChatColor.YELLOW + (winner.startsWith("The ") && winner.endsWith("s") ? winner + " have won the battle." : winner + " has won this duel."));
 
 		// Prepare for graceful delete
 		setDeleteTime(System.currentTimeMillis() + 3000L);
 		setInactive();
 	}
 
 	public void delete()
 	{
 		DataManager.battles.remove(getId());
 	}
 
 	public void sendMessage(String message)
 	{
 		for(String stringId : involvedPlayers)
 		{
 			OfflinePlayer offlinePlayer = DCharacter.Util.load(UUID.fromString(stringId)).getOfflinePlayer();
 			if(offlinePlayer.isOnline()) offlinePlayer.getPlayer().sendMessage(message);
 		}
 	}
 
 	public void sendRawMessage(String message)
 	{
 		for(String stringId : involvedPlayers)
 		{
 			OfflinePlayer offlinePlayer = DCharacter.Util.load(UUID.fromString(stringId)).getOfflinePlayer();
 			if(offlinePlayer.isOnline()) offlinePlayer.getPlayer().sendRawMessage(message);
 		}
 	}
 
 	public static class File extends ConfigFile
 	{
 		private static String SAVE_PATH;
 		private static final String SAVE_FILE = "battles.yml";
 
 		public File()
 		{
 			super(Demigods.plugin);
 			SAVE_PATH = Demigods.plugin.getDataFolder() + "/data/";
 		}
 
 		@Override
 		public ConcurrentHashMap<UUID, Battle> loadFromFile()
 		{
 			final FileConfiguration data = getData(SAVE_PATH, SAVE_FILE);
 			ConcurrentHashMap<UUID, Battle> map = new ConcurrentHashMap<UUID, Battle>();
 			for(String stringId : data.getKeys(false))
 				map.put(UUID.fromString(stringId), new Battle(UUID.fromString(stringId), data.getConfigurationSection(stringId)));
 			return map;
 		}
 
 		@Override
 		public boolean saveToFile()
 		{
 			FileConfiguration saveFile = getData(SAVE_PATH, SAVE_FILE);
 			Map<UUID, Battle> currentFile = loadFromFile();
 
 			for(UUID id : DataManager.battles.keySet())
 				if(!currentFile.keySet().contains(id) || !currentFile.get(id).equals(DataManager.battles.get(id))) saveFile.createSection(id.toString(), Util.get(id).serialize());
 
 			for(UUID id : currentFile.keySet())
 				if(!DataManager.battles.keySet().contains(id)) saveFile.set(id.toString(), null);
 
 			return saveFile(SAVE_PATH, SAVE_FILE, saveFile);
 		}
 	}
 
 	public static class Util
 	{
 		public static void save(Battle battle)
 		{
 			DataManager.battles.put(battle.getId(), battle);
 		}
 
 		public static Battle create(Participant damager, Participant damaged)
 		{
 			Battle battle = new Battle();
 			battle.generateId();
 			battle.setStartLocation(damager.getCurrentLocation().toVector().getMidpoint(damaged.getCurrentLocation().toVector()).toLocation(damager.getCurrentLocation().getWorld()));
 			battle.setStartTime(System.currentTimeMillis());
 
 			int default_range = Demigods.config.getSettingInt("battles.min_range");
 			double range = damager.getCurrentLocation().distance(damaged.getCurrentLocation());
 			if(range < default_range) battle.setRange(default_range);
 			else battle.setRange(range);
 
 			battle.setActive();
 
 			battle.setDuration(Demigods.config.getSettingInt("battles.min_duration") * 1000);
 			battle.setMinKills(Demigods.config.getSettingInt("battles.min_kills"));
 			battle.setMaxKills(Demigods.config.getSettingInt("battles.max_kills"));
 
 			battle.initialize();
 
 			battle.setStarter(damager.getRelatedCharacter());
 			battle.addParticipant(damager);
 			battle.addParticipant(damaged);
 			save(battle);
 			return battle;
 		}
 
 		public static Battle get(UUID id)
 		{
 			return DataManager.battles.get(id);
 		}
 
 		public static Set<Battle> getAll()
 		{
 			return Sets.newHashSet(DataManager.battles.values());
 		}
 
 		public static List<Battle> getAllActive()
 		{
 			return Lists.newArrayList(Collections2.filter(getAll(), new Predicate<Battle>()
 			{
 				@Override
 				public boolean apply(Battle battle)
 				{
 					return battle.isActive();
 				}
 			}));
 		}
 
 		public static List<Battle> getAllInactive()
 		{
 			return Lists.newArrayList(Collections2.filter(getAll(), new Predicate<Battle>()
 			{
 				@Override
 				public boolean apply(Battle battle)
 				{
 					return !battle.isActive();
 				}
 			}));
 		}
 
 		public static boolean existsInRadius(Location location)
 		{
 			return getInRadius(location) != null;
 		}
 
 		public static Battle getInRadius(final Location location)
 		{
 			try
 			{
 				return Iterators.find(getAllActive().iterator(), new Predicate<Battle>()
 				{
 					@Override
 					public boolean apply(Battle battle)
 					{
 						return battle.getStartLocation().distance(location) <= battle.getRange();
 					}
 				});
 			}
 			catch(NoSuchElementException ignored)
 			{}
 			return null;
 		}
 
 		public static boolean isInBattle(final Participant participant)
 		{
 			return Iterators.any(getAllActive().iterator(), new Predicate<Battle>()
 			{
 				@Override
 				public boolean apply(Battle battle)
 				{
 					return battle.getParticipants().contains(participant);
 				}
 			});
 		}
 
 		public static Battle getBattle(final Participant participant)
 		{
 			try
 			{
 				return Iterators.find(getAllActive().iterator(), new Predicate<Battle>()
 				{
 					@Override
 					public boolean apply(Battle battle)
 					{
 						return battle.getParticipants().contains(participant);
 					}
 				});
 			}
 			catch(NoSuchElementException ignored)
 			{}
 			return null;
 		}
 
 		public static boolean existsNear(Location location)
 		{
 			return getNear(location) != null;
 		}
 
 		public static Battle getNear(final Location location)
 		{
 			try
 			{
 				return Iterators.find(getAllActive().iterator(), new Predicate<Battle>()
 				{
 					@Override
 					public boolean apply(Battle battle)
 					{
 						double distance = battle.getStartLocation().distance(location);
 						return distance > battle.getRange() && distance <= Demigods.config.getSettingInt("battles.merge_range");
 					}
 				});
 			}
 			catch(NoSuchElementException ignored)
 			{}
 			return null;
 		}
 
 		public static Collection<Location> battleBorder(final Battle battle)
 		{
 			if(!Demigods.isRunningSpigot()) throw new SpigotNotFoundException();
 			return Collections2.transform(DLocation.Util.getCirclePoints(battle.getStartLocation(), battle.getRange(), 120), new Function<Location, Location>()
 			{
 				@Override
 				public Location apply(Location point)
 				{
 					return new Location(point.getWorld(), point.getBlockX(), point.getWorld().getHighestBlockYAt(point), point.getBlockZ());
 				}
 			});
 		}
 
 		/*
 		 * This is completely broken. TODO
 		 */
 		public static Location randomRespawnPoint(Battle battle)
 		{
 			List<Location> respawnPoints = getSafeRespawnPoints(battle);
 			if(respawnPoints.size() == 0) return battle.getStartLocation();
 
 			Location target = respawnPoints.get(Randoms.generateIntRange(0, respawnPoints.size() - 1));
 
 			Vector direction = target.toVector().subtract(battle.getStartLocation().toVector()).normalize();
 			double X = direction.getX();
 			double Y = direction.getY();
 			double Z = direction.getZ();
 
 			// Now change the angle
 			Location changed = target.clone();
 			changed.setYaw(180 - DLocation.Util.toDegree(Math.atan2(Y, X)));
 			changed.setPitch(90 - DLocation.Util.toDegree(Math.acos(Z)));
 			return changed;
 		}
 
 		/*
 		 * This is completely broken. TODO
 		 */
 		public static boolean isSafeLocation(Location reference, Location checking)
 		{
 			if(checking.getBlock().getType().isSolid() || checking.getBlock().getType().equals(Material.LAVA)) return false;
 			double referenceY = reference.getY();
 			double checkingY = checking.getY();
 			return Math.abs(referenceY - checkingY) <= 5;
 		}
 
 		public static List<Location> getSafeRespawnPoints(final Battle battle)
 		{
 			return Lists.newArrayList(Collections2.filter(Collections2.transform(DLocation.Util.getCirclePoints(battle.getStartLocation(), battle.getRange() - 1.5, 100), new Function<Location, Location>()
 			{
 				@Override
 				public Location apply(Location point)
 				{
 					return new Location(point.getWorld(), point.getBlockX(), point.getWorld().getHighestBlockYAt(point), point.getBlockZ());
 				}
 			}), new Predicate<Location>()
 			{
 				@Override
 				public boolean apply(Location location)
 				{
 					return isSafeLocation(battle.getStartLocation(), location);
 				}
 			}));
 		}
 
 		public static boolean canParticipate(Entity entity)
 		{
 			if(!(entity instanceof Player) && !(entity instanceof Tameable)) return false;
 			if(entity instanceof Player && DPlayer.Util.getPlayer((Player) entity).getCurrent() == null) return false;
 			return !(entity instanceof Tameable && Pet.Util.getTameable((LivingEntity) entity) == null);
 		}
 
 		public static Participant defineParticipant(Entity entity)
 		{
 			if(!canParticipate(entity)) return null;
 			if(entity instanceof Player) return DPlayer.Util.getPlayer((Player) entity).getCurrent();
 			return Pet.Util.getTameable((LivingEntity) entity);
 		}
 
 		public static void battleDeath(Participant damager, Participant damagee, Battle battle)
 		{
 			if(damager instanceof DCharacter) ((DCharacter) damager).addKill();
 			if(damager.getRelatedCharacter().getOfflinePlayer().isOnline()) damager.getRelatedCharacter().getOfflinePlayer().getPlayer().sendMessage(ChatColor.GREEN + "+1 Kill.");
 			battle.addKill(damager);
 			damagee.getEntity().setHealth(damagee.getEntity().getMaxHealth());
 			damagee.getEntity().teleport(randomRespawnPoint(battle));
 			if(damagee instanceof DCharacter) ((DCharacter) damagee).addDeath(damager.getRelatedCharacter());
 			if(damagee.getRelatedCharacter().getOfflinePlayer().isOnline()) damagee.getRelatedCharacter().getOfflinePlayer().getPlayer().sendMessage(ChatColor.RED + "+1 Death.");
 			battle.addDeath(damagee);
 		}
 
 		public static void battleDeath(Participant damagee, Battle battle)
 		{
 			damagee.getEntity().setHealth(damagee.getEntity().getMaxHealth());
 			damagee.getEntity().teleport(randomRespawnPoint(battle));
 			if(damagee instanceof DCharacter) ((DCharacter) damagee).addDeath();
 			if(damagee.getRelatedCharacter().getOfflinePlayer().isOnline()) damagee.getRelatedCharacter().getOfflinePlayer().getPlayer().sendMessage(ChatColor.RED + "+1 Death.");
 			battle.addDeath(damagee);
 		}
 
 		public static void sendBattleStats(Battle battle)
 		{
 			battle.sendMessage(ChatColor.DARK_AQUA + Titles.chatTitle("Battle Stats"));
 			battle.sendMessage(ChatColor.YELLOW + "  " + Unicodes.rightwardArrow() + " # of Participants: " + ChatColor.WHITE + battle.getParticipants().size());
 			battle.sendMessage(ChatColor.YELLOW + "  " + Unicodes.rightwardArrow() + " Duration: " + ChatColor.WHITE + (int) (System.currentTimeMillis() - battle.getStartTime()) / 1000 + " / " + (int) battle.getDuration() / 1000 + " seconds");
 			battle.sendMessage(ChatColor.YELLOW + "  " + Unicodes.rightwardArrow() + " Kill-count: " + ChatColor.WHITE + battle.getKillCounter() + " / " + battle.getMinKills());
 		}
 
 		public static String findWinner(Battle battle)
 		{
 			Map<String, Integer> score = Maps.newHashMap();
 			for(Map.Entry<String, Object> entry : battle.kills.entrySet())
 				score.put(entry.getKey(), Integer.parseInt(entry.getValue().toString()));
 			for(Map.Entry<String, Object> entry : battle.deaths.entrySet())
 			{
 				int base = 0;
 				if(score.containsKey(entry.getKey())) base = score.get(entry.getKey());
 				score.put(entry.getKey(), base - Integer.parseInt(entry.getValue().toString()));
 			}
			try
 			{
				ImmutableMap<String, Integer> sortedScore = ImmutableSortedMap.copyOf(score, Ordering.natural().reverse().onResultOf(Functions.forMap(score)));
				for(String stringId : sortedScore.keySet())
				{
					DCharacter character = DCharacter.Util.load(UUID.fromString(stringId));
					if(sortedScore.size() < 3) return character.getName();
					return "The " + character.getAlliance() + "s";
				}
 			}
			catch(Exception ignored)
			{}
 			return "Nobody";
 		}
 
 		public static boolean canTarget(Entity entity)
 		{
 			return canParticipate(entity) && canTarget(defineParticipant(entity));
 		}
 
 		/**
 		 * Returns true if doTargeting is allowed for <code>player</code>.
 		 * 
 		 * @param participant the player to check.
 		 * @return true/false depending on if doTargeting is allowed.
 		 */
 		public static boolean canTarget(Participant participant) // TODO REDO THIS
 		{
 			return !(participant instanceof DCharacter || participant instanceof Pet) || participant.canPvp() || participant.getCurrentLocation() != null && !Structures.isInRadiusWithFlag(participant.getCurrentLocation(), Structure.Flag.NO_PVP);
 		}
 
 		/**
 		 * Updates all battle particles. Meant for use in a Runnable.
 		 */
 		public static void updateBattleParticles()
 		{
 			for(Battle battle : Battle.Util.getAllActive())
 				for(Location point : Battle.Util.battleBorder(battle))
 					Spigots.playParticle(point, Effect.MOBSPAWNER_FLAMES, 0, 6, 0, 1F, 10, (int) (battle.getRange() * 2.5));
 		}
 
 		/**
 		 * Updates all battles.
 		 */
 		public static void updateBattles()
 		{
 			// End all active battles that should end.
 			for(Battle battle : Collections2.filter(Battle.Util.getAllActive(), new Predicate<Battle>()
 			{
 				@Override
 				public boolean apply(Battle battle)
 				{
 					return battle.getKillCounter() > battle.getMaxKills() || battle.getStartTime() + battle.getDuration() <= System.currentTimeMillis() && battle.getKillCounter() > battle.getMinKills() || battle.getParticipants().size() < 2;
 				}
 			}))
 				battle.end();
 
 			// Delete all inactive battles that should be deleted.
 			for(Battle battle : Collections2.filter(Battle.Util.getAllInactive(), new Predicate<Battle>()
 			{
 				@Override
 				public boolean apply(Battle battle)
 				{
 					return battle.getDeleteTime() >= System.currentTimeMillis();
 				}
 			}))
 				battle.delete();
 		}
 	}
 }
