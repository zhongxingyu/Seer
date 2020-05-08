 package com.github.sashman.MC_tehboyz_survival;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.entity.CreatureSpawnEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerLoginEvent;
 import org.bukkit.event.server.MapInitializeEvent;
 import org.bukkit.event.world.WorldLoadEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  * 
  * @author SASHMAN, HugeCannon
  * @version 0.1
  */
 public class MC_tehboyz_survival extends JavaPlugin implements Listener {
 	
 	private World world;
 	Logger log;
 	FileConfiguration config;
 	
 	/* Game state fields */
 	private static enum GameState {Lobby, PreGame, Game};
 	protected GameState current_state = GameState.Lobby;
 
 	/* Spawn + Start of game fields */
 	protected static int MAX_PLAYERS;
 	private static int TELEPORT_RADIUS; // how far from the centre the players will be teleported
 	private static int COUNTDOWN_SEC;
 	private static int[] SPAWN_LOCATION;
 	private static float TELEPORT_RADIAN_OFFSET; // radian difference between each player
 	
 	/* Game fields - bounds */
 	protected static int WORLD_SIZE;
 	protected static int BOUNDS_CHANGE_TIME;
 	protected static int BOUNDS_CHANGE_AMOUNT;
 	protected static int MINIMUM_WORLD_SIZE;
 	
 	/* Message fields */
 	private static String WELCOME_MSG;
 	private static String GAME_START_MSG;
 
 	/* Runnable task IDs */
 	private int lobbyBoundId;
 	private int gameBoundId;
 	
 	/**
 	 * List of players actually taking part in the game
 	 */
 	private ArrayList<Player> players_playing = new ArrayList<Player>();
 
 
 	
 	public void onEnable() {
 		
 		log = this.getLogger();
 		clear_player_data();
 		getServer().getPluginManager().registerEvents(this, this);
 		
 		readConfig();
 		init();
 		
 		startDayKeeper();
 	}
 
 
 	private void readConfig() {
 		config = getConfig();
 		
 		// Read values from config file
 		MAX_PLAYERS = config.getInt("max_players");
 		COUNTDOWN_SEC = config.getInt("countdoown_sec");
 		TELEPORT_RADIUS = config.getInt("teleport_radius");
 		WORLD_SIZE = config.getInt("world_size");
 		MINIMUM_WORLD_SIZE = config.getInt("minimum_world_size");
 		
 		List<Integer> spawn_loc = config.getIntegerList("spawn_location");
 		SPAWN_LOCATION = new int[]{spawn_loc.get(0), spawn_loc.get(1), spawn_loc.get(2)};
 		
 		WELCOME_MSG = config.getString("welcome_msg");
 		GAME_START_MSG = config.getString("game_start_msg");
 		
 		BOUNDS_CHANGE_AMOUNT = config.getInt("bounds_change_amount");
 		BOUNDS_CHANGE_TIME = config.getInt("bounds_change_time") *60*1000; // To ms from mins
 		
 		// Set up fields which depend on config values
 		TELEPORT_RADIAN_OFFSET = (float) ((Math.PI * 2) / MAX_PLAYERS);
 		
 		saveDefaultConfig();
 		
 	}
 	
 	private void clear_player_data() {
 		File dir = new File("world/players");
 		if(dir.exists()) log.info("Clearing players directory");
 		if(deleteDir(dir)) log.info("Cleared successfully");
 	}
 	
 	public boolean deleteDir(File dir) {
 	    if (dir.isDirectory()) {
 	        String[] children = dir.list();
 	        for (int i=0; i<children.length; i++) {
 	            boolean success = deleteDir(new File(dir, children[i]));
 	            
 	            return success;
 	        }
 	    }
 	    //debug
 	    //log.info(dir.toString());
 	    return dir.delete();
 	}
 
 
 	public void onDisable() {
 
 	}
 
 	private void broadcast_msg(String msg) {
 
 		getServer().broadcastMessage(ChatColor.AQUA + msg);
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd,
 			String commandLabel, String[] args) {
 
 		switch (current_state) {
 		case Lobby:
 
 			/* Command /ready */
 			if (cmd.getName().equalsIgnoreCase("ready")) {
 
 				/* Only player can issue */
 				if (sender instanceof Player) {
 					List<Player> players = ((Player) sender).getWorld()
 							.getPlayers();
 					if (!players_playing.contains((Player) sender)) {
 						players_playing.add((Player) sender);
 						broadcast_msg(((Player) sender).getName()
 								+ " is ready to play! ("
 								+ players_playing.size() + "/" + MAX_PLAYERS
 								+ ")");
 					}
 
 					/*
 					 * If enough players are ready, start the game, change to
 					 * PreGame state
 					 */
 					if (players_playing.size() >= MAX_PLAYERS) {
 						for (Player player : players) {
 							player.sendMessage(ChatColor.AQUA + GAME_START_MSG);
 						}
 						current_state = GameState.PreGame;
 						dispatchCounter();
 					}
 
 				}
 				return true;
 
 			}
 
 			break;
 
 		case Game:
 			break;
 
 		default:
 			break;
 		}
 
 		return false;
 	}
 
 	private void dispatchCounter() {
 
 		/* Start new thread used for the game countdown */
 		this.getServer().getScheduler()
 				.scheduleSyncDelayedTask(this, new Runnable() {
 					public void run() {
 
 						int count = COUNTDOWN_SEC;
 						while (count >= 0) {
 							try {
 								Thread.sleep(1000);
 							} catch (InterruptedException e) {
 								e.printStackTrace();
 							}
 							getServer()
 									.broadcastMessage(
 											ChatColor.AQUA + "" + count--
 													+ " sec left");
 
 						}
 
 						/* Start game after countdown */
 						getServer().broadcastMessage(
 								ChatColor.AQUA + "Game has began!");
 
 						GameStartEvent event = new GameStartEvent(
 								"Game started");
 						getServer().getPluginManager().callEvent(event);
 					}
 				}, 40L);
 	}
 
 	
 	public void init() {
 		world.setSpawnLocation(SPAWN_LOCATION[0], SPAWN_LOCATION[1], SPAWN_LOCATION[2]);
 	}
 
 
 	private void startDayKeeper() {
 		
 		final MC_tehboyz_survival ref_this = this;
 		this.getServer().getScheduler().scheduleAsyncRepeatingTask(this,
 				new Runnable(){
 					@Override
 					public void run() {
 						if(ref_this.world != null && ref_this.getState() == GameState.Lobby && world.getTime() > 8000){
 							world.setTime(6000);
 						}
 					}
 				},
 				20L, 20L);
 	}
 	
 	private void initWorldBounds() {
 		lobbyBoundId = this.getServer().getScheduler().scheduleAsyncRepeatingTask(this, 
 																new WorldBoundCheckerLobby(this.getServer(),WORLD_SIZE, WORLD_SIZE),
 																20L, 20L);
 	}
 
 	@EventHandler(priority = EventPriority.HIGH)
 	public void highLogin(PlayerLoginEvent event) {
 
 	}
 
 	private void teleportPlayers() {
 
 		TELEPORT_RADIAN_OFFSET = (float) ((Math.PI * 2) / players_playing
 				.size());
 
 		int i = 0;
 		for (Player player : players_playing) {
 
 			// location for the player
 			int x = (int) (Math.cos(TELEPORT_RADIAN_OFFSET * i)
 					* TELEPORT_RADIUS + SPAWN_LOCATION[0]);
 			int z = (int) (Math.sin(TELEPORT_RADIAN_OFFSET * i)
 					* TELEPORT_RADIUS + SPAWN_LOCATION[2]);
 			int y = player.getWorld().getHighestBlockYAt(x, z);
 
 			player.teleport(new Location(player.getWorld(), x, y, z));
 			log.info(player.getDisplayName() + " " + x + "," + z);
 			i++;
 			player.setGameMode(GameMode.SURVIVAL);
 			player.setHealth(player.getMaxHealth());
 			player.setFoodLevel(player.getMaxHealth());
 			player.getInventory().clear();
 		}
 
 	}
 
 	@EventHandler(priority = EventPriority.HIGH)
 	public void onGameStart(GameStartEvent event) {
 		current_state = GameState.Game;
 		teleportPlayers();
 		
 		// Kill lobbyBoundChecker
 		this.getServer().getScheduler().cancelTask(lobbyBoundId);
 		gameBoundId =  this.getServer().getScheduler().scheduleAsyncRepeatingTask(this, 
 				new WorldBoundCheckerGame(this.getServer(),WORLD_SIZE, WORLD_SIZE, BOUNDS_CHANGE_TIME, BOUNDS_CHANGE_AMOUNT, MINIMUM_WORLD_SIZE),
 				20L, 20L);
 
 	}
 
 	boolean bounds_set = false;
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onPlayerJoin(PlayerJoinEvent event) {
 
 		
		
		// hack to set the current world
		if (world == null)
			world = event.getPlayer().getWorld();
		
 		//hack to get the bounds set
 		if(!bounds_set){
 			initWorldBounds();
 			bounds_set = true;
 		}
 
 		switch (current_state) {
 		case Lobby:
 		case PreGame:
 
 			/* Creative mode for every newly joined player */
 			Player player = event.getPlayer();
 			player.sendMessage(ChatColor.AQUA + WELCOME_MSG);
 			player.setGameMode(GameMode.CREATIVE);
 
 			break;
 		case Game:
 
 			break;
 		default:
 			break;
 		}
 
 	}
 
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void CreatureSpawn(CreatureSpawnEvent event){ 
 		switch(current_state) {
 		case Lobby:  // Cancel spawning of items when in lobby stage
 			event.setCancelled(true);
 			break;
 		default:
 			break;
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onBlockBreak(BlockBreakEvent event) {
 		/* Disallow world editing in lobby state */
 		switch (current_state) {
 		case Lobby:
 		case PreGame:
 			event.setCancelled(true);
 			break;
 		case Game:
 			event.setCancelled(false);
 		default:
 			break;
 		}
 	}
 
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onBlockPlaceEvent(BlockPlaceEvent event) {
 		/* Disallow world editing in lobby state */
 		switch (current_state) {
 		case Lobby:
 		case PreGame:
 			event.setCancelled(true);
 			break;
 		case Game:
 			event.setCancelled(false);
 		default:
 			break;
 		}
 	}
 
 	public synchronized GameState getState(){
 		return current_state;
 	}
 
 }
