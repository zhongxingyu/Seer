 package me.sleepyti;
 
import java.util.Calendar;

 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 
 public class SleepyTimeActivity extends Activity {
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
         Button btnCalc = (Button)findViewById(R.id.button1);
         Button btnSleepNow = (Button)findViewById(R.id.button2);
       
 		btnCalc.setOnClickListener(calc);
 		btnSleepNow.setOnClickListener(calc);	
 			
 		}
     
     OnClickListener calc = new OnClickListener() {
 		
 		@Override
 		public void onClick(View v) {
 			// TODO Auto-generated method stub		
	
			
			Calendar cal = Calendar.getInstance();
			int hour = cal.get(Calendar.HOUR);
			int minute = cal.get(Calendar.MINUTE);
			int ampm = cal.get(Calendar.AM_PM);
					
 			
			String strAmPm = " AM";
			if(ampm == 1){
				strAmPm = " PM";
			}
	
 			
 			AlertDialog.Builder ad = new AlertDialog.Builder(SleepyTimeActivity.this);
 			ad.setTitle(R.string.alertboxtitle);
			ad.setMessage("Current time is " + twoDigit(hour) + ":" + twoDigit(minute) + strAmPm);
 			ad.show();
 			
 //			Intent i = new Intent(SleepyTimeActivity.this,Calculator.class);
 //			startActivity(i);		
 			
 		}
		
		public String twoDigit(Integer num){
			String twodigitnumber = num.toString();
		
			if(twodigitnumber.length()<2){
				twodigitnumber = "0"+twodigitnumber;
			}
			return twodigitnumber;				
		} 
		
 	};
         
     }
     
 
 
 
