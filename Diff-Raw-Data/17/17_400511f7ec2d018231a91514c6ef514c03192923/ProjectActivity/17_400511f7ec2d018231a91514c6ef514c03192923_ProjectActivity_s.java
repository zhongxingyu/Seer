 package com.example.portfolioapp;
 
 import java.util.ArrayList;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.app.NavUtils;
 import android.support.v4.view.PagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 
 public class ProjectActivity extends FragmentActivity {
 
     /**
      * The number of pages (projects) to show on this user.
      */
     private static final int NUM_PAGES = 5;
 
     /**
      * The pager widget, which handles animation and allows swiping horizontally to access previous
      * and next project steps.
      */
     private ViewPager mPager;
 
     /**
      * The pager adapter, which provides the pages to the view pager widget.
      */
     private PagerAdapter mPagerAdapter;
     
     Person person;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_project);
 		
 		// Check if we got a User sent to us in EXTRAS:
 		Intent extras = getIntent();
 		//Bundle extras = getIntent().getExtras(); 
		
		if(extras.getStringExtra("name") == "Jimmi Elofsson")
 		{
 			person = new Jimmi();
 		}
		else if(extras.getStringExtra("name") == "Kaan Orgunmat")
 		{
 			person = new Kaan();
 		}
		else if(extras.getStringExtra("name") == "Carl Blockås")
 		{
 			person = new Carl();
 		}
         // Instantiate a ViewPager and a PagerAdapter.
         mPager = (ViewPager) findViewById(R.id.pager);
         //mPager.setPageTransformer(true, new DepthPageTransformer());
         mPager.setPageTransformer(true, new ZoomOutPageTransformer());
         mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager(), person);
         mPager.setAdapter(mPagerAdapter);
 	}
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);
 
         getMenuInflater().inflate(R.menu.project, menu);
         menu.findItem(R.id.action_previous).setEnabled(mPager.getCurrentItem() > 0);
 
         // Add either a "next" or "finish" button to the action bar, depending on which page
         // is currently selected.
         MenuItem item = menu.add(Menu.NONE, R.id.action_next, Menu.NONE,
                 (mPager.getCurrentItem() == mPagerAdapter.getCount() - 1)
                         ? R.string.action_previous
                         : R.string.action_next);
         item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
         return true;
     }
 	
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
     	
         switch (item.getItemId()) {
             case android.R.id.home:
                 // Navigate "up" the demo structure to the launchpad activity.
                 // See http://developer.android.com/design/patterns/navigation.html for more.
                 NavUtils.navigateUpTo(this, new Intent(this, MainActivity.class));
                 return true;
 
             case R.id.action_previous:
                 // Go to the previous step in the wizard. If there is no previous step,
                 // setCurrentItem will do nothing.
                 mPager.setCurrentItem(mPager.getCurrentItem() - 1);
                 return true;
 
             case R.id.action_next:
                 // Advance to the next step in the wizard. If there is no next step, setCurrentItem
                 // will do nothing.
                 mPager.setCurrentItem(mPager.getCurrentItem() + 1);
                 return true;
         }
 
         return super.onOptionsItemSelected(item);
     }
 
     /**
      * A simple pager adapter that represents 5 ScreenSlidePageFragment objects, in
      * sequence.
      */
     private class ScreenSlidePagerAdapter extends FragmentPagerAdapter {
     	
     	protected Person person;
     	
         public ScreenSlidePagerAdapter(FragmentManager fm, Person p) {
             super(fm);
             person = p;
         }
 
         @Override
         public Fragment getItem(int position) {
             return (Fragment) ScreenSlideProjectFragment.create(position, person.getProjectById(position));
         }
 
         @Override
         public int getCount() {
             return person.projects.size();
         }
     }
     
     /**
      * A transformation-effect to be used in the pager->setPageTransformer()
      */
 	public class DepthPageTransformer implements ViewPager.PageTransformer {
 	    private static final float MIN_SCALE = 0.75f;
 	
 	    public void transformPage(View view, float position) {
 	        int pageWidth = view.getWidth();
 	
 	        if (position < -1) { // [-Infinity,-1)
 	            // This page is way off-screen to the left.
 	            view.setAlpha(0);
 	
 	        } else if (position <= 0) { // [-1,0]
 	            // Use the default slide transition when moving to the left page
 	            view.setAlpha(1);
 	            view.setTranslationX(0);
 	            view.setScaleX(1);
 	            view.setScaleY(1);
 	
 	        } else if (position <= 1) { // (0,1]
 	            // Fade the page out.
 	            view.setAlpha(1 - position);
 	
 	            // Counteract the default slide transition
 	            view.setTranslationX(pageWidth * -position);
 	
 	            // Scale the page down (between MIN_SCALE and 1)
 	            float scaleFactor = MIN_SCALE
 	                    + (1 - MIN_SCALE) * (1 - Math.abs(position));
 	            view.setScaleX(scaleFactor);
 	            view.setScaleY(scaleFactor);
 	
 	        } else { // (1,+Infinity]
 	            // This page is way off-screen to the right.
 	            view.setAlpha(0);
 	        }
 	  }
 	}
 	
 	public class ZoomOutPageTransformer implements ViewPager.PageTransformer {
 	    private static final float MIN_SCALE = 0.85f;
 	    private static final float MIN_ALPHA = 0.5f;
 
 	    public void transformPage(View view, float position) {
 	        int pageWidth = view.getWidth();
 	        int pageHeight = view.getHeight();
 
 	        if (position < -1) { // [-Infinity,-1)
 	            // This page is way off-screen to the left.
 	            view.setAlpha(0);
 
 	        } else if (position <= 1) { // [-1,1]
 	            // Modify the default slide transition to shrink the page as well
 	            float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position));
 	            float vertMargin = pageHeight * (1 - scaleFactor) / 2;
 	            float horzMargin = pageWidth * (1 - scaleFactor) / 2;
 	            if (position < 0) {
 	                view.setTranslationX(horzMargin - vertMargin / 2);
 	            } else {
 	                view.setTranslationX(-horzMargin + vertMargin / 2);
 	            }
 
 	            // Scale the page down (between MIN_SCALE and 1)
 	            view.setScaleX(scaleFactor);
 	            view.setScaleY(scaleFactor);
 
 	            // Fade the page relative to its size.
 	            view.setAlpha(MIN_ALPHA +
 	                    (scaleFactor - MIN_SCALE) /
 	                    (1 - MIN_SCALE) * (1 - MIN_ALPHA));
 
 	        } else { // (1,+Infinity]
 	            // This page is way off-screen to the right.
 	            view.setAlpha(0);
 	        }
 	    }
 	}
 
 }
