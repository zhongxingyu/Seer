 package com.martinbrook.tesseractuhc;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Random;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Difficulty;
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.Server;
 import org.bukkit.SkullType;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.block.Skull;
 import org.bukkit.configuration.Configuration;
 import org.bukkit.entity.EnderDragon;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Ghast;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.MagmaCube;
 import org.bukkit.entity.Monster;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Slime;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.Recipe;
 import org.bukkit.inventory.ShapedRecipe;
 import org.bukkit.inventory.ShapelessRecipe;
 import com.martinbrook.tesseractuhc.countdown.BorderCountdown;
 import com.martinbrook.tesseractuhc.countdown.MatchCountdown;
 import com.martinbrook.tesseractuhc.countdown.PVPCountdown;
 import com.martinbrook.tesseractuhc.countdown.PermadayCountdown;
 import com.martinbrook.tesseractuhc.customevent.UhcJoinEvent;
 import com.martinbrook.tesseractuhc.customevent.UhcMatchEndEvent;
 import com.martinbrook.tesseractuhc.customevent.UhcMatchStartEvent;
 import com.martinbrook.tesseractuhc.customevent.UhcPlayerLocationUpdateEvent;
 import com.martinbrook.tesseractuhc.event.UhcEvent;
 import com.martinbrook.tesseractuhc.notification.ProximityNotification;
 import com.martinbrook.tesseractuhc.notification.UhcNotification;
 import com.martinbrook.tesseractuhc.startpoint.LargeGlassStartPoint;
 import com.martinbrook.tesseractuhc.startpoint.SmallGlassStartPoint;
 import com.martinbrook.tesseractuhc.startpoint.UhcStartPoint;
 import com.martinbrook.tesseractuhc.util.FileUtils;
 import com.martinbrook.tesseractuhc.util.MatchUtils;
 import com.martinbrook.tesseractuhc.util.PluginChannelUtils;
 
 public class UhcMatch {
 
 	private World startingWorld;
 	private HashMap<Integer, UhcStartPoint> startPoints = new HashMap<Integer, UhcStartPoint>();
 	private Location lastNotifierLocation;
 	private Location lastDeathLocation;
 	private Location lastEventLocation;
 	private Location lastLogoutLocation;
 
 	private ArrayList<String> chatScript;
 	private Boolean chatMuted = false;
 	private Boolean permaday = false;
 	private int permadayTaskId;
 	
 	private ArrayList<UhcStartPoint> availableStartPoints = new ArrayList<UhcStartPoint>();
 	private HashMap<String, UhcTeam> uhcTeams = new HashMap<String, UhcTeam>(32);
 	
 	private ArrayList<String> launchQueue = new ArrayList<String>();
 	public static int GOLD_LAYER = 32;
 	public static int DIAMOND_LAYER = 16;
 	private ArrayList<UhcParticipant> participantsInMatch = new ArrayList<UhcParticipant>();
 	private ArrayList<UhcTeam> teamsInMatch = new ArrayList<UhcTeam>();
 	private Calendar matchStartTime = null;
 	private int matchTimer = -1;
 	private long lastMatchTimeAnnouncement = 0;
 	private ArrayList<Location> calculatedStarts = null;
 	private boolean pvp = false;
 	private int spawnKeeperTask = -1;
 	private TesseractUHC plugin;
 	private Server server;
 	private MatchPhase matchPhase = MatchPhase.PRE_MATCH;
 	private MatchCountdown matchCountdown;
 	private BorderCountdown borderCountdown;
 	private PermadayCountdown permadayCountdown;
 	private PVPCountdown pvpCountdown;
 	private ArrayList<UhcPOI> uhcPOIs = new ArrayList<UhcPOI>();
 	private ArrayList<UhcEvent> uhcEvents = new ArrayList<UhcEvent>();
 	private int locationCheckerTask;
 	private static int PROXIMITY_THRESHOLD_SQUARED = 10000;
 	protected static int PLAYER_DAMAGE_ALERT_TICKS = 80; // 4 seconds
 	protected static int PLAYER_HEAL_ALERT_TICKS = 80; // 4 seconds
 	public static short DURABILITY_PENALTY_GOLD = 1;
 	public static short DURABILITY_PENALTY_WOOD = 2;
 	public static short DURABILITY_PENALTY_STONE = 3;
 	public static short DURABILITY_PENALTY_IRON = 4;
 	public static short DURABILITY_PENALTY_DIAMOND = 5;
 	public static int EVENT_COUNTDOWN_LENGTH = 3; // 3 minute countdown for all match events
 	private HashMap<String, UhcPlayer> allPlayers = new HashMap<String, UhcPlayer>();
 	private UhcConfiguration config;
 	private int borderCheckerTask;
 	private Integer worldRadiusFinal = null;
 	private Integer worldRadius = null;
 	
 	// ALL COLORS. Currently doesn't include white.
 	private static final ChatColor[] COLORS = {ChatColor.BLUE, ChatColor.RED, ChatColor.DARK_GREEN, ChatColor.DARK_PURPLE, ChatColor.YELLOW, ChatColor.GRAY, ChatColor.DARK_GRAY, ChatColor.DARK_AQUA, ChatColor.DARK_RED, ChatColor.LIGHT_PURPLE, ChatColor.GREEN, ChatColor.BLACK, ChatColor.BLUE, ChatColor.AQUA, ChatColor.GOLD};
 	
 	public UhcMatch(TesseractUHC plugin, World startingWorld, Configuration defaults) {
 
 		this.startingWorld = startingWorld;
 		this.plugin = plugin;
 		this.server = plugin.getServer();
 		this.config = new UhcConfiguration(this, defaults);
 		
 		this.setPermaday(true);
 		this.setPVP(false);
 		this.setVanish();
 		this.enableSpawnKeeper();
 		this.setWorldBorder(config.getWorldBorder());
 		
 	}
 	
 
 	public UhcConfiguration getConfig() {
 		return this.config;
 	}
 	
 	
 	/**
 	 * Send a message to all spectators
 	 * 
 	 * @param string The message to be sent
 	 */
 	public void spectatorBroadcast(String string) {
 		for(UhcPlayer up : getOnlinePlayers()) {
 			if (up.isSpectator())
 				up.sendMessage(string);
 		}
 	}
 	
 	/**
 	 * Send a message to all ops
 	 * 
 	 * @param string The message to be sent
 	 */
 	public void adminBroadcast(String string) {
 		broadcast(string,Server.BROADCAST_CHANNEL_ADMINISTRATIVE);
 	}
 	
 	/**
 	 * Send a message to all players on the server
 	 * 
 	 * @param string The message to be sent
 	 */
 	public void broadcast(String string) {
 		broadcast(string,Server.BROADCAST_CHANNEL_USERS);
 	}
 	/**
 	 * Send a message to all players on a certain team
 	 * 
 	 * @param string The message to be sent
 	 * @param UhcTeam the team to be broadcast too
 	 */
 	public void broadcastTeam(String message, UhcTeam team){
 		for(UhcParticipant up: team.getMembers()){
 			up.sendMessage(message);
 		}
 	}
 
 	/**
 	 * Send a message to specific players on the server
 	 * 
 	 * @param string The message to be sent
 	 * @param permission The permission level to send the message to
 	 */
 	private void broadcast(String string, String permission) {
 		server.broadcast(string, permission);
 	}
 	
 	/**
 	 * Set time to midday, to keep permaday in effect.
 	 */
 	private void keepPermaday() {
 		this.startingWorld.setTime(6000);
 	}
 
 	/**
 	 * Enables / disables PVP on overworld
 	 * 
 	 * @param pvp Whether PVP is to be allowed
 	 */
 	public void setPVP(boolean pvp) {
 		this.pvp = pvp;
 		startingWorld.setPVP(pvp);
 
 		adminBroadcast(TesseractUHC.OK_COLOR + "PVP has been " + (pvp ? "enabled" : "disabled") + "!");
 	
 	}
 	
 	/**
 	 * @return Whether PVP is enabled
 	 */
 	public boolean getPVP() {
 		return this.pvp;
 	}
 
 	/**
 	 * Enables / disables permaday
 	 * 
 	 * @param p whether permaday is to be on or off
 	 */
 	public void setPermaday(boolean p) {
 		if (p == permaday) return;
 		
 		this.permaday=p;
 	
 		adminBroadcast(TesseractUHC.OK_COLOR + "Permaday has been " + (permaday ? "enabled" : "disabled") + "!");
 		
 		
 		if (permaday) {
 			startingWorld.setTime(6000);
 			permadayTaskId = server.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
 				public void run() {
 					keepPermaday();
 				}
 			}, 1200L, 1200L);
 			
 		} else {
 			server.getScheduler().cancelTask(permadayTaskId);
 		}
 	}
 	
 
 	/**
 	 * @return Whether permaday is enabled
 	 */
 	public boolean getPermaday() {
 		return this.permaday;
 	}
 
 
 
 	/**
 	 * Try to find a start point from a user-provided search string.
 	 * 
 	 * @param searchParam The string to search for - a player name, or a start number may be sent
 	 * @return The start point, or null if not found.
 	 */
 	public UhcStartPoint findStartPoint(String searchParam) {
 		UhcParticipant up = this.getMatchParticipant(searchParam);
 		if (up != null) {
 			// Argument matches a participant
 			return up.getStartPoint();
 			
 		} else {
 			try {
 				int i = Integer.parseInt(searchParam);
 				return startPoints.get(i);
 			} catch (Exception e) {
 				return null;
 			}
 		}
 		
 	}
 
 	private UhcParticipant getMatchParticipant(String name) {
 		UhcPlayer pl = getExistingPlayer(name);
 		if (pl != null)
 			if (pl.isParticipant())
 				return pl.getParticipant();
 		
 		return null;
 	}
 
 
 	/**
 	 * Set a death location for teleporters
 	 * 
 	 * @param l The location to be stored
 	 */
 	public void setLastDeathLocation(Location l) {
 		// For void deaths, increase y to 0;
 		if (l.getY() < 0) l.setY(0);
 		lastDeathLocation = l;
 		lastEventLocation = l;
 	}
 
 	/**
 	 * Set a notification location for teleporters
 	 * 
 	 * @param l The location to be stored
 	 */
 	private void setLastNotifierLocation(Location l) {
 		lastNotifierLocation = l;
 		lastEventLocation = l;
 	}
 
 	/**
 	 * Set a logout location for teleporters
 	 * 
 	 * @param l The location to be stored
 	 */
 	public void setLastLogoutLocation(Location l) {
 		lastLogoutLocation = l;
 	}
 	
 
 	
 	/**
 	 * Remove all hostile mobs in the overworld
 	 */
 	public void butcherHostile() {
 		for (Entity entity : startingWorld.getEntitiesByClass(LivingEntity.class)) {
 			if (entity instanceof Monster || entity instanceof MagmaCube || entity instanceof Slime || entity instanceof EnderDragon
 					|| entity instanceof Ghast)
 				entity.remove();
 		}
 	}
 
 	
 	/**
 	 * Start the match
 	 * 
 	 * Butcher hostile mobs, turn off permaday, turn on PVP, put all participants in survival and reset all participants.
 	 */
 	public void startMatch() {
 		// Remove participants who didn't turn up
 		if (config.isNoLatecomers())
 			for (UhcPlayer up : this.allPlayers.values())
 				if (up.isParticipant() && !up.getParticipant().isLaunched())
 					this.removeParticipant(up.getName());
 
 		this.matchCountdown = null;
 		matchPhase = MatchPhase.MATCH;
 		startingWorld.setTime(0);
 		butcherHostile();
 		server.setSpawnRadius(0);
 		startingWorld.setDifficulty(Difficulty.HARD);
 		for (UhcParticipant up : this.getUhcParticipants()) up.start();
 		setPermaday(false);
 		setVanish();
 		broadcast("GO!");
 		
 		// Set up pvp countdown
 		if (config.getNopvp() > 0) {
 			new PVPCountdown(config.getNopvp(), plugin, this, true);
 		} else {
 			setPVP(true);
 		}
 		enableLocationChecker();
 		enableBorderChecker();
 		enableMatchTimer();
 		updatePlayerListCompletely();
 		server.getPluginManager().callEvent(new UhcMatchStartEvent(this, startingWorld.getSpawnLocation()));
 
 	}
 	
 	/**
 	 * End the match
 	 * 
 	 * Announce the total match duration
 	 */
 	public void endMatch() {
 		server.getPluginManager().callEvent(new UhcMatchEndEvent(this, startingWorld.getSpawnLocation()));
 		broadcast(matchTimeAnnouncement(true));
 		disableMatchTimer();
 		matchPhase = MatchPhase.POST_MATCH;
 		// Put all players into creative
 		for (UhcPlayer pl : getOnlinePlayers()) pl.setGameMode(GameMode.CREATIVE);
 		setVanish();
 		disableLocationChecker();
 		disableBorderChecker();
 
 	}
 	
 
 	public void setWorldBorder(Integer nextRadius) {
 		if (nextRadius==null || nextRadius == 0) {
 			worldRadiusFinal = null;
 			worldRadius = null;
 			return;
 		}
 		
 		worldRadiusFinal = nextRadius;
 		if (worldRadiusFinal >= 100) {
 			worldRadius = worldRadiusFinal-25;
 		} else {
 			worldRadius = (int) (worldRadiusFinal * 0.9);
 		}
 			
 
 	}
 	
 	
 	/**
 	 * Starts the match timer
 	 */
 	private void enableMatchTimer() {
 		matchStartTime = Calendar.getInstance();
 		
 		// Immediately do the first one
 		doScheduledTasks();
 		
 		// Repeat every 20 seconds
 		matchTimer = server.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
 			public void run() {
 				doScheduledTasks();
 			}
 		}, 420L, 400L);
 	}
 	
 	/**
 	 * Stops the match timer
 	 */
 	private void disableMatchTimer() {
 		if (matchTimer != -1) {
 			server.getScheduler().cancelTask(matchTimer);
 		}
 	}
 	
 	/**
 	 * Get the current match time in seconds
 	 * 
 	 * @return Time since the match started, in seconds
 	 */
 	public long getMatchTime() {
 		if (matchStartTime == null) return 0;
 		return MatchUtils.getDuration(matchStartTime, Calendar.getInstance());
 	}
 	
 	/**
 	 * Display the current match time if it is a multiple of 30.
 	 */
 	private void doScheduledTasks() {
 		// Get current match time
 		long matchTime = getMatchTime() / 60;
 		
 		// Make a match time announcement if necessary
 		if (config.getAnnouncementinterval() > 0) {
 			if (matchTime % config.getAnnouncementinterval() == 0 && matchTime > this.lastMatchTimeAnnouncement) {
 				broadcast(matchTimeAnnouncement(false));
 				this.lastMatchTimeAnnouncement = matchTime;
 			}
 		}
 		
 		// Process any UhcEvents that are due
 		for(UhcEvent e : this.uhcEvents) {
 			if (!e.isHandled() && e.getTime() <= matchTime + e.getCountdownLength()) {
 				e.startCountdown((int)(e.getTime() - matchTime));
 			}
 				
 		}
 	}
 
 
 	/**
 	 * Plays a chat script
 	 * 
 	 * @param filename The file to read the chat script from
 	 * @param muteChat Whether other chat should be muted
 	 */
 	public void playChatScript(String filename, boolean muteChat) {
 		if (muteChat) this.setChatMuted(true);
 		chatScript = FileUtils.readFile(filename);
 		if (chatScript != null)
 			continueChatScript();
 	}
 	
 	/**
 	 * Output next line of current chat script, unmuting the chat if it's finished.
 	 */
 	private void continueChatScript() {
 		broadcast(ChatColor.GREEN + chatScript.get(0));
 		chatScript.remove(0);
 		if (chatScript.size() > 0) {
 			server.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
 				public void run() {
 					continueChatScript();
 				}
 			}, 30L);
 		} else {
 			this.setChatMuted(false);
 			chatScript = null;
 		}
 		
 	}
 	
 	/**
 	 * Get all participants currently registered with the game
 	 * 
 	 * @return All registered participants
 	 */
 	private Collection<UhcParticipant> getUhcParticipants() {
 		ArrayList<UhcParticipant> participants = new ArrayList<UhcParticipant>();
 		
 		for (UhcTeam team : this.getTeams())
 			for (UhcParticipant up : team.getMembers())
 				participants.add(up);
 
 		return participants;
 	}
 	
 	private UhcTeam createTeam(String identifier, String name, UhcStartPoint startPoint) {
 		// Fail if team exists
 		if (existsTeam(identifier)) return null;
 		
 		UhcTeam team = new UhcTeam(identifier, name, startPoint,COLORS[uhcTeams.size() % COLORS.length]);
 		uhcTeams.put(identifier.toLowerCase(), team);
 		return team;
 	}
 	
 	/**
 	 * Create a new participant and add them to the game
 	 * 
 	 * @param pl The participant's UhcPlayer object
 	 * @param sp The participant's start point
 	 * @return The newly created participant, or null if they already existed
 	 */
 	private UhcParticipant createParticipant(UhcPlayer pl, UhcTeam team) {
 		// Fail if player exists
 		if (pl.isParticipant()) return null;
 		
 		UhcParticipant up = new UhcParticipant(pl, team);
 		pl.setParticipant(up);
 		team.addMember(up);
 		return up;
 	}
 	
 
 	
 	
 	
 	/**
 	 * Check if a team exists
 	 * 
 	 * @param identifier The team identifier to check (case-insensitive)
 	 * @return Whether the team exists
 	 */
 	public boolean existsTeam(String identifier) {
 		return uhcTeams.containsKey(identifier.toLowerCase());
 	}
 
 	/** 
 	 * Compares a passed name to existing team names
 	 * 
 	 * @param name of proposed team
 	 * @return true if an existing team has passed team else false
 	 */
 	public boolean existsTeamByName(String name) {
 		for(UhcTeam team : this.getTeams()){
 			if(team.getName().equals(name))
 				return true;
 		}
 		return false;
 	}
 
 
 	/**
 	 * Get a specific UhcTeam by identifier
 	 * 
 	 * @param name The exact name of the player to be found  (case insensitive)
 	 * @return The UhcPlayer, or null if not found
 	 */
 	public UhcTeam getTeam(String identifier) {
 		return uhcTeams.get(identifier.toLowerCase());
 	}
 	
 
 	public UhcParticipant getParticipantByIndex(int index) {
 		return participantsInMatch.get(index);
 	}
 	
 	/**
 	 * Add the team as detailed, and assign them a start point
 	 * 
 	 * @param identifier The team's short identifier
 	 * @param name The full name of the team
 	 * @return success or failure
 	 */
 	public boolean addTeam(String identifier, String name) {
 		// Check that there are available start points
 		if (!roomForAnotherTeam()) return false;
 		
 		// Check that the team doesn't exist already 
 		if (existsTeam(identifier)) return false;
 		
 		// Get them a start point
 		Random rand = new Random();
 		UhcStartPoint start = availableStartPoints.remove(rand.nextInt(availableStartPoints.size()));
 		
 		// Create the team
 		UhcTeam team = createTeam(identifier, name, start);
 		start.setTeam(team);
 		teamsInMatch.add(team);
 		
 		// Send update to the spectators
 		if (getConfig() != null && !getConfig().isFFA()){
 			PluginChannelUtils.messageSpectators("team", name, "init");
 			PluginChannelUtils.messageSpectators("team", name, "color", team.getColor().toString());
 		}
 		
 		return true;
 	}
 	
 	/**
 	 * Checks if there is room for another team to be created
 	 * 
 	 * @return Whether there is another start point
 	 */
 	public boolean roomForAnotherTeam() {
 		return (availableStartPoints.size()>0);
 	}
 
 	/**
 	 * Create a participant and add them to the specified team
 	 * 
 	 * @param name The name of the player to add
 	 * @param teamIdentifier The team to add them to
 	 * @return success or failure
 	 */
 	public boolean addParticipant(UhcPlayer pl, String teamIdentifier) {
 		// If player is op, fail
 		if (pl.isAdmin()) return false;
 		
 		// If player is already a participant, fail
 		if (pl.isParticipant()) return false;
 		
 		// If the player has the autoreferee-client mod, fail
 		if (pl.getAutoRefereeClientEnabled()) {
 			this.spectatorBroadcast(pl.getDisplayName() + ChatColor.DARK_GRAY + " attempted to log in with a modified client!");
 			pl.sendMessage(TesseractUHC.WARN_COLOR + " you attempted to join with a modified client mod.");
 			return false;
 		}
 		
 		// Get the team
 		UhcTeam team = getTeam(teamIdentifier);
 		
 		// If team doesn't exist, fail
 		if (team == null) return false;
 		
 		// If player is a spectator, make them not one
 		if (pl.isSpectator()) pl.makeNotSpectator();
 		
 		// Create the player
 		UhcParticipant up = createParticipant(pl, team);
 
 		// If player wasn't created, fail
 		if (up == null) return false;
 		
 		// Message client mod that player joined team
 		if (getConfig() != null) // Don't do this if we are still busy constructing match and config
 			PluginChannelUtils.messageSpectators("team", team.getName(), "player", "+" + up.getName());
 		
 		participantsInMatch.add(up);
 		return true;
 	}
 	
 	/**
 	 * Add the supplied player as a team of one, creating the team and assigning it a start point
 	 * 
 	 * @param name The name of the player to add
 	 * @return success or failure
 	 */
 	public boolean addSoloParticipant (UhcPlayer pl) {
 		// If player is op, fail
 		if (pl.isAdmin()) return false;
 		
 		// If player is already a participant, fail
 		if (pl.isParticipant()) return false;
 		
 		// If the player has the autoreferee-client mod, fail
 		if (pl.getAutoRefereeClientEnabled()) {
 			this.spectatorBroadcast(pl.getDisplayName() + ChatColor.DARK_GRAY + " attempted to log in with a modified client!");
 			pl.sendMessage(TesseractUHC.WARN_COLOR + " you attempted to join with a modified client mod.");
 			return false;
 		}
 		
 		// If player is a spectator, make them not one
 		if (pl.isSpectator()) pl.makeNotSpectator();
 		
 		// Create a team of one for the player
 		String teamName = pl.getName(); 
 		if (!addTeam(teamName, teamName)) return false;
 		
 		// Add the new player to the team of one, and return the result
 		return addParticipant(pl, teamName);
 		
 	}
 	
 	
 	/**
 	 * Launch the specified player only
 	 * 
 	 * @param p The UhcParticipant to be launched
 	 * @return success or failure
 	 */
 	public boolean launch(UhcPlayer pl) {
 
 		if (!pl.isParticipant()) return false;
 		
 		UhcParticipant up = pl.getParticipant();
 		
 		// If player already launched, ignore
 		if (up.isLaunched()) return false;
 		
 		// Get the player
 		Player p = server.getPlayerExact(up.getName());
 		
 		// If player not online, return
 		if (p == null) return false;
 		
 		
 		up.sendToStartPoint();
 		
 		up.setLaunched(true);
 		up.sendMessage(ChatColor.GOLD + "This is " + ChatColor.ITALIC + config.getMatchTitle() + "\n" 
 				+ ChatColor.RESET + ChatColor.AQUA + "To find out the parameters for this game, type " + ChatColor.GOLD + "/params" + "\n"
 				+ ChatColor.AQUA + "To view the match status at any time, type " + ChatColor.GOLD + "/match");
 
 		// Trigger a join event
 		server.getPluginManager().callEvent(new UhcJoinEvent(this, up.getStartPoint().getLocation(), p));
 
 		return true;
 
 
 		
 	}
 	
 
 	
 	/**
 	 * Remove the given player, removing them from the match, and their team.
 	 * 
 	 * The player will be teleported back to spawn if they are still on the server
 	 * 
 	 * @param name The player to be removed
 	 * @return Whether the removal succeeded
 	 */
 	public boolean removeParticipant(String name) {
 		UhcPlayer pl = getPlayer(name);
 		
 		if (pl.isParticipant()) {
 			
 			// Remove them from their team
 			UhcTeam team = pl.getParticipant().getTeam();
 			team.removeMember(pl.getParticipant());
 						
 			// Mark them as a non participant
 			getPlayer(name).setParticipant(null);
 			
 			// Message client mod that player left team
 			PluginChannelUtils.messageSpectators("team", team.getName(), "player", "-" + pl.getName());
 			
 			// Remove them from the match
 			participantsInMatch.remove(pl.getParticipant());
 			
 			// Remove team if empty
 			if(team.getMembers().size() == 0){
 				removeTeam(team.getIdentifier());	
 				if (matchPhase == MatchPhase.MATCH && !config.isFFA()) {
 					broadcast(ChatColor.GOLD + team.getName() + " now has no members.");
 				}
 			}
 			
 			
 			if (matchPhase == MatchPhase.MATCH) {
 				broadcast(ChatColor.GOLD + pl.getName() + " has left the match");
 				broadcastMatchStatus();
 			}
 			pl.teleport(startingWorld.getSpawnLocation());
 			
 			getPlayer(name).makeSpectator();
 			return true;
 		} else return false;
 	}
 	
 	/**
 	 * Remove the given team, which must be empty, from the match, and free up its start point.
 	 * 
 	 * @param identifier The team to remove
 	 * @return Whether the removal succeeded
 	 */
 	public boolean removeTeam(String identifier) {
 		UhcTeam team = getTeam(identifier);
 		
 		// If team not found, fail
 		if (team == null) return false;
 		
 		// If team not empty, fail
 		if (team.playerCount()>0) return false;
 		
 		uhcTeams.remove(identifier.toLowerCase());
 		teamsInMatch.remove(team);
 		
 		// Free up the start point
 		UhcStartPoint sp = team.getStartPoint();
 		sp.setTeam(null);
 		sp.makeSign();
 		sp.emptyChest();
 		availableStartPoints.add(sp);
 		
 		// Message client mod that player left team
 		PluginChannelUtils.messageSpectators("team", team.getName(), "destroy");
 		
 		return true;
 	}
 
 
 	/**
 	 * Pre-warn 2 minutes before the match is due to begin, and inform players who haven't joined yet that
 	 * they have 30 seconds
 	 */
 	public void preWarn() {
 		// For all players on the server, check if they are joined up
 		for(Player p : server.getOnlinePlayers())
 			if (!this.getPlayer(p).isSpectator() && !this.getPlayer(p).isParticipant())
 				p.sendMessage(ChatColor.GOLD + "Joining the match will be disabled in 30 seconds. If you want to play, use /join.");
 		
 	}
 	/**
 	 * Start the launching phase, and launch all players who have been added to the game
 	 */
 	public void launchAll() {
 		// If already launched, do nothing.
 		if (matchPhase != MatchPhase.PRE_MATCH) return;
 		
 		matchPhase = MatchPhase.LAUNCHING;
 		disableSpawnKeeper();
 		if (config.isUHC()) setupModifiedRecipes();
 		setVanish(); // Update vanish status
 		butcherHostile();
 		sortParticipantsInMatch();
 		
 		// Fill bonus chests
 		for (UhcTeam team : getTeams()) {
 			team.getStartPoint().makeSign();
 			team.getStartPoint().fillChest(config.getBonusChest());
 		}
 
 		// Add all players to the launch queue
 		for(UhcParticipant up : getUhcParticipants())
 			if (!up.isLaunched()) addToLaunchQueue(up);
 		
 		// Make all others spectators
 		for(UhcPlayer pl : getOnlinePlayers())
 			if (!pl.isParticipant())
 				pl.makeSpectator();
 		
 
 		// Begin launching
 		launchNext();
 	}
 	
 
 	private void sortParticipantsInMatch() {
 		// Refill the playersInMatch arraylist, sorting players into order.
 		participantsInMatch.clear();
 		for (UhcTeam team : teamsInMatch)
 			for (UhcParticipant up : team.getMembers())
 				participantsInMatch.add(up);
 		
 		
 		
 	}
 
 
 	private void launchNext() {
 		if (this.launchQueue.size()==0) {
 			adminBroadcast(TesseractUHC.OK_COLOR + "Launching complete");
 			return;
 		}
 		
 		String playerName = this.launchQueue.remove(0);
 		UhcPlayer up = this.getPlayer(playerName);
 		launch(up);
 		
 		server.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
 			public void run() {
 				launchNext();
 			}
 		}, 20L);
 		
 	}
 
 	private void addToLaunchQueue(UhcParticipant up) {
 		this.launchQueue.add(up.getName().toLowerCase());
 	}
 	
 	
 	public void schedulePlayerListUpdate(final UhcPlayer pl) {
 		server.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
 			public void run() {
 				pl.updatePlayerListName();
 			}
 		});
 	}
 	
 	public void updatePlayerListCompletely() {
 		for (UhcPlayer pl : this.getOnlinePlayers()) {
 			pl.updatePlayerListName();
 		}
 	}
 	
 
 	private void enableSpawnKeeper() {
 		spawnKeeperTask = server.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
 			public void run() {
 				runSpawnKeeper();
 			}
 		}, 40L, 40L);
 	}
 
 	private void disableSpawnKeeper() {
 		server.getScheduler().cancelTask(spawnKeeperTask);
 	}
 	
 	private void runSpawnKeeper() {
 		boolean keep = (startingWorld.getSpawnLocation().getBlockY() >= 128);
 		for (UhcPlayer pl : getOnlinePlayers()) {
 			if (keep && pl.getLocation().getY() < 128 && !pl.isSpectator()) {
 				pl.teleport(startingWorld.getSpawnLocation(), null);
 			}
 			pl.heal();
 			pl.feed();
 		}
 	}
 	
 	private void enableBorderChecker() {
 		borderCheckerTask = server.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
 			public void run() {
 				runBorderChecker();
 			}
 		}, 10L, 10L);
 	}
 
 	private void disableBorderChecker() {
 		server.getScheduler().cancelTask(borderCheckerTask);
 	}
 	
 	private void runBorderChecker() {
 		// If there is no world border, do nothing
 		if (worldRadiusFinal == null) return;
 		
 		// Cycle through all UhcPlayers, checking their location
 		for (UhcParticipant up : participantsInMatch) {
 			if (!up.getPlayer().isOnline()) continue;
 			if (up.isOutsideBorder(worldRadius)) {
 				if (up.getPlayer().getLocation().getWorld().equals(startingWorld)
 						&& up.isOutsideBorder(worldRadiusFinal)) {
 					
 					// Player is outside the hard world boundary in the overworld, TP them back
 					Location l = up.getPlayer().getLocation();
 
 					double newX = l.getX();
 					double newZ = l.getZ();
 					if (newX > worldRadiusFinal) newX = worldRadius;
 					if (newZ > worldRadiusFinal) newZ = worldRadius;
 					if (newX < -worldRadiusFinal) newX = -worldRadius;
 					if (newZ < -worldRadiusFinal) newZ = -worldRadius;
 					
 					Location l2 = l.clone();
 					l2.setX(newX);
 					l2.setZ(newZ);
 					l2.setY(l2.getWorld().getHighestBlockYAt(l2));
 					up.getPlayer().teleport(l2);
 					up.clearWorldEdgeWarning();
 				} else {
 					up.doWorldEdgeWarning();
 				}
 			} else {
 				up.clearWorldEdgeWarning();
 			}
 		}
 	}
 	
 
 
 	/**
 	 * Send warnings to players who are outside the new border
 	 * @param newRadius
 	 */
 	public void sendBorderWarnings(int newRadius) {
 		if (newRadius == 0) return;
 		
 		// Cycle through all UhcPlayers, checking their location
 		for (UhcParticipant up : participantsInMatch) {
 			if (!up.getPlayer().isOnline()) continue;
 			if (up.isOutsideBorder(newRadius)) {
 				up.sendMessage(ChatColor.YELLOW + "Warning! You are currently OUTSIDE the new world border.");
 			}
 		}
 	}
 	
 	private void enableLocationChecker() {
 		locationCheckerTask = server.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
 			public void run() {
 				runLocationChecker();
 			}
 		}, 200L, 600L);
 	}
 
 	private void disableLocationChecker() {
 		server.getScheduler().cancelTask(locationCheckerTask);
 	}
 	
 	private void runLocationChecker() {
 		// Cycle through all UhcPlayers
 		for (int i = 0; i < participantsInMatch.size(); i++) {
 			UhcParticipant up = participantsInMatch.get(i);
 
 			// Check proximity to other players (only if not on same team).
 			int j = i + 1;
 			while (j < participantsInMatch.size()) {
 				// Check proximity of player up to player j
 				UhcParticipant up2 = participantsInMatch.get(j);
 				if (up.getTeam() != up2.getTeam()) {
 					if (checkProximity(up, up2)) {
 						sendSpectatorNotification(new ProximityNotification(up, up2), server.getPlayerExact(up.getName()).getLocation());
 					}
 				}
 				j++;
 			}
 
 			// Check proximity to all POIs. 
 			for (UhcPOI poi : uhcPOIs) {
 				if (checkProximity(up, poi)) {
 					sendSpectatorNotification(new ProximityNotification(up, poi), server.getPlayerExact(up.getName()).getLocation());
 				}
 			}
 			
 			// Trigger a location event
			Location l = up.getPlayer().getLocation();
			if (l != null) server.getPluginManager().callEvent(new UhcPlayerLocationUpdateEvent(this, l, up.getPlayer().getPlayer()));
 		}
 	}
 	
 	private boolean checkProximity(UhcParticipant player, UhcParticipant enemy) {
 		Player p1 = server.getPlayerExact(player.getName());
 		Player p2 = server.getPlayerExact(enemy.getName());
 		if (p1 == null || p2 == null) return false;
 		
 		if (player.isNearTo(enemy)) {
 			if (p1.getLocation().distanceSquared(p2.getLocation()) >= (PROXIMITY_THRESHOLD_SQUARED))
 				player.setNearTo(enemy, false);
 			return false;
 		} else {
 			if (p1.getLocation().distanceSquared(p2.getLocation()) < (PROXIMITY_THRESHOLD_SQUARED)) {
 				player.setNearTo(enemy, true);
 				return true;
 			}
 			return false;
 		}
 	}
 	
 	private boolean checkProximity(UhcParticipant player, UhcPOI poi) {
 		Player p1 = server.getPlayerExact(player.getName());
 		if (p1 == null) return false;
 		
 		if (player.isNearTo(poi)) {
 			if (p1.getLocation().distanceSquared(poi.getLocation()) >= (PROXIMITY_THRESHOLD_SQUARED))
 				player.setNearTo(poi, false);
 			return false;
 		} else {
 			if (p1.getLocation().distanceSquared(poi.getLocation()) < (PROXIMITY_THRESHOLD_SQUARED)) {
 				player.setNearTo(poi, true);
 				return true;
 			}
 			return false;
 		}
 	}
 	
 	public void clearStartPoints() {
 		startPoints.clear();
 		availableStartPoints.clear();
 	}
 	/**
 	 * Create a new start point at a given location
 	 * 
 	 * @param number The start point's number
 	 * @param l The location of the start point
 	 * @param buildTrough Whether to add a starting trough
 	 * @return The created start point
 	 */
 	private UhcStartPoint createStartPoint(int number, Location l, Boolean large, Boolean buildTrough) {
 		// Check there is not already a start point with this number		
 		if (startPoints.containsKey(number))
 			return null;
 		
 		UhcStartPoint sp;
 		if (large)
 			sp = new LargeGlassStartPoint(number, l, true);
 		else
 			sp = new SmallGlassStartPoint(number, l, true);
 		
 		if (buildTrough) sp.buildStartingTrough();
 		
 		startPoints.put(number,  sp);
 		availableStartPoints.add(sp);
 		
 		return sp;
 	}
 	
 	/**
 	 * Create a new start point at a given location, with optional starting trough
 	 * 
 	 * @param number The start point's number
 	 * @param world The world to create the start point
 	 * @param x x coordinate of the start point
 	 * @param y y coordinate of the start point
 	 * @param z z coordinate of the start point
 	 * @param buildTrough Whether to add a starting trough
 	 * @return The created start point
 	 */
 	public UhcStartPoint createStartPoint(int number, World world, Double x, Double y, Double z, Boolean large, Boolean buildTrough) {
 		return createStartPoint(number, new Location(world, x, y, z), large, buildTrough);
 	}
 	
 	/**
 	 * Create a new start point at a given location, giving it the next available number
 	 * 
 	 * @param l The location of the start point
 	 * @param buildTrough Whether to add a starting trough
 	 * @return The created start point
 	 */
 	private UhcStartPoint createStartPoint(Location l, Boolean large, Boolean buildTrough) {
 		return createStartPoint(getNextAvailableStartNumber(), l, large, buildTrough);
 	}
 
 	/**
 	 * Add a new start point at a given location, giving it the next available number.
 	 * 
 	 * This function will also update the saved match data.
 	 * 
 	 * @param x x coordinate of the start point
 	 * @param y y coordinate of the start point
 	 * @param z z coordinate of the start point
 	 * @param buildTrough Whether to add a starting trough
 	 * @return The created start point
 	 */
 	public UhcStartPoint addStartPoint(Double x, Double y, Double z, Boolean buildTrough) {
 		UhcStartPoint sp = createStartPoint(new Location(startingWorld, x, y, z), !config.isFFA(), buildTrough);
 		if (sp != null) config.saveMatchParameters();
 		return sp;
 	}
 	
 		
 	/**
 	 * Determine the lowest unused start number
 	 * 
 	 * @return The lowest available start point number
 	 */
 	public int getNextAvailableStartNumber() {
 		int n = 1;
 		while (startPoints.containsKey(n))
 			n++;
 		return n;
 	}
 	
 
 	
 
 	
 	/**
 	 * @return Whether chat is currently muted
 	 */
 	public boolean isChatMuted() {
 		return chatMuted;
 	}
 	
 	/**
 	 * Mute or unmute chat
 	 * 
 	 * @param muted Status to be set
 	 */
 	public void setChatMuted(Boolean muted) {
 		chatMuted = muted;
 	}
 
 
 	/**
 	 * @return The number of players still in the match
 	 */
 	public int countParticipantsInMatch() {
 		return participantsInMatch.size();
 	}
 
 
 	/**
 	 * @return The number of teams still in the match
 	 */
 	public int countTeamsInMatch() {
 		return teamsInMatch.size();
 	}
 
 	public void placeHeadDelayed(final Location l, final String name) {
 		server.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
 			public void run() {
 				placeHead(l,name);
 			}
 		});
 	}
 	
 	public void placeHead(Location l, String name) {
 		Block b = l.getBlock();
 		b.setTypeIdAndData(Material.SKULL.getId(), (byte) 1, true);
 		
 		Skull s = (Skull) b.getState();
 		s.setSkullType(SkullType.PLAYER);
 		s.setRotation(MatchUtils.getBlockFaceDirection(l));
 		s.setOwner(name);
 		s.update(true);
 
 	}
 	
 	public void handleParticipantDeath(final UhcParticipant up) {
 		server.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
 			public void run() {
 				processParticipantDeath(up);
 			}
 		});
 		
 	}
 
 	public void handleDragonKill(final UhcParticipant killer) {
 		server.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
 			public void run() {
 				processDragonKill(killer);
 			}
 		});
 		
 	}
 
 	public void handleEliminatedPlayer(final UhcPlayer p) {
 		server.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
 			public void run() {
 				p.makeSpectator();
 			}
 		});
 
 	}
 
 	/**
 	 * Process the death of a player
 	 * 
 	 * @param up The player who died
 	 */
 	private void processParticipantDeath(UhcParticipant up) {
 		// Set them as dead
 		up.setDead(true);
 		
 		// Reduce survivor counts
 		participantsInMatch.remove(up);
 
 		if (!config.isDragonMode() && config.isFFA() && countParticipantsInMatch() == 1) {
 			processVictory(participantsInMatch.get(0));
 			return;
 		}
 			
 			
 		UhcTeam team = up.getTeam();
 		
 		if (team != null && team.aliveCount()<1) {
 			teamsInMatch.remove(team);
 			if (!config.isDragonMode() && !config.isFFA() && countTeamsInMatch() == 1) {
 				processVictory(teamsInMatch.get(0));
 				return;
 			}
 		}
 		
 		if (countParticipantsInMatch() == 0) endMatch();
 		
 		broadcastMatchStatus();
 	}
 
 	private void processDragonKill(UhcParticipant winner) {
 		if (config.isFFA()) {
 			broadcast(ChatColor.GOLD + "The winner is: " + winner.getName() + "!");
 		} else {
 			broadcast(ChatColor.GOLD + "The winner is: " + winner.getTeam().getName() + "!");
 		}
 		endMatch();
 	}
 	
 	
 	private void processVictory(UhcTeam winner) {
 		broadcast(ChatColor.GOLD + "The winner is: " + winner.getName() + "!");
 		endMatch();
 		
 	}
 
 	private void processVictory(UhcParticipant winner) {
 		broadcast(ChatColor.GOLD + "The winner is: " + winner.getName() + "!");
 		endMatch();
 		
 	}
 
 	/**
 	 * Publicly announce how many players are still in the match 
 	 */
 	private void broadcastMatchStatus() {
 		// Make no announcement if match has ended
 		if (matchPhase != MatchPhase.POST_MATCH)
 			broadcast(matchStatusAnnouncement());
 		
 	}
 
 	/**
 	 * Get the text of a match time announcement
 	 * 
 	 * @param precise Whether to give a precise time (00:00:00) instead of (xx minutes)
 	 * @return Current match time as a nicely-formatted string
 	 */
 	public String matchTimeAnnouncement(boolean precise) {
 		if (matchPhase == MatchPhase.PRE_MATCH || matchPhase == MatchPhase.LAUNCHING)
 			return TesseractUHC.MAIN_COLOR + "Match time: " + TesseractUHC.SIDE_COLOR + MatchUtils.formatDuration(0, precise);
 		else
 			return TesseractUHC.MAIN_COLOR + "Match time: " + TesseractUHC.SIDE_COLOR + MatchUtils.formatDuration(matchStartTime, Calendar.getInstance(), precise);
 
 	}
 	/**
 	 * Get the text of a match status announcement
 	 * 
 	 * @return List of remaining players / teams
 	 */
 	public String matchStatusAnnouncement() {
 		if (this.matchPhase == MatchPhase.PRE_MATCH) {
 			if (config.isFFA()) {
 				int c = countParticipantsInMatch();
 				return c + " player" + (c != 1 ? "s have" : " has") + " joined";
 			} else {
 				int c = countTeamsInMatch();
 				return c + " team" + (c != 1 ? "s have" : " has") + " joined";
 			}
 		}
 		if (config.isFFA()) {
 			int c = countParticipantsInMatch();
 			if (c == 0)
 				return "There are no surviving players";
 			if (c == 1)
 				return "1 surviving player: " + participantsInMatch.get(0).getName();
 			
 			if (c <= 4) {
 				String message = c + " surviving players: ";
 				for (UhcParticipant up : participantsInMatch)
 					message += up.getName() + ", ";
 				
 				return message.substring(0, message.length()-2);
 			}
 			
 			return c + " surviving players";
 
 		} else {
 			int c = countTeamsInMatch();
 			if (c == 0)
 				return "There are no surviving teams";
 			if (c == 1)
 				return "1 surviving team: " + teamsInMatch.get(0).getName();
 			
 			if (c <= 4) {
 				String message = c + " surviving teams: ";
 				for (UhcTeam t : teamsInMatch)
 					message += t.getName() + ", ";
 				
 				return message.substring(0, message.length()-2);
 			}
 			
 			return c + " surviving teams";
 
 		}
 	}
 	
 
 
 
 
 
 
 	
 
 	/**
 	 * Set the correct vanish status for all players on the server
 	 * 
 	 * @param p1
 	 */
 	public void setVanish() {
 		for(UhcPlayer pl : getOnlinePlayers()) pl.setVanish();
 	}
 
 
 
 	public ArrayList<Location> getCalculatedStarts() {
 		return calculatedStarts;
 	}
 
 	public void setCalculatedStarts(ArrayList<Location> calculatedStarts) {
 		this.calculatedStarts = calculatedStarts;
 	}
 
 	public HashMap<Integer, UhcStartPoint> getStartPoints() {
 		return startPoints;
 	}
 
 	public int countAvailableStartPoints() {
 		return availableStartPoints.size();
 	}
 
 	public World getStartingWorld() {
 		return startingWorld;
 	}
 
 	public Location getLastEventLocation() {
 		return lastEventLocation;
 	}
 
 	public void setLastEventLocation(Location lastEventLocation) {
 		this.lastEventLocation = lastEventLocation;
 	}
 
 	public Location getLastNotifierLocation() {
 		return lastNotifierLocation;
 	}
 
 	public Location getLastDeathLocation() {
 		return lastDeathLocation;
 	}
 
 	public Location getLastLogoutLocation() {
 		return lastLogoutLocation;
 	}
 	
 
 
 
 	public MatchPhase getMatchPhase() {
 		return matchPhase;
 	}
 
 	public void sendNotification(UhcNotification n, Location l) {
 		setLastNotifierLocation(l);
 		String message = n.formatForPlayers();
 		if (message != null) this.broadcast(message);
 	}
 	
 	public void sendSpectatorNotification(UhcNotification n, Location l) {
 		setLastNotifierLocation(l);
 		String message = n.formatForStreamers();
 		if (message != null) this.spectatorBroadcast(message);
 
 	}
 
 	public boolean startMatchCountdown(int countLength) {
 		if ((this.matchCountdown == null || !this.matchCountdown.isActive()) && (this.matchPhase == MatchPhase.LAUNCHING || this.matchPhase == MatchPhase.PRE_MATCH)) {
 			this.matchCountdown = new MatchCountdown(countLength, plugin, this);
 			return true;
 		}
 		return false;
 		
 	}
 	
 	public boolean cancelMatchCountdown() {
 		if (this.matchCountdown == null) return false;
 		return matchCountdown.cancel();
 	}
 	
 	public boolean cancelBorderCountdown() {
 		if (this.borderCountdown == null) return false;
 		return borderCountdown.cancel();
 	}
 
 	public boolean startBorderCountdown(int countLength, int newRadius) {
 		if ((this.borderCountdown == null || !this.borderCountdown.isActive()) && this.matchPhase == MatchPhase.MATCH) {
 			this.borderCountdown = new BorderCountdown(countLength, plugin, this, newRadius);
 			return true;
 		}
 		return false;
 	}
 	
 	public boolean startPVPCountdown(int countLength, Boolean newValue) {
 		if ((this.pvpCountdown == null || !this.pvpCountdown.isActive()) && this.matchPhase == MatchPhase.MATCH) {
 			this.pvpCountdown = new PVPCountdown(countLength, plugin, this, newValue);
 			return true;
 		}
 		return false;
 	}
 
 	public boolean cancelPVPCountdown() {
 		if (this.pvpCountdown == null) return false;
 		return pvpCountdown.cancel();
 	}
 	
 	public boolean startPermadayCountdown(int countLength, Boolean newValue) {
 		if ((this.permadayCountdown == null || !this.permadayCountdown.isActive()) && this.matchPhase == MatchPhase.MATCH) {
 			this.permadayCountdown = new PermadayCountdown(countLength, plugin, this, newValue);
 			return true;
 		}
 		return false;
 	}
 	
 	public boolean cancelPermadayCountdown() {
 		if (this.permadayCountdown == null) return false;
 		return permadayCountdown.cancel();
 	}
 	
 	public Collection<UhcTeam> getTeams() {
 		return uhcTeams.values();
 	}
 
 	public boolean clearTeams() {
 		if (matchPhase != MatchPhase.PRE_MATCH) return false;
 		
 		this.uhcTeams.clear();
 		this.teamsInMatch.clear();
 		this.participantsInMatch.clear();
 		return true;
 	}
 
 
 
 	/**
 	 * Modify the relevant recipes for UHC
 	 */
 	public void setupModifiedRecipes() {
 		ItemStack goldenApple = new ItemStack(Material.GOLDEN_APPLE, 1, (short) 0);
 		ItemStack glisteringMelon = new ItemStack(Material.SPECKLED_MELON);
 
 		Iterator<Recipe> recipes = Bukkit.recipeIterator();
 		while(recipes.hasNext()) {
 			Recipe recipe = recipes.next();
 			// Find recipe for golden apple
 			if(recipe.getResult().equals(goldenApple) || recipe.getResult().equals(glisteringMelon))
 				recipes.remove();
 			
 		}
 
 		ShapedRecipe goldenAppleRecipe = new ShapedRecipe(goldenApple);
 		
 		goldenAppleRecipe.shape(new String[]{"GGG", "GAG", "GGG"});
 		goldenAppleRecipe.setIngredient('G', Material.GOLD_INGOT);
 		goldenAppleRecipe.setIngredient('A', Material.APPLE);
 		
 		server.addRecipe(goldenAppleRecipe);
 		
 		ShapelessRecipe glisteringMelonRecipe = new ShapelessRecipe(glisteringMelon);
 		
 		glisteringMelonRecipe.addIngredient(Material.MELON);
 		glisteringMelonRecipe.addIngredient(Material.GOLD_BLOCK);
 				
 		server.addRecipe(glisteringMelonRecipe);
 	}
 
 	public void addPOI(Location location, String name) {
 		uhcPOIs.add(new UhcPOI(location, name));
 		config.saveMatchParameters();
 	}
 	public void addPOI(String world, double x, double y, double z, String name) {
 		addPOI(new Location(server.getWorld(world), x, y, z), name);
 	}
 
 	public ArrayList<UhcPOI> getPOIs() {
 		return uhcPOIs;
 	}
 
 	public void clearPOIs() {
 		uhcPOIs.clear();
 	}
 	
 	public ArrayList<UhcEvent> getEvents() {
 		return uhcEvents;
 	}
 	
 	public void addEvent(UhcEvent event) {
 		uhcEvents.add(event);
 	}
 	
 	public void clearEvents() {
 		uhcEvents.clear();
 	}
 
 
 	public String getPlayerStatusReport() {
 		String response = "";
 		if (config.isFFA()) {
 			Collection<UhcParticipant> allPlayers = getUhcParticipants();
 			response += allPlayers.size() + " players (" + countParticipantsInMatch() + " still alive):\n";
 			
 			for (UhcParticipant up : allPlayers) {
 				String health;
 				
 				Player p = server.getPlayerExact(up.getName());
 				if (up.isDead()) {
 					health = ChatColor.RED + "(dead)";
 				} else {
 					if (p != null)
 						health = ChatColor.GOLD + "(" + Double.toString(p.getHealth() / 2.0) + ")";
 					else
 						health = ChatColor.GRAY + "(offline)";
 				}
 				response += (up.isDead() ? ChatColor.RED : ChatColor.GREEN)
 						+ "    " + up.getName() + " " + health + "\n";
 			}
 
 		} else {
 			Collection<UhcTeam> allTeams = getTeams();
 			response = ChatColor.GOLD + "" + allTeams.size() + " teams (" + countTeamsInMatch() + " still alive):\n";
 			
 			for (UhcTeam team : allTeams) {
 				response += (team.aliveCount()==0 ? ChatColor.RED + "[D] " : ChatColor.GREEN) + "" +
 						ChatColor.ITALIC + team.getName() + ChatColor.GRAY +
 						" [" + team.getIdentifier() + "]\n";
 				for (UhcParticipant up : team.getMembers()) {
 					String health;
 					
 					Player p = server.getPlayerExact(up.getName());
 					if (up.isDead()) {
 						health = ChatColor.RED + "(dead)";
 					} else {
 						if (p != null)
 							health = ChatColor.GOLD + "(" + Double.toString(p.getHealth() / 2.0) + ")";
 						else
 							health = ChatColor.GRAY + "(offline)";
 					}
 					response += (up.isDead() ? ChatColor.RED : ChatColor.GREEN)
 							+ "    " + up.getName() + " " + health + "\n";
 				}
 			}
 		}
 		
 		
 		return response;
 	}
 
 
 	public Server getServer() { return server; }
 	
 	public UhcPlayer getPlayer(String name) { return this.getPlayer(server.getOfflinePlayer(name)); }
 	public UhcPlayer getPlayer(OfflinePlayer p) {
 		if (p == null) return null;
 		UhcPlayer pl = allPlayers.get(p.getName().toLowerCase());
 		if (pl == null) {
 			pl = new UhcPlayer(p.getName().toLowerCase(), this);
 			allPlayers.put(p.getName().toLowerCase(),  pl);
 		}
 		return pl;
 	}
 	
 	public UhcPlayer getExistingPlayer(String name) { return this.getExistingPlayer(server.getOfflinePlayer(name)); }
 	public UhcPlayer getExistingPlayer(OfflinePlayer p) {
 		return allPlayers.get(p.getName().toLowerCase());
 	}
 	
 	public ArrayList<UhcPlayer> getOnlinePlayers() {
 		ArrayList<UhcPlayer> ups = new ArrayList<UhcPlayer>();
 		for (Player p : server.getOnlinePlayers()) ups.add(getPlayer(p));
 		return ups;
 	}
 	
 	public Calendar getMatchStartTime(){
 		return matchStartTime;
 	}
 	
 	public ArrayList<UhcParticipant> getParticipants(){
 		return participantsInMatch;
 	}
 
 }
