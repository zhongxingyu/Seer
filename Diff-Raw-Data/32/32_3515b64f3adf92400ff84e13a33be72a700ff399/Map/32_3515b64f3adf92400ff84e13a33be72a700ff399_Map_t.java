 /* gvSIG Mini. A free mobile phone viewer of free maps.
  *
  * Copyright (C) 2009 Prodevelop.
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,USA.
  *
  * For more information, contact:
  *
  *   Prodevelop, S.L.
  *   Pza. Don Juan de Villarrasa, 14 - 5
  *   46001 Valencia
  *   Spain
  *
  *   +34 963 510 612
  *   +34 963 510 968
  *   prode@prodevelop.es
  *   http://www.prodevelop.es
  *
  *   gvSIG Mini has been partially funded by IMPIVA (Instituto de la Peque�a y
  *   Mediana Empresa de la Comunidad Valenciana) &
  *   European Union FEDER funds.
  *   
  *   2009.
  *   author Alberto Romeu aromeu@prodevelop.es 
  *   author Ruben Blanco rblanco@prodevelop.es 
  *
  *
  * Original version of the code made by Nicolas Gramlich.
  * No header license code found on the original source file.
  * 
  * Original source code downloaded from http://code.google.com/p/osmdroid/
  * package org.andnav.osm.samples;
  * 
  * License stated in that website is 
  * http://www.gnu.org/licenses/lgpl.html
  * As no specific LGPL version was specified, LGPL license version is LGPL v2.1
  * is assumed.
  *  
  * 
  */
 
 package es.prodevelop.gvsig.mini.activities;
 
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import net.sf.microlog.core.Logger;
 import net.sf.microlog.core.LoggerFactory;
 
 import org.anddev.android.weatherforecast.weather.WeatherCurrentCondition;
 import org.anddev.android.weatherforecast.weather.WeatherForecastCondition;
 import org.anddev.android.weatherforecast.weather.WeatherSet;
 
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.DialogInterface.OnCancelListener;
 import android.content.pm.ActivityInfo;
 import android.content.res.Configuration;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.location.Location;
 import android.location.LocationProvider;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.os.PowerManager;
 import android.text.Editable;
 import android.util.DisplayMetrics;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.RadioButton;
 import android.widget.RadioGroup;
 import android.widget.RelativeLayout;
 import android.widget.SeekBar;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.ZoomControls;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.RadioGroup.OnCheckedChangeListener;
 import android.widget.RelativeLayout.LayoutParams;
 import android.widget.SeekBar.OnSeekBarChangeListener;
 import es.prodevelop.geodetic.utils.conversion.ConversionCoords;
 import es.prodevelop.gvsig.mini.R;
 import es.prodevelop.gvsig.mini.activities.NameFinderActivity.BulletedText;
 import es.prodevelop.gvsig.mini.activities.NameFinderActivity.BulletedTextListAdapter;
 import es.prodevelop.gvsig.mini.context.ItemContext;
 import es.prodevelop.gvsig.mini.context.map.DefaultContext;
 import es.prodevelop.gvsig.mini.context.map.POIContext;
 import es.prodevelop.gvsig.mini.context.map.RouteContext;
 import es.prodevelop.gvsig.mini.context.map.RoutePOIContext;
 import es.prodevelop.gvsig.mini.geom.Feature;
 import es.prodevelop.gvsig.mini.geom.FeatureCollection;
 import es.prodevelop.gvsig.mini.geom.LineString;
 import es.prodevelop.gvsig.mini.geom.Point;
 import es.prodevelop.gvsig.mini.map.Downloader;
 import es.prodevelop.gvsig.mini.map.GPSPoint;
 import es.prodevelop.gvsig.mini.map.GeoUtils;
 import es.prodevelop.gvsig.mini.map.Layers;
 import es.prodevelop.gvsig.mini.map.MapLocation;
 import es.prodevelop.gvsig.mini.map.MapState;
 import es.prodevelop.gvsig.mini.map.ViewPort;
 import es.prodevelop.gvsig.mini.map.renderer.MapRenderer;
 import es.prodevelop.gvsig.mini.map.renderer.OSMMercatorRenderer;
 import es.prodevelop.gvsig.mini.map.renderer.OSMRenderer;
 import es.prodevelop.gvsig.mini.map.renderer.TMSRenderer;
 import es.prodevelop.gvsig.mini.namefinder.NameFinder;
 import es.prodevelop.gvsig.mini.namefinder.Named;
 import es.prodevelop.gvsig.mini.namefinder.NamedMultiPoint;
 import es.prodevelop.gvsig.mini.phonecache.DownloadWaiter;
 import es.prodevelop.gvsig.mini.tasks.Functionality;
 import es.prodevelop.gvsig.mini.tasks.TaskHandler;
 import es.prodevelop.gvsig.mini.tasks.WorkQueue;
 import es.prodevelop.gvsig.mini.tasks.map.GetCellLocationFunc;
 import es.prodevelop.gvsig.mini.tasks.namefinder.NameFinderFunc;
 import es.prodevelop.gvsig.mini.tasks.twitter.TweetMyLocationFunc;
 import es.prodevelop.gvsig.mini.tasks.weather.WeatherFunctionality;
 import es.prodevelop.gvsig.mini.tasks.yours.YOURSFunctionality;
 import es.prodevelop.gvsig.mini.user.UserContext;
 import es.prodevelop.gvsig.mini.user.UserContextManager;
 import es.prodevelop.gvsig.mini.util.ResourceLoader;
 import es.prodevelop.gvsig.mini.util.Utils;
 import es.prodevelop.gvsig.mini.utiles.Tags;
 import es.prodevelop.gvsig.mini.views.overlay.CircularRouleteView;
 import es.prodevelop.gvsig.mini.views.overlay.NameFinderOverlay;
 import es.prodevelop.gvsig.mini.views.overlay.RouteOverlay;
 import es.prodevelop.gvsig.mini.views.overlay.TileRaster;
 import es.prodevelop.gvsig.mini.views.overlay.ViewSimpleLocationOverlay;
 import es.prodevelop.gvsig.mini.yours.Route;
 import es.prodevelop.gvsig.mobile.fmap.proj.CRSFactory;
 import es.prodevelop.gvsig.mini.location.MockLocationProvider;
 import es.prodevelop.gvsig.mini.location.Config;
 
 /**
  * The main activity of the application. Consists on a RelativeLayout with some
  * zoom controls and the TileRaster that manages the MapView
  * 
  * This class manages the Messages sent by Functionalities with the MapHandler
  * 
  * Also has instances of Route and NamedMultiPoint
  * 
  * Needs to be refactored
  * 
  * @author aromeu
  * @author rblanco
  * 
  */
 public class Map extends MapLocation implements GeoUtils, DownloadWaiter {
 	SeekBar trans;
 
 	public static String twituser = null;
 	public static String twitpass = null;
 	private ViewSimpleLocationOverlay mMyLocationOverlay;
 	public TileRaster osmap;
 	public final Route route = new Route();
 	public NamedMultiPoint nameds;
 	private Point nearestPOI;
 	private int indexNP;
 	String datalog = null;
 	public float datatransfer2 = 0;
 	public RelativeLayout rl;
 	public Downloader down;
 	ImageView ivSwitch;
 	ImageView ivCompassY;
 	ImageView ivCompass;
 	public ProgressDialog dialog2 = null;
 	private boolean recenterOnGPS = false;
 	private boolean backpressed = false;
 	private AlertDialog alertP;
 	SensorEventListener mTop = null;
 	// private SensorManager mSensorManager;
 	private MenuItem myNavigator;
 	private MenuItem myLocationButton;
 	private MenuItem myZoomRectangle;
 	private MenuItem mySearchDirection;
 	private MenuItem myDownloadLayers;
 	private MenuItem myGPSButton;
 	private MenuItem myAbout;
 	private MenuItem myWhats;
 	private MenuItem myLicense;
 	public Handler mHandler;
 	public static ViewPort vp;
 	int nearopt = 0;
 	public static final int ROUTE_CANCELED = 100;
 	public static final int ROUTE_INITED = 101;
 	public static final int WEATHER_INITED = 102;
 	public static final int TWEET_SENT = 103;
 	public static final int SHOW_TWEET_DIALOG = 104;
 	public static final int POI_INITED = 105;
 	public static final int SHOW_POI_DIALOG = 106;
 	public static final int SHOW_ADDRESS_DIALOG = 107;
 	public static final int ROUTE_CLEARED = 108;
 	public static final int POI_LIST = 109;
 	public static final int VOID = 110;
 	public static final int POI_CLEARED = 111;
 	public static final int TWEET_ERROR = 112;
 	public static final int SHOW_TOAST = 113;
 	public static final int POI_CANCELED = 1;
 	public static final int POI_SUCCEEDED = 2;
 	public static final int ROUTE_SUCCEEDED = 3;
 	public static final int ROUTE_NO_RESPONSE = 4;
 	public static final int ROUTE_NO_CALCULATED = 5;
 	public static final int POI_SHOW = 6;
 	public static final int ROUTE_ORIENTATION_CHANGED = 7;
 	public static final int WEATHER_SHOW = 8;
 	public static final int WEATHER_CANCELED = 9;
 	public static final int WEATHER_ERROR = 10;
 	public static final int CALCULATE_ROUTE = 11;
 	TextView reportView;
 	int cacheCounter = 0;
 	MapState mapState;
 	boolean cleanVisible = false;
 	boolean cleanRoute = false;
 	public boolean navigation = false;
 	public boolean connection = true;
 	boolean listVisible = false;
 	boolean cleanCompassVisible = true;
 	public ZoomControls z = null;
 	public CircularRouleteView c;
 	private ItemContext context;
 	/**
 	 * Whether we currently automatically update the animation.
 	 */
 	boolean mUpdatingAnimation;
 	PowerManager.WakeLock wl;
 	private MapHandler handler = new MapHandler();
 	private final static Logger log = LoggerFactory.getLogger(Map.class);
 	private UserContextManager contextManager; // singleton with user contexts
 	// list
 	private UserContext userContext;
 
 	/**
 	 * Called when the activity is first created.
 	 * */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		try {
 			super.onCreate(savedInstanceState);
 			try {
 				log.debug("on create");
 				PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
 				wl = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK,
 						"Prueba de ScreenPower");
 				// Utils.sendExceptionEmail(this, "Esto es una prueba");
 				// SDCardAppender appender = new SDCardAppender();
 				// long milis = System.currentTimeMillis();
 				// appender.setDir(Utils.LOG_DIR);
 				// appender.setFileName("log" + String.valueOf(milis) + ".txt");
 				// appender.setContext(this);
 				// log.addAppender(appender);
 				// log.addAppender(new ConsoleAppender());
 				// log.setLevel(Utils.LOG_LEVEL);
 				// log.error("Testing to log error message with Microlog.");
 				onNewIntent(getIntent());
 				Config.setContext(this.getApplicationContext());
 				ResourceLoader.initialize(this.getApplicationContext());
 
 			} catch (Exception e) {
 				log.error("onCreate", e);
 				// log.error(e.getMessage());
 			} finally {
 				Layers.setContext(this.getApplicationContext());
 				log.setLevel(Utils.LOG_LEVEL);
 				log.setClientID(this.toString());
 			}
 
 			mapState = new MapState(this);
 			this.setContext(new DefaultContext(this));
 
 			// nameFinderTask = new NameFinderTask(this, handler);
 			rl = new RelativeLayout(this);
 			loadSettings(savedInstanceState);
 			loadMap(savedInstanceState);
 
 			// Load UserContexts
 			// First create/get UserContextManager singleton instance
 			contextManager = UserContextManager.getInstance();
 			// Create UserContext and register it in the manager
 			userContext = new UserContext();
 			contextManager.Register(userContext);
 			// add new user contexts here
 			// Load previously persisted user context data
 			contextManager.loadContexts();
 			userContext.incExecutionCounter(); // update execution number
 
 			boolean isSaved = false;
 			try {
 				isSaved = savedInstanceState.getBoolean("isSaved", false);
 			} catch (Exception e) {
 				log.error("isSaved: ", e);
 			}
 
 			if (isSaved) {
 				log.debug("Restoring from previous state");
 				loadRoute(savedInstanceState);
 				loadPois(savedInstanceState);
 				loadUI(savedInstanceState);
 				loadMap(savedInstanceState);
 				loadCenter(savedInstanceState);
 			} else {
 				log.debug("Not restoring from previous state");
 				loadUI(savedInstanceState);
 				boolean succeed = mapState.load();
 				if (!succeed) {
 					log.debug("map state was not persisted. Loading ICC ORTO");
					this.osmap.setZoomLevel(0);
					this.osmap.setMapCenter(400300, 4578900);
 					String[] props1 = new String[] {
 							"3",
 							"http://sagitari.icc.cat/tilecache/tilecache.py/1.0.0/orto/",
 							"jpg",
							"11",
 							"256",
 							"258000.000000",
 							"4485000.000000",
 							"536000.000000",
 							"4752000.000000",
 							"258000.000000",
 							"4485000.000000",
 							"EPSG:23031",
 							"1100.000000000:550.000000000:275.000000000:100.000000000:50.000000000:25.000000000:10.000000000:5.000000000:2.000000000:1.000000000:0.500000000:0.250000000"
 					};
 					String[] props2 = new String[props1.length];
 					for(int i = 0; i < props2.length; i++) {
 						props2[i] = props1[i];
 					}
 					props2[1] = "http://sagitari.icc.cat/tilecache/tilecache.py/1.0.0/topo/";
 					this.osmap.setRenderer(
 							TMSRenderer.loadProperties(props1, "ICC-ORTO"),
 							TMSRenderer.loadProperties(props2, "ICC-TOPO"));
 				}
 			}
 
 			this.setContentView(rl);
 			// if (isSaved) setContentView(null);
 			// enableAcelerometer(savedInstanceState);
 
 			if (!userContext.isUsedCircleMenu()) {
 				// The Circle Context Menu (Roulette) has never been displayed,
 				// so let's
 				// give the user a hint about it
 				Toast t = Toast.makeText(this, R.string.Map_23,
 						Toast.LENGTH_LONG);
 				t.show();
 			}
 
 		} catch (Exception e) {
 			log.error("", e);
 			Utils.showSendLogDialog(this, R.string.fatal_error);
 		} finally {
 			// this.obtainCellLocation();
 		}
 	}
 
 	/**
 	 * Starts the NameFinderActivity after the NameFinderFunc has finished
 	 * 
 	 * @param descr
 	 *            An array with the description of the results of the NameFinder
 	 *            service
 	 * @param nm
 	 *            A NamedMultiPoint with the full results of the service
 	 * @return True if the activity is started, false if the results were not
 	 *         correct
 	 */
 	public boolean showPOIs(String[] descr, NamedMultiPoint nm) {
 		try {
 			if (descr == null) {
 				log.debug("show pois with descr null, returning...");
 				return false;
 			}
 
 			if (nm == null || nm.getNumPoints() <= 0) {
 				log.debug("show pois with nm null, returning...");
 				AlertDialog.Builder alert = new AlertDialog.Builder(this);
 
 				dialog2.dismiss();
 				Toast.makeText(this, R.string.Map_1, Toast.LENGTH_LONG).show();
 				// alert.setIcon(R.drawable.pois);
 				// alert.setTitle("Error");
 				// alert.setMessage("No results were found");
 				//
 				// alert.setNegativeButton("OK",
 				// new DialogInterface.OnClickListener() {
 				// public void onClick(DialogInterface dialog,
 				// int whichButton) {
 				// }
 				// });
 				//
 				// alert.show();
 				return false;
 			}
 
 			Intent intent = new Intent(this, NameFinderActivity.class);
 			String[] res = new String[nm.getPoints().length];
 
 			for (int i = 0; i < nm.getNumPoints(); i++) {
 				res[i] = ((Named) nm.getPoint(i)).description;
 			}
 			intent.putExtra("test", res);
 
 			startActivityForResult(intent, 0);
 
 			this.nameds = nm;
 			dialog2.dismiss();
 			return true;
 
 		} catch (Exception e) {
 			log.error("showPOIS: ", e);
 			return false;
 		} finally {
 			if (dialog2 != null)
 				dialog2.dismiss();
 		}
 	}
 
 	/**
 	 * Calls {@link #showPOIs(String[], NamedMultiPoint)} with the last results
 	 */
 	public void viewLastPOIs() {
 		try {
 			log.debug("view last pois");
 			String[] desc = new String[nameds.getNumPoints()];
 
 			for (int i = 0; i < nameds.getNumPoints(); i++) {
 				desc[i] = ((Named) nameds.getPoint(i)).description;
 			}
 			showPOIs(desc, nameds);
 		} catch (Exception e) {
 			log.error("viewLastPOIS: ", e);
 		}
 	}
 
 	/**
 	 * Starts the LayersActivity with the MapState.gvTilesPath file
 	 */
 	public void viewLayers() {
 		try {
 			log.debug("Launching load layers activity");
 			Intent intent = new Intent(this, LayersActivity.class);
 
 			intent.putExtra("loadLayers", true);
 			intent.putExtra("gvtiles", mapState.gvTilesPath);
 
 			startActivityForResult(intent, 1);
 		} catch (Exception e) {
 			log.error("viewLayers: ", e);
 		}
 	}
 
 	/**
 	 * Checks that the route can be calculated and launches route calculation
 	 */
 	public void calculateRoute() {
 		try {
 			log.debug("calculate route");
 			if (route.canCalculate()) {
 				this.launchRouteCalculation();
 				// User context update
 				this.userContext.setUsedRoutes(true);
 				this.userContext.setLastExecRoutes();
 			}
 		} catch (Exception e) {
 			log.error("calculateRoute: ", e);
 			Toast t = Toast.makeText(this, R.string.Map_2, Toast.LENGTH_LONG);
 			t.show();
 		} finally {
 		}
 	}
 
 	/**
 	 * Launches YOURSFunctionality
 	 */
 	public void launchRouteCalculation() {
 		try {
 			log.debug("launching route calculation");
 			route.iscancelled = false;
 			YOURSFunctionality yoursFunc = new YOURSFunctionality(this, 0);
 			yoursFunc.onClick(null);
 			userContext.setUsedRoutes(true);
 		} catch (Exception e) {
 			log.error("launchRouteCalculation: ", e);
 		}
 	}
 
 	/**
 	 * Manages the results of the NameFinderActivity, LayersActivity
 	 */
 	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
 		try {
 			super.onActivityResult(requestCode, resultCode, intent);
 			log.debug("onActivityResult (code, resultCode): " + requestCode
 					+ ", " + resultCode);
 			if (intent == null) {
 				log.debug("intent was null, returning from onActivityResult");
 				return;
 			}
 
 			switch (requestCode) {
 			case 0:
 				switch (resultCode) {
 				case 0:
 					log.debug("from NameFinderActivity: route from here");
 					int pos = Integer.parseInt(intent.getExtras().get(
 							"selected").toString());
 					Named p = (Named) nameds.getPoint(pos);
 					route.setStartPoint(new Point(p.getX(), p.getY()));
 					calculateRoute();
 					osmap.setMapCenter(p.projectedCoordinates.getX(),
 							p.projectedCoordinates.getY());
 					break;
 				case 1:
 					log.debug("from NameFinderActivity: route to here");
 					int pos1 = Integer.parseInt(intent.getExtras().get(
 							"selected").toString());
 					Named p1 = (Named) nameds.getPoint(pos1);
 					route.setEndPoint(new Point(p1.getX(), p1.getY()));
 					calculateRoute();
 					osmap.setMapCenter(p1.projectedCoordinates.getX(),
 							p1.projectedCoordinates.getY());
 					break;
 				case 2:
 					log.debug("from NameFinderActivity: set map center");
 					int pos2 = Integer.parseInt(intent.getExtras().get(
 							"selected").toString());
 					Named p2 = (Named) this.nameds.getPoint(pos2);
 					osmap.setMapCenterFromLonLat(p2.getX(), p2.getY());
 					if (osmap.getMRendererInfo() instanceof OSMRenderer)
 						osmap.setZoomLevel(p2.zoom);
 					break;
 				}
 
 				break;
 			case 1:
 				switch (resultCode) {
 				case RESULT_OK:
 					log.debug("from LayersActivity");
 					String layerName = intent.getStringExtra("layer");
 					mapState.gvTilesPath = intent.getStringExtra("gvtiles");
 					if (layerName != null) {
 						log.debug("load layer: " + layerName);
 						this.osmap.onLayerChanged(layerName);
 					}
 					break;
 				}
 			}
 		} catch (Exception e) {
 			log.error("Map onActivityResult: ", e);
 		}
 
 	}
 
 	@Override
 	public void onLocationChanged(final Location pLoc) {
 		try {
 			log.debug("onLocationChanged (lon, lat): " + pLoc.getLongitude()
 					+ ", " + pLoc.getLatitude());
 			this.mMyLocationOverlay.setLocation(MapLocation
 					.locationToGeoPoint(pLoc), pLoc.getAccuracy(), pLoc
 					.getProvider());
 			if (recenterOnGPS && pLoc.getLatitude() != 0
 					&& pLoc.getLongitude() != 0 && navigation == true) {
 
 				double[] coords = ConversionCoords.reproject(pLoc
 						.getLongitude(), pLoc.getLatitude(), CRSFactory
 						.getCRS("EPSG:4326"), CRSFactory.getCRS(osmap
 						.getMRendererInfo().getSRS()));
 				osmap.animateTo(coords[0], coords[1]);
 			}
 			// osmap.animateTo(pLoc.getLongitude(), pLoc
 			// .getLatitude());
 			if (myNavigator != null && mMyLocationOverlay.mLocation != null) {
 				connection = true;
 				myNavigator.setEnabled(connection);
 			}
 
 		} catch (Exception e) {
 			log.error("onLocationChanged: ", e);
 		} finally {
 			if (osmap != null)
 				osmap.postInvalidate();
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(final Menu pMenu) {
 		try {
 			mySearchDirection = pMenu.add(0, 0, 0, R.string.Map_3).setIcon(
 					R.drawable.menu00);
 			// pMenu.add(0, 1, 1, "Mock provider options");
 			myLocationButton = pMenu.add(0, 2, 2, R.string.Map_4).setIcon(
 					R.drawable.menu_location);
 			pMenu.add(0, 3, 3, R.string.Map_5).setIcon(R.drawable.menu02);
 			// myZoomRectangle = pMenu.add(0, 4, 4, R.string.Map_6).setIcon(
 			// R.drawable.mv_rectangle);
 			// pMenu.add(0, 4, 4, "Weather").setIcon(R.drawable.menu03);
 			// pMenu.add(0, 5, 5, "Tweetme").setIcon(R.drawable.menu04);
 			myDownloadLayers = pMenu.add(0, 5, 5, R.string.download_tiles_01)
 					.setIcon(R.drawable.menu_download);
 			myNavigator = pMenu.add(0, 6, 6, R.string.Map_Navigator).setIcon(
 					R.drawable.menu_navigation).setEnabled(connection);
 			myGPSButton = pMenu.add(0, 7, 7, R.string.Map_26).setIcon(
 					R.drawable.menu_location).setCheckable(true).setChecked(
 					true);
 			myWhats = pMenu.add(0, 8, 8, R.string.Map_30).setIcon(
 					R.drawable.menu_location);
 			myLicense = pMenu.add(0, 9, 9, R.string.Map_29).setIcon(
 					R.drawable.menu_location);
 			myAbout = pMenu.add(0, 10, 10, R.string.Map_28).setIcon(
 					R.drawable.menu_location);
 		} catch (Exception e) {
 			log.error("onCreateOptionsMenu: ", e);
 		}
 		return true;
 	}
 
 	@Override
 	public boolean onMenuItemSelected(int featureId, MenuItem item) {
 		boolean result = true;
 		try {
 			result = super.onMenuItemSelected(featureId, item);
 			switch (item.getItemId()) {
 			case 0:
 				try {
 					Map.this.showAddressDialog();
 				} catch (Exception e) {
 					log.error("showAddressDialog: ", e);
 				}
 				break;
 			case 1:
 				try {
 					LayoutInflater factory = LayoutInflater.from(this);
 					final View textEntryView = factory.inflate(
 							R.layout.mock_location_spinner, null);
 					Spinner sp = (Spinner) textEntryView
 							.findViewById(R.id.spinner_edit);
 					sp.setOnItemSelectedListener(new OnItemSelectedListener() {
 						@Override
 						public void onItemSelected(AdapterView parent, View v,
 								int position, long id) {
 							switch (position) {
 							case 0:
 								MockLocationProvider.locationManager
 										.setTestProviderStatus("gps",
 												LocationProvider.AVAILABLE,
 												null, 1000);
 								break;
 							case 1:
 								MockLocationProvider.locationManager
 										.setTestProviderStatus(
 												"gps",
 												LocationProvider.OUT_OF_SERVICE,
 												null, 1000);
 								break;
 							case 2:
 								MockLocationProvider.locationManager
 										.setTestProviderStatus(
 												"gps",
 												LocationProvider.TEMPORARILY_UNAVAILABLE,
 												null, 1000);
 								break;
 							case 3:
 								MockLocationProvider.locationManager
 										.setTestProviderEnabled("gps", true);
 								break;
 							case 4:
 								MockLocationProvider.locationManager
 										.setTestProviderEnabled("gps", false);
 								break;
 							}
 
 						}
 
 						@Override
 						public void onNothingSelected(AdapterView arg0) {
 
 						}
 					});
 
 					AlertDialog.Builder alertCache = new AlertDialog.Builder(
 							this);
 					alertCache.setView(textEntryView)
 							.setIcon(R.drawable.menu04).setTitle(
 									"Mock provider options").setPositiveButton(
 									"Close",
 									new DialogInterface.OnClickListener() {
 										public void onClick(
 												DialogInterface dialog,
 												int whichButton) {
 
 										}
 
 									}).setNegativeButton(
 									R.string.alert_dialog_cancel,
 									new DialogInterface.OnClickListener() {
 										public void onClick(
 												DialogInterface dialog,
 												int whichButton) {
 										}
 									}).create();
 					alertCache.show();
 
 				} catch (Exception e) {
 					log.error("", e);
 				}
 				break;
 			case 2:
 				try {
 
 					// recenterOnGPS = !recenterOnGPS;
 					// // osmap.switchPanMode();
 					//
 					// if (recenterOnGPS) {
 					// log.debug("recentering on GPS after check MyLocation on");
 					// myLocation.setIcon(R.drawable.menu01);
 					//
 					// } else {
 					// log
 					// .debug("stop recentering on GPS after check MyLocation off"
 					// );
 					// myLocation.setIcon(R.drawable.menu01_2);
 					// }
 					//
 					// if (this.mMyLocationOverlay.mLocation != null &&
 					// recenterOnGPS) {
 					log.debug("click on my location menu item");
 					if (this.mMyLocationOverlay.mLocation == null
 							|| (this.mMyLocationOverlay.mLocation
 									.getLatitudeE6() == 0 && this.mMyLocationOverlay.mLocation
 									.getLongitudeE6() == 0)) {
 						Toast
 								.makeText(this, R.string.Map_24,
 										Toast.LENGTH_LONG).show();
 						return true;
 					}
 					this.osmap.setZoomLevel(15, true);
 					this.osmap
 							.setMapCenterFromLonLat(
 									this.mMyLocationOverlay.mLocation
 											.getLongitudeE6() / 1E6,
 									this.mMyLocationOverlay.mLocation
 											.getLatitudeE6() / 1E6);
 					// }
 				} catch (Exception e) {
 					log.error("My location: ", e);
 				}
 				break;
 			case 3:
 				viewLayers();
 				break;
 			case 4:
 				try {
 					log.debug("switch pan mode");
 					osmap.switchPanMode();
 					if (osmap.isPanMode()) {
 						item.setIcon(R.drawable.mv_rectangle).setTitle(
 								R.string.Map_6);
 					} else {
 						item.setIcon(R.drawable.mv_pan)
 								.setTitle(R.string.Map_7);
 					}
 				} catch (Exception e) {
 					log.error("switchPanMode: ", e);
 				}
 				break;
 			case 5:
 				try {
 					showDownloadDialog();
 				} catch (Exception e) {
 					log.error("OpenWebsite: ", e);
 				}
 				break;
 			case 6:
 				try {
 					recenterOnGPS = !recenterOnGPS;
 
 					myLocationButton.setEnabled(!recenterOnGPS);
 					if (myZoomRectangle != null)
 						myZoomRectangle.setEnabled(!recenterOnGPS);
 					myDownloadLayers.setEnabled(!recenterOnGPS);
 					mySearchDirection.setEnabled(!recenterOnGPS);
 					myGPSButton.setEnabled(!recenterOnGPS);
 
 					if (!recenterOnGPS) {
 						log
 								.debug("recentering on GPS after check MyLocation on");
 						z.setVisibility(View.VISIBLE);
 						myNavigator.setIcon(R.drawable.menu_navigation);
 
 					} else {
 						log
 								.debug("stop recentering on GPS after check MyLocation off");//						
 						z.setVisibility(View.INVISIBLE);
 						myNavigator.setIcon(R.drawable.menu_navigation_off);
 					}
 					// if (recenterOnGPS || mMyLocationOverlay.mLocation != null
 					// ) {
 					// log.debug("recentering on GPS after check MyLocation on");
 					// myNavigator.setIcon(R.drawable.menu_navigation_off);
 					//
 					// } else {
 					// log
 					// .debug("stop recentering on GPS after check MyLocation off"
 					// );
 					// myNavigator.setIcon(R.drawable.menu_navigation);
 					// }
 					//				 
 					if (!navigation) {
 						// wl.acquire();
 						// setRequestedOrientation(ActivityInfo.
 						// SCREEN_ORIENTATION_PORTRAIT);
 						// navigation = true;
 						this.showNavigationModeAlert();
 					} else {
 						log.debug("navigation mode off");
 						setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
 						navigation = false;
 						wl.release();
 					}
 
 					//				
 					// osmap.switchPanMode();
 					// if (osmap.isPanMode()) {
 					// item.setIcon(R.drawable.mv_rectangle).setTitle(
 					// R.string.Map_6);
 					// } else {
 					// item.setIcon(R.drawable.mv_pan).setTitle(R.string.Map_7);
 					// }
 				} catch (Exception e) {
 					log.error("switchPanMode: ", e);
 				}
 				break;
 			case 7:
 				if (this.isLocationHandlerEnabled()) {
 					disableGPS();
 					myGPSButton.setChecked(false);
 				} else {
 					enableGPS();
 					myGPSButton.setChecked(true);
 
 				}
 				break;
 			case 8:
 				try {
 					showWhatsNew();
 				} catch (Exception e) {
 					log.error("OpenWebsite: ", e);
 				}
 				break;
 			case 9:
 				try {
 					showLicense();
 				} catch (Exception e) {
 					log.error("OpenWebsite: ", e);
 				}
 				break;
 			case 10:
 				try {
 					showAboutDialog();
 				} catch (Exception e) {
 					log.error("OpenWebsite: ", e);
 				}
 				break;
 			default:
 				break;
 			}
 		} catch (Exception e) {
 			log.error(e);
 		}
 		return result;
 	}
 
 	private void showNavigationModeAlert() {
 		try {
 			RadioGroup r = new RadioGroup(this);
 			RadioButton r1 = new RadioButton(this);
 			r1.setText(R.string.portrait);
 			r1.setId(0);
 			RadioButton r2 = new RadioButton(this);
 			r2.setText(R.string.landscape);
 			r2.setId(1);
 			r.addView(r1);
 			r.addView(r2);
 			r.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 
 				@Override
 				public void onCheckedChanged(RadioGroup arg0, int arg1) {
 					try {
 						centerOnGPSLocation();
 						wl.acquire();
 						navigation = true;
 						osmap
 								.onLayerChanged(osmap.getMRendererInfo()
 										.getNAME());
 						// final MapRenderer r =
 						// Map.this.osmap.getMRendererInfo();
 						Map.this.osmap.setZoomLevel(17, true);
 						switch (arg1) {
 						case 0:
 							log.debug("navifation mode vertical on");
 							setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
 							break;
 						case 1:
 							log.debug("navifation mode horizontal on");
 							setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
 							break;
 						default:
 							break;
 						}
 					} catch (Exception e) {
 						log.error("onCheckedChanged", e);
 					}
 				}
 
 			});
 			AlertDialog.Builder alertCache = new AlertDialog.Builder(this);
 			alertCache.setView(r).setIcon(R.drawable.menu_navigation).setTitle(
 					R.string.Map_Navigator).setPositiveButton(R.string.ok,
 					new DialogInterface.OnClickListener() {
 
 						@Override
 						public void onClick(DialogInterface dialog, int which) {
 							// TODO Auto-generated method stub
 
 						}
 
 					}).create();
 			alertCache.show();
 			r1.setChecked(true);
 		} catch (Exception e) {
 			log.error(e);
 		}
 	}
 
 	private void startCache(MapRenderer mr, int minZoom, int maxZoom) {
 		// GPSPoint center = this.osmap.getMapCenter();
 		// Point centerDouble = new Point(center.getLongitudeE6() / 1E6, center
 		// .getLatitudeE6() / 1E6);
 		// centerDouble = TileConversor.latLonToMercator(centerDouble.getX(),
 		// centerDouble.getY());
 		// // Point a = new Point(-0.43, 39.4);
 		// // Point b = new Point(-0.3, 39.53);
 		// Extent e = ViewPort.calculateExtent(centerDouble,
 		// Tags.RESOLUTIONS[this.osmap.getZoomLevel()], this.osmap
 		// .getWidth(), this.osmap.getHeight());
 		//
 		// // Extent e = new Extent(-20037508.3427892430765884088807,
 		// // -20037508.3427892430765884088807,
 		// // 20037508.3427892430765884088807,
 		// // 20037508.3427892430765884088807);
 		// // Extent e = new Extent(-5037508.3427892430765884088807,
 		// // -5037508.3427892430765884088807,
 		// // 37508.3427892430765884088807, 37508.3427892430765884088807);
 		// OSMHandler handler = new OSMHandler();
 		// handler.setURL(new String[] { mr.getBASEURL() });
 		//
 		// // YahooHandler h = new YahooHandler();
 		// // h.setURL(new
 		// // String[]{"http://png.maps.yimg.com/png?t=m&v=4.1&s=256&f=j"});
 		//
 		// // String layerName = this.osmap.getMRendererInfo().getNAME();
 		// String layerName = "estoesunaprueba2";
 		//
 		// try {
 		// es.prodevelop.gvsig.mini.phonecache.Cancellable c =
 		// es.prodevelop.gvsig.mini.phonecache.Utilities
 		// .getNewCancellable();
 		// Grid g = new Grid(handler, e, Tags.RESOLUTIONS[minZoom],
 		// Tags.RESOLUTIONS[maxZoom], layerName, c);
 		// g.addDownloadWaiter(this);
 		//
 		// ThreadPool.getInstance().assign(g);
 		// } catch (IOException exc) {
 		// Log.e("", exc.getMessage());
 		// }
 	}
 
 	/**
 	 * Shows an AlertDialog with the results from WeatherFunctionality
 	 * 
 	 * @param ws
 	 *            The results from WeatherFunctionality
 	 */
 	public void showWeather(WeatherSet ws) {
 		try {
 			log.debug("showWeather");
 			if (ws == null) {
 				log
 						.debug("ws == null: Can't get weather. Check another location");
 				Toast.makeText(Map.this, R.string.Map_8, Toast.LENGTH_LONG)
 						.show();
 				return;
 			}
 			if (ws.getWeatherCurrentCondition() == null) {
 				dialog2.dismiss();
 				AlertDialog.Builder alertW = new AlertDialog.Builder(this);
 				alertW.setCancelable(true);
 				alertW.setIcon(R.drawable.menu03);
 				alertW.setTitle(R.string.error);
 				if (ws.place == null || ws.place.compareTo("") == 0) {
 					ws.place = this.getResources().getString(R.string.Map_9);
 				}
 
 				log.debug("The weather in " + ws.place + " is not available");
 				alertW.setMessage(String.format(this.getResources().getString(
 						R.string.Map_10), ws.place));
 
 				alertW.setNegativeButton(R.string.ok,
 						new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog,
 									int whichButton) {
 							}
 						});
 				alertW.show();
 				return;
 			}
 
 			AlertDialog.Builder alertW = new AlertDialog.Builder(this);
 			alertW.setCancelable(true);
 			alertW.setIcon(R.drawable.menu03);
 			alertW.setTitle(this.getResources().getString(R.string.Map_11)
 					+ " " + ws.place);
 
 			final ListView lv = new ListView(this);
 
 			BulletedTextListAdapter adapter = new BulletedTextListAdapter(this);
 
 			WeatherCurrentCondition wc = ws.getWeatherCurrentCondition();
 
 			adapter.addItem(new BulletedText(new StringBuffer().append(
 					this.getResources().getString(R.string.Map_12)).append(
 					" - ").append(wc.getTempCelcius()).append(" �C").append(
 					"\n").append(wc.getCondition()).append("\n").append(
 					wc.getWindCondition()).append("\n")
 					.append(wc.getHumidity()).toString(), BulletedText
 					.getRemoteImage(new URL("http://www.google.com"
 							+ wc.getIconURL()))).setSelectable(false));
 
 			ArrayList<WeatherForecastCondition> l = ws
 					.getWeatherForecastConditions();
 
 			WeatherForecastCondition temp;
 			for (int i = 0; i < l.size(); i++) {
 				try {
 					temp = l.get(i);
 					adapter.addItem(new BulletedText(new StringBuffer().append(
 							temp.getDayofWeek()).append(" - ").append(
 							temp.getTempMinCelsius()).append(" �C").append("/")
 							.append(temp.getTempMaxCelsius()).append(" �C")
 							.append("\n").append(temp.getCondition())
 							.toString(), BulletedText.getRemoteImage(new URL(
 							"http://www.google.com" + temp.getIconURL())))
 							.setSelectable(false));
 				} catch (Exception e) {
 					log.error("showWeather: ", e);
 				}
 			}
 
 			lv.setAdapter(adapter);
 			lv.setPadding(10, 0, 10, 0);
 
 			alertW.setView(lv);
 
 			alertW.setNegativeButton(this.getResources().getString(
 					R.string.back), new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int whichButton) {
 				}
 			});
 
 			alertW.show();
 			dialog2.dismiss();
 		} catch (Exception e) {
 			log.error("showWeather: ", e);
 		} finally {
 		}
 	}
 
 	/**
 	 * Launches the WeatherFunctionality
 	 * 
 	 * @param x
 	 *            The x coordinate
 	 * @param y
 	 *            The y coordinate
 	 * @param SRS
 	 *            The SRS in which the coordinates are expressed
 	 */
 	public void getWeather(double x, double y, String SRS) {
 		try {
 			log.debug("Launching weather functionality");
 			double[] coords = ConversionCoords.reproject(x, y, CRSFactory
 					.getCRS(SRS), CRSFactory.getCRS("EPSG:4326"));
 
 			WeatherFunctionality w = new WeatherFunctionality(this, 0,
 					coords[1], coords[0]);
 			w.onClick(null);
 			userContext.setUsedWeather(true);
 			userContext.setLastExecWeather();
 		} catch (Exception e) {
 			log.error("getWeather: ", e);
 		}
 	}
 
 	/**
 	 * Shows an AlertDialog indicating that the route calculation has failed
 	 */
 	public void showRouteError() {
 		try {
 			log.debug("Show route error");
 			dialog2.dismiss();
 			AlertDialog.Builder alert = new AlertDialog.Builder(this);
 			alert.setIcon(R.drawable.routes);
 			alert.setTitle(R.string.error);
 			alert.setMessage(R.string.Map_13);
 
 			alert.setNegativeButton(R.string.ok,
 					new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog,
 								int whichButton) {
 						}
 					});
 
 			alert.show();
 		} catch (Exception e) {
 			log.error("showRouteError: ", e);
 		}
 	}
 
 	@Override
 	public void onDestroy() {
 		try {
 			log.debug("onDestroy map activity");
 
 			try {
 				log.debug("release wake lock");
 				wl.release();
 			} catch (Exception e) {
 				log.error("release wake lock", e);
 
 			}
 
 			try {
 				log.debug("persist map");
 				mapState.persist();
 			} catch (Exception e) {
 				log.error("Persist mapstate: ", e);
 			}
 
 			if (dialog2 != null)
 				dialog2.dismiss();
 
 			Utils.clearLogs();
 			osmap.clearCache();
 			this.stopSensor(this);
 			if (backpressed) {
 				log.debug("back key pressed");
 				// cosas a hacer para la limpieza
 
 				// this.getGPSManager().stopLocationProviders();
 				WorkQueue.getInstance().finalize();
 				WorkQueue.getExclusiveInstance().finalize();
 
 				backpressed = false;
 
 			}
 			try {
 				contextManager.saveContexts();
 			} catch (Exception e) {
 				log.error("onDestroy. contextManager.saveContexts(): ", e);
 			}
 
 			this.destroy();
 
 			super.onDestroy();
 		} catch (Exception e) {
 			log.error("onDestroy: ", e);
 		}
 	}
 
 	/**
 	 * Frees memory
 	 */
 	public void destroy() {
 		try {
 			log.debug("destroy");
 			osmap.destroy();
 			// route.destroy();
 			// nameds.destroy();
 			// osmap = null;
 			// nameds = null;
 		} catch (Exception e) {
 			log.error("destroy", e);
 		}
 	}
 
 	/**
 	 * Load pois stored on a Bundle when the configuration has changed or the
 	 * Activity has been restarted
 	 * 
 	 * @param outState
 	 *            The Bundle @see {@link #onSaveInstanceState(Bundle)}
 	 */
 	public void loadPois(Bundle outState) {
 		try {
 			log.debug("loadPOIS from saved instance");
 			int poisSize = outState.getInt("PoisSize");
 			Named[] points = new Named[poisSize];
 			Named n;
 			for (int i = 0; i < poisSize; i++) {
 				try {
 					n = new Named(outState.getDouble("X" + i), outState
 							.getDouble("Y" + i));
 					n.description = outState.getString("Description" + i);
 					n.type = outState.getString("Type" + i);
 					n.id = outState.getInt("Id" + i);
 					n.name = outState.getString("Name" + i);
 					n.category = outState.getString("Category" + i);
 					n.info = outState.getString("Info" + i);
 					n.rank = outState.getInt("Rank" + i);
 					n.id = outState.getInt("In" + i);
 					n.distance = outState.getDouble("Distance" + i);
 					n.projectedCoordinates = new Point(outState.getDouble("PX"
 							+ i), outState.getDouble("PY" + i));
 					points[i] = n;
 				} catch (Exception e) {
 					log.error("loadPois: ", e);
 				}
 			}
 
 			if (nameds == null) {
 				nameds = new NamedMultiPoint(points);
 			} else {
 				nameds.setPoints(points);
 			}
 
 			cleanVisible = outState.getBoolean("cleanPoisVisible", false);
 			listVisible = outState.getBoolean("listPoisVisible", false);
 			log.debug("POIS loaded");
 		} catch (Exception e) {
 			log.error("loadPOIS: ", e);
 		}
 	}
 
 	/**
 	 * Save pois when the configuration has changed or the Activity has been
 	 * restarted
 	 * 
 	 * @param outState
 	 *            The Bundle @see {@link #onSaveInstanceState(Bundle)}
 	 */
 	public void savePois(Bundle outState) {
 		try {
 			log.debug("Save POIS to bundle");
 			int pointNumber = this.nameds.getNumPoints();
 			outState.putInt("PoisSize", pointNumber);
 			Named n;
 			for (int i = 0; i < pointNumber; i++) {
 				try {
 					n = (Named) this.nameds.getPoint(i);
 					outState.putString("Type" + i, n.type);
 					outState.putInt("Id" + i, n.id);
 					outState.putString("Name" + i, n.name);
 					outState.putString("Category" + i, n.category);
 					outState.putString("Info" + i, n.info);
 					outState.putInt("Rank" + i, n.rank);
 					outState.putString("In" + i, n.isIn);
 					outState.putDouble("Distance" + i, n.distance);
 					outState.putString("Description" + i, n.description);
 					outState.putDouble("X" + i, n.getX());
 					outState.putDouble("Y" + i, n.getY());
 					outState.putDouble("PX" + i, n.projectedCoordinates.getX());
 					outState.putDouble("PY" + i, n.projectedCoordinates.getY());
 				} catch (Exception e) {
 					log.error("savePOIS: ", e);
 				}
 			}
 
 			if (pointNumber > 0) {
 				outState.putBoolean("cleanPoisVisible", true);
 				outState.putBoolean("listPoisVisible", true);
 			}
 			log.debug("POIS saved");
 		} catch (Exception e) {
 			log.error("savePOIS: ", e);
 		}
 	}
 
 	/**
 	 * Load route stored on a Bundle when the configuration has changed or the
 	 * Activity has been restarted
 	 * 
 	 * @param outState
 	 *            The Bundle @see {@link #onSaveInstanceState(Bundle)}
 	 */
 	public void loadRoute(Bundle outState) {
 		try {
 			log.debug("load route from saved instance");
 			boolean isDone = outState.getBoolean("isDone", true);
 
 			double startPointX = outState.getDouble("StartPointX");
 			double startPointY = outState.getDouble("StartPointY");
 			double endPointX = outState.getDouble("EndPointX");
 			double endPointY = outState.getDouble("EndPointY");
 			Point starPoint = new Point(startPointX, startPointY);
 			Point endPoint = new Point(endPointX, endPointY);
 
 			if (starPoint.equals(endPoint)) {
 				log
 						.debug("start and end point equals. The route will not be loaded"
 								+ "return");
 			}
 
 			route.setStartPoint(starPoint);
 			route.setEndPoint(endPoint);
 			if (!isDone) {
 				log
 						.debug("Route was calculating before saving instance: Relaunch it");
 				this.launchRouteCalculation();
 				// return;
 			}
 			log.debug("Loading route points");
 			int routeSize = outState.getInt("RouteSize");
 			double[] xCoords = new double[routeSize];
 			double[] yCoords = new double[routeSize];
 			for (int i = 0; i < routeSize; i++) {
 				xCoords[i] = outState.getDouble("RX" + i);
 				yCoords[i] = outState.getDouble("RY" + i);
 			}
 			LineString line = new LineString(xCoords, yCoords);
 			FeatureCollection f = new FeatureCollection();
 			f.addFeature(new Feature(line));
 			route.setRoute(f);
 
 			cleanRoute = outState.getBoolean("cleanRouteVisible", false);
 			log.debug("route loaded");
 		} catch (Exception e) {
 			log.error("loadRoute: ", e);
 		}
 	}
 
 	/**
 	 * Save route when the configuration has changed or the Activity has been
 	 * restarted
 	 * 
 	 * @param outState
 	 *            The Bundle @see {@link #onSaveInstanceState(Bundle)}
 	 */
 	public void saveRoute(Bundle outState) {
 		try {
 			Point routeStart = this.route.getStartPoint();
 			Point routeEnd = this.route.getEndPoint();
 			FeatureCollection r = this.route.getRoute();
 
 			boolean isDone = true;
 			try {
 				Functionality yoursF = getItemContext()
 						.getExecutingFunctionality();
 				if (yoursF != null && yoursF instanceof YOURSFunctionality) {
 					isDone = !yoursF.isActive();
 				}
 				outState.putBoolean("isDone", isDone);
 			} catch (Exception e) {
 
 			}
 			outState.putDouble("StartPointX", routeStart.getX());
 			outState.putDouble("StartPointY", routeStart.getY());
 			outState.putDouble("EndPointX", routeEnd.getX());
 			outState.putDouble("EndPointY", routeEnd.getY());
 
 			boolean save = false;
 			if (r != null && r.getSize() > 0) {
 				Feature f = r.getFeatureAt(0);
 				LineString l = (LineString) f.getGeometry();
 				outState.putInt("RouteSize", l.getXCoords().length);
 				for (int i = 0; i < l.getXCoords().length; i++) {
 					save = true;
 					outState.putDouble("RX" + i, l.getXCoords()[i]);
 					outState.putDouble("RY" + i, l.getYCoords()[i]);
 				}
 			}
 
 			if (save)
 				outState.putBoolean("cleanRouteVisible", true);
 
 			log.debug("route saved");
 		} catch (Exception e) {
 			log.error("savRoute: ", e);
 		}
 	}
 
 	/**
 	 * Load map info stored on a Bundle when the configuration has changed or
 	 * the Activity has been restarted
 	 * 
 	 * @param outState
 	 *            The Bundle @see {@link #onSaveInstanceState(Bundle)}
 	 */
 	public void loadMap(Bundle outState) {
 		DisplayMetrics metrics = new DisplayMetrics();
 		getWindowManager().getDefaultDisplay().getMetrics(metrics);
 		try {
 			log.debug("load map from saved instance");
 			String mapLayer = outState.getString("maplayer");
 			log.debug("previous layer: " + mapLayer);
 			osmap.onLayerChanged(mapLayer);
 			this.mMyLocationOverlay.loadState(outState);
 			log.debug("map loaded");
 		} catch (Exception e) {
 			log.error("loadMap: ", e);
 			TMSRenderer t1 = TMSRenderer.getICCOrtoRenderer();
 			TMSRenderer t2 = TMSRenderer.getICCTopoRenderer();
 			this.osmap = new TileRaster(this, t1, t2, metrics.widthPixels,
 					metrics.heightPixels);
 
 		} finally {
 			try {
 				if (outState == null)
 					return;
 				String contextName = outState.getString("contextClassName");
 				log.debug("loading previous context: " + contextName);
 				ItemContext context = (ItemContext) Class.forName(contextName)
 						.newInstance();
 				if (context != null) {
 					context.setMap(this);
 					this.setContext(context);
 				}
 			} catch (IllegalAccessException e) {
 				log.error("", e);
 			} catch (InstantiationException e) {
 				log.error("", e);
 			} catch (ClassNotFoundException e) {
 				log.error("", e);
 			} catch (Exception e) {
 				log.error("", e);
 			}
 		}
 	}
 
 	/**
 	 * Save settings when the configuration has changed or the Activity has been
 	 * restarted
 	 * 
 	 * @param outState
 	 *            The Bundle @see {@link #onSaveInstanceState(Bundle)}
 	 */
 	public void saveSettings(Bundle outState) {
 		try {
 			outState.putFloat("dataTransfer", this.datatransfer2);
 			outState.putBoolean("dataNavigation", this.navigation);
 			outState.putBoolean("dataRecent", this.recenterOnGPS);
 		} catch (Exception e) {
 			log.error("saveSettings: ", e);
 		}
 	}
 
 	/**
 	 * Load settings stored on a Bundle when the configuration has changed or
 	 * the Activity has been restarted
 	 * 
 	 * @param outState
 	 *            The Bundle @see {@link #onSaveInstanceState(Bundle)}
 	 */
 	public void loadSettings(Bundle outState) {
 		try {
 			if (outState == null)
 				return;
 			this.datatransfer2 = outState.getFloat("dataTransfer");
 			this.navigation = outState.getBoolean("dataNavigation");
 			this.recenterOnGPS = outState.getBoolean("dataRecent");
 			if (navigation && recenterOnGPS)
 				wl.acquire();
 		} catch (Exception e) {
 			log.error("loadSettings: ", e);
 		}
 	}
 
 	/**
 	 * Save map info (current layer, GPS location, ItemContext...) when the
 	 * configuration has changed or the Activity has been restarted
 	 * 
 	 * @param outState
 	 *            The Bundle @see {@link #onSaveInstanceState(Bundle)}
 	 */
 	public void saveMap(Bundle outState) {
 		try {
 			log.debug("save map to bundle");
 			outState.putString("maplayer", osmap.getMRendererInfo().getNAME());
 			this.mMyLocationOverlay.saveState(outState);
 			ItemContext context = this.getItemContext();
 			if (context != null)
 				outState.putString("contextClassName", context.getClass()
 						.getName());
 			else
 				outState.putString("contextClassName", DefaultContext.class
 						.getName());
 			log.debug("map saved");
 		} catch (Exception e) {
 			log.error("saveMap: ", e);
 		}
 	}
 
 	/**
 	 * Synchronize zoomcontrols with TileRaster.MapRenderer zoom level
 	 */
 	public void updateZoomControl() {
 		try {
 			MapRenderer r = Map.this.osmap.getMRendererInfo();
 
 			if (r.getZoomLevel() > 0)
 				z.setIsZoomOutEnabled(true);
 			else
 				z.setIsZoomOutEnabled(false);
 
 			if (r.getZOOM_MAXLEVEL() > r.getZoomLevel()) {
 				z.setIsZoomInEnabled(true);
 			} else {
 				z.setIsZoomInEnabled(false);
 			}
 		} catch (Exception e) {
 			log.error("updateZoomControl: ", e);
 		}
 	}
 
 	/**
 	 * Instantiates the UI: TileRaster, ZoomControls, SeekBar in a
 	 * RelativeLayout
 	 * 
 	 * @param savedInstanceState
 	 */
 	public void loadUI(Bundle savedInstanceState) {
 		try {
 			log.debug("load UI");
 			rl.addView(this.osmap, new RelativeLayout.LayoutParams(
 					LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
 
 			/* Creating the main Overlay */
 			{
 				this.mMyLocationOverlay = new ViewSimpleLocationOverlay(this,
 						osmap);
 				this.osmap.getOverlays()
 						.add(new NameFinderOverlay(this, osmap));
 				this.osmap.getOverlays().add(new RouteOverlay(this, osmap));
 				this.osmap.getOverlays().add(mMyLocationOverlay);
 			}
 
 			z = new ZoomControls(this);
 			final RelativeLayout.LayoutParams zzParams = new RelativeLayout.LayoutParams(
 					RelativeLayout.LayoutParams.WRAP_CONTENT,
 					RelativeLayout.LayoutParams.WRAP_CONTENT);
 			zzParams.addRule(RelativeLayout.ALIGN_BOTTOM);
 			zzParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
 			rl.addView(z, zzParams);
 			z.setId(107);
 			z.setVisibility(View.VISIBLE);
 
 			z.setOnZoomInClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					try {
 						Map.this.osmap.zoomIn();
 
 					} catch (Exception e) {
 						log.error("onZoomInClick: ", e);
 					}
 				}
 			});
 			z.setOnZoomOutClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					try {
 						Map.this.osmap.zoomOut();
 						// Map.this.osmap.switchPanMode();
 					} catch (Exception e) {
 						log.error("onZoomOutClick: ", e);
 					}
 				}
 			});
 			
 			trans = new SeekBar(this);
 			trans.setProgress(100);
 			trans.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
 				@Override
 				public void onProgressChanged(SeekBar seekBar, int progress,
 						boolean fromUser) {
 					int alpha = (int) Math.floor(256.0 * progress / 100.0);
 					Map.this.setLayerAlpha(alpha);
 				}
 				
 				@Override
 				public void onStartTrackingTouch(SeekBar seekBar) {
 				}
 
 				@Override
 				public void onStopTrackingTouch(SeekBar seekBar) {
 					try {
 						int alpha = (int) Math.floor(256.0 * seekBar.getProgress() / 100.0);
 						Map.this.setLayerAlpha(alpha);
 					} catch (Exception e) {
 						log.error("onStopTrackingTouch for trans SeekBar: ", e);
 					}
 				}
 			});
 			
 			final RelativeLayout.LayoutParams transParams = new RelativeLayout.LayoutParams(
 					RelativeLayout.LayoutParams.FILL_PARENT, 40);
 			transParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
 			trans.setVisibility(View.VISIBLE);
 			rl.addView(trans, transParams);
 
 			log.debug("ui loaded");
 		} catch (Exception e) {
 			log.error("", e);
 		} catch (OutOfMemoryError ou) {
 			System.gc();
 			log.error("", ou);
 		}
 	}
 
 	/**
 	 * Loads the last map center and zoom level stored on a Bundle when the
 	 * configuration has changed or the Activity has been restarted
 	 * 
 	 * @param outState
 	 *            The Bundle @see {@link #onSaveInstanceState(Bundle)}
 	 */
 	public void loadCenter(Bundle savedInstanceState) {
 		try {
 			log.debug("load center from saved instance");
 			double lat = savedInstanceState.getDouble("lat");
 			double longit = savedInstanceState.getDouble("longit");
 			int zoomlvl = savedInstanceState.getInt("zoomlvl");
 			log
 					.debug("lat, lon, zoom: " + lat + ", " + longit + ", "
 							+ zoomlvl);
 
 			this.osmap.setMapCenter(longit, lat);
 			this.osmap.setZoomLevel(zoomlvl);
 			
 			log
 					.debug("lat, lon, zoom: " + longit + ", " + lat + ", "
 							+ zoomlvl);
 		} catch (Exception e) {
 			log.error("loadCenter: ", e);
 			osmap.getMRendererInfo().centerOnBBox();
 		}
 	}
 
 	/**
 	 * Saves the map center and zoom level on a Bundle when the configuration
 	 * has changed or the Activity has been restarted
 	 * 
 	 * @param outState
 	 *            The Bundle @see {@link #onSaveInstanceState(Bundle)}
 	 */
 	public void saveCenter(Bundle outState) {
 		try {
 			log.debug("save center to bundle");
 			final Point center = this.osmap.getMRendererInfo().getCenter();
 			final int zoomLevel = this.osmap.getMRendererInfo().getZoomLevel();
 			outState.putDouble("lat", center.getY());
 			outState.putDouble("longit", center.getX());
 			outState.putInt("zoomlvl", zoomLevel);
 			log.debug("lat, lon, zoom: " + center.getY() + ", " + center.getX()
 					+ ", " + zoomLevel);
 		} catch (Exception e) {
 			log.error("saveCenter: ", e);
 		}
 	}
 
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		try {
 			log.debug("onSaveInstanceState");
 			outState.putBoolean("isSaved", true);
 			super.onSaveInstanceState(outState);
 			saveSettings(outState);
 			saveMap(outState);
 		} catch (Exception e) {
 			log.error("onSaveInstanceState: ", e);
 		}
 		try {
 			saveCenter(outState);
 		} catch (Exception e) {
 			log.error("", e);
 		}
 		try {
 			saveRoute(outState);
 		} catch (Exception e) {
 			log.error("", e);
 		}
 		try {
 			savePois(outState);
 		} catch (Exception e) {
 			log.error("", e);
 		}
 	}
 
 	/**
 	 * This class Handles messages from Functionalities
 	 * 
 	 * @author aromeu
 	 * @author rblanco
 	 * 
 	 */
 	private class MapHandler extends Handler {
 		@Override
 		public void handleMessage(final Message msg) {
 			try {
 				log.debug("MapHandler -> handleMessage");
 				final int what = msg.what;
 				switch (what) {
 				case TaskHandler.NO_RESPONSE:
 					log.debug("task handler NO_RESPONSE");
 					if (dialog2 != null)
 						dialog2.dismiss();
 					Toast
 							.makeText(Map.this, R.string.Map_22,
 									Toast.LENGTH_LONG).show();
 					break;
 				case Map.SHOW_TOAST:
 					log.debug("SHOW_TOAST");
 					Toast t = Toast.makeText(Map.this, msg.obj.toString(),
 							Toast.LENGTH_LONG);
 					t.show();
 					break;
 				case Map.POI_LIST:
 					log.debug("POI_LIST");
 					Map.this.viewLastPOIs();
 					break;
 				case Map.POI_CLEARED:
 					log.debug("POI_CLEARED");
 					Map.this.updateContext(Map.POI_CLEARED);
 					break;
 				case Map.ROUTE_CLEARED:
 					log.debug("ROUTE_CLEARED");
 					Map.this.updateContext(Map.ROUTE_CLEARED);
 					break;
 				case Map.WEATHER_INITED:
 					log.debug("WEATHER_INITED");
 					dialog2 = ProgressDialog.show(Map.this, Map.this
 							.getResources().getString(R.string.please_wait),
 							Map.this.getResources().getString(R.string.Map_14),
 							true);
 					dialog2.setCancelable(true);
 					dialog2.setCanceledOnTouchOutside(true);
 					dialog2.setIcon(R.drawable.menu03);
 					dialog2.setOnCancelListener(new OnCancelListener() {
 						@Override
 						public void onCancel(DialogInterface dialog2) {
 							try {
 								log.debug("weather canceled");
 								Map.this.getItemContext().cancelCurrentTask();
 								dialog2.dismiss();
 							} catch (Exception e) {
 								log.error("onCancelDialog: ", e);
 							}
 						}
 					});
 					break;
 				case Map.WEATHER_CANCELED:
 					log.debug("WEATHER_CANCELED");
 					Toast
 							.makeText(Map.this, R.string.Map_15,
 									Toast.LENGTH_LONG).show();
 					break;
 				case Map.WEATHER_ERROR:
 					log.debug("WEATHER_ERROR");
 					Toast.makeText(Map.this, R.string.Map_8, Toast.LENGTH_LONG)
 							.show();
 					if (dialog2 != null)
 						dialog2.dismiss();
 					break;
 				case Map.WEATHER_SHOW:
 					log.debug("WEATHER_SHOW");
 					Functionality f = Map.this.getItemContext()
 							.getExecutingFunctionality();
 					if (f instanceof WeatherFunctionality)
 						Map.this.showWeather(((WeatherFunctionality) f).ws);
 					else {
 						log.debug("Nof found Weather functionality");
 					}
 					break;
 				case Map.ROUTE_NO_RESPONSE:
 					log.debug("ROUTE_NO_RESPONSE");
 					Toast.makeText(Map.this, R.string.server_busy,
 							Toast.LENGTH_LONG).show();
 					// ivCleanRoute.setVisibility(View.INVISIBLE);
 					if (dialog2 != null)
 						dialog2.dismiss();
 					break;
 				case Map.ROUTE_SUCCEEDED:
 					log.debug("ROUTE_SUCCEEDED");
 					// ivCleanRoute.setVisibility(View.VISIBLE);
 					osmap.CLEAR_ROUTE = true;
 					if (dialog2 != null)
 						dialog2.dismiss();
 					osmap.getMRendererInfo().reprojectGeometryCoordinates(
 							route.getRoute().getFeatureAt(0).getGeometry(),
 							"EPSG:4326");
 					Map.this.updateContext(Map.ROUTE_SUCCEEDED);
 					osmap.postInvalidate();
 					break;
 				case Map.ROUTE_NO_CALCULATED:
 					log.debug("ROUTE_NO_CALCULATED");
 					Toast
 							.makeText(Map.this, R.string.Map_16,
 									Toast.LENGTH_LONG).show();
 					// ivCleanRoute.setVisibility(View.INVISIBLE);
 					if (dialog2 != null)
 						dialog2.dismiss();
 					break;
 				case Map.POI_SHOW:
 					log.debug("POI_SHOW");
 					Functionality nf = getItemContext()
 							.getExecutingFunctionality();
 					if (nf instanceof NameFinderFunc) {
 						NameFinderFunc n = (NameFinderFunc) nf;
 						osmap.getMRendererInfo().reprojectGeometryCoordinates(
 								n.nm, "EPSG:4326");
 						boolean update = Map.this.showPOIs(n.desc, n.nm);
 						if (update)
 							Map.this.updateContext(Map.POI_SUCCEEDED);
 					} else {
 						log.debug("Nof found NameFinder functionality");
 					}
 					break;
 				case Map.POI_INITED:
 					log.debug("POI_INITED");
 					dialog2 = ProgressDialog.show(Map.this, Map.this
 							.getResources().getString(R.string.please_wait),
 							Map.this.getResources().getString(R.string.Map_17),
 							true);
 					dialog2.setCancelable(true);
 					dialog2.setCanceledOnTouchOutside(true);
 					dialog2.setIcon(R.drawable.pois);
 					dialog2.setOnCancelListener(new OnCancelListener() {
 						@Override
 						public void onCancel(DialogInterface dialog2) {
 							try {
 								Map.this.getItemContext().cancelCurrentTask();
 								dialog2.dismiss();
 							} catch (Exception e) {
 								log.error("onCancelDialog: ", e);
 							}
 						}
 					});
 					break;
 				case Map.ROUTE_CANCELED:
 					log.debug("ROUTE_CANCELED");
 					route.deleteRoute(false);
 					// route.deleteEndPoint();
 					// route.deleteStartPoint();
 					// yoursFunc.cancel();
 					if (dialog2 != null)
 						dialog2.dismiss();
 					// ivCleanRoute.setVisibility(View.INVISIBLE);
 					Toast
 							.makeText(Map.this, R.string.Map_18,
 									Toast.LENGTH_LONG).show();
 					osmap.postInvalidate();
 					break;
 				case Map.POI_CANCELED:
 					log.debug("POI_CANCELED");
 					// ivCleanPois.setVisibility(View.INVISIBLE);
 					// ivShowList.setVisibility(View.INVISIBLE);
 					if (dialog2 != null)
 						dialog2.dismiss();
 					Toast.makeText(Map.this, R.string.task_canceled,
 							Toast.LENGTH_LONG).show();
 					nameds = null;
 					osmap.postInvalidate();
 					break;
 				case Map.CALCULATE_ROUTE:
 					log.debug("CALCULATE_ROUTE");
 					Map.this.calculateRoute();
 					break;
 				case Map.ROUTE_INITED:
 					log.debug("ROUTE_INITED");
 					route.deleteRoute(false);
 					dialog2 = ProgressDialog.show(Map.this, Map.this
 							.getResources().getString(R.string.please_wait),
 							Map.this.getResources().getString(R.string.Map_19),
 							true);
 					dialog2.setCancelable(true);
 					dialog2.setCanceledOnTouchOutside(true);
 					dialog2.setIcon(R.drawable.routes);
 
 					dialog2.setOnCancelListener(new OnCancelListener() {
 						@Override
 						public void onCancel(DialogInterface dialog2) {
 							try {
 								route.deleteRoute(false);
 								// ivCleanRoute.setVisibility(View.INVISIBLE);
 								osmap.postInvalidate();
 								Map.this.getItemContext().cancelCurrentTask();
 							} catch (Exception e) {
 								log.error("onCancelDialog: ", e);
 							}
 						}
 					});
 					break;
 				case Map.TWEET_SENT:
 					log.debug("TWEET_SENT");
 					Toast
 							.makeText(Map.this, R.string.Map_20,
 									Toast.LENGTH_LONG).show();
 					break;
 				case Map.TWEET_ERROR:
 					log.debug("TWEET_ERROR");
 					Toast t1 = Toast.makeText(Map.this, msg.obj.toString(),
 							Toast.LENGTH_LONG);
 					t1.show();
 					break;
 				case Map.SHOW_TWEET_DIALOG:
 					log.debug("SHOW_TWEET_DIALOG");
 					Map.this.showTweetDialog();
 					break;
 				case Map.SHOW_POI_DIALOG:
 					log.debug("SHOW_POI_DIALOG");
 					Map.this.showPOIDialog();
 					break;
 				case Map.SHOW_ADDRESS_DIALOG:
 					log.debug("SHOW_ADDRESS_DIALOG");
 					Map.this.showAddressDialog();
 					break;
 				}
 			} catch (Exception e) {
 				log.error("handleMessage: ", e);
 				if (dialog2 != null)
 					dialog2.dismiss();
 				// Toast.makeText(Map.this,
 				// "Operation could not finish. Please try again.",
 				// 2000).show();
 			} finally {
 				try {
 					Map.this.clearContext();
 					osmap.invalidate();
 				} catch (Exception e) {
 					log.error(e);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Shows an AlertDialog to the user to input the query string for NameFinder
 	 */
 	public void showPOIDialog() {
 		try {
 			log.debug("showPOIDialog");
 			AlertDialog.Builder alertPOI = new AlertDialog.Builder(this);
 
 			alertPOI.setIcon(R.drawable.poismenu);
 			alertPOI.setTitle(R.string.Map_21);
 
 			final EditText inputPOI = new EditText(this);
 
 			alertPOI.setView(inputPOI);
 
 			alertPOI.setPositiveButton(R.string.search,
 					new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog,
 								int whichButton) {
 							try {
 								Editable value = inputPOI.getText();
 								if (nearopt != 0) {
 									NameFinder.parms = value.toString();
 								} else {
 									double[] center = osmap.getCenterLonLat();
 									NameFinder.parms = value.toString()
 											+ " near "
 
 											+ center[1] + "," + center[0];
 								}
 								NameFinderFunc func = new NameFinderFunc(
 										Map.this, 0);
 								func.onClick(null);
 
 							} catch (Exception e) {
 								log.error("clickNameFinder: ", e);
 							}
 							return;
 						}
 					});
 
 			alertPOI.setNegativeButton(R.string.cancel,
 					new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog,
 								int whichButton) {
 						}
 					});
 
 			alertPOI.show();
 		} catch (Exception e) {
 			log.error("", e);
 		}
 	}
 
 	/**
 	 * Show an AlertDialog to the user to input the query string for NameFinder
 	 * addresses
 	 */
 	public void showAddressDialog() {
 		try {
 			log.debug("show address dialog");
 			AlertDialog.Builder alert = new AlertDialog.Builder(this);
 
 			alert.setIcon(R.drawable.menu00);
 			alert.setTitle(R.string.Map_3);
 			final EditText input = new EditText(this);
 			alert.setView(input);
 
 			alert.setPositiveButton(R.string.alert_dialog_text_search,
 					new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog,
 								int whichButton) {
 							try {
 								Editable value = input.getText();
 
 								if (nearopt != 0) {
 									NameFinder.parms = value.toString();
 								} else {
 									double[] center = osmap.getCenterLonLat();
 									NameFinder.parms = value.toString()
 											+ " near "
 
 											+ center[1] + "," + center[0];
 								}
 								NameFinderFunc func = new NameFinderFunc(
 										Map.this, 0);
 								func.onClick(null);
 							} catch (Exception e) {
 								log.error("clickNameFinderAddress: ", e);
 							}
 							return;
 						}
 					});
 
 			alert.setNegativeButton(R.string.alert_dialog_text_cancel,
 					new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog,
 								int whichButton) {
 						}
 					});
 
 			alert.show();
 		} catch (Exception e) {
 			log.error("", e);
 		}
 	}
 
 	// public void enableAcelerometer(Bundle savedInstanceState) {
 	// try {
 	// log.debug("enable accelerometer");
 	// // mSensorManager = (SensorManager)
 	// // getSystemService(Context.SENSOR_SERVICE);
 	// // mSensorManager.registerListener(mSensorListener, mSensorManager
 	// // .getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
 	// // SensorManager.SENSOR_DELAY_UI);
 	// } catch (Exception e) {
 	// log.error("", e);
 	// }
 	// }
 
 	// private final SensorEventListener mSensorListener = new
 	// SensorEventListener() {
 	//
 	// public void onSensorChanged(SensorEvent event) {
 	// if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
 	// double forceThreshHold = 4.0f;
 	// double totalForce = 0.0f;
 	// totalForce += Math.pow(event.values[SensorManager.DATA_X]
 	// / SensorManager.GRAVITY_EARTH, 2.0);
 	// totalForce += Math.pow(event.values[SensorManager.DATA_Y]
 	// / SensorManager.GRAVITY_EARTH, 2.0);
 	// totalForce += Math.pow(event.values[SensorManager.DATA_Z]
 	// / SensorManager.GRAVITY_EARTH, 2.0);
 	// totalForce = Math.sqrt(totalForce);
 	// if (totalForce > 1.5 && totalForce < forceThreshHold) {
 	// recenterOnGPS = !recenterOnGPS;
 	// if (mMyLocationOverlay.mLocation != null && recenterOnGPS) {
 	// osmap.setZoomLevel(15);
 	// osmap.setMapCenter(mMyLocationOverlay.mLocation);
 	// }
 	// }
 	// }
 	// }
 	//
 	// public void onAccuracyChanged(Sensor sensor, int accuracy) {
 	// }
 	// };
 
 	@Override
 	protected void onResume() {
 		try {
 			log.debug("onResume");
 			super.onResume();
 			if (navigation && recenterOnGPS)
 				wl.acquire();
 			// mSensorManager.registerListener(mSensorListener, mSensorManager
 			// .getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
 			// SensorManager.SENSOR_DELAY_FASTEST);
 		} catch (Exception e) {
 			log.error("onResume: ", e);
 		}
 	}
 
 	@Override
 	public boolean onTrackballEvent(MotionEvent event) {
 		try {
 			return this.osmap.onTrackballEvent(event);
 		} catch (Exception e) {
 			log.error("onTrackBallEvent: ", e);
 			return false;
 		}
 	}
 
 	@Override
 	protected void onStop() {
 		try {
 			// mSensorManager.unregisterListener(mSensorListener);
 			super.onStop();
 		} catch (Exception e) {
 			log.error("onStop: ", e);
 		}
 	}
 
 	@Override
 	public void downloadCanceled() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void finishDownload() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void incrementProgressBar(int progress) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void numTilesRetrieved(int totalNumTiles) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void startDownload() {
 		cacheCounter = 0;
 
 	}
 
 	@Override
 	public void tileDownloaded(String URL) {
 		cacheCounter++;
 		log.debug("tileDownloaded: " + URL);
 		// this.reportView.setText("Tiles downloaded: " + cacheCounter);
 
 	}
 
 	@Override
 	public void onAccuracyChanged(Sensor sensor, int accuracy) {
 		// TODO Auto-generated method stub
 
 	}
 
 	private long last = 0;
 
 	@Override
 	public void onSensorChanged(SensorEvent event) {
 		try {
 			this.osmap.mBearing = (int) event.values[0];
 			long current = System.currentTimeMillis();
 			if (!navigation) {
 				osmap.postInvalidate();
 			} else {
 				if (current - last > Utils.REPAINT_TIME) {
 					osmap.postInvalidate();
 					last = current;
 				}
 			}
 		} catch (Exception e) {
 			log.error("onSensorChanged: ", e);
 		}
 	}
 
 	/**
 	 * Instantiates a CircularRouleteView @see Contextable, ItemContext
 	 * 
 	 * @param context
 	 *            The ItemContext that contains the IDs of the Drawables to
 	 *            instantiate buttons and associate Functionalities to it
 	 */
 	public void showContext(ItemContext context) {
 		try {
 			log.debug("show context");
 			LayoutInflater factory = LayoutInflater.from(this);
 			final int[] viewsID = context.getViewsId();
 			final int size = viewsID.length;
 
 			HashMap h = context.getFunctionalities();
 
 			int id;
 			View view;
 			Functionality func;
 			c = new CircularRouleteView(this, true);
 
 			for (int i = 0; i < size; i++) {
 				try {
 					id = viewsID[i];
 					view = (View) factory.inflate(id, null);
 					func = (Functionality) h.get(id);
 					if (func != null)
 						view.setOnClickListener(func);
 					c.addView(view);
 				} catch (Exception e) {
 					log.error("show context", e);
 
 				}
 			}
 
 			c.setVisibility(View.VISIBLE);
 			final int count = rl.getChildCount();
 			View v;
 			for (int i = 0; i < count; i++) {
 				v = rl.getChildAt(i);
 				if (v instanceof CircularRouleteView) {
 					rl.removeView(v);
 				}
 			}
 			rl.addView(c);
 
 			// Change user context to store that the CircularRouleteView has
 			// been
 			// shown once at least
 			userContext.setUsedCircleMenu(true);
 			userContext.setLastExecCircle();
 
 		} catch (Exception e) {
 			log.error("showContext: ", e);
 		}
 	}
 
 	/**
 	 * Removes the current CircularRouleteView from the RouleteLayout
 	 */
 	public void clearContext() {
 		try {
 			final int size = c.getChildCount();
 
 			View child = null;
 			for (int i = 0; i < size; i++) {
 				try {
 					child = c.getChildAt(i);
 					if (child != null)
 						child.getBackground().setCallback(null);
 					child = null;
 				} catch (Exception e) {
 
 				}
 			}
 
 			rl.removeView(c);
 		} catch (Exception e) {
 			log.error("clearContext: ", e);
 		}
 	}
 
 	/**
 	 * @see MapHandler
 	 * @return The MapHandler
 	 */
 	public Handler getMapHandler() {
 		return handler;
 	}
 
 	/**
 	 * Sets the current ItemContext of the application
 	 * 
 	 * @param context
 	 */
 	public void setContext(ItemContext context) {
 		try {
 			if (context == null) {
 				this.setContext(new DefaultContext(this));
 				log.debug("setContext: " + "DefaultContext");
 			} else {
 				try {
 					Functionality f = this.getItemContext()
 							.getExecutingFunctionality();
 					if (f != null)
 						context.setExecutingFunctionality(f);
 				} catch (Exception e) {
 					log.error("setContext", e);
 
 				}
 				this.context = null;
 				this.context = context;
 
 				log.debug("setContext: " + context.getClass().getName());
 			}
 		} catch (Exception e) {
 			log.error("setContext: ", e);
 		}
 	}
 
 	/**
 	 * Gets the current ItemContext of the application
 	 * 
 	 * @return
 	 */
 	public ItemContext getItemContext() {
 		return context;
 	}
 
 	/**
 	 * Shows an AlertDialog to the user to input his/her twitter account
 	 * credentials
 	 */
 	public void showTweetDialog() {
 		try {
 			log.debug("showTweetDialog");
 			LayoutInflater factory = LayoutInflater.from(this);
 			final View textEntryView = factory.inflate(
 					R.layout.alert_dialog_text_entry, null);
 			AlertDialog.Builder alertTweet = new AlertDialog.Builder(this);
 			alertTweet.setView(textEntryView).setIcon(R.drawable.menu04)
 					.setTitle(R.string.alert_dialog_text_entry)
 					.setPositiveButton(R.string.alert_dialog_tweet,
 							new DialogInterface.OnClickListener() {
 								public void onClick(DialogInterface dialog,
 										int whichButton) {
 									try {
 
 										EditText etrUserName = (EditText) textEntryView
 												.findViewById(R.id.username_edit);
 										String userName = etrUserName.getText()
 												.toString();
 										EditText etrUserPass = (EditText) textEntryView
 												.findViewById(R.id.password_edit);
 										String userPass = etrUserPass.getText()
 												.toString();
 
 										Map.twituser = userName;
 										Map.twitpass = userPass;
 										getItemContext().getFunctionalityByID(
 												R.layout.twitter_image_button)
 												.onClick(null);
 									} catch (Exception e) {
 										log.error("twitter: ", e);
 									}
 								}
 							}).setNegativeButton(R.string.alert_dialog_cancel,
 							new DialogInterface.OnClickListener() {
 								public void onClick(DialogInterface dialog,
 										int whichButton) {
 								}
 							}).create();
 			alertTweet.show();
 			userContext.setUsedTwitter(true);
 			userContext.setLastExecTwitter();
 		} catch (Exception e) {
 			log.error("showTweetDialog: ", e);
 		}
 	}
 	
 	public void setLayerAlpha(int alpha) {
 		osmap.setAlpha(alpha);
 		osmap.postInvalidate();
 	}
 
 	private int state = Map.VOID;
 
 	/**
 	 * This method applies some logic to update the ItemContext from the current
 	 * Map state Map.VOID: No route and pois calculated Map.ROUTE_SUCCEEDED:
 	 * Route calculated Map.POI_SUCCEEDED: POI calculated Map.POI_CLEARED: POI
 	 * has been cleared Map.ROUTE_CLEARED: Route has been cleared
 	 * 
 	 * @param state
 	 */
 	public void updateContext(int state) {
 		try {
 			log.debug("updateContext");
 			switch (state) {
 			case Map.VOID:
 				log.debug("VOID");
 				this.setContext(new DefaultContext(this));
 				break;
 			case Map.ROUTE_SUCCEEDED:
 				log.debug("ROUTE_SUCEEDED");
 				if (nameds != null && nameds.getNumPoints() > 0)
 					this.setContext(new RoutePOIContext(this));
 				else
 					this.setContext(new RouteContext(this));
 				break;
 			case Map.POI_SUCCEEDED:
 				log.debug("POI_SUCCEEDED");
 				if (route != null
 						&& route.getState() == Tags.ROUTE_WITH_2_POINT) {
 					this.setContext(new RoutePOIContext(this));
 				} else {
 					this.setContext(new POIContext(this));
 				}
 				break;
 			case Map.POI_CLEARED:
 				log.debug("POI_CLEARED");
 				if (route != null
 						&& route.getState() == Tags.ROUTE_WITH_2_POINT) {
 					this.setContext(new RouteContext(this));
 				} else {
 					this.setContext(new DefaultContext(this));
 				}
 				break;
 			case Map.ROUTE_CLEARED:
 				log.debug("ROUTE_CLEARED");
 				if (nameds != null && nameds.getNumPoints() > 0) {
 					this.setContext(new POIContext(this));
 				} else {
 					this.setContext(new DefaultContext(this));
 				}
 				break;
 			}
 		} catch (Exception e) {
 			log.error("updateContext: ", e);
 		}
 	}
 
 	@Override
 	public void onConfigurationChanged(Configuration config) {
 		try {
 			log.debug("onConfigurationChanged");
 			if (config.orientation == Configuration.ORIENTATION_PORTRAIT) {
 				this.mMyLocationOverlay
 						.setOffsetOrientation(ViewSimpleLocationOverlay.PORTRAIT_OFFSET_ORIENTATION);
 			} else if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
 				this.mMyLocationOverlay
 						.setOffsetOrientation(ViewSimpleLocationOverlay.LANDSCAPE_OFFSET_ORIENTATION);
 			}
 			super.onConfigurationChanged(config);
 		} catch (Exception e) {
 			log.error("onConfigurationChanged: ", e);
 		}
 	}
 
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		try {
 			if (keyCode == KeyEvent.KEYCODE_BACK) {
 				log.debug("KEY BACK pressed");
 				backpressed = true;
 				this.onDestroy();
 				finish();
 				// TODO: Hacer esto bien, liberando recursos y cerrando la
 				// aplicaci�n
 				android.os.Process.killProcess(android.os.Process.myPid());
 				// return false;
 			}
 			return super.onKeyDown(keyCode, event);
 		} catch (Exception e) {
 			log.error("onKeyDown: ", e);
 			return false;
 		}
 		// return false;
 
 	}
 
 	@Override
 	public void onLowMemory() {
 		try {
 			log.debug("onLowMemory");
 			osmap.getMTileProvider().onLowMemory();
 			super.onLowMemory();
 			Toast.makeText(this, R.string.Map_25, Toast.LENGTH_LONG).show();
 		} catch (Exception e) {
 			log.error("onLowMemory: ", e);
 		}
 	}
 
 	@Override
 	public void onProviderDisabled(String provider) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onProviderEnabled(String provider) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onStatusChanged(String provider, int status, Bundle extras) {
 		GPS_STATUS = status;
 
 	}
 
 	/**
 	 * Launches GetCellLocationFunc
 	 */
 	public void obtainCellLocation() {
 		try {
 			GetCellLocationFunc cellLocationFunc = new GetCellLocationFunc(
 					this, 0);
 			cellLocationFunc.launch();
 		} catch (Exception e) {
 			log.error(e);
 		}
 	}
 
 	public static int GPS_STATUS;
 
 	@Override
 	public void onPause() {
 		try {
 			log.debug("onPause");
 			super.onPause();
 			if (wl != null)
 				wl.release();
 
 		} catch (Exception e)
 
 		{
 			log.error("onPause: ", e);
 		}
 	}
 
 	@Override
 	public void onNewIntent(Intent i) {
 		try {
 			if (i == null) {
 				return;
 			}
 
 			try {
 				String mapLayer = i.getStringExtra("layer");
 				log.debug("previous layer: " + mapLayer);
 				if (mapLayer != null) {
 					osmap.onLayerChanged(mapLayer);
 					log.debug("map loaded");
 				}
 			} catch (Exception e) {
 				log.error("onNewIntent", e);
 
 			}
 
 			String actionName = i.getAction();
 			if (actionName != null
 					&& actionName.equals("android.intent.action.SEND")) {
 				TweetMyLocationFunc t = new TweetMyLocationFunc(this, 0);
 				t.launch();
 			}
 		} catch (Exception e) {
 			log.error("onNewIntent", e);
 		}
 	}
 
 	/**
 	 * persists the MapState
 	 */
 	public void persist() {
 		try {
 			log.debug("map persist");
 			mapState.persist();
 		} catch (Exception e) {
 			log.error("persist", e);
 		}
 	}
 
 	public ViewSimpleLocationOverlay getmMyLocationOverlay() {
 		return mMyLocationOverlay;
 	}
 
 	public void setmMyLocationOverlay(
 			ViewSimpleLocationOverlay mMyLocationOverlay) {
 		this.mMyLocationOverlay = mMyLocationOverlay;
 	}
 
 	/**
 	 * Sets the TileRaster center on the GPS location
 	 */
 	public void centerOnGPSLocation() {
 		try {
 			final GPSPoint location = this.mMyLocationOverlay.mLocation;
 			if (location != null) {
 				this.osmap.setMapCenter(location);
 			}
 		} catch (Exception e) {
 			log.error(e);
 		}
 	}
 
 	/**
 	 * starts the LocationHandler
 	 */
 	@Override
 	public void enableGPS() {
 		log.debug("enableGPS");
 		super.initLocation();
 		boolean enabled = this.isLocationHandlerEnabled();
 		try {
 			this.myLocationButton.setEnabled(enabled);
 			this.myNavigator.setEnabled(enabled);
 			this.myGPSButton.setTitle(R.string.Map_26);
 		} catch (Exception e) {
 			log.error(e);
 		}
 	}
 
 	/**
 	 * Stops the location handler
 	 */
 	public void disableGPS() {
 		log.debug("disableGPS");
 		super.disableGPS();
 		boolean enabled = this.isLocationHandlerEnabled();
 		try {
 			this.myLocationButton.setEnabled(enabled);
 			this.myNavigator.setEnabled(enabled);
 			this.myGPSButton.setTitle(R.string.Map_27);
 		} catch (Exception e) {
 			log.error(e);
 		}
 	}
 }
