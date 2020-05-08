 package no.runsafe.framework.command;
 
 import no.runsafe.framework.server.player.RunsafePlayer;
 import no.runsafe.framework.timer.IScheduler;
 import org.apache.commons.lang.StringUtils;
 
 import java.util.Collection;
 import java.util.logging.Level;
 
 public abstract class RunsafeAsyncCommand extends RunsafeCommand
 {
 	public RunsafeAsyncCommand(String name, IScheduler scheduler, String... params)
 	{
 		super(name, null, params);
 		this.scheduler = scheduler;
 	}
 
 	@Override
 	public Collection<ICommand> getSubCommands()
 	{
 		return null;
 	}
 
 	@Override
 	public void addSubCommand(ICommand command)
 	{
 		throw new UnsupportedOperationException("An asynchronous command cannot have subcommands!");
 	}
 
 	@Override
	public boolean Execute(final RunsafePlayer player, final String[] args)
 	{
		Console.finer(String.format("Execute: %s, %s", commandName, StringUtils.join(args, ", ")));

 		subArgOffset = 0;
 		if (args.length < params.size())
 		{
 			Console.finest(String.format("Missing params (%d < %d)", args.length, params.size()));
			Console.write(getCommandUsage());
 			return true;
 		}
 		captureArgs(args);
 		Console.finest("Executing command..");
 		scheduler.startAsyncTask(new Runnable()
 		{
 			@Override
 			public void run()
 			{
 				final String output = OnExecute(player, args);
 				scheduler.startSyncTask(new Runnable()
 				{
 					@Override
 					public void run()
 					{
 						OnCommandCompletion(player, output);
 					}
 				}, 0L);
 			}
 		}, 0L);
 		return true;
 	}
 
 	@Override
 	public boolean Execute(final String[] args)
 	{
		Console.finer(String.format("Execute: %s, %s", commandName, StringUtils.join(args, ", ")));
 
 		subArgOffset = 0;
 		if (args.length < params.size())
 		{
 			Console.finest(String.format("Missing params (%d < %d)", args.length, params.size()));
			Console.write(getCommandUsage());
 			return true;
 		}
 		captureArgs(args);
 		Console.finest("Executing command..");
 		scheduler.startAsyncTask(new Runnable()
 		{
 			@Override
 			public void run()
 			{
 				final String output = OnExecute(null, args);
 				scheduler.startSyncTask(new Runnable()
 				{
 					@Override
 					public void run()
 					{
 						OnCommandCompletion(null, output);
 					}
 				}, 0L);
 			}
 		}, 0L);
 		return true;
 	}
 
 	@Override
 	public abstract String OnExecute(RunsafePlayer executor, String[] args);
 
 	public void OnCommandCompletion(RunsafePlayer player, String message)
 	{
 		if (message == null)
 			return;
 
		if (player == null)
			Console.outputColoredToConsole(message, Level.INFO);

 		if (player != null)
 			player.sendMessage(message);
 	}
 
 	@Override
 	protected ICommand getSubCommand(String name)
 	{
 		return null;
 	}
 
 	private final IScheduler scheduler;
 }
