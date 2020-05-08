 
 package com.fanoyong.aboutphone.ui;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.util.Log;
 
import com.fanoyong.aboutphone.R;

 public class MainActivity extends Activity {
 
     private static final String TAG = "AP|MainActivity|";
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         Log.v(TAG + "onCreate|", "start");
         this.setContentView(R.layout.activity_twopane);
         replaceFragment();
         Log.v(TAG + "onCreate|", "end");
     }
 
     private void replaceFragment() {
         Log.v(TAG + "replaceFragment|", "start");
         ListFragment mListFragment = (ListFragment) this.getFragmentManager().findFragmentById(R.id.list_fragment);
         DetailFragment mDetailFragment = (DetailFragment) this.getFragmentManager().findFragmentById(R.id.detail_fragment);
         this.getFragmentManager().beginTransaction().replace(R.id.list_fragment, mListFragment).commit();
         this.getFragmentManager().beginTransaction().replace(R.id.detail_fragment, mDetailFragment).commit();
         Log.v(TAG + "replaceFragment|", "end");
     }
 
     @Override
     protected void onPause() {
         super.onPause();
         Log.v(TAG + "onPause|", "start");
         Log.v(TAG + "onPause|", "end");
     }
 
 }
