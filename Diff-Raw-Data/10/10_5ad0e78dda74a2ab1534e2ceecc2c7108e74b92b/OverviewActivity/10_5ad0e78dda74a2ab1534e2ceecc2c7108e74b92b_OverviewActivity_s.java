 package se.goagubbar.twee;
 
 
 import java.util.ArrayList;
 
 import android.content.SharedPreferences;
 import android.net.Uri;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.app.NavUtils;
 import android.support.v4.view.ViewPager;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.ListView;
 import android.widget.Toast;
 import se.goagubbar.twee.Adapters.EpisodeAdapter;
 import se.goagubbar.twee.Fragments.EpisodesFragment;
 import se.goagubbar.twee.Models.Series;
 
 public class OverviewActivity extends FragmentActivity {
 
     /**
      * The {@link android.support.v4.view.PagerAdapter} that will provide fragments for each of the
      * sections. We use a {@link android.support.v4.app.FragmentPagerAdapter} derivative, which will
      * keep every loaded fragment in memory. If this becomes too memory intensive, it may be best
      * to switch to a {@link android.support.v4.app.FragmentStatePagerAdapter}.
      */
     SectionsPagerAdapter mSectionsPagerAdapter;
     ArrayList<Fragment> fragments;
     Series series;
     int totalEpisodes;
     int watchedEpisodes;
     String seriesId;
     String tvdbSeriesId;
 
     /**
      * The {@link ViewPager} that will host the section contents.
      */
     ViewPager mViewPager;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
     	SharedPreferences settings = getSharedPreferences("Twee", 0);
 	    int theme = settings.getInt("Theme", R.style.Light);
 		setTheme(theme);
     	
         super.onCreate(savedInstanceState);
         setContentView(R.layout.layout_overview);
         getActionBar().setDisplayHomeAsUpEnabled(true);
         
         Bundle extras = getIntent().getExtras();
         
         seriesId = extras.getString("SeriesId");
         
         series = new DatabaseHandler(getBaseContext()).getSeriesById(seriesId);
         
         tvdbSeriesId = "" + series.getSeriesId();
         
         getActionBar().setTitle(series.getName());
         
         fragments = new ArrayList<Fragment>();
         fragments.add(new Fragments.SummaryFragment(series));
         fragments.add(new Fragments.OverviewFragment(series));
         
         fragments.add(new Fragments.EpisodesFragment(series, totalEpisodes, watchedEpisodes));
         // Create the adapter that will return a fragment for each of the three primary sections
         // of the app.
         mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
 
         
        
         
         
         // Set up the ViewPager with the sections adapter.
         mViewPager = (ViewPager) findViewById(R.id.pager);
         mViewPager.setAdapter(mSectionsPagerAdapter);
         mViewPager.setCurrentItem(1);
                
         
     }
     
 	public void Reload()
 	{
 		Log.d("Test","Reload");
 		EpisodesFragment fragment = (EpisodesFragment) fragments.get(2);
 		fragment = new Fragments.EpisodesFragment(series, totalEpisodes, watchedEpisodes);
 	}
 
     public void Refresh()
     {
     	View v = (View)fragments.get(1).getView();
     	Fragments.SetProgress(v, tvdbSeriesId);
     }
 	
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.menu_overview, menu);
         return true;
     }
 
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case android.R.id.home:
                 NavUtils.navigateUpFromSameTask(this);
                 return true;
                 
             case R.id.menu_markseries:
             	new DatabaseHandler(getBaseContext()).MarkSeriesAsWatched(series.getSeriesId());
             	Toast.makeText(getBaseContext(), R.string.message_series_watched, Toast.LENGTH_SHORT).show();
             	Refresh();
            	//View v = (View)fragments.get(1).getView();
            	//Fragments.SetProgress(v, seriesId);
         }
         return super.onOptionsItemSelected(item);
     }
 
 
 
     /**
      * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the primary
      * sections of the app.
      */
     public class SectionsPagerAdapter extends FragmentPagerAdapter {
 
         public SectionsPagerAdapter(FragmentManager fm) {
             super(fm);
         }
 
         @Override
         public Fragment getItem(int i) {
             Fragment fragment = fragments.get(i); //array new DummySectionFragment();
             Bundle args = new Bundle();
             //args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, i + 1);
             fragment.setArguments(args);
             return fragment;
         }
 
         @Override
         public int getCount() {
             return 3;
         }
         
         @Override
         public CharSequence getPageTitle(int position) {
             switch (position) {
                 case 0: return getString(R.string.section_title_summary).toUpperCase();
                 case 1: return getString(R.string.section_title_overview).toUpperCase();
                 case 2: return getString(R.string.section_title_episodes).toUpperCase();
             }
             return null;
         }
     }
 }
