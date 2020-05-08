 package net.eledge.android.eu.europeana.gui.activity;
 
 import java.util.ArrayList;
 
 import net.eledge.android.eu.europeana.R;
 import net.eledge.android.eu.europeana.gui.adaptor.SuggestionAdaptor;
 import net.eledge.android.eu.europeana.gui.dialog.AboutDialog;
 import net.eledge.android.eu.europeana.search.SearchController;
 import net.eledge.android.eu.europeana.search.listeners.SuggestionTaskListener;
 import net.eledge.android.eu.europeana.search.model.Suggestion;
 import android.app.Activity;
 import android.app.Dialog;
 import android.app.SearchManager;
 import android.content.Intent;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.os.Bundle;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.inputmethod.EditorInfo;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.EditText;
 import android.widget.GridView;
 import android.widget.TextView;
 
 public class HomeActivity extends Activity implements SuggestionTaskListener, OnItemClickListener {
 	
 	private SuggestionAdaptor mSuggestionsAdaptor;
 	
 	private EditText mEditTextQuery;
 	private GridView mGridViewSuggestions;
 	
 	private SearchController searchController = SearchController.instance;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_home);
 		
 		searchController.suggestionPagesize = getResources().getInteger(R.integer.home_suggestions_pagesize);
 		
 		mSuggestionsAdaptor = new SuggestionAdaptor(this, new ArrayList<Suggestion>());
 		
 		mGridViewSuggestions = (GridView) findViewById(R.id.activity_home_gridview_suggestions);
 		mGridViewSuggestions.setAdapter(mSuggestionsAdaptor);
 		mGridViewSuggestions.setOnItemClickListener(this);
 		
 		mEditTextQuery = (EditText) findViewById(R.id.activity_home_edittext_query);
 		
 		mEditTextQuery.setOnEditorActionListener(new TextView.OnEditorActionListener() {
 		    @Override
 		    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
 		        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
 		            performSearch(v.getText().toString());
 		            return true;
 		        }
 		        return false;
 		    }
 		});
 		
 		mEditTextQuery.addTextChangedListener(new TextWatcher() {
 			
 			@Override
 			public void onTextChanged(CharSequence s, int start, int before, int count) {
 				if ( s.length() > 2 ) {
 					mGridViewSuggestions.setVisibility(View.GONE);
 					searchController.suggestions(s.toString(), HomeActivity.this);
 				}
 			}
 			
 			@Override
 			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
 				// ignore
 			}
 			
 			@Override
 			public void afterTextChanged(Editable s) {
 				// ignore
 			}
 		});
 		
 	}
 	
 	private void performSearch(String query) {
 		final Intent intent = new Intent(this, SearchActivity.class);
 		intent.setAction(Intent.ACTION_SEARCH);
 		intent.putExtra(SearchManager.QUERY, query);
 		this.startActivity(intent);
 	}
 	
 	@Override
 	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 		Suggestion suggestion = mSuggestionsAdaptor.getItem(position);
 		performSearch(suggestion.query);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.home, menu);
 
 	    // Get the SearchView and set the searchable configuration
 //	    SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
 //	    SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
 	    // Assumes current activity is the searchable activity
 //	    searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
 //	    searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default
 	    
 	    return true;
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.action_about:
 			try {
 				Dialog dialog = new AboutDialog(this, getPackageManager().getPackageInfo(getPackageName(), 0));
 				dialog.show();
 			} catch (NameNotFoundException e) {}
 			break;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 		return true;
 	}
 	
 	@Override
 	public void onSuggestionFinish(Suggestion[] suggestions) {
 		mSuggestionsAdaptor.clear();
		mSuggestionsAdaptor.addAll(suggestions);
		mSuggestionsAdaptor.notifyDataSetChanged();
		mGridViewSuggestions.setVisibility(View.VISIBLE);
 	}
 
 }
