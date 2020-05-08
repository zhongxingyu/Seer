 package com.adamcarruthers.foundry;
 
 import java.util.ArrayList;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Looper;
 import android.os.StrictMode;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ImageButton;
 
 import com.adamcarruthers.foundry.widget.PagerHeader;
 
 public class APTActivity extends FragmentActivity {
 	private ImageButton mShare;
 	private ImageButton mSearch;
     private ViewPager mPager;
     private Context mContext;
     private PagerAdapter mPagerAdapter;
     private boolean mRooted;
     private SharedPreferences mPreferences;
 
 	/** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
         mContext = getApplicationContext();
         
         StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
         	.detectAll()
         	.penaltyLog()
         	.penaltyDialog()
         	.build());
         
        /* note that we will run the root check on every launch
     	* as users can root their device without the app being uninstalled
     	* Do not run the root check if we already know it is rooted
     	*/
     	mPreferences = mContext.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
     	mRooted = mPreferences.getBoolean(Constants.KEY_ROOTED, false);
     	if(!mRooted)
 	        new RootCheck().execute();
         
         mShare = (ImageButton)findViewById(R.id.share_button);
         mShare.setOnClickListener(new OnClickListener(){
 	        @Override
 			public void onClick(View v) {
 	        	startActivity(Utils.share(mContext));
 	         }
 	    });
         
         mSearch = (ImageButton)findViewById(R.id.search_button);
         mSearch.setOnClickListener(new OnClickListener(){
 	        @Override
 			public void onClick(View v) {
 	        	// search packages
 	         }
 	    });
     	
         mPager = (ViewPager)findViewById(R.id.pager);
         mPagerAdapter = new PagerAdapter(this,
                 mPager,
                 (PagerHeader)findViewById(R.id.pager_header));
         
         mPagerAdapter.addPage(PackageManager.class, R.string.page_label_pacman);
         mPagerAdapter.addPage(Homepage.class, R.string.page_label_homepage);
         mPagerAdapter.addPage(PackageBrowser.class, R.string.page_label_browse);
         mPagerAdapter.addPage(SourcesBrowser.class, R.string.page_label_sources);
 
         // set the adapter to display our homepage tab
         mPagerAdapter.setDisplayedPage(Constants.HOMEPAGE_TAB_ID);
     }
     
     public void setRooted(boolean root) {
     	mRooted = root;
     	SharedPreferences.Editor editor = mPreferences.edit();
     	editor.putBoolean(Constants.KEY_ROOTED, mRooted);
     	
     	// call to maintain compatibility with older SDK versions (pre-gingerbread)
     	SharedPreferencesCompat.apply(editor);
     }
 
     public static class PagerAdapter extends FragmentPagerAdapter
     	implements ViewPager.OnPageChangeListener, PagerHeader.OnHeaderClickListener {
 
     	 private final Context mContext;
          private final ViewPager mPager;
          private final PagerHeader mHeader;
          private final ArrayList<PageInfo> mPages = new ArrayList<PageInfo>();
          
          static final class PageInfo {
              private final Class<?> clss;
              private final Bundle args;
              
              PageInfo(Class<?> _clss, Bundle _args) {
                  clss = _clss;
                  args = _args;
              }
          }
 
          public PagerAdapter(FragmentActivity activity, ViewPager pager,
                  PagerHeader header) {
              super(activity.getSupportFragmentManager());
              mContext = activity;
              mPager = pager;
              mHeader = header;
              mHeader.setOnHeaderClickListener(this);
              mPager.setAdapter(this);
              mPager.setOnPageChangeListener(this);
          }
          
          public void setDisplayedPage(int index) {
 			mPager.setCurrentItem(index);
 		}
 
 		public void addPage(Class<?> clss, int res) {
              addPage(clss, null, res);
          }
          
          public void addPage(Class<?> clss, String title) {
              addPage(clss, null, title);
          }
          
          public void addPage(Class<?> clss, Bundle args, int res) {
              addPage(clss, null, mContext.getResources().getString(res));
          }
          
          public void addPage(Class<?> clss, Bundle args, String title) {
              PageInfo info = new PageInfo(clss, args);
              mPages.add(info);
              mHeader.add(0, title);
              notifyDataSetChanged();
          }
 
          @Override
          public int getCount() {
              return mPages.size();
          }
          
          @Override
          public Fragment getItem(int position) {
              PageInfo info = mPages.get(position);
              return Fragment.instantiate(mContext, info.clss.getName(), info.args);
          }
 
          @Override
          public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
              mHeader.setPosition(position, positionOffset, positionOffsetPixels);
          }
 
          @Override
          public void onPageSelected(int position) {
              mHeader.setDisplayedPage(position);
          }
 
          @Override
          public void onPageScrollStateChanged(int state) {
          }
 
          @Override
          public void onHeaderClicked(int position) {
              
          }
 
          @Override
          public void onHeaderSelected(int position) {
              mPager.setCurrentItem(position);
          }
     }
     
     public class RootCheck extends AsyncTask<Void, Void, Void> {
     	@Override
     	protected Void doInBackground(Void... args) {
     		if (Utils.isRooted())
     			setRooted(true);
     		else
     			setRooted(false);
     		return null;
     	}
     }
 }
