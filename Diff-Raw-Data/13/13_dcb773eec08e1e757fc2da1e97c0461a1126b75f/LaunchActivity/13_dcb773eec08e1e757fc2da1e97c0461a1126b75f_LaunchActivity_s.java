 package net.homeip.tedk.dashclocknotifications;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.pm.PackageManager;
 import android.os.Bundle;
 
 public class LaunchActivity extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		String packageName = getIntent().getData().getHost();

		PackageManager pm = getPackageManager();
		Intent intent = pm.getLaunchIntentForPackage(packageName);
		startActivity(intent);
 		
 		finish();
 	}
 	
 }
