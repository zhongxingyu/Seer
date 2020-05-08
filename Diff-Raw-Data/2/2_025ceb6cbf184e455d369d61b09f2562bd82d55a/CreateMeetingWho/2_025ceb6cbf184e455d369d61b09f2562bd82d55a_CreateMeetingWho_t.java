 package com.uiproject.meetingplanner;
 
 import java.util.ArrayList;
 
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 
 public class CreateMeetingWho extends Search {
 
 	public static final String PREFERENCE_FILENAME = "MeetAppPrefs";
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.createmeetingwho);
     	SharedPreferences settings = getSharedPreferences(PREFERENCE_FILENAME, MODE_PRIVATE); 
     	String uids = settings.getString("mattendeeids", "");
         
         init();
         
         ArrayList<Integer> checkedUids = new ArrayList<Integer>();
         int commaIndex;
         String u = "";
     	while (uids.length() > 0){
     		commaIndex = uids.indexOf(',');
     		if (commaIndex == -1){
     			checkedUids.add(Integer.parseInt(uids));
     			break;
     		}else{
 	    		u = uids.substring(0, commaIndex);
 	    		checkedUids.add(Integer.parseInt(u));
	    		uids = uids.substring(commaIndex + 1);
     		}
 		}
     	for (int i = 0; i < checkedUids.size(); i++)
     		Log.d("CMW uids", checkedUids.get(i).toString());
     	//recheck();	
     	
     }
 
     public void back(View button){
     	onBackPressed();
     }
 
     public void cancel(View button){
     	clearData();
     	CreateMeetingWho.this.setResult(R.string.cancel_create);
     	CreateMeetingWho.this.finish();
 		//need to clear the previous activities too
     }
     
     private void clearData(){ 
 
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
     	editor.remove("mattendeeids");
     	editor.commit();
 
     	
     }
     
     public void next(View button){
     	saveData();
 		Intent intent = new Intent(CreateMeetingWho.this, CreateMeetingWhen.class);
 		CreateMeetingWho.this.startActivityForResult(intent, 0);
     	
     }
 
     @Override
     public void onBackPressed(){
     	saveData();
     	CreateMeetingWho.this.finish();
     	
     }
     
     private void saveData(){
     	String names = "";
     	String uids = "";
     	boolean added = false;
     	for (UserInstance u : checkedUsers){
     		if (added){
     			names += ", ";
     			uids += ",";
     		}
     		names = names + u.getUserFirstName() + " " + u.getUserLastName();
     		uids += u.getUserID();
     		added = true;
     	}
     	Log.d("CMW", names);
     	Log.d("CMW", uids);
     	//save data in shared preferences
     	SharedPreferences settings = getSharedPreferences(PREFERENCE_FILENAME, MODE_PRIVATE); 
     	SharedPreferences.Editor editor = settings.edit();
     	editor.putString("mnames", names);
     	editor.putString("mattendeeids", uids);
     	editor.commit();
     	
     }
     
 
     
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         super.onActivityResult(requestCode, resultCode, data);
         if (resultCode == R.string.cancel_create) {
             this.setResult(R.string.cancel_create);
             this.finish();
         }else if (resultCode == R.string.meeting_created) {
             this.setResult(R.string.meeting_created);
             this.finish();
         }
     }
     
 
 
 	 // menu 
 	    @Override
 	    public boolean onCreateOptionsMenu(Menu menu) {
 	        MenuInflater inflater = getMenuInflater();
 	        inflater.inflate(R.menu.logoutonly, menu);
 	        return true;
 	    }
 	    
 	    @Override
 	    public boolean onOptionsItemSelected(MenuItem item) {
 	        switch (item.getItemId()) {
 	            case R.id.logout:{
 	            	clearData();
 	            	Logout.logout(this);
 	            	break;
 	            }
 	        }
 	        return true;
 	    }
 }
