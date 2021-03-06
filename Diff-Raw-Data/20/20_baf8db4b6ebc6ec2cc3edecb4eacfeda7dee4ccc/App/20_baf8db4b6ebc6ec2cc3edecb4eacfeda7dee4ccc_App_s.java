 package edu.stanford.cs.gaming.sdk.service;
 
 
 import java.util.Hashtable;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingQueue;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.Intent;
 import android.util.Log;
 import edu.stanford.cs.gaming.sdk.model.*;
 
 public class App {
   public int appId;
   public Intent intent;
   public BlockingQueue<AppRequest> requestQ; 
   public Hashtable<String, LinkedBlockingQueue<AppResponse>> responseQs;   
   public Thread processThread;
   public GamingService gamingService;
   public String tag;
   public int userId;
 
 
   public App(int appId, GamingService gamingService) {
 	  this.appId = appId;
 	  this.intent = new Intent("edu.stanford.cs.gaming.sdk." + appId + ".Event");
       this.requestQ = new LinkedBlockingQueue<AppRequest>(); 
       this.responseQs = new Hashtable<String, LinkedBlockingQueue<AppResponse>>();
       this.gamingService = gamingService;
       tag = "App " + appId + "thread";
       processThread = new ProcessThread();
       processThread.start();
       
       
   }
   public void setUserId(int userId) {
 	  this.userId = userId;
   }
   
   public void addQueue(String intentFilterEvent) {
 	  if (responseQs.get(intentFilterEvent) == null) {
 		  responseQs.put(intentFilterEvent, new LinkedBlockingQueue<AppResponse>()); 
 	  }
   }
   
   public void stopService() {
 	  processThread.interrupt();
   }
   
   class ProcessThread extends Thread {
 	  public void run() {
 		  try {
 			  while (true) {
 				  Log.d(tag, "Request waiting on new request");
 				  
 				  AppRequest request = requestQ.take();
 				  Log.d(tag, "Request received is: " + request);				  
 				  if ("message".equals(request.action)) {
 					  Log.d(tag, "REQUEST OBJECT IS: " + request.object.getClass().getName());
 					  Message msg = (Message) request.object;
					  String[] tags = new String[msg.toUsers.length];
 					  for (int i=0; i < msg.toUsers.length; i++) {
 //						  tags[i] = new String(GamingServiceConnection.GAMING_SERVICE_PREFIX + 
 //								  "." + msg.toUsers[i]);
 						  tags[i] = "" + msg.toUsers[i].id;						  
 						  Log.d(tag, "HERE HERE IN MESSAGE LOOP");
 					  }
 		                Log.d(tag, "MESSAGE BEFORE POST: ");
 		                gamingService.getConcierge().postMessage((JSONObject) Util.toJson(request), tags);
 		                Log.d(tag, "MESSAGE POSTED: ");
 					  
 				  } else {
 //				  sleep(2000);
 //				  AppResponse response = new AppResponse();
 				  AppResponse response = null;
 				  try {
 //					  Log.d(tag, "Group returned is: " + Util.makeGet(gamingService.gamingServer + "/groups/1"));
 //					response.object = Util.fromJson(new JSONObject(Util.makeGet(gamingService.gamingServer + "/groups/1")), null, null);
 //						response.object = Util.fromJson(new JSONObject(Util.makeRequest(request)), null, null);
 						response = (AppResponse) Util.fromJson(new JSONObject(Util.makeRequest(request)), null, null);					  
 
 				  } catch (JSONException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				  response.request_id = request.id;
 				  response.appRequest = request;				  
 //				  response.object = request;
 				  Log.d(tag, "RESPONSE RECEIVED FROM SERVER IS " + response);
 				  //ASLAI: PUT THEM INTO SEPARATE QUEUES
 				  LinkedBlockingQueue<AppResponse> responseQ = responseQs.get(request.intentFilterEvent);
 				  if (responseQ != null) {
 				  responseQ.put(response);
 				  Log.d(tag, "INTENTFILTEREVENT123 IS: " + request.intentFilterEvent);
   		          gamingService.sendBroadcast(new Intent(request.intentFilterEvent));
 				  }
 			  }
 			  }
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	  }	  
   }
 }
