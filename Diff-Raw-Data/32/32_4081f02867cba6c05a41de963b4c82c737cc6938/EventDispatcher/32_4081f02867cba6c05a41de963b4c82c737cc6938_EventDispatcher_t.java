 package net.sayon.dovor.events;
 
 import net.sayon.dovor.Dovor;
 import net.sayon.dovor.listeners.BuddyListener;
 
 public class EventDispatcher implements BuddyListener {
 	private Dovor dov;
 
 	public EventDispatcher(Dovor dov) {
 		this.dov = dov;
 	}
 
 	@Override
 	public void onEvent(Event e) {
		for (BuddyListener l : dov.getBuddyListeners().values()) {
 			try {
 				l.onEvent(e);
 				if (e instanceof ConsumableEvent && ((ConsumableEvent) e).isConsumed())
 					break;
 			} catch (Exception ex) {
 				ex.printStackTrace();
 				e.getBuddy().disconnect();
 			}
 		}
 	}
 
 }
