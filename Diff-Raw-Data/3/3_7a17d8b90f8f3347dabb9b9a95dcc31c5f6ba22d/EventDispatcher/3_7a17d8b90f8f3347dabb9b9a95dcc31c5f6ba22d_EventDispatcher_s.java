 package edu.myhorseshow.events;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import android.util.Log;
 
 public class EventDispatcher implements Dispatcher
 {
 	
 	public void addListener(String type, EventListener listener) {
 		synchronized (mListenersMap)
 		{
 			ArrayList<EventListener> listeners = mListenersMap.get(type);
 			if (listeners == null)
 			{
 				listeners = new ArrayList<EventListener>();
 				mListenersMap.put(type, listeners);
 			}
 			listeners.add(listener);
 		}
 	}
 
 	public void removeListener(String type, EventListener listener) {
 		synchronized (mListenersMap)
 		{
 			ArrayList<EventListener> listeners = mListenersMap.get(type);
 			if (listeners == null)
 				return;
 			listeners.remove(listener);
 			if (listeners.size() == 0)
 				mListenersMap.remove(listeners);
 		}
 	}
 
 	public boolean hasListener(String type, EventListener listener) {
 		synchronized (mListenersMap)
 		{
 			ArrayList<EventListener> listeners = mListenersMap.get(type);
 			if (listeners == null)
 				return false;
 			return listeners.contains(listener);
 		}
 	}
 
 	public void dispatchEvent(Event event) {
 		if (event == null)
 		{
 			Log.e(TAG, "cannot dispatch null event");
 			return;
 		}
 		
 		String type = event.getType();
 		event.setSource(this);
 		
 		ArrayList<EventListener> listeners;
 		synchronized (mListenersMap)
 		{
 			listeners = mListenersMap.get(type);
 		}
 		
 		if (listeners == null)
 			return;
 		
 		for (EventListener listener: listeners)
 			listener.onEvent(event);
 	}
 	
 	protected void notifyChange(String type)
 	{
 		dispatchEvent(new SimpleEvent(type));
 	}
 	
	private HashMap<String, ArrayList<EventListener>> mListenersMap;
 	private static final String TAG = EventDispatcher.class.getSimpleName();
 }
