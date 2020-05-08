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
 
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.servlet.ServletException;
 import javax.ws.rs.core.Application;
 
 import org.bndtools.service.endpoint.Endpoint;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.ServiceRegistration;
 import org.osgi.service.http.HttpService;
 import org.osgi.service.http.NamespaceException;
 import org.osgi.service.remoteserviceadmin.RemoteConstants;
 
 import com.sun.jersey.spi.container.servlet.ServletContainer;
 
 public class RestAppServletManager {
 
 	private static final String SCHEME_HTTP = "http"; //$NON-NLS-1$
 	private static final String SCHEME_HTTPS = "https"; //$NON-NLS-1$
 	private static final String WILDCARD = "*"; //$NON-NLS-1$
 	
 	private final Map<String, ImmutableApplication> appMap = new HashMap<String, ImmutableApplication>();
 	private final Map<String, ServiceRegistration<?>> httpEndpointRegistrations = new HashMap<String, ServiceRegistration<?>>();
 	private final Map<String, ServiceRegistration<?>> httpsEndpointRegistrations = new HashMap<String, ServiceRegistration<?>>();
 	
 	private BundleContext context;
 	private final HttpService httpService;
 	private final int httpPort;
 	private final int httpsPort;
 	private final String hostName;
 	private final ImmutableApplication defaultApplication;
 	
 	public RestAppServletManager(BundleContext context, HttpService httpService, int httpPort, int httpsPort, String hostName) {
 		this.context = context;
 		this.httpService = httpService;
 		this.httpPort = httpPort;
 		this.httpsPort = httpsPort;
 		this.hostName = hostName;
 		
 		Set<Class<?>> defaultClasses = new HashSet<Class<?>>();
 		defaultClasses.add(InjectAnnotationInjectableProvider.class);
 		defaultClasses.add(OptionalAnnotationInjectableProvider.class);
 		defaultClasses.add(TargetFilterAnnotationInjectableProvider.class);
 		defaultApplication = new ImmutableApplication(defaultClasses, null);
 	}
 
 	public void addSingletons(String alias, Set<Object> newResources) throws ServletException, NamespaceException {
 		final ImmutableApplication newApp;
 		synchronized (appMap) {
 			ImmutableApplication existingApp = appMap.remove(alias);
 			if (existingApp == null)
 				existingApp = defaultApplication;
 			newApp = existingApp.addSingletons(newResources);
 			appMap.put(alias, newApp);
 		}
 		replaceAlias(alias, newApp);
 	}
 	
 	public void addClasses(String alias, Set<Class<?>> newClasses) throws ServletException, NamespaceException {
 		final ImmutableApplication newApp;
 		synchronized (appMap) {
 			ImmutableApplication existingApp = appMap.remove(alias);
 			if (existingApp == null)
 				existingApp = defaultApplication;
 			newApp = existingApp.addClasses(newClasses);
 			appMap.put(alias, newApp);
 		}
 		replaceAlias(alias, newApp);
 	}
 
 	public void removeSingletons(String alias, final Collection<? extends Object> removedResources) throws ServletException, NamespaceException {
 		ImmutableApplication newApp;
 		synchronized (appMap) {
 			ImmutableApplication existingApp = appMap.remove(alias);
 			if (existingApp != null) {
 				newApp = existingApp.removeSingletons(removedResources);
 				appMap.put(alias, newApp);
 			} else {
 				newApp = null;
 			}
 		}
 		if (defaultApplication.equals(newApp))
 			newApp = null;
 		replaceAlias(alias, newApp);
 	}
 
 	public void removeClasses(String alias, final Collection<Class<?>> removedClasses) throws ServletException, NamespaceException {
 		ImmutableApplication newApp;
 		synchronized (appMap) {
 			ImmutableApplication existingApp = appMap.remove(alias);
 			if (existingApp != null) {
 				newApp = existingApp.removeClasses(removedClasses);
 				appMap.put(alias, newApp);
 			} else {
 				newApp = null;
 			}
 		}
 		if (defaultApplication.equals(newApp))
 			newApp = null;
 		replaceAlias(alias, newApp);
 	}
 	
 	private synchronized void replaceAlias(String alias, Application application) throws ServletException, NamespaceException {
 		// Unregister old servlet for this alias
 		try {
 			httpService.unregister(alias);
 		} catch (IllegalArgumentException e) {
 			// it wasn't registered
 		}
 
 		// Register the servlet
 		if (application == null) {
 			unregisterEndpointServices(alias);
 		} else {
 			ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
 			try {
 				Thread.currentThread().setContextClassLoader(RestAppServletManager.class.getClassLoader());
 				ServletContainer servlet = new ServletContainer(application);
 				httpService.registerServlet(alias, servlet, null, null);
 				registerEndpointServices(alias);
 			} finally {
 				Thread.currentThread().setContextClassLoader(oldLoader);
 			}
 		}
 	}
 	
 	private synchronized void registerEndpointServices(String alias) {
 		if (httpPort > 0) {
 			try {
 				URI uri = new URI(SCHEME_HTTP, null, hostName, httpPort, alias, null, null);
 				Hashtable<String, String> props = new Hashtable<String, String>();
 				props.put(Endpoint.URI, uri.toString());
 				props.put(RemoteConstants.SERVICE_EXPORTED_INTERFACES, WILDCARD);
 				ServiceRegistration<?> reg = context.registerService(Endpoint.class.getName(), new Endpoint() {}, props);
 				httpEndpointRegistrations.put(alias, reg);
 			} catch (URISyntaxException e) {
 				// shouldn't happen
 				throw new RuntimeException(e);
 			}
 		}
 		
 		if (httpsPort > 0) {
 			try {
 				URI uri = new URI(SCHEME_HTTPS, null, hostName, httpsPort, alias, null, null);
 				Hashtable<String, String> props = new Hashtable<String, String>();
 				props.put(Endpoint.URI, uri.toString());
 				ServiceRegistration<?> reg = context.registerService(Endpoint.class.getName(), new Endpoint() {}, props);
 				httpsEndpointRegistrations.put(alias, reg);
 			} catch (URISyntaxException e) {
 				// shouldn't happen
 				throw new RuntimeException(e);
 			}
 		}
 	}
 
 	private synchronized void unregisterEndpointServices(String alias) {
 		ServiceRegistration<?> httpReg = httpEndpointRegistrations.remove(alias);
 		if (httpReg != null)
 			httpReg.unregister();
 
 		ServiceRegistration<?> httpsReg = httpsEndpointRegistrations.remove(alias);
 		if (httpsReg != null)
 			httpsReg.unregister();
 	}
 	
 	public void destroyAll() {
 		List<String> aliases;
 		synchronized (appMap) {
 			aliases = new ArrayList<String>(appMap.keySet());
 			appMap.clear();
 		}
 		for (String alias : aliases) {
 			try {
 				httpService.unregister(alias);
 			} catch (Exception e) {
 				// ignore
 			}
 		}
 	}
 
 }
