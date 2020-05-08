 package br.ufsm.brunodea.tcc.map;
 
 import java.util.List;
 
 import android.content.Context;
 import android.graphics.PixelFormat;
 import android.location.Location;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.ImageButton;
 import android.widget.ProgressBar;
 import br.ufsm.brunodea.tcc.R;
 import br.ufsm.brunodea.tcc.event.EventItem;
 import br.ufsm.brunodea.tcc.event.EventItem.EventType;
 import br.ufsm.brunodea.tcc.internet.Internet;
 import br.ufsm.brunodea.tcc.map.EventsItemizedOverlay.BalloonType;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MapView;
 import com.google.android.maps.Overlay;
 
 public class InfoCityMap extends MapActivity implements OnClickListener {
 
 	private MapView mMapView;
 	private MapController mMapController;
 	private List<Overlay> mMapOverlays;
 	
 	private LocationManager mLocationManager;
 	private InfoCityLocationListener mLocationListener;
 	
 	private Location mLastKnownLocation;
 	
 	private ImageButton mWindowTitleButtonAddEvent;
 	private ProgressBar mWindowTitleProgressBar;
 	
 
 	private EventsItemizedOverlay mAddEventsOverlay;
 	private EventsItemizedOverlay mUnknownEventsOverlay;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceBundle) {
 		super.onCreate(savedInstanceBundle);
 		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
 		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
 		setContentView(R.layout.map);
 		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.windowtitle);
 		getWindow().setFormat(PixelFormat.RGBA_8888);
 
 		initGUIElements();
 		init();
 		
 		addTestMarkers();
 		
 		Internet.hasConnection(this, true);
 	}
 	
 	private void init() {
 		mMapView = (MapView) findViewById(R.id.mapview);
 		mMapView.getController().setZoom(20);
 		mMapView.setSatellite(true);
 		
 		mMapController = mMapView.getController();
 		
 		mMapOverlays = mMapView.getOverlays();
		mUnknownEventsOverlay = 
				new EventsItemizedOverlay(this, EventType.UNKNOWN, mMapView, BalloonType.INFO);
		mAddEventsOverlay = 
				new EventsItemizedOverlay(this, EventType.UNKNOWN, mMapView, BalloonType.ADD);
		
		mMapOverlays.add(mUnknownEventsOverlay);
		mMapOverlays.add(mAddEventsOverlay);
		
		
 		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 		mLocationListener = new InfoCityLocationListener(this);
 
 		mLastKnownLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
 		if(mLastKnownLocation == null) {
 			mLastKnownLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
 		}
 	
 		centerMapInLastKnownLocation();
 	}
 	
 	private void initGUIElements() {
 		mWindowTitleButtonAddEvent = (ImageButton)findViewById(R.id.button_add_event);
 		mWindowTitleButtonAddEvent.setOnClickListener(this);
 		
 		mWindowTitleProgressBar = (ProgressBar)findViewById(R.id.progressbar_window_title);
 		mWindowTitleProgressBar.setVisibility(View.GONE);
 	}
 	
 	@Override
 	protected boolean isRouteDisplayed() {
 		return false;
 	}
 	
 	public void startRequestLocationUpdates() {
 		mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
 				0, 5, mLocationListener);
 		mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
 				0, 5, mLocationListener);
 	}
 	
 	public void stopRequestLocationUpdates() {
 		mLocationManager.removeUpdates(mLocationListener);
 	}
 	
 	public void centerMapInLastKnownLocation() {
 		if(mLastKnownLocation != null) {
 			mMapController.setCenter(
 					new GeoPoint(
 						(int)(mLastKnownLocation.getLatitude()*1E6), 
 						(int)(mLastKnownLocation.getLongitude()*1E6)
 				    ));
 		}
 	}
 
 	public void onClick(View v) {
 		if(v == mWindowTitleButtonAddEvent) {
 			toggleWindowTitleAddEventProgressBar();
 			startRequestLocationUpdates();
 		}
 	}
 	
 	public void setLastKnownLocation(Location location) {
 		mLastKnownLocation = location;
 	}	
 	
 	public void toggleWindowTitleAddEventProgressBar() {
 		int pb_vis = mWindowTitleProgressBar.getVisibility();
 		mWindowTitleProgressBar.setVisibility(
 				pb_vis == View.VISIBLE ? View.GONE : View.VISIBLE);
 		mWindowTitleButtonAddEvent.setEnabled(!mWindowTitleButtonAddEvent.isEnabled());
 	}
 	
 	public void addAddEventItemMarker() {
 		EventItem new_event = createEventItem(mLastKnownLocation.getLatitude(),
 				mLastKnownLocation.getLongitude(), "New Event", "New Event", EventType.UNKNOWN);
 		mAddEventsOverlay.addEventItem(new_event);
 	}
 	
 	private void addTestMarkers() {		
 		EventItem event = createEventItem(-29.708332, -53.818674, "Teste Um", 
 				"Descrição do Teste Um.", EventType.UNKNOWN);
 		EventItem event2 = createEventItem(-29.696031, -53.860044, "Teste Dois",
 				"Descrição do Teste Dois", EventType.UNKNOWN);
 		mUnknownEventsOverlay.addEventItem(event);
 		mUnknownEventsOverlay.addEventItem(event2);

		mMapOverlays.add(mUnknownEventsOverlay);
 		
 		mUnknownEventsOverlay.removeEventItem(event);
 	}
 	
 	private EventItem createEventItem(double lat, double lon, String title, 
 			String description, EventType type) {
 		GeoPoint gp = new GeoPoint((int)(lat*1E6),(int)(lon*1E6));
 		return new EventItem(gp, title,  description, type);
 	}
 }
