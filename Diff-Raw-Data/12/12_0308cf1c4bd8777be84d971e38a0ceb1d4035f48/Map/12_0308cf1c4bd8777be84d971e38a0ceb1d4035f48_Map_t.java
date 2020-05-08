 package ru.redsolution.rosyama;
 
 import ru.redsolution.rosyama.data.Hole;
 import ru.redsolution.rosyama.data.Rosyama;
 import ru.redsolution.rosyama.data.UpdateListener;
 import android.content.Context;
 import android.graphics.drawable.Drawable;
 import android.location.Location;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnKeyListener;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MapView;
 import com.google.android.maps.MyLocationOverlay;
 import com.google.android.maps.OverlayItem;
 
 public class Map extends MapActivity implements UpdateListener,
 		OnClickListener, OnDropMarkerListener, OnKeyListener {
 	private static final String SAVED_LATITUDE = "SAVED_LATITUDE";
 	private static final String SAVED_LONGITUDE = "SAVED_LONGITUDE";
 
 	/**
 	 * Приложение.
 	 */
 	private Rosyama rosyama;
 
 	/**
 	 * Дефект.
 	 */
 	private Hole hole;
 
 	/**
 	 * Поле ввода адреса.
 	 */
 	private TextView addressView;
 
 	/**
 	 * Управление картой.
 	 */
 	private MapController mapController;
 
 	/**
 	 * Карта.
 	 */
 	private MapView mapView;
 
 	/**
 	 * Отображение перемещаемой метки.
 	 */
 	private DragableOverlay dragableOverlay;
 
 	/**
 	 * Отображение собственного местоположения.
 	 */
 	private MyLocationOverlay myLocationOverlay;
 
 	/**
 	 * Менеджер местоположения.
 	 */
 	private LocationManager locationManager;
 
 	@Override
 	protected boolean isRouteDisplayed() {
 		return false;
 	}
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.map);
 		rosyama = (Rosyama) getApplication();
 
 		hole = rosyama.getHole(getIntent().getStringExtra(HoleEdit.EXTRA_ID));
 		if (hole == null) {
 			finish();
 			return;
 		}
 
 		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 
 		mapView = (MapView) findViewById(R.id.map);
 		mapView.setBuiltInZoomControls(true);
 		mapView.setSatellite(true);
 
 		Drawable marker = getResources().getDrawable(R.drawable.marker);
 		marker.setBounds(0, 0, marker.getIntrinsicWidth(),
 				marker.getIntrinsicHeight());
 		System.out.println(findViewById(R.id.drag));
 		dragableOverlay = new DragableOverlay(marker,
 				(ImageView) findViewById(R.id.drag));
 		dragableOverlay.setOnDropMarkerListener(this);
 		mapView.getOverlays().add(dragableOverlay);
 
 		myLocationOverlay = new MyLocationOverlay(this, mapView);
 		mapView.getOverlays().add(myLocationOverlay);
 
 		mapController = mapView.getController();
 		addressView = (TextView) findViewById(R.id.address);
 
 		Location location;
 		if (savedInstanceState != null) {
 			location = new Location(LocationManager.GPS_PROVIDER);
 			location.setLatitude(savedInstanceState.getDouble(SAVED_LATITUDE));
 			location.setLongitude(savedInstanceState.getDouble(SAVED_LONGITUDE));
 		} else {
 			String address = hole.getAddress();
 			location = hole.getLocation();
 			if (location == null) {
 				location = new Location(LocationManager.GPS_PROVIDER);
 				location.setLatitude(55.75402);
 				location.setLongitude(37.62048);
 				if (!"".equals(address))
 					rosyama.getLocationOperation().execute(address);
 			} else if ("".equals(address)) {
 				rosyama.getAddressOperation().execute(location);
 			}
 			addressView.setText(address);
 		}
 		GeoPoint geoPoint = getGeoPoint(location);
 		mapController.animateTo(geoPoint);
 		mapController.setZoom(15);
 
 		dragableOverlay.addItem(new OverlayItem(geoPoint, "Mark",
 				"Selected position"));
 
 		addressView.setOnKeyListener(this);
 		findViewById(R.id.done).setOnClickListener(this);
 		findViewById(R.id.search).setOnClickListener(this);
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		((Rosyama) getApplication()).setUpdateListener(this);
 		myLocationOverlay.enableCompass();
 		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
 				0, rosyama);
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		((Rosyama) getApplication()).setUpdateListener(null);
 		myLocationOverlay.disableCompass();
 		locationManager.removeUpdates(rosyama);
 	}
 
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		if (dragableOverlay.size() > 0) {
 			Location location = getLocation(dragableOverlay.getItem(0)
 					.getPoint());
 			outState.putDouble(SAVED_LATITUDE, location.getLatitude());
 			outState.putDouble(SAVED_LONGITUDE, location.getLongitude());
 		}
 	}
 
 	@Override
 	public void onClick(View view) {
 		switch (view.getId()) {
 		case R.id.search:
 			rosyama.getAddressOperation().cancel();
			if (!addressView.getText().toString().equals(""))
				rosyama.getLocationOperation().execute(
						addressView.getText().toString());
 			break;
 		case R.id.done:
 			hole.setAddress(addressView.getText().toString());
 			if (dragableOverlay.size() > 0)
 				hole.setLocation(getLocation(dragableOverlay.getItem(0)
 						.getPoint()));
 			finish();
 			break;
 		}
 	}
 
 	@Override
 	public void onUpdate() {
 		if (rosyama.getAddressOperation().isComplited()) {
 			addressView.setText(rosyama.getAddressOperation().getAddress());
 			rosyama.getAddressOperation().clear();
 		} else if (rosyama.getLocationOperation().isComplited()) {
 			GeoPoint geoPoint = getGeoPoint(rosyama.getLocationOperation()
 					.getLocation());
 			mapController.animateTo(geoPoint);
 			if (dragableOverlay.size() > 0)
 				dragableOverlay.removeItem(dragableOverlay.getItem(0));
 			dragableOverlay.addItem(new OverlayItem(geoPoint, "Mark",
 					"Selected position"));
 			rosyama.getLocationOperation().clear();
 		}
 	}
 
 	@Override
 	public void onDropMarker(OverlayItem overlayItem) {
 		rosyama.getLocationOperation().cancel();
 		rosyama.getAddressOperation().execute(
 				getLocation(overlayItem.getPoint()));
 	}
 
 	@Override
 	public boolean onKey(View v, int keyCode, KeyEvent event) {
 		rosyama.getAddressOperation().cancel();
 		return false;
 	}
 
 	private static Location getLocation(GeoPoint geoPoint) {
 		Location location = new Location(LocationManager.GPS_PROVIDER);
 		location.setLatitude(geoPoint.getLatitudeE6() / 1E6);
 		location.setLongitude(geoPoint.getLongitudeE6() / 1E6);
 		return location;
 	}
 
 	private static GeoPoint getGeoPoint(Location location) {
 		return new GeoPoint((int) (location.getLatitude() * 1E6),
 				(int) (location.getLongitude() * 1E6));
 	}
 }
