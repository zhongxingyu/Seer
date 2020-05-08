 package com.edinarobotics.scouting.definitions.event;
 
 import java.util.concurrent.Callable;
 import java.util.concurrent.CancellationException;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.TimeoutException;
 
 /**
  * Represents the result of a future computation based on events.
  * This class is necessary to enable events to be used in these computations
  * without blocking (for example to obtain event ID values before the process
  * is completed).
  * @param <T> The type that is returned by the result of the
  * event-based computation.
  */
 public abstract class Future<T> implements java.util.concurrent.Future<T>{
 	private final String eventId;
 	
 	/**
 	 * Constructs a new {@link Future} object that is based on the
 	 * computation related to {@code event}.
 	 * @param eventId The {@link String} id that any {@link Event} objects
 	 * fired by this action will have.
 	 */
 	public Future(String eventId){
 		this.eventId = eventId;
 	}
 	
 	/**
 	 * Waits for the completion of the event-based computation and then returns
 	 * the result of the computation.
 	 * @return The result of the computation.
 	 * @throws TimeoutException If the wait timed out
 	 * @throws CancellationException If the computation was cancelled (by a
 	 * plugin or otherwise)
 	 * @throws ExecutionException If the computation threw an exception
 	 * @throws InterruptedException If the current thread was interrupted while waiting
 	 */
 	public abstract T get() throws CancellationException, ExecutionException, InterruptedException;
 	
 	/**
 	 * Waits for the result of the event-based computation or the length of a
 	 * given timeout (whichever comes sooner) and then returns the result of the
 	 * computation.
 	 * @param timeout The maximum time to wait.
 	 * @param unit The {@link TimeUnit} representing the unit of time to wait.
 	 * @return The result of the computation.
 	 * @throws TimeoutException If the wait timed out
 	 * @throws CancellationException If the computation was cancelled (by a
 	 * plugin or otherwise)
 	 * @throws ExecutionException If the computation threw an exception
 	 * @throws InterruptedException If the current thread was interrupted while waiting
 	 */
 	public T get(long timeout, TimeUnit unit) throws TimeoutException, ExecutionException, InterruptedException, CancellationException{
 		//Create new ExecutorService to run our timeout enabled call
 		ExecutorService executor = Executors.newSingleThreadExecutor();
 		
 		//Create a new nested class to perform the function call
 		class GetTask implements Callable<T>{
 			private Future<T> future;
 			
 			public GetTask(Future<T> future){
 				this.future = future;
 			}
 
 			public T call() throws Exception {
 				return future.get();
 			}
 		}
 		
 		//Give the nested class to the executor
 		java.util.concurrent.Future<T> future = executor.submit(new GetTask(this));
 		//Return the result and enable the timeout
 		return future.get(timeout, unit);
 	}
 	
 	/**
 	 * Calling this method attempts to cancel the computation.
 	 * <br/>Please note that this method's parameter <em>does not</em>
 	 * set the cancellation state of the event (this is in contrast
 	 * to {@link Cancellable#setCancelled(boolean)}).<br/>
 	 * If {@code mayInterruptIfRunning} is {@code false} the event
 	 * will be cancelled with the highest priority (if the computation has not
 	 * yet run, the event <em>will be</em> cancelled. Nothing will happen if the
 	 * computation has already begun.<br/>
 	 * If {@code mayInterruptIfRunning} is {@code true} the event will
 	 * be cancelled in the same way as described above, <em>but</em> additional
 	 * attempts may occur if defined by the implementor.
	 * @param mayInterruptIfRunning chooses the priority and type of attempts that
 	 * may be used to cancel the computation as defined above or in the implementation.
 	 */
 	public abstract boolean cancel(boolean mayInterruptIfRunning);
 	
 	/**
 	 * Returns whether or not this task is complete. This method does not indicate
 	 * the state in which the computation completed, only that the computation
 	 * completed.
 	 * @return {@code true} if the computation has completed, was cancelled or
 	 * if an error occurred, {@code false} otherwise.
 	 */
 	public abstract boolean isDone();
 	
 	/**
 	 * Returns whether or not this task or its corresponding event was cancelled
 	 * (by a plugin or otherwise).
 	 * @return {@code true} if the event was cancelled, {@code false} otherwise.
 	 */
 	public abstract boolean isCancelled();
 	
 	/**
 	 * Indicates whether this task resulted in an error.
 	 * If this method returns {@code true}, expect
 	 * exceptions when calling {@link #get()}.
 	 * @return {@code true} if the task is finished
 	 * and if it resulted in an error, {@code false}
 	 * otherwise.
 	 */
 	public abstract boolean hasError();
 	
 	/**
 	 * Returns the {@link Result} value representing the result
 	 * of this computation and its event.
 	 * @return The {@link Result} value representing the result
 	 * of the computation as described in the {@link Result} enum.
 	 * If the computation is not finished, returns {@code null}.
 	 */
 	public abstract Result getResult();
 	
 	/**
 	 * Returns the {@link String} ID value of the {@link Event} objects to which
 	 * this {@link Future} object is linked.
 	 * This value matches the value returned by{@link Event#getId()}.
 	 * @return The ID value of the {@link Event} objects to which this
 	 * {@link Future} is linked.
 	 * @see Event#getId()
 	 */
 	public String getId(){
 		return this.eventId;
 	}
 }
