 package geomatico.events;
 
 import java.lang.ref.WeakReference;
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeSet;
 
 /**
  * Class that dispatches all {@link Event}s to interested parties.
  * 
  * @author Fernando González Cortés
  * @author Víctor González Cortés
  */
 public class EventBus {
 	private static final EventBus instance = new EventBus();
 
 	/**
 	 * Singleton
 	 * 
 	 * @return singleton
 	 */
 	public static EventBus getInstance() {
 		return instance;
 	}
 
 	private EventBus() {
 	}
 
 	private Map<Class<?>, Set<WeakReference<EventHandler>>> map = new HashMap<Class<?>, Set<WeakReference<EventHandler>>>();
 
 	/**
 	 * Adds an handler to receive events of this type from all sources.
 	 * 
 	 * @param type
 	 *            the event type associated with this handler
 	 * @param handler
 	 *            the handler
 	 */
 	public synchronized <H extends EventHandler> void addHandler(
 			Class<? extends Event<H>> type, H handler) {
 		Set<WeakReference<EventHandler>> handlers = map.get(type);
 		if (handlers == null) {
 			handlers = new TreeSet<WeakReference<EventHandler>>(
 					new WeakReferenceComparator());
 			map.put(type, handlers);
 		}
 
 		handlers.add(new WeakReference<EventHandler>(handler));
 	}
 
 	/**
 	 * Fires the event
 	 * 
 	 * @param event
 	 *            the event to fire
 	 */
 	public synchronized <H extends EventHandler> void fireEvent(Event<H> event) {
 		Set<WeakReference<EventHandler>> handlers = map.get(event.getClass());
 		ArrayList<WeakReference<EventHandler>> toRemove = new ArrayList<WeakReference<EventHandler>>();
 		for (WeakReference<EventHandler> handlerReference : handlers) {
 			@SuppressWarnings("unchecked")
 			H handler = (H) handlerReference.get();
 			if (handler != null) {
 				event.dispatch(handler);
 			} else {
 				toRemove.add(handlerReference);
 			}
 		}
 
 		for (WeakReference<EventHandler> weakReference : toRemove) {
 			handlers.remove(weakReference);
 		}
 	}
 
 	/**
 	 * Comparator to avoid duplicates of handlers
 	 */
 	private final class WeakReferenceComparator implements
 			Comparator<WeakReference<EventHandler>> {
 		@Override
 		public int compare(WeakReference<EventHandler> o1,
 				WeakReference<EventHandler> o2) {
 			EventHandler eventHandler1 = o1.get();
 			EventHandler eventHandler2 = o2.get();
 			if (eventHandler1 == eventHandler2) {
 				return 0;
 			} else {
 				String str1 = "";
 				String str2 = "";
 				if (eventHandler1 != null) {
 					str1 = eventHandler1.toString();
 				}
 				if (eventHandler2 != null) {
 					str2 = eventHandler2.toString();
 				}
 				return str1.compareTo(str2);
 			}
 		}
 	}
 }
