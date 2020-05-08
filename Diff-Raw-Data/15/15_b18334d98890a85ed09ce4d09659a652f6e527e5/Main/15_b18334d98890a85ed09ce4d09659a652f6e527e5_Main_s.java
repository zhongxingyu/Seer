 package qs.hungergames;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Random;
 import java.util.Set;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Effect;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.WorldCreator;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.util.Vector;
 
 public class Main extends JavaPlugin implements Listener {
 
 	public static final String WINNERS_FILE = "winners.txt";
 	
 	public boolean isAutomated = true;
 
 	private Countdown lobbyTimer;
 	private Countdown countdown;
 
 	public boolean gameStarted = false;
 	public boolean inSafePeriod = false;
 	
 	public double arenaSize;
 	public double arenaSizeSquared;
 
 	public World gameWorld;
 	public Location gameSpawn;
 
 	public Set<Player> alive = new HashSet<Player>();
 	public Set<Player> bypass = new HashSet<Player>();
 	public Set<Player> loggedOut = new HashSet<Player>();
 
 	public HashMap<Player, Integer> trackUses = new HashMap<Player, Integer>();
 	
 	private Logger log;
 	
 	@Override
 	public void onEnable() { 
 		getServer().getPluginManager().registerEvents(this, this);
 		log = this.getLogger();
 		alive.clear();
 		
 		final File dataFolder = this.getDataFolder();
 		if (!dataFolder.exists()) dataFolder.mkdir();
 		
 		File winners = new File(dataFolder, WINNERS_FILE);
 		try {
 			winners.createNewFile();
 		} catch (IOException e) { e.printStackTrace(); }
 		
 		isAutomated = getConfig().getBoolean("automated", false);
 		if (isAutomated) {
 			startLobbyTimer();
 			log.info("Automation is enabled.");
 		} else {
 			log.info("Automation is disabled. Use /hg start to start the game.");
 		}
 		
 		getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
 			@Override
 			public void run() {
 				try {
 					FileInputStream fstream = new FileInputStream(new File(dataFolder, WINNERS_FILE));
 					BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
 					String strLine = br.readLine();
 					if (strLine != null)
 						getServer().broadcastMessage(ChatColor.AQUA + "Congratulations to our last champion, " + strLine);
 				} catch (FileNotFoundException e) {
 					log.warning("Internal exception fetching winner:");
 					e.printStackTrace();
 				} catch (IOException e) {
 					log.warning("Internal exception fetching winner:");
 					e.printStackTrace();
 				}
 			}
 		}, 1200L, 6000L);
 		
 	}
 	@Override
 	public void onDisable() { }
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
 		if (command.getName().equals("hungergames")) {
 			boolean isPlayer = sender instanceof Player;
 			boolean isAdmin = sender.hasPermission("hungergames.admin");
 			
 			if (args.length < 1 || args[0].equalsIgnoreCase("help")) {
 				sender.sendMessage(ChatColor.RED + "-------------------" + ChatColor.GOLD + "[Hunger Games]" + ChatColor.RED + "--------------------");
 				sender.sendMessage(ChatColor.RED + "/hg is an alias for /hungergames");
 				if (isPlayer)
 					sender.sendMessage(ChatColor.RED + "/hg track - tracks a player's coords in final five");
 				sender.sendMessage(ChatColor.RED + "/hg lobby - shows the time until the game starts");
 				if (isAdmin) {
 					sender.sendMessage(ChatColor.RED + "/hg start - starts the game");
 					sender.sendMessage(ChatColor.RED + "/hg end - forces the game to end with no winner");
 					if (isPlayer)
 						sender.sendMessage(ChatColor.RED + "/hg bypass - removes you from games, but keeps you online");
 				}
 				return true;
 			}
 
 			String action = args[0].toLowerCase();
 
 			if (isAdmin) {
 				if (action.equals("start") || action.equals("startgame")) {
 					startGame(false);
 					return true;
 				}
 				if (action.equals("end") || action.equals("forceend")) {
 					endGame(null);
 					return true;
 				}
 				if (action.equals("forcestart")) {
 					startGame(true);
 					return true;
 				}
 			}
 			if ((action.equalsIgnoreCase("lobby") || action.equals("lobbytimer")) && 
 					!gameStarted && !inSafePeriod) {
 				if (lobbyTimer == null) {
 					sendError(sender, "There is no lobby");
 					return true;
 				}
				sender.sendMessage(ChatColor.GOLD.toString() + lobbyTimer.time + " minutes remaining!");
 				return true;
 			}
 			if (isPlayer) {
 				Player pl = (Player) sender;
 				if (action.equals("bypass") && isAdmin) {
 					if (!gameStarted) {
 						if (!bypass.contains(pl)) {
 							bypass.add((Player) sender);
 							sender.sendMessage("bypass enabled");
 							return true;
 						}
 						bypass.remove(pl);
 						sender.sendMessage("bypass disabled");
 						return true;
 					}
 					sendError(pl, "You may not leave, or enter bypass mode mid game!");
 					return true;
 				}
 				if (gameStarted && action.equals("track") && alive.size() <= 5 && trackUses.get(pl) > 0){
 					double count = Double.MAX_VALUE;
 					Player trackee = null;
 					for (Player player: getServer().getOnlinePlayers()){
 						if (bypass.contains(player) || pl == player) continue;
 						double distance = player.getLocation().distanceSquared(pl.getLocation());
 						if (distance < count) {
 							trackee = player;
 							count = distance;
 						}
 					}
 					trackUses.put(pl, (trackUses.get(pl) - 1));
 					pl.sendMessage(ChatColor.BLUE + "You use your tracking skills to find the nearest player! (" +
 							trackUses.get(pl) + " uses left)");
 					pl.getPlayer().sendMessage("X: " + trackee.getLocation().getX() + " Y: " + trackee.getLocation().getY() +
 							" Z: " + trackee.getLocation().getZ());
 					return true;
 				}
 			} else {
 				if (action.equals("automated")) {
 					if (isAutomated) {
 						getConfig().set("automated", isAutomated = false);
 						sender.sendMessage(ChatColor.GOLD + "The game is no longer automated");
 						if (lobbyTimer != null) lobbyTimer.stop();
 						return true;
 					}
 					getConfig().set("automated", isAutomated = true);
 					sender.sendMessage(ChatColor.GOLD + "The game is now automated");
 					startLobbyTimer();
 					return true;
 				}
 			}
 
 		}
 		return false;
 	}
 	
 	/**
 	 * Starts a game.
 	 * @param forced whether the start was forced or effected by a lobby timeout
 	 * @return a boolean indicating success
 	 */
 	public boolean startGame(boolean forced){
 		if (gameStarted) return false;
 		if (lobbyTimer != null) lobbyTimer.stop();
 
 		gameStarted = true;
 		
 		WorldCreator wc = new WorldCreator("Hungergames");
 		Random random = new Random();
 		wc.seed(random.nextLong());
 		gameWorld = wc.createWorld();
 		gameSpawn = gameWorld.getSpawnLocation();
 		
 		alive.clear();
 		Player[] players = getServer().getOnlinePlayers();
 		final int playerCount = players.length;
 		final double
 			distance = 3.0,
 			angleStep = Math.PI * 2.0 / playerCount,
 			radius = distance * playerCount / (2.0 * Math.PI);
 		double angle = 0.0;
 		Location spawn = gameWorld.getSpawnLocation();
 		for (Player pl : players) {
 			if (isAutomated || pl.hasPermission("hungergames.canplay") || bypass.contains(pl)) {
 				pl.teleport(spawn.clone().add(radius * Math.cos(angle), 0, radius * Math.sin(angle)));
 				angle += angleStep;
 				
 				PlayerInventory inv = pl.getInventory();
 				inv.clear();
				ItemStack [] armor = pl.getInventory().getArmorContents();
				int i = 0;
				while (i < armor.length) {
					armor[i] = null;
					i++;
				}
				pl.getInventory().setArmorContents(armor);
 				pl.setHealth(20);
 				pl.setFoodLevel(20);
 				inv.addItem(new ItemStack(19, 10));
 				inv.addItem(new ItemStack(294, 1));
 				inv.addItem(new ItemStack(272, 1));
 				inv.addItem(new ItemStack(345, 1));
 				if (!bypass.contains(pl)) {
 					trackUses.put(pl, 3);
 					alive.add(pl);
 				}
 			} else {
 				pl.kickPlayer("You must pay $4 to participate in the hungergames tournament!");
 			}
 		}
 		
 		final int safeTime = 120 + playerCount * 9 / 4;
 		arenaSize = 300 + playerCount * 45 / 7;
 		arenaSizeSquared = arenaSize * arenaSize;
 		
 		if (forced) {
 			getServer().broadcastMessage(ChatColor.DARK_RED + "LET THE GAMES BEGIN!");
 			gameStarted = true;
 			inSafePeriod = false;
 		} else {
 			this.getServer().broadcastMessage(ChatColor.BLUE + "Welcome to the arena! You have a brief period of invincibility until the killing begins!");
 			inSafePeriod = true;
 
 			if (countdown != null) countdown.stop();
 			countdown = new Countdown(this, safeTime) {
 				@Override
 				public void sendTimeAlert(int time) {
 					getServer().broadcastMessage(ChatColor.GOLD + (
 						time > 60 ? String.format("%d minutes remaining", time / 60) :
 						time == 60 ? "One minute remaining" :
 						time == 30 ? "30 seconds remaining" :
 							Integer.toString(time)));
 				}
 
 				@Override
 				public void start() {
 					getServer().broadcastMessage(ChatColor.DARK_RED + "LET THE GAMES BEGIN!");
 					gameStarted = true;
 					inSafePeriod = false;
 					stop();
 				}
 			};
 		}
 		return true;
 	}
 	
 	/**
 	 * Ends a game.
 	 * @param winner the winner of the game (can be null)
 	 * @return a boolean indicating success
 	 */
 	public boolean endGame(Player winner) {
 		if (!gameStarted) return false;
 
 		if (countdown != null) countdown.stop();
 		World w = getServer().getWorld("world");
 		gameStarted = false;
 		for (Player pl : getServer().getOnlinePlayers()) {
 			pl.teleport(w.getSpawnLocation());
 		}
 		if (winner == null) {
 			getServer().broadcastMessage("The game was stopped!");
 		} else {
 			getServer().broadcastMessage(ChatColor.AQUA + winner.getName() + " has won! Congradulations!");
 			getServer().broadcastMessage(ChatColor.AQUA + "celebratory music will play!");
 			w.playEffect(w.getSpawnLocation(), Effect.RECORD_PLAY, 2258);
 			
 			ArrayList<String> original = new ArrayList<String>();
 			try {
 				BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(getDataFolder(), WINNERS_FILE))));
 				String line;
 				while ((line = br.readLine()) != null) original.add(line);
 				br.close();
 			} catch (IOException e) {
 				log.warning("Failed to update winners.txt:");
 				e.printStackTrace();
 				return false;
 			}
 			
 			PrintWriter out;
 			try {
 				out = new PrintWriter(new FileWriter(new File(getDataFolder(), WINNERS_FILE)));
 				
 				out.println(winner.getName());
 				for (String line : original) out.println(line);
 				out.close();
 			} catch (IOException e) {
 				log.warning("Failed to update winners.txt:");
 				e.printStackTrace();
 				return false;
 			}
 		}
 		
 		alive.clear();
 		
 		File file = gameWorld.getWorldFolder();
 		if (gameWorld.equals(getServer().getWorlds().get(0))){
 			log.info("the Hungergames world is the main world!");
 		}
 		if (!getServer().unloadWorld(gameWorld, false) ||
 				!deleteDir(file)) {
 			log.warning("Failed to delete the Hunger Games world");
 			
 			File f = new File("deleteworld.bat");
 			if (f.exists()) {
 				log.info("Found a batch file to restart the server with a new world.");
 				log.info("The server will be shut down and this file will be run.");
 				try {
 					Runtime.getRuntime().exec("cmd /c start deleteworld.bat");
 					getServer().shutdown();
 				} catch (IOException e) {
 					log.warning("Execution of 'deleteworld.bat' failed:");
 					e.printStackTrace();
 				}
 				return false;
 			}
 			log.info("No batch file was found to reload the world.");
 			log.info("You can download or write 'deleteworld.bat' to perform this operation.");
 			return false;
 		}
 		if (isAutomated) startLobbyTimer();
 		return true;
 	}
 	
 	/**
 	 * Sends an error to a command sender.
 	 * @param the command sender
 	 * @param message the error message
 	 */
 	public void sendError(CommandSender sender, String message){
 		sender.sendMessage(ChatColor.RED + message);
 	}
 	
 	/**
 	 * Recursively deletes a directory.
 	 * @param dir the directory.
 	 * @return a boolean indicating success
 	 */
 	public static boolean deleteDir(File dir) {
 		if (dir.isDirectory())
 			for (File f : dir.listFiles())
 				if (!deleteDir(f))
 					return false;
 		return dir.delete();
 	}
 	
 	/**
 	 * Starts the timer for automatically starting the game.
 	 */
 	public void startLobbyTimer() {
 		lobbyTimer = new Countdown(this, getConfig().getInt("lobbyTime")) {
 			@Override
 			public void sendTimeAlert(int time) {
 				getServer().broadcastMessage(ChatColor.GOLD + (
 					time > 60 ? String.format("%d minutes until game start", time / 60) :
 					time == 60 ? "One minute remaining!" :
 						String.format("%d seconds remaining!", time)));
 			}
 
 			@Override
 			public void start() {
 				startGame(false);
 			}
 		};
 	}
 
 
 	@EventHandler
 	public void onBlockClick(PlayerInteractEvent event) {
 		if (!gameStarted && !inSafePeriod && !(event.getPlayer().hasPermission("hungergames.admin"))) {
 			sendError(event.getPlayer(), "You may not modify blocks until the game begins");
 			event.setCancelled(true);
 		} else if (gameStarted && bypass.contains(event.getPlayer())) {
 			sendError(event.getPlayer(), "You may not modify the game world in bypass mode");
 			event.setCancelled(true);
 		}
 	}
 	
 	@EventHandler(priority=EventPriority.HIGHEST)
 	public void onPreProcessCommand(PlayerCommandPreprocessEvent event){
 		if ((!event.getMessage().startsWith("/hg")) &&
 				(!event.getMessage().startsWith("/hungergames")) &&
 				(!event.getPlayer().hasPermission("hungergames.admin"))){
 			event.setCancelled(true);
 			event.getPlayer().sendMessage(ChatColor.RED + "You may only use Hunger Games commands");
 		}
 	}
 	
 	@EventHandler
 	public void onPlayerDamage(EntityDamageEvent event){
 		if (!gameStarted || event.getEntity() instanceof Player && 
 				bypass.contains((Player) event.getEntity()))
 				event.setCancelled(true);
 	}
 	
 	@EventHandler
 	public void onPlayerDeath(PlayerDeathEvent event){
 		Player player = event.getEntity();
 		if (gameStarted == true && !bypass.contains(player)) {
 			alive.remove(player);
 			log.info(player.getName() + " was killed");
 			event.getEntity().kickPlayer("You were killed");
 			
 			for (Player pl: getServer().getOnlinePlayers()) {
 				Location loc = pl.getLocation();
 				loc.setY(loc.getY() + 15);
 				gameWorld.createExplosion(loc, 0F);
 			}
 			if (alive.size() == 1) {
 				endGame((Player) alive.toArray()[0]);
 				return;
 			}
 			if (alive.size() == 0) {
 				endGame(null);
 				return;
 			}
 			if (alive.size() == 5) {
 				getServer().broadcastMessage("There are five players left! You can use /hg track to hunt down another player.");
 				return;
 			}
 			getServer().broadcastMessage(String.format("A tribute has died. There are %d tributes remaining.", alive.size()));
 		}
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onPlayerJoin(PlayerJoinEvent event) {
 		Player pl = event.getPlayer();
 		if (!gameStarted) {
 			event.getPlayer().getInventory().clear();
 			return;
 		}
 		if (!alive.contains(pl) && !bypass.contains(pl)) {
 			pl.kickPlayer("You must wait until the next round to join");
 			return;
 		}
 		if (loggedOut.contains(event.getPlayer())) {
 			loggedOut.remove(event.getPlayer());
 		}
 	}
 	
 	@EventHandler
 	public void onPlayerMove (PlayerMoveEvent event){
 		Player pl = event.getPlayer();
 		if (!gameStarted || pl.getWorld() != gameWorld) return;
 		
 		Location loc = event.getTo();
 		double distance = gameSpawn.distanceSquared(loc);
 		if (distance <= arenaSizeSquared) return;
 		
 		final double base = 2.0,
 			power = 0.6;
 		Location diff = gameSpawn.clone().subtract(loc);
 		Vector force = diff.subtract(diff.toVector().normalize().multiply(arenaSize - base)).multiply(power).toVector();
 		pl.damage(4);
 		pl.setFireTicks(2);
 		pl.setVelocity(pl.getVelocity().add(force));
 		sendError(pl, "You may not leave the arena!");
 	}
 	
 	@EventHandler
 	public void onPlayerLeave(PlayerQuitEvent event) {
 		alive.remove(event.getPlayer());
 	}
 }
