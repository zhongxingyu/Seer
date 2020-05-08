 package com.cghio.easyjobs;
 
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.text.format.DateUtils;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ListView;
 import android.widget.Toast;
 
 import com.loopj.android.http.AsyncHttpClient;
 import com.loopj.android.http.AsyncHttpResponseHandler;
 import com.loopj.android.http.RequestParams;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.TimeZone;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class JobsDetails extends EasyJobsBase {
 
     private static String API_TOKEN = "";
     private static int JOBS_DETAILS_ID = 0;
     private static String JOBS_SHOW_URL = "";
     private static String JOBS_RUN_URL = "";
 
     private static String JOBS_PARAMETERS_INDEX_URL = "";
 
     private static String jobScript = "";
     private static boolean jobHasNoInterpreter = false;
 
     private static String NOT_DEFINED = "(not defined)\n";
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_jobs_details);
         Bundle extras = getIntent().getExtras();
         if (extras != null) {
             if (extras.containsKey("API_TOKEN")) {
                 API_TOKEN = extras.getString("API_TOKEN");
             }
             if (extras.containsKey("JOBS_SHOW_URL")) {
                 JOBS_SHOW_URL = extras.getString("JOBS_SHOW_URL");
             }
             if (extras.containsKey("JOBS_RUN_URL")) {
                 JOBS_RUN_URL = extras.getString("JOBS_RUN_URL");
             }
             if (extras.containsKey("JOBS_PARAMETERS_INDEX_URL")) {
                 JOBS_PARAMETERS_INDEX_URL = extras.getString("JOBS_PARAMETERS_INDEX_URL");
             }
             if (extras.containsKey("JOB_ID")) {
                 JOBS_DETAILS_ID = extras.getInt("JOB_ID");
                 if (JOBS_DETAILS_ID > 0) {
                     getJobDetails();
                 }
             }
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
                 getJobDetails();
                 return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
 
     private void getJobDetails() {
         if (JOBS_DETAILS_ID == 0 || API_TOKEN.length() == 0 || JOBS_SHOW_URL.length() == 0)
             return;
 
         try {
             RequestParams params = new RequestParams();
             params.put("token", API_TOKEN);
             AsyncHttpClient client = new AsyncHttpClient();
             String url = JOBS_SHOW_URL;
             url = url.replace(":id", JOBS_DETAILS_ID+"");
             client.setTimeout(TIMEOUT);
             showLoading();
             client.get(url, params, new AsyncHttpResponseHandler() {
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
                     showReloadButton();
                 }
                 @Override
                 public void onSuccess(final String response) {
                     try {
                         List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
 
                         JSONObject obj = new JSONObject(response);
                         JSONObject job = obj.getJSONObject("job");
 
                         String[] Keys = getResources().getStringArray(R.array.jobs_keys);
                         String[] Names = getResources().getStringArray(R.array.jobs_values);
 
                         for (int i = 0; i < Keys.length; i++) {
                             Map<String, Object> map = new HashMap<String, Object>();
                             map.put("KEY", Names[i]);
                             if (Keys[i].matches(".*at")) {
                                 map.put("VALUE", toRelativeDateTime(job.getString(Keys[i])));
                             } else {
                                 map.put("VALUE", job.getString(Keys[i]));
                             }
                             data.add(map);
                         }
 
                         jobScript = job.getString("script");
 
                         JSONObject interpreter = obj.optJSONObject("interpreter");
 
                         if (interpreter == null) {
                             Map<String, Object> map = new HashMap<String, Object>();
                             map.put("KEY", getString(R.string.interpreter));
                             map.put("VALUE", getString(R.string.default_interpreter));
                             data.add(map);
 
                             jobHasNoInterpreter = true;
                         } else {
                             Keys = getResources().getStringArray(R.array.interpreters_keys);
                             Names = getResources().getStringArray(R.array.interpreters_values);
 
                             for (int i = 0; i < Keys.length; i++) {
                                 Map<String, Object> map = new HashMap<String, Object>();
                                 map.put("KEY", Names[i]);
                                 map.put("VALUE", interpreter.getString(Keys[i]));
                                 data.add(map);
                             }
                         }
 
                         {
                             Map<String, Object> map = new HashMap<String, Object>();
                             map.put("KEY", getString(R.string.server));
                             data.add(map);
                         }
 
                         JSONObject server = obj.optJSONObject("server");
 
                         if (server == null) {
                             Map<String, Object> map = new HashMap<String, Object>();
                             map.put("KEY", getString(R.string.server));
                             map.put("VALUE", getString(R.string.no_server));
                             data.add(map);
                         } else {
                             Keys = getResources().getStringArray(R.array.servers_keys);
                             Names = getResources().getStringArray(R.array.servers_values);
 
                             for (int i = 0; i < Keys.length; i++) {
                                 Map<String, Object> map = new HashMap<String, Object>();
                                 map.put("KEY", Names[i]);
                                 if (Keys[i].matches(".*at")) {
                                     map.put("VALUE", toRelativeDateTime(server.getString(Keys[i])));
                                 } else {
                                     map.put("VALUE", server.getString(Keys[i]));
                                 }
                                 data.add(map);
                             }
                         }
 
                         {
                             Map<String, Object> map = new HashMap<String, Object>();
                             map.put("KEY", getString(R.string.run));
                             data.add(map);
                         }
 
                         if (jobHasNoInterpreter) {
                             Pattern replace = Pattern.compile("[^\\%]?\\%\\{(.*?)\\}");
                             Matcher matcher = replace.matcher(jobScript);
                             List<String> list = new ArrayList<String>();
                             while (matcher.find()) {
                                 list.add(matcher.group(1));
                             }
                             list = new ArrayList<String>(new HashSet<String>(list));
                             for (String match : list) {
                                 Map<String, Object> map = new HashMap<String, Object>();
                                 map.put("PARAM", match);
                                 map.put("KEY", String.format(getString(R.string.variable_x), match));
                                 map.put("VALUE", NOT_DEFINED);
                                 data.add(map);
                             }
                         }
 
                         String hash = obj.optString("hash", "");
 
                         if (hash.length() == 0) {
                             Map<String, Object> map = new HashMap<String, Object>();
                             map.put("KEY", getString(R.string.run));
                             map.put("VALUE", getString(R.string.run_no));
                             data.add(map);
                         } else {
                             Map<String, Object> map = new HashMap<String, Object>();
                             map.put("HASH", hash);
                             map.put("KEY", getString(R.string.run));
                             map.put("VALUE", getString(R.string.run_now));
                             data.add(map);
                         }
 
                         JobsDetailsAdapter adapter = new JobsDetailsAdapter(JobsDetails.this,
                                 R.layout.listview_jobs_details_items, data);
                         ListView listview_jobs_details =
                                 (ListView) findViewById(R.id.listview_jobs_details);
                         listview_jobs_details.setAdapter(adapter);
                         listview_jobs_details.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                             @Override
                             public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                 Object item = adapterView.getAdapter().getItem(i);
                                 if (item instanceof Map) {
                                     if (((Map) item).containsKey("HASH")) {
                                         String hash = ((Map) item).get("HASH").toString();
                                         toRunJob(hash);
                                     }
                                     if (((Map) item).containsKey("PARAM")) {
                                         showParams(((Map) item).get("PARAM").toString(),
                                                 ((Map) item).get("VALUE").toString());
                                     }
                                 }
                             }
                         });
                         listview_jobs_details.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                             @Override
                             public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                                 Object item = adapterView.getAdapter().getItem(i);
                                 if (item instanceof Map) {
                                     if (((Map) item).containsKey("VALUE")) {
                                         copyText(((Map) item).get("VALUE").toString());
                                         Toast.makeText(JobsDetails.this, R.string.string_copied, Toast.LENGTH_SHORT).show();
                                         return true;
                                     }
                                 }
                                 return false;
                             }
                         });
                     } catch (JSONException e) {
                         showSimpleErrorDialog(getString(R.string.error_unspecified));
                     }
                 }
             });
         } catch (Exception e) {
             showSimpleErrorDialog(getString(R.string.error_unspecified));
         }
     }
 
     private void showReloadButton() {
         List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
         Map<String, Object> map = new HashMap<String, Object>();
         map.put("KEY", getString(R.string.error_connecting));
         map.put("VALUE", getString(R.string.retry));
         data.add(map);
         JobsDetailsAdapter adapter = new JobsDetailsAdapter(JobsDetails.this,
                 R.layout.listview_jobs_details_items, data);
         ListView listview_jobs_details =
                 (ListView) findViewById(R.id.listview_jobs_details);
         listview_jobs_details.setAdapter(adapter);
         listview_jobs_details.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                 getJobDetails();
             }
         });
     }
 
     private void toRunJob(final String hash) {
         AlertDialog.Builder builder = new AlertDialog.Builder(this);
         builder.setMessage(R.string.confirm_run_job).setPositiveButton(R.string.yes,
                 new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialogInterface, int i) {
                         runJob(hash);
                     }
                 }).setNegativeButton(R.string.no, null).show();
     }
 
     private void runJob(String hash) {
         String url = JOBS_RUN_URL;
         url = url.replace(":id", JOBS_DETAILS_ID+"");
         if (hash.length() > 0) {
             url = url + "?hash=" + hash;
             url = url + "&token=" + API_TOKEN;
             String params = collectParams();
             if (params.length() > 0) url += params;
             Intent intent = new Intent(JobsDetails.this, RunJob.class);
             intent.putExtra("URL", url);
             JobsDetails.this.startActivity(intent);
         }
     }
 
     private String collectParams() {
         String params = "";
         ListView listview_jobs_details =
                 (ListView) findViewById(R.id.listview_jobs_details);
         JobsDetailsAdapter adapter = (JobsDetailsAdapter) listview_jobs_details.getAdapter();
         if (adapter != null) {
             for (int i = 0; i < adapter.getCount(); i++) {
                 Object item = adapter.getItem(i);
                 if (item != null) {
                     Map map_item = (Map) item;
                     if (map_item.containsKey("PARAM") && map_item.containsKey("VALUE")) {
                         if (!map_item.get("VALUE").toString().equals(NOT_DEFINED)) {
                             try {
                                 params += "&parameters[" + map_item.get("PARAM").toString() + "]=" +
                                         URLEncoder.encode(map_item.get("VALUE").toString(), "utf-8");
                             } catch (UnsupportedEncodingException e) {
                                 e.printStackTrace();
                             }
                         }
                     }
                 }
             }
         }
         return params;
     }
 
     private void showParams(String param, String defaultValue) {
         Intent intent = new Intent(JobsDetails.this, Parameters.class);
         intent.putExtra("JOBS_PARAMETERS_INDEX_URL", JOBS_PARAMETERS_INDEX_URL);
         intent.putExtra("API_TOKEN", API_TOKEN);
         if (defaultValue.equals(NOT_DEFINED)) defaultValue = "";
         intent.putExtra("DEFAULT", defaultValue);
         intent.putExtra("PARAM", param);
         JobsDetails.this.startActivityForResult(intent, 1);
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         if (requestCode == 1) {
             if (resultCode == RESULT_OK) {
                 String key = "";
                 String value = "";
                 if (data != null) {
                     Bundle extra = data.getExtras();
                     if (extra != null) {
                         key = extra.getString("key");
                         value = extra.getString("value");
                     }
                 }
                 if (key == null || key.length() == 0) return;
                 if (value == null || value.length() == 0) value = NOT_DEFINED;
                 ListView listview_jobs_details =
                         (ListView) findViewById(R.id.listview_jobs_details);
                 JobsDetailsAdapter adapter = (JobsDetailsAdapter) listview_jobs_details.getAdapter();
                 if (adapter != null) {
                     for (int i = 0; i < adapter.getCount(); i++) {
                         if (adapter.getItem(i) != null) {
                             Map<String, Object> map_item = adapter.getItem(i);
                             if (map_item.containsKey("PARAM") &&
                                     map_item.get("PARAM").toString().equals(key)) {
                                 map_item.put("VALUE", value);
                             }
                         }
                     }
                     adapter.notifyDataSetChanged();
                 }
             }
         }
     }
 
     private String toRelativeDateTime(String text) {
         DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
         format.setTimeZone(TimeZone.getTimeZone("UTC"));
         try {
             text = String.valueOf(DateUtils.getRelativeTimeSpanString(
                     format.parse(text).getTime(), (new Date()).getTime(), 0));
         } catch (ParseException e) {
             e.printStackTrace();
         }
         return text;
     }
 
 }
