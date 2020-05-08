 package com.uiproject.meetingplanner;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.Toast;
 
 public class CreateMeetingWhat extends Activity {
 	
 	EditText title, desc;
 	public static final String PREFERENCE_FILENAME = "MeetAppPrefs";
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.createmeetingwhat);
         
         clearData();
         title = (EditText) findViewById(R.id.title);
         desc = (EditText) findViewById(R.id.desc);
     }
     
     
     public void cancel(View button){
     	onBackPressed();
     }
     
     @Override
     public void onBackPressed(){
     	clearData();
     	CreateMeetingWhat.this.finish();
     	
     }
     
     public void clearData(){
 
     	SharedPreferences settings = getSharedPreferences(PREFERENCE_FILENAME, MODE_PRIVATE); 
     	SharedPreferences.Editor editor = settings.edit();
     	editor.remove("mtitle");
     	editor.remove("mdesc");
     	editor.remove("mdatem");
     	editor.remove("mdated");
     	editor.remove("mdatey");
     	editor.remove("mstarth");
     	editor.remove("mstartm");
     	editor.remove("mendh");
     	editor.remove("mendm");
     	editor.remove("mtracktime");
     	editor.remove("maddr");
     	editor.remove("mlat");
     	editor.remove("mlon");
     	editor.remove("mnames");
     	editor.remove("mphones");
     	editor.commit();
     }
     
     public void next(View button){
     	
     	String mtitle = title.getText().toString();
     	
     	if (mtitle.length() == 0){
             Toast.makeText(getBaseContext(), "Please specify meeting title", Toast.LENGTH_SHORT).show();
             return;
     	}
     	
     	String mdesc = desc.getText().toString();
     	
     	if (mdesc.length() == 0){
     		mdesc = null;
     	}
     	
     	SharedPreferences settings = getSharedPreferences(PREFERENCE_FILENAME, MODE_PRIVATE); 
     	SharedPreferences.Editor editor = settings.edit();
     	editor.putString("mtitle", mtitle);
     	editor.putString("mdesc", mdesc);
     	editor.commit();
     	
 		Intent intent = new Intent(CreateMeetingWhat.this, CreateMeetingWhen.class);
 		CreateMeetingWhat.this.startActivityForResult(intent, 0);
     	
     }
     
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         super.onActivityResult(requestCode, resultCode, data);
         if (resultCode == R.string.cancel_create) {
             this.finish();
         }else if (resultCode == R.string.meeting_created) {
    		Intent intent = new Intent(CreateMeetingWhat.this, MeetingList.class);
     		CreateMeetingWhat.this.startActivity(intent);
             this.finish();
         }
     }
 }
