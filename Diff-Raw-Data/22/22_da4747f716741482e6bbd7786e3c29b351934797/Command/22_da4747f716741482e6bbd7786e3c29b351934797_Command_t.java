 package no.runsafe.framework.api.command;
 
 import net.minecraft.util.com.google.common.collect.ImmutableList;
 import no.runsafe.framework.api.command.argument.IArgument;
 import no.runsafe.framework.api.command.argument.IArgumentList;
 import no.runsafe.framework.api.command.argument.IValueExpander;
 import no.runsafe.framework.api.log.IDebug;
 import no.runsafe.framework.internal.command.ArgumentList;
 import no.runsafe.framework.internal.command.PreparedSynchronousCommand;
 import no.runsafe.framework.text.ChatColour;
 import org.apache.commons.lang.StringUtils;
 import org.bukkit.craftbukkit.libs.joptsimple.internal.Strings;
 
 import javax.annotation.Nonnull;
 import javax.annotation.Nullable;
 import java.util.*;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
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
 	public Command(
 		@Nonnull String commandName, @Nonnull String description, @Nullable String permission,
 		IArgument... arguments
 	)
 	{
 		validate(arguments);
 		name = commandName;
 		this.permission = permission;
 		this.description = description;
 		Map<String, IArgument> argumentMap = new LinkedHashMap<String, IArgument>(arguments.length);
 		for (IArgument argument : arguments)
 			argumentMap.put(argument.toString(), argument);
 		argumentList = Collections.unmodifiableMap(argumentMap);
 	}
 
 	/**
 	 * Builds the usage message when an incomplete command is issued.
 	 *
 	 * @return A String to display to the player or console user
 	 */
 	@Nonnull
 	@Override
 	public String getUsage(@Nonnull ICommandExecutor executor)
 	{
 		Map<String, String> available = getAvailableSubCommands(executor);
 		List<String> usage = new ArrayList<String>(subCommands.size());
 		if (available.isEmpty())
 			return "\n " + description;
 
 		String format = "  %s%s%s: %s";
 		for (Map.Entry<String, String> stringStringEntry : available.entrySet())
 			usage.add(String.format(format, ChatColour.YELLOW, stringStringEntry.getKey(), ChatColour.RESET, stringStringEntry.getValue()));
 
 		return String.format(
 			"<%1$scommand%2$s>\nAvailable commands:\n%3$s",
 			ChatColour.YELLOW,
 			ChatColour.RESET,
 			Strings.join(usage, "\n")
 		);
 	}
 
 	/**
 	 * The command arguments listed in usage is built by this.
 	 * Override this if you have optional arguments
 	 *
 	 * @param executor
 	 * @return List of arguments for inclusion in the command usage
 	 */
 	@Nonnull
 	@Override
 	public String getUsageCommandParams(ICommandExecutor executor)
 	{
 		StringBuilder parts = new StringBuilder(ChatColour.BLUE + name + ChatColour.RESET);
 		if (!argumentList.isEmpty())
 			for (IArgument argument : argumentList.values())
 				parts.append(' ').append(getUsageCommandArgument(executor, argument));
 
 		return parts.toString();
 	}
 
 	private static String getUsageCommandArgument(ICommandExecutor executor, IArgument arg)
 	{
 		String argument = arg.toString();
		boolean isRequired = arg.isRequired();
 		if (arg instanceof IValueExpander)
 		{
			String defaultValue = ((IValueExpander) arg).expand(executor, null);
 			if (defaultValue != null)
			{
 				argument += '=' + defaultValue;
				isRequired = false;
			}
			else
				isRequired = true;
 		}
 		return String.format(
			isRequired ? "<%s%s%s>%s" : "[%s%s%s]%s",
 			ChatColour.YELLOW, argument, ChatColour.RESET,
 			arg.isWhitespaceInclusive() ? "+" : ""
 		);
 	}
 
 	/**
 	 * @return The permission required to execute this command
 	 */
 	@Nullable
 	@Override
 	public final String getPermission()
 	{
 		return permission;
 	}
 
 	/**
 	 * Adds a subcommand to this command
 	 *
 	 * @param subCommand The subcommand to add to this command
 	 */
 	public final void addSubCommand(ICommandHandler subCommand)
 	{
 		subCommands.put(subCommand.getName(), subCommand);
 	}
 
 	/**
 	 * Resolve a subcommand
 	 *
 	 * @param executor The player or console executing the command
 	 * @param name     The partial or full subcommand name
 	 * @return The selected subcommand or null if no matches
 	 */
 	@SuppressWarnings("VariableNotUsedInsideIf")
 	@Nullable
 	public final ICommandHandler getSubCommand(ICommandExecutor executor, String name)
 	{
 		if (subCommands.isEmpty())
 			return null;
 
 		if (subCommands.containsKey(name))
 			return subCommands.get(name);
 
 		String target = null;
 		for (String sub : subCommands.keySet())
 			if (sub.startsWith(name) && subCommands.get(sub).isExecutable(executor))
 			{
 				if (target != null)
 					return null;
 
 				target = sub;
 			}
 		if (target != null)
 			return subCommands.get(target);
 
 		return null;
 	}
 
 	@Nonnull
 	@Override
 	public final List<String> getSubCommands(ICommandExecutor executor)
 	{
 		List<String> available = new ArrayList<String>(subCommands.size());
 		for (Map.Entry<String, ICommandHandler> stringCommandEntry : subCommands.entrySet())
 			if (stringCommandEntry.getValue().isExecutable(executor))
 				available.add(stringCommandEntry.getKey());
 		return available;
 	}
 
 	/**
 	 * @return The name of this command
 	 */
 	@Nonnull
 	@Override
 	public final String getName()
 	{
 		return name;
 	}
 
 	@Nonnull
 	@Override
 	public String getDescription()
 	{
 		return description;
 	}
 
 	/**
 	 * Parses user input and returns a prepared command, ready to be executed
 	 *
 	 * @param executor The user or console executing the command
 	 * @param args     The passed argument list
 	 * @return A prepared command, ready to be executed
 	 */
 	@Nonnull
 	@Override
 	public final IPreparedCommand prepare(ICommandExecutor executor, @Nonnull String... args)
 	{
 		console.debugFiner("Preparing command %s %s", name, StringUtils.join(args, " "));
 		return prepareCommand(executor, new HashMap<String, String>(args.length), args, new Stack<ICommandHandler>());
 	}
 
 	@Nonnull
 	@Override
 	public final IPreparedCommand prepareCommand(
 		@Nonnull ICommandExecutor executor,
 		@Nonnull Map<String, String> params,
 		@Nonnull String[] args,
 		@Nonnull Stack<ICommandHandler> stack
 	)
 	{
 		stack.add(this);
 		Map<String, String> myParams = parseParameters(executor, args);
 		params.putAll(myParams);
 		if (!myParams.isEmpty())
 		{
 			args = args.length <= myParams.size()
 				? new String[0] :
 				Arrays.copyOfRange(args, myParams.size(), args.length);
 		}
 		console.debugFiner("Command %s has %d parameters and %d args", name, myParams.size(), args.length);
 		if (args.length > 0)
 		{
 			console.debugFiner("Looking for subcommand %s", args[0]);
 			ICommandHandler subCommand = getSubCommand(executor, args[0]);
 			if (subCommand != null && subCommand.isExecutable(executor))
 			{
 				subCommand.setConsole(console);
 				args = Arrays.copyOfRange(args, 1, args.length);
 				console.debugFiner("Preparing subcommand %s", executor.getName());
 				return subCommand.prepareCommand(executor, params, args, stack);
 			}
 		}
 		return stack.peek().createAction(executor, stack, args, new ArgumentList(executor, argumentList, params));
 	}
 
 	@Nonnull
 	@Override
 	public IPreparedCommand createAction(
 		@Nonnull ICommandExecutor executor,
 		@Nonnull Stack<ICommandHandler> stack,
 		@Nonnull String[] args,
 		@Nonnull IArgumentList params
 	)
 	{
 		console.debugFiner("Preparing Sync command with %d params and %d args", params.size(), args.length);
 		return new PreparedSynchronousCommand(executor, stack, args, params);
 	}
 
 	/**
 	 * Called by the framework to register a console object for debug output
 	 *
 	 * @param console The console to print debug information to
 	 */
 	@Override
 	public void setConsole(@Nonnull IDebug console)
 	{
 		console.debugFiner("Setting console on command object.");
 		this.console = console;
 	}
 
 	@Nullable
 	@Override
 	public List<String> getParameterOptions(@Nonnull String parameter)
 	{
 		return null;
 	}
 
 	@Nullable
 	@Override
 	public List<String> getParameterOptionsPartial(@Nonnull String parameter, @Nonnull String arg)
 	{
 		return null;
 	}
 
 	@Nonnull
 	@Override
 	public List<IArgument> getParameters()
 	{
 		return ImmutableList.copyOf(argumentList.values());
 	}
 
 	@Override
 	public boolean isExecutable(@Nonnull ICommandExecutor executor)
 	{
 		if (permission == null)
 		{
 			for (ICommandHandler subCommand : subCommands.values())
 				if (subCommand.isExecutable(executor))
 					return true;
 
 			return !getClass().equals(Command.class);
 		}
 		return checkPermission(executor);
 	}
 
 	private static void validate(IArgument... arguments)
 	{
 		boolean optional = false;
 		boolean whitespace = false;
 		for (IArgument argument : arguments)
 		{
 			if (whitespace)
 				throw new IllegalArgumentException("Whitespace may only be captured by the final argument!");
 
 			if (optional && argument.isRequired())
 				throw new IllegalArgumentException("There may not be required parameters after an optional one!");
 
 			if (!argument.isRequired())
 				optional = true;
 
 			if (argument.isWhitespaceInclusive())
 				whitespace = true;
 		}
 	}
 
 	private Map<String, String> getAvailableSubCommands(ICommandExecutor executor)
 	{
 		Map<String, String> available = new HashMap<String, String>(subCommands.size());
 		for (ICommandHandler sub : subCommands.values())
 		{
 			if (sub.isExecutable(executor))
 				available.put(sub.getName(), String.format(" - %s", sub.getDescription()));
 		}
 		return available;
 	}
 
 	private boolean checkPermission(ICommandExecutor executor)
 	{
 		Matcher params = paramPermission.matcher(permission);
 		if (params.find())
 		{
 			Iterable<String> options = getParameterOptions(params.group());
 			if (options == null)
 				return true;
 			for (String value : options)
 				if (executor.hasPermission(params.replaceAll(value)))
 					return true;
 			return false;
 		}
 		return executor.hasPermission(permission);
 	}
 
 	private Map<String, String> parseParameters(ICommandExecutor context, String... args)
 	{
 		Map<String, String> parameters = new HashMap<String, String>(args.length);
 
 		int index = 0;
 		for (IArgument parameter : argumentList.values())
 		{
 			if (parameter.isWhitespaceInclusive())
 			{
 				if (args.length > index)
 				{
 					parameters.put(parameter.toString(), StringUtils.join(args, " ", index, args.length));
 					break;
 				}
 			}
 
 			String value = null;
 			if (args.length > index)
 				value = args[index];
 			if (value != null && parameter instanceof IValueExpander)
 				value = ((IValueExpander) parameter).expand(context, value);
 			index++;
 			if (parameter.isRequired() || value != null && !value.isEmpty())
 				parameters.put(parameter.toString(), value);
 		}
 		return parameters;
 	}
 
 	protected IDebug console;
 	protected final Map<String, IArgument> argumentList;
 	private final Map<String, ICommandHandler> subCommands = new HashMap<String, ICommandHandler>(0);
 	private final String name;
 	private final String permission;
 	private final String description;
 	private static final Pattern paramPermission = Pattern.compile("<(.*)>");
 }
