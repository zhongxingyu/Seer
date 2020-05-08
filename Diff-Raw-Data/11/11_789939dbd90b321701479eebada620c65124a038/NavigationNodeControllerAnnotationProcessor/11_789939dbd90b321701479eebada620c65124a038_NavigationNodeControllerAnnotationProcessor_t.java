 /*******************************************************************************
  * Copyright (c) 2007, 2011 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.navigation.annotation.processor;
 
 import java.lang.reflect.InvocationHandler;
 import java.lang.reflect.Method;
 import java.lang.reflect.Proxy;
 import java.util.HashMap;
 import java.util.Map;
import java.util.WeakHashMap;
 
 import org.eclipse.riena.core.singleton.SingletonProvider;
 import org.eclipse.riena.navigation.INavigationNodeController;
 import org.eclipse.riena.navigation.ISimpleNavigationNodeListener;
 import org.eclipse.riena.navigation.annotation.OnNavigationNodeEvent;
 
 /**
  * Annotation processor for {@code INavigationNodeController} annotations.
  * 
  * @since 3.0
  */
 public final class NavigationNodeControllerAnnotationProcessor {
 
 	private static final SingletonProvider<NavigationNodeControllerAnnotationProcessor> NNCAP = new SingletonProvider<NavigationNodeControllerAnnotationProcessor>(
 			NavigationNodeControllerAnnotationProcessor.class);
 
 	/**
 	 * Answer the singleton
 	 * <code>NavigationNodeControllerAnnotationProcessor</code>
 	 * 
 	 * @return the NavigationNodeControllerAnnotationProcessor singleton
 	 */
 	public static NavigationNodeControllerAnnotationProcessor getInstance() {
 		return NNCAP.getInstance();
 	}
 
	private final Map<INavigationNodeController, ?> alreadyProcessed = new WeakHashMap<INavigationNodeController, Object>();

 	private NavigationNodeControllerAnnotationProcessor() {
 		// singleton
 	}
 
 	/**
 	 * Process the {@link OnNavigationNodeEvent} annotations for the given
 	 * {@link INavigationNodeController}.
 	 * 
 	 * @param navigationNodeController
 	 *            the {@link INavigationNodeController} to process
 	 */
 	public void processAnnotations(final INavigationNodeController navigationNodeController) {
		synchronized (alreadyProcessed) {
			if (alreadyProcessed.containsKey(navigationNodeController)) {
				return;
			}
			alreadyProcessed.put(navigationNodeController, null);
		}
 		processAnnotations(navigationNodeController, navigationNodeController.getClass());
 	}
 
 	private void processAnnotations(final INavigationNodeController navigationNodeController, final Class<?> targetClazz) {
 		if (!INavigationNodeController.class.isAssignableFrom(targetClazz)) {
 			return;
 		}
 		processAnnotations(navigationNodeController, targetClazz.getSuperclass());
 
 		final Map<String, Method> events = getEventMethodMap(navigationNodeController, targetClazz);
 		if (!events.isEmpty()) {
 			navigationNodeController.getNavigationNode().addSimpleListener(
 					createListener(events, navigationNodeController));
 		}
 	}
 
 	private Map<String, Method> getEventMethodMap(final INavigationNodeController navigationNodeController,
 			final Class<?> targetClazz) {
 		final Map<String, Method> events = new HashMap<String, Method>();
 		for (final Method method : targetClazz.getDeclaredMethods()) {
 			final OnNavigationNodeEvent onNavigationNodeEvent = method.getAnnotation(OnNavigationNodeEvent.class);
 			if (onNavigationNodeEvent != null) {
 				if (!method.isAccessible()) {
 					method.setAccessible(true);
 				}
 				events.put(onNavigationNodeEvent.event().getMethodName(), method);
 			}
 		}
 		return events;
 	}
 
 	private ISimpleNavigationNodeListener createListener(final Map<String, Method> events,
 			final INavigationNodeController navigationNodeController) {
 		return (ISimpleNavigationNodeListener) Proxy.newProxyInstance(navigationNodeController.getClass()
 				.getClassLoader(), new Class[] { ISimpleNavigationNodeListener.class }, new ListenerHandler(events,
 				navigationNodeController));
 	}
 
 	private static class ListenerHandler implements InvocationHandler {
 
 		private final Map<String, Method> events;
 		private final INavigationNodeController navigationNodeController;
 
 		public ListenerHandler(final Map<String, Method> events,
 				final INavigationNodeController navigationNodeController) {
 			this.events = events;
 			this.navigationNodeController = navigationNodeController;
 		}
 
 		public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
 			final String methodName = method.getName();
 			if (method.getDeclaringClass() == Object.class) {
 				if (methodName.equals("hashCode")) { //$NON-NLS-1$
 					return System.identityHashCode(proxy);
 				} else if (methodName.equals("equals")) { //$NON-NLS-1$
 					return proxy == args[0];
 				} else if (methodName.equals("toString")) { //$NON-NLS-1$
 					return "Proxy for " + ISimpleNavigationNodeListener.class.getName() + " " + proxy.getClass().getName() + '@' //$NON-NLS-1$ //$NON-NLS-2$
 							+ Integer.toHexString(proxy.hashCode());
 				}
 			}
 
 			final Method targetMethod = events.get(methodName);
 			if (targetMethod != null) {
 				if (targetMethod.getParameterTypes().length == 0) {
 					return targetMethod.invoke(navigationNodeController);
 				} else {
 					return targetMethod.invoke(navigationNodeController, args);
 				}
 			}
 
 			return null;
 		}
 	}
 
 }
