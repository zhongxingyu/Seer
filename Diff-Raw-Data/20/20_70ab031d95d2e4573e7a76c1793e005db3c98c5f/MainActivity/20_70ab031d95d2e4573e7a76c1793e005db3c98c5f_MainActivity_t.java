 package com.example.siteGuardian;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.res.Configuration;
 import android.database.Cursor;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 
 import java.util.Date;
 
 public class MainActivity extends Activity implements View.OnClickListener {
 
     private Button startStopButton;
     private Button updateButton;
     private TextView resultTextView;
     private Boolean isCheckerServiceStarted;
     private AsyncTask<Void, Void, String> updateTask;
     private Intent serviceIntent;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         isCheckerServiceStarted = false;
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         startStopButton = (Button) findViewById(R.id.startStopButton);
         resultTextView = (TextView) findViewById(R.id.siteCheckResult);
         updateButton = (Button) findViewById(R.id.updateButton);
         updateButton.setOnClickListener(this);
         startStopButton.setOnClickListener(this);
 
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         updateSettings();
         updateTask = new UpdateStatusTask();
         updateTask.execute();
     }
 
     private void updateSettings() {
         if (isCheckerServiceStarted == true) {
             stopService(serviceIntent);
             startService(serviceIntent);
         }
     }
 
     @Override
     protected void onPause() {
         super.onPause();
 //        updateTask.cancel(false);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);
         getMenuInflater().inflate(R.menu.menu, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         Integer id = item.getItemId();
         switch (id) {
             case R.id.settings:
                 Intent intent = new Intent(this, PrefActivity.class);
                 startActivity(intent);
                 return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
 
     }
 
     @Override
     public void onClick(View view) {
         if (view.getId() == R.id.startStopButton) {
             serviceIntent = new Intent(this, CheckSiteService.class);
             if (isCheckerServiceStarted == true) {
                 stopService(serviceIntent);
                 isCheckerServiceStarted = false;
                 startStopButton.setText(getString(R.string.start));
             } else {
                 startService(serviceIntent);
                 isCheckerServiceStarted = true;
                 startStopButton.setText(getString(R.string.stop));
             }
         } else {
             if(view.getId() == R.id.updateButton){
                 updateTask = new UpdateStatusTask();
                 updateTask.execute();
             }
         }
     }
 
     @Override
     public void onConfigurationChanged(Configuration newConfig) {
         super.onConfigurationChanged(newConfig);
     }
 
     private class UpdateStatusTask extends AsyncTask<Void, Void, String> {
         protected String doInBackground(Void... params) {
             StringBuilder sqlWhere = new StringBuilder();
             sqlWhere.append(SiteGuardSQLHelper.TIMESTAMP_COLUMN).append("=(SELECT MAX(");
             sqlWhere.append(SiteGuardSQLHelper.TIMESTAMP_COLUMN).append(") FROM ").append(SiteGuardSQLHelper.STATUS_TABLE_NAME).append(")");
             Cursor cursor = SharedObjectManager.getInstance().getDb(resultTextView.getContext()).query(SiteGuardSQLHelper.STATUS_TABLE_NAME,
                     null, sqlWhere.toString(), null, null, null, null);
             String resultStatus = getString(R.string.cant_be_determined);
            if (cursor != null ) {
                if(cursor.getCount() > 0){
                 cursor.moveToFirst();
                    resultStatus = "Status: " + cursor.getString(cursor.getColumnIndex(SiteGuardSQLHelper.RESULT_STATUS_COLUMN))
                            + " Time: " + new Date(Long.valueOf(cursor.getString(cursor.getColumnIndex(SiteGuardSQLHelper.TIMESTAMP_COLUMN))));
                }
                 cursor.close();
             }

             return resultStatus;
         }
 
         protected void onPostExecute(String result) {
             resultTextView.setText(result);
         }
     }
 }
