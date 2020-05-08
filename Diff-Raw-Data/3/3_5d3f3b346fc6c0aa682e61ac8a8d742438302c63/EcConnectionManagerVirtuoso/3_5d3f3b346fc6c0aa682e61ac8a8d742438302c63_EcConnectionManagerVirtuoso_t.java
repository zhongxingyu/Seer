 package eu.play_project.dcep.distributedetalis;
 
 import java.io.ByteArrayOutputStream;
 import java.io.Serializable;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 import javax.xml.namespace.QName;
 
 import org.apache.jena.riot.RDFDataMgr;
 import org.apache.jena.riot.RDFFormat;
 import org.ontoware.rdf2go.impl.jena.TypeConversion;
 import org.petalslink.dsb.commons.service.api.Service;
 import org.petalslink.dsb.notification.service.NotificationConsumerService;
 import org.petalslink.dsb.soap.CXFExposer;
 import org.petalslink.dsb.soap.api.Exposer;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import virtuoso.jdbc4.VirtuosoDataSource;
 
 import com.hp.hpl.jena.sparql.core.DatasetGraph;
 import com.hp.hpl.jena.sparql.core.DatasetGraphFactory;
 
 import eu.play_project.dcep.constants.DcepConstants;
 import eu.play_project.dcep.distributedetalis.api.DistributedEtalisException;
 import eu.play_project.dcep.distributedetalis.api.EcConnectionManager;
 import eu.play_project.dcep.distributedetalis.api.EcConnectionmanagerException;
 import eu.play_project.dcep.distributedetalis.join.ResultRegistry;
 import eu.play_project.dcep.distributedetalis.join.SelectResults;
 import eu.play_project.dcep.distributedetalis.utils.EventCloudHelpers;
 import eu.play_project.play_commons.constants.Stream;
 import eu.play_project.play_eventadapter.AbstractReceiverRest;
 import eu.play_project.play_eventadapter.AbstractSenderRest;
 import eu.play_project.play_platformservices.api.BdplQuery;
 import fr.inria.eventcloud.api.CompoundEvent;
 import fr.inria.eventcloud.api.PublishSubscribeConstants;
 import fr.inria.eventcloud.api.Quadruple;
 
 public class EcConnectionManagerVirtuoso implements EcConnectionManager {
 	private final Map<String, SubscriptionUsage> subscriptions = new HashMap<String, SubscriptionUsage>();
 	private Connection virtuosoConnection;
 	private final Logger logger = LoggerFactory.getLogger(EcConnectionManagerVirtuoso.class);
 	private boolean init = false;
 	private AbstractReceiverRest rdfReceiver;
 	private AbstractSenderRest rdfSender;
 	private final DistributedEtalis dEtalis;
 	private EcConnectionListenerVirtuoso dsbListener;
 	static final Properties constants = DcepConstants.getProperties();
 	public static String notificationReceiverEndpoint = constants.getProperty("dcep.notify.endpoint");
 	private Service notifyReceiverServer;
 
 
 	public EcConnectionManagerVirtuoso(DistributedEtalis dEtalis) throws DistributedEtalisException {
 		this(
 				constants.getProperty("dcep.virtuoso.servername"),
 				Integer.parseInt(constants.getProperty("dcep.virtuoso.port")),
 				constants.getProperty("dcep.virtuoso.user"),
 				constants.getProperty("dcep.virtuoso.password"),
 				dEtalis
 				);
 	}
 	
 	public EcConnectionManagerVirtuoso(String server, int port, String user, String pw, DistributedEtalis dEtalis) throws DistributedEtalisException {
 		VirtuosoDataSource virtuoso = new VirtuosoDataSource();
 		virtuoso.setServerName(server);
 		virtuoso.setPortNumber(port);
 		virtuoso.setUser(user);
 		virtuoso.setPassword(pw);
 		this.dEtalis = dEtalis;
 
 		// Test Virtuoso JDBC connection
 		try {
 			virtuoso.getConnection().close();
 			virtuosoConnection = virtuoso.getConnection();
 		} catch (SQLException e) {
 			throw new DistributedEtalisException("Could not connect to Virtuoso.", e);
 		}
 
 		init();
 	}
 	
 	private void init() throws DistributedEtalisException {
 		if (init) {
 			throw new IllegalStateException(this.getClass().getSimpleName() + " has ALREADY been initialized.");
 		}
 
 		logger.info("Initialising {}.", this.getClass().getSimpleName());
 		
		this.rdfReceiver = new AbstractReceiverRest() {};
 				
 		// Use an arbitrary topic as default:
 		this.rdfSender = new AbstractSenderRest(Stream.FacebookCepResults.getTopicQName()) {};
 
 		try {
         	this.dsbListener = new EcConnectionListenerVirtuoso(this.rdfReceiver);
         	this.dsbListener.setDetalis(this.dEtalis);
             
             QName interfaceName = new QName("http://docs.oasis-open.org/wsn/bw-2",
                     "NotificationConsumer");
             QName serviceName = new QName("http://docs.oasis-open.org/wsn/bw-2",
                     "NotificationConsumerService");
             QName endpointName = new QName("http://docs.oasis-open.org/wsn/bw-2",
                     "NotificationConsumerPort");
             // expose the service
             final String notificationReceiverEndpointLocal = constants.getProperty("dcep.notify.endpoint.local");
             logger.info("Exposing notification endpoint at: {} which should be reachable at {}.", notificationReceiverEndpointLocal, notificationReceiverEndpoint);
             NotificationConsumerService service = new NotificationConsumerService(interfaceName,
                     serviceName, endpointName, "NotificationConsumerService.wsdl", notificationReceiverEndpointLocal,
                     this.dsbListener);
             Exposer exposer = new CXFExposer();
             notifyReceiverServer = exposer.expose(service);
             notifyReceiverServer.start();
 
         } catch (Exception e) {
         	
         	if (notifyReceiverServer != null) {
         		notifyReceiverServer.stop();
         	}
             throw new DistributedEtalisException("Error while starting DSB listener.", e);
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
 		
     	if (this.notifyReceiverServer != null) {
     		this.notifyReceiverServer.stop();
     	}
     	
   		init = false;
 	}
 
 	/**
 	 * Persist data in historic storage.
 	 * 
 	 * @param event event containing quadruples
 	 * @param cloudId the cloud ID to allow partitioning of storage
 	 */
 	public void putDataInCloud(CompoundEvent event, String cloudId) {
 
 		StringBuilder s = new StringBuilder();
 		s.append("SPARQL INSERT INTO GRAPH <").append(event.getGraph().toString()).append("> {\n");
 		for (Quadruple quadruple : event) {
 			s.append(TypeConversion.toRDF2Go(quadruple.getSubject()).toSPARQL()).append(" ");
 			s.append(TypeConversion.toRDF2Go(quadruple.getPredicate()).toSPARQL()).append(" ");
 			s.append(TypeConversion.toRDF2Go(quadruple.getObject()).toSPARQL()).append(" . \n");
 		}
 		s.append("}\n");
 		String query = s.toString();
 		
 		logger.debug("Putting event in cloud " + cloudId + ":\n" + query);
 		try {
 			Statement st = virtuosoConnection.createStatement();
 			st.executeUpdate(query);
 		} catch (SQLException e) {
 			logger.error("Error putting an event into Virtuoso.", e);
 		}
 		
    	}
 
 	/**
 	 * Retreive data from historic storage using a SPARQL SELECT query. SPARQL 1.1
 	 * enhancements like the VALUES clause are allowed.
 	 */
 	@Override
 	public synchronized SelectResults getDataFromCloud(String query, String cloudId)
 			throws EcConnectionmanagerException {
 		if (!init) {
 			throw new IllegalStateException(this.getClass().getSimpleName() + " has not been initialized.");
 		}
 
 		logger.debug("Sending historical query to Virtuoso: \n" + query);
 
 		List<String> variables = new ArrayList<String>();
 		List<List> result = new ArrayList<List>();
 
 		Connection con = null;
 		ResultSet res = null;
 		try {
 			con = virtuosoConnection;
 			Statement sta = con.createStatement();
 			res = sta.executeQuery("sparql "+query);
 
 			ResultSetMetaData rmd = res.getMetaData();
 			int colNum = rmd.getColumnCount();
 			for(int i = 1; i <= colNum; i++){
 				variables.add(rmd.getColumnName(i));
 			}
 			logger.debug("Vars: {}", variables);
 
 			//TODO result create, select variable analyze, create
 			while(res.next()){
 				ArrayList<Object> data = new ArrayList<Object>();
 				for(int i = 1; i <= colNum; i++) {
 					data.add(res.getObject(i));
 				}
 				result.add(data);
 				logger.debug("Data: {}", data);
 			}
 
 		} catch (SQLException e) {
 			throw new EcConnectionmanagerException("Exception with Virtuoso.", e);
 		} finally {
 			try {
 				if (res != null) {
 					res.close();
 				}
 				if(con != null) {
 					con.close();
 				}
 			} catch (SQLException e) {
 				// Do nothing
 			}
 		}
 
 		ResultRegistry rr = new ResultRegistry();
 		rr.setResult(result);
 		rr.setVariables(variables);
 		return rr;
 	}
 
 	/**
 	 * Produce a topic {@linkplain QName} for a given cloud ID.
 	 */
 	private QName getTopic(String cloudId) {
 		int index = cloudId.lastIndexOf("/");
 		return new QName(cloudId.substring(0, index+1), cloudId.substring(index + 1), "s");
 	}
 
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
 		
 		// Store event in Virtuoso:
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
 			unsubscribe(cloudId, this.subscriptions.get(cloudId).sub);
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
 			String subId = this.rdfReceiver.subscribe(cloudId, notificationReceiverEndpoint);
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
