 package no.runsafe.framework.api.command;
 
 import com.google.common.collect.ImmutableList;
 import no.runsafe.framework.api.IOutput;
 import no.runsafe.framework.internal.command.prepared.PreparedAsynchronousCallbackCommand;
 import no.runsafe.framework.internal.command.prepared.PreparedAsynchronousCommand;
 import no.runsafe.framework.internal.command.prepared.PreparedSynchronousCommand;
 import no.runsafe.framework.text.ChatColour;
 import org.apache.commons.lang.StringUtils;
 
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Stack;
 
 /**
  * The base command class of the framework
  * Create instances of this object for commands that only contain subcommands
  */
 public class Command implements ICommandHandler
 {
 	/**
 	 * Defines the command
 	 *
 	 * @param commandName The name of the command. For top level commands, this must be as defined in plugin.yml
 	 * @param description A short descriptive text of what the command does
 	 * @param permission  A permission String that a player must have to run the command or null to allow anyone to run it
 	 * @param arguments   Optional list of required command parameters
 	 */
 	public Command(String commandName, String description, String permission, String... arguments)
 	{
 		this.name = commandName;
 		this.permission = permission;
 		this.description = description;
 		if (arguments == null)
 			this.argumentList = null;
 		else
 			this.argumentList = ImmutableList.copyOf(arguments);
 	}
 
 	/**
 	 * Builds the usage message when an incomplete command is issued.
 	 *
 	 * @return A String to display to the player or console user
 	 */
 	public String getUsage(ICommandExecutor executor)
 	{
 		HashMap<String, String> available = new HashMap<String, String>();
 		if (!subCommands.isEmpty())
 		{
 			for (Command sub : subCommands.values())
 			{
 				if (sub.permission == null || executor.hasPermission(sub.permission))
 				{
 					if (sub.description == null)
 						available.put(sub.getName(), "");
 					else
 						available.put(sub.getName(), String.format(" - %s", sub.description));
 				}
 			}
 		}
 		StringBuilder usage = new StringBuilder();
 		if (available.isEmpty())
 			return description == null ? "" : description;
 
 		int width = 0;
 		for (String cmd : available.keySet())
 			if (cmd.length() > width)
 				width = cmd.length();
 		String format = String.format("  %%1s%%2$-%ds%%3$s%%4$s\n", width);
 		for (String cmd : available.keySet())
 			usage.append(String.format(format, ChatColour.YELLOW, cmd, ChatColour.RESET, available.get(cmd)));
 
 		return String.format(
 			"<%1$scommand%2$s>\nAvailable commands:\n%3$s",
 			ChatColour.YELLOW,
 			ChatColour.RESET,
 			usage
 		);
 	}
 
 	/**
 	 * The command arguments listed in usage is built by this.
 	 * Override this if you have optional arguments
 	 *
 	 * @return List of arguments for inclusion in the command usage
 	 */
 	public String getUsageCommandParams()
 	{
 		String part = ChatColour.BLUE + name + ChatColour.RESET;
 		if (!argumentList.isEmpty())
 			part += " <" +
 				ChatColour.YELLOW + StringUtils.join(
 				argumentList,
 				ChatColour.RESET + "> <" + ChatColour.YELLOW
 			) + ChatColour.RESET + ">";
 		return part;
 	}
 
 	/**
 	 * @return The permission required to execute this command
 	 */
 	public final String getPermission()
 	{
 		return permission;
 	}
 
 	/**
 	 * Adds a subcommand to this command
 	 *
 	 * @param subCommand The subcommand to add to this command
 	 */
 	public final void addSubCommand(Command subCommand)
 	{
 		subCommands.put(subCommand.getName(), subCommand);
 	}
 
 	/**
 	 * Resolve a subcommand
 	 *
 	 * @param name The partial or full subcommand name
 	 * @return The selected subcommand or null if no matches
 	 */
 	public final Command getSubCommand(String name)
 	{
 		if (subCommands.isEmpty())
 			return null;
 
 		if (subCommands.containsKey(name))
 			return subCommands.get(name);
 
 		String target = null;
 		for (String sub : subCommands.keySet())
 			if (sub.startsWith(name))
 			{
 				if (target != null)
 					return null;
 
 				target = sub;
 			}
 		if (target != null)
			return subCommands.get(target);
 
 		return null;
 	}
 
 	/**
 	 * @return The name of this command
 	 */
 	public final String getName()
 	{
 		return name;
 	}
 
 	/**
 	 * Call this method in your constructor if the final parameter should grab all tailing arguments
 	 * i.e. if you want to support spaces without "" for input to a command
 	 */
 	protected final void captureTail()
 	{
 		captureTail = true;
 	}
 
 	/**
 	 * Parses user input and returns a prepared command, ready to be executed
 	 *
 	 * @param executor The user or console executing the command
 	 * @param args     The passed argument list
 	 * @return A prepared command, ready to be executed
 	 */
 	@Override
 	public final IPreparedCommand prepare(ICommandExecutor executor, String[] args)
 	{
 		if (args != null)
 			console.finer("Preparing command %s %s", getName(), StringUtils.join(args, " "));
 		else
 			console.finer("Preparing command %s", getName());
 		return prepare(executor, new HashMap<String, String>(), args, new Stack<Command>());
 	}
 
 	/**
 	 * Called by the framework to register a console object for debug output
 	 *
 	 * @param console The console to print debug information to
 	 */
 	@Override
 	public void setConsole(IOutput console)
 	{
 		console.finer("Setting console on command object.");
 		this.console = console;
 	}
 
 	private IPreparedCommand prepare(ICommandExecutor executor, HashMap<String, String> params, String[] args, Stack<Command> stack)
 	{
 		stack.add(this);
 		HashMap<String, String> myParams = getParameters(args);
 		params.putAll(myParams);
 		if (myParams.size() > 0)
 		{
 			if (!captureTail && args.length > myParams.size())
 				args = Arrays.copyOfRange(args, myParams.size(), args.length);
 			else
 				args = new String[0];
 		}
 		console.finer("Command %s has %d parameters and %d args", getName(), myParams.size(), args.length);
 		if (args.length > 0)
 		{
 			console.finer("Looking for subcommand %s", args[0]);
 			Command subCommand = getSubCommand(args[0]);
 			if (subCommand != null)
 			{
 				subCommand.setConsole(console);
 				args = Arrays.copyOfRange(args, 1, args.length);
 				console.finer("Preparing subcommand %s", executor.getName());
 				return subCommand.prepare(executor, params, args, stack);
 			}
 		}
 
 		if (stack.peek() instanceof AsyncCallbackCommand)
 		{
 			console.finer("Preparing AsyncCallback command with %d params and %d args", params.size(), args.length);
 			return new PreparedAsynchronousCallbackCommand(executor, stack, args, params);
 		}
 		if (stack.peek() instanceof AsyncCommand)
 		{
 			console.finer("Preparing Async command with %d params and %d args", params.size(), args.length);
 			return new PreparedAsynchronousCommand(executor, stack, args, params);
 		}
 		console.finer("Preparing Sync command with %d params and %d args", params.size(), args.length);
 		return new PreparedSynchronousCommand(executor, stack, args, params);
 	}
 
 	private HashMap<String, String> getParameters(String[] args)
 	{
 		HashMap<String, String> parameters = new HashMap<String, String>();
 
 		int index = 0;
 		for (String parameter : argumentList)
 		{
 			String value = null;
 			if (args.length > index)
 				value = args[index];
 			index++;
 			parameters.put(parameter, value);
 		}
 		if (captureTail && args.length > index)
 			parameters.put(argumentList.get(index - 1), StringUtils.join(args, " ", index - 1, args.length));
 		return parameters;
 	}
 
 	protected IOutput console;
 	private final ImmutableList<String> argumentList;
 	private final HashMap<String, Command> subCommands = new HashMap<String, Command>();
 	private final String name;
 	private final String permission;
 	private final String description;
 	private boolean captureTail;
 }
