 /**
  * Copyright (C) 2012 t7seven7t
  */
 package net.t7seven7t.swornguard.commands;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import net.t7seven7t.swornguard.SwornGuard;
import net.t7seven7t.util.FormatUtil;

 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.PluginCommand;
 
 /**
  * @author t7seven7t
  */
 public class CommandHandler implements CommandExecutor {
 	private final SwornGuard plugin;
 	// Only need the name of command prefix - all other aliases listed in plugin.yml will be usable
 	private String commandPrefix;
 	private List<SwornGuardCommand> registeredPrefixedCommands;
 	private List<SwornGuardCommand> registeredCommands;
 	
 	public CommandHandler(SwornGuard plugin) {
 		this.plugin = plugin;
 		registeredCommands = new ArrayList<SwornGuardCommand>();
 	}
 	
 	public void registerCommand(SwornGuardCommand command) {
 		PluginCommand pluginCommand = plugin.getCommand(command.getName());
 		if (pluginCommand != null) {
 			pluginCommand.setExecutor(command);
 			registeredCommands.add(command);
 		} else {
 			plugin.getLogHandler().log("Entry for command {0} is missing in plugin.yml", command.getName());
 		}
 	}
 
 	public void registerPrefixedCommand(SwornGuardCommand command) {
 		if (commandPrefix != null)
 			registeredPrefixedCommands.add(command);
 	}
 
 	public List<SwornGuardCommand> getRegisteredCommands() {
 		return registeredCommands;
 	}
 
 	public List<SwornGuardCommand> getRegisteredPrefixedCommands() {
 		return registeredPrefixedCommands;
 	}
 
 	public String getCommandPrefix() {
 		return commandPrefix;
 	}
 
 	public void setCommandPrefix(String commandPrefix) {
 		this.commandPrefix = commandPrefix;
 		registeredPrefixedCommands = new ArrayList<SwornGuardCommand>();
 		plugin.getCommand(commandPrefix).setExecutor(this);
 	}
 
 	public boolean usesCommandPrefix() {
 		return commandPrefix != null;
 	}
 
 	public boolean onCommand(CommandSender sender, Command cmd, String label,
 			String[] args) {
 		List<String> argsList = new ArrayList<String>();
 		
 		if (args.length > 0) {
 			String commandName = args[0];
 			for (int i = 1; i < args.length; i++)
 				argsList.add(args[i]);
 			
 			for (SwornGuardCommand command : registeredPrefixedCommands) {
				if (commandName.equalsIgnoreCase(command.getName()) || command.getAliases().contains(commandName.toLowerCase())) {
 					command.execute(sender, argsList.toArray(new String[0]));
					return true;
				}
 			}
			
			sender.sendMessage(FormatUtil.format(plugin.getMessage("unknown_command"), args[0]));
 		} else {
 			new CmdHelp(plugin).execute(sender, args);
 		}
 		
 		return true;
 	}
}
