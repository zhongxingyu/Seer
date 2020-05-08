 package org.ow2.chameleon.rose.internal;
 
 import static org.osgi.framework.Constants.OBJECTCLASS;
 import static org.osgi.framework.FrameworkUtil.createFilter;
import static org.osgi.service.log.LogService.LOG_ERROR;
import static org.osgi.service.log.LogService.LOG_WARNING;
 import static org.osgi.service.remoteserviceadmin.EndpointListener.ENDPOINT_LISTENER_SCOPE;
 import static org.ow2.chameleon.rose.util.RoseTools.endDescToDico;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import org.apache.felix.ipojo.annotations.Bind;
 import org.apache.felix.ipojo.annotations.Component;
 import org.apache.felix.ipojo.annotations.Instantiate;
 import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
 import org.apache.felix.ipojo.annotations.Unbind;
 import org.apache.felix.ipojo.annotations.Validate;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.Filter;
 import org.osgi.framework.FrameworkUtil;
 import org.osgi.framework.InvalidSyntaxException;
 import org.osgi.framework.ServiceReference;
 import org.osgi.framework.ServiceRegistration;
import org.osgi.service.log.LogService;
 import org.osgi.service.remoteserviceadmin.EndpointDescription;
 import org.osgi.service.remoteserviceadmin.EndpointListener;
 import org.osgi.service.remoteserviceadmin.ExportReference;
 import org.osgi.util.tracker.ServiceTracker;
 import org.osgi.util.tracker.ServiceTrackerCustomizer;
 import org.ow2.chameleon.rose.registry.ExportRegistryListening;
 import org.ow2.chameleon.rose.registry.ExportRegistryProvisioning;
 import org.ow2.chameleon.rose.registry.ExportRegistryService;
 import org.ow2.chameleon.rose.registry.ExportedEndpointListener;
