 package com.github.groupENIGMA.journalEgocentrique;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.Spinner;
 
 public class Filters extends Activity {
 
 	private String filterPeriod = "None";
 	private SharedPreferences pref;
 	private final String FILTER = "filter_period";
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_filters);
 		pref = getSharedPreferences(
                 AppConstants.SHARED_PREFERENCES_FILENAME,
                 MODE_PRIVATE
         );
 		filterPeriod = pref.getString(FILTER, "None");
 		setAdapter();
 	}
 	
 	// Set the adapter for displaying the correct filter period
 	private void setAdapter(){
 		
 		// Set the filter period
 		Spinner filter = (Spinner)findViewById(R.id.filters);
 		final ArrayAdapter<CharSequence> filterAdapter = ArrayAdapter.createFromResource(this, R.array.periodFilter, android.R.layout.simple_spinner_item);
 		filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		filter.setAdapter(filterAdapter);
		if( filter != null ){
 			int spinnerPos = filterAdapter.getPosition(filterPeriod);
 			filter.setSelection(spinnerPos);
 		}
 		   // Set the listener
         OnItemSelectedListener filterListener = new OnItemSelectedListener() {
 
 			@Override
 			public void onItemSelected(AdapterView<?> adapter, View view,
 					int position, long id) {
 				filterPeriod = filterAdapter.getItem(position).toString();
 			}
 
 			@Override
 			public void onNothingSelected(AdapterView<?> adapter) {
 				// Nothing to do
 			}
         };
         filter.setOnItemSelectedListener(filterListener);
 	}
 	
 	public void send(View view){
     	Intent intent = new Intent(getApplicationContext(), GalleryActivity.class);
     	SharedPreferences.Editor edit = pref.edit();
    	if( filterPeriod != null)
     		edit.putString(FILTER, filterPeriod);
     	edit.commit();
     	startActivity(intent);
 	}
 }
