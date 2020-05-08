 package ods.client;
 
 import java.util.Hashtable;
 
 import odscommon.service.interfaces.EchoService;
 
 import org.osgi.framework.BundleActivator;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.ServiceReference;
 import org.osgi.util.tracker.ServiceTracker;
 
 public class Activator implements BundleActivator {
 	private ServiceTracker st;
 
 	public void start(final BundleContext bc) throws Exception {
 		System.err.println("start-------------------------------------------");
 		st = new ServiceTracker(bc, EchoService.class.getName(), null) {
 			@Override
 			public Object addingService(ServiceReference reference) {
 				Object svc = bc.getService(reference);
 				System.err.println("foundref-------------------------------------------");
 				if (svc instanceof EchoService) {
					System.err.println("and add to-------------------------------------------");
					Hashtable<String, String> props = new Hashtable<String, String>();
					props.put("type", "testdosgi");
					bc.registerService(EchoService.class.getName(), svc, props);
 				}
 
 				return super.addingService(reference);
 			}
 		};
 		st.open();
 	}
 
 	public void stop(BundleContext bc) throws Exception {
 		st.close();
 	}

 }
