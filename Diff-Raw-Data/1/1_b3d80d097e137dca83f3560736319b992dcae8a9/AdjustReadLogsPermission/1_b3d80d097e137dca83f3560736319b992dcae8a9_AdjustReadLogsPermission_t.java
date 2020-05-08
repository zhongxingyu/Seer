 /* Copyright 2013 Davis Mosenkovs
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 /*
  * Solution for logcat reading on rooted Android 4.1+
  * This class should be called from onCreate method of application's main Activity be: AdjustReadLogsPermission.adjustIfNeeded(this);
  * For more usage details see http://forum.xda-developers.com/showthread.php?p=43035431
  */
 
 package enter.your.package.name.here;
 
 import android.app.Activity;
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.os.AsyncTask;
 import android.util.Log;
 import android.widget.Toast;
 import android.content.Intent;
 import eu.chainfire.libsuperuser.Shell;
 
 public class AdjustReadLogsPermission extends AsyncTask<Void, Void, Void> {
     private ProgressDialog dialog = null;
     private Activity activity = null;
     private static final String RL_ADJUSTED_EXTRA_NAME = ".READ_LOGS_PERMISSION_ADJUSTED";
 
     public static void adjustIfNeeded(Activity activity) {
            if(activity.getPackageManager().checkPermission(android.Manifest.permission.READ_LOGS, activity.getPackageName())!=android.content.pm.PackageManager.PERMISSION_GRANTED) {
                // Do anything only if lacking android.permission.READ_LOGS permission
                if(!activity.getIntent().hasExtra(activity.getPackageName()+RL_ADJUSTED_EXTRA_NAME)) {
                    // Initialize and execute AsyncTask only once - if extra (see below) is not found
                    (new AdjustReadLogsPermission()).setActivity(activity).execute();
                } else {
                    // Display (toast) and log error if extra is found (and permission still not granted)
                    // Also error is logged, because application should be able to read its own logcat without android.permission.READ_LOGS permission
                    Toast.makeText(activity, "Failed to adjust permissions.\nLog most likely will be INCOMPLETE!", Toast.LENGTH_LONG).show();
                    Log.e(activity.getTitle().toString(), "Failed to adjust permissions. Log most likely will be INCOMPLETE!");
                    Log.e(activity.getTitle().toString(), "Starting from Android 4.1 full logcat is available for applications only by using root features.");
                }
            }
     }
 
     private AdjustReadLogsPermission setActivity(Activity activity) {
         // Save reerence to activity for use inside AsyncTask
         this.activity = activity;
         return this;
     }
 
     @Override
     protected void onPreExecute() {
         // Display "Adjusting permissions..." progress dialog
         dialog = new ProgressDialog(activity);
         dialog.setMessage("Adjusting permissions...");
         dialog.setIndeterminate(true);
         dialog.setCancelable(false);
         dialog.show();
     }
 
     @Override
     protected Void doInBackground(Void... params) {
         // Wait 1 second after displaying "Adjusting permissions..." progress dialog and before actually adjusting permissions.
         // This is meant to allow users to see and read progress dialog.
         // Technically next line can be removed or delay (1000 milliseconds) can be changed.
         try { Thread.sleep(1000); } catch(Exception e) { }
         
         // Adjust permissions by root features using libsuperuser (http://su.chainfire.eu/)
         Shell.SU.run("pm grant "+activity.getPackageName()+" "+android.Manifest.permission.READ_LOGS);
         return null;
     }
 
     @Override
     protected void onPostExecute(Void result) {
         // Launch application again after 1000 ms delay with extra (mentioned above) added to prevent infinite relaunch loop.
         // Delay is REQUIRED to relaunch application AFTER it has been terminated, decreasing delay can prevent application from being relaunched.
         // Increasing delay will decrease risk of application not being relaunched, but may bother the user.
         Intent nIntent = activity.getIntent();
         nIntent.putExtra(activity.getPackageName()+RL_ADJUSTED_EXTRA_NAME, true);
         PendingIntent pIntent = PendingIntent.getActivity(activity, 0, nIntent, 0);
         // Delay is in next line
         ((AlarmManager)activity.getSystemService(Context.ALARM_SERVICE)).set(AlarmManager.RTC, System.currentTimeMillis() + 1000, pIntent);
         System.exit(0);
     }        
 }
