 package net.mms_projects.copyit.ui.android;
 
 import net.mms_projects.copy_it.R;
import android.annotation.TargetApi;
 import android.content.Intent;
import android.os.Build;
 import android.os.Bundle;
import android.support.v4.app.NavUtils;
 import android.view.View;
 
 import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
 import com.google.analytics.tracking.android.EasyTracker;
 
 public class WelcomeActivity extends SherlockActivity {
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_welcome);
 		getSupportActionBar().setDisplayHomeAsUpEnabled(false);
 	}
 
 	@Override
 	protected void onStart() {
 		super.onStart();
		
 		EasyTracker.getInstance().activityStart(this);
 	}
	
 	@Override
 	protected void onStop() {
 		super.onStop();
		
 		EasyTracker.getInstance().activityStop(this);
 	}
	
 	public void doLogin(View view) {
 		Intent intent = new Intent(this, LoginActivity.class);
 		startActivity(intent);
 		finish();
 	}
 }
