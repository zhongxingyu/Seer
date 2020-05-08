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
 
 import org.bukkit.entity.Player;
 
 import com.nodinchan.ncloader.Loader;
 import com.titankingdoms.nodinchan.titanchat.TitanChat;
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
 	
 	private final List<Executor> linked;
 	
 	private Map<String, Executor> executors;
 	private final Map<String, Executor> NAME_MAP;
 	
 	/**
 	 * Initialises variables
 	 * 
 	 * @param plugin TitanChat
 	 */
 	public CommandManager(TitanChat plugin) {
 		this.plugin = plugin;
 		CommandManager.instance = this;
 		
 		if (getCommandDir().mkdir())
 			plugin.log(Level.INFO, "Creating commands directory");
 		
 		this.linked = new LinkedList<Executor>();
 		this.executors = new LinkedHashMap<String, Executor>();
 		this.NAME_MAP = new HashMap<String, Executor>();
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
 		Executor executor = getCommandExecutor(command);
 		
 		if (executor != null) {
 			if (executor.getMethod().getAnnotation(CommandID.class).requireChannel() && !plugin.enableChannels()) {
 				plugin.sendWarning(player, "This command requires channels to be enabled");
 				return;
 			}
 			
 			try {
 				executor.execute(player, args);
 				return;
 			} catch (IllegalAccessException e) {
 				plugin.log(Level.SEVERE, "An IllegalAccessException has occured while using command: " + executor.getName());
 				db.i(e.getLocalizedMessage());
 			} catch (IllegalArgumentException e) {
 				plugin.log(Level.SEVERE, "An IllgealArgumentException has occured while using command: " + executor.getName());
 				db.i(e.getLocalizedMessage());
 			} catch (InvocationTargetException e) {
 				plugin.log(Level.SEVERE, "An InvocationTargetException has occured while using command: " + executor.getName());
 				db.i(e.getLocalizedMessage());
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
 	 * Gets the Executor by one of its triggers
 	 * 
 	 * @param name The trigger
 	 * 
 	 * @return The Executor if it exists, otherwise null
 	 */
 	public Executor getCommandExecutor(String trigger) {
 		return executors.get(trigger.toLowerCase());
 	}
 	
 	/**
 	 * Gets the Executor from the list by index
 	 * 
 	 * @param exeNum The index of the executor in the list
 	 * 
 	 * @return The Executor if exists, otherwise null
 	 */
 	public Executor getCommandExecutor(int exeNum) {
 		return new LinkedList<Executor>(linked).get(exeNum);
 	}
 	
 	public Executor getCommandExecutorByName(String name) {
 		return NAME_MAP.get(name);
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
 				NAME_MAP.put(method.getAnnotation(CommandID.class).name(), executor);
 				
 				for (String trigger : method.getAnnotation(CommandID.class).triggers())
					executors.put(trigger, executor);
 			}
 		}
 	}
 	
 	/**
 	 * Sorts the Commands
 	 */
 	public void sortCommands() {
 		Map<String, Executor> executors = new LinkedHashMap<String, Executor>();
 		List<String> triggers = new ArrayList<String>(this.executors.keySet());
 		
 		Collections.sort(triggers);
 		
 		for (String trigger : triggers)
 			executors.put(trigger, getCommandExecutor(trigger));
 		
 		this.executors = executors;
 		
 		List<String> names = new ArrayList<String>(this.NAME_MAP.keySet());
 		
 		Collections.sort(names);
 		
 		linked.clear();
 		
 		for (String name : names)
 			linked.add(getCommandExecutorByName(name));
 	}
 	
 	/**
 	 * Unloads the Commands
 	 */
 	public void unload() {
 		executors.clear();
 		NAME_MAP.clear();
 	}
 }
