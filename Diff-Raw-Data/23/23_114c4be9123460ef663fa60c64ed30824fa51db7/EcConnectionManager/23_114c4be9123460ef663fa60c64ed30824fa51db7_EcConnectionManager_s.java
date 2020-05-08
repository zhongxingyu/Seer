 package eu.play_project.dcep.distributedetalis.api;
 
 import eu.play_project.play_platformservices.api.EpSparqlQuery;
 import fr.inria.eventcloud.api.CompoundEvent;
 import fr.inria.eventcloud.api.PublishApi;
 import fr.inria.eventcloud.api.PutGetApi;
 import fr.inria.eventcloud.api.SubscribeApi;
 import fr.inria.eventcloud.api.Subscription;
 import fr.inria.eventcloud.api.responses.SparqlSelectResponse;
 import fr.inria.eventcloud.exceptions.EventCloudIdNotManaged;
 
 public interface EcConnectionManager extends SimplePublishApi{
 
 	public abstract SparqlSelectResponse getDataFromCloud(String query,
			String cloudId) throws EventCloudIdNotManaged;
 
 	/**
 	 * Reuses or initiates an event cloud proxy.
 	 */
 	public abstract PutGetApi getHistoricCloud(String cloudId)
 			throws EventCloudIdNotManaged;
 
 	/**
 	 * Reuses or initiates an event cloud proxy.
 	 */
 	public abstract SubscribeApi getInputCloud(String cloudId)
 			throws EventCloudIdNotManaged;
 
 	/**
 	 * Reuses or initiates an event cloud proxy.
 	 */
 	public abstract PublishApi getOutputCloud(String cloudId)
 			throws EventCloudIdNotManaged;
 
 	public abstract void publish(CompoundEvent event);
 
 	public abstract void registerEventPattern(EpSparqlQuery epSparqlQuery);
 
 	public abstract void unregisterEventPattern(EpSparqlQuery epSparqlQuery);
 
 	public abstract Subscription subscribe(String cloudId);
 
 	public abstract void unsubscribe(String cloudId, Subscription sub);
 
 	public abstract void destroy();
 
 }
