 package org.paxle.gui.impl;
 
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.BundleException;
 import org.osgi.framework.InvalidSyntaxException;
 import org.osgi.framework.ServiceReference;
 
 public class ServiceManager {
 	public static BundleContext context = null;
 	
     /**
      * Default constructor.
      */
     public ServiceManager() {
         // do nothing
     }	
 	
 	public void shutdownFramework() throws BundleException {
 		Bundle framework = ServiceManager.context.getBundle(0);
 		if (framework != null) {
 			framework.stop();
 		}
		
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		// wait a view seconds, then try a System.exit
		System.err.println("System.exit");
		System.exit(0);
 	}
 	
 	public void restartFramework() throws BundleException {
 		Bundle framework = ServiceManager.context.getBundle(0);
 		if (framework != null) {
 			framework.update();
 		}		
 	}
 
 	public Object getService(String serviceName) {
 		ServiceReference reference = ServiceManager.context.getServiceReference(serviceName);
 		return (reference == null) ? null : ServiceManager.context.getService(reference);
 	}
 	
 	public boolean hasService(String serviceName) {
 		return ServiceManager.context.getServiceReference(serviceName) != null;
 	}
 	
 	public Object[] getServices(String serviceName, String query) throws InvalidSyntaxException {
 		ServiceReference[] references = ServiceManager.context.getServiceReferences(serviceName,query);
 		if (references == null) return null;
 		
 		Object[] services = new Object[references.length];
 		for (int i=0; i < references.length; i++) {			
 			services[i] = ServiceManager.context.getService(references[i]);
 		}
 		
 		return services;
 	}
     
 	public boolean hasSerivce(String serviceName, String query) throws InvalidSyntaxException {
 		Object[] services = this.getServices(serviceName, query);
 		return (services != null && services.length > 0);
 	}
 	
     public Bundle[] getBundles() {
         return ServiceManager.context.getBundles();
     }
 
     public Bundle getBundle(long bundleID) {
         return ServiceManager.context.getBundle(bundleID);
     }
 }
