 package com.itllp.barleylegalhomebrewers.ontap;
 
 import com.itllp.barleylegalhomebrewers.ontap.R;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.net.ConnectivityManager;
 import android.os.Bundle;
 
 public class EventListActivity extends Activity 
 {
	public static final String SKIP_INSTANTIATION 
		= "com.itllp.barleylegalhomebrewers.ontap.skipInstantiation";
 
 	public EventListActivity() {
 	}
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         Intent intent = getIntent();
        boolean skipInstantiation = intent.getBooleanExtra(SKIP_INSTANTIATION, false);
         if (!skipInstantiation) {
         	EventDatabaseImpl.create();
 
         	Context context = this.getApplicationContext();
         	ConnectivityManager connMgr = (ConnectivityManager) 
         	        context.getSystemService(Context.CONNECTIVITY_SERVICE);
         	NetworkConnectivity netConn = new AndroidNetworkConnectivity(connMgr);
        	EventDatabaseLoaderFactory.createBetaSiteEventDatabaseLoader(netConn);
         }
         setContentView(R.layout.event_list_fragment);
     }
     
 }
