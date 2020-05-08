 package com.intro.compintro;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.KeyEvent;
 import android.widget.Toast;
 
 public class MainActivity extends BaseActivity {
 
 	private Fragment mContent;
	private static Boolean isExit = false;
 
 	public MainActivity() {
 		super(R.string.hello_world);
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.content_frame);
 
 		// set the Above View
 		if (savedInstanceState != null)
 			mContent = getSupportFragmentManager().getFragment(
 					savedInstanceState, "mContent");
 		if (mContent == null)
 			mContent = new SampleListFragment();
 
 		getSupportFragmentManager().beginTransaction()
 				.replace(R.id.content_frame, mContent).commit();
 	}
 
 	@Override
 	public void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		getSupportFragmentManager().putFragment(outState, "mContent", mContent);
 	}
 
 	public void switchContent(Fragment fragment) {
 		mContent = fragment;
 		getSupportFragmentManager().beginTransaction()
 				.replace(R.id.content_frame, fragment).commit();
 		getSlidingMenu().showContent();
 	}
 
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		if (keyCode == KeyEvent.KEYCODE_BACK) {
			exitBy2Click();
 		}
		return super.onKeyDown(keyCode, event);
 	}
 
 	private void exitBy2Click() {
 		Timer tExit = null;
 		if (isExit) {
 			finish();
 			System.exit(0);
 		} else {
 			isExit = true;
 			Toast.makeText(this, R.string.clickAgain, Toast.LENGTH_SHORT)
 					.show();
 			tExit = new Timer();
 			tExit.schedule(new TimerTask() {
 
 				@Override
 				public void run() {
 					isExit = false;
 				}
 			}, 2000);
 		}
 	}
 
 }
