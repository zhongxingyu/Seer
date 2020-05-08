 package com.DGSD.TweeterTweeter.Activity.Phone;
 
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import com.DGSD.TweeterTweeter.Fragment.DashboardFragment;
 import com.DGSD.TweeterTweeter.Fragment.NewTweetFragment;
 import com.DGSD.TweeterTweeter.R;
 
 /**
  * Author: Daniel Grech
  * Date: 22/11/11 2:19 PM
  * Description :
  */
 public class DashboardActivity extends FragmentActivity {
     private static final String TAG = DashboardActivity.class.getSimpleName();
 
     private FragmentManager mFragmentManager;
 
     private DashboardFragment mDashboardFragment;
 
     private static final String DASHBOARD = "_dashboard";
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         //setContentView(R.layout.activity_dashboard);
 
        DashboardFragment f = DashboardFragment.newInstance();

         mFragmentManager = getSupportFragmentManager();
 
         if(savedInstanceState != null) {
             mDashboardFragment = (DashboardFragment) mFragmentManager.getFragment(savedInstanceState, DASHBOARD);
         }
 
         if(mDashboardFragment == null) {
             mDashboardFragment = DashboardFragment.newInstance();
        }
 
        getSupportFragmentManager().beginTransaction()
                                   .replace(android.R.id.content, f)
                                    .commit();
     }
 
     @Override
     public void onSaveInstanceState(Bundle out) {
         super.onSaveInstanceState(out);
 
         if(mDashboardFragment != null && mDashboardFragment.isAdded()) {
             mFragmentManager.putFragment(out, DASHBOARD, mDashboardFragment);
         }
     }
 }
