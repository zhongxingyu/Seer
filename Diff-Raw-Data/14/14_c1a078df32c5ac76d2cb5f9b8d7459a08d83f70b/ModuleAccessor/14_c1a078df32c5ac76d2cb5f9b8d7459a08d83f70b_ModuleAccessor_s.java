 package be.ugent.ods.osgi.protocolabstraction;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.InvalidSyntaxException;
 import org.osgi.framework.ServiceReference;
 import org.osgi.service.remoteserviceadmin.EndpointDescription;
 import org.osgi.service.remoteserviceadmin.RemoteServiceAdmin;
 
 import android.provider.SyncStateContract.Helpers;
 import be.ugent.ods.osgi.felix.OSGiRuntime;
 import be.ugent.ods.testapplications.service.interfaces.EchoService;
 import be.ugent.ods.testapplications.service.interfaces.GlowFilterService;
 import be.ugent.ods.testapplications.service.interfaces.VideoService;
 import be.ugent.ods.testapplications.service.list.TestApplicationProtocolList;
 
 public class ModuleAccessor {
 	
 	private BundleContext context;
 	private RemoteServiceAdmin currentrsa;
 	private int currentrsaindex;
 	
 	public ModuleAccessor(OSGiRuntime runtime) {
 		super();
 		this.context = runtime.getBundleContext();
 		this.currentrsa = null;
 		this.currentrsaindex = -1;
 	}
 	
 	/**
 	 * Get the number of possible ways to call remote methods.
 	 * (number of RSA's)
 	 * @return
 	 */
 	public int getNumberOfRSAModules() {
 		return TestApplicationProtocolList.protocols.length;
 	}
 	
 	/**
 	 * Get the name of a RSA
 	 * @param index
 	 * @return
 	 */
 	public String getRSAModuleName(int index) {
 		if(index==TestApplicationProtocolList.PROTOCOL_LOCAL) {
 			return "Lokaal";
 		}else{
 			return TestApplicationProtocolList.protocolnames[index];
 		}
 	}
 	
 	/**
 	 * USE RSA WITH INDEX index
 	 * @param index
 	 */
 	public void setUseRSAModule(int index) {
 		if(index==TestApplicationProtocolList.PROTOCOL_LOCAL) {
 			currentrsa = null;
 		}else{
 			if(index<0 || index >= getNumberOfRSAModules()) {
 				throw new IllegalArgumentException("Not a valid index in setUseRSAModule");
 			}
 			Collection<ServiceReference<RemoteServiceAdmin>> refs;
 			try {
 				refs = context.getServiceReferences(RemoteServiceAdmin.class, TestApplicationProtocolList.protocols[index]);
 			} catch (InvalidSyntaxException e) {
 				throw new RuntimeException("Error: Sorry, but the filter \""+TestApplicationProtocolList.protocols[index]+"\" is not correct.", e); 
 			}
 			
 			if(refs.size()==0) {
 				throw new RuntimeException("Error: Sorry, but we did not find that RSA module!");
 			}
 			if(refs.size()>1) {
 				throw new RuntimeException("Error: We found more than one RSA module for this filter!");
 			}
 			ServiceReference<RemoteServiceAdmin> rsaref = refs.iterator().next();
 			
 			currentrsa = context.getService(rsaref);
 		}
 		
 		currentrsaindex = index;
 	}
 	
 	/**
 	 * Get a module from somewhere...
 	 * 
 	 * @param c
 	 * @param endpoint
 	 * @return
 	 */
 	public <T> T getModule(Class<T> c){
 		if(currentrsa==null) {
 			ServiceReference<T> ref = context.getServiceReference(c);
 			return context.getService(ref);
 		}else{
 			try{
 				EndpointDescription endpoint = getCurrentEndpointDescriptionFor(c);
 				
 				ServiceReference<T> ref = (ServiceReference<T>) currentrsa.importService(endpoint).getImportReference().getImportedService();
 				
 				return context.getService(ref);
 			}catch (Exception e) {
 				throw new RuntimeException("Could not import the service...", e);
 			}
 		}
 	}
 	
 	public EndpointDescription getCurrentEndpointDescriptionFor(Class<?> c){
 		Map<String, String> rosgitim_ids = new HashMap<String, String>();
 		rosgitim_ids.put(EchoService.class.getName(), "44");
 		rosgitim_ids.put(GlowFilterService.class.getName(), "45");
 		rosgitim_ids.put(VideoService.class.getName(), "46");
 		
 		Map<Integer, EndpointDescription> endpoints = new HashMap<Integer, EndpointDescription>();
 		
 		//lokaal
 		endpoints.put(TestApplicationProtocolList.PROTOCOL_LOCAL, null);
 		
 		//r-osgi-tim server
 		Map<String, Object> properties = new HashMap<String, Object>();
		//properties.put("endpoint.id", "r-osgi://10.0.2.2:9278#"+rosgitim_ids.get(c.getName()));// TODO: put IP in property file or something like that (or in gui)
		properties.put("endpoint.id", "r-osgi://192.168.2.5:9278#"+rosgitim_ids.get(c.getName()));// TODO: put IP in property file or something like that (or in gui)
 		properties.put("service.imported.configs", "r-osgi");
 		properties.put("objectClass", new String[]{c.getName()});
 		EndpointDescription endpoint = new EndpointDescription(properties);
 		endpoints.put(TestApplicationProtocolList.PROTOCOL_ROSGI_TIM, endpoint);
 		
 		//r-osgi-tim-udp server
 		properties = new HashMap<String, Object>();
		//properties.put("endpoint.id", "r-osgi://10.0.2.2:9279#"+rosgitim_ids.get(c.getName()));// TODO: put IP in property file or something like that (or in gui)
		properties.put("endpoint.id", "r-osgi://192.168.2.5:9279#"+rosgitim_ids.get(c.getName()));// TODO: put IP in property file or something like that (or in gui)
 		properties.put("service.imported.configs", "r-osgi-udp");
 		properties.put("objectClass", new String[]{c.getName()});
 		endpoint = new EndpointDescription(properties);
 		endpoints.put(TestApplicationProtocolList.PROTOCOL_ROSGI_UDP, endpoint);
 		
 		// other...
 		properties = new HashMap<String, Object>();
	//	properties.put("endpoint.id", "http://10.0.2.2:8080/" + rosgitim_ids.get(c.getName()) + "/");
		properties.put("endpoint.id", "http://192.168.2.5:8080/" + rosgitim_ids.get(c.getName()) + "/");
 		properties.put("service.imported.configs", "r-osgi-other");
 		properties.put("objectClass", new String[]{c.getName()});
 		properties.put("interface", c);
 		endpoint = new EndpointDescription(properties);
 		endpoints.put(TestApplicationProtocolList.PROTOCOL_OTHER, endpoint);
 		
 		// pick correct one
 		return endpoints.get(currentrsaindex);
 	}
 	
 	public int getCurrentRsaIndex(){
 		return currentrsaindex;
 	}
 }
