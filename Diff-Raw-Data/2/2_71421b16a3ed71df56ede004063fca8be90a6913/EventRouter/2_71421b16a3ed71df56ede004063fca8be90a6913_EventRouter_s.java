 package no.runsafe.framework.event.listener;
 
 import no.runsafe.framework.event.IAsyncEvent;
 import no.runsafe.framework.event.IRunsafeEvent;
 import no.runsafe.framework.output.IOutput;
 import no.runsafe.framework.timer.IScheduler;
 import org.apache.commons.lang.exception.ExceptionUtils;
 import org.bukkit.ChatColor;
 import org.bukkit.event.Cancellable;
 import org.bukkit.event.Event;
 import org.bukkit.event.Listener;
 
 import java.util.logging.Level;
 
 public abstract class EventRouter<Wrapper extends IRunsafeEvent, EventType extends Event> implements Listener
 {
 	public EventRouter(IOutput output, IScheduler scheduler, Wrapper handler)
 	{
 		this.console = output;
 		this.scheduler = scheduler;
 		this.handler = handler;
 		this.isAsync = (handler instanceof IAsyncEvent);
 	}
 
 	// Sadly, this method must be added to all implementing classes, but all you have to do, is call this one.
 	// Don't forget to add @EventHandler - Java does not support annotations on base classes :(
 	public void AcceptEvent(EventType event)
 	{
 		if (isAsync)
 			InvokeAsync(event);
 		else
 			Invoke(event);
 	}
 
 	/**
 	 * This method is called to pass the event on to the plugin expecting it.
 	 * If the subscriber doesn't implement IAsyncEvent and the event is Cancellable,
 	 * returning false from here will cancel the event.
 	 *
 	 * @param event The raw event object from bukkit
 	 * @return false to cancel a cancellable event
 	 */
 	public abstract boolean OnEvent(EventType event);
 
 	private void InvokeAsync(final EventType event)
 	{
 		scheduler.startAsyncTask(
 			new Runnable()
 			{
 				@Override
 				public void run()
 				{
 					Invoke(event);
 				}
 			},
 			0
 		);
 	}
 
 	private void Invoke(EventType event)
 	{
 		boolean result;
 		try
 		{
 			result = OnEvent(event);
 		}
 		catch (Exception e)
 		{
 			console.outputColoredToConsole(
 				String.format(
					"Database failure: %s%s%s\n%s",
 					ChatColor.RED,
 					ExceptionUtils.getMessage(e),
 					ChatColor.RESET,
 					ExceptionUtils.getStackTrace(e)
 				),
 				Level.SEVERE
 			);
 			return;
 		}
 		if (!result && event instanceof Cancellable)
 			((Cancellable) event).setCancelled(true);
 	}
 
 	protected final IScheduler scheduler;
 	protected final Wrapper handler;
 	private final boolean isAsync;
 	private final IOutput console;
 }
