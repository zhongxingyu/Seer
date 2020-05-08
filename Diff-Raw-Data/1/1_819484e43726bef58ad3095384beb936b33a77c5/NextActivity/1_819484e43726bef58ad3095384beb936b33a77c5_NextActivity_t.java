 package com.deepmine.by;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 import com.deepmine.by.components.ViewBinderPlus;
 import com.deepmine.by.components.BaseActivity;
 import com.deepmine.by.helpers.ResourceHelper;
 import com.deepmine.by.services.DataService;
 
 public class NextActivity extends BaseActivity {
 
     ListView listView;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_playlist);
         listView = (ListView) findViewById(R.id.listNext);
         checkStatus(runnable);
         showLoading();
     }
 
     public void onClickList(View view) {
         startActivity(new Intent(getBaseContext(), MediaActivity.class));
         finish();
     }
 
     Runnable runnable = new Runnable() {
         public void run() {
             updateStatus();
         }
     };
 
     private void updateStatus() {
         if (DataService.getPlaylist()!=null) {
            hideLoading();
 
            SimpleAdapter simpleAdapter = new SimpleAdapter( getApplicationContext(),
                     DataService.getPlaylist().getSimpleAdapterList(),
                     R.layout.playlist_row,
                     getResources().getStringArray(R.array.playlist_names),
                     ResourceHelper.getInstance().getIntArray(R.array.playlist_ids)
            );
 
            simpleAdapter.setViewBinder(new ViewBinderPlus());
            listView.setAdapter(simpleAdapter);
            listView.setDividerHeight(0);
            simpleAdapter.notifyDataSetChanged();
           stopCheckStatus();
         }
     }
 
 
 }
