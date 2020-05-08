 package com.itllp.barleylegalhomebrewers.ontap;
 
 import com.itllp.barleylegalhomebrewers.ontap.R;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.net.ConnectivityManager;
 import android.os.Bundle;
 
 public class EventListActivity extends Activity 
 {
	public static final String SKIP_INSTANTIATION_FOR_TESTING 
		= "com.itllp.barleylegalhomebrewers.ontap.skipInstantiation_FOR_TESTING";
 
 	public EventListActivity() {
 	}
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         Intent intent = getIntent();
        boolean skipInstantiation = intent.getBooleanExtra(SKIP_INSTANTIATION_FOR_TESTING, false);
         if (!skipInstantiation) {
         	EventDatabaseImpl.create();
 
         	Context context = this.getApplicationContext();
         	ConnectivityManager connMgr = (ConnectivityManager) 
         	        context.getSystemService(Context.CONNECTIVITY_SERVICE);
         	NetworkConnectivity netConn = new AndroidNetworkConnectivity(connMgr);
        	EventDatabaseLoaderFactory.createProductionSiteEventDatabaseLoader(netConn);
         }
         setContentView(R.layout.event_list_fragment);
     }
     
 }
