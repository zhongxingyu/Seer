 package no.runsafe.command;
 
 import no.runsafe.framework.InjectionPlugin;
 import no.runsafe.framework.command.console.ConsoleCommand;
 import no.runsafe.framework.output.IOutput;
 
 import java.util.HashMap;
 import java.util.logging.Level;
 
 public class DebugLevelCommand extends ConsoleCommand
 {
 	public DebugLevelCommand()
 	{
 		super("debuglevel", "Changes the output debug level for plugins", "plugin", "level");
 	}
 
 	@Override
 	public String getUsageCommandParams()
 	{
		return " *|<plugin> NONE|SEVERE|WARNING|INFO|CONFIG|FINE|FINER|FINEST|ALL";
 	}
 
 	@Override
 	public String OnExecute(HashMap<String, String> parameters)
 	{
 		String pluginName = parameters.get("plugin");
 		Level level = Level.parse(parameters.get("level"));
 
 		StringBuilder result = new StringBuilder(
 			String.format("Setting debug level for %s to %s:\n", pluginName, level)
 		);
 		for (InjectionPlugin plugin : InjectionPlugin.Instances.values())
 		{
 			if ("*".equals(pluginName) || plugin.getName().startsWith(pluginName))
 			{
 				IOutput output = plugin.getComponent(IOutput.class);
 				output.setDebugLevel(level);
 				result.append(String.format("[%s] Debug level is %s.\n", plugin.getName(), output.getDebugLevel().getName()));
 			}
 		}
 		return result.toString();
 	}
 }
