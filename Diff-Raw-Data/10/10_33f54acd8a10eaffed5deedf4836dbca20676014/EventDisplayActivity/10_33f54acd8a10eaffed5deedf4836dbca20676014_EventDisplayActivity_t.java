 package com.allplayers.android;
 
 import java.io.IOException;
 import java.util.List;
 
 import android.graphics.drawable.Drawable;
 import android.location.Address;
 import android.location.Geocoder;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.TextView;
 
 import com.allplayers.android.activities.AllplayersSherlockMapActivity;
 import com.allplayers.objects.EventData;
 import com.devspark.sidenavigation.SideNavigationView;
 import com.devspark.sidenavigation.SideNavigationView.Mode;
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.MapFragment;
 import com.google.android.gms.maps.model.BitmapDescriptorFactory;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.MarkerOptions;
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MapView;
 import com.google.android.maps.Overlay;
 import com.google.android.maps.OverlayItem;
 
 /**
  * TODO If maps are missing on device image, this activity will crash.
  */
 public class EventDisplayActivity extends AllplayersSherlockMapActivity {
 
     /**
      * Called when the activity is first created, this sets up variables,
      * creates the Action Bar, and sets up the Side Navigation Menu.
      * @param savedInstanceState: Saved data from the last instance of the
      * activity.
      */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.eventdetail);
 
         TextView eventInfo = (TextView)findViewById(R.id.eventInfo);
         GoogleMap map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
 
         EventData event = (new Router(this)).getIntentEvent();
         eventInfo.setText("Event Title: " + event.getTitle() + "\nDescription: " +
                           event.getDescription() + "\nCategory: " + event.getCategory() +
                           "\nStart: " + event.getStartDateString() + "\nEnd: " + event.getEndDateString());
         
         
 
         String lat = event.getLatitude();
         String lon = event.getLongitude();
 
         if (lat.equals("") || lon.equals("") || lat.equals("0.000000") || lon.equals("0.000000")) {
             Geocoder geo = new Geocoder(this);
             try {
                 List<Address> addr = geo.getFromLocationName(event.getZip(), 1);
                 lat = addr.get(0).getLatitude() + "";
                 lon = addr.get(0).getLongitude() + "";
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
         
         LatLng location = new LatLng((Float.parseFloat(lat)), (Float.parseFloat(lon)));
         map.moveCamera(CameraUpdateFactory.newLatLng(location));
         map.addMarker(new MarkerOptions()
                 .icon(BitmapDescriptorFactory.fromResource(R.drawable.pindrop_50x50))
                 .position(location)
                 .title(event.getTitle())
                 );
         
         actionbar = getSupportActionBar();
         actionbar.setIcon(R.drawable.menu_icon);
         actionbar.setTitle(event.getTitle());
 
         sideNavigationView = (SideNavigationView)findViewById(R.id.side_navigation_view);
         sideNavigationView.setMenuItems(R.menu.side_navigation_menu);
         sideNavigationView.setMenuClickCallback(this);
         sideNavigationView.setMode(Mode.LEFT);
     }
 }
