 package com.jason.ocbcapp;
 
 import java.io.BufferedInputStream;
 import java.io.InputStream;
 import java.io.OutputStreamWriter;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Scanner;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.app.ActionBar;
 import android.app.AlertDialog;
 import android.app.AlertDialog.Builder;
 import android.app.Dialog;
 import android.app.ListActivity;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.util.Log;
 import android.util.Pair;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.ProgressBar;
import android.widget.Toast;
 
 import com.jason.ocbcapp.Appointment;
 
 public class MyAppointmentsActivity extends ListActivity {
 
     private String APP_TAG = MainActivity.APP_TAG;
 
     ArrayList<Appointment> appointments = null;
     ArrayList<String> appointmentBranches = new ArrayList<String>();
     ProgressBar pb;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_my_appointments);
 
         // Show the up button at the icon
         ActionBar actionBar = getActionBar();
         actionBar.setDisplayHomeAsUpEnabled(true);
 
         pb = (ProgressBar) findViewById(R.id.apptProgressBar);
         pb.setVisibility(View.VISIBLE);
 
         initializeAppointmentsList();
     }
 
     private void initializeAppointmentsList() {
         String url = "http://cutebalrog.com:8080/OCBC-QM-Server-web/webresources/Customer/getAppointment";
         String userToken = getUserTokenFromPreferences();
         JSONObject jobj = new JSONObject();
         try {
             jobj.accumulate("tokenOfCustomer", userToken);
         } catch (JSONException e) {
             e.printStackTrace();
         }
         GetAppointmentsTask task = new GetAppointmentsTask();
         task.execute(new Pair(url, jobj));
     }
 
     private String getUserTokenFromPreferences() {
         // TODO Auto-generated method stub
         SharedPreferences settings = getSharedPreferences(
                 MainActivity.PREFS_NAME, 0);
         String userToken = settings.getString("userToken", "");
         return userToken;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
         case android.R.id.home:
             // app icon clicked; go home
             goBackToMainActivity();
             return true;
         default:
             return super.onOptionsItemSelected(item);
         }
     }
 
     /**
      * Creates the MainActivity intent to go back to it.
      */
     private void goBackToMainActivity() {
         Intent intent = new Intent(this, MainActivity.class);
         intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
         startActivity(intent);
     }
 
     /**
      * 
      */
     private void initializeAppointmentAdapter() {
         AppointmentAdapter adapter = new AppointmentAdapter(this, appointments);
         this.setListAdapter(adapter);
         // hide progress bar
         pb.setVisibility(View.INVISIBLE);
     }
 
     class GetAppointmentsTask extends
             AsyncTask<Pair<String, JSONObject>, String, String> {
 
         private int responseCode = 0;
 
         @Override
         protected String doInBackground(Pair<String, JSONObject>... pairs) {
             String responseString = null;
             InputStream responseStream = null;
             HttpURLConnection urlConnection = null;
             OutputStreamWriter writer = null;
             try {
                 Pair<String, JSONObject> pair = pairs[0];
                 URL url = new URL(pair.first);
                 JSONObject data = pair.second;
                 urlConnection = (HttpURLConnection) url.openConnection();
                 urlConnection.setDoOutput(true);
                 urlConnection.setRequestProperty("Accept", "application/json");
                 urlConnection.addRequestProperty("Content-type",
                         "application/json");
                 urlConnection.setRequestMethod("POST");
                 urlConnection.connect();
 
                 writer = new OutputStreamWriter(urlConnection.getOutputStream());
                 writer.write(data.toString());
                 writer.close();
 
                 responseStream = new BufferedInputStream(urlConnection
                         .getInputStream());
                 responseCode = urlConnection.getResponseCode();
                 Log.d(APP_TAG, "response code: " + responseCode);
                 responseString = readStream(responseStream);
             } catch (Exception e) {
                 Log.e(APP_TAG, e.getMessage());
             }
             return responseString;
         }
 
         private String readStream(InputStream inputStream) {
             StringBuilder buf = new StringBuilder();
             Scanner sc = null;
             try {
 
                 sc = new Scanner(inputStream);
                 while (sc.hasNext()) {
                     buf.append(sc.next());
                 }
 
             } catch (Exception e) {
                 Log.e(APP_TAG, e.getMessage());
             }
             return buf.toString();
         }
 
         @Override
         protected void onPostExecute(String result) {
             super.onPostExecute(result);
             // Extract waiting time from json object
             Log.d(APP_TAG, "Finished get appointment task");
             Log.d(APP_TAG, "result: " + result);
             Boolean isSuccessful = responseCode == HttpURLConnection.HTTP_OK;
             if (isSuccessful) {
                 try {
                     JSONObject jobj = new JSONObject(result);
                     int objectResponseCode = jobj.getInt("response");
                     Boolean noAppointment = objectResponseCode == -2;
                     if (noAppointment) {
                         showNoAppointmentsDialog();
                     } else {
                         JSONArray appointmentsJArr = jobj.getJSONArray("appointments");
                         Log.d(APP_TAG, appointmentsJArr.toString());
                         appointments = convertJSONArrayToAppoinments(appointmentsJArr);
                         initializeAppointmentAdapter();
                     }
                 } catch (JSONException e) {
                     e.printStackTrace();
                 }
            } else {
                showUnableToConnectToast();
             }
         }
 
         private ArrayList<Appointment> convertJSONArrayToAppoinments(
                 JSONArray appointmentsJArr) {
             ArrayList<Appointment> result = new ArrayList<Appointment>();
             for (int i = 0; i < appointmentsJArr.length(); i ++) {
                 long appointmentDateTime = 0;
                 int branchId = 0;
                 int queueNumber = 0;
                 try {
                     JSONObject jsonObj = (JSONObject) appointmentsJArr.get(i);
                     appointmentDateTime = jsonObj.getLong("appDate");
                     branchId = jsonObj.getJSONObject("branch").getInt("branchId");
                     queueNumber = jsonObj.getInt("queueNo");
                 } catch (JSONException e) {
                     e.printStackTrace();
                 }
                 Appointment appt = new Appointment(appointmentDateTime, branchId, queueNumber);
                 result.add(appt);
             }
             return result;
         }
     }
 
     public void showNoAppointmentsDialog() {
         Builder builder = new Builder(getApplicationContext());
         builder.setTitle("No appointments").setMessage(
                 "You have not made any appointments, why not make one now?")
                 .setPositiveButton("Okay", new OnClickListener() {
 
                     @Override
                     public void onClick(DialogInterface dialog, int which) {
                         dialog.dismiss();
                         goBackToMainActivity();
                     }
                 });
     }

    /**
     * 
     */
    private void showUnableToConnectToast() {
        Toast t = new Toast(getApplicationContext());
        t.setText("Unable to connect to server");
        t.show();
    }
 }
