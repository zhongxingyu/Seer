 package edu.upenn.cis573;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.ItemizedOverlay;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MapView;
 import com.google.android.maps.Overlay;
 import com.google.android.maps.OverlayItem;
 
 //import edu.upenn.cis573.R;
 
 import android.app.AlertDialog;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Point;
 import android.graphics.drawable.BitmapDrawable;
 import android.graphics.drawable.Drawable;
 import android.location.Address;
 import android.location.Criteria;
 import android.location.Geocoder;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.support.v4.app.FragmentTransaction;
 import android.widget.LinearLayout;
 import android.widget.Toast;
 
 public class CustomMap extends MapActivity {
 
 	LinearLayout linearLayout;
 	MapView mapView;
 	MapController mc;
 	GeoPoint p;
 	GeoPoint q;
 	GeoPoint avg;
 	Drawable drawableRed, drawableBlue;
 	Preferences pref;
 	private ArrayList<StudySpace> olist;
 	
 	Boolean isInternetPresent = false;
 
 	// Connection detector class
 	ConnectionDetector cd;
 
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		Intent i = super.getIntent();
 		pref = (Preferences) i.getSerializableExtra("PREFERENCES");
 		olist = (ArrayList<StudySpace>) i.getSerializableExtra("STUDYSPACELIST");
 
 		cd = new ConnectionDetector(getApplicationContext());
 
 		// get Internet status
 		isInternetPresent = cd.isConnectingToInternet();
 
 		// check for Internet status
 		if (isInternetPresent) {
 			// Internet Connection is Present
 			// make HTTP requests
 			//            showAlertDialog(SplashScreen.this, "Internet Connection",
 			//                    "You have internet connection", true);
 
 			setContentView(R.layout.mapview);
 			mapView = (MapView) findViewById(R.id.mapview);
 			mapView.setBuiltInZoomControls(true);
 
 			drawableBlue = this.getResources().getDrawable(R.drawable.pushpin_blue);
 			drawableRed = this.getResources().getDrawable(R.drawable.pushpin_red);
 
 
 			double avgLong = 0;
 			double avgLat = 0;
 
 			LocationManager locationManager = (LocationManager) this
 					.getSystemService(Context.LOCATION_SERVICE);
 
 			Criteria _criteria = new Criteria();
 			// _criteria.setAccuracy(Criteria.ACCURACY_LOW);
 			PendingIntent _pIntent = PendingIntent.getBroadcast(
 					getApplicationContext(), 0, getIntent(), 0);
 			locationManager.requestSingleUpdate(_criteria, _pIntent);
 
 			String _bestProvider = locationManager.getBestProvider(_criteria, true);
 			Location location = locationManager.getLastKnownLocation(_bestProvider);
 
 			LocationListener loc_listener = new LocationListener() {
 				public void onLocationChanged(Location l) {
 				}
 
 				public void onProviderEnabled(String p) {
 				}
 
 				public void onProviderDisabled(String p) {
 				}
 
 				public void onStatusChanged(String p, int status, Bundle extras) {
 				}
 			};
 			locationManager.requestLocationUpdates(_bestProvider, 0, 0,
 					loc_listener);
 			location = locationManager.getLastKnownLocation(_bestProvider);
 
 			if (location != null) {
 				double gpsLat = location.getLatitude();
 				double gpsLong = location.getLongitude();
 
 				avgLat += gpsLat;
 				avgLong += gpsLong;
 
 				q = new GeoPoint((int) (gpsLat * 1E6), (int) (gpsLong * 1E6));
 
 				OverlayItem overlayitem = new OverlayItem(q, "", "");
 				PinOverlay pinsBlue = new PinOverlay(drawableBlue);
 				pinsBlue.addOverlay(overlayitem, null);
 				mapView.getOverlays().add(pinsBlue);
 			}
 
 			/*
 			 * MapOverlay mapOverlay = new MapOverlay(); List<Overlay>
 			 * listOfOverlays = mapView.getOverlays(); listOfOverlays.clear();
 			 * listOfOverlays.add(mapOverlay);
 			 */
 			PinOverlay pinsRed = new PinOverlay(drawableRed);
 			for (StudySpace o: olist) {
 				double longitude = o.getSpaceLongitude();
 				double latitude = o.getSpaceLatitude();
 	
 				p = new GeoPoint((int) (latitude * 1E6), (int) (longitude * 1E6));
 				OverlayItem overlayitem = new OverlayItem(p, "", "");
 				pinsRed.addOverlay(overlayitem, o);
 	
 				avgLong += longitude;
 				avgLat += latitude;
 			}
 			mapView.getOverlays().add(pinsRed);
 
 			avgLong /= olist.size() + 1;
 			avgLat /= olist.size() + 1;
 			avg = new GeoPoint((int) (avgLat * 1E6), (int) (avgLong * 1E6));
 
 			mc = mapView.getController();
 			mc.animateTo(avg);
 			mc.setZoom(17);
 		} else {
 			// Internet connection is not present
 			// Ask user to connect to Internet
 			cd.showAlertDialog(CustomMap.this, "No Internet Connection",
 					"You don't have internet connection.", false);
 		}
 
 	}
 	/*
 	 * class MapOverlay extends Overlay {
 	 * 
 	 * @Override public boolean draw(Canvas canvas, MapView mapView, boolean
 	 * shadow, long when) { super.draw(canvas, mapView, shadow);
 	 * 
 	 * //---translate the GeoPoint to screen pixels--- Point screenPts = new
 	 * Point(); mapView.getProjection().toPixels(p, screenPts);
 	 * 
 	 * //---add the marker--- Bitmap bmp = BitmapFactory.decodeResource(
 	 * getResources(), R.drawable.pushpin); //Positions the image
 	 * canvas.drawBitmap(bmp, screenPts.x-10, screenPts.y-34, null); return
 	 * true; } }
 	 */
 
 	@Override
 	protected boolean isRouteDisplayed() {
 		return false;
 	}
 	public class PinOverlay extends ItemizedOverlay<OverlayItem>{
 
 		private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
 		private ArrayList<StudySpace> mSpaces = new ArrayList<StudySpace>();
 		
 		public PinOverlay(Drawable defaultMarker) {
 			super(boundCenterBottom(defaultMarker));
 		}
 
 		public void addOverlay(OverlayItem overlay, StudySpace space) {
 			mOverlays.add(overlay);
 			mSpaces.add(space);
 			populate();
 		}
 
 		@Override
 		protected OverlayItem createItem(int i) {
 			return mOverlays.get(i);
 		}
 
 
 		@Override
 		public int size() {
 			return mOverlays.size();
 		}
 
 		@Override
 		protected boolean onTap(int index) {
 			//called when an item is tapped
 			OverlayItem item = mOverlays.get(index);
			StudySpace space = mSpaces.get(index);
 			if (space == null)
 				return true;
 			Geocoder geoCoder = new Geocoder(
 					getBaseContext(), Locale.getDefault());
 			try {
 				List<Address> addresses = geoCoder.getFromLocation(
 						space.getSpaceLatitude(), space.getSpaceLongitude(), 1); 
 
 				String add = "";
 				if (addresses.size() > 0) 
 				{
 					for (int i=0; i<addresses.get(0).getMaxAddressLineIndex(); 
 							i++)
 						add += addresses.get(0).getAddressLine(i) + "\n";
 				}
 
 				AlertDialog.Builder builder = new AlertDialog.Builder(CustomMap.this);
 				builder.setTitle("Location Information");
 				builder.setMessage(space.getBuildingName() + ": " + space.getSpaceName() + 
 						"\n" + add + "Distance: " + Math.round(space.getDistance()) + " m");
 				builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int id) {
 						dialog.cancel();
 					}
 				});
				builder.setNegativeButton("Return", new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int id) {
						finish();			
 					}
 				});
 				AlertDialog alert = builder.create();
 				alert.show();
 
 			}
 			catch (IOException e) {                
 				e.printStackTrace();
 			}   
 			return true;
 		}
 
 	}
 
 
 }
