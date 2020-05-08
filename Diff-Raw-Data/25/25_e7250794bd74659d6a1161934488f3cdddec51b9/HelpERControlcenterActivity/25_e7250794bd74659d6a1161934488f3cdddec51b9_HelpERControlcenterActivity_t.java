 package com.indago.helpme.gui.dashboard;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.json.simple.JSONObject;
 
 import android.content.Intent;
 import android.graphics.drawable.Drawable;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TabHost;
 import android.widget.TabHost.TabSpec;
 
 import com.android.helpme.demo.interfaces.DrawManagerInterface;
 import com.android.helpme.demo.interfaces.UserInterface;
 import com.android.helpme.demo.manager.HistoryManager;
 import com.android.helpme.demo.manager.MessageOrchestrator;
 import com.android.helpme.demo.manager.UserManager;
 import com.android.helpme.demo.overlay.HistoryItemnizedOverlay;
 import com.android.helpme.demo.overlay.HistoryOverlayItem;
 import com.android.helpme.demo.utils.Task;
 import com.android.helpme.demo.utils.ThreadPool;
 import com.android.helpme.demo.utils.User;
 import com.android.helpme.demo.utils.position.Position;
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MapView;
 import com.google.android.maps.Overlay;
 import com.google.android.maps.OverlayItem;
 import com.indago.helpme.R;
 import com.indago.helpme.gui.list.AboutListAdapter;
 
 public class HelpERControlcenterActivity extends MapActivity implements DrawManagerInterface {
 
 	private Handler mHandler;
 	private TabHost mTabHost;
 
 	private List<Overlay> mapOverlays;
 	private HistoryItemnizedOverlay overlay;
 	private MapController mapController;
 	private Drawable mMapsPin;
 	private ListView lvAbout;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_help_er_controlcenter);
 
 		mHandler = new Handler();
 
 		ImageView logo = (ImageView) findViewById(R.id.iv_helpme_logo);
 		logo.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				exit();
 			}
 		});
 
 		MessageOrchestrator.getInstance().addDrawManager(DRAWMANAGER_TYPE.HELPER, this);
 		MessageOrchestrator.getInstance().addDrawManager(DRAWMANAGER_TYPE.HISTORY, this);
 
 		mHandler.post(HistoryManager.getInstance().loadHistory(getApplicationContext()));
 
 		initTabs();
 		initMaps();
 	}
 
 	private void initTabs() {
 		mTabHost = (TabHost) findViewById(R.id.tabhost);
 		mTabHost.setup();
 
 		TabSpec statisticsTab = mTabHost.newTabSpec("Statistics");
 		statisticsTab.setContent(R.id.ll_tab1);
 		statisticsTab.setIndicator(getResources().getString(R.string.help_er_controlcenter_tab_history));
 
 		TabSpec aboutTab = mTabHost.newTabSpec("About");
 		aboutTab.setContent(R.id.ll_tab2);
 		aboutTab.setIndicator(getResources().getString(R.string.help_er_controlcenter_tab_about));
 
 		mTabHost.addTab(statisticsTab);
 		mTabHost.addTab(aboutTab);
 
 		List<Drawable> list = new ArrayList<Drawable>();
 
 		list.add(getResources().getDrawable(R.drawable.logo_igd));
 		list.add(getResources().getDrawable(R.drawable.logo_uid));
 		list.add(getResources().getDrawable(R.drawable.logo_tud));
 
 		AboutListAdapter adapter = new AboutListAdapter(getApplicationContext(), R.layout.list_item_picture, list);
 
 		lvAbout = (ListView) findViewById(R.id.lv_tab2);
 		lvAbout.setAdapter(adapter);
 		lvAbout.setOnItemClickListener(new OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView<?> parent, View view, int position, long it) {
 				Uri uri;
 
 				switch(position) {
 					case 0:
 						uri = Uri.parse("http://igd.fraunhofer.de");
 						startActivity(new Intent(Intent.ACTION_VIEW, uri));
 						break;
 					case 1:
 						uri = Uri.parse("http://www.uid.com/en/home.html");
 						startActivity(new Intent(Intent.ACTION_VIEW, uri));
 						break;
 					case 2:
 						uri = Uri.parse("http://www.tu-darmstadt.de/");
 						startActivity(new Intent(Intent.ACTION_VIEW, uri));
 						break;
 					default:
 						break;
 				}
 			}
 		});
 
 	}
 
 	private void initMaps() {
 		MapView mapView = (MapView) findViewById(R.id.mapview);
 		mapView.setBuiltInZoomControls(true);
 
 		mapOverlays = mapView.getOverlays();
 
 		mMapsPin = this.getResources().getDrawable(R.drawable.maps_pin_green);
 		mMapsPin.setBounds(0, 0, mMapsPin.getIntrinsicWidth(), mMapsPin.getIntrinsicHeight());
 
 		overlay = new HistoryItemnizedOverlay(mMapsPin, this);
 
 		mapController = mapView.getController();
 		mapOverlays.add(overlay);
 	}
 
 	@Override
 	protected void onResume() {
 		MessageOrchestrator.getInstance().addDrawManager(DRAWMANAGER_TYPE.HELPER, this);
 		MessageOrchestrator.getInstance().addDrawManager(DRAWMANAGER_TYPE.HISTORY, this);
 
 		mHandler.post(HistoryManager.getInstance().loadHistory(getApplicationContext()));
 		super.onResume();
 	}
 
 	@Override
 	protected void onPause() {
 
 		MessageOrchestrator.getInstance().removeDrawManager(DRAWMANAGER_TYPE.HISTORY);
 		MessageOrchestrator.getInstance().removeDrawManager(DRAWMANAGER_TYPE.HELPER);
 		super.onPause();
 	}
 
 	@Override
 	public void onBackPressed() {
 		exit();
 		super.onBackPressed();
 	}
 
 	private void exit() {
		ThreadPool.runTask(UserManager.getInstance().deleteUserChoice(getApplicationContext()));
 		finish();
 	}
 
 	@Override
 	public void drawThis(final Object object) {
 		if(object instanceof User) {
 			/*
 			 * Start Call Details Activity
 			 */
 			mHandler.post(startHelpERDashboard((UserInterface) object));
 		} else if(object instanceof ArrayList<?>) {
 			/*
 			 * Add History
 			 */
 			if(((ArrayList<?>) object).get(0) instanceof JSONObject) {
 				ArrayList<JSONObject> arrayList = (ArrayList<JSONObject>) object;
 				mHandler.post(addMarker(arrayList));
 			}
 		}
 	}
 
 	private Runnable startHelpERDashboard(final UserInterface user) {
 		return new Runnable() {
 
 			@Override
 			public void run() {
 //				HistoryManager.getInstance().startNewTask(user);
 				Intent intent = new Intent(getApplicationContext(), HelpERDashboardActivity.class);
 				intent.putExtra("USER_ID", user.getId());
 
 				startActivityForResult(intent, 0);
 			}
 		};
 	}
 
 	private Runnable addMarker(final ArrayList<JSONObject> jsonObjects) {
 		return new Runnable() {
 			@Override
 			public void run() {
 				for(JSONObject jsonObject : jsonObjects) {
 					Position position = new Position((JSONObject) jsonObject.get(Task.START_POSITION));
 					User user = new User((JSONObject) jsonObject.get(Task.USER));
 					String snippet = new String();
 
 					snippet = "HISTORY SNIPPED";
 
 					HistoryOverlayItem overlayitem = new HistoryOverlayItem(position.getGeoPoint(), user.getId(), snippet, jsonObject);
 					overlay.addOverlay(overlayitem);
 				}
 				setZoomLevel();
 			}
 		};
 	}
 
 	private void setZoomLevel() {
 
 		if(overlay.size() > 1) {
 			int minLatitude = Integer.MAX_VALUE;
 			int maxLatitude = Integer.MIN_VALUE;
 			int minLongitude = Integer.MAX_VALUE;
 			int maxLongitude = Integer.MIN_VALUE;
 
 			for(OverlayItem item : overlay.getItems()) {
 				GeoPoint p = item.getPoint();
 				int lati = p.getLatitudeE6();
 				int lon = p.getLongitudeE6();
 
 				maxLatitude = Math.max(lati, maxLatitude);
 				minLatitude = Math.min(lati, minLatitude);
 				maxLongitude = Math.max(lon, maxLongitude);
 				minLongitude = Math.min(lon, minLongitude);
 			}
 			mapController.zoomToSpan(Math.abs(maxLatitude - minLatitude), Math.abs(maxLongitude - minLongitude));
 			mapController.animateTo(new GeoPoint((maxLatitude + minLatitude) / 2, (maxLongitude + minLongitude) / 2));
 
 		} else if(overlay.size() == 1) {
 			OverlayItem item = overlay.getItem(0);
 			mapController.animateTo(item.getPoint());
 			while(mapController.zoomIn()) {
 
 			}
 			mapController.zoomOut();
 		}
 	}
 
 	@Override
 	protected boolean isRouteDisplayed() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 }
