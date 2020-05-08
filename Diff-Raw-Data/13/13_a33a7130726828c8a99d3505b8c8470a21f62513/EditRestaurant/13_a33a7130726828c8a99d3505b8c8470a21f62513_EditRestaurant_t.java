 package csci498.strupper.munchlist;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.database.Cursor;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.net.ConnectivityManager;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.EditText;
 import android.widget.RadioButton;
 import android.widget.RadioGroup;
import android.widget.TextView;
 import android.widget.Toast;
 
 public class EditRestaurant extends Activity {
 
   private RestaurantHelper helper;
   private String restaurantId;
   private final LocationListener onLocationChange = new LocationListener() {
     public void onLocationChanged(Location fix) {
       helper.updateLocation(restaurantId, fix.getLatitude(),
                             fix.getLongitude());
      ((TextView)findViewById(R.id.location)).setText(String.valueOf(fix.getLatitude())
           + ", "
           + String.valueOf(fix.getLongitude()));
       ((LocationManager)getSystemService(LOCATION_SERVICE)).removeUpdates(onLocationChange);
       Toast
            .makeText(EditRestaurant.this, "Location saved",
                      Toast.LENGTH_LONG)
            .show();
     }
 
     public void onProviderDisabled(String provider) {
       // required for interface, not used
     }
 
     public void onProviderEnabled(String provider) {
       // required for interface, not used
     }
 
     public void onStatusChanged(String provider, int status, Bundle extras) {
       // required for interface, not used
     }
   };
 
   @Override
   public void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
     setContentView(R.layout.edit_restaurant);
 
     helper = new RestaurantHelper(this);
 
     restaurantId = getIntent().getStringExtra(MunchList.ID_EXTRA);
 
     // restore form
     if (savedInstanceState != null) {
       String s;
       if (restaurantId == null && (s = savedInstanceState.getString("id")) != null) {
         restaurantId = s;
       }
       if ((s = savedInstanceState.getString("name")) != null) {
         ((EditText)findViewById(R.id.name)).setText(s);
       }
       if ((s = savedInstanceState.getString("address")) != null) {
         ((EditText)findViewById(R.id.address)).setText(s);
       }
       if ((s = savedInstanceState.getString("feed")) != null) {
         ((EditText)findViewById(R.id.feed)).setText(s);
       }
       if ((s = savedInstanceState.getString("type")) != null) {
         if (s.equals("sit_down")) {
           ((RadioButton)findViewById(R.id.sit_down)).setChecked(true);
         } else if (s.equals("take_out")) {
           ((RadioButton)findViewById(R.id.take_out)).setChecked(true);
         } else if (s.equals("delivery")) {
           ((RadioButton)findViewById(R.id.delivery)).setChecked(true);
         }
       }
     }
 
     if (restaurantId != null) {
       load();
     }
   }
 
   @Override
   public boolean onPrepareOptionsMenu(Menu menu) {
     if (restaurantId == null) {
      menu.findItem(R.id.setLocation).setEnabled(false);
     }
     return super.onPrepareOptionsMenu(menu);
   }
 
 
   @Override protected void onPause() {
     save();
     ((LocationManager)getSystemService(LOCATION_SERVICE)).removeUpdates(onLocationChange);
     super.onPause();
   };
 
   private void setFrom(Restaurant r) {
     ((EditText)findViewById(R.id.name)).setText(r.getName());
     ((EditText)findViewById(R.id.address)).setText(r.getAddress());
     ((EditText)findViewById(R.id.feed)).setText(r.getFeed());
     RadioGroup types = (RadioGroup)findViewById(R.id.types);
     if (r.getType().equals("sit_down")) {
       types.check(R.id.sit_down);
     }
     else if (r.getType().equals("take_out")) {
       types.check(R.id.take_out);
     }
     else {
       types.check(R.id.delivery);
     }
    ((TextView)findViewById(R.id.location)).setText(r.getLat() + ", " + r.getLon());
   }
 
   private void load() {
     Cursor c = helper.getById(restaurantId);
     c.moveToFirst();
     setFrom(RestaurantHelper.restaurantOf(c));
     c.close();
   }
 
   @Override public void onSaveInstanceState(Bundle outState) {
     if (restaurantId != null) {
       outState.putString("id", restaurantId);
     }
     outState
       .putString("name",
                  ((EditText)findViewById(R.id.name)).getText().toString());
     outState
       .putString("address",
                  ((EditText)findViewById(R.id.address)).getText().toString());
     outState
       .putString("feed",
                  ((EditText)findViewById(R.id.feed)).getText().toString());
     RadioGroup types = (RadioGroup)findViewById(R.id.types);
     switch (types.getCheckedRadioButtonId()) {
     case R.id.sit_down:
       outState.putString("type", "sit_down");
       break;
     case R.id.take_out:
       outState.putString("type", "take_out");
       break;
     case R.id.delivery:
       outState.putString("type", "delivery");
       break;
     }
   }
 
   @Override
   public void onDestroy() {
     super.onDestroy();
     helper.close();
   }
 
   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
     getMenuInflater().inflate(R.menu.activity_edit_restaurant, menu);
     return super.onCreateOptionsMenu(menu);
   }
 
   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
     if (item.getItemId() == R.id.viewFeed) {
       if (isNetworkAvailable()) {
         Intent i = new Intent(this, FeedActivity.class);
         i.putExtra(FeedActivity.FEED_URL,
                    ((EditText)findViewById(R.id.feed)).getText().toString());
         startActivity(i);
       }
       else {
         Toast
              .makeText(this, "Sorry, the Internet is not available",
                        Toast.LENGTH_LONG)
              .show();
       }
       return true;
     }
    if (item.getItemId() == R.id.setLocation) {
       ((LocationManager)getSystemService(LOCATION_SERVICE)).requestLocationUpdates(LocationManager.GPS_PROVIDER,
                                                                                    0,
                                                                                    0,
                                                                                    onLocationChange);
       return true;
     }
     return super.onOptionsItemSelected(item);
   }
 
   private boolean isNetworkAvailable() {
     return
       ((ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE))
         .getActiveNetworkInfo() != null;
   }
 
   private void save() {
     Restaurant r = new Restaurant();
     r.setName(((EditText)findViewById(R.id.name))
               .getText()
               .toString());
     r.setAddress(((EditText)findViewById(R.id.address))
                  .getText()
                  .toString());
     r.setFeed(((EditText)findViewById(R.id.feed))
                  .getText()
                  .toString());
 
     RadioGroup types = (RadioGroup)findViewById(R.id.types);
     switch (types.getCheckedRadioButtonId()) {
     case R.id.sit_down:
       r.setType("sit_down");
       break;
     case R.id.take_out:
       r.setType("take_out");
       break;
     case R.id.delivery:
       r.setType("delivery");
       break;
     }
 
     if (restaurantId == null) {
       helper.insert(r);
     } else {
       helper.update(restaurantId, r);
     }
   }
 
 }
