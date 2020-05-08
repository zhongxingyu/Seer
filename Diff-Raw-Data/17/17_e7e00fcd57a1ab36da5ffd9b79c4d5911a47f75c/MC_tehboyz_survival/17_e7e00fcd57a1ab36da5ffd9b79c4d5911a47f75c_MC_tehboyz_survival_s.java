 package com.github.sashman.MC_tehboyz_survival;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.Server;
 import org.bukkit.World;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
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
 
 
 	/* Config fields */
 	protected static final String CONFIG_LOCATION = "plugins/survival.config";
 	protected static int MAX_PLAYERS = 1;
 	private World world;
 	Logger log;
 	
 	/* Game state fields */
 	private static enum GameState {Lobby, PreGame, Game};
 	protected GameState current_state = GameState.Lobby;
 
 	/* Spawn + Start of game fields */
 	private static int COUNTDOWN_SEC = 5;
 	private static int[] SPAWN_LOCATION = { 0, 0, 0 };
 	private static int TELEPORT_RADIUS = 300; // how far from the centre the players will be teleported
 	private static float TELEPORT_RADIAN_OFFSET = (float) ((Math.PI * 2) / MAX_PLAYERS); // radian difference between each player
 	/* Game fields - bounds */
 	protected static int WORLD_SIZE = 512; // 1024 will mean from -512 to 512
 	protected static int BOUNDS_CHANGE_TIME = 1 *60*1000; // In ms 
 	protected static int BOUNDS_CHANGE_AMOUNT = 50; // Blocks reduced per bounds change (x-50 and z-50).
 	protected static int MINIMUM_WORLD_SIZE = 100;

 	/* Message fields */
 	private static final String welcome_msg = "Welcome to the tehboyz survival mod! Type /ready if you are ready to participate";
 	private static final String game_start_msg = "Game will start shortly! Prepare to be teleported...";
 
 	/* Runnable task IDs */
 	private int lobbyBoundId;
 	private int gameBoundId;
 	
 	/**
 	 * List of players actually taking part in the game
 	 */
 	private ArrayList<Player> players_playing = new ArrayList<Player>();
 
 
 	public void onEnable() {
 		log = this.getLogger();
 		getServer().getPluginManager().registerEvents(this, this);
 
 		Config.readFile(this);
 		startDayKeeper();
 
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
 							player.sendMessage(ChatColor.AQUA + game_start_msg);
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
 
 	@EventHandler(priority = EventPriority.HIGH)
 	public void onMapInitializeEvent(MapInitializeEvent event) {
 
 		event.getMap()
 				.getWorld()
 				.setSpawnLocation(SPAWN_LOCATION[0], SPAWN_LOCATION[1],
 						SPAWN_LOCATION[2]);
 
 	}
 
 	//apparently doesnt happen....
 	@EventHandler(priority = EventPriority.HIGH)
 	public void onWorldLoadEvent(WorldLoadEvent event){
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
 			player.sendMessage(ChatColor.AQUA + welcome_msg);
 			player.setGameMode(GameMode.CREATIVE);
 
 			break;
 		case Game:
 
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
