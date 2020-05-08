 package net.frontlinesms.events.impl;
 
 import java.util.concurrent.CopyOnWriteArrayList;
 
 import net.frontlinesms.events.EventBus;
 import net.frontlinesms.events.EventObserver;
 import net.frontlinesms.events.FrontlineEventNotification;
 
 /**
  * Implementation of the event bus.
  * This class uses CopyOnWriteArrayList, so it
  * is thread safe.
 * @author Dieterich Lawson
  */
 public class FrontlineEventBus implements EventBus{
 	
 	/** the observers that receive event notifications */
 	private CopyOnWriteArrayList<EventObserver> observers;
 	
 	public FrontlineEventBus(){
 		observers = new CopyOnWriteArrayList<EventObserver>();
 	}
 	 
	/* (non-Javadoc)
	 * @see net.frontlinesms.events.EventBus#registerObserver(net.frontlinesms.events.EventObserver)
	 */
 	public void registerObserver(EventObserver observer){
		if(!observers.contains(observer))
			observers.add(observer);
 	}
 	
 
	/* (non-Javadoc)
	 * @see net.frontlinesms.events.EventBus#unregisterObserver(net.frontlinesms.events.EventObserver)
	 */
 	public void unregisterObserver(EventObserver observer){
		if(!observers.contains(observer))
			observers.remove(observer);
 	}
 
	/* (non-Javadoc)
	 * @see net.frontlinesms.events.EventBus#triggerEvent(net.frontlinesms.events.FrontlineEvent)
	 */
 	public void notifyObservers(FrontlineEventNotification event){
 		for(EventObserver observer: observers){
 			observer.notify(event);
 		}
 	}
 }
