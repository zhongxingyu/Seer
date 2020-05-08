 package com.gh.first;
 
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentTransaction;
 import android.view.View;
 
 public class MainActivity extends FragmentActivity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 	}
 
 	public void onSelectFragment(View view) {
 		Fragment page = new Fragment();
		if (view == findViewById(R.id.btnStartFrag)) {
 			page = new Page1();
		} else if (view == findViewById(R.id.btnFrag1)) {
 			page = new Page2();
		} else if (view == findViewById(R.id.btnFrag2)) {
 			page = new Page3();
 		}
 
 		FragmentTransaction tr = getSupportFragmentManager().beginTransaction()
 				.replace(R.id.fragment_placeholder, page);
 		tr.commit();
 
 	}
 }
