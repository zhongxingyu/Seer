 package com.wolvencraft.prison.mines;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 
 import com.wolvencraft.prison.mines.mine.Mine;
 import com.wolvencraft.prison.mines.util.Message;
 
 public class CommandManager implements CommandExecutor
 {
 	private static PrisonMine plugin;
 	private static CommandSender sender;
 	private static Mine curMine;
 	
 	public CommandManager(PrisonMine plugin) {
 		CommandManager.plugin = plugin;
 		sender = null;
 		curMine = null;
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
 		if(!command.getName().equalsIgnoreCase("mine")) return false;
 		
 		if(args.length == 0) {
 			MineCommand.HELP.getHelp();
 			return true;
 		}

		CommandManager.sender = sender;
		
 		for(MineCommand cmd : MineCommand.values()) {
 			if(cmd.isCommand(args[0])) {
				if(sender == null) Message.debug("CommandSender is null! (4)");
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
 	public static Mine getCurrentMine() 		{ return curMine; }
 	
 	public static void setCurrentMine(Mine newMine) 	{ curMine = newMine; }
 }
