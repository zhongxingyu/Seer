 package com.htb.cnk.lib;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.KeyEvent;
 
 import com.umeng.analytics.MobclickAgent;
 
 public class BaseActivity extends Activity {
	final static boolean enableUmeng = true;
 	
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 
 		if(keyCode == KeyEvent.KEYCODE_BACK){
 			finish();
 			return true;
 		} else {
 			return super.onKeyDown(keyCode, event);
 		}
 	}
 	
 	@Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         if (enableUmeng) {
         	MobclickAgent.onError(this);
         }
     }
 	
 	@Override
 	protected void onResume() {
 	    super.onResume();
 	    if (enableUmeng) {
 	    	MobclickAgent.onResume(this);
 	    }
 	}
 	
 	@Override
 	protected void onPause() {
 	    super.onPause();
 
 	    if (enableUmeng) {
 	    	MobclickAgent.onPause(this);
 	    }
 
 	}
 }
