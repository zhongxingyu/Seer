 package net.thebub.privacyproxy.activities;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import net.thebub.privacyproxy.PrivacyProxyAPI.APICommand;
 import net.thebub.privacyproxy.PrivacyProxyAPI.APIResponse;
 import net.thebub.privacyproxy.PrivacyProxyAPI.PersonalDataEntry;
 import net.thebub.privacyproxy.PrivacyProxyAPI.WebLogWebsiteDataRequest;
 import net.thebub.privacyproxy.PrivacyProxyAPI.WebLogWebsiteDataResponse;
 import net.thebub.privacyproxy.PrivacyProxyAPI.WebLogWebsiteDataResponse.WebLogWebsiteData;
 import net.thebub.privacyproxy.R;
 import net.thebub.privacyproxy.util.DataTypeIDs;
 import net.thebub.privacyproxy.util.ServerConnection;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import com.google.protobuf.InvalidProtocolBufferException;
 
 /**
  * This activity will show the list of stored data for a certain webpage
  * @author dbub
  *
  */
 public class WebsiteDetailsActivity extends Activity {
 
 	private View mContentView;
 	private View mLoadingScreen;
 	
 	private ListView mDetailsList;
 	private WebsiteDetailsListAdapter mListAdapter;
 	private GetWebsiteDetailsTask mWebDetailsTask;
 	
 	private SharedPreferences mPreferences;
 	private Integer mWebsiteID;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_website_details);
 		
 		Intent intent = getIntent();
 		
 		// Load the prefreences to get the session ID
 		mPreferences = getSharedPreferences("PrivacyProxyPreferences", Context.MODE_PRIVATE);
 		
 		mContentView = findViewById(R.id.webdetails_data_view);
 		mLoadingScreen = findViewById(R.id.webdetails_status);
 		
 		mDetailsList = (ListView) findViewById(R.id.website_details_list);
 		mListAdapter = new WebsiteDetailsListAdapter(this, R.layout.layout_webdetails_entry, new ArrayList<WebsiteDetailsActivity.WeblogEntry>());
 		mDetailsList.setAdapter(mListAdapter);
 		
 		TextView website = (TextView) findViewById(R.id.website_details_website);
 		website.setText(intent.getStringExtra("websiteName"));		
 		
 		mWebsiteID = intent.getIntExtra("websiteID",-1);
 		if(mWebsiteID != -1) {
 			mWebDetailsTask = new GetWebsiteDetailsTask();
 			mWebDetailsTask.execute(mWebsiteID);
 		}
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Set up the menu
 	    MenuInflater inflater = getMenuInflater();
 	    inflater.inflate(R.menu.menu_webdetails, menu);
 	    return true;
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 	    switch (item.getItemId()) {
 	        case R.id.website_details_menu_refresh:
 	        	// Refresh the list of stored data of the webpage
 	        	mListAdapter.clear();
 	        	mWebDetailsTask = new GetWebsiteDetailsTask();
 	        	mWebDetailsTask.execute(mWebsiteID);
 	        default:
 	            return super.onOptionsItemSelected(item);
 	    }
 	}
 
 	/**
 	 * This task will load the list of private data stored for the current webpage
 	 * @author dbub
 	 *
 	 */
 	private class GetWebsiteDetailsTask extends AsyncTask<Integer, Void, List<WebLogWebsiteData>> {
 
 		@Override
 		protected void onPreExecute() {
 			super.onPreExecute();
 
 			mContentView.setVisibility(View.GONE);
 			mLoadingScreen.setVisibility(View.VISIBLE);
 		}
 
 		@Override
 		protected List<WebLogWebsiteData> doInBackground(Integer... websiteIDs) {
 
 			// Get the ID of the webpage
 			Integer websiteID = websiteIDs[0];
 			
 			// Get the connection instance
 			ServerConnection connection = ServerConnection.getInstance();
 			
 			// Build the request with the ID of the webpage
 			WebLogWebsiteDataRequest.Builder requestBuilder = WebLogWebsiteDataRequest.newBuilder();
 			
 			requestBuilder.setId(websiteID);
 
 			String sessionID = mPreferences.getString(getString(R.string.pref_session_id), "");
 
 			// Send the request and wait for the response
 			APIResponse response = connection.sendRequest(APICommand.getWebpageData,sessionID,requestBuilder.build().toByteString());
 
 			if(response == null || !response.getSuccess()) {
 				return null;
 			}
 
 			// Parse the list of website data from the response
 			WebLogWebsiteDataResponse websiteDataResponse;
 			try {
 				websiteDataResponse = WebLogWebsiteDataResponse.parseFrom(response.getData());
 			} catch (InvalidProtocolBufferException e) {
 				e.printStackTrace();
 				return null;
 			}
 
 			return websiteDataResponse.getDataList();
 		}
 
 		@Override
 		protected void onPostExecute(final List<WebLogWebsiteData> data) {
 			mWebDetailsTask = null;
 
 			if (data != null) {				
 				displayWebsiteData(data);
 				mLoadingScreen.setVisibility(View.GONE);
 				mContentView.setVisibility(View.VISIBLE);
 			} else {
 				mLoadingScreen.setVisibility(View.GONE);
 				mContentView.setVisibility(View.VISIBLE);
 			}
 		}
 
 		@Override
 		protected void onCancelled() {
 			mWebDetailsTask = null;
 			mLoadingScreen.setVisibility(View.GONE);
 			mContentView.setVisibility(View.VISIBLE);
 		}
 	}
 	
 	/**
 	 * This list adapter will show all data entries for the current webpage
 	 * @author dbub
 	 *
 	 */
 	private class WebsiteDetailsListAdapter extends ArrayAdapter<WeblogEntry> {
 
 		public WebsiteDetailsListAdapter(Context context, int textViewResourceId, 
 				ArrayList<WeblogEntry> list) {
 			super(context, textViewResourceId, list);
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			if (convertView == null) {
 				// Load the layout
 				LayoutInflater vi = (LayoutInflater) getSystemService(
 						Context.LAYOUT_INFLATER_SERVICE);
 				convertView = vi.inflate(R.layout.layout_webdetails_entry, null);
 			}
			
			// Get the current entry
			WeblogEntry entry = this.getItem(position);
 			
 			// Fill all fields with the corresponding data
 			TextView desc = (TextView) convertView.findViewById(R.id.weblogdetails_entry_desc);
 			desc.setText(entry.entry.getDescription());
 			TextView type = (TextView) convertView.findViewById(R.id.weblogdetails_entry_type);
 			type.setText(getString(DataTypeIDs.getID(entry.entry.getType())));
 			TextView date = (TextView) convertView.findViewById(R.id.weblogdetails_entry_date);
 			date.setText(entry.date);
 			
 			return convertView;
 		}
 
 	}
 	
 	private class WeblogEntry {
 		public String date;
 		public PersonalDataEntry entry;
 	}
 	
 	private void displayWebsiteData(List<WebLogWebsiteData> data) {
 		mListAdapter.clear();
 		
 		ArrayList<WeblogEntry> list = new ArrayList<WeblogEntry>();
 		
 		for (WebLogWebsiteData dateEntry : data) {
 			String date = dateEntry.getDate();
 			
 			for (PersonalDataEntry dataEntry : dateEntry.getEntryList()) {
 				WeblogEntry tmp = new WeblogEntry();
 				tmp.date = date;
 				tmp.entry = dataEntry;
 				
 				list.add(tmp);
 			}
 		}
 		
 		mListAdapter.addAll(list);
 	}
 
 }
