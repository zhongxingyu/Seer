 package com.martinbrook.DeathSwap;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.Random;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Server;
 import org.bukkit.World;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 
 public class DeathSwap extends JavaPlugin {
 	
 	private HashMap<String, DeathSwapPlayer> allPlayers = new HashMap<String, DeathSwapPlayer>();
 	private ArrayList<DeathSwapPlayer> survivors = new ArrayList<DeathSwapPlayer>();
 	private Server server;
 	private Location spawn;
 	private boolean matchRunning = false;
 	private Calendar matchStartTime;
 	private int readyCount = 0;
 	private int matchTimer = -1;
 	private int swapTimer = -1;
	private static int MINIMUM_SWAP_TIME = 22;
	private static int SWAP_CHECK_TIME = 6;
	private static int SWAP_PROBABILITY = 14;
 	private static int NUMBER_OF_PLAYERS = 2;
 	private MatchCountdown matchCountdown;
 	private static int COUNTDOWN_DURATION = 15;
 	private static int INITIAL_RESISTANCE_DURATION = 10;
 	private static int SWAP_RESISTANCE_DURATION = 5;
 	private boolean uhcMode = false;
 	private static int START_RADIUS = 1000;
 	ArrayList<Location> startPoints;
 	
     @Override
     public void onEnable(){
         server = getServer();
         spawn = getServer().getWorlds().get(0).getSpawnLocation();
         getServer().getPluginManager().registerEvents(new DeathSwapListener(this), this);
 		
     }
  
     
     public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
     	String c = cmd.getName().toLowerCase();
     	String response = null;
 
 		if (c.equals("join") && sender instanceof Player) {
 			response = cJoin((Player) sender);
 		} else if (c.equals("leave") && sender instanceof Player) {
 			response = cLeave((Player) sender);
 		} else if (c.equals("ready") && sender instanceof Player) {
 			response = cReady((Player) sender);
 		} else if (c.equals("ds")) {
 			response = cDs(args);
 		}
 		
 		
 		if (response != null)
 			sender.sendMessage(response);
 		
 		return true;
     }
 
 	private String cDs(String[] args) {
 		if (args.length < 1) return ChatColor.GRAY + "/ds command options:\n" +
 				"  /ds reset   - Abort the current match and teleport players back to spawn\n" +
 				"  /ds uhc     - Toggle UHC mode";
 		String cmd = args[0].toLowerCase();
 		
 		if (cmd.equals("reset")) {
 			return cDsReset();
 		}
 		
 		if (cmd.equals("uhc")) {
 			uhcMode = !uhcMode;
 			return ChatColor.AQUA + "UHC mode is now " + (uhcMode? "enabled" : "disabled") + "!";
 		}
 		
 		return ChatColor.RED + "/ds command not understood. Type /ds for options.";
 	}
 
 	private String cDsReset() {
 		// Teleports all players to spawn, aborts the current match.
 		
 		for (DeathSwapPlayer dp : allPlayers.values()) dp.teleport(spawn);
 		resetMatch();
 
 		broadcast(ChatColor.GOLD + "DeathSwap has been reset.");
 		return null;
 	}
 
 	private String cReady(Player sender) {
 		DeathSwapPlayer p = getDeathSwapPlayer(sender);
 		
 		if (p == null) return ChatColor.RED + "You have not joined this match! Type /join to join.";
 		if (p.isReady()) return ChatColor.RED + "You are already marked as ready!";
 
 		p.setReady();
 		broadcast(ChatColor.GREEN + sender.getDisplayName() + " is ready!");
 		readyCount++;
 		if (readyCount == NUMBER_OF_PLAYERS) startMatchCountdown();
 		return null;
 	}
 
 	private void startMatchCountdown() {
 		matchCountdown = new MatchCountdown(COUNTDOWN_DURATION, this);
 		broadcast(ChatColor.GRAY + "Generating chunks, prepare for possible lag...");
 		World w = server.getWorlds().get(0);
 		int playerCount = survivors.size();
 		startPoints = MatchUtils.calculateRadialStarts(w, playerCount, START_RADIUS);
 		 
 	}
 	
 	private void stopMatchCountdown() {
 		if (matchCountdown == null) return;
 
 		matchCountdown.cancel();
 		matchCountdown = null;
 	}
 
 
 
 	private String cLeave(Player sender) {
 		DeathSwapPlayer dp = getDeathSwapPlayer(sender);
 		if (dp == null) return ChatColor.RED + "Unable to leave - you have not joined this match!";
 		
 		if (matchRunning) return ChatColor.RED + "Unable to leave - the match has already started!";
 		
 		allPlayers.remove(sender.getName().toLowerCase());
 		survivors.remove(dp);
 		return ChatColor.GREEN + "You have left the match!";
 		
 	}
 
 	private String cJoin(Player sender) {
 		if (getDeathSwapPlayer(sender) != null) return ChatColor.RED + "You have already joined this match! Type /ready when ready to begin.";
 		
 		if (allPlayers.size() >= NUMBER_OF_PLAYERS) return ChatColor.RED + "Unable to join - the match is already full!";
 		
 		DeathSwapPlayer dp = new DeathSwapPlayer(sender.getName(), this);
 		allPlayers.put(sender.getName().toLowerCase(), dp);
 		survivors.add(dp);
 		return ChatColor.GREEN + "You have joined the match! Type /ready when ready to begin.";
 	}
 	
 	public void broadcast(String message) {
 		server.broadcastMessage(message);
 	}
 	
 	public DeathSwapPlayer getDeathSwapPlayer(Player p) {
 		return getDeathSwapPlayer(p.getName());
 	}
 	
 	public DeathSwapPlayer getDeathSwapPlayer(String name) {
 		return allPlayers.get(name.toLowerCase());
 	}
 
 
 	public void swapPlayers() {
 		broadcast(ChatColor.GOLD + "Swapping now!");
 		doPlayerSwap();
 		
 		startSwapTimer();
 		
 	}
 
 	private void doPlayerSwap() {
 		for (int i = 0; i < survivors.size(); i++) {
 			if (!survivors.get(i).isOnline()) {
 				broadcast(ChatColor.RED + "Unable to swap - " + survivors.get(i).getName() + " is offline");
 				return;
 			}
 		}
 		Player p1 = survivors.get(0).getPlayer();
 		Player p2 = survivors.get(1).getPlayer();
 		
 		Location l1 = p1.getLocation();
 		Location l2 = p2.getLocation();
 		for (DeathSwapPlayer dp : survivors) dp.giveResistance(SWAP_RESISTANCE_DURATION);
 		
 		p1.teleport(l2);
 		p2.teleport(l1);
 	}
 
 
 	public void startMatch() {
 		startMatchTimer();
 		startSwapTimer();
 		matchRunning = true;
 		for (DeathSwapPlayer dp : survivors) {
 			dp.renew();
 			dp.giveResistance(INITIAL_RESISTANCE_DURATION);
 		}
 		server.getWorlds().get(0).setTime(0);
 		launchPlayers();
 		setPlayerVisibility();
 		broadcast(ChatColor.AQUA + "GO!");
 	}
 	
 	
 	private void launchPlayers() {
 		int playerCount = survivors.size();
 				
 		for (int i = 0; i < playerCount; i++) {
 			DeathSwapPlayer dp = survivors.get(i);
 			if (dp.isOnline()) {
 				// Ensure chunk is loaded
 				startPoints.get(i).getChunk().load();
 				dp.teleport(startPoints.get(i));
 			}
 		}
 	}
 
 
 	private void startSwapTimer() {
 		swapTimer = server.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
 			public void run() {
				checkForSwap();
 			}
		}, MINIMUM_SWAP_TIME * 20);
		
 		
 		
 	}
	private void checkForSwap() {
		Random r = new Random();
		if (r.nextInt(100) < SWAP_PROBABILITY) {
			swapPlayers();
		} else {
			swapTimer = server.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
				public void run() {
					checkForSwap();
				}
			}, SWAP_CHECK_TIME * 20);
		}
	}
	
 	private void stopSwapTimer() {
 		if (swapTimer != -1) {
 			server.getScheduler().cancelTask(swapTimer);
 		}
 	}
 
 
 	/**
 	 * Starts the match timer
 	 */
 	private void startMatchTimer() {
 		matchStartTime = Calendar.getInstance();
 		matchTimer = server.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
 			public void run() {
 				doMatchProgressAnnouncement();
 			}
 		}, 1200L, 1200L);
 	}
 	
 	/**
 	 * Stops the match timer
 	 */
 	private void stopMatchTimer() {
 		if (matchTimer != -1) {
 			server.getScheduler().cancelTask(matchTimer);
 		}
 	}
 	
 	/**
 	 * Display the current match time if it is a multiple of 30.
 	 */
 	private void doMatchProgressAnnouncement() {
 		long matchTime = MatchUtils.getDuration(matchStartTime, Calendar.getInstance()) / 60;
 		if (matchTime % 10 == 0 && matchTime > 0) {
 			broadcast(matchTimeAnnouncement(false));
 		}
 	}
 	
 	/**
 	 * Get the text of a match time announcement
 	 * 
 	 * @param precise Whether to give a precise time (00:00:00) instead of (xx minutes)
 	 * @return Current match time as a nicely-formatted string
 	 */
 	public String matchTimeAnnouncement(boolean precise) {
 		if (this.matchRunning == false)
 			return ChatColor.AQUA + "Match time: " + ChatColor.GOLD + MatchUtils.formatDuration(0, precise);
 		else
 			return ChatColor.AQUA + "Match time: " + ChatColor.GOLD + MatchUtils.formatDuration(matchStartTime, Calendar.getInstance(), precise);
 
 	}
 
 
 	public boolean matchInProgress() {
 		return matchRunning;
 	}
 
 
 	public void handlePlayerDeath(DeathSwapPlayer dp) {
 		dp.setDead();
 		survivors.remove(dp);
 		setPlayerVisibility(dp.getPlayer()); // Make them a spectator
 		if (survivors.size() == 1) {
 			handleVictory(getDeathSwapPlayer(survivors.get(0).getName()));
 		}
 		
 	}
 
 
 	private void handleVictory(DeathSwapPlayer winner) {
 		broadcast(ChatColor.AQUA + winner.getName() + " is the winner!");
 		broadcast(matchTimeAnnouncement(true));
 		
 		resetMatch();
 		
 		
 		
 	}
 
 
 	private void resetMatch() {
 		allPlayers.clear();
 		survivors.clear();
 		
 		matchRunning = false;
 		uhcMode = false;
 
 		stopMatchTimer();
 		stopSwapTimer();
 		stopMatchCountdown();
 		setPlayerVisibility();
 		
 	}
 
 
 	public boolean uhcEnabled() {
 		return uhcMode;
 	}
 	
 	/**
 	 * Set the correct vanish status for all players on the server
 	 */
 	public void setPlayerVisibility() {
 		for(Player p : server.getOnlinePlayers()) setPlayerVisibility(p);
 	}
 	
 	public void setPlayerVisibility(Player viewed) {
 		DeathSwapPlayer viewedDp = this.getDeathSwapPlayer(viewed);
 		boolean viewedIsSpectator = (!matchInProgress() || viewedDp == null || !viewedDp.isAlive());
 		
 		MatchUtils.setAffectsSpawning(viewed, !viewedIsSpectator);
 		MatchUtils.setCollidesWithEntities(viewed, !viewedIsSpectator);
 
 		for (Player viewer : server.getOnlinePlayers()) {
 			DeathSwapPlayer dp = this.getDeathSwapPlayer(viewer);
 
 			// Figure out if viewer is a spectator
 			if (!matchInProgress() || dp == null || !dp.isAlive()) {
 				// Viewer is a spectator; they can see the player.
 				viewer.showPlayer(viewed);
 			} else {
 				// Viewer is a player; they can only see other players.
 				if (viewedIsSpectator) viewer.hidePlayer(viewed);
 				else viewer.showPlayer(viewed);
 			}
 		}
 	}
 }
