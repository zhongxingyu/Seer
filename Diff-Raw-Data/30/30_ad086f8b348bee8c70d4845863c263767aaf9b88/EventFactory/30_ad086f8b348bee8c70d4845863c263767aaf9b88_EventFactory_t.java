 package spaceshooters.api;
 
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.concurrent.LinkedBlockingQueue;
 
 import spaceshooters.api.events.Event;
 import spaceshooters.util.Logger;
 
 public class EventFactory {
 	
 	private static EventFactory factory = new EventFactory();
 	private ArrayList<EventReceiver> listeners = new ArrayList<>();
 	private final LinkedBlockingQueue<Event> queue = new LinkedBlockingQueue<>();
 	
 	private EventFactory() {
 	}
 	
 	public void registerListener(Object o) {
 		for (Method m : o.getClass().getMethods()) {
 			if (m.isAnnotationPresent(EventHandler.class) && m.getParameterTypes().length == 1) {
 				listeners.add(new EventReceiver(o, m));
 			}
 		}
 		Logger.info("I registered " + listeners.size() + " listeners!");
 	}
 	
 	public void postEvent(Event event) {
		if (listeners.size() != 0) {
			try {
				queue.put(event);
			} catch (InterruptedException e) {
				Logger.error(e, "I cannot put a new element to the event queue!");
			}
			
			while (!queue.isEmpty()) {
				for (EventReceiver r : listeners) {
					if ((r.clazz == event.getClass()) || (event.getClass().asSubclass(r.clazz) != null)) {
						try {
							r.method.invoke(r.object, event);
							queue.take();
						} catch (Exception e) {
							Logger.error(e, "I can't post the event!");
						}
 					}
 				}
 			}
 		}
 	}
 	
 	public ArrayList<EventReceiver> getReceivers() {
 		return listeners;
 	}
 	
 	public static EventFactory getEventFactory() {
 		return factory;
 	}
 	
 	private class EventReceiver {
 		private Object object;
 		private Method method;
 		private Class clazz;
 		
 		public EventReceiver(Object o, Method m) {
 			object = o;
 			method = m;
 			clazz = m.getParameterTypes()[0];
 		}
 	}
 }
