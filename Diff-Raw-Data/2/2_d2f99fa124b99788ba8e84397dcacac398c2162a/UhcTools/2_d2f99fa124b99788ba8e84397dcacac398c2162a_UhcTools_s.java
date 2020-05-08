 package com.martinbrook.uhctools;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import org.bukkit.ChatColor;
 
 import org.bukkit.Chunk;
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.Server;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.block.Chest;
 import org.bukkit.block.Sign;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.EnderDragon;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Ghast;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.MagmaCube;
 import org.bukkit.entity.Monster;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Slime;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.potion.PotionEffect;
 
 public class UhcTools extends JavaPlugin {
 	public Server server;
 	public World world;
 	public static final ChatColor MAIN_COLOR = ChatColor.GREEN, SIDE_COLOR = ChatColor.GOLD, OK_COLOR = ChatColor.GREEN, ERROR_COLOR = ChatColor.RED,
 			DECISION_COLOR = ChatColor.GOLD, ALERT_COLOR = ChatColor.GREEN;
 	private Location lastNotifierLocation;
 	private Location lastDeathLocation;
 	private Location lastEventLocation;
 	private Location lastLogoutLocation;
 	private int countdown = 0;
 	private String countdownEvent;
 	private String countdownEndMessage;
 	private UhcToolsListener l;
 	private ArrayList<String> chatScript;
 	private Boolean chatMuted = false;
 	private CountdownType countdownType;
 	private Boolean permaday = false;
 	private int permadayTaskId;
 	private Boolean deathban = false;
 	private ArrayList<UhcStartPoint> startPoints = new ArrayList<UhcStartPoint>();
 	private int nextStartPoint = 0;
 	private Boolean launchingPlayers = false;
 	private HashMap<String, UhcPlayer> uhcPlayers = new HashMap<String, UhcPlayer>(32);
 	private Boolean killerBonusEnabled = false;
 	private int killerBonusItemID = 0;
 	private int killerBonusItemQuantity = 0;
 	private Boolean miningFatigueEnabled;
 	private int miningFatigueBlocks;
 	private int miningFatigueExhaustion;
 	private int miningFatigueDamage;
 	private int miningFatigueMaxY;
 	private static String DEFAULT_START_POINTS_FILE = "starts.txt";
 	
 	public void onEnable(){
 		l = new UhcToolsListener(this);
 		this.getServer().getPluginManager().registerEvents(l, this);
 		
 		this.server = this.getServer();
 		this.world = getServer().getWorlds().get(0);
 		
 		loadConfigValues();
 		
 		loadStartPoints(); 
 	}
 	
 	public void onDisable(){
 		
 	}
 	
 	private void loadConfigValues() {
 		saveDefaultConfig();
 		killerBonusEnabled = getConfig().getBoolean("killerbonus.enabled");
 		killerBonusItemID = getConfig().getInt("killerbonus.id");
 		killerBonusItemQuantity = getConfig().getInt("killerbonus.quantity");
 		miningFatigueEnabled = getConfig().getBoolean("miningfatigue.enabled");
 		miningFatigueBlocks = getConfig().getInt("miningfatigue.blocks");
 		miningFatigueExhaustion = getConfig().getInt("miningfatigue.exhaustion");
 		miningFatigueDamage = getConfig().getInt("miningfatigue.damage");
 		miningFatigueMaxY = getConfig().getInt("miningfatigue.maxy");
 	}
 
 	@Override
 	public boolean onCommand(CommandSender commandSender, Command command, String commandLabel, String[] args) {
 		boolean success = false;
 		String cmd = command.getName().toLowerCase();
 		
 		if (commandSender.isOp()) {
 		
 			if (commandSender instanceof Player)
 				success = runCommandAsOp((Player) commandSender, cmd, args);
 
 			if (!success)
 				success = runCommandAsConsole(commandSender, cmd, args);
 		} else {
 			if (commandSender instanceof Player)
 				success = runCommandAsPlayer((Player) commandSender, cmd, args);
 		}
 		return true;
 	}
 	
 	private boolean runCommandAsOp(Player sender, String cmd, String[] args) {
 		boolean success = true;
 		String response = null; // Stores any response to be given to the sender
 
 		if(cmd.equals("tp")) {
 			response = cTp(sender,args);
 		} else if (cmd.equals("tpd")) {
 			response = cTpd(sender);
 		} else if (cmd.equals("tpl")) {
 			response = cTpl(sender);
 		} else if (cmd.equals("tpn")) {
 			response = cTpn(sender);
 		} else if(cmd.equals("tpall")) {
 			response = cTpall(sender);
 		} else if(cmd.equals("tp0")) {
 			response = cTp0(sender);
 		} else if(cmd.equals("tps")) {
 			response = cTps(sender, args);
 		} else if (cmd.equals("gm")) {
 			sender.setGameMode((sender.getGameMode() == GameMode.CREATIVE) ? GameMode.SURVIVAL : GameMode.CREATIVE);
 		} else if (cmd.equals("setspawn")) {
 			response = cSetspawn(sender);
 		} else if (cmd.equals("makestart")) {
 			response = cMakestart(sender);
 		} else {
 			success = false;
 		}
 		
 		if (response != null)
 			sender.sendMessage(response);
 
 		return success;
 		
 	}
 	private boolean runCommandAsConsole(CommandSender sender, String cmd, String[] args) {
 		boolean success = true;
 		String response = null; // Stores any response to be given to the sender
 
 		if(cmd.equals("tp")) {
 			response = sTp(args);
 		} else if(cmd.equals("butcher")) {
 			response = cButcher();
 		} else if (cmd.equals("heal")) {
 			response = cHeal(args);
 		} else if (cmd.equals("healall")) {
 			response = cHealall();
 		} else if (cmd.equals("feed")) {
 			response = cFeed(args);
 		} else if (cmd.equals("feedall")) {
 			response = cFeedall();
 		} else if (cmd.equals("clearinv")) {
 			response = cClearinv(args);
 		} else if (cmd.equals("clearinvall")) {
 			response = cClearinvall();
 		} else if (cmd.equals("renew")) {
 			response = cRenew(args);
 		} else if (cmd.equals("renewall")) {
 			response = cRenewall();
 		} else if (cmd.equals("cdmatch")) {
 			response = cCdmatch(args);
 		} else if (cmd.equals("cdpvp")) {
 			response = cCdpvp(args);
 		} else if (cmd.equals("cdwb")) {
 			response = cCdwb(args);
 		} else if (cmd.equals("cdc")) {
 			response = cCdc();
 		} else if (cmd.equals("pvp")) {
 			response = cPvp(args);
 		} else if (cmd.equals("chatscript")) {
 			response = cChatscript(args);
 		} else if (cmd.equals("muteall")) {
 			response = cMuteall(args);
 		} else if (cmd.equals("match")) {
 			response = cMatch();
 		} else if (cmd.equals("permaday")) {
 			response = cPermaday(args);
 		} else if (cmd.equals("deathban")) {
 			response = cDeathban(args);
 		} else if (cmd.equals("clearstarts")) {
 			response = cClearstarts();
 		} else if (cmd.equals("loadstarts")) {
 			response = cLoadstarts();
 		} else if (cmd.equals("savestarts")) {
 			response = cSavestarts();
 		} else if (cmd.equals("liststarts")) {
 			response = cListstarts();
 		} else if (cmd.equals("launch")) {
 			response = cLaunch(args);
 		} else if (cmd.equals("relaunch")) {
 			response = cRelaunch(args);
 		} else {
 			success = false;
 		}
 
 		if (response != null)
 			sender.sendMessage(response);
 		
 		return success;
 	}
 	
 	private String cMatch() {
 		startGame();
 		return OK_COLOR + "Match started!";
 	}
 
 	private boolean runCommandAsPlayer(Player sender, String cmd, String[] args) {
 		boolean success = true;
 		String response = null; // Stores any response to be given to the sender
 
 		if (cmd.equals("kill")) {
 			response = ERROR_COLOR + "The kill command is disabled.";
 		} else if (cmd.equals("notify") || cmd.equals("n")) {
 			response = cNotify(sender, args);
 		} else {
 			success = false;
 		}
 
 		if (response != null)
 			sender.sendMessage(response);
 		
 		return success;
 	}
 	
 	private String cSetspawn(Player sender) {
 		Location newSpawn = sender.getLocation();
 		newSpawn.getWorld().setSpawnLocation(newSpawn.getBlockX(), newSpawn.getBlockY(), newSpawn.getBlockZ());
 		return OK_COLOR + "This world's spawn point has been set to " + newSpawn.getBlockX() + "," + newSpawn.getBlockY() + "," + newSpawn.getBlockZ();
 	}
 
 	private String cMakestart(Player sender) {
 		Location l = sender.getLocation();
 		double x = l.getBlockX() + 0.5;
 		double y = l.getBlockY();
 		double z = l.getBlockZ() + 0.5;
 		
 		UhcStartPoint startPoint = new UhcStartPoint(world, x,y,z);
 		startPoints.add(startPoint);
 		
 		buildStartingTrough(startPoint.getLocation(), startPoints.size());
 		return OK_COLOR + "Start point added";
 		
 	}
 	private String cCdc() {
 		cancelCountdown();
 		return OK_COLOR + "Countdown cancelled!";
 		
 	}
 	
 	private String cMuteall(String[] args) {
 		if (args.length < 1)
 			return ERROR_COLOR +"Please specify 'on' or 'off'";
 
 		if (args[0].equalsIgnoreCase("on")) {
 			setChatMuted(true);
 			return OK_COLOR + "Chat muted!";
 		}
 		if (args[0].equalsIgnoreCase("off")) {
 			setChatMuted(false);
 			return OK_COLOR + "Chat unmuted!";
 		}
 		
 		return ERROR_COLOR + "Please specify 'on' or 'off'";
 
 	}
 	
 	private String cChatscript(String[] args) {
 		String scriptFile;
 		if (args.length < 1)
 			scriptFile = "announcement.txt"; 
 		else
 			scriptFile = args[0];
 		playChatScript(scriptFile, true);
 		return OK_COLOR + "Starting chat script " + args[0];
 	}
 	
 	private String cPvp(String[] args) {
 		if (args.length < 1)
 			return OK_COLOR + "PVP is " + (permaday ? "on" : "off");
 		
 		if (args[0].equalsIgnoreCase("off") || args[0].equals("0")) {
 			setPVP(false);
 		} else if (args[0].equalsIgnoreCase("on") || args[0].equals("1")) {
 			setPVP(true);
 		} else {
 			return ERROR_COLOR + "Argument '" + args[0] + "' not understood";
 		}
 		return null;
 
 	}
 	
 	
 	private String cPermaday(String[] args) {
 		if (args.length < 1)
 			return OK_COLOR + "Permaday is " + (permaday ? "on" : "off");
 		
 		if (args[0].equalsIgnoreCase("off") || args[0].equals("0")) {
 			setPermaday(false);
 		} else if (args[0].equalsIgnoreCase("on") || args[0].equals("1")) {
 			setPermaday(true);
 		} else {
 			return ERROR_COLOR + "Argument '" + args[0] + "' not understood";
 		}
 		return null;
 
 	}
 	
 	private String cDeathban(String[] args) {
 		if (args.length < 1)
 			return OK_COLOR + "Deathban is " + (deathban ? "on" : "off");
 		
 		if (args[0].equalsIgnoreCase("off") || args[0].equals("0")) {
 			setDeathban(false);
 		} else if (args[0].equalsIgnoreCase("on") || args[0].equals("1")) {
 			setDeathban(true);
 		} else {
 			return ERROR_COLOR + "Argument '" + args[0] + "' not understood";
 		}
 		return null;
 
 	}
 	
 	private String cClearstarts() {
 		startPoints.clear();
 		nextStartPoint = 0;
 		return OK_COLOR + "Start list cleared";
 	}
 	
 	private String cLoadstarts() {
 		nextStartPoint = 0;
 		loadStartPoints();
 		
 		return OK_COLOR.toString() + startPoints.size() + " start points loaded";
 			
 	}
 	
 	private String cSavestarts() {
 		if (saveStartPoints() == true) {
 			return OK_COLOR + "Start points were saved!";
 		} else {
 			return ERROR_COLOR + "Start points could not be saved.";
 		}
 	}
 	
 	private String cListstarts() {
 		if (startPoints == null || startPoints.size()==0)
 			return ERROR_COLOR + "There are no starts";
 
 		String response = "";
 		for (int i = 0; i < startPoints.size(); i++) {
 			UhcStartPoint sp = startPoints.get(i);
 			UhcPlayer p = sp.getUhcPlayer();
 			
 			response += (i+1);
 			
 			if (p != null) response += " (" + p.getName() + ")";
 			
 			response += ": " + sp.getX() + "," + sp.getY() + "," + sp.getZ() + "\n";
 		}
 		return response;
 	}
 	
 	private String cLaunch(String[] args)  {
 		if (args.length == 0) {
 			// launch all players
 			launchAll();
 			return OK_COLOR + "Launched all players";
 		} else {
 			Player p = server.getPlayer(args[0]);
 			if (p == null)
 				return ERROR_COLOR + "Player " + args[0] + " not found";
 			
 			if (p.isOp())
 				return ERROR_COLOR + "Player should be deopped before launching";
 			
 			boolean success = launch(p);
 			if (success)
 				return OK_COLOR + "Launched " + p.getDisplayName();
 			else 
 				return ERROR_COLOR + "Player could not be launched";
 		}
 	}
 	
 	private String cRelaunch(String[] args)  {
 		if (args.length == 0) {
 			return ERROR_COLOR + "Please specify player to relaunch";
 		} else {
 			Player p = server.getPlayer(args[0]);
 			if (p == null)
 				return ERROR_COLOR + "Player " + args[0] + " not found";
 			
 			if (p.isOp())
 				return ERROR_COLOR + "Player should be deopped before launching";
 			
 			boolean success = relaunch(p);
 			if (success)
 				return OK_COLOR + "Relaunched " + p.getDisplayName();
 			else 
 				return ERROR_COLOR + "Player could not be relaunched";
 		}
 	}
 	
 	public void setPVP(boolean pvp) {
 		for(World w : server.getWorlds()) {
 			w.setPVP(pvp);
 		}
 
 		getServer().broadcast(OK_COLOR + "PVP has been " + (pvp ? "enabled" : "disabled") + "!", Server.BROADCAST_CHANNEL_ADMINISTRATIVE);
 
 	}
 	
 	public void setPermaday(boolean p) {
 		if (p == permaday) return;
 		
 		this.permaday=p;
 
 		getServer().broadcast(OK_COLOR + "Permaday has been " + (permaday ? "enabled" : "disabled") + "!", Server.BROADCAST_CHANNEL_ADMINISTRATIVE);
 		
 		
 		if (permaday) {
 			this.world.setTime(6000);
 			permadayTaskId = getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
 				public void run() {
 					keepPermaday();
 				}
 			}, 1200L, 1200L);
 			
 		} else {
 			getServer().getScheduler().cancelTask(permadayTaskId);
 		}
 	}
 	
 	private void keepPermaday() {
 		this.world.setTime(6000);
 	}
 	
 	public void setDeathban(boolean d) {
 		this.deathban = d;
 		getServer().broadcast(OK_COLOR + "Deathban has been " + (deathban ? "enabled" : "disabled") + "!", Server.BROADCAST_CHANNEL_ADMINISTRATIVE);
 	}
 	
 	public boolean getDeathban() {
 		return deathban;
 	}
 	
 	/**
 	 * Execute the /cdwb command
 	 * 
 	 * @param args Arguments passed
 	 * @return Message to be displayed
 	 */
 	private String cCdwb(String[] args) {
 		
 		if (args.length == 0 || args.length > 2)
 			return ERROR_COLOR + "Specify world radius and countdown duration";
 		
 		int radius = Integer.parseInt(args[0]);
 		int countLength = 300;
 		
 		if (args.length == 2)
 			countLength = Integer.parseInt(args[1]);
 		
 		if (startCountdown(countLength, "World border will move to +/- " + radius + " x and z", "World border is now at +/- " + radius + " x and z!", CountdownType.WORLD_REDUCE))
 			return OK_COLOR + "Countdown started";
 		else 
 			return ERROR_COLOR + "Countdown already in progress!"; 
 	}
 
 	private String cCdpvp(String[] args) {
 		if (args.length > 1)
 			return ERROR_COLOR + "Usage: /cdpvp [seconds]";
 		
 		int countLength = 300;
 		
 		if (args.length == 1)
 			countLength = Integer.parseInt(args[0]);
 		
 		if (startCountdown(countLength, "PvP will be enabled", "PvP is now enabled!", CountdownType.PVP))
 			return OK_COLOR + "Countdown started";
 		else 
 			return ERROR_COLOR + "Countdown already in progress!"; 
 	}
 
 	private String cCdmatch(String[] args) {
 		if (args.length > 1)
 			return ERROR_COLOR + "Usage: /cdmatch [seconds]";
 		
 		int countLength = 300;
 		
 		if (args.length == 1)
 			countLength = Integer.parseInt(args[0]);
 		
 		if (startCountdown(countLength, "The match will begin", "Go!", CountdownType.MATCH))
 			return OK_COLOR + "Countdown started";
 		else 
 			return ERROR_COLOR + "Countdown already in progress!"; 
 	}
 
 	/**
 	 * Carry out the /notify command
 	 * 
 	 * @param sender the sender of the command
 	 * @param args arguments
 	 * @return response
 	 */
 	private String cNotify(Player sender, String[] args) {
 		String s = "";
 		if (args.length == 0)
 			s = "no text";
 		else {
 			for (int i = 0; i < args.length; i++) {
 				s += args[i] + " ";
 			}
 			s = s.substring(0, s.length() - 1);
 		}
 
 		getServer().broadcast(ALERT_COLOR + "[N]" + ChatColor.WHITE + " <" + sender.getDisplayName() + "> " + ALERT_COLOR + s,
 				Server.BROADCAST_CHANNEL_ADMINISTRATIVE);
 
 		setLastNotifierLocation(sender.getLocation());
 
 		return sender.isOp() ? null : OK_COLOR + "Administrators have been notified.";
 	}
 	
 	/**
 	 * Carry out the /tpn command
 	 * 
 	 * @param sender the sender of the command
 	 * @return response
 	 */
 	private String cTpn(Player sender) {
 		if (lastNotifierLocation == null)
 			return ERROR_COLOR + "No notification.";
 
 		doTeleport(sender, lastNotifierLocation);
 		return null;
 	}
 
 	/**
 	 * Carry out the /tpd command
 	 * 
 	 * @param sender the sender of the command
 	 * @return response
 	 */
 	private String cTpd(Player sender) {
 		if (lastDeathLocation == null)
 			return ERROR_COLOR + "Nobody has died.";
 
 		doTeleport(sender, lastDeathLocation);
 		return null;
 	}
 
 	/**
 	 * Carry out the /tpl command
 	 * 
 	 * @param sender the sender of the command
 	 * @return response
 	 */
 	private String cTpl(Player sender) {
 		if (lastLogoutLocation == null)
 			return ERROR_COLOR + "Nobody has logged out.";
 
 		doTeleport(sender, lastLogoutLocation);
 		return null;
 	}
 
 
 	/**
 	 * Carry out the /tps command
 	 * 
 	 * @param sender the sender of the command
 	 * @return response
 	 */
 	private String cTps(Player sender, String[] args) {
 		// Teleport sender to the specified start point, either by player name or by number
 		if (args.length != 1)
 			return ERROR_COLOR + "Incorrect number of arguments for /tps";
 		
 		Location destination = null;
 		
 		UhcPlayer up = this.getUhcPlayer(args[0]);
 		if (up != null) {
 			// Argument matches a player
 			destination = up.getStartPoint().getLocation();
 			
 		} else {
 			try {
 				int i = Integer.parseInt(args[0]);
 				destination = startPoints.get(i-1).getLocation();
 			} catch (Exception e) {
 				return ERROR_COLOR + "Unable to find that start point";
 			}
 		}
 		
 		
 		if (destination != null) {
 			doTeleport(sender,destination);
 			return null;
 		} else {
 			return ERROR_COLOR + "Unable to find that start point";
 		}
 	}
 	
 	private String cTp(Player sender, String[] args) {
 		
 		if (args.length == 0) {
 			if (lastEventLocation == null)
 				return ERROR_COLOR + "You haven't specified to who you want to teleport.";
 
 			doTeleport(sender, lastEventLocation);
 			return null;
 		}
 		
 		if(args.length == 1){
 			Player to = getServer().getPlayer(args[0]);
 			if (to == null || !to.isOnline())
 				return ERROR_COLOR + "Player " + args[0] + " not found";
 			doTeleport(sender,to,OK_COLOR + "Teleported to " + to.getName());
 			
 			return null;
 		}
 		
 		if(args.length == 2){
 			Player from = getServer().getPlayer(args[0]);
 			if (from == null || !from.isOnline())
 				return ERROR_COLOR + "Player " + args[0] + " not found";
 			Player to = getServer().getPlayer(args[1]);
 			if (to == null || !to.isOnline())
 				return ERROR_COLOR + "Player " + args[1] + " not found";
 			doTeleport(from,to);
 			
 			return OK_COLOR + "Teleported " + from.getName() + " to " + to.getName();
 		}
 		if(args.length==3){
 			// Teleport sender to coords in overworld
 			Double x;
 			Double y;
 			Double z;
 			try {
 				x = new Double (args[0]);
 				y = new Double (args[1]);
 				z = new Double (args[2]);
 			} catch (NumberFormatException e) {
 				return ERROR_COLOR + "Invalid coordinates";
 			}
 			
 			Location to = new Location(world,x,y,z);
 			doTeleport(sender,to);
 			return null;
 		}
 		if(args.length==4){
 			// Teleport a player to coords in overworld
 			Player from = getServer().getPlayer(args[0]);
 			if (from == null || !from.isOnline())
 				return ERROR_COLOR + "Player " + args[0] + " not found";
 			Double x;
 			Double y;
 			Double z;
 			try {
 				x = new Double (args[1]);
 				y = new Double (args[2]);
 				z = new Double (args[3]);
 			} catch (NumberFormatException e) {
 				return ERROR_COLOR + "Invalid coordinates";
 			}
 			
 			Location to = new Location(world,x,y,z);
 			doTeleport(from,to);
 			return OK_COLOR + from.getName() + " has been teleported";
 			
 		}
 		return ERROR_COLOR + "Incorrect number of arguments";
 	}
 
 	private String sTp(String[] args) {
 		
 		if(args.length == 2){
 			Player from = getServer().getPlayer(args[0]);
 			if (from == null || !from.isOnline())
 				return ERROR_COLOR + "Player " + args[0] + " not found";
 			Player to = getServer().getPlayer(args[1]);
 			if (to == null || !to.isOnline())
 				return ERROR_COLOR + "Player " + args[1] + " not found";
 			doTeleport(from,to);
 			
 			return OK_COLOR + "Teleported " + from.getName() + " to " + to.getName();
 		}
 
 		if(args.length==4){
 			// Teleport a player to coords in overworld
 			Player from = getServer().getPlayer(args[0]);
 			if (from == null || !from.isOnline())
 				return ERROR_COLOR + "Player " + args[0] + " not found";
 			Double x;
 			Double y;
 			Double z;
 			try {
 				x = new Double (args[1]);
 				y = new Double (args[2]);
 				z = new Double (args[3]);
 			} catch (NumberFormatException e) {
 				return ERROR_COLOR + "Invalid coordinates";
 			}
 			
 			Location to = new Location(world,x,y,z);
 			doTeleport(from,to);
 			return OK_COLOR + from.getName() + " has been teleported";
 			
 		}
 		return ERROR_COLOR + "Incorrect number of arguments";
 	}
 	
 	
 	private String cTp0(Player sender) {
 		sender.teleport(world.getSpawnLocation());
 		return OK_COLOR + "Teleported to spawn";
 	}
 	
 	/**
 	 * Carry out the /tpall command
 	 * 
 	 * @param sender the sender of the command
 	 * @param args arguments
 	 * @return response
 	 */
 	private String cTpall(Player sender) {
 		for (Player p : world.getPlayers()) {
 			if (p.getGameMode() != GameMode.CREATIVE) {
 				p.teleport(sender);
 				p.sendMessage(OK_COLOR + "You have been teleported!");
 			}
 		}
 		return OK_COLOR + "Players have been teleported to you!";
 	}
 
 	/**
 	 * Carry out the /renewall command
 	 * 
 	 * @return response
 	 */
 	private String cRenewall() {
 		renewAll();
 		return OK_COLOR + "Renewed all players.";
 	}
 
 	/**
 	 * Carry out the /renew command
 	 * 
 	 * @param args arguments
 	 * @return response
 	 */
 	private String cRenew(String[] args) {
 		if (args.length == 0)
 			return ERROR_COLOR + "Please specify player(s) to heal, or use /healall";
 
 		String response = "";
 
 		for (int i = 0; i < args.length; i++) {
 			Player p = getServer().getPlayer(args[i]);
 			if (p == null) {
 				response += ERROR_COLOR + "Player " + args[i] + " has not been found on the server." + "\n";
 			} else {
 				renew(p);
 				p.sendMessage(OK_COLOR + "You have been healed and fed!");
 				response += OK_COLOR + "Renewed " + p.getName() + "\n";
 			}
 		}
 
 		return response;
 	}
 
 	/**
 	 * Carry out the /clearinvall command
 	 * 
 	 * @return response
 	 */
 	private String cClearinvall() {
 		for (Player p : world.getPlayers()) {
 			if (p.getGameMode() != GameMode.CREATIVE) {
 				clearInventory(p);
 				p.sendMessage(OK_COLOR + "Your inventory has been cleared");
 			}
 		}
 		return OK_COLOR + "Cleared all survival players' inventories.";
 	}
 
 	/**
 	 * Carry out the /clearinv command
 	 * 
 	 * @param args arguments
 	 * @return response
 	 */
 	private String cClearinv(String[] args) {
 		if (args.length == 0)
 			return ERROR_COLOR + "Please specify player(s) to clear, or use /clearinvall";
 
 		String response = "";
 		for (int i = 0; i < args.length; i++) {
 			Player p = getServer().getPlayer(args[i]);
 			if (p == null) {
 				response += ERROR_COLOR + "Player " + args[i] + " has not been found on the server." + "\n";
 			} else {
 				clearInventory(p);
 				p.sendMessage(OK_COLOR + "Your inventory has been cleared");
 				response += OK_COLOR + "Cleared inventory of " + p.getName() + "\n";
 			}
 		}
 
 		return response;
 	}
 
 	/**
 	 * Carry out the /feedall command
 	 * 
 	 * @return response
 	 */
 	private String cFeedall() {
 		for (Player p : world.getPlayers()) {
 			feed(p);
 			p.sendMessage(OK_COLOR + "You have been fed");
 		}
 		return OK_COLOR + "Fed all players.";
 	}
 
 	/**
 	 * Carry out the /feed command
 	 * 
 	 * @param args arguments
 	 * @return response
 	 */
 	private String cFeed(String[] args) {
 		if (args.length == 0)
 			return ERROR_COLOR + "Please specify player(s) to feed, or use /feedall";
 
 		String response = "";
 		for (int i = 0; i < args.length; i++) {
 			Player p = getServer().getPlayer(args[i]);
 			if (p == null) {
 				response += ERROR_COLOR + "Player " + args[i] + " has not been found on the server." + "\n";
 			} else {
 				feed(p);
 				p.sendMessage(OK_COLOR + "You have been fed");
 				response += OK_COLOR + "Restored food levels of " + p.getName() + "\n";
 			}
 		}
 
 		return response;
 	}
 
 	/**
 	 * Carry out the /healall command
 	 * 
 	 * @return response
 	 */
 	private String cHealall() {
 		for (Player p : world.getPlayers()) {
 			heal(p);
 			p.sendMessage(OK_COLOR + "You have been healed");
 		}
 		return OK_COLOR + "Healed all players.";
 	}
 
 	/**
 	 * Carry out the /heal command
 	 * 
 	 * @param args arguments
 	 * @return response
 	 */
 	private String cHeal(String[] args) {
 		if (args.length == 0)
 			return ERROR_COLOR + "Please specify player(s) to heal, or use /healall";
 
 		String response = "";
 		for (int i = 0; i < args.length; i++) {
 			Player p = getServer().getPlayer(args[i]);
 			if (p == null)
 				response += ERROR_COLOR + "Player " + args[i] + " has not been found on the server." + "\n";
 
 			heal(p);
 			p.sendMessage(OK_COLOR + "You have been healed");
 			response += OK_COLOR + "Restored health of " + p.getName() + "\n";
 		}
 
 		return response;
 	}
 
 	public void setLastDeathLocation(Location l) {
 		lastDeathLocation = l;
 		lastEventLocation = l;
 	}
 
 	public void setLastNotifierLocation(Location l) {
 		lastNotifierLocation = l;
 		lastEventLocation = l;
 	}
 
 	public void setLastLogoutLocation(Location l) {
 		lastLogoutLocation = l;
 	}
 	
 	/**
 	 * Teleport one player to another. If player is a staff member, fancy
 	 * teleport will be done. Adds a custom message to be displayed.
 	 * 
 	 * @param p1 player to be teleported
 	 * @param p2 player to be teleported to
 	 * @param message the message to be displayed
 	 */
 	public void doTeleport(Player p1, Player p2, String message) {
 		//saveTpLocation(p1);
 
 		// if the first player is a staff member, do fancy teleport. Otherwise
 		// just
 		// teleport.
 		if (!p1.isOp())
 			p1.teleport(p2);
 		else
 			doFancyTeleport(p1, p2);
 
 		if (p1.getGameMode() == GameMode.CREATIVE)
 			p1.setFlying(true);
 
 		if (message != null && !message.isEmpty())
 			p1.sendMessage(OK_COLOR + message);
 	}
 
 	/**
 	 * Teleport one player to another. If player is a staff member, fancy
 	 * teleport will be done.
 	 * 
 	 * @param p1 player to be teleported
 	 * @param p2 player to be teleported to
 	 */
 	public void doTeleport(Player p1, Player p2) {
 		this.doTeleport(p1, p2, "You have been teleported!");
 	}
 	
 	/**
 	 * Teleport a player to a specific location
 	 * 
 	 * @param p1 player to be teleported
 	 * @param l location to be teleported to
 	 */
 	public void doTeleport(Player p1, Location l) {
 		//saveTpLocation(p1);
 		// Check if the location is loaded
 		World w = l.getWorld();
 		Chunk chunk = w.getChunkAt(l);
 		if (!w.isChunkLoaded(chunk))
 			w.loadChunk(chunk);
 		p1.teleport(l);
 		if (p1.getGameMode() == GameMode.CREATIVE)
 			p1.setFlying(true);
 		p1.sendMessage(OK_COLOR + "You have been teleported!");
 	}
 	
 	/**
 	 * Teleports a player NEAR to another player
 	 * 
 	 * If possible, they will be placed 5 blocks away, facing towards the
 	 * destination player.
 	 * 
 	 * @param streamer the Player who will be fancy-teleported
 	 * @param p the Player they are to be teleported to
 	 */
 	public void doFancyTeleport(Player streamer, Player p) {
 		Location l = p.getLocation();
 
 		Location lp = new Location(l.getWorld(), l.getX(), l.getY(), l.getZ());
 		Location lxp = new Location(l.getWorld(), l.getX(), l.getY(), l.getZ());
 		Location lxn = new Location(l.getWorld(), l.getX(), l.getY(), l.getZ());
 		Location lzp = new Location(l.getWorld(), l.getX(), l.getY(), l.getZ());
 		Location lzn = new Location(l.getWorld(), l.getX(), l.getY(), l.getZ());
 		Location tpl = new Location(l.getWorld(), l.getX(), l.getY(), l.getZ());
 		boolean xp = true, xn = true, zp = true, zn = true;
 
 		for (int i = 0; i < 5; i++) {
 			if (xp) {
 				lxp.setX(lxp.getX() + 1);
 				if (!isSpaceForPlayer(lxp))
 					xp = false;
 			}
 			if (xn) {
 				lxn.setX(lxn.getX() - 1);
 				if (!isSpaceForPlayer(lxn))
 					xn = false;
 			}
 			if (zp) {
 				lzp.setZ(lzp.getZ() + 1);
 				if (!isSpaceForPlayer(lzp))
 					zp = false;
 			}
 			if (zn) {
 				lzn.setZ(lzn.getZ() - 1);
 				if (!isSpaceForPlayer(lzn))
 					zn = false;
 			}
 		}
 
 		if (!xp)
 			lxp.setX(lxp.getX() - 1);
 		if (!xn)
 			lxn.setX(lxn.getX() + 1);
 		if (!zp)
 			lzp.setZ(lzp.getZ() - 1);
 		if (!zn)
 			lzn.setZ(lzn.getZ() + 1);
 
 		tpl.setYaw(90);
 		tpl.setPitch(0);
 
 		if (lxp.distanceSquared(lp) > tpl.distanceSquared(lp)) {
 			tpl = lxp;
 			tpl.setYaw(90);
 		}
 		if (lxn.distanceSquared(lp) > tpl.distanceSquared(lp)) {
 			tpl = lxn;
 			tpl.setYaw(270);
 		}
 		if (lzp.distanceSquared(lp) > tpl.distanceSquared(lp)) {
 			tpl = lzp;
 			tpl.setYaw(180);
 		}
 		if (lzn.distanceSquared(lp) > tpl.distanceSquared(lp)) {
 			tpl = lzn;
 			tpl.setYaw(0);
 		}
 		streamer.teleport(tpl);
 	}
 
 	/**
 	 * Calculates if it's possible for a player to fit in a certain spot.
 	 * 
 	 * @param feetLocation the location where the feet should be
 	 * @return wheter or not there's place for both his feet and head
 	 */
 	public static boolean isSpaceForPlayer(Location feetLocation) {
 		World w = feetLocation.getWorld();
 		int x = feetLocation.getBlockX(), y = feetLocation.getBlockY(), z = feetLocation.getBlockZ();
 		Block b1 = w.getBlockAt(x, y, z);
 		Block b2 = w.getBlockAt(x, y + 1, z);
 		return isSpaceForPlayer(b1) && isSpaceForPlayer(b2);
 	}
 
 	/**
 	 * Calculates if it's possible for a player to fit in a certain spot.
 	 * 
 	 * @param w the world in which we need to check if there's space for the
 	 *            player
 	 * @param x the x coordinate of the block to check
 	 * @param y the y coordinate of the block (at the feet) to check
 	 * @param z the z coordinate of the block to check
 	 * @return whether or not there's place for both his feet and head
 	 */
 	public static boolean isSpaceForPlayer(World w, int x, int y, int z) {
 		Block b1 = w.getBlockAt(x, y, z);
 		Block b2 = w.getBlockAt(x, y + 1, z);
 		return isSpaceForPlayer(b1) && isSpaceForPlayer(b2);
 	}
 
 	/**
 	 * Determine whether a given block is a either empty or liquid (but not
 	 * lava)
 	 * 
 	 * @param b the block to check
 	 * @return whether the block is suitable
 	 */
 	public static boolean isSpaceForPlayer(Block b) {
 		return (b.isEmpty() || b.isLiquid()) && b.getType() != Material.LAVA && b.getType() != Material.STATIONARY_LAVA;
 	}
 
 	
 	public void buildStartingTrough(Location l, int n) {
 		int x = l.getBlockX();
 		int y = l.getBlockY();
 		int z = l.getBlockZ();
 		world.getBlockAt(x,y-1,z).setType(Material.CHEST);
 		Inventory chest = ((Chest) world.getBlockAt(x,y-1,z).getState()).getBlockInventory();
 		chest.setItem(3,new ItemStack(Material.CARROT_STICK, 1));
 		chest.setItem(4,new ItemStack(Material.STONE_SWORD, 1));
 		chest.setItem(5,new ItemStack(Material.WATCH, 1));
 		
 		chest.setItem(12,new ItemStack(Material.STONE_PICKAXE, 1));
 		chest.setItem(13,new ItemStack(Material.LEATHER_CHESTPLATE, 1));
 		chest.setItem(14,new ItemStack(Material.STONE_SPADE, 1));
 		
 		chest.setItem(21,new ItemStack(Material.SADDLE, 1));
 		chest.setItem(22,new ItemStack(Material.STONE_AXE, 1));
 		chest.setItem(23,new ItemStack(Material.MONSTER_EGG, 1, (short) 90));
 		
 		
 		
 		
 		
 		world.getBlockAt(x,y-2,z).setType(Material.GLASS);
 		
 		world.getBlockAt(x-1,y-1,z).setType(Material.GLOWSTONE);
 		world.getBlockAt(x-1,y,z).setType(Material.GLASS);
 		world.getBlockAt(x-1,y+1,z).setType(Material.GLASS);
 		
 		world.getBlockAt(x+1,y-1,z).setType(Material.GLOWSTONE);
 		world.getBlockAt(x+1,y,z).setType(Material.GLASS);
 		world.getBlockAt(x+1,y+1,z).setType(Material.GLASS);
 		
 		world.getBlockAt(x,y-1,z-1).setType(Material.GLOWSTONE);
 		world.getBlockAt(x,y,z-1).setType(Material.GLASS);
 		world.getBlockAt(x,y+1,z-1).setType(Material.GLASS);
 
 		world.getBlockAt(x,y-1,z+1).setType(Material.GLOWSTONE);
 		world.getBlockAt(x,y,z+1).setType(Material.GLASS);
 		world.getBlockAt(x,y+1,z+1).setType(Material.GLASS);
 		
 		world.getBlockAt(x,y,z+2).setType(Material.SIGN_POST);
 		
 		Sign s = (Sign) world.getBlockAt(x,y,z+2).getState();
 		
 		s.setLine(1, "Player " + n);
 		s.update();
 		
 		
 	}
 	
 	
 	public String cButcher() {
 		butcherHostile();
 		return "Hostile mobs have been butchered";
 	}
 	public void butcherHostile() {
 		for (Entity entity : world.getEntitiesByClass(LivingEntity.class)) {
 			if (entity instanceof Monster || entity instanceof MagmaCube || entity instanceof Slime || entity instanceof EnderDragon
 					|| entity instanceof Ghast)
 				entity.remove();
 		}
 	}
 	
 	public void renew(Player p) {
 		heal(p);
 		feed(p);
 		clearXP(p);
 		clearPotionEffects(p);
 		if (p.getGameMode() != GameMode.CREATIVE)
 			clearInventory(p);
 	}
 
 	public void renewAll() {
 		for (Player p : world.getPlayers()) {
 			renew(p);
 			p.sendMessage(OK_COLOR + "You have been healed and fed!");
 		}
 	}
 
 	public void heal(Player p) {
 		p.setHealth(20);
 	}
 
 	public void feed(Player p) {
 		p.setFoodLevel(20);
 		p.setExhaustion(0.0F);
 		p.setSaturation(5.0F);
 	}
 
 	public void clearXP(Player p) {
 		p.setTotalExperience(0);
 		p.setExp(0);
 		p.setLevel(0);
 	}
 
 	public void clearPotionEffects(Player p) {
 		for (PotionEffect pe : p.getActivePotionEffects()) {
 			p.removePotionEffect(pe.getType());
 		}
 	}
 
 	public void clearInventory(Player player) {
 		PlayerInventory i = player.getInventory();
 		i.clear();
 		i.setHelmet(null);
 		i.setChestplate(null);
 		i.setLeggings(null);
 		i.setBoots(null);
 	}
 	
 	public void startGame() {
 		world.setTime(0);
 		butcherHostile();
 		for (Player p : world.getPlayers()) {
 			if (p.getGameMode() != GameMode.CREATIVE) {
 				feed(p);
 				clearXP(p);
 				clearPotionEffects(p);
 				heal(p);
 				p.setGameMode(GameMode.SURVIVAL);
 			}
 		}
 		setDeathban(true);
 		setPermaday(false);
 		setPVP(true);
 	}
 	
 	/**
 	 * Initiates a countdown
 	 * 
 	 * @param countdownLength the number of seconds to count down
 	 * @param eventName The name of the event to be announced
 	 * @param endMessage The message to display at the end of the countdown
 	 * @return Whether the countdown was started
 	 */
 	public boolean startCountdown(Integer countdownLength, String eventName, String endMessage, CountdownType type) {
 		if (countdown>0) return false;
 		countdown = countdownLength;
 		countdownEvent = eventName;
 		countdownEndMessage = endMessage;
 		countdownType = type;
 		countdown();
 		return true;
 	}
 	
 	public void countdown() {
 		if (countdown < 0)
 			return;
 		
 		if (countdown == 0) {
 			if (countdownType == CountdownType.MATCH) {
 				this.startGame();
 			} else if (countdownType == CountdownType.PVP) {
 				this.setPVP(true);
 			} else if (countdownType == CountdownType.WORLD_REDUCE) {
 				
 			}
 			getServer().broadcastMessage(MAIN_COLOR + countdownEndMessage);
 			return;
 		}
 		
 		if (countdown >= 60) {
 			if (countdown % 60 == 0) {
 				int minutes = countdown / 60;
 				getServer().broadcastMessage(ChatColor.RED + countdownEvent + " in " + minutes + " minute" + (minutes == 1? "":"s"));
 			}
 		} else if (countdown % 15 == 0 || countdown <= 5) {
 			getServer().broadcastMessage(ChatColor.RED + countdownEvent + " in " + countdown + " second" + (countdown == 1? "" : "s"));
 		}
 		
 		countdown--;
 		getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
 			public void run() {
 				countdown();
 			}
 		}, 20L);
 	}
 	
 	public void cancelCountdown() {
 		countdown = -1;
 	}
 	
 	public void playChatScript(String filename, boolean muteChat) {
 		if (muteChat) this.setChatMuted(true);
 		chatScript = loadChatScript(filename);
 		if (chatScript != null)
 			continueChatScript();
 	}
 	
 	public void continueChatScript() {
 		getServer().broadcastMessage(ChatColor.GREEN + chatScript.get(0));
 		chatScript.remove(0);
 		if (chatScript.size() > 0) {
 			getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
 				public void run() {
 					continueChatScript();
 				}
 			}, 40L);
 		} else {
 			this.setChatMuted(false);
 			chatScript = null;
 		}
 		
 	}
 	
 	public UhcPlayer getUhcPlayer(String name, Boolean createNew) {
 		UhcPlayer up = uhcPlayers.get(name);
 		if (up == null && createNew) {
 			up = new UhcPlayer(name);
 			uhcPlayers.put(name, up);
 		}
 		return up;
 	}
 	
 	public UhcPlayer getUhcPlayer(String name) {
 		return getUhcPlayer(name, false);
 	}
 	
 	public UhcPlayer getUhcPlayer(Player playerToGet, Boolean createNew) {
		return getUhcPlayer(playerToGet.getName());
 	}
 	
 	public UhcPlayer getUhcPlayer(Player playerToGet) {
 		return getUhcPlayer(playerToGet.getName(), false);
 	}
 	
 	/**
 	 * Launch the specified player only
 	 * 
 	 * @param p
 	 * @return success or failure
 	 */
 	public boolean launch(Player p) {
 
 		// Check that we are not dealing with an op here
 		if (p.isOp()) return false;
 		
 		// Get the player, creating if necessary
 		UhcPlayer up = getUhcPlayer(p, true);
 
 		// If player already launched, ignore
 		if (up.isLaunched()) return false;
 		
 		// Get the next available start point from the list
 		try {
 			UhcStartPoint start = startPoints.get(nextStartPoint);
 			// Increment the start point pointer
 			nextStartPoint ++;
 			// Teleport the player to the start point
 			p.teleport(start.getLocation());
 			renew(p);
 			up.setLaunched(true);
 			up.setStartPoint(start);
 			start.setUhcPlayer(up);
 			return true;
 		} catch (IndexOutOfBoundsException e) {
 			// Out of start points!
 			getServer().broadcast(ERROR_COLOR + "Not enough available start points! " + p.getDisplayName() + " not launched.", Server.BROADCAST_CHANNEL_ADMINISTRATIVE);
 			return false;
 		} 
 
 		
 	}
 	
 	/**
 	 * Re-teleport the specified player
 	 * @param p
 	 */
 	public boolean relaunch(Player p) {
 		UhcPlayer up = getUhcPlayer(p);
 		if (up == null) return false;
 		
 		return p.teleport(up.getStartPoint().getLocation());
 	}
 	
 	/**
 	 * Launch all online non-op players, and set other players to be launched upon joining
 	 */
 	public void launchAll() {
 		launchingPlayers=true;
 		for(Player p : getServer().getOnlinePlayers()) {
 			if (!p.isOp()) {
 				launch(p);
 			}
 		}
 	}
 	
 	public ArrayList<String> loadChatScript(String filename) {
 		File fChat = getDataFile(filename, true);
 		
 		if (fChat == null) return null;
 		
 		ArrayList<String> lines = new ArrayList<String>();
 		try {
 			FileReader fr = new FileReader(fChat);
 			BufferedReader in = new BufferedReader(fr);
 			String s = in.readLine();
 
 			while (s != null) {
 				lines.add(s);
 				s = in.readLine();
 			}
 
 			in.close();
 			fr.close();
 			return lines;
 			
 		} catch (IOException e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	public Boolean loadStartPoints() { return this.loadStartPoints(DEFAULT_START_POINTS_FILE); }
 	
 	public Boolean loadStartPoints(String filename) {
 		File fStarts = getDataFile(filename, true);
 		
 		if (fStarts == null) return false;
 		
 		startPoints.clear();
 
 		try {
 			FileReader fr = new FileReader(fStarts);
 			BufferedReader in = new BufferedReader(fr);
 			String s = in.readLine();
 
 			while (s != null) {
 				String[] coords = s.split(",");
 				if (coords.length == 3) {
 					try {
 						double x = Double.parseDouble(coords[0]);
 						double y = Double.parseDouble(coords[1]);
 						double z = Double.parseDouble(coords[2]);
 						startPoints.add(new UhcStartPoint(world, x, y, z));
 					} catch (NumberFormatException e) {
 						server.broadcast("Bad entry in locations file: " + s, Server.BROADCAST_CHANNEL_ADMINISTRATIVE);
 					}
 					
 				}
 				
 				s = in.readLine();
 			}
 
 			in.close();
 			fr.close();
 			return true;
 			
 		} catch (IOException e) {
 			return false;
 		}
 	}
 	
 	public Boolean saveStartPoints() { return this.saveStartPoints(DEFAULT_START_POINTS_FILE); }
 	
 	/**
 	 * Save start points to a file
 	 * 
 	 * @param filename File to save start points to
 	 * @return Whether the operation succeeded
 	 */
 	public boolean saveStartPoints(String filename) {
 		File fStarts = getDataFile(filename, false);
 		if (fStarts == null) return false;
 
 		try {
 			FileWriter fw = new FileWriter(fStarts);
 			BufferedWriter out = new BufferedWriter(fw);
 			for (UhcStartPoint sp : startPoints) {
 				out.write(sp.getX() + "," + sp.getY() + "," + sp.getZ() + "\n");
 			}
 			out.close();
 			fw.close();
 			return true;
 		} catch (IOException e) {
 			return false;
 		}
 		
 	}
 	
 	/**
 	 * Initialise the data directory for this plugin.
 	 *
 	 * @return true if the directory has been created or already exists.
 	 */
 	private boolean createDataDirectory() {
 	    File file = this.getDataFolder();
 	    if (!file.isDirectory()){
 	        if (!file.mkdirs()) {
 	            // failed to create the non existent directory, so failed
 	            return false;
 	        }
 	    }
 	    return true;
 	}
 	 
 	/**
 	 * Retrieve a File description of a data file for your plugin.
 	 * This file will be looked for in the data directory of your plugin, wherever that is.
 	 * There is no need to specify the data directory in the filename such as "plugin/datafile.dat"
 	 * Instead, specify only "datafile.dat"
 	 *
 	 * @param filename The name of the file to retrieve.
 	 * @param mustAlreadyExist True if the file must already exist on the filesystem.
 	 *
 	 * @return A File descriptor to the specified data file, or null if there were any issues.
 	 */
 	private File getDataFile(String filename, boolean mustAlreadyExist) {
 	    if (createDataDirectory()) {
 	        File file = new File(this.getDataFolder(), filename);
 	        if (mustAlreadyExist) {
 	            if (file.exists()) {
 	                return file;
 	            }
 	        } else {
 	            return file;
 	        }
 	    }
 	    return null;
 	}
 	
 	public boolean isChatMuted() {
 		return chatMuted;
 	}
 	
 	public void setChatMuted(Boolean muted) {
 		chatMuted = muted;
 	}
 
 	public Boolean getLaunchingPlayers() {
 		return launchingPlayers;
 	}
 
 	public ItemStack getKillerBonus() {
 		if (!killerBonusEnabled) return null;
 		if (killerBonusItemID != 0 && killerBonusItemQuantity != 0)
 			return new ItemStack(killerBonusItemID, killerBonusItemQuantity);
 		else
 			return null;
 	}
 
 	public void doMiningFatigue(Player player, int blockY) {
 		if (!miningFatigueEnabled) return;
 		if (blockY > miningFatigueMaxY) return;
 		UhcPlayer up = getUhcPlayer(player);
 		if (up == null) return;
 		up.incMineCount();
 		if (up.getMineCount() >= miningFatigueBlocks) {
 			up.resetMineCount();
 			if (miningFatigueExhaustion > 0) {
 				// Increase player's exhaustion by specified amount
 				player.setExhaustion(player.getExhaustion() + miningFatigueExhaustion);
 			}
 			if (miningFatigueDamage > 0) {
 				// Apply specified damage to player
 				player.damage(miningFatigueDamage);
 			}
 		}
 
 		
 	}
 
 
 }
