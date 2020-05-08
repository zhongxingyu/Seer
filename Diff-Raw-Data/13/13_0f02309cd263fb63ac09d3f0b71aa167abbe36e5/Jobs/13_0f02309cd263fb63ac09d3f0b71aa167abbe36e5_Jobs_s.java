 package com.cghio.easyjobs;
 
 import android.app.AlertDialog;
 import android.content.ActivityNotFoundException;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.net.Uri;
 import android.os.Bundle;
 import android.support.v7.app.ActionBar;
 import android.util.Base64;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 import android.widget.Toast;
 
 import com.loopj.android.http.AsyncHttpClient;
 import com.loopj.android.http.AsyncHttpResponseHandler;
 import com.loopj.android.http.RequestParams;
 
 import org.apache.http.Header;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 public class Jobs extends EasyJobsBase {
 
     private static String PREF_FILE = "auth_info";
 
     private static String PREF_SELECTED = "selected";
 
     private static String PREF_V = "v";
     private static String PREF_U = "u";
     private static String PREF_C = "c";
 
     private class API {
         public String shared_preference_key = "";
 
         public String host = "";
         public String help_url = "";
         public String token = "";
 
         public String jobs_index_url = "";
 
         public String jobs_show_url = "";
         public String jobs_run_url = "";
         public String jobs_parameters_index_url = "";
         public String token_login_url = "";
         public String token_revoke_url = "";
     }
 
     private static List<API> APIs = new ArrayList<API>();
     private static int API_Index = 0;
 
    private static boolean launched = false;

     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_jobs);
 
        startEasyJobs(getLastSelectedAPI());

        if (!launched) { // first time launch from link
            onNewIntent(getIntent());
        }
        launched = true;
     }
 
     @Override
     protected void onNewIntent(Intent intent) {
         super.onNewIntent(intent);
         String fromURI = null;
         if (intent != null) {
             Uri data = intent.getData();
             if (data != null) {
                 fromURI = data.getSchemeSpecificPart();
             }
         }
 
         if (fromURI != null) {
             decode(fromURI.substring(2));
         }
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.reload_menu, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.reload:
                 API api = APIs.get(API_Index);
                 removeEtagContent(api.jobs_index_url);
                 startEasyJobs(null);
                 return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
 
     private void startEasyJobs(String useAPIWithThisURL) {
         SharedPreferences sharedPrefs = getSharedPreferences(PREF_FILE, 0);
 
         clearAPIs();
         Map<String,?> keys = sharedPrefs.getAll();
         if (keys == null) return;
         for (Map.Entry<String,?> entry : keys.entrySet()) {
             if (entry.getKey().length() > 10) {
                 try {
                     String value = new String(Base64.decode(
                             (String) entry.getValue(), Base64.NO_WRAP), "UTF-8");
                     JSONObject object = new JSONObject(value);
                     int VERSION = object.getInt(PREF_V);
                     String URL = object.getString(PREF_U);
                     String CONTENT = object.getString(PREF_C);
 
                     // validate version number
                     if (VERSION <= 0) break;
                     if (VERSION > MAX_API_VERSION) break;
 
                     // validate help URL
                     if (URL.length() == 0) break;
                     try {
                         java.net.URL url = new URL(URL);
                         url.toURI();
                     } catch (Exception e) {
                         break;
                     }
 
                     if (CONTENT.length() == 0) break;
 
                     Uri uri = Uri.parse(URL);
                     String host = uri.getHost();
                     if (uri.getPort() > 0 && uri.getPort() != 80) {
                         host += ":" + uri.getPort();
                     }
 
                     API api = new API();
                     api.shared_preference_key = entry.getKey();
                     api.host = host;
                     api.help_url = URL;
                     api.token = CONTENT;
                     APIs.add(api);
                 } catch (Exception e) {
                     break;
                 }
             }
         }
 
         final ActionBar actionBar = getSupportActionBar();
         if (actionBar == null) return;
         if (APIs.size() > 0) {
             actionBar.setDisplayShowTitleEnabled(false);
             actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
             ArrayList<String> dropdownvalues = new ArrayList<String>();
             for (API api : APIs) {
                 dropdownvalues.add(api.host);
             }
             API_Index = indexOfAPIs(useAPIWithThisURL);
             dropdownvalues.add(getString(R.string.scan));
             Context context = actionBar.getThemedContext();
             if (context == null) return;
             ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,
                     R.layout.actionbar_spinner, android.R.id.text1, dropdownvalues);
             adapter.setDropDownViewResource(R.layout.actionbar_spinner_dropdown_item);
             actionBar.setListNavigationCallbacks(adapter, new ActionBar.OnNavigationListener() {
                 @Override
                 public boolean onNavigationItemSelected(int i, long l) {
                     if (i == APIs.size()) {
                         actionBar.setSelectedNavigationItem(API_Index);
                         openScanner();
                     } else {
                         API_Index = i;
                         getHelp();
                     }
                     return true;
                 }
             });
             actionBar.setSelectedNavigationItem(API_Index);
         } else {
             actionBar.setDisplayShowTitleEnabled(true);
             actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
             setTitle(R.string.app_name);
             showScanButton();
         }
     }
 
     private void getHelp() {
         final API api = APIs.get(API_Index);
         if (api.token.length() == 0) return;
         RequestParams params = new RequestParams();
         params.put("token", api.token);
         AsyncHttpClient client = new AsyncHttpClient();
         client.setTimeout(TIMEOUT);
         showLoading();
         client.get(api.help_url, params, new AsyncHttpResponseHandler() {
             @Override
             public void onFailure(Throwable e, String response) {
                 hideLoading();
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
                 showReloadAndScanButton();
             }
             @Override
             public void onSuccess(String response) {
                 hideLoading();
                 try {
                     JSONObject helpObj = new JSONObject(response);
                     JSONObject jobsObj = helpObj.getJSONObject("jobs");
                     JSONObject jobsIndexObj = jobsObj.getJSONObject("index");
 
                     api.jobs_index_url = jobsIndexObj.getString("url");
 
                     JSONObject jobsShowObj = jobsObj.getJSONObject("show");
                     api.jobs_show_url = jobsShowObj.getString("url");
 
                     JSONObject jobsRunObj = jobsObj.getJSONObject("run");
                     api.jobs_run_url = jobsRunObj.getString("url");
 
                     JSONObject jobsParamsObj = helpObj.getJSONObject("job_parameters");
                     JSONObject jobsParamsIndexObj = jobsParamsObj.getJSONObject("index");
                     api.jobs_parameters_index_url = jobsParamsIndexObj.getString("url");
 
                     JSONObject tokensObj = helpObj.getJSONObject("tokens");
                     JSONObject tokensRevokeObj = tokensObj.getJSONObject("revoke");
                     api.token_revoke_url = tokensRevokeObj.getString("url");
 
                     JSONObject tokensLoginObj = tokensObj.getJSONObject("login");
                     api.token_login_url = tokensLoginObj.getString("url");
 
                     getJobs();
                 } catch (JSONException e) {
                     showSimpleErrorDialog(getString(R.string.error_should_update_easyjobs));
                     showReloadAndScanButton();
                 }
             }
         });
         saveLastSelectedAPI(api.help_url);
     }
 
     private void getJobs() {
         final API api = APIs.get(API_Index);
         if (api.jobs_index_url.length() == 0) return;
 
         String cachedContent = getEtagContent(api.jobs_index_url);
         if (cachedContent.length() > 0) {
             parseContent(cachedContent);
         }
 
         AsyncHttpClient client = new AsyncHttpClient();
         RequestParams params = new RequestParams();
         params.put("token", api.token);
         client.setTimeout(TIMEOUT);
         showLoading();
 
         client.addHeader(IF_NONE_MATCH, getEtag(api.jobs_index_url));
 
         client.get(api.jobs_index_url, params, new AsyncHttpResponseHandler() {
             @Override
             public void onFailure(Throwable e, String response) {
                 hideLoading();
                 if (isNotModified(e)) return;
                 if (e != null && e.getCause() != null) {
                     showSimpleErrorDialog(e.getCause().getMessage());
                 } else if (e != null && e.getCause() == null) {
                     showSimpleErrorDialog(e.getMessage());
                 } else {
                     showSimpleErrorDialog(getString(R.string.error_connection_problem));
                 }
                 showReloadAndScanButton();
             }
             @Override
             public void onSuccess(int statusCode, Header[] headers, String content) {
                 hideLoading();
                 String etag = getHeader(headers, ETAG);
                 saveETagAndContent(api.jobs_index_url, etag, content);
                 parseContent(content);
             }
         });
     }
 
     private void parseContent(String content) {
         try {
             List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
 
             JSONArray jobs = new JSONArray(content);
             for (int i = 0; i < jobs.length(); i++) {
                 JSONObject object = jobs.getJSONObject(i);
                 Map<String, Object> map = new HashMap<String, Object>();
                 map.put("ID", object.getInt("id"));
                 map.put("KEY", object.getString("name"));
                 String server = object.getString("server_name");
                 if (server.equals("null")) server = getString(R.string.no_server);
                 map.put("VALUE", server);
                 int type_id = 0;
                 if (!object.isNull("type_id")) type_id = object.getInt("type_id");
                 map.put("TYPE_ID", type_id);
                 String type_name = object.getString("type_name");
                 if (type_name.equals("null")) type_name = getString(R.string.orphans);
                 map.put("TYPE_NAME", type_name);
                 data.add(map);
             }
 
             Collections.sort(data, new Comparator<Map<String, Object>>() {
                 @Override
                 public int compare(Map<String, Object> obj1, Map<String, Object> obj2) {
                     return Integer.parseInt(obj1.get("TYPE_ID").toString()) -
                             Integer.parseInt(obj2.get("TYPE_ID").toString());
                 }
             });
 
             String last_type_name = null;
             for (int i = 0; i < data.size(); i++) {
                 Map<String, Object> object = data.get(i);
                 String type_name = object.get("TYPE_NAME").toString();
                 if (type_name.equals(last_type_name)) continue;
                 Map<String, Object> map = new HashMap<String, Object>();
                 map.put("KEY", type_name);
                 data.add(i, map);
                 last_type_name = type_name;
             }
 
             {
                 Map<String, Object> map = new HashMap<String, Object>();
                 map.put("KEY", getString(R.string.actions));
                 data.add(map);
             }
 
             int[] other_buttons_text = {R.string.browse_web_page, R.string.revoke_access};
             int[] other_buttons_desc = {R.string.browse_web_page_desc, R.string.revoke_access_desc};
 
             for (int i = 0; i < other_buttons_text.length; i++) {
                 Map<String, Object> map = new HashMap<String, Object>();
                 map.put("KEY", getString(other_buttons_text[i]));
                 map.put("VALUE", getString(other_buttons_desc[i]));
                 data.add(map);
             }
 
             EasyJobsAdapter adapter = new EasyJobsAdapter(Jobs.this, R.layout.listview_jobs_items,
                     data);
             ListView listview_jobs = (ListView) findViewById(R.id.listView_jobs);
             listview_jobs.setAdapter(adapter);
             listview_jobs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                 @Override
                 public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                     switch (i - adapterView.getCount()) {
                         case -1:
                             toRevokeAccess();
                             break;
                         case -2:
                             toBrowseWebPage();
                             break;
                         default:
                             API api = APIs.get(API_Index);
                             Object item = adapterView.getAdapter().getItem(i);
                             if (item instanceof Map) {
                                 if (!((Map) item).containsKey("ID")) break;
                                 int ID = Integer.parseInt(((Map) item).get("ID").toString());
                                 Intent intent = new Intent(Jobs.this, JobsDetails.class);
                                 intent.putExtra("API_TOKEN", api.token);
                                 intent.putExtra("JOB_ID", ID);
                                 intent.putExtra("JOBS_SHOW_URL", api.jobs_show_url);
                                 intent.putExtra("JOBS_RUN_URL", api.jobs_run_url);
                                 intent.putExtra("JOBS_PARAMETERS_INDEX_URL",
                                         api.jobs_parameters_index_url);
                                 Jobs.this.startActivity(intent);
                             }
                     }
                 }
             });
             listview_jobs.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                 @Override
                 public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                     Object item = adapterView.getAdapter().getItem(i);
                     if (item instanceof Map) {
                         if (((Map) item).containsKey("KEY")) {
                             copyText(((Map) item).get("KEY").toString());
                             Toast.makeText(Jobs.this, R.string.string_copied, Toast.LENGTH_SHORT).show();
                             return true;
                         }
                     }
                     return false;
                 }
             });
         } catch (JSONException e) {
             showSimpleErrorDialog(getString(R.string.error_should_update_easyjobs));
             showReloadAndScanButton();
         }
     }
 
     private void showScanButton() {
         List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
         Map<String, Object> map = new HashMap<String, Object>();
         map.put("K", getString(R.string.scan));
         map.put("V", getString(R.string.scan_desc));
         data.add(map);
         map = new HashMap<String, Object>();
         map.put("K", getString(R.string.about));
         map.put("V", getString(R.string.about_desc));
         data.add(map);
         SimpleAdapter adapter = new SimpleAdapter(Jobs.this, data,
                 R.layout.listview_jobs_items, new String[]{"K", "V"},
                 new int[]{R.id.text_key, R.id.text_value});
         ListView listview_jobs = (ListView) findViewById(R.id.listView_jobs);
         listview_jobs.setAdapter(adapter);
         listview_jobs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                 switch (i) {
                     case 0:
                         openScanner();
                         break;
                     case 1:
                         showAboutInfo();
                         break;
                 }
             }
         });
     }
 
     private void showReloadAndScanButton() {
         List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
         Map<String, Object> map = new HashMap<String, Object>();
         map.put("K", getString(R.string.retry));
         map.put("V", getString(R.string.retry_desc));
         data.add(map);
         map = new HashMap<String, Object>();
         map.put("K", getString(R.string.scan));
         map.put("V", getString(R.string.scan_desc));
         data.add(map);
         map = new HashMap<String, Object>();
         map.put("K", getString(R.string.revoke_access));
         map.put("V", getString(R.string.revoke_access_desc));
         data.add(map);
         SimpleAdapter adapter = new SimpleAdapter(Jobs.this, data,
                 R.layout.listview_jobs_items, new String[]{"K", "V"},
                 new int[]{R.id.text_key, R.id.text_value});
         ListView listview_jobs = (ListView) findViewById(R.id.listView_jobs);
         listview_jobs.setAdapter(adapter);
         listview_jobs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                 switch (i) {
                     case 0:
                         startEasyJobs(null);
                         break;
                     case 1:
                         openScanner();
                         break;
                     case 2:
                         toRevokeAccess();
                         break;
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
             decode(content);
         }
     }
 
     private void decode(String content) {
         String decoded_content = null;
         try {
             byte[] decoded = Base64.decode(content, Base64.NO_WRAP);
             decoded_content = new String(decoded);
         } catch (IllegalArgumentException e) {
             showSimpleErrorDialog(getString(R.string.error_invalid_qrcode));
         }
         if (decoded_content != null) {
             try {
                 JSONObject object = new JSONObject(decoded_content);
                 int VERSION = object.getInt(PREF_V);
                 String URL = object.getString(PREF_U);
                 String CONTENT = object.getString(PREF_C);
 
                 // validate version number
 
                 if (VERSION <= 0) throw new JSONException(null);
                 if (VERSION > MAX_API_VERSION)
                     throw new Exception(getString(R.string.error_please_update_app));
 
                 // validate help URL
                 URL url = new URL(URL);
                 url.toURI(); // stop never used warning
 
                 if (CONTENT.length() == 0)
                     throw new Exception(getString(R.string.error_invalid_qrcode));
 
                 SharedPreferences sharedPrefs = getSharedPreferences(PREF_FILE, 0);
                 SharedPreferences.Editor editor = sharedPrefs.edit();
                 editor.putString(Base64.encodeToString(URL.getBytes(), Base64.NO_WRAP).toLowerCase(),
                         Base64.encodeToString(object.toString().getBytes(), Base64.NO_WRAP));
                 editor.commit();
 
                 startEasyJobs(URL);
             } catch (MalformedURLException e) {
                 showSimpleErrorDialog(getString(R.string.error_invalid_url));
             } catch (JSONException e) {
                 showSimpleErrorDialog(getString(R.string.error_invalid_qrcode));
             } catch (Exception e) {
                 showSimpleErrorDialog(e.getMessage());
             }
         }
     }
 
     private void toBrowseWebPage() {
         AlertDialog.Builder builder = new AlertDialog.Builder(this);
         builder.setMessage(R.string.confirm_browse_webpage).setPositiveButton(R.string.yes,
                 new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialogInterface, int i) {
                         API api = APIs.get(API_Index);
                         Intent intent = new Intent(Intent.ACTION_VIEW,
                                 Uri.parse(api.token_login_url.replace(":token", api.token)));
                         startActivity(intent);
                         revokeAccessWithoutSendingRevokeAccessRequest();
                     }
                 }).setNegativeButton(R.string.no, null).show();
     }
 
     private void toRevokeAccess() {
         AlertDialog.Builder builder = new AlertDialog.Builder(this);
         builder.setMessage(R.string.confirm_revoke_access).setPositiveButton(R.string.yes,
                 new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialogInterface, int i) {
                         revokeAccess();
                     }
                 }).setNegativeButton(R.string.no, null).show();
     }
 
     private void removeAccessCredentialsOnly() {
         API api = APIs.get(API_Index);
         SharedPreferences sharedPrefs = getSharedPreferences(PREF_FILE, 0);
         SharedPreferences.Editor editor = sharedPrefs.edit();
         editor.remove(api.shared_preference_key);
         editor.commit();
 
         clearEtags();
     }
 
     private void revokeAccessOnly() {
         removeAccessCredentialsOnly();
 
         API api = APIs.get(API_Index);
         AsyncHttpClient client = new AsyncHttpClient();
         client.delete(api.token_revoke_url + "?token=" + api.token, new AsyncHttpResponseHandler() {
             @Override
             public void onSuccess(String response) {
             }
         });
     }
 
     private void revokeAccessWithoutSendingRevokeAccessRequest() {
         removeAccessCredentialsOnly();
         clearAPIs();
         startEasyJobs(null);
     }
 
     private void revokeAccess() {
         revokeAccessOnly();
         clearAPIs();
         startEasyJobs(null);
     }
 
     private int indexOfAPIs(String URL) {
         if (URL == null || URL.equals("null")) return 0;
         for (int i = 0; i < APIs.size(); i++) {
             if (APIs.get(i).help_url.equals(URL)) {
                 return i;
             }
         }
         return 0;
     }
 
     private void clearAPIs() {
         APIs.clear();
         API_Index = 0;
     }
 
     private void saveLastSelectedAPI(String URL) {
         SharedPreferences sharedPrefs = getSharedPreferences(PREF_FILE, 0);
         SharedPreferences.Editor editor = sharedPrefs.edit();
         editor.putString(PREF_SELECTED, Base64.encodeToString(URL.getBytes(), Base64.NO_WRAP));
         editor.commit();
     }
     private String getLastSelectedAPI() {
         SharedPreferences sharedPrefs = getSharedPreferences(PREF_FILE, 0);
         String content = sharedPrefs.getString(PREF_SELECTED, "");
         if (content.length() > 0) {
             content = new String(Base64.decode(content, Base64.NO_WRAP));
         }
         return content;
     }
 }
