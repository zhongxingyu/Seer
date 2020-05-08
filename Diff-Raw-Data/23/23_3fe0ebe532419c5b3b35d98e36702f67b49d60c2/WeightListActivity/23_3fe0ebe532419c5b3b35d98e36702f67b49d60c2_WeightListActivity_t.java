 package com.thoughtworks.healthgraphexplorer;
 
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.MenuItem;
 import android.widget.TextView;
 
 import com.thoughtworks.healthgraphexplorer.hgclient.HgClient;
 
 import roboguice.activity.RoboActivity;
 import roboguice.inject.InjectView;
 
 public class WeightListActivity extends RoboActivity {
 
     @InjectView(R.id.WeightListTextView)
     private TextView textView;
 
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_weightlist);
         getActionBar().setDisplayHomeAsUpEnabled(true);
     }
 
     @Override
     protected void onResume() {
         super.onResume();

        new AsyncTask<Void,Void,String>() {
            @Override
            protected String doInBackground(Void... params) {
                return getHgClient().getWeightList();
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                textView.setText(s);
            }
        }.execute();
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case android.R.id.home:
                 finish();
                 return true;
         }
         return super.onOptionsItemSelected(item);
     }
 
     private HgClient getHgClient() {
         MyApplication application = (MyApplication) getApplication();
         return application.getHgClient();
     }
 }
