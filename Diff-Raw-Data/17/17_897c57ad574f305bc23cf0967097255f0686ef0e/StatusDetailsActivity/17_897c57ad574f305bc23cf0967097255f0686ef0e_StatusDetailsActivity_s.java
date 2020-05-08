 
 package com.twitter.android.yamba;
 
 import android.app.Activity;
 import android.net.Uri;
 import android.os.Bundle;
 
 public class StatusDetailsActivity extends Activity {
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         Uri uri = super.getIntent().getData();
         StatusDetailsFragment statusDetailsFragment = StatusDetailsFragment.build(uri);
         super.getFragmentManager().beginTransaction()
                .add(android.R.id.content, statusDetailsFragment).commit();
     }

 }
