 package com.kii.push;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 
 public class BucketPushActivity extends FragmentActivity implements
         OnItemClickListener {
 
     @Override
     protected void onCreate(Bundle arg0) {
         super.onCreate(arg0);
         setContentView(R.layout.bucket_push_list);
         ListView listView = (ListView) findViewById(R.id.bucketListView);
         listView.setOnItemClickListener(this);
     }
 
     @Override
     public void onItemClick(AdapterView<?> parent, View v, int pos, long id) {
         if (pos == 0) {
             Intent intent = new Intent(this, BucketControlActivity.class);
             startActivity(intent);
         } else if (pos == 1) {
             new KiiPushAppTask(KiiPushAppTask.MENU_ID.SUBSCRIBE_BUCKET,
                     getString(R.string.subscribe), this).execute();
         } else if (pos == 2) {
             new KiiPushAppTask(KiiPushAppTask.MENU_ID.UNSUBSCRIBE_BUCKET,
                     getString(R.string.unsubscribe), this).execute();
         } else if (pos == 3) {
             new KiiPushAppTask(KiiPushAppTask.MENU_ID.SUBSCRIBE_ABUCKET,
                     getString(R.string.subscribe_ascope_bucket), this)
                     .execute();
        } else {
            throw new RuntimeException("Unknown error!");
         }
     }
 
 }
