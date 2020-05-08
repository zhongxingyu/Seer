 /*******************************************************************************
  * Copyright (c) 2012 Neil Bartlett.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Neil Bartlett - initial API and implementation
  ******************************************************************************/
 package org.bndtools.rt.rest;
 
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.Constants;
 import org.osgi.framework.Filter;
 import org.osgi.framework.InvalidSyntaxException;
 import org.osgi.framework.ServiceReference;
 import org.osgi.service.http.HttpService;
 import org.osgi.service.log.LogService;
 import org.osgi.util.tracker.ServiceTracker;
 
 public class HttpServiceTracker extends ServiceTracker {
 	
 	private final LogService log;
 
 	static final class Endpoint {
 		final RestAppServletManager manager;
 		final ResourceServiceTracker serviceTracker;
 		final ResourceClassTracker classTracker;
 		Endpoint(RestAppServletManager manager, ResourceServiceTracker serviceTracker, ResourceClassTracker classTracker) {
 			this.manager = manager;
 			this.serviceTracker = serviceTracker;
 			this.classTracker = classTracker;
 		}
 	}
 	
 	public HttpServiceTracker(BundleContext context, LogService log) {
 		super(context, createFilter(context), null);
 		this.log = log;
 	}
 	
 	private static Filter createFilter(BundleContext context) {
 		try {
			return context.createFilter(String.format("(&(%s=%s)(org.osgi.service.http.port=*))", Constants.OBJECTCLASS, HttpService.class.getName()));
 		} catch (InvalidSyntaxException e) {
 			// shouldn't happen
 			throw new RuntimeException(e);
 		}
 	}
 	
 	private static int convertIntProperty(Object property, int defaultValue) {
 		if (property == null)
 			return defaultValue;
 		
 		if (property instanceof Number)
 			return ((Number) property).intValue();
 		
 		if (property instanceof String)
 			return Integer.parseInt((String) property);
 
 		throw new IllegalArgumentException("Invalid property type: " + property.getClass().getName());
 	}
 	
 	public Object addingService(@SuppressWarnings("rawtypes") ServiceReference reference) {
 		@SuppressWarnings("unchecked")
 		HttpService httpService = (HttpService) context.getService(reference);
 		
		int httpPort = convertIntProperty(reference.getProperty("org.osgi.service.http.port"), -1);
		if ("false".equals(reference.getProperty("org.apache.felix.http.enable")))
			httpPort = -1;
		int httpsPort = convertIntProperty(reference.getProperty("org.osgi.service.http.port.secure"), -1);
		if ("false".equals(reference.getProperty("org.apache.felix.https.enable")))
			httpsPort = -1;
 		
 		String localhost;
 		try {
 			localhost = InetAddress.getLocalHost().getHostAddress();
 		} catch (UnknownHostException e) {
 			// shouldn't happen
 			throw new RuntimeException(e);
 		}
 		
 		RestAppServletManager manager = new RestAppServletManager(context, httpService, httpPort, httpsPort, localhost);
 		ResourceServiceTracker serviceTracker = ResourceServiceTracker.newInstance(context, manager, log);
 		serviceTracker.open();
 		
 		ResourceClassTracker classTracker = new ResourceClassTracker(context, manager, log);
 		classTracker.open();
 		
 		return new Endpoint(manager, serviceTracker, classTracker);
 	}
 	
 	@Override
 	public void removedService(@SuppressWarnings("rawtypes") ServiceReference reference, Object service) {
 		Endpoint endpoint = (Endpoint) service;
 		
 		endpoint.classTracker.close();
 		endpoint.serviceTracker.close();
 		endpoint.manager.destroyAll();
 		
 		context.ungetService(reference);
 	}
 }
