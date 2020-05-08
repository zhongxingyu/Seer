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
 import android.text.Editable;
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
 import org.json.JSONObject;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 public class UploadTab extends Fragment implements View.OnClickListener {
     public ProgressDialog pDialog;
     public String pName = "";
     public ArrayList<String> lList;
     public ArrayList<String> rList;
 
     @Override
     public void onActivityCreated(Bundle savedInstanceState) {
         super.onActivityCreated(savedInstanceState);
 
         pDialog = new ProgressDialog(getView().getContext());
         lList = new ArrayList<String>();
 
         for (File file : Utils.getProfiles()) {
             String name = file.getName();
             if (name.startsWith(".")) {
                 continue;
             }
 
             if (name.endsWith(".profile")) {
                 lList.add(name);
             }
         }
 
         LinearLayout uploadDecline = (LinearLayout) getView().findViewById(R.id.layout_uc);
         uploadDecline.setVisibility(LinearLayout.GONE);
 
         final Button button = (Button) uploadDecline.findViewById(R.id.button_local_upload);
         button.setOnClickListener(this);
 
         final Button button2 = (Button) uploadDecline.findViewById(R.id.button_local_cancel);
         button2.setOnClickListener(this);
 
         ListView lv = (ListView) getView().findViewById(R.id.view_local_profiles);
 
         TextView vw = new TextView(getView().getContext());
 
         lv.addHeaderView(vw);
 
         ArrayAdapter<String> adapter = new ArrayAdapter<String>(getView().getContext(), android.R.layout.simple_list_item_1, lList);
         lv.setAdapter(adapter);
 
         lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> parent, final View view,
                                     int position, long id) {
                 LinearLayout uploadDecline = (LinearLayout) getView().findViewById(R.id.layout_uc);
 
                 pName = lList.get(position - 1);
 
                 uploadDecline.setVisibility(LinearLayout.VISIBLE);
             }
 
         });
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         return inflater.inflate(R.layout.upload_tab, container, false);
     }
 
     /*----------------------------
     ---------  onClick  ---------
     ----------------------------*/
 
     @Override
     public void onClick(View v) {
         LinearLayout uploadDecline = (LinearLayout) getView().findViewById(R.id.layout_uc);
 
         int id = v.getId();
 
         if (id == R.id.button_local_upload) {
             uploadDecline.setVisibility(LinearLayout.GONE);
 
             createConfirm(pName).show();
         } else if (id == R.id.button_local_cancel) {
             uploadDecline.setVisibility(LinearLayout.GONE);
         } /*else {
             uploadDecline.setVisibility(LinearLayout.VISIBLE);
         }*/
     }
 
     /*----------------------------
     ----------  Dialogs  ----------
     ----------------------------*/
 
     public AlertDialog createConfirm(final String profile) {
         AlertDialog.Builder builder = new AlertDialog.Builder(getView().getContext());
 
         String st = getString(R.string.upload_confirm).replace("@profile_id", profile.split("\\.")[0]);
 
         builder.setMessage(st)
                .setPositiveButton(R.string.button_upload, new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int id) {
                         AlertDialog d = createUpload();
 
                         dialog.dismiss();
                         d.show();
                     }})
                .setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int id) {
                         dialog.dismiss();
                     }});
 
         return builder.create();
     }
 
     public AlertDialog createUpload() {
         AlertDialog.Builder builder = new AlertDialog.Builder(getView().getContext());
 
         builder.setTitle(R.string.email_usage);
 
         LayoutInflater inflater = LayoutInflater.from(getView().getContext());
 
         final View input = inflater.inflate(R.layout.dialog_confirm, null);
 
         builder.setView(input)
                .setPositiveButton(R.string.button_upload, new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int id) {
                         EditText eName = (EditText) input.findViewById(R.id.dialog_name);
                         EditText eAsv = (EditText) input.findViewById(R.id.dialog_asv);
 
                         Editable name = eName.getText();
                         Editable asv = eAsv.getText();
 
                         Toast toast = Toast.makeText(UploadTab.this.getView().getContext(), "", Toast.LENGTH_LONG);
 
 
                         if (Utils.checkData(toast, name, asv)) {
                             uploadProfile(pName, name.toString(), asv.toString());
                             dialog.dismiss();
                         }
                     }
                 })
                .setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int id) {
                         dialog.dismiss();
                     }
                 });
 
         return builder.create();
     }
 
      /*----------------------------
     ---------  Send Data  ---------
     ----------------------------*/
 
     private JSONObject getJsonFromProfile(String profile) {
         File file = new File(Utils.getProfilesPath(), profile);
         JSONObject ob = new JSONObject();
 
         Properties prop = new Properties();
 
         try {
             prop.load(new FileInputStream(file));
 
             for (Map.Entry map : prop.entrySet()) {
                 String key = map.getKey().toString();
                 String value = map.getValue().toString();
 
                 if (key.startsWith("CPU_VOLT_")) {
                     try {
                         ob.put(key.replace("CPU_VOLT_", ""), value);
                     } catch (Exception ignored) {}
                 }
 
                 if (key.startsWith("arm_slice_")) {
                     try {
                         key = key.replace("arm_slice_", "").replace("_volt", "");
                         key = "slice" + key;
 
                         ob.put(key, value);
                     } catch (Exception ignored) { }
                 }
             }
         } catch (Exception ignored) { }
 
         return ob;
     }
 
     private Boolean uploadProfile(String profile, String name, String asv) {
         JSONObject ob = getJsonFromProfile(profile);
 
         if (ob.length() == 0) {
             return false;
         }
 
         try {
             ob.put("name", name);
             ob.put("asv", asv.replace("0", ""));
             ob.put("id", Utils.getEmail(getView().getContext()));
         } catch (Exception ignored) { }
 
         return upload(ob);
     }
 
     private Boolean upload(JSONObject json) {
         UploadTask task = new UploadTask();
 
         task.execute(json);
 
         return task.getResult();
     }
 
     /*----------------------------
     -----------  Tasks  -----------
     ----------------------------*/
 
     private class UploadTask extends AsyncTask<JSONObject, Void, Boolean> {
         private ProgressDialog pDialog;
         private Boolean r;
 
         @Override
         protected void onPreExecute() {
             pDialog = new ProgressDialog(getView().getContext());
 
             pDialog.setTitle("Uploading");
             pDialog.setMessage("Please wait...");
             pDialog.setIndeterminate(true);
             pDialog.show();
         }
 
         @Override
         protected Boolean doInBackground(JSONObject... jsons) {
             ConnectivityManager connMgr = (ConnectivityManager)
                     UploadTab.this.getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
 
             NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
 
             if (networkInfo != null && networkInfo.isConnected()) {
                 for (JSONObject json : jsons) {
                     try {
                         String id = json.getString("id");
 
                         if (id == null || id.isEmpty()) {
 
                             if (pDialog != null) {
                                 pDialog.setMessage("Cannot find Account Email!");
                                 pDialog.cancel();
                             }
                         }
                     } catch (Exception ignored) { }
 
                     DefaultHttpClient httpclient = new DefaultHttpClient();
 
                     try {
                         HttpPost httpPostRequest = new HttpPost("http://q0r.ca/abb/upload.php");
 
                         List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
 
                         nameValuePairs.add(new BasicNameValuePair("data", json.toString()));
 
                         httpPostRequest.setEntity(new UrlEncodedFormEntity(nameValuePairs));
 
                         HttpResponse response = httpclient.execute(httpPostRequest);
 
                         HttpEntity entity = response.getEntity();
 
                         return Boolean.valueOf(EntityUtils.toString(entity));
                     } catch (Exception e) {
                         e.printStackTrace();
                     } finally {
                         httpclient.getConnectionManager().shutdown();
                     }
                 }
             } else {
                 if (pDialog != null) {
                     pDialog.setMessage("No Internet Connection Found!");
                     pDialog.cancel();
                 }
 
                 return false;
             }
 
             return false;
         }
 
         @Override
         protected void onPostExecute(Boolean result) {
             if (!result) {
                 r = false;
                 return;
             }
 
             r =  true;
 
             if (pDialog != null) {
                 pDialog.setMessage("Upload Complete!");
                 pDialog.cancel();
             }
 
             Toast toast = Toast.makeText(UploadTab.this.getView().getContext(), "", Toast.LENGTH_LONG);
 
             toast.setDuration(Toast.LENGTH_LONG);
 
             toast.setText("Upload Not Successful!");
 
 
             if (result) {
                 toast.setText("Upload Successful!");
             }
 
             toast.show();
         }
 
         public Boolean getResult() {
             return r;
         }
     }
 }
