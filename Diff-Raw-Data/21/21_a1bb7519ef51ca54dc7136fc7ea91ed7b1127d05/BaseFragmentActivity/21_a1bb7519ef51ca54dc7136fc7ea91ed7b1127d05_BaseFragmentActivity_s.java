 package com.home.giraffe.base;
 
 import android.os.Bundle;
 import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockFragmentActivity;
 import com.google.inject.Inject;
 import com.home.giraffe.interfaces.ISettingsManager;
 import com.home.giraffe.interfaces.IUiManager;
 import de.greenrobot.event.EventBus;
 import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;
 
 public class BaseFragmentActivity extends RoboSherlockFragmentActivity {
     @Inject
     protected ISettingsManager mSettingsManager;
 
     @Inject
     protected IUiManager mUiManager;
 
     @Inject
     protected EventBus mBus;
 
     private PullToRefreshAttacher mPullToRefreshAttacher;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         mBus.register(this);
         mPullToRefreshAttacher = PullToRefreshAttacher.get(this);
     }
 
     public PullToRefreshAttacher getPullToRefreshAttacher() {
         return mPullToRefreshAttacher;
     }
 }
