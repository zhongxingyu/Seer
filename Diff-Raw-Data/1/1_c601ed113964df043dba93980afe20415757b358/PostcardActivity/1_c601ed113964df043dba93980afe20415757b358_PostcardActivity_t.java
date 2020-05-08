 package eu.hack4europe.postcard;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Activity;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.Intent;
 import android.content.res.Configuration;
 import android.graphics.drawable.Drawable;
 import android.location.Criteria;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.os.Handler;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Gallery;
 import android.widget.ImageView;
 import android.widget.ScrollView;
 import android.widget.TextView;
 import android.widget.Toast;
 import eu.hack4europe.europeana4j.EuropeanaConnection;
 import eu.hack4europe.europeana4j.EuropeanaItem;
 import eu.hack4europe.europeana4j.EuropeanaQuery;
 import eu.hack4europe.europeana4j.EuropeanaResults;
 import eu.hack4europe.postcard.geo.GeoParseJson;
 
 
 public class PostcardActivity extends Activity
         implements View.OnClickListener, OnItemSelectedListener, LocationListener {
 
     private static final int ENTER_CITY_NAME = 0;
     private static final int ABOUT = 1;
     private static final int REFRESH = 2;
     private static final int FIND_CITY = 3;
     
     // This is secret, do not share
     private static final String API_KEY = "HTMQFSCKKB";
 
     public static final int RESULT_SIZE = 30;
 
     private TextView locationText;
     private Gallery gallery;
     private ImageView selectedPostcard;
     
 //    private EditText cityText;
  //   private Button findCityButton;
     
     private final PostcardModel model = PostcardApplication.getInstance().getModel();
     private final PostcardBitmapLoader loader = new PostcardBitmapLoader();
 
     private String bestProvider;
     private LocationManager locationManager;
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuItem city = menu.add(0, ENTER_CITY_NAME, 0, getString(R.string.findCityMenu));
         city.setIcon(android.R.drawable.ic_menu_compass);
         MenuItem refresh = menu.add(0, REFRESH, 2, getString(R.string.refreshMenu));
         refresh.setIcon(android.R.drawable.ic_menu_rotate);
         MenuItem about = menu.add(0, ABOUT, 3, getString(R.string.aboutMenu));
         about.setIcon(android.R.drawable.ic_menu_info_details);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case ENTER_CITY_NAME:
             	cityFindDialog();
                 return true;
             case REFRESH:
                 findIt();
                 return true;
             case ABOUT:
             	aboutDialog();
                 return true;
         }
 
         return false;
     }
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         // Getting the views
         selectedPostcard = (ImageView) findViewById(R.id.bigImage);
         gallery = (Gallery) findViewById(R.id.gallery1);
         locationText = (TextView) findViewById(R.id.location);
 
         // Attaching event listeners
         selectedPostcard.setOnClickListener(this);
         gallery.setOnItemSelectedListener(this);
 
         locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
         Criteria criteria = new Criteria();
         criteria.setAccuracy(Criteria.ACCURACY_COARSE);
         bestProvider = locationManager.getBestProvider(criteria, true);
         Location location = locationManager.getLastKnownLocation(bestProvider);
         model.setLocation(location);
         findIt();
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         locationManager.requestLocationUpdates(bestProvider, 5 * 60 * 1000, 1, this);
     }
 
     @Override
     protected void onPause() {
         super.onPause();
         locationManager.removeUpdates(this);
     }
 
     @Override
     public void onConfigurationChanged(Configuration newConfig) {
       super.onConfigurationChanged(newConfig);
       findIt();
     }
 
     
     @Override
     public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
 		if(!PostcardApplication.getInstance().haveInternet(this)){
             return;
 		} 
         List<EuropeanaItem> europeanaItems = model.getEuropeanaItems();
         final EuropeanaItem item = europeanaItems.get(position);
 
         model.setSelectedItemPosition(position);
 
         Handler handler = new Handler();
         handler.postDelayed(new Runnable() {
             @Override
             public void run() {
                 EuropeanaItem selectedItem = model.getSelectedItem();
                 if (selectedItem == item) { // making smooth scrolling
                     loader.loadAsync(item, selectedPostcard);
                 }
             }
         }, 400);
     }
 
     @Override
     public void onNothingSelected(AdapterView<?> adapterView) {
     }
 
     @Override
     public void onClick(View view) {
         if (view == selectedPostcard) {
             clickIt();
         } 
     }
 
 	private void clickIt() {
         EuropeanaItem selectedItem = model.getSelectedItem();
         String title = selectedItem.getTitle();
         Log.i("postcard", "clicked on " + title);
         Intent navigate = new Intent(PostcardActivity.this, DescriptionActivity.class);
         startActivity(navigate);
        
     }
 
     private void findIt(String ... cityLocation) {
     	String city;
     	if(cityLocation.length == 1){
     		city = cityLocation[0];
     	} else {
 			Location location = model.getLocation();
 
 			// Hardcoded for emulator
 
 			if (location == null) {
 				Location defaultLocation = new Location("");
 				defaultLocation.setLatitude(56.9471);
 				defaultLocation.setLongitude(23.6192);
 				city = GeoParseJson.getCity(defaultLocation);
 			} else {
 				city = GeoParseJson.getCity(location);
 			}
     	}
  
 
         if (city == null) {
             Toast.makeText(
                     getApplicationContext(),
                     "Location name is not available, check your internet connection",
                     1000).show();
             return;
         }
 
         if (city.equals(model.getLoadedCity())) {
             Log.i("postcard", "no need to refresh already in " + city);
             return;
         }
 
         model.reset();
 
         try {
             EuropeanaConnection europeana = new EuropeanaConnection(API_KEY);
 
             EuropeanaQuery query = new EuropeanaQuery();
             query.setType("IMAGE");
             query.setSubject("postcard");
             query.setLocation(city);
 
             EuropeanaResults res = europeana.search(query, RESULT_SIZE);
 
             if (res.getItemCount() > 0) {
                 List<EuropeanaItem> items = res.getAllItems();
                 List<EuropeanaItem> loadedItems = new ArrayList<EuropeanaItem>();
                 for (EuropeanaItem item : items) {
                     loadedItems.add(item);
                 }
                 model.setEuropeanaItems(loadedItems);
 
                 Drawable spinner = getResources().getDrawable(android.R.drawable.ic_menu_slideshow);
                 ImageAdapter imageAdapter = new ImageAdapter(
                         getApplicationContext(),
                         loadedItems,
                         loader,
                         spinner);
                 gallery.setAdapter(imageAdapter);
             }
 
             model.setLoadedCity(city);
             locationText.setText(getString(R.string.location) + " " + city);
         } catch (Exception e) {
             Log.e("postcard", "failed", e);
             Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), 5000).show();
         }
     }
     
     public void aboutDialog() {
     	//set up dialog
         Dialog dialog = new Dialog(this);
         dialog.setContentView(R.layout.aboutdialog);
         dialog.setTitle(getString(R.string.aboutTitle));
         dialog.setCancelable(true);
         //there are a lot of settings, for dialog, check them all out!
 
         //set up text
         
         ScrollView sView = (ScrollView)dialog.findViewById(R.id.ScrollView01);
         //Hide the Scollbar
         sView.setVerticalScrollBarEnabled(true);
         sView.setHorizontalScrollBarEnabled(false);
         
        
         TextView text = (TextView) dialog.findViewById(R.id.TextView01);
         text.setText(R.string.aboutDescription);
         
         //set up image view
         ImageView img = (ImageView) dialog.findViewById(R.id.ImageView01);
         img.setImageResource(R.drawable.ico);
 
         //set up button
         dialog.show();
     }
     
     public void cityFindDialog() {
     	//set up dialog
         final Dialog dialog = new Dialog(this);
         dialog.setContentView(R.layout.city_find_dialog);
         dialog.setTitle(getString(R.string.findCityMenu));
         dialog.setCancelable(true);
         //there are a lot of settings, for dialog, check them all out!
 
         final EditText cityText = (EditText) dialog.findViewById(R.id.findCityDialogEditText);
         final Button findCityButton= (Button) dialog.findViewById(R.id.findCityDialogButton);
         
         OnClickListener ocl = new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				String location = cityText.getText().toString();
 				findIt(location);
 			}
 		};
 		
 		findCityButton.setOnClickListener(ocl);
         dialog.show();
     }
     
     @Override
     public void onLocationChanged(Location location) {
         model.setLocation(location);
         findIt();
     }
 
     @Override
     public void onStatusChanged(String provider, int status, Bundle bundle) {
     }
 
     @Override
     public void onProviderEnabled(String provider) {
     }
 
     @Override
     public void onProviderDisabled(String provider) {
     }
 
 }
