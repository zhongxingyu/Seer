 package com.typeiisoft.lct;
 
 import android.app.ActionBar.Tab;
 import android.app.ActionBar.TabListener;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.util.Log;
 
 /**
  * This class handles paging control of the main program tabs. These tabs are 
  * associated with the program's ActionBar. 
  * 
  * @author Michael Reuter
  *
  * @param <T> : Class parameter that inherits from Fragment.
  */
 public final class MainTabListener<T extends Fragment> implements TabListener {
 	/** Logging identifier. */
     private final static String TAG = MainTabListener.class.getName();
     /** Holder for a given fragment. */
 	private Fragment mFragment;
 	/** Holder for the fragment activity. */
     private final FragmentActivity mActivity;
     /** Tag that identifies the given fragment. */
     private final String mTag;
     /** */
     private final Class<T> mClass;
 
     /** 
      * Constructor used each time a new tab is created.
      * @param activity : The host Activity, used to instantiate the fragment
      * @param tag : The identifier tag for the fragment
      * @param clz : The fragment's Class, used to instantiate the fragment
      */
    public MainTabListener(FragmentActivity activity, String tag, Class<T> clz) {
        mActivity = activity;
        mTag = tag;
        mClass = clz;
       
       // Check to see if we already have a fragment for this tab, probably
       // from a previously saved state.  If so, deactivate it, because our
       // initial state is that a tab isn't shown.
       mFragment = mActivity.getSupportFragmentManager().findFragmentByTag(mTag);
       if (mFragment != null && !mFragment.isDetached()) {
           android.support.v4.app.FragmentTransaction ft = mActivity.getSupportFragmentManager().beginTransaction();
           ft.detach(mFragment);
           ft.commit();
       }
    }
 
    // The following are each of the ActionBar.TabListener callbacks.
 
    /**
     * This function handles when the tab is reselected. Does nothing.
     * @param tab : The current tab object.
     * @param ft : A fragment transactions (unused).
     */
    @Override
    public void onTabReselected(Tab tab, android.app.FragmentTransaction ft) {
 	   // User selected the already selected tab. Usually do nothing.
 	   Log.i(TAG, "Running onTabReselected");
    }
 
    /**
     * This function handles when the tab is selected.
     * @param tab : The current tab object.
     * @param ft : A fragment transactions (unused).
     */
    @Override
    public void onTabSelected(Tab tab, android.app.FragmentTransaction ft) {
 	   android.support.v4.app.FragmentTransaction fft = mActivity.getSupportFragmentManager().beginTransaction();
 	   Log.i(TAG, "Running onTabSelected");
 	   // Check if the fragment is already initialized
 	   if (mFragment == null) {
 		   // If not, instantiate and add it to the activity
 		   mFragment = Fragment.instantiate(mActivity, mClass.getName());
 		   Log.i(TAG, "Adding fragment: " + mFragment.getTag());
 		   fft.add(android.R.id.content, mFragment, mTag);
 	   } else {
 		   // If it exists, simply attach it in order to show it
 		   Log.i(TAG, "Attaching fragment: " + mFragment.getTag());
 		   fft.attach(mFragment);
 	   }
 	   fft.commit();
    }
 
    /**
     * This function handles when the tab is unselected (hidden). 
     * @param tab : The current tab object.
     * @param ft : A fragment transactions (unused).
     */
    @Override
    public void onTabUnselected(Tab tab, android.app.FragmentTransaction ft) {
 	   android.support.v4.app.FragmentTransaction fft = mActivity.getSupportFragmentManager().beginTransaction();
 	   Log.i(TAG, "Running onTabUnselected");
 	   if (mFragment != null) {
 		   // Detach the fragment, because another one is being attached
 		   Log.i(TAG, "Detaching fragment");
 		   fft.detach(mFragment);
 	   }
 	   fft.commit();
    }
 }
