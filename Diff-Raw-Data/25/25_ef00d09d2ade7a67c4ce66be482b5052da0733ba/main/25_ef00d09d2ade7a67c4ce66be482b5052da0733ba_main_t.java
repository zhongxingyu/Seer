 package com.manavo.HowFarTo;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.DialogInterface.OnClickListener;
 import android.graphics.drawable.Drawable;
 import android.location.Address;
 import android.location.Geocoder;
 import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapView;
 import com.google.android.maps.Overlay;
 import com.google.android.maps.OverlayItem;
 
 public class main extends MapActivity {
 	private MapView mapView;
 	private TextView distance;
 	
 	private List<Overlay> mapOverlays;
 	private hftOverlay itemizedoverlay;
 	
 	private List<Address> addresses;
 	
 	private FixedMyLocationOverlay myLocationOverlay;
 	
 	private Address location;
 	private GeoPoint locationPoint;
 	
	private Handler hRefresh;
	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
         this.distance = (TextView)this.findViewById(R.id.distance);
         
         this.mapView = (MapView)this.findViewById(R.id.mapview);
         this.mapView.setBuiltInZoomControls(true);
         
         this.mapOverlays = mapView.getOverlays();
         Drawable drawable = this.getResources().getDrawable(R.drawable.marker);
         this.itemizedoverlay = new hftOverlay(drawable, this);
         



        this.hRefresh = new Handler(){
	        @Override
	        public void handleMessage(Message msg) {
				main.this.mapView.getController().animateTo(main.this.myLocationOverlay.getMyLocation());
				main.this.showDistance();
	        }
        };

        
         this.myLocationOverlay = new FixedMyLocationOverlay(this, this.mapView);
 		this.myLocationOverlay.enableMyLocation();
 		this.myLocationOverlay.runOnFirstFix(new Runnable() {
 			public void run() {
				main.this.hRefresh.sendEmptyMessage(0);
 			}
 		});
 		
         this.addMyLocation();
     }
     
     @Override
     protected void onResume() {
     	super.onResume();
     	this.myLocationOverlay.enableMyLocation();
     }
     
     @Override
     protected void onPause() {
     	super.onPause();
     	this.myLocationOverlay.disableMyLocation();
     }
     
     private void addMyLocation() {
 		this.mapOverlays.add(this.myLocationOverlay);
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.main, menu);
         return true;
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         // Handle item selection
         switch (item.getItemId()) {
         case R.id.about:
             this.startActivity(new Intent(this, About.class));
             return true;
         case R.id.exit:
             this.finish();
             return true;
         default:
             return super.onOptionsItemSelected(item);
         }
     }
 
     // Calculate distance between 2 points in Kilometers
     // Based on the haversine formula from here: http://www.movable-type.co.uk/scripts/latlong.html
     private double calculateDistance(GeoPoint p1, GeoPoint p2) {
     	double lat1 = p1.getLatitudeE6()/1E6;
     	double lon1 = p1.getLongitudeE6()/1E6;
     	double lat2 = p2.getLatitudeE6()/1E6;
     	double lon2 = p2.getLongitudeE6()/1E6;
     	
     	double R = 6371; // km
     	double dLat = Math.toRadians(lat2-lat1);
     	double dLon = Math.toRadians(lon2-lon1);
     	
     	lat1 = Math.toRadians(lat1);
     	lat2 = Math.toRadians(lat2);
 
     	double a = Math.sin(dLat/2) * Math.sin(dLat/2) + Math.sin(dLon/2) * Math.sin(dLon/2) * Math.cos(lat1) * Math.cos(lat2); 
     	double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a)); 
     	double d = R * c;
     	
     	return d;
     }
     
     @Override
     protected boolean isRouteDisplayed() {
         return false;
     }
     
     public void showAddress(Address address) {
     	this.location = address;
     	
     	Integer lat = new Double(address.getLatitude()*1E6).intValue();
 		Integer lng = new Double(address.getLongitude()*1E6).intValue();
 		
 		this.mapOverlays.clear();
         this.addMyLocation();
         
         this.locationPoint = new GeoPoint(lat, lng);
         OverlayItem overlayitem = new OverlayItem(this.locationPoint, "Hello!", "I'm in "+address.getAddressLine(0)+"!");
         
         this.itemizedoverlay.clear();
         this.itemizedoverlay.addOverlay(overlayitem);
 
         this.mapOverlays.add(this.itemizedoverlay);
         
 		this.mapView.invalidate();
 		
 		this.showDistance();
     }
     
     private void showDistance() {
     	GeoPoint myLocation = this.myLocationOverlay.getMyLocation();
     	if (myLocation == null) {
     		Toast.makeText(this, "Waiting for your location", Toast.LENGTH_LONG).show();
     	} else if (this.location == null) {
     		// Do nothing, we got our location but haven't searched for anything yet
     	} else {
         	Double distance = this.calculateDistance(myLocation, this.locationPoint);
         	String locationText = "About " + new Integer(Math.round(Math.round(distance))).toString() + "km";
         	float accuracy = this.myLocationOverlay.getLastFix().getAccuracy();
         	if (accuracy != 0.0f) {
         		if (accuracy < 1000f) {
         			locationText += "( " + accuracy + "m)";
         		} else {
         			locationText += "( " + Math.round(accuracy/1000) + "km)";
         		}
         	}
        	locationText += " to " + this.location.getAddressLine(0) + ", " + this.location.getCountryCode();
         	this.distance.setText(locationText); 
         	this.distance.setVisibility(View.VISIBLE);
     	}
     }
     
     public void searchLocation(View v) {
     	EditText location = (EditText)this.findViewById(R.id.location);
     	
     	Geocoder g = new Geocoder(this);
     	try {
 			this.addresses = g.getFromLocationName(location.getText().toString(), 5);
 			
 			if (this.addresses.size() == 1) {
 				showAddress(this.addresses.get(0));
 			} else if (this.addresses.size() == 0) {
 				Toast.makeText(this, "Nothing found!", Toast.LENGTH_LONG).show();
 			} else {
 				Iterator<Address> i = this.addresses.iterator();
 				Address a;
 				
 				ArrayList<String> options = new ArrayList<String>();
 				while (i.hasNext()) {
 					a = i.next();
 					options.add(a.getAddressLine(0) + ", " + a.getCountryName());
 				}
 				CharSequence[] cs = options.toArray(new CharSequence[options.size()]);
 
 				AlertDialog.Builder dialog = new AlertDialog.Builder(this);
 				dialog.setItems(cs, new OnClickListener() {
 					public void onClick(DialogInterface arg0, int index) {
 						showAddress(main.this.addresses.get(index));
 					}
 				});
 				dialog.show();
 			}
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 	    	Toast.makeText(this, "Could not connect. Please try again!", Toast.LENGTH_SHORT).show();
 		}
     }
 }
