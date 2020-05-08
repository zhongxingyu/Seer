 package ie.appz.popupplaces;
 
 import java.util.List;
 
 import android.app.Dialog;
 import android.content.Context;
 import android.database.Cursor;
 import android.graphics.Canvas;
 import android.graphics.drawable.Drawable;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MotionEvent;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Toast;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapView;
 import com.google.android.maps.Overlay;
 import com.google.android.maps.OverlayItem;
 
 public class ReminderMap_Activity extends MapActivity {
 
 	private MapView mapView;
 	private PlaceOpenHelper placeOpenHelper;
	private MapOverlay mapOverlay;
 
 	public class MapOverlay extends com.google.android.maps.Overlay {
 		@Override
 		public boolean draw(Canvas canvas, MapView mapView, boolean shadow,
 				long when) {
 			super.draw(canvas, mapView, shadow);
 			return true;
 		}
 
 		boolean tap = false;
 
 		/*
 		 * This code filters out pinch-zoom motions from being mistaken for taps
 		 * read more here: http://j.mp/TrNFBo
 		 */
 		@Override
 		public boolean onTouchEvent(MotionEvent motionEvent, MapView mv) {
 			if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
 				tap = true;
 			}
 			if (motionEvent.getAction() == MotionEvent.ACTION_MOVE
 					&& motionEvent.getPointerCount() > 1) {
 				tap = false;
 			}
 			return super.onTouchEvent(motionEvent, mv);
 
 		}
 
 		@Override
 		public boolean onTap(final GeoPoint tappedPoint, MapView mapView) {
 			if (tap == true) {
 				final Dialog dialog = new Dialog(ReminderMap_Activity.this);
 				dialog.setContentView(R.layout.newplacedialog_layout);
 				dialog.setTitle(R.string.newplacedialog_text);
 				dialog.setCancelable(true);
 
 				final EditText inputBox = (EditText) dialog
 						.findViewById(R.id.editText1);
 
 				/* Click on the Save Button */
 				Button saveButton = (Button) dialog
 						.findViewById(R.id.saveButton);
 				saveButton.setOnClickListener(new View.OnClickListener() {
 
 					@Override
 					public void onClick(View arg0) {
 
 						String popupNote = inputBox.getText().toString();
 						if (popupNote.length() > 0) {
 							placeOpenHelper.addPlace(tappedPoint, popupNote);
 
							Toast toast = Toast.makeText(
									getApplicationContext(),
									tappedPoint.toString() + ", " + popupNote,
									Toast.LENGTH_LONG);
 							toast.show();
 							drawPlaces();
 						}
 						dialog.dismiss();
 
 					}
 				});
 
 				/* Click on the Cancel Button */
 				Button cancelButton = (Button) dialog
 						.findViewById(R.id.cancelButton);
 				cancelButton.setOnClickListener(new View.OnClickListener() {
 
 					@Override
 					public void onClick(View arg0) {
 
 						dialog.cancel();
 
 					}
 				});
 				dialog.show();
 				return true;
 			} else
 				return false;
 		}
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.remindermap_layout);
 		mapView = (MapView) findViewById(R.id.mapview);
 		mapView.setBuiltInZoomControls(true);
 		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 		GeoPoint oldGeo = new GeoPoint((int) (locationManager
 				.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
 				.getLatitude() * 1E6), (int) (locationManager
 				.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
 				.getLongitude() * 1E6));
 
 		mapView.getController().animateTo(oldGeo);
 		mapView.getController().setZoom(14);
 
		mapOverlay = new MapOverlay();
 		List<Overlay> listOfOverlays = mapView.getOverlays();
 		listOfOverlays.clear();
 		listOfOverlays.add(mapOverlay);
 
 		mapView.invalidate();
 		placeOpenHelper = new PlaceOpenHelper(this);
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		drawPlaces();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.remindermap_menu, menu);
 		return true;
 	}
 
 	@Override
 	protected boolean isRouteDisplayed() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	void drawPlaces() {
 		PlaceOpenHelper placeOpenHelper = new PlaceOpenHelper(this);
 		Cursor placeCursor = placeOpenHelper.getPlaces();
 		Drawable drawable = getResources().getDrawable(R.drawable.ic_launcher);
 
 		PlacesItemizedOverlay placeOverlay = new PlacesItemizedOverlay(
 				drawable, this);
 
 		if (placeCursor.moveToFirst()) {
 			do {
 				placeOverlay.addOverlay(new OverlayItem(new GeoPoint(
 						placeCursor.getInt(0), placeCursor.getInt(1)),
 						"Popup Note:", placeCursor.getString(2)));
 			} while (placeCursor.moveToNext());
 			placeCursor.close();
			placeOpenHelper.close();

			mapOverlay = new MapOverlay();
			mapView.getOverlays().clear();

			mapView.getOverlays().add(mapOverlay);
 			mapView.getOverlays().add(placeOverlay);
 			mapView.invalidate();
 		}
 	}

 }
