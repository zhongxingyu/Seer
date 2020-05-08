 package org.scribble.core.osgi;
 
 import java.util.Properties;
 
 import org.osgi.framework.BundleActivator;
 import org.osgi.framework.BundleContext;
//import org.osgi.util.tracker.ServiceTracker;
 import org.scribble.core.internal.validation.ValidationManagerImpl;
 import org.scribble.core.validation.ValidationManager;
 
 public class Activator implements BundleActivator {
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
 	 */
 	public void start(BundleContext context) throws Exception {
 		
         Properties props = new Properties();
 
         final ValidationManager vm=new ValidationManagerImpl();
         
         context.registerService(ValidationManager.class.getName(), 
 							vm, props);
         
        /*
         m_tracker = new ServiceTracker(context,
         		org.scribble.core.validation.Validator.class.getName(),
         				null) {
         	
 			public Object addingService(ServiceReference ref) {
 				Object ret=super.addingService(ref);
 				System.out.println("VALIDATOR HAS BEEN ADDED: "+ret);
 				return(ret);
 			}
         };
         
         m_tracker.open();
        */
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
 	 */
 	public void stop(BundleContext context) throws Exception {
 	}
 
	//private org.osgi.util.tracker.ServiceTracker m_tracker=null;
 }
