 package org.mule.galaxy.event;
 
 import org.mule.galaxy.event.annotation.BindToEvent;
 import org.mule.galaxy.event.annotation.BindToEvents;
 import org.mule.galaxy.event.annotation.OnEvent;
 
 import java.lang.annotation.Annotation;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.EventListener;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 public class DefaultEventManager implements EventManager {
 
     protected final Object listenersLock = new Object();
 
     protected LinkedHashMap<Class, List<GalaxyEventListener>> listeners = new LinkedHashMap<Class, List<GalaxyEventListener>>();
 
     public DefaultEventManager(final List<?> newListeners) {
         for (Object listener : newListeners) {
             addListener(listener);
         }
     }
 
     public void addListener(final Object listenerCandidate) {
         if (listenerCandidate == null) {
             throw new IllegalArgumentException("Listener can't be null");
         }
 
         // get event binding annotation
         final Class<?> clazz = listenerCandidate.getClass();
 
         final String[] eventNames;
         GalaxyEventListener adapter = null;
 
         // single-event listeners
         final Annotation annotation = findAnnotation(clazz, BindToEvent.class);
         if (annotation != null) {
             eventNames = new String[] {((BindToEvent) annotation).value()};
             Method[] methods = clazz.getMethods();
             for (final Method method : methods) {
                 if (method.isAnnotationPresent(OnEvent.class)) {
                     // detect duplicate entry-points
                     if (adapter != null) {
                         throw new IllegalArgumentException("Multiple @OnEvent entry-points detected for " + clazz.getName());
                     }
                     adapter = new DelegatingSingleEventListener(annotation, listenerCandidate, method);
                 }
             }
 
             // no OnEvent annotation found, fail
             if (adapter == null) {
                 throw new IllegalArgumentException(String.format("Listener %s is missing an @OnEvent entry point",
                                                                  listenerCandidate.getClass().getName()));
             }
         } else if (clazz.isAnnotationPresent(BindToEvents.class)) {
             // multi-event listeners
             eventNames = clazz.getAnnotation(BindToEvents.class).value();
             adapter = new DelegatingMultiEventListener(listenerCandidate);
         } else {
             throw new IllegalArgumentException(clazz.getName() + " doesn't have a BindToEvent(s) annotation");
         }
         
         for (String eventName : eventNames) {
             registerListener(adapter, eventName);
         }
 
         //synchronized (listenersLock) {
             // TODO reimplement
             // check for duplicate registration, this could be a programming error
             // due to a missing paired call of removeListener() earlier
             //for (Iterator<GalaxyEventListener> it = listeners.iterator(); it.hasNext();) {
             //    GalaxyEventListener reference = it.next();
             //    if (listener == reference) {
             //        System.out.println(">>> Listener " + listener + " already registered. Duplicate listenerCandidate registration could be a " +
             //                           "programming error due to a missing removeListener() call earlier. Ignoring this request.");
             //    }
             //}
             //listeners.add(listener);
         //}
     }
 
     /**
      * Check for annotation presence <strong>with inheritance</strong>. I.e. if a subclass doesn't have an annotation,
      * but extends a class having one, this method would report the subclass as having the annotation.
      * @return annotation or null if none found
      */
     protected Annotation findAnnotation(Class<?> clazz, final Class<? extends Annotation> annotation) {
         boolean annotationPresent = clazz.isAnnotationPresent(annotation);
         // doesn't handle cases when a listener implements an interface which has an annotation
         while (!annotationPresent && clazz.getSuperclass() != null) {
             clazz = clazz.getSuperclass();
             annotationPresent = clazz.isAnnotationPresent(annotation);
         }
 
         return annotationPresent ? clazz.getAnnotation(annotation) : null;
     }
 
     protected void registerListener(final GalaxyEventListener listener, final String eventName) {
 
         if (listener == null) {
             throw new IllegalArgumentException(
                     String.format("Attempt detected to register a null listener for %s event", eventName));
         }
 
         // get event name and load its class
         String evtClassName = "org.mule.galaxy.event." + eventName + "Event";
         ClassLoader current = Thread.currentThread().getContextClassLoader();
         synchronized (listenersLock) {
             try {
                 Class<? extends GalaxyEvent> eventClass = Class.forName(evtClassName, true, current).asSubclass(GalaxyEvent.class);
                 List<GalaxyEventListener> evtListeners = listeners.get(eventClass);
                 if (evtListeners == null) {
                     evtListeners = new LinkedList<GalaxyEventListener>();
                 }
                 evtListeners.add(listener);
                 listeners.put(eventClass, evtListeners);
             } catch (ClassNotFoundException e) {
                 final String realListenerClass = listener instanceof DelegatingGalaxyEventListener
                         ? ((DelegatingGalaxyEventListener) listener).getDelegateListener().getClass().getName()
                         : listener.getClass().getName();
                 throw new IllegalArgumentException(String.format("Event class %s not found for listener %s",
                                                                  evtClassName, realListenerClass));
             }
         }
     }
 
     public void removeListener(final Class eventClass) {
         synchronized (listenersLock) {
             // TODO don't like the way it's done really
             if (listeners.remove(eventClass) == null) {
                 // TODO warn about incorrect usage
             }
         }
     }
 
     public void fireEvent(final GalaxyEvent event) {
         synchronized (listenersLock) {
             List<GalaxyEventListener> eventListeners = listeners.get(event.getClass());
 
             if (eventListeners != null && !eventListeners.isEmpty()) {
                 for (GalaxyEventListener listener : eventListeners) {
                    // TODO blocking/non-blocking
                    listener.onEvent(event);
                 }
             }
         }
     }
 
     /**
      * Delegates to a single method marked with the {@link OnEvent} annotation.
      */
     protected static class DelegatingSingleEventListener implements DelegatingGalaxyEventListener {
         private final Object delegate;
         private final Method method;
 
         public DelegatingSingleEventListener(final Annotation annotation, final Object listenerCandidate, final Method method) {
             this.method = method;
             MethodParamValidator.validateMethodParam(method);
             final String eventName = ((BindToEvent) annotation).value() + "Event";
             final String callbackParam = method.getParameterTypes()[0].getSimpleName();
             if (!callbackParam.equals(eventName)) {
                 throw new IllegalArgumentException(
                         String.format("Listener %s is bound to the %s, but " +
                                       "callback method param %s doesn't match it.",
                                       listenerCandidate.getClass().getName(),
                                       eventName, callbackParam));
             }
             delegate = listenerCandidate;
         }
 
         public void onEvent(final GalaxyEvent event) {
             try {
                 method.invoke(delegate, event);
             } catch (IllegalAccessException e) {
                 throw new RuntimeException(e);
             } catch (InvocationTargetException itex) {
                 final Throwable cause = itex.getTargetException();
                 throw new RuntimeException(cause);
             }
         }
 
         public Object getDelegateListener() {
             return delegate;
         }
 
     }
 
     /**
      * Delegates to a listener observing multiple events (through the {@link BindToEvents} annotation and thus
      * having multiple entry points annotated with {@link OnEvent}.
      */
     protected static class DelegatingMultiEventListener implements DelegatingGalaxyEventListener {
         private final Object delegate;
 
         private Map<Class<? extends GalaxyEvent>, Method> eventToMethodMap = new HashMap<Class<? extends GalaxyEvent>, Method>();
 
         public DelegatingMultiEventListener(final Object listenerCandidate) {
             delegate = listenerCandidate;
             // discover and initialize event-to-method mappings
             Method[] methods = listenerCandidate.getClass().getMethods();
             for (Method method : methods) {
                 if (method.isAnnotationPresent(OnEvent.class)) {
                     MethodParamValidator.validateMethodParam(method);
                     Class<? extends GalaxyEvent> paramType = method.getParameterTypes()[0].asSubclass(GalaxyEvent.class);
                     eventToMethodMap.put(paramType, method);
                 }
             }
         }
 
         public void onEvent(final GalaxyEvent event) {
             Method method = eventToMethodMap.get(event.getClass());
 
             if (method == null) {
                 throw new IllegalArgumentException(
                         String.format("Event %s is not supported by this listener. Supported types are %s",
                                       event.getClass().getName(), eventToMethodMap.keySet())
                 );
             }
 
             try {
                 method.invoke(delegate, event);
             } catch (IllegalAccessException e) {
                 throw new RuntimeException(e);
             } catch (InvocationTargetException itex) {
                 final Throwable cause = itex.getTargetException();
                 throw new RuntimeException(cause);
             }
         }
 
         public Object getDelegateListener() {
             return delegate;
         }
     }
 
     protected static interface GalaxyEventListener extends EventListener {
         void onEvent(GalaxyEvent event);
     }
 
     protected static interface DelegatingGalaxyEventListener extends GalaxyEventListener {
         Object getDelegateListener();
     }
 
     /*
         Ugly static method, but otherwise we face a listener hierarchy explosion.
      */
     protected static class MethodParamValidator {
 
         protected static void validateMethodParam(final Method method) {
             // validate the number of parameters
             Class<?>[] paramTypes = method.getParameterTypes();
             if (paramTypes.length == 0) {
                 throw new IllegalArgumentException(
                         String.format("Method %s has an @OnEvent annotation, but accepts no Galaxy event class",
                                       method.toGenericString()));
             }
 
             if (paramTypes.length > 1) {
                 throw new IllegalArgumentException(
                         String.format("Method %s has an @OnEvent annotation, but accepts multiple parameters. Only a " +
                                       "single parameter is allowed, and it must be a Galaxy event class",
                                       method.toGenericString()));
 
             }
 
             Class<?> paramType = paramTypes[0];
             if (!GalaxyEvent.class.isAssignableFrom(paramType)) {
                 throw new IllegalArgumentException(
                         String.format("Method %s has an @OnEvent annotation, but doesn't accept a Galaxy event class",
                                       method.toGenericString()));
             }
         }
 
     }
 }
