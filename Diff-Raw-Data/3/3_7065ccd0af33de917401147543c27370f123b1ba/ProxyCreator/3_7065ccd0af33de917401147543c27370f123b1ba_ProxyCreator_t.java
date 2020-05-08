 package org.ow2.chameleon.rose.jsonrpc;
 
 import static java.lang.String.valueOf;
 import static java.util.Arrays.asList;
 import static org.ow2.chameleon.rose.util.RoseTools.registerProxy;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.felix.ipojo.annotations.Component;
 import org.apache.felix.ipojo.annotations.Invalidate;
 import org.apache.felix.ipojo.annotations.Provides;
 import org.apache.felix.ipojo.annotations.Requires;
 import org.apache.felix.ipojo.annotations.ServiceProperty;
 import org.apache.felix.ipojo.annotations.Validate;
 import org.jabsorb.client.Client;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.ServiceRegistration;
 import org.osgi.service.log.LogService;
 import org.osgi.service.remoteserviceadmin.EndpointDescription;
 import org.ow2.chameleon.rose.AbstractImporterComponent;
 import org.ow2.chameleon.rose.ImporterService;
 import org.ow2.chameleon.rose.RoseMachine;
 import org.ow2.chameleon.rose.util.RoseTools;
 
 /**
  * Provides an {@link ImporterService} allowing to access a
  * remote endpoint through jsonrpc thanks to the jabsorb implementation. 
  * 
  * TODO Improve the client management, only one client should be created for a given uri.
  */
 @Component(name="RoSe_importer.jabsorb")
 @Provides(specifications={ImporterService.class})
 public class ProxyCreator extends AbstractImporterComponent{
 	/**
 	 * Property containing the URL of the JSONRPC orb.
 	 */
 	private final static String PROP_JABSORB_URL = "org.jabsorb.url";
 	
 	
 	/**
 	 * Configuration supported by this component
 	 */
 	@ServiceProperty(name=ENDPOINT_CONFIG_PREFIX,mandatory=true,value="{json-rpc,jsonrpc,org.jabsorb}")
 	private String[] CONFIGS;
 	
 	/**
 	 * Require the {@link RoseMachine}.
 	 */
 	@Requires(optional=false,id="rose.machine")
 	private RoseMachine machine;
 	
 	@Requires(optional=true)
 	private LogService logger;
 	
 	private final BundleContext context;
 
     /**
      * Map which contains the proxies and theirs Client.
      */
     private HashMap<String, Client> proxies;
 
     public ProxyCreator(BundleContext pContext) {
         proxies = new HashMap<String, Client>();
         context=pContext;
     }
     
     /*
      * (non-Javadoc)
      * @see org.ow2.chameleon.rose.AbstractImporterComponent#createProxy(org.osgi.service.remoteserviceadmin.EndpointDescription, java.util.Map)
      */
     public ServiceRegistration createProxy(EndpointDescription description,Map<String, Object> properties){
     	final Object proxy;
         final Client client;
         
     	// Get the endpoint properties
     	String uri = valueOf(description.getProperties().get(PROP_JABSORB_URL));
         if (uri == null){
          	uri = valueOf(properties.get(PROP_JABSORB_URL));
         }
         
         //Try to load the class
         final List<Class<?>> klass = RoseTools.loadClass(context, description);
         
         if (klass == null){
 			throw new IllegalStateException(
 					"Cannot create a proxy for the description: "
 							+ description
 							+ " unable to find a bundle which export the service class.");
         }
         
         try {
         	MyHttpSession session = new MyHttpSession(new URI(uri));
             client = new MyClient(session);
         } catch (URISyntaxException e) {
             throw new IllegalArgumentException("The property" + PROP_JABSORB_URL + "must be set and a valid String form of the endpoint URL", e);
         }
         
 
         // Create the proxy thanks to jabsorb
         // FIXME implements only the first interface
         proxy = client.openProxy(description.getId(), klass.get(0));
 
         // Add the proxy to the proxy list
         proxies.put(description.getId(), client);
 
         return registerProxy(context, proxy,description,properties);
     } 
 
     /*
      * (non-Javadoc)
      * @see org.ow2.chameleon.rose.AbstractImporterComponent#destroyProxy(org.osgi.service.remoteserviceadmin.EndpointDescription, org.osgi.framework.ServiceRegistration)
      */
     public void destroyProxy(EndpointDescription description, ServiceRegistration registration){
     	if (proxies.containsKey(description.getId())) {
			// Unregister the proxy
			registration.unregister();

             Client client = proxies.remove(description.getId());
             // Close the proxy
             client.closeProxy(description.getId());
         } else {
             throw new IllegalArgumentException("The given object has not been created through this factory");
         }
     }
     
     /*
      * (non-Javadoc)
      * @see org.ow2.chameleon.rose.AbstractImporterComponent#getLogService()
      */
 	@Override
 	protected LogService getLogService() {
 		return logger;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.ow2.chameleon.rose.AbstractImporterComponent#getRoseMachine()
 	 */
 	@Override
 	protected RoseMachine getRoseMachine() {
 		return machine;
 	}
     
 	/*
 	 * (non-Javadoc)
 	 * @see org.ow2.chameleon.rose.ImporterService#getConfigPrefix()
 	 */
 	public List<String> getConfigPrefix() {
 		return asList(CONFIGS);
 	}
 
 
     /*------------------------------------------*
      *  Component LifeCycle method              *
      *------------------------------------------*/
 	
 	/*
 	 * (non-Javadoc)
 	 * @see org.ow2.chameleon.rose.AbstractImporterComponent#start()
 	 */
 	@Override
 	@Validate
 	protected void start(){
 		super.start();
 	}
 
     /*
      * (non-Javadoc)
      * @see org.ow2.chameleon.rose.AbstractImporterComponent#stop()
      */
     @Override
     @Invalidate
 	protected void stop() {
         super.stop();
     }
 
 
 }
