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
 package org.eclipse.riena.communication.core.factory;
 
 import java.lang.reflect.InvocationHandler;
 import java.lang.reflect.Method;
 import java.lang.reflect.Proxy;
 
 import org.eclipse.riena.communication.core.IRemoteServiceReference;
 import org.eclipse.riena.communication.core.IRemoteServiceRegistration;
 import org.eclipse.riena.communication.core.IRemoteServiceRegistry;
 import org.eclipse.riena.communication.core.RemoteServiceDescription;
 import org.eclipse.riena.core.RienaStatus;
 import org.eclipse.riena.core.injector.Inject;
 import org.eclipse.riena.core.util.VariableManagerUtil;
 import org.eclipse.riena.internal.communication.core.Activator;
 import org.eclipse.riena.internal.communication.core.factory.CallHooksProxy;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.equinox.log.Logger;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.InvalidSyntaxException;
 import org.osgi.framework.ServiceReference;
 import org.osgi.framework.ServiceRegistration;
 import org.osgi.service.log.LogService;
 
 /**
  * The IRemoteServiceFactory creates {@link IRemoteServiceReference} for given
  * service end point description. The IRemoteServiceReference holds a
  * serviceInstance reference instance to the service end point. The
  * RemoteService Factory can register an IRemoteServiceReference into the
  * {@link IRemoteServiceRegistry}. To create a {@link IRemoteServiceReference}
  * call {@link #createProxy(..)}. To create and register a "remote" OSGi Service
  * in one step call {@link #createAndRegisterProxy(..)}
  * <p>
  * This RemoteServiceFactory do not create IRemoteServiceReference itself. This
  * RemoteServiceFactory delegates the behavior to a protocol
  * {@link IRemoteServiceFactory} OSGi Service. This RemoteServiceFactory finds a
  * corresponding IRemoteServiceFactory by the filter
  * "riena.remote.protocol=[aProtocol]" (e.g. aProtcol replaced with 'hessian').
  * <p>
  * This RemoteServiceFactory does nothing if no protocol specific
  * IRemoteServiceFactory is available.
  * <p>
  * <b>NOTE</b><br>
  * The Riena communication bundle content includes generic class loading and
  * object instantiation or delegates this behavior to other Riena communication
  * bundles. Riena supports Eclipse-BuddyPolicy concept. For further information
  * about Riena class loading and instantiation please read /readme.txt.
  * 
  * @author Alexander Ziegler
  * @author Christian Campo
  * 
  */
 public class RemoteServiceFactory {
 
 	private BundleContext context;
 	private IRemoteServiceRegistry registry;
 	private static final String HOST_ID = RemoteServiceFactory.class.getName();
 	private final static Logger LOGGER = Activator.getDefault().getLogger(RemoteServiceFactory.class);
 
 	/**
 	 * Creates a RemoteServiceFactory instance with the default bundle context.
 	 * Prerequisite: application bundle should registered as
 	 * Eclipse-RegisterBuddy. Sample Manifest.mf of foo.myapplication.api:
 	 * 
 	 * Eclipse-RegisterBuddy: org.eclipse.riena.communication.core
 	 * 
 	 */
 	public RemoteServiceFactory() {
 		this(Activator.getDefault().getContext());
 	}
 
 	public void bind(IRemoteServiceRegistry registry) {
 		this.registry = registry;
 	}
 
 	public void unbind(IRemoteServiceRegistry registry) {
 		if (this.registry == registry) {
 			this.registry = null;
 		}
 	}
 
 	/**
 	 * Creates a RemoteServiceFactory instance with the given bundle context.
 	 * This bundle context should refer the service interface type.
 	 * 
 	 * @param context
 	 */
 	public RemoteServiceFactory(BundleContext context) {
 		this.context = context;
		Inject.service(IRemoteServiceRegistry.class.getName()).useRanking().into(this).andStart(context);
 	}
 
 	/**
 	 * Creates and registers a protocol specific remote service reference and
 	 * registers the reference into the {@link IRemoteServiceRegistry}. A
 	 * registered reference becomes automatically registered as "remote" OSGi
 	 * Service within the local OSGi container. Answers the registration object
 	 * for the reference. If no protocol specific {@link IRemoteServiceFactory}
 	 * OSGI Service available answers <code>null</code>.
 	 * 
 	 * @param interfaceClass
 	 * @param url
 	 * @param protocol
 	 * @return the registration object or <code>null</code>
 	 */
 	public IRemoteServiceRegistration createAndRegisterProxy(Class<?> interfaceClass, String url, String protocol) {
 		return createAndRegisterProxy(interfaceClass, url, protocol, HOST_ID);
 	}
 
 	/**
 	 * Creates and registers a protocol specific remote service reference and
 	 * registers the reference into the {@link IRemoteServiceRegistry}. A
 	 * registered reference becomes automatically registered as "remote" OSGi
 	 * Service within the local OSGi container. Answers the registration object
 	 * for the reference. If no protocol specific {@link IRemoteServiceFactory}
 	 * OSGI Service available answers <code>null</code>.<br>
 	 * <p>
 	 * The hostId identifies who is responsible for this remote service
 	 * registration
 	 * 
 	 * @param interfaceClass
 	 * @param url
 	 * @param protocol
 	 * @param hostId
 	 * @return the registration object or <code>null</code>
 	 */
 	public IRemoteServiceRegistration createAndRegisterProxy(Class<?> interfaceClass, String url, String protocol,
 			String hostId) {
 		RemoteServiceDescription rsd = createDescription(interfaceClass, url, protocol);
 		return createAndRegisterProxy(rsd);
 	}
 
 	/**
 	 * Creates and registers a protocol specific remote service reference and
 	 * registers the reference into the {@link IRemoteServiceRegistry}. A
 	 * registered reference becomes automatically registered as "remote" OSGi
 	 * Service within the local OSGi container. Answers the registration object
 	 * for the reference. If no protocol specific {@link IRemoteServiceFactory}
 	 * OSGI Service available answers <code>null</code>.
 	 * 
 	 * @param rsDesc
 	 * @return the registration object or <code>null</code>
 	 */
 	public IRemoteServiceRegistration createAndRegisterProxy(RemoteServiceDescription rsDesc) {
 		return createAndRegisterProxy(rsDesc, HOST_ID);
 	}
 
 	/**
 	 * Creates and registers a protocol specific remote service reference and
 	 * registers the reference into the {@link IRemoteServiceRegistry}. A
 	 * registered reference becomes automatically registered as "remote" OSGi
 	 * Service within the local OSGi container. Answers the registration object
 	 * for the reference. If no protocol specific {@link IRemoteServiceFactory}
 	 * OSGI Service available answers <code>null</code>.<br>
 	 * <p>
 	 * The hostId identifies who is responsible for this remote service
 	 * registration
 	 * 
 	 * @param rsDesc
 	 * @param hostId
 	 * @return the registration object or <code>null</code>
 	 */
 	public IRemoteServiceRegistration createAndRegisterProxy(RemoteServiceDescription rsDesc, String hostId) {
 		// create serviceInstance first
 		IRemoteServiceReference rsRef = createProxy(rsDesc);
 		if (rsRef == null) {
 			rsRef = createLazyProxy(rsDesc);
 		}
 		if (rsRef == null) {
 			LOGGER.log(LogService.LOG_ERROR,
 					"could not create serviceInstance (neither serviceInstance nor lazy serviceInstance) for " //$NON-NLS-1$
 							+ rsDesc);
 			return null;
 		}
 		rsRef.setHostId(hostId);
 		// register directly
 		if (registry != null) {
 			IRemoteServiceRegistration reg = registry.registerService(rsRef);
 			return reg;
 		}
 		return null;
 	}
 
 	/**
 	 * Creates a protocol specific serviceInstance reference
 	 * (IRemoteServcieRefernce) with given end point parameters. Answers the
 	 * IRemoteServiceReference. If no protocol specific
 	 * {@link IRemoteServiceFactory} OSGI Service available answers
 	 * <code>null</code>.
 	 * 
 	 * @param interfaceClass
 	 * @param url
 	 * @param protocol
 	 * @return the serviceInstance references or <code>null</code>
 	 */
 	public IRemoteServiceReference createProxy(Class<?> interfaceClass, String url, String protocol) {
 		RemoteServiceDescription rsd = createDescription(interfaceClass, url, protocol);
 		return createProxy(rsd);
 	}
 
 	private RemoteServiceDescription createDescription(Class<?> interfaceClass, String url, String protocol) {
 		RemoteServiceDescription rsd = new RemoteServiceDescription();
 		rsd.setServiceInterfaceClass(interfaceClass);
 		rsd.setServiceInterfaceClassName(interfaceClass.getName());
 		rsd.setURL(url);
 		rsd.setProtocol(protocol);
 		return rsd;
 	}
 
 	/**
 	 * Creates a protocol specific IRemoteServcieReference for the given end
 	 * point description. Answers the IRemoteServiceReference. If no end point
 	 * specific {@link IRemoteServiceFactory} OSGI Service available answers
 	 * <code>null</code>.
 	 * 
 	 * @param rsd
 	 * @return the serviceInstance references or <code>null</code>
 	 */
 	public IRemoteServiceReference createProxy(RemoteServiceDescription rsd) {
 		if (!RienaStatus.isActive()) {
 			LOGGER.log(LogService.LOG_WARNING, "riena.core is not started. This will probably not work."); //$NON-NLS-1$
 		}
 
 		try {
 			// Substitute any ${refs} within the url
 			rsd.setURL(VariableManagerUtil.substitute(rsd.getURL()));
 		} catch (CoreException e) {
 			LOGGER.log(LogService.LOG_ERROR, "Could not substitute url '" + rsd.getURL() + "'.", e); //$NON-NLS-1$ //$NON-NLS-2$
 			return null;
 		}
 		ServiceReference[] references;
 		if (rsd.getProtocol() == null) {
 			return null;
 		}
 		// find a factory for this specific protocol
 		String filter = "(" + IRemoteServiceFactory.PROP_PROTOCOL + "=" + rsd.getProtocol() + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 		try {
 			references = context.getServiceReferences(IRemoteServiceFactory.class.getName(), filter);
 		} catch (InvalidSyntaxException e) {
 			e.printStackTrace();
 			return null;
 		}
 		// no factory for this protocol
 		if (references == null) {
 			LOGGER.log(LogService.LOG_WARNING, "no IRemoteServiceFactory serviceRef available protocol [" //$NON-NLS-1$
 					+ rsd.getProtocol() + "]"); //$NON-NLS-1$
 			return null;
 		}
 
 		ServiceReference refFactory = null;
 		IRemoteServiceFactory factory = null;
 		for (ServiceReference reference : references) {
 			factory = (IRemoteServiceFactory) context.getService(reference);
 			if (factory != null) {
 				refFactory = reference;
 				break;
 			}
 		}
 
 		// could not get instance for existing reference
 		if (factory == null) {
 			LOGGER.log(LogService.LOG_WARNING, "no IRemoteServiceFactory service available protocol [" //$NON-NLS-1$
 					+ rsd.getProtocol() + "] id [" + rsd.getServiceInterfaceClassName() + "]"); //$NON-NLS-1$ //$NON-NLS-2$
 			return null;
 		}
 		LOGGER.log(LogService.LOG_INFO, "found protocol [" + rsd.getProtocol() + "] " + factory); //$NON-NLS-1$ //$NON-NLS-2$
 
 		// ask factory to create a serviceInstance for me, and intercept the
 		// calls with a
 		// CallHooksProxy instance
 		try {
 			IRemoteServiceReference rsr = factory.createProxy(rsd);
 			CallHooksProxy callHooksProxy = new CallHooksProxy(rsr.getServiceInstance());
 			callHooksProxy.setRemoteServiceDescription(rsd);
 			callHooksProxy.setMessageContextAccessor(factory.getMessageContextAccessor());
 			rsr.setServiceInstance(Proxy.newProxyInstance(rsd.getServiceInterfaceClass().getClassLoader(),
 					new Class[] { rsd.getServiceInterfaceClass() }, callHooksProxy));
 			return rsr;
 		} finally {
 			context.ungetService(refFactory);
 		}
 	}
 
 	private IRemoteServiceReference createLazyProxy(RemoteServiceDescription rsd) {
 		try {
 			LazyProxyHandler lazyProxyHandler = new LazyProxyHandler(rsd);
 			Class<?> serviceClass;
 			serviceClass = RemoteServiceFactory.class.getClassLoader().loadClass(rsd.getServiceInterfaceClassName());
 			Object serviceInstance = Proxy.newProxyInstance(this.getClass().getClassLoader(),
 					new Class[] { serviceClass }, lazyProxyHandler);
 			LazyRemoteServiceReference ref = new LazyRemoteServiceReference(serviceInstance, rsd
 					.getServiceInterfaceClassName(), rsd);
 
 			LazyProxyBuilder proxyBuilder = new LazyProxyBuilder(rsd, ref, lazyProxyHandler);
 			String filter = "(" + IRemoteServiceFactory.PROP_PROTOCOL + "=" + rsd.getProtocol() + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 			Inject.service(IRemoteServiceFactory.class.getName()).useFilter(filter).into(proxyBuilder).andStart(
 					Activator.getDefault().getContext());
 			return ref;
 		} catch (ClassNotFoundException e) {
 			LOGGER.log(LogService.LOG_ERROR, "Could not load service interface class '" //$NON-NLS-1$
 					+ rsd.getServiceInterfaceClassName() + "'.", e); //$NON-NLS-1$
 			return null;
 		}
 	}
 
 	/**
 	 * Load class types for the given intefaceClassName.
 	 * 
 	 * @param interfaceClassName
 	 * @return the class type for the given class Name
 	 * 
 	 * @throws ClassNotFoundException
 	 */
 	public Class<?> loadClass(String interfaceClassName) throws ClassNotFoundException {
 
 		return getClass().getClassLoader().loadClass(interfaceClassName);
 	}
 
 	public class LazyProxyBuilder {
 
 		private RemoteServiceDescription rsd;
 		private LazyRemoteServiceReference lazyRef;
 		private LazyProxyHandler lazyProxyHandler;
 
 		protected LazyProxyBuilder(RemoteServiceDescription rsd, LazyRemoteServiceReference ref,
 				LazyProxyHandler lazyProxyHandler) {
 			super();
 			this.rsd = rsd;
 			this.lazyRef = ref;
 			this.lazyProxyHandler = lazyProxyHandler;
 		}
 
 		public void bind(IRemoteServiceFactory factory) {
 			if (rsd != null) {
 				IRemoteServiceReference ref = createProxy(rsd);
 				Object proxyInstance = ref.getServiceInstance();
 				lazyProxyHandler.setDelegateHandler(Proxy.getInvocationHandler(proxyInstance));
 				lazyRef.setDelegateRef(ref);
 				rsd = null;
 			}
 
 		}
 
 		public void unbind(IRemoteServiceFactory factory) {
 
 		}
 	}
 
 	static class LazyProxyHandler implements InvocationHandler {
 
 		private InvocationHandler delegateHandler;
 		private RemoteServiceDescription rsd;
 
 		protected LazyProxyHandler(RemoteServiceDescription rsd) {
 			super();
 			this.rsd = rsd;
 		}
 
 		public void setDelegateHandler(InvocationHandler delegateHandler) {
 			this.delegateHandler = delegateHandler;
 		}
 
 		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
 			if (delegateHandler == null) {
 				throw new RuntimeException("LazyProxy: missing IRemoteServiceFactory to create proxy for " //$NON-NLS-1$
 						+ "protocol=" + rsd.getProtocol() + " url=" + rsd.getURL() + " interface=" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 						+ rsd.getServiceInterfaceClassName());
 			}
 
 			return delegateHandler.invoke(proxy, method, args);
 		}
 
 	}
 
 	static class LazyRemoteServiceReference implements IRemoteServiceReference {
 
 		private Object serviceInstance;
 		private String serviceClass;
 		private IRemoteServiceReference delegateReference;
 		private String tempHostId;
 		private RemoteServiceDescription rsd;
 		private ServiceRegistration serviceRegistration;
 
 		protected LazyRemoteServiceReference(Object serviceInstance, String serviceClass, RemoteServiceDescription rsd) {
 			super();
 			this.serviceInstance = serviceInstance;
 			this.serviceClass = serviceClass;
 			this.rsd = rsd;
 		}
 
 		public void setDelegateRef(IRemoteServiceReference delegateRef) {
 			this.delegateReference = delegateRef;
 			if (tempHostId != null) {
 				delegateRef.setHostId(tempHostId);
 			}
 		}
 
 		public void dispose() {
 			if (delegateReference != null) {
 				delegateReference.dispose();
 			}
 		}
 
 		public boolean equals(Object obj) {
 			if (delegateReference != null) {
 				return delegateReference.equals(obj);
 			} else {
 				return false;
 			}
 		}
 
 		public RemoteServiceDescription getDescription() {
 			if (delegateReference == null) {
 				return rsd;
 			}
 			return delegateReference.getDescription();
 		}
 
 		public String getHostId() {
 			if (delegateReference == null) {
 				return tempHostId;
 			} else {
 				return delegateReference.getHostId();
 			}
 		}
 
 		public Object getServiceInstance() {
 			if (delegateReference == null) {
 				return serviceInstance;
 			}
 			return delegateReference.getServiceInstance();
 		}
 
 		public String getServiceInterfaceClassName() {
 			if (delegateReference == null) {
 				return serviceClass;
 			}
 			return delegateReference.getServiceInterfaceClassName();
 		}
 
 		public ServiceRegistration getServiceRegistration() {
 			return serviceRegistration;
 		}
 
 		public String getURL() {
 			if (delegateReference != null) {
 				return delegateReference.getURL();
 			}
 			return null;
 		}
 
 		public int hashCode() {
 			if (delegateReference != null) {
 				return delegateReference.hashCode();
 			} else {
 				return this.getClass().hashCode();
 			}
 		}
 
 		public void setHostId(String hostId) {
 			if (delegateReference == null) {
 				tempHostId = hostId;
 			} else {
 				delegateReference.setHostId(hostId);
 			}
 		}
 
 		public void setServiceInstance(Object serviceInstance) {
 			if (delegateReference != null) {
 				delegateReference.setServiceInstance(serviceInstance);
 			} else {
 				throw new RuntimeException(
 						"trying to set serviceInstance for lazyRemoteServiceReference with no delegate");
 			}
 		}
 
 		public void setServiceRegistration(ServiceRegistration serviceRegistration) {
 			this.serviceRegistration = serviceRegistration;
 		}
 
 		public String toString() {
 			if (delegateReference != null) {
 				return delegateReference.toString();
 			}
 			return super.toString();
 		}
 
 	}
 }
