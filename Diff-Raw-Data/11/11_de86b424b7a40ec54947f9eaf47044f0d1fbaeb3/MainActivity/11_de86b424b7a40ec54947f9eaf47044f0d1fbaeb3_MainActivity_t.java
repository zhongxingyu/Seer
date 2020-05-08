 package com.turbo_extreme_sloth.ezzence.activities;
 
 import java.util.Locale;
 
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.res.Resources;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.util.DisplayMetrics;
 import android.util.Log;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 
 import com.turbo_extreme_sloth.ezzence.CurrentUser;
 import com.turbo_extreme_sloth.ezzence.R;
 import com.turbo_extreme_sloth.ezzence.core.ListOption;
 import com.turbo_extreme_sloth.ezzence.exceptions.UncaughtExceptionHandler;
 
 public class MainActivity extends BaseActivity implements OnItemClickListener
 {
 	private ListView listView;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		Resources res = this.getResources();
 	    android.content.res.Configuration conf = res.getConfiguration();
 
 	    if(!conf.locale.toString().equals("en") && !conf.locale.toString().equals("nl")){
 	    	conf.locale = new Locale("en");
 	    }
 		// Set the default exceptions handler
 		Thread.setDefaultUncaughtExceptionHandler(UncaughtExceptionHandler.getUncaughtExceptionHandler(this, getResources().getString(R.string.error_unknown_exception)));
 		
 		// Redirect a user to the login page when not logged in
 		if (!CurrentUser.isLoggedIn())
 		{
 			startActivity(new Intent(this, LoginActivity.class));
 	
 			finish();
 		
 			return;
 		}
 		
 		setContentView(R.layout.activity_main);
 		
 		//initiate main options for user
 		ArrayAdapter<ListOption> adapter = new ArrayAdapter<ListOption>(this, 
 		        android.R.layout.simple_list_item_1, getOptions());
 		
 		listView = (ListView) findViewById(R.id.id_listview_main);
 		listView.setAdapter(adapter);
 		listView.setOnItemClickListener(this);
 	}
 	
 	/**
 	 * returns the options which are available at the main activity
 	 * @return the options
 	 */
 	private ListOption[] getOptions()
 	{
 		ListOption[] options = null;
 		
 		options = new ListOption[]
 		{
 			new ListOption(getString(R.string.set_temperature), SetTemperatureActivity.class),
 			new ListOption(getString(R.string.consumption_overview), ConsumptionOverviewActivity.class),
 			new ListOption(getString(R.string.language), SetTemperatureActivity.class)
 		};
 		
 		return options;
 	}
 	
 	/**
 	 * Gets the listview of this activity
 	 * @return ListView of this activity
 	 */
 	public ListView getListView()
 	{
 		return this.listView;
 	}
 
 	@Override
 	public void onItemClick(AdapterView<?> parent, View view, int position, long id)
 	{
 		ListOption lo = (ListOption)listView.getItemAtPosition(position);
 	    Log.e("<tag>", lo.toString());
 	    if(lo.toString().equals(getString(R.string.language))){
 			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
 
 		    Resources res = view.getResources();
 		    // Change locale settings in the app.
 		    DisplayMetrics dm = res.getDisplayMetrics();
 		    android.content.res.Configuration conf = res.getConfiguration();
 		    String selectedLanguage = "en";
 		    Log.e("<message>", conf.locale.toString());
 		    if(conf.locale.toString().equals("en")){
 		    	selectedLanguage = prefs.getString("language", "nl");
 		    }
 		    else{
 		    	selectedLanguage = prefs.getString("language", "en");
 		    }
 		    conf.locale = new Locale(selectedLanguage);
 		    res.updateConfiguration(conf, dm);
 		    
 		    
 		    Intent intent = getIntent();
 		    finish();
 		    startActivity(intent);
 	    }
 		//start the activity that is paired with the clicked option
 	    else{
 			Intent intent = new Intent(this, lo.getValue());
 			this.startActivity(intent);
 	    }
 	}
 	
 }
