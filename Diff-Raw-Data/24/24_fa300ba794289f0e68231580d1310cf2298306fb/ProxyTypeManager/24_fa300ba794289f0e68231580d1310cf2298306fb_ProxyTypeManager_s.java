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
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 
 import com.zenesis.qx.event.EventManager;
 
 /**
  * This class is responsible for creating JSON proxy definitions for the client based
  * on reflection.
  * 
  * ProxyManager only generates proxy implementations for interfaces which extend
  * from Proxied - they don't have to derive directly, but they must derive at some
  * point.
  * 
  * Because Javascript does not support overloaded methods the interfaces cannot
  * have conflicting method names; if this is the case an exception will be thrown.
  * 
  * @author John Spackman
  *
  */
 public class ProxyTypeManager {
 	
 	@SuppressWarnings("unused")
 	private static final Logger log = Logger.getLogger(ProxyTypeManager.class);
 	
 	// Singleton instance
 	public static final ProxyTypeManager INSTANCE = new ProxyTypeManager();
 	
 	// Cache of known ProxyTypes
 	private final HashMap<Class, ProxyType> proxyTypes = new HashMap<Class, ProxyType>();
 	
 	// Cache of known type factories
 	private final HashMap<Class, ProxyTypeFactory> factories = new HashMap<Class, ProxyTypeFactory>();
 	
 	/**
 	 * Constructor; also creates a default EventManager if one has not been set yet
 	 */
 	public ProxyTypeManager() {
 		super();
 		if (EventManager.getInstance() == null)
 			new ProxyEventManager(this);
 	}
 
 	/**
 	 * Returns a ProxyType for a given class, caching the result for future use
 	 * @param clazz
 	 * @return
 	 */
 	public ProxyType getProxyType(Class<? extends Proxied> clazz) {
 		return getProxyType(clazz, null);
 	}
 	
 	/**
 	 * Registers a class for proxying; normal rules for registration apply if factory is null
 	 * but otherwise factory is allowed to include methods and classes that would normally be
 	 * excluded
 	 * @param clazz
 	 * @param factory
 	 * @return
 	 */
 	public ProxyType getProxyType(Class clazz, ProxyTypeFactory factory) {
 		ProxyType type = proxyTypes.get(clazz);
 		if (type != null)
 			return type;
 		
		if (clazz == Proxied.class)
			throw new IllegalArgumentException("Cannot create ProxyType for Proxied interface");
		
		// Get a list of ProxyTypes for interfaces that this class/interface implement directly;
		//	this will recursively discover other interfaces
 		HashSet<ProxyType> interfaces = new HashSet<ProxyType>();
		for (Class ifc : clazz.getInterfaces())
			if (ifc != Proxied.class && ifc != DynamicTypeProvider.class && Proxied.class.isAssignableFrom(ifc)) {
				ProxyType newType = getProxyType(ifc, factory);
				if (newType != null)
					interfaces.add(newType);
			}
 		
 		// If it's an interface then there is nothing more to do except create and store the ProxyType
 		if (clazz.isInterface()) {
 			// Poor inheritance structure may mean we've already created the ProxyType for this
 			//	interface
 			type = proxyTypes.get(clazz);
 			if (type == null) {
 				type = new ProxyTypeImpl(null, clazz, interfaces);
 				proxyTypes.put(clazz, type);
 			}
 			return type;
 		}
 		
 		// For a class, we need to get the supertype
 		ProxyType superType = null;
 		if (clazz.getSuperclass() != Object.class)
 			superType = getProxyType(clazz.getSuperclass(), factory);
 		
 		// Create the type
 		type = newProxyType(factory, superType, clazz, interfaces);
 		if (type != null)
 			proxyTypes.put(clazz, type);
 		return type;
 	}
 	
 	/**
 	 * Creates an instance of ProxyType; tries the factory first, then looks for annotations about the factory,
 	 * and falls back to default ProxyTypeImpl action.  This method is not expected to do anything other than
 	 * create the ProxyType because it is intended to be overridden if required.
 	 * @param factory the factory (can be null)
 	 * @param superType ProxyType for the superclass
 	 * @param clazz the class being instantiated
 	 * @param interfaces a list of ProxyType's for interfaces implemented by the clazz
 	 * @return
 	 */
 	protected ProxyType newProxyType(ProxyTypeFactory factory, ProxyType superType, Class clazz, Set<ProxyType> interfaces) {
 		ProxyType type = null;
 		
 		if (factory != null)
 			type = factory.newProxyType(superType, clazz, interfaces);
 		
 		if (type == null) {
 			com.zenesis.qx.remote.annotations.Proxied ann = getAnnotation(clazz, com.zenesis.qx.remote.annotations.Proxied.class);
 			if (ann != null && ann.factory() != ProxyTypeFactory.class) {
 				factory = getTypeFactory(ann.factory());
 				type = factory.newProxyType(superType, clazz, interfaces);
 			}
 		}
 		
 		if (type == null && Proxied.class.isAssignableFrom(clazz))
 			type = new ProxyTypeImpl(superType, clazz, interfaces);
 		
 		return type;
 	}	
 	
 	/**
 	 * Gets a ProxyTypeFactory instance from the cache, creating one if necessary
 	 * @param clazz
 	 * @return
 	 */
 	public ProxyTypeFactory getTypeFactory(Class<? extends ProxyTypeFactory> clazz) {
 		if (clazz == null)
 			return null;
 		ProxyTypeFactory factory = factories.get(clazz);
 		if (factory == null) {
 			try {
 				factory = clazz.newInstance();
 			} catch(IllegalAccessException e) {
 				throw new IllegalArgumentException("Cannot create factory " + clazz + ": " + e.getClass() + ": " + e.getMessage());
 			} catch(InstantiationException e) {
 				throw new IllegalArgumentException("Cannot create factory " + clazz + ": " + e.getClass() + ": " + e.getMessage());
 			}
 			factories.put(clazz, factory);
 		}
 		
 		return factory;
 	}
 
 	/**
 	 * Tiny helper method to get an annotation (uses generics to reduce the text typed in code)
 	 * @param <T>
 	 * @param clazz
 	 * @param annotationClass
 	 * @return
 	 */
 	private <T> T getAnnotation(Class clazz, Class<T> annotationClass) {
 		return (T)clazz.getAnnotation(annotationClass);
 	}
 	
 }
