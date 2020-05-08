 package rsaother;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.InvalidSyntaxException;
 import org.osgi.framework.ServiceReference;
 import org.osgi.service.remoteserviceadmin.EndpointDescription;
 import org.osgi.service.remoteserviceadmin.ExportReference;
 import org.osgi.service.remoteserviceadmin.ExportRegistration;
 import org.osgi.service.remoteserviceadmin.ImportReference;
 import org.osgi.service.remoteserviceadmin.ImportRegistration;
 import org.osgi.service.remoteserviceadmin.RemoteServiceAdmin;
 import org.osgi.util.tracker.ServiceTracker;
 import org.osgi.util.tracker.ServiceTrackerCustomizer;
 import org.restlet.Component;
 import org.restlet.Request;
 import org.restlet.Response;
 import org.restlet.Restlet;
 import org.restlet.Server;
 import org.restlet.data.Protocol;
 import org.restlet.resource.Get;
 import org.restlet.resource.ServerResource;
 
 import rsaother.exception.RESTException;
 
 /*
  * Main class ... implements RemoteServiceAdmin
  */
 public class RESTServiceAdmin implements RemoteServiceAdmin {
 	
 	public static HashMap<String, Object> servicesByID;
 	
 	BundleContext context;
 	
 	private Component component;
 	
 	public RESTServiceAdmin(BundleContext context){
 		this.context = context;
 	}
 	
 	public void activate() throws RESTException {
 		/*
 		 * server opstarten
		 */		
 		component = new Component();
 		Server server = component.getServers().add(Protocol.HTTP, 8080);
 		try {
 			server.start();
 		} catch (Exception e1) {
 			throw new RESTException("Error starting the server", e1);
 		}
 		
 		/*
 		 * ServiceListener that automatically exports services with exported interfaces
 		 * as defined by the Remote Services specification
 		 */
 		try {
 			ServiceTracker serviceTracker = new ServiceTracker(context,
 				context.createFilter("(service.exported.interfaces=*)"), 
 				new ServiceTrackerCustomizer() {
 
 					@Override
 					public Object addingService(ServiceReference ref) {
 						Collection<ExportRegistration> regs = exportService(ref, null);
 						return regs;
 					}
 
 					@Override
 					public void modifiedService(ServiceReference ref,
 							Object regs) {}
 
 					@Override
 					public void removedService(ServiceReference ref,
 							Object regs) {
 						for(ExportRegistration r : (Collection<ExportRegistration>) regs){
 							r.close();
 						}
 					}
 				});
 			serviceTracker.open();
 		}catch(InvalidSyntaxException e){}
 	}
 
 	
 	
 	@Override
 	public Collection<ExportRegistration> exportService(ServiceReference reference, Map<String, ?> properties) {
 		//Kortom:
 		// - we hebben een implementatie (in reference) van een service
 		// - de interface moeten we wel aangeraken (kan in properties)
 		// - die moeten we toevoegen aan de Restlet server...
 		Object serviceObject = context.getService(reference);
 		Method[] methods = serviceObject.getClass().getMethods();
 		
 		String id = "" + reference.getProperty("service.id");
 		servicesByID.put(id, serviceObject);
 		
 		for(int i = 0; i < methods.length; i++) {
 			String URI = "/" + id + "/" + RESTImportProxyHandler.methodToID(methods[i]);
 			
 			component.getDefaultHost().attach(URI, RemoteMethod.class);
 		}
 
 		return null;// Sorry, not supported -Jeroen
 	}
 
 	@Override
 	public ImportRegistration importService(EndpointDescription endpoint) {
 		Class<?> interfaceClass = (Class<?>) endpoint.getProperties().get("interface");
 		String baseUrl = endpoint.getId();
 		
 		RESTImportProxyHandler proxyhandler = new RESTImportProxyHandler(interfaceClass, baseUrl);
 		
 		return proxyhandler.getImportRegistration(context, endpoint);
 	}
 	
 	
 
 	@Override
 	public Collection<ExportReference> getExportedServices() {
 		throw new RuntimeException("Sorry, not supported yet... -Jeroen");
 	}
 
 	@Override
 	public Collection<ImportReference> getImportedEndpoints() {
 		throw new RuntimeException("Sorry, not supported yet... -Jeroen");
 	}
 	
 }
