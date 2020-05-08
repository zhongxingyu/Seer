 package edu.umn.pinkpanthers.beerfinder.activities;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import edu.umn.pinkpanthers.beerfinder.R;
 import edu.umn.pinkpanthers.beerfinder.network.BeerFinderWebService;
 
 /**
  * The home screen of the application.
  * 
  */
 public class SearchActivity extends Activity implements OnClickListener {
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.search_screen);
         findViewById(R.id.search_by_brand_button).setOnClickListener(this);
         findViewById(R.id.search_by_zip_button).setOnClickListener(this);
         BeerFinderWebService.initialize();
     }
 
     public void onClick(View v) {
         if (v.getId() == R.id.search_by_brand_button) {
             Intent searchByBrandIntent = new Intent(this, SearchByBeerActivity.class);
             startActivity(searchByBrandIntent);
         }
         else if (v.getId() == R.id.search_by_zip_button) {
             Intent searchByZipIntent = new Intent(this, SearchByZipActivity.class);
             startActivity(searchByZipIntent);
         }
     }
 
 }
