 package com.example.Second;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.EditText;
 import org.apache.http.NameValuePair;
 import org.apache.http.message.BasicNameValuePair;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class MyActivity extends Activity {
     private SharedPreferences sharedPref;
 
     /**
      * Called when the activity is first created.
      */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         sharedPref = getSharedPreferences(getString(R.string.user_information), Context.MODE_PRIVATE);
         long user_id = sharedPref.getLong(getString(R.string.current_user_id), 0);
         if(user_id > 0){
             startActivity(new Intent(MyActivity.this, MainMenu.class));
         }else {
             setContentView(R.layout.main);
         }
 
     }
 
    public void registerSubmit(View view){
        new AlertDialog.Builder(this)
                .setTitle("Confirm Submit")
                .setMessage("Are you sure you want to submit?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        List<NameValuePair> user_info = build_user_information();
                       Response response = RequestWrapper.post(getString(R.string.mobile_user_url), user_info);
                        Log.w("response", response.getHttp_status_code());
                        if(response.getHttp_status_code().equals("HTTP/1.1 201 Created")){
                            save_user(response);
                            startActivity(new Intent(MyActivity.this, MainMenu.class));
                        }
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                }).show();
    }
 
     private void save_user(Response response) {
         JSONObject jObject;
         try {
             jObject = new JSONObject(response.getContent());
             long user_id = jObject.getLong("id");
             SharedPreferences.Editor editor = sharedPref.edit();
             editor.putLong(getString(R.string.current_user_id), user_id);
             editor.commit();
 
         } catch (JSONException e) {
             Log.e("CreateUserError", e.toString());
         }
     }
 
     private List<NameValuePair> build_user_information() {
         List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
         nameValuePairs.add(new  BasicNameValuePair("[user][alternative_contact]", getValueById(R.id.alternative_mobile)));
         nameValuePairs.add(new BasicNameValuePair("[user][mobile_contact]", getValueById(R.id.mobile_contact)));
         nameValuePairs.add(new BasicNameValuePair("[user][mobile_of_kin]", getValueById(R.id.mobile_of_kin)));
         nameValuePairs.add(new BasicNameValuePair("[user][name]", getValueById(R.id.name)));
         nameValuePairs.add(new BasicNameValuePair("[user][next_of_kin]", getValueById(R.id.next_of_kin)));
         nameValuePairs.add(new BasicNameValuePair("[user][physical_address]", getValueById(R.id.physic_address)));
         nameValuePairs.add(new BasicNameValuePair("[user][surname]", getValueById(R.id.surname)));
         return  nameValuePairs;
     }
 
     private String getValueById(int id){
         return ((EditText)findViewById(id)).getText().toString();
     }
 
 
 }
