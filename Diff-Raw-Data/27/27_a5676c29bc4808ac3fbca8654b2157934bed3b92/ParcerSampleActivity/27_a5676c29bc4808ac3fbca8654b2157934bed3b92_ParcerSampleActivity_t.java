 package com.argando.parcersample;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentTransaction;
 import android.util.Log;
 import android.view.GestureDetector;
 import android.view.MotionEvent;
 import android.view.View;
 import android.widget.Button;
 import android.widget.RelativeLayout;
 import android.widget.Toast;
 import com.argando.parcersample.backgroundupdate.ParceService;
 import com.argando.parcersample.data.DataNameHelper;
 import com.argando.parcersample.data.LeaguesHandler;
 import com.argando.parcersample.instruments.GestureDetectorImpl;
 import com.argando.parcersample.scoretable.MatchListFragment;
 import com.argando.parcersample.scoretable.MenuFragment;
 import com.argando.parcersample.scoretable.ScoreFragment;
 
 import java.io.File;
 
 public class ParcerSampleActivity extends FragmentActivity implements IFragmentTransaction {
     private static final String LOG_TAG = ParcerSampleActivity.class.getSimpleName();
     private Toast mInternetConnectionToast;
     private Activity mActivity;
     MatchListFragment mMatchFragment;
     private GestureDetector mGestureScanner;
     private MenuFragment mMenuFragment;
     private View gestureView;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         Log.i(LOG_TAG, "onCreate");
         mActivity = this;
         setContentView(R.layout.main);
         Log.w(LOG_TAG, " = " + mActivity.getCacheDir().toString());
         DataNameHelper.EXTERNAL_CACHE_DIR = mActivity.getCacheDir().toString();
         LeaguesHandler.mListLeauges = Cache.INSTANCE.readFromFile(new File(DataNameHelper.EXTERNAL_CACHE_DIR));
         resetUpdateService(500);
         mGestureScanner = new GestureDetector(this, new GestureDetectorImpl(this));
         initUIComponents();

        if(savedInstanceState == null)
        {
            showResultsList();
        }
        else
        {
            mMatchFragment = (MatchListFragment)getSupportFragmentManager().findFragmentByTag("MatchesFragment");
        }

     }
 
     @Override
     public boolean onTouchEvent(MotionEvent event) {
         Log.i(LOG_TAG, "onTouchEvent activity");
          if(mGestureScanner.onTouchEvent(event))
          {
              return true;
          }
          else
          {
              return super.onTouchEvent(event);
          }
     }
 
 
     private void initUIComponents() {
         mInternetConnectionToast = Toast.makeText(getBaseContext(), DataNameHelper.NO_INTERNET_CONNECTION, DataNameHelper.NO_INTERNER_CONNNECTION_TOAST_TIME);
     }
 
     @Override
     public void onResume() {
         super.onResume();
     }
 
     private void resetUpdateService(int time) {
         Intent parseService = new Intent(this, ParceService.class);
         stopService(parseService);
         parseService.putExtra("time", time);
         startService(parseService);
     }
 
     private void showResultsList() {
         Log.i(LOG_TAG, "showResultsList");
         FragmentManager fragmentManager = getSupportFragmentManager();
         if (mMatchFragment != null) {
            //mMatchFragment.onAttach(this);
             if (mMatchFragment.isVisible()) {
                 return;
             }
             if (!mMatchFragment.isInLayout()) {
                 FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                 fragmentTransaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                fragmentTransaction.replace(R.id.container, mMatchFragment, "MatchesFragment");
                mMatchFragment.setRetainInstance(true);
                 fragmentTransaction.commit();
                 mMatchFragment.updateData();
                 return;
             }
         }
         mMatchFragment = new MatchListFragment();
         FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.container, mMatchFragment, "MatchesFragment");
        mMatchFragment.setRetainInstance(true);
         fragmentTransaction.commit();
     }
 
     @Override
     protected void onDestroy() {
         Log.i(LOG_TAG, "onDestroy");
         super.onDestroy();
     }
 
     public void onSelectLeague(View view) {
         Button button = (Button) view;
         LeaguesHandler.match = button.getTag().toString();
         ScoreFragment fragment = new ScoreFragment();
         FragmentManager fragmentManager = this.getSupportFragmentManager();
         FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
         fragmentTransaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.slide_in_left, android.R.anim.slide_out_right);
         if (fragment.isAdded()) {
             fragmentTransaction.show(fragment);
         } else {
             fragmentTransaction.remove(fragmentManager.findFragmentById(R.id.container));
             fragmentTransaction.add(R.id.container2, fragment, "browser");
             fragmentTransaction.addToBackStack(null);
         }
         fragmentTransaction.commit();
     }
 
     @Override
     public void launchMatchFragment() {
         Log.i(LOG_TAG, "launchMatchFragment");
         showResultsList();
     }
 
     @Override
     public void launchMenuFragment() {
         Log.i(LOG_TAG, "launchMenuFragment");
         FragmentManager fragmentManager = getSupportFragmentManager();
         if (mMenuFragment != null) {
             mMenuFragment.onAttach(this);
             if (mMenuFragment.isVisible()) {
                 return;
             }
             if (!mMenuFragment.isInLayout()) {
                 FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                 fragmentTransaction.replace(R.id.container, mMenuFragment);
                mMenuFragment.setRetainInstance(true);
                 fragmentTransaction.commit();
                 return;
             }
         }
         mMenuFragment = new MenuFragment();
         FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
         fragmentTransaction.replace(R.id.container, mMenuFragment);
        mMenuFragment.setRetainInstance(true);
         fragmentTransaction.commit();
     }
 }
