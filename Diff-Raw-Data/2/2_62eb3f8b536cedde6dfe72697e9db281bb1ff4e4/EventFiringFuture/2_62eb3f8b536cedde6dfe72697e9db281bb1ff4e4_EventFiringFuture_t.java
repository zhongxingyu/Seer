 package com.edinarobotics.scouting.definitions.event.helpers;
 
 import java.util.Set;
 import java.util.concurrent.CancellationException;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.TimeoutException;
 import com.edinarobotics.scouting.definitions.event.Cancellable;
 import com.edinarobotics.scouting.definitions.event.Event;
 import com.edinarobotics.scouting.definitions.event.Future;
 import com.edinarobotics.scouting.definitions.event.Result;
 
 /**
  * This class allows asynchronous access to the result of firing an {@link Event}
  * using {@link com.edinarobotics.scouting.definitions.event.helpers.EventFiringManager EventFiringManager}.
  * It provides access to the Event that was fired as well as to any exceptions that were thrown
  * during this process.
  */
 public class EventFiringFuture extends Future<Event>{
 	private EventFiringTask eventTask;
 	private boolean topCancel = false;
 	
 	/**
 	 * Constructs a new EventFiringFuture for an Event
 	 * with the given {@code eventId} and that was
 	 * fired through the given {@link EventFiringTask},
 	 * {@code eventTask}.
 	 * @param eventId The String ID of the fired Event.
 	 * @param eventTask The EventFiringTask managing
 	 * the event firing process for the given Event.
 	 */
 	public EventFiringFuture(String eventId, EventFiringTask eventTask){
 		super(eventId);
 		this.eventTask = eventTask;
 	}
 
 	/**
 	 * Waits for the completion of the event firing process
 	 * and then returns the Event that completed the process.
 	 * This method blocks until the event firing process is
 	 * complete.
 	 * @return The Event object that was submitted to
 	 * the event firing process.
 	 * @throws CancellationException If the event firing process
 	 * (not the Event itself) was cancelled.
 	 * @throws ExecutionException If an Exception occurred during
 	 * the event firing process and outside of the event handling
 	 * methods.
 	 * @throws InterruptedException If the current thread was interrupted
 	 * while waiting for the completion of the event firing process.
 	 */
 	public Event get() throws CancellationException, ExecutionException, InterruptedException {
 		Event toReturn = eventTask.get();
 		if(toReturn instanceof Cancellable && topCancel){
 			((Cancellable)toReturn).setCancelled(true);
 		}
 		return toReturn;
 	}
 	
 	/**
 	 * Waits for the completion of the event firing process
 	 * and then returns the Event that completed the process
 	 * or waits until a timeout occurs.
 	 * This method will block until results of the event
 	 * firing process are available or for the length of
 	 * the given timeout, whichever is shorter.
 	 * @param timeout The maximum time to wait.
 	 * @param unit The {@link TimeUnit} representing the unit of time to wait.
 	 * @return The Event object that was submitted to
 	 * the event firing process.
 	 * @throws TimeoutException If the specified wait time was exhausted.
 	 * @throws CancellationException If the event firing process (not the
 	 * Event itself) was cancelled.
 	 * @throws ExecutionException If an exception occurred during the
 	 * event firing process and outside of the event handling methods.
 	 * @throws InterruptedException If the current thread was interrupted
 	 * while waiting for the completion of the event firing process.
 	 */
 	public Event get(long timeout, TimeUnit unit) throws CancellationException, TimeoutException, ExecutionException, InterruptedException{
 		return super.get(timeout, unit);
 	}
 
 	/**
 	 * This method cancels the Event returned from the
 	 * event firing process. This cancellation request,
 	 * if possible for the Event, will cancel the event
 	 * with the highest priority, overriding all event
 	 * handling methods.
 	 * @param mayInterruptIfRunning Has no effect on
 	 * the cancellation of the Event.
 	 * @return {@code true}
 	 * @see Future#cancel(boolean)
 	 * @see Cancellable#setCancelled(boolean)
 	 * @see #cancel()
 	 */
 	public boolean cancel(boolean mayInterruptIfRunning) {
 		cancel();
 		return true;
 	}
 
 	/**
 	 * This method cancels the Event returned from the
 	 * event firing process. This cancellation request,
 	 * if possible for the Event, will cancel the event
 	 * with the highest priority, overriding all event
 	 * handling methods.
 	 * @see Future#cancel(boolean)
 	 * @see Cancellable#setCancelled(boolean)
 	 * @see #cancel(boolean)
 	 */
 	public void cancel(){
 		topCancel = true;
 	}
 
 	/**
 	 * Indicates whether the event firing process
 	 * for the given Event is complete. If this
 	 * method returns {@code true}, the {@link #get()}
 	 * method will return immediately.
 	 * @return {@code true} if the event firing process
 	 * is complete, {@code false} otherwise.
 	 */
 	public boolean isDone() {
 		return eventTask.isDone();
 	}
 
 	/**
 	 * Indicates whether the Event was cancelled as a part of the
 	 * firing process.
 	 * This method will not block, but will return {@code false} by default
 	 * if the event firing process is not complete. A cancellable event is considered to
 	 * be cancelled if the event firing process is complete and if its isCancelled method returns
 	 * {@code true}, <em>or</em> the {@link #cancel} or {@link #cancel(boolean)} have been called.
 	 * If an exception occurs while calling the {@link #get()} method to obtain the event, only
 	 * the information from {@link #cancel()} and {@link #cancel(boolean)} will be considered.
 	 * Only Event objects that implement {@link Cancellable} may be cancelled. If an Event does
 	 * not implement Cancellable, this method will return {@code false}.
 	 * @return {@code true} if the Event was cancelled as a part of the event firing process,
 	 * {@code false} otherwise.
 	 */
 	public boolean isCancelled() {
 		try{
			if(((isDone() && (get() instanceof Cancellable) && ((Cancellable)this.get()).isCancelled())) || topCancel){
 				return true;
 			}
 		}catch(Exception e){
 			return topCancel;
 		}
 		return false;
 	}
 	
 	/**
 	 * Indicates whether or not an Event handling method threw
 	 * an Exception while processing the given Event. If this
 	 * method returns {@code true}, exceptions are available
 	 * from the {@link #getExceptions()} method. This method
 	 * does not block, results are available (and may change)
 	 * throughout the event firing process.
 	 * @return {@code true} if an event handling method has thrown
 	 * an Exception.
 	 */
 	public boolean hasError(){
 		return eventTask.getExceptions().size() > 0;
 	}
 	
 	/**
 	 * Provides access to any exceptions thrown by
 	 * Event handling methods during the event firing
 	 * process. This method returns an unmodifiable Set
 	 * of the Exception objects thrown by the event handling
 	 * methods.
 	 * @return A Set of the Exceptions thrown by event handling
 	 * methods during the event firing process.
 	 * @see EventFiringTask#getExceptions()
 	 */
 	public Set<Exception> getExceptions(){
 		return eventTask.getExceptions();
 	}
 
 	/**
 	 * Indicates the result of the event firing process.
 	 * If the event firing process is not yet complete,
 	 * this method returns {@code null}.<br/>
 	 * A value of {@link Result#CANCELLED} indicates that
 	 * the {@link #isCancelled()} method will return {@code true}.<br/>
 	 * A value of {@link Result#ERROR} indicates that the
 	 * {@link #hasError()} method will return {@code true}.<br/>
 	 * A value of {@link Result#SUCCESS} indicates that the event was not
 	 * cancelled and that the event firing process completed
 	 * without error.<br/>
 	 * These values are selected in the same order as they are
 	 * listed above.
 	 * @return A Result value as defined above.
 	 */
 	public Result getResult() {
 		if(!isDone()){
 			return null;
 		}
 		if(isCancelled()){
 			return Result.CANCELLED;
 		}
 		if(hasError()){
 			return Result.ERROR;
 		}
 		return Result.SUCCESS;
 	}
 }
