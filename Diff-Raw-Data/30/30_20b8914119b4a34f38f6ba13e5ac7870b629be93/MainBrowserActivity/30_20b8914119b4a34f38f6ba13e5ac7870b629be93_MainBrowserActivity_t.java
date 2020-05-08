 /*
  * Copyright (C) 2009 University of Washington
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
  * in compliance with the License. You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software distributed under the License
  * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
  * or implied. See the License for the specific language governing permissions and limitations under
  * the License.
  */
 
 package com.radicaldynamic.turboform.activities;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 import android.app.AlertDialog;
 import android.app.ListActivity;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.graphics.Color;
 import android.graphics.drawable.BitmapDrawable;
 import android.graphics.drawable.Drawable;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.Gravity;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.Window;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.FrameLayout;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.RelativeLayout;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ImageView.ScaleType;
 
 import com.radicaldynamic.turboform.R;
 import com.radicaldynamic.turboform.adapters.BrowserListAdapter;
 import com.radicaldynamic.turboform.application.Collect;
 import com.radicaldynamic.turboform.documents.FormDocument;
 import com.radicaldynamic.turboform.documents.InstanceDocument;
 import com.radicaldynamic.turboform.preferences.GeneralPreferences;
 import com.radicaldynamic.turboform.repository.FormRepository;
 import com.radicaldynamic.turboform.repository.InstanceRepository;
 import com.radicaldynamic.turboform.services.CouchDbService;
 import com.radicaldynamic.turboform.tasks.InitializeApplicationTask;
 import com.radicaldynamic.turboform.utilities.DocumentUtils;
 import com.radicaldynamic.turboform.utilities.FileUtils;
 
 /**
  * Responsible for displaying buttons to launch the major activities. Launches
  * some activities based on returns of others.
  * 
  * @author Carl Hartung (carlhartung@gmail.com)
  * @author Yaw Anokwa (yanokwa@gmail.com)
  */
 public class MainBrowserActivity extends ListActivity
 {
     private static final String t = "MainBrowserActivity: ";
 
     private static boolean mShowSplash = true;
 
     private AlertDialog mAlertDialog;
     private RefreshViewTask mRefreshViewTask;
 
     private ServiceConnection mConnection = new ServiceConnection() {
         public void onServiceConnected(ComponentName className, IBinder service)
         {
             Collect.mDb = ((CouchDbService.LocalBinder) service).getService();
             Collect.mDb.open();
             loadScreen();
         }
 
         public void onServiceDisconnected(ComponentName className)
         {
             Log.d(Collect.LOGTAG, t + "TFCouchDbService unbound");
         }
     };
 
     @Override
     protected void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
 
         // If sd card error, quit
         if (!FileUtils.storageReady()) {
             createErrorDialog(getString(R.string.no_sd_error), true);
         }
 
         displaySplash();
 
         requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
         setContentView(R.layout.main_browser);
         setTitle(getString(R.string.app_name));
         setProgressBarIndeterminateVisibility(false);
 
         startService(new Intent(this, CouchDbService.class));
         bindService(new Intent(this, CouchDbService.class), mConnection, Context.BIND_AUTO_CREATE);
         
         // We don't use the on-screen progress indicator here
         RelativeLayout onscreenProgress = (RelativeLayout) findViewById(R.id.progress);
         onscreenProgress.setVisibility(View.GONE);
 
         // Perform any needed application initialization
         new InitializeApplicationTask().execute();
 
         /*
          * Initiate and populate spinner to filter forms displayed by instances
          * types
          */
         Spinner s1 = (Spinner) findViewById(R.id.form_filter);
         ArrayAdapter<CharSequence> instanceStatus = ArrayAdapter
                 .createFromResource(this, R.array.tf_main_menu_form_filters,
                         android.R.layout.simple_spinner_item);
         instanceStatus
                 .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         s1.setAdapter(instanceStatus);
         s1.setOnItemSelectedListener(new OnItemSelectedListener() {
             public void onItemSelected(AdapterView<?> parent, View view,
                     int position, long id)
             {
                 triggerRefresh(position);
             }
 
             public void onNothingSelected(AdapterView<?> parent)
             {
             }
         });
     }
 
     @Override
     protected void onDestroy()
     {
         unbindService(mConnection);
         super.onDestroy();
     }
 
     @Override
     protected void onPause()
     {
         dismissDialogs();
         super.onPause();
     }
 
     @Override
     protected void onResume()
     {
         super.onResume();
 
         if (Collect.mDb != null)
             loadScreen();
     }
 
     /**
      * onStop Re-enable the splash screen.
      */
     @Override
     protected void onStop()
     {
         super.onStop();
         mShowSplash = true;
     }
 
     @Override
     public void onCreateContextMenu(ContextMenu menu, View v,
             ContextMenuInfo menuInfo)
     {
         super.onCreateContextMenu(menu, v, menuInfo);
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.main_browser_context, menu);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu)
     {
         super.onCreateOptionsMenu(menu);
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.main_browser_options, menu);
         return true;
     }
 
     @Override
     public boolean onContextItemSelected(MenuItem item)
     {
         // AdapterContextMenuInfo info = (AdapterContextMenuInfo)
         // item.getMenuInfo();
         switch (item.getItemId()) {
         default:
             return super.onContextItemSelected(item);
         }
     }
 
     /**
      * Stores the path of selected form and finishes.
      */
     @Override
     protected void onListItemClick(ListView listView, View view, int position,
             long id)
     {
         FormDocument form = (FormDocument) getListAdapter().getItem(position);
         InstanceLoadPathTask ilp;
 
         Log.d(Collect.LOGTAG, t + "selected form " + form.getId() + " from list");
 
         Spinner s1 = (Spinner) findViewById(R.id.form_filter);
 
         switch (s1.getSelectedItemPosition()) {
         // Show all forms (in group)
         case 0:
             Intent i = new Intent("com.radicaldynamic.turboform.action.FormEntry");
             i.putStringArrayListExtra(FormEntryActivity.KEY_INSTANCES, new ArrayList<String>());
             i.putExtra(FormEntryActivity.KEY_FORMID, form.getId());
             startActivity(i);
             break;
         // Show all incomplete forms
         case 1:
             ilp = new InstanceLoadPathTask();
             ilp.execute(form.getId(), InstanceDocument.Status.incomplete);
             break;
         // Show all completed forms
         case 2:
             ilp = new InstanceLoadPathTask();
             ilp.execute(form.getId(), InstanceDocument.Status.complete);
             break;
         // Show all unread forms (e.g., those added or updated by others)
         case 3:
             break;
         }
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item)
     {
         Intent i = null;
 
         switch (item.getItemId()) {
         case R.id.tf_synchronize:
             i = new Intent(this, SynchronizeTabs.class);
             startActivity(i);
             return true;
         case R.id.tf_manage:
             i = new Intent(this, ManageFormsTabs.class);
             startActivity(i);
             return true;
         case R.id.tf_preferences:
             i = new Intent(this, GeneralPreferences.class);
             startActivity(i);
             return true;
         }
 
         return super.onOptionsItemSelected(item);
     }
 
     /*
      * Determine how to load a form instance
      * 
      * If there is only one instance for the form in question then load that
      * instance directly. If there is more than one instance then load the
      * instance browser.
      */
     private class InstanceLoadPathTask extends
             AsyncTask<Object, Integer, Void>
     {
         String mFormId;
         ArrayList<String> mInstanceIds = new ArrayList<String>();
 
         @Override
         protected Void doInBackground(Object... params)
         {
             mFormId = (String) params[0];
             InstanceDocument.Status status = (InstanceDocument.Status) params[1];
 
             mInstanceIds = new InstanceRepository(Collect.mDb.getDb())
                     .findByFormAndStatus(mFormId, status);
             
             return null;
         }
 
         @Override
         protected void onPreExecute()
         {
             setProgressBarIndeterminateVisibility(true);
         }
 
         @Override
         protected void onPostExecute(Void nothing)
         {
             Intent i = new Intent("com.radicaldynamic.turboform.action.FormEntry");
             i.putStringArrayListExtra(FormEntryActivity.KEY_INSTANCES, mInstanceIds);
             i.putExtra(FormEntryActivity.KEY_INSTANCEID, mInstanceIds.get(0));
             i.putExtra(FormEntryActivity.KEY_FORMID, mFormId);            
             startActivity(i);
 
             setProgressBarIndeterminateVisibility(false);
         }
     }
 
     /*
      * Refresh the main form browser view as requested by the user
      */
     private class RefreshViewTask
             extends
             AsyncTask<InstanceDocument.Status, Integer, InstanceDocument.Status>
     {
         private ArrayList<FormDocument> documents = new ArrayList<FormDocument>();
         private Map<String, String> instanceTallies = new HashMap<String, String>();
 
         @Override
         protected InstanceDocument.Status doInBackground(InstanceDocument.Status... status)
         {
             if (status[0] == InstanceDocument.Status.nothing) {
                 try {
                     documents = (ArrayList<FormDocument>) new FormRepository(Collect.mDb.getDb()).getAll();
                     DocumentUtils.sortByName(documents);
                 } catch (ClassCastException e) {
                     // TODO: is there a better way to handle empty lists?
                 }
             } else {
                 instanceTallies = new FormRepository(Collect.mDb.getDb()).getFormsByInstanceStatus(status[0]);
                 
                 if (!instanceTallies.isEmpty()) {
                     documents = (ArrayList<FormDocument>) new FormRepository(Collect.mDb.getDb()).getAllByKeys(new ArrayList<Object>(instanceTallies.keySet()));
                     
                     DocumentUtils.sortByName(documents);
                 }
             }
 
             return status[0];
         }
 
         @Override
         protected void onPreExecute()
         {
             setProgressBarIndeterminateVisibility(true);
         }
 
         @Override
         protected void onPostExecute(InstanceDocument.Status status)
         {
             BrowserListAdapter adapter;
             adapter = new BrowserListAdapter(getApplicationContext(),
                     R.layout.main_browser_list_item, documents,
                     instanceTallies, (Spinner) findViewById(R.id.form_filter));
             setListAdapter(adapter);
 
             if (status == InstanceDocument.Status.nothing) {
                 // Provide hints to user
                 if (documents.isEmpty()) {
                     TextView nothingToDisplay = (TextView) findViewById(R.id.nothingToDisplay);
                     nothingToDisplay.setVisibility(View.VISIBLE);
                     
                     Toast.makeText(getApplicationContext(),
                             getString(R.string.tf_add_form_hint),
                             Toast.LENGTH_LONG).show();
                     openOptionsMenu();
                 } else {
                     Toast.makeText(getApplicationContext(),
                             getString(R.string.tf_begin_instance_hint),
                             Toast.LENGTH_SHORT).show();
                 }
             } else {
                 Spinner s1 = (Spinner) findViewById(R.id.form_filter);
                 String descriptor = s1.getSelectedItem().toString()
                         .toLowerCase();
 
                 // Provide hints to user
                 if (documents.isEmpty()) {
                     TextView nothingToDisplay = (TextView) findViewById(R.id.nothingToDisplay);
                     nothingToDisplay.setVisibility(View.VISIBLE);
                 } else {
                     Toast.makeText(
                             getApplicationContext(),
                             getString(R.string.tf_browse_instances_hint,
                                     descriptor), Toast.LENGTH_SHORT).show();
                 }
             }
 
             setProgressBarIndeterminateVisibility(false);
         }
     }
 
     private void createErrorDialog(String errorMsg, final boolean shouldExit)
     {
         mAlertDialog = new AlertDialog.Builder(this).create();
         mAlertDialog.setIcon(R.drawable.ic_dialog_info);
         mAlertDialog.setMessage(errorMsg);
 
         DialogInterface.OnClickListener errorListener = new DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface dialog, int i)
             {
                 switch (i) {
                 case DialogInterface.BUTTON1:
                     if (shouldExit) {
                         finish();
                     }
                     break;
                 }
             }
         };
 
         mAlertDialog.setCancelable(false);
         mAlertDialog.setButton(getString(R.string.ok), errorListener);
         mAlertDialog.show();
     }
 
     /**
      * Dismiss any showing dialogs that we manage.
      */
     private void dismissDialogs()
     {
         if (mAlertDialog != null && mAlertDialog.isShowing()) {
             mAlertDialog.dismiss();
         }
     }
 
     /**
      * displaySplash
      * 
      * Shows the splash screen if the mShowSplash member variable is true.
      * Otherwise a no-op.
      */
     void displaySplash()
     {
         if (!mShowSplash)
             return;
 
         // Fetch the splash screen Drawable
         Drawable image = null;
 
         try {
             // Attempt to load the configured default splash screen
             // The following code only works in 1.6+
             // BitmapDrawable bitImage = new BitmapDrawable(getResources(), FileUtils.SPLASH_SCREEN_FILE_PATH);
             BitmapDrawable bitImage = new BitmapDrawable(FileUtils.SPLASH_SCREEN_FILE_PATH);
 
             if (bitImage.getBitmap() != null
                     && bitImage.getIntrinsicHeight() > 0
                     && bitImage.getIntrinsicWidth() > 0) {
                 image = bitImage;
             }
         } catch (Exception e) {
             // TODO: log exception for debugging?
         }
 
         if (image == null) {
             // no splash provided, so do nothing...
             return;
         }
 
         // Create ImageView to hold the Drawable...
         ImageView view = new ImageView(getApplicationContext());
 
         // Initialise it with Drawable and full-screen layout parameters
         view.setImageDrawable(image);
         int width = getWindowManager().getDefaultDisplay().getWidth();
         int height = getWindowManager().getDefaultDisplay().getHeight();
         FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(width,
                 height, 0);
         view.setLayoutParams(lp);
         view.setScaleType(ScaleType.CENTER);
         view.setBackgroundColor(Color.WHITE);
 
         // And wrap the image view in a frame layout so that the full-screen
         // layout parameters are honoured
         FrameLayout layout = new FrameLayout(getApplicationContext());
         layout.addView(view);
 
         // Create the toast and set the view to be that of the FrameLayout
         Toast t = Toast.makeText(getApplicationContext(), "splash screen",
                 Toast.LENGTH_SHORT);
         t.setView(layout);
         t.setGravity(Gravity.CENTER, 0, 0);
         t.show();
     }
 
     /**
      * Load the various elements of the screen that must wait for other tasks to
      * complete
      */
     private void loadScreen()
     {
         // Spinner must reflect results of refresh view below
         Spinner s1 = (Spinner) findViewById(R.id.form_filter);        
         triggerRefresh(s1.getSelectedItemPosition());
               
         // TODO: pull in a list of valid groups
 
         registerForContextMenu(getListView());
     }
     
     /*
      * 
      */
     private void triggerRefresh(int position)
     {
         // Hide "nothing to display" message
         TextView nothingToDisplay = (TextView) findViewById(R.id.nothingToDisplay);
         nothingToDisplay.setVisibility(View.INVISIBLE);
         
         mRefreshViewTask = new RefreshViewTask();
 
         switch (position) {
         // Show all forms (in group)
         case 0:
             mRefreshViewTask.execute(InstanceDocument.Status.nothing);
             break;
         // Show all incomplete forms
         case 1:
             mRefreshViewTask.execute(InstanceDocument.Status.incomplete);
             break;
         // Show all completed forms
         case 2:
             mRefreshViewTask.execute(InstanceDocument.Status.complete);
             break;
         // Show all unread forms (e.g., those added or updated by others)
         case 3:
             mRefreshViewTask.execute(InstanceDocument.Status.updated);
             break;
         }   
     }
 }
