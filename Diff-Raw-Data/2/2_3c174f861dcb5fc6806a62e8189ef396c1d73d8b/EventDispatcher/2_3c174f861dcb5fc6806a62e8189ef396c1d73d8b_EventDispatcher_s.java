 package pizzaProgram.events;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 /**
  * The EventDispatcher takes in events and lets objects register for certain event types. 
  * When an event is dispatched, it will notify all listeners listening to that event.
  * @author Bart
  *
  */
 public class EventDispatcher {
 	/**
 	 * A hash map containing lists of event listeners. 
 	 */
 	private final HashMap<String, ArrayList<EventHandler>> listeners = new HashMap<String, ArrayList<EventHandler>>();
 	
 	/**
 	 * Notifies (e.g. calls all listener functions) all event listeners about the event passed into the function
 	 * @param event The event to be dispatched
 	 */
 	public void dispatchEvent(Event<Object> event)
 	{
 		if(!eventTypeExists(event.eventType))
 		{
 			System.out.println("WARNING: dispatch attempted of event with event type '"+event.eventType+"', which has no listeners");
 			return;
 		}else{
 			System.out.println("dispatched event: "+ event.eventType);
 		}
 		
 		ArrayList<EventHandler> eventHandlersList = this.listeners.get(event.eventType);
 		for(EventHandler i : eventHandlersList)
 		{
			if(event.isPropagating())
 			{
 				break;
 			}
 			i.handleEvent(event);
 		}
 	}
 	/**
 	 * adds an event listener to the specified event type. 
 	 * When the event is dispatched, the event dispatcher will call the handleEvent() function if the event has not stopped propagation.
 	 * @param listenerModule The object that implements the EventHandler interface, that will be notified upon occurrence of the event
 	 * @param eventType The string representing the event type. A constand from EventType should be used for this.
 	 */
 	public void addEventListener(EventHandler listenerModule, String eventType)
 	{
 		this.addEventTypeIfNotExistent(eventType);
 		ArrayList<EventHandler> listenerList = this.listeners.get(eventType);
 		listenerList.add(listenerModule);
 
 	}
 	
 	/**
 	 * Creates a new entry in the HashMap holding the listener lists if the inserted event type does not yet exist
 	 * @param eventType The event type to be added upon non-existence
 	 */
 	private void addEventTypeIfNotExistent(String eventType)
 	{
 		if(!eventTypeExists(eventType))
 		{
 			this.listeners.put(eventType, new ArrayList<EventHandler>());
 		}
 	}
 	
 	/**
 	 * A function to check whether an event type has been added to the list of known events in the hash map
 	 * @param eventType The name of the event to check existence of
 	 * @return returns true if the event type exists. false otherwise
 	 */
 	private boolean eventTypeExists(String eventType)
 	{
 		return this.listeners.containsKey(eventType);
 	}
 }
