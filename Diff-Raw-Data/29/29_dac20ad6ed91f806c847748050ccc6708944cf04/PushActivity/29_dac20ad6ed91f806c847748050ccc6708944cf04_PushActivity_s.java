 package com.demo.lightspeedgcm;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.message.BasicNameValuePair;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.content.Context;
 import android.os.Bundle;
 import android.os.Handler;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ImageButton;
 import android.widget.TextView;
 
 public class PushActivity extends Activity {
 	
 	private Handler gMainHandler;
 	private ImageButton gButtonPush;
 	private TextView gTextWelcome;
 	private TextView gShowResult;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 		this.setContentView(R.layout.activity_push);
 		
 		// Retrieve view entity from XML
 		gShowResult = (TextView) findViewById(R.id.showResult);
 		gTextWelcome = (TextView) findViewById(R.id.textWelcome);
 		gTextWelcome.setTypeface(MainActivity.gFont);
 		
 		// Handler binding with main thread. Handler allow you to send task to thread.
 		// Later, we need this handler to post UI task to main thread.
 		gMainHandler = new Handler();
 		
 		// Push Button click event
 		gButtonPush = (ImageButton) findViewById(R.id.buttonPush);
 		gButtonPush.setOnClickListener(new OnClickListener(){
 			public void onClick(View v){
 				PushRunn pushRunn = new PushRunn();
 				Thread th = new Thread(pushRunn);
 				th.start();
 			}
 		});
 		
 	}
 
 	public class PushRunn implements Runnable{
 
 		@Override
 		public void run() {
 			// TODO Auto-generated method stub
 			
 			// Instantiate the entity of httpPost with loginUrl
 			HttpPost httpPost = new HttpPost("http://api.lightspeedmbs.com/v1/push_notification/send.json?key="+MainActivity.appKey);
 			
 			// Put the email and password in key-value pair into a List.
 			// This is the data we're going to post to loginUrl.
 			List<NameValuePair> pair = new ArrayList<NameValuePair>(1);
 			pair.add(new BasicNameValuePair("payload","{android: {alert:This is a Lightspeed Push Notification ,sound:default, vibrate:true, title:Lightspeed GCM}}"));
 			
 			try {
 				// UrlEncodedFromEntity transform the pair list into HTTP request entity.
 				// setEntity will send request to loginUrl.
 				httpPost.setEntity(new UrlEncodedFormEntity(pair));
 				
 				// httpClient execute() would receive the response from server.
 				HttpResponse response = MainActivity.httpClient.execute(httpPost);
 				
 				// The response is a InputStream type. Here convert InputStream into String.
 				String str = MainActivity.inputStreamToString(response.getEntity().getContent());
 				Log.i(MainActivity.LOG_TAG,"Response string = "+ str);
 				
 				// Check whether the login is success or fail.
 				gMainHandler.post(new CheckPushResult(str));
 				
 			} catch (ClientProtocolException e1) {
 				e1.printStackTrace();
 			} catch (IOException e1) {
 				e1.printStackTrace();
 			}
 			
 		}
 	}
 	
 	public class CheckPushResult implements Runnable{
 		private String mResult;
 		private JSONObject json;
 		public CheckPushResult(String result){
 			mResult = result;
 		}
 		
 		public void run(){
 			String status = "null";
 			
 			try{
 				// The response is a json format String
 				json = new JSONObject(mResult);
 				// Get the value of the key "status".
 				status = json.getJSONObject("meta").getString("status");
 			}catch(JSONException e){
 				e.printStackTrace();
 			}
 			
 			if(status.equals("ok")){
 				gShowResult.setText(R.string.push_success);
 			}
 			
 		}
 	}
 	
 
 	@Override
 	protected void onResume() {
 		// TODO Auto-generated method stub
 		super.onResume();
 		//Allocate current activity context to sCurrentAct
 		MainActivity.sCurrentAct = this;
 	}
 	
 }
