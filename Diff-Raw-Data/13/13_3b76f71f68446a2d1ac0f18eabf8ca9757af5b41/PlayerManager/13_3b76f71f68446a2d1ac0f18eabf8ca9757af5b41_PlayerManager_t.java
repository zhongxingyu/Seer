 package com.ftwinston.Killer;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.World;
 import org.bukkit.World.Environment;
 import org.bukkit.block.Block;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.PlayerInventory;
 import org.bukkit.util.BlockIterator;
 import org.bukkit.util.Vector;
 
 public class PlayerManager
 {
 	public static PlayerManager instance;
 	private Killer plugin;
 	private Random random;
 	private int killerAssignProcess;
 	
 	public PlayerManager(Killer _plugin)
 	{
 		this.plugin = _plugin;
 		instance = this;
 		random = new Random();
 		
     	startCheckAutoAssignKiller();
 	}
 	
 	private void startCheckAutoAssignKiller()
 	{
 		if ( !plugin.autoAssignKiller )
 			return;
 		
 		killerAssignProcess = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
 			long lastRun = 0;
 			public void run()
 			{
 				long time = plugin.getServer().getWorlds().get(0).getTime();
 			
 				if ( time < lastRun ) // time of day has gone backwards! Must be a new day! See if we need to add a killer
 					assignKillers(null);
 				
 				lastRun = time;
 			}
 		}, 600L, 100L); // initial wait: 30s, then check every 5s (still won't try to assign unless it detects a new day starting)
 	}
 	
 	private List<String> alive = new ArrayList<String>();
 	private List<String> killers = new ArrayList<String>();
 	private Map<String, String> spectators = new LinkedHashMap<String, String>();
 	
 	public int numSurvivors() { return alive.size(); }
 	public List<String> getSurvivors() { return alive; }
 	
 	public int numKillersAssigned() { return killers.size(); }
 	public int determineNumberOfKillersToAdd()
 	{
 		// if we don't have enough players for a game, we don't want to assign a killer
 		if ( alive.size() < plugin.getGameMode().absMinPlayers() )
 			return 0;
 		
 		int numAliveKillers = 0;
 		for ( String name : alive )
 			if ( isKiller(name) )
 				numAliveKillers ++;
 		
 		return plugin.getGameMode().determineNumberOfKillersToAdd(alive.size(), killers.size(), numAliveKillers);
 	}
 	
 	public void reset(boolean resetInventories)
 	{
 		countdownStarted = false;
 		
 		alive.clear();
 		for ( Player player : plugin.getServer().getOnlinePlayers() )
 		{
 			resetPlayer(player, resetInventories);
 			setAlive(player,true);
 		}
 		
 		// don't do this til after, so addAlive can remove spectator effects if needed
 		spectators.clear();
 		
 		// inform all killers that they're not any more, just to be clear
 		for ( String killerName : killers )
 		{
 			Player killerPlayer = Bukkit.getServer().getPlayerExact(killerName);
 			if ( killerPlayer != null && killerPlayer.isOnline() )
 				killerPlayer.sendMessage(ChatColor.YELLOW + "You are no longer " + (killers.size() == 1 ? "the" : "a") + " killer.");
 		}
 		killers.clear();
 		
 		if ( plugin.banOnDeath )
 			for ( OfflinePlayer player : plugin.getServer().getBannedPlayers() )
 				player.setBanned(false);
 		
 		if ( killerAssignProcess != -1 )
 		{
 			plugin.getServer().getScheduler().cancelTask(killerAssignProcess);
 			killerAssignProcess = -1;
 		}
 		startCheckAutoAssignKiller();
 	}
 	
 	public boolean assignKillers(CommandSender sender)
 	{
 		int numToAdd = determineNumberOfKillersToAdd();
 		if ( numToAdd > 0 )  // don't inform people of any killer being added apart from the first one, unless the config is set
 			return assignKillers(numToAdd, sender);
 		return false;
 	}
 	
 	private boolean assignKillers(int numKillers, CommandSender sender)
 	{
 		countdownStarted = false;
 		
 		Player[] players = plugin.getServer().getOnlinePlayers();
 		if ( players.length < plugin.getGameMode().absMinPlayers() )
 		{
 			String message = "Insufficient players to assign a killer. A minimum of 3 players are required.";
 			if ( sender == null )
 				plugin.getServer().broadcastMessage(message);
 			else
 				sender.sendMessage(message);
 			return false;
 		}
 		
 		if ( plugin.getGameMode().informOfKillerAssignment(this) && !plugin.getGameMode().informOfKillerIdentity() )
 		{
 			String message;
 			if ( numKillers > 1 )
 				message = numKillers + " killers have";
 			else
 				message = "A killer has";
 			
 			message += " been randomly assigned";
 			
 			
 			if ( sender != null )
 				message += " by " + sender.getName();
 			message += " - nobody but the";
 			
 			if ( numKillers > 1 || numKillersAssigned() > 0 )
 				message += " killers";
 			else
 				message += " killer";
 			message += " knows who they are.";
 
 			plugin.getServer().broadcastMessage(message);
 		}
 		
 		int availablePlayers = 0;
 		for ( String name : alive )
 		{
 			if ( isKiller(name) )
 				continue;
 			
 			Player player = plugin.getServer().getPlayerExact(name);
 			if ( player != null && player.isOnline() )
 				availablePlayers ++;
 		}
 		
 		if ( availablePlayers == 0 )
 			return false;
 		
 		if ( !plugin.statsManager.isTracking )
 			plugin.statsManager.gameStarted(numSurvivors());
 		
 		int[] killerIndices;
 		if ( numKillers >= availablePlayers )
 		{// should this ever happen? seriously? everyone's a killer. that's screwed.
 			 killerIndices = new int[availablePlayers];
 			 for ( int i=0; i<availablePlayers; i++ )
 				 killerIndices[i] = i;
 		}
 		else
 		{
 			killerIndices = new int[numKillers];
 			for ( int i=0; i<killerIndices.length; i++ )
 			{
 				int rand;
 				boolean ok;
 				do
 				{
 					rand = random.nextInt(availablePlayers);
 					ok = true;
 					for ( int j=0; j<i; j++ )
 						if ( rand == killerIndices[j] ) // already used this one, it's not good to use again
 						{
 							ok = false;
 							break;
 						}
 
 				} while ( !ok );
 				killerIndices[i] = rand;
 			}
 		}
 		
 		Arrays.sort(killerIndices);
 		
 		int num = 0, nextIndex = 0;
 		for ( Player player : players )
 		{
 			if ( isKiller(player.getName()) || isSpectator(player.getName()) )
 				continue;
 		
 			if ( num == killerIndices[nextIndex] )
 			{
 				if(!killers.contains(player.getName()))
 					killers.add(player.getName());
 				String message = ChatColor.RED + "You are ";
 				message += numKillers > 1 || killers.size() > 1 ? "now a" : "the";
 				message += " killer!";
 				
 				if ( !plugin.getGameMode().informOfKillerAssignment(this) && !plugin.getGameMode().informOfKillerIdentity() )
 					message += ChatColor.WHITE + " No one else has been told a new killer was assigned.";
 					
 				player.sendMessage(message);
 				
 				if ( plugin.getGameMode().informOfKillerIdentity() )
 				{
 					if ( plugin.getGameMode().informOfKillerAssignment(this) )
 						plugin.getServer().broadcastMessage(player.getName() + " is the killer!");
 					colorPlayerName(player, ChatColor.RED);
 				}
 				
 				plugin.getGameMode().prepareKiller(player, this);
 				
 				nextIndex ++;
 				
 				plugin.statsManager.killerAdded();
 				if ( sender != null )
 					plugin.statsManager.killerAddedByAdmin();
 			}
 			else if ( !isKiller(player.getName()) )
 			{
 				plugin.getGameMode().prepareFriendly(player, this);
 				
 				if ( plugin.getGameMode().informOfKillerIdentity() )
 					colorPlayerName(player, ChatColor.BLUE);
 				else if ( plugin.getGameMode().informOfKillerAssignment(this) )
 					player.sendMessage(ChatColor.YELLOW + "You are not " + ( numKillers > 1 || killers.size() > 1 ? "a" : "the") + " killer.");
 			}
			
 			num++;
 		}
 		
 		return true;
 	}
 	
 	private void colorPlayerName(Player player, ChatColor color)
 	{		
 		player.setDisplayName(color + ChatColor.stripColor(player.getDisplayName()));
 		player.setPlayerListName(color + ChatColor.stripColor(player.getPlayerListName()));
 	}
 
 	public void playerJoined(Player player)
 	{		
 		for(String spec:spectators.keySet())
 			if(spec != player.getName())
 			{
 				Player other = plugin.getServer().getPlayerExact(spec);
 				if ( other != null && other.isOnline() )
 					player.hidePlayer(other);
 			}
 		
 		if ( isSpectator(player.getName()) )
 		{
 			player.sendMessage("Welcome back. You are now a spectator. You can fly, but can't be seen or interact. Type " + ChatColor.YELLOW + "/spec" + ChatColor.RESET + " to list available commands.");
 			setAlive(player,false);
 		}
 		else if ( plugin.getGameMode().playerJoined(player, this, !isAlive(player.getName()), isKiller(player.getName()), killers.size()) )
 		{
 			setAlive(player,true); // they're alive
 			
 			if ( plugin.getGameMode().informOfKillerIdentity() )
 				colorPlayerName(player, isKiller(player.getName()) ? ChatColor.RED : ChatColor.BLUE);
 			
 			if ( numKillersAssigned() > 0 && !isAlive(player.getName()) ) // they're late-joining an in-progress game
 				plugin.statsManager.playerJoinedLate();
 		}
 		else
 			setAlive(player,false); // they're a spectator
 		
 		if ( numKillersAssigned() == 0 )
 		{
 			checkImmediateKillerAssignment();
 			
 			if ( plugin.restartDayWhenFirstPlayerJoins && plugin.getServer().getOnlinePlayers().length == 1 )
 				plugin.getServer().getWorlds().get(0).setTime(0);
 		}
 		else
 			checkPlayerCompassTarget(player);
 	}
 	
 	boolean countdownStarted = false;
 	public void checkImmediateKillerAssignment()
 	{
 		if ( !countdownStarted && plugin.getGameMode().immediateKillerAssignment() && plugin.getServer().getOnlinePlayers().length >= plugin.getGameMode().absMinPlayers() )
 		{
 			plugin.getServer().broadcastMessage("Killer assignment in 30 seconds...");
 			countdownStarted = true;
 			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
 				@Override
 				public void run()
 				{
 					assignKillers(null);
 				}
 			}, 600L);
 		}
 	}
 	
 	// player either died, or disconnected and didn't rejoin in the required time
 	public void playerKilled(String playerName)
 	{
 		Player player = plugin.getServer().getPlayerExact(playerName);
 		if ( player != null && player.isOnline() )
 		{
 			setAlive(player, false);
 		}
 		else // player disconnected ... move them to spectator in our records, in case they reconnect
 		{
 			if(alive.contains(playerName) )
 				alive.remove(playerName);
 			if(!spectators.containsKey(playerName))
 				spectators.put(playerName, null);
 		}
 		
 		if ( plugin.banOnDeath )
 		{
 			player.setBanned(true);
 			player.kickPlayer("You died, and are now banned until the end of the game");
 		}
 		
 		plugin.getGameMode().checkForEndOfGame(this, null, null);
 	}
 	
 	public void gameFinished(boolean killerWon, boolean friendliesWon, String winningPlayerName, Material winningItem)
 	{
 		String message;
 		int numFriendlies = numSurvivors() + spectators.size() - killers.size();
 		
 		if ( winningItem != null )
 		{
 			if ( friendliesWon )
 				message = (winningPlayerName == null ? "The " + plugin.getGameMode().describePlayer(false) : winningPlayerName) + (numFriendlies > 1 ? "s brought " : " brought ") + (winningItem == null ? "an item" : "a " + plugin.tidyItemName(winningItem)) + " to the plinth - the " + plugin.getGameMode().describePlayer(false) + (numFriendlies > 1 ? "s win! " : " wins");
 			else
 				message = (winningPlayerName == null ? "The " + plugin.getGameMode().describePlayer(true) : winningPlayerName) + (killers.size() > 1 ? "s win! " : " wins") + " brought " + (winningItem == null ? "an item" : "a " + plugin.tidyItemName(winningItem)) + " to the plinth - the " + plugin.getGameMode().describePlayer(true) + (killers.size() > 1 ? "s win! " : " wins");
 		}
 		else if ( killers.size() == 0 ) // some mode (e.g. Contact Killer) might not assign specific killers. In this case, we only care about the winning player
 		{
 			if ( numSurvivors() == 1 )
 				message = "Only one player left standing, " + alive.get(0) + " wins!";
 			else if ( numSurvivors() == 0 )
 				message = "No players survived, game drawn!";
 			else
 				return; // multiple people still alive... ? don't end the game.
 		}
 		else if ( killerWon )
 		{
 			if ( numFriendlies > 1 )
 				message = "All of the " + plugin.getGameMode().describePlayer(false) + "s have";
 			else
 				message = "The " + plugin.getGameMode().describePlayer(false) + " has";
 			message += " been killed, the " + plugin.getGameMode().describePlayer(true);
 			
 			if ( killers.size() > 1 )
 			{
 				message += "s win!";
 
 				if ( winningPlayerName != null )
 					message += "\nWinning kill by " + winningPlayerName + ".";
 			}
 			else
 				message += " wins!";
 		}
 		else if ( friendliesWon )
 		{
 			if ( killers.size() > 1 )
 				message =  "All of the " + plugin.getGameMode().describePlayer(true) + "s have";
 			else
 				message = "The " + plugin.getGameMode().describePlayer(true) + " has";
 		
 			message += " been killed, the " + plugin.getGameMode().describePlayer(false);
 
 			if ( numFriendlies > 1 )
 			{
 				message += "s win!";
 
 				if ( winningPlayerName != null )
 					message += "\nWinning kill by " + winningPlayerName + ".";
 			}
 			else
 				message += " wins!";
 		}
 		else
 			message = "No players survived, game drawn!";
 		
 		plugin.statsManager.gameFinished(numSurvivors(), killerWon ? 1 : (friendliesWon ? 2 : 0), winningItem == null ? 0 : winningItem.getId());
 		
 		plugin.getServer().broadcastMessage(ChatColor.YELLOW + message);
 		if ( plugin.autoReveal )
 			clearKillers(null);
 
 		if ( winningItem != null || plugin.autoRecreateWorld || plugin.voteManager.isInVote() || plugin.getServer().getOnlinePlayers().length == 0 )
 		{	// plinth victory or other scenario where we don't want a vote schedule a game restart in 10 secs, with a new world
 			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
 	
 				@Override
 				public void run()
 				{
 	    			plugin.restartGame(false, true);					
 				}
 	    	}, 200L);
 		}
 		else
 		{// start a vote that's been set up to call restartGame with true / false parameter 
 			Runnable yesResult = new Runnable() {
 				public void run()
 				{
 					plugin.restartGame(true, false);
 				}
 			};
 			
 			Runnable noResult = new Runnable() {
 				public void run()
 				{
 					plugin.restartGame(false, true);
 				}
 			};
 			
 			plugin.voteManager.startVote("Start next round with the same world & items?", null, yesResult, noResult, noResult);
 		}
 	}
 	
 	public void clearKillers(CommandSender sender)
 	{
 		String message;
 		
 		if ( numKillersAssigned() > 0 )
 		{
 			if ( !plugin.getGameMode().informOfKillerIdentity() )
 			{
 				message = ChatColor.RED + (sender == null ? "Revealed: " : "Revealed by " + sender.getName() + ": ");
 				if ( killers.size() == 1 )
 					message += killers.get(0) + " was the killer!";
 				else
 				{
 					message += "The killers were ";
 					message += killers.get(0);
 					
 					for ( int i=1; i<killers.size(); i++ )
 					{
 						message += i == killers.size()-1 ? " and " : ", ";
 						message += killers.get(i);
 					}
 					
 					message += "!";
 				}
 				plugin.getServer().broadcastMessage(message);
 			}
 			killers.clear();
 			
 			
 			// this game was "aborted"
 			if ( plugin.statsManager.isTracking )
 				plugin.statsManager.gameFinished(numSurvivors(), 3, 0);
 		}
 		else
 		{
 			message = "No killers are currently assigned!";
 			if ( sender == null )
 				plugin.getServer().broadcastMessage(message);
 			else
 				sender.sendMessage(message);
 		}	
 	}
 	
 	public boolean isSpectator(String player)
 	{
 		return spectators.containsKey(player);
 	}
 
 	public boolean isAlive(String player)
 	{
 		return alive.contains(player);
 	}
 
 	public boolean isKiller(String player)
 	{
 		return killers.contains(player);
 	}
 
 	public void setAlive(Player player, boolean bAlive)
 	{
 		if ( bAlive )
 		{
 			// you shouldn't stop being able to fly in creative mode, cos you're (hopefully) only there for testing
 			if ( player.getGameMode() != GameMode.CREATIVE )
 			{
 				player.setFlying(false);
 				player.setAllowFlight(false);
 			}
 
 			// fixme: reconnecting killer in invisible killer mode will become visible
 			makePlayerVisibleToAll(player);
 			
 			if(!alive.contains(player.getName()))
 				alive.add(player.getName());
 			if(spectators.containsKey(player.getName()))
 				spectators.remove(player.getName());
 		}
 		else
 		{
 			player.setAllowFlight(true);
 			player.getInventory().clear();
 			makePlayerInvisibleToAll(player);
 			
 			if(alive.contains(player.getName()))
 				alive.remove(player.getName());
 			if(!spectators.containsKey(player.getName()))
 			{
 				spectators.put(player.getName(), null);
 				player.sendMessage("You are now a spectator. You can fly, but can't be seen or interact. Type " + ChatColor.YELLOW + "/spec" + ChatColor.RESET + " to list available commands.");
 			}
 		}
 	}
 	
 	public void makePlayerInvisibleToAll(Player player)
 	{
 		for(Player p : plugin.getServer().getOnlinePlayers())
 			p.hidePlayer(player);
 	}
 	
 	public void makePlayerVisibleToAll(Player player)
 	{
 		for(Player p :  plugin.getServer().getOnlinePlayers())
 			p.showPlayer(player);
 	}
 
 	public void resetPlayer(Player player, boolean resetInventory)
 	{
 		player.setTotalExperience(0);
 		player.setHealth(player.getMaxHealth());
 		player.setFoodLevel(20);
 		player.setSaturation(20);
 		player.setExhaustion(0);
 		player.setFireTicks(0);
 		
 		if ( resetInventory )
 		{
 			PlayerInventory inv = player.getInventory();
 			inv.clear();
 			inv.setHelmet(null);
 			inv.setChestplate(null);
 			inv.setLeggings(null);
 			inv.setBoots(null);		
 		}
 		
 		player.setDisplayName(ChatColor.stripColor(player.getDisplayName()));
 		player.setPlayerListName(ChatColor.stripColor(player.getPlayerListName()));
 	}
 	
 	public void putPlayerInWorld(Player player, World world, boolean checkSpawn)
 	{	
 		// check spawn location is clear and is on the ground!
 		// update it if its not!
 		Location spawn = world.getSpawnLocation();
 		
 		if ( checkSpawn )
 		{
 			if ( spawn.getBlock().isEmpty() )
 			{	// while the block below spawn is empty, move down one
 				Block b = world.getBlockAt(spawn.getBlockX(), spawn.getBlockY() - 1, spawn.getBlockZ()); 
 				while ( b.isEmpty() && !b.isLiquid() )
 				{
 					spawn.setY(spawn.getY()-1);
 					b = world.getBlockAt(spawn.getBlockX(), spawn.getBlockY() - 1, spawn.getBlockZ());
 				}
 			}
 			else
 			{	// keep moving up til we have two empty blocks
 				do
 				{
 					do
 					{
 						spawn.setY(spawn.getY()+1);
 					}
 					while ( !spawn.getBlock().isEmpty() );
 				
 					// that's one empty, see if there's another (or if we're at the tom of the world)
 					if ( spawn.getBlockY() >= world.getMaxHeight() || world.getBlockAt(spawn.getBlockX(), spawn.getBlockY() + 1, spawn.getBlockZ()).isEmpty() )
 						break;
 				} while (true);
 				
 				world.setSpawnLocation(spawn.getBlockX(), spawn.getBlockY(), spawn.getBlockZ());
 			}
 		}
 		
 		if ( !player.isDead() || player.getWorld() != world )
 			player.teleport(spawn);
 	}
 	
     public void checkPlayerCompassTarget(Player player)
     {
     	if ( plugin.getGameMode().usesPlinth() && player.getWorld().getEnvironment() == Environment.NORMAL )
 		{
 			if ( isKiller(player.getName()) )
 			{
 				if ( !plugin.getGameMode().killersCompassPointsAtFriendlies() )
 					player.setCompassTarget(plugin.getPlinthLocation());
 			}
 			else if ( !plugin.getGameMode().friendliesCompassPointsAtKiller() )
 				player.setCompassTarget(plugin.getPlinthLocation());
 		}
     }
 
 	public Location getNearestPlayerTo(Player player, boolean findFriendlies)
 	{
 		Location nearest = null;
 		double nearestDistSq = Double.MAX_VALUE;
 		World playerWorld = player.getWorld();
 		for ( Player other : plugin.getServer().getOnlinePlayers() )
 		{
 			if ( other == player || other.getWorld() != playerWorld || !isAlive(other.getName()))
 				continue;
 			
 			if ( findFriendlies == isKiller(other.getName()) )
 				continue;
 				
 			double distSq = other.getLocation().distanceSquared(player.getLocation());
 			if ( distSq < nearestDistSq )
 			{
 				nearestDistSq = distSq;
 				nearest = other.getLocation();
 			}
 		}
 		
 		// if there's no player to point at, point in a random direction
 		if ( nearest == null )
 		{
 			nearest = player.getLocation();
 			nearest.setX(nearest.getX() + random.nextDouble() * 32 - 16);
 			nearest.setZ(nearest.getZ() + random.nextDouble() * 32 - 16);
 		}
 		return nearest;
 	}
 	
 	public String getFollowTarget(Player player)
 	{
 		if ( spectators.containsKey(player.getName()) )
 			return spectators.get(player.getName());
 		return null;
 	}
 	
 	public void setFollowTarget(Player player, String target)
 	{
 		spectators.put(player.getName(), target);
 	}
 	
 	private final double maxFollowSpectateRangeSq = 40 * 40, maxAcceptableOffsetDot = 0.65;
 	private final int maxSpectatePositionAttempts = 5, idealFollowSpectateRange = 20;
 	
 	public void checkFollowTarget(Player player)
 	{
 		String targetName = spectators.get(player.getName()); 
 		if ( targetName == null )
 			return; // don't have a target, don't want to get moved to it
 		
 		Player target = plugin.getServer().getPlayerExact(targetName);
 		if ( !isAlive(targetName) || target == null || !target.isOnline() )
 		{
 			targetName = getDefaultFollowTarget();
 			setFollowTarget(player, targetName);
 			if ( targetName == null )
 				return; // if there isn't a valid follow target, don't let it try to move them to it
 
 			target = plugin.getServer().getPlayerExact(targetName);
 			if ( !isAlive(targetName) || target == null || !target.isOnline() )
 			{// something went wrong with the default follow target, so just clear it
 				setFollowTarget(player, null);
 				return;
 			}
 		}
 		
 		Location specLoc = player.getEyeLocation();
 		Location targetLoc = target.getEyeLocation();
 		
 		// check they're in the same world
 		if ( specLoc.getWorld() != targetLoc.getWorld() )
 		{
 			moveToSee(player, target);
 			return;
 		}
 		
 		// then check the distance is appropriate
 		double targetDistSqr = specLoc.distanceSquared(targetLoc); 
 		if ( targetDistSqr > maxFollowSpectateRangeSq )
 		{
 			moveToSee(player, target);
 			return;
 		}
 		
 		// check if they're facing the right way
 		Vector specDir = specLoc.getDirection().normalize();
 		Vector dirToTarget = targetLoc.subtract(specLoc).toVector().normalize();
 		if ( specDir.dot(dirToTarget) < maxAcceptableOffsetDot )
 		{
 			moveToSee(player, target);
 			return;
 		}
 		
 		// then do a ray trace to see if there's anything in the way
         Iterator<Block> itr = new BlockIterator(specLoc.getWorld(), specLoc.toVector(), dirToTarget, 0, (int)Math.sqrt(targetDistSqr));
         while (itr.hasNext())
         {
             Block block = itr.next();
             if ( !block.isEmpty() )
 			{
 				moveToSee(player, target);
 				return;
 			}
         }
 	}
 	
 	public void moveToSee(Player player, Player target)
 	{
 		if ( target == null || !target.isOnline() )
 			return;
 
 		Location targetLoc = target.getEyeLocation();
 		
 		Location bestLoc = targetLoc;
 		double bestDistSq = 0;
 		
 		// try a few times to move away in a random direction, see if we can make it up to idealFollowSpectateRange
 		for ( int i=0; i<maxSpectatePositionAttempts; i++ )
 		{
 			// get a mostly-horizontal direction
 			Vector dir = new Vector(random.nextDouble()-0.5, random.nextDouble() * 0.35 - 0.1, random.nextDouble()-0.5).normalize();
 			if ( dir.getY() > 0.25 )
 			{
 				dir.setY(0.25);
 				dir = dir.normalize();
 			}
 			else if ( dir.getY() < -0.1 )
 			{
 				dir.setY(-0.1);
 				dir = dir.normalize();
 			}
 			
 			Location pos = targetLoc;
 			// keep going until we reach the ideal distance or hit a non-empty block
 			Iterator<Block> itr = new BlockIterator(targetLoc.getWorld(), targetLoc.toVector(), dir, 0, idealFollowSpectateRange);
 	        while (itr.hasNext())
 	        {
 	            Block block = itr.next();
 	            if ( !block.isEmpty() )
 	            	break;
 	            
 	            if ( targetLoc.getWorld().getBlockAt(block.getLocation().getBlockX(), block.getLocation().getBlockY()-1, block.getLocation().getBlockZ()).isEmpty() )
 	            	pos = block.getLocation().add(0.5, player.getEyeHeight()-1, 0.5);
 	        }
 	        
 	        if ( !itr.hasNext() ) // we made it the max distance! use this!
 	        {
 	        	bestLoc = pos;
 	        	break;
 	        }
 	        else
 	        {
 	        	double distSq = pos.distanceSquared(targetLoc); 
 	        	if ( distSq > bestDistSq )
 		        {
 		        	bestLoc = pos;
 		        	bestDistSq = distSq; 
 		        }
 	        }
 		}
 
 		// as we're dealing in eye position thus far, reduce the Y to get the "feet position"
 		bestLoc.setY(bestLoc.getY() - player.getEyeHeight());
 		
 		// work out the yaw
 		double xDif = targetLoc.getX() - bestLoc.getX();
 		double zDif = targetLoc.getZ() - bestLoc.getZ();
 		
 		if ( xDif == 0 )
 		{
 			if ( zDif >= 0 )
 				bestLoc.setYaw(270);
 			else
 				bestLoc.setYaw(90);
 		}
 		else if ( xDif > 0 )
 		{
 			if ( zDif >= 0)
 				bestLoc.setYaw(270f + (float)Math.toDegrees(Math.atan(zDif / xDif)));
 			else
 				bestLoc.setYaw(180f + (float)Math.toDegrees(Math.atan(xDif / -zDif)));
 		}
 		else
 		{
 			if ( zDif >= 0)
 				bestLoc.setYaw((float)(Math.toDegrees(Math.atan(-xDif / zDif))));
 			else
 				bestLoc.setYaw(90f + (float)Math.toDegrees(Math.atan(zDif / xDif)));
 		}
 		
 		// work out the pitch
 		double horizDist = Math.sqrt(xDif * xDif + zDif * zDif);
 		double yDif = targetLoc.getY() - bestLoc.getY();
 		if ( horizDist == 0 )
 			bestLoc.setPitch(0);
 		else if ( yDif >= 0 )
 			bestLoc.setPitch(-(float)Math.toDegrees(Math.atan(yDif / horizDist)));
 		else
 			bestLoc.setPitch((float)Math.toDegrees(Math.atan(-yDif / horizDist)));
 		
 		// set them as flying so they don't fall from this position, then do the teleport
 		player.setFlying(true);
 		player.teleport(bestLoc);
 	}
 	
 	public String getDefaultFollowTarget()
 	{
 		for ( String name : alive )
 		{
 			Player player = plugin.getServer().getPlayerExact(name);
 			if ( player != null && player.isOnline() )
 				return name;
 		}
 		return null;
 	}
 }
