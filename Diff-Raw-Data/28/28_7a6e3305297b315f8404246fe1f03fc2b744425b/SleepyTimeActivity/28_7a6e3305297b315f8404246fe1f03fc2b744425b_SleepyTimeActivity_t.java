 package me.sleepyti;
 
 import android.app.Activity;
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
			Intent i = new Intent(SleepyTimeActivity.this,Calculator.class);
			startActivity(i);		
 			
 		}
 	};
         
     }
     
 
 
 
