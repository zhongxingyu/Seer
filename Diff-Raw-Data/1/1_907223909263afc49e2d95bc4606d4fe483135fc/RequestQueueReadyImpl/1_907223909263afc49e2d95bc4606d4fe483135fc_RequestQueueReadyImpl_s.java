 package aic12.project3.service.requestManagement;
 
 import java.net.URI;
 import java.util.Date;
 import java.util.HashMap;
 
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.UriBuilder;
 
 import org.springframework.beans.factory.annotation.Autowired;
 
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.WebResource;
 import com.sun.jersey.api.client.config.ClientConfig;
 import com.sun.jersey.api.client.config.DefaultClientConfig;
 import com.sun.jersey.api.json.JSONConfiguration;
 
 import aic12.project3.common.beans.SentimentProcessingRequest;
 import aic12.project3.common.beans.SentimentRequest;
 import aic12.project3.common.enums.REQUEST_QUEUE_STATE;
 import aic12.project3.service.util.ManagementConfig;
 
 /**
  * Main implementation of the Request Queue
  * @author johannes
  *
  */
 public class RequestQueueReadyImpl extends RequestQueueReady {
 
 	private static RequestQueueReadyImpl instance = new RequestQueueReadyImpl();
 
 	@Autowired private ManagementConfig config;
 	/**
 	 * Singleton method
 	 */
 	private RequestQueueReadyImpl(){}
 
 	/**
 	 * Singleton method, returns request queue
 	 * @return
 	 */
 	public static RequestQueueReadyImpl getInstance(){
 		return instance;
 	}
 
 	/**
 	 * Add a request (update or new)
 	 */
 	@Override
 	public void addRequest(SentimentRequest req) {
 		// Put request into Queue
 		readyQueue.put(req.getId(), req);
 
 		// TODO: ENable
 		// And save request to DB
 		saveRequestToDB(req.getId());
 		
 		// Delete request from queue if done
 		if (req.getState()==REQUEST_QUEUE_STATE.ARCHIVED){
 			readyQueue.remove(req.getId());
 		}
 
 		// Inform all Observers
 		super.setChanged();
 		super.notifyObservers(req.getId());
 
 		// TODO: Remove
 		logger.info("Request added");
 	}
 
 	/**
 	 * Return the request Queue
 	 */
 	public HashMap<String, SentimentRequest> getRequestQueue(){
 		return readyQueue;
 	}
 
 	/**
 	 * Save request to Database
 	 */
 	@Override
 	protected void saveRequestToDB(String id){
 		// TODO
 		SentimentRequest s = readyQueue.get(id);
 
 		URI uri = UriBuilder.fromUri(config.getProperty("databaseServer"))
 				.path(config.getProperty("databaseDeployment"))
 				.path(config.getProperty("databaseRequestRestPath"))
 				.path("insert")
 				.build();
 
 		// Jersey Client Config
 		ClientConfig config = new DefaultClientConfig();
 		config.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, true);
 		Client client = Client.create(config);
 
 		WebResource resource = client.resource(uri);
 		resource.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON).post(ClientResponse.class, s);
 
 	}
 
 	/**
 	 * Delete a request from the queue
 	 */
 	@Override
 	public void deleteRequestFromQueue(String id) {
 		// Remove from Queue
 		this.readyQueue.remove(id);		
 	}
 
 
 
 }
