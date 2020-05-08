 package core;
 
 import java.util.HashMap;
 import java.util.Map;
 
 public class EventManager implements EventManagerInterface{
 
 	private Map<EventCondition, String> mapEventConditionToEvent;
 	private static EventManager myEventManager;
 	private EventQueue myEventQueue;
 
 	public EventManager() {
 		myEventManager = this;
		myEventQueue = new EventQueue();
 		mapEventConditionToEvent = new HashMap<EventCondition, String>();
 	}
 
 	public void addEvent(String eventName) {
 		myEventQueue.addEvent(eventName);
 	}
 
 	public void addEventCondition(EventCondition cond, String s) {
 		mapEventConditionToEvent.put(cond, s);
 	}
 
 	public void removeEventCondition(EventCondition condition) {
 		mapEventConditionToEvent.remove(condition);
 	}
 
 	public void registerEventListener(String e, EventListener listener) {
 		myEventQueue.registerEventListener(e, listener);
 	}
 
 	public void unregisterEventListener(String e, EventListener listener) {
 		myEventQueue.unregisterEventListener(e,listener);
 	}
 
 	public void sendEvent(String e) {
 		for (String event : myEventQueue.getEventListenerMap().keySet()) {
 			if (event.equals(e)) {
 				for (EventListener listener : myEventQueue.getEventListenerMap()
 						.get(event)) {
 					listener.actionPerformed(event);
 				}
 			}
 		}
 	}
 
 	public static EventManager getEventManager() {
 		if (myEventManager == null) {
 			myEventManager = new EventManager();
 		}
 		return myEventManager;
 	}
 
 	public void update(long elapsedTime) {
 
 		while(myEventQueue.hasEvents()){
 			String eventName = myEventQueue.removeEvent();
 			for (EventListener listener  : myEventQueue.getEventListeners(eventName)){
 				listener.actionPerformed(elapsedTime);
 			}
 		}
 	}
 
 }	
 
 
