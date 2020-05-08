 /**
  *
  * @author David Findley (ThinksInBits)
  * 
  * The source for this application may be found in its entirety at 
  * https://github.com/ThinksInBits/OU-Mobile-App
  * 
  * This application is published on the Google Play Store under
  * the title: OU Mobile Alpha:
  * https://play.google.com/store/apps/details?id=com.geared.ou
  * 
  * If you want to follow the official development of this application
  * then check out my Trello board for the project at:
  * https://trello.com/board/ou-app/4f1f697a28390abb75008a97
  * 
  * Please email me at: thefindley@gmail.com with questions.
  * 
  */
 
 package com.geared.ou;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.drawable.Drawable;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.view.View;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MapView;
 import com.google.android.maps.MyLocationOverlay;
 import com.google.android.maps.Overlay;
 import com.google.android.maps.OverlayItem;
 import com.slidingmenu.lib.SlidingMenu;
 import com.slidingmenu.lib.app.SlidingMapActivity;
 
 /**
  *
  * This is a top level activity that displays News that is read from oudaily.com
  * to the user. Currently this is just a framework, and none of the real
  * functionality has been implemented.
  * 
  */
 public class CampusMapActivity extends SlidingMapActivity implements View.OnClickListener, TextWatcher  {
 	
 	private MapView mapView;
 	private OUApplication app;
 	private TextView whoAmI;
 	private CampusLocations campusLocations;
 	private CampusMapOverlay itemizedoverlay;
 	private ListView locationsListView;
 	private LocationsAdapter locationsAdapter;
 	private Context c;
 	private CampusMapActivity tc;
 	private ActionBar actionBar;
 	private MyLocationOverlay userLocation;
 	private EditText buildingSearch;
 	private ImageView clearSearchImage;
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle icicle) {
         super.onCreate(icicle);
         app = (OUApplication)getApplication();
         c = (Context)this;
         tc = this;
         
         setContentView(R.layout.map);
         setBehindLeftContentView(R.layout.side_nav);
         setBehindRightContentView(R.layout.side_locations);
         
         locationsListView = (ListView) findViewById(R.id.locationsListView);
         buildingSearch = (EditText) findViewById(R.id.buildingSearch);
         buildingSearch.addTextChangedListener(this);
         clearSearchImage = (ImageView) findViewById(R.id.searchClearText);
         whoAmI = (TextView)findViewById(R.id.whoAmI);
         if (!app.getUser().isEmpty())
         {
         	whoAmI.setText(getResources().getString(R.string.loggedInAsText)+" "+app.getUser());
         }
         else
         {
         	whoAmI.setText(R.string.loginButtonText);
         }
         
         actionBar = getSupportActionBar();
         if (actionBar != null)
         {
         	actionBar.setIcon(R.drawable.side_menu_button);
         	actionBar.setTitle(R.string.mapButton);
         	actionBar.setDisplayHomeAsUpEnabled(true);
         }
         
         SlidingMenu sm = getSlidingMenu();
         sm.setBehindWidth(350, SlidingMenu.BOTH);
         
         campusLocations = new CampusLocations();
         
         
         mapView = (MapView) findViewById(R.id.mapview);
         mapView.setBuiltInZoomControls(true);
         List<GeoPoint> points = new ArrayList<GeoPoint>();
         points.add(new GeoPoint(35211098, -97447894));
         points.add(new GeoPoint(35203866, -97441263));
         setMapBoundsToPois(points,0.0,0.0,mapView);
 
         new LoadLocations().execute();
         
         //Overlays
         List<Overlay> mapOverlays = mapView.getOverlays();
         Drawable drawable = this.getResources().getDrawable(R.drawable.map_marker);
         itemizedoverlay = new CampusMapOverlay(drawable,this);
         mapOverlays.add(itemizedoverlay);
         
         userLocation = new MyLocationOverlay(this, mapView);
         mapOverlays.add(userLocation);
         mapView.postInvalidate();
     }
     
     @Override
 	protected void onPause() {
 		userLocation.disableCompass();
 		userLocation.disableMyLocation();
 		super.onPause();
 	}
 
 	@Override
 	protected void onResume() {
 		userLocation.enableCompass();
 		userLocation.enableMyLocation();
 		super.onResume();
 	}
 
 	private class LoadLocations extends AsyncTask<Integer, Integer, Boolean> {
         @Override
         protected Boolean doInBackground(Integer... sg) {
             return campusLocations.loadLocations(app.getDb());
         }
 
         @Override
         protected void onProgressUpdate(Integer... values) {
             super.onProgressUpdate(values);
             // Update percentage
         }
 
         @Override
         protected void onPostExecute(Boolean result) {
             super.onPostExecute(result);
             if (result == false)
             	return;
             locationsAdapter = new LocationsAdapter(c, campusLocations, tc);
             locationsListView.setAdapter(locationsAdapter);
         }
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getSupportMenuInflater();
         inflater.inflate(R.menu.map_menu, menu);
         return super.onCreateOptionsMenu(menu);
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch(item.getItemId()) {
         	case android.R.id.home:
         		toggle(SlidingMenu.LEFT);
         		break;
         	case R.id.mapRightMenu:
         		toggle(SlidingMenu.RIGHT);
         	default:
             	break;
         }
         return super.onOptionsItemSelected(item);
     }
     
 	public void setMapBoundsToPois(List<GeoPoint> items, double hpadding, double vpadding, MapView mv) {
         MapController mapController = mv.getController();
         // If there is only on one result
         // directly animate to that location
 
         if (items.size() == 1) { // animate to the location
             mapController.animateTo(items.get(0));
         } else {
             // find the lat, lon span
             int minLatitude = Integer.MAX_VALUE;
             int maxLatitude = Integer.MIN_VALUE;
             int minLongitude = Integer.MAX_VALUE;
             int maxLongitude = Integer.MIN_VALUE;
 
             // Find the boundaries of the item set
             for (GeoPoint item : items) {
                 int lat = item.getLatitudeE6(); int lon = item.getLongitudeE6();
 
                 maxLatitude = Math.max(lat, maxLatitude);
                 minLatitude = Math.min(lat, minLatitude);
                 maxLongitude = Math.max(lon, maxLongitude);
                 minLongitude = Math.min(lon, minLongitude);
             }
 
             // leave some padding from corners
             // such as 0.1 for hpadding and 0.2 for vpadding
             maxLatitude = maxLatitude + (int)((maxLatitude-minLatitude)*hpadding);
             minLatitude = minLatitude - (int)((maxLatitude-minLatitude)*hpadding);
 
             maxLongitude = maxLongitude + (int)((maxLongitude-minLongitude)*vpadding);
             minLongitude = minLongitude - (int)((maxLongitude-minLongitude)*vpadding);
 
             // Calculate the lat, lon spans from the given pois and zoom
             mapController.zoomToSpan(Math.abs(maxLatitude - minLatitude), Math
     .abs(maxLongitude - minLongitude));
 
             // Animate to the center of the cluster of points
             mapController.animateTo(new GeoPoint(
                   (maxLatitude + minLatitude) / 2, (maxLongitude + minLongitude) / 2));
         }
     }
 	
 	public void userSideMenuButton(View v)
     {
     	if (app.getUser().isEmpty())
     	{
     		app.setCurrentFragment(OUApplication.FRAGMENT_PREFS);
     		startActivity(new Intent(this, NewsActivity.class).setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY));
 			toggle(SlidingMenu.LEFT);
     	}
     	else
     	{
     		
     	}
     }
 	
 	public void aboutButton(View v)
     {
 		app.setCurrentFragment(OUApplication.FRAGMENT_ABOUT);
 		startActivity(new Intent(this, NewsActivity.class).setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY));
 		toggle(SlidingMenu.LEFT);
     }
 	
 	public void sideNavItemSelected(View v)
     {
     	switch(v.getId())
     	{
     		case R.id.news_button:
     			app.setCurrentFragment(OUApplication.FRAGMENT_NEWS);
     			startActivity(new Intent(this, NewsActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
     			break;
     		case R.id.classes_button:
     			app.setCurrentFragment(OUApplication.FRAGMENT_CLASSES);
     			startActivity(new Intent(this, NewsActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
     			break;
     		case R.id.map_button:
     			
     			break;
    		case R.id.bus_button:
    			app.setCurrentFragment(OUApplication.FRAGMENT_ROUTE_LIST);
    			startActivity(new Intent(this, NewsActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
 			default:
 				break;
     	}
     	toggle(SlidingMenu.LEFT);
     }
        
     @Override
     protected boolean isRouteDisplayed() {
         return false;
     }
 
 	public void onClick(View v) {
 		OverlayItem overlayItem = locationsAdapter.getItem(v.getId());
 		itemizedoverlay.removeAllItems();
 		itemizedoverlay.addOverlay(overlayItem);
 		List<GeoPoint> items = new ArrayList<GeoPoint>();
 		items.add(overlayItem.getPoint());
 		setMapBoundsToPois(items, 0.0, 0.0, mapView);
 		String title = overlayItem.getTitle();
 		if (title.length() > 17)
 			title = title.substring(0,17);
 		actionBar.setTitle(getResources().getString(R.string.mapButton)+": "+title);
 		
 		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
 		imm.hideSoftInputFromWindow(buildingSearch.getWindowToken(), 0);
 		
 		toggle(SlidingMenu.RIGHT);
 	}
 
 	public void onTextChanged(CharSequence s, int start, int before, int count) {
 		if (s.length() == 0)
 			clearSearchImage.setVisibility(View.GONE);
 		if (s.length() == 1)
 			clearSearchImage.setVisibility(View.VISIBLE);
 		campusLocations.filterLocations(s.toString());
 		locationsAdapter.notifyDataSetChanged();
 	}
 
 	public void afterTextChanged(Editable s) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void beforeTextChanged(CharSequence s, int start, int count,
 			int after) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	public void clearSearch(View v)
 	{
 		buildingSearch.setText("");
 	}
     
 }
