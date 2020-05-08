 package com.jeremyhaberman.playgrounds;
 
 import java.util.ArrayList;
 import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
 import java.util.List;
 
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MapView;
 import com.google.android.maps.MyLocationOverlay;
 import com.google.android.maps.Overlay;
 
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.os.Handler;
import android.os.Looper;
 import android.os.Message;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 
 /**
 * Swingset is the entry and primary Activity. It displays the map, the user's
  * current location and all playgrounds
  * 
  * @author jeremyhaberman
  * 
  */
 public class Playgrounds extends MapActivity {
 
 	static Context context;
 	static Object initPlaygroundLock = new Object();
 	private List<Overlay> mapOverlays;
 	private PlaygroundsLayer playgroundsLayer;
 	private MapView map;
 	private MapController controller;
 	private PlaygroundDAO playgroundDAO;
 	private MyLocationOverlay overlay;
 	private ProgressDialog progressDialog;
 	protected static final int ERROR_LOADING_PLAYGROUNDS = 1;
 	protected static final CharSequence ERROR_LOADING_PLAYGROUNDS_STRING = "Error loading playgrounds.";
 	protected static List<Playground> mPlaygrounds;
 	protected static boolean initializing = true;
 	protected static boolean mNewPlaygrounds = true;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		context = this;
 		playgroundDAO = new WebPlaygroundDAO(this);
 		setContentView(R.layout.map);
 		initMapView();
 		showMyLocation();
 		showPlaygrounds();
 	}
 
 	/**
 	 * Display the map, allowing zoom controls
 	 */
 	private void initMapView() {
 		map = (MapView) findViewById(R.id.map);
 		controller = map.getController();
 		map.setBuiltInZoomControls(true);
 	}
 
 	/**
 	 * Show the current location on the map and zoom in to level 13
 	 */
 	private void showMyLocation() {
 		if (overlay == null) {
 			overlay = new MyLocationOverlay(this, map);
 		}
 
 		overlay.enableMyLocation();
 
 		// Once the location is known, zoom in to it
 		overlay.runOnFirstFix(new Runnable() {
 			@Override
 			public void run() {
 				controller.setZoom(13);
 				controller.animateTo(overlay.getMyLocation());

 			}
 		});
 
 		map.getOverlays().add(overlay);
 	}
 
 	/**
 	 * Show the playgrounds on the map. Displays a spinning progress dialog
 	 * while the playground data is retrieved via background thread.
 	 */
 	protected void showPlaygrounds() {
 
 		progressDialog = ProgressDialog.show(Playgrounds.this, "",
				"Loading playgrounds...", true);
 		progressDialog.show();
 
 		Thread getPlaygroundsFromWebThread = new Thread() {
 			public void run() {
 				synchronized (initPlaygroundLock) {
 					setPlaygrounds(loadPlaygroundData());
 					if (mPlaygrounds.size() == 0) {
 						mHandler.post(displayErrorTask);
 					} else {
 						mHandler.post(updatePlaygroundsOnMap);
 					}
 				}
 			}
 		};
 
 		getPlaygroundsFromWebThread.start();
 	}
 
 	/**
 	 * Adds the playgrounds to the map after the playground data has been
 	 * retrieved by the thread in showPlaygrounds()
 	 */
 	final Runnable updatePlaygroundsOnMap = new Runnable() {
 		public void run() {
 			addPlaygroundsToLayer();
 			addPlaygroundsLayerToMap();
 		}
 	};
 
 	/**
 	 * Displays an error if the playground data was not successfully loaded by
 	 * the thread in showPlaygrounds().
 	 */
 	final Runnable displayErrorTask = new Runnable() {
 		public void run() {
 			displayPlaygroundLoadError();
 		}
 	};
 
 	/**
 	 * Display an error about failing to load playground data. Called by
 	 * displayErrorTask
 	 */
 	protected void displayPlaygroundLoadError() {
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setTitle("ERROR");
 
 		builder.setMessage("Error loading playgrounds");
 		builder.setCancelable(false);
 		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 			}
 		});
 		AlertDialog dialog = builder.create();
 		dialog.show();
 		mHandler.sendEmptyMessage(0);
 	}
 
 	/**
 	 * Setter for mPlaygrounds
 	 * 
 	 * @param playgrounds
 	 */
 	protected static void setPlaygrounds(List<Playground> playgrounds) {
 		mPlaygrounds = playgrounds;
 	}
 
 	/**
 	 * Refreshes the playgrounds on the map
 	 */
 	protected void refreshPlaygrounds() {
 		removePlaygroundsOnMap();
 		showPlaygrounds();
 	}
 
 	/**
 	 * Loads playground data from the web. This is a long-running task that
 	 * should only be called from a background thread.
 	 * 
 	 * @return
 	 */
 	protected List<Playground> loadPlaygroundData() {
 		List<Playground> playgrounds = new ArrayList<Playground>(
 				playgroundDAO.getAll(this));
 		return playgrounds;
 	}
 
 	/**
 	 * Converts playgrounds to map overlay items and then adds the items to an
 	 * itemized map overlay
 	 * 
 	 * @return PlaygroundsLayer playgroundsLayer
 	 */
 	protected PlaygroundsLayer addPlaygroundsToLayer() {
 		mapOverlays = map.getOverlays();
 		Drawable drawable = this.getResources().getDrawable(R.drawable.pin);
 		playgroundsLayer = new PlaygroundsLayer(drawable);
 
 		Collection<PlaygroundItem> playgroundItems = PlaygroundItemCreator
 				.createItems(mPlaygrounds);
 
 		for (PlaygroundItem item : playgroundItems) {
 			playgroundsLayer.addOverlayItem(item);
 		}
 		return playgroundsLayer;
 	}
 
 	protected void addPlaygroundsLayerToMap() {
 		mapOverlays.add(playgroundsLayer);
 		initializing = false;
 		mNewPlaygrounds = false;
 		mHandler.sendEmptyMessage(0);
 	}
 
 	// Handler for callbacks to the GUI thread
 	final Handler mHandler = new Handler() {
 		public void handleMessage(Message message) {
 			switch (message.what) {
 			case ERROR_LOADING_PLAYGROUNDS:
 				progressDialog.dismiss();
 				AlertDialog.Builder builder = new AlertDialog.Builder(
 						getApplicationContext());
 				builder.setMessage(ERROR_LOADING_PLAYGROUNDS_STRING);
 
 			}
 			progressDialog.dismiss();
 		}
 	};
 
 	private void removePlaygroundsOnMap() {
 		mapOverlays = map.getOverlays();
 		Drawable drawable = this.getResources().getDrawable(R.drawable.icon);
 		if (playgroundsLayer != null) {
 			mapOverlays.remove(playgroundsLayer);
 		}
 	}
 
 	@Override
 	protected void onResume() {
 		synchronized (initPlaygroundLock) {
 			super.onResume();
 			initMapView();
 			showMyLocation();
 
 			Bundle errorBundle = getIntent().getExtras();
 
 			if (errorBundle != null) {
 				String error = errorBundle.getString("Exception");
 			}
 			// refreshPlaygrounds();
 		}
 	}
 
 	@Override
 	protected void onPause() {
 		overlay.disableMyLocation();
 		// removePlaygroundsOnMap();
 		super.onPause();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.menu, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		super.onOptionsItemSelected(item);
 		switch (item.getItemId()) {
 		case R.id.add:
 			startActivity(new Intent(this, AddPlayground.class));
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	protected boolean isRouteDisplayed() {
 		return false;
 	}
 
 	protected static void setNewPlaygrounds(boolean newPlaygrounds) {
 		mNewPlaygrounds = newPlaygrounds;
 	}
 }
