 package net.amoebaman.gamemaster;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Logger;
 
 import net.amoebaman.gamemaster.api.AutoGame;
 import net.amoebaman.gamemaster.api.GameMap;
 import net.amoebaman.gamemaster.enums.MasterStatus;
 import net.amoebaman.gamemaster.enums.PlayerStatus;
 import net.amoebaman.gamemaster.modules.MessagerModule;
 import net.amoebaman.statmaster.StatMaster;
 import net.amoebaman.statmaster.Statistic;
 import net.amoebaman.utils.CommandController;
 import net.amoebaman.utils.GenUtil;
 import net.amoebaman.utils.S_Loc;
 import net.amoebaman.utils.chat.Align;
 import net.amoebaman.utils.chat.Chat;
 import net.amoebaman.utils.chat.CustomChar;
 import net.amoebaman.utils.chat.Scheme;
 import net.amoebaman.utils.maps.PlayerMap;
 
 import org.bukkit.Bukkit;
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockState;
 import org.bukkit.block.Chest;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.scoreboard.Criterias;
 import org.bukkit.scoreboard.DisplaySlot;
 import org.bukkit.scoreboard.Objective;
 import org.bukkit.scoreboard.Scoreboard;
 
 
 /**
  * 
  * The main functioning class of GameMaster that stores and manages most important information.
  * This is a complete rewrite of the code based on a better-functioning and more flexible system for maps that was devised later.
  * The potential sloppiness of surgically inserting the new map system made a full refactoring seem more appealing to me.
  * For the old code, see <code>com.amoebaman.gamemaster.GameMaster.java</code>
  * 
  * @author Dennison Richter
  *
  */
 public class GameMaster extends JavaPlugin{
 	
 	/** A mapping of all players on the server to their status with the game master */
 	public static final PlayerMap<PlayerStatus> players = new PlayerMap<PlayerStatus>(PlayerStatus.PLAYING);
 	
 	/** A mapping to store the last recorded damage taken for each player */
 	public static final PlayerMap<Long> lastDamage = new PlayerMap<Long>(0L);
 	
 	/** A set containing all players that are awaiting auto-respawn (if the current game implements {@link RespawnModule}) */
 	public static final Set<Player> respawning = new HashSet<Player>();
 	
 	/** A set containing all the players that are utilizing team-only chat */
 	public static final Set<Player> teamChatters = new HashSet<Player>();
 	
 	/** A set mapping all players to their most recent vote for the next game or map */
 	public static final Map<CommandSender, String> votes = new HashMap<CommandSender, String>();
 	
 	/** The game that is currently playing */
 	public static AutoGame activeGame;
 	
 	/** The game that will play next, overriding the vote */
 	public static AutoGame nextGame;
 	
 	/** The game that played most recently */
 	public static AutoGame lastGame;
 	
 	/** The map that is currently playing */
 	public static GameMap activeMap;
 	
 	/** The map that will play next, overriding the vote */
 	public static GameMap nextMap;
 	
 	/** The maps that have been most recently played */
 	public static final List<GameMap> mapHistory = new ArrayList<GameMap>();
 	
 	/** The master state */
 	public static MasterStatus status = MasterStatus.PREP;
 	
 	/** The main spawn point for the game master */
 	public static Location mainLobby;
 	
 	/** The location where victory fireworks are launched from */
 	public static Location fireworksLaunch;
 	
 	/** The map being edited by admins */
 	public static GameMap editMap;
 	
 	/** The time when the current game started */
 	public static long gameStart;
 	
	public static boolean debugCycle;
 	protected static int recurringOpsTaskID;
 	protected static String mainDir;
 	protected static File configFile, mapsFile, repairFile;
 	protected static final Set<AutoGame> games = new HashSet<AutoGame>();
 	protected static final Set<GameMap> maps = new HashSet<GameMap>();
 	
 	public void onEnable(){
 		/*
 		 * Initialize listeners
 		 */
 		Bukkit.getPluginManager().registerEvents(new EventListener(), this);
 		CommandController.registerCommands(new CommandListener());
 		/*
 		 * Establish files and directories
 		 */
 		getDataFolder().mkdirs();
 		mainDir = getDataFolder().getPath();
 		configFile = GenUtil.getConfigFile(this,"config");
 		mapsFile = GenUtil.getConfigFile(this,"maps");
 		repairFile = GenUtil.getConfigFile(this,"repair");
 		/*
 		 * Load up configurations
 		 */
 		try{
 			getConfig().options().pathSeparator('/');
 			getConfig().load(configFile);
 			getConfig().options().copyDefaults();
 			getConfig().save(configFile);
 			mainLobby = S_Loc.stringLoad(getConfig().getString("main-lobby"));
 			if(mainLobby == null)
 				mainLobby = new Location(Bukkit.getWorlds().get(0), 0.5, 80, 0.5);
 			fireworksLaunch = S_Loc.stringLoad(getConfig().getString("fireworks-launch"));
 			if(fireworksLaunch == null)
 				fireworksLaunch = mainLobby.clone();
 			
 			YamlConfiguration mapsYaml = new YamlConfiguration();
 			mapsYaml.options().pathSeparator('/');
 			mapsYaml.load(mapsFile);
 			for(String name : mapsYaml.getKeys(false)){
 				ConfigurationSection mapSection = mapsYaml.getConfigurationSection(name);
 				GameMap map = new GameMap(name);
 				for(String key : mapSection.getKeys(true))
 					if(!mapSection.isConfigurationSection(key))
 						map.properties.set(key, mapSection.get(key));
 				registerMap(map);
 			}
 			
 			repair();
 		}
 		catch(Exception e){
 			e.printStackTrace();
 		}
 		/*
 		 * Update the statuses of players with admin permissions
 		 * This is needed in the event of a reload
 		 */
 		Bukkit.setDefaultGameMode(GameMode.SURVIVAL);
 		for(Player player : Bukkit.getOnlinePlayers())
 			if(player.hasPermission("arenamaster.admin"))
 				changeStatus(player, PlayerStatus.ADMIN);
 			else{
 				changeStatus(player, PlayerStatus.PLAYING);
 				player.teleport(mainLobby);
 			}
 		/*
 		 * Register statistics with the StatMaster
 		 */	
 		StatMaster.getHandler().registerStat(new Statistic("Wins", 0, "games", "default"));
 		StatMaster.getHandler().registerStat(new Statistic("Losses", 0, "games", "default"));
 		StatMaster.getHandler().registerCommunityStat(new Statistic("Big games", 0));
 		StatMaster.getHandler().registerCommunityStat(new Statistic("Votes", 0));
 		/*
 		 * Schedule recurring ops
 		 */
 		recurringOpsTaskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new RecurringOps(), 0, 10L);
 		/*
 		 * Add player health to the scoreboard
 		 */
 		Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
 		Objective objective = board.getObjective("health");
 		if(objective == null)
 			objective = board.registerNewObjective("health", Criterias.HEALTH);
 		objective.setDisplaySlot(DisplaySlot.BELOW_NAME);
 		objective.setDisplayName(" Heath");
 		/*
 		 * Start the works
 		 */
 		GameFlow.startIntermission();
 	}
 	
 	public void onDisable(){
 		Bukkit.getScheduler().cancelTask(recurringOpsTaskID);
 		/*
 		 * Save up configurations
 		 */
 		try{
 			getConfig().set("main-lobby", S_Loc.stringSave(mainLobby, true, true));
 			getConfig().set("fireworks-launch", S_Loc.stringSave(fireworksLaunch, true, false));
 			getConfig().save(configFile);
 			
 			YamlConfiguration mapsYaml = new YamlConfiguration();
 			mapsYaml.options().pathSeparator('/');
 			for(GameMap map : maps)
 				mapsYaml.createSection(map.name, map.properties.getValues(true));
 			mapsYaml.save(mapsFile);
 			
 			repair();
 		}
 		catch(Exception e){ e.printStackTrace(); }
 	}
 	
 	public static JavaPlugin plugin(){ return (JavaPlugin) Bukkit.getPluginManager().getPlugin("GameMaster"); }
 	
 	public static Logger logger(){ return plugin().getLogger(); }
 	
 	/**
 	 * Registers an {@link AutoGame} with the game master.
 	 * @param game a game
 	 */
 	public static void registerGame(AutoGame game){
 		if(game != null){
 			if(games.add(game))
 				logger().info("Successfully registered arena game named " + game.getGameName());
 			else
 				logger().warning("Failed to re-register arena game named " + game.getGameName());
 		}
 		else
 			logger().warning("Failed to register null arena game");
 	}
 	
 	/**
 	 * Removes an {@link AutoGame} from the list of available games.
 	 * @param game a game
 	 */
 	protected static void deregisterGame(AutoGame game){
 		games.remove(game);
 	}
 	
 	public static AutoGame getRegisteredGame(String name){
 		if(name == null)
 			return null;
 		name = name.replace('-', ' ').replace('_', ' ').toLowerCase();
 		for(AutoGame game : games)
 			if(game.getGameName().equalsIgnoreCase(name))
 				return game;
 		for(AutoGame game : games)
 			if(game.getGameName().toLowerCase().startsWith(name))
 				return game;
 		for(AutoGame game : games)
 			for(String alias : game.getAliases())
 				if(alias.equalsIgnoreCase(name))
 					return game;
 		return null;
 	}
 	
 	public static void registerMap(GameMap map){
 		if(map != null){
 			if(maps.add(map))
 				logger().info("Successfully loaded arena map named " + map.name);
 			else
 				logger().warning("Failed to re-load arena map named " + map.name);
 		}
 		else
 			logger().warning("Failed to load null arena map");
 	}
 	
 	protected static void deregisterMap(GameMap map){
 		maps.remove(map);
 	}
 	
 	public static GameMap getRegisteredMap(String name){
 		for(GameMap map : maps)
 			if(map.name.equalsIgnoreCase(name))
 				return map;
 		for(GameMap map : maps)
 			if(map.name.toLowerCase().startsWith(name.toLowerCase()))
 				return map;
 		return null;
 	}
 	
 	public static Set<GameMap> getCompatibleMaps(AutoGame game){
 		Set<GameMap> set = new HashSet<GameMap>();
 		for(GameMap map : maps)
 			if(game.isCompatible(map))
 				set.add(map);
 		return set;
 	}
 	
 	public static PlayerStatus getStatus(Player player){
 		return players.get(player);
 	}
 	
 	public static void changeStatus(Player player, PlayerStatus pStatus){
 		players.put(player, pStatus == null ? PlayerStatus.PLAYING : pStatus);
 		resetPlayer(player);
 		lastDamage.remove(player);
 		respawning.remove(player);
 		if(status.active){
 			switch(pStatus){
 				case ADMIN:
 				case EXTERIOR:
 					activeGame.removePlayer(player);
 					break;
 				case PLAYING:
 					activeGame.addPlayer(player);
 					if(activeGame instanceof MessagerModule)
 						Chat.send(player, Align.addSpacers("" + Scheme.HIGHLIGHT.normal.color() + CustomChar.LIGHT_BLOCK, Align.center(((MessagerModule) activeGame).getRespawnMessage(player))));
 					break;
 			}
 		}
 		else if(pStatus == PlayerStatus.PLAYING)
 			player.teleport(mainLobby);
 		switch(pStatus){
 			case ADMIN:
 				player.setGameMode(GameMode.CREATIVE);
 				break;
 			case EXTERIOR:
 			case PLAYING:
 				player.setGameMode(GameMode.SURVIVAL);
 				break;
 		}
 	}
 	
 	/**
 	 * Gets a list of all currently online players who are participanting in the game.
 	 * This method guarantees to return no null elements in the set, in addition to no offline players.
 	 * @return a set containing all participating players
 	 */
 	public static Set<Player> getPlayers(){
 		Set<Player> players = new HashSet<Player>();
 		for(Player player : Bukkit.getOnlinePlayers())
 			if(player.isOnline() && getStatus(player) == PlayerStatus.PLAYING)
 				players.add(player);
 		players.remove(null);
 		return players;
 	}
 	
 	/**
 	 * Clears a player's inventory and removes all their potion effects.
 	 * 
 	 * @param player a player
 	 */
 	public static void clearInventory(Player player){
 		player.closeInventory();
 		player.getInventory().clear();
 		player.getInventory().setArmorContents(null);
 		for(PotionEffect effect : player.getActivePotionEffects())	
 			player.removePotionEffect(effect.getType());
 	}
 	
 	/**
 	 * Resets a player fully.
 	 * <p>
 	 * Clears inventory; restores health, hunger, and saturation to full; removes
 	 * formatting from display name and list name; removes GameMaster records;
 	 * ejects the player from their vehicle.
 	 * 
 	 * @param player a player
 	 */
 	public static void resetPlayer(Player player){
 		clearInventory(player);
 		player.setHealth(player.getMaxHealth());
 		player.setFoodLevel(20);
 		player.setSaturation(20);
 		player.setDisplayName(player.getName());
 		player.setPlayerListName(player.getName());
 		lastDamage.remove(player);
 		respawning.remove(player);
 		if(player.isInsideVehicle())
 			player.getVehicle().eject();
 	}
 	
 	public static void updatePlayerColors(){
 		for(Player player : Bukkit.getOnlinePlayers()){
 			String colorName = player.getName();
 			if(getStatus(player) == PlayerStatus.PLAYING && status.active)
 				colorName = GameMaster.activeGame.getNameColor(player) + colorName;
 			player.setDisplayName(colorName);
 		}
 	}
 	
 	/**
 	 * Runs through the votes map and returns the option that has recieved the most votes.
 	 * @return the most voted-for option
 	 */
 	public static String getMostVoted(){
 		Map<String, Integer> tally = new HashMap<String, Integer>();
 		for(String vote : votes.values()){
 			if(!tally.containsKey(vote))
 				tally.put(vote, 0);
 			tally.put(vote, tally.get(vote) + 1);
 		}
 		String mostVoted = null;
 		int mostVotes = 0;
 		for(String vote : tally.keySet())
 			if(tally.get(vote) > mostVotes){
 				mostVoted = vote;
 				mostVotes = tally.get(vote);
 			}
 		return mostVoted;
 	}
 	
 	/**
 	 * Gets the killer of a player.
 	 * This method will first try to use Player.getKiller().
 	 * If that call returns null, it will examine the player's last damage cause and attempt to determine the killer from that.
 	 * If no killer can be found, it will return null.
 	 * @param victim the recently deceased
 	 * @return the player responsible for the death, or null if none was found
 	 */
 	public static Player getKiller(Player victim){
 		Player killer = victim.getKiller();
 		if(killer == null){
 			try{
 				killer = (Player) GenUtil.getTrueCulprit((EntityDamageByEntityEvent) victim.getLastDamageCause());
 			}
 			catch(ClassCastException cce){}
 		}
 		return killer;
 	}
 	
 	/**
 	 * Registers a set of blockstates to be automatically restored when the game ends, or
 	 * if the server shuts down prematurely.  This set is saved to a flatfile, so even if the
 	 * server crashes without warning these blocks will still be repaired on the next startup.
 	 * <p>
 	 * Games should register all the blocks they change in this manner, to ensure maps are
 	 * always repaired when the game is finished.
 	 * 
 	 * @param states
 	 */
 	@SuppressWarnings("deprecation")
     public static void defRepair(Set<BlockState> states){
 		YamlConfiguration repairYaml = new YamlConfiguration();
 		repairYaml.options().pathSeparator('/');
 		for(BlockState state : states)
 			repairYaml.set(S_Loc.stringSave(state.getLocation(), true, false), state.getTypeId() + " " + state.getRawData());
 		try {
 	        repairYaml.save(repairFile);
         }
         catch (Exception e) {
 	        e.printStackTrace();
         }
 	}
 	
 	/**
 	 * Loads the repair list from flat-file and restores all the block states therein.
 	 */
 	@SuppressWarnings("deprecation")
     public static void repair(){
 		YamlConfiguration repairYaml = new YamlConfiguration();
 		repairYaml.options().pathSeparator('/');
 		try {
 	        repairYaml.load(repairFile);
 	        repairFile.delete();
 	        repairFile.createNewFile();
         }
         catch (Exception e) {
 	        e.printStackTrace();
 	        return;
         }
 		for(String key : repairYaml.getKeys(false)){
 			Block block = S_Loc.stringLoad(key).getBlock();
 			String[] split = repairYaml.getString(key).split(" ");
 			if(block.getType() == Material.CHEST)
 				((Chest) block.getState()).getBlockInventory().clear();
 			block.setTypeIdAndData(Integer.parseInt(split[0]), Byte.parseByte(split[1]), false);
 		}
 	}
 }
