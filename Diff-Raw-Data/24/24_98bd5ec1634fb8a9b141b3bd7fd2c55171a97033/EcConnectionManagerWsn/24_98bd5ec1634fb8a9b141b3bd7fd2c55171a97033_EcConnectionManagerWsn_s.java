 package eu.play_project.dcep.distributedetalis;
 
 import java.io.ByteArrayOutputStream;
 import java.io.Serializable;
 import java.net.URI;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 
 import javax.xml.namespace.QName;
 
 import org.apache.jena.riot.RDFDataMgr;
 import org.apache.jena.riot.RDFFormat;
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.servlet.ServletContextHandler;
 import org.eclipse.jetty.servlet.ServletHolder;
 import org.glassfish.jersey.moxy.json.MoxyJsonFeature;
 import org.glassfish.jersey.server.ResourceConfig;
 import org.glassfish.jersey.servlet.ServletContainer;
 import org.petalslink.dsb.commons.service.api.Service;
 import org.petalslink.dsb.notification.service.NotificationConsumerService;
 import org.petalslink.dsb.soap.CXFExposer;
 import org.petalslink.dsb.soap.api.Exposer;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.hp.hpl.jena.sparql.core.DatasetGraph;
 import com.hp.hpl.jena.sparql.core.DatasetGraphFactory;
 
 import eu.play_project.dcep.constants.DcepConstants;
 import eu.play_project.dcep.distributedetalis.api.DistributedEtalisException;
 import eu.play_project.dcep.distributedetalis.api.EcConnectionManager;
 import eu.play_project.dcep.distributedetalis.api.EcConnectionmanagerException;
 import eu.play_project.dcep.distributedetalis.join.SelectResults;
 import eu.play_project.dcep.distributedetalis.listeners.EcConnectionListenerRest;
 import eu.play_project.dcep.distributedetalis.listeners.EcConnectionListenerWsn;
 import eu.play_project.dcep.distributedetalis.utils.EventCloudHelpers;
 import eu.play_project.play_commons.constants.Stream;
 import eu.play_project.play_eventadapter.AbstractReceiverRest;
 import eu.play_project.play_eventadapter.AbstractSenderRest;
 import eu.play_project.play_platformservices.api.BdplQuery;
 import fr.inria.eventcloud.api.CompoundEvent;
 import fr.inria.eventcloud.api.PublishSubscribeConstants;
 import fr.inria.eventcloud.api.Quadruple;
 
 public abstract class EcConnectionManagerWsn implements EcConnectionManager {
 	private final Map<String, SubscriptionUsage> subscriptions = new HashMap<String, SubscriptionUsage>();
 	private final Logger logger = LoggerFactory.getLogger(EcConnectionManagerWsn.class);
 	protected boolean init = false;
 	private AbstractReceiverRest rdfReceiver;
 	private AbstractSenderRest rdfSender;
 	private final DistributedEtalis dEtalis;
 	private EcConnectionListenerWsn dsbListener;
 	private EcConnectionListenerRest dsbRestListener;
 	static final Properties constants = DcepConstants.getProperties();
 	public static final String SOAP_URI = constants.getProperty("dcep.notify.endpoint");
     public static final String REST_URI = constants.getProperty("dcep.notify.rest.local");
 	private Service notifyReceiverSoap;
 	private Server notifyReceiverRest;
 	
 	public EcConnectionManagerWsn(DistributedEtalis dEtalis) {
 		this.dEtalis = dEtalis;
 	}
 	
 	public void init() throws DistributedEtalisException {
 		if (init) {
 			throw new IllegalStateException(this.getClass().getSimpleName() + " has ALREADY been initialized.");
 		}
 
 		logger.info("Initialising {}.", this.getClass().getSimpleName());
 		
 		this.rdfReceiver = new AbstractReceiverRest() {};
 				
 		// Use an arbitrary topic as default:
 		this.rdfSender = new AbstractSenderRest(Stream.FacebookCepResults.getTopicQName()) {};
 
         /*
          * Expose the SOAP service to receive notifications
          */
 		try {
         	this.dsbListener = new EcConnectionListenerWsn(this.rdfReceiver);
         	this.dsbListener.setDetalis(this.dEtalis);
             
             QName interfaceName = new QName("http://docs.oasis-open.org/wsn/bw-2",
                     "NotificationConsumer");
             QName serviceName = new QName("http://docs.oasis-open.org/wsn/bw-2",
                     "NotificationConsumerService");
             QName endpointName = new QName("http://docs.oasis-open.org/wsn/bw-2",
                     "NotificationConsumerPort");
  
             final String notificationReceiverEndpointLocal = constants.getProperty("dcep.notify.endpoint.local");
             logger.info("Exposing notification endpoint at: {} which should be reachable at {}.", notificationReceiverEndpointLocal, SOAP_URI);
             NotificationConsumerService service = new NotificationConsumerService(interfaceName,
                     serviceName, endpointName, "NotificationConsumerService.wsdl", notificationReceiverEndpointLocal,
                     this.dsbListener);
             Exposer exposer = new CXFExposer();
             notifyReceiverSoap = exposer.expose(service);
             notifyReceiverSoap.start();
        } catch (Exception e) {
            throw new DistributedEtalisException("Error while starting DSB listener (SOAP service).", e);
        }
		finally {
        	destroy();
 		}
 		
         /*
          * Expose the REST service to receive notifications
          */
 		try {
         	this.dsbRestListener = new EcConnectionListenerRest(this.rdfReceiver);
         	this.dsbRestListener.setDetalis(this.dEtalis);
 
     		final ResourceConfig rc = new ResourceConfig()
    			.register(dsbRestListener)
    			.register(MoxyJsonFeature.class);
 
     		notifyReceiverRest = new Server(URI.create(REST_URI).getPort());
             ServletContextHandler context = new ServletContextHandler();
             context.setContextPath("/");
             ServletHolder h = new ServletHolder(new ServletContainer(rc));
             context.addServlet(h, "/");
             notifyReceiverRest.setHandler(context);
             notifyReceiverRest.start();
   	    } catch (Exception e) {
	        throw new DistributedEtalisException("Error while starting DSB listener (REST service).", e);
	    }
		finally {
        	destroy();
 		}
 		
 		init = true;
 	}
 	
 	@Override
 	public void destroy() {
 		logger.info("Terminating {}.", this.getClass()
 				.getSimpleName());
 		logger.info("Unsubscribe from Topics");
 	
 		// Unsubscribe
 		this.rdfReceiver.unsubscribeAll();
 		subscriptions.clear();
 		
     	if (this.notifyReceiverSoap != null) {
     		this.notifyReceiverSoap.stop();
     	}
 
     	if (this.notifyReceiverRest != null) {
     		try {
 				this.notifyReceiverRest.stop();
 			} catch (Exception e) {
 				logger.error("Exception while stoppping REST server. Nothing we can do now. " + e.getMessage());
 			}
     		this.notifyReceiverRest.destroy();
     	}
      	
   		init = false;
 	}
 
 	/**
 	 * Persist data in historic storage.
 	 * 
 	 * @param event event containing quadruples
 	 * @param cloudId the cloud ID to allow partitioning of storage
 	 */
 	public abstract void putDataInCloud(CompoundEvent event, String cloudId);
 
 	/**
 	 * Retreive data from historic storage using a SPARQL SELECT query. SPARQL 1.1
 	 * enhancements like the VALUES clause are allowed.
 	 */
 	@Override
 	public abstract SelectResults getDataFromCloud(String query, String cloudId)
 			throws EcConnectionmanagerException;
 
 	@Override
 	public void publish(CompoundEvent event) {
 		if (!init) {
 			throw new IllegalStateException(this.getClass().getSimpleName() + " has not been initialized.");
 		}
 		
 		String cloudId = EventCloudHelpers.getCloudId(event);
         
 		// Send event to DSB:
 		ByteArrayOutputStream out = new ByteArrayOutputStream();
 		RDFDataMgr.write(out, quadruplesToDatasetGraph(event), RDFFormat.TRIG_BLOCKS);
 		this.rdfSender.notify(new String(out.toByteArray()), cloudId);
 		
 		// Store event in Triple Store:
 		this.putDataInCloud(event, cloudId);
 	}
 
 	@Override
 	public void registerEventPattern(BdplQuery bdplQuery) throws EcConnectionmanagerException {
 		if (!init) {
 			throw new IllegalStateException(this.getClass().getSimpleName() + " has not been initialized.");
 		}
 		
 		for (String cloudId : bdplQuery.getDetails().getInputStreams()) {
 			subscribe(cloudId);
 		}
 
 		// Nothing to do for output streams, they are stateless
 	}
 
 	@Override
 	public void unregisterEventPattern(BdplQuery bdplQuery) {
 		for (String cloudId : bdplQuery.getDetails().getInputStreams()) {
 			SubscriptionUsage sub = this.subscriptions.get(cloudId);
 			if (sub != null) {
 				unsubscribe(cloudId, sub.sub);
 			}
 		}
 	}
 
 	/**
 	 * Subscribe to a given topic on the DSB. Duplicate subscriptions are handled using counters.
 	 */
 	private void subscribe(String cloudId) throws EcConnectionmanagerException {
 		if (!init) {
 			throw new IllegalStateException(this.getClass().getSimpleName() + " has not been initialized.");
 		}
 
 		if (this.subscriptions.containsKey(cloudId)) {
 			logger.info("Still subscribed to topic {}.", cloudId);
 			this.subscriptions.get(cloudId).usage++;
 		}
 		else {
 			logger.info("Subscribing to topic {}.", cloudId);
 			String subId = this.rdfReceiver.subscribe(cloudId, SOAP_URI);
 			this.subscriptions.put(cloudId, new SubscriptionUsage(subId));
 		}
 	}
 
 	/**
 	 * Unsubscribe from a given topic on the DSB. Duplicate subscriptions are handled using counters.
 	 */
 	private void unsubscribe(String cloudId, String subId) {
 		if (!init) {
 			throw new IllegalStateException(this.getClass().getSimpleName() + " has not been initialized.");
 		}
 		
 		if (this.subscriptions.containsKey(cloudId)) {
 			this.subscriptions.get(cloudId).usage--;
 			
 			if (this.subscriptions.get(cloudId).usage == 0) {
 				logger.info("Unsubscribing from topic {}.", cloudId);
 				rdfReceiver.unsubscribe(subId);
 				this.subscriptions.remove(cloudId);
 			}
 			else {
 				logger.info("Still subscribed to topic {}.", cloudId);
 			}
 		}
 	}
 	
 	/**
 	 * Usage counter for a subscription.
 	 */
 	private class SubscriptionUsage implements Serializable {
 		
 		private static final long serialVersionUID = 100L;
 		
 		public SubscriptionUsage(String sub) {
 			this.sub = sub;
 			this.usage = 1;
 		}
 		
 		public String sub;
 		public int usage;
 	}
 
     /**
      * A private method to convert a collection of quadruples into the
      * corresponding data set graph to be used in the event format writers
      * 
      * @author ialshaba
      * 
      * @param quads
      *            the collection of the quadruples
      * @return the corresponding data set graph
      */
     private static DatasetGraph quadruplesToDatasetGraph(CompoundEvent quads) {
         DatasetGraph dsg = DatasetGraphFactory.createMem();
         for (Quadruple q : quads) {
             if (q.getPredicate() != PublishSubscribeConstants.EVENT_NB_QUADRUPLES_NODE) {
                 dsg.add(
                         q.getGraph(), q.getSubject(), q.getPredicate(),
                         q.getObject());
             }
         }
 
         return dsg;
     }
 }
