 package com.jackwilsdon.PvPoints;
 
 import java.util.Arrays;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 /*
  * PvPointsCommandExecutor
  * Command Executor for PvPoints
  */
 public class PvPointsCommandExecutor implements CommandExecutor {
 	/*
 	 * Variable for plugin
 	 */
 	private static PvPointsPlugin plugin;
 	
 	/*
 	 * Message return prefix
 	 */
 	String prefix = "["+ChatColor.GREEN+"PvPoints"+ChatColor.WHITE+"] ";
 	
 	/*
 	 * PvPointsPlayerManager
 	 * Constructor
 	 */
 	PvPointsCommandExecutor(PvPointsPlugin pl)
 	{
 		/*
 		 * Set plugin variable
 		 */
 		plugin = pl;
 	}
 	
 	/*
 	 * issue()
 	 * Displays a problem to the reciever
 	 */
 	public void issue(String issue, CommandSender reciever)
 	{
 		switch (issue)
 		{
 		case "syntax":
 			reciever.sendMessage(prefix+ChatColor.YELLOW+"Invalid syntax!");
 			reciever.sendMessage(prefix+ChatColor.YELLOW+"Valid commands are "+ChatColor.GREEN+"reset"+ChatColor.YELLOW+", "+ChatColor.GREEN+"add"+ChatColor.YELLOW+", "+ChatColor.GREEN+"help");
 			reciever.sendMessage(prefix+ChatColor.YELLOW+"Optional arguments are denoted as "+ChatColor.GREEN+"[option]");
 			break;
 		case "reset-syntax":
 			reciever.sendMessage(prefix+ChatColor.YELLOW+"Invalid syntax for "+ChatColor.GREEN+"reset"+ChatColor.YELLOW+"!");
 			reciever.sendMessage(prefix+ChatColor.YELLOW+"Valid syntax: /pvpoints reset [username]");
 			reciever.sendMessage(prefix+ChatColor.YELLOW+"Optional arguments are denoted as "+ChatColor.GREEN+"[option]");
 			break;
 		case "reset-sender":
 			reciever.sendMessage(prefix+ChatColor.GREEN+"reset"+ChatColor.RED+" without parameters can only be run by a player!");
 			break;
 		case "add-syntax":
 			reciever.sendMessage(prefix+ChatColor.YELLOW+"Invalid syntax for "+ChatColor.GREEN+"add"+ChatColor.YELLOW+"!");
 			reciever.sendMessage(prefix+ChatColor.YELLOW+"Valid syntax: /pvpoints add <username>");
 			break;
		case "existing-player":
 			reciever.sendMessage(prefix+ChatColor.RED+"That player already exists!");
 			break;
 		case "missing-player":
 			reciever.sendMessage(prefix+ChatColor.RED+"That is not a valid player!");
 			break;
 		default:
 			reciever.sendMessage(prefix+ChatColor.RED+"Something went wrong!");
 			break;
 		}
 	}
 	
 	/*
 	 * onCommand()
 	 * Called when a command is run
 	 */
 	@Override
 	public boolean onCommand(CommandSender cmdSender, Command cmd, String label, String[] arguments)
 	{
 		/*
 		 * Check argument length is correct
 		 */
 		if (arguments.length == 0)
 		{
 			/*
 			 * If the sender isn't a player, display command help
 			 */
 			if (!(cmdSender instanceof Player))
 			{
 				issue("syntax", cmdSender);
 				return true;
 			}
 			
 			/*
 			 * Retrieve information
 			 */
 			int points = PvPointsPlayerManager.getPoints(cmdSender.getName());
 			int kills = PvPointsPlayerManager.getKills(cmdSender.getName());
 			int deaths = PvPointsPlayerManager.getDeaths(cmdSender.getName());
 			
 			/*
 			 * Output statistics
 			 */
 			cmdSender.sendMessage(prefix+ChatColor.YELLOW+"Points: "+ChatColor.WHITE+points);
 			cmdSender.sendMessage(prefix+ChatColor.GREEN+"Kills: "+ChatColor.WHITE+kills);
 			cmdSender.sendMessage(prefix+ChatColor.RED+"Deaths: "+ChatColor.WHITE+deaths);
 			float ratio = (float)kills/(float)deaths;
 			cmdSender.sendMessage(prefix+ChatColor.YELLOW+"Kill/Death ratio: "+ChatColor.WHITE+ratio);
 			return true;
 		}
 		
 		/*
 		 * Reload the configuration
 		 */
 		plugin.reloadConfig();
 		
 		/*
 		 * Retrieve command and remove it from the arguments
 		 */
 		String command = arguments[0];
 		arguments = Arrays.copyOfRange(arguments, 1, arguments.length);
 		
 		/*
 		 * Manage the command
 		 */
 		switch(command)
 		{
 		/*
 		 * Reset score command
 		 */
 		case "reset":
 			if (arguments.length == 0)
 			{
 				if (!(cmdSender instanceof Player))
 				{
 					issue("reset-sender", cmdSender);
 					break;
 				}
 				PvPointsPlayerManager.reset(cmdSender.getName());
 				cmdSender.sendMessage(prefix+ChatColor.YELLOW+"Your kills/deaths/points have been reset!");
 			} else if (arguments.length == 1) {
 				if (!PvPointsPlayerManager.playerExists(arguments[0]))
 				{
 					issue("missing-player", cmdSender);
 					break;
 				}
 				PvPointsPlayerManager.reset(arguments[0]);
 				cmdSender.sendMessage(prefix+ChatColor.YELLOW+"The player "+ChatColor.GREEN+arguments[0]+ChatColor.YELLOW+" has been reset!");
 				plugin.getServer().getPlayer(arguments[0]).sendMessage(prefix+ChatColor.YELLOW+"Your kills/deaths/points have been reset!");
 			} else {
 				issue("reset-syntax", cmdSender);
 				break;
 			}
 			break;
 		
 		/*
 		 * Add player command
 		 */
 		case "add":
 			if (arguments.length == 0)
 			{
 				issue("add-syntax", cmdSender);
 				break;
 			}
 			if (PvPointsPlayerManager.playerExists(arguments[0]))
 			{
 				issue("existing-player", cmdSender);
 				break;
 			}
 			PvPointsPlayerManager.reset(arguments[0]);
 			cmdSender.sendMessage(prefix+ChatColor.YELLOW+"The player '"+arguments[0]+"' was added.");
 			break;
 			
 		/*
 		 * Help command
 		 */
 		case "help":
 			if (arguments.length != 1)
 			{
 				cmdSender.sendMessage(ChatColor.GREEN+"reset [username]"+ChatColor.YELLOW+" - "+ChatColor.WHITE+"Resets a player's kills, deaths and points");
 				cmdSender.sendMessage(ChatColor.GREEN+"add [username]"+ChatColor.YELLOW+" - "+ChatColor.WHITE+"Add a player to PvPoints");
 				cmdSender.sendMessage(ChatColor.GREEN+"help [command]"+ChatColor.YELLOW+" - "+ChatColor.WHITE+"Displays help for a certain command, or for all commands");
 				break;
 			} else {
 				switch (arguments[0])
 				{
 				case "reset":
 					cmdSender.sendMessage(ChatColor.GREEN+"reset [username]"+ChatColor.YELLOW+" - "+ChatColor.WHITE+"Using "+ChatColor.GREEN+"reset"+ChatColor.WHITE+" without any parameters will reset your own statistics.");
 					cmdSender.sendMessage(ChatColor.WHITE+"Using "+ChatColor.GREEN+"reset"+ChatColor.WHITE+" with a username will reset the supplied user to default, as long as they are in the PvPoints system");
 					cmdSender.sendMessage(ChatColor.RED+"Using reset will clear statistics and scores WITHOUT WARNING!");
 					break;
 				case "add":
 					cmdSender.sendMessage(ChatColor.GREEN+"add [username]"+ChatColor.YELLOW+" - "+ChatColor.WHITE+"Add a user to the PvPoints configuration.");
 					cmdSender.sendMessage(ChatColor.WHITE+"This should not normally need to be used, as players are added to the configuration on join.");
 					break;
 				case "help":
 					cmdSender.sendMessage(ChatColor.GREEN+"help [command]"+ChatColor.YELLOW+" - "+ChatColor.WHITE+"If [command] is not passed, generalised help will be shown for all commands");
 					cmdSender.sendMessage(ChatColor.WHITE+"If [command] is passed, help for the specified command will be shown");
 					break;
 				default:
 					cmdSender.sendMessage(prefix+ChatColor.WHITE+"Not a valid command! To view all commands, use "+ChatColor.GREEN+"pvpoints help");
 					break;
 				}
 			}
 			cmdSender.sendMessage(ChatColor.YELLOW+"Optional arguments are denoted as "+ChatColor.GREEN+"[option]");
 			break;
 			
 		/*
 		 * Default, unknown command
 		 */
 		default:
 			issue("syntax", cmdSender);
 			break;
 		}
 		
 		/*
 		 * Save any configuration changes
 		 */
 		plugin.saveConfig();
 		
 		/*
 		 * Return true to prevent default help from appearing
 		 */
 		return true;
 	}
 
 }
