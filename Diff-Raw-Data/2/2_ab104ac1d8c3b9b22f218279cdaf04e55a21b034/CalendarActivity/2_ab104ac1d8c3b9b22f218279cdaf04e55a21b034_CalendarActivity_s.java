 package com.tvshowtrakt;
 
 import java.util.Date;
 import java.util.List;
 
 import com.jakewharton.trakt.ServiceManager;
 import com.jakewharton.trakt.entities.CalendarDate;
 import com.jakewharton.trakt.entities.CalendarDate.CalendarTvShowEpisode;
 import com.jakewharton.trakt.entities.TvShow;
 import com.tvshowtrakt.adapters.LazyAdapterGalleryEpisodes;
 import com.tvshowtrakt.adapters.LazyAdapterGalleryTrending;
 import com.tvshowtrakt.adapters.ViewPagerAdapterCalendar;
 import com.viewpagerindicator.TitlePageIndicator;
 
 import greendroid.app.GDActivity;
 import android.app.AlertDialog;
 import android.content.SharedPreferences;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.support.v4.view.ViewPager;
 import android.widget.Gallery;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 
 /**
  * Actividade que  iniciada quando  carregado em calendrio
  */
 public class CalendarActivity extends GDActivity {
 
 	private boolean login;
 	private String password;
 	private String username;
 	public String apikey = "a7b42c4fb5c50a85c68731b25cc3c1ed";
 	private CalendarActivity calendarActivity;
 	ListView mListSeasons;
 	private ViewPagerAdapterCalendar mAdapter;
 	private ViewPager mPager;
 	private TitlePageIndicator mIndicator;
 	public boolean[] tSeen;
 	Gallery mGalleryEpisodes;
 	public LazyAdapterGalleryEpisodes galleryEpisodesAdapter;
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see greendroid.app.GDActivity#onCreate(android.os.Bundle)
 	 */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setActionBarContentView(R.layout.calendar_itemlist);
 		setTitle("Calendar");
 
 		calendarActivity = this;
 		getPrefs();
 		// mAdapter = new ViewPagerAdapterCalendar(getApplicationContext());
 		//
 		// mPager = (ViewPager) findViewById(R.id.pager);
 		// mPager.setAdapter(mAdapter);
 		//
 		// mIndicator = (TitlePageIndicator) findViewById(R.id.indicator);
 		// mIndicator.setViewPager(mPager);
 		// mIndicator.setCurrentItem(1);
 		new downloadCalendarInfo().execute();
 	}
 
 	/**
 	 * Metodo para obter as preferencias da applicao
 	 */
 	private void getPrefs() {
 		// Get the xml/preferences.xml preferences
 		SharedPreferences prefs = PreferenceManager
 				.getDefaultSharedPreferences(getBaseContext());
 		login = prefs.getBoolean("login", false);
 
 		username = prefs.getString("username", "username");
 		password = prefs.getString("password", "password");
 	}
 
 	/**
 	 * Metodo para obter as preferencias da applicao, quando esta  retomada,
 	 * ao invs de iniciada
 	 */
 	public void onResume() {
 		super.onResume();
 		SharedPreferences prefs = PreferenceManager
 				.getDefaultSharedPreferences(getBaseContext());
 		login = prefs.getBoolean("rating", true);
 
 		username = prefs.getString("username", "username");
 		password = prefs.getString("password", "password");
 	}
 
 	private class downloadCalendarInfo extends
 			AsyncTask<String, Void, List<CalendarDate>> {
 		private Exception e = null;
 
 		/**
 		 * primeiro mtodo a correr, usar o manager para obter os dados da api
 		 */
 		@Override
 		protected List<CalendarDate> doInBackground(String... params) {
 
 			try {
 				Date d = new Date();
 				ServiceManager manager = new ServiceManager();
 				manager.setAuthentication(username,
 						new Password().parseSHA1Password(password));
 				manager.setApiKey(apikey);
 
 				List<CalendarDate> calShows = manager.userService()
						.calendarShows("lopesdasilva").date(d).fire();
 				return calShows;
 
 			} catch (Exception e) {
 				this.e = e;
 			}
 			return null;
 		}
 
 		/**
 		 * Os resultados so passados para aqui e depois tratados aqui.
 		 */
 		protected void onPostExecute(List<CalendarDate> result) {
 			if (e == null) {
 				TextView t = (TextView) findViewById(R.id.textViewDate);
 				CalendarDate calendarDate = result.get(0);
 //				Thursday March 1, 2012
 					t.setText(calendarDate.date.toLocaleString());
 					
 					String mFanArt[] = new String[calendarDate.episodes.size()];
 					String mName[] = new String[calendarDate.episodes.size()];
 					String mEpisode[] = new String[calendarDate.episodes.size()];
 					int i=0;
 					for (CalendarTvShowEpisode e: calendarDate.episodes){
 						mFanArt[i]=e.episode.images.screen;
 						mName[i]=e.show.title;
 						mEpisode[i]=e.episode.title;
 						i++;
 					}
 					
 					galleryEpisodesAdapter = new LazyAdapterGalleryEpisodes(calendarActivity, mFanArt, mName, mEpisode);
 					mGalleryEpisodes = (Gallery) findViewById(R.id.galleryEpisodes);
 					mGalleryEpisodes.setAdapter(galleryEpisodesAdapter);
 					
 					
 			} else
 				goBlooey(e);
 		}
 	}
 
 	private void goBlooey(Throwable t) {
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 
 		builder.setTitle("Connection Error")
 				.setMessage("Movie Trakt can not connect with trakt service")
 				.setPositiveButton("OK", null).show();
 	}
 
 }
