 package com.project202.address;
 
 import java.io.File;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Dialog;
 import android.content.Intent;
 import android.location.Address;
 import android.location.Criteria;
 import android.location.Geocoder;
 import android.location.Location;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.provider.Settings;
 import android.view.KeyEvent;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.inputmethod.EditorInfo;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.TextView.OnEditorActionListener;
 import android.widget.Toast;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MapView;
 import com.google.android.maps.MyLocationOverlay;
 import com.google.android.maps.Overlay;
 import com.google.android.maps.Projection;
 import com.googlecode.androidannotations.annotations.AfterViews;
 import com.googlecode.androidannotations.annotations.Background;
 import com.googlecode.androidannotations.annotations.Bean;
 import com.googlecode.androidannotations.annotations.Click;
 import com.googlecode.androidannotations.annotations.EActivity;
 import com.googlecode.androidannotations.annotations.NoTitle;
 import com.googlecode.androidannotations.annotations.SystemService;
 import com.googlecode.androidannotations.annotations.UiThread;
 import com.googlecode.androidannotations.annotations.ViewById;
 import com.project202.AboutDialogHelper;
 import com.project202.LogHelper;
 import com.project202.R;
 import com.project202.loading.DownloadActivity_;
 import com.project202.loading.RatingDownloadTask;
 
 @EActivity(R.layout.address_map)
 @NoTitle
 public class AddressActivity extends MapActivity {
 
 	@ViewById
 	MapView mapView;
 
 	@ViewById
 	EditText addressEditText;
 
 	@ViewById
 	View addressHint;
 
 	@ViewById
 	View searchButton;
 
 	@ViewById
 	View locationButton;
 
 	@ViewById
 	View historyButton;
 
 	@SystemService
 	InputMethodManager inputMethodManager;
 
 	@Bean
 	AboutDialogHelper aboutDialogHelper;
 
 	@SystemService
 	LocationManager locationManager;
 
 	private MyLocationOverlay myLocationOverlay;
 
 	private MapController mapController;
 
 	private AddressOverlay addressOverlay;
 
 	private boolean shouldMoveToMyLocationOnFirstFix;
 
 	private Geocoder geocoder;
 
 	private SearchOverlay searchOverlay;
 
 	private boolean hasHistory;
 
 	@AfterViews
 	void initLayout() {
 		geocoder = new Geocoder(this);
 
 		addressEditText.addTextChangedListener(new AbstractTextWatcher() {
 			@Override
 			public void onTextChanged(CharSequence s, int start, int before, int count) {
 				if (s.length() == 0) {
 					onAddressEmpty();
 				} else {
 					onAddressNotEmpty();
 				}
 			}
 		});
 		addressEditText.setOnEditorActionListener(new OnEditorActionListener() {
 
 			@Override
 			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
 				if (actionId == EditorInfo.IME_ACTION_SEARCH) {
 					searchButtonClicked();
 					return true;
 				}
 				return false;
 			}
 		});
 
 		List<Overlay> mapOverlays = mapView.getOverlays();
 
 		GestureOverlay gestureOverlay = new GestureOverlay(this, new AbstractGestureListener() {
 			@Override
 			public void onLongPress(MotionEvent e) {
 				Projection projection = mapView.getProjection();
 				GeoPoint location = projection.fromPixels((int) e.getX(), (int) e.getY());
 				showAddressPopup(location);
 			}
 
 			@Override
 			public boolean onSingleTapUp(MotionEvent e) {
 				shouldMoveToMyLocationOnFirstFix = false;
 				float x = e.getX();
 				float y = e.getY();
 				boolean addressTapped = addressOverlay.onSingleTapUp(x, y);
 				if (addressTapped) {
 					GeoPoint addressLocation = addressOverlay.getAddressLocation();
 					Address address = addressOverlay.getAddress();
 					noteAddress(address, addressLocation);
 				} else {
 					Address tappedAddress = searchOverlay.onSingleTapUp(x, y);
 					if (tappedAddress != null) {
 						showAddressPopup(tappedAddress);
 					} else {
 						Projection projection = mapView.getProjection();
 						GeoPoint location = projection.fromPixels((int) x, (int) y);
 						boolean myLocationTapped = myLocationOverlay.onTap(location, mapView);
 						if (!myLocationTapped) {
 							addressOverlay.hideAddressPopup();
 						}
 					}
 				}
 				return true;
 			}
 		});
 
 		myLocationOverlay = new MyLocationOverlay(this, mapView) {
 			@Override
 			protected boolean dispatchTap() {
 				GeoPoint myLocation = getMyLocation();
 				showAddressPopup(myLocation);
 				return true;
 			}
 		};
 
		addressOverlay = new AddressOverlay(this);
 
 		searchOverlay = new SearchOverlay(this);
 
 		mapOverlays.add(myLocationOverlay);
 		mapOverlays.add(searchOverlay);
 		mapOverlays.add(addressOverlay);
 		mapOverlays.add(gestureOverlay);
 
 		mapController = mapView.getController();
 		mapController.setZoom(18);
 
 		/*
 		 * Init location
 		 */
 
 		Criteria criteria = new Criteria();
 		String provider = locationManager.getBestProvider(criteria, true);
 		if (provider != null) {
 			Location lastKnownLocation = locationManager.getLastKnownLocation(provider);
 
 			if (lastKnownLocation != null) {
 				int lastKnownLatitude = (int) (lastKnownLocation.getLatitude() * 1E6);
 				int lastKnownLongitude = (int) (lastKnownLocation.getLongitude() * 1E6);
 				GeoPoint initGeoPoint = new GeoPoint(lastKnownLatitude, lastKnownLongitude);
 				mapController.animateTo(initGeoPoint);
 			}
 		}
 
 		moveToMyLocationOnFirstFix();
 	}
 
 	protected void onAddressEmpty() {
 		searchButton.setVisibility(View.GONE);
 		locationButton.setVisibility(View.VISIBLE);
 		addressHint.setVisibility(View.VISIBLE);
 	}
 
 	protected void onAddressNotEmpty() {
 		searchButton.setVisibility(View.VISIBLE);
 		locationButton.setVisibility(View.GONE);
 		addressHint.setVisibility(View.GONE);
 	}
 
 	@Click
 	void searchButtonClicked() {
 		addressOverlay.hideAddressPopup();
 		inputMethodManager.hideSoftInputFromWindow(addressEditText.getWindowToken(), 0);
 		String address = addressEditText.getText().toString();
 		findAddressLocations(address);
 	}
 
 	@Click
 	void historyButtonClicked() {
 		if (hasHistory) {
 			Toast.makeText(this, "MOCK History Button Clicked", Toast.LENGTH_LONG).show();
 		}
 	}
 
 	@Background
 	void findAddressLocations(String address) {
 		try {
 			List<Address> addresses = geocoder.getFromLocationName(address, 8);
 
 			if (addresses.size() == 0) {
 				noAddressFound();
 			} else {
 				addressesFound(addresses);
 			}
 		} catch (IOException e) {
 			LogHelper.logException("Could not retrieve addresses for " + address, e);
 			searchAddressError();
 		}
 	}
 
 	@UiThread
 	void searchAddressError() {
 		Toast.makeText(this, "Impossible de déterminer l'adresse, merci de réessayer", Toast.LENGTH_LONG).show();
 	}
 
 	@UiThread
 	void noAddressFound() {
 		Toast.makeText(this, "Aucun résultat correspondant à l'adresse, merci de réessayer", Toast.LENGTH_LONG).show();
 	}
 
 	@UiThread
 	void addressesFound(List<Address> addresses) {
 
 		List<GeoPoint> geopoints = new ArrayList<GeoPoint>();
 
 		int minLat = Integer.MAX_VALUE;
 		int maxLat = Integer.MIN_VALUE;
 		int minLon = Integer.MAX_VALUE;
 		int maxLon = Integer.MIN_VALUE;
 		for (Address address : addresses) {
 			GeoPoint geoPoint = new GeoPoint((int) (address.getLatitude() * 1E6), (int) (address.getLongitude() * 1E6));
 			geopoints.add(geoPoint);
 
 			int lat = geoPoint.getLatitudeE6();
 			int lon = geoPoint.getLongitudeE6();
 
 			maxLat = Math.max(lat, maxLat);
 			minLat = Math.min(lat, minLat);
 			maxLon = Math.max(lon, maxLon);
 			minLon = Math.min(lon, minLon);
 		}
 
 		double fitFactor = 1.5;
 		mapController.zoomToSpan((int) (Math.abs(maxLat - minLat) * fitFactor), (int) (Math.abs(maxLon - minLon) * fitFactor));
 		mapController.animateTo(new GeoPoint((maxLat + minLat) / 2, (maxLon + minLon) / 2));
 
 		searchOverlay.setAddresses(addresses, geopoints);
 
 	}
 
 	private void showAddressPopup(Address address) {
 		GeoPoint location = new GeoPoint((int) (address.getLatitude() * 1E6), (int) (address.getLongitude() * 1E6));
 		shouldMoveToMyLocationOnFirstFix = false;
 		addressOverlay.showAddressPopup(location);
 		addressOverlay.setAddress(address);
 		mapController.animateTo(location);
 	}
 
 	private void showAddressPopup(GeoPoint location) {
 		shouldMoveToMyLocationOnFirstFix = false;
 		addressOverlay.showAddressPopup(location);
 		mapController.animateTo(location);
 		findAddress(location);
 	}
 
 	@Background
 	void findAddress(GeoPoint location) {
 		try {
 			List<Address> addresses = geocoder.getFromLocation(location.getLatitudeE6() / 1E6, location.getLongitudeE6() / 1E6, 1);
 			if (addresses.size() > 0) {
 				Address address = addresses.get(0);
 				addressFound(address);
 			} else {
 				noAddressFromLocationFound();
 			}
 		} catch (IOException e) {
 			LogHelper.logException("Could not find address for location", e);
 			searchAddressFromLocationError();
 		}
 	}
 	
 	@UiThread
 	void addressFound(Address address) {
		showAddressPopup(address);
 	}
 	
 	@UiThread
 	void searchAddressFromLocationError() {
 		Toast.makeText(this, "Impossible de déterminer l'adresse, merci de réessayer", Toast.LENGTH_LONG).show();
 		addressOverlay.hideAddressPopup();
 	}
 
 	@UiThread
 	void noAddressFromLocationFound() {
 		Toast.makeText(this, "Aucun résultat correspondant à l'adresse, merci de réessayer", Toast.LENGTH_LONG).show();
 		addressOverlay.hideAddressPopup();
 	}
 
 	@Click
 	void locationButtonClicked() {
 		if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
 			moveToMyLocation();
 		} else {
 			Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
 			startActivity(intent);
 		}
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		myLocationOverlay.enableMyLocation();
 
 		File directory = getFilesDir();
 		File[] historyFiles = directory.listFiles(new FilenameFilter() {
 
 			@Override
 			public boolean accept(File dir, String filename) {
 				return filename.startsWith(RatingDownloadTask.HISTO_FILE_PREFIX);
 			}
 		});
 
 		if (historyFiles.length > 0) {
 			hasHistory = true;
 		} else {
 			hasHistory = false;
 		}
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		myLocationOverlay.disableMyLocation();
 	}
 
 	@Override
 	protected boolean isRouteDisplayed() {
 		return false;
 	}
 
 	private void moveToMyLocation() {
 		GeoPoint currentLocation = myLocationOverlay.getMyLocation();
 		if (currentLocation != null) {
 			try {
 				mapController.setZoom(18);
 				mapController.animateTo(currentLocation);
 			} catch (Exception e) {
 				LogHelper.logException("Could not animate to " + currentLocation, e);
 			}
 		} else {
 			moveToMyLocationOnFirstFix();
 			Toast.makeText(this, R.string.searching_location, Toast.LENGTH_SHORT).show();
 		}
 	}
 
 	private void moveToMyLocationOnFirstFix() {
 		shouldMoveToMyLocationOnFirstFix = true;
 		myLocationOverlay.runOnFirstFix(new Runnable() {
 			public void run() {
 				if (shouldMoveToMyLocationOnFirstFix) {
 					moveToMyLocation();
 				}
 			}
 		});
 	}
 
 	protected void noteAddress(Address address, GeoPoint location) {
 		addressOverlay.hideAddressPopup();
 
 		StringBuilder sb = new StringBuilder();
 		boolean first = true;
 		for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
 			if (first) {
 				first = false;
 			} else {
 				sb.append(", ");
 			}
 			sb.append(address.getAddressLine(i));
 		}
 
 		DownloadActivity_.intent(this) //
 				.address(sb.toString()) //
 				.latitudeE6(location.getLatitudeE6()) //
 				.longitudeE6(location.getLongitudeE6()) //
 				.start();
 	}
 
 	@Click
 	void homeClicked() {
 		showDialog(R.id.about_dialog);
 	}
 
 	@Override
 	protected Dialog onCreateDialog(int id, Bundle args) {
 		switch (id) {
 		case R.id.about_dialog:
 			return aboutDialogHelper.createAboutDialog();
 		default:
 			return null;
 		}
 	}
 }
