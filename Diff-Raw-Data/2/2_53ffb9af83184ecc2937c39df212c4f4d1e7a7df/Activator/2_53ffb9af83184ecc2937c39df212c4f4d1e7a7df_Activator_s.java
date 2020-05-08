 package org.ponyKnight.osgi;
 
 import org.osgi.framework.BundleActivator;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.ServiceReference;
 import org.osgi.service.http.HttpService;
 
 public class Activator implements BundleActivator {
 	public void start(BundleContext context) throws Exception {
 		ServiceReference sRef = context.getServiceReference(HttpService.class
 				.getName());
 		if (sRef != null) {
 			System.out.println("-----------------------------------------------------------------------");
 			System.out.println("-----------------------------------------------------------------------");
 			System.out.println("-----------------------------------------------------------------------");
 			System.out.println("-----------------------------------------------------------------------");
 			System.out.println("-----------------------------------------------------------------------");
 			System.out.println("-----------------------------------------------------------------------");
 			System.out.println("-----------------------------------------------------------------------");
 			System.out.println("-----------------------------------------------------------------------");
 			System.out.println("-----------------------------------------------------------------------");
 			System.out.println("-----------------------------------------------------------------------");
 			System.out.println("-----------------------------------------------------------------------");
 			System.out.println("-----------------------------------------------------------------------");
 			System.out.println("-----------------------------------------------------------------------");
 			System.out.println("-----------------------------------------------------------------------");
 			System.out.println("-----------------------------------------------------------------------");
 			System.out.println("-----------------------------------------------------------------------");
 			System.out.println("-----------------------------------------------------------------------");
 			System.out.println("-----------------------------------------------------------------------");
 			System.out.println("-----------------------------------------------------------------------");
 			System.out.println("-----------------------------------------------------------------------");
 			System.out.println("-----------------------------------------------------------------------");
 			System.out.println("-----------------------------------------------------------------------");
 			System.out.println("-----------------------------------------------------------------------");
 			System.out.println("-----------------------------------------------------------------------");
 			System.out.println("-----------------------------------------------------------------------");
 			HttpService service = (HttpService) context.getService(sRef);
			service.registerResources("/", "/www", null);
 		}
 	}
 
 	public void stop(BundleContext arg0) throws Exception {
 		// TODO Auto-generated method stub
 
 	}
 }
