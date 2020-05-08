 package ca.q0r.kfreqs.app.tabs;
 
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.*;
 import ca.q0r.kfreqs.app.R;
 import ca.q0r.kfreqs.app.util.Utils;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.util.EntityUtils;
 import org.json.simple.JSONArray;
 import org.json.simple.JSONObject;
 import org.json.simple.parser.JSONParser;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.util.ArrayList;
 import java.util.Map;
 import java.util.Properties;
 
 public class DownloadTab extends Fragment implements View.OnClickListener {
     public String pName = "";
     public String key = "";
     public String value = "";
     ArrayList<String> list;
 
     @Override
     public void onActivityCreated(Bundle savedInstanceState) {
         super.onActivityCreated(savedInstanceState);
 
         list = new ArrayList<String>();
 
         LinearLayout uploadDecline = (LinearLayout) getView().findViewById(R.id.layout_dc);
         uploadDecline.setVisibility(LinearLayout.GONE);
 
         final Button button = (Button) uploadDecline.findViewById(R.id.button_remote_download);
         button.setOnClickListener(this);
 
         final Button button2 = (Button) uploadDecline.findViewById(R.id.button_remote_cancel);
         button2.setOnClickListener(this);
 
         ListView lv = (ListView) getView().findViewById(R.id.view_remote_profiles);
 
         TextView vw = new TextView(getView().getContext());
 
         lv.addHeaderView(vw);
 
         InfoTask task = new InfoTask();
 
         task.execute("");
 
         ArrayAdapter<String>  adapter = new ArrayAdapter<String>(getView().getContext(), android.R.layout.simple_list_item_1, list);
 
         lv.setAdapter(adapter);
 
         lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> parent, final View view,
                                     int position, long id) {
                 LinearLayout uploadDecline = (LinearLayout) getView().findViewById(R.id.layout_dc);
 
                 pName = list.get(position - 1);
 
                 uploadDecline.setVisibility(LinearLayout.VISIBLE);
             }
 
         });

        //TODO: Finish Download Tab

     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         return inflater.inflate(R.layout.download_tab, container, false);
     }
 
     /*----------------------------
     ---------  onClick  ---------
     ----------------------------*/
 
     @Override
     public void onClick(View v) {
         LinearLayout uploadDecline = (LinearLayout) getView().findViewById(R.id.layout_dc);
 
         int id = v.getId();
 
         if (id == R.id.button_remote_download) {
             uploadDecline.setVisibility(LinearLayout.GONE);
 
             DownloadTask dTask = new DownloadTask();
             dTask.execute("");
 
             WriteTask wTask = new WriteTask();
             wTask.execute(key, value);
         } else if (id == R.id.button_remote_cancel) {
             uploadDecline.setVisibility(LinearLayout.GONE);
         } /*else {
             uploadDecline.setVisibility(LinearLayout.VISIBLE);
         }*/
     }
 
     /*----------------------------
     -----------  Tasks  -----------
     ----------------------------*/
 
     private class InfoTask extends AsyncTask<String, Void, ArrayList<Object>> {
         private ProgressDialog pDialog;
 
         @Override
         protected void onPreExecute() {
             pDialog = new ProgressDialog(getView().getContext());
 
             pDialog.setTitle("Downloading Info");
             pDialog.setMessage("Please wait...");
             pDialog.setIndeterminate(true);
             pDialog.show();
         }
 
         @Override
         protected ArrayList<Object> doInBackground(String... strings) {
             ArrayList<Object> list = new ArrayList<Object>();
 
             ConnectivityManager connMgr = (ConnectivityManager)
                     DownloadTab.this.getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
 
             NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
 
             if (networkInfo != null && networkInfo.isConnected()) {
                 DefaultHttpClient httpclient = new DefaultHttpClient();
 
                 try {
                     HttpPost httpPostRequest = new HttpPost("http://q0r.ca/abb/download.php");
 
                     ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
 
                     nameValuePairs.add(new BasicNameValuePair("keys", "?"));
 
                     httpPostRequest.setEntity(new UrlEncodedFormEntity(nameValuePairs));
 
                     HttpResponse response = httpclient.execute(httpPostRequest);
 
                     HttpEntity entity = response.getEntity();
 
                     JSONParser parser = new JSONParser();
 
                     Object obj = parser.parse(EntityUtils.toString(entity));
 
                     list.add(obj);
                 } catch (Exception e) {
                     e.printStackTrace();
                 } finally {
                     httpclient.getConnectionManager().shutdown();
                 }
             } else {
                 pDialog.setMessage("No Internet Connection Found!");
                 pDialog.cancel();
             }
 
             return list;
         }
 
         @Override
         protected void onPostExecute(ArrayList<Object> list) {
             pDialog.setMessage("Info Download Complete!");
             pDialog.cancel();
 
             Toast toast = Toast.makeText(DownloadTab.this.getView().getContext(), "", Toast.LENGTH_LONG);
 
             toast.setDuration(Toast.LENGTH_LONG);
 
             toast.setText("Info Download Not Successful!");
 
             for (Object obj : list) {
                 toast.setText("Info Download Successful!");
 
                 if (obj instanceof JSONArray) {
                     JSONArray jArr = (JSONArray) obj;
 
                     for (Object o : jArr) {
                         DownloadTab.this.list.add(o.toString());
                     }
                 }
             }
 
             toast.show();
         }
     }
 
     private class DownloadTask extends AsyncTask<String, Void, ArrayList<Object>> {
         private ProgressDialog pDialog;
 
         @Override
         protected void onPreExecute() {
             pDialog = new ProgressDialog(getView().getContext());
 
             pDialog.setTitle("Downloading Profile");
             pDialog.setMessage("Please wait...");
             pDialog.setIndeterminate(true);
             pDialog.show();
         }
 
         @Override
         protected ArrayList<Object> doInBackground(String... strings) {
             ArrayList<Object> list = new ArrayList<Object>();
 
             ConnectivityManager connMgr = (ConnectivityManager)
                     DownloadTab.this.getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
 
             NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
 
             if (networkInfo != null && networkInfo.isConnected()) {
                 DefaultHttpClient httpclient = new DefaultHttpClient();
 
                 try {
                     HttpPost httpPostRequest = new HttpPost("http://q0r.ca/abb/download.php");
 
                     ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
 
                     nameValuePairs.add(new BasicNameValuePair("values", DownloadTab.this.pName));
 
                     httpPostRequest.setEntity(new UrlEncodedFormEntity(nameValuePairs));
 
                     HttpResponse response = httpclient.execute(httpPostRequest);
 
                     HttpEntity entity = response.getEntity();
 
                     JSONParser parser = new JSONParser();
 
                     Object obj = parser.parse(EntityUtils.toString(entity));
 
                     list.add(obj);
                 } catch (Exception e) {
                     e.printStackTrace();
                 } finally {
                     httpclient.getConnectionManager().shutdown();
                 }
             } else {
                 pDialog.setMessage("No Internet Connection Found!");
                 pDialog.cancel();
             }
 
             return list;
         }
 
         @Override
         protected void onPostExecute(ArrayList<Object> list) {
             pDialog.setMessage("Profile Download Complete!");
             pDialog.cancel();
 
             Toast toast = Toast.makeText(DownloadTab.this.getView().getContext(), "", Toast.LENGTH_LONG);
 
             toast.setDuration(Toast.LENGTH_LONG);
 
             toast.setText("Profile Download Not Successful!");
 
             for (Object oj : list) {
                 JSONObject jObj = null;
 
                 if (oj instanceof JSONArray) {
                     JSONArray jArr = (JSONArray) oj;
 
                     Object obj = jArr.get(0);
 
                     jObj = (JSONObject) obj;
 
                     if (jObj instanceof  JSONObject) {
                         jObj = (JSONObject) obj;
                     }
                 }
 
                 if (jObj != null) {
                     toast.setText("Profile Download Successful!");
 
                     for (Object kV : jObj.entrySet()) {
                         String key = kV.toString().split("=")[0];
                         String value = jObj.get(key).toString();
 
                         DownloadTab.this.key = key;
                         DownloadTab.this.value = value;
                     }
                 }
             }
 
             toast.show();
         }
     }
 
     private class WriteTask extends AsyncTask<String, Void, Boolean> {
         private ProgressDialog pDialog;
 
         @Override
         protected void onPreExecute() {
             pDialog = new ProgressDialog(getView().getContext());
 
             pDialog.setTitle("Writing Profile");
             pDialog.setMessage("Please wait...");
             pDialog.setIndeterminate(true);
             pDialog.show();
 
             System.err.println("NOT EXECUTING");
         }
 
         @Override
         protected Boolean doInBackground(String... strings) {
             String profile = pName;
             File prf = new File(Utils.getProfilesPath(), profile + ".profile");
             Boolean result = false;
 
             String key = strings[0];
             String value = strings[1];
 
             Properties rProp = new Properties();
             Properties wProp = new Properties();
 
             if (!prf.exists()) {
                 try {
                     if (!prf.createNewFile()
                             || !prf.setReadable(true)
                             || !prf.setWritable(true)
                             || !prf.setExecutable(true)) {
                         result = false;
                     }
 
                     rProp.load(new FileInputStream(Utils.getDefaultProfile()));
                     wProp.load(new FileInputStream(prf));
 
                     for (Map.Entry map : rProp.entrySet()) {
                         wProp.setProperty(map.getKey().toString(), map.getValue().toString());
                     }
 
                     wProp.store(new FileOutputStream(prf), null);
                 } catch (Exception ignored) { }
             }
 
             try {
                 wProp.load(new FileInputStream(prf));
 
                 if (key.startsWith("slice")) {
                     key = "arm_slice_" + key + "_volt";
                 } else {
                     key = "CPU_VOLT_" + key;
                 }
 
                 wProp.setProperty(key, value);
 
                 wProp.store(new FileOutputStream(prf), null);
 
                 result = true;
             } catch (Exception ignored) { }
 
             return result;
         }
 
         @Override
         protected void onPostExecute(Boolean result) {
             pDialog.setMessage("Profile Writing Complete!");
             pDialog.cancel();
             pDialog.dismiss();
 
             Toast toast = Toast.makeText(DownloadTab.this.getView().getContext(), "", Toast.LENGTH_LONG);
 
             toast.setDuration(Toast.LENGTH_LONG);
 
             toast.setText("Profile Writing Not Successful!");
 
             if (result) {
                 toast.setText("Profile Writing Successful!");
             }
 
             toast.show();
         }
     }
 }
