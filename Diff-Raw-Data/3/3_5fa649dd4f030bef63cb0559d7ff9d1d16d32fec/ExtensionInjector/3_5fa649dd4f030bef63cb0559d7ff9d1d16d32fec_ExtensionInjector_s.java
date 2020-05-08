 /*******************************************************************************
  * Copyright (c) 2007, 2008 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.core.injector.extension;
 
 import java.lang.reflect.Array;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.IExtension;
 import org.eclipse.core.runtime.IExtensionPoint;
 import org.eclipse.core.runtime.IExtensionRegistry;
 import org.eclipse.core.runtime.IRegistryEventListener;
 import org.eclipse.core.runtime.RegistryFactory;
 import org.eclipse.equinox.log.Logger;
 import org.eclipse.riena.internal.core.Activator;
 import org.osgi.framework.BundleContext;
 import org.osgi.service.log.LogService;
 
 /**
  * This is the extension injector.<br>
  * See {@link ExtensionDescriptor} for explanation and usage.
  */
 public class ExtensionInjector {
 
 	private final ExtensionDescriptor extensionDesc;
 	private final Object target;
 
 	// private BundleContext context; // see comment in andStart()
 	private boolean started;
 	private boolean symbolReplace = true;
 	private boolean nonSpecific = true;
 	private String updateMethodName = DEFAULT_UPDATE_METHOD_NAME;
 	private Method updateMethod;
 	private IRegistryEventListener injectorListener;
 	private boolean isArray;
 	private Class<?> componentType;
 
 	private final static String DEFAULT_UPDATE_METHOD_NAME = "update"; //$NON-NLS-1$
 	private final static Logger LOGGER = Activator.getDefault().getLogger(ExtensionInjector.class);
 
 	/**
 	 * @param extensionDesc
 	 * @param target
 	 */
 	ExtensionInjector(final ExtensionDescriptor extensionDesc, final Object target) {
 		this.extensionDesc = extensionDesc;
 		this.target = target;
 	}
 
 	/**
 	 * Start the extension injector.<br>
 	 * 
 	 * @param context
 	 * @return itself
 	 */
 	public ExtensionInjector andStart(final BundleContext context) {
 		Assert.isTrue(!started, "ExtensionInjector already started."); //$NON-NLS-1$
 		started = true;
 		// Currently not used (or better no longer used).
 		// However would like to keep the method signature so that it is in sync
 		// with the service injector.
 		// this.context = context;
 		updateMethod = findUpdateMethod();
 		Class<?> paramaterType = updateMethod.getParameterTypes()[0];
 		isArray = paramaterType.isArray();
 		// if the interface type is given explicitly it will be used; otherwise
 		// the formal parameter type of the update method will be used.
 		componentType = extensionDesc.getInterfaceType() != null ? extensionDesc.getInterfaceType()
 				: isArray ? paramaterType.getComponentType() : paramaterType;
 		populateInterfaceBeans(true);
 
 		final IExtensionRegistry extensionRegistry = RegistryFactory.getRegistry();
 		Assert.isLegal(extensionRegistry != null,
 				"For some reason the extension registry has not been created. Injecting extensions is not possible."); //$NON-NLS-1$
 		injectorListener = new InjectorListener();
 		extensionRegistry.addListener(injectorListener, extensionDesc.getExtensionPointId());
 		return this;
 	}
 
 	/**
 	 * Define the update method name.<br>
 	 * If not given 'update' will be assumed.
 	 * 
 	 * @param updateMethodName
 	 * @return itself
 	 */
 	public ExtensionInjector update(final String updateMethodName) {
 		Assert.isNotNull(updateMethodName, "Update method name must not be null"); //$NON-NLS-1$
 		Assert.isTrue(!started, "ExtensionInjector already started."); //$NON-NLS-1$
 		this.updateMethodName = updateMethodName;
 		return this;
 	}
 
 	/**
 	 * Explicitly force specific injection, i.e. the injected types reflect that
 	 * they are contributed from different extensions. Otherwise (which is the
 	 * default) it will not be differentiated.
 	 * 
 	 * @return itself
 	 */
 	public ExtensionInjector specific() {
 		Assert.isTrue(!started, "ExtensionInjector already started."); //$NON-NLS-1$
 		nonSpecific = false;
 		return this;
 	}
 
 	/**
 	 * Modify the values with ConfigurationPlugin.
 	 * 
 	 * @return itself
 	 */
 	public ExtensionInjector doNotReplaceSymbols() {
 		Assert.isTrue(!started, "ExtensionInjector already started."); //$NON-NLS-1$
 		symbolReplace = false;
 		return this;
 	}
 
 	/**
 	 * Stop the extension injector.
 	 */
 	public void stop() {
 		if (!started) {
 			return;
 		}
 
 		final IExtensionRegistry extensionRegistry = RegistryFactory.getRegistry();
 		if (extensionRegistry == null) {
 			LOGGER.log(LogService.LOG_ERROR, "For some reason the extension registry has been gone!"); //$NON-NLS-1$
 		} else {
 			extensionRegistry.removeListener(injectorListener);
 		}
 
 		// cleanup
		update(new Object[] { null });
 		injectorListener = null;
 	}
 
 	private Method findUpdateMethod() {
 		return extensionDesc.getInterfaceType() == null ? findUpdateMethodForUnkownType()
 				: findUpdateMethodForKownType();
 	}
 
 	/**
 	 * Determine method from update method name and known type
 	 * 
 	 * @return
 	 */
 	private Method findUpdateMethodForKownType() {
 		try {
 			if (extensionDesc.requiresArrayUpdateMethod()) {
 				return seekMatchingUpdateMethod(extensionDesc.getInterfaceType(), true);
 			}
 
 			try {
 				return seekMatchingUpdateMethod(extensionDesc.getInterfaceType(), false);
 			} catch (NoSuchMethodException e) {
 				// retry with array
 				return seekMatchingUpdateMethod(extensionDesc.getInterfaceType(), true);
 			}
 
 		} catch (SecurityException e) {
 			throw new IllegalStateException("Could not find 'bind' method " + updateMethodName + "(" //$NON-NLS-1$ //$NON-NLS-2$
 					+ extensionDesc.getInterfaceType() + ").", e); //$NON-NLS-1$
 		} catch (NoSuchMethodException e) {
 			throw new IllegalStateException("Could not find 'bind' method " + updateMethodName + "(" //$NON-NLS-1$ //$NON-NLS-2$
 					+ extensionDesc.getInterfaceType() + ").", e); //$NON-NLS-1$
 		}
 	}
 
 	private Method seekMatchingUpdateMethod(final Class<?> interfaceType, final boolean isArray)
 			throws NoSuchMethodException {
 		try {
 			final Class<?> seeking = isArray ? Array.newInstance(interfaceType, 0).getClass() : interfaceType;
 			return target.getClass().getMethod(updateMethodName, seeking);
 		} catch (NoSuchMethodException e) {
 			for (final Class<?> superInterfaceType : interfaceType.getInterfaces()) {
 				final Method attempt = seekMatchingUpdateMethod(superInterfaceType, isArray);
 				if (attempt != null) {
 					return attempt;
 				}
 			}
 		}
 		throw new NoSuchMethodError("In " + target.getClass() + " is no method matching " + updateMethodName //$NON-NLS-1$ //$NON-NLS-2$
 				+ "(" + interfaceType + " )"); //$NON-NLS-1$ //$NON-NLS-2$
 	}
 
 	/**
 	 * Determine method from update method name and also determine interface
 	 * type (array?)
 	 * 
 	 * @return
 	 */
 	private Method findUpdateMethodForUnkownType() {
 		final List<Method> candidates = new ArrayList<Method>();
 		final Method[] methods = target.getClass().getMethods();
 		for (final Method method : methods) {
 			if (method.getName().equals(updateMethodName) && method.getParameterTypes().length == 1
 					&& isExtensionInterface(method.getParameterTypes()[0])) {
 				candidates.add(method);
 			}
 		}
 
 		if (candidates.size() == 0) {
 			throw new IllegalStateException("No suitable 'bind' method found. Looking for method " + updateMethodName //$NON-NLS-1$
 					+ "(<someinterface>[]). someinterface must be annotated with @ExtensionInterface."); //$NON-NLS-1$
 		}
 
 		if (candidates.size() == 1) {
 			if (matchesExtensionPointConstraint(candidates.get(0).getParameterTypes()[0])) {
 				return candidates.get(0);
 			} else {
 				throw new IllegalStateException("Found method " + candidates.get(0) //$NON-NLS-1$
 						+ " does not match extension point constraints (e.g. requires an array type)."); //$NON-NLS-1$
 			}
 		}
 
 		if (candidates.size() > 2) {
 			throw new IllegalStateException("Too much (>2) candidates (" + candidates + ") for 'bind' method " //$NON-NLS-1$ //$NON-NLS-2$
 					+ updateMethodName + "."); //$NON-NLS-1$
 		}
 
 		if (matchesExtensionPointConstraint(candidates.get(0).getParameterTypes()[0])) {
 			return candidates.get(0);
 		}
 
 		if (matchesExtensionPointConstraint(candidates.get(1).getParameterTypes()[0])) {
 			return candidates.get(1);
 		}
 
 		throw new IllegalStateException("No suitable candidate from (" + candidates + ") found for 'bind' method " //$NON-NLS-1$ //$NON-NLS-2$
 				+ updateMethodName + "."); //$NON-NLS-1$
 	}
 
 	/**
 	 * @param type
 	 * @return
 	 */
 	private boolean isExtensionInterface(Class<?> type) {
 		type = type.isArray() ? type.getComponentType() : type;
 		return type.isInterface() && type.isAnnotationPresent(ExtensionInterface.class);
 	}
 
 	/**
 	 * @param type
 	 * @return
 	 */
 	private boolean matchesExtensionPointConstraint(final Class<?> type) {
 		return !extensionDesc.requiresArrayUpdateMethod() || type.isArray();
 	}
 
 	void populateInterfaceBeans(boolean onStart) {
 		try {
 			final Object[] beans = ExtensionMapper.map(symbolReplace, extensionDesc, componentType, nonSpecific);
 			if (!matchesExtensionPointConstraint(beans.length)) {
 				LOGGER.log(LogService.LOG_ERROR,
 						"Number of extensions does not fullfil the extension point's constraints."); //$NON-NLS-1$
 			}
 			if (isArray) {
 				update(new Object[] { beans });
 			} else {
 				update(new Object[] { beans.length > 0 ? beans[0] : null });
 			}
 		} catch (IllegalArgumentException e) {
 			if (onStart) {
 				throw e;
 			}
 			update(new Object[] { null });
 		}
 	}
 
 	private void update(Object[] params) {
 		try {
 			updateMethod.invoke(target, params);
 		} catch (IllegalArgumentException e) {
 			throw new IllegalStateException("Calling 'bind' method " + updateMethod + " fails.", e); //$NON-NLS-1$ //$NON-NLS-2$
 		} catch (IllegalAccessException e) {
 			throw new IllegalStateException("Calling 'bind' method " + updateMethod + " fails.", e); //$NON-NLS-1$ //$NON-NLS-2$
 		} catch (InvocationTargetException e) {
 			throw new IllegalStateException("Calling 'bind' method " + updateMethod + " fails.", e.getCause()); //$NON-NLS-1$ //$NON-NLS-2$
 		}
 	}
 
 	private boolean matchesExtensionPointConstraint(int occurence) {
 		return occurence >= extensionDesc.getMinOccurences() && occurence <= extensionDesc.getMaxOccurences();
 	}
 
 	/**
 	 * Listen to extension registry events.
 	 */
 	private class InjectorListener implements IRegistryEventListener {
 
 		/*
 		 * @see
 		 * org.eclipse.core.runtime.IRegistryEventListener#added(org.eclipse
 		 * .core. runtime.IExtension[])
 		 */
 		public void added(final IExtension[] extensions) {
 			populateInterfaceBeans(false);
 		}
 
 		/*
 		 * @see
 		 * org.eclipse.core.runtime.IRegistryEventListener#added(org.eclipse
 		 * .core. runtime.IExtensionPoint[])
 		 */
 		public void added(final IExtensionPoint[] extensionPoints) {
 			// We dont care about other extension points. We only listen to the
 			// extensions for the id <code>extensionDesc</code>!
 		}
 
 		/*
 		 * @see
 		 * org.eclipse.core.runtime.IRegistryEventListener#removed(org.eclipse
 		 * .core. runtime.IExtension[])
 		 */
 		public void removed(final IExtension[] extensions) {
 			populateInterfaceBeans(false);
 		}
 
 		/*
 		 * @see
 		 * org.eclipse.core.runtime.IRegistryEventListener#removed(org.eclipse
 		 * .core. runtime.IExtensionPoint[])
 		 */
 		public void removed(final IExtensionPoint[] extensionPoints) {
 			// We dont care about other extension points. We only listen to the
 			// extensions for the id <code>extensionDesc</code>!
 		}
 
 	}
 
 }
