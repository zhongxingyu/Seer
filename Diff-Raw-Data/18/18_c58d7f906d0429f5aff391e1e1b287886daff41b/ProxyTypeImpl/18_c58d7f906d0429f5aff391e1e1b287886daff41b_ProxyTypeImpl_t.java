 /**
  * ************************************************************************
  * 
  *    server-objects - a contrib to the Qooxdoo project that makes server 
  *    and client objects operate seamlessly; like Qooxdoo, server objects 
  *    have properties, events, and methods all of which can be access from
  *    either server or client, regardless of where the original object was
  *    created.
  * 
  *    http://qooxdoo.org
  * 
  *    Copyright:
  *      2010 Zenesis Limited, http://www.zenesis.com
  * 
  *    License:
  *      LGPL: http://www.gnu.org/licenses/lgpl.html
  *      EPL: http://www.eclipse.org/org/documents/epl-v10.php
  *      
  *      This software is provided under the same licensing terms as Qooxdoo,
  *      please see the LICENSE file in the Qooxdoo project's top-level directory 
  *      for details.
  * 
  *    Authors:
  *      * John Spackman (john.spackman@zenesis.com)
  * 
  * ************************************************************************
  */
 package com.zenesis.qx.remote;
 
 import java.io.IOException;
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import java.util.ArrayList;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import com.fasterxml.jackson.core.JsonGenerator;
 import com.fasterxml.jackson.core.JsonProcessingException;
 import com.fasterxml.jackson.databind.SerializerProvider;
 import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
 import com.zenesis.qx.remote.annotations.AlwaysProxy;
 import com.zenesis.qx.remote.annotations.DoNotProxy;
 import com.zenesis.qx.remote.annotations.Event;
 import com.zenesis.qx.remote.annotations.Events;
 import com.zenesis.qx.remote.annotations.ExplicitProxyOnly;
 import com.zenesis.qx.remote.annotations.Properties;
 import com.zenesis.qx.remote.annotations.Property;
 import com.zenesis.qx.remote.annotations.Remote;
 
 public class ProxyTypeImpl extends AbstractProxyType {
 	
 	private final static class MethodSig {
 		private final Method method;
 
 		public MethodSig(Method method) {
 			super();
 			this.method = method;
 		}
 
 		/* (non-Javadoc)
 		 * @see java.lang.Object#hashCode()
 		 */
 		@Override
 		public int hashCode() {
 			return method.getName().hashCode();
 		}
 
 		/* (non-Javadoc)
 		 * @see java.lang.Object#equals(java.lang.Object)
 		 */
 		@Override
 		public boolean equals(Object obj) {
 			MethodSig that = (MethodSig)obj;
 			return that.method.getName().equals(method.getName()) && hasSameSignature(method, that.method);
 		}
 
 		/* (non-Javadoc)
 		 * @see java.lang.Object#toString()
 		 */
 		@Override
 		public String toString() {
 			return method.toString();
 		}		
 	}
 
 	/*
 	 * Helper class to track methods
 	 */
 	private final class MethodsCompiler {
 		// Methods we found
 		final HashMap<String, Method> methods = new HashMap<String, Method>();
 		
 		// Methods that should not be proxied
 		final HashSet<MethodSig> doNotProxyMethods = new HashSet<ProxyTypeImpl.MethodSig>();
 		
 		/**
 		 * Adds all methods from a given class
 		 * @param fromClass
 		 * @param defaultProxy
 		 */
 		public void addMethods(Class fromClass, boolean defaultProxy) {
 			boolean explicitOnly = fromClass.isAnnotationPresent(ExplicitProxyOnly.class);
 			Method[] ifcMethods = fromClass.getDeclaredMethods();
 			for (Method method : ifcMethods) {
 				int mods = method.getModifiers();
 				// Public and protected only
 				if (!Modifier.isPublic(mods) && !Modifier.isProtected(mods))
 					continue;
 				if (Modifier.isStatic(mods))
 					continue;
 				
 				method.setAccessible(true);// Short cut access controls validation
 				if (explicitOnly && !method.isAnnotationPresent(AlwaysProxy.class) && !method.isAnnotationPresent(com.zenesis.qx.remote.annotations.Method.class))
 					continue;
 
 				if (method.isAnnotationPresent(DoNotProxy.class)) {
 					doNotProxyMethods.add(new MethodSig(method));
 					continue;
 				}
 				
 				Method existing = methods.get(method.getName());
 				if (existing != null) {
 					// Method overloading is not possible
 					if (isConflicting(method, existing))
 						throw new IllegalArgumentException("Cannot create a proxy for " + clazz + 
 								" because it has overloaded method " + method + " first seen in " + existing);
 				}
 				Boolean canProxy = canProxy(fromClass, method);
 				if (canProxy == null)
 					canProxy = defaultProxy;
 				if (canProxy)
 					methods.put(method.getName(), method);
 			}
 		}
 		
 		/**
 		 * Removes all methods listed in the super type (because super type is already
 		 * defined)
 		 */
 		public void removeSuperTypeMethods() {
 			for (ProxyType tmpType = superType; tmpType != null; tmpType = tmpType.getSuperType()) {
 				for (ProxyMethod method : tmpType.getMethods())
 					methods.remove(method.getName());
 			}
 		}
 		
 		/**
 		 * Removes any methods which are property accessor methods
 		 * @param prop
 		 */
 		public void removePropertyAccessors(ProxyPropertyImpl prop) {
 			String upname = Character.toUpperCase(prop.getName().charAt(0)) + prop.getName().substring(1);
 			methods.remove("get" + upname);
 			methods.remove("set" + upname);
 			methods.remove("is" + upname);
 			if (prop.getSerializeMethod() != null)
 				methods.remove(prop.getSerializeMethod().getName());
 			if (prop.getDeserializeMethod() != null)
 				methods.remove(prop.getDeserializeMethod().getName());
 		}
 		
 		/**
 		 * Checks that the list of methods is valid, i.e. that there are no conflicts with DoNotProxy
 		 * @throws IllegalArgumentException
 		 */
 		public void checkValid() throws IllegalArgumentException {
 			for (MethodSig sig : doNotProxyMethods) {
 				Method method = methods.get(sig.method.getName());
 				if (method != null && hasSameSignature(method, sig.method)) {
 					throw new IllegalArgumentException("Cannot create a proxy for " + clazz + 
 							" because it has conflicting DoNotProxy for method " + method);
 				}
 			}
 		}
 		
 		/**
 		 * Converts the list of methods to a sorted array of ProxyMethods
 		 * @return
 		 */
 		public ProxyMethod[] toArray() {
 			ArrayList<ProxyMethod> proxyMethods = new ArrayList<ProxyMethod>();
 			for (Method method : methods.values())
 				proxyMethods.add(new ProxyMethod(method));
 			Collections.sort(proxyMethods, ProxyMethod.ALPHA_COMPARATOR);
 			
 			return proxyMethods.toArray(new ProxyMethod[proxyMethods.size()]);
 		}
 	}
 
 	// The class being represented
 	private final Class clazz;
 	
 	// Base ProxyType that represents the class that clazz is derived from
 	private final ProxyType superType;
 	
 	// Interfaces implements by clazz 
 	private final Set<ProxyType> interfaces;
 	
 	// Methods (not including property accessors)
 	private final ProxyMethod[] methods;
 	
 	// Properties
 	private final HashMap<String, ProxyProperty> properties;
 	
 	// Events
 	private final HashMap<String, ProxyEvent> events;
 	
 	/**
 	 * Constructor, used for defining interfaces which are to be proxied
 	 * @param className
 	 * @param methods
 	 */
 	public ProxyTypeImpl(ProxyType superType, Class clazz, Set<ProxyType> interfaces) {
 		super();
 		if (interfaces == null)
 			interfaces = Collections.EMPTY_SET;
 		this.superType = superType;
 		this.interfaces = interfaces;
 		this.clazz = clazz;
 
 		MethodsCompiler methodsCompiler = new MethodsCompiler();
 		
 		// Get a complete list of methods from the interfaces that the new class has to 
 		//	implement; we include methods marked as DoNotProxy so that we can check for 
 		//	conflicting instructions
 		if (!clazz.isInterface()) {
 			// Get a full list of the interfaces which our class has to implement
 			HashSet<ProxyType> allInterfaces = new HashSet<ProxyType>();
 			getAllInterfaces(allInterfaces, interfaces);
 
 			for (ProxyType ifcType : allInterfaces) {
 				try {
 					methodsCompiler.addMethods(Class.forName(ifcType.getClassName()), true);
 				}catch(ClassNotFoundException e) {
 					throw new IllegalStateException("Cannot find class " + ifcType.getClassName());
 				}
 			}
 		}
 		
		boolean defaultProxy = false;
		if (clazz.isInterface())
			defaultProxy = true;
		else {
			for (Class tmp = clazz; tmp != null; tmp = tmp.getSuperclass()) {
				if (tmp.isAnnotationPresent(AlwaysProxy.class)) {
					defaultProxy = true;
					break;
				} else if (tmp.isAnnotationPresent(ExplicitProxyOnly.class)) {
					break;
				}
			}
		}
		
 		// If the class does not have any proxied interfaces or the class is marked with
 		//	the AlwaysProxy annotation, then we take methods from the class definition
		methodsCompiler.addMethods(clazz, defaultProxy);
 			
 		methodsCompiler.checkValid();
 		methodsCompiler.removeSuperTypeMethods();
 		
 		// Load properties
 		HashMap<String, ProxyEvent> events = new HashMap<String, ProxyEvent>();
 		HashMap<String, ProxyProperty> properties = new HashMap<String, ProxyProperty>();
 		Properties annoProperties = (Properties)clazz.getAnnotation(Properties.class);
 		if (annoProperties != null) {
 			for (Property anno : annoProperties.value()) {
 				ProxyProperty property = new ProxyPropertyImpl(clazz, anno.value(), anno, annoProperties);
 				properties.put(property.getName(), property);
 				ProxyEvent event = property.getEvent();
 				if (event != null)
 					events.put(event.getName(), event);
 			}
 		}
 		for (Field field : clazz.getDeclaredFields()) {
 			Property anno = field.getAnnotation(Property.class);
 			if (anno != null) {
 				ProxyProperty property = new ProxyPropertyImpl(clazz, anno.value().length() > 0 ? anno.value() : field.getName(), anno, annoProperties);
 				properties.put(property.getName(), property);
 				ProxyEvent event = property.getEvent();
 				if (event != null)
 					events.put(event.getName(), event);
 			}
 		}
 		
 		for (Method method : clazz.getDeclaredMethods()) {
 			String name = method.getName();
 			if (name.length() < 4 || !name.startsWith("get") || !Character.isUpperCase(name.charAt(3)))
 				continue;
 			Property anno = method.getAnnotation(Property.class);
 			if (anno == null)
 				continue;
 
 			name = Character.toLowerCase(name.charAt(3)) + name.substring(4);
 			if (properties.containsKey(name))
 				continue;
 			
 			ProxyProperty property = new ProxyPropertyImpl(clazz, anno.value().length() > 0 ? anno.value() : name, anno, annoProperties);
 			properties.put(property.getName(), property);
 			ProxyEvent event = property.getEvent();
 			if (event != null)
 				events.put(event.getName(), event);
 		}
 		
 		// Classes need to have all inherited properties added
 		if (!clazz.isInterface()) {
 			for (ProxyType ifc : interfaces)
 				addProperties((ProxyTypeImpl)ifc, properties);
 		}
 		
 		// Remove property accessors
 		for (ProxyProperty prop : properties.values())
 			methodsCompiler.removePropertyAccessors((ProxyPropertyImpl)prop);
 		
 		// Load events
 		if (clazz.isAnnotationPresent(Events.class)) {
 			Events annoEvents = (Events)clazz.getAnnotation(Events.class);
 			for (Event annoEvent : annoEvents.value()) {
 				if (!events.containsKey(annoEvent.value()))
 					events.put(annoEvent.value(), new ProxyEvent(annoEvent));
 			}
 		}
 		
 		// Classes need to have all inherited events added
 		if (!clazz.isInterface()) {
 			for (ProxyType type : interfaces)
 				addEvents((ProxyTypeImpl)type, events);
 		}
 		
 		// Save
 		this.properties = properties.isEmpty() ? null : properties;
 		this.events = events.isEmpty() ? null : events;
 		this.methods = methodsCompiler.toArray();
 	}
 	
 	protected boolean isConflicting(Method method, Method existing) {
 		// The same method can appear more than once, but only if they are
 		//	identical - we just ignore it
 		if (existing != null) {
 			Class[] epts = existing.getParameterTypes();
 			Class[] mpts = method.getParameterTypes();
 			if (epts.length == mpts.length) {
 				for (int i = 0; i < epts.length; i++)
 					if (epts[i] != mpts[i])
 						return true;
 				return false;
 			}
 		}
 		
 		return true;
 	}
 	
 	protected static boolean hasSameSignature(Method method, Method existing) {
 		Class[] epts = existing.getParameterTypes();
 		Class[] mpts = method.getParameterTypes();
 		if (epts.length == mpts.length) {
 			for (int i = 0; i < epts.length; i++)
 				if (epts[i] != mpts[i])
 					return false;
 			return true;
 		}
 		
 		return false;
 	}
 	
 	protected Boolean canProxy(Class clazz, Method method) {
 		if (method.isAnnotationPresent(AlwaysProxy.class) || method.isAnnotationPresent(com.zenesis.qx.remote.annotations.Method.class))
 			return true;
 		for (Class tmp : clazz.getInterfaces())
 			if (Proxied.class.isAssignableFrom(tmp)) {
 				Boolean test = canProxy(tmp, method);
 				if (test != null)
 					return test;
 			}
 		Class superClazz = clazz.getSuperclass();
 		if (superClazz!= null && Proxied.class.isAssignableFrom(superClazz))
 			return canProxy(superClazz, method);
 		return null;
 	}
 	
 	/**
 	 * Recursively adds to addInterfaces to get a list of all interfaces which can be proxied.
 	 * @param allInterfaces
 	 * @param interfaces
 	 * @return
 	 */
 	protected HashSet<ProxyType> getAllInterfaces(HashSet<ProxyType> allInterfaces, Set<ProxyType> interfaces) {
 		for (ProxyType type : interfaces) {
 			allInterfaces.add(type);
 			getAllInterfaces(allInterfaces, type.getInterfaces());
 		}
 		return allInterfaces;
 	}
 	
 	/**
 	 * Detects whether the method is a property accessor
 	 * @param method
 	 * @return
 	 */
 	protected boolean isPropertyAccessor(Method method) {
 		String name = method.getName();
 		if (!name.startsWith("set") && !name.startsWith("get"))
 			return false;
 		name = Character.toLowerCase(name.charAt(3)) + name.substring(4);
 		return isProperty(name);
 	}
 	
 	@Override
 	public boolean isProperty(String name) {
 		if (properties != null && properties.containsKey(name))
 			return true;
 		if (interfaces != null)
 			for (ProxyType type : interfaces)
 				if (type.isProperty(name))
 					return true;
 		return false;
 	}
 	
 	/**
 	 * Adds all properties recursively
 	 * @param properties
 	 */
 	public void addProperties(ProxyTypeImpl proxyType, HashMap<String, ProxyProperty> properties) {
 		if (proxyType.properties != null)
 			for (ProxyProperty prop : proxyType.properties.values())
 				properties.put(prop.getName(), prop);
 		if (proxyType.interfaces != null)
 			for (ProxyType type : proxyType.interfaces)
 				addProperties((ProxyTypeImpl)type, properties);
 	}
 	
 	/**
 	 * Adds all events recursively
 	 * @param events
 	 */
 	public void addEvents(ProxyTypeImpl proxyType, HashMap<String, ProxyEvent> events) {
 		if (proxyType.events != null)
 			for (ProxyEvent event : proxyType.events.values())
 				events.put(event.getName(), event);
 		if (proxyType.interfaces != null)
 			for (ProxyType type : proxyType.interfaces)
 				addEvents((ProxyTypeImpl)type, events);
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.codehaus.jackson.map.JsonSerializable#serialize(org.codehaus.jackson.JsonGenerator, org.codehaus.jackson.map.SerializerProvider)
 	 */
 	@Override
 	public void serialize(JsonGenerator gen, SerializerProvider sp) throws IOException, JsonProcessingException {
 		serialize(gen, sp, null);
 	}
 	
 	/* (non-Javadoc)
 	 * @see com.fasterxml.jackson.databind.JsonSerializable#serializeWithType(com.fasterxml.jackson.core.JsonGenerator, com.fasterxml.jackson.databind.SerializerProvider, com.fasterxml.jackson.databind.jsontype.TypeSerializer)
 	 */
 	@Override
 	public void serializeWithType(JsonGenerator gen, SerializerProvider sp, TypeSerializer ts) throws IOException, JsonProcessingException {
 		serialize(gen, sp);
 	}
 
 	/**
 	 * @return the superType
 	 */
 	@Override
 	public ProxyType getSuperType() {
 		return superType;
 	}
 
 	@Override
 	public boolean isInterface() {
 		return clazz.isInterface();
 	}
 	
 	@Override
 	public String getClassName() {
 		return clazz.getName();
 	}
 
 	@Override
 	public Class getClazz() {
 		return clazz;
 	}
 
 	@Override
 	public ProxyMethod[] getMethods() {
 		return methods;
 	}
 
 	@Override
 	public Set<ProxyType> getInterfaces() {
 		return interfaces;
 	}
 	
 	@Override
 	public Map<String, ProxyProperty> getProperties() {
 		if (properties == null)
 			return Collections.EMPTY_MAP;
 		return properties;
 	}
 	
 	@Override
 	public Map<String, ProxyEvent> getEvents() {
 		if (events == null)
 			return Collections.EMPTY_MAP;
 		return events;
 	}
 	
 	@Override
 	public boolean supportsEvent(String eventName) {
 		if (events == null)
 			return false;
 		return events.containsKey(eventName);
 	}
 	
 	@Override
 	public ProxyEvent getEvent(String eventName) {
 		if (events == null)
 			return null;
 		return events.get(eventName);
 	}
 	
 	/* (non-Javadoc)
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	@Override
 	public boolean equals(Object obj) {
 		return obj instanceof ProxyType && ((ProxyTypeImpl)obj).getClazz() == clazz;
 	}
 
 	/* (non-Javadoc)
 	 * @see java.lang.Object#hashCode()
 	 */
 	@Override
 	public int hashCode() {
 		return clazz.hashCode();
 	}
 
 	/* (non-Javadoc)
 	 * @see java.lang.Object#toString()
 	 */
 	@Override
 	public String toString() {
 		return clazz.toString();
 	}
 }
