 package no.runsafe;
 
 import no.runsafe.framework.RunsafePlugin;
 import no.runsafe.framework.api.IKernel;
 import no.runsafe.framework.api.command.argument.EnumArgument;
 import no.runsafe.framework.api.command.argument.RequiredArgument;
 import no.runsafe.framework.api.command.console.ConsoleCommand;
 import no.runsafe.framework.api.log.IDebug;
 import no.runsafe.framework.internal.log.Debug;
 
 import javax.annotation.Nonnull;
 import java.util.ArrayList;
 import java.util.Map;
 import java.util.logging.Level;
 
 public class DebugLevelCommand extends ConsoleCommand
 {
 	public DebugLevelCommand()
 	{
 		super(
 			"debuglevel", "Changes the output debug level for plugins",
			new RequiredArgument("plugin"), new EnumArgument("enum", DebugLevel.values(), true)
 		);
 	}
 
 	@Nonnull
 	@Override
 	public String getUsageCommandParams()
 	{
 		return " *|<plugin> OFF|SEVERE|WARNING|INFO|CONFIG|FINE|FINER|FINEST|ALL";
 	}
 
 	@SuppressWarnings("StringToUpperCaseOrToLowerCaseWithoutLocale")
 	@Override
 	public String OnExecute(Map<String, String> parameters)
 	{
 		String pluginName = parameters.get("plugin");
 		Level level = Level.parse(parameters.get("level"));
 
 		if ("*".equals(pluginName))
 			Debug.Global().setDebugLevel(level);
 
 		for (IKernel plugin : RunsafePlugin.getPlugins(pluginName))
 		{
 			IDebug debugger = plugin.getComponent(IDebug.class);
 			debugger.setDebugLevel(level);
 		}
 		return String.format("Set debug level for plugins matching %s to %s", pluginName, level);
 	}
 }
