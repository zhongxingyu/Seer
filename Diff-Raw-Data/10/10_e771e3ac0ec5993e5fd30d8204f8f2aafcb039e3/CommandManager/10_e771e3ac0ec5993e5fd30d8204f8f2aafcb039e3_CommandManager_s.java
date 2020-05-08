 /*******************************************************************************
  * Copyright (c) 2013 James Richardson
  * 
  * CommandManager.java is part of BukkitUtilities.
  * 
  * BukkitUtilities is free software: you can redistribute it and/or modify it
  * under the terms of the GNU General Public License as published by the Free
  * Software Foundation, either version 3 of the License, or (at your option) any
  * later version.
  * 
  * BukkitUtilities is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * BukkitUtilities. If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package name.richardson.james.bukkit.utilities.command;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.LinkedHashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.ResourceBundle;
 
 import org.bukkit.Bukkit;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.TabExecutor;
 import org.bukkit.plugin.PluginDescriptionFile;
 
 import name.richardson.james.bukkit.utilities.localisation.LocalisedCommandSender;
 import name.richardson.james.bukkit.utilities.localisation.PluginResourceBundle;
 import name.richardson.james.bukkit.utilities.matchers.CommandMatcher;
 import name.richardson.james.bukkit.utilities.matchers.Matcher;
 
 public class CommandManager implements TabExecutor {
 
 	private final ResourceBundle localisation = PluginResourceBundle.getBundle(CommandManager.class);
 
 	private final Map<String, Command> commands = new LinkedHashMap<String, Command>();
 	private final Command helpCommand;
 	private final Matcher matcher;
 
 	public CommandManager(final String commandName, PluginDescriptionFile pluginDescriptionFile) {
 		Bukkit.getServer().getPluginCommand(commandName).setExecutor(this);
 		this.helpCommand = new HelpCommand(this.commands, commandName, pluginDescriptionFile);
		this.addCommand(this.helpCommand);
 		this.matcher = new CommandMatcher(this.commands);
 	}
 
 	public void addCommand(final Command command) {
 		this.commands.put(command.getName(), command);
 	}
 
 	public boolean onCommand(final CommandSender sender, final org.bukkit.command.Command cmd, final String label, final String[] args) {
 		final List<String> arguments = new LinkedList<String>(Arrays.asList(args));
 		if (arguments.isEmpty()) {
 			this.helpCommand.execute(arguments, sender);
		} else
 			if (this.commands.containsKey(arguments.get(0).toLowerCase())) {
 				final Command command = this.commands.get(arguments.get(0).toLowerCase());
 				arguments.remove(0);
 				if (command.isAuthorized(sender)) {
 					command.execute(arguments, sender);
 				} else {
 					LocalisedCommandSender lsender = new LocalisedCommandSender(sender, localisation);
 					lsender.error("not-allowed-to-use-command");
 				}
 			} else {
 				this.helpCommand.execute(arguments, sender);
 			}
 		return true;
 	}
 
 	public List<String> onTabComplete(final CommandSender sender, final org.bukkit.command.Command cmd, final String label, final String[] args) {
 		final List<String> arguments = new LinkedList<String>(Arrays.asList(args));
 		final Command command = this.commands.get(arguments.get(0));
 		if (command != null) {
 			if (command.isAuthorized(sender)) {
 				arguments.remove(0);
 				return command.onTabComplete(arguments, sender);
 			} else {
 				return new ArrayList<String>();
 			}
 		} else {
 			return this.matcher.getMatches(arguments.get(0));
 		}
 	}
 
 }
