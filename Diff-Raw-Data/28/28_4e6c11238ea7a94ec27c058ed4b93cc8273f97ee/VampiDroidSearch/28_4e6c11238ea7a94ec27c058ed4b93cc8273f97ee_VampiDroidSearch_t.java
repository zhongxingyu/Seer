 package name.vampidroid;
 
 import android.app.SearchManager;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.support.v4.view.Menu;
 import android.support.v4.view.MenuItem;
 import android.util.Log;
 
 public class VampiDroidSearch extends VampiDroidBase {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 		
 		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
 
 	}
 
 	@Override
 	protected String getLibraryQuery() {
 
 		SharedPreferences prefs = PreferenceManager
 				.getDefaultSharedPreferences(this.getApplicationContext());
 
 		String query = formatQueryString(getIntent().getStringExtra(
				SearchManager.QUERY)).toLowerCase();
 
 		VampidroidSuggestionProvider.getBridge(this.getApplicationContext()).saveRecentQuery(query,
 				null);
 
 		String queryLibraryDatabase;
 
		
		queryLibraryDatabase = "select _id, Name, Type, Clan, Discipline from library where lower(Name) like '%"
			+ query + "%'";
		
 		if (prefs.getBoolean("searchCardText", false))
			queryLibraryDatabase += " or lower(CardText) like '%" + query + "%')";
 
 		// TODO Auto-generated method stub
 		return queryLibraryDatabase;
 
 	}
 
 	@Override
 	protected String getCryptQuery() {
 
 		SharedPreferences prefs = PreferenceManager
 				.getDefaultSharedPreferences(this.getApplicationContext());
 
 		String query = formatQueryString(getIntent().getStringExtra(
 				SearchManager.QUERY));
 
 		VampidroidSuggestionProvider.getBridge(this.getApplicationContext()).saveRecentQuery(query,
 				null);
 
 		String queryCryptDatabase;
 
		queryCryptDatabase = "select _id, case when length(Adv) > 0 then 'Adv.' || ' ' || Name else Name end as Name, Disciplines, Capacity, substr(CardText, 1, 40) as InitialCardText from crypt where lower(Name) like '%"
			+ query + "%'";
		
		
 		if (prefs.getBoolean("searchCardText", false))
			queryCryptDatabase += " or lower(CardText) like '%" + query + "%')";
		
 		// TODO Auto-generated method stub
 		return queryCryptDatabase;
 	}
 
 	@Override
 	protected void onNewIntent(Intent intent) {
 		// TODO Auto-generated method stub
 
 		Log.i("vampidroid", "VampiDroidSearch.onNewintent");
 		super.onNewIntent(intent);
 
 		// userSearchTerm = intent.getStringExtra(SearchManager.QUERY);
 		setIntent(intent);
 		
 		
 		updateQueries();
 		
 	
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.search_menu, menu);
 		
 		return super.onCreateOptionsMenu(menu);
         
 	}
 
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		
 		case R.id.menu_search:
 			onSearchRequested(); 
 			return true;
 		
 
 		case android.R.id.home:
 			// app icon in Action Bar clicked; go home
 			Intent intent = new Intent(this, VampiDroid.class);
 			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 			startActivity(intent);
 			return true;
 
 		}
 
 		return (super.onOptionsItemSelected(item));
 	}
 
 }
