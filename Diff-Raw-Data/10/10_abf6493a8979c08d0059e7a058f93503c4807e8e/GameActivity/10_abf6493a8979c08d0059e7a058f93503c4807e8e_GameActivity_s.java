 package com.moonbc.numbercalc;
 
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.os.Handler;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class GameActivity extends Activity {
 	int value =0;
 	TextView mTimeCount;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
		
		View customView = getLayoutInflater().inflate(R.layout.main_game, null);
 		setContentView(R.layout.main_game);
 		
		mTimeCount = (TextView)customView.findViewById(R.id.time_counter_text);
 		mHandler.sendEmptyMessage(0);
 		
 	
 	}
 	Handler mHandler = new Handler() {
 		public void handleMessage(android.os.Message msg) {
 			value++;
			mTimeCount.setText(value);
 			mHandler.sendEmptyMessageDelayed(0, 1000);
 		}
 	};
 	@Override
 	public void onBackPressed() {
 		// TODO Auto-generated method stub
 		Toast.makeText(getApplicationContext(), "back key", Toast.LENGTH_SHORT).show();
 		super.onBackPressed();
 	}
 }
