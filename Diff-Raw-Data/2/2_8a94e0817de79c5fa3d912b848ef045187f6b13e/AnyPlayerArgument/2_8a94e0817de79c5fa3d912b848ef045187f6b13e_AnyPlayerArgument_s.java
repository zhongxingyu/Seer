 package no.runsafe.framework.api.command.argument;
 
 import no.runsafe.framework.api.IServer;
 import no.runsafe.framework.api.command.ICommandExecutor;
 import no.runsafe.framework.api.player.IPlayer;
 import no.runsafe.framework.internal.InjectionPlugin;
 import no.runsafe.framework.internal.command.BasePlayerArgument;
 import no.runsafe.framework.internal.extension.RunsafeServer;
 import no.runsafe.framework.internal.extension.player.RunsafeAmbiguousPlayer;
 
 import javax.annotation.Nullable;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Matcher;
 
 public class AnyPlayerArgument extends BasePlayerArgument implements IValueProvider<IPlayer>
 {
 	public AnyPlayerArgument()
 	{
 		this(false);
 	}
 
 	public AnyPlayerArgument(boolean required)
 	{
 		this(required, false);
 	}
 
 	public AnyPlayerArgument(boolean required, boolean context)
 	{
		this("world", required, context);
 	}
 
 	public AnyPlayerArgument(String name, boolean required)
 	{
 		this(name, required, false);
 	}
 
 	public AnyPlayerArgument(String name, boolean required, boolean context)
 	{
 		super(name, required, context);
 	}
 
 	@Nullable
 	@Override
 	public String expand(ICommandExecutor context, @Nullable String value)
 	{
 		if (value == null)
 			return super.expand(context, null);
 
 		Matcher quoted = QUOTEDNAME.matcher(value);
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
 		return isRequired() ? Invalid : value;
 	}
 
 	public IPlayer getValue(IPlayer context, Map<String, String> params)
 	{
 		return InjectionPlugin.getGlobalComponent(IServer.class).getPlayerExact(params.get(name));
 	}
 
 	public static final String Invalid = "\0INVALID\0";
 }
