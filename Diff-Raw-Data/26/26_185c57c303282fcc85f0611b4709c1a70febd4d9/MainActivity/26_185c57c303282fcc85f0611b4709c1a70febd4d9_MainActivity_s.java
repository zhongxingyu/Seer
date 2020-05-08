 package arb.mportal;
 
 import tasks.POILoadTask;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.pm.ActivityInfo;
 import android.content.res.Configuration;
 import android.location.Location;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.view.Window;
 import android.widget.TextView;
 import android.widget.Toast;
 import arb.mportal.models.POI;
 import arb.mportal.models.User;
 import arb.mportal.util.BoundingBox;
 
 public class MainActivity extends Activity {
 
 	private LocationManager lm = null;
 	private LocationListenerImpl locationListener = null;	
 	private TextView statusText = null; 
 	private int queryAttempt = 0; 
 	
 	public void onCreate(Bundle bundle) {
 		
 		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
 		    Toast.makeText(this, "Die Anwendung ist fr den Landscape-Modus optimiert.", Toast.LENGTH_LONG).show();
 		    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
 		} 
 		
 		super.onCreate(bundle);
 		
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		setContentView(R.layout.main); 
 		statusText = (TextView)findViewById(R.id.statusText);
 		statusText.setText("Starte...");
 		
         locationListener = new LocationListenerImpl(new LocationReceivable() {
 			public void receiveNewLocation(Location l) {
				User.getInstance().setUserLocation(l);
 				setStatusText("Aktuellen Standort empfangen..."); 
 				lm.removeUpdates(locationListener);
 				startTasks();
 			}
 		});  
         lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);  
         lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, locationListener);		
 	} 
 
 	
 	private void startTasks() {
 		setStatusText("Starte AsyncTasks..."); 
 		queryAttempt++;
 		POILoadTask task = new POILoadTask(this);  
		task.execute(new BoundingBox(User.getInstance().getUserLocation(), 0.5 * queryAttempt));
 	}
 	
 	public void setStatusText(String text) {
 		statusText.setText(text); 
 	}
 	
 	public void poiLoadTaskFinished() {
 		if(POI.size() >= 3) { 
 			startActivity(new Intent(this, OverlayActivity.class));  
 		} else { 
 			POI.clear(); 
 			setStatusText("Keine Point of Interests gefunden, starte nochmal mit grerem Radius..."); 
 			startTasks(); 
 		} 		
 	}
 }
