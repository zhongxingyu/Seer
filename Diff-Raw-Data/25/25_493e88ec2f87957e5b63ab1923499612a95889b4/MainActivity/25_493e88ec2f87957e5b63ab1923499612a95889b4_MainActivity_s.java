 package com.aboveware.abovetracker;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.graphics.Path;
 import android.graphics.Point;
 import android.location.Location;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.provider.Settings.Secure;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 import android.widget.Toast;
 
 import com.aboveware.abovetracker.TrackService.Mode;
 import com.aboveware.abovetracker.TrackService.TrackServiceListener;
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MapView;
 import com.google.android.maps.Overlay;
 import com.google.android.maps.Projection;
 
 /**
  * @author SåA
  * 
  */
 
 public class MainActivity extends MapActivity implements TrackServiceListener {
 
 	private TrackServiceConnection trackServiceConnection;
 
 	public void ClickHandler(View v) {
 		switch (v.getId()) {
 		case R.id.buttonPause:
 			if (Mode.PAUSED == trackServiceConnection.getMode()) {
 				trackServiceConnection.resume();
 			} else {
 				trackServiceConnection.pause();
 			}
 			break;
 		case R.id.buttonStart:
 			start();
 			break;
 		case R.id.buttonStop:
 			trackServiceConnection.stop();
 			break;
 		}
 		updateButtons();
 	}
 
 	private void start() {
 		if (!Secure.isLocationProviderEnabled(getContentResolver(),
 		    LocationManager.GPS_PROVIDER)) {
 			ask4Gps();
 		} else {
 			ask4Name();
 		}
 	}
 
 	private void ask4Name() {
 		final PromptDialog dlg = new PromptDialog(MainActivity.this, android.R.string.cancel, R.string.enter_name) {
 			 @Override
 			 public boolean onOkClicked(String input) {
 				 if (input.length() > 0){
 					 trackServiceConnection.start(input);
 					 updateButtons();
 				 }
 				 return true;
 			 }
 			};
 			dlg.show();
   }
 
 	private void ask4Gps() {
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder
 		    .setMessage(R.string.gps_is_not_enabled)
 		    .setCancelable(false)
 		    .setPositiveButton(android.R.string.yes,
 		        new DialogInterface.OnClickListener() {
 			        public void onClick(DialogInterface dialog, int id) {
 				        Intent intent = new Intent(
 				            android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
 				        startActivity(intent);
 			        }
 		        })
 		    .setNegativeButton(android.R.string.no,
 		        new DialogInterface.OnClickListener() {
 			        public void onClick(DialogInterface dialog, int id) {
 				        dialog.cancel();
 			        }
 		        });
 		AlertDialog alert = builder.create();
 		alert.show();
 	}
 
 	private void exit() {
 		System.runFinalizersOnExit(true);
 		System.exit(0);
 	}
 
 	public void ModeChange(Mode mode) {
 		updateButtons();
 	}

 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
		trackServiceConnection = new TrackServiceConnection(this, this);
		setContentView(R.layout.main);
 		new SmsLogger(this).execute((Void[]) null);
 		mapView = (MapView) findViewById(R.id.mapView);

 		// enable Street view by default
 		mapView.setStreetView(true);

 		// enable to show Satellite view
 		// mapView.setSatellite(true);

 		// enable to show Traffic on map
 		// mapView.setTraffic(true);
 		mapView.setBuiltInZoomControls(true);
 
 		mapController = mapView.getController();
 		mapController.setZoom(16);
 		
 		// Show'em where we're
 		LocationChange(TrackService.getLastKnownLocation(this));
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.app.Activity#onCreateContextMenu(android.view.ContextMenu,
 	 * android.view.View, android.view.ContextMenu.ContextMenuInfo)
 	 */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.app.Activity#onPrepareOptionsMenu(android.view.Menu)
 	 */
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu) {
 		for (int index = 0; index < menu.size(); ++index) {
 			if (menu.getItem(index).getItemId() == R.id.archive
 			    || menu.getItem(index).getItemId() == R.id.settings)
 				menu.getItem(index).setEnabled(
 				    Mode.STOPPED == trackServiceConnection.getMode());
 		}
 		return super.onPrepareOptionsMenu(menu);
 	}
 
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 		trackServiceConnection.Unbind();
 	}
 
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.app.Activity#onContextItemSelected(android.view.MenuItem)
 	 */
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.settings:
 			startActivity(new Intent(this, Preferences.class));
 			break;
 		case R.id.log:
 			startActivity(new Intent(this, SmsErrorActivity.class));
 			break;
 		case R.id.archive:
 			startActivity(new Intent(this, TrackListActivity.class));
 			break;
 		case R.id.exit:
 			if (Mode.STOPPED == trackServiceConnection.getMode()) {
 				exit();
 			} else {
 				AlertDialog.Builder builder = new AlertDialog.Builder(this);
 				builder
 				    .setMessage(R.string.the_tracker_is_running_exit_)
 				    .setCancelable(false)
 				    .setPositiveButton(android.R.string.yes,
 				        new DialogInterface.OnClickListener() {
 					        public void onClick(DialogInterface dialog, int id) {
 						        trackServiceConnection.stop();
 						        exit();
 					        }
 				        })
 				    .setNegativeButton(android.R.string.no,
 				        new DialogInterface.OnClickListener() {
 					        public void onClick(DialogInterface dialog, int id) {
 						        dialog.cancel();
 					        }
 				        });
 				AlertDialog alert = builder.create();
 				alert.show();
 			}
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		updateButtons();
 		Intent intent = getIntent();
 		CharSequence[] dates = intent
 		    .getCharSequenceArrayExtra(TrackListActivity.TRACK_LIST_VIEW);
 		if (null != dates) {
 			TrackDbAdapter dbHelper = new TrackDbAdapter(this);
 			dbHelper.open();
 			for (CharSequence date : dates) {
 				Cursor cursor = dbHelper.selectTrackRecords((String) date);
 				if (null != cursor) {
 
 					PolylineOverlay mapOverlay = new PolylineOverlay();
 
 					int latIndex = cursor.getColumnIndex(TrackDbAdapter.LAT);
 					int lonIndex = cursor.getColumnIndex(TrackDbAdapter.LON);
 					mapController.setZoom(16);
 					cursor.moveToFirst();
 					while (!cursor.isAfterLast()) {
 						GeoPoint point = new GeoPoint(
 						    (int) (cursor.getDouble(latIndex) * 1E6),
 						    (int) (cursor.getDouble(lonIndex) * 1E6));
 						mapOverlay.addPoint(point);
 						if (!cursor.moveToNext()) {
 							mapController.animateTo(point);
 						}
 					}
 					cursor.close();
 
 					List<Overlay> listOfOverlays = mapView.getOverlays();
 					listOfOverlays.clear();
 					listOfOverlays.add(mapOverlay);
 
 					mapView.invalidate();
 				}
 			}
 			dbHelper.close();
 		}
 	}
 
 
 	// Update "all" buttons enable state to follow the "running" state.
 	private void updateButtons() {
 		// Start button
 		Button button = (Button) findViewById(R.id.buttonStart);
 		button.setEnabled(Mode.STOPPED == trackServiceConnection.getMode());
 
 		// Stop button
 		button = (Button) findViewById(R.id.buttonStop);
 		button.setEnabled(Mode.RUNNING == trackServiceConnection.getMode()
 		    || Mode.PAUSED == trackServiceConnection.getMode());
 
 		// Pause button
 		button = (Button) findViewById(R.id.buttonPause);
 		button.setEnabled(Mode.RUNNING == trackServiceConnection.getMode()
 		    || Mode.PAUSED == trackServiceConnection.getMode());
 		button
 		    .setText(getString(Mode.PAUSED == trackServiceConnection.getMode() ? R.string.resume
 		        : R.string.pause));
 	}
 
 	@Override
   protected boolean isRouteDisplayed() {
 	  // TODO Auto-generated method stub
 	  return false;
   }
 	
 	public class PolylineOverlay extends Overlay {
 		private List<GeoPoint> polyline; // Contains set of points to be connected.
 		private Paint pathPaint = null; // Paint tool that is used to draw on the
 																		// map canvas.
 
 		public PolylineOverlay() {
 			super();
 			this.polyline = new ArrayList<GeoPoint>();
 			this.pathPaint = new Paint();
 			this.pathPaint.setAntiAlias(true);
 		}
 
 		public void addPoint(GeoPoint point) {
 			polyline.add(point);
 		}
 
 		/**
 		 * Draws the poly line route on the map the this overlay belongs to.
 		 */
 		@Override
 		public void draw(Canvas canvas, MapView mView, boolean shadow) {
 			super.draw(canvas, mView, shadow);
 
 			// Reset our paint.
 			this.pathPaint.setStrokeWidth(4);
 			this.pathPaint.setARGB(100, 113, 105, 252);
 			this.pathPaint.setStyle(Paint.Style.STROKE);
 
 			Projection projection = mView.getProjection();
 			Path routePath = new Path();
 
 			// Add each point to the routePath.
 			for (GeoPoint inPoint : polyline) {
 				Point outPoint = null;
 				outPoint = projection.toPixels(inPoint, outPoint);
 				routePath.lineTo(outPoint.x, outPoint.y);
 			}
 
 			canvas.drawPath(routePath, pathPaint);
 		}
 	}
 
 	class MapOverlay extends Overlay {
 		private List<GeoPoint> points = new ArrayList<GeoPoint>();
 
 		@Override
 		public boolean draw(Canvas canvas, MapView mapView, boolean shadow,
 		    long when) {
 			super.draw(canvas, mapView, shadow);
 			Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.red);
 
 			for (GeoPoint point : points) {
 				// convert point to pixels
 				Point screenPts = new Point();
 				mapView.getProjection().toPixels(point, screenPts);
 
 				// add marker
 				canvas.drawBitmap(bmp, screenPts.x, screenPts.y - 24, null); // 24
 				// is
 				// the
 				// height of
 				// image
 			}
 			return true;
 		}
 
 		public void addPoint(GeoPoint point) {
 			points.add(point);
 		}
 	}
 	
 	private MapController mapController;
 	private MapView mapView;
 
 	public void LocationChange(Location location) {
 		LocationChange(location.getLatitude(), location.getLongitude());
 	}
 
 	private void LocationChange(double latitude, double longitude) {
 		GeoPoint point = TrackService.geoPoint(latitude, longitude);
 		mapController.animateTo(point);
 		mapController.setZoom(16);
 
 		// add marker
 		MapOverlay mapOverlay = new MapOverlay();
 		mapOverlay.addPoint(point);
 		List<Overlay> listOfOverlays = mapView.getOverlays();
 		listOfOverlays.clear();
 		listOfOverlays.add(mapOverlay);
 
 		String address = TrackService.ConvertPointToLocation(getBaseContext(), point);
 		Toast.makeText(getBaseContext(), address, Toast.LENGTH_SHORT).show();
 
 		mapView.invalidate();
 	}
 }
