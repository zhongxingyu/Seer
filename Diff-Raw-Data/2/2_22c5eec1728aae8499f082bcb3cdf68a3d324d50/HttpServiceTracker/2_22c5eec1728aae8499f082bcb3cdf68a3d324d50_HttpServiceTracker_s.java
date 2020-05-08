 package org.bndtools.rt.rest;
 
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.ServiceReference;
 import org.osgi.service.http.HttpService;
 import org.osgi.service.log.LogService;
 import org.osgi.util.tracker.ServiceTracker;
 
 public class HttpServiceTracker extends ServiceTracker {
 	
 	private final LogService log;
 
 	static final class Endpoint {
 		final RestAppServletManager manager;
 		final ResourceServiceTracker serviceTracker;
 		final ResourceClassTracker classTracker;
 		Endpoint(RestAppServletManager manager, ResourceServiceTracker serviceTracker, ResourceClassTracker classTracker) {
 			this.manager = manager;
 			this.serviceTracker = serviceTracker;
 			this.classTracker = classTracker;
 		}
 	}
 	
 	public HttpServiceTracker(BundleContext context, LogService log) {
 		super(context, HttpService.class.getName(), null);
 		this.log = log;
 	}
 	
 	public Object addingService(@SuppressWarnings("rawtypes") ServiceReference reference) {
 		@SuppressWarnings("unchecked")
		HttpService httpService = context.getService(reference);
 		
 		RestAppServletManager manager = new RestAppServletManager(httpService);
 		ResourceServiceTracker serviceTracker = ResourceServiceTracker.newInstance(context, manager, log);
 		serviceTracker.open();
 		
 		ResourceClassTracker classTracker = new ResourceClassTracker(context, manager, log);
 		classTracker.open();
 		
 		return new Endpoint(manager, serviceTracker, classTracker);
 	}
 	
 	@Override
 	public void removedService(@SuppressWarnings("rawtypes") ServiceReference reference, Object service) {
 		Endpoint endpoint = (Endpoint) service;
 		
 		endpoint.classTracker.close();
 		endpoint.serviceTracker.close();
 		endpoint.manager.destroyAll();
 		
 		context.ungetService(reference);
 	}
 }
