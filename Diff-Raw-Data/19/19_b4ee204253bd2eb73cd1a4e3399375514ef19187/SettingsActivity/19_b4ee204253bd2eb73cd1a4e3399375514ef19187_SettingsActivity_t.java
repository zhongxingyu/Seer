 package net.thebub.privacyproxy.activities;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import net.thebub.privacyproxy.PrivacyProxyAPI.APICommand;
 import net.thebub.privacyproxy.PrivacyProxyAPI.APIResponse;
 import net.thebub.privacyproxy.PrivacyProxyAPI.GetSettingsResponse;
 import net.thebub.privacyproxy.PrivacyProxyAPI.PersonalDataEntry;
 import net.thebub.privacyproxy.R;
 import net.thebub.privacyproxy.util.DataTypeIDs;
 import net.thebub.privacyproxy.util.ServerConnection;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import com.google.protobuf.InvalidProtocolBufferException;
 
 /**
  * This activity displays a list of existing settings of the user.
  * @author dbub
  *
  */
 public class SettingsActivity extends Activity {
 
 	public ListView mListView;
 	private SettingsListAdapter mListAdapter;
 	private GetSettingsTask mSettingsTask;
 	
 	private SharedPreferences mPreferences;
 	private View mLoadingScreen;
 
 	/**
 	 * The get settings task, is used to acquire a list of existing settings
 	 * @author dbub
 	 *
 	 */
 	private class GetSettingsTask extends AsyncTask<Void, Void, List<PersonalDataEntry>> {
 		
 		@Override
 		protected void onPreExecute() {
 			super.onPreExecute();
 			
 			mListView.setVisibility(View.GONE);
 			mLoadingScreen.setVisibility(View.VISIBLE);
 		}
 		
 		@Override
 		protected List<PersonalDataEntry> doInBackground(Void... none) {
 			// Get the connection instance
 			ServerConnection connection = ServerConnection.getInstance();
 
 			String sessionID = mPreferences.getString(getString(R.string.pref_session_id), "");
 			
 			// Send the request and wait for the list of settings
 			APIResponse response = connection.sendRequest(APICommand.getSettings,sessionID);
 
 			if(response == null || !response.getSuccess()) {
 				return null;
 			}
 
 			GetSettingsResponse settingsResponse;
 			try {
 				settingsResponse = GetSettingsResponse.parseFrom(response.getData());
 			} catch (InvalidProtocolBufferException e) {
 				e.printStackTrace();
 				return null;
 			}
 
 			return settingsResponse.getEntryList();
 		}
 
 		@Override
 		protected void onPostExecute(final List<PersonalDataEntry> data) {
 			mSettingsTask = null;
 			
 			// Build the list of settings out of the response
 			ArrayList<PersonalDataEntry> dataArray = new ArrayList<PersonalDataEntry>(data);
 			
 			if (data != null) {
 				// Show the setting list and hide the loading spinner
 				displaySettings(dataArray);
 				mLoadingScreen.setVisibility(View.GONE);
 				mListView.setVisibility(View.VISIBLE);
 			} else {
 				mLoadingScreen.setVisibility(View.GONE);
 				mListView.setVisibility(View.VISIBLE);				
 			}
 		}
 
 		@Override
 		protected void onCancelled() {
 			mSettingsTask = null;
 			mLoadingScreen.setVisibility(View.GONE);
 			mListView.setVisibility(View.VISIBLE);
 		}
 	}
 
 	/**
 	 * This list adapter creates displays the list of settings in the listview
 	 * @author dbub
 	 *
 	 */
 	private class SettingsListAdapter extends ArrayAdapter<PersonalDataEntry> {
 
 		public SettingsListAdapter(Context context, int textViewResourceId, 
 				ArrayList<PersonalDataEntry> settingsList) {
 			super(context, textViewResourceId, settingsList);
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			if (convertView == null) {
 				// Load the layout for ech entry
 				LayoutInflater vi = (LayoutInflater) getSystemService(
 						Context.LAYOUT_INFLATER_SERVICE);
 				convertView = vi.inflate(R.layout.layout_settings_entry, null);
 			}
 			
			// Get the setting for the current position
 			PersonalDataEntry setting = this.getItem(position);
 			
 			// Fill all field of the setting entry with the corresponding data
 			TextView title = (TextView) convertView.findViewById(R.id.settingslist_entry_title);
 			title.setText(setting.getDescription());
 			TextView type = (TextView) convertView.findViewById(R.id.settingslist_entry_type);
 			type.setText(getString(DataTypeIDs.getID(setting.getType())));
 
 			return convertView;
 
 		}
 
 	}
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_settings);
 
 		this.mListView = (ListView) findViewById(R.id.settings_list_view);
 		
 		mListAdapter = new SettingsListAdapter(this, R.layout.layout_weblog_entry, new ArrayList<PersonalDataEntry>());
 
 		mListView.setAdapter(mListAdapter);
 		
 		this.mListView.setOnItemClickListener(new OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
 				// When a list elemet is clicked open the details activity and pass the data of the entry over
 				PersonalDataEntry item = (PersonalDataEntry) parent.getAdapter().getItem(position);
 				
 				Intent openSettingDeatilsIntent = new Intent(parent.getContext(), SettingDetailActivity.class);
 				
 				openSettingDeatilsIntent.putExtra("settingID", item.getId());
 				openSettingDeatilsIntent.putExtra("settingDesc", item.getDescription());
 				openSettingDeatilsIntent.putExtra("settingType", getString(DataTypeIDs.getID(item.getType())));
 				openSettingDeatilsIntent.putExtra("settingData", item);
 				
 				startActivityForResult(openSettingDeatilsIntent, 0);
 			}
 			
 		});
 		
 		mPreferences = getSharedPreferences("PrivacyProxyPreferences", Context.MODE_PRIVATE); 
 
 		mLoadingScreen = findViewById(R.id.settings_status);
 		
 		mSettingsTask = new GetSettingsTask();
 		mSettingsTask.execute();
 	}
 	
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		// Evaluate the result of the create or detail activity and refresh the list if the activities were successful
 	    if (resultCode == RESULT_OK) {
 	        Log.d("SETTINGS", "Child returned: ok");
 	        mListAdapter.clear();
 	        mSettingsTask = new GetSettingsTask();
 	        mSettingsTask.execute();
 	    }
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Load the menu and fill it
 	    MenuInflater inflater = getMenuInflater();
 	    inflater.inflate(R.menu.menu_settingslist, menu);
 	    return true;
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Check which menu entry was selected
 	    switch (item.getItemId()) {
 	        case R.id.settings_menu_add:
 	        	// Open the new setting activity
 	            newSetting();
 	            return true;
 	        case R.id.settings_menu_refresh:
 	        	// Refresh the list of settings
 	        	mListAdapter.clear();
 	        	mSettingsTask = new GetSettingsTask();
 	        	mSettingsTask.execute();
 	        default:
 	            return super.onOptionsItemSelected(item);
 	    }
 	}
 
 	private void displaySettings(ArrayList<PersonalDataEntry> entries) {
 		// Clear the previous entries and fill with 
 		mListAdapter.clear();
 		mListAdapter.addAll(entries);
 	}
 	
 	
 	private void newSetting() {
 		// Open the new setting activity
 		Intent openCreateSettingIntent = new Intent(this, SettingCreateActivity.class);
 				
 		startActivityForResult(openCreateSettingIntent, 0);
 	}
 }
