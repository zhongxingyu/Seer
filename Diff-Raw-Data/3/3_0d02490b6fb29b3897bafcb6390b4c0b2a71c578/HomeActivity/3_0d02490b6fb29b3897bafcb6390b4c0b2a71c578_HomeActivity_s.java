 package com.goliathonline.android.greenstreetcrm.ui;
 
 import com.goliathonline.android.greenstreetcrm.R;
 
 import com.bugsense.trace.BugSenseHandler;
 import com.pushlink.android.PushLink;
 
 import android.app.Activity;
 import android.os.Bundle;
 
 /**
  * Front-door {@link Activity} that displays high-level features the schedule application offers to
  * users. Depending on whether the device is a phone or an Android 3.0+ tablet, different layouts
  * will be used. For example, on a phone, the primary content is a {@link DashboardFragment},
  * whereas on a tablet, both a {@link DashboardFragment} and a {@link TagStreamFragment} are
  * displayed.
  */
 public class HomeActivity extends BaseActivity {
 	private static final String TAG = "HomeActivity";
 	
 	private PushLink mPushLink;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.activity_home);
 
 		BugSenseHandler.setup(this, "fd8c0e92");
 		mPushLink = new PushLink(this, R.drawable.ic_launcher, 10, "63f9131513fa3991");
 
 		getActivityHelper().setupActionBar(null, 0);
 	}
 
 	@Override
     protected void onPostCreate(Bundle savedInstanceState) {
         super.onPostCreate(savedInstanceState);
         getActivityHelper().setupHomeActivity();
     }
 	
 	@Override
 	protected void onResume() {
 		mPushLink.start();
 	}
 	
 	@Override
 	protected void onPause() {
 		mPushLink.stop();
 	}
 }
