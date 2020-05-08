 package ie.smartcommuter.controllers.tabcontents;
 
 import ie.smartcommuter.R;
 import ie.smartcommuter.controllers.RealtimeArrayAdapter;
 import ie.smartcommuter.controllers.SmartTabContentActivity;
 import ie.smartcommuter.models.Station;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.os.Handler;
 import android.preference.PreferenceManager;
 import android.view.View;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 
 /**
  * This is a class is used to display the realtime
  * information for the station.
  * @author Shane Bryan Doyle
  */
 public class StationRealtimeActivity extends SmartTabContentActivity {
 	
 	private Station station;
 	private ListView arrivals;
 	private ListView departures;
 	private RealtimeArrayAdapter arrivalsAdapter;
 	private RealtimeArrayAdapter departuresAdapter;
 	private int realtimeRefreshInterval;
 	private SharedPreferences prefs;
 	private Context context;
 	private Boolean getRealtimeUpdates;
 	private Handler handler;
 	private Runnable runnable;
 	private Boolean hideProgressBar;
 
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.screen_station_realtime);
         
         context = this;
         getRealtimeUpdates = true;
         handler = new Handler();
         hideProgressBar = false;
         
         prefs = PreferenceManager.getDefaultSharedPreferences(this);
         realtimeRefreshInterval = Integer.parseInt(prefs.getString("realtimeRefreshInterval", "30000"));
 
         Intent intent = getIntent();
         Bundle bundle = intent.getExtras();
         
         station = (Station) bundle.getSerializable("station");
 
         arrivals = (ListView)findViewById(R.id.arrivalsListView);
         arrivals.setTextFilterEnabled(false);
         arrivals.setEmptyView(findViewById(R.id.arrivalsLoadingListView));
         
         departures = (ListView)findViewById(R.id.departuresListView);
         departures.setTextFilterEnabled(false);
         departures.setEmptyView(findViewById(R.id.departuresLoadingListView));
         
 		runnable = getRealtimeRunnable();
     }
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		getRealtimeUpdates = false;
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
		
 		realtimeRefreshInterval = Integer.parseInt(prefs.getString("realtimeRefreshInterval", "30000"));
 		
 		getRealtimeUpdates = true;
 		
 		new Thread(runnable).start();
 	}
 	
 	/**
 	 * This method is used to run a thread that updates
 	 * the realtime information on the screen.
 	 * @return
 	 */
 	private Runnable getRealtimeRunnable() {
 		Runnable runnable = new Runnable() {
 			@Override
 			public void run() {
 				
 				while(getRealtimeUpdates) {
 					station.getRealTimeData();
 					
 					handler.post(new Runnable() {
 						@Override
 						public void run() {
 					        arrivalsAdapter = new RealtimeArrayAdapter(context, station.getArrivals());
 					        arrivals.setAdapter(arrivalsAdapter);
 					        
 					        departuresAdapter = new RealtimeArrayAdapter(context, station.getDepartures());
 					        departures.setAdapter(departuresAdapter);
 					        
 					        if(!hideProgressBar){
 					        	updateEmptyListMessages();
 					        }
 						}
 					});
 					
 					try {
 						Thread.sleep(realtimeRefreshInterval);
 					} catch (InterruptedException e) {
 						e.printStackTrace();
 					}
 				}
 			}
 		};
 		
 		return runnable;
 	}
 	
 	/**
 	 * This method is used to change the original
 	 * empty view message from loading to the no
 	 * data message.
 	 * @return 
 	 */
 	private void updateEmptyListMessages() {
 		ProgressBar arrivalsLoading = (ProgressBar) findViewById(R.id.arrivalsLoadingProgressBar);
 		arrivalsLoading.setVisibility(View.INVISIBLE);
 		TextView arrivalsTextView = (TextView) findViewById(R.id.arrivalsLoadingTextView);
 		arrivalsTextView.setText(R.string.arrivalsListEmptyMessage);
 		
 		ProgressBar departuresLoading = (ProgressBar) findViewById(R.id.departuresLoadingProgressBar);
 		departuresLoading.setVisibility(View.INVISIBLE);
 		TextView departuresTextView = (TextView) findViewById(R.id.departuresLoadingTextView);
 		departuresTextView.setText(R.string.departuresListEmptyMessage);
 		
 		hideProgressBar = true;
 	}
 	
 }
 
