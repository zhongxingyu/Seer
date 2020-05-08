 package ch.hsr.traildevil.presentation;
 
 import android.content.Intent;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.text.Html;
 import android.widget.ImageView;
 import android.widget.TextView;
 import ch.hsr.traildevil.R;
 import ch.hsr.traildevil.application.Controller;
 import ch.hsr.traildevil.domain.Trail;
 import ch.hsr.traildevil.util.StateUtility;
 import ch.hsr.traildevil.util.maps.POIOverlay;
 import ch.hsr.traildevil.util.maps.POIOverlayItem;
 import ch.hsr.traildevil.util.network.ImageDownloader;
 import ch.hsr.traildevil.util.network.WeatherService;
 
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MapView;
 
 public class TrailActivity extends MapActivity {
 
 	private MapView mapView;
 	private ImageView trailLogo, trailWeather;
 	private TextView trailStatus;
 	private TextView trailDesc;
 	private Controller appController;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.detail);
 
 		initComponents();
 		handleIntent(getIntent());
 	}
 
 	/**
 	 * Initializes the components
 	 */
 	private void initComponents() {
 		appController = new Controller(getApplicationContext());
 
 		trailLogo = (ImageView) findViewById(R.id.detailview_logo);
 		trailStatus = (TextView) findViewById(R.id.detailview_status);
 		trailWeather = (ImageView) findViewById(R.id.detailview_weather);
 		trailDesc = (TextView) findViewById(R.id.detailview_description);
 		mapView = (MapView) findViewById(R.id.detailview_mapview);
 	}
 
 	private void handleIntent(Intent intent) {
		int trailPosition = intent.getIntExtra("trailPosition", 0);
		updateViews(appController.getTrail(trailPosition));
 	}
 
 	private void updateViews(Trail trail) {
 		setTitle(trail.getName());
 		StateUtility.setState(trailStatus, trail.getIsOpen());
 		trailDesc.setText(Html.fromHtml(trail.getDesc()));
 		ImageDownloader.Instance.loadDrawable(WeatherService.getWeatherImageUrl(trail.getNextCity()), trailWeather, R.drawable.weather_na);
 		ImageDownloader.Instance.loadDrawable(trail.getImageUrl800(), trailLogo, R.drawable.nophotobig);
 		createGoogleMapView(trail);
 	}
 
 	private void createGoogleMapView(Trail trail) {
 		// create and add POI's
 		Drawable marker = getResources().getDrawable(R.drawable.map_marker);
 		POIOverlayItem trailLocation = new POIOverlayItem(trail.getGmapX(), trail.getGmapY(), trail.getName(), trail.getName());
 
 		POIOverlay overlay = new POIOverlay(marker, trailLocation);
 		mapView.getOverlays().add(overlay);
 		mapView.setBuiltInZoomControls(true);
 
 		MapController mapController = mapView.getController();
 		mapController.setCenter(overlay.getCenterPoint());
 		mapController.setZoom(17);
 	}
 
 	@Override
 	protected boolean isLocationDisplayed() {
 		return false;
 	}
 
 	@Override
 	protected boolean isRouteDisplayed() {
 		return false;
 	}
 }
