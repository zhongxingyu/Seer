 package uk.co.blackpepper.penguin.android;
 
 import java.util.List;
 
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import uk.co.blackpepper.penguin.R;
 import uk.co.blackpepper.penguin.client.Queue;
 import uk.co.blackpepper.penguin.client.QueueService;
 import uk.co.blackpepper.penguin.client.ServiceException;
 import uk.co.blackpepper.penguin.client.httpclient.HttpClientQueueService;
 import android.app.ListActivity;
 import android.app.LoaderManager;
 import android.content.AsyncTaskLoader;
 import android.content.Intent;
 import android.content.Loader;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.ListView;
 
 public class QueueListActivity extends ListActivity implements LoaderManager.LoaderCallbacks<List<Queue>>
 {
	private static final String API_PATH = "/api";

 	private static final String TAG = QueueListActivity.class.getName();
 
 	private QueueService queueService;
 
 	private QueueListAdapter adapter;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu)
 	{
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_queue_list, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item)
 	{
 		// Handle item selection
 		switch (item.getItemId())
 		{
 			case R.id.menu_settings:
 				Intent settingsIntent = new Intent(this, SettingsActivity.class);
 				startActivity(settingsIntent);
 				return true;
 				
 			default:
 				return super.onOptionsItemSelected(item);
 		}
 	}
 
 	@Override
 	public Loader<List<Queue>> onCreateLoader(int id, Bundle args)
 	{
 		return new AsyncTaskLoader<List<Queue>>(this)
 		{
 			@Override
 			protected void onStartLoading()
 			{
 				forceLoad();
 			}
 
 			@Override
 			public List<Queue> loadInBackground()
 			{
 				try
 				{
 					return queueService.getAll();
 				}
 				catch (ServiceException e)
 				{
 					// TODO Handle Properly
 					Log.e(TAG, "Error Loading Queues", e);
 					return null;
 				}
 			}
 		};
 	}
 
 	@Override
 	public void onLoadFinished(Loader<List<Queue>> loader, List<Queue> data)
 	{
 		adapter.setData(data);
 	}
 
 	@Override
 	public void onLoaderReset(Loader<List<Queue>> loader)
 	{
 		adapter.setData(null);
 	}
 	
 	@Override
 	protected void onListItemClick(ListView listView, View view, int position, long id)
 	{
 		Queue queue = (Queue) listView.getItemAtPosition(position);
 		
 		Intent intent = new Intent(this,  QueueActivity.class);
 		intent.putExtra("id", queue.getId());
 		startActivity(intent);
 	}
 
 	@Override
 	public void onResume() {
 		if (isServerUrlConfigured()) 
 		{
			queueService = new HttpClientQueueService(new DefaultHttpClient(), getServerUrl() + API_PATH);
 
 			adapter = new QueueListAdapter(this, R.layout.queue_list_item);
 			setListAdapter(adapter);
 			getLoaderManager().initLoader(0, null, this);
 		} else {
 			Log.i(TAG, "Displaying Setting from onResume");
 			displaySettings();
 		}
 		super.onResume();
 	}
 
 	private String getServerUrl()
 	{
 		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
 		String serverUrl = sharedPrefs.getString(SettingsActivity.PENGUIN_SERVER_URL_PREF_KEY, null);
 		return serverUrl;
 	}
 	
 	private boolean isServerUrlConfigured() 
 	{
		String defaultServerUrl = getResources().getString(R.string.pref_default_server_url);
 		String serverUrl = getServerUrl();
		
		if (defaultServerUrl.equals(serverUrl)) {
 			return false;
 		}
 		return true;
 	}
 
 	private void displaySettings() 
 	{
 		Intent settingsIntent = new Intent(this, SettingsActivity.class);
 		startActivity(settingsIntent);			
 	}
 
 }
