 package com.anuragbhandari.unjumble;
 
 import java.util.Arrays;
 
 import com.anuragbhandari.unjumble.contentprovider.UnjumbleContentProvider;
 import com.anuragbhandari.unjumble.database.WordsTable;
 
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.app.ListActivity;
 import android.app.SearchManager;
 import android.app.LoaderManager;
 import android.content.Context;
 import android.content.CursorLoader;
 import android.content.Intent;
 import android.content.Loader;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.support.v4.widget.SimpleCursorAdapter;
 import android.view.Gravity;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.FrameLayout.LayoutParams;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 import android.widget.SearchView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 
 import net.jeremybrooks.knicker.Knicker;
 import net.jeremybrooks.knicker.KnickerException;
 import net.jeremybrooks.knicker.WordApi;
 import net.jeremybrooks.knicker.dto.Definition;
 
 public class MainActivity extends ListActivity implements LoaderManager.LoaderCallbacks<Cursor> {
 	{
 		// Set the Wordnik API key
 		// A unique key is required to use their API
 		// Get yours from http://developer.wordnik.com
		System.setProperty("WORDNIK_API_KEY", "your-key-here");
 	}
 	
 	
 	/******* T H E   C O M M O N   V A R I A B L E S *******/
 	private SimpleCursorAdapter adapter;
 	private ProgressBar progressBar;
 	private ViewGroup root;
 	private Menu mainMenu;
 	private TextView queryWordTextView;
 	private boolean isFetchMeanings;
 	private Cursor sqlData;
 	private HashMap<String, Definition> wordMeanings;
 	
 	
 	/******* T H E   A C T I V I T Y   M E T H O D S *******/
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         queryWordTextView = (TextView) this.findViewById(R.id.query);
     }
     
     @Override
     protected void onNewIntent(Intent intent) {
     	setIntent(intent); // optional
     	// Get the intent, verify the action and get the query
     	if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
   	      String query = intent.getStringExtra(SearchManager.QUERY);
   	      queryWordTextView.setText("\'" + query + "\' " + getString(R.string.text_could_mean));
   	      DoUnjumbling(query);
   	    }
     }
 
 	@Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_main, menu);
         mainMenu = menu; // for later use elsewhere
         
         // Get the SearchView and set the searchable configuration
         SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
         SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
         searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
 
         return true;
     }
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 	    switch (item.getItemId()) {
 	    	case R.id.menu_settings:
 	    		Intent intent = new Intent(this, SettingsActivity.class);
 			    startActivity(intent);
 			    break;
 	    	case R.id.menu_clear:
 	    		clearResults();
 			    break;
 		    case R.id.menu_about:
 		    	AboutDialog about = new AboutDialog(this);
 		    	about.setTitle(getString(R.string.title_activity_about));
 		    	about.show();
 		    	break;
 		    case R.id.menu_exit:
 		    	queryWordTextView.setText("");
 		    	setListAdapter(null);
 		    	this.moveTaskToBack(true);
 		    	break;
 	    }
 	    return super.onOptionsItemSelected(item);
 	}
 	
 	@SuppressWarnings("deprecation")
 	@Override
 	protected void onListItemClick(ListView l, View v, int position, long id) {
 	    super.onListItemClick(l, v, position, id);
 	    // Copy the word suggestion to clipboard
 	    String selectedWord = "";
 	    Cursor cc = (Cursor) (getListView().getItemAtPosition(position));
 	    if (cc != null) {
 	    	selectedWord = cc.getString(cc.getColumnIndex(WordsTable.COLUMN_WORD));
 	    }
 	    if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
 	        android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
 	        clipboard.setText(selectedWord);
 	    }
 	    else {
 	        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
 	        android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Text", selectedWord);
             clipboard.setPrimaryClip(clip);
 	    }
 	    Toast.makeText(this, getString(R.string.text_copy_success), Toast.LENGTH_SHORT).show();
 	}
 	
 	
 	/******* T H E   C O M M O N   M E T H O D S *******/
 	private void DoUnjumbling(String query) {
     	// Fields from the database (projection). Must include the _id column for the adapter to work
         String[] from = new String[] { WordsTable.COLUMN_WORD, WordsTable.COLUMN_WORDMEANING, WordsTable.COLUMN_ID };
         int[] to = new int[] { R.id.item_word, R.id.item_description, R.id.item_id };
         Bundle bundle = new Bundle();
         bundle.putString("query", query);
         
         // Start the Loader that'll fetch cursor into adapter
         if(getLoaderManager().getLoader(0) == null) {
         	getLoaderManager().initLoader(0, bundle, this);
         }
         else {
         	getLoaderManager().restartLoader(0, bundle, this);
         }
         adapter = new SimpleCursorAdapter(this, R.layout.activity_main_list_item, null, from, to, 0);
         
         // Bind fetched meanings of suggested words to their corresponding TextView 
         SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
         isFetchMeanings = sharedPref.getBoolean(SettingsActivity.KEY_PREF_FETCH_MEANINGS, true);
         if(isFetchMeanings && isNetworkAvailable()) {// && AccountApi.apiTokenStatus().isValid()) {
 	        adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
 	            @Override
 	            public boolean setViewValue(View view, Cursor cursor, int column) {
 	                if(column == 1) {
 	                    TextView tv = (TextView) view;
 	                    String word = cursor.getString(cursor.getColumnIndex(WordsTable.COLUMN_WORD));
 	                    String wordMeaning = (wordMeanings == null ? getString(R.string.text_meaning_not_found) : wordMeanings.get(word).getText());
 	                    tv.setText(wordMeaning);
 	                    return true;
 	                }
 	                return false;
 	            }
 	        });
         }
         
         setListAdapter(adapter);
 	}
 	
 	private boolean isNetworkAvailable() {
 	    ConnectivityManager connectivityManager 
 	          = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
 	    return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
 	}
 	
 	private void clearResults() {
 		queryWordTextView.setText("");
     	//setListAdapter(null);
 		getLoaderManager().destroyLoader(0);
 		((TextView)findViewById(android.R.id.empty)).setText(getString(R.string.text_empty));
 	}
 	
 	private void showProgressBar() {
 		if(progressBar == null) {
 	        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleLarge);
 	        progressBar.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER));
 	        progressBar.setIndeterminate(true);
 	        root = (ViewGroup) findViewById(android.R.id.content);
 		}
 		getListView().setEmptyView(progressBar);
         root.addView(progressBar);
 	}
 	
 	private void hideProgressBar() {
 		if(progressBar != null) {
 	    	root.removeView(progressBar);
 	    	getListView().setEmptyView(findViewById(android.R.id.empty));
 	    }
 	}
 	
 	
 	/******* T H E   L O A D E R   M E T H O D S *******/
 	// Creates a new loader after the initLoader() call
 	@Override
 	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
 		// Clear the emptyview text
 		((TextView)findViewById(android.R.id.empty)).setText("");
 		
 		// Show a progress bar to display while the list loads
 		showProgressBar();
         
 		// Disable the Clear button
 		mainMenu.findItem(R.id.menu_clear).setEnabled(false);
 		
 		// Create the CursorLoader
 	    String[] projection = { WordsTable.COLUMN_WORD, WordsTable.COLUMN_WORDMEANING, WordsTable.COLUMN_ID };
 	    String selection = WordsTable.COLUMN_WORDHASH;
 	    String query = args.getString("query").toLowerCase();
 	    char[] queryTemp = query.toCharArray();
 	    Arrays.sort(queryTemp);
 	    query = new String(queryTemp);
 	    CursorLoader cursorLoader = new CursorLoader(this,
 	        UnjumbleContentProvider.CONTENT_URI, projection, 
 	        selection + " = ?", new String[] { query } , null);
 	    return cursorLoader;
 	}
 
 	@Override
 	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
 		int cursorSize = data.getCount();
 
 		if(cursorSize < 1) {
 			// Remove the progressbar after data fetching has finished
 		    hideProgressBar();
 		    // Show the "nothing found" message
 		    ((TextView)findViewById(android.R.id.empty)).setText(getString(R.string.text_nothing_found));
 		}
 		
 		else {
 			// Get meanings of suggested words (if user has opted for it and there *is* an Internet connection)
 			if(isFetchMeanings && isNetworkAvailable()) {// && AccountApi.apiTokenStatus().isValid()) {
 		    	ArrayList<String> suggestedWords = new ArrayList<String>();
 		    	
 		    	data.moveToFirst();
 		    	do {
 		    	    suggestedWords.add(data.getString(data.getColumnIndex(WordsTable.COLUMN_WORD)));
 		    	}while(data.moveToNext());
 		    	
 		    	// Execute our AsyncTask (on a background thread)
 				sqlData = data;
 				new FetchMeaningsTask().execute(suggestedWords.toArray(new String[suggestedWords.size()]));
 			}
 			// Show words without meanings (either user hasn't opted for it or there's no Internet)
 			else {
 				// Fill adapter with fetched data (cursor)
 				adapter.swapCursor(data);
 				// Remove the progressbar after data fetching has finished
 			    hideProgressBar();
 				// Re-enable the Clear button
 				mainMenu.findItem(R.id.menu_clear).setEnabled(true);
 			}
 		}
 	}
 	
 	@Override
 	public void onLoaderReset(Loader<Cursor> loader) {
 	    // data is not available anymore, delete reference
 	    adapter.swapCursor(null);
 	}
 
 	
 	/******* T H E   A S Y N C T A S K   C L A S S *******/
 	// Creating our AsyncTask class as an inner class to give it access to the 
 	// "this" instance of our MainActivity class
 	private class FetchMeaningsTask extends AsyncTask<String, Void, HashMap<String, Definition>> {
 		@Override
 		protected HashMap<String, Definition> doInBackground(String... words) {
 			HashMap<String, Definition> wordMeanings = new HashMap<String, Definition>();
             try {
             	for(String word : words) {
             		@SuppressWarnings("serial")
 					List<Definition> defs = WordApi.definitions(word, new HashSet<Knicker.SourceDictionary>() {
 						{
 					          add(Knicker.SourceDictionary.wiktionary);
 					    }
 					});
             		@SuppressWarnings("serial")
 					Definition noMeaning = new Definition() {
             			{
             				setText(getString(R.string.text_meaning_not_found));
             			}
             		};
             		wordMeanings.put(word, defs == null || defs.size() == 0 ? noMeaning  : defs.get(0));
             	}
 			}
             catch (KnickerException e) {
             	e.printStackTrace();
 			}
             return wordMeanings;
 	    }
 		
 		@Override
 		protected void onPostExecute(HashMap<String, Definition> result) {
 			// Save result of the background operation into MainActivity's wordMeanings field
 			MainActivity.this.wordMeanings = result;
 			// Fill the adapter with fetched data (cursor)
 		    adapter.swapCursor(MainActivity.this.sqlData);
 		    // Remove the progressbar after data fetching has finished
 		    MainActivity.this.hideProgressBar();
 		    // Re-enable the Clear button
 			MainActivity.this.mainMenu.findItem(R.id.menu_clear).setEnabled(true);
 		}
 	}
 }
