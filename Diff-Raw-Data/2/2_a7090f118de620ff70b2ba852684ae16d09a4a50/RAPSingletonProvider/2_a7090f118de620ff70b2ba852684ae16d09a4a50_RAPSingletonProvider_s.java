 /*******************************************************************************
  * Copyright (c) 2007, 2013 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.internal.core.singleton;
 
 import java.lang.reflect.Method;
 import java.util.Map;
 import java.util.WeakHashMap;
 
 import org.osgi.framework.Bundle;
 
 import org.eclipse.riena.core.singleton.ISingletonInitializer;
 import org.eclipse.riena.core.singleton.SingletonFailure;
 import org.eclipse.riena.core.util.Nop;
 import org.eclipse.riena.core.util.RAPDetector;
 import org.eclipse.riena.core.wire.Wire;
 
 /**
  * Riena's wrapper for RAP's session based singleton provider. This wrapper
  * additionally wires a newly created singleton.
  * 
  * @since 4.0
  */
 public final class RAPSingletonProvider {
 
 	private static final boolean IS_AVAILABLE;
 	private static Class<?> sessionSingletonBaseClass;
 	private static Method getInstanceMethod;
 	private static final Map<Object, Boolean> WIRED_RAP_SINGLETONS = new WeakHashMap<Object, Boolean>();
 
	private static final String SESSION_SINGLETON_BASE = "org.eclipse.rwt.SessionSingletonBase"; //$NON-NLS-1$
 	private static final String GET_INSTANCE = "getInstance"; //$NON-NLS-1$
 
 	static {
 		IS_AVAILABLE = RAPDetector.isRAPavailable() && loadSessionSingletonBase();
 	}
 
 	private RAPSingletonProvider() {
 		Nop.reason("utility"); //$NON-NLS-1$
 	}
 
 	/**
 	 * Load the RAP {@code SessionSingletonBase} class and the
 	 * {@code getInstance} method as a side effect.
 	 * 
 	 * @return {@code true} if class and method have been found; otherwise
 	 *         {@code false}
 	 */
 	private static boolean loadSessionSingletonBase() {
 		final Bundle rapBundle = RAPDetector.getRWTBundle();
 		try {
 			sessionSingletonBaseClass = rapBundle.loadClass(SESSION_SINGLETON_BASE);
 			getInstanceMethod = sessionSingletonBaseClass.getMethod(GET_INSTANCE, Class.class);
 			return true;
 		} catch (final Exception e) {
 			// There seems to be no RAP available.
 			return false;
 		}
 	}
 
 	/**
 	 * Is RAP available and could RAP's singleton creation function be located
 	 * (reflection)?
 	 * 
 	 * @return true RAP is available and we can create RAP based singletons;
 	 *         otherwise not
 	 */
 	public static boolean isAvailable() {
 		return IS_AVAILABLE;
 	}
 
 	/**
 	 * Return the requested wired RAP session singleton.
 	 * 
 	 * @param singletonClass
 	 *            the class to create a RAP session based singleton
 	 * 
 	 * @return the singleton
 	 */
 	public static <S> S getInstance(final Class<S> singletonClass) {
 		return getInstance(singletonClass, null);
 	}
 
 	/**
 	 * Return the requested probably initialized and wired RAP session
 	 * singleton.
 	 * 
 	 * @param singletonClass
 	 *            the class to create a RAP session based singleton
 	 * @param initializer
 	 *            a optional initializer 'call back' (may be {@code null}
 	 * 
 	 * @return the singleton
 	 */
 	public static <S> S getInstance(final Class<S> singletonClass, final ISingletonInitializer<S> initializer) {
 		try {
 			final S rapSingleton = (S) getInstanceMethod.invoke(sessionSingletonBaseClass, singletonClass);
 			synchronized (WIRED_RAP_SINGLETONS) {
 				if (WIRED_RAP_SINGLETONS.put(rapSingleton, Boolean.TRUE) == null) {
 					if (initializer != null) {
 						initializer.init(rapSingleton);
 					}
 					Wire.instance(rapSingleton).andStart();
 				}
 			}
 			return rapSingleton;
 		} catch (final Exception e) {
 			throw new SingletonFailure("Could not instantiate RAP controlled singleton.", e); //$NON-NLS-1$
 		}
 	}
 
 }
