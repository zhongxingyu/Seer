 package radicalpi.spplus;
 
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.scheduler.BukkitScheduler;
 
 import shadowmax507.spleefextreme.GameManager;
 
 public class SPplus extends JavaPlugin {
 	
 	Logger log = Logger.getLogger("Minecraft");
 	
 	private PlayerGameListener _GameListener;
 	
 	private PluginDescriptionFile _pdfFile;
 
 	@Override
 	public void onDisable() {
 		SPPlayerManager.restoreAllPlayers(1);
 		SPPlayerManager.restoreAllPlayers(2);
 		
 		log.info("SPPlus disabled");
 	}
 
 	@Override
 	public void onEnable() {
 		log.info("SPPlus enabled");
 		
 		_GameListener = new PlayerGameListener();
 		
 		_pdfFile = getDescription();
 		
 		PluginManager pm = this.getServer().getPluginManager();
 		
 		pm.registerEvent(Event.Type.PLAYER_TELEPORT, _GameListener, Event.Priority.Normal, this);
 		pm.registerEvent(Event.Type.PLAYER_GAME_MODE_CHANGE, _GameListener, Event.Priority.Normal, this);
 		
 		GameManager.getGame("game").getArena().saveState();
 		GameManager.getGame("game2").getArena().saveState();
 
 	}
 	
 	@Override
 	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
 	{
 		Player player = null;
 		String subCmd = null;
 		
 		if (sender instanceof Player) {
 			player = (Player) sender;
 		}
 		
 		if (args.length == 0)
 		{
 			sender.sendMessage(_pdfFile.getName() + " v" + _pdfFile.getVersion());
 			sender.sendMessage(_pdfFile.getDescription());
 			return true;
 		}
 		
 		if (args.length >= 1)
 		{
 			String gameID = null;
 			int gameNum = 0;
			if (command.getName().equalsIgnoreCase("sp2"))
 			{
 				gameID = "game2";
 				gameNum = 2;
 			} else {
 				gameID = "game";
 				gameNum = 1;
 			}
 			
 			if (player == null)
 			{
 				sender.sendMessage("[" + _pdfFile.getName() + "] This command cannot be run from console");
 				return true;
 			}
 			
 			subCmd = args[0];
 			
 			if (subCmd.equalsIgnoreCase("join"))
 			{
 				if (SPPlayerManager.isPlayingGame(player))
 				{
 					player.sendMessage(ChatColor.RED + "You appear to already be playing");
 					return true;
 				}
 				if (SPPlayerManager.isRunningGame(gameNum))
 				{
 					player.sendMessage(ChatColor.RED + "The Spleef game is currently running");
 					return true;
 				}
 				if (SPPlayerManager.beginPlayerGame(player, gameNum))
 				{
 					getServer().dispatchCommand(player, "spleef join " + gameID);
 					getServer().broadcastMessage(ChatColor.GREEN + "[SPLEEF] " + player.getName() + " joined the Spleef game");
 				}
 				return true;
 			} else if (subCmd.equalsIgnoreCase("start"))
 			{
 				int time = new Integer(5);
 				int i;
 				if (!SPPlayerManager.isPlayingGame(player))
 				{
 					player.sendMessage(ChatColor.RED + "You can't start a game you're not playing in!");
 					return true;
 				}
 				if (SPPlayerManager.isRunningGame(gameNum))
 				{
 					player.sendMessage(ChatColor.RED + "The Spleef game has already started");
 					return true;
 				}
 				if (!SPPlayerManager.hasPlayers(gameNum))
 				{
 					player.sendMessage(ChatColor.RED + "You can't start an empty game!");
 					return true;
 				}
 				if (args.length == 2)
 				{
 					try{
 						time = Integer.parseInt(args[1]);
 					} catch (NumberFormatException e) {
 						player.sendMessage(ChatColor.RED + "I don't think \"" + args[1] + "\" is a whole number...");
 						return true;
 					}
 				}
 				getServer().dispatchCommand(player, "spleef reset " + gameID);
 				SPPlayerManager.startGame(gameNum);
 				BukkitScheduler scheduler = getServer().getScheduler();
 				getServer().broadcastMessage(ChatColor.GREEN + "[SPLEEF] " + player.getName() + " started the Spleef countdown");
 				// Time delay is in ticks, 20 TPS
 				for (i=1; time > 0; time--, i++)
 				{
 					scheduler.scheduleSyncDelayedTask(this, new GameStart(player, time, gameID), i * 20L);
 				}
 				scheduler.scheduleSyncDelayedTask(this, new GameStart(player, 0, gameID), i * 20L);
 				return true;
 			} else if (subCmd.equalsIgnoreCase("leave"))
 			{
 				if (!SPPlayerManager.isPlayingGame(player))
 				{
 					player.sendMessage(ChatColor.RED + "You aren't in the game!");
 					return true;
 				}
 				getServer().dispatchCommand(player, "spleef leave");
 				getServer().broadcastMessage(ChatColor.GREEN + "[SPLEEF] " + player.getName() + " left the Spleef game");
 				return true;
 			} else if (subCmd.equalsIgnoreCase("reset"))
 			{
 				if (SPPlayerManager.isRunningGame(gameNum))
 				{
 					player.sendMessage(ChatColor.RED + "You can't reset during a Spleef game");
 					return true;
 				}
 				getServer().dispatchCommand(player, "spleef reset " + gameID);
 				return true;
 			} else if (subCmd.equalsIgnoreCase("updatefield"))
 			{
 				if (SPPlayerManager.isRunningGame(gameNum))
 				{
 					player.sendMessage(ChatColor.RED + "You don't REALLY want to save the field during the game, do you?");
 					return true;
 				}
 				GameManager.getGame(gameID).getArena().saveState();
 				player.sendMessage(ChatColor.RED + "Field successfully updated");
 				return true;
 			} else if (subCmd.equalsIgnoreCase("cancel"))
 			{
 				if (!SPPlayerManager.isPlayingGame(player))
 				{
 					player.sendMessage(ChatColor.RED + "You can't cancel a game you're not playing!");
 					return true;
 				}
 				
 				if (SPPlayerManager.isRunningGame())
 				{
 					getServer().getScheduler().cancelTasks(this);
 					getServer().broadcastMessage(ChatColor.GREEN + "[SPLEEF] All countdowns canceled");
 				} else {
 					player.sendMessage(ChatColor.RED + "I don't see any games to cancel");
 				}
 				return true;
 			}
 		}
 		return false;
 	}
 }
