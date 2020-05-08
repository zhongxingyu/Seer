 package com.titankingdoms.nodinchan.titanchat.command;
 
 import java.io.File;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.nodinchan.ncbukkit.loader.Loader;
 import com.titankingdoms.nodinchan.titanchat.TitanChat;
 import com.titankingdoms.nodinchan.titanchat.channel.Channel;
 import com.titankingdoms.nodinchan.titanchat.command.Command.Executor;
 import com.titankingdoms.nodinchan.titanchat.command.commands.*;
 import com.titankingdoms.nodinchan.titanchat.util.Debugger;
 
 /*     Copyright (C) 2012  Nodin Chan <nodinchan@live.com>
  * 
  *     This program is free software: you can redistribute it and/or modify
  *     it under the terms of the GNU General Public License as published by
  *     the Free Software Foundation, either version 3 of the License, or
  *     (at your option) any later version.
  * 
  *     This program is distributed in the hope that it will be useful,
  *     but WITHOUT ANY WARRANTY; without even the implied warranty of
  *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *     GNU General Public License for more details.
  * 
  *     You should have received a copy of the GNU General Public License
  *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 /**
  * CommandManager - Manages registered commands
  * 
  * @author NodinChan
  *
  */
 public final class CommandManager {
 	
 	private final TitanChat plugin;
 	
 	private static CommandManager instance;
 	
 	private static final Debugger db = new Debugger(4);
 	
 	private final Map<String, String> aliases;
 	private final Map<String, Executor> executors;
 	
 	private Dynamic dynamic;
 	
 	/**
 	 * Initialises variables
 	 */
 	public CommandManager() {
 		this.plugin = TitanChat.getInstance();
 		CommandManager.instance = this;
 		
 		if (getCommandDir().mkdir())
 			plugin.log(Level.INFO, "Creating commands directory");
 		
 		this.executors = new LinkedHashMap<String, Executor>();
 		this.aliases = new LinkedHashMap<String, String>();
 		
 		try { this.dynamic = new Dynamic(plugin); } catch (Exception e) { e.printStackTrace(); }
 	}
 	
 	/**
 	 * Searches for the command and executes it if found
 	 * 
 	 * @param player The command sender
 	 * 
 	 * @param command The command
 	 * 
 	 * @param args The arguments
 	 */
 	public void execute(Player player, String command, String[] args) {
 		if (aliases.get(command.toLowerCase()) != null) {
 			Executor executor = getCommandExecutor(command);
 			
 			if (executor.getMethod().getAnnotation(CommandID.class).requireChannel() && !plugin.enableChannels()) {
 				if (player != null)
 					plugin.sendWarning(player, "This command requires channels to be enabled");
 				return;
 			}
 			
 			for (String trigger : executor.getMethod().getAnnotation(CommandID.class).aliases()) {
 				db.i("Checking trigger \"" + trigger + "\" with command \"" + command + "\"");
 				
 				if (trigger.equalsIgnoreCase(command)) {
 					try {
 						executor.execute(player, args);
 						return;
 					} catch (IllegalAccessException e) {
 						plugin.sendWarning(player, "An error seems to have occured, please check console");
 						plugin.log(Level.SEVERE, "An IllegalAccessException has occured while using command: " + executor.getName());
 						if (db.isDebugging())
 							e.printStackTrace();
 					} catch (IllegalArgumentException e) {
 						plugin.sendWarning(player, "An error seems to have occured, please check console");
 						plugin.log(Level.SEVERE, "An IllgealArgumentException has occured while using command: " + executor.getName());
 						if (db.isDebugging())
 							e.printStackTrace();
 					} catch (InvocationTargetException e) {
 						plugin.sendWarning(player, "An error seems to have occured, please check console");
 						plugin.log(Level.SEVERE, "An InvocationTargetException has occured while using command: " + executor.getName());
 						if (db.isDebugging()) {
 							e.printStackTrace();
 							e.getTargetException().printStackTrace();
 						}
 					}
 				}
 			}
 		}
 		
 		plugin.sendWarning(player, "Invalid Command");
 		plugin.sendInfo(player, "\"/titanchat commands [page]\" for command list");
 	}
 	
 	/**
 	 * Gets the amount of commands
 	 * 
 	 * @return The amount of commands
 	 */
 	public int getCommandAmount() {
 		return executors.size();
 	}
 	
 	/**
 	 * Gets the Command directory
 	 * 
 	 * @return The Command directory
 	 */
 	public File getCommandDir() {
 		return new File(TitanChat.getInstance().getAddonManager().getAddonDir(), "commands");
 	}
 	
 	/**
 	 * Gets the Executor by its name
 	 * 
 	 * @param name The name
 	 * 
 	 * @return The Executor if it exists, otherwise null
 	 */
 	public Executor getCommandExecutor(String alias) {
 		if (alias == null)
 			return null;
 		
 		String name = aliases.get(alias.toLowerCase());
 		
 		if (name == null)
 			return null;
 		
 		return executors.get(name.toLowerCase());
 	}
 	
 	/**
 	 * Gets the Executor from the list by index
 	 * 
 	 * @param exeNum The index of the executor in the list
 	 * 
 	 * @return The Executor if exists, otherwise null
 	 */
 	public Executor getCommandExecutor(int exeNum) {
 		return new LinkedList<Executor>(executors.values()).get(exeNum);
 	}
 	
 	/**
 	 * Gets the dynamic command registrator
 	 * 
 	 * @return The dynamic command registrator
 	 */
 	public Dynamic getDynamic() {
 		return dynamic;
 	}
 	
 	/**
 	 * Gets an instance of this
 	 * 
 	 * @return CommandManager instance
 	 */
 	public CommandManager getInstance() {
 		return instance;
 	}
 	
 	/**
 	 * Loads all Commands
 	 */
 	public void load() {
 		register(new AdministrateCommand());
 		register(new ChannelCommand());
 		register(new ChatCommand());
 		register(new DisplayNameCommand());
 		register(new InformationCommand());
 		register(new InvitationCommand());
 		register(new PluginCommand());
 		register(new RankingCommand());
 		register(new SettingsCommand());
 		
 		for (Command command : new Loader<Command>(plugin, getCommandDir(), new Object[0]).load()) { register(command); }
 		
 		sortCommands();
 	}
 	
 	/**
 	 * Registers the Command
 	 * 
 	 * @param command The Command to be registered
 	 */
 	public void register(Command command) {
 		db.i("Try to register command " + command.toString());
 		
 		for (Method method : command.getClass().getMethods()) {
 			if (method.getAnnotation(CommandID.class) != null) {
 				db.i("Adding new executor: " + method.getAnnotation(CommandID.class).name());
 				
 				Executor executor = new Executor(method, command);
 				executors.put(method.getAnnotation(CommandID.class).name().toLowerCase(), executor);
 				
 				aliases.put(executor.getName(), executor.getName());
 				
 				for (String alias : method.getAnnotation(CommandID.class).aliases())
 					aliases.put(alias.toLowerCase(), executor.getName());
 			}
 		}
 	}
 	
 	/**
 	 * Sorts the Commands
 	 */
 	public void sortCommands() {
 		Map<String, Executor> executors = new LinkedHashMap<String, Executor>();
 		List<String> names = new ArrayList<String>(this.executors.keySet());
 		
 		Collections.sort(names);
 		
 		for (String name : names)
 			executors.put(name, getCommandExecutor(name));
 		
 		this.executors.clear();
 		this.executors.putAll(executors);
 	}
 	
 	/**
 	 * Unloads the Commands
 	 */
 	public void unload() {
 		executors.clear();
 	}
 	
 	/**
 	 * Dynamic - For dynamic command registration of channels
 	 * 
 	 * @author NodinChan
 	 *
 	 */
 	public final class Dynamic implements CommandExecutor {
 		
 		private final TitanChat plugin;
 		
 		private final com.nodinchan.ncbukkit.command.CommandManager dynamic;
 		
 		private final Map<String, Channel> joinCommand;
 		private final Map<String, Channel> sendCommand;
 		
 		public Dynamic(TitanChat plugin) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
 			this.plugin = plugin;
 			this.dynamic = new com.nodinchan.ncbukkit.command.CommandManager(plugin);
 			this.joinCommand = new HashMap<String, Channel>();
 			this.sendCommand = new HashMap<String, Channel>();
 		}
 		
 		/**
 		 * Loads the commands for the Channel
 		 * 
 		 * @param channel The Channel to be loaded
 		 */
 		public void load(Channel channel) {
 			if (!channel.getConfig().getString("commands.join").equals("")) {
 				String cmd = channel.getConfig().getString("commands.join");
 				com.nodinchan.ncbukkit.command.PluginCommand command = dynamic.register(cmd);
				command.setExecutor(this);
				command.setDescription("Joins the channel");
				command.setUsage("/<command>");
				joinCommand.put(cmd.toLowerCase(), channel);
 			}
 			
 			if (!channel.getConfig().getString("commands.message").equals("")) {
 				String cmd = channel.getConfig().getString("commands.message");
 				com.nodinchan.ncbukkit.command.PluginCommand command = dynamic.register(cmd);
				command.setExecutor(this);
				command.setDescription("Sends a message to the channel");
				command.setUsage("/<command>");
				sendCommand.put(cmd.toLowerCase(), channel);
 			}
 		}
 		
 		public boolean onCommand(CommandSender sender, org.bukkit.command.Command cmd, String label, String[] args) {
 			if (joinCommand.get(cmd.getName().toLowerCase()) != null) {
 				plugin.getServer().dispatchCommand(sender, "titanchat join " + joinCommand.get(cmd.getName().toLowerCase()).getName());
 				return true;
 			}
 			
 			if (sendCommand.get(cmd.getName().toLowerCase()) != null) {
 				StringBuilder str = new StringBuilder();
 				
 				for (String arg : args) {
 					if (str.length() > 0)
 						str.append(" ");
 					
 					str.append(arg);
 				}
 				
 				plugin.getServer().dispatchCommand(sender, "titanchat send " + sendCommand.get(cmd.getName().toLowerCase()).getName() + " " + str.toString());
 				return true;
 			}
 			
 			return false;
 		}
 	}
 }
