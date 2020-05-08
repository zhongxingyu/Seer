 package com.martinbrook.tesseractuhc;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import org.bukkit.ChatColor;
 
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.martinbrook.tesseractuhc.listeners.ChatListener;
 import com.martinbrook.tesseractuhc.listeners.LoginListener;
 import com.martinbrook.tesseractuhc.listeners.MatchListener;
 import com.martinbrook.tesseractuhc.listeners.SpectateListener;
 import com.martinbrook.tesseractuhc.notification.PlayerMessageNotification;
 import com.martinbrook.tesseractuhc.startpoint.UhcStartPoint;
 import com.martinbrook.tesseractuhc.util.TeleportUtils;
 import com.martinbrook.tesseractuhc.util.MatchUtils;
 
 public class TesseractUHC extends JavaPlugin {
 	private static TesseractUHC instance = null;
 	public static final ChatColor MAIN_COLOR = ChatColor.GREEN, SIDE_COLOR = ChatColor.GOLD, OK_COLOR = ChatColor.GREEN, WARN_COLOR = ChatColor.LIGHT_PURPLE, ERROR_COLOR = ChatColor.RED,
 			DECISION_COLOR = ChatColor.GOLD, ALERT_COLOR = ChatColor.GREEN;
 	private static final boolean DEBUG_BUILD = false;
 	
 	private UhcMatch match;
 	
 	/**
 	 * Get the singleton instance of UhcTools
 	 * 
 	 * @return The plugin instance
 	 */
 	public static TesseractUHC getInstance() {
 		return instance;
 	}
 	
 	public void onEnable() {
 		
 		// Store singleton instance
 		instance = this;
 
 		saveDefaultConfig();
 		match = new UhcMatch(this, getServer().getWorlds().get(0), getConfig());
 	
 		getServer().getPluginManager().registerEvents(new ChatListener(match), this);
 		getServer().getPluginManager().registerEvents(new LoginListener(match), this);
 		getServer().getPluginManager().registerEvents(new MatchListener(match), this);
 		getServer().getPluginManager().registerEvents(new SpectateListener(match), this);
 		
 		
 		
 	}
 	
 	public void onDisable(){
 		match.saveMatchParameters();
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
 		} else if (cmd.equals("tpp")) {
 			response = cTpp(sender,args);
 		} else if (cmd.equals("gm")) {
 			sender.setGameMode((sender.getGameMode() == GameMode.CREATIVE) ? GameMode.SURVIVAL : GameMode.CREATIVE);
 		} else if (cmd.equals("vi") || cmd.equals("pi")) {
 			response = cVi(sender, args);
 		} else if (cmd.equals("setspawn")) {
 			response = cSetspawn(sender);
 		} else if (cmd.equals("notify") || cmd.equals("n")) {
 			response = cNotify(sender, args);
 		} else if (cmd.equals("uhc")) {
 			response = cUhc(sender, args);
 		} else if (cmd.equals("interact")) {
 			response = cInteract(sender);
 		} else {
 			success = false;
 		}
 		
 		if (response != null)
 			sender.sendMessage(response);
 	
 		return success;
 		
 	}
 
 	private String cInteract(Player sender) {
 		boolean interacting = !match.isInteractingAdmin(sender);
 		match.setInteractingAdmin(sender, interacting);
 		
 		return OK_COLOR + "Interaction has been " + (interacting ? "enabled" : "disabled") + ".";
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
 		} else if (cmd.equals("match")) {
 			response = cMatch(args);
 		} else if (cmd.equals("cdwb")) {
 			response = cCdwb(args);
 		} else if (cmd.equals("cdc")) {
 			response = cCdc();
 		} else if (cmd.equals("chatscript")) {
 			response = cChatscript(args);
 		} else if (cmd.equals("muteall")) {
 			response = cMuteall(args);
 		} else if (cmd.equals("permaday")) {
 			response = cPermaday(args);
 		} else if (cmd.equals("uhc")) {
 			response = cUhc(sender, args);
 		} else if (cmd.equals("launch")) {
 			response = cLaunch();
 		} else if (cmd.equals("relaunch")) {
 			response = cRelaunch(args);
 		} else if (cmd.equals("calcstarts")) {
 			response = cCalcstarts(args);
 		} else if (cmd.equals("setvanish")) {
 			response = cSetvanish();
 		} else if (cmd.equals("players")) {
 			response = cPlayers(args);
 		} else if (cmd.equals("teams")) {
 			response = cTeams(args);
 		} else if (cmd.equals("matchinfo") || cmd.equals("mi")) {
 			response = cMatchinfo();
 		} else {
 			success = false;
 		}
 	
 		if (response != null)
 			sender.sendMessage(response);
 		
 		return success;
 	}
 
 	private String cTeams(String[] args) {
 		// teams - lists all teams
 		// teams add identifier name name name - adds a team
 		if (match.isFFA())
 			return ERROR_COLOR + "This is not a team match. Use /players to list players";
 		
 		if (args.length < 1) {
 			String response = "";
 			Collection<UhcTeam> allTeams = match.getTeams();
 			response += allTeams.size() + " teams (" + match.countTeamsInMatch() + " still alive):\n";
 			
 			for (UhcTeam team : allTeams) {
 				response += (team.aliveCount()==0 ? ERROR_COLOR + "[D] " : OK_COLOR);
 				
 				response += team.getName();
 				response += " (start point " + (team.getStartPoint().getNumber()) + ")";
 				response += "\n";
 			}
 			return response;
 		}
 		if (args[0].equalsIgnoreCase("add")) {
 			if (args.length < 2)
 				return ERROR_COLOR + "Specify team to add!";
 			
 			String identifier = args[1];
 			String name = "";
 			
 			if (args.length < 3)
 				name = identifier;
 			else {
 				for (int i = 2; i < args.length; i++) name += args[i] + " ";
 				name = name.substring(0,name.length()-1);
 			}
 				
 			if (!match.addTeam(identifier, name))
 				return ERROR_COLOR + "Could not add team";
 			
 			return OK_COLOR + "Team created";
 			
 		} else if (args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("rm")) {
 			if (args.length < 2)
 				return ERROR_COLOR + "Specify team to remove!";
 			if (!match.removeTeam(args[1]))
 				return ERROR_COLOR + "Team could not be removed!";
 
 			return OK_COLOR + "Team removed";
 		}
 
 		
 		
 		return null;
 	}
 
 	private String cPlayers(String[] args) {
 		// players - lists all players
 		// players add playername
 		// players add playername teamidentifier
 		// players addall
 
 		if (args.length < 1) {
 
 			String response = "";
 			
 			if (match.isFFA()) {
 				Collection<UhcPlayer> allPlayers = match.getUhcPlayers();
 				response += allPlayers.size() + " players (" + match.countPlayersInMatch() + " still alive):\n";
 				
 				for (UhcPlayer up : allPlayers) {
 					response += (up.isDead() ? ERROR_COLOR + "[D] " : OK_COLOR);
 					
 					response += up.getName();
 					response += " (start point " + (up.getStartPoint().getNumber()) + ")";
 					response += (!up.isLaunched() ? " (unlaunched)" : "");
 					response += "\n";
 				}
 	
 			} else {
 				Collection<UhcTeam> allTeams = match.getTeams();
 				response += allTeams.size() + " teams (" + match.countTeamsInMatch() + " still alive):\n";
 				
 				for (UhcTeam team : allTeams) {
 					response += (team.aliveCount()==0 ? ERROR_COLOR + "[D] " : OK_COLOR);
 					
 					response += team.getName();
 					response += " (start point " + (team.getStartPoint().getNumber()) + ")";
 					response += "\n";
 					for (UhcPlayer up : team.getPlayers()) {
 						response += "  ";
 						response += (up.isDead() ? ERROR_COLOR + "[D] " : OK_COLOR);
 						
 						response += up.getName();
 						response += " (start point " + (up.getStartPoint().getNumber()) + ")";
 						response += (!up.isLaunched() ? " (unlaunched)" : "");
 						response += "\n";
 					}
 				}
 			}
 			
 			
 			return response;
 		}
 		if (args[0].equalsIgnoreCase("add")) {
 			if (args.length < 2)
 				return ERROR_COLOR + "Specify player to add!";
 			
 			String response = "";
 			
 			String name = args[1];
 			Player p = getServer().getPlayer(name);
 			
 			if (p != null)
 				name = p.getName();
 			else
 				response += WARN_COLOR + "Warning: adding a player who is not currently online\n";
 			
 			if (match.isFFA()) {
 				if (!match.addSoloPlayer(name))
 					return ERROR_COLOR + "Failed to add player";
 				
 				return response + OK_COLOR + "Player added";
 			} else {
 				if (args.length < 3)
 					return ERROR_COLOR + "Please specify the team! /players add NAME TEAM";
 				if (!match.addPlayer(p.getName(), args[2]))
 					return ERROR_COLOR + "Failed to add player " + args[1] + " to team " + args[2];
 				
 				return response + OK_COLOR + "Player added";
 			}
 				
 		} else if (args[0].equalsIgnoreCase("addall")) {
 			if (!match.isFFA())
 				return ERROR_COLOR + "Cannot auto-add players in a team match";
 			
 			int added = 0;
 			for (Player p : getServer().getOnlinePlayers()) {
 				if (!p.isOp())
 					if (match.addSoloPlayer(p.getName())) added++;
 			}
 			if (added > 0)
 				return "" + OK_COLOR + added + " player" + (added == 1? "" : "s") + " added";
 			else
 				return ERROR_COLOR + "No players to add!";
 			
 			
 		} else if (args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("rm")) {
 			if (args.length < 2)
 				return ERROR_COLOR + "Specify player to remove!";
 			if (!match.removePlayer(args[1]))
 				return ERROR_COLOR + "Player could not be removed!";
 
 			return OK_COLOR + "Player removed";
 		}
 		return null;
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
 		} else if (cmd.equals("join")) {
 			response = pJoin(sender, args);
 		} else if (cmd.equals("team")) {
 			response = pTeam(sender, args);
 		} else if (cmd.equals("leave")) {
 			response = pLeave(sender, args);
 		} else {
 			success = false;
 		}
 	
 		if (response != null)
 			sender.sendMessage(response);
 		
 		return success;
 	}
 
 	private String pLeave(Player sender, String[] args) {
 		if (!match.removePlayer(sender.getName()))
 			return ERROR_COLOR + "Leave failed";
 		else
 			return OK_COLOR + "You have left the match";
 	}
 
 	private String pTeam(Player sender, String[] args) {
 		if (match.getUhcPlayer(sender) != null)
 			return ERROR_COLOR + "You have already joined this match. Please /leave before creating a new team.";
 		
 		if (match.getMatchPhase() != MatchPhase.PRE_MATCH)
 			return ERROR_COLOR + "The match is already underway. You cannot create a team.";
 		
 		if (match.isFFA())
 			return ERROR_COLOR + "This is a FFA match. There are no teams.";
 		
 		if (!match.roomForAnotherTeam())
 			return ERROR_COLOR + "There are no more team slots left.";
 		
 		if (args.length < 1)
 			return ERROR_COLOR + "Syntax: /team identifier [full name]";
 		
 		String identifier = args[0];
 		String name = "";
 		
 		if (args.length < 2)
 			name = identifier;
 		else {
 			for (int i = 1; i < args.length; i++) name += args[i] + " ";
 			name = name.substring(0,name.length()-1);
 		}
 			
 		if (!match.addTeam(identifier, name))
 			return ERROR_COLOR + "Could not add a new team. Use /join to join an existing team.";
 		
 		if (!match.addPlayer(sender.getName(), identifier))
 			return ERROR_COLOR + "An error occurred. The team has been created but you could not be joined to it.";
 		
 		return OK_COLOR + "You have created the team: " + name;
 		
 	}
 
 	private String pJoin(Player sender, String[] args) {
 		if (match.getMatchPhase() != MatchPhase.PRE_MATCH)
 			return ERROR_COLOR + "The match is already underway. You cannot join.";
 		
 		if (match.isFFA()) {
 			if (match.addSoloPlayer(sender.getName())) {
 				return OK_COLOR + "You have joined the match";
 			} else {
 				return ERROR_COLOR + "Unable to join";
 			}
 		}
 		
 		UhcTeam teamToJoin = null;
 				
 		if(args.length > 0) teamToJoin = match.getTeam(args[0]);
 			
 		if (teamToJoin == null) {
 			String response = ERROR_COLOR + "Please specify a team to join. Available teams:\n";
 			for (UhcTeam t : match.getTeams()) {
 				response += t.getIdentifier() + ": " + t.getName() + "\n";
 			}
 			return response;
 		}
 		
 		if (match.addPlayer(sender.getName(), teamToJoin.getIdentifier()))
 			return OK_COLOR + "You are now a member of " + teamToJoin.getName();
 		else
 			return ERROR_COLOR + "Unable to join team. Are you already on a team?";
 	}
 
 	private String cSetvanish() {
 		match.setVanish();
 		return OK_COLOR + "Visibility of all players has been updated";
 	}
 
 	/**
 	 * Carry out the /vi or /pi command
 	 * 
 	 * @param sender the sender of the command
 	 * @param args arguments
 	 * @return response
 	 */
 	private String cVi(Player sender, String[] args) {
 		if (args.length < 1)
 			return ERROR_COLOR + "Please give the name of a player";
 		
 		Player p = getServer().getPlayer(args[0]);
 		
 		if (p == null)
 			return ERROR_COLOR + "Player " + args[0] + " not found.";
 
 		if (!match.showInventory(sender, p))
 			return ERROR_COLOR + "Unable to view inventory";
 		
 		return null;
 
 	}
 
 
 	private String cTpp(Player sender, String[] args) {
 		ArrayList<UhcPOI> pois = match.getPOIs();
 		if (args.length != 1)
 			return ERROR_COLOR + "Please give the POI number - see /uhc pois";
 		
 		try {
 			int poiNo = Integer.parseInt(args[0]);
 			TeleportUtils.doTeleport(sender,pois.get(poiNo - 1).getLocation());
 		} catch (NumberFormatException e) {
 			return ERROR_COLOR + "Please give the POI number - see /uhc pois";
 		} catch (IndexOutOfBoundsException e) {
 			return ERROR_COLOR + "That POI does not exist";
 		}
 		
 		return null;
 	}
 
 	/**
 	 * Carry out the /tpcs command
 	 * 
 	 * @param sender player who sent the command
 	 * @param args arguments
 	 * @return response
 	 */
 	private String cTpcs(Player sender, String[] args) {
 		ArrayList<Location> starts = match.getCalculatedStarts();
 		if (starts == null)
 			return ERROR_COLOR + "Start points have not been calculated";
 		
 		if (args.length != 1)
 			return ERROR_COLOR + "Please give the start number";
 		
 		
 		try {
 			int startNumber = Integer.parseInt(args[0]);
 			TeleportUtils.doTeleport(sender,starts.get(startNumber - 1));
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
 		ArrayList<Location> starts = MatchUtils.calculateStarts(args);
 		if (starts == null) return ERROR_COLOR + "No start locations were calculated";
 		
 		String response = OK_COLOR + "" + starts.size() + " start locations calculated: \n";
 		for(int i = 0 ; i < starts.size() ; i++) {
 			Location l = starts.get(i);
 			response += (i+1) + ": x=" + l.getX() + " z=" + l.getZ() + "\n";
 		}
 		match.setCalculatedStarts(starts);
 		return response;
 		
 		
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
 	 * Carry out the /cdc command
 	 * 
 	 * @param sender the sender of the command
 	 * @param args arguments
 	 * @return response
 	 */
 	private String cCdc() {
 		match.cancelCountdown();
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
 			match.setChatMuted(true);
 			return OK_COLOR + "Chat muted!";
 		}
 		if (args[0].equalsIgnoreCase("off")) {
 			match.setChatMuted(false);
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
 		match.playChatScript(scriptFile, true);
 		return OK_COLOR + "Starting chat script";
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
 			return OK_COLOR + "Permaday is " + (match.getPermaday() ? "on" : "off");
 		
 		if (args[0].equalsIgnoreCase("off") || args[0].equals("0")) {
 			match.setPermaday(false);
 		} else if (args[0].equalsIgnoreCase("on") || args[0].equals("1")) {
 			match.setPermaday(true);
 		} else {
 			return ERROR_COLOR + "Argument '" + args[0] + "' not understood";
 		}
 		return null;
 
 	}
 
 	
 	/**
 	 * Carry out the /uhc command
 	 * 
 	 * @param sender the sender of the command
 	 * @param args arguments
 	 * @return response
 	 */
 	private String cUhc(CommandSender sender, String[] args) {
 		if (args.length<1) {
 			return ERROR_COLOR + "Please specify an action.";
 		}
 		
 		
 		if (sender instanceof Player && ("startpoint".equalsIgnoreCase(args[0]) || "sp".equalsIgnoreCase(args[0]))) {
 			Location l = ((Player) sender).getLocation();
 			double x = l.getBlockX() + 0.5;
 			double y = l.getBlockY();
 			double z = l.getBlockZ() + 0.5;
 			
 			UhcStartPoint startPoint = match.addStartPoint(x, y, z, args.length < 2 || !("-n".equalsIgnoreCase(args[1])));
 			
 			return OK_COLOR + "Start point " + startPoint.getNumber() + " added at your current location";
 			
 		} else if (sender instanceof Player && ("poi".equalsIgnoreCase(args[0]))) {
 			if (args.length<2) return ERROR_COLOR + "Please give a description/name";
 			String name = "";
 			for(int i = 1; i < args.length; i++) name += args[i] + " ";
 			name = name.substring(0, name.length()-1);
 			match.addPOI(((Player) sender).getLocation(), name);
 			return OK_COLOR + "POI added at your current location";
 			
 		} else if ("reset".equalsIgnoreCase(args[0])) {
 			if (args.length < 2 || "params".equalsIgnoreCase(args[1])) {
 				match.resetMatchParameters();
 				return OK_COLOR + "Match data reset to default values";
 			} else if ("teams".equalsIgnoreCase(args[1]) || "players".equalsIgnoreCase(args[1])) {
 				if (!match.clearTeams())
 					return ERROR_COLOR + "Failed to clear teams and players";
 				else
 					return OK_COLOR + "Teams and players have been reset";
 			}
 		} else if ("save".equalsIgnoreCase(args[0])) {
 			if (args.length < 2 || "params".equalsIgnoreCase(args[1])) {
 				match.saveMatchParameters();
 				return OK_COLOR + "If no errors appear above, match parameters have been saved";
 			} else if ("teams".equalsIgnoreCase(args[1]) || "players".equalsIgnoreCase(args[1])) {
 				match.saveTeams();
 				return OK_COLOR + "If no errors appear above, teams and players have been saved";
 			}
 		} else if ("starts".equalsIgnoreCase(args[0])) {
 			HashMap<Integer, UhcStartPoint> startPoints = match.getStartPoints();
 			if (startPoints.size()==0)
 				return ERROR_COLOR + "There are no starts";
 
 			String response = "";
 			for (UhcStartPoint sp : startPoints.values()) {
 				UhcTeam t = sp.getTeam();
 				
 				response += (sp.getNumber());
 				
 				if (t != null) response += " (" + t.getName() + ")";
 				
 				response += ": " + sp.getX() + "," + sp.getY() + "," + sp.getZ() + "\n";
 			}
 			return response;
 		} else if ("pois".equalsIgnoreCase(args[0])) {
 			ArrayList<UhcPOI> pois = match.getPOIs();
 			String response = "";
 			for(int i = 0; i < pois.size(); i++) {
 				response += (i + 1) + ": " + pois.get(i).getName() + " (" + pois.get(i).toString() + ")\n"; 
 			}
 			return response;
 		} else if ("params".equalsIgnoreCase(args[0])) {
 			return this.listMatchParameters();
 		} else if ("set".equalsIgnoreCase(args[0])) {
 			if (args.length < 3)
 				return ERROR_COLOR + "Invalid command";
 
 			String parameter = args[1].toLowerCase();
 
 			String value = "";
 			for (int i = 2; i < args.length; i++) {
 				value += args[i] + " ";
 			}
 			value = value.substring(0, value.length()-1);
 			if (this.setMatchParameter(parameter, value)) {
 				value = this.getMatchParameter(parameter);
 				if (value == null)
 					return ERROR_COLOR + "Error getting value of " + parameter;
 				return OK_COLOR + parameter + " = " + value;
 
 			} else
 				return ERROR_COLOR + "Unable to set value of " + parameter;
 
 		} else if ("get".equalsIgnoreCase(args[0])) {
 			if (args.length < 2)
 				return ERROR_COLOR + "Invalid command";
 			String parameter = args[1].toLowerCase();
 			String value = this.getMatchParameter(parameter);
 			if (value == null)
 				return ERROR_COLOR + "No such match parameter as " + parameter;
 			return OK_COLOR + parameter + " = " + value;
 		} else if (sender instanceof Player && "setbonus".equalsIgnoreCase(args[0])) {
 			match.setBonusChest(((Player) sender).getEnderChest().getContents());
 			return OK_COLOR + "Bonus chest saved from your ender chest";
 		} else if (sender instanceof Player && "getbonus".equalsIgnoreCase(args[0])) {
 			((Player) sender).getEnderChest().setContents(match.getBonusChest());
 			return OK_COLOR + "Bonus chest loaded into your ender chest";
 		} 
 		
 		return ERROR_COLOR + "Command not understood";
 	}
 
 	
 	private String listMatchParameters() {
 		String response = "";
 		response += "deathban: " + getMatchParameter("deathban") + "\n";
 		response += "killerbonus: " + getMatchParameter("killerbonus") + "\n";
 		response += "miningfatigue: " + getMatchParameter("miningfatigue") + "\n";
 		response += "nopvp: " + getMatchParameter("nopvp") + "\n";
 		response += "ffa: " + getMatchParameter("ffa") + "\n";
 		response += "uhc: " + getMatchParameter("uhc") + "\n";
 		
 		return response;
 	}
 
 	/**
 	 * Return a human-friendly representation of a specified match parameter
 	 * 
 	 * @param parameter The match parameter to look up
 	 * @return A human-readable version of the parameter's value
 	 */
 	private String getMatchParameter(String parameter) {
 		if ("deathban".equalsIgnoreCase(parameter)) {
 			return (match.getDeathban() ? "On" : "Off");
 		} else if ("killerbonus".equalsIgnoreCase(parameter)) {
 			ItemStack kb = match.getKillerBonus();
 			if (kb == null) return "Off";
 			return kb.getAmount() + " " + kb.getType().toString();
 				
 		} else if ("miningfatigue".equalsIgnoreCase(parameter)) {
 			return match.getMiningFatigueGold() + " at gold, " + match.getMiningFatigueDiamond() + " at diamond";
 			
 		} else if ("nopvp".equalsIgnoreCase(parameter)) {
 			return String.valueOf(match.getNopvp());
 		} else if ("ffa".equalsIgnoreCase(parameter)) {
 			return (match.isFFA() ? "Yes" : "No");
 		} else if ("uhc".equalsIgnoreCase(parameter)) {
 			return (match.isUHC() ? "Yes" : "No");
 		}
 		
 		return null;
 		
 	}
 
 	private boolean setMatchParameter(String parameter, String value) {
 		// Look up the parameter.
 		
 		if ("deathban".equalsIgnoreCase(parameter)) {
 
 			Boolean v = MatchUtils.stringToBoolean(value);
 			if (v == null) return false;
 			match.setDeathban(v);
 			return true;
 			
 		} else if ("killerbonus".equalsIgnoreCase(parameter)) {
 			Boolean b = MatchUtils.stringToBoolean(value);
 			if (b != null && !b) {
 				match.setKillerBonus(0);
 				return true;
 			}
 			String[] split = value.split(" ");
 			if (split.length > 2)
 				return false;
 			
 			int quantity = 1;
 			
 			try {
 				int id = Integer.parseInt(split[0]);
 				if (split.length > 1)
 					quantity = Integer.parseInt(split[1]);
 				
 				match.setKillerBonus(id, quantity);
 				return true;
 			} catch (NumberFormatException e) {
 				return false;
 			}
 			
 		
 			
 		} else if ("miningfatigue".equalsIgnoreCase(parameter)) {
 			Boolean b = MatchUtils.stringToBoolean(value);
 			if (b != null && !b) {
 				match.setMiningFatigue(0,0);
 				return true;
 			}
 			
 			String[] split = value.split(" ");
 			if (split.length != 2)
 				return false;
 			
 			try {
 				double gold = Double.parseDouble(split[0]);
 				double diamond = Double.parseDouble(split[1]);
 				match.setMiningFatigue(gold, diamond);
 				return true;
 			} catch (NumberFormatException e) {
 				return false;
 			}
 			
 			
 		} else if ("nopvp".equalsIgnoreCase(parameter)) {
 			try {
 				match.setNopvp(Integer.parseInt(value));
 				return true;
 			} catch (NumberFormatException e) {
 				return false;
 			}
 		} else if ("ffa".equalsIgnoreCase(parameter)) {
 			Boolean v = MatchUtils.stringToBoolean(value);
 			if (v == null) return false;
 			match.setFFA(v);
 			return true;
 		} else if ("uhc".equalsIgnoreCase(parameter)) {
 			Boolean v = MatchUtils.stringToBoolean(value);
 			if (v == null) return false;
 			match.setUHC(v);
 			return true;
 		} else {
 			return false;
 		}
 		
 		
 	}
 
 	/**
 	 * Carry out the /matchinfo or /mi command
 	 * 
 	 * @return response
 	 */
 	private String cMatchinfo() {
 		return match.matchTimeAnnouncement(true) + "\n" + match.matchStatusAnnouncement(); 
 	}
 
 	/**
 	 * Carry out the /launch command
 	 * 
 	 * @param sender the sender of the command
 	 * @param args arguments
 	 * @return response
 	 */
 	private String cLaunch()  {
 		// launch all players
 		match.launchAll();
 		return OK_COLOR + "Player launching has begun";
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
 			Player p = getServer().getPlayer(args[0]);
 			if (p == null)
 				return ERROR_COLOR + "Player " + args[0] + " not found";
 			
 			if (p.isOp())
 				return ERROR_COLOR + "Player should be deopped before launching";
 			
 			boolean success = match.sendToStartPoint(p);
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
 		
 		int newRadius;
 		
 		if (args.length == 0 || args.length > 2)
 			return ERROR_COLOR + "Specify world radius and countdown duration";
 		
 		try {
 			newRadius = Integer.parseInt(args[0]);
 		} catch (NumberFormatException e) {
 			return ERROR_COLOR + "World radius must be specified as an integer";
 		}
 		
 		int countLength = 300;
 		
 		if (args.length == 2)
 			countLength = Integer.parseInt(args[1]);
 		
 		if (match.startBorderCountdown(countLength, newRadius))
 			return OK_COLOR + "Countdown started";
 		else
 			return ERROR_COLOR + "Unable to start border countdown";
 		 
 	}
 
 	/**
 	 * Carry out the /match command
 	 * 
 	 * @param sender the sender of the command
 	 * @param args arguments
 	 * @return response
 	 */
 	private String cMatch(String[] args) {
 		if (args.length > 1)
 			return ERROR_COLOR + "Usage: /match [seconds]";
 		
 		int countLength = 300;
 		
 		if (args.length == 1)
 			countLength = Integer.parseInt(args[0]);
 		
 		if (countLength < 120 && match.getMatchPhase() == MatchPhase.PRE_MATCH && !DEBUG_BUILD)
 			return ERROR_COLOR + "Countdown less than 2 minutes - you must launch players first!";
 		
 		if (match.startMatchCountdown(countLength))
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
 
 		match.sendAdminNotification(new PlayerMessageNotification(sender, s), sender.getLocation());
 
 		return sender.isOp() ? null : OK_COLOR + "Administrators have been notified.";
 	}
 	
 	/**
 	 * Carry out the /tpn command
 	 * 
 	 * @param sender the sender of the command
 	 * @return response
 	 */
 	private String cTpn(Player sender) {
 		Location l = match.getLastNotifierLocation();
 		if (l == null)
 			return ERROR_COLOR + "No notification.";
 
 		TeleportUtils.doTeleport(sender, l);
 		return null;
 	}
 
 	/**
 	 * Carry out the /tpd command
 	 * 
 	 * @param sender the sender of the command
 	 * @return response
 	 */
 	private String cTpd(Player sender) {
 		Location l = match.getLastDeathLocation();
 		if (l == null)
 			return ERROR_COLOR + "Nobody has died.";
 
 		TeleportUtils.doTeleport(sender, l);
 		return null;
 	}
 
 	/**
 	 * Carry out the /tpl command
 	 * 
 	 * @param sender the sender of the command
 	 * @return response
 	 */
 	private String cTpl(Player sender) {
 		Location l = match.getLastLogoutLocation();
 		if (l == null)
 			return ERROR_COLOR + "Nobody has logged out.";
 
 		TeleportUtils.doTeleport(sender, l);
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
 		
 		UhcStartPoint destination = match.findStartPoint(args[0]);
 		
 		if (destination != null) {
 			TeleportUtils.doTeleport(sender,destination.getLocation());
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
 			if (match.getLastEventLocation() == null)
 				return ERROR_COLOR + "You haven't specified to who you want to teleport.";
 
 			TeleportUtils.doTeleport(sender, match.getLastEventLocation());
 			return null;
 		}
 		
 		if(args.length == 1){
 			Player to = getServer().getPlayer(args[0]);
 			if (to == null || !to.isOnline())
 				return ERROR_COLOR + "Player " + args[0] + " not found";
 			TeleportUtils.doTeleport(sender,to,OK_COLOR + "Teleported to " + to.getName());
 			
 			return null;
 		}
 		
 		if(args.length == 2){
 			Player from = getServer().getPlayer(args[0]);
 			if (from == null || !from.isOnline())
 				return ERROR_COLOR + "Player " + args[0] + " not found";
 			Player to = getServer().getPlayer(args[1]);
 			if (to == null || !to.isOnline())
 				return ERROR_COLOR + "Player " + args[1] + " not found";
 			TeleportUtils.doTeleport(from,to);
 			
 			return OK_COLOR + "Teleported " + from.getName() + " to " + to.getName();
 		}
 		if(args.length==3){
 			// Teleport sender to coords in their current world
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
 			
 			Location to = new Location(sender.getWorld(),x,y,z);
 			TeleportUtils.doTeleport(sender,to);
 			return null;
 		}
 		if(args.length==4){
 			// Teleport a player to coords in their current world
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
 			
 			Location to = new Location(from.getWorld(),x,y,z);
 			TeleportUtils.doTeleport(from,to);
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
 			TeleportUtils.doTeleport(from,to);
 			
 			return OK_COLOR + "Teleported " + from.getName() + " to " + to.getName();
 		}
 
 		if(args.length==4){
 			// Teleport a player to coords in their current world
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
 			
 			Location to = new Location(from.getWorld(),x,y,z);
 			TeleportUtils.doTeleport(from,to);
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
 		sender.teleport(match.getStartingWorld().getSpawnLocation());
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
 		for (Player p : getServer().getOnlinePlayers()) {
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
 		for (Player p : getServer().getOnlinePlayers()) {
 			if (p.getGameMode() != GameMode.CREATIVE) {
 				match.renew(p);
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
 				match.renew(p);
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
 		for (Player p : getServer().getOnlinePlayers()) {
 			if (p.getGameMode() != GameMode.CREATIVE) {
 				match.clearInventory(p);
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
 				match.clearInventory(p);
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
 		for (Player p : getServer().getOnlinePlayers()) {
 			match.feed(p);
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
 				match.feed(p);
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
 		for (Player p : getServer().getOnlinePlayers()) {
 			match.heal(p);
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
 
 			match.heal(p);
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
 		match.butcherHostile();
 		return "Hostile mobs have been butchered";
 	}
 
 	
 
 
 	public UhcMatch getMatch() {
 		return match;
 	}
 
 
 	
 
 }
