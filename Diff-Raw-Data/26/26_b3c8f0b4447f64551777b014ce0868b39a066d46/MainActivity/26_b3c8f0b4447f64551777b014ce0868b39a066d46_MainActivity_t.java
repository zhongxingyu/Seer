 package edu.fiu.cs.seniorproject;
 
 
 import android.app.Activity;
 import android.content.Intent;
 import android.database.SQLException;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import edu.fiu.cs.seniorproject.data.MbGuideDB;
 
 public class MainActivity extends Activity {
 
 	MbGuideDB db;
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main); 
         db = new MbGuideDB(this);
         
         try
         {
         	db.openDatabase();
         	
         	if(!db.isUserPrefSet())
         	{
         		  Intent intent = new Intent(this, PersonalizationActivity.class);
         	      startActivity(intent);
         	}
         	db.closeDatabase();
         }
         catch(SQLException s)
         {
         	s.printStackTrace();
         	
         }
       
         
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.menu_settings:
                 this.onSettingsClick(item);
                 return true;            
         }
         return super.onOptionsItemSelected(item);
     }
     
     public void onFacebookLoginButtonClick(View view) {
     	Intent intent = new Intent(this, FacebookLoginActivity.class);
     	this.startActivity(intent);
     }
     
     public void onShowGeoLocationClick(View view) {
     	Intent intent = new Intent(this, GeoLocationActivity.class);
     	this.startActivity(intent);
     }
     
     
     
     public void  onShowEventsClick(View view) {
     	Intent intent = new Intent(this, EventsActivity.class);
     	this.startActivity(intent);
     }
     
     public void  onPlacesButtonClick(View view) {
     	Intent intent = new Intent(this, PlacesActivity.class);
     	this.startActivity(intent);
     }
     
     public void onShowPlacesClick(View view) {
     	Intent intent = new Intent(this, PlacesActivity.class);
     	this.startActivity(intent);
     }
     
     public void onShowToursClick(View view) {
     	Intent intent = new Intent(this, TourActivity.class);
     	this.startActivity(intent);
     }
     
     public void onSettingsClick(MenuItem view) {
     	Intent intent = new Intent(this, SettingsActivity.class);
     	this.startActivity(intent);
     }   
     
     public void onShowMyPlacesClick(View view) {
     	Intent intent = new Intent(this, MyPlacesActivity.class);
     	this.startActivity(intent);
     }
     public void onShowMyEventsClick (View view) {
    	Intent intent = new Intent(this,MyEventsActivity.class); 
    	//Intent intent = new Intent(this,PersonalizationActivity.class); 
     	this.startActivity(intent);
     
     	
     }
 }
