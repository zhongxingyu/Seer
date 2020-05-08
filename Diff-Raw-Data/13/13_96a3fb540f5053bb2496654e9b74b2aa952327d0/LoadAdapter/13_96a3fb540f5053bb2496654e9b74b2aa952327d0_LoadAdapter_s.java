 package adapters;
 
 import hubs.LoadHub;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 
 import org.codehaus.jackson.JsonGenerationException;
 import org.codehaus.jackson.map.JsonMappingException;
 import org.codehaus.jackson.node.ObjectNode;
 
 import models.Message;
 import models.Monitor;
 import models.TestDataEntity;
 
 import subscriptions.JsonSubscription;
 import subscriptions.listeners.JsonSubscriptionListener;
 import util.JSONHelper;
 
 import com.lightstreamer.interfaces.data.DataProviderException;
 import com.lightstreamer.interfaces.data.FailureException;
 import com.lightstreamer.interfaces.data.ItemEventListener;
 import com.lightstreamer.interfaces.data.SmartDataProvider;
 import com.lightstreamer.interfaces.data.SubscriptionException;
 
 public class LoadAdapter implements SmartDataProvider {
 	private ItemEventListener listener;
 	public static JsonSubscription subscription = new JsonSubscription();
 	private final ConcurrentHashMap<String, Object> echoSubscribers = new ConcurrentHashMap<>();
 	private Object handleMutex = new Object();
 	private static final LoadHub _loadHub = new LoadHub();
 	private static final Monitor _monitor = Monitor.getInstance();
 
 	@Override
 	public void init(Map arg0, File arg1) throws DataProviderException {
 
 	}
 
 	@Override
 	public boolean isSnapshotAvailable(String arg0)
 			throws SubscriptionException {
 		return false;
 	}
 
 	@Override
 	public void setListener(ItemEventListener listener) {
 		this.listener = listener;
 	}
 
 	@Override
 	public void unsubscribe(String arg0) throws SubscriptionException,
 			FailureException {
 	}
 
 	@Override
 	public void subscribe(String arg0, boolean arg1)
 			throws SubscriptionException, FailureException {
 	}
 
 	@Override
 	public void subscribe(String itemName, Object itemHandle, boolean needsIterator) throws SubscriptionException, FailureException {
 		if(itemName.equals("broadcastJson")) {
 			LoadJsonSubscriptionListener jsonListener = new LoadJsonSubscriptionListener();
 			jsonListener.broadCastHandle = itemHandle;
 			jsonListener.listener = listener;
 			subscription.setListener(jsonListener);
 		} else {
 			synchronized (handleMutex) {			
 				if(echoSubscribers.containsKey(itemName)) {
 					throw new IllegalArgumentException("Client with id: " + itemName + " tried to subscribe more than one time");
 				} else {
 					echoSubscribers.put(itemName, itemHandle);
 				}
 			}
 		}
 	}
 	
 	private class LoadJsonSubscriptionListener implements JsonSubscriptionListener {
 		public Object broadCastHandle;
 		public ItemEventListener listener;
 		
 		@Override
 		public void initTest(String cid, String testToRun, int numberOfClients, int spacing, long startTime) {
 			_loadHub.initTest(testToRun, numberOfClients, spacing, startTime);
 			ObjectNode response = JSONHelper.newObject();
 			response.put("messageKind", "initTest");
 			response.put("cid", cid);
 			response.put("testToRun", testToRun);
 			
 			sendToAll(response.toString());
 		}
 
 		@Override
 		public void echo(String cid, Message message) throws JsonGenerationException, JsonMappingException, IOException {
 			_loadHub.echo(message);
 			ObjectNode response = JSONHelper.newObject();
     		response.put("messageKind", "receiveMessage");
     		response.put("cid", cid);
     		response.put("data", JSONHelper.writeObjectToJson(message));
     		
     		Object handle = echoSubscribers.get(cid);
     		send(response.toString(), handle);    		
 		}
 
 		@Override
 		public void broadcast(String cid, Message message) throws JsonGenerationException, JsonMappingException, IOException {
 			_loadHub.broadcast(message);
 			ObjectNode response = JSONHelper.newObject();
     		response.put("messageKind", "receiveMessage");
     		response.put("cid", cid);
     		response.put("data", JSONHelper.writeObjectToJson(message));
     		
     		sendToAll(response.toString());
 		}
 
 		@Override
 		public void complete(String cid) {
 			if(_loadHub.complete(cid)) {
 				ObjectNode response = JSONHelper.newObject();
 	    		response.put("messageKind", "harvest");
 	    		response.put("cid", cid);
 				
 	    		sendToAll(response.toString());
 			}
 		}
 
 		@Override
 		public void getData(String cid, TestDataEntity testData, int numberOfClientsInBrowser) throws JsonGenerationException, JsonMappingException, IOException {
 			if(_loadHub.getData(testData, numberOfClientsInBrowser)){
     			ObjectNode response = JSONHelper.newObject();
     			response.put("messageKind", "harvestComplete");
     			response.put("cid", cid);
     			ObjectNode dataObj = JSONHelper.newObject();
     			dataObj.put("Duration", _monitor.duration);
     			dataObj.put("StartTime", _monitor.startTime);
     			dataObj.put("SentFromClientEvents", JSONHelper.writeListToJson(_monitor.sentFromClientEvents));
     			dataObj.put("ReceivedAtServerEvents", JSONHelper.writeListToJson(_monitor.receivedAtServerEvents));
     			dataObj.put("SentFromServerEvents", JSONHelper.writeListToJson(_monitor.sentFromServerEvents));
     			dataObj.put("Spacing", _monitor.spacing);
     			dataObj.put("TestDataEntities", JSONHelper.writeListToJson(_monitor.testDataEntities));    			
     			
     			response.put("data", dataObj);
     			
     			sendToAll(response.toString());
 			}
 		}
 		
 		private void sendToAll(String json) {
 			HashMap<String, String> update = new HashMap<>();
			update.put("json", json);
 			listener.smartUpdate(broadCastHandle, update, false);
 		}
 		
 		private void send(String json, Object handle) {
 			HashMap<String, String> update = new HashMap<>();
			update.put("json", json);
 			listener.smartUpdate(handle, update, false);
 		}
 		
 	}
 
 }
