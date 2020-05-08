 package org.androidtitlan.geotaskmanager;
 
 import java.io.IOException;
 import java.util.List;
 import java.util.Locale;
 
 import org.androidtitlan.geotaskmanager.R;
 import org.androidtitlan.geotaskmanager.adapter.TaskListAdapter;
 import org.androidtitlan.geotaskmanager.tasks.Task;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ListActivity;
 import android.app.ProgressDialog;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.location.Address;
 import android.location.Criteria;
 import android.location.Geocoder;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.provider.Settings;
 import android.view.View;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.ToggleButton;
 
 public class ViewTasksActivity extends ListActivity implements LocationListener {
 
 	private static final long LOCATION_FILTER_DISTANCE = 3000;
 	private Button addButton;
 	private TaskListAdapter adapter;
 	private GeoTaskManagerApplication app;
 	private Button removeButton;
 	private TextView locationText;
 	private ToggleButton localTasksToggle;
 	private LocationManager locationManager;
 	private Location latestLocation;
 	private ProgressDialog pd;
 	private Thread searchAdress;
 	private List<Address> foundAdresses;
 	private String location;
 
 
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         setUpViews();
         app = (GeoTaskManagerApplication)getApplication();
         adapter = new TaskListAdapter(this, app.getCurrentTasks());
         setListAdapter(adapter);
         setUpLocation();
      //  printMyCurrentLocationAsString(location); 
 
         
        
 
     }
 	
 	
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		adapter.forceReload();
 	}
 
 	@Override
 	protected void onListItemClick(ListView l, View v, int position, long id) {
 		super.onListItemClick(l, v, position, id);
 		adapter.toggleTaskCompleteAtPosition(position);
 		Task t = adapter.getItem(position);
 		app.saveTask(t);
 	}
 	//we added a LocationManager and a Geocoder to change as a human-readable string
 	public void onLocationChanged(Location location) {
		printMyCurrentLocationAsString(location);
 		
 		/*latestLocation = location;
 		String locationString = String.format(
 				"@ %f, %f +/- %fm",
 				location.getLatitude(),
 				location.getLongitude(),
 				location.getAccuracy());
 		locationText.setText(locationString); */
 	}
 	 
 
	private void printMyCurrentLocationAsString(Location location) {
 		LocationManager mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
         Criteria criteria = new Criteria(); criteria.setAccuracy(Criteria.ACCURACY_FINE); 
         criteria.setPowerRequirement(Criteria.POWER_LOW); 
         String locationprovider = mLocationManager.getBestProvider(criteria,true);
         Location mLocation = mLocationManager.getLastKnownLocation(locationprovider);
        latestLocation = location;
         List<Address> addresses; 
         try {
         	Geocoder mGC = new Geocoder(this, Locale.ENGLISH); 
         	addresses = mGC.getFromLocation(mLocation.getLatitude(),
         	mLocation.getLongitude(), 1);
         	if(addresses != null) {
         		Address currentAddr = addresses.get(0); 
         		StringBuilder mSB = new StringBuilder(""); 
         		for(int i=0; i<currentAddr.getMaxAddressLineIndex(); i++) {
         	mSB.append(currentAddr.getAddressLine(i)).append(",");
         	}
         	locationText.setText(mSB.toString());
         	}
         } catch(IOException e){
         	locationText.setText(e.getMessage());
         	}
 	}
 
 	public void onProviderDisabled(String provider) { }
 
 	public void onProviderEnabled(String provider) { }
 
 	public void onStatusChanged(String provider, int status, Bundle extras) { }
 
 	protected void removeCompletedTasks() {
 		Long[] ids = adapter.removeCompletedTasks();
 		app.deleteTasks(ids);
 	}
 	
 	protected void showLocalTasks(boolean checked) {
 		
 		if (checked) {
 			isOnline();
 			adapter.filterTasksByLocation(latestLocation, LOCATION_FILTER_DISTANCE);			
 		} else {
 			adapter.removeLocationFilter();
 		}
 	}
 	
 	
 	private void setUpViews() {
 		addButton = (Button)findViewById(R.id.add_button);
 		removeButton = (Button)findViewById(R.id.remove_button);
 		localTasksToggle = (ToggleButton)findViewById(R.id.show_local_tasks_toggle);
 		locationText = (TextView)findViewById(R.id.location_text);
 		
 		addButton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {				
 				Intent intent = new Intent(ViewTasksActivity.this, AddTaskActivity.class);
 				startActivity(intent);
 			}
 		});
 		removeButton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				removeCompletedTasks();
 			}
 		});
 		localTasksToggle.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				showLocalTasks(localTasksToggle.isChecked());
 			}
 		});
 
 	}
 	
 	private void setUpLocation() {
 		LocationManager locationmanager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
 		//we say we need to send us a ping constantly using 60ms or 5 meters of difference to requests
 		locationmanager.requestLocationUpdates(
                 LocationManager.GPS_PROVIDER,
                 60,
                 5,
                 this);
 	    if ( !locationmanager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
 	        buildAlertMessageNoGps();
 	    }
 		
 	}
 	
 	//Checking if there are connection, else activate from the dialog 
 	private void buildAlertMessageNoGps() {
 		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
 	    builder.setMessage("You GPS seems to be disabled, do you want to enable it?")
 	           .setCancelable(false)
 	           .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
 	               public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
 	                   launchGPSOptions(); 
 	               }
 	           })
 	           .setNegativeButton("No", new DialogInterface.OnClickListener() {
 	               public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
 	                    dialog.cancel();
 	               }
 	           });
 	    final AlertDialog alert = builder.create();
 	    alert.show();
 		
 	}
 	//If there arent GPS on, then launch it!
 	private void launchGPSOptions() {
         final ComponentName toLaunch = new ComponentName("com.android.settings","com.android.settings.SecuritySettings");
         final Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
         intent.addCategory(Intent.CATEGORY_LAUNCHER);
         intent.setComponent(toLaunch);
         intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
         startActivityForResult(intent, 0);
     }
 
 	//method to detect if device is connected by any means
 	public boolean isOnline() {
 		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 		NetworkInfo netInfo = cm.getActiveNetworkInfo();
 		
 		if (netInfo != null && netInfo.isConnected()) {
 			return true;
 		} else{
 			
 			return false;
 		}
 		
 	}
 	//Showing the No internet connection Custom Dialog =)
 	public void showNoInternetConnectionDialog(){
 		Context mContext = getApplicationContext();
 		Dialog dialog = new Dialog(mContext);
 
 		dialog.setContentView(R.layout.no_internet_connection_dialog);
 		dialog.setTitle("You don't have internet connection");
 
 		TextView text = (TextView) dialog.findViewById(R.id.no_internet_connection_text);
 		text.setText("Whoops! Its seems you don't have internet connection, please try again!");
 		ImageView image = (ImageView) dialog.findViewById(R.id.stop_png);
 		image.setImageResource(R.drawable.stop_mdpi);
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setCancelable(false)
 		       .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 		           public void onClick(DialogInterface dialog, int id) {
 		                ViewTasksActivity.this.finish();
 		           }
 		       })
 		       .setNegativeButton("No", new DialogInterface.OnClickListener() {
 		           public void onClick(DialogInterface dialog, int id) {
 		                dialog.cancel();
 		           }
 		       });
 		AlertDialog alert = builder.create();
 	}
 
 
 }     
