 package tk.crazysoft.ego.services;
 
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.content.WakefulBroadcastReceiver;
 import android.util.Pair;
 import android.view.View;
 import android.widget.ProgressBar;
 import android.widget.Toast;
 
 import java.util.LinkedList;
 import java.util.Queue;
 
 import tk.crazysoft.ego.R;
 import tk.crazysoft.ego.data.AddressImporter;
 import tk.crazysoft.ego.data.StandbyImporter;
 
 public class DataImportReceiver extends WakefulBroadcastReceiver {
     private Queue<Pair<String, ProgressBar>> progressBars;
 
     public DataImportReceiver(Bundle savedInstanceState) {
         super();
         progressBars = new LinkedList<Pair<String, ProgressBar>>();
         if (savedInstanceState != null) {
             LinkedList<Pair<String, ProgressBar>> list = (LinkedList<Pair<String, ProgressBar>>)progressBars;
             String[] actions = savedInstanceState.getStringArray("progressActions");
             for (String action : actions) {
                 list.add(new Pair<String, ProgressBar>(action, null));
             }
         }
     }
 
     @Override
     public void onReceive(Context context, Intent intent) {
         ProgressBar progressBar = null;
         if (progressBars.size() > 0) {
             progressBar = progressBars.peek().second;
         }
 
         String action = intent.getAction();
         if (action == null) {
             return;
         }
 
         Context appContext = context.getApplicationContext();
         if (action.equals(DataImportService.BROADCAST_ERROR)) {
             String error = intent.getStringExtra(DataImportService.EXTRA_ERROR_MESSAGE);
             Toast.makeText(appContext, error, Toast.LENGTH_LONG).show();
             hideProgressBar(progressBar);
         } else if (action.equals(DataImportService.BROADCAST_PROGRESS)) {
             double progressPercent = intent.getDoubleExtra(DataImportService.EXTRA_PROGRESS_PERCENT, 0);
             setProgress(progressBar, progressPercent);
         } else if (action.equals(DataImportService.BROADCAST_RESULT_IMPORT)) {
             hideProgressBar(progressBar);
 
             int[] counts = intent.getIntArrayExtra(DataImportService.EXTRA_RESULT_COUNTS);
             if (counts == null || counts.length != 2) {
                 return;
             }
             String message = String.format((String)context.getResources().getText(R.string.service_dataimport_result_import), counts[0], counts[1]);
             Toast.makeText(appContext, message, Toast.LENGTH_LONG).show();
         } else if (action.equals(DataImportService.BROADCAST_RESULT_POSTPROCESS)) {
             hideProgressBar(progressBar);
 
             int[] counts = intent.getIntArrayExtra(DataImportService.EXTRA_RESULT_COUNTS);
             boolean result = intent.getBooleanExtra(DataImportService.EXTRA_RESULT_RESULT, false);
 
             String actionResult = intent.getStringExtra(DataImportService.EXTRA_RESULT_ACTION);
             if (actionResult != null && actionResult.equals(AddressImporter.ADDRESS_IMPORTER_POSTPOCESS_ACTION)) {
                 String message = String.format((String)context.getResources().getText(R.string.service_dataimport_result_merge), counts[0], counts[1]);
                 Toast.makeText(appContext, message, Toast.LENGTH_LONG).show();
             } else if (actionResult != null && actionResult.equals(StandbyImporter.STANDBY_IMPORTER_POSTPOCESS_ACTION) && !result) {
                 String message = context.getString(R.string.service_dataimport_result_db_write_fail);
                 Toast.makeText(appContext, message, Toast.LENGTH_LONG).show();
             }
        } else if (action.equals(DataImportService.BROADCAST_COMPLETED) && progressBars.size() > 0) {
             progressBars.remove();
         }
     }
 
     public void registerProgressBar(String action, ProgressBar progressBar) {
         LinkedList<Pair<String, ProgressBar>> list = (LinkedList<Pair<String, ProgressBar>>)progressBars;
         for (int i = 0; i < list.size(); i++) {
             if (list.get(i).first.equals(action)) {
                 list.set(i, new Pair<String, ProgressBar>(list.get(i).first, progressBar));
             }
         }
     }
 
     public boolean startServiceIntent(Context context, String action, ProgressBar progressBar) {
         if (indexOf(action) >= 0) {
             return false;
         }
 
         // Start the service, keeping the device awake while the service is
         // launching. This is the Intent to deliver to the service.
         Intent importIntent = new Intent(context, DataImportService.class);
         importIntent.setAction(action);
         progressBars.add(new Pair<String, ProgressBar>(action, progressBar));
         startWakefulService(context, importIntent);
         return true;
     }
 
     public void onSaveInstanceState(Bundle outState) {
         LinkedList<Pair<String, ProgressBar>> list = (LinkedList<Pair<String, ProgressBar>>)progressBars;
         String[] actions = new String[list.size()];
         for (int i = 0; i < list.size(); i++) {
             actions[i] = list.get(i).first;
         }
         outState.putStringArray("progressActions", actions);
     }
 
     private int indexOf(String action) {
         int i = 0;
         for (Pair<String, ProgressBar> pair : progressBars) {
             if (pair.first.equals(action)) {
                 return i;
             }
             i++;
         }
         return -1;
     }
 
     private void hideProgressBar(ProgressBar progressBar) {
         if (progressBar != null) {
             progressBar.setVisibility(View.GONE);
         }
     }
 
     private void setProgress(ProgressBar progressBar, double progressPercent) {
         if (progressBar != null) {
             progressBar.setVisibility(View.VISIBLE);
             progressBar.setProgress((int)(progressPercent * progressBar.getMax()));
         }
     }
 }
