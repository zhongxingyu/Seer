 /**
  * GeoZen LLC
  * Copyright 2011. All rights reserved.
  */
 
 package com.geozen.whiterim.ui;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.ContentResolver;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.Handler;
 import android.provider.BaseColumns;
 import android.text.TextUtils;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.Toast;
 
 import com.geozen.whiterim.R;
 import com.geozen.whiterim.app.Constants;
 import com.geozen.whiterim.app.SmartTrailApplication;
 import com.geozen.whiterim.model.Condition;
 import com.geozen.whiterim.model.TrailDataSet;
 import com.geozen.whiterim.provider.SmartTrailSchema.AreasColumns;
 import com.geozen.whiterim.provider.SmartTrailSchema.AreasSchema;
 import com.geozen.whiterim.provider.SmartTrailSchema.RegionsSchema;
 import com.geozen.whiterim.provider.SmartTrailSchema.TrailsColumns;
 import com.geozen.whiterim.provider.SmartTrailSchema.TrailsSchema;
 import com.geozen.whiterim.service.SyncService;
 import com.geozen.whiterim.ui.phone.TrailDetailActivity;
 import com.geozen.whiterim.util.ActivityHelper;
 import com.geozen.whiterim.util.AppLog;
 import com.geozen.whiterim.util.DetachableResultReceiver;
 import com.geozen.whiterim.util.NotifyingAsyncQueryHandler;
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapView;
 
 public class TrailMapActivity extends MapActivity implements
 		NotifyingAsyncQueryHandler.AsyncQueryListener, OnRefreshStatusListener,
 		DetachableResultReceiver.Receiver {
 
 	static final String EXTRA_TRAIL_ID = "com.geozen.whiterim.extra.TRAIL_ID";
 	static final String EXTRA_AREA_ID = "com.geozen.whiterim.extra.AREA_ID";
 	private static final String CLASSTAG = "TrailMapActivity";
 
 	final ActivityHelper mActivityHelper = ActivityHelper.createInstance(this);
 
 	private MapView mMapView;
 
 	private FixedMyLocationOverlay mMyLocationOverlay;
 
 	private TrailMapData mTrailMapData;
 
 	private SetupTrailsTask mSetupTrailsTask;
 
 	private boolean mSyncing;
 	private DetachableResultReceiver mReceiver;
 	private NotifyingAsyncQueryHandler mHandler;
 
 	private String[] mAreaNames;
 	private String[] mAreaIds;
 	//private boolean[] mAreaSelections;
 	private AlertDialog mAreaSelectDialog;
 	private AlertDialog mTrailSelectDialog;
 
 	private String mAreaId;
 	private int mAreaSelection;
 	// private int mTmpAreaSelection;
 	private String mRegionId;
 	TrailHeadItemizedOverlay mTrailHeadOverlay;
 
 	private int mTrailSelection = 0;
 	private int mSelectType = 0;
 	private String[] mTrailNames;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_map);
 
 		mMapView = (MapView) findViewById(R.id.mapview);
 		getActivityHelper().setupActionBar(null, 0);
 
 		mMapView.setEnabled(true);
 		mMapView.setLongClickable(true);
 
 		mMapView.setBuiltInZoomControls(true);
 		mMyLocationOverlay = new FixedMyLocationOverlay(this, mMapView);
 
 		mMapView.getController().setZoom(12);
 
 		mMapView.getOverlays().add(mMyLocationOverlay);
 
 		Bundle extras = getIntent().getExtras();
 
 		// String areaId;
 
 		SmartTrailApplication app = (SmartTrailApplication) getApplication();
 
 		mAreaId = extras.getString(EXTRA_AREA_ID);
 
 		if (mAreaId == null) {
 			mAreaId = app.getArea();
 		} else {
 			app.setArea(mAreaId);
 		}
 
 		mRegionId = app.getRegion();
 
 		mHandler = new NotifyingAsyncQueryHandler(getContentResolver(), this);
 		// mHandler.cancelOperation(TrailsQuery._TOKEN);
 		// mHandler.cancelOperation(AreasQuery._TOKEN);
 
 		//
 		// trailMapData cannot have any activity references or there will be a
 		// memory leak!
 		//
 		TrailMapData trailMapData = (TrailMapData) getLastNonConfigurationInstance();
 
 		if (trailMapData == null
 				|| (trailMapData != null && (!trailMapData.mComplete || !trailMapData.mAreaId
 						.equals(mAreaId)))) {
 			refreshMapData();
 
 		} else {
 			mTrailMapData = trailMapData;
 			mMapView.getOverlays().addAll(mTrailMapData.mTrailOverlays);
 
 			createTrailHeadOverlay();
 			mMapView.getOverlays().add(mTrailHeadOverlay);
 			mMapView.invalidate();
 			mMapView.getController().animateTo(mTrailMapData.mCenterGeoPoint);
 			int N = mTrailMapData.mTrailHeads.size();
 			mTrailNames = new String[N];
 			for (int i=0; i < N; i++) {
 				mTrailNames[i] = mTrailMapData.mTrailHeads.get(i).getTitle();
 			}
 		}
 
 		// catch sync updates.
 		mReceiver = new DetachableResultReceiver(new Handler());
 		mReceiver.setReceiver(this);
 
 		mHandler.startQuery(AreasQuery._TOKEN,
 				RegionsSchema.buildAreasUri(mRegionId), AreasQuery.PROJECTION);
 	}
 
 	private void refreshMapData() {
 		mTrailMapData = new TrailMapData();
 		mTrailMapData.mAreaId = mAreaId;
 
 		mHandler.startQuery(TrailsQuery._TOKEN,
 				RegionsSchema.buildTrailsUri(mRegionId), TrailsQuery.PROJECTION);
 	}
 
 	@Override
 	protected void onPostCreate(Bundle savedInstanceState) {
 		super.onPostCreate(savedInstanceState);
 		mActivityHelper.onPostCreate(savedInstanceState);
 	}
 
 	protected void onResume() {
 		super.onResume();
 
 		mMyLocationOverlay.enableMyLocation();
 		mMyLocationOverlay.enableCompass();
 
 	}
 
 	protected void onPause() {
 		super.onPause();
 		mMyLocationOverlay.disableMyLocation();
 		mMyLocationOverlay.disableCompass();
 
 	}
 
 	@Override
 	protected boolean isRouteDisplayed() {
 		return false;
 	}
 
 	public TrailDataSet loadDataset(String regionId, String mapId)
 			throws IOException, ParserConfigurationException, SAXException {
 		if (!TextUtils.isEmpty(regionId) && !TextUtils.isEmpty(mapId)) {
 			File dir = Environment.getExternalStorageDirectory();
 			File mapFile = new File(dir, Constants.CACHE_DIR + "/" + regionId
 					+ "/" + mapId + ".gpx");
 
 			// TrailDataSet dataSet = loadDatasetFromCache(mapFile);
 			TrailDataSet dataSet = loadDatasetFromAssets(mapFile);
 
 			if (dataSet != null) {
 				return dataSet;
 			} else {
 				mapFile = new File(dir, Constants.CACHE_DIR + "/" + regionId
 						+ "/" + mapId + ".gpx");
 				return null;
 			}
 		} else {
 			return null;
 		}
 	}
 
 	public TrailDataSet loadDatasetFromCache(File file) throws IOException,
 			ParserConfigurationException, SAXException {
 
 		FileInputStream is = new FileInputStream(file);
 		TrailDataSet dataSet = null;
 
 		if (is != null) {
 			try {
 				InputSource source = new InputSource(is);
 				dataSet = TrailDataSet.parseGpxDataSet(source);
 			} finally {
 
 				is.close();
 			}
 		}
 
 		return dataSet;
 	}
 
 	public TrailDataSet loadDatasetFromAssets(File file) throws IOException,
 			ParserConfigurationException, SAXException {
 
 		InputStream is = null;
 		TrailDataSet dataSet = null;
 
 		String fileName = file.getCanonicalPath().replace("/mnt/sdcard/", "");
 		is = getAssets().open(fileName);
 		if (is != null) {
 			try {
 				InputSource source = new InputSource(is);
 				dataSet = TrailDataSet.parseGpxDataSet(source);
 			} finally {
 
 				is.close();
 			}
 		}
 
 		return dataSet;
 	}
 
 	String getAreaName(String areaId) {
 		ContentResolver resolver = getContentResolver();
 		Cursor cursor = resolver.query(AreasSchema.buildAreaUri(areaId),
 				new String[] { AreasColumns.NAME }, null, null, null);
 		String areaName = "";
 		if (cursor != null) {
 
 			try {
 				while (cursor.moveToNext()) {
 					areaName = cursor.getString(0);
 				}
 			} finally {
 				cursor.close();
 			}
 		}
 
 		return areaName;
 	}
 
 	/**
 	 * 
 	 * @param areaId
 	 */
 	void setupTrailOverlays(String areaId) {
 
 		String where = TrailsColumns.AREA_ID + " = '" + areaId + "'";
 		ContentResolver resolver = getContentResolver();
 		Cursor cursor = resolver.query(TrailsSchema.CONTENT_URI, new String[] {
 				TrailsColumns.AREA_ID, TrailsColumns.TRAIL_ID,
 				TrailsColumns.MAP_ID, TrailsColumns.NAME,
 				TrailsColumns.CONDITION }, where, null, TrailsColumns.NAME
 				+ " COLLATE LOCALIZED ASC");
 
 		if (cursor != null) {
 
 			try {
 				String trailName;
 				String aMapId;
 				String aTrailId;
 				String status;
 				mTrailNames = new String[cursor.getCount()];
 				// int latSum = 0;
 				// int lonSum = 0;
 				// int n = 0;
 				int i = 0;
 				while (cursor.moveToNext()) {
 					trailName = cursor.getString(cursor
 							.getColumnIndex(TrailsColumns.NAME));
 					mTrailNames[i++] = trailName;
 					aTrailId = cursor.getString(cursor
 							.getColumnIndex(TrailsColumns.TRAIL_ID));
 					aMapId = cursor.getString(cursor
 							.getColumnIndex(TrailsColumns.MAP_ID));
 					status = cursor.getString(cursor
 							.getColumnIndex(TrailsColumns.CONDITION));
 
 					try {
 						TrailDataSet dataSet = loadDataset(mRegionId, aMapId);
 
 						if (dataSet != null) {
 							// n++;
 							// GeoPoint center = dataSet.getCenter();
 							// latSum = latSum + center.getLatitudeE6();
 							// lonSum = lonSum + center.getLongitudeE6();
 
 							TrailOverlay overlay = new TrailOverlay(areaId,
 									aTrailId, dataSet);
 							overlay.setTrailName(trailName);
 							overlay.setTrailColor(Condition.getStatusColor(
 									this, status));
 
 							mTrailMapData.mTrailOverlays.add(overlay);
 						}
 
 					} catch (IOException e) {
 						AppLog.e("setupTrailsOverlay Exception:", e);
 					} catch (ParserConfigurationException e) {
 						AppLog.e("setupTrailsOverlay Exception:", e);
 					} catch (SAXException e) {
 						AppLog.e("setupTrailsOverlay Exception:", e);
 					}
 
 				}
 
 				// if (n > 0) {
 				// mTrailMapData.mCenterGeoPoint = new GeoPoint(latSum / n,
 				// lonSum / n);
 				// }
 				mTrailMapData.mCenterGeoPoint = mTrailMapData.mTrailOverlays
 						.get(0).getCenter();
 
 				
 
 			} finally {
 				cursor.close();
 			}
 
 		}
 	}
 
 	@Override
 	public Object onRetainNonConfigurationInstance() {
 		if (mSetupTrailsTask != null) {
 			mSetupTrailsTask.cancel(true);
 			mSetupTrailsTask = null;
 		}
 
 		if (mTrailMapData.mComplete) {
 			return mTrailMapData;
 		} else {
 			return null;
 		}
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.map_menu_items, menu);
 		// getMenuInflater().inflate(R.menu.refresh_menu_items, menu);
 
 		return mActivityHelper.onCreateOptionsMenu(menu)
 				|| super.onCreateOptionsMenu(menu);
 	}
 
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu) {
 		return mActivityHelper.onPrepareOptionsMenu(menu)
 				|| super.onPrepareOptionsMenu(menu);
 
 	}
 
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Intent intent;
 		switch (item.getItemId()) {
 
 		case R.id.menu_map:
 			if (mSelectType == 0) {
 				mAreaSelectDialog.show();
 			} else if (mSelectType == 1) {
 				mTrailSelectDialog = new AlertDialog.Builder(this)
 				.setTitle(getText(R.string.selectTrail))
 
 				.setSingleChoiceItems(mTrailNames, mTrailSelection,
 						new DialogInterface.OnClickListener() {
 
 							@Override
 							public void onClick(DialogInterface dialog,
 									int which) {
 								selectTrail(which);
 								mTrailSelectDialog.dismiss();
 							}
 						})
 
 				.create();
 				mTrailSelectDialog.show();
 			}
 
 			return true;
 
 		case R.id.myLocation:
 			animateToCurrentLocation();
 			return true;
 
 		case R.id.menu_refresh:
 			triggerRefresh();
 			return true;
 
 		default:
 			return mActivityHelper.onOptionsItemSelected(item)
 					|| super.onOptionsItemSelected(item);
 
 		}
 
 	}
 
 	public void animateToCurrentLocation() {
 		if (mMyLocationOverlay.isMyLocationEnabled()) {
 			GeoPoint currentLocation = mMyLocationOverlay.getMyLocation();
 			if (currentLocation != null) {
 				mMapView.getController().animateTo(currentLocation);
 			}
 		}
 	}
 
 	//
 	// Base activity stuff
 	//
 
 	@Override
 	public void onRefreshStatusChange(boolean status) {
 		getActivityHelper().setRefreshActionButtonCompatState(status);
 		refreshMapData();
 	}
 
 	protected void triggerRefresh() {
 		final Intent intent = new Intent(Intent.ACTION_SYNC, null, this,
 				SyncService.class);
 		intent.putExtra(SyncService.EXTRA_STATUS_RECEIVER, mReceiver);
 		startService(intent);
 
 	}
 
 	@Override
 	public boolean onKeyLongPress(int keyCode, KeyEvent event) {
 		return mActivityHelper.onKeyLongPress(keyCode, event)
 				|| super.onKeyLongPress(keyCode, event);
 	}
 
 	/**
 	 * Returns the {@link ActivityHelper} object associated with this activity.
 	 */
 	protected ActivityHelper getActivityHelper() {
 		return mActivityHelper;
 	}
 
 	@Override
 	public void onReceiveResult(int resultCode, Bundle resultData) {
 		switch (resultCode) {
 		case SyncService.STATUS_RUNNING: {
 			mSyncing = true;
 			break;
 		}
 		case SyncService.STATUS_FINISHED: {
 			mSyncing = false;
 			break;
 		}
 		case SyncService.STATUS_ERROR: {
 			// Error happened down in SyncService, show as toast.
 			mSyncing = false;
 			final String errorText = getString(R.string.toast_sync_error,
 					resultData.getString(Intent.EXTRA_TEXT));
 			Toast.makeText(this, errorText, Toast.LENGTH_LONG).show();
 			break;
 		}
 		}
 
 		onRefreshStatusChange(mSyncing);
 
 	}
 
 	class TrailMapData {
 		public GeoPoint mCenterGeoPoint;
 		String mAreaId;
 		String mTrailId;
 		boolean mComplete;
 
 		ArrayList<TrailOverlay> mTrailOverlays;
 		ArrayList<TrailHead> mTrailHeads;
 
 		TrailMapData() {
 			mAreaId = "";
 			mTrailId = "";
 			mTrailOverlays = new ArrayList<TrailOverlay>();
 			mTrailHeads = new ArrayList<TrailHead>();
 			mComplete = false;
 		}
 
 		void clear() {
 			mComplete = false;
 			mTrailOverlays.clear();
 			mCenterGeoPoint = null;
 		}
 	}
 
 	public void createTrailHeadOverlay() {
 		mTrailHeadOverlay = new TrailHeadItemizedOverlay(getResources()
 				.getDrawable(R.drawable.trailhead), TrailMapActivity.this,
 				mTrailMapData.mTrailHeads);
 
 		mTrailHeadOverlay
 				.setOnTrailHeadClickListener(new OnTrailHeadClickListener() {
 
 					@Override
 					public boolean onClick(int index, TrailHead th) {
 
 						Intent intent = new Intent(TrailMapActivity.this,
 								TrailDetailActivity.class);
 						final Uri trailsUri = TrailsSchema
 								.buildUri(th.mTrailId);
 						intent.setData(trailsUri);
 						final Uri areaUri = AreasSchema
 								.buildAreaUri(mTrailMapData.mAreaId);
 						intent.putExtra(TrailDetailFragment.EXTRA_AREA, areaUri);
 						startActivity(intent);
 
 						return true;
 					}
 				});
 	}
 
 	@Override
 	public void onQueryComplete(int token, Object cookie, Cursor cursor) {
 		if (token == TrailsQuery._TOKEN) {
 			onTrailQueryComplete(cursor);
 		} else if (token == AreasQuery._TOKEN) {
 			onAreaQueryComplete(cursor);
 		} else {
 			AppLog.d(CLASSTAG, "Query complete, Not Actionable: " + token);
 			cursor.close();
 		}
 
 	}
 
 	/**
 	 * Handle {@link TrailsQuery} {@link Cursor}. Populate the trailHeads list.
 	 */
 	private void onTrailQueryComplete(Cursor cursor) {
 		if (cursor != null) {
 
 			mTrailMapData.mTrailHeads.clear();
 			try {
 
 				String trailId;
 				String trailName;
 
 				GeoPoint head;
 				while (cursor.moveToNext()) {
 
 					trailId = cursor.getString(TrailsQuery.TRAIL_ID);
 					trailName = cursor.getString(TrailsQuery.TRAIL_NAME);
 
 					head = new GeoPoint(
 							cursor.getInt(TrailsQuery.TRAIL_HEAD_LAT_E6),
 							cursor.getInt(TrailsQuery.TRAIL_HEAD_LON_E6));
 
 					String areaId = cursor.getString(TrailsQuery.TRAIL_AREA_ID);
 
 					//
 					// get area name and rating from the database.
 					//
 					ContentResolver resolver = getContentResolver();
 					Cursor areaCursor = resolver.query(
 							AreasSchema.buildAreaUri(areaId), new String[] {
 									AreasColumns.NAME, AreasColumns.RATING },
 							null, null, null);
 					String areaName = "";
 					float areaRating = 0.0f;
 					if (areaCursor != null) {
 
 						try {
 							if (areaCursor.moveToNext()) {
 								areaName = areaCursor.getString(0);
 								areaRating = areaCursor.getFloat(1);
 							}
 						} finally {
 							areaCursor.close();
 						}
 					}
 
 					TrailHead th = new TrailHead(trailId, areaId, head,
 							trailName, areaName, areaRating);
 
 					mTrailMapData.mTrailHeads.add(th);
 				}
 
 				mSetupTrailsTask = new SetupTrailsTask();
 				mSetupTrailsTask
 						.execute(new String[] { mTrailMapData.mAreaId });
 
 			} finally {
 				cursor.close();
 			}
 
 		}
 	}
 
 	/**
 	 * Handle {@link AreasQuery} {@link Cursor}.
 	 */
 	private void onAreaQueryComplete(Cursor cursor) {
 		if (cursor != null) {
 
 			//
 			// Construct arrays from the trails cursor for the trails
 			// selection dialog.
 			//
 			mAreaNames = new String[cursor.getCount()];
 			mAreaIds = new String[cursor.getCount()];
 			//mAreaSelections = new boolean[mAreaNames.length];
 
 			try {
 				int i = 0;
 				while (cursor.moveToNext()) {
 					mAreaIds[i] = cursor.getString(AreasQuery.AREA_ID);
 					mAreaNames[i] = cursor.getString(AreasQuery.AREA_NAME);
 					if (mAreaIds[i].equals(mAreaId)) {
 					//	mAreaSelections[i] = true;
 						mAreaSelection = i;
 					}
 					i++;
 				}
 				if (cursor.getCount() > 1) {
 					//final boolean[] tmpSelections = new boolean[mAreaSelections.length];
 
 //					System.arraycopy(mAreaSelections, 0, tmpSelections, 0,
 //							mAreaSelections.length);
 
 					mAreaSelectDialog = new AlertDialog.Builder(this)
 							.setTitle(getText(R.string.selectAreasTitle))
 
 							.setSingleChoiceItems(mAreaNames, mAreaSelection,
 									new DialogInterface.OnClickListener() {
 
 										@Override
 										public void onClick(
 												DialogInterface dialog,
 												int which) {
 											mAreaSelection = which;
 
 											selectArea(mAreaIds[mAreaSelection]);
 											mAreaSelectDialog.dismiss();
 										}
 									})
 
 							.create();
 					mSelectType = 0;
 				} else {
 					mSelectType = 1;
 				}
 			} finally {
 				cursor.close();
 			}
 
 		}
 	}
 
 	public void selectArea(String areaId) {
 
 		SmartTrailApplication app = (SmartTrailApplication) TrailMapActivity.this
 				.getApplication();
 
 		app.setArea(areaId);
 		if (mSetupTrailsTask != null) {
 			mSetupTrailsTask.cancel(true);
 		}
 		mSetupTrailsTask = new SetupTrailsTask();
 
 		mSetupTrailsTask.execute(new String[] { areaId });
 	}
 
 	public void selectTrail(int trailIndex) {
 		mTrailSelection = trailIndex;
 		mTrailMapData.mTrailOverlays.get(trailIndex).getCenter();
 		mMapView.getController().animateTo(mTrailMapData.mTrailOverlays.get(trailIndex).mTrailDataSet.getStart());
 	}
 
 	/**
 	 * {@link com.geozen.whiterim.provider.SmartTrailSchema.AreasSchema} query
 	 * parameters.
 	 */
 	private interface TrailsQuery {
 		int _TOKEN = 0x1;
 
 		String[] PROJECTION = { BaseColumns._ID, TrailsColumns.TRAIL_ID,
 				TrailsColumns.NAME, TrailsColumns.HEAD_LAT_E6,
 				TrailsColumns.HEAD_LON_E6, TrailsColumns.AREA_ID,
 				TrailsColumns.REGION_ID };
 
 		@SuppressWarnings("unused")
 		int _ID = 0;
 		int TRAIL_ID = 1;
 		int TRAIL_NAME = 2;
 		int TRAIL_HEAD_LAT_E6 = 3;
 		int TRAIL_HEAD_LON_E6 = 4;
 		int TRAIL_AREA_ID = 5;
 		@SuppressWarnings("unused")
 		int TRAIL_REGION_ID = 6;
 	}
 
 	/**
 	 * {@link com.geozen.whiterim.provider.SmartTrailSchema.AreasSchema} query
 	 * parameters.
 	 */
 	private interface AreasQuery {
 		int _TOKEN = 0x2;
 
 		String[] PROJECTION = { BaseColumns._ID, AreasColumns.AREA_ID,
 				AreasColumns.NAME };
 
 		@SuppressWarnings("unused")
 		int _ID = 0;
 		int AREA_ID = 1;
 		int AREA_NAME = 2;
 	}
 
 	public MapView getMapView() {
 
 		return mMapView;
 	}
 
 	/**
 	 * 
 	 * @author matt
 	 * 
 	 */
 	private class SetupTrailsTask extends AsyncTask<String[], Void, Boolean> {
 
 		// private final String CLASSTAG = "SetupTrailsTask";
 
 		// private Exception mReason;
 		private final ProgressDialog dialog = new ProgressDialog(
 				TrailMapActivity.this);
 
 		@Override
 		protected void onPreExecute() {
 
 			// view might have been shutdown with a device rotation
 			if (mSetupTrailsTask != null) {
 				this.dialog.setMessage(getText(R.string.loadingTrails));
 				this.dialog.show();
 			}
 
 		}
 
 		@Override
 		protected Boolean doInBackground(String[]... params) {
 
 			String[] areas = params[0];
 			mTrailMapData.mComplete = false;
 			mTrailMapData.clear();
 
 			for (String areaId : areas) {
 				setupTrailOverlays(areaId);
 			}
 
 			return true;
 		}
 
 		@Override
 		protected void onPostExecute(Boolean success) {
 
 			// progress.setVisibility(View.INVISIBLE);
 
 			mMapView.getOverlays().clear();
 			mMapView.getOverlays().addAll(mTrailMapData.mTrailOverlays);
 
 			// create new trailHead overlay
 			// (due to threading issue this must be done in the gui thread)
 			createTrailHeadOverlay();
 			mTrailMapData.mComplete = true;
 
 			MapView mapView = mMapView;
 
 			mapView.getOverlays().add(mTrailHeadOverlay);
 			mapView.getOverlays().add(mMyLocationOverlay);
 
 			mapView.invalidate();
 
 			if (mTrailMapData.mCenterGeoPoint != null) {
 				mapView.getController()
 						.animateTo(mTrailMapData.mCenterGeoPoint);
 			}
 
 			if (mSetupTrailsTask != null && dialog.isShowing()) {
 				dialog.dismiss();
 			}
 		}
 
 		@Override
 		protected void onCancelled() {
 			if (mSetupTrailsTask != null && dialog.isShowing()) {
 				dialog.dismiss();
 			}
 		}
 	}
 }
