 
 package org.paxle.desktop.impl;
 
 import java.lang.reflect.Array;
 
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.BundleException;
 import org.osgi.framework.InvalidSyntaxException;
 import org.osgi.framework.ServiceReference;
 
 public class ServiceManager {
 	
 	public static final int FRAMEWORK_BUNDLE_ID = 0;
 	
 	private final BundleContext context;
 	
 	public ServiceManager(BundleContext context) {
 		this.context = context;
 	}
 	
 	public void shutdownFramework() throws BundleException {
 		final Bundle framework = this.context.getBundle(FRAMEWORK_BUNDLE_ID);
 		if (framework != null) {
 			framework.stop();
 		}
 	}
 	
 	public void restartFramework() throws BundleException {
 		final Bundle framework = this.context.getBundle(FRAMEWORK_BUNDLE_ID);
 		if (framework != null) {
 			framework.update();
 		}
 	}
 	
 	public String getProperty(String key) {
 		return this.context.getProperty(key);
 	}
 	
 	public Object getService(String name) {
 		return getService(name, Object.class);
 	}
 	
 	public <E> E getService(Class<E> service) {
 		return getService(service.getName(), service);
 	}
 	
 	public <E> E getService(String name, Class<E> service) {
 		ServiceReference reference = this.context.getServiceReference(name);
 		return (reference == null) ? null : service.cast(this.context.getService(reference));
 	}
 	
 	public boolean hasService(Class<?> service) {
 		return hasService(service.getName());
 	}
 	
 	public boolean hasService(Class<?> service, String query) throws InvalidSyntaxException {
 		return hasService(service.getName(), query);
 	}
 	
 	public boolean hasService(String name, String query) throws InvalidSyntaxException {
 		try {
 			return this.context.getServiceReferences(name, query) != null;
 		} catch (Exception e) {
 			if (e instanceof InvalidSyntaxException)
 				throw (InvalidSyntaxException)e;
 			return false;
 		}
 	}
 	
 	public boolean hasService(String name) {
 		try {
 			return this.context.getServiceReference(name) != null;
 		} catch (Exception e) {
 			return false;
 		}
 	}
 	
 	@SuppressWarnings("unchecked")
 	public <E> E[] getServices(Class<E> service, String query) throws InvalidSyntaxException {
 		final ServiceReference[] references = this.context.getServiceReferences(service.getName(), query);
 		if (references == null) return null;
 		
 		final E[] services = (E[])Array.newInstance(service, references.length);
 		for (int i=0; i<references.length; i++)
 			services[i] = service.cast(this.context.getService(references[i]));
 		return services;
 	}
     
     public Bundle[] getBundles() {
         return this.context.getBundles();
     }
     
     public Bundle getBundle(long bundleID) {
         return this.context.getBundle(bundleID);
     }
 }
