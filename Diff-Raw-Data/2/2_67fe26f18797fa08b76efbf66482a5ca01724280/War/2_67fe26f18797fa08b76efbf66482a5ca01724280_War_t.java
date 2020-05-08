 package Lihad.Conflict;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.Arrays;
 import java.util.Set;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.HashMap;
 import java.util.Date;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Projectile;
 
 public class War implements org.bukkit.event.Listener, org.bukkit.command.CommandExecutor {
 
 	// -------------------------------------
 	// static funcs
 	public static Date getNextWartime() {
 
 		Calendar now = Calendar.getInstance();
 
         Calendar nextWednesdayWar = Calendar.getInstance();
         nextWednesdayWar.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
         nextWednesdayWar.set(Calendar.HOUR_OF_DAY, 19);
         nextWednesdayWar.set(Calendar.MINUTE, 15);
         if (now.getTime().after(nextWednesdayWar.getTime())) {
             nextWednesdayWar.add(Calendar.DATE, 7);
         }
 
         Calendar nextSaturdayWar = Calendar.getInstance();
         nextSaturdayWar.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
         nextSaturdayWar.set(Calendar.HOUR_OF_DAY, 13);
         nextSaturdayWar.set(Calendar.MINUTE, 00);
         if (now.getTime().after(nextSaturdayWar.getTime())) {
             nextSaturdayWar.add(Calendar.DATE, 7);
         }
 
         if (nextWednesdayWar.getTime().before(nextSaturdayWar.getTime()))  {
             return nextWednesdayWar.getTime();
         }
         else {
             return nextSaturdayWar.getTime();
         }
     }
 
 	public boolean warShouldEnd() {
 
 		if (allNodesConquered) {
 			return true;
 		}
 
 		return (new Date()).after(endTime);
 	}
 
 	// -------------------------------------
 	// Classes/enums --------------------
 	enum WarState {
 		PENDING,
 		RUNNING
 	};
 
 	static class Team {
 
 		String name;
 		Set<Player> players = new HashSet<Player>();
 
 		Team(String n) { name = n; }
 
 		public String getName() { return name; }
 
 		public void addPlayer(Player p) { players.add(p); }
 		public void removePlayer(Player p) { players.remove(p); }
 		public boolean hasPlayer(Player p) { return players.contains(p); }
 		
 		/**
 		 * Gets the list of players belonging to a team.
 		 * @return Player[] - An array of Players.
 		 */
 		public Player[] getPlayers() {
 			Object[] javaSucks = players.toArray();
 			Player[] returnMe = new Player[javaSucks.length];
 			for (int i=0; i<returnMe.length; i++) {
 				if (javaSucks[i] instanceof Player)
 				{
 					returnMe[i] = (Player) javaSucks[i];
 				} else {
 					returnMe[i] = null;
 				}
 			}
 			return returnMe;
 		}
 		public int size() { return players.size(); }
 
 	}
 
 	static class WarNode {
 		public String name;
 		public Location location;
 		public int captureCounter = 0;
 		public Team owner = null;
 		public Team captureTeamTemp;
 
 		public Map<Team, Integer> teamCounters = new HashMap<Team, Integer>();
 		public boolean conquered = false;
 
 		public WarNode(String n, Location l) {
 			name = n;
 			location = l;
 		};
 	}
 
 	// -------------------------------------
 	// Members --------------------
 	WarState state = WarState.PENDING;
 	LinkedList<Player> unassignedPlayers = new LinkedList<Player>();
 	ArrayList<WarNode> nodes = new ArrayList<WarNode>();
 	List<Team> teams = new ArrayList<Team>();
 	boolean allNodesConquered = false;
     int ticksToConquer = 0;
     Date beginTime = null;
     Date endTime = null;
     Map<String, Team> loggedPlayers = new HashMap<String, Team>();
 
     static final Team Contested = new Team("Contested");
 
     static final int PREP_TIME_IN_MINS = 15;
     
 	// -------------------------------------
 	// Constructor --------------------
 	public War() { } // Empty ctor creates a War that doesn't do anything, because Bukkit listeners require an instance
     
     public War(int startDelayMinutes, int durationMinutes) {
 
 		for (Node n : Conflict.nodes) {
             if (n instanceof Warzone) {
                 WarNode wn = new WarNode(n.name, n.getLocation());
                 this.nodes.add(wn);
             }
         }
 
         Calendar c = Calendar.getInstance();
         c.add(Calendar.MINUTE, startDelayMinutes);
         beginTime = c.getTime();
         c.add(Calendar.MINUTE, durationMinutes);
         endTime = c.getTime();
 
         ticksToConquer = durationMinutes * 30;
         
         postWarPendingNotice();
 	}
 
 	// -------------------------------------
 	// Helpers --------------------
 
     public Team getPlayerTeam(Player p) {
 		for (Team t : teams) {
 			if (t.hasPlayer(p)) { return t; }
 		}
 		return null;
 	}
 
 
 	// -------------------------------------
 	// Methods --------------------
 	public boolean registerPlayer(Player p) {
 		if (unassignedPlayers.contains(p)) {
 			return false;
 		}
 		if (getPlayerTeam(p) != null) {
 			return false;
 		}
 		unassignedPlayers.add(p);
 		return true;
 	}
 
 	public void unregisterPlayer(Player p) {
 		Team t = getPlayerTeam(p);
 		if (t != null) {
 			t.removePlayer(p);
 	        // Remember the team they were on, so they get put there if they re-join
 	        loggedPlayers.put(p.getName(), t);
 		}
 		else if (unassignedPlayers.contains(p)) {
 			unassignedPlayers.remove(p);
 		}
 	}
 
 	void begin() {
 
 		LinkedList<String> teamNames = new LinkedList<String>(Arrays.asList(
 				"Marauders",
 				"Crusaders",
 				"Destroyers",
 				"Bonebreakers",
 				"Buccaneers",
 				"Plunderers",
 				"Assassins",
 				"Templars",
 				"Imperials"
 				));
 		java.util.Collections.shuffle(teamNames);
 
 		for (int i = 0; i < 3; i++) {
 			teams.add(new Team(teamNames.pop()));
 		}
 
 		Bukkit.getServer().broadcastMessage("War is starting!  Teams are as follows:");
 
 		assignPlayers(false);
 		for (Team t : teams) {
 			Bukkit.getServer().broadcastMessage(ChatColor.GRAY.toString() + "Team " + ChatColor.GOLD + t.getName() + ChatColor.GRAY + ": " + ChatColor.AQUA + t.getPlayers());
 		}
 
 		LinkedList<WarNode> nodesClone = new LinkedList<WarNode>();
 		nodesClone.addAll(nodes);
 		java.util.Collections.shuffle(nodesClone);
 
 		// tp all team members to the top of a random node block
 		for (Team t : teams) {
 			Location dest = nodesClone.pop().location;
 			dest = new Location(dest.getWorld(), dest.getBlockX(), dest.getBlockY() + 2, dest.getBlockZ());
 			for (Player p : t.getPlayers()) {
 				p.teleport(dest);
 			}
 		}
 
 		state = WarState.RUNNING;        
 	}
 
 	void assignPlayers(boolean broadcastAdditions) {
 
 		// Repeatedly finds smallest team and adds a player
 		while(unassignedPlayers.size() > 0) {
             Player p = unassignedPlayers.pop();
             Team team = null;
 
             if (loggedPlayers.containsKey(p.getName())) {
                 team = loggedPlayers.get(p.getName());
             }
             else {
                 int smallestValue = Integer.MAX_VALUE;
                 for (Team t : teams) {
                     int size = t.size();
                     // If sizes are equal, 60% chance we'll switch our guess.  This way it doesn't load up the first team always.
                     if (size < smallestValue || ((size == smallestValue) && (Conflict.random.nextInt(100) < 60))) {
                         smallestValue = size;
                         team = t;
                     }
                 }
             }
             if (team != null) {
                 team.addPlayer(p);
                 if (broadcastAdditions) {
                     String message = "" + ChatColor.AQUA + p.getName();
                     message = message + ChatColor.GRAY + " has joined the war on team ";
                     message = message + ChatColor.GOLD + team.getName();
                     Bukkit.getServer().broadcastMessage(message);
                 }
             }
 		}
 	}
 
 	public void executeMaintenanceTick() {
 		if (state == WarState.RUNNING) {
 			postWarScoreboard(null);
 		}
 		else {
 			postWarPendingNotice();
 		}
 	}
 
 	public void postWarPendingNotice() {
 
 		final String[] warNotices = new String[] {
 				"Put down the chicken and back away from that melon patch, ",
 				"Nail down your armor and load up on ender pearls, ",
 				"Put away the pickaxe and grab a sword, ",
 				"Don't place another block, ",
 				"Put that giant Lihad statue on hold, ",
 				"Grab a bow and guzzle a potion, ",
 				"Prep your weapons and shut off the redstone cactus massager, ",
 				"Sharpen your armor and buff your sword, ",
 				"Get ready to rumble! ",
 				"Look out below, "
 		};
 
 
 		Date now = new Date();
 		if (now.after(beginTime)) {
 			begin();
 		}
         else {
             long mins = (beginTime.getTime() - now.getTime()) / (1000 * 60);
             int noticeIndex = Conflict.random.nextInt(warNotices.length);
             Bukkit.getServer().broadcastMessage(ChatColor.BLUE.toString() + warNotices[noticeIndex] + ChatColor.GOLD + "WAR is starting in " + mins + " minutes!");
             Bukkit.getServer().broadcastMessage(ChatColor.GRAY.toString() + "Type /war join to get in on the action.");
         }
 	}
 	
 	public void postWarTeams(org.bukkit.command.CommandSender sender){
 		for (Team team : this.teams) {
 			String message = ChatColor.GOLD + team.getName() +" Members | ";
 
 			boolean noComma = true;
 			for (Player player: team.getPlayers()) {
 				if (!noComma)
 					message+=ChatColor.GOLD + ",";
 				else
 					noComma = false;
 				message += ChatColor.GREEN + player.getName();
 			}
 			if (sender != null) {
 				sender.sendMessage(message);
 			}
 			else {
 				Bukkit.getServer().broadcastMessage(message);
 			}
 		}
 	}
 
 	public void postWarScoreboard(org.bukkit.command.CommandSender sender){
 		for (WarNode node : nodes) {
 			String message = ChatColor.GOLD + node.name +" Tally | ";
 
 			if (node.conquered) {
 				message += ChatColor.GREEN + "Conquered by " + node.owner.getName();
 			}
 			else if (node.teamCounters.isEmpty()) {
 				message += ChatColor.DARK_AQUA + "Not claimed by any team!";
 			}
 			else {
 				for (Map.Entry entry : node.teamCounters.entrySet()) {
 					Team team = (Team)entry.getKey();
 					int count = (Integer)entry.getValue();
 
 					String countText = (team == node.owner) ? ("" + ChatColor.YELLOW + "[" + count + "]") : ("" + ChatColor.WHITE + count);
 					message += "" + ChatColor.AQUA + team.getName() + ": " + countText + "; ";
 				}
 			}
 
 			if (sender != null) {
 				sender.sendMessage(message);
 			}
 			else {
 				Bukkit.getServer().broadcastMessage(message);
 			}
 		}
 	}
 
 	public void executeWarTick() {
 
 		if (state == WarState.PENDING) {
 			Date now = new Date();
 			if (now.after(beginTime)) {
 				begin();
 			}
 			return;
 		}
 
 		// Otherwise, war is running
 
 		if (unassignedPlayers.size() > 0) {
 			// Somebody joined late
 			assignPlayers(true);
 		}
 
 		for (WarNode node : nodes) {
 			node.captureTeamTemp = null;
 		}
 
 		Player[] players = Bukkit.getServer().getOnlinePlayers();
 
 		for(Player player : players){
             if (getPlayerTeam(player) == null) {
                 // Ignore anyone not in the war
                 continue;
             }
             if(player.getLocation().getWorld().getName().equalsIgnoreCase("survival")){
 
 				for (WarNode node : nodes) {
 					if (node.conquered) {
 						continue;
 					}
 					if (player.getLocation().distanceSquared(node.location) < 3*3){  
 						if(node.captureTeamTemp == Contested){
 							continue;
 						}
 						else if( getPlayerTeam(player) == node.owner ) {
 							node.captureTeamTemp = getPlayerTeam(player);
 							continue;
 						}
 						if(node.captureTeamTemp == null){
 							node.captureTeamTemp = getPlayerTeam(player);
 							node.captureCounter++;
 							player.sendMessage(ChatColor.GOLD+"Taking point. "+node.captureCounter+"/30");
 						}
 						else if( getPlayerTeam(player) == node.captureTeamTemp ) {
 							node.captureCounter++;
 							player.sendMessage(ChatColor.GOLD+"Taking point. "+node.captureCounter+"/30");
 						}else{
 							node.captureTeamTemp = Contested;
 							node.captureCounter = 0;
 						}
 					}
 				}
 			}
 		}
 
 		boolean unconqueredNodesRemain = false;
 
 		for (WarNode node : nodes) {
 			if (!node.conquered) {
 				unconqueredNodesRemain = true;
 
 				if((node.captureTeamTemp != null) && (node.captureTeamTemp != Contested) && node.captureCounter >= 30) {
 					node.owner = node.captureTeamTemp;
 					node.captureCounter = 0;
 					Bukkit.getServer().broadcastMessage("The " + ChatColor.RED + node.owner.getName() + " have taken control of " + node.name + "!");
 
 					if (!node.teamCounters.containsKey(node.owner)) {
 						node.teamCounters.put(node.owner, 0);
 					}
 				}
 
 				if (node.owner != null) {
 					int counter = node.teamCounters.get(node.owner);
 					counter++;
 
					if (counter > ticksToConquer) {
 						node.conquered = true;
 					}
 					node.teamCounters.put(node.owner, counter);
 				}
 			}
 		}
 		if (!unconqueredNodesRemain) {
 			allNodesConquered = true;
 		}
 	}
 
 	public void endWar() {
 
 		Bukkit.getServer().broadcastMessage(ChatColor.RED+"Lay down your weapons and shag a wench!  The war has ended!");
 
 		for (WarNode node : nodes) {
 
 			Team winner = null;
 
 			if (node.conquered) {
 				winner = node.owner;
 			}
 			else {
 				// Not conquered.  We have to go through the cities and find the highest
 				int highestScore = 0;
 				for (Map.Entry entry : node.teamCounters.entrySet()) {
 					if ((Integer)entry.getValue() > highestScore) {
 						highestScore = (Integer)entry.getValue();
 						winner = (Team)entry.getKey();
 					}
 				}
 			}
 
 			Bukkit.getServer().broadcastMessage("" + ChatColor.GOLD + winner.getName() + ChatColor.GRAY + " has won the " + node.name);
 
 			// Reward is a stack of gold ingots
 			org.bukkit.inventory.ItemStack stack = new org.bukkit.inventory.ItemStack(org.bukkit.Material.GOLD_INGOT, 64);
 			for (Player p : winner.getPlayers()) {
 				if (p.getInventory().firstEmpty() > -1) {
 					p.getInventory().addItem(stack);
 				}
 				else {
 					p.getWorld().dropItem(p.getLocation(), stack);
 				}
 			}
 		}
 	}
 
     // ------------------------------------
     // handlers ------------------
     
     // WARNING: Note about handlers - These handlers are run on a blank instance of War.  The 'this' pointer is NOT valid.
     // If you need any War members or methods, you need to use Conflict.war (and check for null first)
     
 	@org.bukkit.event.EventHandler
 	public static void onEntityDamageByEntity(org.bukkit.event.entity.EntityDamageByEntityEvent event){
 		/////////////////////////////////////////////////////////////////////////////////////////////////////////////
 		//
 		// PvP rules
 		// 
 		// PVP is blocked in the Survival world within 5000 blocks of Lihad City.
 		// PVP is allowed in anywhere further than 5000 blocks, and on other worlds.
 		//
 		// During war, PVP is turned on between members of different teams.  Friendly fire is off, and PVP is off if you're not participating.
 		//
 		// Ops can hurt people everywhere.
 		//
 		// Mayors can hurt their town members in their city.
 		//
 		if(!event.getEntity().getWorld().getName().equals("survival")) {
 			// alternate world
 			return;
 		}
 
 		if (!(event.getEntity() instanceof Player)) {
 			// Not a player
 			return;
 		}
 
 		Player hurt = (Player)event.getEntity();
 
 		if (hurt.getLocation().distanceSquared(hurt.getWorld().getSpawnLocation()) > (5000 * 5000)) {
 			// outside 5k
 			return;
 		}
 
 		if(!(event.getDamager() instanceof Player || (event.getDamager() instanceof Projectile && ((Projectile)event.getDamager()).getShooter() instanceof Player))) {
 			// Attacker is not a player
 			return;
 		}
 
 		Player attacker;
 		if(event.getDamager() instanceof Player) attacker = (Player)event.getDamager();
 		else attacker = (Player)((Projectile)event.getDamager()).getShooter();
 
 		if(attacker.isOp()){
 			// Ops can always hurt you.
 			return;
 		}
 
         if (attacker.getInventory().getHelmet() != null && attacker.getInventory().getHelmet().getType() == org.bukkit.Material.PUMPKIN 
             && hurt.getInventory().getHelmet() != null && hurt.getInventory().getHelmet().getType() == org.bukkit.Material.PUMPKIN) {
             // Pumpkin on the head means willing pvp
             return;
         }
 
 		City city = Conflict.getPlayerCity(attacker.getName());
 		if (city == Conflict.getPlayerCity(hurt.getName()) && city.getMayors().contains(attacker) && city.isInRadius(hurt.getLocation())) {
 			// Mayor hitting own city member
 			return;
 		}
 
 		if (Conflict.war != null) {
 			Team attackerTeam = Conflict.war.getPlayerTeam(attacker);
 			Team hurtTeam = Conflict.war.getPlayerTeam(hurt);
 			if (hurtTeam != null && attackerTeam != null && attackerTeam != hurtTeam) {
 				// Both players in war, on different teams
 				return;
 			}
 		}
 		// Couldn't find an excuse to allow it.  Guess we should cancel the pvp.
 		event.setCancelled(true);
 	}
 
 	@org.bukkit.event.EventHandler
 	public static void onPlayerQuit(org.bukkit.event.player.PlayerQuitEvent event) {
 		if (Conflict.war != null) {
             Conflict.war.unregisterPlayer(event.getPlayer());
 		}
 	}
 
 	@Override
 	public boolean onCommand(org.bukkit.command.CommandSender sender, org.bukkit.command.Command cmd, String string, String[] arg) {
 		if(cmd.getName().equalsIgnoreCase("war")) {
 			if (Conflict.war != null) {
 				if (arg.length == 0) {
 					sender.sendMessage("/war join  -- Join current war");
 					sender.sendMessage("/war stats -- See current war scoreboard");
 					sender.sendMessage("/war teams -- See the current teams");
 				}
 				else if (arg[0].equalsIgnoreCase("stats")) {
 					Conflict.war.postWarScoreboard(sender);
 				}
 				else if (arg[0].equalsIgnoreCase("join")) {
 					if (sender instanceof Player) {
 						sender.sendMessage("Put on your best gear, and load up on ender pearls.  You're signed up!");
 						if (Conflict.war.registerPlayer((Player)sender)) {
 							Bukkit.getServer().broadcastMessage(ChatColor.GREEN.toString() + sender.getName() + ChatColor.WHITE + " straps on a pack and prepares for WAR!");
 						}
 					}
 					else {
 						sender.sendMessage("Console can't join wars! You don't have enough gear.");
 					}
 				}
 				else if (arg[0].equalsIgnoreCase("teams")) {
 					Conflict.war.postWarTeams(sender);
 				}
                 else if (arg[0].equalsIgnoreCase("debug")) {
                     sender.sendMessage("Unassigned players = " + Conflict.war.unassignedPlayers);
                     String message = "Logged players: ";
                     for (Map.Entry entry : Conflict.war.loggedPlayers.entrySet()) {
                         Team t = (Team)entry.getValue();
                         message += (String)entry.getKey() + "(" + (t != null ? t.getName() : null) + ")";
                     }
                     sender.sendMessage(message);
                 }
 			}
 			return true;
 		}
 		return false;
 	}
 
 };
