 package com.ftwinston.Killer;
 
 /*
  * Killer Minecraft
  * a Minecraft Bukkit Mod by 
  * David "Canazza" McQuillan and
  * Andrew Winston "FTWinston" Winston Watkins AKA Winston
  * Created 18/06/2012
  */
 
 import java.util.Arrays;
 import java.util.List;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class Killer extends JavaPlugin
 {
 	// enable this to add extra info messages and debug commands 
 	public static final boolean DEBUG = false;
 	
 	public static Killer instance;
 	Logger log = Logger.getLogger("Minecraft");
 	private Location plinthPressurePlateLocation;
 
 	private EventListener eventListener = new EventListener(this);
 	private WorldManager worldManager;
 	public PlayerManager playerManager;
 	public VoteManager voteManager;
 	public StatsManager statsManager;
 	
 	public final int absMinPlayers = 3;
 	public boolean canChangeGameMode, autoAssignKiller, autoReassignKiller, autoReveal, restartDayWhenFirstPlayerJoins, lateJoinersStartAsSpectator, tweakDeathMessages, banOnDeath, informEveryoneOfReassignedKillers, autoRecreateWorld, stopServerToRecreateWorld, reportStats;
 	public Material[] winningItems;
 	
 	private int compassProcessID, spectatorFollowProcessID;
 	private boolean restarting;
 	
 	public enum GameMode
 	{
 		MysteryKiller
 	}
 	
 	private GameMode gameMode = GameMode.MysteryKiller, nextGameMode = GameMode.MysteryKiller;
 	public GameMode getGameMode() { return gameMode; }
 	public GameMode getNextGameMode() { return nextGameMode; }
 	public void setNextGameMode(GameMode g, CommandSender changedBy)
 	{
 		nextGameMode = g;
 		if ( changedBy == null )
 			getServer().broadcastMessage("The next game mode will be " + g.toString());
 		else
 			getServer().broadcastMessage(changedBy.getName() + " set the next game mode to " + g.toString());
 	}
 	
 	public void onEnable()
 	{	
         instance = this;
         restarting = false;
         
         setupConfiguration();
 		
         getServer().getPluginManager().registerEvents(eventListener, this);
         playerManager = new PlayerManager(this);
         worldManager = new WorldManager(this);
         voteManager = new VoteManager(this);
         statsManager = new StatsManager(this);
         
         // create a plinth in the default world. Always done with the same offset, so if the world already has a plinth, it should just get overwritten.
         plinthPressurePlateLocation = worldManager.createPlinth(getServer().getWorlds().get(0));
         
         // disable spawn protection
         getServer().setSpawnRadius(0);
         
         // set up a task to mess with killers' compasses
         compassProcessID = getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
         	public void run()
         	{
 	        	for ( Player player : instance.getServer().getOnlinePlayers() )
 	        		if ( playerManager.isKiller(player.getName()) && playerManager.isAlive(player.getName()) && player.getInventory().contains(Material.COMPASS) )
 	        			player.setCompassTarget(playerManager.getNearestPlayerTo(player));
         	}
         }, 20, 10);
 	        			
 		spectatorFollowProcessID = getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
         	public void run()
         	{
 	        	for ( Player player : instance.getServer().getOnlinePlayers() )
 	        	{
 	        		String target = playerManager.getFollowTarget(player);
 	        		if ( target != null )
 	        			playerManager.checkFollowTarget(player);
 	        	}
         	}
         }, 40, 40);
 	}
 	
 	public void onDisable()
 	{
 		worldManager.onDisable();
 		getServer().getScheduler().cancelTask(compassProcessID);
 		getServer().getScheduler().cancelTask(spectatorFollowProcessID);
 	}
 	
 	private void setupConfiguration()
 	{
 		getConfig().addDefault("defaultGameMode", "MysteryKiller");
 		getConfig().addDefault("canChangeGameMode", true);
 		getConfig().addDefault("autoAssign", false);
 		getConfig().addDefault("autoReassign", false);
 		getConfig().addDefault("autoReveal", true);
 		getConfig().addDefault("restartDay", true);
 		getConfig().addDefault("lateJoinersStartAsSpectator", false);
 		getConfig().addDefault("tweakDeathMessages", true);
 		getConfig().addDefault("banOnDeath", false);
 		getConfig().addDefault("informEveryoneOfReassignedKillers", true);
 		getConfig().addDefault("reportStats", true);
 		getConfig().addDefault("winningItems", Arrays.asList(Material.BLAZE_ROD.getId(), Material.GHAST_TEAR.getId()));		
 		
 		
 		getConfig().addDefault("autoRecreateWorld", false);
 		getConfig().addDefault("stopServerToRecreateWorld", false);
 		getConfig().options().copyDefaults(true);
 		saveConfig();
 		
 		try
 		{
			gameMode = nextGameMode = GameMode.valueOf(getConfig().getString("defaultGameMode"));
 		}
 		catch ( Exception ex )
 		{
 			log.warning("Invalid value for defaultGameMode: " + getConfig().getString("defaultGameMode"));
 			gameMode = nextGameMode = GameMode.MysteryKiller;
 		}
 		
 		canChangeGameMode = getConfig().getBoolean("canChangeGameMode");
 		autoAssignKiller = getConfig().getBoolean("autoAssign");
 		autoReassignKiller = getConfig().getBoolean("autoReassign");
 		autoReveal = getConfig().getBoolean("autoReveal");
 		restartDayWhenFirstPlayerJoins = getConfig().getBoolean("restartDay");
 		lateJoinersStartAsSpectator = getConfig().getBoolean("lateJoinersStartAsSpectator");
 		tweakDeathMessages = getConfig().getBoolean("tweakDeathMessages");
 		banOnDeath = getConfig().getBoolean("banOnDeath");
 		informEveryoneOfReassignedKillers = getConfig().getBoolean("informEveryoneOfReassignedKillers");
 		autoRecreateWorld = getConfig().getBoolean("autoRecreateWorld");
 		stopServerToRecreateWorld = getConfig().getBoolean("stopServerToRecreateWorld");
 		reportStats = getConfig().getBoolean("reportStats");
 
 		List<Integer> winningItemIDs = getConfig().getIntegerList("winningItems"); 
 		winningItems = new Material[winningItemIDs.size()];
 		for ( int i=0; i<winningItems.length; i++ )
 		{
 			Material mat = Material.getMaterial(winningItemIDs.get(i));
 			if ( mat == null )
 			{
 				mat = Material.BLAZE_ROD;
 				log.warning("Material ID " + winningItemIDs.get(i) + " not recognized.");
 			} 
 			winningItems[i] = mat;
 		}
 	}
 	
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
 	{
 		if (cmd.getName().equalsIgnoreCase("spec"))
 		{
 			if ( !(sender instanceof Player) )
 				return false;
 			
 			if ( args.length == 0 )
 			{
 				sender.sendMessage("Usage: /spec main, /spec nether, /spec <player name>, or /spec follow");
 				return true;
 			}
 			
 			if ( !playerManager.isSpectator(sender.getName()) )
 			{
 				if ( DEBUG && args[0].equals("addme") )
 				{
 					playerManager.setAlive((Player)sender, false);
 					return true;
 				}
 					
 				sender.sendMessage("Only spectators can use this command");
 				return true;
 			}
 			
 			Player player = (Player)sender;
 			if ( args[0].equalsIgnoreCase("main") )
 			{
 				playerManager.putPlayerInWorld(player, getServer().getWorlds().get(0), true);
 			}
 			else if ( args[0].equalsIgnoreCase("nether") )
 			{
 				if ( getServer().getWorlds().size() > 1 )
 					playerManager.putPlayerInWorld(player, getServer().getWorlds().get(1), true);
 				else
 					sender.sendMessage("Nether world not found, please try again");
 			}
 			else if ( args[0].equalsIgnoreCase("follow") )
 			{
 				if ( playerManager.getFollowTarget(player) == null )
 				{
 					playerManager.setFollowTarget(player, playerManager.getDefaultFollowTarget());
 					playerManager.checkFollowTarget(player);
 					sender.sendMessage("Follow mode enabled. Type " + ChatColor.YELLOW + "/spec follow" + ChatColor.RESET + " again to exist follow mode, or /spec <player name> to follow another player.");
 				}
 				else
 				{
 					playerManager.setFollowTarget(player, null);
 					sender.sendMessage("Follow mode disabled.");
 				}
 			}
 			else
 			{
 				Player other = getServer().getPlayer(args[0]);
 				if ( other == null || !other.isOnline() )
 					sender.sendMessage("Player not found: " + args[0]);
 				else if ( playerManager.getFollowTarget(player) != null )
 					playerManager.setFollowTarget(player, other.getName());
 				
 				playerManager.moveToSee(player, other);
 			}
 			
 			return true;
 		}
 		else if (cmd.getName().equalsIgnoreCase("vote"))
 		{
 			if ( sender instanceof Player )
 				voteManager.showVoteMenu((Player)sender);
 			return true;
 		}
 		else if (cmd.getName().equalsIgnoreCase("killer"))
 		{
 			if ( sender instanceof Player && !((Player)sender).isOp() )
 			{
 				sender.sendMessage("You must be a server op to run this command");
 				return true;
 			}
 			
 			if ( args.length == 0 )
 			{
 				sender.sendMessage("Usage: /killer add, /killer clear, /killer reallocate, /killer restart");
 				return true;
 			}
 			
 			if ( args[0].equalsIgnoreCase("add") )
 			{
 				playerManager.assignKiller(true, sender);
 			}
 			else if ( args[0].equalsIgnoreCase("clear") )
 			{
 				playerManager.clearKillers(sender);
 			}
 			else if ( args[0].equalsIgnoreCase("reallocate") )
 			{
 				playerManager.clearKillers(sender);
 				playerManager.assignKiller(true, sender);
 			}
 			else if ( args[0].equalsIgnoreCase("restart") )
 			{
 				if ( !restarting )
 				{
 					getServer().broadcastMessage(sender.getName() + " is restarting the game");
 					restartGame(false, true);
 				}
 			}
 			else
 				sender.sendMessage("Invalid parameter: " + args[0] + " - type /killer to list allowed parameters");
 			
 			return true;
 		}
 		
 		return false;
 	}
 
 	public Location getPlinthLocation()
 	{
 		return plinthPressurePlateLocation;
 	}
 	
 	public void restartGame(boolean useSameWorld, boolean resetItems)
 	{
 		if ( restarting )
 			return;
 		
 		// if the stats manager is tracking, then the game didn't finish "properly" ... this counts as an "aborted" game
 		if ( statsManager.isTracking )
 			statsManager.gameFinished(playerManager.numSurvivors(), 3, 0);
 			
 		if ( useSameWorld )
 		{
 			// what should we do to the world on restart if we're not deleting it?
 			// remove all user-placed portal, obsidian, chests, dispensers and furnaces? We'd have to track them being placed.
 			
 			getServer().broadcastMessage("Game is restarting, using the same world...");
 
 			boolean first = true; // only check the spawn point is valid the first time 
 			World defaultWorld = getServer().getWorlds().get(0);
 			for ( Player player : getServer().getOnlinePlayers() )
 			{
 				playerManager.putPlayerInWorld(player, defaultWorld, first);
 				first = false;
 			}
 			
 			playerManager.reset(resetItems);
 		}
 		else if ( stopServerToRecreateWorld )
 		{
 			getServer().shutdown();
 		}
 		else
 		{
 			restarting = true;
 			getServer().broadcastMessage("Game is restarting, please wait while the world is deleted and a new one is prepared...");
 			playerManager.reset(resetItems);
 			worldManager.deleteWorlds(new Runnable() {
 				public void run()
 				{
 					World defaultWorld = getServer().getWorlds().get(0);
 					plinthPressurePlateLocation = worldManager.createPlinth(defaultWorld);
 					playerManager.reset(false);
 					restarting = false;
 				}
 			});
 		}
 	}
 
 	public String tidyItemName(Material m)
 	{
 		return m.name().toLowerCase().replace('_', ' ');
 	}
 }
