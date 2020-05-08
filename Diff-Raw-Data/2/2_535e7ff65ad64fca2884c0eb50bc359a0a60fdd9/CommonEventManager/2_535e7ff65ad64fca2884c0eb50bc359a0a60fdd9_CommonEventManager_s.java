 /*
  * This file is part of Beaker.
  *
  * Copyright (c) 2013, alta189 <http://beaker.alta189.com/>
  * Beaker is licensed under the GNU Lesser General Public License.
  *
  * Beaker is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Beaker is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.alta189.beaker;
 
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import com.alta189.beaker.exceptions.EventExecutorException;
 import com.alta189.beaker.exceptions.EventRegistrationException;
 import com.alta189.commons.objects.Named;
 import com.alta189.commons.util.CastUtil;
 import com.alta189.commons.util.ReflectionUtil;
 
 import org.apache.commons.lang3.Validate;
 
 /**
  * Simple implementation of {@link EventManager}
  */
 public class CommonEventManager implements EventManager {
 	private final Map<Class<? extends Event>, List<HandlerRegistration>> registrations = new HashMap<Class<? extends Event>, List<HandlerRegistration>>();
 	private final HandlerPriorityComparator priorityComparator = new HandlerPriorityComparator();
 
 	/**
 	 * Calls the event
 	 *
 	 * @param event event to be called
 	 */
 	@Override
 	public void call(Event event) {
 		List<HandlerRegistration> list = getRegistrationList(event.getClass());
 		final boolean cancellable = Cancellable.class.isAssignableFrom(event.getClass());
 
 		for (HandlerRegistration registration : list) {
 			try {
 				if ((cancellable && event.isCancelled()) && !registration.isIgnoredCancelled()) {
 					continue;
 				}
 				registration.getExecutor().execute(event);
 			} catch (Exception e) {
 				new EventExecutorException("Error while '" + registration.getOwner().getName() + "' was handling the event", e).printStackTrace();
 			}
 		}
 	}
 
 	/**
 	 * Registers all the {@link EventHandler}s contained in the
 	 * Listener
 	 *
 	 * @param listener registration, not null
 	 * @param owner owner of the listener, not null
 	 * @throws com.alta189.beaker.exceptions.EventRegistrationException
 	 */
 	@Override
 	public void registerListener(Listener listener, Named owner) throws EventRegistrationException {
 		try {
 			Validate.notNull(listener, "Listener cannot be null");
 			Validate.notNull(owner, "Owner cannot be null");
 			Validate.notNull(owner.getName(), "Owner's name cannot be null");
 			Validate.notEmpty(owner.getName(), "Owner's name cannot be empty");
 
 			for (Method method : listener.getClass().getDeclaredMethods()) {
				if (method.isAnnotationPresent(EventHandler.class)) {
 					continue;
 				}
 
 				EventHandler handler = method.getAnnotation(EventHandler.class);
 				Validate.notNull(handler.ignoreCancelled(), "ignoredCancelled cannot be null");
 				Validate.notNull(handler.priority(), "Priority cannot be null");
 
 				if (!Modifier.isPublic(method.getModifiers())) {
 					throw new EventRegistrationException("Method has to be public");
 				}
 
 				if (Modifier.isStatic(method.getModifiers())) {
 					throw new EventRegistrationException("Method cannot be static");
 				}
 
 				if (method.getParameterTypes().length != 1) {
 					throw new EventRegistrationException("Method cannot have more than one parameter");
 				}
 
 				if (!Event.class.isAssignableFrom(method.getParameterTypes()[0])) {
 					throw new EventRegistrationException("Method's parameter type has to extend class");
 				}
 
 				EventExecutor executor = new AnnotatedEventExecutor(listener, method);
 				HandlerRegistration registration = new HandlerRegistration(executor, handler.priority(), handler.ignoreCancelled(), owner);
 				getRegistrationList((Class<? extends Event>) method.getParameterTypes()[0], true).add(registration);
 			}
 		} catch (EventRegistrationException e) {
 		    throw e;
 		} catch (Exception e) {
 			throw new EventRegistrationException(e);
 		}
 	}
 
 	/**
 	 * Registers a single {@link EventExecutor}
 	 *
 	 * @param event  event to register to the executor to, not null
 	 * @param eventExecutor executor to be registered, not null
 	 * @param priority the executor's priority, not null
 	 * @param owner the owner of the executor, not null
 	 * @throws com.alta189.beaker.exceptions.EventRegistrationException
 	 */
 	@Override
 	public void registerExecutor(Class<? extends Event> event, EventExecutor eventExecutor, Priority priority, Named owner) throws EventRegistrationException {
 		registerExecutor(event, eventExecutor, priority, false, owner);
 	}
 
 	/**
 	 * Registers a single {@link EventExecutor}
 	 *
 	 * @param event  event to register to the executor to, not null
 	 * @param eventExecutor executor to be registered, not null
 	 * @param priority the executor's priority, not null
 	 * @param ignoreCancelled  whether a listener ignores if an event has been cancelled, not null
 	 * @param owner the owner of the executor, not null
 	 * @throws com.alta189.beaker.exceptions.EventRegistrationException
 	 */
 	@Override
 	public void registerExecutor(Class<? extends Event> event, EventExecutor eventExecutor, Priority priority, boolean ignoreCancelled, Named owner) throws EventRegistrationException {
 		try {
 			Validate.notNull(event, "Event cannot be null");
 			Validate.notNull(eventExecutor, "Executor cannot be null");
 			Validate.notNull(priority, "Priority cannot be null");
 			Validate.notNull(ignoreCancelled, "ignoredCancelled cannot be null");
 			Validate.notNull(owner, "Owner cannot be null");
 			Validate.notNull(owner.getName(), "Owner's name cannot be null");
 			Validate.notEmpty(owner.getName(), "Owner's name cannot be empty");
 
 			getRegistrationList(event, true).add(new HandlerRegistration(eventExecutor, priority, ignoreCancelled, owner));
 		} catch (Exception e) {
 			throw new EventRegistrationException(e);
 		}
 	}
 
 	private List<HandlerRegistration> getRegistrationList(Class<? extends Event> clazz) {
 	    return getRegistrationList(clazz, false);
 	}
 
 	private List<HandlerRegistration> getRegistrationList(Class<? extends Event> clazz, boolean create) {
 		List<HandlerRegistration> result = registrations.get(clazz);
 		if (result == null) {
 			result = new ArrayList<HandlerRegistration>();
 			registrations.put(clazz, result);
 		}
 		return result;
 	}
 
 	private List<HandlerRegistration> sortList(List<HandlerRegistration> list) {
 		Collections.sort(list, priorityComparator);
 		return list;
 	}
 }
