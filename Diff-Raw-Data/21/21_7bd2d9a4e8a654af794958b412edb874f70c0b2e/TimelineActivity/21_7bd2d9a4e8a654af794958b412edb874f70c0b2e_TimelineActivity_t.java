 package pt.isel.pdm.yamba;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import pt.isel.pdm.yamba.TwitterAsync.TwitterAsync;
 import pt.isel.pdm.yamba.TwitterAsync.listeners.TimelineObtainedListener;
 import pt.isel.pdm.yamba.ViewModel.Tweet;
 import winterwell.jtwitter.Twitter.Status;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 
 
 public class TimelineActivity extends MenuActivity implements OnSharedPreferenceChangeListener, OnItemClickListener, TimelineObtainedListener{
 
 	private static final TweetDateFormat _DateFormat = new TweetDateFormat();
 
 	private static class TweetHolder {
 		
 		TextView author, status, publicationTime;
 		
 	}
 	
 	private class TweetAdapter extends ArrayAdapter<Tweet> {
 						
 		public TweetAdapter() {
 			super(TimelineActivity.this, 0);
 		}
 		
 		public TweetAdapter(List<Tweet> data) {
 			super(TimelineActivity.this, 0, data);
 		}
 		
 		public void setData(ArrayList<Tweet> tweets) {
 			clear();
 			
 			for(Tweet status : tweets) {
 				add(status);
 			}
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			Tweet tweet = getItem(position);
 			View v = convertView;
 						
 			TweetHolder dataHolder;
 			
 			if(v == null) {
 				v = getLayoutInflater().inflate(R.layout.tweetlist, null);
 				dataHolder = new TweetHolder(); 
 				
 				dataHolder.author = (TextView) v.findViewById(R.id.tweetAuthor);
 				dataHolder.status = (TextView) v.findViewById(R.id.tweetStatus);
 				dataHolder.publicationTime = (TextView) v.findViewById(R.id.tweetPublicationTime);
 			
 				v.setTag(dataHolder);
 			}
 			else {
 				dataHolder = (TweetHolder) v.getTag();
 			}
 			
 			dataHolder.author.setText(tweet.getAuthor());
 			dataHolder.status.setText(tweet.getStatusSample(maxChar / 2));
 			dataHolder.publicationTime.setText(_DateFormat.format(tweet.getElapsedTime()));
 			
 			return v;
 		}		
 	}
 	
 	private TwitterAsync _client;
 	private TweetAdapter _adapter;
 	
 	private int maxChar = -1;
 	private int maxEntries = -1;
 	private SharedPreferences _preferences;
 	
 	private ArrayList<Tweet> _data;
 	private boolean _reload = true;
 	
 	private MenuItem _refreshMenuItem;
 	private boolean _menuState;
 	
 	/** Updates the values that are dependent of preferences, and all that depends on those values.
 	 * 
 	 * @param name - The name of the preference to update, or null if all.
 	 * @return true, if any preferences were changed.
 	 */
 	private boolean updatePreferences(String name) {
 		boolean changedPreference = false;
 				
 		if(name == null || name.equalsIgnoreCase("maxCharStatus")) {
 			maxChar = Integer.parseInt(_preferences.getString("maxCharStatus", null));
 			changedPreference = true;	
 		} 
 		
 		if(name == null || name.equalsIgnoreCase("maxTimelineEntries")) {
 			maxEntries = Integer.parseInt(_preferences.getString("maxTimelineEntries", null));
 			changedPreference = true;	
 		}
 		
 		return changedPreference;
 	}
 	
 	private void setMenuState(boolean state) {
 		_menuState = state;
 		
 		if(_refreshMenuItem != null) {
 			_refreshMenuItem.setEnabled(state);
 		}
 	}
 	
 	private void setLoading() {
 		TimelineActivity.this.findViewById(R.id.loadingLayout).setVisibility(View.VISIBLE);
 		TimelineActivity.this.findViewById(android.R.id.list).setVisibility(View.GONE);
 		
 		setMenuState(false);
 	}
 	
 	private void setLoaded() {
 		TimelineActivity.this.findViewById(R.id.loadingLayout).setVisibility(View.GONE);
 		TimelineActivity.this.findViewById(android.R.id.list).setVisibility(View.VISIBLE);
 
 		setMenuState(true);
 	}
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.timeline);
 		
 		_client = TwitterAsync.connect();
 		
 		ListView list = (ListView) findViewById(android.R.id.list);
 		list.setAdapter(_adapter = new TweetAdapter());
 		list.setOnItemClickListener(this);
 		
 		_preferences = PreferenceManager.getDefaultSharedPreferences(this);
 		_preferences.registerOnSharedPreferenceChangeListener(this);
 		updatePreferences(null);
 	}
 
 	@Override
     protected void doModifyMenu(Menu menu) {
 		_refreshMenuItem = menu.findItem(R.id.refresh_timeline);
 		enableMenuItem(menu, R.id.create_status);
    	enableMenuItem(menu, R.id.show_user_info);
 		
 		if(_menuState) enableMenuItem(menu, R.id.refresh_timeline);
     }	
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {		
 		if(item.getItemId() == R.id.refresh_timeline) {		
 			
 			setLoading();			
 			_client.getUserTimelineAsync(this);				
 		}
 
 		return super.onOptionsItemSelected(item);
 	}
 
 	@Override
 	protected void onResume() {	
 		super.onResume();
 
 		_client.setTimelineObtainedListener(this);		
 		if(_reload) {				
 			setLoading();
 			_client.getUserTimelineAsync(this);
 		}
 		else {
 			if(_data != null)
 				_adapter.setData(_data);
 			setLoaded();
 		}
 	}
 
 	private static final String RELOAD_KEY = "RELOAD";
 	private static final String DATA_KEY = "DATA";
 	
 	@Override
 	protected void onRestoreInstanceState(Bundle savedInstanceState) {
 		super.onRestoreInstanceState(savedInstanceState);
 		
 		_reload = savedInstanceState.getBoolean(RELOAD_KEY);
 		_data = savedInstanceState.getParcelableArrayList(DATA_KEY);
 	}
 
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		
 		outState.putBoolean(RELOAD_KEY, _reload);
 		outState.putParcelableArrayList(DATA_KEY, _data);
 	}
 
 	@Override
 	public void onSharedPreferenceChanged(SharedPreferences arg0, String arg1) {
 
 		_reload = true;
 		updatePreferences(arg1);		
 	}
 
 	@Override
 	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
 
 		Intent intent = new Intent(this, DetailsActivity.class);
 		intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
 		intent.putExtra(YambaApplication.TweetExtra, _data.get(arg2));
 		startActivity(intent);
 	}
 
 	@Override
 	public void onTimelineObtained(Iterable<Status> timeline) {
 		setLoaded();
 		
 		ArrayList<Tweet> tweets = new ArrayList<Tweet>();
 		int count = 0;
 		for(Status status : timeline) {
 			count++;
 			if(count > maxEntries) 
 				break;
 			tweets.add(Tweet.createFrom(status));
 		}
 		
 		_data = tweets;
 		_adapter.setData(tweets);
 		_reload = false;		
 	}		
 }
