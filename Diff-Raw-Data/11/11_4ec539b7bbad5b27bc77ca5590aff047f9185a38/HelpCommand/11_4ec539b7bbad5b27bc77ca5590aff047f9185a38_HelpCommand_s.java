 /*******************************************************************************
  Copyright (c) 2013 James Richardson.
 
  HelpCommand.java is part of bukkit-utilities.
 
  BukkitUtilities is free software: you can redistribute it and/or modify it
  under the terms of the GNU General Public License as published by the Free
  Software Foundation, either version 3 of the License, or (at your option) any
  later version.
 
  BukkitUtilities is distributed in the hope that it will be useful, but WITHOUT
  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 
  You should have received a copy of the GNU General Public License along with
  BukkitUtilities. If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 
 package name.richardson.james.bukkit.utilities.command;
 
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.permissions.Permissible;
 
 import org.apache.commons.lang.Validate;
 
 import name.richardson.james.bukkit.utilities.command.context.CommandContext;
 import name.richardson.james.bukkit.utilities.command.matcher.StringMatcher;
 import name.richardson.james.bukkit.utilities.formatters.ColourFormatter;
 import name.richardson.james.bukkit.utilities.formatters.DefaultColourFormatter;
 import name.richardson.james.bukkit.utilities.localisation.Localisation;
 import name.richardson.james.bukkit.utilities.localisation.ResourceBundleByClassLocalisation;
 
 /**
  * Formats and returns basic help information for a {@link Command} using the {@link CommandMetadata}. Additionally it can provide a list of all known commands
  * and their usage information.
  * <p/>
  * This command was designed to be a fall through command for the {@link name.richardson.james.bukkit.utilities.command.invoker.FallthroughCommandInvoker}
  * providing the user with additional help if the command they request does not exist.
  */
 public final class HelpCommand extends AbstractCommand {
 
 	private static final String PLUGIN_NAME_AND_VERSION_KEY = "plugin-name-and-version";
 	private static final String PLUGIN_DESCRIPTION_KEY = "plugin-description";
 	private static final String USING_HELP_KEY = "using-help";
 	private final ColourFormatter colourFormatter = new DefaultColourFormatter();
 	private final Map<String, Command> commandMap = new TreeMap<String, Command>(String.CASE_INSENSITIVE_ORDER);
 	private final String label;
 	private final Localisation localisation = new ResourceBundleByClassLocalisation(HelpCommand.class);
 
 	/**
 	 * Construct a HelpCommand using the label and commands.
 	 *
 	 * @param label    the label that must be prefixed for commands to be valid. This is usually the name of the {@link org.bukkit.command.PluginCommand} that the
 	 *                 executor is attached to.
 	 * @param commands the commands that the class should provide help for.
 	 */
 	public HelpCommand(String label, Set<Command> commands) {
 		Validate.notEmpty(label, "Command label can not be empty or null!");
 		Validate.notEmpty(commands, "CommandMap can not be empty or null!");
 		this.label = label;
 		for (Command command : commands) {
 			commandMap.put(command.getName(), command);
 		}
 		this.addMatcher(new StringMatcher(commandMap.keySet()));
 	}
 
 	@Override
 	public void execute(CommandContext commandContext) {
 		Validate.notNull(commandContext, "Command context may not be null!");
 		CommandSender commandSender = commandContext.getCommandSender();
 		Command requestedCommand = getCommand(commandContext);
 		if (requestedCommand == null) {
 			commandSender.sendMessage(ChatColor.LIGHT_PURPLE + localisation.getMessage(PLUGIN_NAME_AND_VERSION_KEY));
 			commandSender.sendMessage(ChatColor.AQUA + localisation.getMessage(PLUGIN_DESCRIPTION_KEY));
 			commandSender.sendMessage(colourFormatter.format(localisation.getMessage(USING_HELP_KEY), ColourFormatter.FormatStyle.WARNING, "/" + label, getName(), getColouredCommandUsage(getUsage())));
 			for (Command command : commandMap.values()) {
 				if (!command.isAuthorised(commandSender)) continue;
 				commandSender.sendMessage(getCommandUsage(command));
 			}
 		} else {
 			commandSender.sendMessage(ChatColor.AQUA + requestedCommand.getDescription());
 			commandSender.sendMessage(getCommandUsage(requestedCommand));
 		}
 	}
 
 	/**
 	 * Returns {@code true} if the user is authorised to use this command.
 	 *
 	 * @param permissible the permissible requesting authorisation.
 	 * @return Always returns {@code true}.
 	 * @since 6.0.0
 	 */
 	@Override
 	public boolean isAuthorised(Permissible permissible) {
 		return true;
 	}
 
 	private String getColouredCommandUsage(String usage) {
 		usage = usage.replaceAll("\\<", ChatColor.YELLOW + "\\<");
 		usage = usage.replaceAll("\\[", ChatColor.GREEN + "\\[");
 		return usage;
 	}
 
 	private Command getCommand(CommandContext commandContext) {
		if (commandContext.has(0)) {
			return commandMap.get(commandContext.getString(0));
 		} else {
 			return null;
 		}
 	}
 
 	private String getCommandUsage(Command command) {
 		String usage = getColouredCommandUsage(command.getUsage());
 		String message = ChatColor.RED + "/" + label + " " + ChatColor.YELLOW + command.getName() + " " + usage;
 		return message;
 	}
 
 }
