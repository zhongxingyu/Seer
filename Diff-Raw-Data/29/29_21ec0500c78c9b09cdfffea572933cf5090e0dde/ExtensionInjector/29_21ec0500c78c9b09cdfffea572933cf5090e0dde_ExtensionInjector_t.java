 /*******************************************************************************
  * Copyright (c) 2007, 2010 compeople AG and others.
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
 import java.lang.reflect.Modifier;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.osgi.framework.BundleContext;
 import org.osgi.service.log.LogService;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.IExtension;
 import org.eclipse.core.runtime.IExtensionPoint;
 import org.eclipse.core.runtime.IExtensionRegistry;
 import org.eclipse.core.runtime.IRegistryEventListener;
 import org.eclipse.core.runtime.RegistryFactory;
 import org.eclipse.equinox.log.Logger;
 
 import org.eclipse.riena.core.Log4r;
 import org.eclipse.riena.core.injector.IStoppable;
 import org.eclipse.riena.core.injector.InjectionFailure;
 import org.eclipse.riena.core.util.WeakRef;
 import org.eclipse.riena.internal.core.injector.ObjectCounter;
 import org.eclipse.riena.internal.core.injector.extension.ExtensionMapper;
 
 /**
  * This is the extension injector.<br>
  * See {@link ExtensionDescriptor} for explanation and usage.
  */
 public class ExtensionInjector implements IStoppable {
 
 	/**
 	 * When this system property is set to {@code true} then the
 	 * {@code ExtensionInjector} will not wire created executable extension.
 	 */
 	public static final String RIENA_EXTENSIONS_DONOTWIRE_SYSTEM_PROPERTY = "riena.extensions.donotwire"; //$NON-NLS-1$
 
 	private final ExtensionDescriptor extensionDesc;
 	private final WeakRef<Object> targetRef;
 	private final Class<?> targetClass;
 	private final List<IRegistryEventListener> injectorListeners = new ArrayList<IRegistryEventListener>(2);
 
 	// private BundleContext context; // see comment in andStart()
 	private boolean started;
 	private boolean symbolReplace = true;
 	private boolean nonSpecific = true;
 	private String updateMethodName = DEFAULT_UPDATE_METHOD_NAME;
 	private Method updateMethod;
 	private boolean isArray;
 	private Class<?> componentType;
 	private boolean onceOnly;
 
 	private static final ObjectCounter<Method> ONCE_ONLY_METHODS = new ObjectCounter<Method>();
 
 	private static final String DEFAULT_UPDATE_METHOD_NAME = "update"; //$NON-NLS-1$
 	private static final Logger LOGGER = Log4r.getLogger(ExtensionInjector.class);
 
 	/**
 	 * @param extensionDesc
 	 * @param target
 	 */
 	ExtensionInjector(final ExtensionDescriptor extensionDesc, final Object target) {
 		this.extensionDesc = extensionDesc;
 		this.targetRef = new WeakRef<Object>(target, new Runnable() {
 			public void run() {
 				stop();
 			}
 		});
 		this.targetClass = target.getClass();
 	}
 
 	/**
 	 * Start the extension injector.
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
 		final Class<?> parameterType = updateMethod.getParameterTypes()[0];
 		isArray = parameterType.isArray();
 		// if the interface type is given explicitly it will be used; otherwise
 		// the formal parameter type of the update method will be used.
 		componentType = extensionDesc.getInterfaceType() != null ? extensionDesc.getInterfaceType()
 				: isArray ? parameterType.getComponentType() : parameterType;
 
 		onceOnly = Modifier.isStatic(updateMethod.getModifiers()) || onceOnly;
 		if (onceOnly && ONCE_ONLY_METHODS.incrementAndGetCount(updateMethod) > 1) {
 			// prevent another extension registry listener
 			return this;
 		}
 
 		// Fix the extension point descriptor
 		extensionDesc.getExtensionPointId().normalize(componentType);
 
 		populateInterfaceBeans(true);
 
 		final IExtensionRegistry extensionRegistry = RegistryFactory.getRegistry();
 		Assert.isLegal(extensionRegistry != null,
 				"For some reason the extension registry has not been created. Injecting extensions is not possible."); //$NON-NLS-1$
 		for (final String id : extensionDesc.getExtensionPointId().compatibleIds()) {
 			final IRegistryEventListener injectorListener = new InjectorListener();
 			injectorListeners.add(injectorListener);
 			extensionRegistry.addListener(injectorListener, id);
 		}
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
 	 * Defines that the 'update' method will only be called once for instances
 	 * of the same class. This can also be forced by declaring the 'update'
 	 * method static.
 	 * <p>
 	 * This can be used for instances that share configuration data to avoid
 	 * multiple injection of the same data. This reduces the amount of listeners
 	 * and memory footprint.
 	 * 
 	 * @return this
 	 */
 	public ExtensionInjector onceOnly() {
 		Assert.isTrue(!started, "ExtensionInjector already started."); //$NON-NLS-1$
 		onceOnly = true;
 		return this;
 	}
 
 	/**
 	 * Stop the extension injector.
 	 */
 	public void stop() {
 		if (!started) {
 			return;
 		}
 
 		if (onceOnly && ONCE_ONLY_METHODS.decrementAndGetCount(updateMethod) > 0) {
 			return;
 		}
 
 		final IExtensionRegistry extensionRegistry = RegistryFactory.getRegistry();
 		if (extensionRegistry == null) {
 			LOGGER.log(LogService.LOG_ERROR, "For some reason the extension registry has been gone!"); //$NON-NLS-1$
 		} else {
 			for (final IRegistryEventListener injectorListener : injectorListeners) {
 				extensionRegistry.removeListener(injectorListener);
 			}
 		}
 		injectorListeners.clear();
 
		update(emptyBeans());
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
 			} catch (final NoSuchMethodException e) {
 				// retry with array
 				return seekMatchingUpdateMethod(extensionDesc.getInterfaceType(), true);
 			}
 
 		} catch (final SecurityException e) {
 			throw new InjectionFailure("Could not find 'bind' method " + updateMethodName + "(" //$NON-NLS-1$ //$NON-NLS-2$
 					+ extensionDesc.getInterfaceType() + ").", e); //$NON-NLS-1$
 		} catch (final NoSuchMethodException e) {
 			throw new InjectionFailure("Could not find 'bind' method " + updateMethodName + "(" //$NON-NLS-1$ //$NON-NLS-2$
 					+ extensionDesc.getInterfaceType() + ").", e); //$NON-NLS-1$
 		}
 	}
 
 	private Method seekMatchingUpdateMethod(final Class<?> interfaceType, final boolean isArray)
 			throws NoSuchMethodException {
 		try {
 			final Class<?> seeking = isArray ? Array.newInstance(interfaceType, 0).getClass() : interfaceType;
 			return targetClass.getMethod(updateMethodName, seeking);
 		} catch (final NoSuchMethodException e) {
 			for (final Class<?> superInterfaceType : interfaceType.getInterfaces()) {
 				final Method attempt = seekMatchingUpdateMethod(superInterfaceType, isArray);
 				if (attempt != null) {
 					return attempt;
 				}
 			}
 		}
 		throw new NoSuchMethodError("In " + targetClass + " is no method matching " + updateMethodName //$NON-NLS-1$ //$NON-NLS-2$
 				+ "(" + interfaceType + (isArray ? "[]" : "") + " )"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
 	}
 
 	/**
 	 * Determine method from update method name and also determine interface
 	 * type (array?)
 	 * 
 	 * @return
 	 */
 	private Method findUpdateMethodForUnkownType() {
 		final List<Method> candidates = new ArrayList<Method>();
 		final Method[] methods = targetClass.getMethods();
 		for (final Method method : methods) {
 			if (method.getName().equals(updateMethodName) && method.getParameterTypes().length == 1
 					&& isExtensionInterface(method.getParameterTypes()[0])) {
 				candidates.add(method);
 			}
 		}
 
 		if (candidates.size() == 0) {
 			throw new InjectionFailure("No suitable 'bind' method found. Looking for method " + updateMethodName //$NON-NLS-1$
 					+ "(<someinterface>[]). someinterface must be annotated with @ExtensionInterface."); //$NON-NLS-1$
 		}
 
 		if (candidates.size() == 1) {
 			if (matchesExtensionPointConstraint(candidates.get(0).getParameterTypes()[0])) {
 				return candidates.get(0);
 			} else {
 				throw new InjectionFailure("Found method " + candidates.get(0) //$NON-NLS-1$
 						+ " does not match extension point constraints (e.g. requires an array type)."); //$NON-NLS-1$
 			}
 		}
 
 		if (candidates.size() > 2) {
 			throw new InjectionFailure("Too many (>2) candidates (" + candidates + ") for 'bind' method " //$NON-NLS-1$ //$NON-NLS-2$
 					+ updateMethodName + "."); //$NON-NLS-1$
 		}
 
 		if (matchesExtensionPointConstraint(candidates.get(0).getParameterTypes()[0])) {
 			return candidates.get(0);
 		}
 
 		if (matchesExtensionPointConstraint(candidates.get(1).getParameterTypes()[0])) {
 			return candidates.get(1);
 		}
 
 		throw new InjectionFailure("No suitable candidate from (" + candidates + ") found for 'bind' method " //$NON-NLS-1$ //$NON-NLS-2$
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
 
 	void populateInterfaceBeans(final boolean onStart) {
 		try {
 			final Object[] beans = ExtensionMapper.map(symbolReplace, extensionDesc, componentType, nonSpecific);
 			if (!matchesExtensionPointConstraint(beans.length)) {
 				LOGGER.log(LogService.LOG_ERROR, "Number of extensions " + beans.length //$NON-NLS-1$
 						+ " does not fulfill the extension point's " + extensionDesc.getExtensionPointId() //$NON-NLS-1$
 						+ " constraints."); //$NON-NLS-1$
 			}
 			if (isArray) {
				update(beans);
 			} else {
				update(beans.length > 0 ? beans[0] : null);
 			}
 		} catch (final InjectionFailure e) {
			update(emptyBeans());
			LOGGER.log(LogService.LOG_ERROR,
					"Failure injecting extension point " + extensionDesc.getExtensionPointId() + " into bean " //$NON-NLS-1$ //$NON-NLS-2$
							+ targetClass + " using method " + updateMethod + "."); //$NON-NLS-1$ //$NON-NLS-2$
			throw e;
 		}
 	}
 
	private Object emptyBeans() {
		return isArray ? Array.newInstance(componentType, 0) : null;
	}

	private void update(final Object beans) {
 		try {
 			final Object target = targetRef.get();
 			if (target != null) {
				updateMethod.invoke(target, new Object[] { beans });
 			}
 		} catch (final IllegalArgumentException e) {
 			throw new InjectionFailure("Calling 'bind' method " + updateMethod + " failed.", e); //$NON-NLS-1$ //$NON-NLS-2$
 		} catch (final IllegalAccessException e) {
 			throw new InjectionFailure("Calling 'bind' method " + updateMethod + " failed.", e); //$NON-NLS-1$ //$NON-NLS-2$
 		} catch (final InvocationTargetException e) {
 			throw new InjectionFailure("Calling 'bind' method " + updateMethod + " failed.", e //$NON-NLS-1$ //$NON-NLS-2$
 					.getTargetException());
 		}
 	}
 
 	private boolean matchesExtensionPointConstraint(final int occurence) {
 		return occurence >= extensionDesc.getMinOccurrences() && occurence <= extensionDesc.getMaxOccurrences();
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
 			//			System.out.println("InjectorListener.added()");
 			//			ExtensionRegistryAnalyzer.dumpRegistry(extensions[0].getExtensionPointUniqueIdentifier());
 			//			System.out.println("Added [" + Thread.currentThread().getName() + "] length:" + extensions.length + ":");
 			//			for (final IExtension extension : extensions) {
 			//				System.out.println("Extensions for " + extension.getExtensionPointUniqueIdentifier());
 			//				for (final IConfigurationElement element : extension.getConfigurationElements()) {
 			//					System.out.println("conf: " + Arrays.toString(element.getAttributeNames()));
 			//				}
 			//			}
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
 			// We don't care about other extension points. We only listen to the
 			// extensions for the id <code>extensionDesc</code>!
 		}
 
 	}
 
 }
