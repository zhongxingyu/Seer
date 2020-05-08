 package core;
 
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.regex.Pattern;
 
 public class EventManager implements EventManagerInterface{
 
 	private Map<EventCondition, String> mapEventConditionToEvent;
 	private static EventManager myEventManager;
 	private EventQueue myEventQueue;
 	private long elapsedTime;
 
 	public EventManager() {
 		myEventManager = this;
 		myEventQueue = new EventQueue();
 		mapEventConditionToEvent = new HashMap<EventCondition, String>();
 	}
 
 	public void addEvent(Event event) {
 		myEventQueue.addEvent(event);
 	}
 
 	public void addEventCondition(EventCondition cond, String eventName) {
 		mapEventConditionToEvent.put(cond, eventName);
 	}
 
 	public void removeEventCondition(EventCondition condition) {
 		mapEventConditionToEvent.remove(condition);
 	}
 
 	public void registerEventListener(String eventName, EventListener listener) {
 		myEventQueue.registerEventListener(eventName, listener);
 	}
 
 	public void unregisterEventListener(String eventName, EventListener listener) {
 		myEventQueue.unregisterEventListener(eventName,listener);
 	}
 
 	public void sendEvent(String eventName) {
 //		for (String event : myEventQueue.getEventListenerMap().keySet()) {
 //			if (event.equals(eventName)) {
 //				for (EventListener listener : myEventQueue.getEventListenerMap()
 //						.get(event)) {
 //					listener.actionPerformed(event);
 //				}
 //			}
 //		}
 		sendEvent(eventName, null);
 	}
 
 	public void sendEvent(final String eventName, final Object obj){
 		final ArrayList<EventListener> listeners = myEventQueue.getEventListeners(eventName);
 		
		System.out.println(myEventQueue.getQueues().get(0).size() );
 		
 		  addEvent(new Event()
           {
                   @Override
                   public void run()
                  {                	  
                	  
                 	  	for(EventListener l : listeners){
                 	  		l.actionPerformed(eventName);
                 	  	}
                   }
 
                   @Override
                   public String toString()
                   {
                           return eventName;
                   }
 
           });
 		
 		
 	}
 	
 	public long getElapsedTime(){
 		return elapsedTime;
 	}
 	
 	public static EventManager getEventManager() {
 		if (myEventManager == null) {
 			myEventManager = new EventManager();
 		}
 		return myEventManager;
 	}
 
 	public void update(long timeElapsed) {
 		elapsedTime = timeElapsed;
 		while(myEventQueue.hasEvents()){
             Event event = myEventQueue.removeEvent();
             event.run();
 		}
 		
 		
 	}
 
 }	
 
 
