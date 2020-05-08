 /*
 Copyright (c) 2013 J. L. Canales Gasco
  
 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.
  
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
  
 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA}]
  */
 package org.rotarysource.listener;
 
 import java.lang.reflect.Array;
 import java.lang.reflect.GenericArrayType;
 import java.lang.reflect.ParameterizedType;
 import java.lang.reflect.Type;
 import java.lang.reflect.TypeVariable;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.espertech.esper.client.EventBean;
 import com.espertech.esper.client.UpdateListener;
 
 /**
  * Implements Base class for a Esper listener to manage calls that Collects an
  * EvenProd event. This listener allow create event Object based listeners avoiding
  * the complexity to map Event Fiels to object and concentrate in Business Logic
  * 
  * To infere Argument Types, this class use methods described by Ian Robertson in his
  * blog http://www.artima.com/weblogs/viewpost.jsp?thread=208860
  * @author J. L. Canales
  * 
  */
 public abstract class ObjectBaseListener<T> implements UpdateListener {
 
 	private static Logger log = LoggerFactory.getLogger(ObjectBaseListener.class);
 
 	/**
 	 * UpdateListener update overriden method This method implements the
 	 * UpdateListener interfaces mapping EventBean object to a T Object
 	 * 
 	 * @param newEvents
 	 *            . Event data received from esper engine
 	 * @param oldEvents
 	 *            . Old Event Data received from esper engine
 	 * 
 	 */
 	@Override
 	public void update(EventBean[] newEvents, EventBean[] oldEvents) {
 		if (newEvents == null) {
 			return;
 		}
 
 		for (int i = 0; i < newEvents.length; i++) {
 			// check for class types in order to avoid
 			// Cast exceptions
 			if (getArgumentTypeClass().equals(
 					newEvents[i].getEventType().getUnderlyingType())) {
				@SuppressWarnings("unchecked")
 				T event = (T) newEvents[i].getUnderlying();
 				processEvent(event);
 			} else {
 				log.error("Event Type {} don't match with type listener {}",
 						getArgumentTypeClass(), 
 						newEvents[i].getEventType().getUnderlyingType());
 			}
 
 		}
 	}
 
 	/**
 	 * Abstract method to allow child classes to implement the processing logic
 	 * for each Map object.
 	 * 
 	 * @param event
 	 *            Map Object to be processed
 	 */
 	public abstract void processEvent(T event);
 
 	/**
 	 * Get the underlying class for a type, or null if the type is a variable
 	 * type.
 	 * 
 	 * @param type
 	 *            the type
 	 * @return the underlying class
 	 */
 	public static Class<?> getClass(Type type) {
 		if (type instanceof Class) {
 			return (Class<?>) type;
 		} else if (type instanceof ParameterizedType) {
 			return getClass(((ParameterizedType) type).getRawType());
 		} else if (type instanceof GenericArrayType) {
 			Type componentType = ((GenericArrayType) type)
 					.getGenericComponentType();
 			Class<?> componentClass = getClass(componentType);
 			if (componentClass != null) {
 				return Array.newInstance(componentClass, 0).getClass();
 			} else {
 				return null;
 			}
 		} else {
 			return null;
 		}
 	}
 
 	/**
 	 * Get the actual type arguments a child class has used to extend a generic
 	 * base class.
 	 * 
 	 * @param baseClass
 	 *            the base class
 	 * @param childClass
 	 *            the child class
 	 * @return a list of the raw classes for the actual type arguments.
 	 */
 	public static <T> List<Class<?>> getTypeArguments(Class<T> baseClass,
 			Class<? extends T> childClass) {
 		Map<Type, Type> resolvedTypes = new HashMap<Type, Type>();
 		Type type = childClass;
 		// start walking up the inheritance hierarchy until we hit baseClass
 		while (!getClass(type).equals(baseClass)) {
 			if (type instanceof Class) {
 				// there is no useful information for us in raw types, so just
 				// keep going.
 				type = ((Class<?>) type).getGenericSuperclass();
 			} else {
 				ParameterizedType parameterizedType = (ParameterizedType) type;
 				Class<?> rawType = (Class<?>) parameterizedType.getRawType();
 
 				Type[] actualTypeArguments = parameterizedType
 						.getActualTypeArguments();
 				TypeVariable<?>[] typeParameters = rawType.getTypeParameters();
 				for (int i = 0; i < actualTypeArguments.length; i++) {
 					resolvedTypes
 							.put(typeParameters[i], actualTypeArguments[i]);
 				}
 
 				if (!rawType.equals(baseClass)) {
 					type = rawType.getGenericSuperclass();
 				}
 			}
 		}
 
 		// finally, for each actual type argument provided to baseClass,
 		// determine (if possible)
 		// the raw class for that type argument.
 		Type[] actualTypeArguments;
 		if (type instanceof Class) {
 			actualTypeArguments = ((Class<?>) type).getTypeParameters();
 		} else {
 			actualTypeArguments = ((ParameterizedType) type)
 					.getActualTypeArguments();
 		}
 		List<Class<?>> typeArgumentsAsClasses = new ArrayList<Class<?>>();
 		// resolve types by chasing down type variables.
 		for (Type baseType : actualTypeArguments) {
 			while (resolvedTypes.containsKey(baseType)) {
 				baseType = resolvedTypes.get(baseType);
 			}
 			typeArgumentsAsClasses.add(getClass(baseType));
 		}
 		return typeArgumentsAsClasses;
 	}
 
 	public Class<?> getArgumentTypeClass() {
 		return getTypeArguments(ObjectBaseListener.class, getClass()).get(0);
 	}
 
 }
