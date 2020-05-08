 package pegasus.eventbus.topology.service;
 
 import org.osgi.framework.BundleActivator;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.ServiceReference;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import pegasus.eventbus.client.EventManager;
 import pegasus.eventbus.topology.TopologyRegistry;
 
 public class Activator implements BundleActivator {
 
     protected static final Logger  LOG = LoggerFactory.getLogger(Activator.class);
 
     private static TopologyService topologyService;
 
     public void start(BundleContext bundleContext) throws Exception {
 
         LOG.info("OSGi Starting: {}", TopologyService.class.getName());
 
         ServiceReference eventManagerServiceReference = bundleContext.getServiceReference(EventManager.class.getName());
         if (eventManagerServiceReference != null) {
             EventManager eventManager = (EventManager) bundleContext.getService(eventManagerServiceReference);
             TopologyRegistry topologyRegistry = new TopologyRegistry();
             ClientRegistry clientRegistry = new ClientRegistry();
             RegistrationHandler registrationHandler = new RegistrationHandler(eventManager, clientRegistry, topologyRegistry);
            topologyService = new TopologyService(registrationHandler);
             topologyService.start();
 
             LOG.info("OSGi Started: {}", TopologyService.class.getName());
 
         } else {
 
             LOG.error("Unable to find EventManager service.");
 
         }
 
     }
 
     public void stop(BundleContext bundleContext) throws Exception {
 
         LOG.info("OSGi Stopping: {}", TopologyService.class.getName());
 
         if (topologyService != null) {
             topologyService.stop();
         }
 
         LOG.info("OSGi Stopped: {}", TopologyService.class.getName());
 
     }
 
 }