import org.ow2.chameleon.rose.util.DefaultLogService;
 
 @Component(name="rose.export.registry",immediate=true)
 @Instantiate(name="rose.export.registry-instance")
 @Provides(specifications={ExportRegistryProvisioning.class,ExportRegistryListening.class})
 public class ExportRegistryComponent implements ExportRegistryService{
 	
 	private static final String FILTER = "(" + OBJECTCLASS + "=" + ExportReference.class.getName() + ")";
 	private final Map<Object, ServiceRegistration> registrations;
 	private final Map<EndpointListener,ListenerWrapper> listeners;
 	private final BundleContext context;
 	
	@Requires(defaultimplementation=DefaultLogService.class)
	private LogService logger;
	
 	public ExportRegistryComponent(BundleContext pContext) {
 		context=pContext;
 		registrations = new HashMap<Object, ServiceRegistration>();
 		listeners = new HashMap<EndpointListener, ListenerWrapper>();
 	}
 	
 	@Validate
 	@SuppressWarnings("unused")
 	private void stop(){
 		synchronized (registrations) {
 			for (Iterator<ServiceRegistration> iterator = registrations.values().iterator(); iterator.hasNext();) {
 				ServiceRegistration regis = (ServiceRegistration) iterator.next();
 				regis.unregister();
 				iterator.remove();
 			}
 		}
 	}
 	
 
 	public void put(Object key, ExportReference xref) {
 		
 		synchronized (registrations) {
 			
 			if (registrations.containsKey(key)){
 				throw new IllegalStateException("An ExportReference associated with the given key as already been registered");
 			}
 			
 			ServiceRegistration reg = context.registerService(ExportReference.class.getName(), xref, endDescToDico(xref.getExportedEndpoint()));
 			registrations.put(key, reg);
 		}
 		
 	}
 
 	public ExportReference remove(Object key) {
 		ServiceRegistration sreg;
 
 		synchronized (registrations) {
 			sreg = registrations.remove(key);
 		}
 		
 		if(sreg == null){
 			return null;
 		}
 
 		ExportReference xref = (ExportReference) context.getService(sreg.getReference());
 		sreg.unregister();
 
 		return xref;
 	}
 	
 	public boolean contains(Object key) {
 		synchronized (registrations) {
 			return registrations.containsKey(key);
 		}
 	}
 	
 	/*-----------------------------------------*
 	 * ExportRegistryListening service methods *
 	 *-----------------------------------------*/
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.ow2.chameleon.rose.registry.ExportRegistryListening#addEndpointListener(org.osgi.service.remoteserviceadmin.EndpointListener)
 	 */
 	public void addEndpointListener(EndpointListener listener) {
 		try {
 			addEndpointListener(listener, null);
 		} catch (InvalidSyntaxException e) {
			logger.log(LOG_ERROR, "This exeception should not occured. ",e);
			assert false; //impossible right ?
 		}
 	}
 
 	public void addEndpointListener(EndpointListener listener, String filter)
 			throws InvalidSyntaxException {
 		
 		//Check the filter if != null
 		if (filter != null){
 			FrameworkUtil.createFilter(filter);
 		}
 		
 		//XXX The filter must not contains the ObjectClass filter.
 		
 		synchronized (listeners) {
 
 			ListenerWrapper slist;
 
 			// update if was already present
 			if (listeners.containsKey(listener)) {
 
 				listeners.get(listener).setFilter(filter);
 				
 			} else { // create otherwise
 				slist = new ListenerWrapper(listener, filter);
 				// add the listeners to the listeners map.
 				listeners.put(listener, slist);
 			}
 
 			
 
 		}
 		
 	}
 
 	public void removeEndpointListener(EndpointListener listener) {
 		synchronized (listeners) {
 			if (listeners.containsKey(listener)){
 				listeners.remove(listener).close(); //remove and close ! (stop the tracker)
 			}
 		}
 	}
 	
 	/*-----------------------------*
 	 * WhiteBoard pattern support  *
 	 *-----------------------------*/
 	
 	@SuppressWarnings("unused")
 	@Bind(aggregate=true,optional=true,id="listeners")
 	private void bindExportedEndpointListener(ExportedEndpointListener listener,Map<String,Object> properties){
 		String filter = (String) properties.get(ENDPOINT_LISTENER_SCOPE);
 		try {
 			addEndpointListener(listener, filter);
 		} catch (Exception e) {
			logger.log(LOG_WARNING, "Cannot bind listener: "+listener+" an exception occured.",e);
 		}
 	}
 	
 	@Unbind(id="listeners")
 	@SuppressWarnings("unused")
 	private void unBindExportedEndpointListener(ExportedEndpointListener listener){
 		removeEndpointListener(listener);
 	}
 	
 	
 	
 	/**
 	 * InnerClass, wrap an EndpointListener in a {@link ServiceTrackerCustomizer}
 	 */
 	public final class ListenerWrapper implements ServiceTrackerCustomizer {
 		private ServiceTracker tracker;
 		private final EndpointListener listener;
 		private String filter;
 		
 		
 		private ListenerWrapper(EndpointListener pListener,String pFilter) throws InvalidSyntaxException {
 			listener = pListener; 
 			Filter ofilter = createFilter("(&" +FILTER + pFilter +")");
 			tracker = new ServiceTracker(context, ofilter, this);
 			filter = pFilter;
 			tracker.open();
 		}
 		
 		public void close(){
 			tracker.close();
 		}
 		
 		private void setFilter(String pFilter) throws InvalidSyntaxException{
 			//update only if the filter are not equal
 			if ( (pFilter!= filter) && (pFilter != null && !pFilter.equals(filter))){
 				Filter ofilter = createFilter("(&" +FILTER + pFilter +")");
 				filter=pFilter;
 				tracker.close();
 				tracker = new ServiceTracker(context,ofilter,this);
 				tracker.open();
 			}
 		}
 
 		public Object addingService(ServiceReference reference) {
 			ExportReference xref = (ExportReference) context.getService(reference);
 			listener.endpointAdded(xref.getExportedEndpoint(), filter);
 			return xref.getExportedEndpoint();
 		}
 
 		public void modifiedService(ServiceReference reference, Object service) {
			logger.log(LOG_WARNING, "Modification of an ExportReference " +
									"is not supported. EndpointDescription: " +
									service);
 		}
 
 		public void removedService(ServiceReference reference, Object service) {
 			listener.endpointRemoved((EndpointDescription) service, filter);
 		}
 	}
 
 }
