 package com.wolvencraft.prison.mines;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 
 import com.wolvencraft.prison.mines.util.Message;
 
 public class CommandManager implements CommandExecutor {
 	private static PrisonMine plugin;
 	private static CommandSender sender;
 	
 	public CommandManager(PrisonMine plugin) {
 		CommandManager.plugin = plugin;
 		sender = null;
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
 		if(!command.getName().equalsIgnoreCase("mine")) return false;
 		
 		if(args.length == 0) {
			MineCommand.HELP.run("");
 			return true;
 		}
 
 		CommandManager.sender = sender;
 		
 		for(MineCommand cmd : MineCommand.values()) {
 			if(cmd.isCommand(args[0])) {
 				boolean result = cmd.run(args);
 				CommandManager.sender = null;
 				return result;
 			}
 		}
 		
 		Message.sendError(PrisonMine.getLanguage().ERROR_COMMAND);
 		CommandManager.sender = null;
 		return false;
 	}
 	
 	public static CommandSender getSender() 	{ return sender; }
 	public static PrisonMine getPlugin() 		{ return plugin; }
 }
