 package ca.q0r.kfreqs.app.tabs;
 
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
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
 
 import java.io.*;
 import java.util.*;
 
 public class DownloadTab extends Fragment implements View.OnClickListener {
     public String pName = "";
     ArrayList<String> list;
     HashMap<String, String> map;
 
     @Override
     public void onActivityCreated(Bundle savedInstanceState) {
         super.onActivityCreated(savedInstanceState);
 
         list = new ArrayList<String>();
         map = new HashMap<String, String>();
 
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

            createConfirm();
         } else if (id == R.id.button_remote_cancel) {
             uploadDecline.setVisibility(LinearLayout.GONE);
         } /*else {
             uploadDecline.setVisibility(LinearLayout.VISIBLE);
         }*/
     }
 
     /*----------------------------
     ----------  Dialogs  ----------
     ----------------------------*/
 
    public AlertDialog createConfirm() {
         AlertDialog.Builder builder = new AlertDialog.Builder(getView().getContext());
 
        String st = getString(R.string.download_confirm).replace("@profile_id", pName).replace("@asv", map.get("asv"));
 
         builder.setMessage(st)
                 .setPositiveButton(R.string.button_download, new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int id) {
                         WriteTask wTask = new WriteTask();
                         wTask.execute("");
                     }})
                 .setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int id) {
                         dialog.dismiss();
                     }});
 
         return builder.create();
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
 
                         map.put(key, value);
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
         }
 
         @Override
         protected Boolean doInBackground(String... strings) {
             String profile = pName;
             File prf = new File(Utils.getProfilesPath(), profile + "_r.profile");
             Boolean result = false;
 
             //Properties rProp = new Properties();
             LinkedProperties wProp = new LinkedProperties();
 
             if (!prf.exists()) {
                 try {
                     if (!prf.createNewFile()
                             || !prf.setReadable(true, false)
                             || !prf.setWritable(true, false)
                             || !prf.setExecutable(true, false)) {
                         result = false;
                     }
 
                     /*rProp.load(new FileInputStream(Utils.getDefaultProfile()));
                     wProp.load(new FileInputStream(prf));
 
                     wProp.putAll(rProp);
 
                     wProp.store(new FileOutputStream(prf), null);*/
 
                     InputStream inStream = new FileInputStream(Utils.getDefaultProfile());
                     OutputStream outStream = new FileOutputStream(prf);
 
                     byte[] buffer = new byte[1024];
 
                     int length;
                     while ((length = inStream.read(buffer)) > 0){
                         outStream.write(buffer, 0, length);
                     }
 
                     inStream.close();
                     outStream.close();
                 } catch (Exception ignored) { }
             }
 
             try {
                 wProp.load(new FileInputStream(prf));
 
                 for (Map.Entry<String, String> entry : DownloadTab.this.map.entrySet()) {
                     String key = entry.getKey();
                     String value = entry.getValue();
 
                     if (key.startsWith("slice")) {
                         key = "arm_slice_" + key.replace("slice", "") + "_volt";
 
                         Integer i = Integer.parseInt(value) / 1000;
 
                         wProp.put(key, i.toString());
                     } else if (key.contains("0")) {
                         key = "CPU_VOLT_" + key;
 
                         wProp.put(key, value);
                     }
 
                 }
 
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
 
     private class LinkedProperties extends Properties {
         private LinkedHashMap<Object, Object> entries = new LinkedHashMap<Object, Object>();
 
         public Enumeration<Object> keys() { return Collections.enumeration(entries.keySet()); }
 
         public Enumeration<Object> elements() { return Collections.enumeration(entries.values()); }
 
         public boolean contains(Object value) { return entries.containsValue(value); }
 
         public void putAll(Map<?, ?> map) {
             entries.putAll(map);
         }
 
         public int size() { return entries.size(); }
 
         public boolean isEmpty() { return entries.isEmpty(); }
 
         public boolean containsKey(Object key) { return entries.containsKey(key); }
 
         public boolean containsValue(Object value) {        return entries.containsValue(value); }
 
         public Object get(Object key) { return entries.get(key); }
 
         public Object put(Object key, Object value) { return entries.put(key, value); }
 
         public Object remove(Object key) { return entries.remove(key); }
 
         public void clear() { entries.clear(); }
 
         public Set<Object> keySet() { return entries.keySet(); }
 
         public Collection<Object> values() { return entries.values(); }
 
         public Set<Entry<Object, Object>> entrySet() { return entries.entrySet(); }
 
         public boolean equals(Object o) {
             return o instanceof Entry && entries.equals(o);
         }
 
         public int hashCode() { return entries.hashCode(); }
     }
 }
