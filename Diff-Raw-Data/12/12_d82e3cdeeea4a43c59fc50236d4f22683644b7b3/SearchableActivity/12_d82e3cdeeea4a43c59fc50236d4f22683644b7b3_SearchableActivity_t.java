 package fr.eyal.datalib.sample.netflix;
 
 import android.app.SearchManager;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
 import android.view.Menu;
 import android.widget.FrameLayout;
 import android.widget.SearchView;
 import fr.eyal.datalib.sample.netflix.fragment.SearchFragment;
 
 public class SearchableActivity extends FragmentActivity {
 
 	@SuppressWarnings("unused")
 	private static final String TAG = "SearchableActivity";
 
 	SearchFragment mFragment;
 	FrameLayout mLayout;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 	    super.onCreate(savedInstanceState);
 	    setContentView(R.layout.activity_search);
 
 	    mLayout = (FrameLayout) findViewById(R.id.search_holder);
 	    
 	    onNewIntent(getIntent());
 	    
 	}
 
 	public String handleIntent(Intent intent) {
 		// Get the intent, verify the action and get the query
 	    String query = "";
 	    if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
 	    	query = intent.getStringExtra(SearchManager.QUERY);
 	    }
 		return query;
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_netflix, menu);
 
 	    // Get the SearchView and set the searchable configuration
 	    SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
 	    SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
 	    searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
 	    searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default
 
 		return true;
 	}
 	
 	@Override
 	protected void onNewIntent(Intent intent) {
 
 		String query = handleIntent(intent);
 		//TODO implement no query
 		
 		//we avoid the double queries
 		if(mFragment != null && mFragment.getQuery().compareTo(query) == 0)
 			return;
 		
 		setIntent(intent);
 		
 		SearchFragment newFragment = SearchFragment.newInstance(query);
 	    //we add the Fragment into the layout
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		
		if(mFragment != null)
			ft.remove(mFragment);
		
		ft.add(R.id.search_holder, newFragment).commit();
		
 	    mFragment = newFragment;
 	}
 }
 
 
