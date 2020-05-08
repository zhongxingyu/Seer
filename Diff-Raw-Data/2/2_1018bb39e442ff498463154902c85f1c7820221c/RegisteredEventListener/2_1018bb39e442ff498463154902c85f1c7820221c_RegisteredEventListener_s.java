 package com.edinarobotics.scouting.definitions.event.helpers;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.nio.channels.SeekableByteChannel;
 
 import com.edinarobotics.scouting.definitions.event.Cancellable;
 import com.edinarobotics.scouting.definitions.event.Event;
 import com.edinarobotics.scouting.definitions.event.EventListener;
 import com.edinarobotics.scouting.definitions.event.Listener;
 import com.edinarobotics.scouting.definitions.event.ListenerPriority;
 
 /**
  * This class handles the reflection tasks of calling an
  * {@link EventListener} tagged event handling method.
  * It examines the arguments of handling methods and
  * examines their EventListener annotations.
  * It provides methods to invoke these methods
  * and to provide events to them for processing.
  */
 public class RegisteredEventListener {
 	private ListenerPriority priority;
 	private Listener listener;
 	private Class<? extends Event> eventType;
 	private boolean ignoresCancelled;
 	private Method listenerMethod;
 	
 	/**
 	 * Constructs a new RegisteredEventListener surrounding the
 	 * given {@code listenerMethod} of the provided {@code listener} object.
 	 * @param listenerMethod The event handler method to be wrapped
 	 * by this RegisteredEventListener.
 	 * @param listener The object on which {@code method} is to be called.
 	 * @throws IllegalArgumentException If {@code listenerMethod} does not have
 	 * an EventListener annotation or if {@code listenerMthod} does not accept
 	 * a single {@link Event} subclass object as a parameter.
 	 */
 	public RegisteredEventListener(Method listenerMethod, Listener listener){
 		//Attempt to get the EventListener annotation on this method
 		EventListener listenerAnnotation = listenerMethod.getAnnotation(EventListener.class);
 		if(listenerAnnotation == null){
 			//No annotation, throw exception
 			throw new IllegalArgumentException("Listener method does not have an EventListener annotation.");
 		}
 		//Get priority and ignoreCancelled values from the annotation
 		priority = listenerAnnotation.priority();
 		ignoresCancelled = listenerAnnotation.ignoreCancelled();
 		//Get the parameter types of the method
 		Class<?>[] listenerMethodParameters = listenerMethod.getParameterTypes();
 		//Verify that the parameters of the method are valid for an event-handling method
		if(listenerMethodParameters.length != 1 && Event.class.isAssignableFrom(listenerMethodParameters[0])){
 			//Parameters are invalid, throw exception
 			throw new IllegalArgumentException("Listener method signature invalid. Does not have a single Event subclass parameter.");
 		}
 		//Store the type of the Event-subclass parameter in eventType
 		eventType = (Class<? extends Event>)listenerMethodParameters[0];
 		//Store the listener method in listenerMethod
 		this.listenerMethod = listenerMethod;
 		//Make sure that listenerMethod is accessible
 		if(!listenerMethod.isAccessible()){
 			listenerMethod.setAccessible(true);
 		}
 		//Store the listener object in listener
 		this.listener = listener;
 	}
 	
 	/**
 	 * Returns the ListenerPriority at which this event
 	 * handler listens.
 	 * @return The ListenerPriority of this RegisteredEventListener.
 	 */
 	public ListenerPriority getPriority(){
 		return priority;
 	}
 	
 	/**
 	 * Returns whether this RegisteredEventListener wishes
 	 * to ignore cancelled events.
 	 * If this method returns {@code true} {@link Cancellable} objects
 	 * that are cancelled should not be sent to {@link #invoke(Event)}.
 	 * @return {@code true} if the event handling method wrapped by this
 	 * RegisteredEventListener does not wish to receive cancelled events.
 	 */
 	public boolean getIgnoresCancelled(){
 		return ignoresCancelled;
 	}
 	
 	/**
 	 * Returns the {@link Listener} object to which this
 	 * RegisteredEventListener is attached.
 	 * @return The Listener object to which this event handling
 	 * method is attached.
 	 */
 	public Listener getListener(){
 		return listener;
 	}
 	
 	/**
 	 * Returns the class of the type of Event accepted
 	 * by this RegisteredEventListener.
 	 * @return The Class object representing the type
 	 * of Event accepted by this RegisteredEventListener.
 	 */
 	public Class<? extends Event> getEventType(){
 		return eventType;
 	}
 	
 	/**
 	 * Invokes the method wrapped by this RegisteredEventListener with the
 	 * given Event.
 	 * This method performs <em>no</em> checks. It will directly invoke the
 	 * method wrapped by this RegisteredEventListener with the provided
 	 * Event.
 	 * @param event The event to be passed to the event handling method.
 	 * @throws IllegalAccessException If the event handling method is inaccessible.
 	 * @throws IllegalArgumentException If the given Event is not a suitable argument
 	 * for this method.
 	 * @throws InvocationTargetException If the underlying method throws an exception.
 	 * @see Method#invoke(Object, Object...)
 	 */
 	public void invoke(Event event) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException{
 		listenerMethod.invoke(getListener(), event);
 	}
 	
 	/**
 	 * Handles sending Event objects to the underlying method of this
 	 * RegisteredEventListener.
 	 * This method will not send cancelled events to a handling method
 	 * that does not wish to receive cancelled events, it also verifies the
 	 * parameter types of the underlying method before submitting the Event
 	 * object. It is generally safe to submit any type of Event object to
 	 * this method. 
 	 * @param event The Event to be sent to the underlying method of this
 	 * RegisteredEventListener.
 	 * @throws IllegalAccessException If the event handling method is inaccessible.
 	 * @throws IllegalArgumentException If the given Event is not a suitable argument
 	 * for this method (this issue should be prevented by fireEvent).
 	 * @throws InvocationTargetException If the underlying method throws an exception.
 	 * @see #getIgnoresCancelled()
 	 * @see #invoke(Event)
 	 */
 	public void fireEvent(Event event) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException{
 		if(event instanceof Cancellable && ((Cancellable)event).isCancelled() && getIgnoresCancelled()){
 			return;
 		}
 		if(getEventType().isAssignableFrom(event.getClass())){
 			invoke(event);
 		}
 	}
 }
