 /*******************************************************************************
  * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.runtime.services;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.expressions.EvaluationContext;
 import org.eclipse.core.expressions.EvaluationResult;
 import org.eclipse.core.expressions.Expression;
 import org.eclipse.core.expressions.ExpressionConverter;
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IExecutableExtension;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.tcf.te.runtime.nls.Messages;
 import org.eclipse.tcf.te.runtime.services.activator.CoreBundleActivator;
 import org.eclipse.tcf.te.runtime.services.interfaces.IService;
 
 /**
  * Abstract service manager implementation.
  */
 public abstract class AbstractServiceManager {
 
 	// Map for all contributed services stored by their respective unique id
 	private Map<String, ServiceProxy> services = new HashMap<String, ServiceProxy>();
 
 	/**
 	 * Proxy to provide lazy loading of contributing plug-ins.
 	 */
 	protected class ServiceProxy implements IExecutableExtension {
 		// Reference to the configuration element
 		private IConfigurationElement configElement = null;
 		// The id of the service contribution
 		public String id;
 		// The class implementing the service
 		public String clazz;
 		// The service instance
 		private IService service = null;
 		// The list of service types the service is implementing
 		private List<Class<? extends IService>> serviceTypes = new ArrayList<Class<? extends IService>>();
 		// The converted expression
 		private Expression expression;
 
 		/**
 		 * Constructor.
 		 */
 		protected ServiceProxy() {
 		}
 
 		/* (non-Javadoc)
 		 * @see org.eclipse.core.runtime.IExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement, java.lang.String, java.lang.Object)
 		 */
 		@Override
 		public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
 			Assert.isNotNull(config);
 			this.configElement = config;
 
 			// Initialize the id field by reading the <id> extension attribute.
 			// Throws an exception if the id is empty or null.
 			id = config.getAttribute("id"); //$NON-NLS-1$
 			if (id == null || (id != null && "".equals(id.trim()))) { //$NON-NLS-1$
 				throw new CoreException(new Status(IStatus.ERROR,
 										CoreBundleActivator.getUniqueIdentifier(),
 										NLS.bind(Messages.Extension_error_missingRequiredAttribute, "id", config.getContributor().getName()))); //$NON-NLS-1$
 			}
 
 			// Read the class attribute. If null, check for the class sub element
 			clazz = config.getAttribute("class"); //$NON-NLS-1$
 			if (clazz == null) {
 				IConfigurationElement[] children = config.getChildren("class"); //$NON-NLS-1$
 				// Single element definition assumed (see extension point schema)
 				if (children.length > 0) {
 					clazz = children[0].getAttribute("class"); //$NON-NLS-1$
 				}
 			}
 			if (clazz == null || (clazz != null && "".equals(clazz.trim()))) { //$NON-NLS-1$
 				throw new CoreException(new Status(IStatus.ERROR,
 										CoreBundleActivator.getUniqueIdentifier(),
 										NLS.bind(Messages.Extension_error_missingRequiredAttribute, "class", config.getContributor().getName()))); //$NON-NLS-1$
 			}
 
 			// Read the "enablement" sub element of the extension
 			IConfigurationElement[] children = configElement.getChildren("enablement"); //$NON-NLS-1$
 			// Only one "enablement" element is expected
 			if (children != null && children.length > 0) {
 				expression = ExpressionConverter.getDefault().perform(children[0]);
 			}
 		}
 
 		/**
 		 * Add a type to the proxy. Types are used unless the proxy is instantiated to provide lazy
 		 * loading of services. After instantiated, a service will be identified only by its type
 		 * and implementing or extending interfaces or super-types.
 		 *
 		 * @param serviceType The type to add.
 		 */
 		public void addType(Class<? extends IService> serviceType) {
 			Assert.isNotNull(serviceType);
 			if (service == null && serviceTypes != null && !serviceTypes.contains(serviceType)) {
 				serviceTypes.add(serviceType);
 			}
 		}
 
 		/**
 		 * Return the real service instance for this proxy.
 		 */
 		protected IService getService(boolean unique) {
 			if ((service == null || unique) && configElement != null) {
 				try {
 					// Create the service class instance via the configuration element
 					Object service = configElement.createExecutableExtension("class"); //$NON-NLS-1$
 					if (service instanceof IService) {
 						if (unique) {
 							return (IService)service;
 						}
 						else if (service instanceof IService) {
 							this.service = (IService)service;
 						}
 						else {
 							IStatus status = new Status(IStatus.ERROR, CoreBundleActivator.getUniqueIdentifier(), "Service '" + service.getClass().getName() + "' not of type IService."); //$NON-NLS-1$ //$NON-NLS-2$
 							Platform.getLog(CoreBundleActivator.getContext().getBundle()).log(status);
 						}
 					}
 				}
 				catch (CoreException e) {
 					IStatus status = new Status(IStatus.ERROR, CoreBundleActivator.getUniqueIdentifier(), "Cannot create service '" + clazz + "'.", e); //$NON-NLS-1$ //$NON-NLS-2$
 					Platform.getLog(CoreBundleActivator.getContext().getBundle()).log(status);
 				}
 				if (serviceTypes != null) {
 					serviceTypes.clear();
 				}
 				serviceTypes = null;
 			}
 			return service;
 		}
 
 		/**
 		 * Check whether this proxy holds a service that is suitable for the given type.
 		 *
 		 * @param serviceType The service type. Must not be <code>null</code>.
 		 * @return <code>True</code> if the proxy holds a suitable service, <code>false</code> otherwise.
 		 */
 		protected boolean isMatching(Class<? extends IService> serviceType) {
 			Assert.isNotNull(serviceType);
 
 			if (service != null) {
 				return serviceType.isInstance(service);
 			}
 			else if (configElement != null) {
 				if (serviceType.getClass().getName().equals(clazz)) {
 					return true;
 				}
 				for (Class<? extends IService> type : serviceTypes) {
 					if (type.equals(serviceType)) {
 						return true;
 					}
 				}
 			}
 			return false;
 		}
 
 		/**
 		 * Check whether this proxy holds a service that is suitable for the given type.
 		 *
 		 * @param serviceTypeName The service type name. Must not be <code>null</code>.
 		 * @return <code>True</code> if the proxy holds a suitable service, <code>false</code> otherwise.
 		 */
 		protected boolean isMatching(String serviceTypeName) {
 			Assert.isNotNull(serviceTypeName);
 
 			if (service != null) {
 				Class<?>[] interfaces = service.getClass().getInterfaces();
 				for (Class<?> interfaze : interfaces) {
 					if (serviceTypeName.equals(interfaze.getName())) {
 						return true;
 					}
 				}
 			}
 			else if (configElement != null) {
 				if (serviceTypeName.equals(clazz)) {
 					return true;
 				}
 				for (Class<? extends IService> type : serviceTypes) {
 					if (serviceTypeName.equals(type.getName())) {
 						return true;
 					}
 				}
 			}
 			return false;
 		}
 
 		/**
 		 * Returns if or if not the service contribution is enabled for the given service context.
 		 * <p>
 		 * If the given service context is <code>null</code>, only globally unbound services are
 		 * enabled.
 		 *
 		 * @param context The service context or <code>null</code>.
 		 * @return <code>True</code> if the service contribution is enabled for the given service
 		 *         context, <code>false</code> otherwise.
 		 */
 		protected boolean isEnabled(Object context) {
 			if (context == null) {
 				return getEnablement() == null;
 			}
 
 			Expression enablement = getEnablement();
 
 			// The service contribution is enabled by default if no expression is specified.
 			boolean enabled = enablement == null;
 
 			if (enablement != null) {
 				// Set the default variable to the service context.
 				EvaluationContext evalContext = new EvaluationContext(null, context);
 				// Allow plugin activation
 				evalContext.setAllowPluginActivation(true);
 				// Evaluate the expression
 				try {
 					enabled = enablement.evaluate(evalContext).equals(EvaluationResult.TRUE);
 				} catch (CoreException e) {
 					IStatus status = new Status(IStatus.ERROR, CoreBundleActivator.getUniqueIdentifier(), e.getLocalizedMessage(), e);
 					Platform.getLog(CoreBundleActivator.getContext().getBundle()).log(status);
 				}
 			}
 
 			return enabled;
 		}
 
 		/**
 		 * Returns the id of the service contribution.
 		 *
 		 * @return The service contribution id.
 		 */
 		protected String getId() {
 			return id;
 		}
 
 		/**
 		 * Returns the enablement expression.
 		 *
 		 * @return The enablement expression or <code>null</code>.
 		 */
 		protected Expression getEnablement() {
 			return expression;
 		}
 
 		public boolean equals(IService service) {
 			Assert.isNotNull(service);
 			return clazz.equals(service.getClass().getCanonicalName());
 		}
 
 		public boolean equals(ServiceProxy proxy) {
 			Assert.isNotNull(proxy);
 			return clazz.equals(proxy.clazz);
 		}
 	}
 
 	/**
 	 * Constructor.
 	 */
 	protected AbstractServiceManager() {
 		loadServices();
 	}
 
 	/**
 	 * Creates a new service proxy instance and initialize it.
 	 *
 	 * @param config The configuration element. Must not be <code>null</code>.
 	 * @return The new service proxy instance.
 	 */
 	protected ServiceProxy getServiceProxy(IConfigurationElement config) {
 		Assert.isNotNull(config);
 		ServiceProxy proxy = new ServiceProxy();
 		try {
 			proxy.setInitializationData(config, null, null);
 		} catch (CoreException e) {
 			if (Platform.inDebugMode()) {
 				Platform.getLog(CoreBundleActivator.getContext().getBundle()).log(e.getStatus());
 			}
 		}
 		return proxy;
 	}
 
 	/**
 	 * Get a service for the given service context that implements at least the needed service type.
 	 * <p>
 	 * If an interface type is given, the service with the highest implementation is returned. This
 	 * may result in a random selection depending on the extension registration order, especially
 	 * when a service interface is implemented two times in different hierarchy paths. If a class
 	 * type is given, if available, the service of exactly that class is returned. Otherwise the
 	 * highest implementation is returned.
 	 *
 	 * @param context The service context or <code>null</code>.
 	 * @param serviceType The service type the service should at least implement or extend.
 	 *
 	 * @return The service or <code>null</code>.
 	 */
 	public <V extends IService> V getService(Object context, Class<? extends V> serviceType) {
 		return getService(context, serviceType, false);
 	}
 
 	/**
 	 * Get a service for the given service context that implements at least the needed service type.
 	 * <p>
 	 * If an interface type is given, the service with the highest implementation is returned. This
 	 * may result in a random selection depending on the extension registration order, especially
 	 * when a service interface is implemented two times in different hierarchy paths. If a class
 	 * type is given, if available, the service of exactly that class is returned. Otherwise the
 	 * highest implementation is returned.
 	 *
 	 * @param context The service context or <code>null</code>.
 	 * @param serviceType The service type the service should at least implement or extend.
 	 * @param unique <code>true</code> if a new instance of the service is needed.
 	 *
 	 * @return The service or <code>null</code>.
 	 */
 	@SuppressWarnings("unchecked")
     public <V extends IService> V getService(Object context, Class<? extends V> serviceType, boolean unique) {
 		Assert.isNotNull(serviceType);
 
 		Collection<ServiceProxy> proxies = services.values();
 		if (proxies != null && !proxies.isEmpty()) {
 			List<ServiceProxy> candidates = new ArrayList<ServiceProxy>();
 			boolean isInterface = serviceType.isInterface();
 			for (ServiceProxy proxy : proxies) {
 				if (proxy.isMatching(serviceType) && proxy.isEnabled(context)) {
					if (!isInterface && proxy.equals(serviceType)) {
 						V service = (V)proxy.getService(unique);
 						service.setId(proxy.getId());
 						return service;
 					}
 					candidates.add(proxy);
 				}
 			}
 
 			V service = null;
 			if (!candidates.isEmpty()) {
 				service = (V)candidates.get(0).getService(unique);
 				service.setId(candidates.get(0).getId());
 			}
 
 			return service;
 		}
 
 		return null;
 	}
 
 	/**
 	 * Get a service list for the given service context that implements at least the needed service type.
 	 *
 	 * @param context The service context or <code>null</code>.
 	 * @param serviceType The service type the service should at least implement or extend.
 	 * @param unique <code>true</code> if a new instance of the service is needed.
 	 *
 	 * @return The service list or empty list.
 	 */
 	public  IService[] getServices(Object context, Class<? extends IService> serviceType, boolean unique) {
 		Assert.isNotNull(serviceType);
 
 		Collection<ServiceProxy> proxies = services.values();
 		List<IService> services = new ArrayList<IService>();
 		if (proxies != null && !proxies.isEmpty()) {
 			List<ServiceProxy> candidates = new ArrayList<ServiceProxy>();
 			for (ServiceProxy proxy : proxies) {
 				if (proxy.isMatching(serviceType) && proxy.isEnabled(context)) {
 					candidates.add(proxy);
 				}
 			}
 			for (ServiceProxy serviceProxy : candidates) {
 				IService service = serviceProxy.getService(unique);
 				service.setId(serviceProxy.getId());
 				services.add(service);
 			}
 		}
 		return services.toArray(new IService[services.size()]);
 	}
 
 	/**
 	 * Adds the given service to the service proxy map.
 	 */
 	protected void addService(ServiceProxy proxy) {
 		Assert.isNotNull(services);
 		Assert.isNotNull(proxy);
 
 		String id = proxy.getId();
 		Assert.isNotNull(id);
 		services.put(id, proxy);
 	}
 
 	/**
 	 * Returns if or if not a service contribution for the given service context, implementing the
 	 * given service type, exist.
 	 *
 	 * @param context The service context or <code>null</code>.
 	 * @param serviceTypeName The name of a service type the service should at least implement or extend.
 	 *
 	 * @return <code>True</code> if a matching service contribution exist, <code>false</code> otherwise.
 	 */
 	public boolean hasService(Object context, String serviceTypeName) {
 		Assert.isNotNull(serviceTypeName);
 
 		// Get all service contributions
 		Collection<ServiceProxy> proxies = services.values();
 		if (proxies != null && !proxies.isEmpty()) {
 			for (ServiceProxy proxy : proxies) {
 				if (proxy.isMatching(serviceTypeName) && proxy.isEnabled(context)) {
 					return true;
 				}
 			}
 		}
 
 		return false;
 	}
 
 
 	/**
 	 * Loads the contributed services into proxies (lazy loading!!) and adds them to this manager;
 	 */
 	protected abstract void loadServices();
 }
