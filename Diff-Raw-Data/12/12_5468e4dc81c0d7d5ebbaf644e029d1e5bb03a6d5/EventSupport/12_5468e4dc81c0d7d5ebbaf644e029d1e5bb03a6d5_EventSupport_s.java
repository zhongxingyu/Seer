 package org.wm.timebox.app.di;
 
 import java.util.ArrayList;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Set;
 
 /**
  * Provides means to register observers for a certain event and to fire events.
  * 
  * @author Andi
  * 
  * @param <T>
  */
 public class EventSupport<T> {
 
 	private Set<Observer<T>> listeners = new LinkedHashSet<Observer<T>>();
 
 	public EventSupport() {
 		super();
 	}
 
 	public void addListener(Observer<T> aListener) {
 		listeners.add(aListener);
 	}
 
 	public void fire(T anEvent) {
		List<Observer<T>> tempListeners = new ArrayList<>(listeners);
 		for (Observer<T> tempListener : tempListeners) {
 			tempListener.notify(anEvent);
 		}
 	}
 }
