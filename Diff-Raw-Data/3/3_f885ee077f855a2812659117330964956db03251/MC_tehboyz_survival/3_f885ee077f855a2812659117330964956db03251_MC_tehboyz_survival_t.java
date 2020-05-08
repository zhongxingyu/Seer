 package com.github.sashman.MC_tehboyz_survival;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.Location;
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
 
 
 
 
 
 public class MC_tehboyz_survival extends JavaPlugin implements Listener {
 	
 	public static final String CONFIG_LOCATION = "plugins/survival.config";
 	
 	public static enum GameState {Lobby, PreGame, Game};
 	private GameState current_state = GameState.Lobby;
 	
 	public static int MAX_PLAYERS = 1;
 	public static int WORLD_SIZE = 2048; //TODO Not a scoob what a decent default value is.
 	public static int BOUNDS_CHANGE_TIME = 10; // In mins?
 	
 	public static int COUNTDOWN_SEC = 5;
 	
 	private static int[] SPAWN_LOCATION = {0,0,0};
 	
 	//how far from the centre the players will be teleported
 	private static int TELEPORT_RADIUS = 300;
 	//radain difference between each player
 	private static float TELEPORT_RADIAN_OFFSET = (float) ((Math.PI*2)/MAX_PLAYERS);
 	
 	public static String welcome_msg = "Welcome to the tehboyz survival mod! Type /ready if you are ready to participate";
 	public static String game_start_msg = "Game will start shortly! Prepare to be teleported...";
 	
 	private World world;
 	
 	/**
 	 * List of players actually taking part in the game
 	 */
 	private ArrayList<Player> players_playing = new ArrayList<Player>();
 	
 	Logger log;
 
 	public void onEnable(){
 		log = this.getLogger();
 		getServer().getPluginManager().registerEvents(this, this);
 		
 		Config.readFile(this);
 		
 		
 	}
 	
 	// Perhaps enable/disable this on state change. Only runs every 10 seconds though ... 
 	// SASH: no, it will run every 10 sec, changed to 1 sec
 		private void startDayKeeper() {
 			
 			final MC_tehboyz_survival ref_this = this;
 			this.getServer().getScheduler().scheduleAsyncRepeatingTask(this,
 					new Runnable(){
 						@Override
 						public void run() {
 							if(ref_this.getState() == GameState.Lobby && world.getTime() > 8000){
 								world.setTime(6000);
 							}
 						}
 					},
 					20L, 20L);
 		}
 
 	
 	
 	public void onDisable(){ 
 
 	}
 	
 	private void broadcast_msg(String msg){
 		
 		getServer().broadcastMessage(ChatColor.AQUA
 				+ msg);
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
 								+ players_playing.size() + "/"
 								+ MAX_PLAYERS + ")");
 					}
 
 					/* If enough players are ready, start the game, change to PreGame state*/
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
 							getServer().broadcastMessage(ChatColor.AQUA + "" + count-- + " sec left");
 
 						}
 						
 						/* Start game after countdown */
 						getServer().broadcastMessage(ChatColor.AQUA + "Game has began!");
 						
 						GameStartEvent event = new GameStartEvent("Game started");
 						getServer().getPluginManager().callEvent(event);
 					}
 				}, 40L);
 	}
 	
 	@EventHandler(priority = EventPriority.HIGH)
 	public void onMapInitializeEvent(MapInitializeEvent event){
 	
 		
 		event.getMap().getWorld().setSpawnLocation(SPAWN_LOCATION[0], SPAWN_LOCATION[1], SPAWN_LOCATION[2]);

 	}
 	
 	@EventHandler(priority = EventPriority.HIGH)
 	public void onWorldLoadEvent(WorldLoadEvent event){
 	
 		world = event.getWorld();
 		startDayKeeper(); 
 	}
 	
 	@EventHandler(priority = EventPriority.HIGH)
     public void highLogin(PlayerLoginEvent event) {
 
     }
 	
 	private void teleportPlayers(){
 		
 		TELEPORT_RADIAN_OFFSET = (float) ((Math.PI*2)/players_playing.size());
 		
 		int i = 0;
 		for (Player player : players_playing) {
 			
 			//location for the player
 			int x = (int) (Math.cos(TELEPORT_RADIAN_OFFSET*i) * TELEPORT_RADIUS + SPAWN_LOCATION[0]);
 			int z = (int) (Math.sin(TELEPORT_RADIAN_OFFSET*i) * TELEPORT_RADIUS + SPAWN_LOCATION[2]);
 			int y = player.getWorld().getHighestBlockYAt(x,z);
 			
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
 		
     }
 	
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onPlayerJoin(PlayerJoinEvent event) {
 		switch (current_state) {
 		case Lobby:
 		case PreGame:
 			
 			/* Creative mode for every newly joined player  */
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
 		/* Disallow world editing in lobby state*/
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
 		/* Disallow world editing in lobby state*/
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
 	
 	public GameState getState(){
 		return current_state;
 	}
 	
 }
