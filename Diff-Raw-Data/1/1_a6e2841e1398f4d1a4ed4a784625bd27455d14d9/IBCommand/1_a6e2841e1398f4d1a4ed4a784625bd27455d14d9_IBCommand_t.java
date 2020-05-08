 package musician101.itembank.commands.ibcommand;
 
 import musician101.itembank.ItemBank;
 import musician101.itembank.lib.Commands;
 import musician101.itembank.lib.Messages;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 
 /**
  * The code used to run when the ItemBank command is executed.
  * 
  * @author Musician101
  */
 public class IBCommand implements CommandExecutor
 {
 	ItemBank plugin;
 	
 	/**
 	 * @param plugin References the plugin's main class.
 	 * @param config References the config options.
 	 */
 	public IBCommand(ItemBank plugin)
 	{
 		this.plugin = plugin;
 	}
 	
 	/**
 	 * @param sender Who sent the command.
 	 * @param command Which command was executed.
 	 * @param label Alias of the command.
 	 * @param args Command parameters.
 	 * @return True if the command was successfully executed.
 	 */
 	@Override
 	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
 	{
 		if (command.getName().equalsIgnoreCase(Commands.BASE_CMD))
 		{
 			/** Base Command */
 			if (args.length == 0)
 			{
 				sender.sendMessage(new String[]{Messages.PREFIX + "Version " + plugin.getDescription().getVersion() + " compiled with Bukkit 1.7.2-R0.2.",
 						Messages.PREFIX + "Base command, type /itembank help for more info."});
 				return true;
 			}
 			
 			/** Account Command */
 			if (args[0].equalsIgnoreCase(Commands.ACCOUNT_CMD))
 				return AccountCommand.execute(plugin, sender, args);
 			/** Help Command */
 			else if (args[0].equalsIgnoreCase(Commands.HELP_CMD))
 				return HelpCommand.execute(plugin, sender, args);
 			/** Purge Command */
 			else if (args[0].equalsIgnoreCase(Commands.PURGE_CMD))
 				return PurgeCommand.execute(plugin, sender, args);
 			else if (args[0].equalsIgnoreCase("reload"))
 			{
 				plugin.config.reloadConfiguration();
 				sender.sendMessage(Messages.PREFIX + "Config and item translator reloaded.");
 				return true;
 			}
 		}
		sender.sendMessage(Messages.PREFIX + "Error: Unrecognized arguments.");
 		return false;
 	}
 }
