 package jm.org.data.area;
 
 
 
 import android.app.SearchManager;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.text.method.ScrollingMovementMethod;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.SearchView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import static jm.org.data.area.AreaConstants.*;
 
 import com.android.actionbarcompat.ActionBarActivity;
 import jm.org.data.area.R;
 
 
 public class AreaActivity extends ActionBarActivity {
 	private String TAG = AreaActivity.class.getSimpleName();
 	
 	private TextView jsonText;	
 	AreaApplication area;
 	SharedPreferences preferences;
 	String idsKey;
 	String bingKey;
 	
 	Button btnInvokeSearch;
 
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         setContentView(R.layout.test);
 
         /*
         setContentView(R.layout.main);
         area = (AreaApplication) getApplication();
         
         //Initialize preferences
         idsKey = getString(R.string.pref_idsKey); 
         bingKey = getString(R.string.pref_bingKey);
         
         Log.d(TAG, String.format("IDS: %s. Bing: %s", idsKey, bingKey));
         
         if ((area.prefs.getString(idsKey, null) == null) || (area.prefs.getString(bingKey, null) == null)){
         	startActivity(new Intent(this, AreaPreferencesActivity.class));
         	Toast.makeText(this, "Please setup preferences", Toast.LENGTH_LONG);
         	return;
         }
         
         Button btnPrefs = (Button) findViewById(R.id.btn_showPrefs);
         btnPrefs.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				String idsAPIKey = area.prefs.getString(idsKey, "n/a");
 				String bingAPIKey = area.prefs.getString(bingKey, "n/a");
 				String dateRange = area.prefs.getString("timePeriod", null);
 				String resultMax = area.prefs.getString("resultNumber", null);
 				Log.d(TAG, String.format("IDS API Key: %s, Bing API Key: %s, Time period: %s years and Max results: %s", idsAPIKey,
 						bingAPIKey, dateRange, resultMax));
 				showPrefs(idsAPIKey, bingAPIKey, dateRange, resultMax);
 			}
 		});
         */
 		jsonText = (TextView) findViewById(R.id.txtView1);
 		
		Button btnInvokeSearch = (Button) findViewById(R.id.btnInvokeSearch);
 		//to make the text scrollable
 		jsonText.setMovementMethod(new ScrollingMovementMethod());
 		
 		 // Attach actions to buttons
 		btnInvokeSearch.setOnClickListener(
             new OnClickListener() {
                 public void onClick(View v) {
                     onSearchRequested();
                 }
             });
         
 		// Called before in the StartUpActivity
 		//AreaData dataService = new AreaData(AreaActivity.this);
 		//dataService.genericSearch(WORLD_SEARCH, "TX.VAL.AGRI.ZS.UN", new String[]{"Jamaica", "Kenya","Barbados"});
     }
     
     private void showPrefs(String idsKey, String bingKey, String dateRange, String resultMax) {
 		Toast.makeText(
 				AreaActivity.this,
 				String.format("IDS API Key: %s, Bing API Key: %s, Time period: %s years and and Max results: %s articles/reports", idsKey,
 						bingKey, dateRange, resultMax), Toast.LENGTH_LONG).show();
 	}
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
     	MenuInflater menuInflater = getMenuInflater();
     	menuInflater.inflate(R.menu.home, menu);
     	
     	/*// --------------TO ADD SEARCH WIDGET TO THE BAR
     	// Get the SearchView and set the searchable configuration
         SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
         
         // error nullpointer exception right here
         SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
         /// <-------- Null Pointer Need to check out the Search VIewview
         
         searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
         searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default
     */
     	
     	return super.onCreateOptionsMenu(menu);
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
     	switch (item.getItemId()) {
     	case R.id.menu_prefs:
     		startActivity(new Intent(AreaActivity.this, AreaPreferencesActivity.class));
     	}
     	return super.onOptionsItemSelected(item);
 
     }
 }
