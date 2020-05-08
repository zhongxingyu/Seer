 package com.externc.coexist.services;
 
 import org.apache.http.HttpResponse;
 
 import android.content.Intent;
 
 import com.externc.coexist.Config;
 import com.externc.coexist.DebugLogger;
 import com.externc.coexist.DebugLogger.Level;
 import com.externc.coexist.api.API;
 import com.externc.coexist.api.SimpleResponse;
 import com.externc.coexist.api.Sync;
 
 public class CreateService extends BaseService {
 
 	public CreateService() {
 		super("create");
 	}
 
 	@Override
 	protected String targetApi() {
 		return "create";
 	}
 	
 	@Override
 	protected String getUpdateMessage() {
 		return "Sending form to server.";
 	}
 
 	@Override
 	protected void onHandleIntent(Intent intent) {
 		Sync sync = intent.getParcelableExtra("sync");
 		DebugLogger.log(this, Level.LOW, "Got the sync "+sync);
 		sendStartSync();
 		
 		Config conf = new Config(this);
 		
 		addParameter("version", conf.getVersion());
 		addParameter("sync", getSerializer().encode(sync));
 		
 		String url = getUrl();
		DebugLogger.log(this, Level.LOW, "Generaetd create url: " + url);
 		
 		try{
 			sendServiceProgressBroadcast();
 			DebugLogger.log(this, Level.LOW, "Starting the request.");
 			HttpResponse response = execute(url);
 			
 			SimpleResponse r = getSerializer().decode(response.getEntity().getContent(), SimpleResponse.class);
 			
 			if(r.getStatus() == 200){
 				sendEndSync(false, "Successfuly updated server.");
 				new API().sync(this);
 			}else{
 				sendEndSync(true, r.getMessage());
 				sendFinishedSyncBroadcast(false);
 			}
 			
 		}catch(Exception e){
 			sendEndSync(true, e.getMessage());
 			sendFinishedSyncBroadcast(false);
 		}
 		
 	}
 
 	
 
 }
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
