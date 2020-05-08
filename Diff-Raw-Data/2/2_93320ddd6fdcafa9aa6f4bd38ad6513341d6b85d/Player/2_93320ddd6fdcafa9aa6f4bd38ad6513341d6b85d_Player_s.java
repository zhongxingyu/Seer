 package no.runsafe.framework.api.command.argument;
 
 import no.runsafe.framework.api.IServer;
 import no.runsafe.framework.api.command.Command;
 import no.runsafe.framework.api.command.ICommandExecutor;
 import no.runsafe.framework.api.player.IPlayer;
 import no.runsafe.framework.internal.InjectionPlugin;
 import no.runsafe.framework.internal.command.argument.BasePlayerArgument;
 import no.runsafe.framework.internal.extension.RunsafeServer;
 import no.runsafe.framework.internal.extension.player.RunsafeAmbiguousPlayer;
 
 import javax.annotation.Nullable;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Matcher;
 
 public class Player extends BasePlayerArgument
 {
 	public Player()
 	{
 		this("player");
 	}
 
 	public Player(String name)
 	{
 		super(name);
 	}
 
 	public Player onlineOnly()
 	{
 		onlineOnly = true;
 		return this;
 	}
 
 	public Player defaultToExecutor()
 	{
 		expand = true;
 		return this;
 	}
 
 	@Nullable
 	@Override
 	public String expand(ICommandExecutor context, @Nullable String value)
 	{
 		if (onlineOnly)
 		{
 			return context instanceof IPlayer
 				? expandOnlineForPlayer((IPlayer) context, value)
 				: expandOnlineForConsole(context, value);
 		}
 		return expandAny(context, value);
 	}
 
 	@Override
 	public IPlayer getValue(IPlayer context, Map<String, String> params)
 	{
 		return onlineOnly ? getOnlineValue(context, params) : getAnyValue(context, params);
 	}
 
 	private IPlayer getAnyValue(IPlayer context, Map<String, String> params)
 	{
 		String param = params.get(name);
 		if (param == null || param.isEmpty())
 			return defaultValue;
 		return InjectionPlugin.getGlobalComponent(IServer.class).getPlayerExact(param);
 	}
 
 	private IPlayer getOnlineValue(IPlayer context, Map<String, String> params)
 	{
 		String param = params.get(name);
 		if (param == null || param.isEmpty())
 			return defaultValue;
 		return InjectionPlugin.getGlobalComponent(IServer.class).getPlayerExact(param);
 	}
 
 	private String expandAny(ICommandExecutor context, @Nullable String value)
 	{
 		if (value == null)
 			return super.expand(context, null);
 
 		Matcher quoted = Command.QUOTED_ARGUMENT.matcher(value);
 		if (quoted.matches())
 			return quoted.group(1);
 
 		List<String> matches = RunsafeServer.findPlayer(value);
 		if (matches.size() > 1)
 		{
 			context.sendColouredMessage(new RunsafeAmbiguousPlayer(null, matches).toString());
 			if (!isRequired() && expand)
 				return null;
 		}
 		if (matches.size() == 1)
 			return matches.get(0);
 
 		context.sendColouredMessage("Unable to locate any players matching '%s'!", value);
 		return null;
 	}
 
 	private String expandOnlineForConsole(ICommandExecutor context, @Nullable String value)
 	{
 		Matcher quoted = Command.QUOTED_ARGUMENT.matcher(value);
 		if (quoted.matches())
 		{
 			IPlayer target = no.runsafe.framework.internal.Player.Get().getExact(quoted.group(1));
 			if (target.isOnline())
 				return target.getName();
 			return null;
 		}
 
 		List<String> matches = no.runsafe.framework.internal.Player.Get().getOnline(value);
 		if (matches.size() > 1)
 		{
 			context.sendColouredMessage(new RunsafeAmbiguousPlayer(null, matches).toString());
 			if (!isRequired() && expand)
 				return null;
 		}
 		if (matches != null && matches.size() == 1)
 			return matches.get(0);
 
 		context.sendColouredMessage("Unable to locate any players matching '%s'!", value);
 		return null;
 	}
 
 	@Nullable
 	private String expandOnlineForPlayer(IPlayer context, String value)
 	{
 		Matcher quoted = Command.QUOTED_ARGUMENT.matcher(value);
 		if (quoted.matches())
 		{
 			IPlayer target = no.runsafe.framework.internal.Player.Get().getExact(quoted.group(1));
 			if (context.shouldNotSee(target))
 				return null;
 			return quoted.group(1);
 		}
 		List<String> matches = no.runsafe.framework.internal.Player.Get().getOnline(context, value);
 		if (matches.size() > 1)
 		{
 			context.sendColouredMessage(new RunsafeAmbiguousPlayer(null, matches).toString());
 			if (!isRequired() && expand)
 				return null;
 		}
 		if (matches != null && matches.size() == 1)
 			return matches.get(0);
 
 		context.sendColouredMessage("Unable to locate any players matching '%s'!", value);
 		return null;
 	}
 
 	private boolean onlineOnly;
 }
