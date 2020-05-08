 package edu.stanford.cs.gaming.sdk.service;
 
 
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
   public BlockingQueue<AppResponse> responseQ;   
   public Thread processThread;
   public GamingService gamingService;
   public String tag;
 
 
   public App(int appId, GamingService gamingService) {
 	  this.appId = appId;
 	  this.intent = new Intent("edu.stanford.cs.gaming.sdk." + appId + ".Event");
       this.requestQ = new LinkedBlockingQueue<AppRequest>(); 
       this.responseQ = new LinkedBlockingQueue<AppResponse>(); 
       this.gamingService = gamingService;
       tag = "App " + appId + "thread";
       processThread = new ProcessThread();
       processThread.start();
       
       
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
				  
 				  //ASLAI: PUT THEM INTO SEPARATE QUEUES
 				  responseQ.put(response);
				  Log.d(tag, "INTENTFILTEREVENT IS: " + request.intentFilterEvent);
   		          gamingService.sendBroadcast(new Intent(request.intentFilterEvent));
 				  
 			  }
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	  }	  
   }
 }
