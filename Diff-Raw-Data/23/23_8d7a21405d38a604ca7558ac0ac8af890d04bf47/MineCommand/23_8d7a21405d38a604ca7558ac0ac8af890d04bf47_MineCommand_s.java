 package com.wolvencraft.prison.mines;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
import com.wolvencraft.prison.CommandManager;
 import com.wolvencraft.prison.hooks.CommandHook;
 import com.wolvencraft.prison.mines.cmd.*;
 import com.wolvencraft.prison.mines.util.Message;
 
 /**
  * bitWolfy's awkward way of registering all the subcommands.<br />
  * Wolfy <3 subcommands. They are kinky.
  * @author bitWolfy
  */
 public enum MineCommand implements CommandHook {
 	BLACKLIST (BlacklistCommand.class, "mcprison.mine.edit", true, "blacklist", "bl"),
 	DATA (DataCommand.class, "mcprison.mine.admin", true, "data"),
 	EDIT (EditCommand.class, "mcprison.mine.edit", true, "edit", "none", "add", "+", "remove", "-", "delete", "del", "name", "silent", "generator", "link", "setparent", "cooldown", "setregion"),
 	HELP (HelpCommand.class, null, true, "help"),
 	INFO (InfoCommand.class, "mcprison.mine.info.time", true, "info"),
 	LIST (ListCommand.class, "mcprison.mine.info.list", true, "list"),
 	META (MetaCommand.class, null, true, "meta", "about"),
 	PROTECTION (ProtectionCommand.class, "mcprison.mine.edit", true, "protection", "prot"),
 	RESET (ResetCommand.class, null, true, "reset"),
 	SAVE (SaveCommand.class, "mcprison.mine.edit", false, "save", "create", "new"),
 	TIMER (TimerCommand.class, "mcprison.mine.edit", true, "timer", "auto");
 	
 	MineCommand(Class<?> clazz, String permission, boolean allowConsole, String... args) {
 		try {
 			this.clazz = (BaseCommand) clazz.newInstance();
 			this.permission = permission;
 			this.allowConsole = allowConsole;
 			alias = new ArrayList<String>();
 			for(String arg : args) {
 				alias.add(arg);
 			}
 		} catch (InstantiationException e) {
 			Message.log(Level.SEVERE, "Error while instantiating a command! InstantiationException");
 			return;
 		} catch (IllegalAccessException e) {
 			Message.log(Level.SEVERE, "Error while instantiating a command! IllegalAccessException");
 			return;
 		}
 	}
 	
 	private BaseCommand clazz;
 	private List<String> alias;
 	private String permission;
 	private boolean allowConsole;
 	
 	public BaseCommand get() { return clazz; }
 	public boolean isCommand(String arg) { return alias.contains(arg); }
 	public void getHelp() { clazz.getHelp(); }
 	public void getHelpLine() { clazz.getHelpLine(); }
 
 	public boolean run(String[] args) {
 		CommandSender sender = CommandManager.getSender();
 		if(!allowConsole && !(sender instanceof Player)) { Message.sendError(PrisonMine.getLanguage().ERROR_SENDERISNOTPLAYER); return false; }
 		if(permission != null && (sender instanceof Player) && !sender.hasPermission(permission)) { Message.sendError(PrisonMine.getLanguage().ERROR_ACCESS); return false; }
 		return clazz.run(args);
 	}
 
 	public boolean run(String arg) {
 		String[] args = {"", arg};
 		return run(args);
 	}
 	
 }
