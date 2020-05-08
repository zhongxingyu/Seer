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
 package org.eclipse.riena.communication.core;
 
import static org.eclipse.riena.communication.core.publisher.RSDPublisherProperties.*;

 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.osgi.framework.Bundle;
 import org.osgi.framework.ServiceReference;
 
 import org.eclipse.core.runtime.CoreException;
 
 import org.eclipse.riena.communication.core.factory.IRemoteServiceFactory;
 import org.eclipse.riena.communication.core.publisher.IServicePublishEventDispatcher;
 import org.eclipse.riena.communication.core.publisher.IServicePublisher;
 import org.eclipse.riena.communication.core.util.CommunicationUtil;
 import org.eclipse.riena.core.util.VariableManagerUtil;
 
 /**
  * The RemoteServiceDescription describes an remote service end point. Usually
  * an service end point describes a published OSGi Service within a server
  * container.
  * <p>
  * For example Riena communication has defined an "remote" OSGi service
  * {@link IServicePublishEventDispatcher} which provides a list of all published
  * OSGi Services within a server container
  * <p>
  * Usually a RemoteServiceDescription becomes instantiated within an server
  * container and is used by an IServicePublisher within an server and by an
  * {@link IRemoteServiceFactory} within a client container
  * 
  * @see IServicePublishEventDispatcher
  * @see IServicePublisher
  * @see IRemoteServiceFactory
  */
 public class RemoteServiceDescription {
 
 	public enum State {
 		/**
 		 * The "remote" OSGi Service was registered
 		 */
 		REGISTERED,
 		/**
		 * The "remote" OSGi Service was modified
		 * 
		 * @deprecated no longer be used
		 */
		@Deprecated
		MODIFIED,
		/**
 		 * The "remote" OSGi Service was unregistered
 		 */
 		UNREGISTERED
 	};
 
 	private final Class<?> serviceInterfaceClass;
 	private final Bundle serviceBundle;
 	private final String serviceInterfaceClassName;
 	private final String protocol;
 	private final ServiceReference serviceRef;
 	private final String path;
 	private final ClassLoader serviceClassLoader;
 
 	private State state = State.REGISTERED;
 	private Object service;
 	private String url;
 
 	/**
 	 * Create an instance of a RemoteServiceDescription with the given
 	 * parameters.
 	 * 
 	 * @param interfaceClass
 	 * @param url
 	 * @param protocol
 	 * @param bundle
 	 * @since 3.0
 	 */
 	public RemoteServiceDescription(final Class<?> interfaceClass, final String url, final String protocol,
 			final Bundle bundle) {
 		this.serviceInterfaceClass = interfaceClass;
 		this.serviceInterfaceClassName = interfaceClass.getName();
 		this.url = url;
 		this.protocol = protocol;
 		this.serviceBundle = bundle;
 		// not needed in this context
 		this.path = null;
 		this.serviceRef = null;
 		this.serviceClassLoader = new ServiceClassLoader(serviceBundle);
 	}
 
 	/**
 	 * Create an instance of a RemoteServiceDescription with the given
 	 * parameters.
 	 * 
 	 * @param interfaceName
 	 * @param serviceRef
 	 * @param path
 	 * @param protocol
 	 * @throws ClassNotFoundException
 	 * @since 3.0
 	 */
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	public RemoteServiceDescription(final String interfaceName, final ServiceReference serviceRef, final String path,
 			final String protocol) throws ClassNotFoundException {
 		this.serviceInterfaceClassName = interfaceName;
 		this.serviceRef = serviceRef;
 		this.serviceBundle = serviceRef.getBundle();
 		this.service = serviceBundle.getBundleContext().getService(serviceRef);
		this.path = CommunicationUtil.accessProperty(serviceRef.getProperty(PROP_REMOTE_PATH), path);
		this.protocol = CommunicationUtil.accessProperty(serviceRef.getProperty(PROP_REMOTE_PROTOCOL), protocol);
 		this.serviceInterfaceClass = serviceBundle.loadClass(interfaceName);
 		this.serviceClassLoader = new ServiceClassLoader(serviceBundle);
 	}
 
 	/**
 	 * @since 3.0
 	 */
 	public ClassLoader getServiceClassLoader() {
 		return serviceClassLoader;
 	}
 
 	/**
 	 * Disposes this RemoteServiceDescription. release all references.
 	 */
 	public void dispose() {
 		service = null;
 		url = null;
 		state = State.UNREGISTERED;
 	}
 
 	/**
 	 * Get the bundle this {@code RemoteServiceDescription} uses for class
 	 * resolution.
 	 * 
 	 * @return the bundle
 	 * 
 	 * @since 3.0
 	 */
 	public Bundle getBundle() {
 		return serviceBundle;
 	}
 
 	/**
 	 * 
 	 * @return the content path where the service end point is published
 	 */
 	public String getPath() {
 		return path;
 	}
 
 	/**
 	 * 
 	 * @return the protocol under the service end point is available
 	 */
 	public String getProtocol() {
 		return protocol;
 	}
 
 	/**
 	 * 
 	 * @return the service i.e. the OSGi Service
 	 */
 	public Object getService() {
 		return service;
 	}
 
 	/**
 	 * 
 	 * @return the service interface of the service
 	 * @throws ClassNotFoundException
 	 */
 	public Class<?> getServiceInterfaceClass() {
 		return serviceInterfaceClass;
 	}
 
 	/**
 	 * 
 	 * @return the service interface name of the service
 	 */
 	public String getServiceInterfaceClassName() {
 		return serviceInterfaceClassName;
 	}
 
 	/**
 	 * 
 	 * @return the service reference for the service
 	 */
 	public ServiceReference getServiceRef() {
 		return serviceRef;
 	}
 
 	/**
 	 * The state of the service. Values are:<br>
 	 * <br>
 	 * {@link #REGISTERED}<br>
	 * {@link #MODIFIED}<br>
 	 * {@link #UNREGISTERED}<br>
 	 * 
 	 * @return the current state of the service
 	 */
 	public State getState() {
 		return state;
 	}
 
 	/**
 	 * 
 	 * @return the absolute URL the the service end point is published
 	 */
 	public String getURL() {
 		try {
 			return VariableManagerUtil.substitute(url);
 		} catch (final CoreException e) {
 			return url;
 		}
 	}
 
 	/**
 	 * Sets the service i.e. the OSGi Service
 	 * 
 	 * @param service
 	 */
 	public void setService(final Object service) {
 		this.service = service;
 		// TODO enable this later
 		//		assertServiceConstraints();
 	}
 
 	/**
 	 * Sets the absolute URL the the service end point is published
 	 * 
 	 * @param url
 	 */
 	public void setURL(final String url) {
 		this.url = url;
 	}
 
 	@Override
 	public String toString() {
 		return "RemoteServiceDescription [serviceInterfaceClassName=" + serviceInterfaceClassName + ", protocol=" //$NON-NLS-1$ //$NON-NLS-2$
 				+ protocol + ", url=" + url + "]"; //$NON-NLS-1$ //$NON-NLS-2$
 	}
 
 	// TODO remove @Suppresswarnings when this becomes activated
 	@SuppressWarnings("unused")
 	private void assertServiceConstraints() {
 		// Do not allow multiple occurrences of the same service method names, i.e. no overloading
 		if (serviceInterfaceClass != null) {
 			final Method[] methods = serviceInterfaceClass.getMethods();
 			final Set<String> names = new HashSet<String>();
 			for (final Method method : methods) {
 				if (!Modifier.isVolatile(method.getModifiers()) && !names.add(method.getName())) {
 					throw new RemoteFailure("Can not use interface " + serviceInterfaceClass.getName() //$NON-NLS-1$
 							+ " as service interface because at least one method (" + method.getName() //$NON-NLS-1$
 							+ ") is overloaded, i.e. methods exists with the same name but different signatures."); //$NON-NLS-1$
 				}
 			}
 		}
 		// Check whether service instance is compatible to service class
 		if (service != null && serviceInterfaceClass != null) {
 			if (!serviceInterfaceClass.isAssignableFrom(service.getClass())) {
 				throw new RemoteFailure("Service class " + service.getClass().getName() //$NON-NLS-1$
 						+ " and service interface class " + serviceInterfaceClass.getName() //$NON-NLS-1$
 						+ " do not match, i.e. service can not be assigend to service interface."); //$NON-NLS-1$
 			}
 		}
 	}
 
 }
