 package edu.stanford.cs.gaming.sdk;
 
 import org.json.JSONObject;
 
 import edu.stanford.cs.gaming.sdk.model.AppRequest;
 import edu.stanford.cs.gaming.sdk.model.*;
 import edu.stanford.cs.gaming.sdk.service.*;
 
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.Bundle;
 import android.os.RemoteException;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 
 public class GamingTestActivity extends Activity {
 	
 	private static final String TAG = "GamingTestActivity";	
     private boolean binded = false;
 	Intent loggerIntent = null;
 	private GamingServiceConnection gameConn;
 	private GameReceiver receiver;
 	private TextView tv;
 	private int counter = 0;
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         receiver = new GameReceiver();
         setContentView(R.layout.main); 
         tv = (TextView) findViewById(R.id.TextView01);
         gameConn = new GamingServiceConnection(this, receiver, 1, "", "TestActivity");
         Button bindButton = (Button) findViewById(R.id.BindButton);
         bindButton.setOnClickListener(new OnClickListener() {
           	public void onClick(View v){
           		gameConn.bind();
                 gameConn.setUserId(1);          		
           	}});
         Button unbindButton = (Button) findViewById(R.id.UnbindButton);
         unbindButton.setOnClickListener(new OnClickListener() {
           	public void onClick(View v){
           		gameConn.unbind();
           	}});        
         Button putObjButton = (Button) findViewById(R.id.PutObjButton);
         putObjButton.setOnClickListener(new OnClickListener() {
           	public void onClick(View v){
  //         		try {
 
           			Log.d(TAG, "Object: Test" + counter);
 //					gameConn.grs.putGameObject((new GameObject("Test" + (counter++))).toJson().toString());
 					try {
 //						gameConn.grs.putGameObject(Util.toJson(new GameObject("Test" + (counter++))).toString());
 //						Log.d(TAG, "USER OBJECT JSON IS" + Util.toJson(new User()).toString());
 //						gameConn.grs.putGameObject(Util.toJson(new User()).toString());
 //						AppRequest appRequest = new AppRequest(1, 1, "");
 //						Log.d(TAG, "Request sent is: " + appRequest);
 //						gameConn.grs.sendRequest(1, Util.toJson(appRequest).toString());
						gameConn.createGroup(1, new Group("Test Group 12345"));
 //						gameConn.getGroup(2, 1);
 //						gameConn.getGroups(3, "Test Group", -1, -1, -1);
 //						gameConn.deleteGroup(4, 1);
 					} catch (RemoteException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 
 //          		} catch (RemoteException e) {
 					// TODO Auto-generated catch block
 //					e.printStackTrace();
 //				}
           	}});                
         
       
     }
     
     
 
 	    class GameReceiver extends BroadcastReceiver {
 			public void onReceive(Context context, Intent intent) {
 				Log.d(TAG, "onReceiver in GameReceiver");			
 				try {
 					Log.d(TAG, "GameReceiver Counter is: " + gameConn.grs.getCounter());
 					tv.append("Counter is: " + gameConn.grs.getCounter() + "\n");
 					String str = null;
 					/*
 					while ((str = gameConn.grs.getNextCompletedTask(1)) != null) {
 						Log.d(TAG, "Task completion received");
 						tv.append(Util.fromJson(new JSONObject(str), null, null).toString() + "\n");
 					}
 					*/
 					AppResponse appResponse = null;
 					while ((appResponse = gameConn.getNextPendingNotification()) != null) {
 						Log.d(TAG, "Task completion received");
 						tv.append(appResponse.toString() + "\n");
 						Log.d(TAG, "APP RESPONSE IS:" + appResponse);
 						
						Log.d(TAG, "OBJ IS: " + ((AppResponse) appResponse.object).object.getClass());						
						Log.d(TAG, "OBJ IS: " + ((AppResponse) appResponse.object).object);
 					}					
 //					tv.append("Location is: " + logger.getLocationString() + "\n");
 				} catch (Exception e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}           	
 	    }			
     
 }
