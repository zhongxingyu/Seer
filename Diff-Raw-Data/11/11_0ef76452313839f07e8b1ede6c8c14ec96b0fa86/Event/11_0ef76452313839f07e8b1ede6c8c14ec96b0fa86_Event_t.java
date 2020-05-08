 package sat.events;
 
 import java.io.Serializable;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 
 public class Event implements Cloneable, Serializable {
	transient private EventEmitterInterface emitter;
 
 	public EventEmitterInterface getEmitter() {
 		return emitter;
 	}
 
 	public final void notify(EventListener listener) throws UnhandledEventException, InvocationTargetException {
 		notify(listener, null);
 	}
 
 	public final void notify(EventListener listener, EventEmitterInterface emitter) throws UnhandledEventException, InvocationTargetException {
 		Event event = (Event) this.clone();
 		event.emitter = emitter;
 
 		// In memoriam of Generics-powered events
 
 		Class<?> eventClass = event.getClass();
 
 		while(eventClass != null) {
 			try {
 				Method on = listener.getClass().getMethod("on", eventClass);
 				on.setAccessible(true); // Inner-class are otherwise unavailable
 				on.invoke(listener, event);
 				return;
 			}
 			catch(InvocationTargetException e) {
 				throw e;
 			}
 			catch(ReflectiveOperationException e) {
 				//e.printStackTrace();
 
 				if(eventClass == Event.class) {
 					break;
 				}
 
 				eventClass = eventClass.getSuperclass();
 			}
 		}
 
 		throw new UnhandledEventException();
 	}
 
 	public Object clone() {
 		try {
 			return super.clone();
 		}
 		catch(CloneNotSupportedException e) {
 			return this;
 		}
 	}
 
 	private static final long serialVersionUID = -6882392750878581169L;
 }
