 package com.buglabs.util;
 
 import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
 import org.osgi.framework.ServiceReference;
 
 /**
  * A utility for loading a set of pre-existing registered services into client-defined state.
  * @author kgilmer
  *
  */
 public class OSGiServiceLoader {
 	/**
 	 * This method will be called on every service that matches the criteria passed into loadServices().
 	 * @author kgilmer
 	 *
 	 */
 	public interface IServiceLoader {
		public void load(Object service);
 	}
 	
 	/**
 	 * Load services from the OSGi service registry.  Refer to BundleContext.getServiceReferences() for more information.
 	 * @param context
 	 * @param clazz
 	 * @param filter
 	 * @param loader
	 * @throws InvalidSyntaxException
 	 */
	public static void loadServices(BundleContext context, String clazz, String filter, IServiceLoader loader) throws InvalidSyntaxException {
 		ServiceReference[] sr = context.getServiceReferences(clazz, filter);
 		
 		if (sr != null) {
 			for (int i = 0; i < sr.length; ++i) {		
 				loader.load(context.getService(sr[i]));
 			}
 		}
 	}
 }
