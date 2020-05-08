 /*******************************************************************************
  Copyright (c) 2013 James Richardson.
 
  AbstractCommandContext.java is part of bukkit-utilities.
 
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
 
 package name.richardson.james.bukkit.utilities.command.context;
 
import java.text.MessageFormat;
 import java.util.*;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.bukkit.Bukkit;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.Server;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.Validate;
 
 import name.richardson.james.bukkit.utilities.logging.PrefixedLogger;
 
 /**
  * An example implementation of {@link CommandContext}. This implementation makes no additional verification checks on requested arguments and may throw
  * IndexOutOfBoundExceptions from the internal backing storage.
  */
 public class AbstractCommandContext implements CommandContext {
 
 	private final Matcher ARGUMENT_MATCHER = Pattern.compile("(\\w+)").matcher("");
 	private final Matcher FLAG_MATCHER = Pattern.compile("(\\w{1}):(\\w+)").matcher("");
 
 	private final List<String> arguments = new ArrayList<String>();
 	private final Logger logger = PrefixedLogger.getLogger(this.getClass());
 	private final Map<String, String> flags = new HashMap<String, String>();
 	private final CommandSender sender;
 	private final Server server;
 
 	public AbstractCommandContext(String[] arguments, CommandSender sender) {
 		this(arguments, sender, Bukkit.getServer());
 	}
 
 	public AbstractCommandContext(String[] arguments, CommandSender sender, Server server) {
 		Validate.notNull(arguments);
 		Validate.notNull(sender);
 		Validate.notNull(server);
 		String argument = StringUtils.join(arguments, " ");
 		this.server = server;
 		this.sender = sender;
 		setArguments(argument);
 		setFlags(argument);
		logger.finest(this.toString());
 	}
 
 	/**
 	 * Get the CommandSender who called this command.
 	 *
 	 * @return the CommandSender
 	 */
 	@Override
 	public CommandSender getCommandSender() {
 		return sender;
 	}
 
 	/**
 	 * Get the contents of the flag.
 	 *
 	 * @param label the flag label to look up.
 	 * @return the contents of the flag.
 	 */
 	@Override
 	public String getFlag(String label) {
 		Validate.notNull(label);
 		return flags.get(label);
 	}
 
 	/**
 	 * Join all the arguments from a specified index onwards into one String.
 	 *
 	 * @param initialIndex the index to start at at
 	 * @return a String containing all the arguments seperated by ' '.
 	 */
 	@Override
 	public String getJoinedArguments(int initialIndex) {
 		Validate.notNull(initialIndex);
 		return StringUtils.join(getArguments().subList(initialIndex, size()), " ");
 	}
 
 	/**
 	 * Convert the argument in the specified index to a {@link org.bukkit.OfflinePlayer}.
 	 *
 	 * @param index the argument number to use.
 	 * @return a matching OfflinePlayer using the argument index as the player's name.
 	 */
 	@Override
 	public OfflinePlayer getOfflinePlayer(int index) {
 		Validate.notNull(index);
 		return server.getOfflinePlayer(getString(index));
 	}
 
 	/**
 	 * Convert the argument in the specified index to a {@link org.bukkit.entity.Player}.
 	 *
 	 * @param index the argument number to use.
 	 * @return a matching Player using the argument index as the player's name..
 	 */
 	@Override
 	public Player getPlayer(int index) {
 		Validate.notNull(index);
 		return server.getPlayer(getString(index));
 	}
 
 	/**
 	 * Get the argument at the specified index.
 	 *
 	 * @param index the argument number to fetch.
 	 * @return the argument specified.
 	 */
 	@Override
 	public String getString(int index) {
 		Validate.notNull(index);
 		return arguments.get(index);
 	}
 
 	/**
 	 * Check to see if the context contains an argument.
 	 *
 	 * @param index the argument number to check
 	 * @return true if the argument exists, false otherwise.
 	 */
 	@Override
 	public boolean has(int index) {
 		Validate.notNull(index);
 		return size() > 0 && (index + 1) <= size();
 	}
 
 	/**
 	 * Check to see if the context contains a flag.
 	 *
 	 * @param label the prefix of the flag to check
 	 * @return true if the flag exists, false otherwise.
 	 */
 	@Override
 	public boolean hasFlag(String label) {
 		Validate.notNull(label);
 		return flags.containsKey(label);
 	}
 
 	/**
 	 * Check if the {@link org.bukkit.command.CommandSender} is not an instance of {@link org.bukkit.entity.Player}.
 	 *
 	 * @return false if the CommandSender is a Player, otherwise true.
 	 */
 	@Override
 	public boolean isConsoleCommandSender() {
 		if (sender instanceof Player) return false;
 		return true;
 	}
 
 	/**
 	 * Get the total number of arguments contained within this context. The total does not include the CommandSender or any optional flags.
 	 *
 	 * @return total number of arguments.
 	 */
 	@Override
 	public int size() {
 		return arguments.size();
 	}
 
 	@Override
 	public String toString() {
		return MessageFormat.format("{0} [arguments={1}, flags={2}, sender={3}]", this.getClass().getSimpleName(), arguments, flags, sender);
 	}
 
 	/**
 	 * Get a list containing all arguments.
 	 *
 	 * @return a immutable list.
 	 */
 	protected List<String> getArguments() {
 		return Collections.unmodifiableList(arguments);
 	}
 
 	private void setArguments(String arguments) {
 		ARGUMENT_MATCHER.reset(arguments.replaceAll(FLAG_MATCHER.pattern().toString(), ""));
 		while (ARGUMENT_MATCHER.find()) {
 			this.arguments.add(ARGUMENT_MATCHER.group(1));
 		}
 	}
 
 	private void setFlags(String arguments) {
 		FLAG_MATCHER.reset(arguments);
 		while (FLAG_MATCHER.find()) {
 			flags.put(FLAG_MATCHER.group(1), FLAG_MATCHER.group(2));
 		}
 	}
 
 }
