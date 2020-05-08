 package subscriptions;
 
 import java.io.IOException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import models.Message;
 import models.TestDataEntity;
 
 import org.codehaus.jackson.JsonNode;
 import org.codehaus.jackson.JsonProcessingException;
 import org.codehaus.jackson.node.ArrayNode;
 import org.codehaus.jackson.node.ObjectNode;
 
 import subscriptions.listeners.JsonSubscriptionListener;
 import util.JSONHelper;
 
 public class JsonSubscription {
 	private final ExecutorService executor;
 	private JsonSubscriptionListener listener;
 	
 	public JsonSubscription() {
 		executor = Executors.newCachedThreadPool(); //test vs newSingleThreadExecutor()?
 	}
 	
 	public synchronized void routeMessage(String json) throws JsonProcessingException, IOException {
 		JsonNode event = JSONHelper.getJsonNodeFromJson(json);
 		JSONHelper helper = new JSONHelper(event);		
     	String messageKind = helper.getMessageKind();
     	String cid = helper.getCid();    
     	ArrayNode data = helper.getArrayNode("data");
     	
     	if(messageKind.equals("initTest")) {
     		String testToRun = JSONHelper.getValueAt(0, data).asText();
     		int numberOfClients = JSONHelper.getValueAt(1, data).asInt();
     		int spacing = JSONHelper.getValueAt(2, data).asInt();
     		long startTime = JSONHelper.getValueAt(3, data).asLong();
     		
     		initTest(cid, testToRun, numberOfClients, spacing, startTime);
     	} else if(messageKind.equals("echo")) {
     		ObjectNode node = (ObjectNode)JSONHelper.getValueAt(0, data);
     		Message message = JSONHelper.getObject(node, Message.class);
     		
     		echo(cid, message);
     	} else if(messageKind.equals("broadcast")) {
     		ObjectNode node = (ObjectNode)JSONHelper.getValueAt(0, data);
     		Message message = JSONHelper.getObject(node, Message.class);
     		
     		broadcast(cid, message);
     	} else if(messageKind.equals("complete")) {
     		String clientId = JSONHelper.getValueAt(0, data).asText();	
     		complete(clientId);
     	} else if(messageKind.equals("getData")) {
     		ObjectNode node = (ObjectNode)JSONHelper.getValueAt(0, data);
     		TestDataEntity testData = JSONHelper.getObject(node, TestDataEntity.class);
     		int numberOfClientsInBrowser = JSONHelper.getValueAt(1, data).asInt();
     		getData(cid, testData, numberOfClientsInBrowser);
     	}
 	}
 	
 	private void initTest(final String cid, final String testToRun, final int numberOfClients, final int spacing, final long startTime) {
 		final JsonSubscriptionListener localListener = listener;
 		Runnable task = new Runnable() {
 			@Override
 			public void run() {
 				localListener.initTest(cid, testToRun, numberOfClients, spacing, startTime);				
 			}			
 		};
 		
 		executor.execute(task);
 	}
 	private void echo(final String cid, final Message message) {
 		final JsonSubscriptionListener localListener = listener;
 		Runnable task = new Runnable() {
 			@Override
 			public void run() {
 				try {
 					localListener.echo(cid, message);
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}			
 		};
 		
 		executor.execute(task);
 	}
 	private void broadcast(final String cid, final Message message) {
 		final JsonSubscriptionListener localListener = listener;
 		Runnable task = new Runnable() {
 			@Override
 			public void run() {
 				try {
 					localListener.broadcast(cid, message);			
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}			
 		};
 		
 		executor.execute(task);
 	}
 	private void complete(final String cid) {
 		final JsonSubscriptionListener localListener = listener;
 		Runnable task = new Runnable() {
 			@Override
 			public void run() {
 				localListener.complete(cid);
 			}			
 		};
 		
 		executor.execute(task);
 	}
 	
 	private void getData(final String cid, final TestDataEntity testData, final int numberOfClientsInBrowser)  {
 		final JsonSubscriptionListener localListener = listener;
 		Runnable task = new Runnable() {
 			@Override
 			public void run() {
 				try {
 					localListener.getData(cid, testData, numberOfClientsInBrowser) ;
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}			
 		};
 		
 		executor.execute(task);
 	}
 	
 	public synchronized void setListener(JsonSubscriptionListener listener) {
 		this.listener = listener;
 	}
 }
