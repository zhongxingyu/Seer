 package net.stackueberflow.excusewidget;
 
 import java.util.List;
 import java.util.Random;
 
 import net.stackueberflow.bofhexcuses.R;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Parcelable;
 import android.support.v4.view.PagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.text.Spannable;
 import android.text.SpannableString;
 import android.text.style.StyleSpan;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 import android.widget.ShareActionProvider;
 import android.widget.TextView;
 
 import com.viewpagerindicator.TitlePageIndicator;
 import com.viewpagerindicator.TitleProvider;
 
 
 public class ExcuseBrowseActivity extends Activity {
 	public static final String TAG = "excuse activity";
 	private ViewPagerAdapter adapter;
 	// Used for the share menu
 	private ShareActionProvider mShareActionProvider;
 	private ExcusesDataSource datasource;
 	
     /** Called when the activity is first created. */
     @Override
     protected void onCreate(Bundle savedInstanceState) {        
         super.onCreate(savedInstanceState); 
         setContentView(R.layout.main);
         
 	    
 	    // Set the listener for the button
 	    
 	    Button button = (Button)findViewById(R.id.button);
 	   
 	    button.setOnClickListener(new View.OnClickListener() {
 	    	@Override
 	    	public void onClick(View v) {
 	    		setRandom();
 	    	}
 	    });
    }
    
    @Override 
    protected void onStart() {
     	datasource = new ExcusesDataSource(this);
  		datasource.open();
 		
          adapter = new ViewPagerAdapter( this );
  	    ViewPager pager = (ViewPager)findViewById( R.id.viewpager );
  	    
  	    adapter.setData(datasource.getAllExcuses());
  	    Log.i(TAG, "Excuses lenght"+datasource.getAllExcuses().size());
 
  	    TitlePageIndicator titleIndicator = (TitlePageIndicator)findViewById(R.id.titles);
  	    pager.setAdapter( adapter );
  	    titleIndicator.setViewPager(pager);
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
     	super.onCreateOptionsMenu(menu);
     	getMenuInflater().inflate(R.menu.menu, menu);
         
         mShareActionProvider = (ShareActionProvider) menu.findItem(R.id.menu_share).getActionProvider();
         // Set the share intent
         Intent i = createShareIntent();
         if (i != null)
         	mShareActionProvider.setShareIntent(i);
 
         return true;
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.menu_random:
             	setRandom();
                 return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
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
     	shareIntent.putExtra(Intent.EXTRA_TEXT, adapter.data.get(index).getExcuse());
 
     	return shareIntent;
     }
     
     private void setRandom() {
 		int index;
 		ViewPager pager = (ViewPager)findViewById( R.id.viewpager );
 		index = (new Random()).nextInt(adapter.data.size());
 		pager.setCurrentItem(index);
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
     		Log.i(TAG, "instantiateItem: "+excuse);
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
