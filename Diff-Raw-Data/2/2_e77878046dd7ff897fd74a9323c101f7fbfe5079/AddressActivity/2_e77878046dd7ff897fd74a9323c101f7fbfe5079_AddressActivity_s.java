 package com.vuzzz.android.address;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.Intent;
 import android.location.Address;
 import android.location.Criteria;
 import android.location.Geocoder;
 import android.location.Location;
 import android.location.LocationManager;
 import android.net.Uri;
 import android.os.Bundle;
 import android.provider.Settings;
 import android.view.KeyEvent;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.animation.Animation;
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
 import com.googlecode.androidannotations.annotations.Click;
 import com.googlecode.androidannotations.annotations.EActivity;
 import com.googlecode.androidannotations.annotations.NoTitle;
 import com.googlecode.androidannotations.annotations.SystemService;
 import com.googlecode.androidannotations.annotations.UiThread;
 import com.googlecode.androidannotations.annotations.ViewById;
 import com.googlecode.androidannotations.annotations.res.AnimationRes;
 import com.vuzzz.android.AbstractAnimationListener;
 import com.vuzzz.android.LogHelper;
 import com.vuzzz.android.MapHelper;
 import com.vuzzz.android.R;
 import com.vuzzz.android.ShowNoteActivity_;
 import com.vuzzz.android.VuzZzConfig;
 import com.vuzzz.android.loading.DownloadActivity_;
 import com.vuzzz.android.model.Rating;
 import com.vuzzz.android.views.HelpView;
 
 @EActivity
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
 
 	@ViewById
 	View loadingMenuView;
 
 	@ViewById
 	View actionBar;
 
 	@ViewById
 	HelpView helpView;
 
 	@AnimationRes
 	Animation slideInFromBottom;
 
 	@AnimationRes
 	Animation slideOutFromTop;
 
 	@SystemService
 	InputMethodManager inputMethodManager;
 
 	@SystemService
 	LocationManager locationManager;
 
 	private MyLocationOverlay myLocationOverlay;
 
 	private MapController mapController;
 
 	private AddressOverlay addressOverlay;
 
 	private boolean shouldMoveToMyLocationOnFirstFix;
 
 	private Geocoder geocoder;
 
 	private SearchOverlay searchOverlay;
 
 	private boolean hasHistory;
 
 	private boolean loading;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		if (VuzZzConfig.MUI) {
 			setContentView(R.layout.address_map_mui);
 		} else {
 			setContentView(R.layout.address_map);
 		}
 
 	}
 
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
 
 				if (MapHelper.isInRestrictedArea(location)) {
 					showAddressPopup(location);
 				} else {
 					mapView.getController().animateTo(MapHelper.getRestrictedAreaCenter());
 				}
 			}
 
 			@Override
 			public boolean onSingleTapUp(MotionEvent e) {
 				shouldMoveToMyLocationOnFirstFix = false;
 				float x = e.getX();
 				float y = e.getY();
 				GeoPoint addressLocation = addressOverlay.onSingleTapUp(x, y);
 				if (addressLocation != null) {
 					Address address = addressOverlay.getAddress();
 					if (address != null) {
 						noteAddress(address, addressLocation);
 					}
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
 
 		addressOverlay = new AddressOverlay(this, mapView);
 
 		searchOverlay = new SearchOverlay(this);
 
 		mapOverlays.add(myLocationOverlay);
 		mapOverlays.add(searchOverlay);
 		mapOverlays.add(addressOverlay);
 		mapOverlays.add(gestureOverlay);
 
 		// Default position
 		mapController = mapView.getController();
 		mapController.setZoom(MapHelper.MAX_ZOOM);
 		mapController.animateTo(MapHelper.getRestrictedAreaCenter());
 
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
 				if (MapHelper.isInRestrictedArea(initGeoPoint)) {
 					mapController.animateTo(initGeoPoint);
 				} else {
 					mapController.animateTo(MapHelper.getRestrictedAreaCenter());
 				}
 			}
 		}
 
 		moveToMyLocationOnFirstFix();
 
 		handleVuzZzIntent();
 
 	}
 
 	@Override
 	public void onNewIntent(Intent intent) {
 		setIntent(intent);
 		handleVuzZzIntent();
 	}
 
 	private void handleVuzZzIntent() {
 		Intent intent = getIntent();
 		if (intent.getAction() != null && intent.getAction().equals(Intent.ACTION_VIEW)) {
 			Uri data = intent.getData();
 			String queryParameter = data.getQueryParameter("q");
 			if (queryParameter != null) {
 				int comma = queryParameter.indexOf(',');
 				if (comma != -1 && comma < queryParameter.length() - 1) {
 					try {
 						double latitude = Double.parseDouble(queryParameter.substring(0, comma));
 						double longitude = Double.parseDouble(queryParameter.substring(comma + 1));
 						GeoPoint location = new GeoPoint((int) (latitude * 1E6), (int) (longitude * 1E6));
 						showAddressPopup(location);
 						return;
 					} catch (NumberFormatException e) {
 						LogHelper.logException("Could not parse coordinates", e);
 					}
 				}
 			}
 
 			Toast.makeText(this, "Désolé, l'adresse n'est pas lisible", Toast.LENGTH_SHORT).show();
 		}
 	}
 
 	protected void onAddressEmpty() {
 		if (loading) {
 			loadingMenuView.setVisibility(View.VISIBLE);
 			locationButton.setVisibility(View.GONE);
 		} else {
 			loadingMenuView.setVisibility(View.GONE);
 			locationButton.setVisibility(View.VISIBLE);
 		}
 		searchButton.setVisibility(View.GONE);
 		addressHint.setVisibility(View.VISIBLE);
 	}
 
 	protected void onAddressNotEmpty() {
 		if (loading) {
 			loadingMenuView.setVisibility(View.VISIBLE);
 			searchButton.setVisibility(View.GONE);
 		} else {
 			loadingMenuView.setVisibility(View.GONE);
 			searchButton.setVisibility(View.VISIBLE);
 		}
 		locationButton.setVisibility(View.GONE);
 		addressHint.setVisibility(View.GONE);
 	}
 
 	@Click
 	void searchButtonClicked() {
 		if (helpView.getVisibility() == View.VISIBLE) {
 			hideHelp();
 		}
 		if (loading) {
 			Toast.makeText(this, "Vous êtes déjà en train de rechercher une adresse", Toast.LENGTH_SHORT).show();
 			return;
 		}
 		addressOverlay.hideAddressPopup();
 		inputMethodManager.hideSoftInputFromWindow(addressEditText.getWindowToken(), 0);
 		String address = addressEditText.getText().toString();
 		findAddressLocations(address);
 		loading = true;
 		updateLoading();
 	}
 
 	private void updateLoading() {
 		if (addressEditText.getText().length() > 0) {
 			onAddressNotEmpty();
 		} else {
 			onAddressEmpty();
 		}
 	}
 
 	@Click
 	void historyButtonClicked() {
 		if (hasHistory) {
 			if (helpView.getVisibility() == View.VISIBLE) {
 				hideHelp();
 				slideOutFromTop.setAnimationListener(new AbstractAnimationListener() {
 					@Override
 					public void onAnimationEnd(Animation animation) {
 						ShowNoteActivity_.intent(AddressActivity.this).start();
 					}
 				});
 			} else {
 				ShowNoteActivity_.intent(this).start();
 			}
 		} else {
 			if (helpView.getVisibility() == View.VISIBLE) {
 				hideHelp();
 			}
 			Toast.makeText(this, "Historique vide", Toast.LENGTH_SHORT).show();
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
 		loading = false;
 		updateLoading();
 	}
 
 	@UiThread
 	void noAddressFound() {
 		Toast.makeText(this, "Aucun résultat correspondant à l'adresse, merci de réessayer", Toast.LENGTH_LONG).show();
 		loading = false;
 		updateLoading();
 	}
 
 	@UiThread
 	void addressesFound(List<Address> addresses) {
 		loading = false;
 		updateLoading();
 
 		List<GeoPoint> geopoints = new ArrayList<GeoPoint>();
 
 		int minLat = Integer.MAX_VALUE;
 		int maxLat = Integer.MIN_VALUE;
 		int minLon = Integer.MAX_VALUE;
 		int maxLon = Integer.MIN_VALUE;
 		for (Address address : addresses) {
 			GeoPoint geoPoint = new GeoPoint((int) (address.getLatitude() * 1E6), (int) (address.getLongitude() * 1E6));
 
 			if (MapHelper.isInRestrictedArea(geoPoint)) {
 				geopoints.add(geoPoint);
 			} else {
 				mapView.getController().animateTo(MapHelper.getRestrictedAreaCenter());
 			}
 
 			int lat = geoPoint.getLatitudeE6();
 			int lon = geoPoint.getLongitudeE6();
 
 			maxLat = Math.max(lat, maxLat);
 			minLat = Math.min(lat, minLat);
 			maxLon = Math.max(lon, maxLon);
 			minLon = Math.min(lon, minLon);
 		}
 
 		if (geopoints.isEmpty()) {
 			outsideAreaAddressFound();
 		} else {
 			double fitFactor = 1.5;
 			mapController.zoomToSpan((int) (Math.abs(maxLat - minLat) * fitFactor), (int) (Math.abs(maxLon - minLon) * fitFactor));
 			mapController.animateTo(new GeoPoint((maxLat + minLat) / 2, (maxLon + minLon) / 2));
 
 			searchOverlay.setAddresses(addresses, geopoints);
 		}
 
 	}
 
 	@UiThread
 	protected void outsideAreaAddressFound() {
		Toast.makeText(this, "L'adresse que vous avez indique est en dehors de la ville concerne.", Toast.LENGTH_LONG).show();
 		loading = false;
 		updateLoading();
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
 		addressOverlay.setAddress(address);
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
 		if (helpView.getVisibility() == View.VISIBLE) {
 			hideHelp();
 		}
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
 
 		File[] historyFiles = Rating.listHistoryFiles(this);
 
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
 		if (helpView.getVisibility() == View.VISIBLE) {
 			hideHelp();
 		} else {
 			showHelp();
 		}
 	}
 
 	private void hideHelp() {
 		helpView.startAnimation(slideOutFromTop);
 		helpView.setVisibility(View.GONE);
 	}
 
 	private void showHelp() {
 		helpView.startAnimation(slideInFromBottom);
 		helpView.setVisibility(View.VISIBLE);
 	}
 
 }
