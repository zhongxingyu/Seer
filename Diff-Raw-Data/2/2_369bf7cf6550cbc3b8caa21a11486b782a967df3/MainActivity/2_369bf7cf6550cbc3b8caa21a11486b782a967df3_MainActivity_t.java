 /*
 	Copyright 2012 OpenTeamMap
 	
 	This software is a part of LocalizeTeaPot whose purpose is to Localize your friends.
 	
 	This software is governed by the CeCILL license under French law and
 	abiding by the rules of distribution of free software.  You can  use, 
 	modify and/ or redistribute the software under the terms of the CeCILL
 	license as circulated by CEA, CNRS and INRIA at the following URL
 	"http://www.cecill.info". 
 	
 	As a counterpart to the access to the source code and  rights to copy,
 	modify and redistribute granted by the license, users are provided only
 	with a limited warranty  and the software's author,  the holder of the
 	economic rights,  and the successive licensors  have only  limited
 	liability. 
 	
 	In this respect, the user's attention is drawn to the risks associated
 	with loading,  using,  modifying and/or developing or reproducing the
 	software by the user in light of its specific status of free software,
 	that may mean  that it is complicated to manipulate,  and  that  also
 	therefore means  that it is reserved for developers  and  experienced
 	professionals having in-depth computer knowledge. Users are therefore
 	encouraged to load and test the software's suitability as regards their
 	requirements in conditions enabling the security of their systems and/or 
 	data to be ensured and,  more generally, to use and operate it in the 
 	same conditions as regards security. 
 	
 	The fact that you are presently reading this means that you have had
 	knowledge of the CeCILL license and that you accept its terms.
  */
 
 package fr.univsavoie.ltp.client;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.osmdroid.DefaultResourceProxyImpl;
 import org.osmdroid.api.IGeoPoint;
 import org.osmdroid.bonuspack.location.FlickrPOIProvider;
 import org.osmdroid.bonuspack.location.GeoNamesPOIProvider;
 import org.osmdroid.bonuspack.location.GeocoderNominatim;
 import org.osmdroid.bonuspack.location.NominatimPOIProvider;
 import org.osmdroid.bonuspack.location.POI;
 import org.osmdroid.bonuspack.location.PicasaPOIProvider;
 import org.osmdroid.bonuspack.overlays.ExtendedOverlayItem;
 import org.osmdroid.bonuspack.overlays.ItemizedOverlayWithBubble;
 import org.osmdroid.bonuspack.overlays.MapEventsOverlay;
 import org.osmdroid.bonuspack.overlays.MapEventsReceiver;
 import org.osmdroid.bonuspack.routing.OSRMRoadManager;
 import org.osmdroid.bonuspack.routing.Road;
 import org.osmdroid.bonuspack.routing.RoadManager;
 import org.osmdroid.bonuspack.routing.RoadNode;
 import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
 import org.osmdroid.util.BoundingBoxE6;
 import org.osmdroid.util.GeoPoint;
 import org.osmdroid.views.MapController;
 import org.osmdroid.views.MapView;
 import org.osmdroid.views.overlay.ItemizedIconOverlay.OnItemGestureListener;
 import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
 import org.osmdroid.views.overlay.Overlay;
 import org.osmdroid.views.overlay.OverlayItem;
 import org.osmdroid.views.overlay.PathOverlay;
 import org.osmdroid.views.overlay.SimpleLocationOverlay;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.SherlockActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 import com.actionbarsherlock.view.SubMenu;
 
 import fr.univsavoie.ltp.client.map.POIInfoWindow;
 import fr.univsavoie.ltp.client.map.Session;
 import fr.univsavoie.ltp.client.map.Friends;
 import fr.univsavoie.ltp.client.map.FriendsAdapter;
 import fr.univsavoie.ltp.client.map.Popup;
 import fr.univsavoie.ltp.client.map.Tools;
 import fr.univsavoie.ltp.client.map.ViaPointInfoWindow;
 
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.res.Configuration;
 import android.content.res.Resources;
 import android.content.res.TypedArray;
 import android.graphics.Bitmap;
 import android.graphics.drawable.Drawable;
 import android.location.Address;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.net.NetworkInfo.DetailedState;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.MenuInflater;
 import android.view.View;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.AutoCompleteTextView;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.ListView;
 import android.widget.ScrollView;
 import android.widget.Toast;
 
 /**
  * MainActivity l'activit principale de l'application Android
  */
 public class MainActivity extends SherlockActivity implements MapEventsReceiver, LocationListener
 {
 	/* --------------------------------
 	 * Variables globales de l'activit
 	 * --------------------------------
 	 */
 	
 	/* Variables pour la bibliothque OSMdroid */
 	private MapView map;
 	private MapController mapController;
 	protected GeoPoint startPoint, destinationPoint;
 	private Location location;
 	private LocationManager locationManager;
 	private double longitude, latitude;
 	protected ArrayList<GeoPoint> viaPoints;
 	protected static int START_INDEX=-2, DEST_INDEX=-1;
 	protected ItemizedOverlayWithBubble<ExtendedOverlayItem> itineraryMarkers;
 	protected ExtendedOverlayItem markerStart, markerDestination;
 	private SimpleLocationOverlay myLocationOverlay;
 	protected Road mRoad;
 	protected PathOverlay roadOverlay;
 	protected ItemizedOverlayWithBubble<ExtendedOverlayItem> roadNodeMarkers;
 	protected static final int ROUTE_REQUEST = 3;
 	private ArrayList<POI> mPOIs;
 	private ItemizedOverlayWithBubble<ExtendedOverlayItem> poiMarkers;
 	private AutoCompleteTextView poiTagText;
 	protected static final int POIS_REQUEST = 4;
 	private ArrayList<OverlayItem> anotherOverlayItemArray;
 	private MapEventsOverlay overlay;
 	
 	/* Variables de traitements */
 	private boolean displayUserInfos;
 	private String login;
 	private ListView lvListeFriends;
 	private List<Friends> listFriends = new ArrayList<Friends>();
 	private ScrollView viewMapFilters;
 	
 	/* Variables de classes */
 	private Popup popup;
 	private Session session;
 	private Tools tools;
 	
     /* --------------------------------------------------------
      * Evenements de l'activity (onCreate, onResume, onStop...)
      * --------------------------------------------------------
      */
 	
     @Override
     public void onCreate(Bundle savedInstanceState)  
     {
     	super.onCreate(savedInstanceState);
     	
     	// Appliquer le thme LTP a l'ActionBar
     	//setTheme(R.style.Theme_ltp);
     	
     	// Cration de l'activit principale
         setContentView(R.layout.activity_main);
         
 		// Instancier les classes utiles
 		setPopup(new Popup(this));
 		setSession(new Session(this));
 		setTools(new Tools(this));
         
         // Afficher la ActionBar
         ActionBar mActionBar = getSupportActionBar();
         mActionBar.setHomeButtonEnabled(true);
         mActionBar.setDisplayShowHomeEnabled(true);
                
 		// MapView settings
         map = (MapView)findViewById(R.id.openmapview);
 		map.setTileSource(TileSourceFactory.MAPNIK);
 		map.setBuiltInZoomControls(false);
 		map.setMultiTouchControls(true);
 		
 		// MapController settings
 		mapController = map.getController();
         
 		//To use MapEventsReceiver methods, we add a MapEventsOverlay:
 		overlay = new MapEventsOverlay(this, this);
 		map.getOverlays().add(overlay);
 		
 		boolean isWifiEnabled = false;
 		boolean isGPSEnabled = false;
 		
 		// Vrifier si le wifi ou le rseau mobile est activ
 		final ConnectivityManager connMgr = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
 		final NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
 		final NetworkInfo mobile = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
 		if (wifi.isAvailable() && (wifi.getDetailedState() == DetailedState.CONNECTING || wifi.getDetailedState() == DetailedState.CONNECTED)) {
 			Toast.makeText(this, R.string.toast_wifi, Toast.LENGTH_LONG).show();
 			isWifiEnabled = true;
 		} else if (mobile.isAvailable() && (mobile.getDetailedState() == DetailedState.CONNECTING || mobile.getDetailedState() == DetailedState.CONNECTED)) {
 			Toast.makeText(this, R.string.toast_3G, Toast.LENGTH_LONG).show();
 		} else {
 			Toast.makeText(this, R.string.toast_aucun_reseau, Toast.LENGTH_LONG).show();
 		}
 		
 		// Obtenir le service de localisation
 		locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
 		
 		// Verifier si le service de localisation GPS est actif, le cas echeant, tester le rseau
 		if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
 		{
 			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 30 * 1000, 250.0f, this);
 			location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
 			isGPSEnabled = true;
 		} 
 		else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
 		{
 	    	locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 30 * 1000, 250.0f, this);
 	    	location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
 		}
 		
 		// Afficher une boite de dialogue et proposer d'activer un ou plusieurs services pas actifs
 		if(!isWifiEnabled || !isGPSEnabled)
 		{
 			//getTools().showSettingsAlert(this, isWifiEnabled, isGPSEnabled);
 		}
 		
 		// Si on a une localisation, on dfinit ses coordonnes geopoint
 		if (location != null) 
 		{
 			startPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
 		} 
 		else 
 		{
 			// Sinon, on indique des paramtres par dfaut
 			location = getTools().getLastKnownLocation(locationManager);
 			if (location == null)
 			{
 				location = new Location("");
 				location.setLatitude(46.227638);
 				location.setLongitude(2.213749000000);
 			}
 			startPoint = new GeoPoint(46.227638, 2.213749000000);
 		}
 		
 		setLongitude(location.getLongitude());
 		setLatitude(location.getLatitude());
 		
 		destinationPoint = null;
 		viaPoints = new ArrayList<GeoPoint>();
 		
 		// On recupre quelques paramtres de la session prcdents si possible
 		if (savedInstanceState == null) 
 		{
 			mapController.setZoom(15);
 			mapController.setCenter(startPoint);
 		} 
 		else 
 		{
 			mapController.setZoom(savedInstanceState.getInt("zoom_level"));
 			mapController.setCenter((GeoPoint) savedInstanceState.getParcelable("map_center"));
 		}
 		
 		// Crer un overlay sur la carte pour afficher notre point de dpart
         myLocationOverlay = new SimpleLocationOverlay(this, new DefaultResourceProxyImpl(this));
 		map.getOverlays().add(myLocationOverlay);
 		myLocationOverlay.setLocation(startPoint);
 		
 		// Boutton pour zoomer la carte
 		ImageButton btZoomIn = (ImageButton)findViewById(R.id.btZoomIn);
 		btZoomIn.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View view) {
 				map.getController().zoomIn();
 			}
 		});
 		
 		// Boutton pour dezoomer la carte
 		ImageButton btZoomOut = (ImageButton)findViewById(R.id.btZoomOut);
 		btZoomOut.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View view) {
 				map.getController().zoomOut();
 			}
 		});
         
 		// Pointeurs d'itinrairea:
 		final ArrayList<ExtendedOverlayItem> waypointsItems = new ArrayList<ExtendedOverlayItem>();
 		itineraryMarkers = new ItemizedOverlayWithBubble<ExtendedOverlayItem>(this, waypointsItems, 
 				map, new ViaPointInfoWindow(R.layout.itinerary_bubble, map));
 		map.getOverlays().add(itineraryMarkers);
 		//updateUIWithItineraryMarkers();
 		
 		Button searchButton = (Button)findViewById(R.id.buttonSearch);
 		searchButton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View view) {
 				handleSearchLocationButton();
 			}
 		});
 		
 		//context menu for clicking on the map is registered on this button. 
 		registerForContextMenu(searchButton);
         
 		// Routes et Itinraires
 		final ArrayList<ExtendedOverlayItem> roadItems = new ArrayList<ExtendedOverlayItem>();
     	roadNodeMarkers = new ItemizedOverlayWithBubble<ExtendedOverlayItem>(this, roadItems, map);
 		map.getOverlays().add(roadNodeMarkers);
 		
 		if (savedInstanceState != null)
 		{
 			mRoad = savedInstanceState.getParcelable("road");
 			updateUIWithRoad(mRoad);
 		}
 		
 		//POIs:
         //POI search interface:
         String[] poiTags = getResources().getStringArray(R.array.poi_tags);
         poiTagText = (AutoCompleteTextView) findViewById(R.id.poiTag);
         ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
 				android.R.layout.simple_dropdown_item_1line, poiTags);
         poiTagText.setAdapter(adapter);
         Button setPOITagButton = (Button) findViewById(R.id.buttonSetPOITag);
         setPOITagButton.setOnClickListener(new View.OnClickListener()
         {
 			public void onClick(View v)
 			{
 				//Hide the soft keyboard:
 				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
 				imm.hideSoftInputFromWindow(poiTagText.getWindowToken(), 0);
 				//Start search:
 				getPOIAsync(poiTagText.getText().toString());
 			}
 		});
         
         //POI markers:
 		final ArrayList<ExtendedOverlayItem> poiItems = new ArrayList<ExtendedOverlayItem>();
 		poiMarkers = new ItemizedOverlayWithBubble<ExtendedOverlayItem>(this, 
 				poiItems, map, new POIInfoWindow(map));
 		map.getOverlays().add(poiMarkers);
 		if (savedInstanceState != null){
 			mPOIs = savedInstanceState.getParcelableArrayList("poi");
 			updateUIWithPOI(mPOIs);
 		}
 		
 		// Load friends ListView
 		lvListeFriends = (ListView)findViewById(R.id.listViewFriends);
 		//lvListeFriends.setBackgroundResource(R.drawable.listview_roundcorner_item);
 		lvListeFriends.setOnItemClickListener(new OnItemClickListener() 
 		{
 			public void onItemClick(AdapterView<?> adapter, View v, int position, long id) 
 			{
 				Friends item = (Friends) adapter.getItemAtPosition(position);
 				if (item.getLongitude() != 0.0 && item.getLatitude() != 0.0)
 				{
 					destinationPoint = new GeoPoint(item.getLongitude(), item.getLatitude());
 					markerDestination = putMarkerItem(markerDestination, destinationPoint, DEST_INDEX,
 				    		R.string.destination, R.drawable.marker_destination, -1);
 					getRoadAsync();
 					map.getController().setCenter(destinationPoint);
 				}
 				else
 				{
 					Toast.makeText(MainActivity.this, R.string.toast_friend_statut, Toast.LENGTH_LONG).show();
 				}
 			}
 		});
 		
         viewMapFilters = (ScrollView) this.findViewById(R.id.scrollViewMapFilters);
         viewMapFilters.setVisibility(View.GONE);
 		
         // Initialiser tout ce qui est donnes utilisateur propres  l'activit
         init();
         
         getTools().relocateUser(mapController, map, myLocationOverlay, location);
     }
     
 	protected void onSaveInstanceState (Bundle outState)
 	{
 		outState.putParcelable("start", startPoint);
 		outState.putParcelable("destination", destinationPoint);
 		outState.putParcelableArrayList("viapoints", viaPoints);
 		outState.putParcelable("road", mRoad);
 		outState.putInt("zoom_level", map.getZoomLevel());
 		GeoPoint c = (GeoPoint) map.getMapCenter();
 		outState.putParcelable("map_center", c);
 		outState.putParcelableArrayList("poi", mPOIs);
 	}
     
 	// Cration d'un menu contenant un bouton rafraichir et un menu droulant
 	public boolean onCreateOptionsMenu(Menu menu) 
 	{
 		menu.add(0, 1, 0, R.string.back_to_my_pos).setIcon(R.drawable.ic_10_device_access_location_found)
 		.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
 
 		menu.add(0, 2, 1, R.string.user_app_settings).setIcon(R.drawable.ic_2_action_settings)
 		.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
 		
 		//menu.add(0, 3, 2, R.string.refresh_map).setIcon(R.drawable.ic_1_navigation_refresh)
 		//.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
 		
 		menu.add(0, 4, 3, R.string.search_on_map).setIcon(R.drawable.ic_2_action_search)
 		.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
 		
 		//Ajouter langue
 		SubMenu subLangue = menu.addSubMenu(0, R.id.menuLangue, 3, R.string.langue);
 		subLangue.add(0, R.id.menuEnglais, 6, R.string.english);
 		subLangue.add(0, R.id.menuFrance,  7, R.string.france);
 		subLangue.add(0, R.id.menuPersan,  8, R.string.persan);
 		subLangue.getItem().setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
 		
 		// Afficher le menu selon si utilisateur connect ou pas
 		if (login != null) 
 		{
 			SubMenu sub = menu.addSubMenu(0, 10, 4, R.string.my_account);
 			//sub.add(0, 11, 5, R.string.disply_user_infos);
 			sub.add(0, 12, 6, R.string.logout);
 			sub.getItem().setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
 			
 			menu.add(0, 13, 7, R.string.publish_status).setIcon(R.drawable.ic_6_social_chat)
 			.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
 			
 			menu.add(0, 14, 8, R.string.history_status).setIcon(R.drawable.ic_4_collections_view_as_list)
 			.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
 		} 
 		else 
 		{
 			SubMenu sub = menu.addSubMenu(0, 20, 9, R.string.my_account);
 			sub.add(0, 21, 10, R.string.signup);
 			sub.add(0, 22, 11, R.string.signin);
 			sub.getItem().setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
 		}
 		
 		//menu.add(0, 30, 12, R.string.about).setIcon(R.drawable.ic_2_action_about)
 		//.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
 
 		return true;
 	}
 
 	// Mthode callback appele lorqu'un item du menu est slcetionn
 	public boolean onOptionsItemSelected(MenuItem item) 
 	{
 		// Le bouton "retour"  le mme titre que la page.
 		if (item.getTitle().toString().compareTo(getTitle().toString()) == 0)
 		{
 			finish();
 		}
 
 		switch (item.getItemId()) 
 		{
 		// Publier son status
 		case 13:
 			getPopup().popupPublishStatus(); 
 			break;
 			
 		// Liste des derniers status
 		case 14:
 			// Rcuprer la liste des amis via appel serveur REST
 			getSession().getJSON("https://jibiki.univ-savoie.fr/ltpdev/rest.php/api/1/statuses", "STATUSES");
 			break;			
 			
 		// Quitter l'application
 		case 99:
 			finish();
 			break;			
 
 		// Se refixer au dernier point localis de l'utilisateur
 		case 1:
 			getTools().relocateUser(mapController, map, myLocationOverlay, location);
 			break;
 
 		// Afficher les prfrences
 		case 2:
 			Intent i = new Intent(MainActivity.this, UserPreferencesActivity.class);
 			startActivityForResult(i, 2);
 			break;
 			
 		// Rechercher sur la carte
 		case 4:
 			viewMapFilters.setVisibility(View.VISIBLE);
 			break;			
 
 		// Inscrire l'utilisateur
 		case 21:
 			Intent j = new Intent(MainActivity.this, SignupActivity.class);
 			startActivityForResult(j, 3);
 			break;
 			
 		// Connecter l'utilisateur
 		case 22:
 			Intent k = new Intent(MainActivity.this, LoginActivity.class);
 			startActivityForResult(k, 1);
 			break;
 			
 		// Afficher les dernieres infos de l'utilisateur connect
 		case 11:
 			getPopup().popupDisplayUserInfos();
 			break;
 			
 		// Dconnecter l'utilisateur actif
 		case 12:
 			getSession().logout();
 			break;
 		
 			// Ajouter menu langue	
 		case R.id.menuPersan:
 			// Comportement du bouton "langue PERSAN "		
 			parsi();
 			finish();
 			startActivity(getIntent());
 			return true;
 		
 		case R.id.menuEnglais:
 			// Comportement du bouton "langue PERSAN "		
 			english();
 			finish();
 			startActivity(getIntent());
 			return true;
 			
 		case R.id.menuFrance:
 			// Comportement du bouton "langue PERSAN "		
 			french();
 			finish();
 			startActivity(getIntent());
 			return true;
 		default:
 			break;
 		}
 		return true;
 	}
 	
 	public void onActivityResult(int requestCode, int resultCode, Intent data) 
 	{
 		super.onActivityResult(requestCode, resultCode, data);
 
 		// Le code de requte est utilis pour identifier lactivit enfant
 		switch (requestCode) 
 		{
 		// Cas si authentification
 		case 1:
 			switch (resultCode) 
 			{
 			case RESULT_OK:
 				Toast.makeText(this, R.string.auth_ok, Toast.LENGTH_LONG).show();
 				finish();
 				startActivity(getIntent());
 				return;
 				
 			case RESULT_CANCELED:
 				Toast.makeText(this, R.string.auth_fail, Toast.LENGTH_LONG).show();
 				return;
 				
 			case ROUTE_REQUEST : 
 				if (resultCode == RESULT_OK) 
 				{
 					int nodeId = data.getIntExtra("NODE_ID", 0);
 					map.getController().setCenter(mRoad.mNodes.get(nodeId).mLocation);
 					roadNodeMarkers.showBubbleOnItem(nodeId, map);
 				}
 				break;
 				
 			case POIS_REQUEST:
 				if (resultCode == RESULT_OK) 
 				{
 					int id = data.getIntExtra("ID", 0);
 					map.getController().setCenter(mPOIs.get(id).mLocation);
 					poiMarkers.showBubbleOnItem(id, map);
 				}
 				break;
 				
 			default:
 				// Faire quelque chose
 				return;
 			}
 
 		// Cas si paramtres utilisateur
 		case 2:
 			switch (resultCode) 
 			{
 			case RESULT_OK:
 				// On relance l'activit car on doit rafraichir la map !
 				finish();
 				startActivity(getIntent());
 				return;
 				
 			case RESULT_CANCELED:
 				Toast.makeText(this, R.string.prefs_update_error, Toast.LENGTH_LONG).show();
 				return;
 				
 			default:
 				// Faire quelque chose
 				return;
 			}
 
 		// Cas si inscription
 		case 3:
 			switch (resultCode) 
 			{
 			case RESULT_OK:
 				Toast.makeText(this, R.string.signup_ok, Toast.LENGTH_LONG).show();
 				return;
 			case RESULT_CANCELED:
 				Toast.makeText(this, R.string.signup_fail, Toast.LENGTH_LONG).show();
 				return;
 			default:
 				// Faire quelque chose
 				return;
 			}
 
 			// Aucune activit en retour...
 		default:
 			// Faire quelque chose
 			return;
 		}
 	}
 	
 	public void onConfigurationChanged(Configuration newConfig) 
 	{
 		super.onConfigurationChanged(newConfig);
 	}
 	
 	@Override
 	protected void onResume() 
     {
 		super.onResume();
 	}
     
 	@Override
 	protected void onPause() 
 	{
 		// On arrte le GPS
 		try 
 		{
 			locationManager.removeUpdates(this);
 			locationManager = null;
 		} 
 		catch (Exception e) {
 			// Rien de grave si on tombe ici
 		}
 		super.onPause();
 	}
 	
 	@Override
 	protected void onStop() 
 	{
 		super.onStop();
 	}
 	
 	@Override
 	protected void onDestroy() 
 	{
 		super.onDestroy();
 
 		finish();
 	}
 	
 	/* ----------------------------------------
 	 * Fonctions et procdures de l'application
 	 * ----------------------------------------
 	 */
     
     /**
      * Geocoding of the destination address
      */
 	public void handleSearchLocationButton()
 	{
 		EditText destinationEdit = (EditText)findViewById(R.id.editDestination);
 		//Hide the soft keyboard:
 		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
 		imm.hideSoftInputFromWindow(destinationEdit.getWindowToken(), 0);
 		
 		String destinationAddress = destinationEdit.getText().toString();
 		GeocoderNominatim geocoder = new GeocoderNominatim(this);
 		try {
 			List<Address> foundAdresses = geocoder.getFromLocationName(destinationAddress, 1);
 			if (foundAdresses.size() == 0) { //if no address found, display an error
 				Toast.makeText(this, R.string.toast_address, Toast.LENGTH_SHORT).show();
 			} else {
 				Address address = foundAdresses.get(0); //get first address
 				destinationPoint = new GeoPoint(address.getLatitude(), address.getLongitude());
 				markerDestination = putMarkerItem(markerDestination, destinationPoint, DEST_INDEX,
 			    		R.string.destination, R.drawable.marker_destination, -1);
 				getRoadAsync();
 				map.getController().setCenter(destinationPoint);
 			}
 		} catch (Exception e) {
 			Toast.makeText(this, R.string.toast_geocode, Toast.LENGTH_SHORT).show();
 		}
 	}
 	
 	//Async task to reverse-geocode the marker position in a separate thread:
 	private class GeocodingTask extends AsyncTask<Object, Void, String> 
 	{
 		ExtendedOverlayItem marker;
 		protected String doInBackground(Object... params)
 		{
 			marker = (ExtendedOverlayItem)params[0];
 			return getTools().getAddress(marker.getPoint());
 		}
 		protected void onPostExecute(String result)
 		{
 			marker.setDescription(result);
 			//itineraryMarkers.showBubbleOnItem(???, map); //open bubble on the item
 		}
 	}
 	
 	/** add (or replace) an item in markerOverlays. p position. */
 	public ExtendedOverlayItem putMarkerItem(ExtendedOverlayItem item, GeoPoint p, int index, int titleResId, int markerResId, int iconResId)
 	{
 		if (item != null)
 		{
 			itineraryMarkers.removeItem(item);
 		}
 		Drawable marker = getResources().getDrawable(markerResId);
 		String title = getResources().getString(titleResId);
 		ExtendedOverlayItem overlayItem = new ExtendedOverlayItem(title, "", p, this);
 		overlayItem.setMarkerHotspot(OverlayItem.HotspotPlace.BOTTOM_CENTER);
 		overlayItem.setMarker(marker);
 		if (iconResId != -1)
 		{
 			overlayItem.setImage(getResources().getDrawable(iconResId));
 		}
 		overlayItem.setRelatedObject(index);
 		itineraryMarkers.addItem(overlayItem);
 		map.invalidate();
 		//Start geocoding task to update the description of the marker with its address:
 		new GeocodingTask().execute(overlayItem);
 		return overlayItem;
 	}
 	
 	public void addViaPoint(GeoPoint p)
 	{
 		viaPoints.add(p);
 		putMarkerItem(null, p, viaPoints.size()-1,
 			R.string.via_point, R.drawable.marker_via, -1);
 	}
 	
 	public void removePoint(int index)
 	{
 		if (index == START_INDEX)
 			startPoint = null;
 		else if (index == DEST_INDEX)
 			destinationPoint = null;
 		else 
 			viaPoints.remove(index);
 		getRoadAsync();
 		updateUIWithItineraryMarkers();
 	}
 	
 	public void updateUIWithItineraryMarkers()
 	{
 		itineraryMarkers.removeAllItems();
 		
 		//Start marker:
 		if (startPoint != null){
 			markerStart = putMarkerItem(null, startPoint, START_INDEX, 
 				R.string.departure, R.drawable.marker_departure, -1);
 		}
 		
 		//Via-points markers if any:
 		for (int index=0; index<viaPoints.size(); index++){
 			putMarkerItem(null, viaPoints.get(index), index, 
 				R.string.via_point, R.drawable.marker_via, -1);
 		}
 		
 		//Destination marker if any:
 		if (destinationPoint != null){
 			markerDestination = putMarkerItem(null, destinationPoint, DEST_INDEX,
 				R.string.destination, R.drawable.marker_destination, -1);
 		}
 	}
 	
 	//------------ Routes et Itinraires
     
 	private void putRoadNodes(Road road)
 	{
 		roadNodeMarkers.removeAllItems();
 		Drawable marker = getResources().getDrawable(R.drawable.marker_node);
 		int n = road.mNodes.size();
 		TypedArray iconIds = getResources().obtainTypedArray(R.array.direction_icons);
 		for (int i = 0; i < n; i++)
 		{
 			RoadNode node = road.mNodes.get(i);
 			String instructions = (node.mInstructions == null ? "" : node.mInstructions);
 			ExtendedOverlayItem nodeMarker = new ExtendedOverlayItem("Step " + (i + 1), instructions, node.mLocation, this);
 			nodeMarker.setSubDescription(road.getLengthDurationText(node.mLength, node.mDuration));
 			nodeMarker.setMarkerHotspot(OverlayItem.HotspotPlace.CENTER);
 			nodeMarker.setMarker(marker);
 			int iconId = iconIds.getResourceId(node.mManeuverType, R.drawable.ic_empty);
 			if (iconId != R.drawable.ic_empty)
 			{
 				Drawable icon = getResources().getDrawable(iconId);
 				nodeMarker.setImage(icon);
 			}
 			roadNodeMarkers.addItem(nodeMarker);
 		}
 	}
 	
 	void updateUIWithRoad(Road road)
 	{
 		roadNodeMarkers.removeAllItems();
 		List<Overlay> mapOverlays = map.getOverlays();
 		if (roadOverlay != null){
 			mapOverlays.remove(roadOverlay);
 		}
 		if (road == null)
 			return;
 		if (road.mStatus == Road.STATUS_DEFAULT)
 			Toast.makeText(map.getContext(), R.string.toast_route, Toast.LENGTH_SHORT).show();
 		roadOverlay = RoadManager.buildRoadOverlay(road, map.getContext());
 		Overlay removedOverlay = mapOverlays.set(1, roadOverlay);
 			//we set the road overlay at the "bottom", just above the MapEventsOverlay,
 			//to avoid covering the other overlays. 
 		mapOverlays.add(removedOverlay);
 		putRoadNodes(road);
 		map.invalidate();
 		//Set route info in the text view:
 		//((TextView)findViewById(R.id.routeInfo)).setText(road.getLengthDurationText(-1));
 		getTools().infoBar(this, road.getLengthDurationText(-1), true);
     }
 	
 	/**
 	 * Tche asynchrone afin d'obtenir trajet avec des processus spars
 	 */
 	private class UpdateRoadTask extends AsyncTask<Object, Void, Road> 
 	{
 		protected Road doInBackground(Object... params) 
 		{
 			@SuppressWarnings("unchecked")
 			ArrayList<GeoPoint> waypoints = (ArrayList<GeoPoint>)params[0];
 			//RoadManager roadManager = new GoogleRoadManager();
 			RoadManager roadManager = new OSRMRoadManager();
 			/*
 			RoadManager roadManager = new MapQuestRoadManager();
 			Locale locale = Locale.getDefault();
 			roadManager.addRequestOption("locale="+locale.getLanguage()+"_"+locale.getCountry());
 			*/
 			return roadManager.getRoad(waypoints);
 		}
 		
 		protected void onPostExecute(Road result) 
 		{
 			mRoad = result;
 			updateUIWithRoad(result);
 			getPOIAsync(poiTagText.getText().toString());
 		}
 	}
 	
 	public void getRoadAsync()
 	{
 		mRoad = null;
 		if (startPoint == null || destinationPoint == null){
 			updateUIWithRoad(mRoad);
 			return;
 		}
 		ArrayList<GeoPoint> waypoints = new ArrayList<GeoPoint>(2);
 		waypoints.add(startPoint);
 		//add intermediate via points:
 		for (GeoPoint p:viaPoints){
 			waypoints.add(p); 
 		}
 		waypoints.add(destinationPoint);
 		new UpdateRoadTask().execute(waypoints);
 	}
 	
 	//----------------- POIs
 	
 	void updateUIWithPOI(ArrayList<POI> pois)
 	{
 		if (pois != null){
 			for (POI poi:pois){
 				ExtendedOverlayItem poiMarker = new ExtendedOverlayItem(
 					poi.mType, poi.mDescription, 
 					poi.mLocation, this);
 				Drawable marker = null;
 				if (poi.mServiceId == POI.POI_SERVICE_NOMINATIM){
 					marker = getResources().getDrawable(R.drawable.marker_poi_default);
 				} else if (poi.mServiceId == POI.POI_SERVICE_GEONAMES_WIKIPEDIA){
 					if (poi.mRank < 90)
 						marker = getResources().getDrawable(R.drawable.marker_poi_wikipedia_16);
 					else
 						marker = getResources().getDrawable(R.drawable.marker_poi_wikipedia_32);
 				} else if (poi.mServiceId == POI.POI_SERVICE_FLICKR){
 					marker = getResources().getDrawable(R.drawable.marker_poi_flickr);
 				} else if (poi.mServiceId == POI.POI_SERVICE_PICASA){
 					marker = getResources().getDrawable(R.drawable.marker_poi_picasa_24);
 					poiMarker.setSubDescription(poi.mCategory);
 				}
 				poiMarker.setMarker(marker);
 				poiMarker.setMarkerHotspot(OverlayItem.HotspotPlace.CENTER);
 				//thumbnail loading moved in POIInfoWindow.onOpen for better performances. 
 				poiMarker.setRelatedObject(poi);
 				poiMarkers.addItem(poiMarker);
 			}
 		}
 		map.invalidate();
 	}
 	
 	/** Loads all thumbnails in background */
 	ExecutorService mThreadPool = Executors.newFixedThreadPool(5);
 	void startAsyncThumbnailsLoading(ArrayList<POI> pois)
 	{
 		for (int i=0; i<pois.size(); i++){
 			final int index = i;
 			final POI poi = pois.get(index);
 			mThreadPool.submit(new Runnable(){
 				@Override public void run(){
 					Bitmap b = poi.getThumbnail();
 					if (b != null){
 						/*
 						//Change POI marker:
 						ExtendedOverlayItem item = poiMarkers.getItem(index);
 						b = Bitmap.createScaledBitmap(b, 48, 48, true);
 						BitmapDrawable bd = new BitmapDrawable(getResources(), b);
 						item.setMarker(bd);
 						*/
 					}
 				}
 			});
 		}
 	}
 	
 	private class POITask extends AsyncTask<Object, Void, ArrayList<POI>> 
 	{
 		String mTag;
 		protected ArrayList<POI> doInBackground(Object... params) {
 			mTag = (String)params[0];
 			
 			if (mTag == null || mTag.equals("")){
 				return null;
 			} else if (mTag.equals("wikipedia")){
 				GeoNamesPOIProvider poiProvider = new GeoNamesPOIProvider("mkergall");
 				//ArrayList<POI> pois = poiProvider.getPOICloseTo(point, 30, 20.0);
 				//Get POI inside the bounding box of the current map view:
 				BoundingBoxE6 bb = map.getBoundingBox();
 				ArrayList<POI> pois = poiProvider.getPOIInside(bb, 30);
 				return pois;
 			} else if (mTag.equals("flickr")){
 				FlickrPOIProvider poiProvider = new FlickrPOIProvider("c39be46304a6c6efda8bc066c185cd7e");
 				BoundingBoxE6 bb = map.getBoundingBox();
 				ArrayList<POI> pois = poiProvider.getPOIInside(bb, 30);
 				return pois;
 			} else if (mTag.startsWith("picasa")){
 				PicasaPOIProvider poiProvider = new PicasaPOIProvider(null);
 				BoundingBoxE6 bb = map.getBoundingBox();
 				String q = mTag.substring("picasa".length());
 				ArrayList<POI> pois = poiProvider.getPOIInside(bb, 30);
 				return pois;
 			} else {
 				NominatimPOIProvider poiProvider = new NominatimPOIProvider();
 				poiProvider.setService(NominatimPOIProvider.MAPQUEST_POI_SERVICE);
 				ArrayList<POI> pois;
 				if (destinationPoint == null){
 					BoundingBoxE6 bb = map.getBoundingBox();
 					pois = poiProvider.getPOIInside(bb, mTag, 100);
 				} else {
 					pois = poiProvider.getPOIAlong(mRoad.getRouteLow(), mTag, 100, 2.0);
 				}
 				return pois;
 			}
 		}
 		protected void onPostExecute(ArrayList<POI> pois) 
 		{
 			mPOIs = pois;
 			if (mTag.equals("")){
 				//no search, no message
 			} else if (mPOIs == null){
 				Toast.makeText(getApplicationContext(), "Technical issue when getting "+mTag+ " POI.", Toast.LENGTH_LONG).show();
 			} else {
 				Toast.makeText(getApplicationContext(), ""+mPOIs.size()+" "+mTag+ " entries found", Toast.LENGTH_LONG).show();
 				if (mTag.equals("flickr")||mTag.startsWith("picasa")||mTag.equals("wikipedia"))
 					startAsyncThumbnailsLoading(mPOIs);
 			}
 			updateUIWithPOI(mPOIs);
 		}
 	}
 	
 	void getPOIAsync(String tag){
 		poiMarkers.removeAllItems();
 		new POITask().execute(tag);
 	}
 	
 	//------------ MapEventsReceiver implementation
 
 	GeoPoint tempClickedGeoPoint; //any other way to pass the position to the menu ???
 	
 	public boolean longPressHelper(IGeoPoint p) 
 	{
 		tempClickedGeoPoint = new GeoPoint((GeoPoint)p);
 		Button searchButton = (Button)findViewById(R.id.buttonSearch);
 		openContextMenu(searchButton); //menu is hooked on the "Search" button
 		return true;
 	}
 		
 	public boolean singleTapUpHelper(IGeoPoint p) 
 	{
 		viewMapFilters.setVisibility(View.GONE);
 		return false;
 	}
 
 	//----------- Menu contextuel lors d'un appuis prolong sur la carte
 	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) 
 	{
 		super.onCreateContextMenu(menu, v, menuInfo);
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.map_menu, menu);
 	}
 		
 	public boolean onContextItemSelected(android.view.MenuItem item) 
 	{
 		if(item.getItemId() == R.id.menu_departure)
 		{
 			startPoint = new GeoPoint((GeoPoint)tempClickedGeoPoint);
 			markerStart = putMarkerItem(markerStart, startPoint, START_INDEX,
 				R.string.departure, R.drawable.marker_departure, -1);
 			getRoadAsync();
 			return true;
 		}
 		else if(item.getItemId() == R.id.menu_destination)
 		{
 			destinationPoint = new GeoPoint((GeoPoint)tempClickedGeoPoint);
 			markerDestination = putMarkerItem(markerDestination, destinationPoint, DEST_INDEX,
 				R.string.destination, R.drawable.marker_destination, -1);
 			getRoadAsync();
 			return true;
 		}
 		else if(item.getItemId() == R.id.menu_viapoint)
 		{
 			GeoPoint viaPoint = new GeoPoint((GeoPoint)tempClickedGeoPoint);
 			addViaPoint(viaPoint);
 			getRoadAsync();
 			return true;
 		}
 		else
 		{
 			return false;
 		}
 	}
 	
 	/**
 	 * Initialiser la logique de l'application
 	 */
 	private void init()
 	{
 		try
 		{
 			// On recupre les prfrences utilisateurs paramtrer dans l'activit des Paramtres
 			//SharedPreferences userPrefs = PreferenceManager.getDefaultSharedPreferences(this);
 			//displayUserInfos = userPrefs.getBoolean("checkBoxDisplayUserInfos", false);
 			
 			// Instance de SharedPreferences pour lire les donnes dans un fichier
 			SharedPreferences myPrefs = this.getSharedPreferences("UserPrefs", MODE_WORLD_READABLE); 
 			login = myPrefs.getString("Login", null);
 			
 			// Afficher la boite de dialogue au dmarrage ?
 			// Si oui, celle anonyme ou utilisateur connect ?
 			if (login == null)
 			{
 				getPopup().popupGuest();
 				getTools().infoBar(this, this.getString(R.string.msg_accueil), true);
 			}
 			else
 			{
				getTools().infoBar(this, this.getString(R.string.msg_salut) + " " + login, true);
 				
 				// Appeler la fonction pour parser les amis et les affichs sur la carte
 				displayFriends();
 			}
 		} catch (Exception e)
 		{
 			Log.e("Catch", "> init() : " + e.getMessage());
 		}
 	}
 	
     /**
      * Fonction pour afficher sur la carte les amis en forme de marqueurs avec a l'interieur son status, ...
      */
 	public void displayFriends()
     {
         try
         {
         	// Rcuprer la liste des amis via appel serveur REST
 			getSession().getJSON("https://jibiki.univ-savoie.fr/ltpdev/rest.php/api/1/friends", "FRIENDS"); 
 		}
         catch (Exception e) 
         {
 			Log.e("Catch", "> parseFriends() - Exception: " + e.getMessage());
 		}
     }
     
 	/**
 	 * On appel cette mthode quand on appuie sur un marquer pour ensuite afficher un toast
 	 */
     OnItemGestureListener<OverlayItem> myOnItemGestureListener = new OnItemGestureListener<OverlayItem>() 
 	{
         public boolean onItemSingleTapUp(int index, OverlayItem item)
         {
             Toast.makeText(MainActivity.this,
                 item.mDescription + "\n" + item.mTitle + "\n"
                     + item.mGeoPoint.getLatitudeE6() + " : "
                     + item.mGeoPoint.getLongitudeE6(),
                 Toast.LENGTH_LONG).show();
                  
             return true;
         }
 
 		@Override
 		public boolean onItemLongPress(int arg0, OverlayItem arg1) {
 			return false;
 		}
     };
     
 	@Override
 	public void onLocationChanged(Location location)
 	{
 		// On recupre les paramtres de l'utilisateur
 		SharedPreferences userPrefs = PreferenceManager.getDefaultSharedPreferences(this);
 		boolean useTracker = userPrefs.getBoolean("checkBoxTracker", false);
 		
 		// Mettre a jour la position de l'utilisateur sur la carte
 		myLocationOverlay.setLocation(new GeoPoint(location));
 		map.invalidate();
 		
 		setLongitude(location.getLongitude());
 		setLatitude(location.getLatitude());
 		
 		// Si le trackeur est activ, on met a jours les coordonnes gps sur le serveur
 		if(useTracker)
 		{
 			String json = "{\"ltp\":{\"application\":\"Client LTP\",\"track\":{\"lon\" : \"" + String.valueOf(getLongitude()) + "\",\"lat\" : \"" + String.valueOf(getLatitude()) + "\"}}}";
 			session.putJSON("https://jibiki.univ-savoie.fr/ltpdev/rest.php/api/1/tracker", "TRACKER", json);
 		}
 		
 		// Mettre a jours les status
 		displayFriends();
 	}
 
 	@Override
 	public void onProviderDisabled(String provider) {
 		
 	}
 
 	@Override
 	public void onProviderEnabled(String provider) {
 		
 	}
 
 	@Override
 	public void onStatusChanged(String provider, int status, Bundle extras) {
 		
 	}
 	
 	/**
 	 * Afficher les resultats des requetes JSON
 	 * @param pJSONArray
 	 */
 	public void parseJSONResult(JSONArray pJSONArray)
     {
         anotherOverlayItemArray = new ArrayList<OverlayItem>();
         
         try 
         {	
 			// Vider la liste actuel
 			listFriends.clear();
 
 			// Parser la liste des amis dans le OverlayItem ArrayList
 	        for (int i = 0 ; (i < pJSONArray.length()) ; i++ )
 	        {
 	        	// Obtenir l'amis
 	        	JSONObject pote = pJSONArray.getJSONObject(i);
 	        	
 	        	double lat = 0.00;
 	        	double lon = 0.00;
 	        	String status = "";
 	        	
 	        	if(!pote.isNull("lon") || !pote.isNull("lat"))
 	        	{
 	        		lon =  pote.getDouble("lon");
 	        		lat = pote.getDouble("lat");
 	        	}
 	        	
 	        	if (!pote.isNull("content"))
 	        	{
 	        		status = pote.getString("content");
 	        	}
 	        	
 		        // Prparer l'array des icnes amis sur la carte
 		        anotherOverlayItemArray.add(new OverlayItem(pote.getString("username"), status, new GeoPoint(lat, lon)));
 		        
 		        // Ajouter l'ami dans la friends ListView
 		        listFriends.add(new Friends(pote.getString("username"), status, lon, lat));
 	        }
 	        
 	        // Load & setfriends & notify ListView adapter
 			FriendsAdapter adapter = new FriendsAdapter(this, listFriends);       
 			lvListeFriends.setAdapter(adapter);
 	        adapter.notifyDataSetChanged();
 	        
             // Crer un nouveau ItemizedOverlayWithFocus avec notre array d'amis
 	        // Ensuite, on redessine la carte pour actualiser les marqueurs
             ItemizedOverlayWithFocus<OverlayItem> anotherItemizedIconOverlay = new ItemizedOverlayWithFocus<OverlayItem>(this, anotherOverlayItemArray, myOnItemGestureListener);
             
             map.getOverlays().clear();
             
             map.getOverlays().add(anotherItemizedIconOverlay);
             
             map.getOverlays().add(myLocationOverlay);
             map.getOverlays().add(itineraryMarkers);
             map.getOverlays().add(roadNodeMarkers);
             map.getOverlays().add(poiMarkers);
             map.getOverlays().add(overlay);
             
             startPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
             
             map.refreshDrawableState();
 
             //map.postInvalidate();
 			runOnUiThread(new Runnable()
 			{
 				public void run() {
 					map.postInvalidate();
 				}
 			});
 			
             // Paramtres pour l'overlay des icnes
             anotherItemizedIconOverlay.setFocusItemsOnTap(true);
             anotherItemizedIconOverlay.setFocusedItem(0);      
 		}
         catch (JSONException e) 
         {
 			Log.e("Catch", "> parseFriends() - JSONException: " + e.getMessage());
 		} 
         catch (Exception e) 
         {
 			Log.e("Catch", "> parseFriends() - Exception: " + e.getMessage());
 		}
     }
 	
 	/*
 	 * Mthodes pour les langues de l'application
 	 */
 	
 	public void parsi()
 	{
 		String languageToLoad  = "fa";
 	    Locale locale = new Locale(languageToLoad); 
 	    Locale.setDefault(locale);
 	    Configuration config = new Configuration();
 	    config.locale = locale;	    
 	    getBaseContext().getResources().updateConfiguration(config,getBaseContext().getResources().getDisplayMetrics());
 	    Resources standardResources = getBaseContext().getResources();
 	}
 	
 	private void english() 
 	{
 		String languageToLoad  = "en";
 	    Locale locale = new Locale(languageToLoad); 
 	    Locale.setDefault(locale);
 	    Configuration config = new Configuration();
 	    config.locale = locale;	    
 	    getBaseContext().getResources().updateConfiguration(config,getBaseContext().getResources().getDisplayMetrics());
 	    Resources standardResources = getBaseContext().getResources();
 	}
 	
 	private void french() 
 	{
 		// TODO Auto-generated method stub
 		String languageToLoad  = "fr";
 	    Locale locale = new Locale(languageToLoad); 
 	    Locale.setDefault(locale);
 	    Configuration config = new Configuration();
 	    config.locale = locale;	    
 	    getBaseContext().getResources().updateConfiguration(config,getBaseContext().getResources().getDisplayMetrics());
 	    Resources standardResources = getBaseContext().getResources();
 	}
 	
 	
 	/*
 	 * Getteurs & Setteurs
 	 */
 	
 	public Popup getPopup() 
 	{
 		return popup;
 	}
 
 	public void setPopup(Popup popup) 
 	{
 		this.popup = popup;
 	}
 
 	public Session getSession() {
 		return session;
 	}
 
 	public void setSession(Session session) {
 		this.session = session;
 	}
 
 	public double getLongitude() {
 		return longitude;
 	}
 
 	public void setLongitude(double longitude) {
 		this.longitude = longitude;
 	}
 
 	public double getLatitude() {
 		return latitude;
 	}
 
 	public void setLatitude(double latitude) {
 		this.latitude = latitude;
 	}
 
 	public Tools getTools() {
 		return tools;
 	}
 
 	public void setTools(Tools tools) {
 		this.tools = tools;
 	}
 }
