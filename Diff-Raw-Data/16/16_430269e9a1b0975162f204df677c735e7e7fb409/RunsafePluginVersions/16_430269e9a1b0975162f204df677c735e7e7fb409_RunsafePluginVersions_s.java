 package no.runsafe.command;
 
 import no.runsafe.framework.RunsafePlugin;
 import no.runsafe.framework.command.ExecutableCommand;
 import no.runsafe.framework.server.ICommandExecutor;
 
 import java.util.HashMap;
 
 public class RunsafePluginVersions extends ExecutableCommand
 {
 	public RunsafePluginVersions()
 	{
 		super("plugins", "Lists information about plugins using the runsafe framework", "runsafe.plugin.list");
 	}
 
 	@Override
	public String OnExecute(ICommandExecutor executor, HashMap<String, String> parameters, String[] args)
 	{
 		StringBuilder result = new StringBuilder();
 		for (String plugin : RunsafePlugin.Instances.keySet())
 			result.append(String.format("%s %s\n", plugin, RunsafePlugin.Instances.get(plugin).getDescription().getVersion()));
 
 		return result.toString();
 	}
 }
