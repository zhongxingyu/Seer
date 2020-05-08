 package rageteam.cookieslap.commands;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 
 import rageteam.cookieslap.util.Messages;
 import rageteam.cookieslap.main.CS;
 
 public class HelpCommand implements CommandExecutor {
 	CS plugin;
 	public HelpCommand(CS instance) { this.plugin = instance; }
 	
 	@Override
 	public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
 		//----------------------------------------------------------------//
 		//                         CHECKSUM                               //
 		//----------------------------------------------------------------//
 		if (args.length > 1) { 
 			sender.sendMessage(Messages.WRONG_SYNTAX); return true;
 		}
		if (!sender.hasPermission("sg.help")) {
 			sender.sendMessage(Messages.NOT_ENOUGH_PERMISSIONS); return true;
 		}
 		
 		
 		//----------------------------------------------------------------//
 		//                       MANAGING ARGS                            //
 		//----------------------------------------------------------------//
 		
 		int page = 0;
 		if (args.length > 0) { page = Integer.parseInt(args[0]) - 1; }
 		if ((page < 0) || (page > pages.length - 1)) { page = 0; }
 		
 		sender.sendMessage(plugin.chat.Colourize(pages[page]));	
 		
 		return true;
 	}
 	
 	/** Help pages array */
 	private String[] pages = {
 			"&6-----( &cHELP (1/2) &6)-----\n" +
 			" &a/help [#] &7- &eShows this page\n",
 
 			"&6-----( &cHELP (2/2) &6)-----\n" +
 			" &afor more commands\n",
 	};
 }
