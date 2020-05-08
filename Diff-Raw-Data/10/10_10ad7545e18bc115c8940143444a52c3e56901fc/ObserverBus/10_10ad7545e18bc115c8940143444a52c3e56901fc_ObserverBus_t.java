 /*******************************************************************************
  * Copyright (c) 2008-2011 Chair for Applied Software Engineering,
  * Technische Universitaet Muenchen.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  ******************************************************************************/
 package org.eclipse.emf.emfstore.common.observer;
 
 import java.lang.reflect.InvocationHandler;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.lang.reflect.Proxy;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.eclipse.emf.emfstore.common.extensionpoint.ExtensionElement;
 import org.eclipse.emf.emfstore.common.extensionpoint.ExtensionPoint;
 import org.eclipse.emf.emfstore.common.extensionpoint.ExtensionPointException;
 
 /**
  * This is a universal observer bus. This class follows the publish/subscribe pattern, it is a central dispatcher for
  * observers and makes use of generics in order to allow type safety. It can be used as singleton or be injected through
  * DI.
  * Observers have to implement the {@link IObserver} interface, which is only used as a marker. Future use of
  * Annotations is possible.
  * by using {@link #notify(Class)} (e.g. <code>bus.notify(MyObserver.class).myObserverMethod()</code>) all registered
  * Observers are notified.
  * This is implemented by using the java {@link Proxy} class. By calling {@link #notify(Class)} a proxy is returned,
  * which then calls all registered observers.
  * The proxy can also be casted into {@link ObserverCall}, which allows to access all results by the different
  * observers.
  * 
  * 
  * Example code:
  * 
  * <pre>
  * // A is IObserver
  * A a = new A() {
  * 
  * 	public void foo() {
  * 		System.out.println(&quot;A says: go!&quot;);
  * 	}
  * };
  * 
  * // B extends A and is IObserver
  * B b = new B() {
  * 
  * 	public void say(String ja) {
  * 		System.out.println(&quot;B says: &quot; + ja);
  * 	}
  * 
  * 	public void foo() {
  * 		System.out.println(&quot;B says: h??&quot;);
  * 	}
  * };
  * 
  * // B is registered first
  * ObserverBus.register(b);
  * ObserverBus.register(a);
  * 
  * ObserverBus.notify(A.class).foo();
  * 
  * ObserverBus.notify(B.class).say(&quot;w00t&quot;);
  * 
  * // Output:
  * 
  * // B says: h??
  * // A says: go!
  * //
  * // B says: w00t
  * 
  * </pre>
  * 
  * @author wesendon
  */
 public class ObserverBus {
 	
 	/**
 	 * Initializes the singleton instance statically.
 	 */
 	private static class SingletonHolder {
 		public static final ObserverBus INSTANCE = new ObserverBus();
 	}
 
 	/**
 	 * Static ObserverBus singleton. Use of singleton is optional, for that reason the constructor is public.
 	 * 
 	 * @return Static instance of the observerbus
 	 */
 	public static ObserverBus getInstance() {
 		return SingletonHolder.INSTANCE;
 	}
 	
 	private HashMap<Class<? extends IObserver>, List<IObserver>> observerMap;
 
 	/**
 	 * Default constructor.
 	 */
 	public ObserverBus() {
 		observerMap = new HashMap<Class<? extends IObserver>, List<IObserver>>();
 		collectionExtensionPoints();
 	}
 
 	/**
 	 * This method allows you to notify all observers.
 	 * 
 	 * @param <T> class of observer
 	 * @param clazz class of observer
 	 * @return call object
 	 */
 	public <T extends IObserver> T notify(Class<T> clazz) {
 		if (clazz == null) {
 			return null;
 		}
 		return (T) createProxy(clazz);
 	}
 
 	/**
 	 * Registers an observer for all observer interfaces implemented by the object or its super classes.
 	 * 
 	 * @param observer observer object
 	 */
 	public void register(IObserver observer) {
 		register(observer, getObserverInterfaces(observer));
 	}
 
 	/**
 	 * Registers an observer for the specified observer interfaces.
 	 * 
 	 * @param observer observer object
 	 * @param classes set of classes
 	 */
 	public void register(IObserver observer, Class<? extends IObserver>... classes) {
 		for (Class<? extends IObserver> iface : classes) {
 			if (iface.isInstance(observer)) {
 				addObserver(observer, iface);
 			}
 		}
 	}
 
 	/**
 	 * Unregisters an observer for all observer interfaces implemented by the object or its super classes.
 	 * 
 	 * @param observer observer object
 	 */
 	public void unregister(IObserver observer) {
 		unregister(observer, getObserverInterfaces(observer));
 	}
 
 	/**
 	 * Unregisters an observer for the specified observer interfaces.
 	 * 
 	 * @param observer observer object
 	 * @param classes set of classes
 	 */
 	public void unregister(IObserver observer, Class<? extends IObserver>... classes) {
 		for (Class<? extends IObserver> iface : classes) {
 			if (iface.isInstance(observer)) {
 				removeObserver(observer, iface);
 			}
 		}
 	}
 
 	private void addObserver(IObserver observer, Class<? extends IObserver> iface) {
 		List<IObserver> observers = initObserverList(iface);
 		observers.add(observer);
 	}
 
 	private void removeObserver(IObserver observer, Class<? extends IObserver> iface) {
 		List<IObserver> observers = initObserverList(iface);
 		observers.remove(observer);
 	}
 
 	private List<IObserver> initObserverList(Class<? extends IObserver> iface) {
 		List<IObserver> list = observerMap.get(iface);
 		if (list == null) {
 			list = new ArrayList<IObserver>();
 			observerMap.put(iface, list);
 		}
 		return list;
 	}
 
 	private List<IObserver> getObserverByClass(Class<IObserver> clazz) {
 		List<IObserver> list = observerMap.get(clazz);
 		if (list == null) {
 			list = Collections.emptyList();
 		}
 		return Collections.unmodifiableList(list);
 	}
 
 	@SuppressWarnings("unchecked")
 	private <T extends IObserver> T createProxy(Class<T> clazz) {
 		ProxyHandler handler = new ProxyHandler((Class<IObserver>) clazz);
 		return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[] { clazz, ObserverCall.class }, handler);
 
 	}
 
 	/**
 	 * Proxyobserver which notifies all observers.
 	 * 
 	 * @author wesendon
 	 */
 	private final class ProxyHandler implements InvocationHandler, ObserverCall {
 
 		private Class<IObserver> clazz;
 		private List<ObserverCall.Result> lastResults;
 
 		public ProxyHandler(Class<IObserver> clazz) {
 			this.clazz = clazz;
 			this.lastResults = new ArrayList<ObserverCall.Result>();
 		}
 
 		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
 			// fork for calls to ObserverCall.class
 			if (ObserverCall.class.equals(method.getDeclaringClass())) {
 				return accessObserverCall(method, args);
 			}
 
 			List<IObserver> observers = getObserverByClass(clazz);
 
 			// return default value if no observers are registered
 			if (observers.size() == 0) {
 				lastResults = new ArrayList<ObserverCall.Result>();
 				return Result.getDefaultValue(method);
 			}
 
 			lastResults = notifiyObservers(observers, method, args);
 			return lastResults.get(0).getResultOrDefaultValue();
 		}
 
 		private Object accessObserverCall(Method method, Object[] args) throws IllegalArgumentException,
 			IllegalAccessException, InvocationTargetException {
 			return method.invoke(this, args);
 
 		}
 
 		private List<ObserverCall.Result> notifiyObservers(List<IObserver> observers, Method method, Object[] args) {
 			List<ObserverCall.Result> results = new ArrayList<ObserverCall.Result>(observers.size());
 			for (IObserver observer : observers) {
 				try {
 					results.add(new Result(observer, method, method.invoke(observer, args)));
 				} catch (Throwable e) {
 					results.add(new Result(observer, e, method));
 				}
 			}
 			return results;
 		}
 
 		public List<Result> getObserverCallResults() {
 			return lastResults;
 		}
 
 		// END SUPRESS CATCH EXCEPTION
 	}
 
 	@SuppressWarnings("unchecked")
 	private Class<? extends IObserver>[] getObserverInterfaces(IObserver observer) {
 		HashSet<Class<? extends IObserver>> observerInterfacsFound = new HashSet<Class<? extends IObserver>>();
 		getClasses(observer.getClass(), observerInterfacsFound);
 		return observerInterfacsFound.toArray(new Class[observerInterfacsFound.size()]);
 	}
 
 	@SuppressWarnings("unchecked")
 	private boolean getClasses(Class<?> clazz, HashSet<Class<? extends IObserver>> result) {
 		for (Class<?> iface : getAllInterfaces(clazz, new HashSet<Class<?>>())) {
 			if (iface.equals(IObserver.class) && clazz.isInterface()) {
 				result.add((Class<? extends IObserver>) clazz);
 				return true;
 			} else {
 				if (getClasses(iface, result) && clazz.isInterface()) {
 					result.add((Class<? extends IObserver>) clazz);
 				}
 			}
 		}
 		return false;
 	}
 	
 	private Set<Class<?>> getAllInterfaces(final Class<?> clazz, 
 		final Set<Class<?>> interfacesFound) {
 		
 		for (Class<?> iface : clazz.getInterfaces()) {
 			interfacesFound.add((Class<?>) iface);
			interfacesFound.addAll(getAllInterfaces(iface, interfacesFound));
 		}
 		
 		if (clazz.getSuperclass() == null) {
 			return interfacesFound; 
 		}
 		
 		return getAllInterfaces(clazz.getSuperclass(), interfacesFound);
 	}
 
 	/**
 	 * Pulls observers from an extensionpoint and registers them.
 	 */
 	public void collectionExtensionPoints() {
 		for (ExtensionElement outer : new ExtensionPoint("org.eclipse.emf.emfstore.common.observer",true)
 			.getExtensionElements()) {
 			try {
 				for (ExtensionElement inner : new ExtensionPoint(outer.getAttribute("extensionPointName"),true)
 					.getExtensionElements()) {
 					register(inner.getClass(outer.getAttribute("observerAttributeName"), IObserver.class));
 				}
 			} catch (ExtensionPointException e) {
 			}
 		}
 	}
 }
