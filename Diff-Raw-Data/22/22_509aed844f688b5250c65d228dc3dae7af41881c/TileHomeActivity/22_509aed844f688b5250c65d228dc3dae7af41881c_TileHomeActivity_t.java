 package com.twofivesix.pt.activities;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.Window;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.GridView;
 import android.widget.TextView;
 
 import com.twofivesix.pt.R;
 import com.twofivesix.pt.data.CustomTitleBar;
 import com.twofivesix.pt.data.TileItem;
 import com.twofivesix.pt.helpers.SharedPreferencesHelper;
 import com.twofivesix.pt.helpers.VersionAlertHelper;
 import com.twofivesix.pt.listAdapters.TileWithNameAdapter;
 
 public class TileHomeActivity extends Activity {
 	
 	private static final int REPORTING_REQUEST = 1;
 	private static final int QUESTIONS_REQUEST = 2;
 	private static final int PARNTERS_REQUEST = 3;
 	private ArrayList<TileItem> tileList = new ArrayList<TileItem>();
 	private boolean customTitleSupported;
 	
 	private TextView lastReportTV;
 	private SharedPreferencesHelper settings;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) 
 	{
 		super.onCreate(savedInstanceState);
 		
 		//check if custom title is supported BEFORE setting the content view!
 		customTitleSupported = requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
 		
 		setContentView(R.layout.tile_home_layout);
 		
 		if(customTitleSupported)
 			CustomTitleBar.customTitleBar(this, getText(R.string.home).toString());
 		
 		settings = new SharedPreferencesHelper(this);
 		
 		
 		// runs a check for first version run then show change log if something 
 		// is different 
 		new VersionAlertHelper(this, settings);
 		
 		
 	    tileList.add(new TileItem(R.drawable.reporting, R.string.reporting, ReportingActivity.class, REPORTING_REQUEST));
 	    tileList.add(new TileItem(R.drawable.question, R.string.questions, ViewQuestionsListActivity.class, QUESTIONS_REQUEST));
 	    tileList.add(new TileItem(R.drawable.links, R.string.partners, ViewPartnersListActivity.class, PARNTERS_REQUEST));
 		
 		final GridView gridview = (GridView) findViewById(R.id.gridview);
 		lastReportTV = (TextView) findViewById(R.id.tile_home_last_report);
 		
 		final TileWithNameAdapter tileAdapter = new TileWithNameAdapter(this, tileList);
         gridview.setAdapter(tileAdapter);
 
         gridview.setOnItemClickListener(new OnItemClickListener() {
         	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                 //Toast.makeText(TileHomeActivity.this, "" + getText(tileList.get(position).getLabelRef()), Toast.LENGTH_SHORT).show();
         		startActivityForResult(
         				new Intent(
         						TileHomeActivity.this, 
         						tileList.get(position).getActivityDestination()
         				), 
         				tileList.get(position).getRequestCode()
         		);
             }
         });
 	}
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
     	MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.home_options_menu, menu);
         
         Intent settingsIntent = new Intent(TileHomeActivity.this, SettingsActivity.class);
         MenuItem settingsItem = menu.findItem(R.id.settings_option_item);
         settingsItem.setIntent(settingsIntent);
         
         return true;
     }
     
     @Override
     public boolean onMenuItemSelected(int featureId, MenuItem item) {
     	//AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item.getMenuInfo();
     	
     	switch (item.getItemId()) {
 		case R.id.settings_option_item:
 			startActivity(item.getIntent());
 			return true;
 		case R.id.logout_option_item:
 			setResult(LoginActivity.LOGOUT_RESULT_CODE);
 			finish();
 			return true;
 		default:
 			return false;
 		}
     }
 	
 	@Override
 	protected void onResume() {
 		super.onResume();
 		refreshNotificationsCounts();
 	}
 
 	private void refreshNotificationsCounts() {
 		// TODO Implment in 0.2.1
 		CharSequence info;
 		//Log.d("SPENCER", "settings.getLastReportDateMillis() = " + settings.getLastReportDateMillis());
 		if(settings.getLastReportDateMillis() != 0)
 		{
 			int count;
 			String interval;
 			long difference = System.currentTimeMillis() - settings.getLastReportDateMillis();
 			count = (int)(difference/(1000*60*60*24));
 			if(count > 0)
 			{
 				if(count == 1)
 					interval = getString(R.string.day);
 				else
 					interval = getString(R.string.days);
 			}
 			else
 			{
 				count = (int)(difference/(1000*60*60));
 				if(count > 0)
 				{
 					if(count == 1)
 						interval = getString(R.string.hour);
 					else
 						interval = getString(R.string.hours);
 				}
 				else 
 				{
 					count = (int)(difference/(1000*60));
 					if(count > 0)
 					{
 						if(count == 1)
 							interval = getString(R.string.minute);
 						else
 							interval = getString(R.string.minute);
 					}
 					else
 					{
 						count = 0;
 						interval = getString(R.string.moments);
 					}	
 				}
 				
 			}
 			if(count == 0)
 				info = getText(R.string.manager_it_has_been).toString().replace("$", interval);
 			else
 				info = getText(R.string.manager_it_has_been).toString().replace("$", count + " " + interval);
 				
 			lastReportTV.setText(info);
 		}
 		else
 		{
 			lastReportTV.setText(R.string.manager_never_reported);
 		}
 		//lastReportTV.setText(getText(R.string.manager_it_has_been).toString().replace(target, replacement))
 	}
 	
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) 
 	{
 		
 	}
 }
