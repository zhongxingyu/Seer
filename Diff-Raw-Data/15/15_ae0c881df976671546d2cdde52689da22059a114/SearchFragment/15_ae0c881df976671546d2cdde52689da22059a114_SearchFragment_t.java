 package com.bigpupdev.synodroid.ui;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.SearchManager;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.database.MatrixCursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Message;
 import android.provider.SearchRecentSuggestions;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.WindowManager.BadTokenException;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.RelativeLayout;
 import android.widget.SimpleCursorAdapter;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemSelectedListener;
 
 import com.bigpupdev.synodroid.R;
 import com.bigpupdev.synodroid.Synodroid;
 import com.bigpupdev.synodroid.action.AddTaskAction;
 import com.bigpupdev.synodroid.action.SetSearchEngines;
 import com.bigpupdev.synodroid.data.DSMVersion;
 import com.bigpupdev.synodroid.data.SearchEngine;
 import com.bigpupdev.synodroid.protocol.ResponseHandler;
 import com.bigpupdev.synodroid.server.SynoServer;
 import com.bigpupdev.synodroid.utils.SearchResultsOpenHelper;
 import com.bigpupdev.synodroid.utils.SearchViewBinder;
 import com.bigpupdev.synodroid.utils.SynodroidDSMSearch;
 import com.bigpupdev.synodroid.utils.SynodroidSearchSuggestion;
 
 import de.keyboardsurfer.android.widget.crouton.Crouton;
 
 public class SearchFragment extends SynodroidFragment {
 	private static final String PREFERENCE_GENERAL = "general_cat";
 	private static final String PREFERENCE_SEARCH_SOURCE = "general_cat.search_source";
 	private static final String PREFERENCE_SEARCH_ORDER = "general_cat.search_order";
 
 	private final String[] from = new String[] { "NAME", "SIZE", "ADDED", "LEECHERS", "SEEDERS", "TORRENTURL" };
 	private final int[] to = new int[] { R.id.result_title, R.id.result_size, R.id.result_date, R.id.result_leechers, R.id.result_seeds, R.id.result_url };
 	private TextView emptyText;
 
 	private Spinner SpinnerSource, SpinnerSort;
 	private ArrayAdapter<CharSequence> AdapterSource, AdapterSort;
 
 	private String[] SortOrder = { "Seeders ASC", "Seeders DESC", "Leachers ASC", "Leachers DESC", "Name ASC", "Name DESC", "Size ASC", "Size DESC", "Date ASC",  "Date DESC" };
 	private String lastSearch = "";
 	private ListView resList;
 
 	private TorrentSearchTask curSearchTask;
 	private SearchResultsOpenHelper db_helper;
 	
 	private boolean fromCache = false;
 
 	private static final String getCachedQuery = "SELECT "+SearchResultsOpenHelper.CACHE_ID+","+SearchResultsOpenHelper.CACHE_TITLE+","+SearchResultsOpenHelper.CACHE_TURL+","+SearchResultsOpenHelper.CACHE_DURL+","+SearchResultsOpenHelper.CACHE_SIZE+","+SearchResultsOpenHelper.CACHE_ADDED+","+SearchResultsOpenHelper.CACHE_SEED+","+SearchResultsOpenHelper.CACHE_LEECH+" FROM "+ SearchResultsOpenHelper.TABLE_CACHE + " WHERE " +SearchResultsOpenHelper.CACHE_QUERY+ "=? AND "+SearchResultsOpenHelper.CACHE_PROVIDER+ "=? AND "+SearchResultsOpenHelper.CACHE_ORDER+"=?";
 	private static final String[] COLS = new String[] { "_ID", "NAME", "TORRENTURL", "DETAILSURL", "SIZE", "ADDED", "SEEDERS", "LEECHERS" };
 	
 	
 	public String getLastSearch(){
 		return lastSearch;
 	}
 	
 	public String getSourceString(){
 		return SpinnerSource.getSelectedItem().toString();
 	}
 	
 	public String getSortString(){
 		return SpinnerSort.getSelectedItem().toString();
 	}
 	
 	/**
 	 * Activity creation
 	 */
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		final Activity a = getActivity();
 		try{
 			if (((Synodroid)a.getApplication()).DEBUG) Log.v(Synodroid.DS_TAG,"SearchFragment: Creating search fragment.");
 		}catch (Exception ex){/*DO NOTHING*/}
 
 		if (savedInstanceState != null){
 			lastSearch = savedInstanceState.getString("lastSearch");
 		}
 		else{
 			lastSearch = "";
 		}
 
 		db_helper = new SearchResultsOpenHelper(a);
 
 		RelativeLayout searchContent = (RelativeLayout) inflater.inflate(R.layout.torrent_search, null, false);
 		resList = (ListView) searchContent.findViewById(R.id.resList);
 
 		emptyText = (TextView) searchContent.findViewById(R.id.empty);
 
 		SpinnerSource = (Spinner) searchContent.findViewById(R.id.srcSpinner);
 		SpinnerSort = (Spinner) searchContent.findViewById(R.id.sortSpinner);
 
 		AdapterSource = new ArrayAdapter<CharSequence>(a, android.R.layout.simple_spinner_item);
 		AdapterSource.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		SpinnerSource.setAdapter(AdapterSource);
 
 		AdapterSort = new ArrayAdapter<CharSequence>(a, android.R.layout.simple_spinner_item);
 		AdapterSort.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		SpinnerSort.setAdapter(AdapterSort);
 
 		SharedPreferences preferences = a.getSharedPreferences(PREFERENCE_GENERAL, Activity.MODE_PRIVATE);
 		String pref_src = preferences.getString(PREFERENCE_SEARCH_SOURCE, "");
 		String pref_order = preferences.getString(PREFERENCE_SEARCH_ORDER, "");
 
 		int lastOrder = 0;
 		int lastSource = 0;
 
 		for (int i = 0; i < SortOrder.length; i++) {
 			if (pref_order.equals(SortOrder[i])) {
 				lastOrder = i;
 			}
 			AdapterSort.add(SortOrder[i]);
 		}
 
 		// Gather the supported torrent sites
 		StringBuilder s = new StringBuilder();
 		List<Object[]> sites = getSupportedSites();
 		if (sites != null) {
 			int i = 0;
 			for (Object[] site :sites){
 				s.append((String) site[1]);
 				s.append("\n");
 				if (pref_src.equals((String) site[1])) {
 					lastSource = i;
 				}
 				AdapterSource.add((String) site[1]);
 				i++;
 			}
 			emptyText.setText(getString(R.string.sites) + "\n" + s.toString());
 			resList.setVisibility(ListView.GONE);
 
 			SpinnerSource.setSelection(lastSource);
 			SpinnerSort.setSelection(lastOrder);
 
 			final String default_site = SpinnerSource.getSelectedItem().toString();
 
 			SpinnerSource.setOnItemSelectedListener(new OnItemSelectedListener() {
 				public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
 					String source = ((TextView) arg1).getText().toString();
 					SharedPreferences preferences = a.getSharedPreferences(PREFERENCE_GENERAL, Activity.MODE_PRIVATE);
 					Message msg = new Message();
 					msg.what = MSG_OPERATION_DONE;
 					SearchFragment.this.handleReponse(msg);
 					
 					if (!source.equals(preferences.getString(PREFERENCE_SEARCH_SOURCE, default_site))){
 						preferences.edit().putString(PREFERENCE_SEARCH_SOURCE, source).commit();
 						if (!lastSearch.equals("")) {
 							((BaseActivity) getActivity()).getActivityHelper().stopSearch();
 							try{
 								curSearchTask.cancel(true);
 							}
 							catch (NullPointerException e){
 								//Ignore NPEs
 							}
 							curSearchTask = new TorrentSearchTask();
 							curSearchTask.execute(lastSearch);
 						}
 					}					
 				}
 
 				public void onNothingSelected(AdapterView<?> arg0) {
 				}
 			});
 
 			SpinnerSort.setOnItemSelectedListener(new OnItemSelectedListener() {
 				public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
 					String order = ((TextView) arg1).getText().toString();
 					SharedPreferences preferences = a.getSharedPreferences(PREFERENCE_GENERAL, Activity.MODE_PRIVATE);
 					Message msg = new Message();
 					msg.what = MSG_OPERATION_DONE;
 					SearchFragment.this.handleReponse(msg);
 					
 					if (!order.equals(preferences.getString(PREFERENCE_SEARCH_ORDER, "BySeeders"))){
 						preferences.edit().putString(PREFERENCE_SEARCH_ORDER, order).commit();
 						if (!lastSearch.equals("")) {
 							((BaseActivity) getActivity()).getActivityHelper().stopSearch();
 							try{
 								curSearchTask.cancel(true);
 							}
 							catch (NullPointerException e){
 								//Ignore NPEs
 							}
 							curSearchTask = new TorrentSearchTask();
 							curSearchTask.execute(lastSearch);
 						}
 					}
 				}
 
 				public void onNothingSelected(AdapterView<?> arg0) {
 				}
 			});
 
 			resList.setOnItemClickListener(new OnItemClickListener() {
 				public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
 					final RelativeLayout rl = (RelativeLayout) arg1;
 					TextView itemValue = (TextView) rl.findViewById(R.id.result_title);
 					TextView itemSize = (TextView) rl.findViewById(R.id.result_size);
 					TextView itemSeed = (TextView) rl.findViewById(R.id.result_seeds);
 					TextView itemLeech = (TextView) rl.findViewById(R.id.result_leechers);
 					TextView itemDate = (TextView) rl.findViewById(R.id.result_date);
 
 					LayoutInflater inflater = (LayoutInflater) a.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 					View layout = (View) inflater.inflate(R.layout.search_dialog, null);
 
 					final TextView msgView = (TextView) layout.findViewById(R.id.msg);
 					final TextView tView = (TextView) layout.findViewById(R.id.title);
 					final TextView sView = (TextView) layout.findViewById(R.id.size);
 					final TextView seedView = (TextView) layout.findViewById(R.id.seed);
 					final TextView leechView = (TextView) layout.findViewById(R.id.leech);
 					final TextView dateView = (TextView) layout.findViewById(R.id.date);
 
 					tView.setText(itemValue.getText());
 					sView.setText(itemSize.getText());
 					seedView.setText(itemSeed.getText());
 					leechView.setText(itemLeech.getText());
 					dateView.setText(itemDate.getText());
 					msgView.setText(getString(R.string.dialog_message_confirm_add));
 
 					Dialog d = new AlertDialog.Builder(a)
 					.setTitle(R.string.dialog_title_confirm)
 					.setView(layout)
 					.setNegativeButton(android.R.string.no, null)
 					.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog, int which) {
 							TextView tv = (TextView) rl.findViewById(R.id.result_url);
 
 							Uri uri = Uri.parse(tv.getText().toString());
 							if (uri.toString().startsWith("//")){
 								uri = Uri.parse("http:"+uri.toString());
 							}
 							
 							AddTaskAction addTask = new AddTaskAction(uri, true, true);
 							Synodroid app = (Synodroid) getActivity().getApplication();
 							app.executeAction(SearchFragment.this, addTask, true);
 
 						}
 					}).create();
 					try {
 						d.show();
 					} catch (BadTokenException e) {
 						try{
 							if (((Synodroid)getActivity().getApplication()).DEBUG) Log.e(Synodroid.DS_TAG, "SearchFragment: " + e.getMessage());
 						}
 						catch (Exception ex){/*DO NOTHING*/}
 						// Unable to show dialog probably because intent has been closed. Ignoring...
 					}
 				}
 			});
 
 		} else {
 			SpinnerSort.setVisibility(Spinner.GONE);
 			SpinnerSource.setVisibility(Spinner.GONE);
 			resList.setVisibility(ListView.GONE);
 			emptyText.setText(R.string.provider_missing);
 		}
 
 		return searchContent;
 	}
 
 	private List<Object[]> getSupportedSites() {
 		// Create the URI of the TorrentSitesProvider
 		String uriString = "content://org.transdroid.search.torrentsitesprovider/sites";
 		Uri uri = Uri.parse(uriString);
 		// Then query all torrent sites (no selection nor projection nor sort):
 		Cursor sites =  getActivity().managedQuery(uri, null, null, null, null);
 		Synodroid app = (Synodroid) getActivity().getApplication();
 		List<Object[]> ret = new ArrayList<Object[]>();
 		SynoServer server = app.getServer();
 
 		if (server != null && app.getServer().getDsmVersion().greaterThen(DSMVersion.VERSION3_0)){
 			Object[] values = new Object[4];
 			values[0] = 11223344;
 			values[1] = "DSM Search";
 			values[2] = "DSM Proprietary Search Engine";
 			values[3] = null;
 			ret.add(values);
 		}
 		if (sites != null){
 			if (sites.moveToFirst()) {
 				do {
 					Object[] values = new Object[4];
 					values[0] = sites.getInt(0);
 					values[1] = sites.getString(1);
 					values[2] = sites.getString(2);
 					values[3] = sites.getString(3);
 					ret.add(values);
 				} while (sites.moveToNext());
 			}
 		}
 
 		if (ret.size() == 0){
 			return null;
 		}
 		return ret;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.app.Activity#onResume()
 	 */
 	@Override
 	public void onResume() {
 		super.onResume();
 
 		Activity a = this.getActivity();
 		Intent intent = a.getIntent();
 		String action = intent.getAction();
 
 		if (Intent.ACTION_SEARCH.equals(action)) {
 			((BaseActivity) getActivity()).getActivityHelper().stopSearch();
 			
 			try{
 				if (((Synodroid)a.getApplication()).DEBUG) Log.v(Synodroid.DS_TAG,"SearchFragment: New search intent received.");
 			}
 			catch (Exception ex){/*DO NOTHING*/}
 
 			if ((intent.getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) == 0) {
 				if (getSupportedSites() != null) {
 					String searchKeywords = intent.getStringExtra(SearchManager.QUERY);
 					lastSearch = searchKeywords;
 					if (!searchKeywords.equals("")) {
 						try{
 							curSearchTask.cancel(true);
 						}
 						catch (NullPointerException e){
 							//Ignore NPEs
 						}
 						curSearchTask = new TorrentSearchTask();
 						curSearchTask.execute(searchKeywords);
 						SearchRecentSuggestions suggestions = new SearchRecentSuggestions(a, SynodroidSearchSuggestion.AUTHORITY, SynodroidSearchSuggestion.MODE);
 						suggestions.saveRecentQuery(searchKeywords, null);
 					} else {
 						emptyText.setText(R.string.no_keyword);
 						emptyText.setVisibility(TextView.VISIBLE);
 						resList.setVisibility(TextView.GONE);
 					}
 				}
 				else {
 					try{
 						if (((Synodroid)a.getApplication()).DEBUG) Log.w(Synodroid.DS_TAG,"SearchFragment: No providers available to handle intent.");
 					}
 					catch (Exception ex){/*DO NOTHING*/}
 				}
 			}
 			else{
 				try{
 					if (((Synodroid)a.getApplication()).DEBUG) Log.i(Synodroid.DS_TAG,"SearchFragment: This was an old intent. Skipping it...");
 				}
 				catch (Exception ex){/*DO NOTHING*/}
 			}
 			//Mark intent as already processed
 			intent.setFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY);
 			a.setIntent(intent);
 
 		}
 		else if (intent.getBooleanExtra("start_search", false)){
 			getActivity().onSearchRequested();
 		}
 	}
 
 	public void refresh(){
 		if (!lastSearch.equals("")) {
 			((BaseActivity) getActivity()).getActivityHelper().stopSearch();
 			try{
 				curSearchTask.cancel(true);
 			}
 			catch (NullPointerException e){
 				//Ignore NPEs
 			}
 			curSearchTask = new TorrentSearchTask();
 			curSearchTask.execute(lastSearch);
 		}
 	}
 
 	private class TorrentSearchTask extends AsyncTask<String, Void, Cursor> {
		private static final String SEARCH_ORDER = "Combined";
 		
 		class SearchResult{
 			public int id;
 			public String name;
 			public String torrentUrl;
 			public String detailUrl;
 			public String size;
 			public String dateAdded;
 			public int seeders;
 			public int leachers;
 			
 			public SearchResult(int _id, String _name, String _torrentUrl, String _detailUrl, String _size, String _dateAdded, int _seeders, int _leachers){
 				id = _id;
 				name = _name;
 				torrentUrl = _torrentUrl;
 				detailUrl = _detailUrl;
 				size = _size;
 				dateAdded = _dateAdded;
 				seeders = _seeders;
 				leachers = _leachers;
 			}
 		}
 		
 		public class SearchResultNameComparator implements Comparator<SearchResult>
 		{
 		    public int compare(SearchResult left, SearchResult right) {
 		        return left.name.compareTo(right.name);
 		    }
 		}
 		
 		public class SearchResultSizeComparator implements Comparator<SearchResult>
 		{
 		    public int compare(SearchResult left, SearchResult right) {
 		        return left.size.compareTo(right.size);
 		    }
 		}
 		
 		public class SearchResultDateComparator implements Comparator<SearchResult>
 		{
 		    public int compare(SearchResult left, SearchResult right) {
 		        return left.dateAdded.compareTo(right.dateAdded);
 		    }
 		}
 		
 		public class SearchResultSeedersComparator implements Comparator<SearchResult>
 		{
 		    public int compare(SearchResult left, SearchResult right) {
 		        return left.seeders - right.seeders;
 		    }
 		}
 		
 		public class SearchResultLeachersComparator implements Comparator<SearchResult>
 		{
 		    public int compare(SearchResult left, SearchResult right) {
 		        return left.leachers - right.leachers;
 		    }
 		}
 		
 		public class SearchResultNameDESCComparator implements Comparator<SearchResult>
 		{
 		    public int compare(SearchResult left, SearchResult right) {
 		        return right.name.compareTo(left.name);
 		    }
 		}
 		
 		public class SearchResultSizeDESCComparator implements Comparator<SearchResult>
 		{
 		    public int compare(SearchResult left, SearchResult right) {
 		        return right.size.compareTo(left.size);
 		    }
 		}
 		
 		public class SearchResultDateDESCComparator implements Comparator<SearchResult>
 		{
 		    public int compare(SearchResult left, SearchResult right) {
 		        return right.dateAdded.compareTo(left.dateAdded);
 		    }
 		}
 		
 		public class SearchResultSeedersDESCComparator implements Comparator<SearchResult>
 		{
 		    public int compare(SearchResult left, SearchResult right) {
 		        return right.seeders - left.seeders;
 		    }
 		}
 		
 		public class SearchResultLeachersDESCComparator implements Comparator<SearchResult>
 		{
 		    public int compare(SearchResult left, SearchResult right) {
 		        return right.leachers - left.leachers;
 		    }
 		}
 		
 		@Override
 		protected void onPreExecute() {
 			emptyText.setVisibility(TextView.VISIBLE);
 			emptyText.setText(getString(R.string.searching));
 			resList.setVisibility(ListView.GONE);
 			resList.setAdapter(null);
 
 			((SearchActivity) SearchFragment.this.getActivity()).updateActionBarTitle(lastSearch);
 
 			Message msg = new Message();
 			msg.what = MSG_OPERATION_PENDING;
 			SearchFragment.this.handleReponse(msg);
 		}
 
 		@Override
 		protected Cursor doInBackground(String... params) {
 			SQLiteDatabase cache = db_helper.getWritableDatabase();
 			
 			try {
 				SharedPreferences preferences = getActivity().getSharedPreferences(PREFERENCE_GENERAL, Activity.MODE_PRIVATE);
 				String search_src = preferences.getString(PREFERENCE_SEARCH_SOURCE, SpinnerSource.getSelectedItem().toString());
 				
				Cursor res = cache.rawQuery(getCachedQuery, new String[]{params[0], search_src, SEARCH_ORDER});
 				
 				if (res.getCount() == 0){
 					fromCache = false;
 					if (search_src.equals("DSM Search")){
 						Synodroid app = (Synodroid) getActivity().getApplication();
 
						return getActivity().managedQuery(Uri.parse(SynodroidDSMSearch.CONTENT_URI+params[0]), null, null, new String[] { app.getServer().getDsmVersion().getTitle(), app.getServer().getCookies(), app.getServer().getUrl(), String.valueOf(app.DEBUG), "0", "50"}, SEARCH_ORDER);
 
 					}
 					else{
 						// Create the URI of the TorrentProvider
 						String uriString = "content://org.transdroid.search.torrentsearchprovider/search/" + params[0];
 						Uri uri = Uri.parse(uriString);
 						// Then query for this specific record (no selection nor projection nor sort):
 
						return getActivity().managedQuery(uri, null, "SITE = ?", new String[] { search_src }, SEARCH_ORDER);
 					}
 				}
 				else{
 					fromCache = true;
 					MatrixCursor cursor = new MatrixCursor(COLS);
 					res.moveToFirst();
 					do {
 						Object[] values = new Object[8];
                         values[0] = res.getInt(0);
                         values[1] = res.getString(1);
                         values[2] = res.getString(2);
                         values[3] = res.getString(3);
                         values[4] = res.getString(4);
                         values[5] = res.getString(5);
                         values[6] = res.getInt(6);
                         values[7] = res.getInt(7);
                         cursor.addRow(values);
 					} while(res.moveToNext());
 					
 					return cursor;
 				}
 			} catch (Exception e) {
 				return null;
 			}
 			finally{
 				try{
 					cache.close();
 				}catch (Exception e){}
 			}
 		}
 
 		@Override
 		protected void onPostExecute(Cursor cur) {
 			try{
 				if (cur == null) {
 					emptyText.setVisibility(TextView.VISIBLE);
 					resList.setVisibility(ListView.GONE);
 					emptyText.setText(getString(R.string.no_results));
 				} else {// Show results in the list
 					if (cur.getCount() == 0) {
 						emptyText.setVisibility(TextView.VISIBLE);
 						resList.setVisibility(ListView.GONE);
 						emptyText.setText(getString(R.string.no_results));
 					} else {
 						emptyText.setVisibility(TextView.GONE);
 						resList.setVisibility(ListView.VISIBLE);
 						SharedPreferences preferences = getActivity().getSharedPreferences(PREFERENCE_GENERAL, Activity.MODE_PRIVATE);
 						String pref_order = preferences.getString(PREFERENCE_SEARCH_ORDER, SpinnerSort.getSelectedItem().toString());
 						
 						if (!fromCache){
 							String pref_src = preferences.getString(PREFERENCE_SEARCH_SOURCE, SpinnerSource.getSelectedItem().toString());
 							SQLiteDatabase cache = db_helper.getWritableDatabase();
 							
 							try{
 								cur.moveToFirst();
 								do {
 									ContentValues values = new ContentValues();
 									values.put(SearchResultsOpenHelper.CACHE_QUERY, lastSearch);
 									values.put(SearchResultsOpenHelper.CACHE_PROVIDER, pref_src);
									values.put(SearchResultsOpenHelper.CACHE_ORDER, SEARCH_ORDER);
 									values.put(SearchResultsOpenHelper.CACHE_ID, String.valueOf(cur.getInt(0)));
 									values.put(SearchResultsOpenHelper.CACHE_TITLE, cur.getString(1));
 									values.put(SearchResultsOpenHelper.CACHE_TURL, cur.getString(2));
 									values.put(SearchResultsOpenHelper.CACHE_DURL, cur.getString(3));
 									values.put(SearchResultsOpenHelper.CACHE_SIZE, cur.getString(4));
 									values.put(SearchResultsOpenHelper.CACHE_ADDED, cur.getString(5));
 									values.put(SearchResultsOpenHelper.CACHE_SEED, String.valueOf(cur.getInt(6)));
 									values.put(SearchResultsOpenHelper.CACHE_LEECH, String.valueOf(cur.getInt(7)));
 									cache.insert(SearchResultsOpenHelper.TABLE_CACHE,null, values);
 								} while(cur.moveToNext());
 							}
 							finally{
 								try{
 									cache.close();
 								}catch (Exception e){}
 							}
 							
 						}
 						
 						List<SearchResult> toSort = new ArrayList<SearchResult>();
 						cur.moveToFirst();
 						do {
 							toSort.add(new SearchResult(cur.getInt(0), cur.getString(1), cur.getString(2), cur.getString(3), cur.getString(4), cur.getString(5), cur.getInt(6), cur.getInt(7)));
 						} while(cur.moveToNext());
 						
 						if (pref_order.equals("Seeders ASC")){
 							Collections.sort(toSort, new SearchResultSeedersComparator());
 						}
 						else if (pref_order.equals("Leachers ASC")){
 							Collections.sort(toSort, new SearchResultLeachersComparator());
 						}
 						else if (pref_order.equals("Name ASC")){
 							Collections.sort(toSort, new SearchResultNameComparator());
 						}
 						else if (pref_order.equals("Size ASC")){
 							Collections.sort(toSort, new SearchResultSizeComparator());
 						}
 						else if (pref_order.equals("Date ASC")){
 							Collections.sort(toSort, new SearchResultDateComparator());
 						}
 						else if (pref_order.equals("Seeders DESC")){
 							Collections.sort(toSort, new SearchResultSeedersDESCComparator());
 						}
 						else if (pref_order.equals("Leachers DESC")){
 							Collections.sort(toSort, new SearchResultLeachersDESCComparator());
 						}
 						else if (pref_order.equals("Name DESC")){
 							Collections.sort(toSort, new SearchResultNameDESCComparator());
 						}
 						else if (pref_order.equals("Size DESC")){
 							Collections.sort(toSort, new SearchResultSizeDESCComparator());
 						}
 						else if (pref_order.equals("Date DESC")){
 							Collections.sort(toSort, new SearchResultDateDESCComparator());
 						}
 						
 						MatrixCursor sorted = new MatrixCursor(COLS);
 						for (SearchResult sr: toSort){
 							Object[] values = new Object[8];
 	                        values[0] = sr.id;
 	                        values[1] = sr.name;
 	                        values[2] = sr.torrentUrl;
 	                        values[3] = sr.detailUrl;
 	                        values[4] = sr.size;
 	                        values[5] = sr.dateAdded;
 	                        values[6] = sr.seeders;
 	                        values[7] = sr.leachers;
 	                        sorted.addRow(values);
 						}
 						
 						SimpleCursorAdapter cursor = new SimpleCursorAdapter(getActivity(), R.layout.search_row, sorted, from, to);
 						cursor.setViewBinder(new SearchViewBinder());
 						resList.setAdapter(cursor);
 					}
 				}
 			}
 			catch (Exception e){
 				try{
 					if (((Synodroid)getActivity().getApplication()).DEBUG) Log.e(Synodroid.DS_TAG, "SearchFragment: Activity was killed before the searchresult came back...");
 				}
 				catch (Exception ex){/*DO NOTHING*/}
 			}
 
 			Message msg = new Message();
 			msg.what = MSG_OPERATION_DONE;
 			SearchFragment.this.handleReponse(msg);
 		}
 
 	}
 
 	@Override
 	public void onSaveInstanceState(Bundle savedInstanceState) {
 		// Save UI state changes to the savedInstanceState.
 		// This bundle will be passed to onCreate if the process is
 		// killed and restarted.
 		savedInstanceState.putString("lastSearch", lastSearch);
 
 		// etc.
 		super.onSaveInstanceState(savedInstanceState);
 	}
 
 	public void handleMessage(Message msg) {
 		// Update tasks
 		if (msg.what == ResponseHandler.MSG_TASK_DL_WAIT){
 			Crouton.makeText(getActivity(), getString(R.string.wait_for_download), Synodroid.CROUTON_INFO).show();
 		}
 		else if (msg.what == ResponseHandler.MSG_SE_LIST_RETRIEVED) {
 			final Activity a = getActivity();
 			try{
 				if (((Synodroid)a.getApplication()).DEBUG) Log.v(Synodroid.DS_TAG,"DownloadFragment: Received search engine listing message.");
 			}catch (Exception ex){/*DO NOTHING*/}
 
 			@SuppressWarnings("unchecked")
 			List<SearchEngine> seList = (List<SearchEngine>) msg.obj;
 			final CharSequence[] seNames = new CharSequence[seList.size()];
 			final boolean[] seSelection = new boolean[seList.size()];
 			for (int iLoop = 0; iLoop < seList.size(); iLoop++) {
 				SearchEngine se = seList.get(iLoop);
 				seNames[iLoop] = se.name;
 				seSelection[iLoop] = se.enabled;
 			}
 			AlertDialog.Builder builder = new AlertDialog.Builder(a);
 			builder.setTitle(getString(R.string.search_engine_title));
 			builder.setMultiChoiceItems(seNames, seSelection, new DialogInterface.OnMultiChoiceClickListener() {
 				public void onClick(DialogInterface dialog, int which, boolean isChecked) {
 					seSelection[which] = isChecked;
 				}
 			});
 			builder.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int which) {
 					List<SearchEngine> newList = new ArrayList<SearchEngine>();
 					for (int iLoop = 0; iLoop < seNames.length; iLoop++) {
 						SearchEngine se = new SearchEngine();
 						se.name = (String) seNames[iLoop];
 						se.enabled = seSelection[iLoop];
 						newList.add(se);
 					}
 					dialog.dismiss();final Activity a = getActivity();
 
 					Synodroid app = (Synodroid) a.getApplication();
 					app.executeAsynchronousAction(SearchFragment.this, new SetSearchEngines(newList), true);
 				}
 			});
 			builder.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int which) {
 					dialog.dismiss();
 				}
 			});
 
 			AlertDialog alert = builder.create();
 			try {
 				alert.show();
 			} catch (BadTokenException e) {
 				// Unable to show dialog probably because intent has been closed. Ignoring...
 			}
 		}
 	}
 }
