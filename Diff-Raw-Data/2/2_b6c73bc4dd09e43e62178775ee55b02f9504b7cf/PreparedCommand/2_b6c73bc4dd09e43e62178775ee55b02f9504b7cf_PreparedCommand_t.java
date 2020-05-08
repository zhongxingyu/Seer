 package no.runsafe.framework.internal.command;
 
 import com.google.common.collect.Lists;
 import no.runsafe.framework.api.command.*;
 import no.runsafe.framework.api.command.argument.IArgument;
 import no.runsafe.framework.api.command.argument.IArgumentList;
 import no.runsafe.framework.api.command.argument.IContextualTabComplete;
 import no.runsafe.framework.api.command.argument.ITabComplete;
 import no.runsafe.framework.api.player.IPlayer;
 import org.apache.commons.lang.StringUtils;
 
 import javax.annotation.Nonnull;
 import javax.annotation.Nullable;
 import java.util.*;
 import java.util.regex.Pattern;
 
 public abstract class PreparedCommand implements IPreparedCommand
 {
 	protected PreparedCommand(
 		ICommandExecutor executor, Stack<ICommandHandler> definingCommand, String[] args, IArgumentList parameters)
 	{
 		this.executor = executor;
 		command = definingCommand;
 		arguments = args;
 		this.parameters = parameters;
 		ICommandHandler execute = command.peek();
 		String permission = null;
 		if (execute instanceof IContextPermissionProvider)
 			permission = ((IContextPermissionProvider) execute).getPermission(executor, parameters, args);
 		if (permission == null)
 			permission = command.peek().getPermission();
 		if (permission != null)
 			for (Map.Entry<String, String> parameter : parameters.entrySet())
 				if (parameter.getValue() != null)
 					permission = permission.replace('<' + parameter.getKey() + '>', parameter.getValue());
 		requiredPermission = permission;
 	}
 
 	@Override
 	@Nullable
 	public String getRequiredPermission()
 	{
 		if (requiredPermission == null || paramPermission.matcher(requiredPermission).matches())
 			return null;
 		return requiredPermission;
 	}
 
 	@Override
 	@Nullable
 	public Iterable<String> tabComplete(String... args)
 	{
 		int i = countSuperParams();
 		List<IArgument> params = command.peek().getParameters();
 		List<String> subcommands = command.peek().getSubCommands(executor);
 		boolean takeParams = !params.isEmpty();
 		boolean takeSub = !subcommands.isEmpty();
 		boolean whitespaceInclusive = params.get(params.size() - 1).isWhitespaceInclusive();
 
 		if (!(takeParams || takeSub))
 			return Lists.newArrayList();
 
 		if (args.length > i + params.size() + 1 && !whitespaceInclusive)
 			return null;
 
 		if (takeParams && whitespaceInclusive)
			return getSuggestions(params.get(params.size() - 1), args);
 
 		if (takeParams && args.length - i > 0 && args.length - i <= params.size())
 			return getSuggestions(params.get(args.length - i - 1), args);
 
 		if (takeSub)
 			return filterList(command.peek().getSubCommands(executor), args[args.length - 1]);
 
 		return null;
 	}
 
 	@Override
 	@Nullable
 	public Iterable<String> getSuggestions(@Nonnull IArgument param, @Nonnull String... args)
 	{
 		List<String> matches;
 		if (param instanceof IContextualTabComplete)
 			matches = ((IContextualTabComplete) param).getAlternatives(
 				(IPlayer)executor,
 				args[args.length - 1],
 				Arrays.copyOfRange(args, 0, args.length - 1)
 			);
 
 		else if (param instanceof ITabComplete)
 			matches = ((ITabComplete) param).getAlternatives((IPlayer) executor, args[args.length - 1]);
 
 		else
 		{
 			matches = command.peek().getParameterOptionsPartial(param.toString(), args[args.length - 1]);
 			if (matches != null)
 			{
 				if (matches.isEmpty())
 					return null;
 				return matches;
 			}
 			matches = command.peek().getParameterOptions(param.toString());
 			if (matches == null)
 				return Lists.newArrayList();
 		}
 		return matches == null || args[args.length - 1].isEmpty() ? matches : filterList(matches, args[args.length - 1]);
 	}
 
 	@Override
 	public int countSuperParams()
 	{
 		int i = 0;
 		for (ICommandHandler cmd : command)
 		{
 			if (cmd.equals(command.peek()))
 				break;
 			i += cmd.getParameters().size(); // Args taken by command
 			i++; // Arg taken by selecting the next subcommand
 		}
 		return i;
 	}
 
 	@Override
 	public String usage(ICommandHandler target)
 	{
 		Collection<String> params = new ArrayList<String>(command.size());
 		for (ICommandHandler tier : command)
 			params.add(tier.getUsageCommandParams(executor));
 
 		//noinspection HardcodedFileSeparator
 		return String.format("Usage: /%1$s %2$s", StringUtils.join(params, " "), target.getUsage(executor));
 	}
 
 	private static List<String> filterList(Iterable<String> values, String filter)
 	{
 		if (filter == null || filter.isEmpty())
 			return Lists.newArrayList(values);
 		List<String> matches = new ArrayList<String>(1);
 		for (String value : values)
 			if (value.toLowerCase().startsWith(filter.toLowerCase()))
 				matches.add(value);
 		return matches;
 	}
 
 	protected final ICommandExecutor executor;
 	protected final Stack<ICommandHandler> command;
 	protected final String[] arguments;
 	protected final IArgumentList parameters;
 	private final String requiredPermission;
 	private static final Pattern paramPermission = Pattern.compile(".*<.*>.*");
 }
