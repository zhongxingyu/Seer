 package com.dgsd.android.ShiftTracker;
 
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.support.v4.view.ViewPager;
 import android.text.format.Time;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 import com.dgsd.android.ShiftTracker.Adapter.WeekPagerAdapter;
 import com.dgsd.android.ShiftTracker.Fragment.DatePickerFragment;
 import com.dgsd.android.ShiftTracker.Util.*;
 import com.viewpagerindicator.TitlePageIndicator;
import de.keyboardsurfer.android.widget.crouton.Crouton;
 
 public class MainActivity extends SherlockFragmentActivity implements DatePickerFragment.OnDateSelectedListener {
     private static final int ANIM_TYPE_PLAIN = 0;
     private static final int ANIM_TYPE_INNER_CUBE = 1;
     private static final int ANIM_TYPE_OUTER_CUBE = 2;
     private static final int ANIM_TYPE_TWIST = 3;
     private static final int ANIM_TYPE_COMPRESS = 4;
 
     private TitlePageIndicator mIndicator;
     private ViewPager mPager;
     private WeekPagerAdapter mAdapter;
     private DatePickerFragment mGoToFragment;
 
     private Prefs mPrefs;
 
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
 
         mPrefs = Prefs.getInstance(this);
 
         //Show our 'Rate in Market' dialog if needed
         AppRating.app_launched(this);
 
         final int currentJd = TimeUtils.getCurrentJulianDay();
         mAdapter = new WeekPagerAdapter(this, getSupportFragmentManager(), currentJd);
 
         mPager = (ViewPager) findViewById(R.id.pager);
         mPager.setAdapter(mAdapter);
 
         if(Api.isMin(Api.HONEYCOMB) && !StApp.isFreeApp(this)) {
             final int animType = Integer.valueOf(mPrefs.get(getString(R.string.settings_key_animation), "0"));
 
             ViewPager.PageTransformer transformer = null;
             switch(animType) {
                 case ANIM_TYPE_INNER_CUBE:
                     transformer = PageTransformerUtils.getInnerCubeTransformer();
                     break;
                 case ANIM_TYPE_OUTER_CUBE:
                     transformer = PageTransformerUtils.getOuterCubeTransformer();
                     break;
                 case ANIM_TYPE_TWIST:
                     transformer = PageTransformerUtils.getTwistTransformer();
                     break;
                 case ANIM_TYPE_COMPRESS:
                     transformer = PageTransformerUtils.getCompressTransformer();
                     break;
             }
 
             if(transformer != null)
                 mPager.setPageTransformer(false, transformer);
         }
 
         mIndicator = (TitlePageIndicator) findViewById(R.id.indicator);
         mIndicator.setViewPager(mPager, mAdapter.getPositionForJulianDay(currentJd));
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getSupportMenuInflater().inflate(R.menu.main, menu);
         return super.onCreateOptionsMenu(menu);
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         if(item.getItemId() == R.id.settings) {
             startActivity(new Intent(this, SettingsActivity.class));
         } else if(item.getItemId() == R.id.go_to) {
             if(mGoToFragment != null && mGoToFragment.isResumed()) {
                 //We're showing already!
                 return true;
             }
 
             final int centerJd = mAdapter.getJulianDayForPosition(mAdapter.getCenterPosition());
             final int count = mAdapter.getCount() * 7;
 
             final Time time = new Time();
             time.setJulianDay(centerJd - (count / 2));
             final long min = time.toMillis(true);
 
             time.setJulianDay(centerJd + (count / 2));
             final long max = time.toMillis(true);
 
             mGoToFragment = DatePickerFragment.newInstance("Go to date..", min, max, -1);
             mGoToFragment.setOnDateSelectedListener(this);
             mGoToFragment.show(getSupportFragmentManager(), null);
         } else if(item.getItemId() == R.id.get_full_version) {
             Uri uri = Uri.parse("market://details?id=com.dgsd.android.ShiftTracker");
             startActivity(new Intent(Intent.ACTION_VIEW, uri));
         }
 
         return super.onOptionsItemSelected(item);
     }
 
     @Override
     public void onDateSelected(int typeCode, int julianDay) {
         mIndicator.setCurrentItem(mAdapter.getPositionForJulianDay(julianDay));
     }
 
     @Override
     protected void onDestroy() {
         Crouton.clearCroutonsForActivity(this);
         super.onDestroy();
     }
 }
