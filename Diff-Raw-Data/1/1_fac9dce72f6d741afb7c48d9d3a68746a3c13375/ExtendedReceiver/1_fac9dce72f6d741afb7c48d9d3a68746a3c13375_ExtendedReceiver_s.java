 package com.demo.lightspeedgcm;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Typeface;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Gravity;
 import android.widget.TextView;
 
 import com.arrownock.push.PushBroadcastReceiver;
 
 // This is the receiver extends Lightspeed default receiver PushBroadcastReceiver
 // We can override "onReceive" method to implement our customized behavior.  
 public class ExtendedReceiver extends PushBroadcastReceiver {
 	private Context currentContext;
 	
 	@Override
 	// As receiving notification, we'll show a dialog with title and alert message 
 	public void onReceive(Context context, Intent intent) {
 		// TODO Auto-generated method stub
 		
 		// Default behavior of Lightspeed PushBroadcastReceiver
 		super.onReceive(context, intent);
 		//Log.i("TEST","Origin context = "+ context.getPackageName() + ", string= "+ context.toString());
 		
 		if( MainActivity.sCurrentAct == null ){
 			return;
 		}		
 		
 		String alert = null;
 		String title = null;
 		
 		// Lightspeed notification content would be carried with intent as a bundle.
 		// Get the bundle and retrieve the string by key "payload" in the bundle.
 		Bundle bundle = intent.getExtras();
 		String payload = bundle.getString("payload");
 		
 		//currentContext = context.getApplicationContext();
 		//Log.i("TEST","Origin context = "+ currentContext.getPackageName() + ", string= "+ currentContext.toString());
 		
 		try{
 			
 			// Content of payload is json format.
 			JSONObject json = new JSONObject(payload);
 			
 			// Get the string of alert and title.
 			JSONObject androidPayload = json.getJSONObject("android");
 			alert = (String) androidPayload.get("alert");
 			title = (String) androidPayload.get("title");			
 			
 		}catch (JSONException e){
 			e.printStackTrace();
 		}
 		
 		// Show a dialog with title and alert inside.
 		ShowDialog(title, alert);
 
 	}
 	
 	public void ShowDialog(String title , String alert){
 		
 		// Generate a dialog builder which's able to create dialog.
 		// Here we need to put current activity as argument since the dialog is going to display on it.
 		AlertDialog.Builder builder = new AlertDialog.Builder( MainActivity.sCurrentAct );
 		
 		// To center the text on the dialog, we need to customize both the title view and message view.
 		TextView tvTitle = new TextView(MainActivity.sCurrentAct);
 		tvTitle.setGravity(Gravity.CENTER);
 		tvTitle.setTextSize(20);
 		tvTitle.setPadding(10, 10, 10, 10);
 		tvTitle.setText(title);
 		tvTitle.setTypeface(null, Typeface.BOLD);
 	
 		TextView tvMsg = new TextView(MainActivity.sCurrentAct);
 		tvMsg.setGravity(Gravity.CENTER);
 		tvMsg.setTextSize(20);
 		tvMsg.setText(alert);
 		
 		// Bind the two views with dialog builder
 		builder.setCustomTitle(tvTitle);
 		builder.setView(tvMsg);
 		
 		// Create dialog by builder.
 		AlertDialog dialog = builder.create();//MainActivity.CreateMsgDialog(PushActivity.sPushContext, title, alert);
 		
 		// As user touch outside of the dialog, the dialog will dismiss.
 		dialog.setCanceledOnTouchOutside(true);
 		
 		// Show the created dialog on screen.
 		dialog.show();
 	}
 
 
 
 }
