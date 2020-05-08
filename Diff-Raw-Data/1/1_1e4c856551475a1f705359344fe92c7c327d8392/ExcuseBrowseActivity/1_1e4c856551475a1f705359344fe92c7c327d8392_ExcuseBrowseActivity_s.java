 package net.stackueberflow.excusewidget;
 
 import java.util.List;
 import java.util.Random;
 
 import android.app.Activity;
 import android.app.SearchManager;
 import android.content.Context;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Parcelable;
 import android.support.v4.view.PagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.text.Spannable;
 import android.text.SpannableString;
 import android.text.style.StyleSpan;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.View;
 import android.widget.Button;
 import android.widget.ProgressBar;
 import android.widget.SearchView;
 import android.widget.ShareActionProvider;
 import android.widget.TextView;
 
import com.viewpagerindicator.R;
 import com.viewpagerindicator.TitlePageIndicator;
 import com.viewpagerindicator.TitleProvider;
 
 
 public class ExcuseBrowseActivity extends Activity {
 	public static final String TAG = "excuse activity";
 	private ViewPagerAdapter adapter;
 	private ProgressBar progressBar;
     // Used for the share menu
 	private ShareActionProvider mShareActionProvider;
 	private ExcusesDataSource datasource;
 	
     /** Called when the activity is first created. */
     @Override
     protected void onCreate(Bundle savedInstanceState) {        
         super.onCreate(savedInstanceState); 
         setContentView(R.layout.main);
         
         datasource = new ExcusesDataSource(this);
 		datasource.open();
 		
         adapter = new ViewPagerAdapter( this );
 	    ViewPager pager = (ViewPager)findViewById( R.id.viewpager );
 	    
 	    adapter.setData(datasource.getAllExcuses());
 	    Log.i(TAG, "Excuses lenght"+datasource.getAllExcuses().size());
 	    
 	    // Hide the progress bar
 	    progressBar = (ProgressBar)findViewById(R.id.progressBar1);
 	    progressBar.setVisibility(View.INVISIBLE);
 
 	    TitlePageIndicator titleIndicator = (TitlePageIndicator)findViewById(R.id.titles);
 	    pager.setAdapter( adapter );
 	    titleIndicator.setViewPager(pager);
 	    
 	    // Handle searches
 	    Intent intent = getIntent();
 	    if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
 	      String query = intent.getStringExtra(SearchManager.QUERY);
 	      Log.i(TAG, "Search query: "+query);
 	      long id = datasource.cursorToExcuse(datasource.query(query)).getId();
 	      pager.setCurrentItem((int)id);
 	    }  else if (Intent.ACTION_VIEW.equals(intent.getAction())) {
 	        // Handle a suggestions click (because the suggestions all use ACTION_VIEW)
 	        Uri data = intent.getData();
 	        Log.i(TAG, "Search suggestion:" + data);
 	        //showResult(data);
 	    }
 	    
 	    // Set the listener for the button
 	    
 	    Button button = (Button)findViewById(R.id.button);
 	    button.setOnClickListener(new View.OnClickListener() {
 	    	@Override
 	    	public void onClick(View v) {
 	    			int index;
 	    			ViewPager pager = (ViewPager)findViewById( R.id.viewpager );
 	    			index = (new Random()).nextInt(adapter.data.size());
 	    			pager.setCurrentItem(index);
 	    	}
 	    });
     }
     
     @Override 
     protected void onStart() {
     	datasource.open();
         super.onStart();
     }
     
     @Override
     protected void onStop() {
         super.onStop();
     
 	    // Done with the DB at this point.
 	    datasource.close();
     }
     
     @Override
     protected void onResume() {
         super.onResume();
     }
 
     @Override
     protected void onPause() {
     	super.onPause();
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
     	MenuInflater  inflater = getMenuInflater();
     	inflater.inflate(R.menu.menu, menu);
         
         mShareActionProvider = (ShareActionProvider) menu.findItem(R.id.menu_share).getActionProvider();
         // Set the share intent
         Intent i = createShareIntent();
         if (i != null)
         	mShareActionProvider.setShareIntent(i);
         
         // Searching
         // Get the SearchView and set the searchable configuration
         SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
         SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
         searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
         searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default
         return true;
     }
     
     @Override
     public void onLowMemory () {
     	// Be nice and release stuff
     	super.onLowMemory();
     }
     
     private Intent createShareIntent() {
     	ViewPager pager = (ViewPager)findViewById( R.id.viewpager );
     	int index = pager.getCurrentItem();
 
     	Intent shareIntent = new Intent(Intent.ACTION_SEND);
     	shareIntent.setAction(Intent.ACTION_SEND);
     	shareIntent.setType("text/*");
     	shareIntent.putExtra(Intent.EXTRA_SUBJECT, "BOFH Excuse of the day");
 
     	//TODO
     	shareIntent.putExtra(Intent.EXTRA_TEXT, adapter.data.get(index).getExcuse());
 
     	return shareIntent;
     }
     
     private class ViewPagerAdapter extends PagerAdapter
     implements TitleProvider {
 
     	private static final String TAG = "PageAdapter";
     	public List<Excuse> data;
 
     	private Context context;
 
     	public ViewPagerAdapter( Context context) {
     		this.context = context;
     	}
 
     	@Override
     	public int getCount() {
     		//return 0;
     		return data.size();
     	}
 
     	@Override
     	public String getTitle( int position ) {
     		String title = "#"+position;
     		return title;
     	}
 
     	@Override
     	public int getItemPosition(Object object) {
     		return POSITION_NONE;
     	}
 
     	@Override
     	public Object instantiateItem( View pager, int position ) {
     		String excuse = data.get(position).toString();
     		TextView v = new TextView( context );
     		v.setTextIsSelectable(true);
     		v.setTextSize(20);
     		v.setText( excuse );
     		SpannableString str = new SpannableString(excuse);
     		str.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, str.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
     		((ViewPager)pager).addView( v, 0 );
     		
     		// This calls onPrepareOptionsMenu() in order to update the shareIntents text
     		invalidateOptionsMenu();
     		return v;
     	}
 
     	@Override
     	public void destroyItem( View pager, int position, Object view ) {
     		((ViewPager)pager).removeView( (TextView)view );
     	}
 
     	@Override
     	public boolean isViewFromObject( View view, Object object ) {
     		return view.equals( object );
     	}
 
     	@Override
     	public void finishUpdate( View view ) {}
 
     	@Override
     	public void restoreState( Parcelable p, ClassLoader c ) {}
 
     	@Override
     	public Parcelable saveState() {
     		return null;
     	}
 
     	@Override
     	public void startUpdate( View view ) {}
 
     	public void setData(List<Excuse> data) {
     		Log.v(TAG, "setData called");
     		this.data = data;
     	}
     }
 }
