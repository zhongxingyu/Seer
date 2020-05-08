 package edu.teco.dnd.util;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.concurrent.atomic.AtomicInteger;
 
 /**
  * Combines multiple FutureNotifiers. If all FutureNotifiers succeed this will succeed as well. If at least one fails this
  * will also be set to be a failure and the cause will be set to the cause of the failed FutureNotifier. If multiple FutureNotifiers
  * fail the cause is set to one of the causes of the failed FutureNotifiers without any guarantee as to which one.
  * 
  * @author Philipp Adolf
  *
  * @param <T> the type of the result of the FutureNotifiers that should be joined
  */
 public class JoinedFutureNotifier<T> extends DefaultFutureNotifier<Collection<T>> implements FutureListener<FutureNotifier<T>>{
 	/**
 	 * The FutureNotifiers that are combined.
 	 */
 	private final Collection<FutureNotifier<? extends T>> futures;
 	
 	/**
 	 * The number of FutureNotifiers that have not yet finished. Only relevant in the success case.
 	 */
 	private final AtomicInteger unfinished;
 	
 	/**
 	 * Initializes a new JoinedFutureNotifier.
 	 * 
 	 * @param futures the FutureNotifiers that should be joined
 	 */
 	public JoinedFutureNotifier(final Collection<FutureNotifier<? extends T>> futures) {
 		this.futures = Collections.unmodifiableSet(new HashSet<FutureNotifier<? extends T>>(futures));
		final int unfinished = this.futures.size();
		this.unfinished = new AtomicInteger(unfinished);
		if (unfinished <= 0) {
			setSuccess(new ArrayList<T>());
		} else {
			for (FutureNotifier<? extends T> future : this.futures) {
				future.addListener(this);
			}
 		}
 	}
 
 	@Override
 	public synchronized void operationComplete(final FutureNotifier<T> future) {
 		if (!isDone()) {
 			if (future.isSuccess()) {
 				if (unfinished.decrementAndGet() <= 0) {
 					final Collection<T> result = new ArrayList<T>();
 					for (final FutureNotifier<? extends T> notifier : this.futures) {
 						result.add(notifier.getNow());
 					}
 					setSuccess(result);
 				}
 			} else {
 				setFailure(future.cause());
 			}
 		}
 	}
 }
