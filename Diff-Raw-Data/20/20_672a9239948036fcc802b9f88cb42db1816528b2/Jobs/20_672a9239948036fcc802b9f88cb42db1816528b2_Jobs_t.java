 package com.cghio.easyjobs;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.ActivityNotFoundException;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Base64;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 
 import com.loopj.android.http.AsyncHttpClient;
 import com.loopj.android.http.AsyncHttpResponseHandler;
 import com.loopj.android.http.RequestParams;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 public class Jobs extends Activity {
 
     private static String PREF_FILE = "auth_info";
     private static String PREF_VERSION = "VERSION";
     private static String PREF_URL = "URL";
     private static String PREF_CONTENT = "CONTENT";
 
     private static String API_HELP_URL = "";
     private static String API_TOKEN = "";
 
     private static String JOBS_INDEX_VERB = "";
     private static String JOBS_INDEX_URL = "";
 
     private static String JOBS_SHOW_URL = "";
 
     private static String JOBS_RUN_URL = "";
 
     private static String REVOKE_TOKEN_URL = "";
 
     private static ProgressDialog dialog;
     private static Handler dialogHandler;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_jobs);
 
         startEasyJobs();
     }
 
     private void startEasyJobs() {
         if (readPrefs()) {
             getHelp();
         } else {
             showScanButton();
         }
     }
 
     private boolean readPrefs() {
         SharedPreferences sharedPrefs = getSharedPreferences(PREF_FILE, 0);
         int VERSION = sharedPrefs.getInt(PREF_VERSION, 0);
         String URL = sharedPrefs.getString(PREF_URL, "");
         String CONTENT = sharedPrefs.getString(PREF_CONTENT, "");
 
         // validate version number
         if (VERSION <= 0) return false;
         if (VERSION > getResources().getInteger(R.integer.max_api_version)) return false;
 
         // validate help URL
         try {
             java.net.URL url = new URL(URL);
             url.toURI();
         } catch (Exception e) {
             return false;
         }
 
         if (CONTENT.length() == 0) return false;
 
         API_HELP_URL = URL;
         API_TOKEN = CONTENT;
 
         return true;
     }
 
     private void getHelp() {
         if (API_TOKEN.length() == 0) return;
         RequestParams params = new RequestParams();
         params.put("token", API_TOKEN);
         AsyncHttpClient client = new AsyncHttpClient();
         client.setTimeout(5000);
         showLoading();
         client.get(API_HELP_URL, params, new AsyncHttpResponseHandler() {
             @Override
             public void onFinish() {
                 hideLoading();
             }
             @Override
             public void onFailure(Throwable e, String response) {
                String error;
                 if (e != null && e.getCause() != null) {
                    error = e.getCause().getMessage();
                 } else if (e != null && e.getCause() == null) {
                    error = e.getMessage();
                 } else {
                    error = getString(R.string.error_connection_problem);
                }
                if (error.matches(".*[Uu]nauthorized.*")) {
                    error += "\n\n" + getString(R.string.error_need_refresh);
                 }
                showSimpleErrorDialog(error);
                 showReloadAndScanButton(true);
             }
             @Override
             public void onSuccess(final String response) {
                 try {
                     JSONObject helpObj = new JSONObject(response);
                     JSONObject jobsObj = helpObj.getJSONObject("jobs");
                     JSONObject jobsIndexObj = jobsObj.getJSONObject("index");
                     JOBS_INDEX_VERB = jobsIndexObj.getString("verb");
                     JOBS_INDEX_URL = jobsIndexObj.getString("url");
 
                     JSONObject jobsShowObj = jobsObj.getJSONObject("show");
                     JOBS_SHOW_URL = jobsShowObj.getString("url");
 
                     JSONObject jobsRunObj = jobsObj.getJSONObject("run");
                     JOBS_RUN_URL = jobsRunObj.getString("url");
 
                     JSONObject tokensObj = helpObj.getJSONObject("tokens");
                     JSONObject tokensRevokeObj = tokensObj.getJSONObject("revoke");
                     REVOKE_TOKEN_URL = tokensRevokeObj.getString("url");
 
                     getJobs();
                 } catch (JSONException e) {
                     showSimpleErrorDialog(getString(R.string.error_should_update_easyjobs));
                     showReloadAndScanButton(true);
                 }
             }
         });
     }
 
     private void getJobs() {
         if (JOBS_INDEX_VERB.length() == 0 || JOBS_INDEX_URL.length() == 0) return;
         if (JOBS_INDEX_VERB.equals("get")) {
             AsyncHttpClient client = new AsyncHttpClient();
             RequestParams params = new RequestParams();
             params.put("token", API_TOKEN);
             client.setTimeout(5000);
             showLoading();
             client.get(JOBS_INDEX_URL, params, new AsyncHttpResponseHandler() {
                 @Override
                 public void onFinish() {
                     hideLoading();
                 }
                 @Override
                 public void onFailure(Throwable e, String response) {
                     if (e != null && e.getCause() != null) {
                         showSimpleErrorDialog(e.getCause().getMessage());
                     } else if (e != null && e.getCause() == null) {
                         showSimpleErrorDialog(e.getMessage());
                     } else {
                         showSimpleErrorDialog(getString(R.string.error_connection_problem));
                     }
                     showReloadAndScanButton(true);
                 }
                 @Override
                 public void onSuccess(String response) {
                     try {
                         List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
 
                         JSONArray jobs = new JSONArray(response);
                         for (int i = 0; i < jobs.length(); i++) {
                             Map<String, Object> map = new HashMap<String, Object>();
                             map.put("ID", jobs.getJSONObject(i).getInt("id"));
                             map.put("NAME", jobs.getJSONObject(i).getString("name"));
                             String server = jobs.getJSONObject(i).getString("server_name");
                             if (server.equals("null")) server = getString(R.string.no_server);
                             map.put("SERVER_NAME", server);
                             data.add(map);
                         }
 
                         Map<String, Object> map = new HashMap<String, Object>();
                         map.put("NAME", "Revoke Access");
                         map.put("SERVER_NAME", "Remove access credentials on your phone and " +
                                 "renew token on server.");
                         data.add(map);
 
                         SimpleAdapter adapter = new SimpleAdapter(Jobs.this, data,
                                 R.layout.listview_jobs_items, new String[]{"NAME", "SERVER_NAME"},
                                 new int[]{R.id.text_job_name, R.id.text_server_name});
                         ListView listview_jobs = (ListView) findViewById(R.id.listView_jobs);
                         listview_jobs.setAdapter(adapter);
                         listview_jobs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                             @Override
                             public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                 if (i == adapterView.getCount() - 1) {
                                     revokeAccess();
                                 } else {
                                     Object item = adapterView.getAdapter().getItem(i);
                                     if (item instanceof Map) {
                                         int ID = Integer.parseInt(((Map) item).get("ID").toString());
                                         Intent intent = new Intent(Jobs.this, JobsDetails.class);
                                         intent.putExtra("API_TOKEN", API_TOKEN);
                                         intent.putExtra("JOB_ID", ID);
                                         intent.putExtra("JOBS_SHOW_URL", JOBS_SHOW_URL);
                                         intent.putExtra("JOBS_RUN_URL", JOBS_RUN_URL);
                                         Jobs.this.startActivity(intent);
                                     }
                                 }
                             }
                         });
                     } catch (JSONException e) {
                         showSimpleErrorDialog(getString(R.string.error_should_update_easyjobs));
                         showReloadAndScanButton(true);
                     }
                 }
             });
         }
     }
 
     private void showScanButton() {
         showReloadAndScanButton(false);
     }
 
     private void showReloadAndScanButton(boolean withRetryButton) {
         List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
         Map<String, Object> map = new HashMap<String, Object>();
         if (withRetryButton) {
             map.put("T", 0);
             map.put("K", "Retry");
             map.put("V", "Try connecting to server again.");
             data.add(map);
         }
         map = new HashMap<String, Object>();
         map.put("T", 1);
         map.put("K", "Scan QR Code");
         map.put("V", "Browse EasyJobs web page, scan the QR code in your account settings page.");
         data.add(map);
         SimpleAdapter adapter = new SimpleAdapter(Jobs.this, data,
                 R.layout.listview_jobs_items, new String[]{"K", "V"},
                 new int[]{R.id.text_job_name, R.id.text_server_name});
         ListView listview_jobs = (ListView) findViewById(R.id.listView_jobs);
         listview_jobs.setAdapter(adapter);
         listview_jobs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                 Object item = adapterView.getAdapter().getItem(i);
                 if (item instanceof Map) {
                     switch (Integer.parseInt(((Map) item).get("T").toString())) {
                         case 0:
                             startEasyJobs();
                             break;
                         case 1:
                             openScanner();
                             break;
                     }
                 }
             }
         });
     }
 
     private void openScanner() {
         try {
             Intent intent = new Intent("com.google.zxing.client.android.SCAN");
             intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
             startActivityForResult(intent, 1);
         } catch (ActivityNotFoundException e) {
             AlertDialog alertDialog = new AlertDialog.Builder(Jobs.this).create();
             alertDialog.setTitle(R.string.error);
             alertDialog.setMessage(getString(R.string.error_barcode_scanner_not_installed));
             alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.cancel),
                     new DialogInterface.OnClickListener() {
                         @Override
                         public void onClick(DialogInterface dialogInterface, int i) {
                         }
                     });
             alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.ok),
                     new DialogInterface.OnClickListener() {
                         @Override
                         public void onClick(DialogInterface dialogInterface, int i) {
                             Intent intent = new Intent(Intent.ACTION_VIEW);
                             intent.setData(Uri.parse(
                                     "market://details?id=com.google.zxing.client.android"));
                             startActivity(intent);
                         }
                     });
             alertDialog.show();
         }
     }
 
     public void onActivityResult(int requestCode, int resultCode, Intent intent) {
         if (requestCode == 1 && resultCode == RESULT_OK) {
             String content = intent.getStringExtra("SCAN_RESULT");
             if (content == null) return;
             String decoded_content = null;
             try {
                 byte[] decoded = Base64.decode(content, Base64.DEFAULT);
                 decoded_content = new String(decoded);
             } catch (IllegalArgumentException e) {
                 showSimpleErrorDialog(getString(R.string.error_invalid_content));
             }
             if (decoded_content != null) {
                 try {
                     JSONObject object = new JSONObject(decoded_content);
                     int VERSION = object.getInt("v");
                     String URL = object.getString("u");
                     String CONTENT = object.getString("c");
 
                     // validate version number
 
                     if (VERSION <= 0) throw new JSONException(null);
                     if (VERSION > getResources().getInteger(R.integer.max_api_version))
                         throw new Exception(getString(R.string.error_please_update_app));
 
                     // validate help URL
                     URL url = new URL(URL);
                     url.toURI(); // stop never used warning
 
                     if (CONTENT.length() == 0)
                         throw new Exception(getString(R.string.error_invalid_content));
 
                     SharedPreferences sharedPrefs = getSharedPreferences(PREF_FILE, 0);
                     SharedPreferences.Editor editor = sharedPrefs.edit();
                     editor.putInt(PREF_VERSION, VERSION);
                     editor.putString(PREF_URL, URL);
                     editor.putString(PREF_CONTENT, CONTENT);
                     editor.commit();
 
                 } catch (MalformedURLException e) {
                     showSimpleErrorDialog(getString(R.string.error_invalid_url));
                 } catch (JSONException e) {
                     showSimpleErrorDialog(getString(R.string.error_invalid_content));
                 } catch (Exception e) {
                     showSimpleErrorDialog(e.getMessage());
                 }
                 if (readPrefs()) {
                     startEasyJobs();
                 }
             }
         }
     }
 
     private void revokeAccess() {
         SharedPreferences sharedPrefs = getSharedPreferences(PREF_FILE, 0);
         SharedPreferences.Editor editor = sharedPrefs.edit();
         editor.remove(PREF_VERSION);
         editor.remove(PREF_URL);
         editor.remove(PREF_CONTENT);
         editor.commit();
 
         AsyncHttpClient client = new AsyncHttpClient();
         client.delete(REVOKE_TOKEN_URL + "?token=" + API_TOKEN, new AsyncHttpResponseHandler(){
             @Override
             public void onSuccess(String response) {
             }
         });
 
         startEasyJobs();
     }
 
     private void showSimpleErrorDialog(String message) {
         AlertDialog alertDialog = new AlertDialog.Builder(this).create();
         alertDialog.setMessage(message);
         alertDialog.setTitle(R.string.error);
         alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.ok), (Message) null);
         alertDialog.show();
     }
 
     private void showLoading() {
         dialog = new ProgressDialog(Jobs.this);
         dialog.setMessage(getString(R.string.loading));
         dialog.setCancelable(false);
         if (dialogHandler == null) {
             dialogHandler = new Handler();
         }
         dialogHandler.postDelayed(new Runnable() {
             public void run() {
                 if (dialog != null) dialog.show();
             }
         }, 600);
     }
 
     private void hideLoading() {
         if (dialogHandler != null) {
             dialogHandler.removeCallbacksAndMessages(null);
             dialogHandler = null;
         }
         if (dialog != null) {
             dialog.dismiss();
             dialog = null;
         }
     }
 }
