 package se.goagubbar.twee;
 
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 
 import se.goagubbar.twee.Adapters.SeriesAdapter;
 import se.goagubbar.twee.Models.Episode;
 import se.goagubbar.twee.Models.ExtendedSeries;
 import se.goagubbar.twee.Models.Profile;
 import se.goagubbar.twee.Models.Series;
 import se.goagubbar.twee.XMLParser;
 import android.app.AlertDialog;
 import android.app.SearchManager;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.ActionMode;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.ListView;
 import android.widget.SearchView;
 import android.widget.Toast;
 
 public class HomeActivity extends BaseActivity{
 
 
 	private DatabaseHandler db;
 	static final String KEY_URL = "http://www.thetvdb.com/api/GetSeries.php?seriesname=";
 	static final String KEY_FULLURL = "http://www.thetvdb.com//data/series/%s/all/";
 	static final String KEY_SERIES = "Series";
 	static final String KEY_EPISODE = "Episode";
 	static final String KEY_ID = "seriesid";
 	static final String KEY_NAME = "SeriesName";
 	static final String KEY_AIRED = "FirstAired";
 	static final String KEY_ACTORS = "Actors";
 	static final String KEY_RATING = "Rating";
 	static final String KEY_IMAGE = "banner";
 	static final String KEY_HEADER = "fanart";
 	static final String KEY_STATUS = "Status";
 	static final String KEY_AIRSDAY = "Airs_DayOfWeek";
 	static final String KEY_AIRTIME = "Airs_Time";
 	static final String KEY_GENRE = "Genre";
 	static final String KEY_SUMMARY = "Overview";
 	static final String KEY_IMDBID = "IMDB_ID";
 	static final String KEY_LASTUPDATED = "lastupdated";
 
 	static final String KEY_EP_AIRED = "FirstAired";
 	static final String KEY_EP_EPISODE = "EpisodeNumber";
 	static final String KEY_EP_SEASON = "SeasonNumber";
 	static final String KEY_EP_SUMMARY = "Overview";
 	static final String KEY_EP_TITLE = "EpisodeName";
 	static final String KEY_EP_ID = "id";
 
 	static List<Series> series;
 	private static ListView mySeries;
 	protected Object mActionMode;
 	public String selectedItem = null;
 	public int selectedProfile;
 	public int newSelectedProfile;
 	SeriesAdapter seriesAdapter;
 	Menu menu;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		final SharedPreferences settings = getSharedPreferences("Twee", 0);
 		Utils.selectedProfile = settings.getInt("Profile", 1);
 		
 		setContentView(R.layout.layout_home);
 
 		mySeries = (ListView)findViewById(R.id.lstMySeries);
 		mySeries.setDividerHeight(1);
 		mySeries.setLongClickable(true);
 
 		registerForContextMenu(mySeries);
 
 	
 		//findViewById(R.id.menu_chooseprofile).
 
 
 		mySeries.setOnItemLongClickListener(new OnItemLongClickListener() {
 
 			@Override
 			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
 				if(mActionMode != null){
 					return false;
 				}
 				mActionMode = mySeries.startActionMode(mActionModeCallback);
 				view.setSelected(true);
 				selectedItem = view.getTag(R.string.homeactivity_tag_seriesid).toString();
 				return true;
 			}
 
 		});
 
 		mySeries.setOnItemClickListener(new OnItemClickListener() {
 			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 
 				Intent intent = new Intent(getBaseContext(), OverviewActivity.class);
 				intent.putExtra("SeriesId", view.getTag(R.string.homeactivity_tag_id).toString());
 				startActivity(intent);
 
 			}
 		});
 
 		new GetMySeries().execute();
 
 	}
 	
 	@Override
 	protected void onResume() {
 		// TODO Auto-generated method stub
 		super.onResume();
 		db = new DatabaseHandler(this);
 	}
 
 	@Override
 	protected void onRestart() {
 		// TODO Auto-generated method stub
 		super.onRestart();
 		seriesAdapter.notifyDataSetChanged();
 		//new GetMySeries().execute();
 		//mySeries.notify();
 	}
 
 	private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {
 
 		@Override
 		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
 			return false;
 		}
 
 		@Override
 		public void onDestroyActionMode(ActionMode mode) {
 			selectedItem = null;
 			mActionMode = null;
 		}
 
 		@Override
 		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
 			MenuInflater inflater = mode.getMenuInflater();
 			inflater.inflate(R.menu.menu_contextual_home, menu);
 
 			return true;
 		}
 
 		@Override
 		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
 			switch (item.getItemId()) {
 			case R.id.menu_delete:
 				deleteSeries(selectedItem);
 				new GetMySeries().execute();
 				mode.finish();		
 				return true;
 
 			case R.id.menu_refresh:
 				Toast.makeText(HomeActivity.this, R.string.message_episodes_updates, Toast.LENGTH_SHORT).show();
 				new GetNewEpisodes().execute(selectedItem);
 				mode.finish();
 				return true;
 
 			default:
 				return false;
 			}
 		}
 	};
 
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.menu_home, menu);
 
 		setupSearchView(menu);
 		this.menu = menu;
 		return true;
 	}
 
 	private void setupSearchView(Menu menu) {
 		SearchManager searchManager =                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
 		SearchView searchView =                 (SearchView) menu.findItem(R.id.menu_add).getActionView();
 		searchView.setSearchableInfo(
 				searchManager.getSearchableInfo(getComponentName()));
 	}
 
 	public boolean onOptionsItemSelected(MenuItem item){
 
 		switch(item.getItemId()){
 		case android.R.id.home:
 			Intent intent = new Intent(this, HomeActivity.class);
 			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 			startActivity(intent);
 			return true;
 
 		case R.id.menu_add:
 			return true;
 
 		case R.id.menu_refresh:
 			seriesAdapter.notifyDataSetChanged();
 			//new GetMySeries().execute();
 			return true;
 
 		case R.id.menu_calendar:
 			startActivity(new Intent(this,CalendarActivity.class));
 			return true;
 
 		case R.id.menu_settings:
 			startActivity(new Intent(this,SettingsActivity.class));
 			return true;
 
 		case R.id.menu_about:
 			startActivity(new Intent(this,AboutActivity.class));
 			return true;
 
 		case R.id.menu_chooseprofile:
 			displayProfileChooser();
 			return true;
 
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	private void displayProfileChooser(){
 
 		
 		selectedProfile = Utils.selectedProfile;
 		newSelectedProfile = selectedProfile;
 
 		final ArrayList<Profile> profiles = db.GetAllprofiles();
 		ArrayList<String> availableProfiles = new ArrayList<String>();
 
 		for (int i = 0; i < profiles.size(); i++) {
 			availableProfiles.add(profiles.get(i).getName());
 			if(profiles.get(i).getId() == selectedProfile)
 			{
 				selectedProfile = i;
 			}
 		}
 
 
 		String[] allProfiles = availableProfiles.toArray(new String[availableProfiles.size()]);
 
 		AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
 		builder.setTitle(R.string.dialog_selectprofile_header)
 		.setSingleChoiceItems(allProfiles, selectedProfile, new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int which) {
 				newSelectedProfile = profiles.get(which).getId();
 			}
 		})
 
 		.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int id) {
 				final SharedPreferences settings = getSharedPreferences("Twee", 0);
 				SharedPreferences.Editor editor = settings.edit();
 				Utils.selectedProfile = newSelectedProfile;
 				editor.putInt("Profile", newSelectedProfile);
 				editor.apply();
 				new GetMySeries().execute();
 			}
 		})
 		.setNegativeButton(R.string.delete_cancel, new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int id) {
 				// Just close
 			}
 		}).show();	
 
 	}
 
 	private void deleteSeries(final String seriesId)
 	{
 		new AlertDialog.Builder(this)
 		.setMessage(R.string.delete_text)
 		.setTitle(R.string.delete_title)
 		.setCancelable(false)
 		.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int id) {
 				db.DeleteShow(seriesId);
 				new GetMySeries().execute();
 			}
 		})
 		.setNegativeButton(R.string.dialog_cancel, null)
 		.show();
 
 
 	}
 
 	public class GetMySeries extends AsyncTask<String, Void, ArrayList<ExtendedSeries>>{
 
 		@Override
 		protected ArrayList<ExtendedSeries> doInBackground(String... params) {
 			ArrayList<ExtendedSeries> series = new ArrayList<Models.ExtendedSeries>();
			 series = db.GetMyShows();
 			return series;
 		}
 		
 
 		@Override
 		protected void onPostExecute(ArrayList<ExtendedSeries> result) {
 			
 			int selectedView = getSharedPreferences("Twee", 0).getInt("Display", 0);
 			int viewToDisplay = 0;
 			switch (selectedView) {
 			case 0:
 				viewToDisplay = R.layout.listitem_series;
 				break;
 			case 1:
 				viewToDisplay = R.layout.listitem_series_alt;
 				break;
 			default:
 				break;
 			}
 			
 			seriesAdapter = new SeriesAdapter(getApplicationContext(), viewToDisplay, mySeries, result);
 			mySeries.setAdapter(seriesAdapter);
 
 		}
 
 	}
 
 
 	public class GetNewEpisodes extends AsyncTask<String, Void, Boolean>
 	{
 
 		@Override
 		protected Boolean doInBackground(String... q) {
 			String completeAddress = String.format(KEY_FULLURL, q[0]);
 			XMLParser parser = new XMLParser();
 
 			String xml = parser.getXmlFromUrl(completeAddress);		
 
 			Document doc = parser.getDomElement(xml);
 			NodeList nl = doc.getElementsByTagName(KEY_SERIES);
 			NodeList episodes = doc.getElementsByTagName(KEY_EPISODE);		
 
 			ArrayList<Episode> Episodes = new ArrayList<Episode>();
 
 			//Fetch series and save;
 
 			Element e = (Element) nl.item(0);
 
 
 			for(int i = 0;i < episodes.getLength();i++)
 			{
 				Episode ep = new Episode();
 				e = (Element) episodes.item(i);
 				ep.setAired(parser.getValue(e, KEY_EP_AIRED));
 				ep.setEpisode(parser.getValue(e, KEY_EP_EPISODE));
 				ep.setSeason(parser.getValue(e, KEY_EP_SEASON));
 				ep.setSeriesId(q[0].toString());
 				ep.setSummary(parser.getValue(e, KEY_EP_SUMMARY));
 				ep.setTitle(parser.getValue(e, KEY_EP_TITLE));
 				ep.setLastUpdated(parser.getValue(e, KEY_LASTUPDATED));
 				ep.setEpisodeId(parser.getValue(e, KEY_EP_ID));
 				ep.setWatched("0");
 				Episodes.add(ep);
 			}
 
 			Log.d("Parsat episoderna","Klart");
 
 			db.UpdateAndAddEpisodes(Episodes, q[0]);
 
 
 
 			return true;
 		}
 
 		@Override
 		protected void onPostExecute(Boolean result) {
 			// TODO Auto-generated method stub
 			super.onPostExecute(result);
 			Toast.makeText(HomeActivity.this, R.string.message_episodes_updates_done, Toast.LENGTH_SHORT).show();
 
 		}
 
 	}
 
 	
 }
