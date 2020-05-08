 package dk.christer.malmofestivalen;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 
 import org.joda.time.DateTime;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.provider.BaseColumns;
 import android.text.format.DateUtils;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.Window;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.ScrollView;
 import android.widget.SimpleAdapter;
 import android.widget.AdapterView.OnItemClickListener;
 import dk.christer.malmofestivalen.analytics.GoogleAnalyticsWrapper;
 import dk.christer.malmofestivalen.data.EventProvider;
 import dk.christer.malmofestivalen.data.FavoritesProvider;
 import dk.christer.malmofestivalen.helpers.DateHelper;
 import dk.christer.malmofestivalen.helpers.ToastHelper;
 
 public class FavoritesActivity extends Activity {
 
 	DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 	DateFormat simpleHourFormat = new SimpleDateFormat("HH:mm");
 	DateFormat shortday = new SimpleDateFormat("d");
 	
     ArrayList<HashMap<String, String>> _favoriteData = new ArrayList<HashMap<String, String>>();
 	GoogleAnalyticsWrapper _tracker;
 
 	ListView _listView;
 	ScrollView _nofavorites; 
 	
 	MenuItem _favoritesAll;
 	MenuItem _favoritesFuture;
 	String _currentListViewType; 
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
 		setContentView(R.layout.favorites);
 
 		setupAnalytics();
 		
 		 _listView = (ListView)findViewById(R.id.favoritelist);
 		_nofavorites = (ScrollView)findViewById(R.id.nofavorites);
 		setTitle(R.string.startfavoriter);
       	CharSequence thetext = getText(R.string.clickmenuforoptions);
       	ToastHelper.ShowToastOnce(this, "FavoritesActivityMenuForMore", thetext.toString());
 	}
 
 
 	private void setupAnalytics() {
 		_tracker = GoogleAnalyticsWrapper.getInstance();
 		_tracker.trackPageView("/view/favorites");
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		menu.add(0, 0, 0, R.string.importmenutext);
 		_favoritesAll = menu.add(0, 1, 0, R.string.favorites_all);
 		_favoritesFuture = menu.add(0, 2, 0, R.string.favorites_future);
 		MenuItemVisibilityState();
 		return super.onCreateOptionsMenu(menu);
 	}
 
 	private void MenuItemVisibilityState() {
 		if (Settings.getWantedFavoritesList(this) == Settings.FAVORITES_ALL) {
 			_favoritesAll.setVisible(false);
 			_favoritesFuture.setVisible(true);
 		}
 		else {
 			_favoritesAll.setVisible(true);
 			_favoritesFuture.setVisible(false);
 		}
 
 	}
 
 	@Override
 	public void onOptionsMenuClosed(Menu menu) {
 		super.onOptionsMenuClosed(menu);
 		MenuItemVisibilityState();
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 
 		switch (item.getItemId()) {
 		case 0:
 			startActivity(new Intent(FavoritesActivity.this, ImportActivity.class));
 			break;
 		case 1:
 			Settings.saveWantedFavoritesList(this, Settings.FAVORITES_ALL);
 			BindList();
 			break;
 		case 2:
 			Settings.saveWantedFavoritesList(this, Settings.FAVORITES_FUTURE);
 			BindList();
 			break;
 		default:
 			break;
 		}
 		
 		return super.onOptionsItemSelected(item);
 	}
 	
 	private void HideControl(View control) {
 		control.setVisibility(View.GONE);
 	}
 	
 	private void ShowControl(View control) {
 		control.setVisibility(View.VISIBLE);
 	}
 	
 	@Override
 	protected void onStart() {
 		super.onStart();
 		HideControl(_listView);
 		HideControl(_nofavorites);
 		BindList();
 	}
 	
 	private void BindList() {
 		GenerateFavoriteListData listGenerator = new GenerateFavoriteListData();
 		if (Settings.getWantedFavoritesList(this) == Settings.FAVORITES_ALL) {
 			listGenerator.execute(true);
 		} else {
 			listGenerator.execute(false);
 		}
 	}
 
 	public class GenerateFavoriteListData extends AsyncTask<Boolean, Object, ArrayList<HashMap<String, String>>> {
 		
 		@Override
 		protected ArrayList<HashMap<String, String>> doInBackground(Boolean... includeOld) {
 			ArrayList<HashMap<String, String>> favoriteDataResult = new ArrayList<HashMap<String, String>>();
 			Cursor favorites = getContentResolver().query(FavoritesProvider.CONTENT_URI_FAVORITES, null, null, null, null);
 			
             while (favorites.moveToNext()) {
                 HashMap<String, String> itemMap = CreateMapItem(favorites, includeOld[0]);
                 if (itemMap != null) {
                 	favoriteDataResult.add(itemMap);
                 }
             }
             favorites.close();
 			return favoriteDataResult;
 
 		}
 		@Override
 		protected void onPostExecute(ArrayList<HashMap<String, String>> result) {
 			super.onPostExecute(result);
 			_favoriteData = result;
 			
 			if (_favoriteData.size() > 0) {
 				ShowControl(_listView);
 				SimpleAdapter adapter = new SimpleAdapter(FavoritesActivity.this, 
 						_favoriteData,
 						R.layout.eventitemrow,
 						new String[] {"title", "time", "scen"},
 						new int[] {R.id.eventitemrowtitle, R.id.eventitemrowtimeresume, R.id.eventitemrowscene}
 						);
 				_listView.setAdapter(adapter);
 			}
 			else {
 				ShowControl(_nofavorites);
 			}
 			setProgressBarIndeterminateVisibility(false);
 		}
 		
 		@Override
 		protected void onPreExecute() {
 			super.onPreExecute();
 			setProgressBarIndeterminateVisibility(true);
 			ListView listView = (ListView)findViewById(R.id.favoritelist);
 			listView.setOnItemClickListener(new OnItemClickListener() {
 
 				@Override
 				public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
 					HashMap<String, String> hash = _favoriteData.get(arg2);
 					String id = hash.get("_id");
 					_tracker.trackClick("/click/event/favorites");
 					Intent intent = new Intent(FavoritesActivity.this, EventDetailActivity.class);
 					intent.putExtra(EventDetailActivity.EXTRA_SCHEDULEID, Integer.parseInt(id));
 					startActivity(intent);
 				}
 			});
 
 		}
 	}
 	
 	private HashMap<String, String> CreateMapItem(Cursor favoriteCursor, Boolean includeOld) {
 		HashMap<String, String> item = null;
         Uri uri = Uri.withAppendedPath(EventProvider.CONTENT_URI_EVENTS_BY_BUSINESS_ID, favoriteCursor.getString(favoriteCursor.getColumnIndex(FavoritesProvider.FAVORITE_KEY_BUSINESSID)));
         Cursor event = getContentResolver().query(uri, null, null, null, null);
 
         if (event != null) { 
 			if (event.moveToNext()) {
 				String endDate = event.getString(event.getColumnIndex(EventProvider.EVENT_KEY_ENDDATE));
 				if (includeOld || DateHelper.AsDate(endDate).isAfterNow()) {
 					item = new HashMap<String, String>();
 					item.put("_id", Integer.toString(event.getInt(event.getColumnIndex(BaseColumns._ID))));
 					item.put("title", event.getString(event.getColumnIndex(EventProvider.EVENT_KEY_TITLE)));
 					item.put("scen", event.getString(event.getColumnIndex(EventProvider.EVENT_KEY_SCENETITLE)));
 					
 					String dateString = DateHelper.createShortDateResume(event.getString(event.getColumnIndex(EventProvider.EVENT_KEY_STARTDATE)), event.getString(event.getColumnIndex(EventProvider.EVENT_KEY_ENDDATE)));
 		
 					item.put("time", dateString);
 				}
 			}
 			event.close();
 		}
 		return item;
 	}
 }
