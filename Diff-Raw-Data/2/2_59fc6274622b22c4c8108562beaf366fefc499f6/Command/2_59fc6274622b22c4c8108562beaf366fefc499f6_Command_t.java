 package fr.ribesg.alix.api.bot.command;
 
 import fr.ribesg.alix.api.Channel;
 import fr.ribesg.alix.api.Receiver;
 import fr.ribesg.alix.api.Server;
 import fr.ribesg.alix.api.Source;
 import fr.ribesg.alix.api.enums.Codes;
 
 import java.util.Set;
 
 /**
  * Represents a Command
  */
 public abstract class Command {
 
 	/**
 	 * The CommandManager this Command belongs to.
 	 */
 	protected final CommandManager manager;
 
 	/**
 	 * The name of this Command, the main String that has to be written for
 	 * the command to be used, without prefix.
 	 */
 	protected final String name;
 
 	/**
 	 * Aliases of this Command, some Strings you can use instead of this
 	 * Command's name.
 	 */
 	protected final String[] aliases;
 
 	/**
 	 * Usage Strings, used by the help command.
 	 * You can also use them to send error messages.
 	 * <p>
 	 * Note that the first line will be prepended with the
 	 * Command prefix, followed by the command name and a space.
 	 * All other lines are prepended by enough spaces to match the first line
 	 * prefix length.
 	 */
 	protected final String[] usage;
 
 	/**
 	 * If this Command is a restricted Command or not.
 	 * A restricted Command cannot be ran by everybody, the nickname has to be
 	 * a bot admin or in the {@link #allowedNickNames} Set.
 	 */
 	protected final boolean restricted;
 
 	/**
 	 * Set of Nicknames allowed to use this Command.
 	 * Nicknames have to be registered and identified with NickServ for this
 	 * to be effective.
 	 * This is not considered if {@link #restricted} is false.
 	 */
 	protected final Set<String> allowedNickNames;
 
 	/**
 	 * Public Command constructor.
 	 * Calls {@link #Command(CommandManager, String, String[], boolean, Set, String...)}.
 	 *
 	 * @param manager the CommandManager this Command belongs to
 	 * @param name    the name of this Command
 	 * @param usage   usage of this Command
 	 *
 	 * @see #Command(CommandManager, String, String[], boolean, Set, String...) for non-public Command
 	 */
 	public Command(final CommandManager manager, final String name, final String[] usage) {
 		this(manager, name, usage, false, null);
 	}
 
 	/**
 	 * Public Command with aliases constructor.
 	 * Calls {@link #Command(CommandManager, String, String[], boolean, Set, String...)}.
 	 *
 	 * @param manager the CommandManager this Command belongs to
 	 * @param name    the name of this Command
 	 * @param usage   usage of this Command
 	 * @param aliases possible aliases for this Command
 	 *
 	 * @see #Command(CommandManager, String, String[], boolean, Set, String...) for non-public Command
 	 */
 	public Command(final CommandManager manager, final String name, final String[] usage, final String... aliases) {
 		this(manager, name, usage, false, null, aliases);
 	}
 
 	/**
 	 * Complete Command constructor.
 	 * Should be used for restricted Commands.
 	 *
 	 * @param manager          the CommandManager this Command belongs to
 	 * @param name             the name of this Command
 	 * @param usage            usage of this Command
 	 * @param restricted       if this Command is restricted or public
 	 * @param allowedNickNames a Set of allowed nicknames, all registered
 	 *                         with the NickServ Service
 	 * @param aliases          possible aliases for this Command
 	 *
 	 * @throws IllegalArgumentException if the Command is public and a Set
 	 *                                  of allowedNickNames was provided
 	 */
 	public Command(final CommandManager manager, final String name, final String[] usage, final boolean restricted, final Set<String> allowedNickNames, final String... aliases) {
 		if (!restricted && allowedNickNames != null) {
 			throw new IllegalArgumentException("A public Command should not have allowedNickNames, did you do something wrong?");
 		}
 		this.manager = manager;
 		this.name = name.toLowerCase();
 		this.aliases = aliases;
 		this.restricted = restricted;
 		this.allowedNickNames = allowedNickNames;
 
 		// Make the aliases lowercase, too
 		for (int i = 0; i < this.aliases.length; i++) {
 			this.aliases[i] = this.aliases[i].toLowerCase();
 		}
 
 		if (usage == null) {
 			this.usage = null;
 		} else {
 			final String commandString = this.toString();
			this.usage = new String[usage.length + 1 + (this.aliases.length > 0 ? 1 : 0)];
 			this.usage[0] = Codes.RED + commandString;
 			if (usage.length > 0) {
 				for (int i = 1; i < usage.length + 1; i++) {
 					this.usage[i] = Codes.RED + " | " + usage[i - 1].replaceAll("##", commandString);
 				}
 			}
 			if (this.aliases.length > 0) {
 				final StringBuilder aliasesStringBuilder = new StringBuilder(Codes.RED + " | Aliases: " + this.aliases[0]);
 				for (int i = 1; i < this.aliases.length; i++) {
 					aliasesStringBuilder.append(", ").append(this.aliases[i]);
 				}
 				this.usage[this.usage.length - 1] = aliasesStringBuilder.toString();
 			}
 		}
 	}
 
 	/**
 	 * Gets the name of this Command, the main String that has to be written
 	 * for the command to be used, without prefix.
 	 *
 	 * @return the name of this Command
 	 */
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * Gets the aliases of this Command, some Strings you can use instead of
 	 * this Command's name.
 	 *
 	 * @return possible aliases for this Command, may be of length 0
 	 */
 	public String[] getAliases() {
 		return aliases;
 	}
 
 	/**
 	 * Checks if this Command is a restricted Command or not.
 	 * A restricted Command cannot be ran by everybody, the nickname has to be
 	 * a bot admin or in the {@link #getAllowedNickNames()} Set.
 	 *
 	 * @return true if this Command is restricted, false otherwise
 	 */
 	public boolean isRestricted() {
 		return restricted;
 	}
 
 	/**
 	 * Gets a Set of Nicknames allowed to use this Command.
 	 * Nicknames have to be registered and identified with the NickServ
 	 * Service for this to be effective.
 	 * This should not be considered if {@link #isRestricted()} is false.
 	 *
 	 * @return a Set of allowed Nicknames if this Command is restricted,
 	 * null otherwise
 	 */
 	public Set<String> getAllowedNickNames() {
 		return allowedNickNames;
 	}
 
 	/**
 	 * Executes this Command.
 	 *
 	 * @param server          the Server this Command has been called from
 	 * @param channel         the Channel this Command has been called in, or
 	 *                        null if there's none (i.e. if it's a private message)
 	 * @param user            the User that wrote the Command
 	 * @param primaryArgument argument passed as commandPrefix.primaryArgument
 	 * @param args            arguments passed the the Command
 	 */
 	public abstract void exec(final Server server, final Channel channel, final Source user, final String primaryArgument, final String[] args);
 
 	/**
 	 * Sends the usage of this Command to a Receiver.
 	 *
 	 * @param receiver the receiver to send the usage to
 	 */
 	public void sendUsage(final Receiver receiver) {
 		receiver.sendMessage(this.usage);
 	}
 
 	/**
 	 * @return commandPrefix + commandName
 	 */
 	public String toString() {
 		return this.manager.getCommandPrefix() + getName();
 	}
 }
