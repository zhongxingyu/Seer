 package eu.play_project.dcep.distributedetalis;
 
 import java.io.Serializable;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.objectweb.proactive.ActiveObjectCreationException;
 import org.objectweb.proactive.api.PAActiveObject;
 import org.objectweb.proactive.core.node.NodeException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import eu.play_project.dcep.distributedetalis.api.EcConnectionManager;
 import eu.play_project.dcep.distributedetalis.api.SimplePublishApi;
 import eu.play_project.play_platformservices.api.EpSparqlQuery;
 import fr.inria.eventcloud.api.CompoundEvent;
 import fr.inria.eventcloud.api.EventCloudId;
 import fr.inria.eventcloud.api.PublishApi;
 import fr.inria.eventcloud.api.PutGetApi;
 import fr.inria.eventcloud.api.SubscribeApi;
 import fr.inria.eventcloud.api.Subscription;
import fr.inria.eventcloud.api.exceptions.MalformedSparqlQueryException;
 import fr.inria.eventcloud.api.responses.SparqlSelectResponse;
 import fr.inria.eventcloud.exceptions.EventCloudIdNotManaged;
 import fr.inria.eventcloud.factories.ProxyFactory;
 
 public class EcConnectionManagerNet implements SimplePublishApi, Serializable, EcConnectionManager {
 
 	private static final long serialVersionUID = -368781636399635332L;
 
 	private String eventCloudRegistryUrl;
 
 	private Map<String, PublishApi> outputClouds;
 	private Map<String, SubscribeApi> inputClouds;
 	private Map<String, PutGetApi> putGetClouds;
 	private Map<SubscribeApi, SubscriptionUsage> subscriptions = new HashMap<SubscribeApi, SubscriptionUsage>();
 
 	private boolean init = false;
 	private Logger logger;
 	private EcConnectionListener eventCloudListener;
 	
 	public EcConnectionManagerNet(){}
 	public EcConnectionManagerNet(String eventCloudRegistry, DistributedEtalis dEtalis){
 		logger = LoggerFactory.getLogger(EcConnectionManagerNet.class);
 		
 		putGetClouds = new HashMap<String, PutGetApi>();
 		outputClouds = new HashMap<String, PublishApi>();
 		inputClouds = new HashMap<String, SubscribeApi>();
 		this.eventCloudRegistryUrl = eventCloudRegistry;
 		//try {
 			//TODO sobermeier, Run ConnectinListern in own thread.
 			//this.eventCloudListener = PAActiveObject.newActive(EcConnectionListener.class, new Object[] {});
 			this.eventCloudListener = new EcConnectionListener();
 			this.eventCloudListener.setDetalis(dEtalis);
 			this.init = true;
 		//} catch (ActiveObjectCreationException e) {
 		//	logger.error("Error while initializing event cloud listener.", e);
 		//} catch (NodeException e) {
 		//	logger.error("Error while initializing event cloud listener.", e);
 		//}
 	}
 		
 	/* (non-Javadoc)
 	 * @see eu.play_project.dcep.distributedetalis.EcConnectionManagerApi#getDataFromCloud(java.lang.String, java.lang.String)
 	 */
 	@Override
	public synchronized SparqlSelectResponse getDataFromCloud(String query, String cloudId) throws EventCloudIdNotManaged, MalformedSparqlQueryException {
 		if (!init) {
 			throw new IllegalStateException(this.getClass().getSimpleName() + " has not been initialized.");
 		}
 		
 		logger.info("Get data from EventCloud '" + cloudId + "' with query : " + query);
 
 		PutGetApi putGetCloud;
 		SparqlSelectResponse response = null;
 		try {
 			putGetCloud = getHistoricCloud(cloudId);
 			response = putGetCloud.executeSparqlSelect(query);
 		} catch (EventCloudIdNotManaged e) {
 			logger.error("Error while connecting to event cloud {}.", cloudId);
 			throw e;
 		}
 		return response;
 	}
 	
 	/* (non-Javadoc)
 	 * @see eu.play_project.dcep.distributedetalis.EcConnectionManagerApi#getHistoricCloud(java.lang.String)
 	 */
 	@Override
 	public PutGetApi getHistoricCloud(String cloudId) throws EventCloudIdNotManaged {
 		if (!init) {
 			throw new IllegalStateException(this.getClass().getSimpleName() + " has not been initialized.");
 		}
 		
 		
 		if (!putGetClouds.containsKey(cloudId)) {
 			PutGetApi proxy =  ProxyFactory.newPutGetProxy(eventCloudRegistryUrl, new EventCloudId(eu.play_project.play_commons.constants.Stream.toTopicUri(cloudId)));
 			putGetClouds.put(cloudId, proxy);
 		}
 		return putGetClouds.get(cloudId);
 	}
 
 	/* (non-Javadoc)
 	 * @see eu.play_project.dcep.distributedetalis.EcConnectionManagerApi#getInputCloud(java.lang.String)
 	 */
 	@Override
 	public SubscribeApi getInputCloud(String cloudId) throws EventCloudIdNotManaged {
 		if (!init) {
 			throw new IllegalStateException(this.getClass().getSimpleName() + " has not been initialized.");
 		}
 		
 		if (!inputClouds.containsKey(cloudId)) {
 			SubscribeApi proxy =  ProxyFactory.newSubscribeProxy(eventCloudRegistryUrl, new EventCloudId(cloudId));
 			inputClouds.put(cloudId, proxy);
 		}
 		return inputClouds.get(cloudId);
 	}
 
 	/* (non-Javadoc)
 	 * @see eu.play_project.dcep.distributedetalis.EcConnectionManagerApi#getOutputCloud(java.lang.String)
 	 */
 	@Override
 	public PublishApi getOutputCloud(String cloudId) throws EventCloudIdNotManaged {
 		if (!init) {
 			throw new IllegalStateException(this.getClass().getSimpleName() + " has not been initialized.");
 		}
 		
 		if (!outputClouds.containsKey(cloudId)) {
 			PublishApi proxy =  ProxyFactory.newPublishProxy(eventCloudRegistryUrl, new EventCloudId(cloudId));
 			outputClouds.put(cloudId, proxy);
 		}
 		return outputClouds.get(cloudId);
 	}
 	
 	/* (non-Javadoc)
 	 * @see eu.play_project.dcep.distributedetalis.EcConnectionManagerApi#publish(fr.inria.eventcloud.api.CompoundEvent)
 	 */
 	@Override
 	public void publish(CompoundEvent event) {
 		if (!init) {
 			throw new IllegalStateException(this.getClass().getSimpleName() + " has not been initialized.");
 		}
 		
 		String cloudId = EventCloudHelpers.getCloudId(event);
 		
 		try {
 			this.getOutputCloud(cloudId).publish(event);
 		} catch (EventCloudIdNotManaged e) {
 			logger.error("Event could not be published to cloud {}.", cloudId);
 		}
 	}
 	
 	/* (non-Javadoc)
 	 * @see eu.play_project.dcep.distributedetalis.EcConnectionManagerApi#registerEventPattern(eu.play_project.play_platformservices.api.EpSparqlQuery)
 	 */
 	@Override
 	public void registerEventPattern(EpSparqlQuery epSparqlQuery) {
 		for (String cloudId : epSparqlQuery.getQueryDetails().getInputStreams()) {
 			subscribe(cloudId);
 		}
 
 		// Treat output streams lazily: don't connect before a compex event is detected.
 	}
 	
 	
 	/* (non-Javadoc)
 	 * @see eu.play_project.dcep.distributedetalis.EcConnectionManagerApi#unregisterEventPattern(eu.play_project.play_platformservices.api.EpSparqlQuery)
 	 */
 	@Override
 	public void unregisterEventPattern(EpSparqlQuery epSparqlQuery) {
 		for (String cloudId : epSparqlQuery.getQueryDetails().getInputStreams()) {
 			try {
 					unsubscribe(cloudId, this.subscriptions.get(getInputCloud(cloudId)).sub);
 			}
 			catch (EventCloudIdNotManaged e) {
 				logger.error("Incurred unknown event cloud {}.", cloudId);
 			}
 		}
 
 		// TODO stuehmer: handle (i.e. close) other proxy connections, too? needs counters!
 	}
 
 	/* (non-Javadoc)
 	 * @see eu.play_project.dcep.distributedetalis.EcConnectionManagerApi#subscribe(java.lang.String)
 	 */
 	@Override
 	public Subscription subscribe(String cloudId) {
 		if (!init) {
 			throw new IllegalStateException(this.getClass().getSimpleName() + " has not been initialized.");
 		}
 		
 		Subscription sub = Subscription.any();
 
 		try {
 			if (this.subscriptions.containsKey(getInputCloud(cloudId))) {
 				logger.info("Still subscribed to eventcloud {}.", cloudId);
 				this.subscriptions.get(getInputCloud(cloudId)).usage++;
 			}
 			else {
 				logger.info("Subscribing to eventcloud {}.", cloudId);
 				this.getInputCloud(cloudId).subscribe(sub, eventCloudListener);
 				this.subscriptions.put(getInputCloud(cloudId), new SubscriptionUsage(sub));
 			}
 		}
 		catch (EventCloudIdNotManaged e) {
 			logger.error("Incurred unknown event cloud {}.", cloudId);
 		}
 		return sub;
 	}
 	
 	/* (non-Javadoc)
 	 * @see eu.play_project.dcep.distributedetalis.EcConnectionManagerApi#unsubscribe(java.lang.String, fr.inria.eventcloud.api.Subscription)
 	 */
 	@Override
 	public void unsubscribe(String cloudId, Subscription sub) {
 		try {
 			if (this.subscriptions.containsKey(getInputCloud(cloudId))) {
 				this.subscriptions.get(getInputCloud(cloudId)).usage--;
 				
 				if (this.subscriptions.get(getInputCloud(cloudId)).usage == 0) {
 					logger.info("Unsubscribing from eventcloud {}.", cloudId);
 					getInputCloud(cloudId).unsubscribe(sub.getId());
 					this.subscriptions.remove(getInputCloud(cloudId));
 					this.inputClouds.remove(cloudId);
 				}
 				else {
 					logger.info("Still subscribed to eventcloud {}.", cloudId);
 				}
 			}
 		} catch (EventCloudIdNotManaged e) {
 			logger.error("Incurred unknown event cloud {}.", cloudId);
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see eu.play_project.dcep.distributedetalis.EcConnectionManagerApi#destroy()
 	 */
 	@Override
 	public void destroy() {
 		logger.info("Unsubscribe from Event Clouds");
 		
 		// Unsubscribe
 		for (SubscribeApi proxy : subscriptions.keySet()) {
 			proxy.unsubscribe(subscriptions.get(proxy).sub.getId());
 		}
 		
 		subscriptions.clear();
 		inputClouds.clear();
 		outputClouds.clear();
 		
 		this.init = false;
 	}
 	
 	private class SubscriptionUsage implements Serializable {
 		
 		private static final long serialVersionUID = -6063251924935507681L;
 		
 		public SubscriptionUsage(Subscription sub) {
 			this.sub = sub;
 			this.usage = 1;
 		}
 		
 		public Subscription sub;
 		public int usage;
 	}
 }
