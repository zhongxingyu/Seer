 package com.khaotixinc.barhopper;
 
 import java.util.ArrayList;
 
 import net.simonvt.menudrawer.MenuDrawer;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.pm.ApplicationInfo;
 import android.content.pm.PackageManager;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.graphics.Color;
 import android.graphics.Typeface;
 import android.location.Location;
 import android.location.LocationManager;
 import android.net.Uri;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.text.SpannableStringBuilder;
 import android.text.style.CharacterStyle;
 import android.text.style.StyleSpan;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.SupportMapFragment;
 import com.google.android.gms.maps.model.BitmapDescriptorFactory;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.MarkerOptions;
 import com.khaotixinc.barhopper.menu.Item;
 import com.khaotixinc.barhopper.menu.MenuAdapter;
 
 public class MapActivity extends FragmentActivity {
 
 	private MenuDrawer menuDrawer;
 
 	private ListView menuListView;
 	private MenuAdapter menuAdapter;
 
 	private GoogleMap map;
 
 	Location givenLocation;
 	boolean sensor;
 	float givenRating;
 
 	ArrayList<Bar> bars;
 	JSONBarManager manager;
 
 	Button nextButton, prevButton;
 
 	private final Activity thisActivity = this;
 
 	boolean restarted = false;
 	boolean startedFromMain = false;
 	boolean loadingInitialMap = true;
 
 	Runnable updateRunnable;
 
 	ProgressBar loadingBar;
 	boolean loading = false;
 
 	LoadingDialog dialog = new LoadingDialog();
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		Object lastManager = getLastCustomNonConfigurationInstance();
 
 		if (lastManager == null){
 			manager = new JSONBarManager(this);
 			manager.setActivityInitialLoading(true);
 		}
 		else {
 			manager = (JSONBarManager) lastManager;
 			manager.setActivity(this);
 		}
 
 		dialog.setCancelable(false);
 		
 		if (manager.isInitialLoading())
 			dialog.show(getSupportFragmentManager(), "loading");
 
 		Intent intent = getIntent();
 
 		Bundle bundle = intent.getBundleExtra("BUNDLE_EXTRA");
 
 		if (bundle != null) {
 
 			givenLocation = bundle.getParcelable("EXTRA_MY_LOCATION");
 
 			sensor = bundle.getBoolean("EXTRA_SENSOR", false);
 
 			givenRating = bundle.getFloat("EXTRA_RATING", 2.5f);
 
 			startedFromMain = intent.getBooleanExtra("FROM_MAIN_ACTIVITY",
 					false);
		} else {
 			Toast.makeText(this, "There was an error. Please try again.", Toast.LENGTH_LONG).show();
 			finish();
 		}
 
 		if (savedInstanceState != null)
 			restarted = savedInstanceState.getBoolean("RESTARTED", false);
 		else
 			restarted = false;
 
 		// Adding a left menu drawer to the activity
 		menuDrawer = MenuDrawer.attach(this, MenuDrawer.MENU_DRAG_CONTENT);
 
 		// Make the menu appear by dragging it from anywhere on the screen
 		menuDrawer.setTouchMode(MenuDrawer.TOUCH_MODE_BEZEL);
 
 		// sets the content for the drawer
 		menuDrawer.setContentView(R.layout.activity_map);
 
 		menuAdapter = new MenuAdapter(this);
 
 		// Populate the menu list
 		menuAdapter.addItems(getString(R.string.menu_category_search),
 				MenuAdapter.ItemType.CATEGORY);
 		menuAdapter.addItems(getString(R.string.menu_item_search),
 				MenuAdapter.ItemType.ITEM);
 		menuAdapter.addItems(getString(R.string.menu_category_show),
 				MenuAdapter.ItemType.CATEGORY);
 		menuAdapter.addItems(getString(R.string.menu_item_like),
 				MenuAdapter.ItemType.ITEM);
 		menuAdapter.addItems(getString(R.string.menu_item_dislike),
 				MenuAdapter.ItemType.ITEM);
 
 		// sets the menu
 		menuListView = new ListView(this);
 		menuListView.setAdapter(menuAdapter);
 		menuListView.setOnItemClickListener(menuItemClickListener);
 
 		menuListView.setPadding(25, 25, 25, 25);
 		menuListView.setDivider(null);
 
 		menuDrawer.setMenuView(menuListView);
 
 		
 
 		// updateButtons();
 		updateRunnable = new Runnable() {
 
 			@Override
 			public void run() {
 				updateButtons();
 			}
 		};
 
 		runOnUiThread(updateRunnable);
 	}
 
 	private AdapterView.OnItemClickListener menuItemClickListener = new AdapterView.OnItemClickListener() {
 		@Override
 		public void onItemClick(AdapterView<?> parent, View view, int position,
 				long id) {
 			if (menuAdapter.getItem(position) instanceof Item) {
 				Item item = (Item) menuAdapter.getItem(position);
 
 				if (item.getName().equals(getString(R.string.menu_item_search))) {
 					menuDrawer.closeMenu();
 
 					finish();
 				}
 
 			}
 			menuDrawer.closeMenu();
 		}
 	};
 
 	public void onMenuToggleClick(View view) {
 		menuDrawer.toggleMenu();
 
 	}
 
 	@Override
 	protected void onStart() {
 		super.onStart(); // Always call the superclass method first
 
 		// The activity is either being restarted or started for the first time
 		// so this is where we should make sure that GPS is enabled
 		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 		boolean gpsEnabled = locationManager
 				.isProviderEnabled(LocationManager.GPS_PROVIDER);
 
 		if (!gpsEnabled) {
 			// Create a dialog here that requests the user to enable GPS, and
 			// use an intent
 			// with the
 			// android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS action
 			// to take the user to the Settings screen to enable GPS when they
 			// click "OK"
 		}
 
 		InitialMapSetup();
 
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 
 		if (givenLocation != null)
 			if (manager.size() == 0) {
 				if (startedFromMain) {
 					downloadBarList(createGoogleURL(
 							new LatLng(givenLocation.getLatitude(),
 									givenLocation.getLongitude()), 50000,
 							sensor));
 				}
 			} else if (manager.size() > 0)
 				SetupMap();
 
 		runOnUiThread(updateRunnable);
 	}
 
 	public void SetupMap() {
 		// Do a null check to confirm that we have not already instantiated the
 		// map.
 		if (map == null) {
 			// Try to obtain the map from the SupportMapFragment.
 			map = ((SupportMapFragment) getSupportFragmentManager()
 					.findFragmentById(R.id.map)).getMap();
 		}
 
 		map.clear();
 
 		// add marker to show current location (in grey)
 
 		LatLng herePoint = new LatLng(givenLocation.getLatitude(),
 				givenLocation.getLongitude());
 
 		map.addMarker(new MarkerOptions()
 				.position(herePoint)
 				.title("You are here")
 				.draggable(false)
 				.icon(BitmapDescriptorFactory
 						.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));
 		// center map on your location
 
 		// center on the here point
 
 		if (manager.index >= manager.getBars().size())
 			manager.index = manager.getBars().size() - 1;
 
 		if (manager.index < 0)
 			manager.index = 0;
 
 		if (manager.getBars().size() == 0)
 			return;
 
 		Bar bar = manager.getBars().get(manager.index);
 
 		map.addMarker(
 				new MarkerOptions()
 						.position(bar.getLatLong())
 						.title(bar.getName())
 						.snippet("Rating: " + bar.getRatingString())
 						.draggable(false)
 						.icon(BitmapDescriptorFactory
 								.defaultMarker(BitmapDescriptorFactory.HUE_RED)))
 				.showInfoWindow();
 
 		// center on the next point
 		if (restarted) {
 			map.moveCamera(CameraUpdateFactory.newLatLng(bar.getLatLong()));
 			restarted = false;
 		} else
 			map.animateCamera(CameraUpdateFactory.newLatLng(bar.getLatLong()));
 
 		populateAddressBar(bar.getName(), bar.getAddress());
 	}
 
 	public void InitialMapSetup() {
 		if (map == null) {
 			// Try to obtain the map from the SupportMapFragment.
 			map = ((SupportMapFragment) getSupportFragmentManager()
 					.findFragmentById(R.id.map)).getMap();
 		}
 
 		map.clear();
 
 		LatLng herePoint = new LatLng(givenLocation.getLatitude(),
 				givenLocation.getLongitude());
 
 		map.addMarker(new MarkerOptions()
 				.position(herePoint)
 				.title("You are here")
 				.draggable(false)
 				.icon(BitmapDescriptorFactory
 						.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));
 		// center map on your location
 		// keep zoom factor at 15f
 		map.moveCamera(CameraUpdateFactory.zoomTo(15.0f));
 		// center on the here point
 		map.moveCamera(CameraUpdateFactory.newLatLng(herePoint));
 
 	}
 
 	public void populateAddressBar(String name, String address) {
 		TextView addressBar = (TextView) findViewById(R.id.addressView1);
 
 		String text = "##" + name + "##" + "\n" + address;
 
 		addressBar.setText(setSpanBetweenTokens(text, "##", new StyleSpan(
 				Typeface.BOLD)));
 	}
 
 	public void onClickNext(View view) {
 		if (((manager.size() - manager.index) <= (manager.size() * 0.4))) {
 			manager.getMoreBars(givenRating, sensor);
 		}
 
 		manager.index++;
 		SetupMap();
 		runOnUiThread(updateRunnable);
 	}
 
 	public void onClickPrev(View view) {
 		manager.index--;
 		SetupMap();
 		runOnUiThread(updateRunnable);
 	}
 
 	public void downloadBarList(String httpURL) {
 		manager.downloadAndGetList(httpURL, givenRating);
 		// updateButtons();
 	}
 
 	public String createGoogleURL(LatLng location, int radiusMeters,
 			boolean sensor) {
 		// https://maps.googleapis.com/maps/api/place/radarsearch/json?parameters
 		String URL, key, types, rankby;
 
 		URL = getString(R.string.root_places_url);
 
 		types = "bar|night_club";
 		rankby = "distance";
 		key = "";
 
 		ApplicationInfo appInfo;
 		try {
 			appInfo = getPackageManager().getApplicationInfo(getPackageName(),
 					PackageManager.GET_META_DATA);
 			Bundle metaBundle = appInfo.metaData;
 			key = metaBundle
 					.getString(getString(R.string.google_api_version_name));
 		} catch (NameNotFoundException e) {
 			e.printStackTrace();
 		}
 
 		URL += "location="
 				+ Uri.encode(location.latitude + "," + location.longitude);
 		URL += "&types=" + Uri.encode(types);
 		// URL += "&radius=" + radiusMeters;
 		URL += "&sensor=" + Uri.encode(Boolean.toString(sensor));
 		URL += "&key=" + Uri.encode(key);
 		URL += "&rankby=" + Uri.encode(rankby);
 
 		return URL;
 	}
 
 	public void updateButtons() {
 
 		if (nextButton == null)
 			nextButton = (Button) findViewById(R.id.button_next);
 		if (prevButton == null)
 			prevButton = (Button) findViewById(R.id.button_prev);
 		if (loadingBar == null)
 			loadingBar = (ProgressBar) findViewById(R.id.progressBar1);
 
 		if (!dialog.isVisible()) {
 			if (loading)
 				loadingBar.setVisibility(View.VISIBLE);
 			else
 				loadingBar.setVisibility(View.INVISIBLE);
 		}
 
 		if (manager.size() == 0) {
 			prevButton.setEnabled(false);
 			nextButton.setEnabled(false);
 			findViewById(R.id.addressBar).setVisibility(View.GONE);
 		} else {
 			findViewById(R.id.addressBar).setVisibility(View.VISIBLE);
 
 			if (manager.index == manager.size() - 1)
 				nextButton.setEnabled(false);
 			else
 				nextButton.setEnabled(true);
 
 			if (manager.index <= 0)
 				prevButton.setEnabled(false);
 			else
 				prevButton.setEnabled(true);
 		}
 
 		if (nextButton.isEnabled()) {
 			nextButton.setBackgroundColor(Color.BLACK);
 			nextButton.setTextColor(Color.WHITE);
 		} else {
 			nextButton.setBackgroundColor(Color.GRAY);
 			nextButton.setTextColor(Color.DKGRAY);
 		}
 
 		if (prevButton.isEnabled()) {
 			prevButton.setBackgroundColor(Color.BLACK);
 			prevButton.setTextColor(Color.WHITE);
 		} else {
 			prevButton.setBackgroundColor(Color.GRAY);
 			prevButton.setTextColor(Color.DKGRAY);
 		}
 	}
 
 	public static CharSequence setSpanBetweenTokens(CharSequence text,
 			String token, CharacterStyle... cs) {
 		// Start and end refer to the points where the span will apply
 		int tokenLen = token.length();
 		int start = text.toString().indexOf(token) + tokenLen;
 		int end = text.toString().indexOf(token, start);
 
 		if (start > -1 && end > -1) {
 			// Copy the spannable string to a mutable spannable string
 			SpannableStringBuilder ssb = new SpannableStringBuilder(text);
 			for (CharacterStyle c : cs)
 				ssb.setSpan(c, start, end, 0);
 
 			// Delete the tokens before and after the span
 			ssb.delete(end, end + tokenLen);
 			ssb.delete(start - tokenLen, start);
 
 			text = ssb;
 		}
 
 		return text;
 	}
 
 	public void onMoreBarsComplete() {
 
 		runOnUiThread(new Runnable() {
 
 			@Override
 			public void run() {
 				SetupMap(); // on ui thread only when starting
 				loadingInitialMap = false;
 			}
 		});
 
 		loading = false;
 
 		if (dialog != null)
 			dialog.dismiss();
 		
 		manager.setActivityInitialLoading(false);
 
 		runOnUiThread(updateRunnable);
 
 		if (((manager.size() - manager.index) <= (manager.size() * 0.4))) {
 			manager.getMoreBars(givenRating, sensor);
 		}
 
 		if (manager.size() == 0 && manager.getNextPageToken() == null)
 			runOnUiThread(new Runnable() {
 
 				@Override
 				public void run() {
 					Toast.makeText(thisActivity, "No bars found... sorry.",
 							Toast.LENGTH_SHORT).show();
 				}
 			});
 	}
 
 	@Override
 	public Object onRetainCustomNonConfigurationInstance() {
 		manager.setActivity(null);
 		return manager;
 	}
 
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		outState.putBoolean("RESTARTED", true);
 
 		super.onSaveInstanceState(outState);
 	}
 
 }
