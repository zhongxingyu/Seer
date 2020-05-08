 package com.example.grubber;
 
 
 import com.example.grubber.R;
 import com.google.analytics.tracking.android.*;
 import com.google.android.gms.maps.SupportMapFragment;
 
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.DialogFragment;
 import android.app.FragmentManager;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.MenuItem.OnMenuItemClickListener;
 import android.view.View;
 import android.widget.SearchView;
 import android.widget.Toast;
 
 @SuppressLint("NewApi")
 public class MainActivity extends Activity  {
 	
 	public final Context context = this;
 
 	private Tracker mGaTracker;
 	private GoogleAnalytics mGaInstance;
 	
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         mGaInstance = GoogleAnalytics.getInstance(this);
         mGaTracker = mGaInstance.getTracker("UA-40885024-1");
         setContentView(R.layout.activity_main);        
     }
     
     protected void onDestroy() {
     	super.onDestroy();
     }
     
     @SuppressLint("NewApi")
 	@Override
 
 	public void onResume() {
     	super.onResume();
     	//Refresh the options menu when this activity comes in focus
     	invalidateOptionsMenu();
     	//this.tracker.trackPageView("/TopTracksActivity");
     }
 
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.main, menu);
                       
         //Change profile button to login/register if they are not logged in
         if(SaveSharedPreference.getUserId(MainActivity.this) == 0)
         {
             MenuItem profileItem = menu.findItem(R.id.action_profile);
         	profileItem.setTitle(R.string.login);
             //Toast.makeText(this,"Not logged in",Toast.LENGTH_SHORT).show();
         }
         else {
         	MenuItem signout = menu.findItem(R.id.action_signout);
         	signout.setVisible(true);
             signout.setOnMenuItemClickListener(new OnMenuItemClickListener() {
             	public boolean onMenuItemClick(MenuItem item) {            		        	
         			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
         			alertDialogBuilder.setTitle(R.string.logout_msg);
         			alertDialogBuilder.setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
         				public void onClick(DialogInterface dialog,int id) {            					    							
 							//int tempUserName = SaveSharedPreference.getUserId(context);    			        		
 			        		dialog.cancel();    			        		
 			        		SaveSharedPreference.setUserId(context, 0);
         					Toast.makeText(context , "Logged out" , Toast.LENGTH_SHORT).show();
         					invalidateOptionsMenu();
 						}    						
 					}).setNegativeButton("No", new DialogInterface.OnClickListener() {
     					public void onClick(DialogInterface dialog,int id) {    						
     						dialog.cancel();    					
     					}}
     				  );            		
             		AlertDialog alertDialog = alertDialogBuilder.create();
             		alertDialog.show();   
             		return true;            		
             	} 	
             });        	
         }        
         return true;
       } 					
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
     	LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
     	switch (item.getItemId()) {
 	      case R.id.menu_search:
 	    	  if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER) && lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER) ) {
 	    		  Intent intent = new Intent(this, SearchActivity.class);
 	    		  startActivity(intent);
 	    	  } else {
 	    		  showNeedServicesDialog();	
 	    	  }
 	    	  break;
 	      case R.id.action_nearby:
 	    	  if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER) && lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER) ) {
 		    	  Intent intent2 = new Intent(this, Results.class);
 	    		  //get long lat 
 	    		  Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
 	    		  
 	    		  if( location == null ) {
 	    			// Define a listener that responds to location updates
 	    			LocationListener locationListener = new LocationListener() {
 	    			    public void onLocationChanged(Location location) {
 	    			      // Called when a new location is found by the network location provider.
 	    			    }
 
 	    			    public void onStatusChanged(String provider, int status, Bundle extras) {}
 
 	    			    public void onProviderEnabled(String provider) {}
 
 	    			    public void onProviderDisabled(String provider) { showNeedServicesDialog(); }
 	    			};
 
 	    			// Register the listener with the Location Manager to receive location updates
 	    			lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5 * 60000, 0, locationListener);
 	    			location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
 	    		  }
 	    		  
 	    		  intent2.putExtra("longitude", location.getLongitude());
 	    		  intent2.putExtra("latitude", location.getLatitude());			
 	    		  startActivity(intent2);   
 	    	  } else {
 	    		  showNeedServicesDialog();	
 	    	  }
 	    	  break;
 	      case R.id.action_profile:
 	    	  if(SaveSharedPreference.getUserId(MainActivity.this) != 0){
 	    		  Intent intent3 = new Intent(context, ProfileActivity.class);
 	    		  startActivity(intent3);   
 	    	  } else {
 	    		  Intent intent3 = new Intent(context, LoginActivity.class);
 	    		  startActivity(intent3);   
 	    	  }
 	          break;        
 	      default:
 	    	  break;
       }
 
       return true;
     } 
     
     
     public void showNeedServicesDialog() {
     	DialogFragment servicesDialog = new NeedServicesDialogFragment();
 		servicesDialog.show(getFragmentManager(), "results_services_dialog");		
     }
     
 public static class NeedServicesDialogFragment extends DialogFragment {
     static NeedServicesDialogFragment newInstance() {
         return new NeedServicesDialogFragment();
     }
 
 	
 	@Override	    
 	    public Dialog onCreateDialog(Bundle savedInstanceState) {
 	        // Use the Builder class for convenient dialog construction
 	        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
 	        builder.setMessage(R.string.need_services)
 	               .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
 	                   public void onClick(DialogInterface dialog, int id) {
 	                	   dialog.dismiss();
 	                   }
 	               });
 
 	        // Create the AlertDialog object and return it
 	        return builder.create();
 	    }
 	}
 	
     
    }
 }
