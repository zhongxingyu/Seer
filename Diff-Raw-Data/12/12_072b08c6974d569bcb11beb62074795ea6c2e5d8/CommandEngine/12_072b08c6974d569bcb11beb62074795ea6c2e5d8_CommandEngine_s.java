 package no.runsafe.framework.internal.command;
 
 import no.runsafe.framework.RunsafePlugin;
 import no.runsafe.framework.api.IOutput;
 import no.runsafe.framework.api.command.ICommandExecutor;
 import no.runsafe.framework.api.command.ICommandHandler;
 import no.runsafe.framework.minecraft.RunsafeConsole;
 import org.bukkit.command.PluginCommand;
 import org.picocontainer.Startable;
 
 import javax.annotation.Nonnull;
 import javax.annotation.Nullable;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * This class handles the registration of command objects with bukkit.
  */
 public final class CommandEngine implements Startable
 {
 	/**
 	 * @param output   The console to output debug information to
 	 * @param commands The commands provided by the plugin
 	 * @param plugin   The plugin
 	 */
 	public CommandEngine(@Nullable IOutput output, @Nonnull RunsafePlugin plugin, @Nonnull ICommandHandler... commands)
 	{
 		this.commands = commands;
 		this.plugin = plugin;
 		console = new RunsafeConsole(output);
 		this.output = output;
 	}
 
 	/**
 	 * Hooks the commands into bukkit when plugin starts
 	 */
 	@Override
 	public void start()
 	{
 		for (BukkitCommandTabExecutor executor : getCommands())
 			hookCommand(plugin.getCommand(executor.getName()), executor);
 	}
 
 	/**
 	 * Not used
 	 */
 	@Override
 	public void stop()
 	{
 	}
 
 	private void hookCommand(PluginCommand command, BukkitCommandTabExecutor executor)
 	{
 		assert output != null;
 		if (command == null)
 			output.logError("Command not found: %s - does it exist in plugin.yml?", executor.getName());
 		else
 		{
 			command.setExecutor(executor);
 			output.finer("Command handler for %s registered with bukkit.", executor.getName());
 		}
 	}
 
 	private Iterable<BukkitCommandTabExecutor> getCommands()
 	{
 		List<BukkitCommandTabExecutor> handlers = new ArrayList<BukkitCommandTabExecutor>(commands.length);
 		for (ICommandHandler command : commands)
 		{
 			command.setConsole(output);
 			handlers.add(new BukkitCommandTabExecutor(command, console, output));
 		}
 		return handlers;
 	}
 
 	@Nullable
 	private final ICommandExecutor console;
 	@Nullable
 	private final IOutput output;
 	@Nonnull
 	private final ICommandHandler[] commands;
 	@Nonnull
 	private final RunsafePlugin plugin;
 }
