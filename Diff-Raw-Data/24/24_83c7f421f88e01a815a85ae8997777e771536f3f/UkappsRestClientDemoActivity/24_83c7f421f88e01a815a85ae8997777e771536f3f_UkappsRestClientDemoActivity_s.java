 package no.uka.findmyapp.android.demo;
 
 import java.io.Serializable;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.List;
 
 import no.uka.findmyapp.android.rest.R;
 import no.uka.findmyapp.android.rest.client.BroadcastTokens;
 import no.uka.findmyapp.android.rest.client.HttpType;
 import no.uka.findmyapp.android.rest.client.RestServiceHelper;
 import no.uka.findmyapp.android.rest.client.ServiceDataFormat;
 import no.uka.findmyapp.android.rest.client.UkappsServices;
 import no.uka.findmyapp.android.rest.client.model.ServiceModel;
 import no.uka.findmyapp.android.rest.contracts.UkaEvents.UkaEventContract;
 import no.uka.findmyapp.android.rest.datamodels.Temperature;
 import no.uka.findmyapp.android.rest.datamodels.UkaEvent;
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.database.ContentObserver;
 import android.os.Bundle;
 import android.os.Handler;
 import android.util.Log;
 import android.widget.TextView;
 
 import com.google.gson.Gson;
 import com.google.gson.reflect.TypeToken;
 
 public class UkappsRestClientDemoActivity extends Activity {
 	private static RestServiceHelper serviceHelper = RestServiceHelper.getInstance(); 
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
         ServiceModel serviceModel;
 		try {
 	        ReciveIntent intentReceiver = new ReciveIntent();
 			IntentFilter intentFilter = new IntentFilter(BroadcastTokens.BROADCAST_INTENT_TOKEN);
 
 			registerReceiver(intentReceiver, intentFilter); 
 			Handler handler = new Handler();
 			
 			this.getContentResolver()
 			.registerContentObserver(UkaEventContract.EVENT_CONTENT_URI, false, new MyContentObserver(handler));
 			Log.v("DEBUG", "HERE1");
 			serviceHelper.startServiceTest(this, UkappsServices.UKAEVENTS); 
 			
 		} catch (URISyntaxException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InstantiationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
         
     }
     
     class MyContentObserver extends ContentObserver {
 
 		public MyContentObserver(Handler handler) {
 			super(handler);
 			// TODO Auto-generated constructor stub
 		}
     	@Override
     	public void onChange(boolean selfChange) {
     		// TODO Auto-generated method stub
     		super.onChange(selfChange);
     		Log.v("CONTENT OBSERVER", "oBSERVED");
     	}
     }
     
     public class ReciveIntent extends BroadcastReceiver {
 
 		@Override
 		public void onReceive(Context context, Intent intent) {
 
 			Log.w("BroadcastIntentDebug", "---------");
 			//TextView tv = (TextView) findViewById(R.id.debugResult);
 			Gson gson = new Gson(); 
 			if (intent.getAction().equals(BroadcastTokens.BROADCAST_INTENT_TOKEN)) {
 				Serializable obj = intent.getSerializableExtra("return");
				List<UkaEvent> t = (List<UkaEvent>) obj;
 				
				for(UkaEvent u : t) {
 					Log.w("BroadcastIntentDebug", u.toString());
 					
 				}
 				//tv.setText(t.toString()); 
 			}
 		}
 	}
 }
 
