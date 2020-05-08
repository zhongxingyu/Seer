 package no.runsafe;
 
 import no.runsafe.framework.RunsafePlugin;
 import no.runsafe.framework.api.command.ExecutableCommand;
 import no.runsafe.framework.api.command.ICommandExecutor;
import no.runsafe.framework.api.command.ICommandHandler;
 import no.runsafe.framework.api.command.argument.IArgumentList;
 import no.runsafe.framework.api.command.argument.RequiredArgument;
 import no.runsafe.framework.internal.command.ITabExecutor;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.PluginCommand;
 
 public class HelpCommand extends ExecutableCommand
 {
 	public HelpCommand(RunsafePlugin plugin)
 	{
 		super("help", "Get command usage help", "runsafe.help", new RequiredArgument("command"), new SubCommandArgument(plugin));
 		this.plugin = plugin;
 	}
 
 	@Override
 	public String OnExecute(ICommandExecutor executor, IArgumentList parameters)
 	{
 		PluginCommand command = plugin.getServer().getPluginCommand(parameters.get("command"));
 		if (command == null)
 			return "No such command exists.";
 		CommandExecutor commandExecutor = command.getExecutor();
 		if (commandExecutor instanceof ITabExecutor)
 		{
 			String path = parameters.get("subcommand-path");
			ICommandHandler handler = ((ITabExecutor) commandExecutor).getHandler();
			if (handler == null)
				return "Invalid command.";
 			if (path != null)
				return "Usage: /" + handler.getSubCommandUsage(executor, path.split("\\s+"));
			String subUsage = handler.getSubCommandUsage(executor);
			return "Usage: /" + subUsage;
 		}
 
 		return "Usage: " + command.getUsage().replace("<command>", parameters.get("command"));
 	}
 
 	private final RunsafePlugin plugin;
 }
