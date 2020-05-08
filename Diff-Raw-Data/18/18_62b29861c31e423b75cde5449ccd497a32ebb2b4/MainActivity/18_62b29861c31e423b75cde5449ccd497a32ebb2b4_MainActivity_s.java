 package com.tihonchik.lenonhonor360.ui.user;
 
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 
import com.tihonchik.lenonhonor360.services.ResponseReceiver;
 import com.tihonchik.lenonhonor360.ui.BaseActivity;
 import com.tihonchik.lenonhonor360.R;
 
 public class MainActivity extends BaseActivity {
 
	private ResponseReceiver receiver;
 	
 	@Override
 	protected Context getContextforBase() {
 		return MainActivity.this;
 	}
 
 	@Override
 	public void onBackPressed() {
 		super.onBackPressed();
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		getWindow().setBackgroundDrawable(null);
 
 		if (savedInstanceState == null) {
 			Fragment f = new BlogDisplayFragment();
 			getSupportFragmentManager().beginTransaction()
 					.replace(R.id.body, f, "BlogDisplayFragment").commit();
 		}
 		
        IntentFilter filter = new IntentFilter(ResponseReceiver.ACTION_RESP);
         filter.addCategory(Intent.CATEGORY_DEFAULT);
        receiver = new ResponseReceiver();
         registerReceiver(receiver, filter);
 	}
 
 	@Override
 	public void onPause() {
 		super.onPause();
 	}
 }
