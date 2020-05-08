 package no.runsafe.framework.command.console;
 
 import no.runsafe.framework.command.AsyncCommand;
 import no.runsafe.framework.server.ICommandExecutor;
 import no.runsafe.framework.server.RunsafeConsole;
 import no.runsafe.framework.timer.IScheduler;
 
 import java.util.HashMap;
 
 /**
  * Base class representing a command that can only be executed by the console and has an implementation that can be executed asynchronously
  * WARNING: Do not call bukkit APIs from the background thread!
  */
 public abstract class ConsoleAsyncCommand extends AsyncCommand
 {
 	public ConsoleAsyncCommand(String name, String description, IScheduler scheduler, String... args)
 	{
 		super(name, description, null, scheduler, args);
 	}
 
 	@Override
 	public final String OnExecute(ICommandExecutor executor, HashMap<String, String> parameters, String[] arguments)
 	{
 		if (executor instanceof RunsafeConsole)
 			return OnExecute(parameters, arguments);
 		return "This command must be used from the console.";
 	}
 
 	@Override
 	public final String OnAsyncExecute(ICommandExecutor executor, HashMap<String, String> parameters, String[] arguments)
 	{
 		if (executor instanceof RunsafeConsole)
			return OnAsyncExecute(executor, parameters, arguments);
 		return "This command must be used from the console.";
 	}
 
 	@Override
 	public final String OnAsyncExecute(ICommandExecutor executor, HashMap<String, String> parameters)
 	{
 		if (executor instanceof RunsafeConsole)
 			return OnAsyncExecute(executor, parameters);
 		return "This command must be used from the console.";
 	}
 
 	/**
 	 * This method is called on the main thread before {@link ConsoleAsyncCommand#OnAsyncExecute(java.util.HashMap)}
 	 * Override this method if you use optional arguments
 	 *
 	 * @param parameters The arguments you defined in the constructor and their values as supplied by the user
 	 * @param arguments  Tailing arguments not asked for in the command definition
 	 * @return Message to show in the console
 	 */
 	public String OnExecute(HashMap<String, String> parameters, String[] arguments)
 	{
 		return OnExecute(parameters);
 	}
 
 	/**
 	 * This method is called on the main thread before {@link ConsoleAsyncCommand#OnAsyncExecute(java.util.HashMap)}
 	 * Override this method if you don't use optional arguments
 	 *
 	 * @param parameters The arguments you defined in the constructor and their values as supplied by the user
 	 * @return Message to show in the console
 	 */
 	public String OnExecute(HashMap<String, String> parameters)
 	{
 		return null;
 	}
 
 	/**
 	 * If you use optional arguments, override this method
 	 *
 	 * @param parameters The arguments you defined in the constructor and their values as supplied by the user
 	 * @param arguments  Tailing arguments not asked for in the command definition
 	 * @return Message to show in the console after the command completes
 	 */
 	public String OnAsyncExecute(HashMap<String, String> parameters, String[] arguments)
 	{
 		return OnAsyncExecute(parameters);
 	}
 
 	/**
 	 * If you use optional arguments, you still need to override this but you can leave it empty.
 	 *
 	 * @param parameters The arguments you defined in the constructor and their values as supplied by the user
 	 * @return Message to show in the console after the command completes
 	 */
 	public abstract String OnAsyncExecute(HashMap<String, String> parameters);
 }
