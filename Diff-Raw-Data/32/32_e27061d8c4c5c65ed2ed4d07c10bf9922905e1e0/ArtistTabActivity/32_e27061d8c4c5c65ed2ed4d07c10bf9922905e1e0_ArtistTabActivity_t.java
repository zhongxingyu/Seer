 package com.example.lyrics;
 
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Locale;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 import android.app.ActionBar;
 import android.app.FragmentTransaction;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 
 import com.example.lyrics.model.SearchResultsRow;
 
 public class ArtistTabActivity extends FragmentActivity implements
 		ActionBar.TabListener {
 

 	SectionsPagerAdapter mSectionsPagerAdapter;
 
 	private ArrayList<SearchResultsRow> resultRows = new ArrayList<SearchResultsRow>();
 	private boolean songsFetched, artistFetched;
 	private static TableLayout topSongsTableLayout;
 	private static String artistInfoText;
 	private static TextView artistInfoTextView;
 

 	ViewPager mViewPager;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_artist_tab);
 
 		// Set up the action bar.
 		final ActionBar actionBar = getActionBar();
 		actionBar.setDisplayShowTitleEnabled(false);
 		actionBar.setDisplayShowHomeEnabled(false);
 		actionBar.setDisplayUseLogoEnabled(false);
 		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
 
 		// Create the adapter that will return a fragment for each of the three
 		// primary sections of the app.
 		mSectionsPagerAdapter = new SectionsPagerAdapter(
 				getSupportFragmentManager());
 
 		// Set up the ViewPager with the sections adapter.
 		mViewPager = (ViewPager) findViewById(R.id.pager);
 		mViewPager.setAdapter(mSectionsPagerAdapter);
 
 		// When swiping between different sections, select the corresponding
 		// tab. We can also use ActionBar.Tab#select() to do this if we have
 		// a reference to the Tab.
 		mViewPager
 				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
 					@Override
 					public void onPageSelected(int position) {
 						actionBar.setSelectedNavigationItem(position);
 					}
 				});
 
 		// For each of the sections in the app, add a tab to the action bar.
 		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
 			// Create a tab with text corresponding to the page title defined by
 			// the adapter. Also specify this Activity object, which implements
 			// the TabListener interface, as the callback (listener) for when
 			// this tab is selected.
 			actionBar.addTab(actionBar.newTab()
 					.setText(mSectionsPagerAdapter.getPageTitle(i))
 					.setTabListener(this));
 		}
 
 		Intent i = this.getIntent();
 		String artist = i.getStringExtra(SongLyricsActivity.TITLE_KEY);
 		String url = i.getStringExtra(SongLyricsActivity.URL_KEY);
 		GetSongsTask gst = new GetSongsTask();
 		gst.execute(url);
 		String artist_url = "http://en.wikipedia.org/wiki/"
 				+ artist.replaceAll("\\s", "_");
 		GetArtistInfo gai = new GetArtistInfo();
 		gai.execute(artist_url);
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.artist_tab, menu);
 		return true;
 	}
 
 	@Override
 	public void onTabSelected(ActionBar.Tab tab,
 			FragmentTransaction fragmentTransaction) {
 		// When the given tab is selected, switch to the corresponding page in
 		// the ViewPager.
 		mViewPager.setCurrentItem(tab.getPosition());
 	}
 
 	@Override
 	public void onTabUnselected(ActionBar.Tab tab,
 			FragmentTransaction fragmentTransaction) {
 	}
 
 	@Override
 	public void onTabReselected(ActionBar.Tab tab,
 			FragmentTransaction fragmentTransaction) {
 	} 
 

 	public class SectionsPagerAdapter extends FragmentPagerAdapter {
 
 		public SectionsPagerAdapter(FragmentManager fm) {
 			super(fm);
 		}
 
 		@Override
 		public Fragment getItem(int position) {
 			// getItem is called to instantiate the fragment for the given page.
 			// Return a DummySectionFragment (defined as a static inner class
 			// below) with the page number as its lone argument.
 			if (position == 0) {
 				Fragment fragment = new ArtistInfoFragment();
 				return fragment;
 			} else if (position == 1) {
 				Fragment fragment = new TopSongsFragment();
 				return fragment;
 
 			} else
 				return null;
 		}
 
 		@Override
 		public int getCount() {
 			// Show 3 total pages.
 			return 2;
 		}
 
 		@Override
 		public CharSequence getPageTitle(int position) {
 			Locale l = Locale.getDefault();
 			switch (position) {
 			case 0:
 				return "Artist Info";
 				// return getString(R.string.title_section1).toUpperCase(l);
 			case 1:
 				return "Top Songs";
 			}
 			return null;
 		}
 	}
 

 	public static class ArtistInfoFragment extends Fragment {

 		public static final String ARG_SECTION_NUMBER = "section_number";
 
 		public ArtistInfoFragment() {
 		}
 
 		@Override
 		public View onCreateView(LayoutInflater inflater, ViewGroup container,
 				Bundle savedInstanceState) {
 			View rootView = inflater.inflate(
 					R.layout.fragment_artist_tab_dummy, container, false);
 			artistInfoTextView = (TextView) rootView
 					.findViewById(R.id.section_label);
 			artistInfoTextView.setText(artistInfoText);
 			return rootView;
 		}
 	}
 
 	public static class TopSongsFragment extends Fragment {
 
 		@Override
 		public View onCreateView(LayoutInflater inflater, ViewGroup container,
 				Bundle savedInstanceState) {
 			View rootView = inflater.inflate(R.layout.fragment_top_songs,
 					container, false);
 			// TextView dummyTextView = (TextView) rootView
 			// .findViewById(R.id.section_label);
 			// dummyTextView.setText("SOME OTHER TEXT");
 			topSongsTableLayout = (TableLayout) rootView
 					.findViewById(R.id.topSongsTableLayout);
 			Log.d("TAG", (topSongsTableLayout == null) + "");
 
 			return rootView;
 		}
 	}
 
 	public class GetSongsTask extends AsyncTask<String, Void, String> {
 
 		@Override
 		protected String doInBackground(String... urls) {
 			try {
 				HttpClient client = new DefaultHttpClient();
 				HttpGet request = new HttpGet(urls[0]);
 				HttpResponse response = client.execute(request);
 				InputStream in = response.getEntity().getContent();
 				BufferedReader reader = new BufferedReader(
 						new InputStreamReader(in));
 				StringBuilder sb = new StringBuilder();
 				String line = null;
 				while ((line = reader.readLine()) != null) {
 					sb.append(line + "\n");
 				}
 				String httpResponseVal = sb.toString();
 				Document doc = Jsoup.parse(httpResponseVal);
 				Elements elements = doc.select("a[class=tlink]");
 				// Elements songs = new Elements();
 				for (Element e : elements) {
 					if (e.attr("href").contains("/lyrics/")) {
 						resultRows.add(new SearchResultsRow(e.text(), "", e
 								.attr("href"), ""));
 					}
 					// songs.add(e);
 				}
 
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 			return null;
 		}
 
 		@Override
 		protected void onPostExecute(String result) {
 			songsFetched = true;
 			if (songsFetched && artistFetched)
 				updateUI();
 		}
 
 	}
 
 	private class GetArtistInfo extends AsyncTask<String, Void, String> {
 		@Override
 		protected String doInBackground(String... urls) {
 			try {
 				HttpClient client = new DefaultHttpClient();
 				HttpGet request = new HttpGet(urls[0]);
 				HttpResponse response = client.execute(request);
 				InputStream in = response.getEntity().getContent();
 				BufferedReader reader = new BufferedReader(
 						new InputStreamReader(in));
 				StringBuilder sb = new StringBuilder();
 				String line = null;
 				while ((line = reader.readLine()) != null) {
 					sb.append(line + "\n");
 				}
 				String httpResponseVal = sb.toString();
 				Document doc = Jsoup.parse(httpResponseVal);
 				Elements elements = doc.select("#mw-content-text p");
 				Element firstParagraph = elements.get(0);
 				return firstParagraph.text();
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 
 			return "";
 		}
 
 		@Override
 		protected void onPostExecute(String result) {
 			artistFetched = true;
 			artistInfoText = result;
 			Log.d("TEXT",artistInfoText);
 			if (songsFetched && artistFetched)
 				updateUI();
 		}
 	}
 
 	private class SongTextViewListener implements OnClickListener {
 		String url;
 
 		public SongTextViewListener(String url) {
 			this.url = url;
 		}
 
 		@Override
 		public void onClick(View v) {
 			TextView tv = (TextView) v;
 			Intent i = new Intent(getApplicationContext(),
 					SongLyricsActivity.class);
 			i.putExtra(SongLyricsActivity.TITLE_KEY, tv.getText().toString());
 			i.putExtra(SongLyricsActivity.URL_KEY, url);
 			startActivity(i);
 
 		}
 
 	}
 
 	private void updateUI() {
 		for (SearchResultsRow s : resultRows) {
 			TableRow row = new TableRow(getApplicationContext());
 			TextView title = new TextView(getApplicationContext());
 			title.setText(s.getTitle());
 			title.setOnClickListener(new SongTextViewListener(
 					"http://www.seeklyrics.com" + s.getTitleLink()));
 			title.setTextColor(getResources().getColor(R.color.link_color));
 			row.addView(title);
 			topSongsTableLayout.addView(row);
 		}
 		artistInfoTextView.setText(artistInfoText);
 	}
 
 }
