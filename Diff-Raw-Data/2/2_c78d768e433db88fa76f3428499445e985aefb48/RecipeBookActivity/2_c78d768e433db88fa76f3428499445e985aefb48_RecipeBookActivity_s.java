 package net.potterpcs.recipebook;
 
 import android.app.SearchManager;
 import android.content.ContentUris;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentTransaction;
 import android.support.v4.view.MenuCompat;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 
 public class RecipeBookActivity extends FragmentActivity {
 	
 	private final String TAG = RecipeBookActivity.class.getSimpleName();
 
 	FragmentManager manager;
 	private Intent lastIntent;
 	private String searchQuery;
 	private String searchTag;
 	private boolean searchMode;
 	private boolean sortDescending;
 	private boolean tagSearchMode;
 	int sortKey;
 
 	static final String SORT_DESCENDING = " desc";
 	static final String SORT_KEY = "sort_key";
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         manager = getSupportFragmentManager();
         
         setContentView(R.layout.main);
         lastIntent = getIntent();
         handleIntent(lastIntent);
     }
     
     @Override
     protected void onNewIntent(Intent intent) {
     	// TODO Auto-generated method stub
 //    	super.onNewIntent(intent);
     	lastIntent = intent;
     	handleIntent(intent);
     }
     
     private void handleIntent(Intent intent) {
     	if (Intent.ACTION_SEARCH.equals(intent.getAction()) || intent.hasExtra(RecipeBook.SEARCH_EXTRA)) {
     		searchMode = true;
     		if (intent.hasExtra(RecipeBook.SEARCH_EXTRA)) {
     			searchQuery = intent.getStringExtra(RecipeBook.SEARCH_EXTRA);
     		} else {
     			searchQuery = intent.getStringExtra(SearchManager.QUERY);
     		}
     		Log.i(TAG, "Started as search with query: " + searchQuery);
     	} else {
     		searchMode = false;
     		searchQuery = null;
     		Log.i(TAG, "Started as app");
     	}
     	
     	Bundle searchData = intent.getBundleExtra(SearchManager.APP_DATA);
     	if (searchData == null) {
 	    	sortDescending = intent.getBooleanExtra(SORT_DESCENDING, false);
 	    	sortKey = intent.getIntExtra(SORT_KEY, 0);
     	} else {
     		sortDescending = searchData.getBoolean(SORT_DESCENDING, false);
     		sortKey = searchData.getInt(SORT_KEY, 0);
     	}
     	
     	if (intent.hasExtra(RecipeBook.TAG_EXTRA)) {
     		tagSearchMode = true;
     		searchTag = intent.getStringExtra(RecipeBook.TAG_EXTRA);
     	} else {
     		tagSearchMode = false;
     		searchTag = null;
     	}
     	
 		Log.i(TAG, "Sort descending == " + sortDescending + ", sort key == " + sortKey);
     	
     	try {
 			invalidateOptionsMenu();
 		} catch (NoSuchMethodError e) {
 			Log.i(TAG, "Invalidate method not available");
 		}
     }
     
     @Override
     public void onResume() {
     	super.onResume();
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
     	MenuInflater inflater = getMenuInflater();
     	inflater.inflate(R.menu.mainmenu, menu);
     	MenuCompat.setShowAsAction(menu.findItem(R.id.menunew), 
     			MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
     	MenuCompat.setShowAsAction(menu.findItem(R.id.menushowall),
    			MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
 
     	hideShowAllItem(menu);
     	setSortOptions(menu);
     	return true;
     }
 
     private void setSortOptions(Menu menu) {
 		// activate the correct options in the sort menu
     	if (sortKey != 0) {
     		menu.findItem(sortKey).setChecked(true);
     	} else {
     		menu.findItem(R.id.menusortname).setChecked(true);
     	}
     	
     	if (sortDescending) {
     		menu.findItem(R.id.menusortdescending).setChecked(true);
     	} else {
     		menu.findItem(R.id.menusortascending).setChecked(true);
     	}
 	}
 
 	@Override
     public boolean onPrepareOptionsMenu(Menu menu) {
     	super.onPrepareOptionsMenu(menu);
     	
     	hideShowAllItem(menu);
     	return true;
     }
     
 	private void hideShowAllItem(Menu menu) {
 		// hide "Show All" option if already showing all recipes
 		boolean menuStatus = searchMode || tagSearchMode;
 		menu.findItem(R.id.menushowall).setVisible(menuStatus).setEnabled(menuStatus);	
 	}
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
     	boolean descending = false;
     	switch (item.getItemId()) {
     	case R.id.menunew:
     		onNewItemSelected(item);
     		return true;
     	case R.id.menusearch:
     		onSearchRequested();
     		return true;
     	case R.id.menusearchtag:
     		onSearchByTag();
     		return true;
     	case R.id.menushowall:
     		onShowAllRecipes(item);
     		return true;
 //		case android.R.id.home:
 //			switchToFlipBook();
 //			return true;
     	case R.id.menusortdescending:
     		descending = true;
     		// fall-through on purpose
     	case R.id.menusortascending:
     		item.setChecked(!item.isChecked());
     		startSortActivity(sortKey, descending);
     		return true;
     	
     	case R.id.menusortname:
     	case R.id.menusortrating:
     	case R.id.menusorttime:
     	case R.id.menusortdate:
     		item.setChecked(!item.isChecked());
     		startSortActivity(item.getItemId(), sortDescending);
     		return true;
     		
     	default:
     		return super.onOptionsItemSelected(item);
     	}
     }
 
 	private void startSortActivity(int key, boolean descending) {
 		Intent intent = new Intent(this, this.getClass());
 		intent.putExtra(SORT_KEY, key);
 		intent.putExtra(SORT_DESCENDING, descending);
 		if (searchMode) {
 			intent.putExtra(RecipeBook.SEARCH_EXTRA, searchQuery);
 		}
 		if (tagSearchMode) {
 			intent.putExtra(RecipeBook.TAG_EXTRA, searchTag);
 		}
 		startActivity(intent);
 	}
 
     void switchToFlipBook() {
 //    	Intent intent = new Intent(this, RecipeFlipbook.class);
     	Intent intent = new Intent(lastIntent);
     	intent.setClass(this, RecipeFlipbook.class);
 //    	intent.putExtra(RecipeBook.SEARCH_EXTRA, searchQuery);
     	startActivity(intent);
     }
     
     @Override
     public boolean onSearchRequested() {
     	Bundle searchData = new Bundle();
     	searchData.putBoolean(SORT_DESCENDING, sortDescending);
     	searchData.putInt(SORT_KEY, sortKey);
     	startSearch(null, false, searchData, false);
     	return true;
     }
     
     @Override
     public boolean onContextItemSelected(MenuItem item) {
     	// TODO Auto-generated method stub
     	return false;
     }
     
     public void onNewItemSelected(MenuItem item) {
     	// TODO
     	Log.i(TAG, "New option selected");
     	Uri uri = new Uri.Builder().scheme("content").authority("net.potterpcs.recipebook").build();
     	uri = ContentUris.withAppendedId(uri, -1);
     	Log.i(TAG, "new option selected");
     	Intent intent = new Intent(Intent.ACTION_INSERT_OR_EDIT, uri);
     	startActivity(intent);
     }
     
     public void onShowAllRecipes(MenuItem item) {
     	Log.i(TAG, "Show all option selected");
     	Intent intent = new Intent(this, RecipeBookActivity.class);
     	intent.putExtra(SORT_DESCENDING, sortDescending);
     	intent.putExtra(SORT_KEY, sortKey);
     	startActivity(intent);
     }
     
     public void onSearchByTag() {
     	FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
     	TagSearchDialog tsd = TagSearchDialog.newInstance();
     	tsd.show(ft, null);
     }
 
 	public boolean isSearchMode() {
 		return searchMode;
 	}
 
 	public String getSearchQuery() {
 		return searchQuery;
 	}
 	
 	public boolean getSortDirection() {
 		// true if descending, false if ascending
 		return sortDescending;
 	}
 	
 	public int getSortKey() {
 		// returns an ID value
 		return sortKey;
 	}
 
 	public String getSearchTag() {
 		// returns a tag to be used for searching
 		return searchTag;
 	}
 	
 	public boolean isTagSearch() {
 		// true if we are searching for a tag
 		return tagSearchMode;
 	}
 }
