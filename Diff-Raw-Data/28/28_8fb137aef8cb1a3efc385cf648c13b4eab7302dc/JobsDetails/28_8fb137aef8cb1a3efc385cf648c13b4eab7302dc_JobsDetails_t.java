 package com.cghio.easyjobs;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.os.Bundle;
 import android.os.Message;
 import android.widget.ListView;
 
 import com.loopj.android.http.AsyncHttpClient;
 import com.loopj.android.http.AsyncHttpResponseHandler;
 import com.loopj.android.http.RequestParams;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 public class JobsDetails extends Activity {
 
     public static int JOBS_DETAILS_ID = 0;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_jobs_details);
         Bundle extras = getIntent().getExtras();
         if (extras != null && extras.containsKey("JOB_ID")) {
             JOBS_DETAILS_ID = extras.getInt("JOB_ID");
             if (JOBS_DETAILS_ID > 0) {
                 getJobDetails();
             }
         }
     }
 
     private void getJobDetails() {
         if (JOBS_DETAILS_ID == 0 || Home.API_TOKEN.length() == 0 ||Jobs.JOBS_SHOW_URL.length() == 0)
             return;
 
         try {
             RequestParams params = new RequestParams();
             params.put("token", Home.API_TOKEN);
             AsyncHttpClient client = new AsyncHttpClient();
             String url = Jobs.JOBS_SHOW_URL;
             url = url.replace(":id", JOBS_DETAILS_ID+"");
             client.get(url, params, new AsyncHttpResponseHandler() {
                 @Override
                 public void onSuccess(final String response) {
                     try {
                         List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
 
                         JSONObject obj = new JSONObject(response);
                         JSONObject job = obj.getJSONObject("job");
 
                         String[] Keys = new String[]{"name", "script", "id", "created_at",
                                 "updated_at", "mean_time"};
                         String[] Names = new String[]{"Name", "Script", "ID", "Created at",
                                 "Updated at", "Mean time"};
 
                         for (int i = 0; i < Keys.length; i++) {
                             Map<String, Object> map = new HashMap<String, Object>();
                             map.put("KEY", Names[i]);
                             map.put("VALUE", job.getString(Keys[i]));
                             data.add(map);
                         }
 
                         JSONObject interpreter = obj.optJSONObject("interpreter");
 
                         if (interpreter == null) {
                             Map<String, Object> map = new HashMap<String, Object>();
                             map.put("KEY", "Interpreter");
                             map.put("VALUE", "(default)");
                             data.add(map);
                         } else {
                             Keys = new String[]{"path", "upload_script_first"};
                             Names = new String[]{"Interpreter", "Upload script first?"};
 
                             for (int i = 0; i < Keys.length; i++) {
                                 Map<String, Object> map = new HashMap<String, Object>();
                                 map.put("KEY", Names[i]);
                                 map.put("VALUE", interpreter.getString(Keys[i]));
                                 data.add(map);
                             }
                         }
 
                        JSONObject server = obj.optJSONObject("server");
 
                        if (server == null) {
                             Map<String, Object> map = new HashMap<String, Object>();
                            map.put("KEY", "Server");
                            map.put("VALUE", getString(R.string.no_server));
                             data.add(map);
                        } else {
                            Keys = new String[]{"name", "host", "username", "created_at",
                                    "updated_at"};
                            Names = new String[]{"Server Name", "Host", "Username", "Server created at",
                                    "Server updated at"};

                            for (int i = 0; i < Keys.length; i++) {
                                Map<String, Object> map = new HashMap<String, Object>();
                                map.put("KEY", Names[i]);
                                map.put("VALUE", server.getString(Keys[i]));
                                data.add(map);
                            }
                         }
 
                         JobsDetailsAdapter adapter = new JobsDetailsAdapter(JobsDetails.this,
                                 R.layout.listview_jobs_details_items, data);
                         ListView listview_jobs_details =
                                 (ListView) findViewById(R.id.listview_jobs_details);
                         listview_jobs_details.setAdapter(adapter);
                     } catch (JSONException e) {
                         showSimpleErrorDialog(getString(R.string.error_unspecified));
                     }
                 }
             });
         } catch (Exception e) {
             showSimpleErrorDialog(getString(R.string.error_unspecified));
         }
     }
 
     private void showSimpleErrorDialog(String message) {
         AlertDialog alertDialog = new AlertDialog.Builder(this).create();
         alertDialog.setMessage(message);
         alertDialog.setTitle(R.string.error);
         alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.ok), (Message) null);
         alertDialog.show();
     }
 }
