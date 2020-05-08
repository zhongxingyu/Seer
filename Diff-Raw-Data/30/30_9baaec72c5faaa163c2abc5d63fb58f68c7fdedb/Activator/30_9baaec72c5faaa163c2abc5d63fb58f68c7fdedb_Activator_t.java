 package ar.edu.unicen.ringo.agent;
 
 import org.osgi.framework.BundleActivator;
 import org.osgi.framework.BundleContext;
 import org.osgi.service.http.HttpService;
 import org.osgi.util.tracker.ServiceTracker;
 
 import ar.edu.unicen.ringo.agent.api.AgentService;
 import ar.edu.unicen.ringo.persistence.PersistenceService;
 
 public class Activator implements BundleActivator {
 
     private BundleContext bc = null;
     private ServiceTracker<HttpService, HttpService> httpTracker = null;
     private ServiceTracker<PersistenceService, PersistenceService> persistenceTracker = null;
     private AgentApplication jaxRsApplication = null;
    private AgentService service;
 
     public void start(BundleContext context) throws Exception {
         this.bc = context;
        this.service = new AgentService();
        this.service.setSla(System.getProperty("agent.sla"));
        this.service.setNode(System.getProperty("agent.node"));
         jaxRsApplication = new AgentApplication(service);
 
         persistenceTracker = new ServiceTracker<>(context,
                 PersistenceService.class, new PersistenceServiceTracker(
                         context, service));
         persistenceTracker.open();
 
         httpTracker = new ServiceTracker<HttpService, HttpService>(bc,
                 HttpService.class, new HttpServiceTracker(context,
                         jaxRsApplication));
         httpTracker.open();
     }
 
     public void stop(BundleContext context) throws Exception {
         httpTracker.close();
         persistenceTracker.close();
        this.service = null;
     }
 
 }
