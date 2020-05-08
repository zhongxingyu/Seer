 package com.wolvencraft.prison;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.wolvencraft.prison.cmd.*;
 import com.wolvencraft.prison.hooks.CommandHook;
 import com.wolvencraft.prison.util.Message;
 
 public enum CommandManager implements CommandHook {
	DEBUG (DebugCommand.class, "", true, "displayTasks", "cancelTask"),
 	HELP (HelpCommand.class, null, true, "help"),
 	SELECT (SelectCommand.class, "prison.select", false, "select", "hpos1", "hpos2", "pos1", "pos2"),
 	TRANSFORM (TransformCommand.class, "prison.select", false, "transform", "expand", "contract", "shift"),
 	WAND (WandCommand.class, "prison.select", false, "wand");
 	
 	CommandManager(Class<?> clazz, String permission, boolean allowConsole, String... args) {
 		try {
 			this.clazz = (BaseCommand) clazz.newInstance();
 			this.permission = permission;
 			this.allowConsole = allowConsole;
 			alias = new ArrayList<String>();
 			for(String arg : args) {
 				alias.add(arg);
 			}
 		}
 		catch (InstantiationException e) 	{ Message.log(Level.SEVERE, "Error while instantiating a command! (InstantiationException)"); return; }
 		catch (IllegalAccessException e) 	{ Message.log(Level.SEVERE, "Error while instantiating a command! (IllegalAccessException)"); return; }
 		catch (Exception e) 				{ Message.log(Level.SEVERE, "Error while instantiating a command! (Exception)"); return; }
 	}
 	
 	private static CommandSender sender = null;
 	
 	private BaseCommand clazz;
 	private String permission;
 	private boolean allowConsole;
 	private List<String> alias;
 	
 	public boolean isCommand(String arg) 	{ return alias.contains(arg); }
 	public void getHelp() 					{ clazz.getHelp(); }
 	public void getHelpLine() 				{ clazz.getHelpLine(); }
 	
 	public boolean run(String[] args) {
 		CommandSender sender = CommandManager.getSender();
 		if(!allowConsole && !(sender instanceof Player)) { Message.sendError(PrisonSuite.getLanguage().ERROR_SENDERISNOTPLAYER); return false; }
 		if(permission != null && (sender instanceof Player) && !sender.hasPermission(permission)) { Message.sendError(PrisonSuite.getLanguage().ERROR_ACCESS); return false; }
 		return clazz.run(args);
 	}
 
 	public boolean run(String arg) {
 		String[] args = {"", arg};
 		return run(args);
 	}
 	
 	public static CommandSender getSender() 	{ return sender; }
 	public static void setSender(CommandSender sender) { CommandManager.sender = sender; }
 	public static void resetSender() { sender = null; }
 }
