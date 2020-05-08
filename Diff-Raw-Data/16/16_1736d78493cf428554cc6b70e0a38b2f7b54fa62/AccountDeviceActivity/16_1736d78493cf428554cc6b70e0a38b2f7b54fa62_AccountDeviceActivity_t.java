 package com.radicaldynamic.groupinform.activities;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.message.BasicNameValuePair;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.json.JSONTokener;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.radicaldynamic.groupinform.R;
 import com.radicaldynamic.groupinform.application.Collect;
 import com.radicaldynamic.groupinform.logic.AccountDevice;
 import com.radicaldynamic.groupinform.logic.InformOnlineState;
 import com.radicaldynamic.groupinform.utilities.FileUtilsExtended;
 import com.radicaldynamic.groupinform.utilities.HttpUtils;
 
 public class AccountDeviceActivity extends Activity
 {
     private static final String t = "AccountDeviceActivity: ";
 
     private static final int MENU_RESET_DEVICE = 0;
     private static final int MENU_REMOVE_DEVICE = 1;
     
     public static final int SAVING_DIALOG = 0;
     public static final int REMOVING_DIALOG = 1;
     public static final int CONFIRM_REMOVAL_DIALOG = 2;
     public static final int RESET_PROGRESS_DIALOG = 3;
     public static final int CONFIRM_RESET_DIALOG = 4;
    public static final int CANNOT_RESET_OWN_DEVICE = 5;
     
     // Keys for saving and restoring activity state
     public static final String KEY_DEVICE_ID = "key_device_id";     // Also used to accept device ID into activity
 
     @SuppressWarnings("unused")
     private AlertDialog mAlertDialog;
     private ProgressDialog mProgressDialog;
     
     private String mDeviceId;
     private AccountDevice mDevice;
     
     private EditText mDeviceAlias;
     private EditText mDeviceEmail;
     private TextView mDevicePin;
     private TextView mDeviceCheckin;
     private TextView mDeviceStatus;
     
     @Override
     protected void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         
         setContentView(R.layout.device);
         setTitle(getString(R.string.app_name) + " > " + getString(R.string.tf_account_device));
         
         if (savedInstanceState == null) {
             Intent i = getIntent();
             mDeviceId = i.getStringExtra(KEY_DEVICE_ID);            
         } else {
             if (savedInstanceState.containsKey(KEY_DEVICE_ID))
                 mDeviceId = savedInstanceState.getString(KEY_DEVICE_ID);
         }
         
         mDevice = Collect.getInstance().getInformOnlineState().getAccountDevices().get(mDeviceId);
         
         mDeviceAlias = (EditText) findViewById(R.id.alias);
         mDeviceEmail = (EditText) findViewById(R.id.email);
         mDevicePin = (TextView) findViewById(R.id.pin);
         mDeviceCheckin = (TextView) findViewById(R.id.checkin);        
         mDeviceStatus = (TextView) findViewById(R.id.status);
         
         mDeviceAlias.setText(mDevice.getAlias());
         mDeviceEmail.setText(mDevice.getEmail());
         mDevicePin.setText(mDevice.getPin());
         
         // lastCheck is delivered in milliseconds
         Integer lastCheckin;
         
         try {
             lastCheckin = Integer.valueOf(mDevice.getLastCheckin()) / 1000;
         } catch (NumberFormatException e) {
             // Not sure why we run into problems with this
             lastCheckin = 0;
         }
         
         String approximation = "";
         String period = "";
         String unit = "";
         
         if (lastCheckin / 86400 > 0) {
             period = Integer.toString(lastCheckin / 86400);
             approximation = "About ";
             unit = " days";
         } else if ((lastCheckin % 86400) / 3600 > 0) {
             period = Integer.toString((lastCheckin % 86400) / 3600);
             approximation = "About ";
             unit = " hours";
         } else if (((lastCheckin % 86400) % 3600) / 60 > 0) {
             period = Integer.toString(((lastCheckin % 86400) % 3600) / 60);
             unit = " minutes";
         } else if ((lastCheckin % 86400) % 3600 > 0) {
             period = Integer.toString((lastCheckin % 86400) % 3600);
             unit = " seconds";
         } else {
             approximation = getString(R.string.tf_unavailable);
         }
 
         // Hack to turn "minutes" into "minute" or whatever
         if (period.equals("1")) {
             unit = unit.substring(0, unit.length() - 1);
         }
         
         String text = approximation + period + unit;
         
         if (!text.equals(getString(R.string.tf_unavailable).toString())) {
             text = text + " ago";
         }
 
         mDeviceCheckin.setText(text);
         
         // Initialize fields based on whether the device is locked
         if (mDevice.getStatus().equals(AccountDevice.STATUS_ACTIVE))
             mDeviceStatus.setText(getString(R.string.tf_device_admin_status_inuse));                        
         else
             mDeviceStatus.setText(getString(R.string.tf_device_admin_status_unused));            
         
         // Warn user that changes cannot be saved while offline
         if (!Collect.getInstance().getIoService().isSignedIn())
             Toast.makeText(getApplicationContext(), getString(R.string.tf_while_offline), Toast.LENGTH_LONG).show();
     }
     
     /*
      * (non-Javadoc)
      * 
      * @see android.app.Activity#onCreateDialog(int)
      */
     @Override
     protected Dialog onCreateDialog(int id)
     {
         switch (id) {
         case SAVING_DIALOG:
             mProgressDialog = new ProgressDialog(this);   
             mProgressDialog.setMessage(getText(R.string.tf_saving_please_wait));
             mProgressDialog.setIndeterminate(true);
             mProgressDialog.setCancelable(false);            
             return mProgressDialog;
             
         case REMOVING_DIALOG:
             mProgressDialog = new ProgressDialog(this);   
             mProgressDialog.setMessage(getText(R.string.tf_removing_please_wait));
             mProgressDialog.setIndeterminate(true);
             mProgressDialog.setCancelable(false);            
             return mProgressDialog;
             
         case CONFIRM_REMOVAL_DIALOG:
             mAlertDialog = new AlertDialog.Builder(this)
                 .setIcon(R.drawable.ic_dialog_alert)
                 .setTitle(getString(R.string.tf_remove_device) + "?")
                 .setMessage(R.string.tf_remove_device_dialog_msg)
                 .setPositiveButton(R.string.tf_remove, new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int id) {
                         new RemoveDeviceTask().execute();
                     }
                 })
                 .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int id) {
                         dialog.cancel();
                     }
                 })
                 .show();
             break;
             
         case CONFIRM_RESET_DIALOG:
             mAlertDialog = new AlertDialog.Builder(this)
                 .setCancelable(false)
                 .setIcon(R.drawable.ic_dialog_alert)
                 .setMessage(R.string.tf_reset_warning_admin_msg)
                 .setTitle(getString(R.string.tf_reset_device) + "?")            
                 .setPositiveButton(R.string.tf_reset, new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int whichButton) {
                         new ResetDeviceTask().execute();
                     }
                 })            
                 .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int whichButton) {
                         dialog.cancel();
                     }
                 })            
                 .show();    
             break;
             
         case RESET_PROGRESS_DIALOG:
             mProgressDialog = new ProgressDialog(this);   
             mProgressDialog.setMessage(getText(R.string.tf_resetting_please_wait));
             mProgressDialog.setIndeterminate(true);
             mProgressDialog.setCancelable(false);            
             return mProgressDialog;
            
        case CANNOT_RESET_OWN_DEVICE:
            mAlertDialog = new AlertDialog.Builder(this)
            .setCancelable(false)
            .setMessage(R.string.tf_unable_to_reset_self)         
            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    dialog.cancel();
                }
            })         
            .show(); 
         }
 
         return null;
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu)
     {
         super.onCreateOptionsMenu(menu);
         
         boolean enabled = false;
 
         if (Collect.getInstance().getIoService().isSignedIn())
             enabled = true;
 
         menu.add(0, MENU_RESET_DEVICE, 0, getString(R.string.tf_reset_device))
             .setIcon(R.drawable.ic_menu_close_clear_cancel)
             .setEnabled(enabled);
 
         menu.add(0, MENU_REMOVE_DEVICE, 0, getString(R.string.tf_remove_device))
             .setIcon(R.drawable.ic_menu_delete)
             .setEnabled(enabled);
 
         return true;
     }    
     
     @Override
     public boolean onKeyDown(int keyCode, KeyEvent event)
     {
         switch (keyCode) {
         case KeyEvent.KEYCODE_BACK:
             if (Collect.getInstance().getIoService().isSignedIn())
                 new UpdateDeviceInfoTask().execute();
             else 
                 finish();
             
             return true;
         }
 
         return super.onKeyDown(keyCode, event);
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item)
     {
         switch (item.getItemId()) {        
         case MENU_RESET_DEVICE:
             showDialog(CONFIRM_RESET_DIALOG);
             return true;
         
         case MENU_REMOVE_DEVICE:
             showDialog(CONFIRM_REMOVAL_DIALOG);
             return true;
         }
         
         return super.onOptionsItemSelected(item);
     }
 
     @Override
     public void onSaveInstanceState(Bundle savedInstanceState)
     {
         savedInstanceState.putString(KEY_DEVICE_ID, mDeviceId);
         
         super.onSaveInstanceState(savedInstanceState);
     }
     
     private class RemoveDeviceTask extends AsyncTask<Void, Void, String>
     {        
         @Override
         protected String doInBackground(Void... nothing)
         {            
             String removeUrl = Collect.getInstance().getInformOnlineState().getServerUrl() + "/device/remove/" + mDeviceId;            
             return HttpUtils.getUrlData(removeUrl);
         }
     
         @Override
         protected void onPreExecute()
         {
             showDialog(REMOVING_DIALOG);
         }
     
         @Override
         protected void onPostExecute(String getResult)
         {
             mProgressDialog.cancel();
             
             JSONObject update;
             
             try {
                 Log.d(Collect.LOGTAG, t + "parsing getResult " + getResult);                
                 update = (JSONObject) new JSONTokener(getResult).nextValue();
                 
                 String result = update.optString(InformOnlineState.RESULT, InformOnlineState.ERROR);
                 
                 // Update successful
                 if (result.equals(InformOnlineState.OK)) {  
                     Toast.makeText(getApplicationContext(), getString(R.string.tf_removed_with_param, mDevice.getDisplayName()), Toast.LENGTH_SHORT).show();                    
                     
                     // Force the list to refresh (do not be destructive in case something bad happens later)
                     new File(getCacheDir(), FileUtilsExtended.DEVICE_CACHE_FILE).setLastModified(0);
                     
                     // Get out of here
                     finish();
                 } else if (result.equals(InformOnlineState.FAILURE)) {
                     // TODO: user tried to remove self is the only possible failure (implement at some point)
                     Toast.makeText(getApplicationContext(), getString(R.string.tf_unable_to_remove_self), Toast.LENGTH_LONG).show();
                 } else {
                     // Something bad happened
                     Log.e(Collect.LOGTAG, t + "system error while processing getResult");                   
                     Toast.makeText(getApplicationContext(), getString(R.string.tf_system_error_dialog_msg), Toast.LENGTH_LONG).show();
                 }                
             } catch (NullPointerException e) {
                 // Communication error
                 Log.e(Collect.LOGTAG, t + "no getResult to parse.  Communication error with node.js server?");               
                 Toast.makeText(getApplicationContext(), getString(R.string.tf_communication_error_try_again), Toast.LENGTH_LONG).show();
                 e.printStackTrace();
             } catch (JSONException e) {
                 // Parse error (malformed result)
                 Log.e(Collect.LOGTAG, t + "failed to parse getResult " + getResult);                
                 Toast.makeText(getApplicationContext(), getString(R.string.tf_system_error_dialog_msg), Toast.LENGTH_LONG).show();
                 e.printStackTrace();
             }
         }
     }
 
     public class ResetDeviceTask extends AsyncTask<Void, Void, String> 
     {
         @Override
         protected String doInBackground(Void... params)
         {
             String url = Collect.getInstance().getInformOnlineState().getServerUrl() + "/device/reset/" + mDeviceId;            
             return HttpUtils.getUrlData(url);
         }
         
         @Override
         protected void onPreExecute()
         {
             showDialog(RESET_PROGRESS_DIALOG);
         }
 
         @Override
         protected void onPostExecute(String getResult)
         {
             mProgressDialog.cancel();
             
             JSONObject reset;
             
             try {
                 Log.d(Collect.LOGTAG, t + "parsing getResult " + getResult);   
                 reset = (JSONObject) new JSONTokener(getResult).nextValue();
                 
                 String result = reset.optString(InformOnlineState.RESULT, InformOnlineState.ERROR);
                 
                 if (result.equals(InformOnlineState.OK)) {  
                     Toast.makeText(getApplicationContext(), getString(R.string.tf_reset_with_param, mDevice.getDisplayName()), Toast.LENGTH_SHORT).show();                    
                     
                     // Force the list to refresh (do not be destructive in case something bad happens later)
                     new File(getCacheDir(), FileUtilsExtended.DEVICE_CACHE_FILE).setLastModified(0);
                     
                     // Get out of here
                     finish();                    
                 } else if (result.equals(InformOnlineState.FAILURE)) {
                    showDialog(CANNOT_RESET_OWN_DEVICE);
                 } else {
                     Toast.makeText(getApplicationContext(), getString(R.string.tf_system_error_dialog_msg), Toast.LENGTH_LONG).show();
                     Log.e(Collect.LOGTAG, t + "system error while processing getResult"); 
                 }
             } catch (NullPointerException e) {
                 // Communication error
                 Toast.makeText(getApplicationContext(), getString(R.string.tf_communication_error_try_again), Toast.LENGTH_LONG).show();
                 Log.e(Collect.LOGTAG, t + "no getResult to parse.  Communication error with node.js server?");
                 e.printStackTrace();
             } catch (JSONException e) {
                 // Parse error (malformed result)
                 Toast.makeText(getApplicationContext(), getString(R.string.tf_system_error_dialog_msg), Toast.LENGTH_LONG).show();
                 Log.e(Collect.LOGTAG, t + "failed to parse getResult " + getResult);
                 e.printStackTrace();
             }
         }
     }
 
     private class UpdateDeviceInfoTask extends AsyncTask<Void, Void, String>
     {        
         @Override
         protected String doInBackground(Void... nothing)
         {
             List<NameValuePair> params = new ArrayList<NameValuePair>();
             params.add(new BasicNameValuePair("deviceId", mDevice.getId()));
             params.add(new BasicNameValuePair("alias", mDeviceAlias.getText().toString().trim()));
             params.add(new BasicNameValuePair("email", mDeviceEmail.getText().toString().trim()));
             
             String updateUrl = Collect.getInstance().getInformOnlineState().getServerUrl() + "/device/update";
             
             return HttpUtils.postUrlData(updateUrl, params);
         }
 
         @Override
         protected void onPreExecute()
         {
             showDialog(SAVING_DIALOG);
         }
 
         @Override
         protected void onPostExecute(String postResult)
         {
             JSONObject update;
             
             mProgressDialog.cancel();
             
             try {
                 update = (JSONObject) new JSONTokener(postResult).nextValue();
                 String result = update.optString(InformOnlineState.RESULT, InformOnlineState.FAILURE);
                 
                 // Update successful
                 if (result.equals(InformOnlineState.OK)) {  
                     Toast.makeText(getApplicationContext(), getString(R.string.tf_saved_data), Toast.LENGTH_SHORT).show();                    
                     
                     // Force the list to refresh (do not be destructive in case something bad happens later)
                     new File(getCacheDir(), FileUtilsExtended.DEVICE_CACHE_FILE).setLastModified(0);
                     
                     // Commit changes to the cache-in-memory to avoid running InformOnlineService.loadDeviceHash()
                     Collect.getInstance().getInformOnlineState().getAccountDevices().get(mDeviceId).setAlias(mDeviceAlias.getText().toString().trim());
                     Collect.getInstance().getInformOnlineState().getAccountDevices().get(mDeviceId).setEmail(mDeviceEmail.getText().toString().trim());                
                     
                     // Get out of here
                     finish();
                 } else if (result.equals(InformOnlineState.FAILURE)) {
                     // Update failed because of something the user did                    
                     String reason = update.optString(InformOnlineState.REASON, ClientRegistrationActivity.REASON_UNKNOWN);
                     
                     if (reason.equals(ClientRegistrationActivity.REASON_INVALID_EMAIL)) {
                         Log.w(Collect.LOGTAG, t + "invalid email address \"" + mDeviceEmail.getText().toString().trim() + "\"");                    
                         Toast.makeText(getApplicationContext(), getString(R.string.tf_invalid_email), Toast.LENGTH_LONG).show();
                     } else if (reason.equals(ClientRegistrationActivity.REASON_EMAIL_ASSIGNED)) {
                         Log.i(Collect.LOGTAG, t + "email address \"" + mDeviceEmail.getText().toString().trim() + "\" already assigned to an account");                    
                         Toast.makeText(getApplicationContext(), getString(R.string.tf_registration_error_email_in_use), Toast.LENGTH_LONG).show();
                     } else {
                         // Unhandled response
                         Log.e(Collect.LOGTAG, t + "system error while processing postResult");                    
                         Toast.makeText(getApplicationContext(), getString(R.string.tf_system_error_dialog_msg), Toast.LENGTH_LONG).show();
                     }                    
                 } else {
                     // Something bad happened
                     Log.e(Collect.LOGTAG, t + "system error while processing postResult");                   
                     Toast.makeText(getApplicationContext(), getString(R.string.tf_system_error_dialog_msg), Toast.LENGTH_LONG).show();
                 }                
             } catch (NullPointerException e) {
                 // Communication error
                 Log.e(Collect.LOGTAG, t + "no postResult to parse.  Communication error with node.js server?");               
                 Toast.makeText(getApplicationContext(), getString(R.string.tf_communication_error_try_again), Toast.LENGTH_LONG).show();
                 e.printStackTrace();
             } catch (JSONException e) {
                 // Parse error (malformed result)
                 Log.e(Collect.LOGTAG, t + "failed to parse postResult " + postResult);                
                 Toast.makeText(getApplicationContext(), getString(R.string.tf_system_error_dialog_msg), Toast.LENGTH_LONG).show();
                 e.printStackTrace();
             }
         }
     }
 }
