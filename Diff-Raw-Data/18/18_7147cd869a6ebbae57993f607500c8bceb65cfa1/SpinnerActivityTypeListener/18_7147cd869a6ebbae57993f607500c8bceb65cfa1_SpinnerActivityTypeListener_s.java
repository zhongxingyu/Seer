 package com.activitymonitor.listeners;
 
 import com.activitymonitor.preferences.Preferences;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.Toast;
 
 public class SpinnerActivityTypeListener implements OnItemSelectedListener {
 
 	@Override
 	public void onItemSelected(AdapterView<?> parent, View view, int pos,
 			long id) {
 		SharedPreferences settings = parent.getContext().getSharedPreferences(Preferences.PREFS_NAME, Context.MODE_PRIVATE);
 		SharedPreferences.Editor editor = settings.edit();
 
 		String activityType = parent.getItemAtPosition(pos).toString();
 
 		if(activityType.equals("Sit To Stand") ){
 			editor.putString(Preferences.CURRENT_TYPE, Preferences.SIT_TO_STAND);
 			editor.commit();
 		}
 		else if(activityType.equals("Stand to Sit") ){
 			editor.putString(Preferences.CURRENT_TYPE, Preferences.STAND_TO_SIT);
 			editor.commit();
 		}
 		else if(activityType.equals("S-T-S") ){
 			editor.putString(Preferences.CURRENT_TYPE, Preferences.STS);
 			editor.commit();
 		}
 		else if(activityType.equals("S-W-S") ){
 			editor.putString(Preferences.CURRENT_TYPE, Preferences.SWS);
 			editor.commit();
 		}
 		Toast.makeText(parent.getContext(), 
				"OnItemSelectedListener : " + parent.getItemAtPosition(pos).toString(),
 				Toast.LENGTH_SHORT).show();
 		
 	}
 
 	@Override
 	public void onNothingSelected(AdapterView<?> arg0) {
 		// TODO Auto-generated method stub
 
 	}
 
 }
