 package org.eclipse.b3.util;
 
 import java.util.IdentityHashMap;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.Plugin;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.InvalidSyntaxException;
 import org.osgi.framework.ServiceReference;
 
 /**
  * Copyright (c) 2006-2009, Cloudsmith Inc.
  * The code, documentation and other materials contained herein have been
  * licensed under the Eclipse Public License - v 1.0 by the copyright holder
  * listed above, as the Initial Contributor under such license. The text of
  * such license is available at www.eclipse.org.
  */
 
 /**
  * @author filip.hrbek@cloudsmith.com
  * 
  */
 public class B3Util extends Plugin {
 	private static Plugin plugin;
 
 	public static B3Util getPlugin() {
 		return (B3Util) plugin;
 	}
 
 	public static String getPluginID() {
 		return plugin.getBundle().getSymbolicName();
 	}
 
 	private IdentityHashMap<Object, ServiceReference> services;
 
 	public B3Util() {
 		plugin = this;
 	}
 
 	public <T> T getService(Class<T> serviceClass) throws CoreException {
		return getService(serviceClass, null);
 	}
 
 	public <T> T getService(Class<T> serviceClass, String filter) throws CoreException {
 		BundleContext context = getPlugin().getBundle().getBundleContext();
 		String serviceName = serviceClass.getName();
 		ServiceReference[] serviceRef;
 		try {
			serviceRef = context.getAllServiceReferences(serviceName, filter);
 		}
 		catch(InvalidSyntaxException e) {
 			throw ExceptionUtils.wrap(e);
 		}
 		if(serviceRef == null || serviceRef.length == 0)
 			throw ExceptionUtils.fromMessage("Missing OSGi Service %s", serviceName);
 		T service = serviceClass.cast(context.getService(serviceRef[0]));
 		if(services == null)
 			services = new IdentityHashMap<Object, ServiceReference>();
 		services.put(service, serviceRef[0]);
 		return service;
 	}
 
 	public void start(BundleContext context) throws Exception {
 		super.start(context);
 	}
 
 	public void stop(BundleContext context) throws Exception {
 		super.stop(context);
 	}
 
 	public void ungetService(Object service) {
 		if(services != null && service != null) {
 			ServiceReference serviceRef = services.remove(service);
 			if(serviceRef != null)
 				getPlugin().getBundle().getBundleContext().ungetService(serviceRef);
 		}
 	}
 }
