 package com.wolvencraft.prison;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 
 import com.wolvencraft.prison.util.Message;
 
 public class CommandManager implements CommandExecutor
 {
 	private static PrisonSuite plugin;
 	private static CommandSender sender;
 	
 	public CommandManager(PrisonSuite plugin) {
 		CommandManager.plugin = plugin;
 		sender = null;
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
 		CommandManager.sender = sender;
 		if(!(command.getName().equalsIgnoreCase("prison") || command.getName().equalsIgnoreCase("ps"))) return false;
 		
 		if(args.length == 0) {
			CommandHandler.HELP.getHelp();
 			CommandManager.sender = null;
 			return true;
 		}
 		for(CommandHandler cmd : CommandHandler.values()) {
 			if(cmd.isCommand(args[0])) {
 				
 				String argString = "/prison";
 		        for (String arg : args) { argString = argString + " " + arg; }
 				Message.debug(sender.getName() + ": " + argString);
 				
 				boolean result = cmd.run(args);
 				CommandManager.sender = null;
 				return result;
 			}
 		}
 		
 		Message.sendError(PrisonSuite.getLanguage().ERROR_COMMAND);
 		CommandManager.sender = null;
 		return false;
 	}
 	
 	public static CommandSender getSender() 	{ return sender; }
 	public static PrisonSuite getPlugin() 		{ return plugin; }
 }
