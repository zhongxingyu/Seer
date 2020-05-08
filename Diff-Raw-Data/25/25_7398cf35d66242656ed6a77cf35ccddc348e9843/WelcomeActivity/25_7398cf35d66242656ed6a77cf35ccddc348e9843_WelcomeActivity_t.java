 package net.mms_projects.copyit.ui.android;
 
 import net.mms_projects.copy_it.R;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 
 import com.actionbarsherlock.app.SherlockActivity;
 import com.google.analytics.tracking.android.EasyTracker;
 
 public class WelcomeActivity extends SherlockActivity {
	@Override
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
		Intent mainIntent = new Intent(this, MainActivity.class);
		mainIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
		startActivity(mainIntent);
 		Intent intent = new Intent(this, LoginActivity.class);
 		startActivity(intent);
 		finish();
 	}
 }
