 package com.crimezone.sd;
 
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.Context;
 import android.graphics.Color;
 import android.graphics.PorterDuff.Mode;
 import android.graphics.drawable.Drawable;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.FrameLayout;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MapView;
 import com.google.android.maps.OverlayItem;
 
 public class ShowCrimeMapActivity extends MapActivity {
 
   private MapController mapController;
   private MapView mapView;
   private LocationManager locationManager;
   private CrimeMapOverlay violentCrimeOverlay;
   private CrimeMapOverlay propertyCrimeOverlay;
   private CrimeMapOverlay otherCrimeOverlay;
   private GeoUpdateHandler gps;
   private Button violentCrimeButton;
   private Button propertyCrimeButton;
   private Button otherCrimeButton;
   private Boolean violentIsDisplayed = false;
   private Boolean propertyIsDisplayed = false;
   private Boolean otherIsDisplayed = false;
 
   public void onCreate(Bundle bundle) {
     super.onCreate(bundle);
     setContentView(R.layout.map); // bind the layout to the activity
 
     JSONArray results = null;
     Bundle bun = getIntent().getExtras();
     try {
       results = new JSONArray(bun.getString("results"));
       // this.populateGoogleMaps(results);
     } catch (JSONException e) {
       e.printStackTrace();
     }
     String startLat = bun.getString("startLat");
     String startLng = bun.getString("startLng");
     String radius = bun.getString("radius");
     GeoPoint currLocation = SDCrimeZoneApplication.getGeoPoint(startLat, startLng);
 
     // create a map view
     FrameLayout relativeLayout = (FrameLayout) findViewById(R.id.mapViewLayout);
     mapView = (MapView) findViewById(R.id.mapView);
     mapView.setBuiltInZoomControls(true);
     mapView.getOverlays().add(
         new CircleMapOverlay(this, Double.valueOf(startLat).doubleValue(), Double.valueOf(startLng)
             .doubleValue(), Float.valueOf(radius).floatValue()));
     // Either satellite or 2d
     mapView.setSatellite(false);
     mapController = mapView.getController();
     mapController.setZoom(15); // Zoon 1 is world view
     locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
     gps = new GeoUpdateHandler(currLocation);
     locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, gps);
 
     Drawable drawable = this.getResources().getDrawable(R.drawable.point);
 
     violentCrimeOverlay = new CrimeMapOverlay(drawable, this);
 
     propertyCrimeOverlay = new CrimeMapOverlay(drawable, this);
 
     otherCrimeOverlay = new CrimeMapOverlay(drawable, this);
 
     populateCrimeOverlays(results);
 
     // Button to control food overlay
     violentCrimeButton = (Button) findViewById(R.id.showViolent);
     violentCrimeButton.setOnClickListener(new OnClickListener() {
       public void onClick(View v) {
         toggleViolentOverlay();
       }
     }); 
 
     // Button to control access overlay
     propertyCrimeButton = (Button) findViewById(R.id.showProperty);
     propertyCrimeButton.setOnClickListener(new OnClickListener() {
       public void onClick(View v) {
         togglePropertyOverlay();
       }
     });
 
     // Button to control access overlay
     otherCrimeButton = (Button) findViewById(R.id.showOther);
     otherCrimeButton.setOnClickListener(new OnClickListener() {
       public void onClick(View v) {
         toggleOtherOverlay();
       }
     });
     
     toggleOtherOverlay();
     togglePropertyOverlay();
     toggleViolentOverlay();
   }
 
   public void onPause() {
     super.onPause();
     locationManager.removeUpdates(gps);
   }
 
   public void onDestroy() {
     super.onDestroy();
   }
   
   @Override
   protected boolean isRouteDisplayed() {
     return false;
   }
 
   public class GeoUpdateHandler implements LocationListener {
 
     public GeoUpdateHandler(GeoPoint startingLocation) {
       mapController.animateTo(startingLocation); // mapController.setCenter(point);
     }
 
     public void onLocationChanged(Location location) {
     }
 
     public void onProviderDisabled(String provider) {
     }
 
     public void onProviderEnabled(String provider) {
     }
 
     public void onStatusChanged(String provider, int status, Bundle extras) {
     }
     
     
   }
 
   private void populateCrimeOverlays(JSONArray results) {
     for (int i = 0; i < results.length(); i++) {
       JSONObject obj;
       try {
         obj = results.getJSONObject(i);
         String lng = obj.get("lng").toString();
         String lat = obj.get("lat").toString();
         // SDCrimeZoneApplication.debug(this, "mapping " + lat + ", " + lng);
         Double myLat = new Double(lat);
         Double myLng = new Double(lng);
         // String crime =
         // SDCrimeZoneApplication.bccMap.get(obj.get("bcc").toString());
         try {
           BccCodeEnum crimeEnum = BccCodeEnum.fromCode(obj.get("bcc").toString());
 
           String address = obj.get("address").toString();
           
           DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
           Date date = df.parse((obj.get("date").toString()));
           //System.out.println("got date: " + date.toString());
           //DateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");  
           //String formattedDate = df.format(date);
           String formattedDate = "";
           if (date != null) {
             formattedDate = date.toString();
             System.out.println("date = " + formattedDate);
           }
           
           int iLat = (int) (myLat.doubleValue() * 1E6);
           int iLng = (int) (myLng.doubleValue() * 1E6);
           // create the marker for Maps view
          OverlayItem overlayItem = createMarker(iLat, iLng, crimeEnum.getName(), address + "\n( " + formattedDate + ")");
 
           switch (crimeEnum) {
           case MURDER:
           case RAPE:
           case ROBBERY:
           case ASSAULT:
             violentCrimeOverlay.addOverlay(overlayItem);
             break;
           case BURGLARY:
           case THEFT:
           case ARSON:
             propertyCrimeOverlay.addOverlay(overlayItem);
             break;
           default:
             otherCrimeOverlay.addOverlay(overlayItem);
           }
         } catch (IllegalArgumentException e) {
           // BCC code not in our system
           continue;
         } catch (ParseException e) {
           // TODO Auto-generated catch block
           e.printStackTrace();
         }
       } catch (JSONException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
       }
     }
   }
 
   private OverlayItem createMarker(int lat, int lng, String title, String subtext) {
     GeoPoint p = new GeoPoint(lat, lng);
     OverlayItem overlayItem = new OverlayItem(p, title, subtext);
     return overlayItem;
   }
 
   public void toggleViolentOverlay() {
     if (!violentIsDisplayed) {
       mapView.getOverlays().add(violentCrimeOverlay);
       violentCrimeButton.getBackground().setColorFilter(Color.LTGRAY, Mode.MULTIPLY);
     } else {
       mapView.getOverlays().remove(violentCrimeOverlay);
       violentCrimeButton.getBackground().setColorFilter(Color.WHITE, Mode.MULTIPLY);
     }
     violentIsDisplayed = !violentIsDisplayed;
     // Added symbols will be displayed when map is redrawn so force redraw now
     mapView.postInvalidate();
   }
 
   public void togglePropertyOverlay() {
     if (!propertyIsDisplayed) {
       mapView.getOverlays().add(propertyCrimeOverlay);
       propertyCrimeButton.getBackground().setColorFilter(Color.LTGRAY, Mode.MULTIPLY);
     } else {
       mapView.getOverlays().remove(propertyCrimeOverlay);
       propertyCrimeButton.getBackground().setColorFilter(Color.WHITE, Mode.MULTIPLY);
     }
     propertyIsDisplayed = !propertyIsDisplayed;
     // Added symbols will be displayed when map is redrawn so force redraw now
     mapView.postInvalidate();
   }
 
   public void toggleOtherOverlay() {
     if (!otherIsDisplayed) {
       mapView.getOverlays().add(otherCrimeOverlay);
       otherCrimeButton.getBackground().setColorFilter(Color.LTGRAY, Mode.MULTIPLY);
     } else {
       mapView.getOverlays().remove(otherCrimeOverlay);
       otherCrimeButton.getBackground().setColorFilter(Color.WHITE, Mode.MULTIPLY);
     }
     otherIsDisplayed = !otherIsDisplayed;
     // Added symbols will be displayed when map is redrawn so force redraw now
     mapView.postInvalidate();
   }
 
 }
