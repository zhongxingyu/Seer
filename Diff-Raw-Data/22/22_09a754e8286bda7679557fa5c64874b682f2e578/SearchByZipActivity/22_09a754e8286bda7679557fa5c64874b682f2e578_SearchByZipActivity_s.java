 package edu.umn.pinkpanthers.beerfinder.activities;
 
 import java.io.IOException;
 import java.util.List;
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.location.Address;
 import android.location.Geocoder;
 import android.location.Location;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.EditText;
 import edu.umn.pinkpanthers.beerfinder.R;
 import edu.umn.pinkpanthers.beerfinder.network.Callback;
 import edu.umn.pinkpanthers.beerfinder.network.LocationResolver;
 
 /**
  * Displays a list of brands to select.
  */
 public class SearchByZipActivity extends Activity implements OnClickListener {
 
     public static final String SELECTED_ZIP = "SELECTED_ZIP";
     private EditText enterZipInput;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.search_by_zip_screen);
         enterZipInput = (EditText) findViewById(R.id.enter_zip_button);
         findViewById(R.id.use_current_zip_button).setOnClickListener(this);
         findViewById(R.id.search_by_zip_go_button).setOnClickListener(this);
     }
 
     public void onClick(View v) {
         if (v.getId() == R.id.use_current_zip_button) {
             getCurrentZip();
         } else if (v.getId() == R.id.search_by_zip_go_button) {
             startMapResultsActivity(enterZipInput.getText().toString());
         }
     }
 
     private void getCurrentZip() {
         // Show a spinner while the zip code is retrieved
         final ProgressDialog spinner = ProgressDialog.show(this, getString(R.string.working_spinner),
                 getString(R.string.getting_current_zip), true, false);
 
         LocationResolver.getCurrentLocation(this, new Callback<Location>() {
             @Override
             public void onSuccess(Location location) {
                 List<Address> addresses = null;
                 try {
                     addresses = new Geocoder(SearchByZipActivity.this).getFromLocation(location.getLatitude(),
                             location.getLongitude(), 1);
                     if (addresses != null && addresses.size() >= 1) {
                         Address firstAddress = addresses.get(0);
                         String zipCode = firstAddress.getPostalCode();
                         enterZipInput.setText(zipCode);
                     }
                 } catch (IOException e) {
                     // There is a bug in the 2.3.3 emulator that always throws a
                     // "Service not Available" exception, documented here:
                     // http://code.google.com/p/android/issues/detail?id=8816
                     if (e.getMessage().contains("Service not Available")) {
                         enterZipInput.setText("55455");
                     } else {
                         e.printStackTrace();
                     }
                 } finally {
                     spinner.dismiss();
                 }
             }
 
             @Override
             public void onFailure(Location value) {
             }
 
         });
 
     }
 
     public void homeClicked(View view) {
         finish();
     }
 
     private void startMapResultsActivity(String zip) {
         Intent mapResultsIntent = new Intent(getApplicationContext(), MapResultsActivity.class);
         mapResultsIntent.putExtra(SearchByZipActivity.SELECTED_ZIP, zip);
         startActivity(mapResultsIntent);
     }
 }
