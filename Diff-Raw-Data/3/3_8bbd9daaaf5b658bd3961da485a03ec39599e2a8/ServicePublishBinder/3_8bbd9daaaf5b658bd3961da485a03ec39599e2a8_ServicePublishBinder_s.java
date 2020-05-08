 /*******************************************************************************
  * Copyright (c) 2007, 2009 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.communication.publisher;
 
 import java.lang.reflect.Proxy;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.riena.communication.core.RemoteServiceDescription;
 import org.eclipse.riena.communication.core.publisher.IServicePublishBinder;
 import org.eclipse.riena.communication.core.publisher.IServicePublisher;
 import org.eclipse.riena.internal.communication.publisher.Activator;
 import org.eclipse.riena.internal.communication.publisher.ServiceHooksProxy;
 
 import org.eclipse.equinox.log.Logger;
 import org.osgi.framework.Constants;
 import org.osgi.framework.ServiceReference;
 import org.osgi.service.log.LogService;
 
 /**
  * The class publishes all services that are existing as life services in the
  * OSGi Registry as web service endpoints.
  * 
  */
 public class ServicePublishBinder implements IServicePublishBinder {
 
 	/**
 	 * contains a map of available publishers per protocol
 	 */
 	private Map<String, IServicePublisher> servicePublishers = new HashMap<String, IServicePublisher>();
 	/**
 	 * contains services that are not yet published (due to missing publishers)
 	 */
 	private List<RemoteServiceDescription> unpublishedServices = new ArrayList<RemoteServiceDescription>();
 	/**
 	 * contains registered published Services
 	 */
 	private Map<String, RemoteServiceDescription> rsDescs = new HashMap<String, RemoteServiceDescription>();
 
 	private final static Logger LOGGER = Activator.getDefault().getLogger(ServicePublishBinder.class);
 
 	public ServicePublishBinder() {
 		super();
 	}
 
 	public void bind(IServicePublisher publisher) {
 		servicePublishers.put(publisher.getProtocol(), publisher);
 		if (unpublishedServices.size() > 0) {
 			LOGGER.log(LogService.LOG_DEBUG, "servicePublish=" + publisher.getProtocol() //$NON-NLS-1$
 					+ " REGISTER...publishing all services that were waiting for him"); //$NON-NLS-1$
 		} else {
 			LOGGER.log(LogService.LOG_DEBUG, "servicePublish=" + publisher.getProtocol() //$NON-NLS-1$
 					+ " REGISTER...no unpublished services waiting for this protocol"); //$NON-NLS-1$
 
 		}
 
 		// check for services which are missing a publisher
 		checkUnpublishedServices(publisher.getProtocol());
 	}
 
 	public void unbind(IServicePublisher publisher) {
 		String protocol = publisher.getProtocol();
 		LOGGER.log(LogService.LOG_DEBUG, "servicePublish=" + publisher.getProtocol() //$NON-NLS-1$
 				+ " UNREGISTER...unpublishing all its services"); //$NON-NLS-1$
 		// unregister all web services for this type
 
 		// for (RemoteServiceDescription rsDesc : rsDescs.values()) {
 		// if (protocol.equals(rsDesc.getProtocol())) {
 		// unpublish(rsDesc);
 		// }
 		// }
 		servicePublishers.remove(protocol);
 	}
 
 	private void checkUnpublishedServices(String protocol) {
 		List<RemoteServiceDescription> removedItems = new ArrayList<RemoteServiceDescription>();
 		for (RemoteServiceDescription rsd : unpublishedServices) {
 			if (rsd.getProtocol().equals(protocol)) {
 				publish(rsd);
 				removedItems.add(rsd);
 			}
 		}
 		for (RemoteServiceDescription item : removedItems) {
 			unpublishedServices.remove(item);
 		}
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.riena.communication.publisher.IServicePublishBinder#publish
 	 * (org.osgi.framework.ServiceReference, java.lang.String, java.lang.String)
 	 */
 	public void publish(ServiceReference ref, String url, String protocol) {
 		String[] interfaces = (String[]) ref.getProperty(Constants.OBJECTCLASS);
 		assert interfaces.length == 1 : "OSGi service registrations only with one interface supported"; //$NON-NLS-1$
 		String interfaceName = interfaces[0];
 		publish(interfaceName, ref, url, protocol);
 	}
 
 	public void unpublish(ServiceReference serviceRef) {
 		for (RemoteServiceDescription rsd : rsDescs.values()) {
 			if (serviceRef.equals(rsd.getServiceRef())) {
 				IServicePublisher servicePublisher = servicePublishers.get(rsd.getProtocol());
 				if (servicePublisher != null) {
 					servicePublisher.unpublishService(rsd);
 				}
 				rsDescs.remove(rsd.getProtocol() + "::" + rsd.getPath()); //$NON-NLS-1$
 				return;
 			}
 		}
 	}
 
 	public void publish(String interfaceName, ServiceReference serviceRef, String path, String protocol) {
 		RemoteServiceDescription rsd;
 		try {
 			Class<?> interfaceClazz = serviceRef.getBundle().loadClass(interfaceName);
 			rsd = new RemoteServiceDescription(serviceRef, Activator.getDefault().getContext().getService(serviceRef),
 					interfaceClazz);
 			rsd.setService(Activator.getDefault().getContext().getService(serviceRef));
 			rsd.setPath(path);
 			rsd.setProtocol(protocol);
 			publish(rsd);
 		} catch (ClassNotFoundException e) {
 			LOGGER.log(LogService.LOG_WARNING,
 					"Could not load class for remote service interface for service reference" + serviceRef, e); //$NON-NLS-1$
 		}
 	}
 
 	private void publish(RemoteServiceDescription rsd) {
 		synchronized (rsDescs) {
 
 			ServiceHooksProxy handler = new ServiceHooksProxy(rsd.getService());
 			Object service = Proxy.newProxyInstance(rsd.getServiceInterfaceClass().getClassLoader(), new Class[] { rsd
 					.getServiceInterfaceClass() }, handler);
			rsd.setService(service);
 			handler.setRemoteServiceDescription(rsd);
 			RemoteServiceDescription rsDescFound = rsDescs.get(rsd.getProtocol() + "::" + rsd.getPath()); //$NON-NLS-1$
 			if (rsDescFound != null) {
 				LOGGER.log(LogService.LOG_WARNING, "A service endpoint with path=[" + rsd.getPath() //$NON-NLS-1$
 						+ "] and remoteType=[" + rsd.getProtocol() + "] already published... ignored"); //$NON-NLS-1$ //$NON-NLS-2$
 				return;
 			}
 
 			if (rsd.getPath() == null) {
 				LOGGER.log(LogService.LOG_WARNING, "no path for service: " + service.toString() //$NON-NLS-1$
 						+ " Service not published remote"); //$NON-NLS-1$
 				return;
 			}
 
 			IServicePublisher servicePublisher = servicePublishers.get(rsd.getProtocol());
 			if (servicePublisher == null) {
 				LOGGER.log(LogService.LOG_INFO, "no publisher found for protocol " + rsd.getProtocol()); //$NON-NLS-1$
 				unpublishedServices.add(rsd);
 				return;
 			}
 			String url = null;
 			try {
 				url = servicePublisher.publishService(rsd);
 			} catch (RuntimeException e) {
 				LOGGER.log(LogService.LOG_ERROR, e.getMessage());
 				return;
 			}
 			// set full URL under which the service is available
 			rsd.setURL(url);
 			handler.setMessageContextAccessor(servicePublisher.getMessageContextAccessor());
 			rsDescs.put(rsd.getProtocol() + "::" + rsd.getPath(), rsd); //$NON-NLS-1$
 			LOGGER.log(LogService.LOG_DEBUG, "service endpoints count: " + rsDescs.size()); //$NON-NLS-1$
 
 		}
 
 	}
 
 	public RemoteServiceDescription[] getAllServices() {
 		RemoteServiceDescription[] result = new RemoteServiceDescription[rsDescs.size()];
 		synchronized (rsDescs) {
 			rsDescs.values().toArray(result);
 		}
 		return result;
 	}
 
 }
