 package edu.uj.pmd.locationalarm.activities;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.Toast;
 import edu.uj.pmd.locationalarm.favorites.DatabaseHandler;
 import edu.uj.pmd.locationalarm.favorites.Destination;
 import edu.uj.pmd.locationalarm.favorites.tasks.AddFavoriteDestinationTask;
 import edu.uj.pmd.locationalarm.utilities.AppPreferences;
 import edu.uj.pmd.locationalarm.R;
 
 public class AlarmDetailsActivity extends Activity {
 	private double destinationLongitude;
 	private double destinationLatitude;
     private int destinationRadius;
     private int noTimeLocation;
     private AppPreferences appPrefs;
     private boolean preserveDestination = false;
 	
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_alarm_details);
 
         this.appPrefs = new AppPreferences(getApplicationContext());
         this.destinationLongitude = appPrefs.getDestinationLongitude();
         this.destinationLatitude = appPrefs.getDestinationLatitude();
 
         hideFavoritesNameEditIfDestinationFromFavorites();
     }
 
     private void hideFavoritesNameEditIfDestinationFromFavorites() {
         Intent intent = getIntent();
        if(intent.getExtras() != null && intent.getExtras().getBoolean("fromFavorites")) {
             findViewById(R.id.addToFavoritesText).setVisibility(View.GONE);
             findViewById(R.id.favoritesNameEdit).setVisibility(View.GONE);
         }
     }
 
 
 	
 	public void setAlarm(View view) {
 		EditText radiusEdit = (EditText) findViewById(R.id.radiusEdit);
         EditText timeEdit = (EditText) findViewById(R.id.timeEdit);
         EditText favoritesNameEdit = (EditText) findViewById(R.id.favoritesNameEdit);
 
         try {
             this.destinationRadius = Integer.valueOf(radiusEdit.getText().toString());
         } catch (NumberFormatException e) {
             Context context = getApplicationContext();
             CharSequence text = getString(R.string.insert_radius);
             int duration = Toast.LENGTH_SHORT;
 
             Toast toast = Toast.makeText(context, text, duration);
             toast.show();
             return;
         }
 
         try {
             this.noTimeLocation = Integer.valueOf(timeEdit.getText().toString());
         } catch (NumberFormatException e) {
             Context context = getApplicationContext();
             CharSequence text = getString(R.string.insert_time);
             int duration = Toast.LENGTH_SHORT;
 
             Toast toast = Toast.makeText(context, text, duration);
             toast.show();
             return;
         }
 
 
         String name = favoritesNameEdit.getText().toString();
         if(!(name.isEmpty())) {
             addFavoriteDestination(name);
         } else {
             DatabaseHandler database = new DatabaseHandler(this);
             Log.i("LocationAlarm", "(not added) favorites count: " + String.valueOf(database.getFavoriteDestinationsCount()));
         }
 
         appPrefs.setDestinationRadius(this.destinationRadius);
         appPrefs.setNoLocationTime((this.noTimeLocation - 1) * 60 * 1000);
         this.preserveDestination = true;
 
 		Intent intent = new Intent(this, MainActivity.class);
     	this.startActivity(intent);
 		finish();
 	}
 
     private void addFavoriteDestination(String name) {
         DatabaseHandler database = new DatabaseHandler(this);
 
         Destination destination = new Destination(name, destinationLongitude, destinationLatitude);
         new AddFavoriteDestinationTask(this).execute(destination);
         Log.i("LocationAlarm", "favorites count: " + String.valueOf(database.getFavoriteDestinationsCount()));
     }
 
     @Override
     public void onBackPressed() {
         Intent intent = new Intent(this, MainActivity.class);
         this.startActivity(intent);
         finish();
     }
 
 
 
     @Override
     protected void onPause() {
         super.onPause();
         if(!preserveDestination) {
             appPrefs.clearDestination();
         } else {
             preserveDestination = false;
         }
     }
 }
