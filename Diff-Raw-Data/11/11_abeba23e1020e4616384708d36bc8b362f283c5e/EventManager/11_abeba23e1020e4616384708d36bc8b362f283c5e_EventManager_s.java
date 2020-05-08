 package org.jackie.event;
 
 import org.jackie.context.Service;
 import org.jackie.utils.Assert;
 
 import java.util.List;
 import java.util.Map;
import java.util.Set;
 import java.util.ArrayList;
 import java.util.HashMap;
 import static java.util.Collections.emptyList;
 
 /**
  * @author Patrik Beno
  */
 public class EventManager implements Service {
 
 	Map<Class, List<Event>> listenersByType;
 
 	Map<Class, EventDispatcherProxy> proxies;
 
 	{
 		listenersByType = new HashMap<Class, List<Event>>();
 		proxies = new HashMap<Class, EventDispatcherProxy>();
 	}
 
 	public <T extends Event> void registerEventListener(Class<T> type, T listener) {
 		List<Event> listeners = listenersByType.get(type);
 		if (listeners == null) {
 			listeners = new ArrayList<Event>();
 			listenersByType.put(type, listeners);
 		}
 		listeners.add(listener);
 	}
 
 	public <T extends Event> void unregisterEventListener(T listener) {
 		throw Assert.notYetImplemented(); // todo implement this
 	}
 
 	public <T extends Event> List<T> getListeners(Class<T> type) {
 		List<Event> listeners = listenersByType.get(type);
 		return (listeners != null) ? new ArrayList(listeners) : emptyList();
 	}
 
 	public <T extends Event> T getEventDispatcher(Class<T> type) {
 		EventDispatcherProxy proxy = proxies.get(type);
 		if (proxy == null) {
 			proxy = new EventDispatcherProxy(this, type);
 			proxies.put(type, proxy);
 		}
 		return type.cast(proxy.getProxy());
 	}
 }
