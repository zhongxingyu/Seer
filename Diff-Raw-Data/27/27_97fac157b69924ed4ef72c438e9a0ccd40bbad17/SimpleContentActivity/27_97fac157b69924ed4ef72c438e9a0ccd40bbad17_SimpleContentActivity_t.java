 package ru.ratadubna.dubnabus;
 
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 
 public class SimpleContentActivity extends SherlockFragmentActivity {
 	public static final String EXTRA_DATA = "data";
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
		setTheme(R.style.Theme_Sherlock_Light);
 		if (getSupportFragmentManager().findFragmentById(android.R.id.content) == null) {
 			String file = getIntent().getStringExtra(EXTRA_DATA);
 			Fragment f = SimpleContentFragment.newInstance(file);
 			getSupportFragmentManager().beginTransaction()
 					.add(android.R.id.content, f).commit();
 		}
 	}
 }
