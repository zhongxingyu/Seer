 package com.main.passthedoodle;
 
 import java.io.File;
 import java.util.ArrayList;
 
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentStatePagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.view.View;
 
 import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
 import com.nostra13.universalimageloader.core.DisplayImageOptions;
 import com.nostra13.universalimageloader.core.ImageLoader;
 import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
 import com.nostra13.universalimageloader.utils.StorageUtils;
 
 public class ViewCompletedActivity extends FragmentActivity {
 	static ArrayList<TurnInfo> stringsList;
 	ViewPager mViewPager;
 	ViewCompletedPagerAdapter mViewCompletedPagerAdapter;
 	ImageLoader imageLoader;
 	DisplayImageOptions options;
 	
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         // Configure Android-Universal-Image-Loader which will handle image caching
         File cacheDir = StorageUtils.getCacheDirectory(this);
         DisplayImageOptions dio = new DisplayImageOptions.Builder()
 									        .cacheInMemory(true)
 									        .cacheOnDisc(true)
 									        .build();
         ImageLoaderConfiguration ilConfig = new ImageLoaderConfiguration.Builder(this)
         										.defaultDisplayImageOptions(dio)
         										.discCache(new UnlimitedDiscCache(cacheDir))
         										.writeDebugLogs()
         										.build();
         imageLoader = ImageLoader.getInstance();
         imageLoader.init(ilConfig);
         
         // Stores strings that will be passed to each round fragment
         // Size of the list = # of drawings in the game = # fragments
         stringsList = new ArrayList<TurnInfo>();
         buildTest();
         
         setContentView(R.layout.activity_viewcompleted);
         mViewCompletedPagerAdapter = new ViewCompletedPagerAdapter(getSupportFragmentManager());
         mViewPager = (ViewPager) findViewById(R.id.completed_pager);
         mViewPager.setPageTransformer(false, new DepthPageTransformer()); // fancy animation
         mViewPager.setAdapter(mViewCompletedPagerAdapter);
 
         mViewPager.setOffscreenPageLimit(2);
         
         String gameID = getIntent().getStringExtra("GameID");
         /* Implement method to retrieve from db
          * for every drawing of gameID
            		TurnInfo.imageUrl <- image urls
            		TurnInfo.prompt <- description for (drawing sequence) - 1
            		TurnInfo.guess <- description for (drawing sequence) + 1
            		add TurnInfo to stringsList
          */
     }
 
     private void buildTest() {
         stringsList.add(new TurnInfo("http://passthedoodle.com/i/26_1385351398.png", "Turtle", "Frog"));
         stringsList.add(new TurnInfo("http://passthedoodle.com/i/3z1qsrmi.png", "Frog", "Battletoad"));
         stringsList.add(new TurnInfo("http://i.imgur.com/JpKpG.jpg", "Battletoad", "Cheater"));
         stringsList.add(new TurnInfo("http://i.imgur.com/OxMEYsj.jpg", "Cheater", "End"));
         stringsList.add(new TurnInfo("http://i.imgur.com/oMLLDgE.jpg", "blah", "boop"));
         stringsList.add(new TurnInfo("http://i.imgur.com/dLukzCx.jpg", "blah", "boop"));
         stringsList.add(new TurnInfo("http://i.imgur.com/Vh8WEd2.jpg", "blah", "boop"));
         stringsList.add(new TurnInfo("http://i.imgur.com/aFXrucw.jpg", "blah", "boop"));
         stringsList.add(new TurnInfo("http://i.imgur.com/28Jq75s.jpg", "blah", "boop"));
         stringsList.add(new TurnInfo("http://i.imgur.com/uUCzB9F.jpg", "blah", "boop"));
     }
     
 	public static class ViewCompletedPagerAdapter extends FragmentStatePagerAdapter {
 
         public ViewCompletedPagerAdapter(FragmentManager fm) {
             super(fm);
         }
 
         @Override
         public Fragment getItem(int position) {
         	// fragment's tab index corresponds to its index in stringsList
         	// mod by stringsList size to achieve loop effect
         	int i = position % stringsList.size();
             Fragment frag = new ViewCompletedFragment();
             Bundle args = new Bundle();
             //args.putInt("DrawingNumber", i);
             args.putString("URL", stringsList.get(i).imageUrl);
             args.putString("Prompt", stringsList.get(i).prompt);
             args.putString("Description", stringsList.get(i).desc);
             frag.setArguments(args);
 
             return frag;
         }
 
         @Override
         public int getCount() {
         	return 999999; // arbitrarily big number
             //return stringsList.size();
         }
 
         @Override
         public CharSequence getPageTitle(int position) {
        	int i = position % stringsList.size();
            return (i*2 + 1) + " \u2192 " + (i*2 + 2);
         }
     }
 
 	private class TurnInfo {
 		String imageUrl;
 		String prompt;
 		String desc;
 		
 		public TurnInfo(String a, String b, String c) {
 			imageUrl = a;
 			prompt = b;
 			desc = c;
 		}
 	}
 }
 
  class DepthPageTransformer implements ViewPager.PageTransformer {
     private static float MIN_SCALE = 0.75f;
 
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
             view.setTranslationX(-1 * view.getWidth() * position);
             //view.setTranslationX(pageWidth * -position);
 
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
