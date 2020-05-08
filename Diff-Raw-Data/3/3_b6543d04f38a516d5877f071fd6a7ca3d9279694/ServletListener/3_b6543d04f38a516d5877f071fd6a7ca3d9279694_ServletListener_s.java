 /**
  * This file is part of the Paxle project.
  * Visit http://www.paxle.net for more information.
  * Copyright 2007-2009 the original author or authors.
  *
  * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0").
  * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
  * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt
  * or in the file LICENSE.txt in the root directory of the Paxle distribution.
  *
  * Unless required by applicable law or agreed to in writing, this software is distributed
  * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  */
 package org.paxle.gui.impl;
 
 import javax.servlet.Servlet;
 
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.Constants;
 import org.osgi.framework.InvalidSyntaxException;
 import org.osgi.framework.ServiceEvent;
 import org.osgi.framework.ServiceListener;
 import org.osgi.framework.ServiceReference;
 import org.osgi.service.http.HttpContext;
 import org.osgi.util.tracker.ServiceTracker;
 
 public class ServletListener implements ServiceListener {
 	/**
 	 * A LDAP styled expression used for the service-listener
 	 */
 	public static final String FILTER = String.format("(%s=%s)",
 			Constants.OBJECTCLASS, Servlet.class.getName());	
 
 	/**
 	 * A class to manage registered servlets
 	 */
 	private ServletManager servletManager = null;
 	
 	/**
 	 * A class to manage menu entries
 	 */
 	private MenuManager menuManager = null;
 	
 	/**
 	 * The {@link BundleContext osgi-bundle-context} of this bundle
 	 */	
 	private BundleContext context = null;	
 
 	ServiceTracker userAdminTracker;	
 	
 	public ServletListener(ServletManager servletManager, MenuManager menuManager, ServiceTracker userAdminTracker, BundleContext context) throws InvalidSyntaxException {		
 		this.servletManager = servletManager;
 		this.menuManager = menuManager;
 		this.userAdminTracker = userAdminTracker;
 		this.context = context;
 		
 		ServiceReference[] services = context.getServiceReferences(null,FILTER);
 		if (services != null) for (ServiceReference service : services) serviceChanged(service, ServiceEvent.REGISTERED);	
 	}
 
 	/**
 	 * @see ServiceListener#serviceChanged(ServiceEvent)
 	 */
 	public void serviceChanged(ServiceEvent event) {
 		// getting the service reference
 		ServiceReference reference = event.getServiceReference();
 		this.serviceChanged(reference, event.getType());
 	}	
 
 	private void serviceChanged(ServiceReference reference, int eventType) {
 		if (reference == null) return;		
 
 		String path = (String)reference.getProperty("path");
 		String menuName = (String)reference.getProperty("menu");
 		Boolean userAuth = (Boolean)reference.getProperty("doUserAuth");		
 		if (userAuth == null) userAuth = Boolean.FALSE;
 
 		if (eventType == ServiceEvent.REGISTERED) {
 			// getting a reference to the servlet
 			Servlet servlet = (Servlet) this.context.getService(reference);
 			
 			// register servlet
 			if (!userAuth.booleanValue()) {
 				this.servletManager.addServlet(path, servlet);
 			} else {
 				Bundle bundle = reference.getBundle();
 				HttpContext context = new HttpContextAuth(bundle, this.userAdminTracker);
 				this.servletManager.addServlet(path, servlet, context);
 			}
 			
 			// registering menu
 			if (menuName != null && menuName.length() > 0) {
 				String resourceBundleBase = null;
 				ClassLoader resourceBundleLoader = null;
 				
 				if (menuName.startsWith("%") || menuName.contains("/%")) {
 					/* 
 					 * The menu-name needs to be localized.
 					 * We are trying to finde the proper resource-bundle to use
 					 */
 					
 					// the resource-bundle basename
 					resourceBundleBase = (String) reference.getProperty("menu-localization");
 					if (resourceBundleBase == null)
 						resourceBundleBase = (String) reference.getBundle().getHeaders().get(Constants.BUNDLE_LOCALIZATION);
 					if (resourceBundleBase == null)
 						resourceBundleBase = Constants.BUNDLE_LOCALIZATION_DEFAULT_BASENAME;
 					
 					// the classloader to use
 					resourceBundleLoader = servlet.getClass().getClassLoader();
 				}
 				this.menuManager.addItem(path, menuName, resourceBundleBase, resourceBundleLoader);
 			}
 		} else if (eventType == ServiceEvent.UNREGISTERING) {
 			// unregister servlet
 			this.servletManager.removeServlet(path);
 			
 			// remove menu
 			if (menuName != null && menuName.length() > 0) {
 				this.menuManager.removeItem(menuName);
 			}
 			
 		} else if (eventType == ServiceEvent.MODIFIED) {
 		}	
 	}
 }
