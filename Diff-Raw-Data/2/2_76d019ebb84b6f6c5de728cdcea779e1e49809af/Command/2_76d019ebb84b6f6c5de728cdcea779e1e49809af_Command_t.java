 package io.arkeus.fatebot.commands;
 
 import io.arkeus.fatebot.Fate;
 
 /**
  * A class containing information about a single bot command.
  */
 public abstract class Command {
 	protected final int expectedParameters;
 
 	protected Fate bot;
 	protected String channel;
 	protected String sender;
 	protected String login;
 	protected String hostname;
 	protected CommandArguments arguments;
 	protected boolean requiresAdministrator;
 
 	public Command(final int expectedParameters) {
 		this(expectedParameters, false);
 	}
 
 	public Command(final int expectedParameters, final boolean requiresAdministrator) {
 		this.expectedParameters = expectedParameters;
 		this.requiresAdministrator = requiresAdministrator;
 	}
 
 	/**
 	 * Initializes a command with all the information regarding who triggered the command.
 	 *
 	 * @param bot The bot instance.
 	 * @param channel The channel the command was used in.
 	 * @param sender The user who used the command.
 	 * @param login The login of the user who used the command.
 	 * @param hostname The hostname of the user who used the command.
 	 * @param message The entire command message.
 	 * @throws CommandException
 	 */
 	public void initialize(final Fate bot, final String channel, final String sender, final String login, final String hostname, final String message) throws CommandException {
 		this.bot = bot;
 		this.channel = channel;
 		this.sender = sender;
 		this.login = login;
 		this.hostname = hostname;
 		this.arguments = new CommandArguments(message, expectedParameters);
 	}
 
 	/**
 	 * Validates arguments to the command and executes it. Outputs any {@link CommandException}
 	 * directly to the channel.
 	 */
 	public void execute() {
 		try {
 			validate();
 			run();
 		} catch(final CommandException e) {
 			bot.sendMessage(channel, "Error: " + e.getMessage());
 			Fate.logger.error("Recieved invalid command or invalid arguments, ignoring", e);
 		} catch (final InvalidPermissionsException e) {
 			// ignore, user using the command does not have permissions
 		}
 	}
 
 	/**
 	 * Validates all input to the command.
 	 *
 	 * @throws CommandException if validation fails.
 	 * @throws InvalidPermissionsException
 	 */
 	protected void validate() throws CommandException, InvalidPermissionsException {
		if (requiresAdministrator && !bot.isAdministrator(sender)) {
 			throw new InvalidPermissionsException();
 		}
 	}
 
 	/**
 	 * Runs the command. Each command has its own implementation.
 	 *
 	 * @throws CommandException
 	 */
 	protected abstract void run() throws CommandException;
 }
