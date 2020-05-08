 
 package com.twitter.android.yamba;
 
 import android.app.Activity;
import android.app.FragmentTransaction;
 import android.net.Uri;
 import android.os.Bundle;
 
 public class StatusDetailsActivity extends Activity {
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_status_details);
         Uri uri = super.getIntent().getData();
         StatusDetailsFragment statusDetailsFragment = StatusDetailsFragment.build(uri);
         super.getFragmentManager().beginTransaction()
                .replace(R.id.status_details, statusDetailsFragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commit();
     }
 }
