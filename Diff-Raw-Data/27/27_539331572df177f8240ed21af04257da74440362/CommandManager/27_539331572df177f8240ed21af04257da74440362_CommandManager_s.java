 package com.mcnsa.mcnsachat2.util;
 
 import com.mcnsa.mcnsachat2.MCNSAChat2;
 
 import com.mcnsa.mcnsachat2.commands.*;
 
 import java.lang.annotation.Annotation;
 import java.util.Arrays;
 import java.util.Comparator;
 import java.util.HashMap;
 
 import org.bukkit.entity.Player;
 
 public class CommandManager {
 	// keep track of the plugin
 	public MCNSAChat2 plugin = null;
 
 	// and the commands
 	public HashMap<String, InternalCommand> commands = new HashMap<String, InternalCommand>();
 	// and the aliases
 	public HashMap<String, String> aliases = new HashMap<String, String>();
 
 	public CommandManager(MCNSAChat2 instance) {
 		plugin = instance;
 
 		// develop the list of all commands here!
 		// TODO: dynamically load commands ALA CommandBook
 		//plugin.debug("registering commands...");
 		registerCommand(new CommandChannel(plugin));
 		registerCommand(new CommandMe(plugin));
 		registerCommand(new CommandList(plugin));
 		registerCommand(new CommandColour(plugin));
 		registerCommand(new CommandHelp(plugin));
 		registerCommand(new CommandSeeAll(plugin));
 		registerCommand(new CommandTimeout(plugin));
 		registerCommand(new CommandVoxelChat(plugin));
 		registerCommand(new CommandWho(plugin));
 		registerCommand(new CommandToggleVoxelChat(plugin));
 		registerCommand(new CommandSearch(plugin));
 		registerCommand(new CommandVerbosity(plugin));
 		registerCommand(new CommandMute(plugin));
 		registerCommand(new CommandLock(plugin));
 		registerCommand(new CommandMove(plugin));
 		registerCommand(new CommandPoof(plugin));
 		registerCommand(new CommandRollTheDice(plugin));
 		registerCommand(new CommandRaveMode(plugin));
 		registerCommand(new CommandConfusion(plugin));
 		//plugin.debug("commands all registered!");
 	}
 
 	// register new command
 	public void registerCommand(Command command) {
 		// get the class
 		Class<? extends Command> cls = command.getClass();
 		//plugin.debug("registering command: " + cls.getSimpleName());
 		
 		// get the class's annotations
 		Annotation[] annotations = cls.getAnnotations();
 		for(int i = 0; i < annotations.length; i++) {
 			if(annotations[i] instanceof CommandInfo) {
 				// we found our annotation!
 				CommandInfo ci = (CommandInfo)annotations[i];
 				
 				// get the deets!
 				//plugin.debug("with alias: " + ci.alias());
 				//plugin.debug("with perms: " + ci.permission());
 				//plugin.debug("with usage: " + ci.usage());
 				//plugin.debug("with description: " + ci.description());
 				//plugin.debug("with visibility: " + ci.visible());
 				
 				// create the internal command
 				InternalCommand ic = new InternalCommand(ci.alias(), ci.permission(), ci.usage(), ci.description(), ci.visible(), command);
 				commands.put(ci.alias(), ic);
 				
 				// we're done
 				return;
 			}
 		}
 	}
 
 	// quick method to register a new alias (for a channel)
 	public void registerAlias(String alias, String channel) {
 		//plugin.debug("registering alias: " + alias);
 		
 		// add it to the list!
 		aliases.put(alias, channel);
 	}
 	
 	// handle commands
 	public Boolean handleCommand(Player player, String command) {
 		// get the actual command
 		//plugin.debug(player.getName() + " sent command: " + command);
 		
 		// strip off the proceeding "/"
 		command = command.substring(1);
 		
 		// tokenize it
 		String[] tokens = command.split("\\s");
 		
 		// get the command
 		if(tokens.length < 1) {
 			// we're not handling it
 			return false;
 		}
 		tokens[0] = tokens[0].toLowerCase();
 		
 		// check to see if it's an alias first
 		if(aliases.containsKey(tokens[0])) {
 			// handle the alias
 			//plugin.debug("handling alias: " + tokens[0]);
 			// get the arguments
 			String args = new String("");
 			if(command.length() > 1 + tokens[0].length()) {
 				args = command.substring(1 + tokens[0].length());
 			}
 			// and handle the alias!
 			handleAlias(player, tokens[0], args);
 			// we handled it!
 			return true;
 		}
 		
 		// find the command
 		if(!commands.containsKey(tokens[0])) {
 			// we're not handling it
 			//plugin.debug("not handling command: " + tokens[0]);
 			return false;
 		}
 		
 		// make sure they have permission first
 		if(!commands.get(tokens[0]).permissions.equals("") && !plugin.hasPermission(player, commands.get(tokens[0]).permissions)) {
 			// return a message if they don't have permission
 			plugin.log(player.getName() + " attempted to use command: " + tokens[0] + " without permission!");
 			ColourHandler.sendMessage(player, "&cYou don't have permission to do that!");
 			// we handled it, but they don't have perms
 			return true;
 		}
 		
 		// we have the command, send it in!
 		String sArgs = new String("");
 		//plugin.debug("handling command: " + tokens[0]);
 		// make sure we have args
 		if(command.length() > (1 + tokens[0].length())) {
 			// substring out the args
 			sArgs = command.substring(1 + tokens[0].length());
 			//plugin.debug("with arguments: " + sArgs);
 		}
 		
 		// and handle the command!
 		if(commands.get(tokens[0]).command.handle(player, sArgs)) {
 			// we handled it!
 			//plugin.debug("command " + tokens[0] + " handled successfully!");
 			return true;
 		}
 		
 		// they didn't use it properly! let them know!
 		//plugin.debug("command " + tokens[0] + " NOT handled successfully");
 		ColourHandler.sendMessage(player, "&cInvalid usage! &aCorrect usage: &6/" + commands.get(tokens[0]).alias + " &e" + commands.get(tokens[0]).usage + " &7(" + commands.get(tokens[0]).description + ")");
 		return true;
 	}
 	
 	private void handleAlias(Player player, String alias, String message) {
 		// make sure they're not locked
 		if(plugin.channelManager.isLocked(player)) {
 			// they can't change channels!
 			ColourHandler.sendMessage(player, "&cYou have been locked in your channel and cannot change channels!");
 			return;
 		}
 		
 		// check their perms
 		String channel = aliases.get(alias);
 		String perm = plugin.channelManager.getPermission(channel);
 		if(!perm.equals("") && !plugin.hasPermission(player, "channel." + perm)) {
 			// return a message if they don't have permission
 			plugin.log(player.getName() + " attempted to change to channel: " + channel + " without permission!");
 			ColourHandler.sendMessage(player, "&cYou don't have permission to do that!");
 			return;
 		}
 		
 		// if non-empty message, don't change channel
 		if(!message.trim().equals("")) {
 			// broadcast their message!
 			plugin.chatManager.handleChat(player, message, false, channel, true);
 			
 			// return, we're not changing channels
 			return;
 		}
 		
 		// ok, change their channel
 		plugin.channelManager.movePlayer(channel, player, false);
 	}
 	
 	// return a sorted list of commands
 	public InternalCommand[] listCommands() {
 		// count the number of invisible commands
 		//plugin.debug("counting number of invisible commands");
 		int numInvisible = 0;
 		for(String cmd: commands.keySet()) {
 			if(!commands.get(cmd).visible) {
 				numInvisible++;
 			}
 		}
 		
 		// create the list
 		//plugin.debug("creating command list array");
 		InternalCommand[] cList = new InternalCommand[commands.size() - numInvisible];
 		
 		// get them all!
 		int i = 0;
 		//plugin.debug("getting all visible commands");
 		for(String cmd: commands.keySet()) {
 			// add only the visible ones!
 			if(commands.get(cmd).visible) {
 				cList[i] = commands.get(cmd);
 				i += 1;
 			}
 		}
 		
 		// sort the array
 		//plugin.debug("sorting command list");
 		Arrays.sort(cList, new CommandComp());
 		
 		// and return!
 		return cList;
 	}
 
 	public class InternalCommand {
 		public String alias = new String("");
 		public String permissions = new String("");
 		public String usage = new String("");
 		public String description = new String("");
 		public Boolean visible = new Boolean(true);
 		public Command command = null;
 	
 		public InternalCommand(String _alias, String _perms, String _usage, String _desc, boolean _visible, Command _command) {
 			alias = _alias;
 			permissions = _perms;
 			usage = _usage;
 			description = _desc;
 			visible = _visible;
 			command = _command;
 		}
 	}
 	
 	class CommandComp implements Comparator<InternalCommand> {
 		public int compare(InternalCommand a, InternalCommand b) {
 			return a.alias.compareTo(b.alias);
 		}
 	}
 }
