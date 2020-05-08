 package ca.idrc.tagin.app;
 
 import ca.idrc.tagin.app.util.LauncherDialog;
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.Menu;
 
 public class LauncherActivity extends Activity {
 	
 	private LauncherDialog mLauncherDialog;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		mLauncherDialog = new LauncherDialog(this);
 	}
 	
 	@Override
	protected void onPause() {
		super.onPause();
		mLauncherDialog.dismissDialog();
	}
	
	@Override
 	protected void onResume() {
 		super.onResume();
 		mLauncherDialog.showDialog();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.launcher, menu);
 		return true;
 	}
 
 }
