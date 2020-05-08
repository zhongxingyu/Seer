 package se.chalmers.krogkollen.map;
 
 import android.app.ActionBar;
 import android.app.Activity;
 import android.app.AlertDialog.Builder;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.res.Resources;
 import android.graphics.Point;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.SystemClock;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.animation.LinearInterpolator;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.Toast;

 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.Projection;
 import com.google.android.gms.maps.model.*;

 import se.chalmers.krogkollen.R;
 import se.chalmers.krogkollen.backend.NoBackendAccessException;
 import se.chalmers.krogkollen.backend.NotFoundInBackendException;
 import se.chalmers.krogkollen.utils.ActivityID;
 
 import java.util.ArrayList;
 
 /*
  * This file is part of Krogkollen.
  *
  * Krogkollen is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Krogkollen is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with Krogkollen.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 /**
  * The standard implementation of IMapView.
  *
  * This is a normal map with the user marked on the map, and with a list of pubs marked on the map.
  */
 
 public class MapActivity extends Activity implements IMapView {
 
     /**
      * Identifier for the intent used to start the activity for detailed view.
      */
     public static final String MARKER_PUB_ID = "se.chalmers.krogkollen.MARKER_PUB_ID";
     public static final int USER_ZOOM = 16;
     public static final int MARKER_ZOOM = 18;
 
     private MapPresenter presenter;
     private Marker userMarker;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_map);
 
         try {
             MapWrapper.INSTANCE.init(this.getFragmentManager(), this.getResources());
         } catch (NoBackendAccessException e) {
             showErrorMessage(getResources().getString(R.string.error_no_backend_access));
         } catch (NotFoundInBackendException e) {
             showErrorMessage(getResources().getString(R.string.error_no_backend_item));
         }
 
         presenter = new MapPresenter();
         presenter.setView(this);
 
         MapWrapper.INSTANCE.getMap().setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
             @Override
             public boolean onMarkerClick(Marker marker) {
 
                 // Move camera to the clicked marker.
                 moveCameraToPosition(marker.getPosition(), MARKER_ZOOM);
 
                 if (marker.getTitle().equalsIgnoreCase("user")) {
                     // TODO Open favorites.
                 } else {
                     // Open detailed view.
                     presenter.pubMarkerClicked(marker.getTitle());
                 }
                 return true; // Suppress default behavior; move camera and open info window.
             }
         });
 
         ActionBar actionBar = getActionBar();
         actionBar.setIcon(R.drawable.list_icon);
         actionBar.setDisplayHomeAsUpEnabled(true);
     }
 
     // Start the activity in a local method to keep the right context.
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);
 
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.map, menu);
 
         return true;
     }
 
     /**
      * Center the Google maps camera on the user.
      *
      * @param zoom how close to zoom in on the user.
      */
     public void moveCameraToPosition(LatLng pos, int zoom) {
         MapWrapper.INSTANCE.getMap().animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(pos, zoom, 0, 0)));
     }
 
     /**
      * Adds a user marker to the map.
      *
      * @param latLng the user location
      */
     public void addUserMarker(LatLng latLng){
         userMarker = MapWrapper.INSTANCE.getMap().addMarker(new MarkerOptions()
                 .position(latLng)
                 .icon(BitmapDescriptorFactory.fromResource(R.drawable.user_marker))
                 .title("user"));
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem menuItem) {
         presenter.onActionBarClicked(menuItem);
         return true;
     }
 
     /**
      * ** Method written by Google, found on stackoverflow.com **
      * ** http://stackoverflow.com/questions/13728041/move-markers-in-google-map-v2-android **
      * Moves the user marker smoothly to a new position.
      *
      * @param marker	the marker that will be moved
      * @param toPosition	the position to where the marker will be moved
      */
     public void animateUserMarker(final LatLng toPosition) {
         final Handler handler = new Handler();
         final long start = SystemClock.uptimeMillis();
         Projection proj = MapWrapper.INSTANCE.getMap().getProjection();
         Point startPoint = proj.toScreenLocation(userMarker.getPosition());
         final LatLng startLatLng = proj.fromScreenLocation(startPoint);
         final long duration = 500;
 
         final LinearInterpolator interpolator = new LinearInterpolator();
 
         handler.post(new Runnable() {
             @Override
             public void run() {
                 long elapsed = SystemClock.uptimeMillis() - start;
                 float t = interpolator.getInterpolation((float) elapsed / duration);
                 double lng = t * toPosition.longitude + (1 - t) * startLatLng.longitude;
                 double lat = t * toPosition.latitude + (1 - t) * startLatLng.latitude;
                 userMarker.setPosition(new LatLng(lat, lng));
 
                 if (t < 1.0) {
                     // Post again 16ms later.
                     handler.postDelayed(this, 16);
                 }
             }
         });
     }
 
     @Override
     public SharedPreferences getPreferences() {
         return this.getPreferences(Context.MODE_PRIVATE);
     }
 
     @Override
     public void onPause() {
         super.onPause();
         presenter.onPause();
     }
 
     @Override
     public void onResume() {
         super.onResume();
         presenter.onResume();
     }
 
     @Override
     public void onBackPressed() {
 
         //Get what activity you came from originally.
         int activity = this.getIntent().getIntExtra(ActivityID.ACTIVITY_ID, 0);
         Intent intent;
         switch(activity) {
             case ActivityID.LIST:
                 // TODO do something here
                 //If you came from the list view, return there.
                 //Intent intent = new Intent(this, ListActivity.class);
                 //intent.putExtra(ActivityID.ACTIVITY_ID, CallingActivity.MAP);
                 //this.startActivity(intent);
                 break;
             default:
                 //If you came from anything else, return to home screen.
                 intent = new Intent(Intent.ACTION_MAIN);
                 intent.addCategory(Intent.CATEGORY_HOME);
                 startActivity(intent);
                 break;
         }
     }
 
     @Override
     public void navigate(Class<?> destination) {
         Intent intent = new Intent(this, destination);
         startActivity(intent);
     }
 
     @Override
     public void navigate(Class<?> destination, Bundle extras) {
         Intent intent = new Intent(this, destination);
         intent.putExtra(MARKER_PUB_ID, extras.getString(MapPresenter.MAP_PRESENTER_KEY));
         startActivity(intent);
     }
 
     @Override
     public void showErrorMessage(String message) {
         CharSequence text = message;
         int duration = Toast.LENGTH_SHORT;
 
         Toast toast = Toast.makeText(this, text, duration);
         toast.show();
     }
 
     @Override
     public void showAlertDialog(final String msg, final boolean showCheckbox) {
         //Create most of the dialog that will be shown if either wifi or gps are disabled.
         Builder builder = new Builder(this);
         final ArrayList<Integer> selected = new ArrayList<Integer>();
 
         if (showCheckbox) {
             //Check box, making it possible to chose not to show this dialog again.
             View checkBoxView = View.inflate(this, R.layout.checkbox, null);
             CheckBox checkBox = (CheckBox)checkBoxView.findViewById(R.id.checkbox);
             checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 
                 @Override
                 public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                     if (isChecked) {
                         selected.add(0);
                     } else if(selected.size() != 0){
                         selected.clear();
                     }
                 }
             });
             checkBox.setText(this.getString(R.string.alert_dialog_dont_show_again));
             builder.setView(checkBoxView);
         }
         builder.setTitle(R.string.alert_dialog_title);
         builder.setMessage(msg);
 
         //Set listeners to the buttons in the dialog and chose appropriate consequences for clicks.
         builder.setPositiveButton(R.string.alert_dialog_activate, new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int which) {
                 if (showCheckbox) {
                     presenter.saveOption(!selected.isEmpty());
                 }
                 //Send user to location settings on the phone.
                 Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                 startActivity(intent);
             }
         });
         builder.setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int which) {
                 if (showCheckbox) {
                     presenter.saveOption(!selected.isEmpty());
                 }
             }
         });
 
         builder.show();
     }
 
     @Override
     public Resources getResources() {
         return super.getResources();
     }
 
 }
