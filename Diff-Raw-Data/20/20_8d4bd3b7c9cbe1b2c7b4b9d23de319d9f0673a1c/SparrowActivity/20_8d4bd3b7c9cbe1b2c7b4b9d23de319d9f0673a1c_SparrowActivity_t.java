 package com.aquasoup.aquasparrow;
 
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.widget.TextView;
 
 import java.util.LinkedList;
 
 public class SparrowActivity extends Activity {
 
     private Context ctx = SparrowActivity.this;
     private TextView logTextView;
     private BroadcastReceiver LogsReceiver = new BroadcastReceiver() {
         @Override
         public void onReceive(Context context, Intent intent) {
             String log = intent.getStringExtra(SparrowConstants.STRING_ID);
             Logger.addLog(log);
             updateLogTextView();
         }
     };
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
 
         RegisterLogsReceiver();
 
         logTextView = (TextView) findViewById(R.id.log_text_view);
 
         updateLogTextView();
     }
 
     @Override
     public void onDestroy() {
         super.onDestroy();
         unregisterReceiver(LogsReceiver);
     }
 
     private void RegisterLogsReceiver() {
         IntentFilter filter = new IntentFilter();
         filter.addAction(SparrowConstants.ACTION_LOG);
         registerReceiver(LogsReceiver, filter);
     }
 
     private void updateLogTextView() {
         logTextView.setText(Logger.getLogs());
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.activity_main, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         Intent IntentService = new Intent(ctx, SparrowService.class);
 
         switch (item.getItemId()) {
 
             case R.id.start:
                 startService(IntentService);
                 break;
 
             case R.id.stop:
                 stopService(IntentService);
                 break;
 
         }
 
         return true;
     }
 
     private static class Logger {
         private static final int LOGGER_SIZE = 20;
         private static final LinkedList<String> logsList = new LinkedList<String>();
        private static final String newLine="\n";
 
         private static void addLog(String log) {
             int actualLoggerSize = logsList.size();
 
             if (isLoggerFull(actualLoggerSize)) {
                 logsList.removeFirst();
             }
 
             logsList.addLast(log);
         }
 
         private static boolean isLoggerFull(int actualLoggerSize) {
 
             return actualLoggerSize >= LOGGER_SIZE;
         }
 
         private static String getLogs() {
             String logs = "";
 
             for (String log : logsList) {
                logs = logs + log + newLine;
             }
 
             return logs;
         }
     }
 
 }
