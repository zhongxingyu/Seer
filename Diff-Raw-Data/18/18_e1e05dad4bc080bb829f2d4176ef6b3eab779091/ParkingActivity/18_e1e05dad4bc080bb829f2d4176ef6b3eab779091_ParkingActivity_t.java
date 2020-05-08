 package com.ssasha.parking;
 
 import android.app.Activity;
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Toast;
 
 public class ParkingActivity extends Activity {
 	
 	protected PrefsEditor prefs;
 
 	/** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
     }
     
     public void onButtonClick(View view) {
     	Intent intent;
     
     	switch (view.getId()) {
     	case R.id.park_car_btn :
     		intent = new Intent(this, WheresmycarActivity.class);
     		startActivity(intent);
     		break;
     	case R.id.where_btn :
     		intent = new Intent(this, RetrieveActivity.class);
     		startActivity(intent);
     		break;
     	case R.id.forget_btn :
     		clearPrefs();
     		break;
     	}
     }
     
     //forget location of car and cleaning time
     //prefs stuff should be factored out
     private void clearPrefs() {
     	if (prefs == null)
     		prefs = new PrefsEditor();
     	prefs.clearPrefs(this);
     	//clear alarm
 		Intent intent = new Intent(this, ReminderReceiver.class);
 		AlarmManager am = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
 		PendingIntent pi = PendingIntent.getBroadcast(this, 1, intent, 0);
 		am.cancel(pi);
     	Toast.makeText(getApplicationContext(), "Car Reminder Cleared", Toast.LENGTH_SHORT).show();
     }
 }
