 /*
  * Copyright 2012 the original author or authors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */ 
 package org.yldt.event;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.LinkedHashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 import org.yldt.logging.LogManager;
 import org.yldt.logging.Logger;
 
 /**
  * Implementation of {@link EventBus}.
  * @author Yun Liu
  *
  */
 public class ApplicationEventBus implements EventBus {
 	private final ConcurrentMap<Class<?>, List<EventHandler<?>>> registry;
 	private static final Logger logger = LogManager.getLogger(ApplicationEventBus.class);
 
 	public ApplicationEventBus() {
 		registry = new ConcurrentHashMap<Class<?>, List<EventHandler<?>>>();
 	}
 
 	public void scanEventHandlers(final Object source) {
 		for (final Method method : source.getClass().getMethods()) {
 			scanForHandler(source, method);
 		}
 	}
 
 	private void scanForHandler(final Object source, final Method method) {
 		final Handles handles = method.getAnnotation(Handles.class);
 		if (handles != null) {
 			logHandlesMethodDetected(source, method);
 			final Class<?>[] parameterTypes = method.getParameterTypes();
 			if (parameterTypes.length == 0) {
 				final Class<? extends Event> eventType = handles.value();
 				registerNoArgEventHandler(eventType, source, method);
 			} else if (parameterTypes.length == 1) {
 				final Class<? extends Event> eventType = handles.value();
 				final Class<?> declaredParameterType = parameterTypes[0];
 				assertAssignable(eventType, declaredParameterType);
 				registerOneArgEventHandler(eventType, source, method);
 			} else {
 				throw new EventBusException(
 						"Invalid event handler definition. Expect 0 or 1 argument but found ["
 								+ parameterTypes.length + "]");
 			}
 		}
 	}
 
 	private void assertAssignable(final Class<? extends Event> eventType,
 			Class<?> declaredParameterType) {
 		if (!declaredParameterType.isAssignableFrom(eventType))
 			throw new EventBusException("Incompatiable formal parameter argument type ["
 					+ declaredParameterType
 					+ "]. The formal parameter must be assignable from the declared event type [" + eventType
 					+ "]");
 	}
 
 	private <T extends Event> void registerOneArgEventHandler(
 			final Class<T> eventType, final Object source, final Method method) {
 		this.registerHandler(eventType, new OneArgEventHandler<T>(source,
 				method));
 	}
 
 	private <T extends Event> void registerNoArgEventHandler(
 			final Class<T> eventType, final Object source, final Method method) {
 		this.registerHandler(eventType,
 				new NoArgEventHandler<T>(source, method));
 	}
 
 	private static abstract class ReflectionEventHandler<T extends Event>
 			implements EventHandler<T> {
 		private final Object object;
 		private final Method method;
 
 		public ReflectionEventHandler(final Object object, final Method method) {
 			this.object = object;
 			this.method = method;
 		}
 
 		public void onEvent(final T event) {
 			try {
 				innerOnEvent(event);
 			} catch (final IllegalAccessException e) {
 				throw new EventBusException("IllegalAccess when invoking event handler",e);
 			} catch (final InvocationTargetException e) {
 				throw new EventBusException("Event handler failed due to exception from handler.", e.getCause());
 			}
 		}
 
 		protected abstract void innerOnEvent(T event)
 				throws IllegalAccessException, InvocationTargetException;
 
 		@Override
 		public String toString() {
 			return object.toString() + "." + method.getName();
 		}
 
 		public Object getObject() {
 			return object;
 		}
 
 		public Method getMethod() {
 			return method;
 		}
 	}
 
 	private static class NoArgEventHandler<T extends Event> extends
 			ReflectionEventHandler<T> {
 		public NoArgEventHandler(final Object object, final Method method) {
 			super(object, method);
 		}
 
 		@Override
 		protected void innerOnEvent(final T event)
 				throws IllegalAccessException, InvocationTargetException {
 			this.getMethod().invoke(getObject());
 		}
 	}
 
 	private static class OneArgEventHandler<T extends Event> extends
 			ReflectionEventHandler<T> {
 		public OneArgEventHandler(final Object object, final Method method) {
 			super(object, method);
 		}
 
 		@Override
 		protected void innerOnEvent(final T event)
 				throws IllegalAccessException, InvocationTargetException {
 			this.getMethod().invoke(getObject(), event);
 		}
 	}
 
 	public <T extends Event> void registerHandler(final Class<T> eventType,
 			final EventHandler<T> handler) {
 		List<EventHandler<?>> handlers = registry.get(eventType);
 		if (handlers == null) {
 			handlers = new CopyOnWriteArrayList<EventHandler<?>>();
 			List<EventHandler<?>> previous = registry.putIfAbsent(eventType, handlers);
 			if(previous != null)
 				handlers = previous;
 		}
 
 		logHandlerRegistered(handler, eventType);
 		handlers.add(handler);
 	}
 
 	public <T extends Event> void fire(final T event) {
 		if(event == null)
 			throw new IllegalArgumentException("Event must not be null");
 		log(event);
 		final List<EventHandler<?>> handlers = getHandlersFor(event);
 		if (!handlers.isEmpty()) {
 			for (final EventHandler<?> handler : handlers) {
 				@SuppressWarnings("unchecked")
 				final EventHandler<T> typedHandler = (EventHandler<T>) handler;
 				typedHandler.onEvent(event);
 				logEventHandled(typedHandler, event);
 			}
 		} else {
 			logNoHandlers(event);
 		}
 	}
 
 	private <T> List<EventHandler<?>> getHandlersFor(final T event) {
 		List<EventHandler<?>> handlers = new LinkedList<EventHandler<?>>();
 		Class<?> eventClass = event.getClass();
 		while(eventClass != null && eventClass != Object.class){
 			List<EventHandler<?>> handlerForClass = registry.get(eventClass);
 			if(handlerForClass != null){
 				handlers.addAll(handlerForClass);
 			}
 			
 			eventClass = eventClass.getSuperclass();
 		}
 		
 		
 		LinkedHashSet<Class<?>> eventInterfaces = collectAllEventInterfaces(event.getClass());
 	
 		for(Class<?> eventInterface : eventInterfaces){
 			List<EventHandler<?>> handlerForClass = registry.get(eventInterface);
 			if(handlerForClass != null){
 				handlers.addAll(handlerForClass);
 			}
 		}
 		return handlers;
 	}
 
 	private LinkedHashSet<Class<?>> collectAllEventInterfaces(Class<?> klass) {
 		LinkedHashSet<Class<?>> interfaces = new LinkedHashSet<Class<?>>();
 		
 		for(Class<?> eventInterface : klass.getInterfaces()){
 			if(Event.class.isAssignableFrom(eventInterface)){
 				interfaces.add(eventInterface);
 				if(eventInterface != Event.class){
 					interfaces.addAll(collectAllEventInterfaces(eventInterface));
 				}
 			}
 		}
 		
 		Class<?> superClass = klass.getSuperclass();
		if(klass != null && klass != Object.class)
 			interfaces.addAll(collectAllEventInterfaces(superClass));
 		return interfaces;
 	}
 
 	private <T extends Event> void log(final T event) {
 		if (logger.isDebugEnabled())
 			logger.debug("Event [" + event + "] is fired");
 	}
 
 	private <T extends Event> void logHandlerRegistered(
 			final EventHandler<T> handler, final Class<T> eventType) {
 		if (logger.isDebugEnabled())
 			logger.debug("Handler " + handler
 					+ " is registered to handle event [" + eventType + "]");
 	}
 
 	private <T extends Event> void logNoHandlers(final T event) {
 		if (logger.isDebugEnabled())
 			logger.debug("Not handler is configured to handle Event [" + event
 					+ "]");
 	}
 
 	private <T extends Event> void logEventHandled(
 			final EventHandler<T> handler, final T event) {
 		if (logger.isDebugEnabled())
 			logger.debug("Handler " + handler + " handles event [" + event
 					+ "]");
 	}
 
 	private void logHandlesMethodDetected(final Object source,
 			final Method method) {
 		if (logger.isDebugEnabled())
 			logger.debug("Detected handler method "
 					+ source.getClass().getName() + "." + method.getName());
 
 	}
 }
