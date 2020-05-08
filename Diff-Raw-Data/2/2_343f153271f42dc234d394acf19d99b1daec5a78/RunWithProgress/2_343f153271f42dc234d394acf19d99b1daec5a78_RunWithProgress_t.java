 package org.dreamcwli.MarketAccess.utils;
 
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.SharedPreferences;
 import android.os.AsyncTask;
 import android.telephony.TelephonyManager;
 import android.util.Log;
 import android.widget.Toast;
 import org.dreamcwli.MarketAccess.R;
 import org.dreamcwli.MarketAccess.view.StartUpView;
 
 import java.io.IOException;
 import java.util.regex.Pattern;
 
 import static org.dreamcwli.MarketAccess.utils.RunWithProgress.RunResult.*;
 
 /**
  * Date: Mar 24, 2010
  * Time: 4:14:07 PM
  *
  * @author serge
  */
 public class RunWithProgress {
   private static String[]         commands;
   private        ProgressDialog   pd;
   private final  Context          ctx;
   private final  String           message;
   private        CompleteListener completeListener;
   private        boolean          silent;
 
   enum RunResult {
     OK,
     ERROR,
     NO_ROOT
   }
 
   private static final Pattern PATTERN = Pattern.compile(" ");
 
   private static final String KILL_ALL = "killall";
   private static final String SETPREF  = "setpref";
   private static final String SETOWN   = "setown";
   private static final String VERIFY   = "verify_sim_numeric";
 
   private static final String[] COMMANDS = new String[]{
     "setprop gsm.sim.operator.numeric %n",
     "killall com.android.vending",
    "rm -r /data/data/com.android.vending/cache/*",
     "chmod 777 /data/data/com.android.vending/shared_prefs",
     "chmod 666 /data/data/com.android.vending/shared_prefs/vending_preferences.xml",
     "setpref com.android.vending vending_preferences boolean metadata_paid_apps_enabled true",
     "chmod 660 /data/data/com.android.vending/shared_prefs/vending_preferences.xml",
     "chmod 771 /data/data/com.android.vending/shared_prefs",
     "setown com.android.vending /data/data/com.android.vending/shared_prefs/vending_preferences.xml",
     "verify_sim_numeric %n"
   };
 
   private String errorMessage;
   private String okMessage;
 
   public void setSilent(boolean silent) {
     this.silent = silent;
   }
 
   public void setErrorMessage(String errorMessage) {
     this.errorMessage = errorMessage;
   }
 
   public void setOkMessage(String okMessage) {
     this.okMessage = okMessage;
   }
 
   public void setCompleteListener(CompleteListener completeListener) {
     this.completeListener = completeListener;
   }
 
   private void showNoRootAlert() {
     new AlertDialog.Builder(ctx)
       .setMessage(R.string.no_root)
       .setCancelable(false)
       .setPositiveButton(R.string.no_root_ok, new DialogInterface.OnClickListener() {
         @Override
         public void onClick(DialogInterface dialog, int id) {
           dialog.cancel();
         }
       }).create().show();
   }
 
   public static String[] makeCommand(String numeric) {
     final String[] strings = new String[COMMANDS.length];
     System.arraycopy(COMMANDS, 0, strings, 0, COMMANDS.length);
     for (int i = 0, stringsLength = strings.length; i < stringsLength; i++) {
       strings[i] = strings[i].replace("%n", numeric);
     }
     return strings;
   }
 
   public RunWithProgress(Context ctx, String value, String message) {
     this.ctx = ctx;
     this.message = message;
 
     setErrorMessage(ctx.getString(R.string.error));
     setOkMessage(ctx.getString(R.string.applied));
 
     commands = makeCommand(value);
   }
 
   public RunWithProgress(Context ctx, String[] runCommands, String message) {
     this.ctx = ctx;
     this.message = message;
 
     commands = runCommands;
   }
 
   public void doRun() {
     new RunTask().execute(commands);
   }
 
   public void doRunForeground() {
     silent = true;
     doRunCommands(commands, null);
   }
 
   private void handleOwn(String single) {
     final String[] parts = PATTERN.split(single, 3);
     try {
       final int uid = ctx.getPackageManager().getApplicationInfo(parts[1], 0).uid;
       Log.i(StartUpView.TAG, "setting owner: " + uid);
       ShellInterface.runCommand("chown " + uid + '.' + uid + ' ' + parts[2]);
     } catch (Exception e) {
       Log.e(StartUpView.TAG, "error setting owner", e);
     }
   }
 
   private void handlePref(String single) {
     final String[] parts = PATTERN.split(single, 6);
     try {
       Context app = ctx.createPackageContext(parts[1], 0);
       final SharedPreferences preferences =
         app.getSharedPreferences(parts[2], Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE);
       // TODO add support for other types if you want it to be universal
       // only change if set to false, don't add new preference or overwrite if already true
       if (parts[3].equals("boolean") && !preferences.getBoolean(parts[4], true)) {
         final SharedPreferences.Editor editor = preferences.edit();
         editor.putBoolean(parts[4], Boolean.parseBoolean(parts[5]));
         editor.commit();
       }
     } catch (Exception e) {
       Log.e(StartUpView.TAG, "error setting preference", e);
     }
   }
 
   private static void handleKill(String single) throws IOException {
     if (single.indexOf(' ') > 0) {
       final String app = single.substring(single.indexOf(' ') + 1);
       final AppManager am = AppManager.getInstance();
       int count = 0;
       while (am.isRunning(app)) {
         count++;
         am.kill(app);
         if (am.isRunning(app)) {
           Log.w(StartUpView.TAG, "failed to kill " + app);
           try {
             Thread.sleep(200);
           } catch (InterruptedException ignored) {
             break;
           }
         } else {
           break;
         }
         if (count >= 5) {
           Log.e(StartUpView.TAG, "failed to kill " + app + " 5 times, aborting");
           throw new IOException("can't kill app: " + app);
         }
       }
     }
   }
 
   private boolean handleVerify(String single) {
     if (single.indexOf(' ') > 0) {
       final String expectedNumeric = single.substring(single.indexOf(' ') + 1);
       final TelephonyManager tm = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
       final String currentNumeric = tm.getSimOperator();
       return expectedNumeric.equals(currentNumeric);
     }
     return false;
   }
 
   private RunResult doRunCommands(String[] commands, ProgressCallback callback) {
     if (!ShellInterface.isSuAvailable()) {
       return NO_ROOT;
     }
     int i = 0;
     try {
       for (String cmd : commands) {
         Log.i(StartUpView.TAG, cmd);
         if (cmd.startsWith(KILL_ALL)) {
           // special treatment for killall command, use java to kill the process
           handleKill(cmd);
         } else if (cmd.startsWith(SETPREF)) {
           handlePref(cmd);
         } else if (cmd.startsWith(SETOWN)) {
           handleOwn(cmd);
         } else if (cmd.startsWith(VERIFY)) {
           if (!handleVerify(cmd)) return ERROR;
         } else {
           if (!ShellInterface.runCommand(cmd)) throw new IOException("Shell command failed: " + cmd);
         }
         if (!silent) callback.progress(++i);
       }
       if (!silent) callback.progress(++i);
       return OK;
     } catch (Exception e) {
       Log.e(StartUpView.TAG, "RunTask error", e);
       return ERROR;
     }
   }
 
   interface ProgressCallback {
     void progress(int i);
   }
 
   class RunTask extends AsyncTask<String[], Integer, RunResult> {
     @Override
     protected void onPreExecute() {
       super.onPreExecute();
       if (!silent) {
         pd = new ProgressDialog(ctx);
         pd.setMax(commands.length);
         pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
         pd.setProgress(1);
         pd.setTitle(R.string.working);
         pd.setMessage(message);
         pd.show();
       }
     }
 
     @Override
     protected void onPostExecute(RunResult result) {
       super.onPostExecute(result);
       if (!silent) {
         pd.dismiss();
         if (result == OK) {
           if (okMessage != null) Toast.makeText(ctx, okMessage, Toast.LENGTH_SHORT).show();
         } else if (result == ERROR) {
           if (errorMessage != null) Toast.makeText(ctx, errorMessage, Toast.LENGTH_LONG).show();
         } else {
           showNoRootAlert();
         }
       }
       if (completeListener != null) {
         completeListener.onComplete();
       }
     }
 
     @Override
     protected void onProgressUpdate(Integer... values) {
       super.onProgressUpdate(values);
       if (!silent) pd.setProgress(values[0]);
     }
 
     @Override
     protected RunResult doInBackground(String[]... strings) {
       String[] commands = strings[0];
       return runCommands(commands);
     }
 
     public RunResult runCommands(String[] commands) {
       return doRunCommands(commands, new ProgressCallback() {
         @Override
         public void progress(int i) {
           publishProgress(i);
         }
       });
     }
   }
 }
