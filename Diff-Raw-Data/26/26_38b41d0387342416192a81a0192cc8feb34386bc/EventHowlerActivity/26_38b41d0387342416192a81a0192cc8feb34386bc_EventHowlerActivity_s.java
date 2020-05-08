 package com.onb.eventHowler.activity;
 
 import com.onb.eventHowler.R;
 
 import com.onb.eventHowler.application.EventHowlerApplication;
 import com.onb.eventHowler.application.Status;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.Toast;
 import android.widget.ToggleButton;
 
 public class EventHowlerActivity extends Activity {
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
     }
     
     public void onSwitchToggled(View view){
     	final EventHowlerApplication application = (EventHowlerApplication)getApplication();
     	ToggleButton toggleButton = (ToggleButton)view.findViewById(R.id.howl_toggle_button);
     	EditText eventId = (EditText)findViewById(R.id.event_id_edit_text);
 		EditText secretKey = (EditText)findViewById(R.id.secret_key_edit_text);
 		if(eventId.getText().toString().equals("") || secretKey.getText().toString().equals("")){
 			Toast.makeText(getApplicationContext(), "please input event id and secret key",
 					Toast.LENGTH_SHORT).show();
 			toggleButton.setChecked(false);
 		}
 		else if(toggleButton.isChecked()){
     		application.setEventId(eventId.getText().toString());
     		application.setSecretKey(secretKey.getText().toString());
     		application.startRetrievingToURL();
     		Log.d("wow", "awesome");
     		application.setEventHowlerURLRetrieverServiceStatus(Status.START);
     		Runnable urlRetreiverChecker = new Runnable(){
     			public void run(){
     				while(application.getEventHowlerURLRetrieverServiceStatus() == Status.START){
     					try {
 							Thread.sleep(2000);
 						} catch (InterruptedException e) {}
     					Log.d("not free from loop", "gugugugugug");
     				}
     				Log.d("free from loop", "fhgfhjfjhg");
     				if(application.getEventHowlerURLRetrieverServiceStatus() == Status.RUNNING){
 						application.startEvent();
 					}
     			}
     		};
     		new Thread(urlRetreiverChecker).start();
     		if(application.getEventHowlerURLRetrieverServiceStatus() == Status.STOP){
     			Toast.makeText(getApplicationContext(), "forced stop",
     					Toast.LENGTH_SHORT).show();
     			toggleButton.setChecked(false);
     		}
     	}
     	else{
     		application.stopEvent();
     		final ProgressDialog dialog = ProgressDialog.show(this, "finishing", "please wait until the last process finish");
     		Runnable finishingChecker = new Runnable(){
     			public void run(){
     				while(application.isRunning()){
     					try {
 							Thread.sleep(2000);
 						} catch (InterruptedException e) {}
     				}
     				dialog.dismiss();
     			}
     		};
     		new Thread(finishingChecker).start();
     	}
     }
 }
