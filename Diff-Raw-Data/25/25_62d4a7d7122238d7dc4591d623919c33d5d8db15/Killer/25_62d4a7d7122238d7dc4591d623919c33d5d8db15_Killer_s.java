 package com.ftwinston.Killer;
 
 /*
  * Killer Minecraft
  * a Minecraft Bukkit Mod by 
  * David "Canazza" McQuillan and
  * Andrew Winston "FTWinston" Winston Watkins AKA Winston
  * Created 18/06/2012
  */
 
 import java.util.Random;
 import java.util.Vector;
 import java.util.logging.Logger;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class Killer extends JavaPlugin
 {
 	public void onEnable()
 	{	
         instance = this;
         
 		getConfig().addDefault("autoAssign", false);
 		getConfig().addDefault("autoReveal", true);
 		getConfig().addDefault("restartDay", true);
 		getConfig().options().copyDefaults(true);
 		saveConfig();
 		
 		autoAssignKiller = getConfig().getBoolean("autoAssign");
 		autoReveal = getConfig().getBoolean("autoReveal");
 		restartDayWhenFirstPlayerJoins = getConfig().getBoolean("restartDay");
 		
 		deadPlayers = new Vector<String>();
 		
         getServer().getPluginManager().registerEvents(eventListener, this);
         spectatorManager = new SpectatorManager(this);
         worldManager = new WorldManager(this);
         
         // create a plinth in the default world. Always done with the same offset, so if the world already has a plinth, it should just get overwritten.
         plinthPressurePlateLocation = worldManager.createPlinth(getServer().getWorlds().get(0));
 	}
 	
 	public void onDisable()
 	{
 		worldManager.onDisable();
 	}
 	
 	public static Killer instance;
 	Logger log = Logger.getLogger("Minecraft");
 	Location plinthPressurePlateLocation;
 
 	private EventListener eventListener = new EventListener(this);
 	private WorldManager worldManager;
 	private SpectatorManager spectatorManager;
 	
 	private final int absMinPlayers = 2;
 	public boolean autoAssignKiller, autoReveal, restartDayWhenFirstPlayerJoins;
 	public Vector<String> deadPlayers;
 	
	
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
 	{
 		if (cmd.getName().equalsIgnoreCase("killer"))
 		{
 			if ( sender instanceof Player )
 			{
 				Player player = (Player)sender;
 				if ( !player.isOp() )
 				{
 					sender.sendMessage("Sorry, you must be an op to use this command.");
 					return true;
 				}
 			}
 			
 			if ( args.length > 0 )
 			{
 				if ( args[0].equalsIgnoreCase("assign") )
 				{
 					if  ( !assignKiller(sender) )
 						return true;
 				}
 				else if ( args[0].equalsIgnoreCase("reveal") )
 				{
 					revealKiller(sender);
 					return true;
 				}
 				else if ( args[0].equalsIgnoreCase("clear") )
 				{
 					clearKiller(sender);					
 					return true;
 				} else if( args[0].equalsIgnoreCase("spectator")) {
 					if(args.length > 2) {
 					sender.sendMessage(spectatorManager.handleSpectatorCommand(args[1], args[2]));
 					} else {
 						sender.sendMessage(spectatorManager.handleSpectatorCommand(args[1],""));
 					}
 					return true;
 				}
 				else if ( args[0].equalsIgnoreCase("restart") )
 				{
 					restartGame();
 					return true;
 				}
 				else if ( args[0].equalsIgnoreCase("holding") )
 				{
 					if ( sender instanceof Player )
 					{
 						Player player = (Player)sender;
 						Location loc = new Location(worldManager.holdingWorld, 0, 1, 0);
 						player.teleport(loc);
 	
 						player.sendMessage("Teleporting to holding world");
 					}
 					return true;
 				}
 			}
 			
 			sender.sendMessage("Invalid command, available parameters are: assign, reveal, clear, spectator");
 			return true;
 		}
 		
 		return false;
 	}
 	
 	public boolean assignKiller(CommandSender sender)
 	{
 		Player[] players = getServer().getOnlinePlayers();
 		if ( players.length < absMinPlayers )
 		{
 			if ( sender != null )
 				sender.sendMessage("This game mode really doesn't work with fewer than " + absMinPlayers + " players. Seriously.");
 			return false;
 		}
 		
 		Random random = new Random();
 		int randomIndex = random.nextInt(players.length);
 		
 		for ( int i=0; i<players.length; i++ )
 		{
 			Player player = players[i];
 			if ( i == randomIndex )
 			{
 				killerName = player.getName();
 				player.sendMessage(ChatColor.RED + "You are the killer!");
 			}
 			else
 				player.sendMessage(ChatColor.YELLOW + "You are not the killer.");
 		}
 		
 		String senderName = sender == null ? "" : " by " + sender.getName();
 		getServer().broadcastMessage("A killer has been randomly assigned" + senderName + " - nobody but the killer knows who it is.");
 		return true;
 	}
 	
 	public void revealKiller(CommandSender sender)
 	{
 		if ( hasKillerAssigned() )
 		{
 			String senderName = sender == null ? "automatically" : "by " + sender.getName();
 			getServer().broadcastMessage(ChatColor.RED + "Revealed: " + killerName + " was the killer! " + ChatColor.WHITE + "(revealed " + senderName + ")");
 			
 			killerName = null;
 		}
 		else if ( sender != null )
 			sender.sendMessage("No killer has been assigned, nothing to reveal!");
 	}
 	
 	private void clearKiller(CommandSender sender)
 	{
 		if ( hasKillerAssigned() )
 		{
			String senderName = sender == null ? "automatically" : "by " + sender.getName();
			getServer().broadcastMessage(ChatColor.RED + "The killer has been cleared: there is no longer a killer! " + ChatColor.WHITE + "(cleared " + senderName + ")");
 			
 			Player killerPlayer = Bukkit.getServer().getPlayerExact(killerName);
 			if ( killerPlayer != null )
 				killerPlayer.sendMessage(ChatColor.YELLOW + "You are no longer the killer.");
 				
 			killerName = null;
 		}
 		else if ( sender != null )
 			sender.sendMessage("No killer has been assigned, nothing to clear!");
 	}
 
 	private String killerName = null;
 	protected int autoStartProcessID;
 
 	public boolean hasKillerAssigned()
 	{
 		return killerName != null;
 	}
 
 	public void cancelAutoStart()
 	{
 		if ( autoAssignKiller && autoStartProcessID != -1 )
     	{
     		Player[] players = getServer().getOnlinePlayers();
     		if ( players.length > 1 )
     			return; // only do this when the server is empty
     		
     		getServer().getScheduler().cancelTask(autoStartProcessID);
 			autoStartProcessID = -1;
     	}
 	}
 
 	public void playerKilled(String name)
 	{
 		boolean alreadyDead = false;
 		for ( int i=0; i<deadPlayers.size(); i++ )
 			if ( name.equals(deadPlayers.get(i)))
 			{
 				alreadyDead = true;
 				break;
 			}
 		
 		if ( !alreadyDead )
 		{
 			deadPlayers.add(name);
 			
 			// currently, we're banning players instead of setting them into some "observer" mode
 			Player player = Bukkit.getServer().getPlayerExact(name);
 			if (player != null)
 			{
 				//player.setBanned(true);
 				//player.kickPlayer("You were killed, and are banned until the end of the game");
 				spectatorManager.addSpectator(player);
 			}
 		}
 		
 		if ( !autoReveal )
 			return;
 		
 		int numSurvivors = 0;
 		Player[] players = getServer().getOnlinePlayers();
 		if ( players.length == 0 )
 		{
 			restartGame();
 			return;
 		}
 		
 		for ( int i=0; i<players.length; i++ )
 		{
 			boolean isDead = false;
 			for ( int j=0; j<deadPlayers.size(); j++ )
 				if ( players[i].getName().equals(deadPlayers.get(j)) )
 				{
 					isDead = true;
 					break;
 				}
 		
 			if ( !isDead )
 				numSurvivors ++;
 		}		
 		
 		if ( numSurvivors < 2 )
 			revealKiller(null);
 	}
 	
 	public void restartGame()
 	{	
 		getServer().broadcastMessage("Game is restarting, please wait while the world is deleted and a new one is prepared...");
 		
 		worldManager.deleteWorlds(new Runnable() {
 			public void run()
 			{
 				World defaultWorld = getServer().getWorlds().get(0);
 				plinthPressurePlateLocation = worldManager.createPlinth(defaultWorld);
 			}
 		});
 	}
 
 	public void doItemVictory(Player player)
 	{
 		getServer().broadcastMessage(player.getDisplayName() + " brought a blaze rod to the plinth - the friendlies win!");
 		revealKiller(player);
 		
 		getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
 			public void run()
 			{
 				restartGame();
 			}
 		}, 100);
 	}
 }
