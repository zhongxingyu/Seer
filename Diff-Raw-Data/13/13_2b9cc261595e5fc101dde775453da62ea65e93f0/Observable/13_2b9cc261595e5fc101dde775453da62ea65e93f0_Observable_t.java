 package org.logparser;
 
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Set;
 
 import com.google.common.base.Preconditions;
 
 /**
  * Generic implementation of the {@link IObservable} interface.
  * 
  * @author jorge.decastro
  * 
  */
 public class Observable<T> implements IObservable<T> {
 	private final Set<IObserver<T>> subscribers;
 
 	public Observable() {
 		subscribers = new HashSet<IObserver<T>>();
 	}
 
 	public void attach(final IObserver<T>... observers) {
 		Preconditions.checkNotNull(observers, "'observers[]' argument cannot be null.");
 		for (IObserver<T> subscriber : observers) {
 			Preconditions.checkNotNull(subscriber, "'observer' element in observers[] cannot be null.");
 		}
 		subscribers.addAll(Arrays.asList(observers));
 	}
 
 	public void detach(final IObserver<T>... observers) {
 		Preconditions.checkNotNull(observers, "'observers[]' argument cannot be null.");
 		for (IObserver<T> subscriber : observers) {
 			Preconditions.checkNotNull(subscriber, "'observer' element in observers[] cannot be null.");
 		}
 		subscribers.removeAll(Arrays.asList(observers));
 	}
 
 	public void notifyObservers(final T event) {
 		for (IObserver<T> subscriber : subscribers) {
			// detach subscriber if exception is thrown?
 			subscriber.consume(event);
 		}
 	}
 
 	public Set<IObserver<T>> getSubscribers() {
 		return Collections.unmodifiableSet(subscribers);
 	}
 }
