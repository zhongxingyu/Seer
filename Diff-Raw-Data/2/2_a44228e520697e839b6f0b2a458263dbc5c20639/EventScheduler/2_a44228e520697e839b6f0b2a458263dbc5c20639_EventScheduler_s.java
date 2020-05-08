 /* JEEventScheduler - Decompiled by JODE
  * Visit http://jode.sourceforge.net/
  */
 package ddg.kernel;
 
 import java.util.HashSet;
 import java.util.PriorityQueue;
 import java.util.Set;
 import java.util.Vector;
 
 /**
  * TODO make doc
  * 
  * @author thiago - thiago@lsd.ufcg.edu.br
  */
 public final class EventScheduler {
 
 	private Time now = new Time(0L);
 	private PriorityQueue<Event> eventsQueue = new PriorityQueue<Event>();
 	private Vector<EventHandler> handlerList;
 	private Set<EventSchedulerObserver> observers = new HashSet<EventSchedulerObserver>();
 	private Boolean isActive;
 	private Time theEmulationEnd;
 
 	/**
      * 
      */
 	public EventScheduler() {
 		this(null);
 	}
 
 	/**
 	 * @param emulationEnd
 	 */
 	public EventScheduler(Time emulationEnd) {
 
 		handlerList = new Vector<EventHandler>();
 		handlerList.setSize(100);
 		handlerList.clear();
 		isActive = Boolean.valueOf(false);
 		theEmulationEnd = emulationEnd;
 	}
 
 	/**
 	 * @param aNewEvent
 	 */
 	public void schedule(Event aNewEvent) {
 
 		Time anEventTime = aNewEvent.getScheduledTime();
 
 		if (anEventTime.isEarlierThan(now())) {
 			throw new RuntimeException("ERROR: emulation time(" + now()
 					+ ") already ahead of event time(" + anEventTime
 					+ "). Event is outdated and will not be processed.");
 		}
 
 		eventsQueue.add(aNewEvent);
 	}
 
 	/**
 	 * @param anObsoleteEvent
 	 */
 	public void cancelEvent(Event anObsoleteEvent) {
 
 		if (anObsoleteEvent == null) {
 			throw new NullPointerException();
 		}
 		eventsQueue.remove(anObsoleteEvent);
 	}
 
 	/**
 	 * @param aNewEventHandler
 	 * @return
 	 */
 	public EventScheduler registerHandler(EventHandler aNewEventHandler) {
 
 		Integer aHandlerId = aNewEventHandler.getHandlerId();
 
 		if(handlerList.size() < aHandlerId.intValue() - 1) {
 			handlerList.setSize(aHandlerId.intValue() - 1);
 		}
 
 		if(handlerList.size() > aHandlerId.intValue() - 1) {
 			handlerList.removeElementAt(aHandlerId.intValue() - 1);
 		}
 
 		handlerList
 				.insertElementAt(aNewEventHandler, aHandlerId.intValue() - 1);
 		return this;
 	}
 
 	/**
      * 
      */
 	public void start() {
 
 		if (!eventsQueue.isEmpty()) {
 			schedule();
 		}
 	}
 
 	/**
      * 
      */
 	private void schedule() {
 
 		isActive = Boolean.valueOf(true);
 
 		while (!eventsQueue.isEmpty() & isActive.booleanValue()
 				& isEarlierThanEmulationEnd(now())) {
 
			Event aNextEvent = eventsQueue.peek();
 
 			if (aNextEvent != null) {
 
 				Time anEventTime = aNextEvent.getScheduledTime();
 
 				if (anEventTime.isEarlierThan(now())) {
 					throw new RuntimeException("ERROR: emulation time(" + now()
 							+ ") " + "already ahead of event time("
 							+ anEventTime
 							+ "). Event is outdated and will not be processed.");
 				}
 
 				if (isEarlierThanEmulationEnd(anEventTime)) {
 					now = anEventTime;
 					processEvent(aNextEvent);
 					notifyEventProcessed();
 				} else {
 					now = theEmulationEnd;
 				}
 			}
 		}
 
 		isActive = Boolean.valueOf(false);
 	}
 
 	private void notifyEventProcessed() {
 		for(EventSchedulerObserver observer : observers) {
 			observer.eventProcessed();
 		}
 	}
 
 	private boolean isEarlierThanEmulationEnd(Time time) {
 		return (theEmulationEnd != null) ? time.isEarlierThan(theEmulationEnd)
 				: true;
 	}
 
 	/**
 	 * @param aNextEvent
 	 */
 	private void processEvent(Event aNextEvent) {
 
 		Integer aTargetHandlerId = aNextEvent.getTheTargetHandlerId();
 
 		if (handlerList.elementAt(aTargetHandlerId.intValue() - 1) == null) {
 			throw new RuntimeException("ERROR: no Handler at vector position "
 					+ (aTargetHandlerId.intValue() - 1)
 					+ ". Something's wrong here, dude.");
 		}
 
 		handlerList.elementAt(aTargetHandlerId.intValue() - 1).handleEvent(
 				aNextEvent);
 	}
 
 	/**
 	 * @return
 	 */
 	public Time now() {
 		return now;
 	}
 
 	public void registerObserver(EventSchedulerObserver eventInjector) {
 		observers.add(eventInjector);
 	}
 }
