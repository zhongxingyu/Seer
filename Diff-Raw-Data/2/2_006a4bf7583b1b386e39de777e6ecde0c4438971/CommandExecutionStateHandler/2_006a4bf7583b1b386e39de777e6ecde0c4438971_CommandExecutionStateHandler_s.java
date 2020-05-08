 package sk.stuba.fiit.perconik.eclipse.core.commands;
 
 import static sk.stuba.fiit.perconik.eclipse.core.commands.CommandExecutionState.WAITING;
 import static sk.stuba.fiit.perconik.utilities.MorePreconditions.checkNotNullAndNotEmpty;
 import java.util.concurrent.atomic.AtomicReference;
 
 public final class CommandExecutionStateHandler
 {
 	private final String identifier;
 	
 	final AtomicReference<CommandExecutionState> state;
 	
 	CommandExecutionStateHandler(final String identifier)
 	{
 		this.identifier = checkNotNullAndNotEmpty(identifier);
 		this.state      = new AtomicReference<>(WAITING);
 	}
 	
	public CommandExecutionStateHandler of(final String identifier)
 	{
 		return new CommandExecutionStateHandler(identifier);
 	}
 	
 	public final void transit(final String identifier, final CommandExecutionState state)
 	{
 		if (identifier.equals(this.identifier))
 		{
 			this.state.set(state);
 		}
 	}
 
 	public final String getIdentifier()
 	{
 		return this.identifier;
 	}
 }
