 package com.siu.android.andgapisutils.activity.tracked;
 
 import android.os.Bundle;
import android.view.Window;
 import com.actionbarsherlock.app.SherlockMapActivity;
 import com.google.android.apps.analytics.easytracking.EasyTracker;
import com.siu.android.andgapisutils.map.EnhancedMapView;
 
 /**
  * @author Lukasz Piliszczuk <lukasz.pili AT gmail.com>
  */
 public class TrackedSherlockMapActivity extends SherlockMapActivity {
 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setProgressBarIndeterminateVisibility(false);
 
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
