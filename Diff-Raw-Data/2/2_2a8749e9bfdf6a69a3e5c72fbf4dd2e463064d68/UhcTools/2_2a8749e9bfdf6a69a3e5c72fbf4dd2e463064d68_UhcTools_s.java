 package com.martinbrook.uhctools;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Random;
 
 import org.bukkit.ChatColor;
 
 import org.bukkit.Chunk;
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.Server;
 import org.bukkit.World;
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
 	private static UhcTools instance = null;
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
 	private HashMap<Integer, UhcStartPoint> startPoints = new HashMap<Integer, UhcStartPoint>();
 	private ArrayList<UhcStartPoint> availableStartPoints = new ArrayList<UhcStartPoint>();
 	private Boolean launchingPlayers = false;
 	private Boolean matchStarted = false;
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
 	private int playersInMatch = 0;
 	private int nextRadius;
 	private Calendar matchStartTime;
 	private int matchTimer = -1;
 	private boolean matchEnded = false;
 	private ArrayList<Location> calculatedStarts = null;
 	
 	/**
 	 * Get the singleton instance of UhcTools
 	 * 
 	 * @return The plugin instance
 	 */
 	public static UhcTools getInstance() {
 		return instance;
 	}
 	
 	public void onEnable() {
 		
 		// Store singleton instance
 		instance = this;
 		
 		l = new UhcToolsListener(this);
 		this.getServer().getPluginManager().registerEvents(l, this);
 		
 		this.server = this.getServer();
 		this.world = getServer().getWorlds().get(0);
 		
 		loadConfigValues();
 		
 		loadStartPoints();
 		setPermaday(true);
 		setPVP(false);
 	}
 	
 	public void onDisable(){
 		saveStartPoints();
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
 
 	/**
 	 * Execute a command sent by an opped player on the server
 	 * 
 	 * @param sender The Player who sent the command
 	 * @param cmd The command
 	 * @param args Command arguments
 	 * @return Whether the command succeeded
 	 */
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
 		} else if (cmd.equals("tpcs")) {
 			response = cTpcs(sender,args);
 		} else if (cmd.equals("gm")) {
 			sender.setGameMode((sender.getGameMode() == GameMode.CREATIVE) ? GameMode.SURVIVAL : GameMode.CREATIVE);
 		} else if (cmd.equals("setspawn")) {
 			response = cSetspawn(sender);
 		} else if (cmd.equals("makestart")) {
 			response = cMakestart(sender,args);
 		} else {
 			success = false;
 		}
 		
 		if (response != null)
 			sender.sendMessage(response);
 	
 		return success;
 		
 	}
 
 	/**
 	 * Run a command sent from the console
 	 * 
 	 * @param sender The sender of the command
 	 * @param cmd The command
 	 * @param args Command arguments
 	 * @return Whether the command succeeded
 	 */
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
 		} else if (cmd.equals("listplayers")) {
 			response = cListplayers();
 		} else if (cmd.equals("launch")) {
 			response = cLaunch(args);
 		} else if (cmd.equals("addplayers")) {
 			response = cAddplayers();
 		} else if (cmd.equals("addplayer")) {
 			response = cAddplayer(args);
 		} else if (cmd.equals("removeplayer") || cmd.equalsIgnoreCase("rmplayer")) {
 			response = cRemoveplayer(args);
 		} else if (cmd.equals("relaunch")) {
 			response = cRelaunch(args);
 		} else if (cmd.equals("calcstarts")) {
 			response = cCalcstarts(args);
 		} else {
 			success = false;
 		}
 	
 		if (response != null)
 			sender.sendMessage(response);
 		
 		return success;
 	}
 
 	/**
 	 * Run a command sent by a player
 	 * 
 	 * @param sender the Player who sent the command
 	 * @param cmd The command
 	 * @param args Command arguments
 	 * @return Whether the command succeeded
 	 */
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
 
 	/**
 	 * Carry out the /tpcs command
 	 * 
 	 * @param sender player who sent the command
 	 * @param args arguments
 	 * @return response
 	 */
 	private String cTpcs(Player sender, String[] args) {
 		if (calculatedStarts == null)
 			return ERROR_COLOR + "Start points have not been calculated";
 		
 		if (args.length != 1)
 			return ERROR_COLOR + "Please give the start number";
 		
 		
 		try {
 			int startNumber = Integer.parseInt(args[0]);
 			doTeleport(sender,calculatedStarts.get(startNumber - 1));
 		} catch (NumberFormatException e) {
 			return ERROR_COLOR + "Please give the start number";
 		} catch (IndexOutOfBoundsException e) {
 			return ERROR_COLOR + "That start does not exist";
 		}
 		
 		return null;
 	}
 
 	/**
 	 * Carry out the /calcstarts
 	 * 
 	 * @param args arguments
 	 * @return response
 	 */
 	private String cCalcstarts(String[] args) {
 		calculatedStarts = UhcUtil.calculateStarts(args);
 		if (calculatedStarts == null) return ERROR_COLOR + "No start locations were calculated";
 		
 		String response = OK_COLOR + "" + calculatedStarts.size() + " start locations calculated: \n";
 		for(int i = 0 ; i < calculatedStarts.size() ; i++) {
 			Location l = calculatedStarts.get(i);
			response += i + ": x=" + l.getX() + " z=" + l.getZ() + "\n";
 		}
 		return response;
 		
 		
 	}
 
 
 	/**
 	 * Carry out the /removeplayer command
 	 * 
 	 * @param args arguments
 	 * @return response
 	 */
 	private String cRemoveplayer(String[] args) {
 		if (args.length != 1)
 			return ERROR_COLOR + "Please specify the player to be removed";
 		
 		UhcPlayer up = getUhcPlayer(args[0]);
 		
 		if (up == null)
 			return ERROR_COLOR + "Player " + args[0] + " not found";;
 		
 		if (removePlayer(up))
 			return OK_COLOR + up.getName() + " removed, start point " + up.getStartPoint().getNumber() + " released";
 		else
 			return ERROR_COLOR + "Unable to unlaunch " + up.getName();
 
 	}
 
 	/**
 	 * Carry out the /addplayers command
 	 * 
 	 * @return response
 	 */
 	private String cAddplayers() {
 		int added = 0;
 		for (Player p : getServer().getOnlinePlayers()) {
 			if (!p.isOp())
 				if (addPlayer(p)) added++;
 		}
 		if (added > 0)
 			return "" + OK_COLOR + added + " player" + (added == 1? "" : "s") + " added";
 		else
 			return ERROR_COLOR + "No players to add!";
 		
 	}
 
 	/**
 	 * Carry out the /addplayer command
 	 * 
 	 * @param args arguments
 	 * @return response
 	 */
 	private String cAddplayer(String[] args) {
 		if (args.length != 1) 
 			return ERROR_COLOR + "Please specify the player to add";
 	
 		Player p = server.getPlayer(args[0]);
 		if (p == null)
 			return ERROR_COLOR + "Player " + args[0] + " not found";
 		
 		if (p.isOp())
 			return ERROR_COLOR + "Player should be deopped first!";
 		
 		boolean success = addPlayer(p);
 		if (success)
 			return OK_COLOR + "Added player " + p.getDisplayName();
 		else 
 			return ERROR_COLOR + "Player could not be added";
 	}
 
 
 	/**
 	 * Carry out the /match command
 	 * 
 	 * @param sender the sender of the command
 	 * @param args arguments
 	 * @return response
 	 */
 	private String cMatch() {
 		startMatch();
 		return OK_COLOR + "Match started!";
 	}
 
 	/**
 	 * Carry out the /setspawn command
 	 * 
 	 * @param sender the sender of the command
 	 * @param args arguments
 	 * @return response
 	 */
 	private String cSetspawn(Player sender) {
 		Location newSpawn = sender.getLocation();
 		newSpawn.getWorld().setSpawnLocation(newSpawn.getBlockX(), newSpawn.getBlockY(), newSpawn.getBlockZ());
 		return OK_COLOR + "This world's spawn point has been set to " + newSpawn.getBlockX() + "," + newSpawn.getBlockY() + "," + newSpawn.getBlockZ();
 	}
 
 	/**
 	 * Carry out the /makestart command
 	 * 
 	 * @param sender the sender of the command
 	 * @param args arguments
 	 * @return response
 	 */
 	private String cMakestart(Player sender, String[] args) {
 		Location l = sender.getLocation();
 		double x = l.getBlockX() + 0.5;
 		double y = l.getBlockY();
 		double z = l.getBlockZ() + 0.5;
 		
 		UhcStartPoint startPoint = createStartPoint(world, x,y,z);
 		
 		if (args.length < 1 || !("-n".equalsIgnoreCase(args[0])))
 			buildStartingTrough(startPoint);
 		
 		return OK_COLOR + "Start point added";
 		
 	}
 	
 	/**
 	 * Carry out the /cdc command
 	 * 
 	 * @param sender the sender of the command
 	 * @param args arguments
 	 * @return response
 	 */
 	private String cCdc() {
 		cancelCountdown();
 		return OK_COLOR + "Countdown cancelled!";
 		
 	}
 	
 	/**
 	 * Carry out the /muteall command
 	 * 
 	 * @param sender the sender of the command
 	 * @param args arguments
 	 * @return response
 	 */
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
 	
 	/**
 	 * Carry out the /chatscript command
 	 * 
 	 * @param sender the sender of the command
 	 * @param args arguments
 	 * @return response
 	 */
 	private String cChatscript(String[] args) {
 		String scriptFile;
 		if (args.length < 1)
 			scriptFile = "announcement.txt"; 
 		else
 			scriptFile = args[0];
 		playChatScript(scriptFile, true);
 		return OK_COLOR + "Starting chat script";
 	}
 	
 	/**
 	 * Carry out the /pvp command
 	 * 
 	 * @param sender the sender of the command
 	 * @param args arguments
 	 * @return response
 	 */
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
 	
 	/**
 	 * Carry out the /permaday command
 	 * 
 	 * @param sender the sender of the command
 	 * @param args arguments
 	 * @return response
 	 */
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
 	
 	/**
 	 * Carry out the /deathban command
 	 * 
 	 * @param sender the sender of the command
 	 * @param args arguments
 	 * @return response
 	 */
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
 	
 	/**
 	 * Carry out the /clearstarts command
 	 * 
 	 * @param sender the sender of the command
 	 * @param args arguments
 	 * @return response
 	 */
 	private String cClearstarts() {
 		clearStartPoints();
 		return OK_COLOR + "Start list cleared";
 	}
 	
 	/**
 	 * Carry out the /loadstarts command
 	 * 
 	 * @param sender the sender of the command
 	 * @param args arguments
 	 * @return response
 	 */
 	private String cLoadstarts() {
 		loadStartPoints();
 		return OK_COLOR.toString() + startPoints.size() + " start points loaded";
 	}
 	
 	/**
 	 * Carry out the /savestarts command
 	 * 
 	 * @param sender the sender of the command
 	 * @param args arguments
 	 * @return response
 	 */
 	private String cSavestarts() {
 		if (saveStartPoints() == true) {
 			return OK_COLOR + "Start points were saved!";
 		} else {
 			return ERROR_COLOR + "Start points could not be saved.";
 		}
 	}
 	
 	/**
 	 * Carry out the /liststarts command
 	 * 
 	 * @param sender the sender of the command
 	 * @param args arguments
 	 * @return response
 	 */
 	private String cListstarts() {
 		if (startPoints.size()==0)
 			return ERROR_COLOR + "There are no starts";
 
 		String response = "";
 		for (UhcStartPoint sp : startPoints.values()) {
 			UhcPlayer p = sp.getUhcPlayer();
 			
 			response += (sp.getNumber());
 			
 			if (p != null) response += " (" + p.getName() + ")";
 			
 			response += ": " + sp.getX() + "," + sp.getY() + "," + sp.getZ() + "\n";
 		}
 		return response;
 	}
 	
 	/**
 	 * Carry out the /listplayers command
 	 * 
 	 * @param sender the sender of the command
 	 * @param args arguments
 	 * @return response
 	 */
 	private String cListplayers() {
 		String response = "Players on server:\n";
 		
 		for (Player p : getServer().getOnlinePlayers()) {
 			UhcPlayer up = getUhcPlayer(p);
 			if (up != null) response += (up.isDead() ? ERROR_COLOR + "[D] " : OK_COLOR);
 			else if (p.isOp()) response += DECISION_COLOR;
 			else response += ERROR_COLOR;
 			
 			response += p.getName();
 			if (up != null) {
 				response += " " + (up.isLaunched() ? " (start point " + (up.getStartPoint().getNumber()) + ")" : " (unlaunched)");
 			}
 			response += "\n";
 		}
 		
 		return response;
 
 	}
 
 	/**
 	 * Carry out the /launch command
 	 * 
 	 * @param sender the sender of the command
 	 * @param args arguments
 	 * @return response
 	 */
 	private String cLaunch(String[] args)  {
 		// launch all players
 		launchAll();
 		return OK_COLOR + "Launching complete";
 	}
 	
 	/**
 	 * Carry out the /relaunch command
 	 * 
 	 * @param sender the sender of the command
 	 * @param args arguments
 	 * @return response
 	 */
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
 	
 	/**
 	 * Execute the /cdwb command
 	 * 
 	 * @param args Arguments passed
 	 * @return Message to be displayed
 	 */
 	private String cCdwb(String[] args) {
 		
 		if (args.length == 0 || args.length > 2)
 			return ERROR_COLOR + "Specify world radius and countdown duration";
 		
 		
 		try {
 			nextRadius = Integer.parseInt(args[0]);
 		} catch (NumberFormatException e) {
 			return ERROR_COLOR + "World radius must be specified as an integer";
 		}
 		
 		int countLength = 300;
 		
 		if (args.length == 2)
 			countLength = Integer.parseInt(args[1]);
 		
 		if (startCountdown(countLength, "World border will move to +/- " + nextRadius + " x and z", "World border is now at +/- " + nextRadius + " x and z!", CountdownType.WORLD_REDUCE))
 			return OK_COLOR + "Countdown started";
 		else 
 			return ERROR_COLOR + "Countdown already in progress!"; 
 	}
 
 	/**
 	 * Carry out the /cdpvp command
 	 * 
 	 * @param sender the sender of the command
 	 * @param args arguments
 	 * @return response
 	 */
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
 
 	/**
 	 * Carry out the /cdmatch command
 	 * 
 	 * @param sender the sender of the command
 	 * @param args arguments
 	 * @return response
 	 */
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
 		
 		UhcStartPoint destination = findStartPoint(args[0]);
 		
 		if (destination != null) {
 			doTeleport(sender,destination.getLocation());
 			return null;
 		} else {
 			return ERROR_COLOR + "Unable to find that start point";
 		}
 	}
 	
 	/**
 	 * Carry out the /tp command for a player
 	 * 
 	 * @param sender the sender of the command
 	 * @param args arguments
 	 * @return response
 	 */
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
 
 	/**
 	 * Carry out the /tp command for a console user
 	 * 
 	 * @param sender the sender of the command
 	 * @param args arguments
 	 * @return response
 	 */
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
 	
 	/**
 	 * Carry out the /tp0 command
 	 * 
 	 * @param sender the sender of the command
 	 * @param args arguments
 	 * @return response
 	 */
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
 		for (Player p : world.getPlayers()) {
 			if (p.getGameMode() != GameMode.CREATIVE) {
 				renew(p);
 				p.sendMessage(OK_COLOR + "You have been healed and fed!");
 			}
 		}
 		return OK_COLOR + "Renewed all non-creative players";
 	}
 
 	/**
 	 * Carry out the /renew command
 	 * 
 	 * @param args arguments
 	 * @return response
 	 */
 	private String cRenew(String[] args) {
 		if (args.length == 0)
 			return ERROR_COLOR + "Please specify player(s) to heal, or use /renewall";
 
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
 
 	/**
 	 * Carry out the /butcher command
 	 * 
 	 * @param sender the sender of the command
 	 * @param args arguments
 	 * @return response
 	 */
 	private String cButcher() {
 		butcherHostile();
 		return "Hostile mobs have been butchered";
 	}
 
 	
 	/**
 	 * Load configuration settings into variables
 	 */
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
 		deathban = getConfig().getBoolean("deathban");
 	}
 
 	/**
 	 * Set time to midday, to keep permaday in effect.
 	 */
 	private void keepPermaday() {
 		this.world.setTime(6000);
 	}
 
 	/**
 	 * Enables / disables PVP on all worlds
 	 * 
 	 * @param pvp Whether PVP is to be allowed
 	 */
 	public void setPVP(boolean pvp) {
 		for(World w : server.getWorlds()) {
 			w.setPVP(pvp);
 		}
 	
 		getServer().broadcast(OK_COLOR + "PVP has been " + (pvp ? "enabled" : "disabled") + "!", Server.BROADCAST_CHANNEL_ADMINISTRATIVE);
 	
 	}
 
 	/**
 	 * Enables / disables permaday
 	 * 
 	 * @param p whether permaday is to be on or off
 	 */
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
 
 	/**
 	 * Check whether deathban is in effect
 	 * 
 	 * @return Whether deathban is enabled
 	 */
 	public boolean getDeathban() {
 		return deathban;
 	}
 
 	/**
 	 * Set deathban on/off
 	 * 
 	 * @param d Whether deathban is to be enabled
 	 */
 	public void setDeathban(boolean d) {
 		this.deathban = d;
 		getServer().broadcast(OK_COLOR + "Deathban has been " + (deathban ? "enabled" : "disabled") + "!", Server.BROADCAST_CHANNEL_ADMINISTRATIVE);
 	}
 
 	/**
 	 * Try to find a start point from a user-provided search string.
 	 * 
 	 * @param searchParam The string to search for - a player name, or a start number may be sent
 	 * @return The start point, or null if not found.
 	 */
 	public UhcStartPoint findStartPoint(String searchParam) {
 		UhcPlayer up = this.getUhcPlayer(searchParam);
 		if (up != null) {
 			// Argument matches a player
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
 
 	/**
 	 * Set a death location for teleporters
 	 * 
 	 * @param l The location to be stored
 	 */
 	public void setLastDeathLocation(Location l) {
 		lastDeathLocation = l;
 		lastEventLocation = l;
 	}
 
 	/**
 	 * Set a notification location for teleporters
 	 * 
 	 * @param l The location to be stored
 	 */
 	public void setLastNotifierLocation(Location l) {
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
 	 * Teleport one player to another. If player is opped, fancy
 	 * teleport will be done. Adds a custom message to be displayed.
 	 * 
 	 * @param p1 player to be teleported
 	 * @param p2 player to be teleported to
 	 * @param message the message to be displayed
 	 */
 	public void doTeleport(Player p1, Player p2, String message) {
 		//saveTpLocation(p1);
 
 		// if the first player is opped, do fancy teleport.
 		if (!p1.isOp())
 			p1.teleport(p2);
 		else
 			doFancyTeleport(p1, p2);
 
 		// If player is in creative, set them to be in flight
 		if (p1.getGameMode() == GameMode.CREATIVE)
 			p1.setFlying(true);
 
 		// Send the teleport message, if provided
 		if (message != null && !message.isEmpty())
 			p1.sendMessage(OK_COLOR + message);
 	}
 
 	/**
 	 * Teleport one player to another. If player is opped, fancy
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
 	private void doFancyTeleport(Player streamer, Player p) {
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
 				if (!UhcUtil.isSpaceForPlayer(lxp))
 					xp = false;
 			}
 			if (xn) {
 				lxn.setX(lxn.getX() - 1);
 				if (!UhcUtil.isSpaceForPlayer(lxn))
 					xn = false;
 			}
 			if (zp) {
 				lzp.setZ(lzp.getZ() + 1);
 				if (!UhcUtil.isSpaceForPlayer(lzp))
 					zp = false;
 			}
 			if (zn) {
 				lzn.setZ(lzn.getZ() - 1);
 				if (!UhcUtil.isSpaceForPlayer(lzn))
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
 	 * Build a starting trough at the specified start point, and puts a starter chest and sign there.
 	 * 
 	 * @param sp The start point where the trough will be created
 	 */
 	public void buildStartingTrough(UhcStartPoint sp) {
 		int x = sp.getLocation().getBlockX();
 		int y = sp.getLocation().getBlockY();
 		int z = sp.getLocation().getBlockZ();
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
 		
 		makeStartSign(sp);
 	}
 	
 	/**
 	 * Make a default start sign
 	 * 
 	 * @param sp The start point to make the sign at
 	 */
 	public void makeStartSign(UhcStartPoint sp) {
 		UhcPlayer up = sp.getUhcPlayer();
 		if (up == null) 
 			makeStartSign(sp, "Player " + sp.getNumber());
 		else
 			makeStartSign(sp, up.getName());
 			
 	}
 	
 	/**
 	 * Make a start sign with specific text
 	 * 
 	 * @param sp The start point to make the sign at
 	 * @param text The text to write on the sign
 	 */
 	public void makeStartSign(UhcStartPoint sp, String text) {
 		int x = sp.getLocation().getBlockX();
 		int y = sp.getLocation().getBlockY();
 		int z = sp.getLocation().getBlockZ();
 		world.getBlockAt(x,y,z+2).setType(Material.SIGN_POST);
 		
 		Sign s = (Sign) world.getBlockAt(x,y,z+2).getState();
 		
 		s.setLine(0, "");
 		s.setLine(1, text);
 		s.setLine(2, "");
 		s.setLine(3, "");
 
 		s.update();
 	}
 	
 	
 	/**
 	 * Remove all hostile mobs in the overworld
 	 */
 	public void butcherHostile() {
 		for (Entity entity : world.getEntitiesByClass(LivingEntity.class)) {
 			if (entity instanceof Monster || entity instanceof MagmaCube || entity instanceof Slime || entity instanceof EnderDragon
 					|| entity instanceof Ghast)
 				entity.remove();
 		}
 	}
 	
 	/**
 	 * Heal, feed, clear XP, inventory and potion effects of the given player
 	 * 
 	 * @param p The player to be renewed
 	 */
 	public void renew(Player p) {
 		heal(p);
 		feed(p);
 		clearXP(p);
 		clearPotionEffects(p);
 		clearInventory(p);
 	}
 
 
 	/**
 	 * Heal the given player
 	 * 
 	 * @param p The player to be healed
 	 */
 	public void heal(Player p) {
 		p.setHealth(20);
 	}
 
 	/**
 	 * Feed the given player
 	 * 
 	 * @param p The player to be fed
 	 */
 	public void feed(Player p) {
 		p.setFoodLevel(20);
 		p.setExhaustion(0.0F);
 		p.setSaturation(5.0F);
 	}
 
 	/**
 	 * Reset XP of the given player
 	 * 
 	 * @param p The player
 	 */
 	public void clearXP(Player p) {
 		p.setTotalExperience(0);
 		p.setExp(0);
 		p.setLevel(0);
 	}
 
 	/**
 	 * Clear potion effects of the given player
 	 * 
 	 * @param p The player
 	 */
 	public void clearPotionEffects(Player p) {
 		for (PotionEffect pe : p.getActivePotionEffects()) {
 			p.removePotionEffect(pe.getType());
 		}
 	}
 
 	/**
 	 * Clear inventory and ender chest of the given player
 	 * 
 	 * @param player
 	 */
 	public void clearInventory(Player player) {
 		PlayerInventory i = player.getInventory();
 		i.clear();
 		i.setHelmet(null);
 		i.setChestplate(null);
 		i.setLeggings(null);
 		i.setBoots(null);
 		
 		player.getEnderChest().clear();
 		
 	}
 	
 	/**
 	 * Start the match
 	 * 
 	 * Butcher hostile mobs, turn off permaday, turn on PVP, put all players in survival and reset all players.
 	 */
 	public void startMatch() {
 		matchStarted = true;
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
 		setPermaday(false);
 		setPVP(true);
 		startMatchTimer();
 	}
 	
 	/**
 	 * End the match
 	 * 
 	 * Announce the total match duration
 	 */
 	public void endMatch() {
 		announceMatchTime(true);
 		stopMatchTimer();
 		matchEnded = true;
 
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
 	
 	/**
 	 * Continues the countdown in progress
 	 */
 	private void countdown() {
 		if (countdown < 0)
 			return;
 		
 		if (countdown == 0) {
 			if (countdownType == CountdownType.MATCH) {
 				this.startMatch();
 			} else if (countdownType == CountdownType.PVP) {
 				this.setPVP(true);
 			} else if (countdownType == CountdownType.WORLD_REDUCE) {
 				if (UhcUtil.setWorldRadius(world,nextRadius)) {
 					getServer().broadcast(OK_COLOR + "Border reduced to " + nextRadius, Server.BROADCAST_CHANNEL_ADMINISTRATIVE);
 				} else {
 					getServer().broadcast(ERROR_COLOR + "Unable to reduce border. Is WorldBorder installed?", Server.BROADCAST_CHANNEL_ADMINISTRATIVE);
 				}
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
 	
 	/**
 	 * Cancels a running countdown
 	 */
 	public void cancelCountdown() {
 		countdown = -1;
 	}
 	
 	/**
 	 * Starts the match timer
 	 */
 	private void startMatchTimer() {
 		matchStartTime = Calendar.getInstance();
 		matchTimer = getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
 			public void run() {
 				announceMatchTime(false);
 			}
 		}, 36000L, 36000L);
 	}
 	
 	/**
 	 * Stops the match timer
 	 */
 	private void stopMatchTimer() {
 		if (matchTimer != -1) {
 			getServer().getScheduler().cancelTask(matchTimer);
 		}
 	}
 	
 	/**
 	 * Announce the current match time in chat
 	 * 
 	 * @param precise Whether to give a precise time (00:00:00) instead of (xx minutes)
 	 */
 	public void announceMatchTime(boolean precise) {
 		getServer().broadcastMessage(MAIN_COLOR + "Match time: " + SIDE_COLOR + UhcUtil.formatDuration(matchStartTime, Calendar.getInstance(), precise));
 	}
 	
 
 	/**
 	 * Plays a chat script
 	 * 
 	 * @param filename The file to read the chat script from
 	 * @param muteChat Whether other chat should be muted
 	 */
 	public void playChatScript(String filename, boolean muteChat) {
 		if (muteChat) this.setChatMuted(true);
 		chatScript = UhcUtil.readFile(filename);
 		if (chatScript != null)
 			continueChatScript();
 	}
 	
 	/**
 	 * Output next line of current chat script, unmuting the chat if it's finished.
 	 */
 	private void continueChatScript() {
 		getServer().broadcastMessage(ChatColor.GREEN + chatScript.get(0));
 		chatScript.remove(0);
 		if (chatScript.size() > 0) {
 			getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
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
 	 * Get all players currently registered with the game
 	 * 
 	 * @return All registered players
 	 */
 	public Collection<UhcPlayer> getUhcPlayers() {
 		return uhcPlayers.values();
 	}
 	
 	
 	/**
 	 * Get a specific UhcPlayer by name, optionally creating a new one if needed
 	 * 
 	 * @param name The exact name of the player to be found  (case insensitive)
 	 * @param createNew Whether to create a new player if not found
 	 * @return The UhcPlayer, or null if not found/created
 	 */
 	public UhcPlayer getUhcPlayer(String name, Boolean createNew) {
 		UhcPlayer up = uhcPlayers.get(name.toLowerCase());
 		if (up == null && createNew) {
 			up = new UhcPlayer(name);
 			uhcPlayers.put(name.toLowerCase(), up);
 		}
 		return up;
 	}
 
 	/**
 	 * Get a specific UhcPlayer by name
 	 * 
 	 * @param name The exact name of the player to be found (case insensitive)
 	 * @return The UhcPlayer, or null if not found
 	 */
 	public UhcPlayer getUhcPlayer(String name) {
 		return getUhcPlayer(name, false);
 	}
 	
 	/**
 	 * Get a specific UhcPlayer matching the given Bukkit Player, optionally creating a new one if needed
 	 * 
 	 * @param playerToGet The Player to look for
 	 * @param createNew Whether to create a new player if not found
 	 * @return The UhcPlayer, or null if not found/created
 	 */
 	public UhcPlayer getUhcPlayer(Player playerToGet, Boolean createNew) {
 		return getUhcPlayer(playerToGet.getName(), createNew);
 	}
 	
 	/**
 	 * Get a specific UhcPlayer matching the given Bukkit Player
 	 * 
 	 * @param playerToGet The Player to look for
 	 * @return The UhcPlayer, or null if not found
 	 */
 	public UhcPlayer getUhcPlayer(Player playerToGet) {
 		return getUhcPlayer(playerToGet.getName(), false);
 	}
 	
 	/**
 	 * Add the supplied player and assign them a start point
 	 * 
 	 * @param p The player to add
 	 * @return success or failure
 	 */
 	public boolean addPlayer(Player p) {
 		// Check that we are not dealing with an op here
 		if (p.isOp()) return false;
 		
 		// Check that there are available start points
 		if (availableStartPoints.size() < 1) return false;
 		
 		
 		
 		// Get the player, creating if necessary
 		UhcPlayer up = getUhcPlayer(p, true);
 
 		
 		// Check if the player already has a start point
 		if (up.getStartPoint() != null) return false;
 		
 		// Get them a start point
 		Random rand = new Random();
 		UhcStartPoint start = availableStartPoints.remove(rand.nextInt(availableStartPoints.size()));
 		up.setStartPoint(start);
 		playersInMatch++;
 		start.setUhcPlayer(up);
 		
 		makeStartSign(start);
 
 		return false;
 	}
 
 	/**
 	 * Launch the specified player only
 	 * 
 	 * @param p The UhcPlayer to be launched
 	 * @return success or failure
 	 */
 	public boolean launch(UhcPlayer up) {
 
 		// If player already launched, ignore
 		if (up.isLaunched()) return false;
 		
 		// Get the player
 		Player p = getServer().getPlayer(up.getName());
 		
 		// If player not online, return
 		if (p == null) return false;
 		
 		// Teleport the player to the start point
 		p.setGameMode(GameMode.ADVENTURE);
 		doTeleport(p, up.getStartPoint().getLocation());
 		renew(p);
 		
 		up.setLaunched(true);
 
 		return true;
 
 
 		
 	}
 	
 	/**
 	 * Re-teleport the specified player
 	 * 
 	 * @param p The player to be relaunched
 	 */
 	public boolean relaunch(Player p) {
 		UhcPlayer up = getUhcPlayer(p);
 		if (up == null) return false;
 		
 		return p.teleport(up.getStartPoint().getLocation());
 	}
 	
 	/**
 	 * Remove the given player, removing them from the match and freeing up a start point.
 	 * 
 	 * The player will be teleported back to spawn if they are still on the server
 	 * 
 	 * @param up The player to be removed
 	 * @return Whether the player was removed
 	 */
 	public boolean removePlayer(UhcPlayer up) {
 		
 		UhcStartPoint sp = up.getStartPoint();
 
 		// If player has no start point, then they are not in the match and cannot be removed
 		if (sp == null) return false;
 		
 		up.setStartPoint(null);
 		sp.setUhcPlayer(null);
 		playersInMatch--;
 		makeStartSign(sp);
 		availableStartPoints.add(sp);
 		
 		if (up.isLaunched()) {
 			up.setLaunched(false);
 			// teleport player back to spawn, if they are online
 			Player p = getServer().getPlayer(up.getName());
 			if (p != null)
 				doTeleport(p,world.getSpawnLocation());
 
 		}
 		
 		return true;
 	}
 
 
 	/**
 	 * Start the launching phase, and launch all players who have been added to the game
 	 */
 	public void launchAll() {
 		launchingPlayers=true;
 		for(UhcPlayer up : getUhcPlayers()) launch(up);
 	}
 	
 
 	
 	/**
 	 * Create a new start point at a given location
 	 * 
 	 * @param number The start point's number
 	 * @param l The location of the start point
 	 * @return The created start point
 	 */
 	public UhcStartPoint createStartPoint(int number, Location l) {
 		// Check there is not already a start point with this number		
 		if (startPoints.containsKey(number))
 			return null;
 		
 		UhcStartPoint sp = new UhcStartPoint(number, l);
 		startPoints.put(number,  sp);
 		availableStartPoints.add(sp);
 		
 		return sp;
 	}
 	
 	/**
 	 * Create a new start point at a given location
 	 * 
 	 * @param number The start point's number
 	 * @param world The world to create the start point
 	 * @param x x coordinate of the start point
 	 * @param y y coordinate of the start point
 	 * @param z z coordinate of the start point
 	 * @return The created start point
 	 */
 	public UhcStartPoint createStartPoint(int number, World world, Double x, Double y, Double z) {
 		return createStartPoint(number, new Location(world, x, y, z));
 	}
 	
 	/**
 	 * Create a new start point at a given location, giving it the next available number
 	 * 
 	 * @param l The location of the start point
 	 * @return The created start point
 	 */
 	public UhcStartPoint createStartPoint(Location l) {
 		return createStartPoint(getNextAvailableStartNumber(), l);
 	}
 
 	/**
 	 * Create a new start point at a given location, giving it the next available number
 	 * 
 	 * @param world The world to create the start point
 	 * @param x x coordinate of the start point
 	 * @param y y coordinate of the start point
 	 * @param z z coordinate of the start point
 	 * @return The created start point
 	 */
 	public UhcStartPoint createStartPoint(World world, Double x, Double y, Double z) {
 		return createStartPoint(new Location(world, x, y, z));
 	}
 		
 	/**
 	 * Clear all start points
 	 */
 	public void clearStartPoints() {
 		startPoints.clear();
 		availableStartPoints.clear();
 
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
 	 * Attempt to load start points from the default file
 	 * 
 	 * @return Whether the operation succeeded
 	 */
 	public Boolean loadStartPoints() { return this.loadStartPoints(DEFAULT_START_POINTS_FILE); }
 	
 	
 	/**
 	 * Attempt to load start points from the specified file
 	 * 
 	 * @param filename The file to load start points from
 	 * @return Whether the operation succeeded
 	 */
 	public Boolean loadStartPoints(String filename) {
 		File fStarts = UhcUtil.getWorldDataFile(filename, true);
 		
 		if (fStarts == null) return false;
 		
 		clearStartPoints();
 		
 		try {
 			FileReader fr = new FileReader(fStarts);
 			BufferedReader in = new BufferedReader(fr);
 			String s = in.readLine();
 
 			while (s != null) {
 				String[] data = s.split(",");
 				if (data.length == 4) {
 					try {
 						int n = Integer.parseInt(data[0]);
 						double x = Double.parseDouble(data[1]);
 						double y = Double.parseDouble(data[2]);
 						double z = Double.parseDouble(data[3]);
 						UhcStartPoint sp = createStartPoint (n, world, x, y, z);
 						if (sp == null) {
 							server.broadcast("Duplicate start point: " + n, Server.BROADCAST_CHANNEL_ADMINISTRATIVE);
 
 						}
 					} catch (NumberFormatException e) {
 						server.broadcast("Bad entry in locations file: " + s, Server.BROADCAST_CHANNEL_ADMINISTRATIVE);
 					}
 					
 				} else {
 					server.broadcast("Bad entry in locations file: " + s, Server.BROADCAST_CHANNEL_ADMINISTRATIVE);
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
 	
 	/**
 	 * Save start points to the default file
 	 * 
 	 * @return Whether the operation succeeded
 	 */
 	public Boolean saveStartPoints() { return this.saveStartPoints(DEFAULT_START_POINTS_FILE); }
 	
 	/**
 	 * Save start points to a file
 	 * 
 	 * @param filename File to save start points to
 	 * @return Whether the operation succeeded
 	 */
 	public boolean saveStartPoints(String filename) {
 		File fStarts = UhcUtil.getWorldDataFile(filename, false);
 		if (fStarts == null) return false;
 
 		try {
 			FileWriter fw = new FileWriter(fStarts);
 			BufferedWriter out = new BufferedWriter(fw);
 			for (UhcStartPoint sp : startPoints.values()) {
 				out.write(sp.getNumber() + "," + sp.getX() + "," + sp.getY() + "," + sp.getZ() + "\n");
 			}
 			out.close();
 			fw.close();
 			return true;
 		} catch (IOException e) {
 			return false;
 		}
 		
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
 	 * @return Whether player launching has started yet
 	 */
 	public Boolean getLaunchingPlayers() {
 		return launchingPlayers;
 	}
 
 	/**
 	 * Get the bonus items to be dropped by a PVP-killed player in addition to their inventory
 	 * 
 	 * @return The ItemStack to be dropped
 	 */
 	public ItemStack getKillerBonus() {
 		if (!killerBonusEnabled) return null;
 		if (killerBonusItemID != 0 && killerBonusItemQuantity != 0)
 			return new ItemStack(killerBonusItemID, killerBonusItemQuantity);
 		else
 			return null;
 	}
 
 	/**
 	 * Apply the mining fatigue game mechanic
 	 * 
 	 * Players who mine stone below a certain depth increase their hunger or take damage
 	 * 
 	 * @param player The player to act upon
 	 * @param blockY The Y coordinate of the mined block
 	 */
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
 
 	/**
 	 * @return Whether the game is underway
 	 */
 	public Boolean isMatchStarted() {
 		return matchStarted;
 	}
 
 	/**
 	 * @return The number of players still in the match
 	 */
 	public int getPlayersInMatch() {
 		return playersInMatch;
 	}
 
 	/**
 	 * Process the death of a player
 	 * 
 	 * @param up The player who died
 	 */
 	public void handlePlayerDeath(UhcPlayer up) {
 		up.setDead(true);
 		playersInMatch--;
 		announcePlayersRemaining();
 		if (playersInMatch == 1) {
 			endMatch();
 		}
 	}
 
 	/**
 	 * Publicly announce how many players are still in the match 
 	 */
 	private void announcePlayersRemaining() {
 		// Make no announcement if final player was killed
 		if (playersInMatch < 1) return;
 		
 		String message;
 		if (playersInMatch == 1) {
 			message = getSurvivingPlayerList() + " is the winner!";
 		} else if (playersInMatch <= 4) {
 			message = playersInMatch + " players remain: " + getSurvivingPlayerList();
 		} else {
 			message = playersInMatch + " players remain";
 		}
 		
 		getServer().broadcast(OK_COLOR + message, Server.BROADCAST_CHANNEL_USERS);
 	}
 
 	/**
 	 * Get a list of surviving players
 	 * 
 	 * @return A comma-separated list of surviving players
 	 */
 	private String getSurvivingPlayerList() {
 		String survivors = "";
 		
 		for (UhcPlayer up : getUhcPlayers())
 			if (up.isLaunched() && !up.isDead()) survivors += up.getName() + ", ";;
 		
 		if (survivors.length() > 2)
 			survivors = survivors.substring(0,survivors.length()-2);
 		
 		return survivors;
 		
 	}
 	
 
 
 	/**
 	 * @return Whether the match is over
 	 */
 	public boolean isMatchEnded() {
 		return matchEnded;
 	}
 
 
 }
