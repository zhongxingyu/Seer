 package com.siu.android.andgapisutils.activity.tracked;
 
 import android.os.Bundle;
 import com.actionbarsherlock.app.SherlockMapActivity;
import com.actionbarsherlock.view.Window;
 import com.google.android.apps.analytics.easytracking.EasyTracker;
 
 /**
  * @author Lukasz Piliszczuk <lukasz.pili AT gmail.com>
  */
 public class TrackedSherlockMapActivity extends SherlockMapActivity {
 
    protected void onCreate(Bundle savedInstanceState, int layout) {
         super.onCreate(savedInstanceState);
 
         requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(layout);
        setSupportProgressBarIndeterminateVisibility(false);
 
         EasyTracker.getTracker().setContext(this);
     }
 
     @Override
     protected void onStart() {
         super.onStart();
         EasyTracker.getTracker().trackActivityStart(this);
     }
 
     @Override
     public Object onRetainNonConfigurationInstance() {
         EasyTracker.getTracker().trackActivityRetainNonConfigurationInstance();
         return super.onRetainNonConfigurationInstance();
     }
 
     @Override
     protected void onStop() {
         super.onStop();
         EasyTracker.getTracker().trackActivityStop(this);
     }
 
     @Override
     protected boolean isRouteDisplayed() {
         return false;
     }
 }
