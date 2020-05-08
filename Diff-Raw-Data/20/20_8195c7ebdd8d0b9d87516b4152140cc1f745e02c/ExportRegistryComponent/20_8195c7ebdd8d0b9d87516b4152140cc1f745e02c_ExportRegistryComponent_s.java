 package org.ow2.chameleon.rose.internal;
 
 import static org.osgi.framework.Constants.OBJECTCLASS;
 import static org.osgi.framework.ServiceEvent.REGISTERED;
 import static org.osgi.framework.ServiceEvent.UNREGISTERING;
 import static org.osgi.service.remoteserviceadmin.EndpointListener.ENDPOINT_LISTENER_SCOPE;
 import static org.ow2.chameleon.rose.util.RoseTools.endDescToDico;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import org.apache.felix.ipojo.annotations.Bind;
 import org.apache.felix.ipojo.annotations.Component;
 import org.apache.felix.ipojo.annotations.Instantiate;
 import org.apache.felix.ipojo.annotations.Provides;
 import org.apache.felix.ipojo.annotations.Unbind;
 import org.apache.felix.ipojo.annotations.Validate;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.FrameworkUtil;
 import org.osgi.framework.InvalidSyntaxException;
 import org.osgi.framework.ServiceEvent;
 import org.osgi.framework.ServiceListener;
 import org.osgi.framework.ServiceRegistration;
 import org.osgi.service.remoteserviceadmin.EndpointDescription;
 import org.osgi.service.remoteserviceadmin.EndpointListener;
 import org.osgi.service.remoteserviceadmin.ExportReference;
 import org.ow2.chameleon.rose.registry.ExportRegistryListening;
 import org.ow2.chameleon.rose.registry.ExportRegistryProvisioning;
 import org.ow2.chameleon.rose.registry.ExportRegistryService;
 import org.ow2.chameleon.rose.registry.ExportedEndpointListener;
 
 @Component(name="rose.export.registry",immediate=true)
 @Instantiate(name="rose.export.registry-instance")
 @Provides(specifications={ExportRegistryProvisioning.class,ExportRegistryListening.class})
 public class ExportRegistryComponent implements ExportRegistryService{
 	
 	private static final String FILTER = "(" + OBJECTCLASS + "=" + ExportReference.class.getName() + ")";
 	private final Map<Object, ServiceRegistration> registrations;
 	private final Map<EndpointListener,ListenerWrapper> listeners;
 	private final BundleContext context;
 	
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
 			assert false; //impossible right ? //TODO log error
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
 				slist = listeners.remove(listener);
 				slist.setFilter(filter); //update filter
 			} else { // create otherwise
 				slist = new ListenerWrapper(listener, filter);
 			}
 
 			try {
 				String newfilter = "(&" +FILTER + filter +")";
 				context.addServiceListener(slist, newfilter);
 
 				// add the listeners to the listeners map.
 				listeners.put(listener, slist);
 
 			} catch (InvalidSyntaxException e) {
 				// impossible right !
 				assert false;
 			}
 		}
 		
 	}
 
 	public void removeEndpointListener(EndpointListener listener) {
 		synchronized (listeners) {
 			if (listeners.containsKey(listener)){
 				ServiceListener slist = listeners.get(listener);
 				context.removeServiceListener(slist);
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
 			//TODO Log warning
 		}
 	}
 	
 	@Unbind(id="listeners")
 	@SuppressWarnings("unused")
 	private void unBindExportedEndpointListener(ExportedEndpointListener listener){
 		removeEndpointListener(listener);
 	}
 	
 	
 	
 	/**
 	 * InnerClass, wrap an EndpointListener in a ServiceListener
 	 */
 	public final class ListenerWrapper implements ServiceListener {
 		private final EndpointListener listener;
 		private volatile String filter;
 		
 		private ListenerWrapper(EndpointListener pListener,String pFilter) {
 			listener = pListener; 
 			setFilter(pFilter);
 		}
 		
 		private void setFilter(String pFilter){
 			filter=pFilter;
 		}
 
 		public void serviceChanged(ServiceEvent event) {
 			switch (event.getType()) {
 			case REGISTERED:
				listener.endpointAdded((EndpointDescription) event.getSource(), filter);
 				break;
 
 			case UNREGISTERING:
				listener.endpointRemoved((EndpointDescription) event.getSource(), filter);
 				break;
 			default:
				//TODO log Warning
 				break;
 			}
 		}
 		
 	}
 
 }
