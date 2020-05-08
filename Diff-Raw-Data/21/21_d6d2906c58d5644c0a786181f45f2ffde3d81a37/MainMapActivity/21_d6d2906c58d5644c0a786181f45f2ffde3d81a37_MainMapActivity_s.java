 //TODO Direction http://maps.google.com/maps/nav?key=ABQIAAAAzr2EBOXUKnm_jVnk0OJI7xSosDVG8KKPE1-m51RBrvYughuyMxQ-i1QfUnH94QxWIa6N4U6MouMmBA&output=js&dirflg=d&hl=en&mapclient=jsapi&q=from:%2065.366837,26.71875%20to:%2065.50,26.80
 // from:%2065.366837,26.71875%20to:%2065.50,26.80
 // http://maps.google.com/maps?saddr=65.366837,26.71875&daddr=65.50,26.80&output=kml
 package biz.navius.saltroad;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.andnav.osm.OpenStreetMapActivity;
 import org.andnav.osm.util.GeoPoint;
 import org.andnav.osm.util.TypeConverter;
 import org.andnav.osm.util.constants.OpenStreetMapConstants;
 import org.andnav.osm.views.OpenStreetMapView;
 import org.andnav.osm.views.controller.OpenStreetMapViewController;
 import org.andnav.osm.views.util.OpenStreetMapRendererInfo;
 import org.andnav.osm.views.util.OpenStreetMapTileFilesystemProvider;
 import org.andnav.osm.views.util.StreamUtils;
 import org.andnav.osm.views.util.constants.OpenStreetMapViewConstants;
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.app.SearchManager;
 import android.content.ActivityNotFoundException;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.res.Resources;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.location.Location;
 import android.location.LocationManager;
 import android.net.Uri;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.os.PowerManager;
 import android.preference.PreferenceManager;
 import android.provider.Browser;
 import android.provider.SearchRecentSuggestions;
 import android.view.ContextMenu;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.Window;
 import android.view.WindowManager;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.View.OnClickListener;
 import android.view.View.OnLongClickListener;
 import android.widget.ImageView;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.RelativeLayout.LayoutParams;
 
 import biz.navius.saltroad.kml.DefaultPoiPoint;
 import biz.navius.saltroad.kml.PoiActivity;
 import biz.navius.saltroad.kml.PoiListActivity;
 import biz.navius.saltroad.kml.PoiManager;
 import biz.navius.saltroad.kml.PoiPoint;
 import biz.navius.saltroad.kml.Track;
 import biz.navius.saltroad.kml.TrackListActivity;
 import biz.navius.saltroad.kml.XMLparser.PredefMapsParser;
 import biz.navius.saltroad.overlays.CurrentTrackOverlay;
 import biz.navius.saltroad.overlays.DefaultPoiOverlay;
 import biz.navius.saltroad.overlays.DefaultTrackOverlay;
 import biz.navius.saltroad.overlays.MyLocationOverlay;
 import biz.navius.saltroad.overlays.PoiOverlay;
 import biz.navius.saltroad.overlays.SearchResultOverlay;
 import biz.navius.saltroad.overlays.TrackOverlay;
 import biz.navius.saltroad.overlays.YandexTrafficOverlay;
 import biz.navius.saltroad.utils.CompassView;
 import biz.navius.saltroad.utils.CrashReportHandler;
 import biz.navius.saltroad.utils.DownloadDefaultPoiImage;
 import biz.navius.saltroad.utils.ScaleBarDrawable;
 import biz.navius.saltroad.utils.SearchSuggestionsProvider;
 import biz.navius.saltroad.utils.Ut;
 
 import biz.navius.saltroad.constants.MapConstants;
 import biz.navius.saltroad.copysdcard.CopyDefaultPoiDBFileToSDCardHandler;
 import biz.navius.saltroad.copysdcard.CopyDefaultPoiDBFileToSDCardThreadRunnable;
 import biz.navius.saltroad.copysdcard.CopyTrackFileToSDCardHandler;
 import biz.navius.saltroad.copysdcard.CopyTrackFileToSDCardThreadRunnable;
 import biz.navius.saltroad.defaultpoi.DecompressDefaultPoiImageFileHandler;
 import biz.navius.saltroad.defaultpoi.DecompressDefaultPoiImageFileThreadRunnable;
 import biz.navius.saltroad.defaultpoi.DefaultPoiDetailActivity;
 
 public class MainMapActivity extends OpenStreetMapActivity implements OpenStreetMapConstants {
 	// ===========================================================
 	// Constants
 	// ===========================================================
 
 	// ===========================================================
 	// Fields
 	// ===========================================================
 
 	private OpenStreetMapView mOsmv; //, mOsmvMinimap;
 	private MyLocationOverlay mMyLocationOverlay;
 	private PoiOverlay mPoiOverlay;
 	private CurrentTrackOverlay mCurrentTrackOverlay;
 	private TrackOverlay mTrackOverlay;
 	private DefaultTrackOverlay mDefaultTrackOverlay;
 	private DefaultPoiOverlay mDefaultPoiOverlay;
 	
 	private SearchResultOverlay mSearchResultOverlay;
 
 	private PowerManager.WakeLock myWakeLock;
 	private boolean mFullScreen;
 	private boolean mShowTitle;
 	private String mStatusListener = "";
 	private boolean mAutoFollow = true;
 	private Handler mCallbackHandler = new MainActivityCallbackHandler();
 	private ImageView ivAutoFollow;
 	private ImageView ivDefaultPoi;
 	private CompassView mCompassView;
 	private SensorManager mOrientationSensorManager;
 	private boolean mCompassEnabled;
 	private boolean mDrivingDirectionUp;
 	private boolean mNorthDirectionUp;
 	private int mScreenOrientation;
 	private float mLastSpeed, mLastBearing;
 	private PoiManager mPoiManager;
 	private String ACTION_SHOW_POINTS = "biz.navius.saltroad.action.SHOW_POINTS";
 	private int mMarkerIndex;
 
 	private ProgressDialog mTrackDlgWait;
 	private ProgressDialog mDefaultPoiDBDlgWait;
 	private ProgressDialog mDecompressDefaultPoiImageFileDlgWait;
 	private Handler mCopyTrackFileToSDCardHandler = null;
 	private Thread mCopyTrackFileToSDCardThreadRunnable = null;
 	private Handler mCopyDefaultPoiDBFileToSDCardHandler = null;
 	private Thread mCopyDefaultPoiDBFileToSDCardThreadRunnable = null;
 	private Handler mDecompressDefaultPoiImageFileHandler = null;
 	private Thread mDecompressDefaultPoiImageFileThreadRunnable = null;
 
 
     private final SensorEventListener mListener = new SensorEventListener() {
 		private int iOrientation = -1;
 
 		public void onAccuracyChanged(Sensor sensor, int accuracy) {
 
 		}
 
 		public void onSensorChanged(SensorEvent event) {
 			if (iOrientation < 0)
 				iOrientation = ((WindowManager) MainMapActivity.this.getSystemService(Context.WINDOW_SERVICE))
 						.getDefaultDisplay().getOrientation();
 
 			mCompassView.setAzimuth(event.values[0] + 90 * iOrientation);
 			mCompassView.invalidate();
 
 			if (mCompassEnabled)
 				if (mNorthDirectionUp)
 					if (mDrivingDirectionUp == false || mLastSpeed == 0) {
 						mOsmv.setBearing(updateBearing(event.values[0]) + 90 * iOrientation);
 						mOsmv.invalidate();
 					}
 		}
 
 	};
 
 	private float updateBearing(float newBearing) {
 		float dif = newBearing - mLastBearing;
 		// find difference between new and current position
 		if (Math.abs(dif) > 180)
 			dif = 360 - dif;
 		// if difference is bigger than 180 degrees,
 		// it's faster to rotate in opposite direction
 		if (Math.abs(dif) < 1)
 			return mLastBearing;
 		// if difference is less than 1 degree, leave things as is
 		if (Math.abs(dif) >= 90)
 			return mLastBearing = newBearing;
 		// if difference is bigger than 90 degress, just update it
 		mLastBearing += 90 * Math.signum(dif) * Math.pow(Math.abs(dif) / 90, 2);
 		// bearing is updated proportionally to the square of the difference
 		// value
 		// sign of difference is paid into account
 		// if difference is 90(max. possible) it is updated exactly by 90
 		while (mLastBearing > 360)
 			mLastBearing -= 360;
 		while (mLastBearing < 0)
 			mLastBearing += 360;
 		// prevent bearing overrun/underrun
 		return mLastBearing;
 	}
 
 	// ===========================================================
 	// Constructors
 	// ===========================================================
 
 	/** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
     	
     	if(OpenStreetMapViewConstants.DEBUGMODE)
     		android.os.Debug.startMethodTracing("lsd");
         super.onCreate(savedInstanceState, false); // Pass true here to actually contribute to OSM!
 
         if(!OpenStreetMapViewConstants.DEBUGMODE)
         	CrashReportHandler.attach(this);
 
         CheckNeedDataUpdate();
 
         mPoiManager = new PoiManager(this);
         mOrientationSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        	final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
 
  		final RelativeLayout rl = new RelativeLayout(this);
         OpenStreetMapRendererInfo RendererInfo = new OpenStreetMapRendererInfo(getResources(), "");
 
         this.mOsmv = new OpenStreetMapView(this, RendererInfo);
         this.mOsmv.setMainActivityCallbackHandler(mCallbackHandler);
         rl.addView(this.mOsmv, new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
         registerForContextMenu(mOsmv);
 
         /* SingleLocation-Overlay */
         {
 	        /* Create a static Overlay showing a single location. (Gets updated in onLocationChanged(Location loc)! */
         	if(RendererInfo.YANDEX_TRAFFIC_ON == 1)
         		this.mOsmv.getOverlays().add(new YandexTrafficOverlay(this, this.mOsmv));
 
 	        this.mMyLocationOverlay = new MyLocationOverlay(this);
 	        this.mOsmv.getOverlays().add(mMyLocationOverlay);
 
 	        this.mSearchResultOverlay = new SearchResultOverlay(this);
 	        this.mOsmv.getOverlays().add(mSearchResultOverlay);
         }
 
         /* Itemized Overlay */
 		{
 			this.mDefaultTrackOverlay = new DefaultTrackOverlay(this, mPoiManager);
 			this.mOsmv.getOverlays().add(this.mDefaultTrackOverlay);
 
 			this.mTrackOverlay = new TrackOverlay(this, mPoiManager);
 			this.mOsmv.getOverlays().add(this.mTrackOverlay);
 
 			this.mCurrentTrackOverlay = new CurrentTrackOverlay(this, mPoiManager, mOsmv);
 			this.mOsmv.getOverlays().add(this.mCurrentTrackOverlay);
 
 			this.mDefaultPoiOverlay = new DefaultPoiOverlay(this, null, pref.getBoolean("pref_hidepoi", false));
 			this.mOsmv.getOverlays().add(this.mDefaultPoiOverlay);
 
 			this.mPoiOverlay = new PoiOverlay(this, mPoiManager, null, pref.getBoolean("pref_hidepoi", false));
 			this.mOsmv.getOverlays().add(this.mPoiOverlay);
 		}
 
         {
         	final int sideBottom = Integer.parseInt(pref.getString("pref_zoomctrl", "1"));
 
             /* Compass */
         	mCompassView = new CompassView(this, sideBottom == 2 ? false : true);
 	        mCompassView.setVisibility(mCompassEnabled ? View.VISIBLE : View.INVISIBLE);
 	        /* Create RelativeLayoutParams, that position in in the top right corner. */
 	        final RelativeLayout.LayoutParams compassParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
 	        compassParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
 	        compassParams.addRule(!(sideBottom == 2 ? false : true) ? RelativeLayout.ALIGN_PARENT_BOTTOM : RelativeLayout.ALIGN_PARENT_TOP);
 	        rl.addView(mCompassView, compassParams);
 
             /* AutoFollow */
 	        ivAutoFollow = new ImageView(this);
 	        ivAutoFollow.setImageResource(R.drawable.autofollow);
 	        ivAutoFollow.setVisibility(ImageView.INVISIBLE);
 	        /* Create RelativeLayoutParams, that position in in the top right corner. */
 	        final RelativeLayout.LayoutParams followParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
 	        followParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
 	        followParams.addRule(!(sideBottom == 2 ? false : true) ? RelativeLayout.ALIGN_PARENT_BOTTOM : RelativeLayout.ALIGN_PARENT_TOP);
 	        rl.addView(ivAutoFollow, followParams);
 
 	        ivAutoFollow.setOnClickListener(new OnClickListener(){
 				// @Override
 				public void onClick(View v) {
 					setAutoFollow(true);
 					mSearchResultOverlay.Clear();
 					setLastKnownLocation();
 				}
 	        });
 
 	        /* ZoomControls */
 	        if(sideBottom > 0){
 		        /* Create a ImageView with a zoomIn-Icon. */
 		        final ImageView ivZoomIn = new ImageView(this);
 		        ivZoomIn.setImageResource(R.drawable.zoom_in);
 		        /* Create RelativeLayoutParams, that position in in the top right corner. */
 		        final RelativeLayout.LayoutParams zoominParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
 		        zoominParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
 		        zoominParams.addRule((sideBottom == 2 ? false : true) ? RelativeLayout.ALIGN_PARENT_BOTTOM : RelativeLayout.ALIGN_PARENT_TOP);
 		        rl.addView(ivZoomIn, zoominParams);
 
 		        ivZoomIn.setOnClickListener(new OnClickListener(){
 					// @Override
 					public void onClick(View v) {
 						MainMapActivity.this.mOsmv.zoomIn();
 						setTitle();
 
 						if(MainMapActivity.this.mOsmv.getZoomLevel() > 16 && MainMapActivity.this.mOsmv.getRenderer().YANDEX_TRAFFIC_ON == 1)
 							Toast.makeText(MainMapActivity.this, R.string.no_traffic, Toast.LENGTH_SHORT).show();
 					}
 		        });
 		        ivZoomIn.setOnLongClickListener(new OnLongClickListener(){
 					// @Override
 					public boolean onLongClick(View v) {
 						SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(MainMapActivity.this);
 						final int zoom = Integer.parseInt(pref.getString("pref_zoommaxlevel", "17"));
 						if (zoom > 0) {
 							MainMapActivity.this.mOsmv.setZoomLevel(zoom - 1);
 							setTitle();
 						}
 						return true;
 					}
 		        });
 
 
 		        /* Create a ImageView with a zoomOut-Icon. */
 		        final ImageView ivZoomOut = new ImageView(this);
 		        ivZoomOut.setImageResource(R.drawable.zoom_out);
 		        ivZoomOut.setId(R.id.whatsnew);
 
 		        /* Create RelativeLayoutParams, that position in in the top left corner. */
 		        final RelativeLayout.LayoutParams zoomoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
 		        zoomoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
 		        zoomoutParams.addRule((sideBottom == 2 ? false : true) ? RelativeLayout.ALIGN_PARENT_BOTTOM : RelativeLayout.ALIGN_PARENT_TOP);
 		        rl.addView(ivZoomOut, zoomoutParams);
 
 		        ivZoomOut.setOnClickListener(new OnClickListener(){
 					// @Override
 					public void onClick(View v) {
 						MainMapActivity.this.mOsmv.zoomOut();
 						setTitle();
 					}
 		        });
 		        ivZoomOut.setOnLongClickListener(new OnLongClickListener(){
 					// @Override
 					public boolean onLongClick(View v) {
 						SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(MainMapActivity.this);
 						final int zoom = Integer.parseInt(pref.getString("pref_zoomminlevel", "10"));
 						if (zoom > 0) {
 							MainMapActivity.this.mOsmv.setZoomLevel(zoom - 1);
 							setTitle();
 						}
 						return true;
 					}
 		        });
 
 	        };
 
 	        /*ScaleBarView*/
 	        if(pref.getBoolean("pref_showscalebar", true)){
 		        final ImageView ivZoomOut2 = new ImageView(this);
 		        final ScaleBarDrawable dr = new ScaleBarDrawable(this, mOsmv, Integer.parseInt(pref.getString("pref_units", "0")));
 		        ivZoomOut2.setImageDrawable(dr);
 		        final RelativeLayout.LayoutParams scaleParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
 		        scaleParams.addRule(RelativeLayout.RIGHT_OF, R.id.whatsnew);
 		        scaleParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
 		        rl.addView(ivZoomOut2, scaleParams);
 	        };
  
 
 	        
 	        /* Create a ImageView with a default poi detail Icon. */
 	        ivDefaultPoi = new ImageView(this);
 	        ivDefaultPoi.setImageResource(R.drawable.ic_menu_info_details);
 	        /* Create RelativeLayoutParams, that position in in the top center. */
 	        final RelativeLayout.LayoutParams defaultpoiParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
 	        defaultpoiParams.addRule(RelativeLayout.CENTER_IN_PARENT);
 	        defaultpoiParams.addRule((sideBottom == 2 ? false : true) ? RelativeLayout.ALIGN_PARENT_TOP : RelativeLayout.ALIGN_PARENT_BOTTOM);
 	        rl.addView(ivDefaultPoi, defaultpoiParams);
 	        ivDefaultPoi.setVisibility(ImageView.INVISIBLE);
 
 	        ivDefaultPoi.setOnClickListener(new OnClickListener(){
 				// @Override
 				public void onClick(View v) {
 					int tapIndex = mDefaultPoiOverlay.getTapIndex();
 					DefaultPoiPoint defaultPoiPoint = null;
 					if (tapIndex >= 0)
 						defaultPoiPoint = mDefaultPoiOverlay.getDefaultPoiPoint(tapIndex);
 						startActivity((new Intent(MainMapActivity.this, DefaultPoiDetailActivity.class)).putExtra("id", defaultPoiPoint.getId()));
 				}
 	        });
         }
 
 		mDrivingDirectionUp = pref.getBoolean("pref_drivingdirectionup", true);
 		mNorthDirectionUp = pref.getBoolean("pref_northdirectionup", true);
 
 		mScreenOrientation = Integer.parseInt(pref.getString("pref_screen_orientation", "-1"));
 		this.setRequestedOrientation(mScreenOrientation);
 
      	mFullScreen = pref.getBoolean("pref_showstatusbar", true);
 		if (mFullScreen)
 			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
 					WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
 		else
 			getWindow()
 					.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
 
 		mShowTitle = pref.getBoolean("pref_showtitle", true);
 		if (mShowTitle)
 	        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
 		else
 			requestWindowFeature(Window.FEATURE_NO_TITLE);
 
         this.setContentView(rl);
         
         if(mShowTitle)
 	        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.main_title);
 
 		restoreUIState();
 
         final Intent queryIntent = getIntent();
         final String queryAction = queryIntent.getAction();
 
         if (Intent.ACTION_SEARCH.equals(queryAction)) {
             doSearchQuery(queryIntent);
         }else if(ACTION_SHOW_POINTS.equalsIgnoreCase(queryAction))
         	ActionShowPoints(queryIntent);
 
         if(OpenStreetMapViewConstants.DEBUGMODE)
         	android.os.Debug.stopMethodTracing();
 
     }
     
 	@Override
 	protected void onDestroy() {
 		try {
 			mOsmv.freeDatabases();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		try {
 			mPoiManager.FreeDatabases();
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		super.onDestroy();
 	}
 
 	private void ActionShowPoints(Intent queryIntent) {
 		final ArrayList<String> locations = queryIntent.getStringArrayListExtra("locations");
 		if(!locations.isEmpty()){
 			Ut.dd("Intent: "+ACTION_SHOW_POINTS+" locations: "+locations.toString());
 			String [] fields = locations.get(0).split(";");
 			String locns = "", title = "", descr = "";
 			if(fields.length>0) locns = fields[0];
 			if(fields.length>1) title = fields[1];
 			if(fields.length>2) descr = fields[2];
 
 			GeoPoint point = GeoPoint.fromDoubleString(locns);
 			mPoiOverlay.setGpsStatusGeoPoint(point, title, descr);
 			setAutoFollow(false);
 			mOsmv.setMapCenter(point);
 		}
 	}
 
 	private void CheckNeedDataUpdate() {
 		SharedPreferences settings = getPreferences(Activity.MODE_PRIVATE);
 		final int versionDataUpdate = settings.getInt("versionDataUpdate", 0);
 		Ut.dd("versionDataUpdate="+versionDataUpdate);
 
 		if(versionDataUpdate < 3){
 			Ut.dd("Upgrade app data to v.3");
 		}
 
 		SharedPreferences uiState = getPreferences(0);
 		SharedPreferences.Editor editor = uiState.edit();
 		editor.putInt("versionDataUpdate", 3);
 		editor.commit();
 	}
 
 	private void CheckNeedDefaultDataUpdate() {
 		SharedPreferences settings = getPreferences(Activity.MODE_PRIVATE);
 		final int versionDataUpdate = settings.getInt("versionDefaultDataUpdate", 0);
 		Ut.dd("versionDefaultDataUpdate="+versionDataUpdate);
 
 		if(versionDataUpdate < 1){
 			Ut.dd("Upgrade app default data to v.1");
 			SharedPreferences.Editor editor1 = settings.edit();
 	    	editor1.putBoolean("copy_track_to_sdcard_success", false);
 	    	editor1.commit();
 		}
 
 		SharedPreferences uiState = getPreferences(0);
 		SharedPreferences.Editor editor = uiState.edit();
 		editor.putInt("versionDefaultDataUpdate", 1);
 		editor.commit();
 	}
 	
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		switch(item.getItemId()){
 		case R.id.menu_addpoi:
 			GeoPoint point = this.mOsmv.getTouchDownPoint();
 			startActivityForResult((new Intent(this, PoiActivity.class))
 					.putExtra("lat", point.getLatitude()).putExtra("lon", point.getLongitude()).putExtra("title", "POI"), R.id.menu_addpoi);
 			break;
 		case R.id.menu_editpoi:
 			startActivityForResult((new Intent(this, PoiActivity.class)).putExtra("pointid", mPoiOverlay.getPoiPoint(mMarkerIndex).getId()), R.id.menu_editpoi);
 			mOsmv.invalidate();
 			break;
 		case R.id.menu_deletepoi:
 			mPoiManager.deletePoi(mPoiOverlay.getPoiPoint(mMarkerIndex).getId());
 			mPoiOverlay.UpdateList();
 			mOsmv.invalidate();
 			break;
 		case R.id.menu_hide:
 			final PoiPoint poi = mPoiOverlay.getPoiPoint(mMarkerIndex);
 			poi.Hidden = true;
 			mPoiManager.updatePoi(poi);
 			mPoiOverlay.UpdateList();
 			mOsmv.invalidate();
 			break;
 		case R.id.menu_toradar:
 			final PoiPoint poi1 = mPoiOverlay.getPoiPoint(mMarkerIndex);
 			try {
 					Intent i = new Intent("com.google.android.radar.SHOW_RADAR");
 					i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
 					i.putExtra("name", poi1.Title);
 					i.putExtra("latitude",  (float)(poi1.GeoPoint.getLatitudeE6() / 1000000f));
 					i.putExtra("longitude", (float)(poi1.GeoPoint.getLongitudeE6() / 1000000f));
 					startActivity(i);
 				} catch (Exception e) {
 					Toast.makeText(this, R.string.message_noradar, Toast.LENGTH_LONG).show();
 				}
 			break;
 		}
 
 		return super.onContextItemSelected(item);
 	}
 
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
 		if(mOsmv.canCreateContextMenu()){
 			mMarkerIndex = mPoiOverlay.getMarkerAtPoint(mOsmv.mTouchDownX, mOsmv.mTouchDownY, mOsmv);
 			if(mMarkerIndex >= 0){
 				menu.add(0, R.id.menu_editpoi, 0, getText(R.string.menu_edit));
 				menu.add(0, R.id.menu_hide, 0, getText(R.string.menu_hide));
 				menu.add(0, R.id.menu_deletepoi, 0, getText(R.string.menu_delete));
 				menu.add(0, R.id.menu_toradar, 0, getText(R.string.menu_toradar));
 			} else {
 				menu.add(0, R.id.menu_addpoi, 0, getText(R.string.menu_addpoi));
 			}
 		}
 
 		super.onCreateContextMenu(menu, v, menuInfo);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);
 
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.main_option_menu, menu);
 
 		return true;
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		super.onOptionsItemSelected(item);
 
 		switch (item.getItemId()) {
 		case (R.id.gpsstatus):
 			try {
 				startActivity(new Intent("com.eclipsim.gpsstatus.VIEW"));
 			} catch (ActivityNotFoundException e) {
 				Toast.makeText(this,
 						R.string.message_nogpsstatus,
 						Toast.LENGTH_LONG).show();
 				try {
 					startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri
 							.parse("market://search?q=pname:com.eclipsim.gpsstatus2")));
 				} catch (Exception e1) {
 					e1.printStackTrace();
 				}
 			}
 			return true;
 		case (R.id.poilist):
 			final GeoPoint point = mOsmv.getMapCenter();
 			startActivityForResult((new Intent(this, PoiListActivity.class)).putExtra("lat", point.getLatitude()).putExtra("lon", point.getLongitude()).putExtra("title", "POI"), R.id.poilist);
 			return true;
 		case (R.id.tracks):
 			startActivityForResult(new Intent(this, TrackListActivity.class), R.id.tracks);
 			return true;
 		case (R.id.search):
 			onSearchRequested();
 			return true;
 		case (R.id.settings):
 			setDefaultMapTrack();
 			startActivityForResult(new Intent(this, MainPreferences.class), R.id.settings_activity_closed);
 			return true;
 		case (R.id.about):
 			showDialog(R.id.about);
 			return true;
 		case (R.id.mapselector):
 			return true;
 		case (R.id.compass):
 			mCompassEnabled = !mCompassEnabled;
 			mCompassView.setVisibility(mCompassEnabled ? View.VISIBLE : View.INVISIBLE);
 			if(mCompassEnabled)
 				mOrientationSensorManager.registerListener(mListener, mOrientationSensorManager
 					.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_UI);
 			else
 				mOrientationSensorManager.unregisterListener(mListener);
 			return true;
 		case (R.id.mylocation):
 			setAutoFollow(true);
 			setLastKnownLocation();
 			return true;
 		case (R.id.default_location):
 			setAutoFollow(false);
 			setDefaultLocation();
 			return true;
 		default:
  			OpenStreetMapRendererInfo RendererInfo = getRendererInfo(getResources(), getPreferences(Activity.MODE_PRIVATE), (String)item.getTitleCondensed());
  			Ut.dd((String)item.getTitleCondensed());
 			mOsmv.setRenderer(RendererInfo);
 
 			this.mOsmv.getOverlays().clear();
 			if(RendererInfo.YANDEX_TRAFFIC_ON == 1){
 	       		this.mOsmv.getOverlays().add(new YandexTrafficOverlay(this, this.mOsmv));
 			}
 	        if(mDefaultTrackOverlay != null)
 	        	this.mOsmv.getOverlays().add(mDefaultTrackOverlay);
 	        if(mTrackOverlay != null)
 	        	this.mOsmv.getOverlays().add(mTrackOverlay);
 	        if(mCurrentTrackOverlay != null)
 	        	this.mOsmv.getOverlays().add(mCurrentTrackOverlay);
 	        if(mDefaultPoiOverlay != null)
 	        	this.mOsmv.getOverlays().add(mDefaultPoiOverlay);
 	        if(mPoiOverlay != null)
 	        	this.mOsmv.getOverlays().add(mPoiOverlay);
 	        this.mOsmv.getOverlays().add(mMyLocationOverlay);
 	        this.mOsmv.getOverlays().add(mSearchResultOverlay);
 
 	        setTitle();
 
 			return true;
 		}
 
 	}
 
 	@Override
 	protected Dialog onCreateDialog(int id) {
 		switch (id) {
 		case R.id.add_yandex_bookmark:
 			return new AlertDialog.Builder(this)
 				.setTitle(R.string.ya_dialog_title)
 				.setMessage(R.string.ya_dialog_message)
 				.setPositiveButton(R.string.ya_dialog_button_caption, new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog, int whichButton) {
 							Browser.saveBookmark(MainMapActivity.this, "��������� ������", "m.yandex.ru");
 						}
 				}).create();
 		case R.id.whatsnew:
 			return new AlertDialog.Builder(this) //.setIcon( R.drawable.alert_dialog_icon)
 					.setTitle(R.string.about_dialog_whats_new)
 					.setMessage(R.string.whats_new_dialog_text)
 					.setNegativeButton(R.string.about_dialog_close, new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog, int whichButton) {
 
 							/* User clicked Cancel so do some stuff */
 						}
 					}).create();
 		case R.id.about:
 			return new AlertDialog.Builder(this) //.setIcon(R.drawable.alert_dialog_icon)
 					.setTitle(R.string.menu_about)
 					.setMessage(getText(R.string.app_name) + " v." + Ut.getAppVersion(this) + "\n\n"
 							+ getText(R.string.about_dialog_text))
 					.setPositiveButton(R.string.about_dialog_whats_new, new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog, int whichButton) {
 
 							showDialog(R.id.whatsnew);
 						}
 					}).setNegativeButton(R.string.about_dialog_close, new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog, int whichButton) {
 
 							/* User clicked Cancel so do some stuff */
 						}
 					}).create();
 		case R.id.error:
 			return new AlertDialog.Builder(this) //.setIcon(R.drawable.alert_dialog_icon)
 			.setTitle(R.string.error_title)
 			.setMessage(getText(R.string.error_text))
 			.setPositiveButton(R.string.error_send, new DialogInterface.OnClickListener() {
 				@SuppressWarnings("static-access")
 				public void onClick(DialogInterface dialog, int whichButton) {
 
 					SharedPreferences settings = getPreferences(Activity.MODE_PRIVATE);
 					String text =  settings.getString("error", "");
 					String subj = "Salt Road error: ";
 					try {
 						final String[] lines = text.split("\n", 2);
 						final Pattern p = Pattern.compile("[.][\\w]+[:| |\\t|\\n]");
 						final Matcher m = p.matcher(lines[0]+"\n");
 						if (m.find())
 							subj += m.group().replace(".", "").replace(":", "").replace("\n", "")+" at ";
 						final Pattern p2 = Pattern.compile("[.][\\w]+[(][\\w| |\\t]*[)]");
 						final Matcher m2 = p2.matcher(lines[1]);
 						if (m2.find())
 							subj += m2.group().substring(2);
 					} catch (Exception e) {
 					}
 
 					final Build b = new Build();
 					final Build.VERSION v = new Build.VERSION();
 					text = "Your message:"
 						+"\n\nSalt Road: "+Ut.getAppVersion(MainMapActivity.this)
 						+"\nAndroid: "+v.RELEASE
 						+"\nDevice: "+b.BOARD+" "+b.BRAND+" "+b.DEVICE+" "+b.MANUFACTURER+" "+b.MODEL+" "+b.PRODUCT
 						+"\n\n"+text;
 
 					startActivity(Ut.SendMail(subj, text));
 
 					SharedPreferences uiState = getPreferences(0);
 					SharedPreferences.Editor editor = uiState.edit();
 					editor.putString("error", "");
 					editor.commit();
 
 				}
 			}).setNegativeButton(R.string.about_dialog_close, new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int whichButton) {
 
 					SharedPreferences uiState = getPreferences(0);
 					SharedPreferences.Editor editor = uiState.edit();
 					editor.putString("error", "");
 					editor.commit();
 				}
 			}).create();
 		case R.id.track_dialog_wait: {
 			mTrackDlgWait = new ProgressDialog(this);
 			mTrackDlgWait.setMessage("Please wait while loading track...");
 			mTrackDlgWait.setIndeterminate(true);
 			mTrackDlgWait.setCancelable(false);
 			return mTrackDlgWait;
 		}
 		case R.id.defaultpoi_db_wait: {
 			mDefaultPoiDBDlgWait = new ProgressDialog(this);
 			mDefaultPoiDBDlgWait.setMessage("Please wait while loading default poi...");
 			mDefaultPoiDBDlgWait.setIndeterminate(true);
 			mDefaultPoiDBDlgWait.setCancelable(false);
 			return mDefaultPoiDBDlgWait;
 		}
 		case R.id.decompress_default_poi_image_file_wait: {
 			mDecompressDefaultPoiImageFileDlgWait = new ProgressDialog(this);
 			mDecompressDefaultPoiImageFileDlgWait.setMessage("Please wait while decompressing default poi image file...");
 			mDecompressDefaultPoiImageFileDlgWait.setIndeterminate(true);
 			mDecompressDefaultPoiImageFileDlgWait.setCancelable(false);
 			return mDecompressDefaultPoiImageFileDlgWait;
 		}
 		}
 
 		return null;
 	}
 
 	private void setLastKnownLocation() {
 		final LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 		final Location loc1 = lm.getLastKnownLocation("gps");
 		final Location loc2 = lm.getLastKnownLocation("network");
 
 		boolean boolGpsEnabled = lm.isProviderEnabled(GPS);
 		boolean boolNetworkEnabled = lm.isProviderEnabled(NETWORK);
 		String str = "";
 		Location loc = null;
 
 		if(loc1 == null && loc2 != null)
 			loc = loc2;
 		else if (loc1 != null && loc2 == null)
 			loc = loc1;
 		else if (loc1 == null && loc2 == null)
 			loc = null;
 		else
 			loc = loc1.getTime() > loc2.getTime() ? loc1 : loc2;
 
 		if(boolGpsEnabled){}
 		else if(boolNetworkEnabled)
 			str = getString(R.string.message_gpsdisabled);
 		else if(loc == null)
 			str = getString(R.string.message_locationunavailable);
 		else
 			str = getString(R.string.message_lastknownlocation);
 
 		if(str.length() > 0)
 			Toast.makeText(this, str, Toast.LENGTH_LONG).show();
 
 		if (loc != null)
 			this.mOsmv
 					.getController()
 					.animateTo(
 							TypeConverter.locationToGeoPoint(loc),
 							OpenStreetMapViewController.AnimationType.MIDDLEPEAKSPEED,
 							OpenStreetMapViewController.ANIMATION_SMOOTHNESS_HIGH,
 							OpenStreetMapViewController.ANIMATION_DURATION_DEFAULT);
 	}
 
 	private void setDefaultLocation() {
 		SharedPreferences settings = getPreferences(Activity.MODE_PRIVATE);
 		SharedPreferences.Editor editor1 = settings.edit();
 		editor1.putInt("Latitude", MapConstants.DEFAULT_LATITUDE);
 		editor1.putInt("Longitude", MapConstants.DEFAULT_LONGITUDE);
 		editor1.putInt("ZoomLevel", MapConstants.DEFAULT_ZOOMLEVEL - 1);
 		editor1.commit();
 
 		mOsmv.setZoomLevel(MapConstants.DEFAULT_ZOOMLEVEL - 1);
 		mOsmv.setMapCenter(MapConstants.DEFAULT_LATITUDE, MapConstants.DEFAULT_LONGITUDE);
 		setTitle();
 	}
 	
 	private void setAutoFollow(boolean autoFollow) {
 		setAutoFollow(autoFollow, false);
 	}
 
 	private void setAutoFollow(boolean autoFollow, final boolean supressToast) {
 		if (mAutoFollow != autoFollow) {
 			mAutoFollow = autoFollow;
 
 			if (autoFollow) {
 				ivAutoFollow.setVisibility(ImageView.INVISIBLE);
 				if(!supressToast)
 					Toast.makeText(this, R.string.auto_follow_enabled, Toast.LENGTH_SHORT).show();
 			} else {
 				ivAutoFollow.setVisibility(ImageView.VISIBLE);
 				if(!supressToast)
 					Toast.makeText(this, R.string.auto_follow_disabled, Toast.LENGTH_SHORT).show();
 			}
 		}
 	}
 
 	@Override
 	public void onLocationChanged(Location loc) {
 		this.mMyLocationOverlay.setLocation(loc);
 
 		mLastSpeed = loc.getSpeed();
 
 		if (mAutoFollow) {
 			if (mDrivingDirectionUp)
 				if (loc.getSpeed() > 0.5)
 					this.mOsmv.setBearing(loc.getBearing());
 
 			this.mOsmv.getController().animateTo(
 					TypeConverter.locationToGeoPoint(loc),
 					OpenStreetMapViewController.AnimationType.MIDDLEPEAKSPEED,
 					OpenStreetMapViewController.ANIMATION_SMOOTHNESS_HIGH,
 					OpenStreetMapViewController.ANIMATION_DURATION_DEFAULT);
 		} else
 			this.mOsmv.invalidate();
 	}
 
 	@Override
 	public void onLocationLost() {
 		// Auto-generated method stub
 
 	}
 
 	@Override
 	public void onStatusChanged(String provider, int status, Bundle extras) {
 		int cnt = extras.getInt("satellites", Integer.MIN_VALUE);
 		mStatusListener = provider+ " " + status + " " + (cnt >= 0 ? cnt : 0);
 		setTitle();
 	}
 
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu) {
 		Menu submenu = menu.findItem(R.id.mapselector).getSubMenu();
 		submenu.clear();
 		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
 
 		File folder = Ut.getRMapsMapsDir(this);
 		if (folder.exists()) {
 			File[] files = folder.listFiles();
 			if (files != null)
 				for (int i = 0; i < files.length; i++) {
 					if (files[i].getName().toLowerCase().endsWith(".mnm")
 							|| files[i].getName().toLowerCase()
 									.endsWith(".tar")
 							|| files[i].getName().toLowerCase().endsWith(
 									".sqlitedb")) {
 						String name = Ut.FileName2ID(files[i].getName());
 						if (pref.getBoolean("pref_usermaps_" + name + "_enabled", false)) {
 							MenuItem item = submenu.add(pref.getString("pref_usermaps_" + name + "_name", files[i]
 									.getName()));
 							item.setTitleCondensed("usermap_" + name);
 						}
 					}
 				}
 		}
 
 		final SAXParserFactory fac = SAXParserFactory.newInstance();
 		SAXParser parser = null;
 		try {
 			parser = fac.newSAXParser();
 			if(parser != null){
 				final InputStream in = getResources().openRawResource(R.raw.predefmaps);
 				parser.parse(in, new PredefMapsParser(submenu, pref));
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		return super.onPrepareOptionsMenu(menu);
 	}
 
 	@Override
 	protected void onPause() {
 		SharedPreferences uiState = getPreferences(0);
 		SharedPreferences.Editor editor = uiState.edit();
 		editor.putString("MapName", mOsmv.getRenderer().ID);
 		editor.putInt("Latitude", mOsmv.getMapCenterLatitudeE6());
 		editor.putInt("Longitude", mOsmv.getMapCenterLongitudeE6());
 		editor.putInt("ZoomLevel", mOsmv.getZoomLevel());
 		editor.putBoolean("CompassEnabled", mCompassEnabled);
 		editor.putBoolean("AutoFollow", mAutoFollow);
 		editor.putString("app_version", Ut.getAppVersion(this));
 		if(mDefaultPoiOverlay != null)
 			editor.putInt("curShowDefaultPoiId", mDefaultPoiOverlay.getTapIndex());
 		if(mPoiOverlay != null)
 			editor.putInt("curShowPoiId", mPoiOverlay.getTapIndex());
 		mSearchResultOverlay.toPref(editor);
 		editor.commit();
 
 		if (myWakeLock != null) {
 			myWakeLock.release();
 		}
 
 		mOrientationSensorManager.unregisterListener(mListener);
 
 		super.onPause();
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		
     	SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
 
     	if (pref.getBoolean("pref_keepscreenon", true)) {
 			myWakeLock = ((PowerManager) getSystemService(POWER_SERVICE)).newWakeLock(
 					PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "SaltRoad");
 			myWakeLock.acquire();
 		} else {
 			myWakeLock = null;
 		}
 
 		if(mCompassEnabled)
 			mOrientationSensorManager.registerListener(mListener, mOrientationSensorManager
 				.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_UI);
 
 		if(mDefaultTrackOverlay != null) {
 			mDefaultTrackOverlay.setStopDraw(false);
 		}
 		if(mTrackOverlay != null) {
 			mTrackOverlay.setStopDraw(false);
 		}
 	}
 
 	@Override
 	protected void onRestart() {
 //		if(mDefaultTrackOverlay != null)
 //			mDefaultTrackOverlay.clearTrack();
 		if(mTrackOverlay != null)
 			mTrackOverlay.clearTrack();
 //		if(mCurrentTrackOverlay != null)
 //			mCurrentTrackOverlay.clearTrack();
 
 		super.onRestart();
 	}
 
 	private OpenStreetMapRendererInfo getRendererInfo(final Resources aRes, final SharedPreferences aPref, final String aName){
 		OpenStreetMapRendererInfo RendererInfo = new OpenStreetMapRendererInfo(aRes, aName);
 		RendererInfo.LoadFromResources(aName, PreferenceManager.getDefaultSharedPreferences(this));
 
 
 		return RendererInfo;
 	}
 
 	private void restoreUIState() {
 		SharedPreferences settings = getPreferences(Activity.MODE_PRIVATE);
 		
 		OpenStreetMapRendererInfo RendererInfo = getRendererInfo(getResources(), settings, settings.getString("MapName", "googleland"));
 		Ut.dd(settings.getString("MapName", "googleland"));
 		if(!mOsmv.setRenderer(RendererInfo)) {
 			mOsmv.setRenderer(getRendererInfo(getResources(), settings, "googleland"));
 		}
 
 		this.mOsmv.getOverlays().clear();
 		if(RendererInfo.YANDEX_TRAFFIC_ON == 1){
        		this.mOsmv.getOverlays().add(new YandexTrafficOverlay(this, this.mOsmv));
 		}
         if(mDefaultTrackOverlay != null) {
         	this.mOsmv.getOverlays().add(mDefaultTrackOverlay);
         }
         if(mTrackOverlay != null) {
         	this.mOsmv.getOverlays().add(mTrackOverlay);
         }
         
         if(mCurrentTrackOverlay != null)
         	this.mOsmv.getOverlays().add(mCurrentTrackOverlay);
         if(mDefaultPoiOverlay != null)
         	this.mOsmv.getOverlays().add(mDefaultPoiOverlay);
         if(mPoiOverlay != null)
         	this.mOsmv.getOverlays().add(mPoiOverlay);
         this.mOsmv.getOverlays().add(mMyLocationOverlay);
         this.mOsmv.getOverlays().add(mSearchResultOverlay);
 
 		mOsmv.setZoomLevel(settings.getInt("ZoomLevel", MapConstants.DEFAULT_ZOOMLEVEL - 1));
 		mOsmv.setMapCenter(settings.getInt("Latitude", MapConstants.DEFAULT_LATITUDE), settings.getInt("Longitude", MapConstants.DEFAULT_LONGITUDE));
 
 		mCompassEnabled = settings.getBoolean("CompassEnabled", false);
 		mCompassView.setVisibility(mCompassEnabled ? View.VISIBLE : View.INVISIBLE);
 
 		mAutoFollow = settings.getBoolean("AutoFollow", true);
 		ivAutoFollow.setVisibility(mAutoFollow ? ImageView.INVISIBLE : View.VISIBLE);
 
 		setTitle();
 
 		int defaultPoiId = settings.getInt("curShowDefaultPoiId", -1);
 
 		if(mDefaultPoiOverlay != null) {
 			mDefaultPoiOverlay.setTapIndex(defaultPoiId);
 			if (defaultPoiId >= 0)
 				defaultPoiDetailButtonEnable(true);
 			else
 				defaultPoiDetailButtonEnable(false);
 		}
 
 		if(mPoiOverlay != null)
 			mPoiOverlay.setTapIndex(settings.getInt("curShowPoiId", -1));
 
 		mSearchResultOverlay.fromPref(settings);
 
 		if(settings.getString("error", "").length() > 0){
 			showDialog(R.id.error);
 		}
 
 		if(!settings.getString("app_version", "").equalsIgnoreCase(Ut.getAppVersion(this))) {
 			showDialog(R.id.whatsnew);
 		}
 
     	CheckNeedDefaultDataUpdate();
 		setDefaultMapTrack();
 		setDefaultPoiImageFile();
 			
 		if (settings.getBoolean("add_yandex_bookmark", true))
 			if (getResources().getConfiguration().locale.toString()
 					.equalsIgnoreCase("ru_RU")) {
 				SharedPreferences uiState = getPreferences(0);
 				SharedPreferences.Editor editor = uiState.edit();
 				editor.putBoolean("add_yandex_bookmark", false);
 				editor.commit();
 
 				Message msg = Message.obtain(mCallbackHandler,
 						R.id.add_yandex_bookmark);
 				mCallbackHandler.sendMessageDelayed(msg, 2000);
 			}
 	}
 	
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		switch(requestCode){
 		case R.id.menu_addpoi:
 		case R.id.menu_editpoi:
 			mPoiOverlay.UpdateList();
 			mOsmv.invalidate();
 			break;
 		case R.id.poilist:
 			if(resultCode == RESULT_OK){
 				PoiPoint point = mPoiManager.getPoiPoint(data.getIntExtra("pointid", PoiPoint.EMPTY_ID()));
 				if(point != null){
 					setAutoFollow(false);
 					mPoiOverlay.UpdateList();
 					mOsmv.setMapCenter(point.GeoPoint);
 				}
 			} else {
 				mPoiOverlay.UpdateList();
 				mOsmv.invalidate();
 			}
 			break;
 		case R.id.tracks:
 			if(resultCode == RESULT_OK){
 				Track track = mPoiManager.getTrack(data.getIntExtra("trackid", PoiPoint.EMPTY_ID()));
 				if(track != null){
 					setAutoFollow(false);
 					mOsmv.setMapCenter(track.getBeginGeoPoint());
 				}
 			}
 			break;
 		case R.id.settings_activity_closed:
 			finish();
 			startActivity(new Intent(this, this.getClass()));
 			break;
 		}
 		
 		super.onActivityResult(requestCode, resultCode, data);
 	}
 
 	private void setTitle(){
 		final TextView leftText = (TextView) findViewById(R.id.left_text);
 		if(leftText != null)
 			leftText.setText(mOsmv.getRenderer().NAME);
 
 		final TextView rightText = (TextView) findViewById(R.id.right_text);
 		if(rightText != null){
 			rightText.setText(mStatusListener + " " + (1+mOsmv.getZoomLevel()));
 		}
 	}
 
 	private class MainActivityCallbackHandler extends Handler{
 		@Override
 		public void handleMessage(final Message msg) {
 			
 			final int what = msg.what;
 			switch(what){
 				case R.id.user_moved_map:
 					setAutoFollow(false);
 					break;
 				case R.id.set_title:
 					setTitle();
 					break;
 				case R.id.add_yandex_bookmark:
 					showDialog(R.id.add_yandex_bookmark);
 					break;
 				case OpenStreetMapTileFilesystemProvider.ERROR_MESSAGE:
 					if(msg.obj != null)
 						Toast.makeText(MainMapActivity.this, msg.obj.toString(), Toast.LENGTH_LONG).show();
 					break;
 			}
 		}
 	}
 
 	@Override
 	public boolean onSearchRequested() {
         startSearch("", false, null, false);
 		return true;
 	}
 
 	@Override
 	protected void onNewIntent(Intent intent) {
 		super.onNewIntent(intent);
 
         final String queryAction = intent.getAction();
         if (Intent.ACTION_SEARCH.equals(queryAction)) {
             doSearchQuery(intent);
         }
 	}
 
 	private void doSearchQuery(Intent queryIntent) {
 		mSearchResultOverlay.Clear();
 		this.mOsmv.invalidate();
 
 		final String queryString = queryIntent.getStringExtra(SearchManager.QUERY);
 
         // Record the query string in the recent queries suggestions provider.
         SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this, SearchSuggestionsProvider.AUTHORITY, SearchSuggestionsProvider.MODE);
         suggestions.saveRecentQuery(queryString, null);
 
 		InputStream in = null;
 		OutputStream out = null;
 
 		try {
 			URL url = new URL(
 					"http://ajax.googleapis.com/ajax/services/search/local?v=1.0&sll="
 							+ this.mOsmv.getMapCenter().toDoubleString()
 							+ "&q=" + URLEncoder.encode(queryString, "UTF-8")
 							+ "");
 			Ut.dd(url.toString());
 			in = new BufferedInputStream(url.openStream(), StreamUtils.IO_BUFFER_SIZE);
 
 			final ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
 			out = new BufferedOutputStream(dataStream, StreamUtils.IO_BUFFER_SIZE);
 			StreamUtils.copy(in, out);
 			out.flush();
 
 			String str = dataStream.toString();
 			JSONObject json = new JSONObject(str);
 			//Ut.dd(json.toString(4)); //
 			JSONArray results = (JSONArray) ((JSONObject) json.get("responseData")).get("results");
 			//Ut.dd("results.length="+results.length());
 			if(results.length() == 0){
 				Toast.makeText(this, R.string.no_items, Toast.LENGTH_SHORT).show();
 				return;
 			}
 			JSONObject res = results.getJSONObject(0);
 			//Ut.dd(res.toString(4));
 			//Toast.makeText(this, res.getString("titleNoFormatting"), Toast.LENGTH_LONG).show();
 			final String address = res.getString("addressLines").replace("\"", "").replace("[", "").replace("]", "").replace(",", ", ").replace("  ", " ");
 			//Toast.makeText(this, address, Toast.LENGTH_LONG).show();
 			//Toast.makeText(this, ((JSONObject) json.get("addressLines")).toString(), Toast.LENGTH_LONG).show();
 
 			setAutoFollow(false, true);
 			this.mSearchResultOverlay.setLocation(new GeoPoint((int)(res.getDouble("lat")* 1E6), (int)(res.getDouble("lng")* 1E6)), address);
 			this.mOsmv.setZoomLevel((int) (2 * res.getInt("accuracy")));
 			this.mOsmv.getController().animateTo(new GeoPoint((int)(res.getDouble("lat")* 1E6), (int)(res.getDouble("lng")* 1E6)), OpenStreetMapViewController.AnimationType.MIDDLEPEAKSPEED, OpenStreetMapViewController.ANIMATION_SMOOTHNESS_HIGH, OpenStreetMapViewController.ANIMATION_DURATION_DEFAULT);
 
 			setTitle();
 
 		} catch (Exception e) {
 			e.printStackTrace();
 			Toast.makeText(this, R.string.no_inet_conn, Toast.LENGTH_LONG).show();
 		} finally {
 			StreamUtils.closeStream(in);
 			StreamUtils.closeStream(out);
 		}
 	}
 
     public void copyTrackToSDCardSuccessPreference(Boolean success) {
 		SharedPreferences settings = getPreferences(Activity.MODE_PRIVATE);
 		SharedPreferences.Editor editor1 = settings.edit();
 
     	if (success) {
     		mDefaultTrackOverlay.setStopDraw(false);
     		editor1.putBoolean("copy_track_to_sdcard_success", true);
     		editor1.commit();
     		setDefaultLocation();
 			mOsmv.invalidate();
     	} else {
     		editor1.putBoolean("copy_track_to_sdcard_success", false);
     		editor1.commit();
     	}
 		dismissDialog(R.id.track_dialog_wait);
 		
 		// Show default track
 		//finish();
 		//startActivity(new Intent(this, this.getClass()));
     }
 
     public void copyDefaultPoiDBFileToSDCardSuccessPreference(Boolean success) {
 		SharedPreferences settings = getPreferences(Activity.MODE_PRIVATE);
 		SharedPreferences.Editor editor1 = settings.edit();
 
     	if (success) {
     		editor1.putBoolean("copy_defaultpoi_to_sdcard_success", true);
     		editor1.commit();
 			mOsmv.invalidate();
     	} else {
     		editor1.putBoolean("copy_defaultpoi_to_sdcard_success", false);
     		editor1.commit();
     	}
 		dismissDialog(R.id.defaultpoi_db_wait);
     }
     
     public void decompressDefaultPoiImageFileSuccessPreference(Boolean success) {
 		SharedPreferences settings = getPreferences(Activity.MODE_PRIVATE);
 		SharedPreferences.Editor editor = settings.edit();
 		editor.putBoolean("decompress_default_poi_image_file_success", success);
 		editor.commit();
 		dismissDialog(R.id.decompress_default_poi_image_file_wait);
     }
 
     public void defaultPoiDetailButtonEnable(Boolean isEnable) {
 		if (isEnable)
 	        ivDefaultPoi.setVisibility(ImageView.VISIBLE);
 		else
 	        ivDefaultPoi.setVisibility(ImageView.INVISIBLE);
 	}
 
 	private void copyTrackFileToSDCard() {
     	if (mCopyTrackFileToSDCardHandler == null)
     	{
     		mCopyTrackFileToSDCardHandler = new CopyTrackFileToSDCardHandler(this);
     		mCopyTrackFileToSDCardThreadRunnable = new Thread(new CopyTrackFileToSDCardThreadRunnable(mCopyTrackFileToSDCardHandler, this, mPoiManager));
     		mCopyTrackFileToSDCardThreadRunnable.start();
     	}
     	if (mCopyTrackFileToSDCardThreadRunnable.getState() != Thread.State.TERMINATED)
     	{
     		Ut.dd("thread is new or alive, but not terminated");
     	}
     	else
     	{
     		Ut.dd("thread is likely dead. starting now");
     		//you have to create a new thread.
     		//no way to resurrect a dead thread.
     		mCopyTrackFileToSDCardThreadRunnable = new Thread(new CopyTrackFileToSDCardThreadRunnable(mCopyTrackFileToSDCardHandler, this, mPoiManager));
     		mCopyTrackFileToSDCardThreadRunnable.start();
     	}
 	}
 
 	private void copyDefaultPoiDBFileToSDCard() {
     	if (mCopyDefaultPoiDBFileToSDCardHandler == null)
     	{
     		mCopyDefaultPoiDBFileToSDCardHandler = new CopyDefaultPoiDBFileToSDCardHandler(this);
     		mCopyDefaultPoiDBFileToSDCardThreadRunnable = new Thread(new CopyDefaultPoiDBFileToSDCardThreadRunnable(mCopyDefaultPoiDBFileToSDCardHandler, this));
     		mCopyDefaultPoiDBFileToSDCardThreadRunnable.start();
     	}
     	if (mCopyDefaultPoiDBFileToSDCardThreadRunnable.getState() != Thread.State.TERMINATED)
     	{
     		Ut.dd("thread is new or alive, but not terminated");
     	}
     	else
     	{
     		Ut.dd("thread is likely dead. starting now");
     		//you have to create a new thread.
     		//no way to resurrect a dead thread.
     		mCopyDefaultPoiDBFileToSDCardThreadRunnable = new Thread(new CopyDefaultPoiDBFileToSDCardThreadRunnable(mCopyDefaultPoiDBFileToSDCardHandler, this));
     		mCopyDefaultPoiDBFileToSDCardThreadRunnable.start();
     	}
 	}
 
 	public void decompressDefaultPoiImageFile() {
     	if (mDecompressDefaultPoiImageFileHandler == null)
     	{
     		mDecompressDefaultPoiImageFileHandler = new DecompressDefaultPoiImageFileHandler(this);
     		mDecompressDefaultPoiImageFileThreadRunnable = new Thread(new DecompressDefaultPoiImageFileThreadRunnable(mDecompressDefaultPoiImageFileHandler, this));
     		mDecompressDefaultPoiImageFileThreadRunnable.start();
     	}
     	if (mDecompressDefaultPoiImageFileThreadRunnable.getState() != Thread.State.TERMINATED)
     	{
     		Ut.dd("thread is new or alive, but not terminated");
     	}
     	else
     	{
     		Ut.dd("thread is likely dead. starting now");
     		//you have to create a new thread.
     		//no way to resurrect a dead thread.
     		mDecompressDefaultPoiImageFileThreadRunnable = new Thread(new DecompressDefaultPoiImageFileThreadRunnable(mDecompressDefaultPoiImageFileHandler, this));
     		mDecompressDefaultPoiImageFileThreadRunnable.start();
     	}
 	}
 
 	private void setDefaultMapTrack() {
 		SharedPreferences settings = getPreferences(Activity.MODE_PRIVATE);
 		
 		File mainFolder = Ut.getRMapsMainDir(this, "data");
 		String defaultPoiDBFilePath = mainFolder.getAbsolutePath() + File.separator + MapConstants.DEFAULTPOI_DB;
 		File defaultPoiDBFile = new File(defaultPoiDBFilePath);
 
 		File trackFolder = Ut.getRMapsDefaultImportDir(this);
 		
 		boolean isExistTrackFile = true;
 		for(Integer i = 0 ; i < MapConstants.TRACK_FILE_NUM ; i++) {
 	        String trackFileBasename = String.format("%s%02d", MapConstants.DEFAULT_TRACK_FILE_BASE_NAME, i + 1);
 	        String trackFilename = trackFileBasename + ".kml";
 			String trackFilePath = trackFolder.getAbsolutePath() + File.separator + trackFilename;
 			File trackFile = new File(trackFilePath);
 			
 			if (!trackFile.exists())
 				isExistTrackFile = false;
 		}
 		
 		if (!settings.getBoolean("copy_track_to_sdcard_success", false) || !isExistTrackFile) {
 			showDialog(R.id.track_dialog_wait);
 			MainMapActivity.this.copyTrackFileToSDCard();
 		}
 		if (!settings.getBoolean("copy_defaultpoi_to_sdcard_success", false) || !defaultPoiDBFile.exists()) {
 			showDialog(R.id.defaultpoi_db_wait);
 			MainMapActivity.this.copyDefaultPoiDBFileToSDCard();
 		}
     }
 
 	private void setDefaultPoiImageFile() {
 		SharedPreferences settings = getPreferences(Activity.MODE_PRIVATE);
 		
 		if (!settings.getBoolean("decompress_default_poi_image_file_success", false)) {
			showDialog(R.id.decompress_default_poi_image_file_wait);
 			String filename = MapConstants.DEFAULT_POI_IMAGE_FILES_ZIP_FILE_NAME;
 			
 			File zipFileFolder = Ut.getRMapsTmpDir(this);
 			String zipFilename = zipFileFolder.getAbsolutePath() + File.separator + filename;
 			String downloadMapUrlString = MapConstants.MAP_URL_BASE + "/" + filename;
 			
 			new DownloadDefaultPoiImage(this).execute(downloadMapUrlString, zipFilename);
 		}
     }
 }
