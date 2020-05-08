 package musician101.itembank.commands.ibcommand;
 
 import musician101.itembank.ItemBank;
 import musician101.itembank.lib.Commands;
 import musician101.itembank.lib.Messages;
 
 import org.bukkit.command.CommandSender;
 
 /**
  * The code used when the Help argument is used in the ItemBank command.
  * 
  * @author Musician101
  */
 public class HelpCommand
 {
 	public static boolean execute(ItemBank plugin, CommandSender sender, String[] args)
 	{
 		if (args.length == 1)
 			sender.sendMessage(Commands.HELP_LIST);
 		else
 		{
 			String cmd = args[1].toLowerCase();
			if (cmd.equals(Commands.ACCOUNT_CMD))
 				sender.sendMessage(Commands.ACCOUNT_HELP);
			else if (cmd.equals(Commands.DEPOSIT_CMD))
				sender.sendMessage(Commands.DEPOSIT_HELP);
			else if (cmd.equals(Commands.ALIAS_CMD))
 				sender.sendMessage(Commands.ALIAS_HELP);
			else if (cmd.equals(Commands.PURGE_CMD))
 				sender.sendMessage(Commands.PURGE_HELP);
			else if (cmd.equals(Commands.RELOAD_CMD))
 				sender.sendMessage(Commands.RELOAD_HELP);
			else if (cmd.equals(Commands.WITHDRAW_CMD))
 				sender.sendMessage(Commands.WITHDRAW_HELP);
 			else
 				sender.sendMessage(Messages.PREFIX + "Error: Command not recognized.");
 		}
 		return true;
 	}
 }
