 package de.binaervarianz.holopod;
 
 import de.binaervarianz.holopod.db.DatabaseHandler;
 import de.binaervarianz.holopod.db.Episode;
 import android.app.ActionBar;
 import android.app.Activity;
 import android.app.DownloadManager;
 import android.app.DownloadManager.Request;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.TextView;
 
 public class EpisodeDetailsActivity extends Activity {
 	Episode episode;
 	DatabaseHandler db;
 
 	private DownloadManager dm;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.podcast_details);
 		episode = (Episode) getIntent().getSerializableExtra("Episode");
 		db = new DatabaseHandler(this);
 		ActionBar actionBar = getActionBar();
 		if (actionBar != null) {
 			actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP
 					| ActionBar.DISPLAY_SHOW_TITLE);
 			actionBar.setTitle(episode.toString());
 		}
 
 		TextView title = (TextView) findViewById(R.id.podcast_detail_title);
 		title.setText(episode.getTitle());
 		TextView subtitle = (TextView) findViewById(R.id.podcast_detail_subtitle);
 		subtitle.setText(episode.getSubtitle());
 		TextView description = (TextView) findViewById(R.id.podcast_detail_description);
 		description.setText(episode.getDescription());
 
 	}
 
 	@Override
 	protected void onResume() {
 		// TODO Auto-generated method stub
 		super.onResume();
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.episode_details, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		SharedPreferences settings = PreferenceManager
 				.getDefaultSharedPreferences(this);
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			finish();
 			return true;
 
 		case R.id.PlayPodcast:
 
 			return true;
 
 		case R.id.menu_dl_rm:
 			dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
 			final String url = episode.getEncUrl();
 			Request request = new Request(Uri.parse(url));
 			if (settings.getBoolean(SettingsFragment.SETTING_MOBILE_DOWNLOAD,
 					false)) {
 				request.setAllowedNetworkTypes(Request.NETWORK_WIFI
 						+ Request.NETWORK_MOBILE);
 			} else {
 				request.setAllowedNetworkTypes(Request.NETWORK_WIFI);
 			}
 			request.setTitle("HoloPod");
 			request.setDescription(episode.toString());
 			request.setMimeType(episode.getEncType());
 			request.setDestinationInExternalFilesDir(
 					this,
 					Environment.DIRECTORY_PODCASTS,
 					url.substring(url.lastIndexOf('/') + 1,
							url.length()));
 			episode.setDownloadId(dm.enqueue(request));
 			Intent dlService = new Intent(this, ReceiverService.class);
 			dlService.putExtra("Episode", episode);
 			startService(dlService);
 
 			return true;
 		case R.id.menu_settings:
 			startActivity(new Intent(getApplicationContext(),
 					SettingsActivity.class));
 
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 
 	}
 
 	
 
 }
