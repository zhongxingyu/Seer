 package se.chalmers.watchme.activity;
 
 import java.util.List;
 import se.chalmers.watchme.R;
 import se.chalmers.watchme.database.DatabaseAdapter;
 import se.chalmers.watchme.model.Movie;
 import se.chalmers.watchme.ui.MovieListFragment;
 import se.chalmers.watchme.ui.TagListFragment;
 import se.chalmers.watchme.utils.DateTimeUtils;
 import android.app.ActionBar;
 import android.app.SearchManager;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.view.ViewPager;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.SearchView;
 
 
 public class MainActivity extends FragmentActivity {
 	
 	public static final String MOVIE_DETAILS_ID = "se.chalmers.watchme.DETAILS_ID";
 	public static final String MOVIE_DETAILS_TITLE = "se.chalmers.watchme.DETAILS_TITLE";
 	public static final String MOVIE_DETAILS_RATING = "se.chalmers.watchme.DETAILS_RATING";
 	public static final String MOVIE_DETAILS_NOTE = "se.chalmers.watchme.DETAILS_NOTE";
 	
 	private ViewPager viewPager;
 	private TabsAdapter tabsAdapter;
 	private ActionBar actionBar;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         //setup view pager
         this.viewPager = new ViewPager(this);
         this.viewPager.setId(R.id.vPager);
         setContentView(viewPager);
         
         //setup actionbar
         actionBar = getActionBar();
 		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
 		actionBar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);
 		
 		//setup tabs
 		tabsAdapter = new TabsAdapter(this, viewPager);
 		tabsAdapter.addTab(actionBar.newTab().setText(R.string.tab_movies), MovieListFragment.class, null);
 		tabsAdapter.addTab(actionBar.newTab().setText(R.string.tab_tags), TagListFragment.class, null);
 		if (savedInstanceState != null) {
 			actionBar.setSelectedNavigationItem(savedInstanceState.getInt("tab", 0));
 		}	
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_main, menu);
         
         SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
         SearchView searchView = (SearchView) menu.findItem(R.id.menu_search_button).getActionView();
         searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
         searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default
 
         return true;
     }
     
     /**
      * When the user clicks on a button in the Action bar.
      */
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         
     	switch(item.getItemId()) {
     	case R.id.menu_add_movie:
     		Intent intent = new Intent(this, AddMovieActivity.class);
             startActivity(intent);
             return true;
     		
     	case R.id.menu_send_email_button:
     		sendEmail();
     		return true;
     		
     	case R.id.menu_search_button:
             onSearchRequested();
             return true;
             
     	default:
     		return super.onOptionsItemSelected(item);
     	}
     }
     
     private void sendEmail() {
     	Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
     	emailIntent.setType("text/plain");
     	
     	List<Movie> movies = new DatabaseAdapter(getContentResolver()).getAllMovies();
     	
     	// Parse all movies with their dates:
     	String movieString = "";
     	for(Movie m : movies) {
     		movieString += m.getTitle() + " ("+DateTimeUtils.toSimpleDate(m.getDate()) + ")\n";
     	}
     	
     	emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, R.string.email_subject);
     	emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, movieString);  
     	
     	// Let the user choose email app to mail from
     	startActivity(Intent.createChooser(emailIntent, getString(R.string.choose_message_app)));
     }
         
     @Override
     protected void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
         outState.putInt("tab", getActionBar().getSelectedNavigationIndex());
     }
 
 }
