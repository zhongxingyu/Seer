 package it.unibz.geospy;
 
 import it.unibz.geospy.datamanager.CoordinatesProvider;
 import it.unibz.geospy.datamanager.IMEIProvider;
 import it.unibz.geospy.datamanager.LocationObject;
 import it.unibz.geospy.sender.LocationSender;
 import it.unibz.geospy.sender.NetworkManager;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.location.Location;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.Toast;
 
 /**
  * Main activity of an Application. When this activity is turned on the spying 
  * process starts.
  * @author Dainius Jocas
  *
  */
 public class LocationSpy extends Activity {
 
 	
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_location_spy);
         CoordinatesProvider.startUpdates();
         final Context aContext = this.getApplicationContext();
         final TextView description = (TextView) findViewById(R.id.textView1);
         final TextView status = (TextView) findViewById(R.id.textView2);
         
         /*
          * Start and stop button configuration:
          * - start -- enabled
          * - stop -- disabled
          */
         final Button aStartButton = (Button) findViewById(R.id.start_button);
         final Button aStopButton = (Button) findViewById(R.id.stop_button);
         aStopButton.setEnabled(false);
         
         aStartButton.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				CoordinatesProvider.startUpdates();
 				Toast.makeText(aContext,
 						"Data collection started", 
 						Toast.LENGTH_SHORT).show();
 				// disable start button
 				aStartButton.setEnabled(false);
 				//enable stop button
 				aStopButton.setEnabled(true);
 				// status is enabled
 				status.setText(R.string.started);
 			}
 		});
         /*
          * Stop spying button
          */
         
         aStopButton.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				CoordinatesProvider.stopUpdates();
 				Toast.makeText(getApplicationContext(), 
 		    			"Data Collection stoped",
 		    			Toast.LENGTH_SHORT).show();
 				// disable stop button
 				aStopButton.setEnabled(false);
 				//enable start button
 				aStartButton.setEnabled(true);
 				// status is not_started
 				status.setText(R.string.not_started);
 			}
 		});
         /*
          * Get the latest location 
          */
         final Button button_location = (Button) findViewById(R.id.get_location);
         button_location.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
             	String anIMEI = IMEIProvider.getIMEI(aContext);
           
             	Location aLocation = CoordinatesProvider.getLocation();
             	String latitude = String.valueOf(aLocation.getLatitude());
             	String longitude = String.valueOf(aLocation.getLongitude());
             	Toast.makeText(aContext, anIMEI + "|" + latitude + "|" + 
             			longitude, Toast.LENGTH_SHORT).show();
             }
         });
         
         /*
          * Check if network is available
          */
         final Button button_network = (Button) findViewById(R.id.check_network);
         button_network.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
             	boolean networkAvailable = 
             			NetworkManager.isNetworkAvailable(aContext);
             	String message = "Network is ";
             	if (networkAvailable) {
             		message = message + "available";
                 	String anIMEI = IMEIProvider.getIMEI(aContext);
                     
                 	Location aLocation = CoordinatesProvider.getLocation();
                 	String latitude = String.valueOf(aLocation.getLatitude());
                 	String longitude = String.valueOf(aLocation.getLongitude());
                 	LocationObject lo = 
                 			new LocationObject(anIMEI, latitude, longitude,
                 					String.valueOf(aLocation.getTime()));
                 	LocationSender.sendLocation(lo);
             	} else {
             		message = message + " not available";
             	}
            	message = message + "| timeout=" + LocationSender.getInterval();
             	Toast.makeText(aContext, message, Toast.LENGTH_SHORT).show();
 
             }
         });
 
     }
 
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.location_spy, menu);
         return true;
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         // So far menu as only one item.
         switch (item.getItemId()) {
             case R.id.action_settings:
             	startActivityForResult(
             			new Intent(android.provider.Settings.
             					ACTION_LOCATION_SOURCE_SETTINGS), 0);
                 return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
     
     /**
      * Exiting the Acivity
      */
 /*    public void onDestroy() {
     	super.onDestroy();
     	Toast.makeText(getApplicationContext(), 
     			"To conserve battery you may want to manage location services",
     			Toast.LENGTH_LONG).show();
     }*/
     
 }
