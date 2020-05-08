 package dk.iha.whooper.trainstations;
 
 import java.util.ArrayList;
 
 import dk.iha.whooper.trainstations.GetStationsService.GetStationsBinder;
 import android.net.MailTo;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.app.Activity;
 import android.app.ListActivity;
 import android.content.BroadcastReceiver;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.ServiceConnection;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.WindowManager;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class Stations_Activity extends ListActivity implements OnClickListener, TextWatcher{
 
 	private static final String TAG="Stations_Activity";
 	private GetStationsService getStationsService;
     private boolean mBound = false;
     private Station[] stations;
     private ArrayList<String> stationNames;
     private BroadcastReceiver updateReciever;
     private ArrayAdapter<String> adapter;
     private EditText filterInput;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_stations_main);
		
 		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN); //Don't show keyboard
 		
 		findViewById(R.id.OpdaterButton).setOnClickListener(this);
 		stationNames = new ArrayList<String>();
 		filterInput = (EditText) findViewById(R.id.FilterText);
 		filterInput.addTextChangedListener(this);
 		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, stationNames);
 		updateReciever = new BroadcastReceiver() {
 	            @Override
 	            public void onReceive(Context context, Intent intent) {
	                Log.d(TAG,"StationsUpdated broadcast recieved");
 	                stations = getStationsService.getStations();
 	                for(Station s : stations){
 	                	stationNames.add(s.getName());
 	                }
 	                
 	        		Stations_Activity.this.runOnUiThread(new Runnable(){
 	        			@Override
 	        			public void run() {
 	        				setListAdapter(adapter);
 	        			}
 	        		});
 	            }
 	        };
 	        
         this.registerReceiver(updateReciever, new IntentFilter("StationsUpdated"));
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_stations_main, menu);
 		return true;
 	}
 	
 	protected void onStart(){
 		super.onStart();
 		Intent intent = new Intent(this, GetStationsService.class);
         bindService(intent, connectionToService, Context.BIND_AUTO_CREATE);
 	}
 	
 	@Override
     protected void onStop() {
         super.onStop();
         if (mBound) {
             unbindService(connectionToService);
             mBound = false;
         }
     }
 
     private ServiceConnection connectionToService = new ServiceConnection() {
 
         @Override
         public void onServiceConnected(ComponentName className,
                 IBinder service) {
         	GetStationsBinder binder = (GetStationsBinder) service;
             getStationsService = binder.getService();
             mBound = true;
             getStationsService.updateStations();
         }
 
         @Override
         public void onServiceDisconnected(ComponentName arg0) {
             mBound = false;
         }
     };
 
     //Event handlers
     @Override
 	public void onClick(View v) {
 		switch(v.getId()){
 		case R.id.OpdaterButton:
 			Log.d(TAG,"UpdateButton clicked");
 			getStationsService.updateStations();
 			break;
 		}
 		
 	}
 
 	@Override
 	public void afterTextChanged(Editable arg0) {
		
 	}
 
 	@Override
 	public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
 			int arg3) {
		
 	}
 
 	@Override
 	public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
 		// TODO Auto-generated method stub
 		Stations_Activity.this.adapter.getFilter().filter(cs);
 	}
 }
 
 
