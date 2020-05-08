 package org.waynak.hackathon;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.widget.Toast;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.ItemizedOverlay;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MapView;
 import com.google.android.maps.Overlay;
 import com.google.android.maps.OverlayItem;
 import com.google.android.maps.MapActivity;
 
 import android.content.Context;
 import android.graphics.drawable.Drawable;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.util.Log;
 
 public class WaynakMapActivity extends MapActivity implements LocationListener {
 	
 	public static final String LOGTAG = "MAP";
 	
 	public static final String NYUAD = "Abu Dhabi";	
 	public static final String QASRALHOSN = "Qasr Al Hosn";
 	public static final String DALMAISLAND = "Dalma Island"; 
 	
 	LocationManager lm; 
 	MapView mv;
 	GeoPoint p;
      
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.map);
 		mv = (MapView) this.findViewById(R.id.mapview);
 		mv.setBuiltInZoomControls(true);
 		
 		// Create the overlays
 		List<Overlay> mapOverlays = mv.getOverlays();
 		
 		
 		//Drawable drawable = this.getResources().getDrawable(android.R.drawable.
 		//		drawable.androidmarker);
 		//Drawable drawable = this.getResources().getDrawable(R.drawable.waynak);
 		//drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
 		Drawable drawable = this.getResources().getDrawable(R.drawable.mapmarker);
 		drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
 		
 		MyLocationOverlay itemizedoverlay = new MyLocationOverlay(drawable, this);
 
 		/*
 		public static final String NYUAD = "Abu Dhabi";	
 		public static final String QASRALHOSN = "Qasr Al Hosn";
 		public static final String DALMAISLAND = "Dalma Island"; 
 		*/
 		// 0 NYU Abu Dhabi
 		itemizedoverlay.addOverlay(new OverlayItem(new GeoPoint((int) (24.484859 * 1000000), (int) (54.35415 * 1000000)), "NYUAD", NYUAD));
 		// 1 Qasr Al Hosn
 		itemizedoverlay.addOverlay(new OverlayItem(new GeoPoint((int) (24.482262 * 1000000), (int) (54.354874 * 1000000)), "Qasr Al Hosn", QASRALHOSN));
 		// 2 Dalma Island
 		itemizedoverlay.addOverlay(new OverlayItem(new GeoPoint((int) (24.483399 * 1000000), (int) (52.312088 * 1000000)), "Dalma Island", DALMAISLAND));
 
 		mapOverlays.add(itemizedoverlay);		
 
 		lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 6000l, 5.0f, this);	}
 	 /*
      * Standard method for menu items.  Uses res/menu/image_editor_menu.xml
      */
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
 
     	MenuInflater inflater = getMenuInflater();
     
     	inflater.inflate(R.menu.menu, menu);
 
     	return true;
     }
 
 	@Override
 	public boolean onMenuItemSelected(int featureId, MenuItem item) {
 		Intent myIntent=null;
 		// Handle item selection
 	    switch (item.getItemId()) {
 	    case R.id.cameraItem:
 	        myIntent=new Intent(getApplicationContext(),CameraActivity.class);
 	    	startActivity(myIntent);
 	    	return true;
 	    /*case R.id.recentItem:
 	    	myIntent=new Intent(getApplicationContext(),RecentActivity.class);
 	    	startActivity(myIntent);
 	        return true;*/
 	    case R.id.eventItem :
 	    	myIntent= new Intent(getApplicationContext(),EventsActivity.class);
 	    	startActivity(myIntent);
 	    	return true ;
 	    default:
 	        return super.onOptionsItemSelected(item);
 	    }
 
 	}
 
 	@Override
 	protected boolean isRouteDisplayed() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	public void onLocationChanged(Location location) {
 	Log.v("MAPTEST",location.getLatitude() + " " + location.getLongitude());
 		
		MapController mc = mv.getController();
		
		p = new GeoPoint((int) (location.getLatitude() * 1000000), (int) (location.getLongitude() * 1000000));
		mc.animateTo(p);
		//http://code.google.com/android/add-ons/google-apis/reference/com/google/android/maps/MapController.html#setZoom(int)
		mc.setZoom(14);
		
 	
 	}
 
         /*
         MyLocationOverlay myLocationOverlay = new MyLocationOverlay();
         List<Overlay> list = mv.getOverlays();
         list.add(myLocationOverlay);
         */
 
 	public void onProviderDisabled(String arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void onProviderEnabled(String arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
 		// TODO Auto-generated method stub
 		
 	}
     
 	
 
     public void onPause() {
 		super.onPause(); 
 		lm.removeUpdates(this);
 	}
 
     /*
 	class MyLocationOverlay extends com.google.android.maps.Overlay {
 	     public boolean draw(Canvas canvas, MapView mapView, boolean shadow, long when) {
              super.draw(canvas, mapView, shadow);
              Paint paint = new Paint();
              paint.setColor(Color.BLUE);
              //paint.setColor(Color.argb(0, 255, 0, 0));
              canvas.drawRect(0, 0, mv.getWidth()/2, mv.getHeight()/2, paint);
      // Converts lat/lng-Point to OUR coordinates on the screen.
      Point myScreenCoords = new Point();
      mapView.getProjection().toPixels(p, myScreenCoords);
      paint.setStrokeWidth(1);
      paint.setARGB(255, 255, 255, 255);
      paint.setStyle(Paint.Style.STROKE);
 
      canvas.drawText("Here I am...", myScreenCoords.x, myScreenCoords.y, paint);
      return true;
      }
 	}	
 */
     
     class MyLocationOverlay extends ItemizedOverlay<OverlayItem> {
 
     	public static final String LOGTAG = "LOCATIONOVERLAY";
     	
     	ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
     	Context mContext;
     	
     	public MyLocationOverlay(Drawable defaultMarker) {
     		  super(defaultMarker);
     	}
     	
     	public MyLocationOverlay(Drawable defaultMarker, Context context) {
     		  super(defaultMarker);
     		  mContext = context;
     	}
 
     	@Override
     	protected OverlayItem createItem(int i) {
     		  return mOverlays.get(i);
     	}
 
     	@Override
     	public int size() {
     		  return mOverlays.size();
     	}
     	
     	public void addOverlay(OverlayItem overlay) {
     	    mOverlays.add(overlay);
     	    populate();
     	}
     	
     	@Override
     	public boolean onTap(int index) {
     		Log.v(LOGTAG,"onTap");
     		Toast.makeText(mContext,
     				mOverlays.get(index).getSnippet(),
                     Toast.LENGTH_SHORT).show();
     		
     		// 0 NYU Abu Dhabi
     		// 1 Qasr Al Hosn
     		// 2 Dalma Island
     		/*
     		public static final String NYUAD = "NYU Abu Dhabi";	
     		public static final String QASRALHOSN = "Qasr Al Hosn";
     		public static final String DALMAISLAND = "Dalma Island"; 
     		*/
     		
     		Intent myIntent= new Intent(WaynakMapActivity.this,EventsActivity.class);
     		if (index == 0) {
         		myIntent.putExtra("place", NYUAD);	
     		} else if (index == 1) {
     			myIntent.putExtra("place", QASRALHOSN);
     		} else if (index == 2) {
     			myIntent.putExtra("place", DALMAISLAND);
     		}
         	startActivity(myIntent);
     		
     	  return true;
     	}
     	
     	public void draw(android.graphics.Canvas canvas, MapView mapView, boolean shadow) {
     		super.draw(canvas, mapView, false);
     	}    	
     }
 }
