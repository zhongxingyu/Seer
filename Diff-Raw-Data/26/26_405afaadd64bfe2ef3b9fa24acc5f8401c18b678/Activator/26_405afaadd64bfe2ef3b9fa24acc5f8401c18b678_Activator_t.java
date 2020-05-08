 package uk.ac.diamond.scisoft.analysis.dataset;
 
import java.util.HashMap;

 import org.osgi.framework.BundleActivator;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.ServiceReference;
 
 public class Activator implements BundleActivator {
 
 	private static BundleContext context;
 
 	static BundleContext getContext() {
 		return context;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
 	 */
 	public void start(BundleContext bundleContext) throws Exception {
 		Activator.context = bundleContext;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
 	 */
 	public void stop(BundleContext bundleContext) throws Exception {
 		Activator.context = null;
 	}
 
 
	private static HashMap<Class<?>, Object> testServices;

 	public static Object getService(Class<?> clazz) {
		if (testServices!=null && testServices.containsKey(clazz)) return testServices.get(clazz);
 		if (context==null) return null;
 		ServiceReference<?> ref = context.getServiceReference(clazz);
 		if (ref==null) return null;
 		return context.getService(ref);
 	}
 
	public static void setTestService(Class<?> clazz, Object impl) {
		if (testServices == null) testServices = new HashMap<Class<?>, Object>(3);
		testServices.put(clazz, impl);
	}

 }
