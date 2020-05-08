 package com.hackrgt.katanalocate;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Point;
 import android.graphics.drawable.BitmapDrawable;
 import android.graphics.drawable.Drawable;
 import android.location.Address;
 import android.location.Geocoder;
 import android.location.Location;
 import android.location.LocationManager;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.WindowManager;
 import android.view.inputmethod.EditorInfo;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.*;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MapView;
 import com.google.android.maps.MyLocationOverlay;
 import com.google.android.maps.Overlay;
 import com.google.android.maps.OverlayItem;
 
 public class MapLocateActivity extends MapActivity implements OnClickListener {
     
 	private EditText mapSearchBox;
 	private MapView mapView;
 	private MyLocationOverlay myLocOverlay;
 	private Location selectedLocation;
 	private Button doneButton, searchButton;
 	
 	private ArrayList<OverlayItem> overlays;
 	private MapItemizedOverlay itemizedOverlay;
 	
 	@Override
 	protected boolean isRouteDisplayed() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 	
 	private Location getLocation(GeoPoint point) {
 		Location loc = new Location(LocationManager.GPS_PROVIDER);
 		if (point != null & loc != null) {
 			loc.setLatitude(point.getLatitudeE6()/1000000F);
 			loc.setLongitude(point.getLongitudeE6()/1000000F);
 			selectedLocation = loc;
 			return loc;
 		}
 		else
 			return null;
 	}
 	
 	@Override
 	public void onClick(View view) {
 		int viewId = view.getId();
 		if (viewId == doneButton.getId()) {
 			Intent sendMessageIntent = new Intent(getApplicationContext(), SendMessageActivity.class);
 			
 			GeoPoint point2 = new GeoPoint((int) (33.7786012 * 1E6), (int) (-84.3983965 * 1E6));
 			Location loc = getLocation(point2);
 			//sendMessageIntent.putExtra("location", loc);
 			sendMessageIntent.putExtra("location", itemizedOverlay.getSelectedLocation());
 			startActivity(sendMessageIntent);
 		}
 		else if (viewId == searchButton.getId()) {
 			removeOverlays();
 			new SearchClicked(mapSearchBox.getText().toString(), this).execute();
             //mapSearchBox.setText("", TextView.BufferType.EDITABLE);
 			getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
 			
 			InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
 			imm.hideSoftInputFromWindow(mapSearchBox.getWindowToken(), 0);
 		}
 	}
 	
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 	    setContentView(R.layout.map_view);
 	    mapView = (MapView) findViewById(R.id.mapview);
 	    mapView.setBuiltInZoomControls(true);
 	    
	    Drawable drawable = this.getResources().getDrawable(R.drawable.pin);
	    mapView.getOverlays().add(new MapItemizedOverlay(drawable));
 	    myLocOverlay=new MyLocationOverlay(this, mapView);
 	    mapView.getOverlays().add(myLocOverlay);
        
        // Center location on current position
        List<Overlay> overlays = mapView.getOverlays();
        myLocOverlay = new MyLocationOverlay(this, mapView);
 
        /**Changes*******************/
         /*Drawable drawable = this.getResources().getDrawable(R.drawable.map_marker_icon);
         Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
         Drawable d = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, 64, 64, true));
         itemizedOverlay = new MapItemizedOverlay(d, this);*/
         
         GeoPoint myLoc = myLocOverlay.getMyLocation();
         myLocOverlay.enableMyLocation();
         //myLocOverlay.onTouchEvent(this, mapView);
         mapView.getOverlays().add(myLocOverlay);
         
         if (myLoc != null)
         	System.out.println(myLoc.getLatitudeE6()+", "+myLoc.getLongitudeE6());
         else
         	System.out.println("My Loc is null!");
         
         searchButton = (Button) findViewById(R.id.searchButton);
         searchButton.setOnClickListener(this);
         doneButton = (Button) findViewById(R.id.doneButton);
         doneButton.setOnClickListener(this);
         
         
        /**End of Changes*******************/
        
        overlays.add(myLocOverlay);
        
         myLocOverlay.runOnFirstFix(new Runnable() {
         	public void run() {
         		mapView.getController().animateTo(myLocOverlay.getMyLocation());
         		mapView.getController().setZoom(4);
         	}
         });
         
         mapSearchBox  = (EditText)findViewById(R.id.search_box);
         mapSearchBox.setOnEditorActionListener(new TextView.OnEditorActionListener() {
 			
 			@Override
 			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
 				if (actionId == EditorInfo.IME_ACTION_SEARCH ||
 	                    actionId == EditorInfo.IME_ACTION_DONE ||
 	                    actionId == EditorInfo.IME_ACTION_GO ||
 	                    event.getAction() == KeyEvent.ACTION_DOWN &&
 	                    event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
 					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
 	                imm.hideSoftInputFromWindow(mapSearchBox.getWindowToken(), 0);
 
 	                removeOverlays();
 	                new SearchClicked(mapSearchBox.getText().toString(), getApplicationContext()).execute();
 	                mapSearchBox.setText("", TextView.BufferType.EDITABLE);
 	                return true;
 				}
 				return false;
 			}
 		});
     }
 	
 	private void removeOverlays() {
         itemizedOverlay.clearOverlays();
         mapView.getOverlays().clear();
 	}
 	
 	private class SearchClicked extends AsyncTask<Void, Void, Boolean> {
         private String toSearch;
         private Address address;
         private ProgressDialog dialog;
 
         public SearchClicked(String toSearch, Context context) {
             this.toSearch = toSearch;
             dialog = new ProgressDialog(context);
         }
         
         @Override
     	public void onPreExecute() {
         	dialog.setMessage("Searching location...");
     	    dialog.show();
         }
 
         @Override
         protected Boolean doInBackground(Void... voids) {
             try {
                 Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.US);
                 List<Address> results = geocoder.getFromLocationName(toSearch, 5);
 
                 if (results.size() == 0) {
                     return false;
                 }
 
                 for (Address addr : results) {
                 	
                 	//Now do something with this GeoPoint:
                     GeoPoint p = new GeoPoint((int) (addr.getLatitude() * 1E6), (int) (addr.getLongitude() * 1E6));
                     OverlayItem overlay = new OverlayItem(p, getAddress(p), "");
                     itemizedOverlay.addOverlay(overlay);
                 }
                 
                 mapView.getOverlays().add(itemizedOverlay);
 
             } catch (Exception e) {
                 Log.e("", "Something went wrong: ", e);
                 return false;
             }
             return true;
         }
         
         @Override
         protected void onPostExecute(Boolean success) {
         	dialog.hide();
         }
         
         private String getAddress(GeoPoint point) {
         	double lat = point.getLatitudeE6()/1000000F;
         	double lon = point.getLongitudeE6()/1000000F;
     		Geocoder geoCoder = new Geocoder(getApplicationContext(), Locale.getDefault());
     		List<Address> list = null;
     		try {
     			list = geoCoder.getFromLocation(lat, lon, 1);
     		} catch (IOException e) {
     			e.printStackTrace();
     		}
     		Address address = null;
             if (list != null & list.size() > 0)
                 address = list.get(0);
     	
             if (address == null)
             	return "unknown Address!";
             else {
             	StringBuilder sb = new StringBuilder();
                 if (address.getMaxAddressLineIndex() > 0) 
                  	sb.append(address.getAddressLine(0));
             	return sb.toString();//address.get.getLocality();
             }
     	}
 	}
 }
