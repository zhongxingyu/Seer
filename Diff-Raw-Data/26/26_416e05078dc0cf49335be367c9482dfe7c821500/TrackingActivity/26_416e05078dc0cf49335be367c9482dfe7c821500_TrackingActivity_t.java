 package uk.ac.sussex.asegr3.tracker.client.ui;
 
 import java.util.List;
 
 import uk.ac.sussex.asegr3.tracker.client.dto.LocationDto;
 import uk.ac.sussex.asegr3.tracker.client.service.LocationService;
 import uk.ac.sussex.asegr3.tracker.client.transport.HttpTransportClientApi;
 import uk.ac.sussex.asegr3.tracker.client.transport.HttpTransportClientApiFactory;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import android.widget.DigitalClock;
 import android.widget.Toast;
 
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapView;
 
 public class TrackingActivity extends MapActivity implements MapViewProvider, FetchLocationCallBack {
 
 
 	private static final int PERMISSION_GRANTED = 1;
 	
 	private MapViewManager mapViewManager;
 	
 	/**
 	 * Called when the activity is first created.
 	 * 
 	 * @param savedInstanceState
 	 *            If the activity is being re-initialized after previously being
 	 *            shut down then this Bundle contains the data it most recently
 	 *            supplied in onSaveInstanceState(Bundle). <b>Note: Otherwise it
 	 *            is null.</b>
 	 */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 
 		MapView mapView = getMap();
 		mapView.setBuiltInZoomControls(true);
 		// enable Street view by default
 
 		HttpTransportClientApi api = HttpTransportClientApiFactory.getCurrentApi();
 		DigitalClock clock = (DigitalClock) findViewById(R.id.digitalClock);
 		Intent trackingIntent = getIntent();
 		
 		
 
 		// TextView txtName = (TextView) findViewById(R.id.TextViewDisplay1);
 		// TextView txtEmail = (TextView) findViewById(R.id.TextViewDisplay2);
 		Intent i = getIntent();
 
 		LocationService locService=new LocationServiceFactory().create(
 				this, api, this, AndroidLogger.INSTANCE,
 				AsyncTask.SERIAL_EXECUTOR, this);
 		
 		
 		
 		
 		Button next = (Button) findViewById(R.id.button);
 
 		next.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View view) {
 
 				Toast.makeText(getBaseContext(),
 						"Your current location is LAT:" + "LONG:",
 						Toast.LENGTH_LONG).show();
 			}
 		});
 		
 		this.mapViewManager=new MapViewManager(getMap(), this);
 		
 		locService.getNearbyLocations();
 	}
 	
 	@Override
 	protected boolean isRouteDisplayed() {
 	
 		return false;
 	}
 
 	@Override
 	public MapView getMap() {
 		return (MapView) findViewById(R.id.mapview);
 	}
 	
 	public MapViewManager getMapViewManager(){
 		return this.mapViewManager;
 	}
 
 	@Override
 	public void processFetchLocations(List<LocationDto> locations) {
		System.out.println("In processFetchLocations");
		System.out.println("Location size" + locations.size());
 		
 		mapViewManager.addSomePointsToMap(locations);
 		
 	}
 
 	@Override
 	public void processFetchFailed(Exception e) {
		throw new RuntimeException(e);
 		
 	}
 	
 	
 	
 
 
 }
