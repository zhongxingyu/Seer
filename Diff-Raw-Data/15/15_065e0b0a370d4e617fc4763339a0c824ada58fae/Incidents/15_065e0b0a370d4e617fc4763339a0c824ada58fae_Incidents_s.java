 package com.example.Second;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import org.apache.http.NameValuePair;
 import org.apache.http.message.BasicNameValuePair;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class Incidents extends Activity {
     private LocationWrapper locationWrapper;
    private long user_id;
 
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.incidents);
         LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
         locationWrapper = new LocationWrapper(locationManager);
        user_id = getSharedPreferences(getString(R.string.user_information), Context.MODE_PRIVATE).getLong(getString(R.string.current_user_id), 0);
     }
 
     public void btn_break_in(View view) {
         Log.w("Break In", locationWrapper.getLocation());
         List<NameValuePair> nameValuePairs = build_incident_infor();
         Response response = RequestWrapper.post(getString(R.string.incidents_url), nameValuePairs);
         Log.w("BI_Reponse_status_code", response.getHttp_status_code());
         Log.w("BI_Reponse_content", response.getContent());
         showResult(response);
     }
 
     private void showResult(Response response) {
         if(response.getHttp_status_code().equals("HTTP/1.1 201 Created")){
             JSONObject jsonObject = null;
             try {
                 jsonObject = new JSONObject(response.getContent());
                 String reference = jsonObject.getString("reference");
                 display("Successful", "Reference ID is:" + reference);
             } catch (JSONException e) {
                 Log.e("Break In", e.toString());
             }
         }else {
             display("Failed", "Please check your network");
         }
     }
 
     private void display(String title, String message) {
         AlertDialog alertDialog = new AlertDialog.Builder(this).create();
         alertDialog.setTitle(title);
         alertDialog.setMessage(message);
         alertDialog.setButton("OK", (DialogInterface.OnClickListener) null);
         alertDialog.show();
     }
 
     private List<NameValuePair> build_incident_infor() {
         String location_street = locationWrapper.getLocation();
         double latitude = locationWrapper.getLatitude();
         double longitude = locationWrapper.getLongitude();
 
         List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
         nameValuePairs.add(new BasicNameValuePair("[incident][category]", "House Break in"));
        nameValuePairs.add(new BasicNameValuePair("[incident][user_id]", Long.toString(user_id)));
         nameValuePairs.add(new BasicNameValuePair("[incident][location_attributes][latitude]", Double.toString(latitude)));
         nameValuePairs.add(new BasicNameValuePair("[incident][location_attributes][longitude]", Double.toString(longitude)));
         nameValuePairs.add(new BasicNameValuePair("[incident][location_attributes][street]", location_street));
         return nameValuePairs;
     }
 
 
     @Override
     public void onPause(){
         super.onPause();
         locationWrapper.stopLocation();
     }
 
     @Override
     public void onResume() {
         super.onResume();
         locationWrapper.startLocation();
     }
 }
